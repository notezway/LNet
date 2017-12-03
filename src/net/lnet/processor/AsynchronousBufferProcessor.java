package net.lnet.processor;

import net.lnet.WriteCallback;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Created by slimon on 20-11-17.
 */
public abstract class AsynchronousBufferProcessor implements BufferProcessor {

    protected final ByteBuffer inputBuffer;
    protected final ByteBuffer outputBuffer;

    protected final Thread inputThread;
    protected final Thread outputThread;

    private WriteCallback writeCallback;

    private SocketChannel owner;

    private volatile boolean needStop = false;

    private volatile int writeAttempts;

    public AsynchronousBufferProcessor(int buffersCapacity) {
        inputBuffer = ByteBuffer.allocateDirect(buffersCapacity);
        outputBuffer = ByteBuffer.allocateDirect(buffersCapacity);

        inputThread = new Thread(() -> {
            synchronized (inputBuffer) {
                while (!needStop) {
                    try {
                        inputBuffer.wait();
                    } catch (InterruptedException ignored) {
                    } finally {
                        if(!needStop) {
                            asynchronousProcessInput();
                        }
                    }
                }
            }
        });

        outputThread = new Thread(() -> {
            synchronized (outputBuffer) {
                while (!needStop) {
                    try {
                        outputBuffer.wait();
                    } catch (InterruptedException ignored) {
                    } finally {
                        while(true) {
                            asynchronousProcessOutput();
                            boolean socketBusy = !writeCallback.write();
                            writeAttempts--;
                            if(socketBusy || writeAttempts < 1) break;
                        }
                    }
                }
            }
        });

        inputThread.setDaemon(true);
        outputThread.setDaemon(true);

        inputThread.start();
        outputThread.start();
    }

    abstract protected void asynchronousProcessInput();

    abstract protected void asynchronousProcessOutput();

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
        writeAttempts++;
        notifyOutput();
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
    public void setOwnerSocketChannel(SocketChannel socketChannel) {
        owner = socketChannel;
    }

    public SocketChannel getOwner() {
        return owner;
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

    /*@Override
    public int getBytesRemaining() {
        synchronized (outputBuffer) {
            return asynchronousGetBytesRemaining();
        }
    }*/

    @Override
    public ByteBuffer getOutputBuffer() {
        if(Thread.currentThread() == outputThread) {
            synchronized (outputBuffer) {
                return outputBuffer;
            }
        }
        return null;
    }

    @Override
    public void close() {
        needStop = true;
        notifyInput();
        notifyOutput();
    }
}
