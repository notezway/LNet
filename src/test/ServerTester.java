package test;

import net.lnet.*;
import net.lnet.processor.AsynchronousBufferProcessor;
import net.lnet.processor.BufferProcessor;
import net.lnet.processor.BufferProcessorProvider;
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

/**
 * Created by slimon on 19-11-17.
 */
public class ServerTester {

    public static void main(String[] args) {
        try {

            System.out.println(1 << 16);
            Server server = new NonblockingServer(asyncProcessorProvider, eventListener);
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open().bind(new InetSocketAddress(5656));
            server.registerAcceptable(serverSocketChannel);
            SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("localhost", 5656));
            server.registerReadable(socketChannel);
            Thread serverThread = new Thread(server);
            serverThread.start();
            Thread.sleep(1000);
            asyncProcessorProvider.map.get(socketChannel.getRemoteAddress()).getRegisteredWriteCallback().write();
            Thread.sleep(2000);
            server.close(socketChannel, null);
            Thread.sleep(500);
            //server.close(serverSocketChannel, null);
            server.close();

        } catch (IOException e) {
            eventListener.onErrorOccurred(e);
        } catch (InterruptedException e) {
            e.printStackTrace();
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

                @Override
                protected int asynchronousGetBytesRemaining() {
                    int ret = bytesRemaining;
                    bytesRemaining = 0;
                    return ret;
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
                public int getBytesRemaining() {
                    return outputBuffer.limit();
                }

                @Override
                public ByteBuffer getOutputBuffer() {
                    return outputBuffer;
                }

                @Override
                public void close() {

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
