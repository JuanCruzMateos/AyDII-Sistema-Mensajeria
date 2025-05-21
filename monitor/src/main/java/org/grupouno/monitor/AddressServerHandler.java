package org.grupouno.monitor;

import org.grupouno.model.connection.ConnectionManager;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.logging.Logger;

public class AddressServerHandler implements Runnable {
    private static final Logger logger = Logger.getLogger(AddressServerHandler.class.getName());
    private final ConnectionManager connectionManager;
    private final PrimaryServerAddress primaryServerAddress;

    public AddressServerHandler(Socket socket, PrimaryServerAddress primaryServerAddress) throws IOException {
        this.connectionManager = new ConnectionManager(socket,
                new ObjectOutputStream(socket.getOutputStream()),
                new ObjectInputStream(socket.getInputStream()));
        this.primaryServerAddress = primaryServerAddress;
    }

    @Override
    public void run() {
        try {
            ObjectInputStream in = this.connectionManager.objectInputStream();
            ObjectOutputStream out = this.connectionManager.objectOutputStream();
            while (true) {
                String message = (String) in.readObject();
                logger.info("Received message: " + message + " from " + this.connectionManager.socket().getRemoteSocketAddress());
                if ("primary.server".equals(message)) {
                    handlePrimaryServerRequest(out);
                } else {
                    logger.warning("Unknown message type: " + message);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            logger.warning("Error in AddressServerHandler: " + e.getMessage());
        } finally {
            this.connectionManager.close();
        }
    }

    private synchronized void handlePrimaryServerRequest(ObjectOutputStream out) throws IOException {
        SocketAddress address = primaryServerAddress.getAddress();
        if (address != null) {
            logger.info("Sending primary server address: " + address);
        } else {
            logger.warning("Primary server address is not set.");
        }
        out.writeObject(address);
        out.flush();
    }
}