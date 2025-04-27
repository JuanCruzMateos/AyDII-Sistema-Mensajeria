package org.grupouno;

import org.grupouno.config.ConfigService;
import org.grupouno.logging.LoggingConfigLoader;
import org.grupouno.model.conversation.ConversationService;
import org.grupouno.model.directory.Directory;
import org.grupouno.network.server.ChatServerImpl;
import org.grupouno.validation.NetworkValidator;

import java.util.HashMap;
import java.util.Objects;
import java.util.logging.Logger;


public class ServerApp {

    public static void main(String[] args) {
        LoggingConfigLoader.loadConfig();
        Logger logger = Logger.getLogger(ServerApp.class.getName());
        logger.info("Starting ChatServerImpl Application V" + ConfigService.getConfig("VERSION"));
        int serverPort = Integer.parseInt(Objects.requireNonNull(ConfigService.getConfig("SERVER_PORT")));
        if (NetworkValidator.isValidPort(serverPort) && NetworkValidator.isPortAvailable(serverPort)) {
            ChatServerImpl chatServerImpl = new ChatServerImpl(serverPort, new Directory(), new ConversationService(), new HashMap<>());
            chatServerImpl.startServer();
        } else {
            logger.severe("Port " + serverPort + " is invalid or already in use. Please choose another port.");
            System.exit(1);
        }
    }
}
