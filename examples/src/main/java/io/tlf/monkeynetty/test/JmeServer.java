/*
MIT License

Copyright (c) 2021 Trevor Flynn

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

package io.tlf.monkeynetty.test;

import io.tlf.monkeynetty.server.*;
import io.tlf.monkeynetty.test.messages.TestTCPMessage;
import io.tlf.monkeynetty.test.messages.TestUDPMessage;
import com.jme3.app.SimpleApplication;
import com.jme3.system.JmeContext;
import io.netty.handler.logging.LogLevel;
import io.tlf.monkeynetty.msg.NetworkMessage;
import io.tlf.monkeynetty.test.messages.TestTCPBigMessageA;
import io.tlf.monkeynetty.test.messages.TestTCPBigMessageB;
import io.tlf.monkeynetty.test.messages.TestUDPBigMessageA;
import io.tlf.monkeynetty.test.messages.TestUDPBigMessageB;

/**
 * @author Trevor Flynn trevorflynn@liquidcrystalstudios.com
 */
public class JmeServer extends SimpleApplication {

    @Override
    public void simpleInitApp() {

        NetworkServerSettings serverSettings = new NetworkServerSettings();
        serverSettings.setService("test");
        serverSettings.setTcpPort(10000);
        serverSettings.setUdpPort(10000);
        serverSettings.setSsl(true);
        serverSettings.setSslSelfSigned(true);

        NettyServer server = new NettyServer(serverSettings);
        server.setLogLevel(LogLevel.INFO);

        stateManager.attach(server);
        server.registerListener(new ServerConnectionListener() {
            @Override
            public void onConnect(NetworkConnection client) {
                System.out.println("Client connected: " + client.getAddress());
            }

            @Override
            public void onDisconnect(NetworkConnection client) {
                System.out.println("Client disconnected: " + client.getAddress());
            }
        });
        server.registerListener(new ServerMessageListener() {
            @Override
            public void onMessage(NetworkMessage msg, NetworkServer server, NetworkConnection client) {
                System.out.println("Got message " + msg.getName() + " from client " + client.getAddress());
                System.out.println(msg.toString());
                client.send(msg);
            }

            @Override
            public Class<? extends NetworkMessage>[] getSupportedMessages() {
                return new Class[] {TestUDPMessage.class, TestTCPMessage.class, TestTCPBigMessageA.class, TestTCPBigMessageB.class, TestUDPBigMessageA.class, TestUDPBigMessageB.class};
            }
        });
    }

    public static void main(String[] args) {
        JmeServer server = new JmeServer();
        server.start(JmeContext.Type.Headless);
    }
}
