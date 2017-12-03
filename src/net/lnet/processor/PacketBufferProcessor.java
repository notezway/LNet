package net.lnet.processor;

import net.lnet.DataPacketListener;

import java.nio.ByteBuffer;
import java.util.LinkedList;

/**
 * Created by slimon on 20-11-17.
 */
public class PacketBufferProcessor extends AsynchronousBufferProcessor {

    private volatile int lastId = -1;
    private volatile int lastLength = -1;
    private volatile int packetPosition;

    private final DataPacketListener packetListener;

    private final LinkedList<Packet> sendQueue = new LinkedList<>();

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
        int position = inputBuffer.position();
        int available = position - packetPosition;
        /*ServerTester.out += "1 pos         " + inputBuffer.position() + System.lineSeparator();
        ServerTester.out += "1 limit       " + inputBuffer.limit() + System.lineSeparator();
        ServerTester.out += "1 lastId      " + lastId + System.lineSeparator();
        ServerTester.out += "1 lastLength  " + lastLength + System.lineSeparator();
        ServerTester.out += "1 packetPos   " + packetPosition + System.lineSeparator();
        ServerTester.out += "1 available   " + available + System.lineSeparator();*/
        /*System.out.println("1 pos         " + inputBuffer.position());
        System.out.println("1 limit       " + inputBuffer.limit());
        System.out.println("1 lastId      " + lastId);
        System.out.println("1 lastLength  " + lastLength);
        System.out.println("1 packetPos   " + packetPosition);
        System.out.println("1 available   " + available);*/
        if(lastLength == -1) {
            if(available >= 4) {
                lastId = shortToUInt(inputBuffer.getShort(packetPosition));
                lastLength = shortToUInt(inputBuffer.getShort(packetPosition + 2));
                //System.out.println("- lastLength  " + lastLength);
                //ServerTester.out += "- lastId         " + lastId + System.lineSeparator();
                //ServerTester.out += "- lastLength         " + lastLength + System.lineSeparator();
                if(packetPosition > 0) {
                    inputBuffer.limit(Math.max(packetPosition + 4 + lastLength, inputBuffer.position()));
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
                ByteBuffer buffer = null;

                if(lastLength > 0) {
                    buffer = ByteBuffer.allocate(lastLength);
                    inputBuffer.position(packetPosition + 4);
                    inputBuffer.limit(packetPosition + 4 + lastLength);
                    buffer.put(inputBuffer);
                }
                //all data collected, tell about to packet listener
                packetListener.onPacket(lastId, lastLength, buffer, getOwner());

                available -= 4 + lastLength;
                if(available > 0) {
                    inputBuffer.limit(Math.max(position + inputBuffer.capacity() / 2, position));
                    inputBuffer.position(position);
                    packetPosition += 4 + lastLength;
                    lastId = -1;
                    lastLength = -1;
                    asynchronousProcessInput();
                }
                else {
                    packetPosition = 0;
                    inputBuffer.limit(inputBuffer.capacity() / 2);
                    inputBuffer.position(0);
                }
                lastId = -1;
                lastLength = -1;
            }
        }
        /*ServerTester.out += "2 pos         " + inputBuffer.position() + System.lineSeparator();
        ServerTester.out += "2 limit       " + inputBuffer.limit() + System.lineSeparator();
        ServerTester.out += "2 lastId      " + lastId + System.lineSeparator();
        ServerTester.out += "2 lastLength  " + lastLength + System.lineSeparator();
        ServerTester.out += "2 packetPos   " + packetPosition + System.lineSeparator();
        ServerTester.out += "2 available   " + available + System.lineSeparator();*/
        /*System.out.println("2 pos         " + inputBuffer.position());
        System.out.println("2 limit       " + inputBuffer.limit());
        System.out.println("2 lastId      " + lastId);
        System.out.println("2 lastLength  " + lastLength);
        System.out.println("2 packetPos   " + packetPosition);
        System.out.println("2 available   " + available);*/
    }

    @Override
    protected void asynchronousProcessOutput() {
        synchronized (sendQueue) {
            Packet packet = sendQueue.poll();
            if (packet != null) {
                int dataRemaining = packet.data != null ? packet.data.remaining() : 0;
                outputBuffer.clear();
                if (dataRemaining + 4 <= outputBuffer.remaining()) {
                    outputBuffer.putShort(uIntToShort(packet.id));
                    outputBuffer.putShort(uIntToShort(dataRemaining));
                    if(dataRemaining > 0) {
                        outputBuffer.put(packet.data);
                    }
                    outputBuffer.flip();
                }
            }
        }
    }

    public void sendPacket(int id, ByteBuffer data, int offset, int length) {
        data.position(offset);
        data.limit(offset + length);
        synchronized (sendQueue) {
            sendQueue.addLast(new Packet(id, data.slice()));
        }
        initiateOutputWrite();
    }

    public void sendPacket(int id, ByteBuffer data) {
        synchronized (sendQueue) {
            sendQueue.addLast(new Packet(id, data.slice()));
        }
        initiateOutputWrite();
    }

    public class Packet {

        int id;
        ByteBuffer data;

        public Packet(int id, ByteBuffer data) {
            this.id = id;
            this.data = data;
        }
    }
}
