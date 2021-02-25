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

package io.tlf.monkeynetty.server;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.socket.SocketChannel;
import io.tlf.monkeynetty.*;
import io.tlf.monkeynetty.msg.NetworkMessage;

import java.nio.channels.ClosedChannelException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Trevor Flynn trevorflynn@liquidcrystalstudios.com
 */
public class NettyConnection implements NetworkConnection {

    private final static Logger LOGGER = Logger.getLogger(NettyConnection.class.getName());
    private final HashSet<ServerMessageListener> handlers = new HashSet<>();
    private final Object handlerLock = new Object();
    private final Set<ServerConnectionListener> listeners = Collections.synchronizedSet(new HashSet<>());
    private final HashMap<String, Object> atts = new HashMap<>();
    private final NetworkServer server;

    private SocketChannel tcpConn;
    private UdpChannel udpConn;

    private boolean connected = false;

    public NettyConnection(NetworkServer server) {
        this.server = server;
    }

    public void setUdp(UdpChannel conn) {
        udpConn = conn;
    }

    public void setTcp(SocketChannel conn) {
        tcpConn = conn;
    }

    /**
     * Run all connection listeners on client.
     * The client will be flagged as connected upon the completion
     * of running all connection listeners.
     */
    protected void connect() {
        for (ServerConnectionListener listener : listeners) {
            listener.onConnect(this);
        }
        connected = true;
    }

    /**
     * The client will be connected once all connection listeners
     * have been run specific to the client, and the client is connected
     * to the remote client endpoint.
     * @return If the client is connected
     */
    @Override
    public boolean isConnected() {
        return connected;
    }

    @Override
    public void disconnect() {
        for (ServerConnectionListener listener : listeners) {
            listener.onDisconnect(this);
        }
        //Close connection
        tcpConn.close();
        connected = false;
    }

    @Override
    public int getTcpPort() {
        return server.getSettings().getTcpPort();
    }

    @Override
    public int getUdpPort() {
        return server.getSettings().getUdpPort();
    }

    public NetworkServer getServer() {
        return server;
    }

    @Override
    public String getService() {
        return server.getSettings().getService();
    }

    public boolean isSsl() {
        return server.getSettings().isSsl();
    }

    @Override
    public String getAddress() {
        return getUserData("address");
    }

    @Override
    public NetworkProtocol[] getProtocol() {
        return server.getSettings().getProtocols();
    }

    @Override
    public void send(NetworkMessage message) {
        ChannelFuture future;
        try {
            if (message.getProtocol() == NetworkProtocol.TCP) {
                future = tcpConn.writeAndFlush(message);
            } else {
                future = udpConn.writeAndFlush(message);
            }
            future.addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
            future.addListener((ChannelFutureListener) future1 -> {
                if (!future1.isSuccess()) {
                    if (!(future1.cause() instanceof ClosedChannelException)) {
                        LOGGER.log(Level.WARNING, "Error on " + (message.getProtocol() == NetworkProtocol.TCP ? "TCP" : "UDP") + " channel");
                        LOGGER.log(Level.WARNING, "Error sending " + message.getName() + " to " + (message.getProtocol() == NetworkProtocol.TCP ? tcpConn.localAddress().toString() : udpConn.localAddress0().toString()));
                        LOGGER.log(Level.WARNING, "Failed to send message to client", future1.cause());
                    }
                }
            });
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Failed to send message to client", ex);
        }
    }

    @Override
    public void receive(NetworkMessage message) {
        //Handlers
        synchronized (handlerLock) {
            for (ServerMessageListener handler : handlers) {
                for (Class a : handler.getSupportedMessages()) {
                    if (a.isInstance(message)) {
                        handler.onMessage(message, null, this);
                    }
                }
            }
        }
    }

    @Override
    public void setUserData(String key, Object obj) {
        atts.put(key, obj);
    }

    @Override
    public <T> T getUserData(String key) {
        return (T) atts.get(key);
    }

    @Override
    public void registerListener(ServerMessageListener handler) {
        synchronized (handlerLock) {
            handlers.add(handler);
        }
    }

    @Override
    public void unregisterListener(ServerMessageListener handler) {
        synchronized (handlerLock) {
            handlers.remove(handler);
        }
    }

    @Override
    public void registerListener(ServerConnectionListener listener) {
        listeners.add(listener);
    }

    @Override
    public void unregisterListener(ServerConnectionListener listener) {
        listeners.remove(listener);
    }
}
