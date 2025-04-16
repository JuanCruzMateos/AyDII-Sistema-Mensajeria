package org.grupouno.exceptions;

public class ClientNotConnectedException extends Exception {
    public ClientNotConnectedException(String message) {
        super(message);
    }

    public ClientNotConnectedException(String message, Throwable cause) {
        super(message, cause);
    }
}
