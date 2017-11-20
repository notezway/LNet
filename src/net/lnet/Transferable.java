package net.lnet;

import java.nio.ByteBuffer;

/**
 * Created by slimon on 20-11-17.
 */
public interface Transferable {

    void toBuffer(ByteBuffer buffer);

    void fromBuffer(ByteBuffer buffer);

    void onReceived();
}
