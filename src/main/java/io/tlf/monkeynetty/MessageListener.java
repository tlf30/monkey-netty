package io.tlf.monkeynetty;

import io.tlf.monkeynetty.msg.NetworkMessage;

public interface MessageListener {
    void onMessage(NetworkMessage msg, NetworkServer server, NetworkClient client);
    Class<? extends NetworkMessage>[] getSupportedMessages();
}
