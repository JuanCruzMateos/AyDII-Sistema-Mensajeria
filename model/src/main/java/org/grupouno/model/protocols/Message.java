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

    public String getFormattedMessageHTML(String user, String lastSender) {
        String ans, body;
        if (user.equals(senderNickname)) {
            ans = "<p style=\"text-align: right; margin:0\">";
            body = "<span style=\"color:gray\"><font size=\"2\">" + timestamp.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) + "</font></span> " + content;
        } else {
            ans = "<p style=\"text-align: left; margin:0\">";
            body = content + " <span style=\"color:gray\"><font size=\"2\">" + timestamp.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) + "</font></span>";
        }
        if (!senderNickname.equals(lastSender))
            ans += "<font size=\"5\"><b>~" + senderNickname + "~</b></font><br/>";
        ans += body;
        ans += "</p>";
        return ans;
    }
}