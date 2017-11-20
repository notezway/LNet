package net.lnet;

/**
 * Created by slimon on 20-11-17.
 */
public interface DataPacketListener {

    void onPacket(int id, byte[] data);
}
