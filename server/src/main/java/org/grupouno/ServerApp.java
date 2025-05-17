package org.grupouno;

import org.grupouno.config.ConfigService;
import org.grupouno.logging.LoggingConfigLoader;
import org.grupouno.model.conversation.ConversationService;
import org.grupouno.model.directory.Directory;
import org.grupouno.network.hearthbeat.Heartbeat;
import org.grupouno.network.server.ChatServerImpl;

import java.util.HashMap;
import java.util.Objects;
import java.util.logging.Logger;


public class ServerApp {

    public static void main(String[] args) {
        LoggingConfigLoader.loadConfig();
        Logger logger = Logger.getLogger(ServerApp.class.getName());

        if (args.length == 1) {
            String serverNumber = args[0];

            String serverAddress = ConfigService.getConfig("server." + serverNumber + ".local.host");
            int serverClientPort = Integer.parseInt(ConfigService.getConfig("server." + serverNumber + ".client.port"));
            int serverHeartbeatPort = Integer.parseInt(Objects.requireNonNull(ConfigService.getConfig("server." + serverNumber + ".heartbeat.port")));
            int serverBrokerPort = Integer.parseInt(Objects.requireNonNull(ConfigService.getConfig("server." + serverNumber + ".broker.port")));
            String heartbeatServerAddress = ConfigService.getConfig("monitor.heartbeat.server.host");
            int heartbeatServerPort = Integer.parseInt(Objects.requireNonNull(ConfigService.getConfig("monitor.heartbeat.server.port")));
            long serverHeartbeatRate = Long.parseLong(Objects.requireNonNull(ConfigService.getConfig("server.heartbeat.rate")));

            logger.info("Starting ChatServerImpl Application V" + ConfigService.getConfig("version"));
            logger.info("Server Number: " + serverNumber);
            logger.info("Server Address: " + serverAddress);
            logger.info("Server Client Port: " + serverClientPort);
            logger.info("Server Broker Port: " + serverBrokerPort);
            logger.info("Server Heartbeat Port: " + serverHeartbeatPort);
            logger.info("Heartbeat Server Address: " + heartbeatServerAddress);
            logger.info("Heartbeat Server Port: " + heartbeatServerPort);
            logger.info("Server Heartbeat Rate: " + serverHeartbeatRate + "ms");

            ChatServerImpl chatServerImpl = new ChatServerImpl(
                    serverAddress,
                    serverClientPort,
                    new Directory(),
                    new ConversationService(),
                    new HashMap<>(), // Todo
                    new Heartbeat(
                            heartbeatServerAddress,
                            heartbeatServerPort,
                            serverAddress,
                            serverHeartbeatPort,
                            serverHeartbeatRate
                    ));
            chatServerImpl.startServer();
        } else {
            logger.warning("Invalid arguments. Usage: java -jar server.jar <server_number>");
            System.exit(1);
        }
    }
}
