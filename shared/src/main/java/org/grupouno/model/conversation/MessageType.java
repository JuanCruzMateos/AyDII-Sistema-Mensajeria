package org.grupouno.model.conversation;

public enum MessageType {
    REGISTER("/register"),
    REGISTER_ACK("/register_ack"),
    MESSAGE("/message"),
    MESSAGE_ACK("/message_ack"),
    DISCONNECT("/disconnect"),
    DISCONNECT_ACK("/disconnect_ack"),
    GET_DIRECTORY("/get_directory"),
    DIRECTORY("/directory"),
    ERROR("/error");

    private final String type;

    MessageType(String type) {
        this.type = type;
    }
}
