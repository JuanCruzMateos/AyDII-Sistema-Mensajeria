package org.grupouno.model.connection;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class SocketConnection {
    private final Socket socket;
    private final ObjectOutputStream objectOutputStream;
    private final ObjectInputStream objectInputStream;
    private volatile boolean closed;

    public SocketConnection(Socket socket, ObjectOutputStream objectOutputStream, ObjectInputStream objectInputStream) {
        this.socket = socket;
        this.objectOutputStream = objectOutputStream;
        this.objectInputStream = objectInputStream;
        this.closed = false;
    }

    public Socket getSocket() {
        return socket;
    }

    public ObjectOutputStream getObjectOutputStream() {
        return objectOutputStream;
    }

    public ObjectInputStream getObjectInputStream() {
        return objectInputStream;
    }

    public boolean isConnected() {
        return !closed && socket != null && socket.isConnected() && !socket.isClosed();
    }

    public boolean isClosed() {
        return closed || socket == null || socket.isClosed();
    }

    public synchronized void close() throws IOException {
        if (!this.closed) {
            if (objectInputStream != null) {
                objectInputStream.close();
            }
            if (objectOutputStream != null) {
                objectOutputStream.close();
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            closed = true;
        }
    }

    @Override
    public String toString() {
        return "SocketConnection{getSocket=" + socket + ", closed=" + closed + "}";
    }
}