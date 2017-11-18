package net.lnet.packet;

import net.lnet.DataProcessor;
import net.lnet.NetTransferable;
import net.lnet.ObjectBuilder;

import java.nio.ByteBuffer;
import java.util.LinkedList;

public class PacketDataProcessor implements DataProcessor {

    //2 bytes for id, 2 bytes for length and 2^16 bytes for data
    private int inputBufferSize = 2 + 2 + (1 << 16);
    private int outputBufferSize = 2 + 2 + (1 << 16);
    private final ByteBuffer inputBuffer;
    private final ByteBuffer outputBuffer;
    //input
    private int lastId;
    private int lastLength = -1;
    //output
    private final LinkedList<NetTransferable> sendQueue;

    private final ObjectBuilder objectBuilder;

    public PacketDataProcessor(ObjectBuilder objectBuilder) {
        this.objectBuilder = objectBuilder;
        inputBuffer = ByteBuffer.allocateDirect(inputBufferSize);
        outputBuffer = ByteBuffer.allocateDirect(outputBufferSize);
        sendQueue = new LinkedList<>();
    }

    public void send(NetTransferable object) {
        synchronized (sendQueue) {
            sendQueue.addLast(object);
        }
    }

    @Override
    public ByteBuffer getInputBuffer() {
        return inputBuffer;
    }

    private void skipData(ByteBuffer buffer, int length) {
        int writeIndex = 0;
        int readIndex = length;
        while(readIndex < buffer.capacity()) {
            buffer.put(writeIndex++, buffer.get(readIndex++));
        }
        buffer.clear();
        buffer.position(writeIndex);
    }

    @Override
    public void processInput() {
        int position = inputBuffer.position();
        System.out.println(position);
        if(lastLength == -1 && position >= 4) {
            lastId = inputBuffer.getShort(0);
            System.out.println(lastId);
            lastLength = inputBuffer.getShort(2);
            System.out.println(lastLength);
        }
        if(position >= lastLength + 4) {
            if(objectBuilder.isIndexMapped(lastId)) {
                inputBuffer.flip();
                inputBuffer.position(4);
                NetTransferable object = objectBuilder.construct(lastId, inputBuffer);
                object.onReceived();
            }
            else {
                System.err.println("Unable to construct object with index " + lastId + ", skipping data");
            }
            skipData(inputBuffer, lastLength + 4);
            lastLength = -1;
            processInput();
        }
    }

    @Override
    public boolean processOutput() {
        if(outputBuffer.hasRemaining())
            return true;
        synchronized (sendQueue) {
            if(!sendQueue.isEmpty()) {
                outputBuffer.clear();
                sendQueue.poll().toBuffer(outputBuffer);
                outputBuffer.flip();
                return true;
            }
        }
        return false;
    }

    @Override
    public ByteBuffer getOutputBuffer() {
        return outputBuffer;
    }
}
