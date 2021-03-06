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

package io.tlf.monkeynetty;

import io.tlf.monkeynetty.msg.NetworkMessage;

/**
 * @author Trevor Flynn trevorflynn@liquidcrystalstudios.com
 */
public interface MessageListener {

    /**
     * When the server/client receives a message, this will be called.
     * This is to be implemented by the user code.
     *
     * @param msg    The message received
     * @param server The server that sent the message, will be null on client side application
     * @param client The client that received the message
     */
    public void onMessage(NetworkMessage msg, NetworkServer server, NetworkClient client);

    /**
     * The listener <code>onMessage</code> will only get called if the message received
     * is within this returned list.
     *
     * @return A list of supported messages by this listener
     */
    public Class<? extends NetworkMessage>[] getSupportedMessages();
}
