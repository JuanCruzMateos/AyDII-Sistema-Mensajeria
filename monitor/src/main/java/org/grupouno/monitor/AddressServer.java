package org.grupouno.monitor;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

public class AddressServer implements Runnable {
    private static final Logger logger = Logger.getLogger(AddressServer.class.getName());
    private final int port;
    private final String address;
    private final PrimaryServerAddress primaryServerAddress;

    public AddressServer(String address, int port, PrimaryServerAddress primaryServerAddress) {
        this.address = address;
        this.port = port;
        this.primaryServerAddress = primaryServerAddress;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port, 50, InetAddress.getByName(address))) {
            serverSocket.setReuseAddress(true);
            logger.info("AddressServer started on " + address + ":" + port);
            while (true) {
                Socket socket = serverSocket.accept();
                logger.info("New connection from " + socket.getRemoteSocketAddress());
                new Thread(new AddressServerHandler(socket, primaryServerAddress)).start();
            }
        } catch (Exception e) {
            logger.warning("Error in AddressServer: " + e.getMessage());
        }
    }
}