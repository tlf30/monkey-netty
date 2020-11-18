package io.tlf.monkeynetty.test;

import com.jme3.app.SimpleApplication;
import com.jme3.system.JmeContext;
import io.tlf.monkeynetty.*;
import io.tlf.monkeynetty.msg.NetworkMessage;
import io.tlf.monkeynetty.server.NettyServer;

import java.util.logging.Level;
import java.util.logging.Logger;

public class JmeServer extends SimpleApplication {

    @Override
    public void simpleInitApp() {
        NettyServer server = new NettyServer("test", true, 10000);
        stateManager.attach(server);
        server.registerListener(new ConnectionListener() {
            @Override
            public void onConnect(NetworkClient client) {
                System.out.println("Client connected: " + client.getAddress());
            }

            @Override
            public void onDisconnect(NetworkClient client) {
                System.out.println("Client disconnected: " + client.getAddress());
            }
        });
        server.registerListener(new MessageListener() {
            @Override
            public void onMessage(NetworkMessage msg, NetworkServer server, NetworkClient client) {
                System.out.println("Got message " + msg.getName() + " from client " + client.getAddress());
                client.send(msg);
            }

            @Override
            public Class<? extends NetworkMessage>[] getSupportedMessages() {
                return new Class[] {TestUDPMessage.class, TestTCPMessage.class};
            }
        });
    }

    public static void main(String[] args) {
        Logger.getLogger(NetworkRegistrar.class.getName()).setLevel(Level.FINE);
        Logger.getLogger(NetworkServer.class.getName()).setLevel(Level.FINE);
        JmeServer server = new JmeServer();
        server.start(JmeContext.Type.Headless);
    }
}
