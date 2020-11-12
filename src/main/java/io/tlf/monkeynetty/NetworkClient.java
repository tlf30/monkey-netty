
package io.tlf.monkeynetty;

import io.tlf.monkeynetty.msg.NetworkMessage;

/**
 * @author Trevor
 */
public interface NetworkClient {
    boolean isConnected();

    void send(NetworkMessage message);

    void receive(NetworkMessage message);

    void disconnect();

    String getAddress();

    int getPort();

    String getService();

    NetworkProtocol[] getProtocol();

    void registerListener(MessageListener handler);

    void unregisterListener(MessageListener handler);

    void registerListener(ConnectionListener listener);

    void unregisterListener(ConnectionListener listener);
}
