package org.grupouno.view;

import org.grupouno.controller.ChatController;
import org.grupouno.validation.NetworkValidator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.io.IOException;
import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConfigScreen extends JFrame {
    private final JTextField nicknameField;
    private final JTextField ipField;
    private final JTextField portField;

    public ConfigScreen() {
        setTitle("Sistema de Mensajería");
        setSize(370, 280);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Main Panel with Padding
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        add(mainPanel, BorderLayout.CENTER);

        // Title Label
        JLabel titleLabel = new JLabel("Sistema de Mensajeria", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Dialog", Font.BOLD, 28));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Spacer Panel to add more space below the title
        JPanel spacerPanel = new JPanel();
        spacerPanel.setPreferredSize(new Dimension(10, 20));
        mainPanel.add(spacerPanel, BorderLayout.CENTER);

        // Form Panel
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 7));
        JLabel labelNick = new JLabel("👤 Nickname:");
        labelNick.setFont(new Font("Dialog", Font.PLAIN, 14));
        formPanel.add(labelNick);
        nicknameField = new JTextField();
        nicknameField.setFont(new Font("Dialog", Font.PLAIN, 14));
        formPanel.add(nicknameField);

        JLabel labelIP = new JLabel("🌐 IP:");
        labelIP.setFont(new Font("Dialog", Font.PLAIN, 14));
        formPanel.add(labelIP);
        String ip;
        try {
            ip = InetAddress.getLoopbackAddress().getHostAddress();
        } catch (Exception e) {
            Logger.getLogger(ConfigScreen.class.getName()).log(Level.SEVERE, null, e);
            ip = "127.0.0.1";
        }
        ipField = new JTextField(ip);
        ipField.setFont(new Font("Dialog", Font.PLAIN, 14));
        formPanel.add(ipField);

        JLabel labelPort = new JLabel("📡 Puerto:");
        labelPort.setFont(new Font("Dialog", Font.PLAIN, 14));
        formPanel.add(labelPort);
        int defaultPort = 50700;
        while (!NetworkValidator.isPortAvailable(defaultPort))
            defaultPort++;
        String port = Integer.toString(defaultPort); // Fallback to default port
        portField = new JTextField(port);
        portField.setFont(new Font("Dialog", Font.PLAIN, 14));
        formPanel.add(portField);

        // Wrap formPanel inside another panel to keep spacing
        JPanel formContainer = new JPanel(new BorderLayout());
        formContainer.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(formContainer, BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel();
        JButton startButton = new JButton("Iniciar");
        startButton.setFont(new Font("Dialog", Font.BOLD, 16)); // Bigger font
        startButton.setPreferredSize(new Dimension(120, 40)); // Bigger button
        buttonPanel.add(startButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        mainPanel.setBackground(GUIColors.backgroundColor);
        formPanel.setBackground(GUIColors.backgroundColor);
        buttonPanel.setBackground(GUIColors.backgroundColor);
        startButton.setBackground(GUIColors.buttonColor);
        nicknameField.setBackground(GUIColors.textFieldColor);
        ipField.setBackground(GUIColors.textFieldColor);
        portField.setBackground(GUIColors.textFieldColor);

        // Button Action
        startButton.addActionListener(e -> {
            String inputNickname = nicknameField.getText();
            String inputIP = ipField.getText();
            String inputPort = portField.getText();

            if (inputNickname.isEmpty() || inputIP.isEmpty() || inputPort.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Todos los campos son obligatorios.", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                if (!NetworkValidator.isValidIp(inputIP)) {
                    JOptionPane.showMessageDialog(null, "IP no válida. Formato esperado: xxx.xxx.xxx.xxx", "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    if (!NetworkValidator.isValidPort(Integer.parseInt(inputPort))) {
                        JOptionPane.showMessageDialog(null, "Puerto no válido. Debe estar entre 1 y 65535.", "Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        if (!NetworkValidator.isPortAvailable(Integer.parseInt(inputPort))) {
                            JOptionPane.showMessageDialog(null, "Puerto no disponible. Por favor, elija otro puerto.", "Error", JOptionPane.ERROR_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(null, "Iniciando sesión como " + inputNickname, "Info", JOptionPane.INFORMATION_MESSAGE);
                            try {
                                ChatController.getInstance().startChatSession(inputNickname, inputIP, Integer.parseInt(inputPort));
                                dispose();
                            } catch (IOException ex) {
                                JOptionPane.showMessageDialog(null, "Error al iniciar sesión: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    }
                }
            }
        });

        // Press enter to send.
        KeyAdapter enterToPressButton = new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyChar() == '\n')
                    startButton.getActionListeners()[0].actionPerformed(null);
            }
        };
        nicknameField.addKeyListener(enterToPressButton);
        ipField.addKeyListener(enterToPressButton);
        portField.addKeyListener(enterToPressButton);

        setLocationRelativeTo(null);
    }
}
