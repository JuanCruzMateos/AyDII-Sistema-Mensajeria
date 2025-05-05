package org.grupouno;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class HeartBeatListener extends Thread {
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
                System.out.println("Received heartbeat from: " + nodeId); //TODO replace with Logger

                // Register the heartbeat with the Monitor
                monitor.registerHeartbeat(nodeId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
