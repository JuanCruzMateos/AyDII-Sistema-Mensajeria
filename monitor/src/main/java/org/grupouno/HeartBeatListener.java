package org.grupouno;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
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
        try (DatagramSocket socket = new DatagramSocket(port)) {    //Works with UDP socket
            byte[] buffer = new byte[256];
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet); // Receive heartbeat message from a node

                String nodeId = new String(packet.getData(), 0, packet.getLength());
                InetAddress senderAddress = packet.getAddress(); // Obtener IP del nodo emisor
                String ip = senderAddress.getHostAddress();
                logger.info("Received heartbeat from: " + nodeId);

                // Register the heartbeat with the Monitor
                monitor.registerHeartbeat(nodeId, ip);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
