package org.grupouno.network.server;

import org.grupouno.model.conversation.IConversationService;
import org.grupouno.model.directory.IDirectory;
import org.grupouno.network.connections.ConnectionManager;
import org.grupouno.network.handler.ClientHandlerImpl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.logging.Logger;

public class ChatServerImpl implements IChatServer {
    private final Logger logger = Logger.getLogger(ChatServerImpl.class.getName());
    private final int serverPort;
    private final IDirectory directory;
    private final IConversationService pendingMessages;
    private final HashMap<String, ConnectionManager> connectedClients;

    public ChatServerImpl(int serverPort, IDirectory directory, IConversationService pendingMessages, HashMap<String, ConnectionManager> connectedClients) {
        this.serverPort = serverPort;
        this.directory = directory;
        this.pendingMessages = pendingMessages;
        this.connectedClients = connectedClients;
    }

    @Override
    public void startServer() {
        InetAddress localHost = null;
        try {
            localHost = InetAddress.getByName("127.0.0.1");
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        logger.info("Starting server on port " + serverPort);
        try (ServerSocket serverSocket = new ServerSocket(serverPort, 50, localHost)) {
            logger.info("Server started on port " + serverSocket.getLocalPort());
            logger.info("Waiting for connections... ");
            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    logger.info("Accepted connection from " + clientSocket.getInetAddress() + ":" + clientSocket.getPort());
                    logger.info("Local address: " + clientSocket.getLocalAddress() + ":" + clientSocket.getLocalPort());
                    new Thread(new ClientHandlerImpl(clientSocket, this.directory, this.pendingMessages, this.connectedClients)).start();
                } catch (IOException e) {
                    logger.warning("Error accepting connection: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            logger.warning("Error starting server: " + e.getMessage());
        }
    }
}
