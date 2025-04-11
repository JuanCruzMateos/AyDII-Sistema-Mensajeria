package org.grupouno.model;

import org.grupouno.exceptions.ConversationNotFoundException;

import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

/**
 * Singleton class that represents the chat application.
 * It contains the agenda and the list of conversations.
 */
public class ChatSession implements IChatSession {
    private static final Logger logger = Logger.getLogger(ChatSession.class.getName());
    private static ChatSession instance;
    private Agenda agenda;
    private ConversationService conversationService;

    private ChatSession() {

    }

    public static ChatSession getInstance() {
        if (ChatSession.instance == null) {
            ChatSession.instance = new ChatSession();
        }
        return ChatSession.instance;
    }

    public void initAgenda() {
        logger.info("Initializing agenda");
        this.agenda = new Agenda(new HashMap<>());
        this.agenda.addContact(new User("juan", "127.0.0.1", 50747));
        this.agenda.addContact(new User("eze", "127.0.0.1", 50748));
        this.agenda.addContact(new User("erik", "127.0.0.1", 50749));
    }

    public void initConversationService() {
        this.conversationService = new ConversationService(new HashMap<>());
    }

    public synchronized List<String> getAgendaContacts() {
        return this.agenda.contacts().keySet().stream().toList();
    }

    public synchronized String getMessagesByContact(String contactNickname) {
        Conversation conversation = this.conversationService.getConversationByContactNickname(contactNickname);
        if (conversation == null) {
            logger.info("Conversation not found, starting new conversation.");
            this.conversationService.startNewConversation(contactNickname);
        }
        Conversation c = this.conversationService.getConversationByContactNickname(contactNickname);
        return c.messages().stream()
                .map(Message::getFormattedSendedMessage)
                .reduce("", (acc, message) -> acc + message + "\n");
    }

    @Override
    public synchronized void addNewContact(User user) {
        this.agenda.addContact(user);
    }

    @Override
    public synchronized boolean isContactInAgenda(String contactNickname) {
        return this.agenda.isContactInAgenda(contactNickname);
    }

    @Override
    public synchronized boolean existsConversationWith(String contactNickname) {
        return this.conversationService.existsConversationWith(contactNickname);
    }

    @Override
    public synchronized User getContactByNickname(String contactNickname) {
        return this.agenda.getContactByNickname(contactNickname);
    }

    @Override
    public synchronized void startNewConversation(String contactNickname) {
        this.conversationService.startNewConversation(contactNickname);
    }

    @Override
    public synchronized Conversation getConversationByContactNickname(String receiverNickname) throws ConversationNotFoundException {
        if (!this.conversationService.existsConversationWith(receiverNickname)) {
            throw new ConversationNotFoundException("Conversation not found with " + receiverNickname);
        } else {
            return this.conversationService.getConversationByContactNickname(receiverNickname);
        }
    }

    @Override
    public synchronized void sendMessage(Message message) {
        this.conversationService.addMessage(message, message.receiverNickname());
    }

    @Override
    public synchronized void receiveMessage(Message message) {
        if (!this.agenda.isContactInAgenda(message.senderNickname())) {
            logger.info("Contact not found in agenda, adding new contact:" + message.senderNickname());
            this.agenda.addContact(new User(message.senderNickname(), message.senderIP(), message.senderPort()));
        }
        if (!this.conversationService.existsConversationWith(message.senderNickname())) {
            logger.info("Conversation not found, starting new conversation.");
            this.conversationService.startNewConversation(message.senderNickname());
        }
        this.conversationService.addMessage(message, message.senderNickname());
    }
}
