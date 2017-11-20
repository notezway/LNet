package net.lnet;

import java.nio.channels.SocketChannel;

/**
 * Created by slimon on 19-11-17.
 */
public interface ServerEventListener {

    void onServerOpen();

    void onServerClosed(CloseReason closeReason);

    void onSocketChannelOpen(SocketChannel socketChannel);

    void onSocketChannelClosed(SocketChannel socketChannel, CloseReason closeReason);

    void onErrorOccurred(Exception e);
}
