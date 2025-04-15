package org.grupouno.view;

import org.grupouno.controller.ChatController;

import javax.swing.*;
import java.awt.*;


public class AddContactScreen extends JFrame {
    private final JTextField nicknameField;
    private final JTextField ipField;
    private final JTextField portField;
    private final JButton addButton;

    public AddContactScreen() {
        setTitle("Agregar Contacto");
        setSize(300, 250);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 20));

        // Main Panel with Padding
        JPanel mainPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        add(mainPanel, BorderLayout.CENTER);

        // Title
        JLabel titleLabel = new JLabel("Nuevo Contacto", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(titleLabel, BorderLayout.NORTH);

        // Form Fields
        mainPanel.add(new JLabel("Nickname:"));
        nicknameField = new JTextField();
        mainPanel.add(nicknameField);

        mainPanel.add(new JLabel("IP:"));
        ipField = new JTextField("127.0.0.1");
        mainPanel.add(ipField);

        mainPanel.add(new JLabel("Puerto:"));
        portField = new JTextField("50747");
        mainPanel.add(portField);

        // Add Button
        this.addButton = new JButton("Agregar");
        addButton.setActionCommand("addNewContact");
        addButton.setFont(new Font("Arial", Font.BOLD, 12));
        addButton.setPreferredSize(new Dimension(120, 35));
        add(addButton, BorderLayout.SOUTH);
        setLocationRelativeTo(null);
    }

    public void addActionListener(ChatController chatController) {
        this.addButton.addActionListener(chatController);
    }

    public String getContactName() {
        return nicknameField.getText().trim();
    }

    public String getContactIp() {
        return ipField.getText().trim();
    }

    public int getContactPort() {
        return Integer.parseInt(portField.getText().trim());
    }
}
