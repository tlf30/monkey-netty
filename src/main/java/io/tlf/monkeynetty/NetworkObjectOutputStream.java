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

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.OutputStream;

/**
 * @author Trevor Flynn trevorflynn@liquidcrystalstudios.com
 *
 * Converts a Object into a binary stream.
 * The first instance of a class sent will send a UID and class name to remote side.
 * All future instances of the class sent will only send UID.
 * Based from: io.netty.handler.codec.serialization.CompactObjectOutputStream
 */
public class NetworkObjectOutputStream extends ObjectOutputStream {

    static final int TYPE_FAT_DESCRIPTOR = 0;
    static final int TYPE_THIN_DESCRIPTOR = 1;
    static final int TYPE_NEW_DESCRIPTOR = 2;

    private NetworkRegistrar registrar;

    NetworkObjectOutputStream(OutputStream out, NetworkRegistrar registrar) throws IOException {
        super(out);
        this.registrar = registrar;
    }

    @Override
    protected void writeStreamHeader() throws IOException {
        writeByte(STREAM_VERSION);
    }

    @Override
    protected void writeClassDescriptor(ObjectStreamClass desc) throws IOException {
        Class<?> clazz = desc.forClass();
        if (clazz.isPrimitive() || clazz.isArray() || clazz.isInterface() ||
                desc.getSerialVersionUID() == 0) {
            write(TYPE_FAT_DESCRIPTOR);
            super.writeClassDescriptor(desc);
        } else {
            Integer id = registrar.getClassRegistry().get(clazz.getName());
            if (id != null) {
                write(TYPE_THIN_DESCRIPTOR);
                writeInt(id);
            } else {
                registrar.register(clazz.getName());
                write(TYPE_NEW_DESCRIPTOR);
                writeUTF(clazz.getName());
                writeInt(registrar.getClassRegistry().get(clazz.getName()));
            }
        }
    }
}
