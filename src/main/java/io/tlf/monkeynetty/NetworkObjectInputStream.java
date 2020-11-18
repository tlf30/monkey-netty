package io.tlf.monkeynetty;

import io.netty.handler.codec.serialization.ClassResolver;

import java.io.*;

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
