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

    public synchronized void broadcastMessage(SyncProtocolMessage message, SocketAddress sender) {
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
                        this.connectedServers.remove(address);
                        logger.info("Closed connection with " + address + " due to send error");
                    } catch (Exception ignored) {
                        // Ignore close exceptions
                    }
                }
            }
        });
    }

    @Override
    public void run() {
        SocketAddress remoteAddress = socket.getRemoteSocketAddress();
        try {
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            SocketConnection socketConnection = new SocketConnection(socket, out, in);
            this.connectedServers.put(remoteAddress, socketConnection);

            try {
                for (; ; ) {
                    SyncProtocolMessage message = (SyncProtocolMessage) in.readObject();
                    logger.info("Received message: " + message.topic() + " from " + remoteAddress);
                    this.broadcastMessage(message, remoteAddress);
                }
            } catch (ClassNotFoundException e) {
                logger.warning("Invalid message format from " + remoteAddress + ": " + e.getMessage());
            } catch (IOException e) {
                logger.warning("Server disconnected " + remoteAddress);
                try {
                    logger.info("Closing connection with " + remoteAddress);
                    this.connectedServers.remove(remoteAddress);
                    socketConnection.close();
                } catch (Exception ignored) {
                    //
                }
            }
        } catch (IOException e) {
            logger.warning("Error with streams with " + remoteAddress + ": " + e.getMessage());
        } finally {
            SocketConnection conn = this.connectedServers.remove(remoteAddress);
            if (conn != null) {
                try {
                    conn.close();
                    logger.info("Closed connection with " + remoteAddress);
                } catch (Exception ignored) {
                    //
                }
            }
        }
    }
}