package org.grupouno;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HeartBeatSender extends Thread {
    private static final Logger logger = Logger.getLogger(HeartBeatSender.class.getName());
    private final String nodeId;
    private final String address;
    private final int port;

    public HeartBeatSender(String nodeId, String address, int port) {
        this.nodeId = nodeId;
        this.address = address;
        this.port = port;
    }

    @Override
    public void run() {
        try (DatagramSocket socket = new DatagramSocket()) {
            InetAddress inetAddress = InetAddress.getByName(address);
            byte[] buffer = nodeId.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, inetAddress, port); // Create UDP package

            while (true) {
                socket.send(packet); // Heartbeat sent
                logger.info("Heartbeat sent to " + address + ":" + port);
                Thread.sleep(2000); // 2 sec
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error in HeartBeatSender: ", e);
        }
    }
}
