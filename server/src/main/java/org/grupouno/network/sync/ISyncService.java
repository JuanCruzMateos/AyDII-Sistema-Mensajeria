package org.grupouno.network.sync;

import org.grupouno.model.conversation.Message;
import org.grupouno.model.protocols.Topic;

public interface ISyncService {
    void publishEvent(Message message, Topic topic);

    void syncAll();

    void syncUser(Message message);

    void syncMessage(Message message);

    void userDisconnect(Message message);

    void userConnect(Message message);

    void removeMessage(Message message);

    void addNewMessage(Message message);
}
