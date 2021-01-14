package io.tlf.monkeynetty.msg;

import io.tlf.monkeynetty.NetworkProtocol;

/**
 * @author Trevor Flynn trevorflynn@liquidcrystalstudios.com
 *
 * This message is sent from the server to the client to nifty the
 * client that the server is ready to receive messages.
 */
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
