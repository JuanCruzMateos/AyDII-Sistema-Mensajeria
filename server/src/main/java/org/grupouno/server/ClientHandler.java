package org.grupouno.server;

import org.grupouno.model.agenda.IAgenda;
import org.grupouno.model.agenda.User;
import org.grupouno.model.conversation.Conversation;
import org.grupouno.model.conversation.IConversationService;
import org.grupouno.model.conversation.Message;
import org.grupouno.model.conversation.MessageType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.logging.Logger;

public class ClientHandler implements Runnable, IClientHandler {
    private final Logger logger = Logger.getLogger(ClientHandler.class.getName());
    private final Socket socket;
    private final IAgenda directory;
    private final IConversationService pendingMessages;

    public ClientHandler(Socket socket, IAgenda directory, IConversationService pendingMessages) {
        this.socket = socket;
        this.directory = directory;
        this.pendingMessages = pendingMessages;
    }

    @Override
    public void run() {
        Message message;
        try (ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            this.registerNewConnectionToDirectory(out, in);
            do {
                message = this.listenForMessage(in);
                this.logger.info("Received message: " + message);
                switch (message.type()) {
                    case MessageType.MESSAGE -> {
                        this.logger.info("Received message: " + message);
                        this.addMessageToPendingMessages(message);
                        this.sendMessageToClient(new Message(
                                message.senderNickname(),
                                message.senderIP(),
                                message.senderPort(),
                                message.receiverNickname(),
                                message.receiverIP(),
                                message.receiverPort(),
                                message.content(),
                                LocalDateTime.now(),
                                MessageType.MESSAGE_ACK
                        ), out);
                    }
                }
            } while (message.type() != MessageType.DISCONNECT);
        } catch (IOException | ClassNotFoundException e) {
            logger.warning("Error handling client: " + e.getMessage());
        } finally {
            try {
                socket.close();
                logger.info("Closed connection to client: " + socket.getInetAddress() + ":" + socket.getPort());
            } catch (IOException e) {
                logger.warning("Error closing socket: " + e.getMessage());
            }
        }
    }

    @Override
    public Message listenForMessage(ObjectInputStream in) throws IOException, ClassNotFoundException {
        Message message = (Message) in.readObject();
        this.logger.info("Received message: " + message);
        return message;
    }


    @Override
    public void registerNewConnectionToDirectory(ObjectOutputStream out, ObjectInputStream in) throws IOException, ClassNotFoundException {
        Message message = (Message) in.readObject();
        this.logger.info("Received message: " + message);
        if (message.type() == MessageType.REGISTER) {
            this.directory.addContact(new User(message.senderNickname(), message.senderIP(), message.senderPort()));
            this.sendMessageToClient(new Message(
                    "ChatServer",
                    this.socket.getInetAddress().getHostAddress(),
                    this.socket.getPort(),
                    message.senderNickname(),
                    message.senderIP(),
                    message.senderPort(),
                    "Connection registered successfully",
                    LocalDateTime.now(),
                    MessageType.REGISTER_ACK), out);
            this.checkForPendingMessages(message.senderNickname(), out);
        } else {
            this.sendMessageToClient(new Message(
                    "ChatServer",
                    this.socket.getInetAddress().getHostAddress(),
                    this.socket.getPort(),
                    message.senderNickname(),
                    message.senderIP(),
                    message.senderPort(),
                    "Invalid message type",
                    LocalDateTime.now(),
                    MessageType.ERROR
            ), out);
        }
    }

    @Override
    public void removeConnectionFromDirectory(String nickname) {
        this.directory.removeContact(nickname);
        this.logger.info("Removed contact: " + nickname);
    }

    @Override
    public void checkForPendingMessages(String nickname, ObjectOutputStream out) {
        this.logger.info("Checking for pending messages for: " + nickname);
        if (this.pendingMessages.existsConversationWith(nickname)) {
            Conversation conversation = this.pendingMessages.getConversationByContactNickname(nickname);
            try {
                for (Message message : conversation.messages()) {
                    if (message.type() == MessageType.MESSAGE) {
                        this.sendMessageToClient(new Message(
                                message.senderNickname(),
                                message.senderIP(),
                                message.senderPort(),
                                message.receiverNickname(),
                                message.receiverIP(),
                                message.receiverPort(),
                                message.content(),
                                LocalDateTime.now(),
                                MessageType.MESSAGE_ACK
                        ), out);
                    }
                }
            } catch (IOException e) {
                logger.warning("Error sending pending messages: " + e.getMessage());
            }
        }
    }

    @Override
    public void addMessageToPendingMessages(Message message) {
        if (!this.pendingMessages.existsConversationWith(message.receiverNickname())) {
            this.pendingMessages.startNewConversation(message.receiverNickname());
        }
        this.pendingMessages.addMessage(message, message.receiverNickname());
    }

    @Override
    public void sendMessageToClient(Message message, ObjectOutputStream out) {
        try {
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            this.logger.warning("Error sending message to client: " + e.getMessage());
            this.addMessageToPendingMessages(message);
        }
    }
}