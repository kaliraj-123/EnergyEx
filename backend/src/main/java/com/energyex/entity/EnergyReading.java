package com.energyex.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "energy_readings", indexes = {
    @Index(name = "idx_reading_appliance", columnList = "appliance_id"),
    @Index(name = "idx_reading_timestamp", columnList = "timestamp")
})
public class EnergyReading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "appliance_id")
    private Appliance appliance;

    @Column(name = "device_id", length = 64)
    private String deviceId;

    @Column(name = "voltage_v", nullable = false, precision = 8, scale = 2)
    private BigDecimal voltageV;

    @Column(name = "current_a", nullable = false, precision = 8, scale = 3)
    private BigDecimal currentA;

    @Column(name = "active_power_w", nullable = false, precision = 10, scale = 2)
    private BigDecimal activePowerW;

    @Column(name = "energy_kwh", nullable = false, precision = 12, scale = 4)
    private BigDecimal energyKwh;

    @Column(name = "frequency_hz", precision = 6, scale = 2)
    private BigDecimal frequencyHz = BigDecimal.valueOf(50.00);

    @Column(name = "power_factor", precision = 4, scale = 2)
    private BigDecimal powerFactor = BigDecimal.valueOf(0.95);

    @Column(name = "cost_inr", precision = 10, scale = 2)
    private BigDecimal costInr = BigDecimal.ZERO;

    @Column(name = "is_demo_data", nullable = false)
    private Boolean isDemoData = true;

    @Column(name = "reading_source", length = 30)
    private String readingSource = "SIMULATION"; // SIMULATION or HARDWARE_PZEM

    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    public EnergyReading() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Appliance getAppliance() { return appliance; }
    public void setAppliance(Appliance appliance) { this.appliance = appliance; }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public BigDecimal getVoltageV() { return voltageV; }
    public void setVoltageV(BigDecimal voltageV) { this.voltageV = voltageV; }

    public BigDecimal getCurrentA() { return currentA; }
    public void setCurrentA(BigDecimal currentA) { this.currentA = currentA; }

    public BigDecimal getActivePowerW() { return activePowerW; }
    public void setActivePowerW(BigDecimal activePowerW) { this.activePowerW = activePowerW; }

    public BigDecimal getEnergyKwh() { return energyKwh; }
    public void setEnergyKwh(BigDecimal energyKwh) { this.energyKwh = energyKwh; }

    public BigDecimal getFrequencyHz() { return frequencyHz; }
    public void setFrequencyHz(BigDecimal frequencyHz) { this.frequencyHz = frequencyHz; }

    public BigDecimal getPowerFactor() { return powerFactor; }
    public void setPowerFactor(BigDecimal powerFactor) { this.powerFactor = powerFactor; }

    public BigDecimal getCostInr() { return costInr; }
    public void setCostInr(BigDecimal costInr) { this.costInr = costInr; }

    public Boolean getIsDemoData() { return isDemoData; }
    public void setIsDemoData(Boolean isDemoData) { this.isDemoData = isDemoData; }

    public String getReadingSource() { return readingSource; }
    public void setReadingSource(String readingSource) { this.readingSource = readingSource; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}

