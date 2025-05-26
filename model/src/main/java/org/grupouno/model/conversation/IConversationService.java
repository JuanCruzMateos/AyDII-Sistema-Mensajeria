package org.grupouno.model.conversation;

import org.grupouno.model.protocols.Message;

import java.util.Optional;

public interface IConversationService {
    boolean existsConversationWith(String contactNickname);

    Optional<IConversation> getConversationByContactNickname(String receiverNickname);

    void startNewConversation(String contactNickname);

    void addMessage(Message message, String contactNickname);

    Iterable<IConversation> getAllConversations();
}
