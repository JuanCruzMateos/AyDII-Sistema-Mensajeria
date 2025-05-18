package org.grupouno.model.protocols;

import org.grupouno.model.conversation.Message;

import java.io.Serializable;

public record SyncProtocolMessage(
        Topic topic,
        Message message
) implements Serializable {
}
