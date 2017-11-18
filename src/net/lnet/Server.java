package net.lnet;

import java.io.Closeable;
import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.*;
import java.util.Iterator;

public class Server implements Runnable, Closeable {

    private final Selector selector;
    private final DataProcessor dataProcessor;
    private long lastEventTime; // last event time in nanoseconds
    private int initialSocketChannelOps = SelectionKey.OP_READ;

    public Server(DataProcessor dataProcessor) throws IOException {
        this.dataProcessor = dataProcessor;
        selector = Selector.open();
        updateTimestamp();
    }

    private void updateTimestamp() {
        lastEventTime = System.nanoTime();
    }

    private void setInitialSocketChannelOp(int op, boolean state) {
        initialSocketChannelOps = state ? initialSocketChannelOps | op :
                ~((~initialSocketChannelOps) | op);
    }

    public void setInitialSocketChannelOpRead(boolean state) {
        setInitialSocketChannelOp(SelectionKey.OP_READ, state);
    }

    public void setInitialSocketChannelOpWrite(boolean state) {
        setInitialSocketChannelOp(SelectionKey.OP_WRITE, state);
    }

    public void setInitialSocketChannelOpConnect(boolean state) {
        setInitialSocketChannelOp(SelectionKey.OP_CONNECT, state);
    }

    public void createServerSocketChannel(SocketAddress socketAddress) throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(socketAddress);
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    }

    public void registerSocketChannel(SocketChannel socketChannel, int ops) throws IOException {
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, ops);
    }

    public void registerSocketChannel(SocketAddress socketAddress, int ops) throws IOException {
        registerSocketChannel(SocketChannel.open(socketAddress), ops);
    }

    @Override
    public void run() {
        while(selector.isOpen()) {
            try {
                if(selector.select() > 0) {
                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                    while(iterator.hasNext()) {
                        SelectionKey selectionKey = iterator.next();
                        if(selectionKey.isValid()) {
                            if (selectionKey.isAcceptable()) {
                                ServerSocketChannel channel = (ServerSocketChannel) selectionKey.channel();
                                SocketChannel newChannel = channel.accept();
                                if(newChannel != null) {
                                    registerSocketChannel(newChannel, initialSocketChannelOps);
                                }
                            } else {
                                SocketChannel channel = (SocketChannel) selectionKey.channel();
                                if (selectionKey.isReadable()) {
                                    if(channel.read(dataProcessor.getInputBuffer()) > 0) {
                                        dataProcessor.processInput();
                                    }
                                }
                                if (selectionKey.isWritable()) {
                                    if (dataProcessor.processOutput()) {
                                        channel.write(dataProcessor.getOutputBuffer());
                                    }
                                }
                            }
                        }
                    }
                    updateTimestamp();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void close() throws IOException {
        selector.close();
    }
}
