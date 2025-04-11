package org.grupouno;

import org.grupouno.view.ConfigScreen;

import javax.swing.*;
import java.util.logging.Logger;

public class App {
    private static final Logger logger = Logger.getLogger(App.class.getName());
    private static final String VERSION = "1.0.0";

    static {
        System.setProperty("java.util.logging.SimpleFormatter.format",
                "[%1$tF %1$tT] [%4$s] [%2$s] %5$s%6$s%n");
    }

    public static void main(String[] args) {
        logger.info("Starting P2P ChatSession Application V" + VERSION);
        SwingUtilities.invokeLater(() -> new ConfigScreen().setVisible(true));
    }
}