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

import io.netty.handler.codec.serialization.ClassResolver;

import java.io.*;

/**
 * @author Trevor Flynn trevorflynn@liquidcrystalstudios.com
 * <p>
 * Converts a binary stream into an Object.
 * If a class name and UID are sent with the object, this will remember the class to UID relationship.
 * All future instances of the UID will be related to the correct class for deserialization.
 * Based from: io.netty.handler.codec.serialization.CompactObjectInputStream
 */
public class NetworkObjectInputStream extends ObjectInputStream {

    private final ClassResolver classResolver;
    private final NetworkRegistrar registrar;

    NetworkObjectInputStream(InputStream in, ClassResolver classResolver, NetworkRegistrar registrar) throws IOException {
        super(in);
        this.classResolver = classResolver;
        this.registrar = registrar;
    }

    @Override
    protected void readStreamHeader() throws IOException {
        int version = readByte() & 0xFF;
        if (version != STREAM_VERSION) {
            throw new StreamCorruptedException("Unsupported version: " + version);
        }
    }

    @Override
    protected ObjectStreamClass readClassDescriptor()
            throws IOException, ClassNotFoundException {
        int type = read();
        if (type < 0) {
            throw new EOFException();
        }
        switch (type) {
            case NetworkObjectOutputStream.TYPE_FAT_DESCRIPTOR:
                return super.readClassDescriptor();
            case NetworkObjectOutputStream.TYPE_THIN_DESCRIPTOR:
                int id = readInt();
                String className = registrar.getUidRegistry().get(id);
                if (className == null) {
                    throw new NetworkMessageException("Unregistered type received for decoding: " + id);
                }
                Class<?> clazz = classResolver.resolve(className);
                return ObjectStreamClass.lookupAny(clazz);
            case NetworkObjectOutputStream.TYPE_NEW_DESCRIPTOR:
                String newName = readUTF();
                int newId = readInt();
                registrar.register(newName, newId);
                Class<?> newClazz = classResolver.resolve(newName);
                return ObjectStreamClass.lookupAny(newClazz);
            default:
                throw new StreamCorruptedException("Unexpected class descriptor type: " + type);
        }
    }

    @Override
    protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
        Class<?> clazz;
        try {
            clazz = classResolver.resolve(desc.getName());
        } catch (ClassNotFoundException ignored) {
            clazz = super.resolveClass(desc);
        }

        return clazz;
    }

}
