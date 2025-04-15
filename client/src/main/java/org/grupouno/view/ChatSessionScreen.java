package org.grupouno.view;

import org.grupouno.controller.ChatController;

import javax.swing.*;
import java.awt.*;

public class ChatSessionScreen extends JFrame implements IChatSessionScreen {
    private final JList<String> conversationList;
    private final JTextArea chatArea;
    private final JTextField messageField;
    private final JLabel chatTitle;
    private String currentConversationContact;

    public ChatSessionScreen(String username, String ip, String port) {
        setTitle("SMChat | Running as " + username + " on " + ip + ":" + port);
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Set current conversation contact to empty string
        this.currentConversationContact = "";

        // Left Panel - Conversations List
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel conversationsLabel = new JLabel("Conversaciones", SwingConstants.CENTER);
        conversationsLabel.setFont(new Font("Arial", Font.BOLD, 14));
        leftPanel.add(conversationsLabel, BorderLayout.NORTH);

        // Center Panel - ChatSession Area
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        this.chatTitle = new JLabel("Seleccione un contacto ", SwingConstants.CENTER);
        chatTitle.setFont(new Font("Arial", Font.BOLD, 16));
        centerPanel.add(chatTitle, BorderLayout.NORTH);

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setFont(new Font("Arial", Font.PLAIN, 12));
        JScrollPane chatScrollPane = new JScrollPane(chatArea);
        centerPanel.add(chatScrollPane, BorderLayout.CENTER);


        JList<String> demoList = new JList<>(new DefaultListModel<>());
        this.conversationList = demoList;
        demoList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedContact = this.conversationList.getSelectedValue();
                if (selectedContact != null) {
                    this.currentConversationContact = selectedContact;
                    chatTitle.setText("Conversando con: " + selectedContact);
                    ChatController.getInstance().setCurrentContact(selectedContact);
                }
            }
        });
        leftPanel.add(this.conversationList, BorderLayout.CENTER);

        // New Conversation Button
        JButton newConversationButton = new JButton("Nueva Conversación");
        newConversationButton.setActionCommand("openNewConversationScreen");
        newConversationButton.addActionListener(ChatController.getInstance());
        newConversationButton.setFont(new Font("Arial", Font.BOLD, 12));
        newConversationButton.setPreferredSize(new Dimension(160, 30));
        leftPanel.add(newConversationButton, BorderLayout.SOUTH);
        add(leftPanel, BorderLayout.WEST);

        // Bottom Panel - Message Input
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        messageField = new JTextField();
        bottomPanel.add(messageField, BorderLayout.CENTER);

        JButton sendButton = new JButton("Enviar");
        sendButton.setActionCommand("send");
        sendButton.addActionListener(ChatController.getInstance());
        sendButton.setFont(new Font("Arial", Font.BOLD, 12));
        sendButton.setPreferredSize(new Dimension(80, 30));
        bottomPanel.add(sendButton, BorderLayout.EAST);

        centerPanel.add(bottomPanel, BorderLayout.SOUTH);
        add(centerPanel, BorderLayout.CENTER);
        setLocationRelativeTo(null);
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
    public void updateConversationList(String contact) {
        // Update the conversation list with the new contact
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
        this.chatArea.append(message);
    }

    @Override
    public void selectContactInList(String contact) {
        this.conversationList.setSelectedValue(contact, true);
    }

}
