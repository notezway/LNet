package net.lnet;

import java.nio.ByteBuffer;

public interface Instancer<T extends NetTransferable> {

    T create(int index, ByteBuffer buffer);
}
