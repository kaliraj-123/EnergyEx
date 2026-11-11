package com.energyex.dto;

import java.math.BigDecimal;

public class ApplianceDTO {
    private Long id;
    private String name;
    private String applianceType;
    private Integer relayChannel;
    private Integer sensorChannel;
    private BigDecimal powerRatingWatts;
    private Boolean isOn;
    private BigDecimal currentPowerW;
    private BigDecimal voltageV;
    private BigDecimal currentA;
    private BigDecimal cumulativeEnergyKwh;
    private BigDecimal estimatedCostInr;
    private String connectionStatus;
    private Boolean isSimulated;

    public ApplianceDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getApplianceType() { return applianceType; }
    public void setApplianceType(String applianceType) { this.applianceType = applianceType; }

    public Integer getRelayChannel() { return relayChannel; }
    public void setRelayChannel(Integer relayChannel) { this.relayChannel = relayChannel; }

    public Integer getSensorChannel() { return sensorChannel; }
    public void setSensorChannel(Integer sensorChannel) { this.sensorChannel = sensorChannel; }

    public BigDecimal getPowerRatingWatts() { return powerRatingWatts; }
    public void setPowerRatingWatts(BigDecimal powerRatingWatts) { this.powerRatingWatts = powerRatingWatts; }

    public Boolean getIsOn() { return isOn; }
    public void setIsOn(Boolean isOn) { this.isOn = isOn; }

    public BigDecimal getCurrentPowerW() { return currentPowerW; }
    public void setCurrentPowerW(BigDecimal currentPowerW) { this.currentPowerW = currentPowerW; }

    public BigDecimal getVoltageV() { return voltageV; }
    public void setVoltageV(BigDecimal voltageV) { this.voltageV = voltageV; }

    public BigDecimal getCurrentA() { return currentA; }
    public void setCurrentA(BigDecimal currentA) { this.currentA = currentA; }

    public BigDecimal getCumulativeEnergyKwh() { return cumulativeEnergyKwh; }
    public void setCumulativeEnergyKwh(BigDecimal cumulativeEnergyKwh) { this.cumulativeEnergyKwh = cumulativeEnergyKwh; }

    public BigDecimal getEstimatedCostInr() { return estimatedCostInr; }
    public void setEstimatedCostInr(BigDecimal estimatedCostInr) { this.estimatedCostInr = estimatedCostInr; }

    public String getConnectionStatus() { return connectionStatus; }
    public void setConnectionStatus(String connectionStatus) { this.connectionStatus = connectionStatus; }

    public Boolean getIsSimulated() { return isSimulated; }
    public void setIsSimulated(Boolean isSimulated) { this.isSimulated = isSimulated; }
}

