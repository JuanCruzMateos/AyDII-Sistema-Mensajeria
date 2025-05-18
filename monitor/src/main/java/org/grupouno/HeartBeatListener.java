package org.grupouno;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.logging.Logger;

public class HeartBeatListener extends Thread {
    private static final Logger logger = Logger.getLogger(HeartBeatListener.class.getName());
    private final Monitor monitor;
    private final int port;

    public HeartBeatListener(Monitor monitor, int port) {
        this.monitor = monitor;
        this.port = port;
    }

    @Override
    public void run() {
        try (DatagramSocket socket = new DatagramSocket(port)) {
            byte[] buffer = new byte[256];

            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String received = new String(packet.getData(), 0, packet.getLength()).trim();
                String[] parts = received.split(":");

                if (parts.length != 2) {
                    logger.warning("Formato inválido de heartbeat: " + received);
                    continue;
                }

                String nodeId = parts[0];
                String serverPort = parts[1];

                String ip = packet.getAddress().getHostAddress();
                logger.info("Received heartbeat from: " + nodeId + " at " + ip + ":" + serverPort);

                monitor.registerHeartbeat(nodeId, ip + ":" + serverPort);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
