package org.grupouno.network.handlers;

import org.grupouno.model.connection.ConnectionManager;
import org.grupouno.model.conversation.IConversation;
import org.grupouno.model.conversation.IConversationService;
import org.grupouno.model.conversation.Message;
import org.grupouno.model.conversation.MessageType;
import org.grupouno.model.directory.IDirectory;
import org.grupouno.model.directory.User;
import org.grupouno.model.protocols.Topic;
import org.grupouno.network.sync.SyncService;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;


public class ClientHandlerImpl implements Runnable, IClientHandler {
    private final Logger logger = Logger.getLogger(ClientHandlerImpl.class.getName());
    private final ConnectionManager connection;
    private final IDirectory directory;
    private final IConversationService pendingMessages;
    private final Map<String, ConnectionManager> connectedClients;
    private final SyncService syncService;


    public ClientHandlerImpl(Socket socket, IDirectory directory, IConversationService pendingMessages, Map<String, ConnectionManager> connectedClients, SyncService syncService) throws IOException {
        this.connection = new ConnectionManager(socket, new ObjectOutputStream(socket.getOutputStream()), new ObjectInputStream(socket.getInputStream()));
        this.directory = directory;
        this.pendingMessages = pendingMessages;
        this.connectedClients = connectedClients;
        this.syncService = syncService;
    }

    @Override
    public void run() {
        try {
            ObjectInputStream in = this.connection.objectInputStream();
            Message message = (Message) in.readObject();
            this.logger.info("Received message from " + message.senderNickname() + ": " + message.type());
            this.registerNewConnection(message);
            this.sendPendingMessages(message.senderNickname());
            while (message.type() != MessageType.DISCONNECT) {
                switch (message.type()) {
                    case GET_DIRECTORY -> this.getDirectoryContacts(message);
                    case MESSAGE -> this.forwardMessage(message);
                    default -> {
                        this.logger.warning("Unhandled message type: " + message.type());
                        logger.info(String.valueOf(message));
                    }
                }
                message = (Message) in.readObject();
                this.logger.info("Received message from " + message.senderNickname() + ": " + message.type());
                this.syncService.publishEvent(message, Topic.NEW_MESSAGE);
            }
            this.removeConnection(message.senderNickname());
            this.syncService.publishEvent(message, Topic.USER_DISCONNECT);
        } catch (IOException | ClassNotFoundException e) {
            this.logger.warning("Error handling client: " + e.getMessage());
        }
    }

    @Override
    public synchronized void registerNewConnection(Message message) {
        boolean isValid = message.type() == MessageType.REGISTER &&
                message.receiverIP().equals(this.connection.socket().getLocalAddress().toString().substring(1)) &&
                message.receiverPort() == this.connection.socket().getLocalPort();
        if (!isValid) {
            this.logger.warning("Invalid connection attempt from " + message.senderNickname());
            this.serverResponse(message.senderNickname(), "Invalid connection attempt", MessageType.ERROR);
        } else {
            this.connectedClients.put(message.senderNickname(), this.connection);
            this.directory.addContact(new User(message.senderNickname(), message.senderIP(), message.senderPort()));
            this.syncService.publishEvent(message, Topic.USER_CONNECT);
            this.logger.info("New connection registered: " + message.senderNickname());
            this.serverResponse(message.senderNickname(), "Connection successful", MessageType.REGISTER_ACK);
        }
    }

    @Override
    public synchronized void removeConnection(String nickname) throws IOException {
        this.logger.info("Removing connection from " + nickname);
        this.serverResponse(nickname, "Disconnected", MessageType.DISCONNECT_ACK);
        ConnectionManager clientConnection = this.connectedClients.remove(nickname);
        if (clientConnection != null) {
            try {
                if (!clientConnection.socket().isClosed()) {
                    clientConnection.socket().close();
                }
                clientConnection.objectOutputStream().close();
                clientConnection.objectInputStream().close();
            } catch (IOException e) {
                this.logger.warning("Error closing resources for " + nickname + ": " + e.getMessage());
            }
        }
        this.directory.removeContact(nickname);
        if (!this.connectedClients.containsKey(nickname) && this.directory.getContactByNickname(nickname).isEmpty()) {
            this.syncService.publishEvent(new Message(nickname, null, 0, null, null, 0, null, null, null), Topic.USER_DISCONNECT);
            this.logger.info("Contact removed from directory: " + nickname);
        } else {
            this.logger.warning("Failed to remove contact from directory for " + nickname);
            this.serverResponse(nickname, "Disconnect Failed", MessageType.ERROR);
        }
        this.logger.info("Current connected clients: " + this.connectedClients.keySet());
    }

    @Override
    public synchronized void sendPendingMessages(String nickname) {
        Optional<IConversation> res = this.pendingMessages.getConversationByContactNickname(nickname);
        IConversation pendingMessages = res.orElse(null);
        ConnectionManager clientConnection = this.connectedClients.get(nickname);
        if (clientConnection != null && pendingMessages != null && !pendingMessages.isEmpty()) {
            ObjectOutputStream out = clientConnection.objectOutputStream();
            Iterator<Message> messageIterator = pendingMessages.iterator();
            while (messageIterator.hasNext()) {
                Message message = messageIterator.next();
                try {
                    this.logger.info("Sending pending message to " + nickname + ": " + message);
                    out.writeObject(message);
                    out.flush();
                    this.serverResponse(message.senderNickname(), "Message received", MessageType.MESSAGE_ACK);
                    messageIterator.remove();
                    this.syncService.publishEvent(message, Topic.REMOVE_MESSAGE);
                } catch (IOException e) {
                    this.logger.warning("Error sending pending messages to " + nickname + ": " + e.getMessage());
                }
            }
            if (pendingMessages.isEmpty()) {
                this.logger.info("All pending messages sent successfully to " + nickname);
            } else {
                this.logger.warning("Some pending messages could not be sent to " + nickname + ". Remain in server");
            }
        } else {
            this.logger.info("No pending messages for " + nickname);
        }
    }

    @Override
    public synchronized void addMessageToPendingMessages(Message message) {
        if (!this.pendingMessages.existsConversationWith(message.receiverNickname())) {
            this.logger.info("Starting new conversation for " + message.receiverNickname());
            this.pendingMessages.startNewConversation(message.receiverNickname());
        }
        this.logger.info("Adding message to pending messages for " + message.receiverNickname());
        this.pendingMessages.addMessage(message, message.receiverNickname());
    }

    @Override
    public synchronized void forwardMessage(Message message) {
        this.syncService.publishEvent(message, Topic.NEW_MESSAGE);
        if (!this.connectedClients.containsKey(message.receiverNickname())) {
            this.logger.warning("Client " + message.receiverNickname() + " not connected. Adding message to pending messages.");
            this.addMessageToPendingMessages(message);
        } else {
            try {
                ObjectOutputStream out = this.connectedClients.get(message.receiverNickname()).objectOutputStream();
                out.writeObject(message);
                out.flush();
                this.logger.info("Message forwarded to " + message.receiverNickname());
                this.syncService.publishEvent(message, Topic.REMOVE_MESSAGE);
            } catch (IOException e) {
                this.logger.warning("Error forwarding message to " + message.receiverNickname() + ": " + e.getMessage());
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
            out.writeObject(new Message("ChatServer",
                    clientSocket.getLocalAddress().toString().substring(1),
                    clientSocket.getLocalPort(), nickname,
                    clientSocket.getInetAddress().toString().substring(1),
                    clientSocket.getPort(), content, LocalDateTime.now(), type));
            out.flush();
            this.logger.info("Server response sent to " + nickname + ": " + type);
        } catch (IOException e) {
            this.logger.warning("Error sending server response to " + nickname + ": " + e.getMessage());
        }
    }
}
