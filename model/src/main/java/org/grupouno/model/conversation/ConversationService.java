package org.grupouno.model.conversation;

import java.util.HashMap;
import java.util.Optional;

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
    public synchronized Optional<IConversation> getConversationByContactNickname(String receiverNickname) {
        return Optional.ofNullable(this.conversations.get(receiverNickname));
    }

    @Override
    public synchronized void startNewConversation(String contactNickname) {
        this.conversations.put(contactNickname, new Conversation());
    }

    @Override
    public synchronized void addMessage(Message message, String contactNickname) {
        this.conversations.get(contactNickname).addMessage(message);
    }

    @Override
    public synchronized Iterable<IConversation> getAllConversations() {
        return this.conversations.values();
    }

}
