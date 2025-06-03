package org.grupouno.network.server;

import org.grupouno.model.connection.SocketConnection;
import org.grupouno.model.conversation.IConversationService;
import org.grupouno.model.directory.IDirectory;
import org.grupouno.network.handlers.ClientHandlerImpl;
import org.grupouno.network.hearthbeat.Heartbeat;
import org.grupouno.network.sync.SyncService;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.logging.Logger;

public class ChatServerImpl implements IChatServer {
    private static final Logger logger = Logger.getLogger(ChatServerImpl.class.getName());
    private final IDirectory directory;
    private final IConversationService pendingMessages;
    private final HashMap<String, SocketConnection> connectedClients;
    private final Heartbeat heartbeat;
    private final SyncService syncService;
    private final String serverAddress;
    private final int serverPort;

    public ChatServerImpl(String serverAddress, int serverPort, IDirectory directory, IConversationService pendingMessages, HashMap<String, SocketConnection> connectedClients, Heartbeat heartbeat, SyncService syncService) {
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

        //logger.info("Starting client service on port " + serverPort);
        try (ServerSocket serverSocket = new ServerSocket()) {
            serverSocket.setReuseAddress(true);
            serverSocket.bind(new InetSocketAddress(InetAddress.getByName(this.serverAddress), this.serverPort));
            //Esto debería hacerse así. No se puede ahora porque está muy hardcodeado...
            //serverSocket.bind(null);
            //serverPort = serverSocket.getLocalPort();
            //serverAddress = serverSocket.getInetAddress().getHostAddress();
            logger.info("Starting client service on " + serverAddress + ":" + serverPort);
            logger.info("Waiting for connections... ");
            for (; ; ) {
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
