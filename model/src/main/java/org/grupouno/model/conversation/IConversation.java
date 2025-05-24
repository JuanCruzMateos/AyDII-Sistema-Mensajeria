package org.grupouno.model.conversation;

import org.grupouno.model.protocols.Message;

import java.util.Iterator;
import java.util.List;

public interface IConversation {
    void addMessage(Message message);

    void removeMessage(Message message);

    List<Message> getMessages();

    boolean isEmpty();

    Iterator<Message> iterator();

    boolean existsMessage(Message message);
}
