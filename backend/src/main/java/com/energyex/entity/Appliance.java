package com.energyex.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "appliances")
public class Appliance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "appliance_type", nullable = false, length = 50)
    private String applianceType; // LIGHT, FAN, TV, REFRIGERATOR, AC, HEATER, OTHER

    @Column(name = "relay_channel", nullable = false)
    private Integer relayChannel = 1; // 1 to 4 on 4-channel relay module

    @Column(name = "sensor_channel")
    private Integer sensorChannel = 1; // Independent measurement channel / CT index

    @Column(name = "power_rating_watts", nullable = false, precision = 10, scale = 2)
    private BigDecimal powerRatingWatts = BigDecimal.valueOf(100.0);

    @Column(name = "is_on", nullable = false)
    private Boolean isOn = false;

    @Column(name = "current_power_w", precision = 10, scale = 2)
    private BigDecimal currentPowerW = BigDecimal.ZERO;

    @Column(name = "voltage_v", precision = 10, scale = 2)
    private BigDecimal voltageV = BigDecimal.ZERO;

    @Column(name = "current_a", precision = 10, scale = 3)
    private BigDecimal currentA = BigDecimal.ZERO;

    @Column(name = "cumulative_energy_kwh", precision = 12, scale = 4)
    private BigDecimal cumulativeEnergyKwh = BigDecimal.ZERO;

    @Column(name = "estimated_cost_inr", precision = 12, scale = 2)
    private BigDecimal estimatedCostInr = BigDecimal.ZERO;

    @Column(name = "connection_status", length = 20)
    private String connectionStatus = "ONLINE"; // ONLINE, OFFLINE

    @Column(name = "is_simulated")
    private Boolean isSimulated = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Appliance() {}

    public Appliance(String name, String applianceType, Integer relayChannel, BigDecimal powerRatingWatts) {
        this.name = name;
        this.applianceType = applianceType;
        this.relayChannel = relayChannel;
        this.sensorChannel = relayChannel;
        this.powerRatingWatts = powerRatingWatts;
    }

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

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

