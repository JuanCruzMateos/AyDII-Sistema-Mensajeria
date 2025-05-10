package org.grupouno;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.logging.Logger;

public class PrimaryQueryResponder extends Thread {
    private final Monitor monitor;
    private final int listenPort = 9998;
    private final Logger logger = Logger.getLogger(PrimaryQueryResponder.class.getName());

    public PrimaryQueryResponder(Monitor monitor) {
        this.monitor = monitor;
    }

    @Override
    public void run() {
        try (DatagramSocket socket = new DatagramSocket(listenPort)) {
            byte[] buffer = new byte[1024];
            logger.info("Monitor listening queries from primary in port " + listenPort);
            while (true) {
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                socket.receive(request);
                String message = new String(request.getData(), 0, request.getLength());
                logger.info("Quey received: " + message);
                if (message.equals("GET_PRIMARY")) {
                    String primaryIp = monitor.getPrimaryIp();
                    byte[] responseData = primaryIp.getBytes();
                    DatagramPacket response = new DatagramPacket(
                            responseData,
                            responseData.length,
                            request.getAddress(),
                            request.getPort()
                    );
                    socket.send(response);
                }
            }
        } catch (Exception e) {
            logger.warning("Error in PrimaryQueryResponder: " + e.getMessage());
        }
    }
}