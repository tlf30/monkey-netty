package io.tlf.monkeynetty.msg;

import io.tlf.monkeynetty.NetworkProtocol;

public class UdpConHashMessage implements NetworkMessage {
    private String udpHash;
    private boolean isServer;

    public UdpConHashMessage(String hash, boolean isServer) {
        udpHash = hash;
        this.isServer = isServer;
    }

    @Override
    public String getName() {
        return "udp-con-string";
    }

    public String getUdpHash() {
        return udpHash;
    }

    @Override
    public NetworkProtocol getProtocol() {
        return isServer ? NetworkProtocol.TCP : NetworkProtocol.UDP;
    }
}
