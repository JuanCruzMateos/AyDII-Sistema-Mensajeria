package org.grupouno.view;

import org.grupouno.controller.ChatController;
import org.grupouno.network.ConnectionValidator;

import javax.swing.*;
import java.awt.*;
import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConfigScreen extends JFrame {
    private final JTextField nicknameField;
    private final JTextField ipField;
    private final JTextField portField;

    public ConfigScreen() {
        setTitle("Sistema Mensajería");
        setSize(320, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Main Panel with Padding
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        add(mainPanel, BorderLayout.CENTER);

        // Title Label
        JLabel titleLabel = new JLabel("Sistema Mensajeria", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Spacer Panel to add more space below the title
        JPanel spacerPanel = new JPanel();
        spacerPanel.setPreferredSize(new Dimension(10, 20)); // Increase vertical space
        mainPanel.add(spacerPanel, BorderLayout.CENTER);

        // Form Panel
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 7));
        formPanel.add(new JLabel("Nickname:"));
        nicknameField = new JTextField();
        formPanel.add(nicknameField);

        formPanel.add(new JLabel("IP:"));
        String ip;
        try {
            ip = InetAddress.getLoopbackAddress().getHostAddress();
        } catch (Exception e) {
            Logger.getLogger(ConfigScreen.class.getName()).log(Level.SEVERE, null, e);
            ip = "127.0.0.1"; // Fallback to localhost
        }
        ipField = new JTextField(ip);
        formPanel.add(ipField);

        formPanel.add(new JLabel("Puerto:"));
        String port = "50747"; // Fallback to default port
        portField = new JTextField(port);
        formPanel.add(portField);

        // Wrap formPanel inside another panel to keep spacing
        JPanel formContainer = new JPanel(new BorderLayout());
        formContainer.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(formContainer, BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel();
        JButton startButton = new JButton("Iniciar");
        startButton.setFont(new Font("Arial", Font.BOLD, 14)); // Bigger font
        startButton.setPreferredSize(new Dimension(120, 40)); // Bigger button
        buttonPanel.add(startButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Button Action
        startButton.addActionListener(e -> {
            String inputNickname = nicknameField.getText();
            String inputIP = ipField.getText();
            String inputPort = portField.getText();

            if (inputNickname.isEmpty() || inputIP.isEmpty() || inputPort.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Todos los campos son obligatorios.", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                if (!ConnectionValidator.isValidIp(inputIP)) {
                    JOptionPane.showMessageDialog(null, "IP no válida. Formato esperado: xxx.xxx.xxx.xxx", "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    if (!ConnectionValidator.isValidPort(Integer.parseInt(inputPort))) {
                        JOptionPane.showMessageDialog(null, "Puerto no válido. Debe estar entre 1 y 65535.", "Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        if (!ConnectionValidator.isPortAvaliable(Integer.parseInt(inputPort))) {
                            JOptionPane.showMessageDialog(null, "Puerto no disponible. Por favor, elija otro puerto.", "Error", JOptionPane.ERROR_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(null, "Iniciando sesión como " + inputNickname, "Info", JOptionPane.INFORMATION_MESSAGE);
                            ChatController.getInstance().setNickname(inputNickname);
                            ChatController.getInstance().setIp(inputIP);
                            ChatController.getInstance().setPort(Integer.parseInt(inputPort));
                            ChatController.getInstance().startChatSession();
                            dispose();
                        }
                    }
                }

            }
        });
        setLocationRelativeTo(null);
    }
}
