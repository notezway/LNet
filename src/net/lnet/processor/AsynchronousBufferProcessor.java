package net.lnet.processor;

import net.lnet.WriteCallback;

import java.nio.ByteBuffer;

/**
 * Created by slimon on 20-11-17.
 */
public abstract class AsynchronousBufferProcessor implements BufferProcessor {

    protected final ByteBuffer inputBuffer;
    protected final ByteBuffer outputBuffer;
    private WriteCallback writeCallback;
    private volatile boolean needStop = false;

    public AsynchronousBufferProcessor(int buffersCapacity) {
        inputBuffer = ByteBuffer.allocateDirect(buffersCapacity);
        outputBuffer = ByteBuffer.allocateDirect(buffersCapacity);

        Thread inputThread = new Thread(() -> {
            synchronized (inputBuffer) {
                while (!needStop) {
                    try {
                        inputBuffer.wait();
                    } catch (InterruptedException ignored) {
                    } finally {
                        asynchronousProcessInput();
                    }
                }
            }
        });

        Thread outputThread = new Thread(() -> {
            synchronized (outputBuffer) {
                while (!needStop) {
                    try {
                        outputBuffer.wait();
                    } catch (InterruptedException ignored) {
                    } finally {
                        asynchronousProcessOutput();
                    }
                }
            }
        });

        inputThread.start();
        outputThread.start();
    }

    abstract protected void asynchronousProcessInput();

    abstract protected void asynchronousProcessOutput();

    abstract protected int asynchronousGetBytesRemaining();

    private void notifyInput() {
        synchronized (inputBuffer) {
            inputBuffer.notify();
        }
    }

    private void notifyOutput() {
        synchronized (outputBuffer) {
            outputBuffer.notify();
        }
    }

    public void initiateOutputWrite() {
        writeCallback.write();
    }

    public void stop() {
        needStop = true;
        notifyInput();
        initiateOutputWrite();
    }

    @Override
    public void registerWriteCallback(WriteCallback writeCallback) {
        this.writeCallback = writeCallback;
    }

    @Override
    public WriteCallback getRegisteredWriteCallback() {
        return writeCallback;
    }

    @Override
    public void processInput() {
        notifyInput();
    }

    @Override
    public void processOutput() {
        notifyOutput();
    }

    @Override
    public ByteBuffer getInputBuffer() {
        synchronized (inputBuffer) {
            return inputBuffer;
        }
    }

    @Override
    public int getBytesRemaining() {
        synchronized (outputBuffer) {
            return asynchronousGetBytesRemaining();
        }
    }

    @Override
    public ByteBuffer getOutputBuffer() {
        synchronized (outputBuffer) {
            return outputBuffer;
        }
    }

    @Override
    public void close() {
        needStop = true;
        notifyInput();
        notifyOutput();
    }
}
