package io.tlf.monkeynetty;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.OutputStream;

public class NetworkObjectOutputStream extends ObjectOutputStream {

    static final int TYPE_FAT_DESCRIPTOR = 0;
    static final int TYPE_THIN_DESCRIPTOR = 1;

    NetworkObjectOutputStream(OutputStream out) throws IOException {
        super(out);
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
            write(TYPE_THIN_DESCRIPTOR);
            Integer id = NetworkRegistrar.getClassRegistry().get(clazz.getName());
            if (id == null) {
                throw new NetworkMessageException("Attempted to encode unregistered network message: " + clazz.getName());
            }
            writeInt(id);
        }
    }
}
