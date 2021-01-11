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

package io.tlf.monkeynetty.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.internal.RecyclableArrayList;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Trevor Flynn trevorflynn@liquidcrystalstudios.com
 */
public class UdpChannel extends AbstractChannel {

    protected final ChannelMetadata metadata = new ChannelMetadata(false);
    protected final DefaultChannelConfig config = new DefaultChannelConfig(this);
    protected final UdpServerChannel serverChannel;
    protected final InetSocketAddress remote;
    protected final ConcurrentLinkedQueue<ByteBuf> buffers = new ConcurrentLinkedQueue<>();

    protected AtomicBoolean isNew = new AtomicBoolean(true);
    protected volatile boolean open = true;
    protected boolean reading = false;

    protected UdpChannel(UdpServerChannel serverchannel, InetSocketAddress remote) {
        super(serverchannel);
        this.serverChannel = serverchannel;
        this.remote = remote;
    }

    protected boolean getIsNew() {
        return isNew.compareAndSet(true, false);
    }

    @Override
    public ChannelMetadata metadata() {
        return metadata;
    }

    @Override
    public ChannelConfig config() {
        return config;
    }

    @Override
    public boolean isActive() {
        return open;
    }

    @Override
    public boolean isOpen() {
        return isActive();
    }

    @Override
    protected void doClose() {
        open = false;
        serverChannel.doUserChannelRemove(this);
    }

    @Override
    protected void doDisconnect() {
        doClose();
    }

    protected void addBuffer(ByteBuf buffer) {
        this.buffers.add(buffer);
    }

    @Override
    protected void doBeginRead() {
        if (!reading) {
            reading = true;
            try {
                ByteBuf buffer;
                while ((buffer = buffers.poll()) != null) {
                    pipeline().fireChannelRead(buffer);
                }
                pipeline().fireChannelReadComplete();
            } finally {
                reading = false;
            }
        }
    }

    @Override
    protected void doWrite(ChannelOutboundBuffer buffer) {
        final RecyclableArrayList list = RecyclableArrayList.newInstance();
        boolean freeList = true;
        try {
            ByteBuf buf;
            while ((buf = (ByteBuf) buffer.current()) != null) {
                list.add(buf.retain());
                buffer.remove();
            }
            freeList = false;
        } finally {
            if (freeList) {
                for (Object obj : list) {
                    ReferenceCountUtil.safeRelease(obj);
                }
                list.recycle();
            }
        }
        serverChannel.doWrite(list, remote);
    }

    @Override
    protected boolean isCompatible(EventLoop eventloop) {
        return true;
    }

    @Override
    protected AbstractUnsafe newUnsafe() {
        return new AbstractUnsafe() {
            @Override
            public void connect(SocketAddress addr1, SocketAddress addr2, ChannelPromise pr) {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    protected SocketAddress localAddress0() {
        return serverChannel.localAddress0();
    }

    @Override
    protected SocketAddress remoteAddress0() {
        return remote;
    }

    @Override
    protected void doBind(SocketAddress addr) {
        throw new UnsupportedOperationException();
    }

}
