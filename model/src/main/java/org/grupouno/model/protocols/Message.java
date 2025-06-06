package org.grupouno.model.protocols;

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

    public String getFormattedMessageHTML(String user) {
        String ans;
        if (user.equals(senderNickname))
            ans = "<p style=\"text-align: right;\">";
        else
            ans = "<p style=\"text-align: left;\">";
        ans += String.format("<b>%s</b> - [%s]<br/>%s<br/>",
                senderNickname,
                timestamp.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")),
                content);
        ans += "</p>";
        return ans;
    }
}