package org.grupouno.network.handler;

import org.grupouno.model.agenda.IAgenda;
import org.grupouno.model.agenda.User;
import org.grupouno.model.conversation.Conversation;
import org.grupouno.model.conversation.IConversationService;
import org.grupouno.model.conversation.Message;
import org.grupouno.model.conversation.MessageType;
import org.grupouno.network.connections.ConnectionManager;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Logger;

public class ClientHandlerImpl implements Runnable, IClientHandler {
    private final Logger logger = Logger.getLogger(ClientHandlerImpl.class.getName());
    private final ConnectionManager connection;
    private final IAgenda directory;
    private final IConversationService pendingMessages;
    private final HashMap<String, ConnectionManager> connectedClients;


    public ClientHandlerImpl(Socket socket, IAgenda directory, IConversationService pendingMessages, HashMap<String, ConnectionManager> connectedClients) throws IOException {
        this.connection = new ConnectionManager(socket, new ObjectOutputStream(socket.getOutputStream()), new ObjectInputStream(socket.getInputStream()));
        this.directory = directory;
        this.pendingMessages = pendingMessages;
        this.connectedClients = connectedClients;
    }

    @Override
    public void run() {
        try {
            ObjectInputStream in = this.connection.objectInputStream();
            Message message = (Message) in.readObject();
            logger.info("Received message from " + message.senderNickname() + ": " + message.type());
            this.registerNewConnection(message);
            this.sendPendingMessages(message.senderNickname());
            while (message.type() != MessageType.DISCONNECT) {
                switch (message.type()) {
                    case GET_DIRECTORY -> this.getDirectoryContacts(message);
                    case MESSAGE -> this.forwardMessage(message);
                }
                message = (Message) in.readObject();
                logger.info("Received message from " + message.senderNickname() + ": " + message.type());
            }
            this.removeConnection(message.senderNickname());
        } catch (IOException | ClassNotFoundException e) {
            logger.warning("Error handling client: " + e.getMessage());
        }
    }

    @Override
    public synchronized void registerNewConnection(Message message) {
        boolean isValid = message.type() == MessageType.REGISTER &&
                message.receiverIP().equals(this.connection.socket().getLocalAddress().toString().substring(1)) &&
                message.receiverPort() == this.connection.socket().getLocalPort();
        if (!isValid) {
            logger.warning("Invalid connection attempt from " + message.senderNickname());
            this.serverResponse(message.senderNickname(), "Invalid connection attempt", MessageType.ERROR);
        } else {
            this.connectedClients.put(message.senderNickname(), this.connection);
            this.directory.addContact(new User(message.senderNickname(), message.senderIP(), message.senderPort()));
            logger.info("New connection registered: " + message.senderNickname());
            this.serverResponse(message.senderNickname(), "Connection successful", MessageType.REGISTER_ACK);
        }
    }

    @Override
    public synchronized void removeConnection(String nickname) throws IOException {
        logger.info("Removing connection from " + nickname);
        this.serverResponse(nickname, "Disconnected", MessageType.DISCONNECT_ACK);
        ConnectionManager clientConnection = this.connectedClients.get(nickname);
        if (clientConnection != null) {
            try {
                if (clientConnection.socket() != null && !clientConnection.socket().isClosed()) {
                    clientConnection.socket().close();
                }
                if (clientConnection.objectOutputStream() != null) {
                    clientConnection.objectOutputStream().close();
                }
                if (clientConnection.objectInputStream() != null) {
                    clientConnection.objectInputStream().close();
                }
            } catch (IOException e) {
                logger.warning("Error closing resources for " + nickname + ": " + e.getMessage());
            }
            this.connectedClients.remove(nickname);
        }
        this.directory.removeContact(nickname);
        if (this.connectedClients.containsKey(nickname)) {
            logger.warning("Failed to remove connection for " + nickname);
        } else {
            logger.info("Connection removed: " + nickname);
        }
        if (this.directory.getContactByNickname(nickname) != null) {
            logger.warning("Failed to remove contact from directory for " + nickname);
        } else {
            logger.info("Contact removed from directory: " + nickname);
        }
        logger.info("Current connected clients: " + this.connectedClients.keySet());
    }

    @Override
    public synchronized void sendPendingMessages(String nickname) {
        Conversation pendingMessages = this.pendingMessages.getConversationByContactNickname(nickname);
        if (pendingMessages != null) {
            try {
                ObjectOutputStream out = this.connectedClients.get(nickname).objectOutputStream();
                for (Message message : pendingMessages.messages()) {
                    logger.info("Sending pending message to " + nickname + ": " + message);
                    out.writeObject(message);
                    out.flush();
                    this.serverResponse(message.senderNickname(), "Message received", MessageType.MESSAGE_ACK);
                }
                logger.info("Pending messages sent to " + nickname);
            } catch (IOException e) {
                logger.warning("Error sending pending messages to " + nickname + ": " + e.getMessage());
            }
        } else {
            logger.info("No pending messages for " + nickname);
        }
    }

    @Override
    public synchronized void addMessageToPendingMessages(Message message) {
        if (!pendingMessages.existsConversationWith(message.receiverNickname())) {
            logger.info("Starting new conversation for " + message.receiverNickname());
            pendingMessages.startNewConversation(message.receiverNickname());
        } else {
            logger.info("Adding message to pending messages for " + message.receiverNickname());
            pendingMessages.addMessage(message, message.receiverNickname());
        }
    }

    @Override
    public synchronized void forwardMessage(Message message) {
        logger.info("DEBUG: " + !this.connectedClients.containsKey(message.receiverNickname()));
        logger.info(this.connectedClients.keySet().toString());
        if (!this.connectedClients.containsKey(message.receiverNickname())) {
            logger.warning("Client " + message.receiverNickname() + " not connected. Adding message to pending messages.");
            this.addMessageToPendingMessages(message);
        } else {
            try {
                ObjectOutputStream out = this.connectedClients.get(message.receiverNickname()).objectOutputStream();
                out.writeObject(message);
                out.flush();
                logger.info("Message forwarded to " + message.receiverNickname());
            } catch (IOException e) {
                logger.warning("Error forwarding message to " + message.receiverNickname() + ": " + e.getMessage());
            }
        }
    }

    @Override
    public synchronized void getDirectoryContacts(Message message) {
        Set<User> activeUsers = this.directory.getContacts();
        this.serverResponse(message.senderNickname(), activeUsers, MessageType.DIRECTORY);
    }

    @Override
    public synchronized void serverResponse(String nickname, Object content, MessageType type) {
        try {
            Socket clientSocket = this.connectedClients.get(nickname).socket();
            ObjectOutputStream out = this.connectedClients.get(nickname).objectOutputStream();
            out.writeObject(new Message("ChatServer", clientSocket.getLocalAddress().toString().substring(1), clientSocket.getLocalPort(), nickname, clientSocket.getInetAddress().toString().substring(1), clientSocket.getPort(), content, LocalDateTime.now(), type));
            out.flush();
            logger.info("Server response sent to " + nickname + ": " + type);
        } catch (IOException e) {
            logger.warning("Error sending server response to " + nickname + ": " + e.getMessage());
        }
    }
}
