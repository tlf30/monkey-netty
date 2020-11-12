package io.netty.handler.codec.serialization;

import io.netty.buffer.ByteBuf;
import io.netty.channel.AddressedEnvelope;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultAddressedEnvelope;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.net.InetSocketAddress;
import java.util.List;

public class DatagramPacketObjectDecoder extends MessageToMessageDecoder<AddressedEnvelope<Object, InetSocketAddress>> {

    private final ObjectDecoder delegateDecoder;

    public DatagramPacketObjectDecoder(ClassResolver resolver) {
        this(resolver, Integer.MAX_VALUE);
    }

    public DatagramPacketObjectDecoder(ClassResolver resolver, int maxObjectSize) {
        delegateDecoder = new ObjectDecoder(maxObjectSize, resolver);
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
