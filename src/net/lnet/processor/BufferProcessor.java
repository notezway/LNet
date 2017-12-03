package net.lnet.processor;

import net.lnet.WriteCallback;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Created by slimon on 19-11-17.
 */
public interface BufferProcessor {

    //give to BufferProcessor ability to initiate attached channel write operations
    void registerWriteCallback(WriteCallback writeCallback);

    void setOwnerSocketChannel(SocketChannel socketChannel);

    WriteCallback getRegisteredWriteCallback();

    ByteBuffer getInputBuffer();

    void processInput();

    void processOutput();

    ByteBuffer getOutputBuffer();

    void close();

    //same as close but with try to flush output
    void stop();
}
