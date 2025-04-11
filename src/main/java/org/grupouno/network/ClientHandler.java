package org.grupouno.network;

// necesito la referencia a la clase ChatSession aca!

import org.grupouno.controller.ChatController;
import org.grupouno.model.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Logger;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final ChatController chatController;
    private final Logger logger = Logger.getLogger(ClientHandler.class.getName());

    public ClientHandler(Socket socket, ChatController chatController) {
        this.socket = socket;
        this.chatController = chatController;
    }

    @Override
    public void run() {
        try (ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            Message message = (Message) in.readObject();
            this.chatController.receiveMessage(message);
            this.logger.info("Received message: " + message);
            out.flush();
        } catch (IOException | ClassNotFoundException e) {
            logger.warning("Error handling client: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                logger.warning("Error closing socket: " + e.getMessage());
            }
        }
    }
}