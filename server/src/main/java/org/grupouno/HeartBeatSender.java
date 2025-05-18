package org.grupouno;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.logging.Logger;

public class HeartBeatSender extends Thread {
    private static final Logger logger = Logger.getLogger(HeartBeatSender.class.getName());
    private final String nodeId;
    private final String monitorIp;
    private final int monitorPort;
    private final int serverTcpPort; // <-- el puerto real del servidor

    public HeartBeatSender(String nodeId, String monitorIp, int monitorPort, int serverTcpPort) {
        this.nodeId = nodeId;
        this.monitorIp = monitorIp;
        this.monitorPort = monitorPort;
        this.serverTcpPort = serverTcpPort;
    }

    @Override
    public void run() {
        try (DatagramSocket socket = new DatagramSocket()) {
            InetAddress inetAddress = InetAddress.getByName(monitorIp);

            while (true) {
                String message = nodeId + ":" + serverTcpPort;
                byte[] buffer = message.getBytes();

                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, inetAddress, monitorPort);
                socket.send(packet);
                Thread.sleep(2000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
