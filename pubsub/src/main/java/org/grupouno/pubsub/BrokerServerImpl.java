package org.grupouno.pubsub;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.logging.Logger;

public class BrokerServerImpl implements IBroker, Runnable {
    private final Logger logger = Logger.getLogger(BrokerServerImpl.class.getName());
    private final InetAddress brokerAddress;
    private final int serverPort;
    private final HashMap<String, Topic> topics;

    public BrokerServerImpl(InetAddress brokerAddress, int serverPort) {
        this.brokerAddress = brokerAddress;
        this.serverPort = serverPort;
        this.topics = new HashMap<>();
    }

    @Override
    public void run() {
        logger.info("Starting server on port " + this.serverPort);
        try (ServerSocket serverSocket = new ServerSocket(this.serverPort, 50, this.brokerAddress)) {
//            logger.info("Server started on port " + serverSocket.getLocalPort());
            logger.info("Waiting for connections... ");
            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    logger.info("Accepted connection from " + clientSocket.getInetAddress() + ":" + clientSocket.getPort());
//                    logger.info("Local address: " + clientSocket.getLocalAddress() + ":" + clientSocket.getLocalPort());
                    new Thread(() -> System.out.println("Handling client connection...")).start();
                } catch (IOException e) {
                    logger.warning("Error accepting connection: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            logger.warning("Error starting Broker Server: " + e.getMessage());
        }
    }
}
