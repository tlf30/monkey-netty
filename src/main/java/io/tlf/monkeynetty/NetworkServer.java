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

package io.tlf.monkeynetty;

import io.tlf.monkeynetty.msg.NetworkMessage;

/**
 * @author Trevor Flynn trevorflynn@liquidcrystalstudios.com
 * <p>
 * Used as a basis for building a custom network server.
 */
public interface NetworkServer {

    /**
     * @return The number of active connections to the server
     */
    public int getConnections();

    /**
     * @return The port number the server is listening on
     */
    public int getPort();

    /**
     * @return If the server is using SSL for TCP transport
     */
    public boolean isSsl();

    /**
     * The service string should be unique to each server.
     * This is entirely for user use, and is not validated between server and client.
     *
     * @return The service the server is running.
     */
    public String getService();

    /**
     * @return Which network protocols the server supports
     */
    public NetworkProtocol[] getProtocol();

    /**
     * Send a message to all clients connected to the server.
     *
     * @param message The message to send
     */
    public void send(NetworkMessage message);

    /**
     * Send a message to the provided client
     *
     * @param message The message to send
     * @param client  The client to send the message to
     */
    public void send(NetworkMessage message, NetworkClient client);

    /**
     * Register a message listener with the server.
     *
     * @param handler The message listener to register
     */
    public void registerListener(MessageListener handler);

    /**
     * Unregister a message listener with the server.
     *
     * @param handler The message listener to unregister
     */
    public void unregisterListener(MessageListener handler);

    /**
     * Register a connection listener with the server.
     *
     * @param listener The connection listener to register
     */
    public void registerListener(ConnectionListener listener);

    /**
     * Unregister a connection listener with the server.
     *
     * @param listener The connection listener to unregister
     */
    public void unregisterListener(ConnectionListener listener);
}
