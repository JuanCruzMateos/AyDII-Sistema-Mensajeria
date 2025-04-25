package org.grupouno.model.directory;

import java.io.Serializable;

/**
 * Represents a user with a nickname, IP address, and port.
 */
public record User(String nickname, String ip, int port) implements Serializable {
}