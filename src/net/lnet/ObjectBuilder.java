package net.lnet;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class ObjectBuilder {

    private List<Instancer> map;

    public ObjectBuilder() {
        map = new ArrayList<>();
    }

    public void add(int index, NetTransferable object) throws IllegalInsertionException {
        if(isIndexMapped(index))
            throw new IllegalInsertionException("Index " + index + " of Instancers map already occupied!");
        map.add(index, object.getInstancer());
    }

    public void clearMap() {
        map.clear();
    }

    public NetTransferable construct(int index, ByteBuffer buffer) {
        return map.get(index).create(index, buffer);
    }

    public boolean isIndexMapped(int index) {
        return map.size() > index && map.get(index) != null;
    }
}
