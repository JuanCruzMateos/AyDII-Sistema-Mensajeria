package org.grupouno.model;

public class NodeInfo {
    private String ipAddress;
    private long lastHeartbeat;
    private boolean isPrimary;

    // Constructor
    public NodeInfo(String ipAddress, long lastHeartbeat, boolean isPrimary) {
        this.ipAddress = ipAddress;
        this.lastHeartbeat = lastHeartbeat;
        this.isPrimary = isPrimary;
    }

    public NodeInfo(String ipAddress) {
        this.ipAddress = ipAddress;
        this.lastHeartbeat = 0;
        this.isPrimary = false;
    }

    // Getters and setters
    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public long getLastHeartbeat() {
        return lastHeartbeat;
    }

    public void setLastHeartbeat(long lastHeartbeat) {
        this.lastHeartbeat = lastHeartbeat;
    }

    public boolean isPrimary() {
        return isPrimary;
    }

    public void setPrimary(boolean primary) {
        isPrimary = primary;
    }

    @Override
    public String toString() {
        return "NodeInfo{" +
                "ipAddress='" + ipAddress + '\'' +
                ", lastHeartbeat=" + lastHeartbeat +
                ", isPrimary=" + isPrimary +
                '}';
    }
}
