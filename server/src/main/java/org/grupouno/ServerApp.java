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
        logger.info("Starting ChatServerImpl Application V" + ConfigService.getConfig("version"));
        ChatServerImpl chatServerImpl = new ChatServerImpl(
                ConfigService.getConfig("server.one.local.host"),
                Integer.parseInt(ConfigService.getConfig("server.one.client.port")),
                new Directory(),
                new ConversationService(),
                new HashMap<>(),
                new Heartbeat(
                        ConfigService.getConfig("monitor.heartbeat.server.host"),
                        Integer.parseInt(Objects.requireNonNull(ConfigService.getConfig("monitor.heartbeat.server.port"))),
                        ConfigService.getConfig("server.one.local.host"),
                        Integer.parseInt(ConfigService.getConfig("server.one.heartbeat.port")),
                        Long.parseLong(ConfigService.getConfig("monitor.heartbeat.interval"))
                ));
        chatServerImpl.startServer();
    }
}
