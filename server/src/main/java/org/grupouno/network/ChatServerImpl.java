package org.grupouno.network;

import org.grupouno.model.agenda.Agenda;
import org.grupouno.model.agenda.IAgenda;
import org.grupouno.model.conversation.ConversationService;
import org.grupouno.model.conversation.IConversationService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.logging.Logger;

public class ChatServerImpl implements IChatServer {
    private final Logger logger = Logger.getLogger(ChatServerImpl.class.getName());
    private final int serverPort;
    private final IAgenda directory;
    private final IConversationService pendingMessages;

    public ChatServerImpl(int serverPort) {
        this.serverPort = serverPort;
        this.directory = new Agenda(new HashMap<>());
        this.pendingMessages = new ConversationService(new HashMap<>());
    }

    @Override
    public void startServer() {
        logger.info("Starting server on port " + serverPort);
        try (ServerSocket serverSocket = new ServerSocket(serverPort)) {
            logger.info("Server started on port " + serverSocket.getLocalPort());
            logger.info("Waiting for connections... ");
            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    logger.info("Accepted connection from " + clientSocket.getInetAddress() + ":" + clientSocket.getPort());
                    new Thread(new ClientHandlerImpl(clientSocket, this.directory, this.pendingMessages)).start();
                } catch (IOException e) {
                    logger.warning("Error accepting connection: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            logger.warning("Error starting server: " + e.getMessage());
        }
    }
}