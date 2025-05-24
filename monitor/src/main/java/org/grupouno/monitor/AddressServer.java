package org.grupouno.monitor;

import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class AddressServer implements Runnable {
    private static final Logger logger = Logger.getLogger(AddressServer.class.getName());
    private final int port;
    private final String address;
    private final List<AddressServerHandler> socketConnections;
    private SocketAddress primaryServerAddress;

    public AddressServer(String address, int port) {
        this.address = address;
        this.port = port;
        this.socketConnections = new ArrayList<>();
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket()) {
            serverSocket.setReuseAddress(true);
            serverSocket.bind(new InetSocketAddress(InetAddress.getByName(address), port));
            logger.info("AddressServer started on " + address + ":" + port);
            while (true) {
                try {
                    Socket socket = serverSocket.accept();
                    logger.info("New connection from " + socket.getRemoteSocketAddress());
                    AddressServerHandler handler = new AddressServerHandler(socket, primaryServerAddress);
                    this.socketConnections.add(handler);
                    logger.info("New AddressServerHandler created for " + socket.getRemoteSocketAddress());
                    new Thread(handler).start();
                } catch (Exception e) {
                    logger.warning("Error accepting connection: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            logger.warning("Error in AddressServer: " + e.getMessage());
        }
    }

    public synchronized void setPrimaryServerAddress(SocketAddress address) {
        this.primaryServerAddress = address;
        for (AddressServerHandler handler : this.socketConnections) {
            handler.setPrimaryServerAddress(address);
        }
        logger.info("All " + this.socketConnections.toArray().length + " handlers updated primary server address set to: " + address);
    }
}