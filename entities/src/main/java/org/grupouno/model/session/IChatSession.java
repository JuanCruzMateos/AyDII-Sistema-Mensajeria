package org.grupouno.model.session;

import org.grupouno.exceptions.UserNotFoundException;
import org.grupouno.model.agenda.User;
import org.grupouno.model.conversation.Message;

public interface IChatSession {

    void addNewContact(User user);

    User getContactByNickname(String contactNickname) throws UserNotFoundException;

    boolean existsConversationWith(String contactNickname);

    void startNewConversation(String contactNickname);

    void sendMessage(Message message);

    void receiveMessage(Message message);
}
