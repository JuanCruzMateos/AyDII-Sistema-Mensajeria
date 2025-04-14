package org.grupouno.model.agenda;

/**
 * Class that represents a user in the system.
 * <p>
 * It contains the user's nickname, IP address, and port.
 */
public record User(String nickname, String ip, int port) {

}
