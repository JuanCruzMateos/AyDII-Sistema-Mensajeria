package org.grupouno;

import org.grupouno.config.ConfigService;
import org.grupouno.logging.LoggingConfigLoader;
import org.grupouno.monitor.Monitor;

import java.util.logging.Logger;

public class MonitorApp {
    public static void main(String[] args) {
        LoggingConfigLoader.loadConfig();
        Logger logger = Logger.getLogger(MonitorApp.class.getName());
        logger.info("Starting Monitor Service V" + ConfigService.getConfig("version"));
        new Thread(new Monitor(
                ConfigService.getConfig("monitor.heartbeat.server.host"),
                Integer.parseInt(ConfigService.getConfig("monitor.heartbeat.server.port")),
                ConfigService.getConfig("monitor.address.server.host"),
                Integer.parseInt(ConfigService.getConfig("monitor.address.server.port")),
                Long.valueOf(ConfigService.getConfig("monitor.heartbeat.tolerance"))
        )).start();
    }
}
