package org.grupouno.network;

import org.grupouno.controller.ChatController;
import org.grupouno.model.conversation.Message;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

public class Peer implements Runnable {
    private final Logger logger = Logger.getLogger(Peer.class.getName());
    private final int serverPort;
    private final ChatController chatController;

    public Peer(int serverPort, ChatController chatController) {
        this.serverPort = serverPort;
        this.chatController = chatController;
    }

    @Override
    public void run() {
        logger.info("Starting server on port " + serverPort);
        try (ServerSocket serverSocket = new ServerSocket(serverPort)) {
            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    logger.info("Accepted connection from " + clientSocket.getInetAddress() + ":" + clientSocket.getPort());
                    new Thread(new ClientHandler(clientSocket, chatController)).start();
                } catch (IOException e) {
                    logger.warning("Error accepting connection: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            logger.warning("Error starting server: " + e.getMessage());
        }
    }

    public void sendMessage(Message message, String recipientIp, int recipientPort) {
        try (Socket clientSocket = new Socket(recipientIp, recipientPort);
             ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream())) {
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            logger.warning("Error sending message: " + e.getMessage());
        }
    }
}