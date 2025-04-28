package org.grupouno.model.conversation;

import java.util.HashMap;

/**
 * Manages conversations and their associated messages.
 */
public class ConversationService implements IConversationService {
    private final HashMap<String, IConversation> conversations;

    public ConversationService() {
        this.conversations = new HashMap<>();
    }

    @Override
    public boolean existsConversationWith(String contactNickname) {
        return this.conversations.containsKey(contactNickname);
    }

    @Override
    public synchronized IConversation getConversationByContactNickname(String receiverNickname) {
        return this.conversations.get(receiverNickname);
    }

    @Override
    public synchronized void startNewConversation(String contactNickname) {
        this.conversations.put(contactNickname, new Conversation());
    }

    @Override
    public synchronized void addMessage(Message message, String contactNickname) {
        this.conversations.get(contactNickname).addMessage(message);
    }

}
