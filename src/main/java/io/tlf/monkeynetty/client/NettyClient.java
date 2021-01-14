/*
MIT License

Copyright (c) 2020 Trevor Flynn

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

package io.tlf.monkeynetty.client;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramChannelConfig;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.SocketChannelConfig;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.DatagramPacketObjectDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.tlf.monkeynetty.*;
import io.tlf.monkeynetty.msg.ConnectionEstablishedMessage;
import io.tlf.monkeynetty.msg.NetworkMessage;
import io.tlf.monkeynetty.msg.UdpConHashMessage;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static io.netty.channel.ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE;

/**
 * @author Trevor Flynn trevorflynn@liquidcrystalstudios.com
 */
public class NettyClient extends BaseAppState implements NetworkClient {

    private final static Logger LOGGER = Logger.getLogger(NettyClient.class.getName());
    private final HashMap<String, Object> atts = new HashMap<>();

    protected String service;
    protected int port;
    protected String server;
    protected boolean ssl;
    protected boolean sslSelfSigned;
    protected volatile boolean reconnect = false;
    protected volatile boolean udpHandshakeComplete = false;
    private volatile boolean pendingEstablish = true;
    /*
     * Connection timeout in milliseconds used when client is unable connect to server
     * Note: Currently it do not apply when server is "off"
     */
    protected int connectionTimeout = 10000;
    protected MessageCacheMode cacheMode = MessageCacheMode.TCP_ENABLED;
    private LogLevel logLevel;

    //Netty
    private EventLoopGroup tcpGroup = new NioEventLoopGroup();
    private Bootstrap tcpClientBootstrap = new Bootstrap();
    private ChannelFuture tcpChannelFuture;
    private SocketChannel tcpChannel;
    private EventLoopGroup udpGroup = new NioEventLoopGroup();
    private Bootstrap udpClientBootstrap = new Bootstrap();
    private ChannelFuture udpChannelFuture;
    private DatagramChannel udpChannel;
    private SslContext sslContext;

    private final HashSet<MessageListener> handlers = new HashSet<>();
    private final Set<ConnectionListener> listeners = Collections.synchronizedSet(new HashSet<>());
    private final Object handlerLock = new Object();
    private final LinkedList<NetworkMessage> messageCache = new LinkedList<>();

    public NettyClient(String service, int port, String server) {
        this(service, false, false, port, server);
    }

    public NettyClient(String service, boolean ssl, int port, String server) {
        this(service, ssl, true, port, server);
    }

    public NettyClient(String service, boolean ssl, boolean sslSelfSigned, int port, String server) {
        this.service = service;
        this.port = port;
        this.server = server;
        this.ssl = ssl;
        this.sslSelfSigned = sslSelfSigned;
    }

    @Override
    public void initialize(Application app) {

    }

    @Override
    public void cleanup(Application app) {

    }

    @Override
    public void onEnable() {
        LOGGER.fine("Loading Netty.IO network client");
        setupTcp();
    }

    public void onDisable() {
        disconnect();
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public void setLogLevel(LogLevel logLevel) {
        this.logLevel = logLevel;
    }

    public void setMessageCacheMode(MessageCacheMode mode) {
        this.cacheMode = mode;
    }

    public MessageCacheMode getMessageCacheMode() {
        return cacheMode;
    }

    private void setupTcp() {
        LOGGER.fine("Setting up tcp");
        if (ssl) {
            try {
                if (sslSelfSigned) {
                    sslContext = SslContextBuilder.forClient()
                            .trustManager(InsecureTrustManagerFactory.INSTANCE).build();
                } else {
                    sslContext = SslContextBuilder.forClient().build();
                }
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, "Failed to load ssl, failing back to no ssl", ex);
                ssl = false;
            }
        }
        //Setup TCP
        tcpGroup = new NioEventLoopGroup();
        tcpClientBootstrap = new Bootstrap();
        tcpClientBootstrap.group(tcpGroup);
        tcpClientBootstrap.channel(NioSocketChannel.class);
        tcpClientBootstrap.remoteAddress(new InetSocketAddress(server, port));
        tcpClientBootstrap.handler(new ChannelInitializer<SocketChannel>() {
            protected void initChannel(SocketChannel socketChannel) {
                tcpChannel = socketChannel;
                SocketChannelConfig cfg = tcpChannel.config();
                cfg.setConnectTimeoutMillis(connectionTimeout);

                ChannelPipeline p = socketChannel.pipeline();
                //Setup ssl
                if (ssl) {
                    p.addLast(sslContext.newHandler(socketChannel.alloc(), server, port));
                }
                //Set log level
                if (logLevel != null) {
                    p.addLast(new LoggingHandler(logLevel));
                }
                //Setup pipeline
                p.addLast(
                        new NetworkMessageEncoder(),
                        new NetworkMessageDecoder(Integer.MAX_VALUE, ClassResolvers.softCachingResolver(null)),
                        new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) {
                                if (!udpHandshakeComplete && msg instanceof UdpConHashMessage) {
                                    String hash = ((UdpConHashMessage) msg).getUdpHash();
                                    setupUdp(hash);
                                } else if (pendingEstablish && msg instanceof ConnectionEstablishedMessage) {
                                    completeConnection();
                                } else if (msg instanceof NetworkMessage) {
                                    receive((NetworkMessage) msg);
                                } else {
                                    LOGGER.log(Level.SEVERE, "Received message that was not a NetworkMessage object");
                                }
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
        try {
            LOGGER.fine("Making tcp connection");
            tcpChannelFuture = tcpClientBootstrap.connect().sync();
            LOGGER.fine("Tcp future synced");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to setup tcp connection");
            catchNetworkError(e);
        }
    }

    private void setupUdp(String hash) {
        LOGGER.fine("Setting up udp");
        udpGroup = new NioEventLoopGroup();
        udpClientBootstrap = new Bootstrap();
        udpClientBootstrap.group(tcpGroup);
        udpClientBootstrap.channel(NioDatagramChannel.class);
        udpClientBootstrap.remoteAddress(new InetSocketAddress(server, port));
        udpClientBootstrap.handler(new ChannelInitializer<DatagramChannel>() {
            protected void initChannel(DatagramChannel socketChannel) {
                udpChannel = socketChannel;
                DatagramChannelConfig cfg = udpChannel.config();
                cfg.setConnectTimeoutMillis(connectionTimeout);
                //Setup pipeline
                ChannelPipeline p = socketChannel.pipeline();
                //Setup pipeline
                if (logLevel != null) {
                    p.addLast(new LoggingHandler(logLevel));
                }
                p.addLast(
                        new NetworkMessageEncoder(),
                        new DatagramPacketObjectDecoder(ClassResolvers.cacheDisabled(null)),
                        new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object netObj) {
                                AddressedEnvelope<Object, InetSocketAddress> envelope = (AddressedEnvelope<Object, InetSocketAddress>) netObj;
                                Object msg = envelope.content();
                                if (msg instanceof NetworkMessage) {
                                    receive((NetworkMessage) msg);
                                } else {
                                    LOGGER.log(Level.SEVERE, "Received message that was not a NetworkMessage object");
                                }
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
        try {
            LOGGER.fine("Making udp connection");
            udpChannelFuture = udpClientBootstrap.connect().sync();
            LOGGER.fine("Udp future synced");
            send(new UdpConHashMessage(hash, false), false);
            udpHandshakeComplete = true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to setup udp connection");
            catchNetworkError(e);
        }
    }

    private void completeConnection() {
        pendingEstablish = false;
        LOGGER.log(Level.FINEST, "Connection established");
        //Notify that we have completed the connection process
        for (ConnectionListener listener : listeners) {
            try {
                listener.onConnect(NettyClient.this);
            } catch (Exception ex) {
                Logger.getLogger(NettyClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void catchNetworkError(Throwable cause) {
        //if (cause instanceof java.net.SocketException) {
        //The server disconnected unexpectedly, we will not log it.
        //} else {
        LOGGER.log(Level.SEVERE, "Network Client Error", cause);
        //}

        LOGGER.fine("Queuing reconnect");
        reconnect = true;
    }

    @Override
    public void update(float tpf) {
        if (reconnect) {

            reconnect = false;

            LOGGER.info("Attempting to reconnect on loss of connection to server");

            //Attempt to reconnect
            try {
                tcpChannel.close();
            } catch (Exception e) {
                //Don't care
            }
            try {
                udpChannel.close();
            } catch (Exception e) {
                //Don't care
            }
            try {
                LOGGER.fine("Making tcp connection");
                tcpChannelFuture = tcpClientBootstrap.connect().sync();
                LOGGER.fine("Tcp future synced");
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to setup tcp connection", e);
                reconnect = true;
                pendingEstablish = true;
                udpHandshakeComplete = false;
            }

            if (isConnected()) {
                LOGGER.info("Network client reconnected to server");
            }
        }
        if (messageCache.size() > 0) {
            LOGGER.finest("Sending cached messages");
            while (messageCache.size() > 0 && isConnected()) {
                send(messageCache.removeFirst());
            }
            LOGGER.finest("Done sending cached messages");
        }
    }

    @Override
    public boolean isConnected() {
        return tcpChannel != null && tcpChannel.isOpen() && udpHandshakeComplete && !pendingEstablish;
    }

    @Override
    public void send(NetworkMessage message) {
        send(message, true);
    }

    private void send(NetworkMessage message, boolean enableCache) {
        if (!isConnected() && enableCache) {
            if (cacheMode == MessageCacheMode.ENABLED) {
                messageCache.push(message);
            } else if (cacheMode == MessageCacheMode.TCP_ENABLED && message.getProtocol() == NetworkProtocol.TCP) {
                messageCache.push(message);
            } else if (cacheMode == MessageCacheMode.UDP_ENABLED && message.getProtocol() == NetworkProtocol.UDP) {
                messageCache.push(message);
            }
            return;
        }
        try {
            if (message.getProtocol() == NetworkProtocol.TCP) {
                ChannelFuture future = tcpChannel.writeAndFlush(message);
                future.addListener(FIRE_EXCEPTION_ON_FAILURE);
            } else {
                ChannelFuture future = udpChannel.writeAndFlush(message);
                future.addListener(FIRE_EXCEPTION_ON_FAILURE);
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Failed to send message to server", ex);
        }
    }

    @Override
    public void disconnect() {
        try {
            for (ConnectionListener listener : listeners) {
                listener.onDisconnect(this);
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        try {
            tcpGroup.shutdownGracefully();
            udpGroup.shutdownGracefully();
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public String getAddress() {
        return server;
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
        return new NetworkProtocol[]{NetworkProtocol.TCP, NetworkProtocol.UDP};
    }

    @Override
    public void receive(NetworkMessage message) {
        LOGGER.finest("Got message: " + message.getName());
        //Handlers
        synchronized (handlerLock) {
            try {
                for (MessageListener handler : handlers) {
                    for (Class<? extends NetworkMessage> a : handler.getSupportedMessages()) {
                        if (a.isInstance(message)) {
                            handler.onMessage(message, null, this);
                        }
                    }
                }
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "An error occurred handling message " + message.getName(), ex);
            }
        }
    }

    @Override
    public void registerListener(MessageListener handler) {
        synchronized (handlerLock) {
            handlers.add(handler);
        }
    }

    @Override
    public void unregisterListener(MessageListener handler) {
        synchronized (handlerLock) {
            handlers.remove(handler);
        }
    }

    @Override
    public void registerListener(ConnectionListener listener) {
        listeners.add(listener);
    }

    @Override
    public void unregisterListener(ConnectionListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void setUserData(String key, Object obj) {
        atts.put(key, obj);
    }

    @Override
    public <T> T getUserData(String key) {
        return (T) atts.get(key);
    }
}
