/*
 * MIT License
 *
 * Copyright (c) 2021 Trevor Flynn
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.tlf.monkeynetty.client;

import io.netty.buffer.ByteBuf;
import io.netty.channel.AddressedEnvelope;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultAddressedEnvelope;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.serialization.ClassResolver;
import io.tlf.monkeynetty.NetworkMessageDecoder;

import java.net.InetSocketAddress;
import java.util.List;

public class DatagramPacketObjectDecoder extends MessageToMessageDecoder<AddressedEnvelope<Object, InetSocketAddress>> {

    private final NetworkMessageDecoder delegateDecoder;

    public DatagramPacketObjectDecoder(ClassResolver resolver) {
        this(resolver, Integer.MAX_VALUE);
    }

    public DatagramPacketObjectDecoder(ClassResolver resolver, int maxObjectSize) {
        delegateDecoder = new NetworkMessageDecoder(maxObjectSize, resolver);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, AddressedEnvelope<Object, InetSocketAddress> msg, List<Object> out) throws Exception {
        if (msg.content() instanceof ByteBuf) {
            ByteBuf payload = (ByteBuf) msg.content();
            Object result = delegateDecoder.decode(ctx, payload);
            AddressedEnvelope<Object, InetSocketAddress> addressedEnvelop = new DefaultAddressedEnvelope<>(result, msg.recipient(), msg.sender());
            out.add(addressedEnvelop);
        }
    }
}
