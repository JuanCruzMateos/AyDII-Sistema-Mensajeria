package org.grupouno.model.protocols;

import java.io.Serializable;

public record SyncProtocolMessage(
        Topic topic,
        Message message
) implements Serializable {
}
