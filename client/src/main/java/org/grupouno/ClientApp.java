package org.grupouno;

import org.grupouno.config.ConfigService;
import org.grupouno.logging.LoggingConfigLoader;
import org.grupouno.view.ConfigScreen;

import javax.swing.*;
import java.util.logging.Logger;

public class ClientApp {
    public static void main(String[] args) {
        LoggingConfigLoader.loadConfig();
        Logger logger = Logger.getLogger(ClientApp.class.getName());
        logger.info("Starting Client Server ChatSessionImpl Application V" + ConfigService.getConfig("VERSION"));
        SwingUtilities.invokeLater(() -> new ConfigScreen().setVisible(true));
    }
}