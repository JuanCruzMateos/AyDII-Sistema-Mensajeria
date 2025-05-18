package org.grupouno.network.sync;

import org.grupouno.model.connection.ConnectionManager;
import org.grupouno.model.conversation.IConversation;
import org.grupouno.model.conversation.IConversationService;
import org.grupouno.model.conversation.Message;
import org.grupouno.model.directory.IDirectory;
import org.grupouno.model.directory.User;
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

public class SyncService implements Runnable {
    private final Logger logger = Logger.getLogger(SyncService.class.getName());
    private final String serverAddress;
    private final int syncPort;
    private final String brokerAddress;
    private final int brokerPort;
    private final IDirectory directory;
    private final IConversationService pendingMessages;
    private ConnectionManager connectionManager;

    public SyncService(String serverAddress, int syncPort, String brokerAddress, int brokerPort, IDirectory directory, IConversationService pendingMessages) {
        this.serverAddress = serverAddress;
        this.syncPort = syncPort;
        this.brokerAddress = brokerAddress;
        this.brokerPort = brokerPort;
        this.directory = directory;
        this.pendingMessages = pendingMessages;
    }

    public void publishEvent(Message message, Topic topic) {
        try {
            SyncProtocolMessage syncMessage = new SyncProtocolMessage(topic, message);
            this.connectionManager.objectOutputStream().writeObject(syncMessage);
            this.connectionManager.objectOutputStream().flush();
        } catch (IOException e) {
            logger.severe("Error sending sync message: " + e.getMessage());
        }
    }

    public synchronized void syncAll(Message message) {
        this.pendingMessages.getAllConversations().forEach(conversation -> conversation.getMessages().forEach(msg -> {
            if (msg.receiverNickname().equals(message.senderNickname())) {
                this.publishEvent(msg, Topic.SYNC_MESSAGE);
            }
        }));
        this.directory.getContacts().forEach(contact -> {
            if (contact.nickname().equals(message.senderNickname())) {
                this.publishEvent(new Message(contact.nickname(), contact.ip(), contact.port(), null, null, 0, null, null, null), Topic.SYNC_USER);
            }
        });
        this.logger.info("Sync all messages for " + message.senderNickname());
    }

    @Override
    public void run() {
        try {
            logger.info("Starting Sync Service on " + this.serverAddress + ":" + this.syncPort + " to " + this.brokerAddress + ":" + this.brokerPort);
            Socket socket = new Socket();
            socket.setReuseAddress(Boolean.TRUE);
//            socket.setSoTimeout(10000);
            socket.bind(new InetSocketAddress(InetAddress.getByName(this.serverAddress), this.syncPort));
            socket.connect(new InetSocketAddress(InetAddress.getByName(this.brokerAddress), this.brokerPort));

            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            this.connectionManager = new ConnectionManager(socket, out, in);

            while (true) {
                SyncProtocolMessage syncMessage = (SyncProtocolMessage) in.readObject();
                switch (syncMessage.topic()) {
                    case Topic.NEW_MESSAGE -> this.addNewMessage(syncMessage.message());
                    case Topic.REMOVE_MESSAGE -> this.removeMessage(syncMessage.message());
                    case Topic.USER_CONNECT -> this.userConnect(syncMessage.message());
                    case Topic.USER_DISCONNECT -> this.userDisconnect(syncMessage.message());
                    case Topic.SYNC_ALL -> this.syncAll(syncMessage.message());
                    case Topic.SYNC_MESSAGE -> this.syncMessage(syncMessage.message());
                    case Topic.SYNC_USER -> this.syncUser(syncMessage.message());
                }
            }
        } catch (ClassNotFoundException | IOException e) {
            logger.severe("Error handling sync service: " + e.getMessage());
        }
    }

    private synchronized void syncUser(Message message) {
        if (this.directory.isContactInAgenda(message.senderNickname())) {
            logger.info("User exists in agenda: " + message.senderNickname() + ". Ignoring sync.");
        } else {
            this.directory.addContact(new User(message.senderNickname(), message.senderIP(), message.senderPort()));
            logger.info("User synced: " + message.senderNickname());
        }
    }

    private synchronized void syncMessage(Message message) {
        if (this.pendingMessages.existsConversationWith(message.receiverNickname())) {
            Optional<IConversation> c = this.pendingMessages.getConversationByContactNickname(message.receiverNickname());
            if (c.isPresent()) {
                IConversation conversation = c.get();
                if (!conversation.existsMessage(message)) {
                    conversation.addMessage(message);
                    this.logger.info("Message synced for " + message.receiverNickname());
                } else {
                    this.logger.info("Message already exists for " + message.receiverNickname());
                }
            }
        } else {
            this.logger.warning("No conversation found for " + message.receiverNickname());
            this.pendingMessages.startNewConversation(message.receiverNickname());
            Optional<IConversation> cc = this.pendingMessages.getConversationByContactNickname(message.receiverNickname());
            if (cc.isPresent()) {
                IConversation conv = cc.get();
                conv.addMessage(message);
                this.logger.info("Message synced for " + message.receiverNickname());
            }
        }
    }

    private synchronized void userDisconnect(Message message) {
        this.directory.removeContact(message.senderNickname());
        this.logger.info("Connection removed: " + message.senderNickname());
    }

    private synchronized void userConnect(Message message) {
        this.directory.addContact(new User(message.senderNickname(), message.senderIP(), message.senderPort()));
        this.logger.info("New connection registered: " + message.senderNickname());
    }

    private synchronized void removeMessage(Message message) {
        if (this.pendingMessages.existsConversationWith(message.receiverNickname())) {
            Optional<IConversation> c = this.pendingMessages.getConversationByContactNickname(message.receiverNickname());
            if (c.isPresent()) {
                IConversation conversation = c.get();
                conversation.removeMessage(message);
                this.logger.info("Message removed from pending messages for " + message.receiverNickname());
            } else {
                this.logger.warning("No conversation found for " + message.receiverNickname());
            }
        } else {
            this.logger.warning("No conversation found for " + message.receiverNickname());
        }
    }

    private synchronized void addNewMessage(Message message) {
        if (!this.pendingMessages.existsConversationWith(message.receiverNickname())) {
            this.logger.info("Starting new conversation for " + message.receiverNickname());
            this.pendingMessages.startNewConversation(message.receiverNickname());
        }
        this.logger.info("Adding message to pending messages for " + message.receiverNickname());
        this.pendingMessages.addMessage(message, message.receiverNickname());
    }
}
