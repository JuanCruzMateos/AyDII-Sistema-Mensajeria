package org.grupouno.model.conversation;

import java.util.ArrayList;
import java.util.List;

/**
 * Class that represents a conversation between two users.
 * It contains the list of messages exchanged in the conversation.
 */
public class Conversation {
    private final List<Message> messages;

    public Conversation() {
        this.messages = new ArrayList<>();
    }

    public void addMessage(Message message) {
        this.messages.add(message);
    }

    public List<Message> getMessages() {
        return messages;
    }

    public boolean isEmpty() {
        return this.messages.isEmpty();
    }

    public void clear() {
        this.messages.clear();
    }

    @Override
    public String toString() {
        return "Conversation{" +
                "messages=" + messages +
                '}';
    }
}
