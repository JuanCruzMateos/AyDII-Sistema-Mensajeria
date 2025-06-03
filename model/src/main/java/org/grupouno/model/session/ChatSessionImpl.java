package org.grupouno.model.session;

import org.grupouno.model.conversation.ConversationService;
import org.grupouno.model.conversation.IConversation;
import org.grupouno.model.conversation.IConversationService;
import org.grupouno.model.directory.Directory;
import org.grupouno.model.directory.IDirectory;
import org.grupouno.model.directory.User;
import org.grupouno.model.protocols.Message;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Singleton class that represents the chat application.
 * It contains the agendaImpl and the list of conversations.
 */
public class ChatSessionImpl implements IChatSession {
    private static final Logger logger = Logger.getLogger(ChatSessionImpl.class.getName());
    private static IChatSession instance;
    private String nickname;
    private String ip;
    private int port;
    private IDirectory agenda;
    private IConversationService conversationService;

    private ChatSessionImpl() {

    }

    public static IChatSession getInstance() {
        if (ChatSessionImpl.instance == null) {
            ChatSessionImpl.instance = new ChatSessionImpl();
        }
        return ChatSessionImpl.instance;
    }

    @Override
    public String getIp() {
        return ip;
    }

    @Override
    public void setIp(String ip) {
        this.ip = ip;
    }

    @Override
    public String getNickname() {
        return nickname;
    }

    @Override
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public void initAgenda() {
        logger.info("Initializing agenda");
        this.agenda = new Directory();
    }

    @Override
    public IDirectory getAgenda() {
        return this.agenda;
    }

    @Override
    public void initAgenda(IDirectory agenda) {
        logger.info("Loading agenda");
        this.agenda = agenda;
    }

    @Override
    public void initConversationService() {
        logger.info("Initializing conversation service");
        this.conversationService = new ConversationService();
    }

    @Override
    public IConversationService getConversationService() {
        return this.conversationService;
    }

    @Override
    public void initConversationService(IConversationService conversationService) {
        logger.info("Loading conversation service");
        this.conversationService = conversationService;
    }

    @Override
    public synchronized List<String> getAgendaContacts() {
        return this.agenda.getAllContactNicknames();
    }

    @Override
    public synchronized String getMessagesByContact(String contactNickname) {
        Optional<IConversation> IConversation = this.conversationService.getConversationByContactNickname(contactNickname);
        if (IConversation.isEmpty()) {
            logger.info("Conversation not found, starting new conversation.");
            this.conversationService.startNewConversation(contactNickname);
        }
        Optional<IConversation> res = this.conversationService.getConversationByContactNickname(contactNickname);
        if (res.isEmpty()) {
            logger.warning("Conversation not found");
            return "";
        }
        IConversation c = res.get();
        return c.getMessages().stream()
                .map(Message::getFormattedMessage)
                .reduce("", (acc, message) -> acc + message + "\n");
    }

    @Override
    public synchronized void addNewContact(User user) {
        this.agenda.addContact(user);
    }

    @Override
    public synchronized boolean existsConversationWith(String contactNickname) {
        return this.conversationService.existsConversationWith(contactNickname);
    }

    @Override
    public synchronized Optional<User> getContactByNickname(String contactNickname) {
        return this.agenda.getContactByNickname(contactNickname);
    }

    @Override
    public synchronized void startNewConversation(String contactNickname) {
        this.conversationService.startNewConversation(contactNickname);
    }

    @Override
    public synchronized void sendMessage(Message message) {
        this.conversationService.addMessage(message, message.receiverNickname());
    }

    @Override
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