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

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(monitorPort, 50, InetAddress.getByName(monitorAddress))) {
            logger.info("HeartbeatServer started on " + monitorAddress + ":" + monitorPort);
            while (true) {
                Socket socket = serverSocket.accept();
                Long currentTime = System.currentTimeMillis();
                SocketAddress address = socket.getRemoteSocketAddress();
                logger.info("Accepted connection from client: " + address);
                if (!this.heartbeats.containsKey(address)) {
                    logger.info("New client detected: " + address);
                } else {
                    logger.info("Heartbeat received from client: " + address);
                }
                this.heartbeats.put(address, currentTime);
            }
        } catch (Exception e) {
            logger.warning("Error in monitor: " + e.getMessage());
        }
    }
}
