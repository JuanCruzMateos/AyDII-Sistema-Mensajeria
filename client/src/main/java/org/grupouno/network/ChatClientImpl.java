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
    private final Logger logger = Logger.getLogger(ChatClientImpl.class.getName());
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
        int retries = 0;
        SocketAddress primaryServer = null;

        try {
            while (primaryServer == null) {
                this.monitorOutputStream.writeObject("primary.server");
                this.monitorOutputStream.flush();
                primaryServer = (SocketAddress) this.monitorInputStream.readObject();
                this.logger.info("Primary server address from monitor: " + primaryServer);
                if (primaryServer == null) {
                    retries++;
                    Thread.sleep(delay * (long) Math.pow(2, retries - 1));
                }
            }
            this.primaryServer = primaryServer;
        } catch (IOException | ClassNotFoundException | InterruptedException e) {
            this.logger.warning("Error getting primary server address: " + e.getMessage());
            this.primaryServer = null;
        }
    }

    public void connectToMonitor(String localAddress, int localPort, String monitorAddress, int monitorPort) {
        this.monitorSocket = new Socket();
        try {
            this.monitorSocket.setReuseAddress(true);
            this.monitorSocket.bind(new java.net.InetSocketAddress(localAddress, localPort));
            this.monitorSocket.connect(new java.net.InetSocketAddress(monitorAddress, monitorPort));
            this.monitorInputStream = new ObjectInputStream(this.monitorSocket.getInputStream());
            this.monitorOutputStream = new ObjectOutputStream(this.monitorSocket.getOutputStream());
            logger.info("Connected to monitor at " + monitorAddress + ":" + monitorPort);
        } catch (IOException e) {
            logger.warning("Error creating socket connection to monitor: " + e.getMessage());
        }
    }

    public boolean connectToServer(SocketAddress primaryServer) {
        this.serverSocket = new Socket();
        try {
            this.serverSocket.setReuseAddress(true);
            this.serverSocket.bind(new InetSocketAddress(localAddress, localPort));
            this.serverSocket.connect(primaryServer);
            this.serverInputStream = new ObjectInputStream(serverSocket.getInputStream());
            this.serverOutputStream = new ObjectOutputStream(serverSocket.getOutputStream());
            logger.info("Connected to primary server: " + primaryServer);
            return true;
        } catch (IOException e) {
            logger.warning("Error connecting to primary server: " + e.getMessage());
            return false;
        }
    }


    @Override
    public void run() {
        logger.info("Starting client thread");
        logger.info("Connecting to monitor at " + this.monitorAddress + ":" + this.monitorPort);
        logger.info("Local address: " + this.localAddress + ":" + this.localPort);

        this.connectToMonitor(this.localAddress, this.localPort, this.monitorAddress, this.monitorPort);
        this.fetchPrimaryServerFromMonitor(1000);
        this.connectToServer(this.primaryServer);
        this.registerWithServer(this.clientName, this.localAddress, this.localPort);

        while (true) {
            try {
                Message message = (Message) this.serverInputStream.readObject();
                while (message.type() != MessageType.DISCONNECT_ACK) {
                    logger.info("Received message: " + message.type());
                    this.handleMessage(message);
                    message = (Message) this.serverInputStream.readObject();
                }
            } catch (IOException | ClassNotFoundException e) {
                logger.warning("Error receiving message: " + e.getMessage());
                logger.info("Reconnecting to server...");
                this.fetchPrimaryServerFromMonitor(1000);
                boolean b1 = this.connectToServer(this.primaryServer);
                if (!b1) {
                    logger.warning("Failed to reconnect to server");
                    break;
                }
            }
        }
//        logger.info("Received DISCONNECT_ACK, stopping client thread");
        this.close();
    }


    private void handleMessage(Message message) {
        switch (message.type()) {
            case MESSAGE -> this.chatController.receiveMessage(message);
            case DIRECTORY -> this.chatController.updateDirectory(message);
            case ERROR -> this.logger.warning("Error receiving message: " + message.type());
            case MESSAGE_ACK, REGISTER_ACK -> {
                // Handle acknowledgment messages
            }
            default -> this.logger.severe("Unkwnon message type: " + message.type());
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
            this.logger.info("Message sent: " + message);
        } catch (IOException e) {
            this.logger.warning("Error sending message: " + e.getMessage());
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
            if (this.monitorInputStream != null) {
                this.monitorInputStream.close();
                logger.info("Input stream closed");
            } else {
                logger.info("Input stream is null");
            }
            if (this.monitorOutputStream != null) {
                this.monitorOutputStream.close();
                logger.info("Output stream closed");
            } else {
                logger.info("Output stream is null");
            }
            if (this.monitorSocket != null && !this.monitorSocket.isClosed()) {
                logger.info("Closing socket: " + this.monitorSocket);
                this.monitorSocket.setReuseAddress(Boolean.TRUE);
                this.monitorSocket.close();
            }
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
        } catch (IOException e) {
            logger.warning("Error closing connection: " + e.getMessage());
        }
    }
}