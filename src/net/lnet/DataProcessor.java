package net.lnet;

import java.nio.ByteBuffer;

public interface DataProcessor {

    ByteBuffer getInputBuffer();

    void processInput();

    boolean processOutput();

    ByteBuffer getOutputBuffer();
}
