package org.grupouno.model.conversation;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record Message(
        String senderNickname,
        String senderIP,
        int senderPort,
        String receiverNickname,
        String receiverIP,
        int receiverPort,
        Object content,
        LocalDateTime timestamp,
        MessageType type
) implements Serializable {

    public String getFormattedMessage() {
        return String.format("%s - [%s]:\n%s\n",
                senderNickname,
                timestamp.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")),
                content);
    }
}