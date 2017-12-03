package net.lnet;

/**
 * Created by slimon on 19-11-17.
 */
public interface WriteCallback {

    //return: true if all bytes written to socket, otherwise false
    boolean write();
}
