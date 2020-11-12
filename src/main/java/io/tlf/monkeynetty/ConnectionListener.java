package io.tlf.monkeynetty;

public interface ConnectionListener {
    void onConnect(NetworkClient client);

    void onDisconnect(NetworkClient client);
}
