/*
MIT License

Copyright (c) 2020 Trevor Flynn

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

import io.tlf.monkeynetty.test.messages.TestUDPBigMessageA;
import io.tlf.monkeynetty.test.messages.TestTCPBigMessageA;
import io.tlf.monkeynetty.test.messages.TestTCPMessage;
import io.tlf.monkeynetty.test.messages.TestUDPMessage;
import com.jme3.app.SimpleApplication;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import io.tlf.monkeynetty.MessageListener;
import io.tlf.monkeynetty.NetworkClient;
import io.tlf.monkeynetty.NetworkServer;
import io.tlf.monkeynetty.client.NettyClient;
import io.tlf.monkeynetty.msg.NetworkMessage;
import io.tlf.monkeynetty.test.messages.TestTCPBigMessageB;
import io.tlf.monkeynetty.test.messages.TestUDPBigMessageB;

/**
 * @author Trevor Flynn trevorflynn@liquidcrystalstudios.com
 */
public class JmeClient extends SimpleApplication {

    @Override
    public void simpleInitApp() {
        NettyClient client = new NettyClient("test", true, 10000, "localhost");
        stateManager.attach(client);
        client.registerListener(new MessageListener() {
            @Override
            public void onMessage(NetworkMessage msg, NetworkServer server, NetworkClient client) {
                System.out.println("Got message " + msg);
            }

            @Override
            public Class<? extends NetworkMessage>[] getSupportedMessages() {
                return new Class[] {TestTCPMessage.class, TestUDPMessage.class, TestTCPBigMessageA.class, TestTCPBigMessageB.class, TestUDPBigMessageA.class, TestUDPBigMessageB.class};
            }
        });
        inputManager.addMapping("enter", new KeyTrigger(KeyInput.KEY_RETURN));
        inputManager.addListener((ActionListener) (name, isPressed, tpf) -> {
            if (isPressed) {
                client.send(new TestTCPMessage());
            }
        }, "enter");
        inputManager.addMapping("space", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addListener((ActionListener) (name, isPressed, tpf) -> {
            if (isPressed) {
                client.send(new TestUDPMessage());
            }
        }, "space");
        //todo: replace with setup protocol, when feature https://github.com/tlf30/monkey-netty/issues/7 will be solved
        inputManager.addMapping("key1", new KeyTrigger(KeyInput.KEY_1));
        inputManager.addListener((ActionListener) (name, isPressed, tpf) -> {
            if (isPressed) {
                client.send(new TestTCPBigMessageA());
            }
        }, "key1");
        inputManager.addMapping("key2", new KeyTrigger(KeyInput.KEY_2));
        inputManager.addListener((ActionListener) (name, isPressed, tpf) -> {
            if (isPressed) {
                client.send(new TestUDPBigMessageA());
            }
        }, "key2");
        inputManager.addMapping("key3", new KeyTrigger(KeyInput.KEY_3));
        inputManager.addListener((ActionListener) (name, isPressed, tpf) -> {
            if (isPressed) {
                client.send(new TestTCPBigMessageB());
            }
        }, "key3");
        inputManager.addMapping("key4", new KeyTrigger(KeyInput.KEY_4));
        inputManager.addListener((ActionListener) (name, isPressed, tpf) -> {
            if (isPressed) {
                client.send(new TestUDPBigMessageB());
            }
        }, "key4");
    }

    @Override
    public void simpleUpdate(float tpf) {

    }

    public static void main(String[] args) {
        JmeClient client = new JmeClient();
        client.start();
    }
}
