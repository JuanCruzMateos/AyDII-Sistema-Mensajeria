package org.grupouno.network;

import org.grupouno.controller.ChatController;
import org.grupouno.model.conversation.Message;
import org.grupouno.model.conversation.MessageType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.time.LocalDateTime;
import java.util.logging.Logger;

public class ChatClientImpl implements IChatClient, Runnable {
    private static final Logger logger = Logger.getLogger(ChatClientImpl.class.getName());
    private final String clientName;
    private final String localAddress;
    private final int localPort;
    private final String monitorAddress;
    private final int monitorPort;
    private final ChatController chatController;
    private SocketAddress primaryServer;
    private Socket monitorSocket;
    private ObjectInputStream monitorInputStream;
    private ObjectOutputStream monitorOutputStream;
    private Socket serverSocket;
    private ObjectInputStream serverInputStream;
    private ObjectOutputStream serverOutputStream;

    public ChatClientImpl(String clientName, String localAddress, int localPort, String monitorAddress, int monitorPort, ChatController chatController) {
        this.clientName = clientName;
        this.localAddress = localAddress;
        this.localPort = localPort;
        this.monitorAddress = monitorAddress;
        this.monitorPort = monitorPort;
        this.chatController = chatController;
    }

    public void fetchPrimaryServerFromMonitor(int delay) {
        SocketAddress lastPrimaryServer = this.primaryServer;
        this.primaryServer = null;
        try {
            // TODO :: retry for a number of times
            while (this.primaryServer == null || !this.primaryServer.equals(lastPrimaryServer)) {
                this.monitorOutputStream.writeObject("primary.server");
                this.monitorOutputStream.flush();
                this.primaryServer = (SocketAddress) this.monitorInputStream.readObject();
                logger.info("Primary server address from monitor: " + primaryServer);
                if (this.primaryServer == null || this.primaryServer.equals(lastPrimaryServer)) {
                    logger.warning("Failed to fetch primary server address from monitor, retrying in " + delay + "ms");
//                    Thread.sleep(delay * (long) Math.pow(2, retries - 1));
                    Thread.sleep(delay);
                }
            }
        } catch (IOException | ClassNotFoundException | InterruptedException e) {
            logger.warning("Error getting primary server address: " + e.getMessage());
            this.primaryServer = null;
        }
    }

    public void connectToMonitor() {
        this.monitorSocket = new Socket();
        try {
            this.monitorSocket.setReuseAddress(true);
            this.monitorSocket.bind(new InetSocketAddress(localAddress, localPort));
            this.monitorSocket.connect(new InetSocketAddress(monitorAddress, monitorPort));
            this.monitorInputStream = new ObjectInputStream(this.monitorSocket.getInputStream());
            this.monitorOutputStream = new ObjectOutputStream(this.monitorSocket.getOutputStream());
            logger.info("Connected to monitor at " + monitorAddress + ":" + monitorPort);
        } catch (IOException e) {
            logger.warning("Error creating socket connection to monitor: " + e.getMessage());
        }
    }

    public void connectToServer() {
        this.serverSocket = new Socket();
        try {
            this.fetchPrimaryServerFromMonitor(1000);
            this.serverSocket.setReuseAddress(true);
            this.serverSocket.bind(new InetSocketAddress(localAddress, localPort));
            this.serverSocket.connect(primaryServer);
            this.serverInputStream = new ObjectInputStream(serverSocket.getInputStream());
            this.serverOutputStream = new ObjectOutputStream(serverSocket.getOutputStream());
            logger.info("Connected to primary server: " + primaryServer);
        } catch (IOException e) {
            logger.warning("Error connecting to primary server: " + e.getMessage());
        }
    }


    @Override
    public void run() {
        logger.info("Local address: " + this.localAddress + ":" + this.localPort);
        logger.info("Connecting to monitor at " + this.monitorAddress + ":" + this.monitorPort);

        this.connectToMonitor();
        this.connectToServer();
        this.registerWithServer(this.clientName, this.localAddress, this.localPort);

        boolean isConnected = true;
        while (isConnected) {
            try {
                Message message = (Message) this.serverInputStream.readObject();
                while (message.type() != MessageType.DISCONNECT_ACK) {
                    logger.info("Received message: " + message.type());
                    this.handleMessage(message);
                    message = (Message) this.serverInputStream.readObject();
                }
                logger.info("Received DISCONNECT_ACK, stopping client thread");
                isConnected = false;
                this.close();
            } catch (IOException | ClassNotFoundException e) {
                logger.warning("Error receiving message: " + e.getMessage());
                logger.info("Reconnecting to server...");
                this.close();
                this.connectToServer();
                this.registerWithServer(this.clientName, this.localAddress, this.localPort);
            }
        }
    }


    private void handleMessage(Message message) {
        switch (message.type()) {
            case MESSAGE -> this.chatController.receiveMessage(message);
            case DIRECTORY -> this.chatController.updateDirectory(message);
            case ERROR -> logger.warning("Error receiving message: " + message.type());
            case MESSAGE_ACK, REGISTER_ACK -> {
                // Handle acknowledgment messages
            }
            default -> logger.severe("Unkwnon message type: " + message.type());
        }
    }

    private Message createMessage(String nickname, String ip, int port, MessageType type) {
        return new Message(
                nickname, ip, port,
                null, this.serverSocket.getInetAddress().getHostAddress(), this.serverSocket.getPort(),
                null, LocalDateTime.now(), type
        );
    }

    @Override
    public synchronized void sendMessage(Message message) {
        try {
            this.serverOutputStream.writeObject(message);
            this.serverOutputStream.flush();
            logger.info("Message sent: " + message);
        } catch (IOException e) {
            logger.warning("Error sending message: " + e.getMessage());
        }
    }

    @Override
    public void registerWithServer(String nickname, String ip, int port) {
        this.sendMessage(this.createMessage(nickname, ip, port, MessageType.REGISTER));
    }

    @Override
    public synchronized void getConnectedUsers(String nickname) {
        this.sendMessage(this.createMessage(nickname, serverSocket.getLocalAddress().getHostAddress(), serverSocket.getLocalPort(), MessageType.GET_DIRECTORY));
    }

    @Override
    public synchronized void disconnect(String nickname) {
        this.sendMessage(this.createMessage(nickname, serverSocket.getLocalAddress().getHostAddress(), serverSocket.getLocalPort(), MessageType.DISCONNECT));
    }

    private synchronized void close() {
        try {
//            if (this.monitorInputStream != null) {
//                this.monitorInputStream.close();
//                logger.info("Input stream closed");
//            } else {
//                logger.info("Input stream is null");
//            }
//            if (this.monitorOutputStream != null) {
//                this.monitorOutputStream.close();
//                logger.info("Output stream closed");
//            } else {
//                logger.info("Output stream is null");
//            }
//            if (this.monitorSocket != null && !this.monitorSocket.isClosed()) {
//                logger.info("Closing socket: " + this.monitorSocket);
//                this.monitorSocket.setReuseAddress(true);
//                this.monitorSocket.close();
//            }
            if (this.serverInputStream != null) {
                this.serverInputStream.close();
                logger.info("Input stream closed");
            } else {
                logger.info("Input stream is null");
            }
            if (this.serverOutputStream != null) {
                this.serverOutputStream.close();
                logger.info("Output stream closed");
            } else {
                logger.info("Output stream is null");
            }
            if (this.serverSocket != null && !this.serverSocket.isClosed()) {
                logger.info("Closing socket: " + this.serverSocket);
                this.serverSocket.setReuseAddress(true);
                this.serverSocket.close();
                this.serverSocket = null;
            }
        } catch (IOException e) {
            logger.warning("Error closing connection: " + e.getMessage());
        }
    }
}