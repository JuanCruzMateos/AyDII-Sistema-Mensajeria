package org.grupouno.model;

/**
 * Class that represents a user in the system.
 * It contains the user's nickname, IP address, and port.
 */
public record User(String nickname, String ip, int port) {

}
