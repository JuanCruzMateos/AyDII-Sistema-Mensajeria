package org.grupouno.network.server;

import org.grupouno.model.connection.ConnectionManager;
import org.grupouno.model.conversation.IConversationService;
import org.grupouno.model.directory.IDirectory;
import org.grupouno.network.handlers.ClientHandlerImpl;
import org.grupouno.network.hearthbeat.Heartbeat;
import org.grupouno.network.sync.SyncService;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.logging.Logger;

public class ChatServerImpl implements IChatServer {
    private static final Logger logger = Logger.getLogger(ChatServerImpl.class.getName());
    private final String serverAddress;
    private final int serverPort;
    private final IDirectory directory;
    private final IConversationService pendingMessages;
    private final HashMap<String, ConnectionManager> connectedClients;
    private final Heartbeat heartbeat;
    private final SyncService syncService;

    public ChatServerImpl(String serverAddress, int serverPort, IDirectory directory, IConversationService pendingMessages, HashMap<String, ConnectionManager> connectedClients, Heartbeat heartbeat, SyncService syncService) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.directory = directory;
        this.pendingMessages = pendingMessages;
        this.connectedClients = connectedClients;
        this.heartbeat = heartbeat;
        this.syncService = syncService;
    }

    @Override
    public void startServer() {
        logger.info("Starting heartbeat service...");
        Thread heartbeatThread = new Thread(heartbeat);
        heartbeatThread.start();

        logger.info("Starting sync service...");
        Thread syncThread = new Thread(syncService);
        syncThread.start();

        logger.info("Starting client service on port " + serverPort);
        try (ServerSocket serverSocket = new ServerSocket(serverPort, 50, InetAddress.getByName(this.serverAddress))) {
            logger.info("Waiting for connections... ");
            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    logger.info("Accepted connection from " + clientSocket.getInetAddress() + ":" + clientSocket.getPort());
                    new Thread(new ClientHandlerImpl(clientSocket, this.directory, this.pendingMessages, this.connectedClients, this.syncService)).start();
                } catch (IOException e) {
                    logger.warning("Error accepting connection: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            logger.warning("Error starting server: " + e.getMessage());
        }
    }
}
