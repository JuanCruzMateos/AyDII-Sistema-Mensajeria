package org.grupouno.exceptions;

public class ConversationNotFoundException extends Exception {
    public ConversationNotFoundException(String message) {
        super(message);
    }

    public ConversationNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
