package net.lnet.packet;

import net.lnet.IllegalInsertionException;
import net.lnet.ObjectBuilder;
import net.lnet.Server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * Created by slimon on 15-11-17.
 */
public class PacketNetManager {

    private Server server;
    private PacketDataProcessor dataProcessor;
    private ObjectBuilder objectBuilder;

    public PacketNetManager() {}

    private void createDataProcessor() {
        objectBuilder = new ObjectBuilder();
        dataProcessor = new PacketDataProcessor(objectBuilder);
    }

    public void createDefaultAcceptableServer(int port) throws IOException {
        createDataProcessor();
        server = new Server(dataProcessor);
        server.createServerSocketChannel(new InetSocketAddress(port));
    }

    public void createDefaultNonAcceptableServer(SocketAddress connectedTo, int supportedOperations) throws IOException {
        createDataProcessor();
        server = new Server(dataProcessor);
        server.registerSocketChannel(connectedTo, supportedOperations);
    }

    public Server getServer() {
        return server;
    }

    public void openServer() {
        new Thread(server).start();
    }

    public void closeServer() throws IOException {
        server.close();
    }

    public void register(int index, Packet packet) throws IllegalInsertionException {
        objectBuilder.add(index, packet);
    }

    public void send(Packet packet) {
        dataProcessor.send(packet);
    }
}
