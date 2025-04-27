package org.grupouno.model.session;

import org.grupouno.model.conversation.ConversationService;
import org.grupouno.model.conversation.IConversation;
import org.grupouno.model.conversation.IConversationService;
import org.grupouno.model.conversation.Message;
import org.grupouno.model.directory.Directory;
import org.grupouno.model.directory.IDirectory;
import org.grupouno.model.directory.User;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Singleton class that represents the chat application.
 * It contains the agendaImpl and the list of conversations.
 */
public class ChatSession {
    private static final Logger logger = Logger.getLogger(ChatSession.class.getName());
    private static ChatSession instance;
    private String nickname;
    private String ip;
    private int port;
    private IDirectory agenda;
    private IConversationService conversationService;

    private ChatSession() {

    }

    public static ChatSession getInstance() {
        if (ChatSession.instance == null) {
            ChatSession.instance = new ChatSession();
        }
        return ChatSession.instance;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void initAgenda() {
        logger.info("Initializing agenda");
        this.agenda = new Directory();
    }

    public void initConversationService() {
        logger.info("Initializing conversation service");
        this.conversationService = new ConversationService();
    }

    public synchronized List<String> getAgendaContacts() {
        return this.agenda.getContactNicknames();
    }

    public synchronized String getMessagesByContact(String contactNickname) {
        IConversation IConversation = this.conversationService.getConversationByContactNickname(contactNickname);
        if (IConversation == null) {
            logger.info("Conversation not found, starting new conversation.");
            this.conversationService.startNewConversation(contactNickname);
        }
        IConversation c = this.conversationService.getConversationByContactNickname(contactNickname);
        return c.getMessages().stream()
                .map(Message::getFormattedMessage)
                .reduce("", (acc, message) -> acc + message + "\n");
    }

    public synchronized void addNewContact(User user) {
        this.agenda.addContact(user);
    }

    public synchronized boolean existsConversationWith(String contactNickname) {
        return this.conversationService.existsConversationWith(contactNickname);
    }


    public synchronized Optional<User> getContactByNickname(String contactNickname) {
        return this.agenda.getContactByNickname(contactNickname);
    }

    public synchronized void startNewConversation(String contactNickname) {
        this.conversationService.startNewConversation(contactNickname);
    }


    public synchronized void sendMessage(Message message) {
        this.conversationService.addMessage(message, message.receiverNickname());
    }

    public synchronized void receiveMessage(Message message) {
        if (!this.agenda.isContactInAgenda(message.senderNickname())) {
            logger.info("Contact not found in agendaImpl, adding new contact:" + message.senderNickname());
            this.agenda.addContact(new User(message.senderNickname(), message.senderIP(), message.senderPort()));
        }
        if (!this.conversationService.existsConversationWith(message.senderNickname())) {
            logger.info("Conversation not found, starting new conversation.");
            this.conversationService.startNewConversation(message.senderNickname());
        }
        this.conversationService.addMessage(message, message.senderNickname());
    }
}