package org.grupouno.network;


import org.grupouno.controller.ChatController;
import org.grupouno.model.conversation.Message;
import org.grupouno.model.conversation.MessageType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.logging.Logger;

public class ChatClientImpl implements IChatClient, Runnable {
    private final Logger logger = Logger.getLogger(ChatClientImpl.class.getName());
    private final Socket socket;
    private final ObjectInputStream inputStream;
    private final ObjectOutputStream outputStream;
    private final ChatController chatController;

    public ChatClientImpl(Socket socket, ChatController chatController) throws IOException {
        this.socket = socket;
        this.outputStream = new ObjectOutputStream(socket.getOutputStream());
        this.outputStream.flush();
        this.inputStream = new ObjectInputStream(socket.getInputStream());
        this.chatController = chatController;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Message message = (Message) inputStream.readObject();
                logger.info("Received message: " + message.type());
                // REGISTER, DISCONNECT, GET_DIRECTORY are the requests send to the server
                switch (message.type()) {
                    case MESSAGE -> this.chatController.receiveMessage(message);
                    case DIRECTORY -> this.chatController.updateDirectory(message);
                    case ERROR -> logger.warning("Error receiving message: " + message.type());
                    case MESSAGE_ACK, DISCONNECT_ACK, REGISTER_ACK -> {
                        // none of these messages should be handled by the client
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            logger.warning("Error receiving message: " + e.getMessage());
        } finally {
            this.close();
        }
    }

    @Override
    public synchronized void sendMessage(Message message) {
        logger.info("Sending message: " + message);
        try {
            outputStream.writeObject(message);
            outputStream.flush(); // Ensure the stream is flushed after writing
            logger.info("Message sent");
        } catch (IOException e) {
            logger.warning("Error during message communication: " + e.getMessage());
        }
    }

    @Override
    public void registerWithServer(String nickname, String ip, int port) {
        Message message = new Message(
                nickname, ip, port, null,
                socket.getInetAddress().getHostAddress(),
                socket.getPort(), null,
                LocalDateTime.now(), MessageType.REGISTER
        );
        this.sendMessage(message);
    }

    @Override
    public synchronized void getConnectedUsers(String nickname) {
        Message message = new Message(
                nickname, socket.getLocalAddress().getHostAddress(),
                socket.getLocalPort(), null,
                socket.getInetAddress().getHostAddress(),
                socket.getPort(), null,
                LocalDateTime.now(), MessageType.GET_DIRECTORY
        );
        this.sendMessage(message);
    }


    @Override
    public void disconnect(String nickname) {
        Message message = new Message(
                nickname, socket.getLocalAddress().getHostAddress(),
                socket.getLocalPort(), null,
                socket.getInetAddress().getHostAddress(),
                socket.getPort(), null,
                LocalDateTime.now(), MessageType.DISCONNECT
        );
        this.sendMessage(message);
    }

    @Override
    public synchronized void close() {
        try {
            if (inputStream != null) inputStream.close();
            if (outputStream != null) outputStream.close();
            if (socket != null && !socket.isClosed()) {
                socket.setReuseAddress(true);
                socket.close();
            }
            logger.info("Connection closed");
        } catch (IOException e) {
            logger.warning("Error closing connection: " + e.getMessage());
        }
    }

}
