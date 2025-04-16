package org.grupouno.network;

import org.grupouno.exceptions.ClientNotConnectedException;
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
import java.util.List;
import java.util.logging.Logger;

public class ClientHandlerImpl implements Runnable, IClientHandler {
    private final Logger logger = Logger.getLogger(ClientHandlerImpl.class.getName());
    private final Socket socket;
    private final IAgenda directory;
    private final IConversationService pendingMessages;

    public ClientHandlerImpl(Socket socket, IAgenda directory, IConversationService pendingMessages) {
        this.socket = socket;
        this.directory = directory;
        this.pendingMessages = pendingMessages;
    }

    @Override
    public Message listenForNewMessages(ObjectInputStream in) throws IOException, ClassNotFoundException {
        Message message = (Message) in.readObject();
        this.logger.info("Received message: " + message);
        return message;
    }


    @Override
    public void registerNewConnectionToDirectory(Message message, ObjectOutputStream out) {
        boolean isValid = message.type() == MessageType.REGISTER && message.receiverIP().equals(socket.getInetAddress().getHostAddress()) && message.receiverPort() == socket.getPort();
        if (isValid) {
            this.directory.addContact(new User(message.senderNickname(), message.senderIP(), message.senderPort()));
            try {
                this.sendMessageToClient(new Message(
                        "ChatServerImpl",
                        this.socket.getInetAddress().getHostAddress(),
                        this.socket.getPort(),
                        message.senderNickname(),
                        message.senderIP(),
                        message.senderPort(),
                        "Connection registered successfully",
                        LocalDateTime.now(),
                        MessageType.REGISTER_ACK), out);
            } catch (ClientNotConnectedException e) {
                logger.warning("Error sending registration acknowledgment: " + e.getMessage());
            }
            this.sendPendingMessages(message.senderNickname(), out);
        } else {
            try {
                this.sendMessageToClient(new Message(
                        "ChatServer",
                        this.socket.getInetAddress().getHostAddress(),
                        this.socket.getPort(),
                        message.senderNickname(),
                        message.senderIP(),
                        message.senderPort(),
                        "Invalid message type: you must send a REGISTER message",
                        LocalDateTime.now(),
                        MessageType.ERROR), out);
            } catch (ClientNotConnectedException e) {
                logger.warning("Error sending error message: " + e.getMessage());
            }
        }
    }

    @Override
    public void removeConnectionFromDirectory(String nickname) {
        this.directory.removeContact(nickname);
        this.logger.info("Removed contact: " + nickname);
    }

    @Override
    public void sendPendingMessages(String nickname, ObjectOutputStream out) {
        this.logger.info("Checking for pending messages for: " + nickname);
        if (this.pendingMessages.existsConversationWith(nickname)) {
            Conversation conversation = this.pendingMessages.getConversationByContactNickname(nickname);
            for (Message message : conversation.messages()) {
                try {
                    this.sendMessageToClient(message, out);
                } catch (ClientNotConnectedException e) {
                    logger.warning("Error sending pending message: " + e.getMessage());
                }
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
    public void sendMessageToClient(Message message, ObjectOutputStream out) throws ClientNotConnectedException {
        try {
            out.writeObject(message);
            out.flush();
            logger.info("Message sent to client: " + message);
        } catch (IOException e) {
            this.logger.warning("Error sending message to client: " + e.getMessage() + ". Saving message to pending messages.");
            throw new ClientNotConnectedException("Cannot send message to client: " + message.receiverNickname(), e);
        }
    }

    @Override
    public void getDirectoryContacts(Message message, ObjectOutputStream out) {
        List<String> contacts = this.directory.getContactNicknames();
        String contactsString = String.join(",", contacts);
        try {
            this.sendMessageToClient(new Message(
                    "ChatServer",
                    this.socket.getInetAddress().getHostAddress(),
                    this.socket.getPort(),
                    message.senderNickname(),
                    message.senderIP(),
                    message.senderPort(),
                    contactsString,
                    LocalDateTime.now(),
                    MessageType.GET_DIRECTORY), out);
        } catch (ClientNotConnectedException e) {
            logger.warning("Error sending directory contacts: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        Message message;
        try (ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            message = this.listenForNewMessages(in);
            this.registerNewConnectionToDirectory(message, out);
            while (message.type() != MessageType.DISCONNECT) {
                message = this.listenForNewMessages(in);
                if (message.type() == MessageType.MESSAGE) {
                    try {
                        this.sendMessageToClient(message, out);
                    } catch (ClientNotConnectedException e) {
                        this.addMessageToPendingMessages(message);
                    }
                } else if (message.type() == MessageType.GET_DIRECTORY) {
                    this.getDirectoryContacts(message, out);
                }
            }
            this.removeConnectionFromDirectory(message.senderNickname());
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
}