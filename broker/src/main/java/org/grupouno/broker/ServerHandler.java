package org.grupouno.broker;

import org.grupouno.model.connection.SocketConnection;
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
    private final HashMap<SocketAddress, SocketConnection> connectedServers;

    public ServerHandler(Socket socket, HashMap<SocketAddress, SocketConnection> connectedServers) {
        this.socket = socket;
        this.connectedServers = connectedServers;
    }

    public void cleanup() {
        this.connectedServers.entrySet().removeIf(entry -> {
            Socket targetSocket = entry.getValue().getSocket();
            return targetSocket.isClosed() || !targetSocket.isConnected();
        });
    }

    private synchronized void broadcastMessage(SyncProtocolMessage message, SocketAddress sender) {
        this.connectedServers.forEach((address, connectionManager) -> {
            if (!address.equals(sender)) {
                try {
                    ObjectOutputStream out = connectionManager.getObjectOutputStream();
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

            SocketConnection socketConnection = new SocketConnection(socket, out, in);
            this.connectedServers.put(remoteAddress, socketConnection);

            for (; ; ) {
                try {
                    SyncProtocolMessage message = (SyncProtocolMessage) in.readObject();
                    logger.info("Received message: " + message.topic() + " from " + remoteAddress);
                    this.broadcastMessage(message, remoteAddress);
                } catch (ClassNotFoundException e) {
                    logger.warning("Invalid message format from " + remoteAddress + ": " + e.getMessage());
                } catch (IOException e) {
                    logger.warning("SocketConnection error with " + remoteAddress + ": " + e.getMessage());
                    try {
                        logger.info("Closing socketConnection with " + remoteAddress);
                        socketConnection.close();
                    } catch (Exception ignored) {
                        // Ignore close exceptions
                    }
                }
            }
        } catch (IOException e) {
            logger.warning("SocketConnection error with " + remoteAddress + ": " + e.getMessage());
        } finally {
            SocketConnection conn = this.connectedServers.remove(remoteAddress);
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