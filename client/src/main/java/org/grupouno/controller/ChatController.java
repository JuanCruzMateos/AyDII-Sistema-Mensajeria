package org.grupouno.controller;

import org.grupouno.config.ConfigService;
import org.grupouno.model.conversation.Message;
import org.grupouno.model.conversation.MessageType;
import org.grupouno.model.directory.User;
import org.grupouno.model.session.ChatSessionImpl;
import org.grupouno.model.session.IChatSession;
import org.grupouno.network.ChatClientImpl;
import org.grupouno.network.IChatClient;
import org.grupouno.view.AgendaScreen;
import org.grupouno.view.ChatSessionScreen;
import org.grupouno.view.DirectoryScreen;
import org.grupouno.view.IChatSessionScreen;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;


/**
 * ChatController is a singleton class that manages the chat application.
 * It handles the chat session, user interactions, and message sending.
 */
public class ChatController implements ActionListener {
    private static final Logger logger = Logger.getLogger(ChatController.class.getName());
    private static ChatController instance;
    private IChatClient chatClient;
    private IChatSession IChatSession;
    private IChatSessionScreen chatSessionScreen;
    private AgendaScreen agendaScreen;
    private DirectoryScreen directoryScreen;

    private ChatController() {
    }

    public synchronized static ChatController getInstance() {
        if (instance == null) {
            instance = new ChatController();
        }
        return instance;
    }

    public void startChatSession(String nickname, String ip, int port) throws IOException {
//        System.out.println(ConfigService.getConfig("SERVER_IP"));
//        System.out.println(ConfigService.getConfig("SERVER_PORT"));
        this.chatClient = new ChatClientImpl(new Socket(InetAddress.getByName(ConfigService.getConfig("SERVER_IP")),
                Integer.parseInt(ConfigService.getConfig("SERVER_PORT")),
                InetAddress.getByName(ip),
                port),
                this);
        logger.info("Starting chat session with nickname: " + nickname);
        this.chatSessionScreen = new ChatSessionScreen(nickname, ip, String.valueOf(port));
        this.IChatSession = ChatSessionImpl.getInstance();
        this.IChatSession.setNickname(nickname);
        this.IChatSession.setIp(ip);
        this.IChatSession.setPort(port);
        this.IChatSession.initAgenda();
        this.IChatSession.initConversationService();
        this.chatClient.registerWithServer(nickname, ip, port);
        new Thread((Runnable) this.chatClient).start();
        this.chatSessionScreen.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        logger.info("Action performed: " + command);
        switch (command) {
            case "send" -> this.sendMessage();
            case "openNewConversationScreen" -> this.openNewConversationScreen();
            case "openDirectoryScreen" -> this.openDirectoryScreen();
            case "addContact" -> this.addContact();
            case "startConversation" -> this.startConversation();
            case "disconnect" -> this.disconect();
        }
    }

    private void disconect() {
        this.chatClient.disconnect(this.chatSessionScreen.getSessionUsername());
        this.chatSessionScreen.closeWindow();
        logger.info("Disconnected from chat session.");
        JOptionPane.showMessageDialog(null, "Desconectado de la sesión de chat.");
    }

    /**
     * Method to open the agenda screen to select a contact for a new conversation.
     * <p>
     * Opens the agenda screen and sets the contact list to the contacts in the chat session.
     */
    private void openNewConversationScreen() {
        logger.info("Opening new conversation screen.");
        this.agendaScreen = new AgendaScreen();
        this.agendaScreen.addActionListener(this);
        this.agendaScreen.setContactList(this.IChatSession.getAgendaContacts());
        this.agendaScreen.setDefaultCloseOperation(AgendaScreen.DISPOSE_ON_CLOSE);
        this.agendaScreen.setVisible(true);
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
            if (!this.IChatSession.existsConversationWith(contact)) {
                logger.info("No existing conversation found, starting a new one.");
                this.IChatSession.startNewConversation(contact);
            }
            this.chatSessionScreen.setChatAreaText(this.IChatSession.getMessagesByContact(contact));
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
    public void openDirectoryScreen() {
        logger.info("Opening directory screen.");
        this.directoryScreen = new DirectoryScreen();
        this.directoryScreen.addActionListener(this);
        this.chatClient.getConnectedUsers(this.IChatSession.getNickname());
        this.directoryScreen.setDefaultCloseOperation(DirectoryScreen.DISPOSE_ON_CLOSE);
        this.directoryScreen.setVisible(true);
    }

    public void updateDirectory(Message message) {
        logger.info("Updating directory with received user list.");
        @SuppressWarnings("unchecked")
        Set<User> connectedUsers = (Set<User>) message.content();
        if (connectedUsers != null) {
            logger.info("Received " + connectedUsers.size() + " connected users.");
            this.directoryScreen.setActiveUsers(connectedUsers, this.IChatSession.getNickname());
        } else {
            logger.warning("Received empty or null user list.");
        }
    }

    private void addContact() {
        logger.info("Adding new contact.");
        String contactName = this.directoryScreen.getContactName();
        String contactIp = this.directoryScreen.getContactIp();
        int contactPort = this.directoryScreen.getContactPort();
        if (contactName.isEmpty() || contactIp.isEmpty() || contactPort == 0) {
            JOptionPane.showMessageDialog(null, "Todos los campos deben estar definidos.", "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            this.IChatSession.addNewContact(new User(contactName, contactIp, contactPort));
            logger.info("New contact added: " + contactName);
            this.agendaScreen.setContactList(this.IChatSession.getAgendaContacts());
            JOptionPane.showMessageDialog(null, "Contacto agregado: " + contactName);
            this.directoryScreen.dispose();
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
            Optional<User> contact = this.IChatSession.getContactByNickname(contactNickName);
            if (contact.isPresent()) {
                LocalDateTime timeStamp = LocalDateTime.now();
                Message message = new Message(this.IChatSession.getNickname(), this.IChatSession.getIp(), this.IChatSession.getPort(), contact.get().nickname(), contact.get().ip(), contact.get().port(), textInputArea, timeStamp, MessageType.MESSAGE);
                logger.info("Sending message: " + textInputArea);
                this.chatClient.sendMessage(message);
                this.IChatSession.sendMessage(message);
                this.chatSessionScreen.appendNewMessageToChatArea(message.getFormattedMessage() + "\n");
                this.chatSessionScreen.resetTextInputArea();
                logger.info("Message sent to " + contactNickName);
            }
        }
    }

    public synchronized void receiveMessage(Message message) {
        logger.info("Receiving message from: " + message.senderNickname());
        this.IChatSession.receiveMessage(message);
        if (message.senderNickname().equals(this.chatSessionScreen.getCurrentConversationContact())) {
            logger.info("Message received from current conversation contact: " + message.senderNickname());
            this.chatSessionScreen.appendNewMessageToChatArea(message.getFormattedMessage());
        } else {
            logger.info("New message from: " + message.senderNickname());
            this.chatSessionScreen.updateConversationList(message.senderNickname());
            JOptionPane.showMessageDialog(null, "Nuevo mensaje de " + message.senderNickname());
        }
    }

    public void setCurrentContact(String selectedContact) {
        logger.info("Setting current contact: " + selectedContact);
        this.chatSessionScreen.setChatTitle("Conversando con: " + selectedContact);
        this.chatSessionScreen.setChatAreaText(this.IChatSession.getMessagesByContact(selectedContact));
        this.chatSessionScreen.selectContactInList(selectedContact);
    }
}
