package org.grupouno.monitor;

import org.grupouno.model.connection.SocketConnection;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.logging.Logger;

public class AddressServerHandler implements Runnable {
    private static final Logger logger = Logger.getLogger(AddressServerHandler.class.getName());
    private final SocketConnection socketConnection;
    private volatile SocketAddress primaryServerAddress;

    public AddressServerHandler(Socket socket, SocketAddress primaryServerAddress) throws IOException {
        this.socketConnection = new SocketConnection(socket,
                new ObjectOutputStream(socket.getOutputStream()),
                new ObjectInputStream(socket.getInputStream()));
        this.primaryServerAddress = primaryServerAddress;
    }

    @Override
    public void run() {
        try {
            ObjectInputStream in = this.socketConnection.getObjectInputStream();
            ObjectOutputStream out = this.socketConnection.getObjectOutputStream();
            for (; ; ) {
                String message = (String) in.readObject();
                logger.info("Received message: " + message + " from " + this.socketConnection.getSocket().getRemoteSocketAddress());
                if ("primary.server".equals(message)) {
                    handlePrimaryServerRequest(out);
                } else {
                    logger.warning("Unknown message type: " + message);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            logger.warning("Connection closed: " + e.getMessage());
        } finally {
            try {
                this.socketConnection.close();
            } catch (IOException e) {
                logger.warning("Error closing socketConnection: " + e.getMessage());
            }
        }
    }

    private synchronized void handlePrimaryServerRequest(ObjectOutputStream out) throws IOException {
        if (this.primaryServerAddress != null) {
            logger.info("Sending primary server address: " + this.primaryServerAddress);
        } else {
            logger.warning("Primary server address is not set.");
        }
        out.writeObject(this.primaryServerAddress);
        out.flush();
    }

    public synchronized void setPrimaryServerAddress(SocketAddress address) {
        this.primaryServerAddress = address;
        logger.info("xxxxxxxxxxxxxxxx xxxxxxxx Primary server address set to: " + address);
    }
}