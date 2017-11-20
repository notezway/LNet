package net.lnet.processor;

import net.lnet.DataPacketListener;
import net.lnet.Transferable;

import java.util.LinkedList;

/**
 * Created by slimon on 20-11-17.
 */
public class PacketBufferProcessor extends AsynchronousBufferProcessor {

    private volatile int outputBytesRemaining;

    private volatile int lastId;
    private volatile int lastLength = -1;
    private volatile int packetPosition;

    private final DataPacketListener packetListener;

    private final LinkedList<Transferable> sendQueue = new LinkedList<>();

    public PacketBufferProcessor(DataPacketListener packetListener) {
        super((4 + (1 << 16) - 1) * 2);
        this.packetListener = packetListener;
        inputBuffer.limit(inputBuffer.capacity() / 2);
    }

    private int shortToUInt(short value) {
        return (int) value + 32768;
    }

    private short uIntToShort(int value) {
        return (short) (value - 32768);
    }

    @Override
    protected void asynchronousProcessInput() {
        int available = inputBuffer.position() - packetPosition;
        if(lastLength == -1) {
            if(available >= 4) {
                lastId = shortToUInt(inputBuffer.getShort(packetPosition));
                lastLength = shortToUInt(inputBuffer.getShort(packetPosition + 2));
                if(packetPosition > 0) {
                    inputBuffer.limit(packetPosition + 4 + lastLength);
                }
            }
            else {
                int space = inputBuffer.limit() - packetPosition;
                if(space < 4) {
                    inputBuffer.limit(inputBuffer.limit() + (4 - space));
                }
            }
        }
        if(lastLength > -1) {
            if(available >= 4 + lastLength) {
                byte[] data = null;
                if(lastLength > 0) {
                    data = new byte[lastLength];
                    int position = inputBuffer.position();
                    inputBuffer.position(packetPosition + 4);
                    inputBuffer.get(data);
                    inputBuffer.position(position);
                }
                //all data collected, tell about to packet listener
                packetListener.onPacket(lastId, data);

                available -= 4 + lastLength;
                if(available > 0) {
                    packetPosition += 4 + lastLength;
                    lastLength = -1;
                    asynchronousProcessInput();
                }
                else {
                    inputBuffer.limit(inputBuffer.capacity() / 2);
                }
                lastLength = -1;
            }
        }
    }

    @Override
    protected void asynchronousProcessOutput() {

    }

    @Override
    protected int asynchronousGetBytesRemaining() {
        int retValue = outputBytesRemaining;
        outputBytesRemaining = 0;
        return retValue;
    }

    public void sendPacket() {

    }
}
