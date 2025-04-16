package org.grupouno;

import org.grupouno.network.ChatServerImpl;
import org.grupouno.validation.ConnectionValidator;

import java.util.logging.Logger;


public class App {
    private static final Logger logger = Logger.getLogger(App.class.getName());
    private static final String VERSION = "2.0.0";
    private static final int SERVER_PORT = 50479;

    static {
        System.setProperty("java.util.logging.SimpleFormatter.format",
                "[%1$tF %1$tT] [%4$s] [%2$s] %5$s%6$s%n");
    }

    public static void main(String[] args) {
        logger.info("Starting ChatServerImpl Application V" + VERSION);

        if (ConnectionValidator.isValidPort(SERVER_PORT) && ConnectionValidator.isPortAvailable(SERVER_PORT)) {
            ChatServerImpl chatServerImpl = new ChatServerImpl(SERVER_PORT);
            chatServerImpl.startServer();
        } else {
            logger.severe("Port " + SERVER_PORT + " is invalid or already in use. Please choose another port.");
            System.exit(1);
        }
    }
}
