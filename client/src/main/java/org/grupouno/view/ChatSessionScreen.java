package org.grupouno.view;

import org.grupouno.controller.ChatController;
import org.grupouno.model.session.ChatSessionImpl;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class ChatSessionScreen extends JFrame implements IChatSessionScreen {
    private final JList<String> conversationList;
    private final JTextPane chatArea;
    private final JTextField messageField;
    private final JButton sendButton;
    private final JLabel chatTitle;
    private final String sessionUsername;
    private String currentConversationContact;

    public ChatSessionScreen(String username, String ip, String port) {
        setTitle("SMChat | Running as " + username + " on " + ip + ":" + port);
        setSize(620, 520);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        super.getContentPane().setBackground(GUIColors.backgroundColor);

        this.setLocationRelativeTo(null);
        this.sessionUsername = username;

        // Top Panel - User Info and Disconnect
        JPanel topPanel = new JPanel(new GridBagLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
        topPanel.setBackground(new Color(245, 245, 245)); // light gray background

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.anchor = GridBagConstraints.WEST;

        JLabel userInfoLabel = new JLabel("👤 Usuario: " + username + "   🌐 IP: " + ip + "   📡 Puerto: " + port);
        userInfoLabel.setFont(new Font("Dialog", Font.PLAIN, 14));
        topPanel.add(userInfoLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.EAST;

        JButton disconnectButton = new JButton("Desconectar ❌");
        disconnectButton.setActionCommand("disconnect");
        disconnectButton.addActionListener(ChatController.getInstance());
        disconnectButton.setFont(new Font("Dialog", Font.BOLD, 12));
//        disconnectButton.setForeground(Color.WHITE);
        disconnectButton.setBackground(new Color(217, 22, 42)); // Bootstrap red
        disconnectButton.setFocusPainted(false);
//        disconnectButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        topPanel.add(disconnectButton, gbc);
        add(topPanel, BorderLayout.NORTH);

        // Set current conversation contact to empty string
        this.currentConversationContact = "";

        // Left Panel - Conversations List
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        leftPanel.setBackground(GUIColors.backgroundColor);

        JLabel conversationsLabel = new JLabel("Conversaciones 📨", SwingConstants.CENTER);
        conversationsLabel.setFont(new Font("Dialog", Font.BOLD, 18));
        leftPanel.add(conversationsLabel, BorderLayout.NORTH);

        // Center Panel - Chat Area
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        centerPanel.setBackground(GUIColors.backgroundColor);

        this.chatTitle = new JLabel("Seleccione un contacto ", SwingConstants.CENTER);
        chatTitle.setFont(new Font("Dialog", Font.BOLD, 18));
        centerPanel.add(chatTitle, BorderLayout.NORTH);

        JList<String> demoList = new JList<>(new DefaultListModel<>());
        this.conversationList = demoList;
        demoList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedContact = this.conversationList.getSelectedValue();
                if (selectedContact != null) {
                    this.currentConversationContact = selectedContact;
                    chatTitle.setText("Conversando con 📥 " + selectedContact);
                    ChatController.getInstance().setCurrentContact(selectedContact);
                }
            }
        });
        this.conversationList.setBackground(GUIColors.textFieldColor);
        leftPanel.add(this.conversationList, BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel(new BorderLayout(1, 1));
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        buttonsPanel.setBackground(GUIColors.backgroundColor);

        // New Conversation Button
        JButton newConversationButton = new JButton("Nueva Conversación");
        newConversationButton.setActionCommand("openNewConversationScreen");
        newConversationButton.addActionListener(ChatController.getInstance());
        newConversationButton.setFont(new Font("Dialog", Font.BOLD, 12));
        newConversationButton.setPreferredSize(new Dimension(160, 30));
        newConversationButton.setBackground(GUIColors.buttonColor);
        buttonsPanel.add(newConversationButton, BorderLayout.NORTH);

        // Open Directory Button
        JButton openDirectoryButton = new JButton("Ver Directorio");
        openDirectoryButton.setActionCommand("openDirectoryScreen");
        openDirectoryButton.addActionListener(ChatController.getInstance());
        openDirectoryButton.setFont(new Font("Dialog", Font.BOLD, 12));
        openDirectoryButton.setPreferredSize(new Dimension(160, 30));
        openDirectoryButton.setBackground(GUIColors.buttonColor);
        buttonsPanel.add(openDirectoryButton, BorderLayout.SOUTH);

        leftPanel.add(buttonsPanel, BorderLayout.SOUTH);
        leftPanel.setBackground(GUIColors.backgroundColor);

        add(leftPanel, BorderLayout.WEST);

        chatArea = new JTextPane();
        chatArea.setContentType("text/html");
        chatArea.setEditable(false);
        chatArea.setFont(new Font("Dialog", Font.PLAIN, 12));
        chatArea.setBackground(GUIColors.textFieldColor);
        JScrollPane chatScrollPane = new JScrollPane(chatArea);
        centerPanel.add(chatScrollPane, BorderLayout.CENTER);

        // Bottom Panel - Message Input
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        bottomPanel.setBackground(GUIColors.backgroundColor);
        messageField = new JTextField();
        messageField.setBackground(GUIColors.textFieldColor);
        bottomPanel.add(messageField, BorderLayout.CENTER);
        // Press enter to send.
        messageField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyChar() == '\n')
                    ChatController.getInstance().actionPerformed(new ActionEvent(evt, 1001, "send"));
            }
        });

        sendButton = new JButton("Enviar ▶");
        sendButton.setActionCommand("send");
        sendButton.addActionListener(ChatController.getInstance());
        sendButton.setFont(new Font("Dialog", Font.BOLD, 12));
        sendButton.setPreferredSize(new Dimension(90, 30));
        sendButton.setBackground(GUIColors.buttonColor);
        bottomPanel.add(sendButton, BorderLayout.EAST);

        // Inician deshabilitados porque no hay nada para hacer sin chats seleccionados...
        this.messageField.setEnabled(false);
        this.chatArea.setEnabled(false);
        this.sendButton.setEnabled(false);


        centerPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(centerPanel, BorderLayout.CENTER);
        setLocationRelativeTo(null);

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });
    }

    public String getTextInputArea() {
        return messageField.getText().trim();
    }

    public String getCurrentConversationContact() {
        return this.currentConversationContact;
    }

    public void setChatTitle(String chatTitle) {
        this.chatTitle.setText(chatTitle);
    }

    @Override
    public void updateConversationList() {
        DefaultListModel<String> model = new DefaultListModel<>();
        for (String contact : ChatSessionImpl.getInstance().getAgendaContacts()) {
            model.addElement(contact);
        }
        this.conversationList.setModel(model);
    }

    @Override
    public void updateConversationList(String contact) {
        DefaultListModel<String> model = (DefaultListModel<String>) this.conversationList.getModel();
        if (!model.contains(contact)) {
            model.addElement(contact);
        }
        this.conversationList.setModel(model);
    }

    @Override
    public void setChatAreaText(String messagesByContact) {
        this.chatArea.setText(messagesByContact);
    }

    @Override
    public void resetTextInputArea() {
        this.messageField.setText("");
    }

    @Override
    public void appendNewMessageToChatArea(String message) {
        int pos = chatArea.getText().indexOf("</body>");
        String oldContent = chatArea.getText();
        String newContent = oldContent.substring(0, pos) + message + oldContent.substring(pos);
        System.out.println(message);

        chatArea.setText(newContent);
    }

    @Override
    public void selectContactInList(String contact) {
        this.conversationList.setSelectedValue(contact, true);
    }

    @Override
    public String getSessionUsername() {
        return this.sessionUsername;
    }

    @Override
    public void closeWindow() {
        dispose();
    }

    @Override
    public void networkError(String s) {
        JOptionPane.showMessageDialog(this, s + "\nIntente otra vez.", "Error de red", JOptionPane.ERROR_MESSAGE);
        this.closeWindow();
    }

    private void formWindowClosing(java.awt.event.WindowEvent evt) {
        ChatController.getInstance().disconect();
    }

    public void enableInputArea() {
        this.messageField.setEnabled(true);
        this.chatArea.setEnabled(true);
        this.sendButton.setEnabled(true);
    }

}


