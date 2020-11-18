package io.tlf.monkeynetty.server;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.socket.SocketChannel;
import io.tlf.monkeynetty.ConnectionListener;
import io.tlf.monkeynetty.NetworkClient;
import io.tlf.monkeynetty.NetworkServer;
import io.tlf.monkeynetty.MessageListener;
import io.tlf.monkeynetty.msg.NetworkMessage;
import io.tlf.monkeynetty.NetworkProtocol;

import java.nio.channels.ClosedChannelException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Trevor
 */
public class NettyConnection implements NetworkClient {

    private final static Logger LOGGER = Logger.getLogger(NettyConnection.class.getName());
    private SocketChannel tcpConn;
    private UdpChannel udpConn;
    private final NetworkServer server;
    private boolean connected = true;
    private final HashSet<MessageListener> handlers = new HashSet<>();
    private final Object handlerLock = new Object();
    private final Set<ConnectionListener> listeners = Collections.synchronizedSet(new HashSet<>());

    private final HashMap<String, Object> atts = new HashMap<>();

    public NettyConnection(NetworkServer server) {
        this.server = server;
    }

    public void setUdp(UdpChannel conn) {
        udpConn = conn;
    }

    public void setTcp(SocketChannel conn) {
        tcpConn = conn;
    }

    protected void connect() {
        for (ConnectionListener listener : listeners) {
            listener.onConnect(this);
        }
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    @Override
    public void disconnect() {
        for (ConnectionListener listener : listeners) {
            listener.onDisconnect(this);
        }
        //Close connection
        tcpConn.close();
        connected = false;
    }

    @Override
    public int getPort() {
        return server.getPort();
    }

    public NetworkServer getServer() {
        return server;
    }

    @Override
    public String getService() {
        return server.getService();
    }

    public boolean isSsl() {
        return server.isSsl();
    }

    @Override
    public NetworkProtocol[] getProtocol() {
        return server.getProtocol();
    }

    @Override
    public void send(NetworkMessage message) {
        ChannelFuture future;
        if (message.getProtocol() == NetworkProtocol.TCP) {
            future = tcpConn.writeAndFlush(message);
        } else {
            future = udpConn.writeAndFlush(message);
        }
        future.addListener((ChannelFutureListener) future1 -> {
            if (!future1.isSuccess()) {
                if (!(future1.cause() instanceof ClosedChannelException)) {
                    LOGGER.log(Level.WARNING, "Error on " + (message.getProtocol() == NetworkProtocol.TCP ? "TCP" : "UDP") + " channel");
                    LOGGER.log(Level.WARNING, "Error sending " + message.getName() + " to " + (message.getProtocol() == NetworkProtocol.TCP ? tcpConn.localAddress().toString() : udpConn.localAddress0().toString()));
                    LOGGER.log(Level.WARNING, "Failed to send message to client", future1.cause());
                }
            }
        });
    }

    @Override
    public void receive(NetworkMessage message) {
        //Handlers
        synchronized (handlerLock) {
            for (MessageListener handler : handlers) {
                for (Class a : handler.getSupportedMessages()) {
                    if (a.isInstance(message)) {
                        handler.onMessage(message, null, this);
                    }
                }
            }
        }
    }

    @Override
    public String getAddress() {
        return getAttribute("address").toString();
    }

    public void setAttribute(String key, Object obj) {
        atts.put(key, obj);
    }

    public Object getAttribute(String key) {
        return atts.get(key);
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
