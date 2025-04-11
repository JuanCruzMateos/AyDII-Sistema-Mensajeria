package org.grupouno.network;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Utility class that validates the connection parameters such as IP address and port number.
 */
public class ConnectionValidator {

    public static boolean isValidIp(String ip) {
        String ipRegex = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
        return ip != null && !ip.isEmpty() && ip.matches(ipRegex);
    }

    public static boolean isValidPort(int port) {
        // Implement the logic to validate the port number
        // This is a placeholder implementation
        return port > 0 && port <= 65535;
    }

    public static boolean isPortAvaliable(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            return serverSocket.getLocalPort() == port;
        } catch (IOException e) {
            return false;
        }
    }
}
