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

import io.tlf.monkeynetty.msg.NetworkMessage;

/**
 * @author Trevor Flynn trevorflynn@liquidcrystalstudios.com
 * <p>
 * Used as a basis for building a custom network client.
 */
public interface NetworkClient {

    /**
     * @return if the client is connected to the server
     */
    public boolean isConnected();

    /**
     * @return The settings of the client
     */
    public NetworkClientSettings getSettings();

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
    public void registerListener(ClientMessageListener handler);

    /**
     * Unregister a message listener with the client.
     *
     * @param handler The message listener to unregister
     */
    public void unregisterListener(ClientMessageListener handler);

    /**
     * Register a connection listener with the client.
     *
     * @param listener The connection listener to register
     */
    public void registerListener(ClientConnectionListener listener);

    /**
     * Unregister a connection listener with the client.
     *
     * @param listener The connection listener to unregister
     */
    public void unregisterListener(ClientConnectionListener listener);

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
