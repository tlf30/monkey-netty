/*
 * MIT License
 *
 * Copyright (c) 2021 Trevor Flynn
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.tlf.monkeynetty.server;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.tlf.monkeynetty.*;
import io.tlf.monkeynetty.msg.ConnectionEstablishedMessage;
import io.tlf.monkeynetty.msg.NetworkMessage;
import io.tlf.monkeynetty.msg.PingMessage;
import io.tlf.monkeynetty.msg.UdpConHashMessage;

import java.io.File;
import java.security.SecureRandom;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NettyServer extends BaseAppState implements NetworkServer {

    private final static Logger LOGGER = Logger.getLogger(NettyServer.class.getName());
    private final Set<MessageListener> messageListeners = Collections.synchronizedSet(new HashSet<>());
    private final Set<ConnectionListener> connectionListeners = Collections.synchronizedSet(new HashSet<>());
    private final Map<Channel, NettyConnection> tcpClients = Collections.synchronizedMap(new HashMap<>());
    private final Map<Channel, NettyConnection> udpClients = Collections.synchronizedMap(new HashMap<>());
    private final Map<String, NettyConnection> secrets = Collections.synchronizedMap(new HashMap<>());
    private final Set<NetworkClient> pendingConnections = Collections.synchronizedSet(new HashSet<>());

    private int maxConnections = 10;
    private boolean blocking = false;
    private LogLevel logLevel;

    //Netty objects
    private EventLoopGroup tcpConGroup;
    private EventLoopGroup tcpMsgGroup;
    private ServerBootstrap tcpServer;
    private ChannelFuture tcpFuture;
    private EventLoopGroup udpConGroup;
    private EventLoopGroup udpMsgGroup;
    private ServerBootstrap udpServer;
    private ChannelFuture udpFuture;
    private SslContext sslContext;

    private final String service;
    private final int port;
    private boolean ssl;
    private boolean selfGenCert;
    private File cert;
    private File key;

    /**
     * Create a new UDP/TCP server.
     * The server will be created without SSL
     *
     * @param service The name of the service the server is running
     * @param port The port the TCP/UDP server will listen on
     */
    public NettyServer(String service, int port) {
        this(service, false, port);
    }

    /**
     * Create a new UDP/TCP server.
     * If ssl is enabled, the TCP server will generate a self signed certificate and use ssl.
     *
     * @param service The name of the service the server is running
     * @param ssl If ssl should be used on the TCP server
     * @param port The port the TCP/UDP server will listen on
     */
    public NettyServer(String service, boolean ssl, int port) {
        this(service, ssl, true, null, null, port);
    }

    /**
     * Create a new UDP/TCP server.
     * If ssl is enabled, and a certificate key pair are provided, the TCP server will use ssl.
     * If the server failes to load the cert key pair, or they are null, it will fail back to non-ssl.
     *
     * @param service The name of the service the server is running
     * @param ssl If ssl should be used on the TCP server
     * @param cert The certificate file, or null
     * @param key The certificate key, or null
     * @param port The port the TCP/UDP server will listen on
     */
    public NettyServer(String service, boolean ssl, File cert, File key, int port) {
        this(service, ssl, false, cert, key, port);
    }

    /**
     * Create a new UDP/TCP server.
     * If ssl is enabled, and a certificate key pair are provided, the TCP server will use ssl.
     * If the server failes to load the cert key pair, or they are null, it will fail back to
     * a self signed certificate if enabled, otherwise will fail back to non-ssl.
     *
     * @param service The name of the service the server is running
     * @param ssl If ssl should be used on the TCP server
     * @param selfGenCert If a self signed certificate can be used.
     * @param cert The certificate file, or null to use self signed cert
     * @param key The certificate key, or null to use self signed cert
     * @param port The port the TCP/UDP server will listen on
     */
    private NettyServer(String service, boolean ssl, boolean selfGenCert, File cert, File key, int port) {
        this.service = service;
        this.port = port;
        this.ssl = ssl;
        this.cert = cert;
        this.key = key;
        this.selfGenCert = selfGenCert;
    }

    @Override
    protected void initialize(Application app) {

    }

    @Override
    protected void cleanup(Application app) {

    }

    @Override
    public void onEnable() {
        LOGGER.log(Level.INFO, "Loading Netty.IO Server {0} on port {1,number,#}", new Object[]{getService(), getPort()});
        setupTcp();
        setupUdp();
        LOGGER.log(Level.INFO, "Server {0} running on port {1,number,#}", new Object[]{getService(), getPort()});
    }

    @Override
    public void onDisable() {
        LOGGER.log(Level.INFO, "Unloading Netty.IO Server {0} on port {1,number,#}", new Object[]{getService(), getPort()});

        try {
            tcpConGroup.shutdownGracefully();
            tcpMsgGroup.shutdownGracefully();
            tcpFuture.channel().closeFuture().sync();
            udpConGroup.shutdownGracefully();
            udpMsgGroup.shutdownGracefully();
            ((UdpServerChannel) udpFuture.channel()).doClose();
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Failed to stop server", ex);
        }

        LOGGER.log(Level.INFO, "Server {0} stopped on port {1,number,#}", new Object[]{getService(), getPort()});
    }

    @Override
    public int getConnections() {
        return tcpClients.size();
    }

    @Override
    public boolean isBlocking() {
        return blocking;
    }

    @Override
    public void setBlocking(boolean blocking) {
        this.blocking = blocking;
    }

    @Override
    public int getMaxConnections() {
        return maxConnections;
    }

    @Override
    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }

    /**
     * Internal use only
     * Process an incoming client connection.
     * Will handle max connections and blocking mode.
     * Will fire connection listeners.
     * @param client The client making the connection
     */
    private void receive(NetworkClient client) {
        if (isBlocking() || getConnections() >= getMaxConnections() || !(client instanceof NettyConnection)) {
            client.disconnect();
            LOGGER.log(Level.INFO, "Server rejected connection from {0}", client.getAddress());
        } else {
            if (udpClients.containsValue(client)) {
                //Run listeners
                ((NettyConnection) client).connect();
                LOGGER.log(Level.INFO, "Connection received from {0}", client.getAddress());
                try {
                    for (ConnectionListener listener : connectionListeners) {
                        listener.onConnect(client);
                    }
                    client.send(new ConnectionEstablishedMessage());
                    pendingConnections.remove(client);
                } catch (Exception ex) {
                    LOGGER.log(Level.WARNING, "Exception thrown running connection listeners", ex);
                }
            } else {
                //We don't have the client on udp yet
                //Send them the hand-shake
                String hash = getUdpHash(128);
                secrets.put(hash, (NettyConnection) client);
                UdpConHashMessage str = new UdpConHashMessage(hash, true);
                client.send(str);
            }
        }
    }

    /**
     * Internal use only
     * Process an incoming message from a client.
     * Will notify message listeners.
     *
     * @param client The client the message was from
     * @param message The message sent
     */
    private void receive(NetworkClient client, NetworkMessage message) {
        client.receive(message);
        for (MessageListener handler : messageListeners) {
            for (Class<? extends NetworkMessage> a : handler.getSupportedMessages()) {
                if (a.isInstance(message)) {
                    try {
                        handler.onMessage(message, this, client);
                    } catch (Exception ex) {
                        LOGGER.log(Level.SEVERE, "Message handler failed to handle message", ex);
                    }
                }
            }
        }
    }

    @Override
    public void send(NetworkMessage message) {
        Collection<NettyConnection> cs = tcpClients.values();
        cs.forEach(c -> c.send(message));
    }

    @Override
    public void send(NetworkMessage message, NetworkClient client) {
        client.send(message);
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public String getService() {
        return service;
    }

    @Override
    public boolean isSsl() {
        return ssl;
    }

    @Override
    public NetworkProtocol[] getProtocol() {
        return new NetworkProtocol[]{NetworkProtocol.UDP, NetworkProtocol.TCP};
    }

    /**
     * Sets the Netty.IO internal log level.
     * This will not change the <code>java.util.logger</code> Logger for Monkey-Netty.
     * @param logLevel The internal Netty.IO log level
     */
    public void setLogLevel(LogLevel logLevel) {
        this.logLevel = logLevel;
    }

    /**
     * @return The internal Netty.IO log level
     */
    public LogLevel getLogLevel() {
        return logLevel;
    }

    /**
     * Internal use only
     * Setup the TCP netty.io server pipeline.
     * This will create a dedicated TCP channel for each client.
     * The pipeline is setup to handle <code>NetworkMessage</code> message types.
     * The pipeline will also send/receive a ping to/from the client to ensure the connection is still active.
     * If the connection becomes inactive, the server will disconnect the client.
     * The pipeline will be configured with SSL if SSL parameters have been passed to the server.
     */
    private void setupTcp() {
        //Setup ssl
        if (ssl) {
            try {
                if (selfGenCert) {
                    LOGGER.log(Level.WARNING, "No SSL cert or key provided, using self signed certificate");
                    SelfSignedCertificate ssc = new SelfSignedCertificate();
                    cert = ssc.certificate();
                    key = ssc.privateKey();
                }
                sslContext = SslContextBuilder.forServer(cert, key).build();
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, "Failed to load ssl, failing back to no ssl", ex);
                ssl = false;
            }
        }
        //Setup tcp socket
        try {
            tcpConGroup = new NioEventLoopGroup();
            tcpMsgGroup = new NioEventLoopGroup();
            tcpServer = new ServerBootstrap();
            tcpServer.group(tcpConGroup, tcpMsgGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) {
                            ChannelPipeline p = ch.pipeline();
                            NettyConnection client = new NettyConnection(NettyServer.this);
                            client.setTcp(ch);
                            client.setUserData("address", ch.remoteAddress());
                            tcpClients.put(ch, client);
                            pendingConnections.add(client);

                            //Disconnect client listener
                            ch.closeFuture().addListener((ChannelFutureListener) future -> {
                                NettyConnection connection = tcpClients.get(future.channel());
                                if (connection == null) {
                                    return; //No client on this connection
                                }

                                //find and remove secret for client if one exists
                                String secret = null;
                                for (String key : Collections.unmodifiableCollection(secrets.keySet())) {
                                    if (secrets.get(key).equals(connection)) {
                                        secret = key;
                                    }
                                }
                                if (secret != null) {
                                    secrets.remove(secret);
                                }

                                tcpClients.remove(future.channel());

                                try {
                                    for (ConnectionListener listener : connectionListeners) {
                                        listener.onDisconnect(client);
                                    }
                                } catch (Exception ex) {
                                    LOGGER.log(Level.WARNING, "Exception thrown running connection listeners", ex);
                                }
                            });

                            //Setup ssl
                            if (ssl) {
                                p.addLast(sslContext.newHandler(ch.alloc()));
                            }

                            //Setup pipeline
                            if (logLevel != null) {
                                p.addLast(new LoggingHandler(logLevel));
                            }
                            p.addLast(
                                    new NetworkMessageEncoder(),
                                    new NetworkMessageDecoder(Integer.MAX_VALUE, ClassResolvers.softCachingResolver(null)),
                                    new ChannelInboundHandlerAdapter() {
                                        @Override
                                        public void channelRead(ChannelHandlerContext ctx, Object msg) {
                                            if (msg instanceof NetworkMessage) {
                                                NettyConnection conn = tcpClients.get(ctx.channel());
                                                if (!pendingConnections.contains(conn)) {
                                                    receive(conn, (NetworkMessage) msg);
                                                } else {
                                                    LOGGER.fine("Rejected message " + ((NetworkMessage) msg).getName() + " from " + conn.getAddress() + ". Connection not fully established");
                                                }
                                            } else {
                                                LOGGER.log(Level.SEVERE, "Received message that was not a NetworkMessage object");
                                            }
                                            ctx.fireChannelRead(msg);
                                        }

                                        @Override
                                        public void channelReadComplete(ChannelHandlerContext ctx) {
                                            ctx.flush();
                                        }

                                        @Override
                                        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                                            catchNetworkError(cause);
                                            ctx.close();
                                        }
                                    },
                                    new IdleStateHandler(30, 10, 0),
                                    new ChannelDuplexHandler() {
                                        @Override
                                        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
                                            if (evt instanceof IdleStateEvent) {
                                                IdleStateEvent e = (IdleStateEvent) evt;
                                                if (e.state() == IdleState.READER_IDLE) {
                                                    ctx.close();
                                                } else if (e.state() == IdleState.WRITER_IDLE) {
                                                    NettyConnection conn = tcpClients.get(ctx.channel());
                                                    conn.send(new PingMessage());
                                                }
                                            }
                                        }
                                    }
                            );
                            //Receive the client after comms are setup
                            receive(client);
                        }
                    });
            tcpFuture = tcpServer.bind(port).sync();
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Outside TCP server crash", ex);
        }
    }

    /**
     * Internal use onle
     * Setup the UDP netty.io server pipeline.
     * This will create a dedicated UDP channel for each client.
     * The pipeline is setup to handle <code>NetworkMessage</code> message types.
     */
    private void setupUdp() {
        try {
            udpConGroup = new NioEventLoopGroup();
            udpMsgGroup = new NioEventLoopGroup();
            udpServer = new ServerBootstrap();
            udpServer.group(udpConGroup, udpMsgGroup)
                    .channel(UdpServerChannel.class)
                    .childHandler(new ChannelInitializer<UdpChannel>() {
                        @Override
                        public void initChannel(UdpChannel ch) {
                            ChannelPipeline p = ch.pipeline();
                            //Disconnect client listener
                            ch.closeFuture().addListener((ChannelFutureListener) future -> {
                                if (udpClients.get(future.channel()) == null) {
                                    return; //No client on this connection
                                }
                                udpClients.remove(future.channel());
                            });

                            //Setup pipeline
                            if (logLevel != null) {
                                p.addLast(new LoggingHandler(logLevel));
                            }
                            p.addLast(
                                    new NetworkMessageEncoder(),
                                    new NetworkMessageDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(null)),
                                    new ChannelInboundHandlerAdapter() {
                                        @Override
                                        public void channelRead(ChannelHandlerContext ctx, Object msg) {
                                            //Connect udp client when requested
                                            if (msg instanceof UdpConHashMessage) {
                                                NettyConnection client = secrets.get(((UdpConHashMessage) msg).getUdpHash());
                                                if (client == null) {
                                                    ctx.close();
                                                    //Do not pass the message on, we are forcibly disconnecting the client
                                                    return;
                                                }
                                                secrets.remove(((UdpConHashMessage) msg).getUdpHash());
                                                client.setUdp((UdpChannel) ctx.channel());
                                                udpClients.put(ctx.channel(), client);
                                                receive(client);
                                                ctx.fireChannelRead(msg);
                                                return;
                                            }
                                            if (msg instanceof NetworkMessage) {
                                                NettyConnection conn = udpClients.get(ctx.channel());
                                                if (!pendingConnections.contains(conn)) {
                                                    receive(conn, (NetworkMessage) msg);
                                                } else {
                                                    LOGGER.fine("Rejected message " + ((NetworkMessage) msg).getName() + " from " + conn.getAddress() + ". Connection not fully established");
                                                }
                                            } else {
                                                LOGGER.log(Level.SEVERE, "Received message that was not a NetworkMessage object");
                                            }
                                            ctx.fireChannelRead(msg);
                                        }

                                        @Override
                                        public void channelReadComplete(ChannelHandlerContext ctx) {
                                            ctx.flush();
                                        }

                                        @Override
                                        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                                            catchNetworkError(cause);
                                            ctx.close();
                                        }
                                    });
                        }
                    });
            udpFuture = udpServer.bind(port).sync();
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Outside UDP server crash", ex);
        }
    }

    /**
     * Internal use only
     * Catch a network error. This will cause the error to be sent to the logger.
     * @param cause The error to catch
     */
    private void catchNetworkError(Throwable cause) {
        if (!(cause instanceof java.net.SocketException)) {
            LOGGER.log(Level.WARNING, "Network Server Error", cause);
        }
        //The client disconnected unexpectedly, we can ignore.
    }

    @Override
    public void registerListener(MessageListener handler) {
        messageListeners.add(handler);
    }

    @Override
    public void unregisterListener(MessageListener handler) {
        messageListeners.remove(handler);
    }

    @Override
    public void registerListener(ConnectionListener listener) {
        connectionListeners.add(listener);
    }

    @Override
    public void unregisterListener(ConnectionListener listener) {
        connectionListeners.remove(listener);
    }

    /**
     * Internal use only
     * Generates a base64 like hash
     *
     * @param len The number of characters to generate in the hash
     * @return The generated base64 like hash
     */
    private String getUdpHash(int len) {
        String SALTCHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890!@#$%^&*()_+-=[]{};':\",.<>/?\\";
        StringBuilder salt = new StringBuilder();
        SecureRandom rnd = new SecureRandom();
        while (salt.length() < len) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        return salt.toString();

    }
}
