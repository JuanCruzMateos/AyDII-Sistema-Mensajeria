package org.grupouno.network;

import org.grupouno.controller.ChatController;
import org.grupouno.model.protocols.Message;
import org.grupouno.model.protocols.MessageType;

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
    private final String monitorAddress;
    private final int monitorPort;
    private final ChatController chatController;
    private final int localPort;
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

    public boolean fetchPrimaryServerFromMonitor(int delay) {
        int maxRetries = 5;
        int attempts = 0;
        boolean connected = false;

        this.primaryServer = null;
        while (this.primaryServer == null && attempts < maxRetries) {
            try {
                this.monitorOutputStream.writeObject("primary.server");
                this.monitorOutputStream.flush();
                this.primaryServer = (SocketAddress) this.monitorInputStream.readObject();
                logger.info("Primary server address from monitor: " + primaryServer);
                if (this.primaryServer == null) {
                    logger.info("No servers found, retrying in " + delay + "ms");
                    attempts++;
                    System.out.println(attempts);
                    Thread.sleep(delay);
                } else {
                    connected = true;
                    logger.info("Primary server address fetched successfully: " + primaryServer);
                }
            } catch (IOException e) {
                logger.warning("Error getting primary server address: " + e.getMessage() + ". Retrying...");
                attempts++;
            } catch (ClassNotFoundException e) {
                logger.warning("Error reading message from monitor: " + e.getMessage() + ". Retrying...");
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                logger.warning("Thread interrupted while waiting for primary server: " + e.getMessage());
                throw new RuntimeException(e);
            }
        }
        return connected;
    }

    // TODO :: Refactor, mucho codigo duplicado entre connectToMonitor y connectToServer
    public boolean connectToMonitor() {
        int maxRetries = 5;
        int attempts = 0;
        boolean connected = false;

        while (!connected && attempts < maxRetries) {
            try {
                this.monitorSocket = new Socket();
                this.monitorSocket.setReuseAddress(true);
                //this.monitorSocket.bind(new InetSocketAddress(localAddress, localPort));
                //Si el error es porque está dos veces con el mismo puerto, el que menos importa es el del monitor que sólo actúa acá
                //Así que, que agarre algún puerto al azar que esté libre...
                this.monitorSocket.bind(null);
                this.monitorSocket.connect(new InetSocketAddress(monitorAddress, monitorPort));
                this.monitorInputStream = new ObjectInputStream(this.monitorSocket.getInputStream());
                this.monitorOutputStream = new ObjectOutputStream(this.monitorSocket.getOutputStream());
                logger.info("Connected to monitor at " + monitorAddress + ":" + monitorPort);
                connected = true;
                logger.info("Connected to monitor at " + monitorAddress + ":" + monitorPort);
            } catch (IOException e) {
                logger.warning("Error connecting to monitor: " + e.getMessage());
                attempts++;
                if (attempts < maxRetries) {
                    logger.info("Retrying connection to monitor...");
                    try {
                        Thread.sleep(2000); // Wait for 2 seconds before retrying
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        }
        if (!connected) {
            logger.warning("Failed to connect to monitor after " + maxRetries + " attempts");
        }
        return connected;
    }

    public boolean connectToServer() {
        int maxRetries = 5;
        int attempts = 0;
        boolean connected = false;

        while (!connected && attempts < maxRetries) {
            try {
                boolean c = this.fetchPrimaryServerFromMonitor(1000);
                if (this.primaryServer != null) {
                    this.serverSocket = new Socket();
                    this.serverSocket.setReuseAddress(true);
                    this.serverSocket.bind(new InetSocketAddress(localAddress, localPort));
                    //No se por qué, pero me tira error acá con puerto ya en uso. Creo que no le gusta esto de usar el mismo puerto 2 veces.
                    //Lo de acá es una solución con esto de bind(null) que agarra un puerto cualquiera que esté disponible.
                    //logger.info("Previo a bind: " + localAddress + ":" + localPort);
                    //this.serverSocket.bind(null);
                    //this.localPort = this.serverSocket.getLocalPort();
                    //logger.info("Luego a bind(null): " + localAddress + ":" + localPort);
                    this.serverSocket.connect(this.primaryServer);
                    this.serverInputStream = new ObjectInputStream(this.serverSocket.getInputStream());
                    this.serverOutputStream = new ObjectOutputStream(this.serverSocket.getOutputStream());
                    connected = true;
                    logger.info("Connected to primary server: " + this.primaryServer);
                }
            } catch (IOException e) {
                logger.warning("Error connecting to primary server: " + e.getMessage());
                attempts++;
                if (attempts < maxRetries) {
                    logger.info("Retrying connection to primary server...");
                    try {
                        Thread.sleep(2000); // Wait for 2 seconds before retrying
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        }
        if (!connected) {
            logger.warning("Failed to connect to primary server after " + maxRetries + " attempts");
        }
        return connected;
    }


    @Override
    public void run() {
        logger.info("Local address: " + this.localAddress + ":" + this.localPort);
        logger.info("Connecting to monitor at " + this.monitorAddress + ":" + this.monitorPort);

        if (!this.connectToMonitor()) {
            logger.severe("Failed to connect to monitor. Exiting...");
            this.chatController.networkError("Failed to connect to monitor at " + this.monitorAddress + ":" + this.monitorPort);
            return;
        }
        if (this.connectToServer()) {
            this.registerWithServer(this.clientName, this.localAddress, this.localPort);
        } else {
            logger.severe("Failed to connect to primary server. Exiting...");
            this.chatController.networkError("Failed to connect to primary server at " + this.primaryServer);
            return;
        }

        boolean running = true;
        while (running) {
            try {
                Message message = (Message) this.serverInputStream.readObject();
                while (message.type() != MessageType.DISCONNECT_ACK) {
                    logger.info("Received message: " + message.type());
                    this.handleMessage(message);
                    message = (Message) this.serverInputStream.readObject();
                }
                logger.info("Received DISCONNECT_ACK, stopping client thread");
                running = false;
                this.close();
            } catch (IOException | ClassNotFoundException e) {
                logger.info("Connection lost. Reconnecting to server...");
                try {
                    Thread.sleep(2000); // Wait for 2 second before reconnecting
                    this.close();
                    if (this.connectToServer()) {
                        this.registerWithServer(this.clientName, this.localAddress, this.localPort);
                    } else {
                        logger.severe("Failed to reconnect to primary server. Exiting...");
                        this.chatController.networkError("Failed to reconnect to primary server at " + this.primaryServer);
                        running = false;
                    }
                } catch (InterruptedException ie) {
                    logger.warning("Thread interrupted while waiting to reconnect: " + ie.getMessage());
                }
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

    public synchronized void close() {
        try {
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
                logger.info("Closing getSocket: " + this.serverSocket);
                this.serverSocket.close();
                this.serverSocket = null;
            }
        } catch (IOException e) {
            logger.warning("Error closing connection: " + e.getMessage());
        }
    }
}