package org.grupouno.model.protocols;

public enum MessageType {
    REGISTER,
    REGISTER_ACK,
    MESSAGE,
    MESSAGE_ACK,
    DISCONNECT,
    DISCONNECT_ACK,
    GET_DIRECTORY,
    DIRECTORY,
    ERROR
}
