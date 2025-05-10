package org.grupouno;

import org.grupouno.model.NodeInfo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class Monitor {
    private static final int TIMEOUT = 5000; // 5 segundos
    private final Logger logger = Logger.getLogger(Monitor.class.getName());
    private final ConcurrentHashMap<String, NodeInfo> nodeInfoMap = new ConcurrentHashMap<>();
    private String currentPrimary = null;

    public static void main(String[] args) throws Exception {
        Monitor monitor = new Monitor();
        HeartBeatListener listener = new HeartBeatListener(monitor, 9999);
        listener.start();
        new PrimaryQueryResponder(monitor).start();

        while (true) {
            monitor.checkServers();
            Thread.sleep(3000);
        }
    }

    public synchronized void registerHeartbeat(String nodeId, String ipAddress) {
        NodeInfo info = nodeInfoMap.get(nodeId);
        if (info == null) {
            info = new NodeInfo(ipAddress, System.currentTimeMillis(), false);
            nodeInfoMap.put(nodeId, info);
        } else {
            info.setLastHeartbeat(System.currentTimeMillis());
        }

        // Si no hay primario, el primero que se registre lo es
        if (currentPrimary == null) {
            promoteToPrimary(nodeId);
        }
    }

    public synchronized void checkServers() {
        long now = System.currentTimeMillis();
        for (Map.Entry<String, NodeInfo> entry : nodeInfoMap.entrySet()) {
            String nodeId = entry.getKey();
            NodeInfo info = entry.getValue();
            long diff = now - info.getLastHeartbeat();

            if (diff > TIMEOUT) {
                logger.warning("INACTIVO: " + nodeId + (info.isPrimary() ? " (PRIMARIO)" : ""));
                // Si el nodo caído era el primario, promover a otro
                if (info.isPrimary()) {
                    promoteBackup();
                }
            } else {
                logger.info("Activo: " + nodeId + (info.isPrimary() ? " (PRIMARIO)" : " (Backup)"));
            }
        }
    }

    private synchronized void promoteToPrimary(String nodeId) {
        NodeInfo info = nodeInfoMap.get(nodeId);
        if (info != null) {
            info.setPrimary(true);
            currentPrimary = nodeId;
            logger.info("Nuevo PRIMARIO asignado: " + nodeId);
        }
    }

    private synchronized void promoteBackup() {
        for (Map.Entry<String, NodeInfo> entry : nodeInfoMap.entrySet()) {
            String nodeId = entry.getKey();
            NodeInfo info = entry.getValue();

            long diff = System.currentTimeMillis() - info.getLastHeartbeat();
            if (!info.isPrimary() && diff <= TIMEOUT) {
                promoteToPrimary(nodeId);
                return;
            }
        }
        logger.warning("No se encontró nodo de backup disponible para promover.");
        currentPrimary = null;
    }

    public synchronized String getPrimaryIp() {
        if (currentPrimary != null && nodeInfoMap.containsKey(currentPrimary)) {
            return nodeInfoMap.get(currentPrimary).getIpAddress();
        }
        return "NONE";
    }
}
