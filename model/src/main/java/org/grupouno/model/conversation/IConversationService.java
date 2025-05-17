package org.grupouno.model.conversation;

public interface IConversationService {
    boolean existsConversationWith(String contactNickname);

    IConversation getConversationByContactNickname(String receiverNickname);

    void startNewConversation(String contactNickname);

    void addMessage(Message message, String contactNickname);
}
