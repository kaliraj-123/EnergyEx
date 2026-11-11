package com.energyex.dto;

import java.math.BigDecimal;
import java.util.List;

public class TelemetryRequestDTO {

    private String deviceId;
    private String authToken;
    private BigDecimal voltageV;
    private BigDecimal currentA;
    private BigDecimal powerWatts;
    private BigDecimal energyKwh;
    private BigDecimal frequencyHz;
    private BigDecimal powerFactor;
    private Boolean isSimulated = false;
    private List<ChannelReadingDTO> channelReadings;

    public TelemetryRequestDTO() {}

    public static class ChannelReadingDTO {
        private Integer channelNumber; // 1 to 4
        private BigDecimal currentA;
        private BigDecimal powerWatts;
        private Boolean isOn;

        public ChannelReadingDTO() {}

        public Integer getChannelNumber() { return channelNumber; }
        public void setChannelNumber(Integer channelNumber) { this.channelNumber = channelNumber; }

        public BigDecimal getCurrentA() { return currentA; }
        public void setCurrentA(BigDecimal currentA) { this.currentA = currentA; }

        public BigDecimal getPowerWatts() { return powerWatts; }
        public void setPowerWatts(BigDecimal powerWatts) { this.powerWatts = powerWatts; }

        public Boolean getIsOn() { return isOn; }
        public void setIsOn(Boolean isOn) { this.isOn = isOn; }
    }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public String getAuthToken() { return authToken; }
    public void setAuthToken(String authToken) { this.authToken = authToken; }

    public BigDecimal getVoltageV() { return voltageV; }
    public void setVoltageV(BigDecimal voltageV) { this.voltageV = voltageV; }

    public BigDecimal getCurrentA() { return currentA; }
    public void setCurrentA(BigDecimal currentA) { this.currentA = currentA; }

    public BigDecimal getPowerWatts() { return powerWatts; }
    public void setPowerWatts(BigDecimal powerWatts) { this.powerWatts = powerWatts; }

    public BigDecimal getEnergyKwh() { return energyKwh; }
    public void setEnergyKwh(BigDecimal energyKwh) { this.energyKwh = energyKwh; }

    public BigDecimal getFrequencyHz() { return frequencyHz; }
    public void setFrequencyHz(BigDecimal frequencyHz) { this.frequencyHz = frequencyHz; }

    public BigDecimal getPowerFactor() { return powerFactor; }
    public void setPowerFactor(BigDecimal powerFactor) { this.powerFactor = powerFactor; }

    public Boolean getIsSimulated() { return isSimulated; }
    public void setIsSimulated(Boolean isSimulated) { this.isSimulated = isSimulated; }

    public List<ChannelReadingDTO> getChannelReadings() { return channelReadings; }
    public void setChannelReadings(List<ChannelReadingDTO> channelReadings) { this.channelReadings = channelReadings; }
}

