package org.grupouno;

import org.grupouno.network.server.ChatServerImpl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class Monitor {
    private static final int TIMEOUT = 5000;    //5 seconds
    private final Logger logger = Logger.getLogger(ChatServerImpl.class.getName());
    private final ConcurrentHashMap<String, Long> lastHeartbeat = new ConcurrentHashMap<>(); //Here uses ConcurrentHashMap to avoid problems when the structure is being used by two or more different threads

    public static void main(String[] args) throws Exception {
        Monitor monitor = new Monitor();
        HeartBeatListener listener = new HeartBeatListener(monitor, 9999); // Pass the monitor and port number
        listener.start();

        while (true) {
            monitor.checkServers();
            Thread.sleep(3000);
        }
    }

    public void registerHeartbeat(String id) {
        lastHeartbeat.put(id, System.currentTimeMillis());
    }

    public void checkServers() {
        long now = System.currentTimeMillis();
        for (Map.Entry<String, Long> entry : lastHeartbeat.entrySet()) {
            long diff = now - entry.getValue();
            if (diff > TIMEOUT) {
                logger.info("Inactive node: " + entry.getKey());
            } else {
                logger.info("Active node: " + entry.getKey());
            }
        }
    }
}
