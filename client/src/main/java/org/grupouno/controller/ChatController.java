package org.grupouno.controller;

import org.grupouno.config.ConfigService;
import org.grupouno.encryption.Encrypter;
import org.grupouno.encryption.EncrypterFactory;
import org.grupouno.model.directory.User;
import org.grupouno.model.protocols.Message;
import org.grupouno.model.protocols.MessageType;
import org.grupouno.model.session.ChatSessionImpl;
import org.grupouno.model.session.IChatSession;
import org.grupouno.network.ChatClientImpl;
import org.grupouno.network.IChatClient;
import org.grupouno.persistence.ISessionPersistence;
import org.grupouno.persistence.PersistenceFactory;
import org.grupouno.view.AgendaScreen;
import org.grupouno.view.ChatSessionScreen;
import org.grupouno.view.DirectoryScreen;
import org.grupouno.view.IChatSessionScreen;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
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
    private IChatSession chatSession;
    private IChatSessionScreen chatSessionScreen;
    private AgendaScreen agendaScreen;
    private DirectoryScreen directoryScreen;
    private ISessionPersistence persistence;
    private String encStrat;
    private Encrypter encrypter;
    private String lastMessageSender = "";

    private ChatController() {
    }

    public synchronized static ChatController getInstance() {
        if (instance == null) {
            instance = new ChatController();
        }
        return instance;
    }

    public void startChatSession(String nickname, String ip, int port) throws IOException {
        this.chatClient = new ChatClientImpl(nickname, ip, port, ConfigService.getConfig("monitor.address.server.host"), Integer.parseInt(ConfigService.getConfig("monitor.address.server.port")), this);
        logger.info("Starting chat session with nickname: " + nickname);
        this.chatSessionScreen = new ChatSessionScreen(nickname, ip, String.valueOf(port));
        this.chatSession = ChatSessionImpl.getInstance();
        this.chatSession.setNickname(nickname);
        this.chatSession.setIp(ip);
        this.chatSession.setPort(port);
        this.chatSession.initAgenda();
        this.chatSession.initConversationService();
//        this.chatClient.registerWithServer(nickname, ip, port);
        new Thread((Runnable) this.chatClient).start();
        this.chatSessionScreen.setVisible(true);

        //TODO Persistence service
        //Verifica si ya existe archivo de guardado para este usuario.
        persistence = PersistenceFactory.getPersistence(nickname);
        if (persistence == null) { // Si no existe, pregunto cuál quiere usar.
            String[] options = new String[]{"XML", "JSON", "TXT"};
            String ans = (String) JOptionPane.showInputDialog((Component) this.chatSessionScreen, "Elija el tipo de archivo de guardado:", "Formato de guardado", JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
            // Creo el nuevo archivo de guardado
            int type = 0;
            if (ans.equals(options[0]))
                type = PersistenceFactory.XML_PERSISTENCE;
            else if (ans.equals(options[1]))
                type = PersistenceFactory.JSON_PERSISTENCE;
            else if (ans.equals(options[2]))
                type = PersistenceFactory.TXT_PERSISTENCE;

            persistence = PersistenceFactory.getPersistence(nickname, type);
        } else {
            logger.info("Loading chat session.");
            persistence.loadSession();
            chatSessionScreen.updateConversationList();
        }
        // Instancia de encriptador
        // Primero se elige una estrategia (al azar jaja)
        int max = Integer.parseInt(ConfigService.getConfig("EncryptionStrategyAmount"));
        int randomStrat = (int) (Math.random() * max) + 1;
        encStrat = ConfigService.getConfig("EncryptionStrategy" + randomStrat);
        logger.info("Eligiendo estrategia de encriptacion: " + encStrat);
        encrypter = EncrypterFactory.createEncrypter(encStrat);
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

    public void disconect() {
        //Persiste datos del usuario
        logger.info("Persisting chat session.");
        persistence.saveSession();
        logger.info("Disconnected from chat session.");
        JOptionPane.showMessageDialog(null, "Desconectado de la sesión de chat.");
        this.chatClient.disconnect(this.chatSessionScreen.getSessionUsername());
        this.chatSessionScreen.closeWindow();
    }

    /**
     * Method to open the agenda screen to select a contact for a new conversation.
     * <p>
     * Opens the agenda screen and sets the contact list to the contacts in the chat session.
     */
    private void openNewConversationScreen() {
        logger.info("Opening new conversation screen.");
        this.agendaScreen = new AgendaScreen();
        this.agendaScreen.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
        this.agendaScreen.addActionListener(this);
        this.agendaScreen.setContactList(this.chatSession.getAgendaContacts());
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
        // CARGAR ARCHIVO DE GUARDADO
    }

    /**
     * Method to open the add contact screen.
     * <p>
     * Opens the add contact screen to allow the user to add a new contact.
     */
    public void openDirectoryScreen() {
        logger.info("Opening directory screen.");
        this.directoryScreen = new DirectoryScreen();
        this.directoryScreen.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
        this.directoryScreen.addActionListener(this);
        this.chatClient.getConnectedUsers(this.chatSession.getNickname());
        this.directoryScreen.setDefaultCloseOperation(DirectoryScreen.DISPOSE_ON_CLOSE);
        this.directoryScreen.setVisible(true);
    }

    public void updateDirectory(Message message) {
        logger.info("Updating directory with received user list.");
        @SuppressWarnings("unchecked")
        Set<User> connectedUsers = (Set<User>) message.content();
        if (connectedUsers != null) {
            logger.info("Received " + connectedUsers.size() + " connected users.");
            this.directoryScreen.setActiveUsers(connectedUsers, this.chatSession.getNickname());
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
            this.chatSession.addNewContact(new User(contactName, contactIp, contactPort));
            logger.info("New contact added: " + contactName);
            if (this.agendaScreen != null)
                this.agendaScreen.setContactList(this.chatSession.getAgendaContacts());
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
            Optional<User> contact = this.chatSession.getContactByNickname(contactNickName);
            if (contact.isPresent()) {
                LocalDateTime timeStamp = LocalDateTime.now();
                //CIFRADO... Seteo la estrategia elegida por el cliente primero!
                this.encrypter = EncrypterFactory.createEncrypter(encStrat);
                this.encrypter.setKey(this.chatSession.getNickname(), contact.get().nickname());
                String encryptedData = encStrat + "|" + encrypter.encrypt(textInputArea);
                Message encryptedMessage = new Message(this.chatSession.getNickname(), this.chatSession.getIp(), this.chatSession.getPort(), contact.get().nickname(), contact.get().ip(), contact.get().port(), encryptedData, timeStamp, MessageType.MESSAGE);
                Message message = new Message(this.chatSession.getNickname(), this.chatSession.getIp(), this.chatSession.getPort(), contact.get().nickname(), contact.get().ip(), contact.get().port(), textInputArea, timeStamp, MessageType.MESSAGE);
                logger.info("Sending message: " + textInputArea);
                logger.info("Encrypted as: " + encryptedData);
                this.chatClient.sendMessage(encryptedMessage);
                this.chatSession.sendMessage(message);
                this.chatSessionScreen.appendNewMessageToChatArea(message.getFormattedMessageHTML(this.chatSession.getNickname(), lastMessageSender));
                this.lastMessageSender = message.senderNickname();
                this.chatSessionScreen.resetTextInputArea();
                logger.info("Message sent to " + contactNickName);
            }
        }
    }

    public synchronized void receiveMessage(Message message) {
        logger.info("Receiving message from: " + message.senderNickname());
        // DESCIFRAR PRIMERO! Primero obtengo la estrategia de encriptado...
        String body = (String) message.content();
        String bodyEnc = body.substring(0, body.indexOf('|'));
        body = body.substring(body.indexOf('|') + 1);
        logger.info("Decrypting \"" + body + "\" with strategy: " + bodyEnc);
        this.encrypter = EncrypterFactory.createEncrypter(encStrat);
        this.encrypter.setKey(this.chatSession.getNickname(), message.senderNickname());
        Message decryptedMessage = new Message(message.senderNickname(),
                message.senderIP(),
                message.senderPort(),
                message.receiverNickname(),
                message.receiverIP(),
                message.receiverPort(),
                encrypter.decrypt(body),
                message.timestamp(),
                message.type());
        this.chatSession.receiveMessage(decryptedMessage);
        if (message.senderNickname().equals(this.chatSessionScreen.getCurrentConversationContact())) {
            logger.info("Message received from current conversation contact: " + decryptedMessage.senderNickname());
            this.chatSessionScreen.appendNewMessageToChatArea(decryptedMessage.getFormattedMessageHTML(this.chatSession.getNickname(), lastMessageSender));
            this.lastMessageSender = message.senderNickname();
        } else {
            logger.info("New message from: " + decryptedMessage.senderNickname());
            this.chatSessionScreen.updateConversationList(decryptedMessage.senderNickname());
            JOptionPane.showMessageDialog(null, "Nuevo mensaje de " + decryptedMessage.senderNickname());
        }
    }

    public void setCurrentContact(String selectedContact) {
        logger.info("Setting current contact: " + selectedContact);
        this.chatSessionScreen.enableInputArea();
        this.chatSessionScreen.setChatTitle("Conversando con: " + selectedContact);
        this.chatSessionScreen.setChatAreaText(this.chatSession.getMessagesByContact(selectedContact));
        this.lastMessageSender = this.chatSession.getLastSender();
        this.chatSessionScreen.selectContactInList(selectedContact);
    }

    public void networkError(String s) {
        this.chatSessionScreen.networkError(s);
    }
}
