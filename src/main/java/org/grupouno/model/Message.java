package org.grupouno.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Class that represents a message exchanged between two users.
 * It contains the sender and receiver information, the content of the message,
 * the timestamp of when it was sent, and whether it has been seen or not.
 */
public record Message(String senderNickname, String senderIP, int senderPort, String receiverNickname,
                      String receiverIP, int receiverPort, String content, LocalDateTime timestamp
) implements Serializable {

    public String getFormattedSendedMessage() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        return String.format("%s - [%s]:\n%s\n", senderNickname, timestamp.format(fmt), content);
    }
}
