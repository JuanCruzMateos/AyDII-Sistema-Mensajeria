package org.grupouno.model.conversation;

import java.util.ArrayList;
import java.util.HashMap;

public class ConversationService implements IConversationService {
    private final HashMap<String, Conversation> conversations;

    public ConversationService(HashMap<String, Conversation> conversations) {
        this.conversations = conversations;
    }

    @Override
    public boolean existsConversationWith(String contactNickname) {
        return this.conversations.containsKey(contactNickname);
    }

    @Override
    public synchronized Conversation getConversationByContactNickname(String receiverNickname) {
        return this.conversations.get(receiverNickname);
    }

    @Override
    public synchronized void startNewConversation(String contactNickname) {
        this.conversations.put(contactNickname, new Conversation(new ArrayList<>()));
    }

    @Override
    public synchronized void addMessage(Message message, String contactNickname) {
        this.conversations.get(contactNickname).addMessage(message);
    }
}
