package org.grupouno.monitor;

import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.logging.Logger;

public class AddressServer implements Runnable {
    private final Logger logger = Logger.getLogger(AddressServer.class.getName());
    private final int addressServerPort;
    private final String addressServerAddress;
    private final SocketAddress primaryServerAddress;


    public AddressServer(String addressServerAddress, int addressServerPort, SocketAddress primaryServerAddress) {
        this.addressServerAddress = addressServerAddress;
        this.addressServerPort = addressServerPort;
        this.primaryServerAddress = primaryServerAddress;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(addressServerPort, 50, InetAddress.getByName(addressServerAddress))) {
            logger.info("HeartbeatServer started on " + addressServerAddress + ":" + addressServerPort);
            while (true) {
                Socket socket = serverSocket.accept();
                logger.info("New connection from " + socket.getRemoteSocketAddress());
                OutputStream out = socket.getOutputStream();
                if (this.primaryServerAddress != null) {
                    logger.info("Sending primary server address: " + this.primaryServerAddress);
                    out.write(this.primaryServerAddress.toString().getBytes());
                } else {
                    logger.warning("Primary server address is not set.");
                    out.write("null".getBytes());
                }
                out.flush();
            }
        } catch (Exception e) {
            logger.warning("Error in monitor: " + e.getMessage());
        }
    }
}
