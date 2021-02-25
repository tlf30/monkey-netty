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

import io.tlf.monkeynetty.NetworkProtocol;
import io.tlf.monkeynetty.msg.NetworkMessage;
import io.tlf.monkeynetty.server.ServerConnectionListener;
import io.tlf.monkeynetty.server.ServerMessageListener;

/**
 * @author Trevor Flynn trevorflynn@liquidcrystalstudios.com
 * <p>
 * Used as a basis for building a custom network connection from a server.
 */
public interface NetworkConnection {

    /**
     * @return if the client is connected to the server
     */
    public boolean isConnected();

    /**
     * @return If the client is connected to the server using SSL
     */
    public boolean isSsl();

    /**
     * @return The address of the server the client is connecting to
     */
    public String getAddress();

    /**
     * @return The tcp port of the server the client is connecting to
     */
    public int getTcpPort();

    /**
     * @return The udp port of the server the client is connecting to
     */
    public int getUdpPort();

    /**
     * The service string should be unique to each server.
     * This is entirely for user use, and is not validated between server and client.
     *
     * @return The service the client is connecting to.
     */
    public String getService();

    /**
     * @return Which network protocols the client supports
     */
    public NetworkProtocol[] getProtocol();

    /**
     * Send a message from the client to the server
     *
     * @param message The message to send
     */
    public void send(NetworkMessage message);

    /**
     * Internal Use Only
     * Called by the server when the server receives a message for the server side connection client.
     *
     * @param message The message received
     */
    public void receive(NetworkMessage message);

    /**
     * Disconnects the client from the server
     */
    public void disconnect();

    /**
     * Register a message listener with the client.
     *
     * @param handler The message listener to register
     */
    public void registerListener(ServerMessageListener handler);

    /**
     * Unregister a message listener with the client.
     *
     * @param handler The message listener to unregister
     */
    public void unregisterListener(ServerMessageListener handler);

    /**
     * Register a connection listener with the client.
     *
     * @param listener The connection listener to register
     */
    public void registerListener(ServerConnectionListener listener);

    /**
     * Unregister a connection listener with the client.
     *
     * @param listener The connection listener to unregister
     */
    public void unregisterListener(ServerConnectionListener listener);

    /**
     * Set obj value attribute for key param
     *
     * @param key key for attribute
     * @param obj value object for attribute
     */
    public void setUserData(String key, Object obj);

    /**
     * Return attribute stored under key param
     *
     * @param <T> type
     * @param key key for attribute
     * @return object casted to T type
     */
    public <T> T getUserData(String key);

}
