package org.grupouno.model.conversation;

import java.util.Iterator;
import java.util.List;

public interface IConversation {
    void addMessage(Message message);

    List<Message> getMessages();

    boolean isEmpty();

    Iterator<Message> iterator();

    @Override
    String toString();
}
