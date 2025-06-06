package org.grupouno.view;

import org.grupouno.controller.ChatController;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class AgendaScreen extends JDialog {
    private final JList<String> contactList;
    private final JButton startConversationButton;
    private final JButton addNewContactButton;

    public AgendaScreen() {
        setTitle("Nueva Conversación");
        setSize(300, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        super.getContentPane().setBackground(GUIColors.backgroundColor);

        // Main Panel with Padding
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        add(mainPanel, BorderLayout.CENTER);
        mainPanel.setBackground(GUIColors.backgroundColor);

        // Title
        JLabel titleLabel = new JLabel("Agenda", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(GUIColors.textFontColor);
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Contact List
        DefaultListModel<String> contactModel = new DefaultListModel<>();
        contactList = new JList<>(contactModel);
        contactList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        contactList.setBackground(GUIColors.textFieldColor);
        contactList.setForeground(GUIColors.textFontColor);
        JScrollPane listScrollPane = new JScrollPane(contactList);
        mainPanel.add(listScrollPane, BorderLayout.CENTER);

        // Bottom Panel (Buttons)
        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        buttonPanel.setBackground(GUIColors.backgroundColor);
        this.startConversationButton = new JButton("Comenzar Conversacion");
        startConversationButton.setActionCommand("startConversation");
        startConversationButton.setFont(new Font("Arial", Font.BOLD, 12));
        startConversationButton.setPreferredSize(new Dimension(160, 35));
        startConversationButton.setBackground(GUIColors.buttonColor);
        startConversationButton.setForeground(GUIColors.textFontColor);
        buttonPanel.add(startConversationButton);

        this.addNewContactButton = new JButton("Ver Directorio");
        addNewContactButton.setActionCommand("openDirectoryScreen");
        addNewContactButton.setFont(new Font("Arial", Font.BOLD, 12));
        addNewContactButton.setPreferredSize(new Dimension(160, 35));
        addNewContactButton.setBackground(GUIColors.buttonColor);
        addNewContactButton.setForeground(GUIColors.textFontColor);
        buttonPanel.add(addNewContactButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        setLocationRelativeTo(null);
    }

    public void addActionListener(ChatController chatController) {
        this.startConversationButton.addActionListener(chatController);
        this.addNewContactButton.addActionListener(chatController);
    }

    public void setContactList(List<String> contacts) {
        DefaultListModel<String> contactListModel = new DefaultListModel<>();
        for (String contact : contacts) {
            contactListModel.addElement(contact);
        }
        this.contactList.setModel(contactListModel);
    }

    public String getSelectedContact() {
        return this.contactList.getSelectedValue();
    }
}
