package org.grupouno.network.hearthbeat;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.logging.Logger;

public class Heartbeat implements Runnable {
    private static final Logger logger = Logger.getLogger(Heartbeat.class.getName());
    private final String monitorAddress;
    private final int monitorPort;
    private final String serverAddress;
    private final int serverPort;
    private final long heartbeatInterval;

    public Heartbeat(String monitorAddress, int monitorPort, String serverAddress, int serverPort, long heartbeatInterval) {
        this.monitorAddress = monitorAddress;
        this.monitorPort = monitorPort;
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.heartbeatInterval = heartbeatInterval;
    }

    @Override
    public void run() {
        try (Socket socket = new Socket()) {
            logger.info("Heartbeat starting on " + serverAddress + ":" + serverPort + " to " + monitorAddress + ":" + monitorPort);
            socket.setReuseAddress(true);
            socket.bind(new InetSocketAddress(InetAddress.getByName(serverAddress), serverPort));
            socket.connect(new InetSocketAddress(InetAddress.getByName(monitorAddress), monitorPort));

            for (; ; ) {
                socket.getOutputStream().write("HEARTBEAT".getBytes());
                socket.getOutputStream().flush();
                Thread.sleep(heartbeatInterval);
            }
        } catch (Exception e) {
            logger.warning("Error in heartbeat: " + e.getMessage());
            // TODO: Fix Address already in use
        }
    }
}
