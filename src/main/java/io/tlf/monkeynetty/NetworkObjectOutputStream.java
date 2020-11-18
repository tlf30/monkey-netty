package io.tlf.monkeynetty;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.OutputStream;

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
