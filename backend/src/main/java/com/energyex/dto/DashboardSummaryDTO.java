package com.energyex.dto;

import com.energyex.entity.Alert;
import com.energyex.entity.Appliance;
import com.energyex.entity.Prediction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class DashboardSummaryDTO {
    private String systemMode; // DEMO or LIVE_IOT
    private String dataLabel;  // "DEMO DATA (SIMULATION)" or "LIVE IOT SENSOR DATA"
    private BigDecimal totalEnergyKwh;
    private BigDecimal todayEnergyKwh;
    private BigDecimal currentPowerWatts;
    private BigDecimal currentVoltageV;
    private BigDecimal currentCurrentA;
    private BigDecimal frequencyHz;
    private BigDecimal powerFactor;
    private BigDecimal estimatedBillInr;
    private Integer totalAppliances;
    private Integer activeAppliances;
    private String deviceStatus; // CONNECTED, DISCONNECTED, SIMULATED
    private LocalDateTime lastDataUpdate;
    private List<Appliance> appliances;
    private List<Alert> recentAlerts;
    private List<Prediction> upcomingPredictions;

    public DashboardSummaryDTO() {}

    public String getSystemMode() { return systemMode; }
    public void setSystemMode(String systemMode) { this.systemMode = systemMode; }

    public String getDataLabel() { return dataLabel; }
    public void setDataLabel(String dataLabel) { this.dataLabel = dataLabel; }

    public BigDecimal getTotalEnergyKwh() { return totalEnergyKwh; }
    public void setTotalEnergyKwh(BigDecimal totalEnergyKwh) { this.totalEnergyKwh = totalEnergyKwh; }

    public BigDecimal getTodayEnergyKwh() { return todayEnergyKwh; }
    public void setTodayEnergyKwh(BigDecimal todayEnergyKwh) { this.todayEnergyKwh = todayEnergyKwh; }

    public BigDecimal getCurrentPowerWatts() { return currentPowerWatts; }
    public void setCurrentPowerWatts(BigDecimal currentPowerWatts) { this.currentPowerWatts = currentPowerWatts; }

    public BigDecimal getCurrentVoltageV() { return currentVoltageV; }
    public void setCurrentVoltageV(BigDecimal currentVoltageV) { this.currentVoltageV = currentVoltageV; }

    public BigDecimal getCurrentCurrentA() { return currentCurrentA; }
    public void setCurrentCurrentA(BigDecimal currentCurrentA) { this.currentCurrentA = currentCurrentA; }

    public BigDecimal getFrequencyHz() { return frequencyHz; }
    public void setFrequencyHz(BigDecimal frequencyHz) { this.frequencyHz = frequencyHz; }

    public BigDecimal getPowerFactor() { return powerFactor; }
    public void setPowerFactor(BigDecimal powerFactor) { this.powerFactor = powerFactor; }

    public BigDecimal getEstimatedBillInr() { return estimatedBillInr; }
    public void setEstimatedBillInr(BigDecimal estimatedBillInr) { this.estimatedBillInr = estimatedBillInr; }

    public Integer getTotalAppliances() { return totalAppliances; }
    public void setTotalAppliances(Integer totalAppliances) { this.totalAppliances = totalAppliances; }

    public Integer getActiveAppliances() { return activeAppliances; }
    public void setActiveAppliances(Integer activeAppliances) { this.activeAppliances = activeAppliances; }

    public String getDeviceStatus() { return deviceStatus; }
    public void setDeviceStatus(String deviceStatus) { this.deviceStatus = deviceStatus; }

    public LocalDateTime getLastDataUpdate() { return lastDataUpdate; }
    public void setLastDataUpdate(LocalDateTime lastDataUpdate) { this.lastDataUpdate = lastDataUpdate; }

    public List<Appliance> getAppliances() { return appliances; }
    public void setAppliances(List<Appliance> appliances) { this.appliances = appliances; }

    public List<Alert> getRecentAlerts() { return recentAlerts; }
    public void setRecentAlerts(List<Alert> recentAlerts) { this.recentAlerts = recentAlerts; }

    public List<Prediction> getUpcomingPredictions() { return upcomingPredictions; }
    public void setUpcomingPredictions(List<Prediction> upcomingPredictions) { this.upcomingPredictions = upcomingPredictions; }
}

