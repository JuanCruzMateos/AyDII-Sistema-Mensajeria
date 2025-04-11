package org.grupouno.model;

import org.grupouno.exceptions.ConversationNotFoundException;
import org.grupouno.exceptions.UserNotFoundException;

public interface IChatSession {

    void addNewContact(User user);

    boolean isContactInAgenda(String contactNickname);

    User getContactByNickname(String contactNickname) throws UserNotFoundException;

    Conversation getConversationByContactNickname(String receiverNickname) throws ConversationNotFoundException;

    boolean existsConversationWith(String contactNickname);

    void startNewConversation(String contactNickname);

    void sendMessage(Message message);

    void receiveMessage(Message message);
}
