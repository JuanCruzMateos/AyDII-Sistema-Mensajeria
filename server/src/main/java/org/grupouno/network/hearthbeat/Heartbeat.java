package org.grupouno.network.hearthbeat;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.logging.Logger;

public class Heartbeat implements Runnable {
    private final Logger logger = Logger.getLogger(Heartbeat.class.getName());
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
            socket.setReuseAddress(true); // set reuse before bind and connecting
            socket.bind(new InetSocketAddress(InetAddress.getByName(serverAddress), serverPort)); // Explicitly bind
            socket.connect(new InetSocketAddress(InetAddress.getByName(monitorAddress), monitorPort)); // Connect to monitor

            while (true) {
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
