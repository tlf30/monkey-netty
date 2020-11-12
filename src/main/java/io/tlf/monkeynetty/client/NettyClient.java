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
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.tlf.monkeynetty.ConnectionListener;
import io.tlf.monkeynetty.NetworkClient;
import io.tlf.monkeynetty.MessageListener;
import io.tlf.monkeynetty.msg.NetworkMessage;
import io.tlf.monkeynetty.NetworkProtocol;
import io.tlf.monkeynetty.msg.UdpConHashMessage;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NettyClient extends BaseAppState implements NetworkClient {

    private final static Logger LOGGER = Logger.getLogger(NettyClient.class.getName());

    protected String service;
    protected int port;
    protected String server;
    protected volatile boolean reconnect = false;

    //Netty
    private EventLoopGroup tcpGroup = new NioEventLoopGroup();
    private Bootstrap tcpClientBootstrap = new Bootstrap();
    private ChannelFuture tcpChannelFuture;
    private SocketChannel tcpChannel;
    private EventLoopGroup udpGroup = new NioEventLoopGroup();
    private Bootstrap udpClientBootstrap = new Bootstrap();
    private ChannelFuture udpChannelFuture;
    private DatagramChannel udpChannel;

    private final HashSet<MessageListener> handlers = new HashSet<>();
    private final Set<ConnectionListener> listeners = Collections.synchronizedSet(new HashSet<>());
    private final Object handlerLock = new Object();

    public NettyClient(String service, int port, String server) {
        this.service = service;
        this.port = port;
        this.server = server;
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

    private void setupTcp() {
        LOGGER.fine("Setting up tcp");
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
                cfg.setConnectTimeoutMillis(10000);
                //Setup pipeline
                ChannelPipeline p = socketChannel.pipeline();
                p.addLast(
                        new ObjectEncoder(),
                        new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(null)),
                        new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) {
                                if (msg instanceof UdpConHashMessage) {
                                    String hash = ((UdpConHashMessage) msg).getUdpHash();
                                    setupUdp(hash);
                                    return;
                                }
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
                cfg.setConnectTimeoutMillis(10000);
                //Setup pipeline
                ChannelPipeline p = socketChannel.pipeline();
                p.addLast(
                        new ObjectEncoder(),
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
            send(new UdpConHashMessage(hash, false));

            //Notify that we have completed the connection process
            try {
                for (ConnectionListener listener : listeners) {
                    listener.onConnect(NettyClient.this);
                }
            } catch (Exception ex) {
                Logger.getLogger(NettyClient.class.getName()).log(Level.SEVERE, null, ex);
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to setup udp connection");
            catchNetworkError(e);
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
            }

            if (isConnected()) {
                LOGGER.info("Network client reconnected to server");
            }
        }
    }

    @Override
    public boolean isConnected() {
        return tcpChannel != null && tcpChannel.isOpen();
    }

    @Override
    public void send(NetworkMessage message) {
        try {
            if (message.getProtocol() == NetworkProtocol.TCP) {
                tcpChannel.writeAndFlush(message);
            } else {
                udpChannel.writeAndFlush(message);
            }
        } catch (Exception ex) {
            Logger.getLogger(NettyClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void disconnect() {
        try {
            for (ConnectionListener listener : listeners) {
                listener.onDisconnect(this);
            }
        } catch (Exception ex) {
            Logger.getLogger(NettyClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            tcpGroup.shutdownGracefully();
            udpGroup.shutdownGracefully();
        } catch (Exception ex) {
            Logger.getLogger(NettyClient.class.getName()).log(Level.SEVERE, null, ex);
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

    public void registerListener(MessageListener handler) {
        synchronized (handlerLock) {
            handlers.add(handler);
        }
    }

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
}
