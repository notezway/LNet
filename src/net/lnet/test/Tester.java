package net.lnet.test;

import net.lnet.IllegalInsertionException;
import net.lnet.NetTransferable;
import net.lnet.packet.Packet;
import net.lnet.packet.PacketNetManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

/**
 * Created by slimon on 15-11-17.
 */
public class Tester {

    public static void main(String[] args) {
        PacketNetManager serverNetManager = new PacketNetManager();
        PacketNetManager clientNetManager = new PacketNetManager();

        try {
            serverNetManager.createDefaultAcceptableServer(5555);
            serverNetManager.getServer().setInitialSocketChannelOpWrite(true);
            clientNetManager.createDefaultNonAcceptableServer(new InetSocketAddress(5555),
                    SelectionKey.OP_READ | SelectionKey.OP_WRITE);

            serverNetManager.openServer();
            clientNetManager.openServer();

            Thread.sleep(1000);

            serverNetManager.register(0, testPacket);
            clientNetManager.register(0, testPacket);

            serverNetManager.send(testPacket);
        } catch (IOException | IllegalInsertionException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                serverNetManager.closeServer();
                clientNetManager.closeServer();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static Packet testPacket = new Packet() {

        char data;

        @Override
        public void toBuffer(ByteBuffer buffer) {
            buffer.putChar('A');
        }

        @Override
        public NetTransferable fromBuffer(ByteBuffer buffer) {
            data = buffer.getChar();
            return this;
        }

        @Override
        public void onReceived() {
            System.out.println(data);
        }
    };
}
