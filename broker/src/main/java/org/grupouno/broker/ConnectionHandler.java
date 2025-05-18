package org.grupouno.broker;

import org.grupouno.model.connection.ConnectionManager;
import org.grupouno.model.protocols.SyncProtocolMessage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.logging.Logger;

public class ConnectionHandler implements Runnable {
    private final Logger logger = Logger.getLogger(ConnectionHandler.class.getName());
    private final Socket socket;
    private final HashMap<SocketAddress, ConnectionManager> connectedClients;

    public ConnectionHandler(Socket socket, HashMap<SocketAddress, ConnectionManager> connectedClients) {
        this.socket = socket;
        this.connectedClients = connectedClients;
    }

    private synchronized void broadcastMessage(SyncProtocolMessage message) {
        for (SocketAddress address : this.connectedClients.keySet()) {
            if (!address.equals(this.socket.getRemoteSocketAddress())) {
                try {
                    ConnectionManager socketManager = this.connectedClients.get(address);
                    Socket socket = socketManager.socket();
                    if (socket.isClosed()) {
                        logger.warning("Socket " + socket.getRemoteSocketAddress() + " is closed. Removing from connected clients.");
                        this.connectedClients.remove(address);
                    } else {
                        ObjectOutputStream out = socketManager.objectOutputStream();
                        out.writeObject(message);
                        out.flush();
                        logger.info("Sent message: " + message.topic() + " to " + address);
                    }
                } catch (IOException e) {
                    logger.warning("Error sending message to " + address + ": " + e.getMessage());
                }
            }
        }
    }

    @Override
    public void run() {
        try {
            this.connectedClients.put(this.socket.getRemoteSocketAddress(),
                    new ConnectionManager(this.socket, new ObjectOutputStream(this.socket.getOutputStream()), new ObjectInputStream(this.socket.getInputStream())));
            while (true) {
                SyncProtocolMessage message = (SyncProtocolMessage) this.connectedClients.get(this.socket.getRemoteSocketAddress()).objectInputStream().readObject();
                logger.info("Received message: " + message.topic() + " from " + this.socket.getRemoteSocketAddress());
                this.broadcastMessage(message);
            }
        } catch (IOException e) {
            logger.warning("Error creating output stream: " + e.getMessage());
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            logger.warning("Error reading message: " + e.getMessage());
        }
    }
}
