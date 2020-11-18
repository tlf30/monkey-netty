package io.tlf.monkeynetty;

public class NetworkMessageException extends RuntimeException {
    public NetworkMessageException() {
        super();
    }

    public NetworkMessageException(String msg) {
        super(msg);
    }
}
