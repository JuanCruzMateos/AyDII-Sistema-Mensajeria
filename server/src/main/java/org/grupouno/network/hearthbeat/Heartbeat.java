package org.grupouno.network.hearthbeat;

import java.net.Socket;
import java.util.logging.Logger;

public class Heartbeat implements Runnable {
    private final Logger logger = Logger.getLogger(Heartbeat.class.getName());
    private final String monitorAddress;
    private final int monitorPort;
    private final long heartbeatInterval;

    public Heartbeat(String monitorAddress, int monitorPort, long heartbeatInterval) {
        this.monitorAddress = monitorAddress;
        this.monitorPort = monitorPort;
        this.heartbeatInterval = heartbeatInterval;
    }

    @Override
    public void run() {
        try (Socket socket = new Socket(monitorAddress, monitorPort)) {
            while (true) {
                socket.getOutputStream().write("HEARTBEAT".getBytes());
                Thread.sleep(heartbeatInterval);
            }
        } catch (Exception e) {
            logger.warning("Error in heartbeat: " + e.getMessage());
        }
    }
}
