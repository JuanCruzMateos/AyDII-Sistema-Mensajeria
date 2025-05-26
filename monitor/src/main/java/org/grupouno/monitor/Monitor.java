package org.grupouno.monitor;

import org.grupouno.config.ConfigService;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class Monitor implements Runnable, AutoCloseable {
    private static final Logger logger = Logger.getLogger(Monitor.class.getName());
    private final ConcurrentHashMap<SocketAddress, Long> heartbeats;
    private final HeartbeatServer heartbeatServer;
    private final AddressServer addressServer;
    private final Long heartbeatTolerance;
    private final ScheduledExecutorService scheduler;
    private final HashMap<SocketAddress, SocketAddress> serverPortMap;
    private volatile SocketAddress primaryServerAddress;

    // TODO : this can be omitted if the Heartbeat Component on the Server sends the address of the client port on the heartbeat
    {
        this.serverPortMap = new HashMap<>();
        this.serverPortMap.put(new InetSocketAddress(ConfigService.getConfig("server.one.local.host"), Integer.parseInt(ConfigService.getConfig("server.one.heartbeat.port"))), new InetSocketAddress(ConfigService.getConfig("server.one.local.host"), Integer.parseInt(ConfigService.getConfig("server.one.client.port"))));
        this.serverPortMap.put(new InetSocketAddress(ConfigService.getConfig("server.two.local.host"), Integer.parseInt(ConfigService.getConfig("server.two.heartbeat.port"))), new InetSocketAddress(ConfigService.getConfig("server.two.local.host"), Integer.parseInt(ConfigService.getConfig("server.two.client.port"))));
        this.serverPortMap.put(new InetSocketAddress(ConfigService.getConfig("server.three.local.host"), Integer.parseInt(ConfigService.getConfig("server.three.heartbeat.port"))), new InetSocketAddress(ConfigService.getConfig("server.three.local.host"), Integer.parseInt(ConfigService.getConfig("server.three.client.port"))));
    }

    public Monitor(String monitorServerAddrress, int monitorServerPort, String addressServerAddress, int addressServerPort, Long heartbeatTolerance) {
        this.heartbeats = new ConcurrentHashMap<>();
        this.heartbeatServer = new HeartbeatServer(monitorServerAddrress, monitorServerPort, this.heartbeats);
        this.addressServer = new AddressServer(addressServerAddress, addressServerPort);
        this.heartbeatTolerance = heartbeatTolerance;
        this.scheduler = Executors.newScheduledThreadPool(1);
    }

    public synchronized void promoteNewPrimaryServer() {
        SocketAddress newPrimary = null;
        long maxTime = Long.MIN_VALUE;
        for (SocketAddress address : this.heartbeats.keySet()) {
            Long lastHeartbeat = this.heartbeats.get(address);
            if (lastHeartbeat != null && lastHeartbeat > maxTime) {
                maxTime = lastHeartbeat;
                newPrimary = address;
            }
        }
        if (newPrimary != null) {
            this.primaryServerAddress = this.serverPortMap.get(newPrimary);
            logger.info("Promoted " + this.primaryServerAddress + " to primary server.");
            this.addressServer.setPrimaryServerAddress(this.primaryServerAddress);
        } else {
            this.addressServer.setPrimaryServerAddress(null);
            logger.warning("No suitable server found to promote to primary.");
        }
    }

    public synchronized void checkForFailure() {
        if (this.heartbeats.isEmpty()) {
            logger.warning("No heartbeats received. No servers are available. Waiting for heartbeats ...");
        } else {
            Long currentTime = System.currentTimeMillis();
            for (SocketAddress address : this.heartbeats.keySet()) {
                if (currentTime - this.heartbeats.get(address) > this.heartbeatTolerance) {
                    logger.warning("Server " + address + " is not responding.");
                    this.heartbeats.remove(address);
                }
            }
            if (this.primaryServerAddress == null ||
                    !this.heartbeats.containsKey(
                            this.serverPortMap.entrySet().stream()
                                    .filter(entry -> entry.getValue().equals(this.primaryServerAddress))
                                    .map(Map.Entry::getKey)
                                    .findFirst()
                                    .orElse(null)
                    )) {
                logger.info("Promoting a new primary server...");
                this.promoteNewPrimaryServer();
            } else {
                logger.info("Primary server is still alive: " + this.primaryServerAddress);
            }
        }
    }

    @Override
    public void run() {
//        logger.info("Starting monitor server deamon...");
        Thread monitorServerThread = new Thread(this.heartbeatServer);
//        monitorServerThread.setDaemon(true);
        monitorServerThread.start();
//        logger.info("Starting address server deamon...");
        Thread addressServerThread = new Thread(this.addressServer);
//        addressServerThread.setDaemon(true);
        addressServerThread.start();
        logger.info("Starting failure detection...");
        this.scheduler.scheduleAtFixedRate(this::checkForFailure, 1000, heartbeatTolerance, TimeUnit.MILLISECONDS);
    }

    @Override
    public void close() {
        // TODO : Release resources and shutdown the monitor
        logger.info("Shutting down monitor...");
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
