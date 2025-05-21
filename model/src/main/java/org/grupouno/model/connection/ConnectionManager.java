package org.grupouno.model.connection;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Logger;

public record ConnectionManager(
        Socket socket,
        ObjectOutputStream objectOutputStream,
        ObjectInputStream objectInputStream
) {

    public void close() {
        if (socket != null && !socket.isClosed()) {
            try {
                socket.close();
            } catch (Exception e) {
                Logger.getLogger(ConnectionManager.class.getName()).info("Error closing socket: " + e.getMessage());
            }
        }
        if (objectOutputStream != null) {
            try {
                objectOutputStream.close();
            } catch (Exception e) {
                Logger.getLogger(ConnectionManager.class.getName()).info("Error closing socket: " + e.getMessage());
            }
        }
        if (objectInputStream != null) {
            try {
                objectInputStream.close();
            } catch (Exception e) {
                Logger.getLogger(ConnectionManager.class.getName()).info("Error closing socket: " + e.getMessage());
            }
        }
    }
}
