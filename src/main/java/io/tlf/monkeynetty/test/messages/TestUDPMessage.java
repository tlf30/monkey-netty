package io.tlf.monkeynetty.test.messages;

import io.tlf.monkeynetty.NetworkProtocol;
import io.tlf.monkeynetty.msg.NetworkMessage;

public class TestUDPMessage implements NetworkMessage {

    private String someValue;

    @Override
    public String getName() {
        return "Test UDP Message";
    }

    @Override
    public NetworkProtocol getProtocol() {
        return NetworkProtocol.UDP;
    }

    public String getSomeValue() {
        return someValue;
    }

    public void setSomeValue(String someValue) {
        this.someValue = someValue;
    }
}
