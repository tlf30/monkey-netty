package io.tlf.monkeynetty.msg;

import io.tlf.monkeynetty.NetworkProtocol;

/**
 * @author Trevor Flynn trevorflynn@liquidcrystalstudios.com
 * <p>
 * Internal Use Onlu
 * This message is sent periodically between the server and client to ensure communication is still active.
 */
public class PingMessage implements NetworkMessage {
    @Override
    public String getName() {
        return "ping-message";
    }

    @Override
    public NetworkProtocol getProtocol() {
        return NetworkProtocol.TCP;
    }
}
