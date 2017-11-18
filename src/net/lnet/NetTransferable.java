package net.lnet;

import java.nio.ByteBuffer;

public interface NetTransferable {

    void toBuffer(ByteBuffer buffer);

    NetTransferable fromBuffer(ByteBuffer buffer);

    Instancer getInstancer();

    void onReceived();
}
