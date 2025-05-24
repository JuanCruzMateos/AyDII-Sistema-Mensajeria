package org.grupouno.network.handlers;

import org.grupouno.model.connection.SocketConnection;
import org.grupouno.model.conversation.IConversation;
import org.grupouno.model.conversation.IConversationService;
import org.grupouno.model.directory.IDirectory;
import org.grupouno.model.directory.User;
import org.grupouno.model.protocols.Message;
import org.grupouno.model.protocols.MessageType;
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
    private static final Logger logger = Logger.getLogger(ClientHandlerImpl.class.getName());
    private final SocketConnection socketConnection;
    private final IDirectory directory;
    private final IConversationService pendingMessages;
    private final Map<String, SocketConnection> connectedClients;
    private final SyncService syncService;


    public ClientHandlerImpl(Socket socket, IDirectory directory, IConversationService pendingMessages, Map<String, SocketConnection> connectedClients, SyncService syncService) throws IOException {
        this.socketConnection = new SocketConnection(socket, new ObjectOutputStream(socket.getOutputStream()), new ObjectInputStream(socket.getInputStream()));
        this.directory = directory;
        this.pendingMessages = pendingMessages;
        this.connectedClients = connectedClients;
        this.syncService = syncService;
    }

    @Override
    public void run() {
        try {
            ObjectInputStream in = this.socketConnection.getObjectInputStream();
            Message message = (Message) in.readObject();
            logger.info("Received message from " + message.senderNickname() + ": " + message.type());
            this.registerNewConnection(message);
            this.sendPendingMessages(message.senderNickname());
            while (message.type() != MessageType.DISCONNECT && this.socketConnection.getSocket().isConnected() && !this.socketConnection.getSocket().isClosed()) {
                switch (message.type()) {
                    case GET_DIRECTORY -> this.getDirectoryContacts(message);
                    case MESSAGE -> this.forwardMessage(message);
                    case REGISTER -> {
                        // let it pass
                    }
                    default -> logger.info("Unhandled message type: " + message.type());
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
                message.receiverIP().equals(this.socketConnection.getSocket().getLocalAddress().toString().substring(1)) &&
                message.receiverPort() == this.socketConnection.getSocket().getLocalPort();
        if (!isValid) {
            logger.warning("Invalid socketConnection attempt from " + message.senderNickname());
            this.serverResponse(message.senderNickname(), "Invalid socketConnection attempt", MessageType.ERROR);
        } else {
            if (!this.directory.isContactInAgenda(message.senderNickname())) {
                this.directory.addContact(new User(message.senderNickname(), message.senderIP(), message.senderPort()));
            }
            this.connectedClients.put(message.senderNickname(), this.socketConnection);
            this.syncService.publishEvent(message, Topic.USER_CONNECT);
            logger.info("New socketConnection registered: " + message.senderNickname());
            this.serverResponse(message.senderNickname(), "SocketConnection successful", MessageType.REGISTER_ACK);
        }
    }

    @Override
    public synchronized void removeConnection(String nickname) throws IOException {
        logger.info("Removing socketConnection from " + nickname);
        this.serverResponse(nickname, "Disconnected", MessageType.DISCONNECT_ACK);
        SocketConnection clientSocketConnection = this.connectedClients.remove(nickname);
        if (clientSocketConnection != null) {
            try {
                if (!clientSocketConnection.getSocket().isClosed()) {
                    clientSocketConnection.getSocket().close();
                }
                clientSocketConnection.getObjectOutputStream().close();
                clientSocketConnection.getObjectInputStream().close();
            } catch (IOException e) {
                logger.warning("Error closing resources for " + nickname + ": " + e.getMessage());
            }
        }
        this.directory.removeContact(nickname);
        if (!this.connectedClients.containsKey(nickname) && this.directory.getContactByNickname(nickname).isEmpty()) {
            this.syncService.publishEvent(new Message(nickname, null, 0, null, null, 0, null, null, null), Topic.USER_DISCONNECT);
            logger.info("Contact removed from directory: " + nickname);
        } else {
            logger.warning("Failed to remove contact from directory for " + nickname);
            this.serverResponse(nickname, "Disconnect Failed", MessageType.ERROR);
        }
        logger.info("Current connected clients: " + this.connectedClients.keySet());
    }

    @Override
    public synchronized void sendPendingMessages(String nickname) {
        Optional<IConversation> res = this.pendingMessages.getConversationByContactNickname(nickname);
        IConversation pendingMessages = res.orElse(null);
        SocketConnection clientSocketConnection = this.connectedClients.get(nickname);
        if (clientSocketConnection != null && pendingMessages != null && !pendingMessages.isEmpty()) {
            ObjectOutputStream out = clientSocketConnection.getObjectOutputStream();
            Iterator<Message> messageIterator = pendingMessages.iterator();
            while (messageIterator.hasNext()) {
                Message message = messageIterator.next();
                try {
                    logger.info("Sending pending message to " + nickname + ": " + message);
                    out.writeObject(message);
                    out.flush();
                    this.serverResponse(message.senderNickname(), "Message received", MessageType.MESSAGE_ACK);
                    messageIterator.remove();
                    this.syncService.publishEvent(message, Topic.REMOVE_MESSAGE);
                } catch (IOException e) {
                    logger.warning("Error sending pending messages to " + nickname + ": " + e.getMessage());
                }
            }
            if (pendingMessages.isEmpty()) {
                logger.info("All pending messages sent successfully to " + nickname);
            } else {
                logger.warning("Some pending messages could not be sent to " + nickname + ". Remain in server");
            }
        } else {
            logger.info("No pending messages for " + nickname);
        }
    }

    @Override
    public synchronized void addMessageToPendingMessages(Message message) {
        if (!this.pendingMessages.existsConversationWith(message.receiverNickname())) {
            logger.info("Starting new conversation for " + message.receiverNickname());
            this.pendingMessages.startNewConversation(message.receiverNickname());
        }
        logger.info("Adding message to pending messages for " + message.receiverNickname());
        this.pendingMessages.addMessage(message, message.receiverNickname());
    }

    @Override
    public synchronized void forwardMessage(Message message) {
        this.syncService.publishEvent(message, Topic.NEW_MESSAGE);
        logger.info("Message published to sync service");
        if (!this.connectedClients.containsKey(message.receiverNickname())) {
            logger.warning("Client " + message.receiverNickname() + " not connected. Adding message to pending messages.");
            this.addMessageToPendingMessages(message);
        } else {
            try {
                ObjectOutputStream out = this.connectedClients.get(message.receiverNickname()).getObjectOutputStream();
                out.writeObject(message);
                out.flush();
                logger.info("Message forwarded to " + message.receiverNickname());
                this.syncService.publishEvent(message, Topic.REMOVE_MESSAGE);
            } catch (IOException e) {
                logger.warning("Error forwarding message to " + message.receiverNickname() + ": " + e.getMessage());
            }
        }
    }

    @Override
    public synchronized void getDirectoryContacts(Message message) {
        Set<User> activeUsers = this.directory.getAllContacts();
        this.serverResponse(message.senderNickname(), activeUsers, MessageType.DIRECTORY);
    }

    @Override
    public synchronized void serverResponse(String nickname, Object content, MessageType type) {
        try {
            Socket clientSocket = this.connectedClients.get(nickname).getSocket();
            ObjectOutputStream out = this.connectedClients.get(nickname).getObjectOutputStream();
            out.writeObject(new Message("ChatServer",
                    clientSocket.getLocalAddress().toString().substring(1),
                    clientSocket.getLocalPort(), nickname,
                    clientSocket.getInetAddress().toString().substring(1),
                    clientSocket.getPort(), content, LocalDateTime.now(), type));
            out.flush();
            logger.info("Server response sent to " + nickname + ": " + type);
        } catch (IOException e) {
            logger.warning("Error sending server response to " + nickname + ": " + e.getMessage());
        }
    }
}
