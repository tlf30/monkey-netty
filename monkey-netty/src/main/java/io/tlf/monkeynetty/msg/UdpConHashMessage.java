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

package io.tlf.monkeynetty.msg;

import io.tlf.monkeynetty.NetworkProtocol;

/**
 * @author Trevor Flynn trevorflynn@liquidcrystalstudios.com
 * <p>
 * Internal Use Onlu
 * UdpConHashMessage is used internally to establish a UDP channel with the client
 * from a tcp channel.
 */
public class UdpConHashMessage implements NetworkMessage {

    /**
     * Internal Use Only
     * The Base64 hash that the server will provide to the client,
     * for which the client must correctly respond with when connecting the UDP channel.
     */
    private String udpHash;

    /**
     * Internal Use Only
     * A flag that indicated which side sent the message
     */
    private boolean isServer;

    /**
     * Internal Use Only
     *
     * @param hash     The Base64 hash used to authenticate the client's UDP channel
     * @param isServer The side that sent the message
     */
    public UdpConHashMessage(String hash, boolean isServer) {
        udpHash = hash;
        this.isServer = isServer;
    }

    @Override
    public String getName() {
        return "udp-con-string";
    }

    /**
     * @return Base64 hash
     */
    public String getUdpHash() {
        return udpHash;
    }

    @Override
    public NetworkProtocol getProtocol() {
        return isServer ? NetworkProtocol.TCP : NetworkProtocol.UDP;
    }
}
