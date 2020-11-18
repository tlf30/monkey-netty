package io.tlf.monkeynetty.test.messages;

import io.tlf.monkeynetty.NetworkProtocol;
import io.tlf.monkeynetty.msg.NetworkMessage;

public class TestTCPMessage implements NetworkMessage {

    private int someValue;

    @Override
    public String getName() {
        return "Test TCP Message";
    }

    @Override
    public NetworkProtocol getProtocol() {
        return NetworkProtocol.TCP;
    }

    public int getSomeValue() {
        return someValue;
    }

    public void setSomeValue(int someValue) {
        this.someValue = someValue;
    }
}
