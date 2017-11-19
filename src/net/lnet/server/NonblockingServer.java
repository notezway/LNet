package net.lnet.server;

import net.lnet.BufferProcessor;
import net.lnet.BufferProcessorProvider;
import net.lnet.CloseReason;
import net.lnet.ServerEventListener;

import java.io.IOException;
import java.nio.channels.*;
import java.util.Iterator;

/**
 * Created by slimon on 19-11-17.
 */
public class NonblockingServer extends Server {

    private final Selector selector;

    public NonblockingServer(BufferProcessorProvider processorProvider, ServerEventListener eventListener) throws IOException {
        super(processorProvider, eventListener);
        selector = Selector.open();
    }

    @Override
    public void run() {
        super.run();
        int selectedCount = 0;
        while(selector.isOpen()) {
            try {
                selectedCount = selector.select();
            } catch (IOException e) {
                getEventListener().onErrorOccurred(e);
            }
            if(selector.isOpen() && selectedCount > 0) {
                Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                while(keyIterator.hasNext()) {
                    processKey(keyIterator.next());
                    keyIterator.remove();
                }
            }
        }
    }

    private void processKey(SelectionKey selectionKey) {
        if(selectionKey.isValid()) {
            if(selectionKey.isAcceptable()) {
                accept((ServerSocketChannel) selectionKey.channel());
            }
            else {
                SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                BufferProcessor bufferProcessor = (BufferProcessor) selectionKey.attachment();
                if(selectionKey.isReadable()) {
                    read(socketChannel, bufferProcessor);
                }
                if(selectionKey.isWritable()) {
                    write(socketChannel, bufferProcessor);
                }
            }
        }
    }

    private void accept(ServerSocketChannel serverSocketChannel) {
        try {
            SocketChannel socketChannel = serverSocketChannel.accept();
            registerReadable(socketChannel);
            getEventListener().onClientConnected(socketChannel);
        } catch (IOException e) {
            getEventListener().onErrorOccurred(e);
        }
    }

    private void read(SocketChannel socketChannel, BufferProcessor bufferProcessor) {
        try {
            if(socketChannel.read(bufferProcessor.getInputBuffer()) > 0) {
                bufferProcessor.processInput();
            }
        } catch (IOException e) {
            getEventListener().onErrorOccurred(e);
        }
    }

    private void write(SocketChannel socketChannel, BufferProcessor bufferProcessor) {
        int bytesRemaining = bufferProcessor.processOutput();
        if(bytesRemaining > 0) {
            try {
                int bytesWritten = socketChannel.write(bufferProcessor.getOutputBuffer());
                setWriteInterestOp(socketChannel, bytesWritten < bytesRemaining);
            } catch (IOException e) {
                getEventListener().onErrorOccurred(e);
            }
        }
    }

    private void setWriteInterestOp(SocketChannel socketChannel, boolean isInterest) {
        SelectionKey selectionKey = socketChannel.keyFor(selector);
        int oldInterests = selectionKey.interestOps();
        int newInterests = isInterest ? oldInterests | SelectionKey.OP_WRITE :
                ~((~oldInterests) | SelectionKey.OP_WRITE);
        selectionKey.interestOps(newInterests);
    }

    private SelectionKey register(SelectableChannel selectableChannel, int selectorOps) {
        try {
            selectableChannel.configureBlocking(false);
            return selectableChannel.register(selector, selectorOps);
        } catch (IOException e) {
            getEventListener().onErrorOccurred(e);
        }
        return null;
    }


    //public methods (except "run")

    public void registerReadable(SocketChannel socketChannel) {
        SelectionKey selectionKey = register(socketChannel, SelectionKey.OP_READ);
        if(selectionKey != null) {
            BufferProcessor bufferProcessor = getProcessorProvider().getNewBufferProcessor(socketChannel);
            bufferProcessor.registerWriteCallback(() -> NonblockingServer.this.write(socketChannel, bufferProcessor));
            selectionKey.attach(bufferProcessor);
        }
    }

    public void registerAcceptable(ServerSocketChannel serverSocketChannel) {
        register(serverSocketChannel, SelectionKey.OP_ACCEPT);
    }

    public void close(SelectableChannel selectableChannel, CloseReason closeReason) {
        try {
            selectableChannel.close();
        } catch (IOException e) {
            getEventListener().onErrorOccurred(e);
        }
        selectableChannel.keyFor(selector).cancel();
        if(selectableChannel instanceof SocketChannel) {
            getEventListener().onClientDisconnected((SocketChannel) selectableChannel, closeReason);
        }
    }

    public void closeAllChannels(CloseReason closeReason) {
        synchronized (selector.keys()) {
            for (SelectionKey selectionKey : selector.keys()) {
                close(selectionKey.channel(), closeReason);
            }
        }
    }

    @Override
    public void close() throws IOException {
        selector.close();
        super.close();
    }

    public void closeAfterSelect(CloseReason closeReason) throws IOException {
        closeAllChannels(closeReason);
        selector.close();
        super.close();
    }
}
