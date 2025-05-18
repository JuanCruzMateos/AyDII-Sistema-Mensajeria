package org.grupouno;

import org.grupouno.broker.MessageBrokerServerImpl;
import org.grupouno.config.ConfigService;
import org.grupouno.logging.LoggingConfigLoader;

import java.util.logging.Logger;

public class MessageBrokerService {
    public static void main(String[] args) {
        LoggingConfigLoader.loadConfig();
        Logger logger = Logger.getLogger(MessageBrokerService.class.getName());
        logger.info("Starting Message Broker Service V" + ConfigService.getConfig("version"));
        logger.info("Broker Server Address: " + ConfigService.getConfig("broker.server.host") +
                ":" + ConfigService.getConfig("broker.server.port"));
        new Thread(new MessageBrokerServerImpl(
                ConfigService.getConfig("broker.server.host"),
                Integer.parseInt(ConfigService.getConfig("broker.server.port"))
        )).start();
    }
}
