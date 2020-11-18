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
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.serialization.ClassResolver;

import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;

/**
 * @author Trevor Flynn trevorflynn@liquidcrystalstudios.com
 *
 * Decodes a NetworkMessage from a binary stream for recieving to remote side.
 * Utilizes NetworkObjectInputtream
 *
 * Based from: io.netty.handler.codec.serialization.ObjectDecoder
 */
public class NetworkMessageDecoder extends LengthFieldBasedFrameDecoder {

    private final ClassResolver classResolver;

    private NetworkRegistrar registrar = new NetworkRegistrar();

    /**
     * Creates a new decoder whose maximum object size is {@code 1048576}
     * bytes.  If the size of the received object is greater than
     * {@code 1048576} bytes, a {@link StreamCorruptedException} will be
     * raised.
     *
     * @param classResolver  the {@link ClassResolver} to use for this decoder
     */
    public NetworkMessageDecoder(ClassResolver classResolver) {
        this(1048576, classResolver);
    }

    /**
     * Creates a new decoder with the specified maximum object size.
     *
     * @param maxObjectSize  the maximum byte length of the serialized object.
     *                       if the length of the received object is greater
     *                       than this value, {@link StreamCorruptedException}
     *                       will be raised.
     * @param classResolver    the {@link ClassResolver} which will load the class
     *                       of the serialized object
     */
    public NetworkMessageDecoder(int maxObjectSize, ClassResolver classResolver) {
        super(maxObjectSize, 0, 4, 0, 4);
        this.classResolver = classResolver;
    }

    @Override
    public Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf frame = (ByteBuf) super.decode(ctx, in);
        if (frame == null) {
            return null;
        }

        ObjectInputStream ois = new NetworkObjectInputStream(new ByteBufInputStream(frame, true), classResolver, registrar);
        try {
            return ois.readObject();
        } finally {
            ois.close();
        }
    }
}
