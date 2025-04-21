package org.grupouno.model.conversation;

public interface IConversationService {
    boolean existsConversationWith(String contactNickname);

    Conversation getConversationByContactNickname(String receiverNickname);

    void startNewConversation(String contactNickname);

    void addMessage(Message message, String contactNickname);

    void setMessages(String contactNickname, Conversation conversation);
}
