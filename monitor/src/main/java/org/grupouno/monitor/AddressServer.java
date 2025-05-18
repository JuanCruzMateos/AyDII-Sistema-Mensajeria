package org.grupouno.monitor;

import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.logging.Logger;

public class AddressServer implements Runnable {
    private final Logger logger = Logger.getLogger(AddressServer.class.getName());
    private final int addressServerPort;
    private final String addressServerAddress;
    private final PrimaryServerAddress primaryServerAddress;

    public AddressServer(String addressServerAddress, int addressServerPort, PrimaryServerAddress primaryServerAddress) {
        this.addressServerAddress = addressServerAddress;
        this.addressServerPort = addressServerPort;
        this.primaryServerAddress = primaryServerAddress;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(addressServerPort, 50, InetAddress.getByName(addressServerAddress))) {
            serverSocket.setReuseAddress(Boolean.TRUE);
            logger.info("HeartbeatServer started on " + addressServerAddress + ":" + addressServerPort);
            while (true) {
                Socket socket = serverSocket.accept();
                logger.info("New connection from " + socket.getRemoteSocketAddress());
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                synchronized (this.primaryServerAddress) {
                    SocketAddress address = this.primaryServerAddress.getAddress();
                    if (address != null) {
                        logger.info("Sending primary server address: " + address);
                    } else {
                        logger.warning("Primary server address is not set.");
                    }
                    out.writeObject(address);
                    out.flush();
                }
            }
        } catch (Exception e) {
            logger.warning("Error in monitor: " + e.getMessage());
        }
    }
}
