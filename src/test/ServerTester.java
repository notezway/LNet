package test;

import net.lnet.*;
import net.lnet.processor.AsynchronousBufferProcessor;
import net.lnet.processor.BufferProcessor;
import net.lnet.processor.BufferProcessorProvider;
import net.lnet.processor.PacketBufferProcessor;
import net.lnet.server.NonblockingServer;
import net.lnet.server.Server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by slimon on 19-11-17.
 */
public class ServerTester {

    public static String out = "";

    public static void main(String[] args) {
        try {

            //System.out.println(1 << 16);
            Server server = new NonblockingServer(packetProcessorProvider, eventListener);
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open().bind(new InetSocketAddress(5656));
            server.registerAcceptable(serverSocketChannel);
            SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("localhost", 5656));
            server.registerReadable(socketChannel);
            Thread serverThread = new Thread(server);
            serverThread.setDaemon(true);
            serverThread.start();
            Thread.sleep(1000);
            String s = "ololo";
            Random random = new Random();
            byte[] arr = new byte[(1 << 16) - 5];
            random.nextBytes(arr);
            packetProcessorProvider.map.get(socketChannel.getRemoteAddress()).sendPacket(0, ByteBuffer.wrap(arr));
            /*for(int i = 0; i < 5000; i++) {
                packetProcessorProvider.map.get(socketChannel.getRemoteAddress()).sendPacket(i, ByteBuffer.wrap((s).getBytes()));
                //Thread.yield();
                s += "" + i;
                if(s.length() > (1 << 16) - 5) break;
            }*/
            /*packetProcessorProvider.map.get(socketChannel.getRemoteAddress()).sendPacket(1, ByteBuffer.wrap("ololo1".getBytes()));
            Thread.sleep(500);
            packetProcessorProvider.map.get(socketChannel.getRemoteAddress()).sendPacket(2, ByteBuffer.wrap("ololo2".getBytes()));*/
            //asyncProcessorProvider.map.get(socketChannel.getRemoteAddress()).getRegisteredWriteCallback().write();
            Thread.sleep(2000);
            //System.out.println(out);
            //server.close(socketChannel, null, false);
            //Thread.sleep(500);
            //server.close(serverSocketChannel, null);
            server.close();

        } catch (IOException e) {
            eventListener.onErrorOccurred(e);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    static class PacketBufferProcessorProvider implements BufferProcessorProvider {

        Map<SocketAddress, PacketBufferProcessor> map = new HashMap<>();

        @Override
        public BufferProcessor getNewBufferProcessor(SocketChannel socketChannel) {

            BufferProcessor bufferProcessor = new PacketBufferProcessor((id, length, data, from) -> {
                System.out.print("Packet received from ");
                try {
                    if(from.isOpen()) {
                        System.out.println(from.getRemoteAddress());
                    } else {
                        System.out.println("unknown");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("ID: " + id);
                System.out.println("Length: " + length);
                System.out.println("Content as string: " + new String(data.array()));
            });

            try {
                map.put(socketChannel.getRemoteAddress(), (PacketBufferProcessor) bufferProcessor);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return bufferProcessor;
        }
    }

    static class AsyncBufferProcessorProvider implements BufferProcessorProvider {

        Map<SocketAddress, BufferProcessor> map = new HashMap<>();
        int bytesRemaining = 0;

        @Override
        public BufferProcessor getNewBufferProcessor(SocketChannel socketChannel) {
            AsynchronousBufferProcessor bufferProcessor = new AsynchronousBufferProcessor(256) {
                @Override
                protected void asynchronousProcessInput() {
                    int length = inputBuffer.position();
                    System.out.println(length);
                    inputBuffer.clear();
                }

                @Override
                protected void asynchronousProcessOutput() {
                    outputBuffer.clear();
                    outputBuffer.put(new byte[(int)(1 + Math.random() * 10)]);
                    outputBuffer.flip();
                    bytesRemaining = outputBuffer.limit();
                }

            };
            try {
                map.put(socketChannel.getRemoteAddress(), bufferProcessor);
                return bufferProcessor;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    static class TestBufferProcessorProvider implements BufferProcessorProvider {

        Map<SocketAddress, BufferProcessor> map = new HashMap<>();

        @Override
        public BufferProcessor getNewBufferProcessor(SocketChannel socketChannel) {
            BufferProcessor bufferProcessor = new BufferProcessor() {

                ByteBuffer inputBuffer = ByteBuffer.allocate(256);
                ByteBuffer outputBuffer = ByteBuffer.allocate(256);

                WriteCallback writeCallback;

                @Override
                public void registerWriteCallback(WriteCallback writeCallback) {
                    this.writeCallback = writeCallback;
                }

                @Override
                public void setOwnerSocketChannel(SocketChannel socketChannel) {

                }

                @Override
                public WriteCallback getRegisteredWriteCallback() {
                    return writeCallback;
                }

                @Override
                public ByteBuffer getInputBuffer() {
                    return inputBuffer;
                }

                @Override
                public void processInput() {
                    int length = inputBuffer.position();
                    System.out.println(length);
                    inputBuffer.clear();
                }

                @Override
                public void processOutput() {
                    outputBuffer.clear();
                    outputBuffer.put(new byte[(int)(1 + Math.random() * 10)]);
                    outputBuffer.flip();
                }

                @Override
                public ByteBuffer getOutputBuffer() {
                    return outputBuffer;
                }

                @Override
                public void close() {

                }

                @Override
                public void stop() {

                }
            };
            try {
                map.put(socketChannel.getRemoteAddress(), bufferProcessor);
                return bufferProcessor;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    static TestBufferProcessorProvider testProcessorProvider = new TestBufferProcessorProvider();
    static AsyncBufferProcessorProvider asyncProcessorProvider = new AsyncBufferProcessorProvider();
    static PacketBufferProcessorProvider packetProcessorProvider = new PacketBufferProcessorProvider();

    static ServerEventListener eventListener = new ServerEventListener() {
        @Override
        public void onServerOpen() {
            System.out.println("onServerOpen");
        }

        @Override
        public void onServerClosed(CloseReason closeReason) {
            System.out.println("onServerClosed");
            System.out.println("    Reason: " + (closeReason == null ? null : closeReason.getReasonString()));
        }

        @Override
        public void onSocketChannelOpen(SocketChannel socketChannel) {
            System.out.println("onSocketChannelOpen");
        }

        @Override
        public void onSocketChannelClosed(SocketChannel socketChannel, CloseReason closeReason) {
            System.out.println("onSocketChannelClosed");
        }

        @Override
        public void onErrorOccurred(Exception e) {
            System.out.println("onErrorOccurred");
            e.printStackTrace();
        }
    };
}
