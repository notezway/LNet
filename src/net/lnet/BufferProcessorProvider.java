package net.lnet;

import java.nio.channels.SocketChannel;

/**
 * Created by slimon on 19-11-17.
 */
public interface BufferProcessorProvider {

    BufferProcessor getNewBufferProcessor(SocketChannel socketChannel);
}
