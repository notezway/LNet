package net.lnet.packet;

import net.lnet.Instancer;
import net.lnet.NetTransferable;

/**
 * Created by slimon on 15-11-17.
 */
public abstract class Packet implements NetTransferable {

    private int id;

    public int getId() {
        return id;
    }

    @Override
    public Instancer getInstancer() {
        return (index, buffer) -> {
            id = index;
            return fromBuffer(buffer);
        };
    }
}
