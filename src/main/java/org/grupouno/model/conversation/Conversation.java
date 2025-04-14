package org.grupouno.model.conversation;

import java.util.List;

/**
 * Class that represents a conversation between two users.
 * It contains the list of messages exchanged in the conversation.
 */
public record Conversation(List<Message> messages) {

    public void addMessage(Message message) {
        this.messages.add(message);
    }

    @Override
    public String toString() {
        return "Conversation{" +
                "messages=" + messages +
                '}';
    }
}
