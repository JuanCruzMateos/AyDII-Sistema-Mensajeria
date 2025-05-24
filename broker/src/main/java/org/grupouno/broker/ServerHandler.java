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

public class ServerHandler implements Runnable {
    private static final Logger logger = Logger.getLogger(ServerHandler.class.getName());
    private final Socket socket;
    private final HashMap<SocketAddress, ConnectionManager> connectedServers;

    public ServerHandler(Socket socket, HashMap<SocketAddress, ConnectionManager> connectedServers) {
        this.socket = socket;
        this.connectedServers = connectedServers;
    }

    private synchronized void broadcastMessage(SyncProtocolMessage message, SocketAddress sender) {
        this.connectedServers.entrySet().removeIf(entry -> {
            Socket targetSocket = entry.getValue().socket();
            return targetSocket.isClosed() || !targetSocket.isConnected();
        });

        this.connectedServers.forEach((address, connectionManager) -> {
            if (!address.equals(sender)) {
                try {
                    ObjectOutputStream out = connectionManager.objectOutputStream();
                    out.reset(); // Reset the stream to avoid object caching issues
                    out.writeObject(message);
                    out.flush();
                    logger.info("Sent message: " + message.topic() + " to " + address);
                } catch (IOException e) {
                    logger.warning("Error sending message to " + address + ": " + e.getMessage());
                    try {
                        connectionManager.close();
                    } catch (Exception ignored) {
                        // Ignore close exceptions
                    }
                    this.connectedServers.remove(address);
                }
            }
        });
    }

    @Override
    public void run() {
        SocketAddress remoteAddress = socket.getRemoteSocketAddress();
        try {
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.flush(); // Flush header
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            ConnectionManager connectionManager = new ConnectionManager(socket, out, in);
            this.connectedServers.put(remoteAddress, connectionManager);

            while (!socket.isClosed() && socket.isConnected()) {
                try {
                    SyncProtocolMessage message = (SyncProtocolMessage) in.readObject();
                    logger.info("Received message: " + message.topic() + " from " + remoteAddress);
                    this.broadcastMessage(message, remoteAddress);
                } catch (ClassNotFoundException e) {
                    logger.warning("Invalid message format from " + remoteAddress + ": " + e.getMessage());
                } catch (IOException e) {
                    logger.warning("Connection error with " + remoteAddress + ": " + e.getMessage());
                    try {
                        logger.info("Closing connection with " + remoteAddress);
                        connectionManager.close();
                    } catch (Exception ignored) {
                        // Ignore close exceptions
                    }
                }
            }
        } catch (IOException e) {
            logger.warning("Connection error with " + remoteAddress + ": " + e.getMessage());
        } finally {
            ConnectionManager conn = this.connectedServers.remove(remoteAddress);
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception ignored) {
                    //
                }
            }
        }
    }
}