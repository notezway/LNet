package net.lnet;

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

    int processOutput();

    ByteBuffer getOutputBuffer();
}
