package org.grupouno.model;

import java.util.ArrayList;
import java.util.HashMap;

public class ConversationService {
    private final HashMap<String, Conversation> conversations;

    public ConversationService(HashMap<String, Conversation> conversations) {
        this.conversations = conversations;
    }

    public boolean existsConversationWith(String contactNickname) {
        return this.conversations.containsKey(contactNickname);
    }

    public synchronized Conversation getConversationByContactNickname(String receiverNickname) {
        return this.conversations.get(receiverNickname);
    }

    public synchronized void startNewConversation(String contactNickname) {
        this.conversations.put(contactNickname, new Conversation(new ArrayList<>()));
    }

    public synchronized void addMessage(Message message, String contactNickname) {
        Conversation conversation = this.conversations.get(contactNickname);
        conversation.addMessage(message);
    }
}
