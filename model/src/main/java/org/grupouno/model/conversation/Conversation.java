package org.grupouno.model.conversation;

import org.grupouno.model.protocols.Message;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Class that represents a conversation between two users.
 * It contains the list of messages exchanged in the conversation.
 */
public class Conversation implements IConversation {
    private final List<Message> messages;

    public Conversation() {
        this.messages = new ArrayList<>();
    }

    @Override
    public void addMessage(Message message) {
        this.messages.add(message);
    }

    @Override
    public void removeMessage(Message message) {
        this.messages.remove(message);
    }

    @Override
    public List<Message> getMessages() {
        return messages;
    }

    @Override
    public boolean isEmpty() {
        return this.messages.isEmpty();
    }

    @Override
    public Iterator<Message> iterator() {
        return this.messages.iterator();
    }

    @Override
    public boolean existsMessage(Message message) {
        return this.messages.contains(message);
    }

    @Override
    public String toString() {
        return "Conversation{" +
                "messages=" + messages +
                '}';
    }
}
