package org.grupouno.model.session;

import org.grupouno.model.conversation.IConversationService;
import org.grupouno.model.directory.IDirectory;
import org.grupouno.model.directory.User;
import org.grupouno.model.protocols.Message;

import java.util.List;
import java.util.Optional;

public interface IChatSession {
    String getIp();

    void setIp(String ip);

    String getNickname();

    void setNickname(String nickname);

    int getPort();

    void setPort(int port);

    void initAgenda();

    IDirectory getAgenda();

    void initAgenda(IDirectory agenda);

    void initConversationService();

    IConversationService getConversationService();
    
    void initConversationService(IConversationService conversationService);

    List<String> getAgendaContacts();

    String getMessagesByContact(String contactNickname);

    void addNewContact(User user);

    boolean existsConversationWith(String contactNickname);

    Optional<User> getContactByNickname(String contactNickname);

    void startNewConversation(String contactNickname);

    void sendMessage(Message message);

    void receiveMessage(Message message);
}
