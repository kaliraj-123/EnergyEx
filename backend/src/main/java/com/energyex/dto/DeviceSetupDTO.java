package com.energyex.dto;

import java.time.LocalDateTime;

public class DeviceSetupDTO {
    private String deviceId;
    private String deviceName;
    private String ipAddress;
    private String authToken;
    private String status; // CONNECTED, DISCONNECTED, CONNECTING
    private Boolean isLiveMode;
    private LocalDateTime lastSeenAt;
    private String wifiSsid;

    public DeviceSetupDTO() {}

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getAuthToken() { return authToken; }
    public void setAuthToken(String authToken) { this.authToken = authToken; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Boolean getIsLiveMode() { return isLiveMode; }
    public void setIsLiveMode(Boolean isLiveMode) { this.isLiveMode = isLiveMode; }

    public LocalDateTime getLastSeenAt() { return lastSeenAt; }
    public void setLastSeenAt(LocalDateTime lastSeenAt) { this.lastSeenAt = lastSeenAt; }

    public String getWifiSsid() { return wifiSsid; }
    public void setWifiSsid(String wifiSsid) { this.wifiSsid = wifiSsid; }
}

