package net.lnet;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Created by slimon on 20-11-17.
 */
public interface DataPacketListener {

    void onPacket(int id, int length, ByteBuffer data, SocketChannel from);
}
