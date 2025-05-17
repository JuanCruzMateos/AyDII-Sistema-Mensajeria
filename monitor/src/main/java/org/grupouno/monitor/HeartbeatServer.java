package org.grupouno.monitor;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * HeartbeatServer is responsible for accepting incoming connections from clients
 * and updating the heartbeat timestamps in the heartbeats map.
 */
public class HeartbeatServer implements Runnable {
    private final Logger logger = Logger.getLogger(HeartbeatServer.class.getName());
    private final int monitorPort;
    private final String monitorAddress;
    private final ConcurrentHashMap<SocketAddress, Long> heartbeats;

    public HeartbeatServer(String monitorAddress, int monitorPort, ConcurrentHashMap<SocketAddress, Long> heartbeats) {
        this.monitorAddress = monitorAddress;
        this.monitorPort = monitorPort;
        this.heartbeats = heartbeats;
    }

    private void handleConnection(Socket socket) {
        SocketAddress address = socket.getRemoteSocketAddress();
        logger.info("Accepted connection from: " + address);
        byte[] buffer = new byte[256];

        try {
            int bytesRead;
            while ((bytesRead = socket.getInputStream().read(buffer)) != -1) {
                String message = new String(buffer, 0, bytesRead);
                long timestamp = System.currentTimeMillis();
                logger.info("Received message: " + message + " from " + address);

                String logMessage = heartbeats.containsKey(address) ? "Heartbeat received from server: " : "New server detected: ";
                logger.info(logMessage + address);
                heartbeats.put(address, timestamp);
            }
        } catch (Exception e) {
            logger.warning("Error reading from socket: " + e.getMessage());
        } finally {
            logger.info((socket.isClosed() ? "Socket closed: " : "Socket not closed properly: ") + address);
        }
    }


    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(monitorPort, 50, InetAddress.getByName(monitorAddress))) {
            serverSocket.setReuseAddress(Boolean.TRUE);
            logger.info("HeartbeatServer started on " + monitorAddress + ":" + monitorPort);
            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(() -> this.handleConnection(socket)).start();
            }
        } catch (Exception e) {
            logger.warning("Error in monitor: " + e.getMessage());
        }
    }
}
