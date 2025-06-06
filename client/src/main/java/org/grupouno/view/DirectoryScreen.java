package org.grupouno.view;

import org.grupouno.controller.ChatController;
import org.grupouno.model.directory.User;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class DirectoryScreen extends JDialog {
    private final JList<String> activeContactsList;
    private final JTextField nicknameField;
    private final JTextField ipField;
    private final JTextField portField;
    private final JButton addButton;
    private HashMap<String, User> activeUsers;


    public DirectoryScreen() {
        setTitle("Directorio");
        setSize(400, 350);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(null);
        setLocationRelativeTo(null);
        super.getContentPane().setBackground(GUIColors.backgroundColor);

        // Directorio Label
        JLabel directorioLabel = new JLabel("Directorio");
        directorioLabel.setFont(new Font("Dialog", Font.BOLD, 18));
        directorioLabel.setBounds(20, 10, 150, 30);
        directorioLabel.setForeground(GUIColors.textFontColor);
        add(directorioLabel);

        // List
        DefaultListModel<String> listModel = new DefaultListModel<>();
        activeContactsList = new JList<>(listModel);
        activeContactsList.setBackground(GUIColors.textFieldColor);
        activeContactsList.setForeground(GUIColors.textFontColor);
        JScrollPane listScrollPane = new JScrollPane(activeContactsList);
        listScrollPane.setBounds(20, 50, 130, 160);
        add(listScrollPane);

        // Nickname
        JLabel nicknameLabel = new JLabel("Nickname");
        nicknameLabel.setBounds(180, 50, 100, 20);
        nicknameLabel.setForeground(GUIColors.textFontColor);
        add(nicknameLabel);

        nicknameField = new JTextField("");
        nicknameField.setBounds(180, 70, 150, 25);
        add(nicknameField);
        nicknameField.setEditable(false);
        nicknameField.setBackground(GUIColors.textFieldColor);
        nicknameField.setForeground(GUIColors.textFontColor);

        // IP
        JLabel ipLabel = new JLabel("IP");
        ipLabel.setBounds(180, 100, 100, 20);
        ipLabel.setForeground(GUIColors.textFontColor);
        add(ipLabel);

        ipField = new JTextField("");
        ipField.setBounds(180, 120, 150, 25);
        add(ipField);
        ipField.setEditable(false);
        ipField.setBackground(GUIColors.textFieldColor);
        ipField.setForeground(GUIColors.textFontColor);

        // Puerto
        JLabel puertoLabel = new JLabel("Puerto");
        puertoLabel.setBounds(180, 150, 100, 20);
        puertoLabel.setForeground(GUIColors.textFontColor);
        add(puertoLabel);

        portField = new JTextField("");
        portField.setBounds(180, 170, 150, 25);
        add(portField);
        portField.setEditable(false);
        portField.setBackground(GUIColors.textFieldColor);
        portField.setForeground(GUIColors.textFontColor);

        // Agregar Button
        addButton = new JButton("Agregar");
        addButton.setActionCommand("addContact");
        addButton.setBounds(100, 250, 180, 35);
        addButton.setBackground(GUIColors.buttonColor);
        addButton.setForeground(GUIColors.textFontColor);
        add(addButton);

        // List Selection Listener
        activeContactsList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedContact = activeContactsList.getSelectedValue();
                if (selectedContact != null) {
                    this.nicknameField.setText(this.activeUsers.get(selectedContact).nickname());
                    this.ipField.setText(this.activeUsers.get(selectedContact).ip());
                    this.portField.setText(String.valueOf(this.activeUsers.get(selectedContact).port()));
                }
            }
        });
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
        String portText = portField.getText().trim();
        if (portText.isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(portText);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public void refreshActiveUsers(List<String> connectedUserNicknames) {
        DefaultListModel<String> listModel = (DefaultListModel<String>) this.activeContactsList.getModel();
        listModel.clear();
        for (String nickname : connectedUserNicknames) {
            listModel.addElement(nickname);
        }
        this.activeContactsList.setModel(listModel);
    }

    public void setActiveUsers(Set<User> activeUsers, String sessionUser) {
        this.activeUsers = new HashMap<>();
        System.out.println(activeUsers);
        for (User user : activeUsers) {
            if (!user.nickname().equals(sessionUser)) {
                this.activeUsers.put(user.nickname(), user);
            }
        }
        System.out.println(this.activeUsers);
        this.refreshActiveUsers(List.of(this.activeUsers.values().stream().map(User::nickname).toArray(String[]::new)));
    }
}
