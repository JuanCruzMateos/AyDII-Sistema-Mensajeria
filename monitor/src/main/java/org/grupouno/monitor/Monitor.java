package org.grupouno.monitor;

import java.net.SocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class Monitor implements Runnable, AutoCloseable {
    private final Logger logger = Logger.getLogger(Monitor.class.getName());
    private final ConcurrentHashMap<SocketAddress, Long> heartbeats;
    private final HeartbeatServer heartbeatServer;
    private final AddressServer addressServer;
    private final Long heartbeatTolerance;
    private final ScheduledExecutorService scheduler;
    private SocketAddress primaryServerAddress;

    public Monitor(String monitorServerAddrress, int monitorServerPort, String addressServerAddress, int addressServerPort, Long heartbeatTolerance) {
        this.heartbeats = new ConcurrentHashMap<>();
        this.heartbeatServer = new HeartbeatServer(monitorServerAddrress, monitorServerPort, this.heartbeats);
        this.addressServer = new AddressServer(addressServerAddress, addressServerPort, this.primaryServerAddress);
        this.heartbeatTolerance = heartbeatTolerance;
        this.scheduler = Executors.newScheduledThreadPool(1);
    }

    private synchronized void promoteToPrimaryServer() {
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
            this.primaryServerAddress = newPrimary;
            logger.info("Promoted " + newPrimary + " to primary server.");
        } else {
            this.primaryServerAddress = null;
            logger.warning("No suitable server found to promote to primary.");
        }
    }

    public synchronized void checkForFailure() {
        if (this.heartbeats.isEmpty()) {
            logger.warning("No heartbeats received. No servers are available. Waiting for heartbeats...");
        } else {
            Long currentTime = System.currentTimeMillis();
            for (SocketAddress address : this.heartbeats.keySet()) {
                if (currentTime - this.heartbeats.get(address) > this.heartbeatTolerance) {
                    logger.warning("Server " + address + " is not responding.");
                    this.heartbeats.remove(address);
                }
            }
            if (this.primaryServerAddress == null || !this.heartbeats.containsKey(this.primaryServerAddress)) {
                logger.info("Promoting a new primary server...");
                promoteToPrimaryServer();
            } else {
                logger.info("Primary server is still alive: " + this.primaryServerAddress);
            }
        }
    }

    @Override
    public void run() {
        logger.info("Starting monitor server deamon...");
        Thread monitorServerThread = new Thread(this.heartbeatServer);
//        monitorServerThread.setDaemon(true);
        monitorServerThread.start();
        logger.info("Starting address server deamon...");
        Thread addressServerThread = new Thread(this.addressServer);
//        addressServerThread.setDaemon(true);
        addressServerThread.start();
        logger.info("Starting failure detection...");
        this.scheduler.scheduleAtFixedRate(this::checkForFailure, 0, heartbeatTolerance, TimeUnit.MILLISECONDS);
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
