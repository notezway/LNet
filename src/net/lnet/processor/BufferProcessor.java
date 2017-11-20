package net.lnet.processor;

import net.lnet.WriteCallback;

import java.nio.ByteBuffer;

/**
 * Created by slimon on 19-11-17.
 */
public interface BufferProcessor {

    //give to BufferProcessor ability to initiate attached channel write operations
    void registerWriteCallback(WriteCallback writeCallback);

    WriteCallback getRegisteredWriteCallback();

    ByteBuffer getInputBuffer();

    void processInput();

    void processOutput();

    int getBytesRemaining();

    ByteBuffer getOutputBuffer();

    void close();
}
