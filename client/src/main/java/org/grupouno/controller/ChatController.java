package org.grupouno.controller;

import org.grupouno.model.agenda.User;
import org.grupouno.model.conversation.Message;
import org.grupouno.model.conversation.MessageType;
import org.grupouno.model.session.ChatSession;
import org.grupouno.network.ChatClientImpl;
import org.grupouno.network.IChatClient;
import org.grupouno.view.AddContactScreen;
import org.grupouno.view.AgendaScreen;
import org.grupouno.view.ChatSessionScreen;
import org.grupouno.view.IChatSessionScreen;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.logging.Logger;


/**
 * ChatController is a singleton class that manages the chat application.
 * It handles the chat session, user interactions, and message sending.
 */
public class ChatController implements ActionListener {
    private static final Logger logger = Logger.getLogger(ChatController.class.getName());
    private static ChatController instance;
    private IChatClient chatClient;
    private ChatSession chatSession;
    private IChatSessionScreen chatSessionScreen;
    private AgendaScreen agendaScreen;
    private AddContactScreen addContactScreen;

    private ChatController() {
    }

    public static ChatController getInstance() {
        if (instance == null) {
            instance = new ChatController();
        }
        return instance;
    }

    public void startChatSession(String nickname, String ip, int port) throws IOException {
        logger.info("Starting chat session with nickname: " + nickname);
        this.chatSessionScreen = new ChatSessionScreen(nickname, ip, String.valueOf(port));
        this.chatSession = ChatSession.getInstance();
        this.chatSession.setNickname(nickname);
        this.chatSession.setIp(ip);
        this.chatSession.setPort(port);
        this.chatSession.initAgenda();
        this.chatSession.initConversationService();
        this.chatClient = new ChatClientImpl(new Socket("127.0.0.1", 50479));
        this.chatSessionScreen.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        logger.info("Action performed: " + command);
        switch (command) {
            case "send" -> this.sendMessage();
            case "openNewConversationScreen" -> this.openNewConversationScreen();
            case "openNewContactScreen" -> this.openNewContactScreen();
            case "addNewContact" -> this.addContact();
            case "startConversation" -> this.startConversation();
        }
    }

    /**
     * Method to open the agenda screen to select a contact for a new conversation.
     * <p>
     * Opens the agenda screen and sets the contact list to the contacts in the chat session.
     */
    private void openNewConversationScreen() {
        logger.info("Opening agenda screen to select a contact.");
        this.agendaScreen = new AgendaScreen();
        this.agendaScreen.setDefaultCloseOperation(AgendaScreen.DISPOSE_ON_CLOSE);
        this.agendaScreen.addActionListener(this);
        this.agendaScreen.setContactList(this.chatSession.getAgendaContacts());
        this.agendaScreen.setVisible(true);
        this.agendaScreen.setDefaultCloseOperation(AgendaScreen.DISPOSE_ON_CLOSE);
    }


    /**
     * Method to start a new conversation with the selected contact from the agenda screen.
     */
    private void startConversation() {
        logger.info("Starting conversation with selected contact.");
        String contact = this.agendaScreen.getSelectedContact();
        if (contact != null) {
            logger.info("Starting conversation with: " + contact);
            this.chatSessionScreen.setChatTitle("Chat with: " + contact);
            this.chatSessionScreen.updateConversationList(contact);
            if (!this.chatSession.existsConversationWith(contact)) {
                logger.info("No existing conversation found, starting a new one.");
                this.chatSession.startNewConversation(contact);
            }
            this.chatSessionScreen.setChatAreaText(this.chatSession.getMessagesByContact(contact));
            this.chatSessionScreen.selectContactInList(contact);
            JOptionPane.showMessageDialog(null, "Iniciando chat con " + contact);
            this.agendaScreen.dispose();
        } else {
            JOptionPane.showMessageDialog(null, "Seleccione un contacto.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Method to open the add contact screen.
     * <p>
     * Opens the add contact screen to allow the user to add a new contact.
     */
    private void openNewContactScreen() {
        logger.info("Opening add contact screen.");
        this.addContactScreen = new AddContactScreen();
        this.addContactScreen.setVisible(true);
        this.addContactScreen.setDefaultCloseOperation(AddContactScreen.DISPOSE_ON_CLOSE);
        this.addContactScreen.addActionListener(this);
    }

    private void addContact() {
        logger.info("Adding new contact.");
        String contactName = this.addContactScreen.getContactName();
        String contactIp = this.addContactScreen.getContactIp();
        int contactPort = this.addContactScreen.getContactPort();
        if (contactName.isEmpty() || contactIp.isEmpty() || contactPort == 0) {
            JOptionPane.showMessageDialog(null, "Por favor, complete todos los campos.", "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            this.chatSession.addNewContact(new User(contactName, contactIp, contactPort));
            logger.info("New contact added: " + contactName);
            this.agendaScreen.setContactList(this.chatSession.getAgendaContacts());
            JOptionPane.showMessageDialog(null, "Contacto agregado: " + contactName);
            this.addContactScreen.dispose();
        }
    }

    public void sendMessage() {
        String textInputArea = this.chatSessionScreen.getTextInputArea();
        String contactNickName = this.chatSessionScreen.getCurrentConversationContact();
        logger.info("Sending message to " + contactNickName);
        if (textInputArea.isEmpty()) {
            JOptionPane.showMessageDialog(null, "El contacto no está disponible.", "Error", JOptionPane.ERROR_MESSAGE);
            logger.warning("Contact not available: " + contactNickName);
        } else {
            User contact = this.chatSession.getContactByNickname(contactNickName);
            if (contact != null) {
                LocalDateTime timeStamp = LocalDateTime.now();
                Message message = new Message(this.chatSession.getNickname(), this.chatSession.getIp(), this.chatSession.getPort(), contact.nickname(), contact.ip(), contact.port(), textInputArea, timeStamp, MessageType.MESSAGE);
                logger.info("Sending message: " + textInputArea);
                this.chatClient.sendMessage(contact.nickname(), contact.ip(), contact.port(), textInputArea);
                this.chatSession.sendMessage(message);
                this.chatSessionScreen.appendNewMessageToChatArea(message.getFormattedSendedMessage() + "\n");
                this.chatSessionScreen.resetTextInputArea();
                logger.info("Message sent to " + contactNickName);
            }
        }
    }

    public synchronized void receiveMessage(Message message) {
        logger.info("Receiving message from: " + message.senderNickname());
        this.chatSession.receiveMessage(message);
        if (message.senderNickname().equals(this.chatSessionScreen.getCurrentConversationContact())) {
            logger.info("Message received from current conversation contact: " + message.senderNickname());
            this.chatSessionScreen.appendNewMessageToChatArea(message.getFormattedSendedMessage());
        } else {
            logger.info("New message from: " + message.senderNickname());
            this.chatSessionScreen.updateConversationList(message.senderNickname());
            JOptionPane.showMessageDialog(null, "Nuevo mensaje de " + message.senderNickname());
        }
    }

    public void setCurrentContact(String selectedContact) {
        logger.info("Setting current contact: " + selectedContact);
        this.chatSessionScreen.setChatTitle("Conversando con: " + selectedContact);
        this.chatSessionScreen.setChatAreaText(this.chatSession.getMessagesByContact(selectedContact));
        this.chatSessionScreen.selectContactInList(selectedContact);
    }
}
