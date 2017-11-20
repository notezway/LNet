package net.lnet.server;

import net.lnet.processor.BufferProcessorProvider;
import net.lnet.CloseReason;
import net.lnet.ServerEventListener;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * Created by slimon on 19-11-17.
 */
public abstract class Server implements Runnable, Closeable {

    private final BufferProcessorProvider processorProvider;
    private ServerEventListener eventListener;

    Server(BufferProcessorProvider processorProvider, ServerEventListener eventListener) {
        this.processorProvider = processorProvider;
        setEventListener(eventListener);
    }

    public void setEventListener(ServerEventListener eventListener) {
        this.eventListener = eventListener;
    }

    public ServerEventListener getEventListener() {
        return eventListener;
    }

    public BufferProcessorProvider getProcessorProvider() {
        return processorProvider;
    }

    abstract public void registerReadable(SocketChannel socketChannel);

    abstract public void registerAcceptable(ServerSocketChannel serverSocketChannel);

    abstract public void close(SelectableChannel selectableChannel, CloseReason closeReason);

    abstract public void closeAllChannels(CloseReason closeReason, boolean immediately);

    abstract public void closeAfterSelect(CloseReason closeReason) throws IOException;

    @Override
    public void run() {
        eventListener.onServerOpen();
    }

    @Override
    public void close() throws IOException {
        eventListener.onServerClosed(null);
    }
}
