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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * @author Trevor Flynn trevorflynn@liquidcrystalstudios.com
 *
 * Encodes a NetworkMessage into a binary stream for sending to remote side.
 * Utilizes NetworkObjectOutputStream
 *
 * Based from: io.netty.handler.codec.serialization.ObjectEncoder
 */
public class NetworkMessageEncoder extends MessageToByteEncoder<Serializable> {
    private static final byte[] LENGTH_PLACEHOLDER = new byte[4];

    private NetworkRegistrar registrar = new NetworkRegistrar();

    @Override
    protected void encode(ChannelHandlerContext ctx, Serializable msg, ByteBuf out) throws Exception {
        int startIdx = out.writerIndex();

        ByteBufOutputStream bout = new ByteBufOutputStream(out);
        ObjectOutputStream oout = null;
        try {
            bout.write(LENGTH_PLACEHOLDER);
            oout = new NetworkObjectOutputStream(bout, registrar);
            oout.writeObject(msg);
            oout.flush();
        } finally {
            if (oout != null) {
                oout.close();
            } else {
                bout.close();
            }
        }

        int endIdx = out.writerIndex();

        out.setInt(startIdx, endIdx - startIdx - 4);
    }
}
