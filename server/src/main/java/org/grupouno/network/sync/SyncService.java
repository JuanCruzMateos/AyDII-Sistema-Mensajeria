package org.grupouno.network.sync;

import org.grupouno.model.connection.SocketConnection;
import org.grupouno.model.conversation.IConversation;
import org.grupouno.model.conversation.IConversationService;
import org.grupouno.model.directory.IDirectory;
import org.grupouno.model.directory.User;
import org.grupouno.model.protocols.Message;
import org.grupouno.model.protocols.SyncProtocolMessage;
import org.grupouno.model.protocols.Topic;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Optional;
import java.util.logging.Logger;

public class SyncService implements ISyncService, Runnable {
    private static final Logger logger = Logger.getLogger(SyncService.class.getName());
    private final String serverAddress;
    private final int syncPort;
    private final String brokerAddress;
    private final int brokerPort;
    private final IDirectory directory;
    private final IConversationService pendingMessages;
    private SocketConnection socketConnection;

    public SyncService(String serverAddress, int syncPort, String brokerAddress, int brokerPort, IDirectory directory, IConversationService pendingMessages) {
        this.serverAddress = serverAddress;
        this.syncPort = syncPort;
        this.brokerAddress = brokerAddress;
        this.brokerPort = brokerPort;
        this.directory = directory;
        this.pendingMessages = pendingMessages;
    }

    @Override
    public void publishEvent(Message message, Topic topic) {
        try {
            SyncProtocolMessage syncMessage = new SyncProtocolMessage(topic, message);
            this.socketConnection.getObjectOutputStream().writeObject(syncMessage);
            this.socketConnection.getObjectOutputStream().flush();
        } catch (IOException e) {
            logger.severe("Error sending sync message: " + e.getMessage());
        }
    }

    @Override
    public synchronized void syncAll() {
        this.pendingMessages.getAllConversations().forEach(conversation -> conversation.getMessages().forEach(msg -> this.publishEvent(msg, Topic.SYNC_MESSAGE)));
        this.directory.getAllContacts().forEach(contact -> this.publishEvent(new Message(contact.nickname(), contact.ip(), contact.port(), null, null, 0, null, null, null), Topic.SYNC_USER));
        logger.info("Sync all messages and users ");
    }

    @Override
    public void run() {
        try {
            logger.info("Starting Sync Service on " + this.serverAddress + ":" + this.syncPort + " to " + this.brokerAddress + ":" + this.brokerPort);
            Socket socket = new Socket();
            socket.setReuseAddress(true);
//            getSocket.setSoTimeout(10000);
            socket.bind(new InetSocketAddress(InetAddress.getByName(this.serverAddress), this.syncPort));
            socket.connect(new InetSocketAddress(InetAddress.getByName(this.brokerAddress), this.brokerPort));

            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            this.socketConnection = new SocketConnection(socket, out, in);

            // sync_all
            out.writeObject(new SyncProtocolMessage(Topic.SYNC_ALL, null));
            out.flush();
            logger.info("Sync all messages and users on startup");

            while (true) {
                SyncProtocolMessage syncMessage = (SyncProtocolMessage) in.readObject();
                switch (syncMessage.topic()) {
                    case Topic.NEW_MESSAGE -> this.addNewMessage(syncMessage.message());
                    case Topic.REMOVE_MESSAGE -> this.removeMessage(syncMessage.message());
                    case Topic.USER_CONNECT -> this.userConnect(syncMessage.message());
                    case Topic.USER_DISCONNECT -> this.userDisconnect(syncMessage.message());
                    case Topic.SYNC_ALL -> this.syncAll();
                    case Topic.SYNC_MESSAGE -> this.syncMessage(syncMessage.message());
                    case Topic.SYNC_USER -> this.syncUser(syncMessage.message());
                }
            }
        } catch (ClassNotFoundException | IOException e) {
            logger.severe("Error handling sync service: " + e.getMessage());
        }
    }

    @Override
    public synchronized void syncUser(Message message) {
        if (this.directory.isContactInAgenda(message.senderNickname())) {
            logger.info("User exists in agenda: " + message.senderNickname() + ". Ignoring sync.");
        } else {
            this.directory.addContact(new User(message.senderNickname(), message.senderIP(), message.senderPort()));
            logger.info("User synced: " + message.senderNickname());
        }
    }

    @Override
    public synchronized void syncMessage(Message message) {
        if (this.pendingMessages.existsConversationWith(message.receiverNickname())) {
            Optional<IConversation> c = this.pendingMessages.getConversationByContactNickname(message.receiverNickname());
            if (c.isPresent()) {
                IConversation conversation = c.get();
                if (!conversation.existsMessage(message)) {
                    conversation.addMessage(message);
                    logger.info("Message synced for " + message.receiverNickname());
                } else {
                    logger.info("Message already exists for " + message.receiverNickname());
                }
            }
        } else {
            logger.warning("No conversation found for " + message.receiverNickname());
            this.pendingMessages.startNewConversation(message.receiverNickname());
            Optional<IConversation> cc = this.pendingMessages.getConversationByContactNickname(message.receiverNickname());
            if (cc.isPresent()) {
                IConversation conv = cc.get();
                conv.addMessage(message);
                logger.info("Message synced for " + message.receiverNickname());
            }
        }
    }

    @Override
    public synchronized void userDisconnect(Message message) {
        if (this.directory.isContactInAgenda(message.senderNickname())) {
            this.directory.removeContact(message.senderNickname());
            logger.info("User disconnected: " + message.senderNickname());
        } else {
            logger.info("User not found in agenda: " + message.senderNickname());
        }
    }

    @Override
    public synchronized void userConnect(Message message) {
        if (!this.directory.isContactInAgenda(message.senderNickname())) {
            this.directory.addContact(new User(message.senderNickname(), message.senderIP(), message.senderPort()));
            logger.info("New socketConnection registered: " + message.senderNickname());
        } else {
            logger.info("User already exists in agenda: " + message.senderNickname());
        }
    }

    @Override
    public synchronized void removeMessage(Message message) {
        if (this.pendingMessages.existsConversationWith(message.receiverNickname())) {
            Optional<IConversation> c = this.pendingMessages.getConversationByContactNickname(message.receiverNickname());
            if (c.isPresent()) {
                IConversation conversation = c.get();
                conversation.removeMessage(message);
                logger.info("Message removed from pending messages for " + message.receiverNickname());
            } else {
                logger.warning("No conversation found for " + message.receiverNickname());
            }
        } else {
            logger.warning("No conversation found for " + message.receiverNickname());
        }
    }

    @Override
    public synchronized void addNewMessage(Message message) {
        if (!this.pendingMessages.existsConversationWith(message.receiverNickname())) {
            logger.info("Starting new conversation for " + message.receiverNickname());
            this.pendingMessages.startNewConversation(message.receiverNickname());
        }
        logger.info("Adding message to pending messages for " + message.receiverNickname());
        this.pendingMessages.addMessage(message, message.receiverNickname());
    }
}
