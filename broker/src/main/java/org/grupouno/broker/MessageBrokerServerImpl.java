package org.grupouno.broker;

import org.grupouno.model.connection.ConnectionManager;

import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.logging.Logger;

public class MessageBrokerServerImpl implements Runnable {
    private final Logger logger = Logger.getLogger(MessageBrokerServerImpl.class.getName());
    private final String brokerAddress;
    private final int brokerPort;
    private final HashMap<SocketAddress, ConnectionManager> connectedServers;

    public MessageBrokerServerImpl(String brokerAddress, int brokerPort) {
        this.brokerAddress = brokerAddress;
        this.brokerPort = brokerPort;
        this.connectedServers = new HashMap<>();
    }

    @Override
    public void run() {
        logger.info("Starting Message Broker Server on " + this.brokerAddress + ":" + this.brokerPort);
        try (ServerSocket serverSocket = new ServerSocket()) {
            serverSocket.setReuseAddress(true);
            serverSocket.bind(new InetSocketAddress(InetAddress.getByName(this.brokerAddress), this.brokerPort));
            logger.info("Waiting for connections... ");
            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    logger.info("Accepted connection from server " + clientSocket.getInetAddress() + ":" + clientSocket.getPort());
                    new Thread(new ConnectionHandler(clientSocket, this.connectedServers)).start();
                } catch (IOException e) {
                    logger.warning("Error accepting connection: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            logger.warning("Error starting Broker Server: " + e.getMessage());
        }
    }
}
