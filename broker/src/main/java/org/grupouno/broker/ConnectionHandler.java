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
    private final HashMap<SocketAddress, ConnectionManager> connectedServers;

    public ConnectionHandler(Socket socket, HashMap<SocketAddress, ConnectionManager> connectedServers) {
        this.socket = socket;
        this.connectedServers = connectedServers;
    }

    private synchronized void broadcastMessage(SyncProtocolMessage message) {
        this.connectedServers.forEach((address, connectionManager) -> {
            if (!address.equals(socket.getRemoteSocketAddress())) {
                try {
                    Socket targetSocket = connectionManager.socket();
                    if (targetSocket.isClosed()) {
                        logger.warning("Socket " + targetSocket.getRemoteSocketAddress() + " is closed. Removing from connected clients.");
                        this.connectedServers.remove(address);
                    } else {
                        ObjectOutputStream out = connectionManager.objectOutputStream();
                        out.writeObject(message);
                        out.flush();
                        logger.info("Sent message: " + message.topic() + " to " + address);
                    }
                } catch (IOException e) {
                    logger.warning("Error sending message to " + address + ": " + e.getMessage());
                }
            }
        });
    }

    @Override
    public void run() {
        try {
            this.connectedServers.put(
                    socket.getRemoteSocketAddress(),
                    new ConnectionManager(
                            socket,
                            new ObjectOutputStream(socket.getOutputStream()),
                            new ObjectInputStream(socket.getInputStream())));
            ConnectionManager connectionManager = this.connectedServers.get(socket.getRemoteSocketAddress());
            ObjectInputStream in = connectionManager.objectInputStream();

            while (true) {
                SyncProtocolMessage message = (SyncProtocolMessage) in.readObject();
                logger.info("Received message: " + message.topic() + " from " + socket.getRemoteSocketAddress());
                broadcastMessage(message);
            }
        } catch (IOException e) {
            logger.warning("Error creating output stream: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            logger.warning("Error reading message: " + e.getMessage());
        }
    }
}