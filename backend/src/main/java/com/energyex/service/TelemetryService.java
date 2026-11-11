package com.energyex.service;

import com.energyex.dto.DashboardSummaryDTO;
import com.energyex.dto.TelemetryRequestDTO;
import com.energyex.entity.Alert;
import com.energyex.entity.Appliance;
import com.energyex.entity.Device;
import com.energyex.entity.EnergyReading;
import com.energyex.entity.Tariff;
import com.energyex.repository.AlertRepository;
import com.energyex.repository.ApplianceRepository;
import com.energyex.repository.DeviceRepository;
import com.energyex.repository.EnergyReadingRepository;
import com.energyex.websocket.TelemetryBroadcaster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TelemetryService {

    private static final Logger log = LoggerFactory.getLogger(TelemetryService.class);

    private final EnergyReadingRepository readingRepository;
    private final DeviceRepository deviceRepository;
    private final ApplianceRepository applianceRepository;
    private final AlertRepository alertRepository;
    private final EnergyCalculationService calculationService;
    private final TelemetryBroadcaster broadcaster;

    @Value("${energyex.iot.device-auth-token:ENERGYEX_IOT_SECURE_TOKEN_2026}")
    private String expectedAuthToken;

    @Value("${energyex.iot.allowed-voltage-min:180.0}")
    private double minVoltage;

    @Value("${energyex.iot.allowed-voltage-max:260.0}")
    private double maxVoltage;

    public TelemetryService(EnergyReadingRepository readingRepository,
                            DeviceRepository deviceRepository,
                            ApplianceRepository applianceRepository,
                            AlertRepository alertRepository,
                            EnergyCalculationService calculationService,
                            TelemetryBroadcaster broadcaster) {
        this.readingRepository = readingRepository;
        this.deviceRepository = deviceRepository;
        this.applianceRepository = applianceRepository;
        this.alertRepository = alertRepository;
        this.calculationService = calculationService;
        this.broadcaster = broadcaster;
    }

    @Transactional
    public boolean ingestHardwareTelemetry(TelemetryRequestDTO dto) {
        // 1. Authenticate ESP32
        if (dto.getAuthToken() == null || !expectedAuthToken.equals(dto.getAuthToken())) {
            log.warn("Unauthorized telemetry ingest attempt from device: {}", dto.getDeviceId());
            throw new SecurityException("Invalid device authentication token");
        }

        // 2. Validate sensor data ranges
        double voltage = dto.getVoltageV() != null ? dto.getVoltageV().doubleValue() : 0.0;
        if (voltage < minVoltage || voltage > maxVoltage) {
            log.warn("Voltage reading out of normal safety range: {}V", voltage);
            Alert voltageAlert = new Alert(
                    "Mains Voltage Warning",
                    "Measured grid voltage of " + voltage + "V is outside normal bounds (" + minVoltage + "V - " + maxVoltage + "V).",
                    "VOLTAGE_FLUCTUATION", "WARNING", null, false
            );
            alertRepository.save(voltageAlert);
            broadcaster.broadcastAlert(voltageAlert);
        }

        // 3. Update device status
        Device device = deviceRepository.findByDeviceId(dto.getDeviceId())
                .orElseGet(() -> {
                    Device d = new Device();
                    d.setDeviceId(dto.getDeviceId());
                    d.setDeviceName("ESP32 Energy Monitor");
                    d.setAuthToken(dto.getAuthToken());
                    return d;
                });
        device.setStatus("CONNECTED");
        device.setIsLiveMode(true);
        device.setLastSeenAt(LocalDateTime.now());
        deviceRepository.save(device);

        // 4. Save hardware energy reading (marked clearly as REAL HARDWARE)
        Tariff tariff = calculationService.getActiveTariff();
        BigDecimal cost = calculationService.calculateCost(dto.getEnergyKwh(), tariff.getRatePerKwh());

        EnergyReading reading = new EnergyReading();
        reading.setDeviceId(dto.getDeviceId());
        reading.setVoltageV(dto.getVoltageV());
        reading.setCurrentA(dto.getCurrentA());
        reading.setActivePowerW(dto.getPowerWatts());
        reading.setEnergyKwh(dto.getEnergyKwh());
        reading.setFrequencyHz(dto.getFrequencyHz() != null ? dto.getFrequencyHz() : BigDecimal.valueOf(50.00));
        reading.setPowerFactor(dto.getPowerFactor() != null ? dto.getPowerFactor() : BigDecimal.valueOf(0.95));
        reading.setCostInr(cost);
        reading.setIsDemoData(false);
        reading.setReadingSource("HARDWARE_PZEM");
        reading.setTimestamp(LocalDateTime.now());
        readingRepository.save(reading);

        // 5. Update per-channel appliances if provided
        if (dto.getChannelReadings() != null) {
            for (TelemetryRequestDTO.ChannelReadingDTO ch : dto.getChannelReadings()) {
                applianceRepository.findByRelayChannel(ch.getChannelNumber()).ifPresent(appliance -> {
                    if (ch.getIsOn() != null) appliance.setIsOn(ch.getIsOn());
                    if (ch.getPowerWatts() != null) appliance.setCurrentPowerW(ch.getPowerWatts());
                    if (ch.getCurrentA() != null) appliance.setCurrentA(ch.getCurrentA());
                    appliance.setVoltageV(dto.getVoltageV());
                    appliance.setIsSimulated(false);
                    applianceRepository.save(appliance);
                });
            }
        }

        // 6. Broadcast updated Live Dashboard over WebSocket
        List<Appliance> appliances = applianceRepository.findAllByOrderByIdAsc();
        DashboardSummaryDTO summary = new DashboardSummaryDTO();
        summary.setSystemMode("LIVE_IOT");
        summary.setDataLabel("LIVE IOT SENSOR DATA (PZEM-004T)");
        summary.setTotalEnergyKwh(dto.getEnergyKwh());
        summary.setTodayEnergyKwh(dto.getEnergyKwh().multiply(BigDecimal.valueOf(0.20)).setScale(2, RoundingMode.HALF_UP));
        summary.setCurrentPowerWatts(dto.getPowerWatts());
        summary.setCurrentVoltageV(dto.getVoltageV());
        summary.setCurrentCurrentA(dto.getCurrentA());
        summary.setFrequencyHz(dto.getFrequencyHz());
        summary.setPowerFactor(dto.getPowerFactor());
        summary.setEstimatedBillInr(cost);
        summary.setTotalAppliances(appliances.size());
        summary.setActiveAppliances((int) appliances.stream().filter(a -> Boolean.TRUE.equals(a.getIsOn())).count());
        summary.setDeviceStatus("CONNECTED");
        summary.setLastDataUpdate(LocalDateTime.now());
        summary.setAppliances(appliances);

        broadcaster.broadcastDashboard(summary);
        return true;
    }
}

