package io.tlf.monkeynetty;

import io.tlf.monkeynetty.msg.NetworkMessage;

/**
 * @author Trevor
 */
public interface NetworkServer {

    int getConnections();

    int getPort();

    boolean isSsl();

    String getService();

    NetworkProtocol[] getProtocol();

    void receive(NetworkClient client);

    void receive(NetworkClient client, NetworkMessage message);

    void send(NetworkMessage message);

    void send(NetworkMessage message, NetworkClient client);

    void registerListener(MessageListener handler);

    void unregisterListener(MessageListener handler);
}
