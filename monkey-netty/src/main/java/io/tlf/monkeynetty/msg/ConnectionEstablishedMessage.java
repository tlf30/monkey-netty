package io.tlf.monkeynetty.msg;

import io.tlf.monkeynetty.NetworkProtocol;

public class ConnectionEstablishedMessage implements NetworkMessage {

    @Override
    public String getName() {
        return "connection-established-message";
    }

    @Override
    public NetworkProtocol getProtocol() {
        return NetworkProtocol.TCP;
    }
}
