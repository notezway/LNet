package net.lnet;

import java.nio.channels.SocketChannel;

/**
 * Created by slimon on 19-11-17.
 */
public interface ServerEventListener {

    void onServerOpen();

    void onServerClosed(CloseReason closeReason);

    void onClientConnected(SocketChannel socketChannel);

    void onClientDisconnected(SocketChannel socketChannel, CloseReason closeReason);

    void onErrorOccurred(Exception e);
}
