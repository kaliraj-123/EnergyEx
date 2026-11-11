package com.energyex.simulator;

import com.energyex.dto.DashboardSummaryDTO;
import com.energyex.entity.Alert;
import com.energyex.entity.Appliance;
import com.energyex.entity.EnergyReading;
import com.energyex.entity.Tariff;
import com.energyex.repository.AlertRepository;
import com.energyex.repository.ApplianceRepository;
import com.energyex.repository.EnergyReadingRepository;
import com.energyex.service.EnergyCalculationService;
import com.energyex.websocket.TelemetryBroadcaster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
@EnableScheduling
public class IoTSimulatorService {

    private static final Logger log = LoggerFactory.getLogger(IoTSimulatorService.class);

    private final ApplianceRepository applianceRepository;
    private final EnergyReadingRepository energyReadingRepository;
    private final AlertRepository alertRepository;
    private final EnergyCalculationService energyCalculationService;
    private final TelemetryBroadcaster telemetryBroadcaster;

    @Value("${energyex.system-mode:DEMO}")
    private String systemMode;

    @Value("${energyex.simulation.enabled:true}")
    private boolean simulationEnabled;

    private final Random random = new Random();
    private long tickCount = 0;
    private boolean fridgeCompressorActive = true;

    public IoTSimulatorService(ApplianceRepository applianceRepository,
                               EnergyReadingRepository energyReadingRepository,
                               AlertRepository alertRepository,
                               EnergyCalculationService energyCalculationService,
                               TelemetryBroadcaster telemetryBroadcaster) {
        this.applianceRepository = applianceRepository;
        this.energyReadingRepository = energyReadingRepository;
        this.alertRepository = alertRepository;
        this.energyCalculationService = energyCalculationService;
        this.telemetryBroadcaster = telemetryBroadcaster;
    }

    public synchronized boolean isSimulationActive() {
        return "DEMO".equalsIgnoreCase(systemMode) && simulationEnabled;
    }

    public synchronized void setSystemMode(String mode) {
        this.systemMode = mode;
        log.info("EnergyEx System Mode switched to: {}", mode);
    }

    public String getSystemMode() {
        return this.systemMode;
    }

    /**
     * Executes every 3 seconds to simulate realistic IoT electricity readings
     */
    @Scheduled(fixedRateString = "${energyex.simulation.interval-ms:3000}")
    @Transactional
    public void generateSimulatedTelemetry() {
        if (!isSimulationActive()) {
            return;
        }

        tickCount++;
        // Toggle refrigerator compressor every 40 ticks (~2 minutes in demo time)
        if (tickCount % 40 == 0) {
            fridgeCompressorActive = !fridgeCompressorActive;
        }

        List<Appliance> appliances = applianceRepository.findAllByOrderByIdAsc();
        if (appliances.isEmpty()) {
            return;
        }

        Tariff tariff = energyCalculationService.getActiveTariff();
        BigDecimal tariffRate = tariff.getRatePerKwh();

        // Realistic Indian grid voltage ~ 230V (+/- 3V variation)
        double baseVoltage = 230.0 + (random.nextGaussian() * 1.8);
        BigDecimal currentVoltage = BigDecimal.valueOf(Math.max(215.0, Math.min(245.0, baseVoltage)))
                .setScale(2, RoundingMode.HALF_UP);

        // Mains frequency ~ 50.00 Hz (+/- 0.05 Hz)
        double baseFrequency = 50.00 + (random.nextGaussian() * 0.03);
        BigDecimal currentFrequency = BigDecimal.valueOf(Math.max(49.80, Math.min(50.20, baseFrequency)))
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal totalPowerW = BigDecimal.ZERO;
        BigDecimal totalCurrentA = BigDecimal.ZERO;
        double durationSeconds = 3.0; // Interval step

        for (Appliance appliance : appliances) {
            if (Boolean.TRUE.equals(appliance.getIsOn())) {
                double simulatedWatts = calculateRealisticAppliancePower(appliance);
                BigDecimal powerW = BigDecimal.valueOf(simulatedWatts).setScale(2, RoundingMode.HALF_UP);

                // PF depending on type
                double pf = getPowerFactorForType(appliance.getApplianceType());
                double current = simulatedWatts / (currentVoltage.doubleValue() * pf);
                BigDecimal currentA = BigDecimal.valueOf(current).setScale(3, RoundingMode.HALF_UP);

                // Incremental energy (kWh)
                BigDecimal deltaKwh = energyCalculationService.calculateEnergyIncrementKwh(powerW, durationSeconds);
                BigDecimal newCumulativeKwh = appliance.getCumulativeEnergyKwh().add(deltaKwh)
                        .setScale(4, RoundingMode.HALF_UP);
                BigDecimal newCost = energyCalculationService.calculateCost(newCumulativeKwh, tariffRate);

                appliance.setCurrentPowerW(powerW);
                appliance.setVoltageV(currentVoltage);
                appliance.setCurrentA(currentA);
                appliance.setCumulativeEnergyKwh(newCumulativeKwh);
                appliance.setEstimatedCostInr(newCost);
                appliance.setIsSimulated(true);

                totalPowerW = totalPowerW.add(powerW);
                totalCurrentA = totalCurrentA.add(currentA);

                // Create individual channel energy reading record periodically (every 5 ticks)
                if (tickCount % 5 == 0) {
                    EnergyReading reading = new EnergyReading();
                    reading.setAppliance(appliance);
                    reading.setDeviceId("ESP32_SIMULATOR_CH" + appliance.getRelayChannel());
                    reading.setVoltageV(currentVoltage);
                    reading.setCurrentA(currentA);
                    reading.setActivePowerW(powerW);
                    reading.setEnergyKwh(newCumulativeKwh);
                    reading.setFrequencyHz(currentFrequency);
                    reading.setPowerFactor(BigDecimal.valueOf(pf).setScale(2, RoundingMode.HALF_UP));
                    reading.setCostInr(newCost);
                    reading.setIsDemoData(true);
                    reading.setReadingSource("SIMULATION");
                    reading.setTimestamp(LocalDateTime.now());
                    energyReadingRepository.save(reading);
                }
            } else {
                appliance.setCurrentPowerW(BigDecimal.ZERO);
                appliance.setVoltageV(BigDecimal.ZERO);
                appliance.setCurrentA(BigDecimal.ZERO);
            }
            applianceRepository.save(appliance);
        }

        // Aggregate total household energy reading
        BigDecimal overallKwh = appliances.stream()
                .map(Appliance::getCumulativeEnergyKwh)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(4, RoundingMode.HALF_UP);

        BigDecimal overallCost = energyCalculationService.calculateCost(overallKwh, tariffRate);

        // Check for simulated high-power spike anomaly demo occasionally
        if (totalPowerW.doubleValue() > 2200.0) {
            checkAndTriggerAlert("High Electricity Consumption Spike",
                    "Total power load exceeded 2200W (" + totalPowerW + "W). High consumption alert triggered.",
                    "HIGH_CONSUMPTION", "WARNING", null);
        }

        // Build and broadcast live dashboard summary over WebSocket
        DashboardSummaryDTO summary = new DashboardSummaryDTO();
        summary.setSystemMode("DEMO");
        summary.setDataLabel("DEMO DATA (SIMULATION MODE)");
        summary.setTotalEnergyKwh(overallKwh);
        summary.setTodayEnergyKwh(overallKwh.multiply(BigDecimal.valueOf(0.18)).setScale(2, RoundingMode.HALF_UP));
        summary.setCurrentPowerWatts(totalPowerW);
        summary.setCurrentVoltageV(currentVoltage);
        summary.setCurrentCurrentA(totalCurrentA);
        summary.setFrequencyHz(currentFrequency);
        summary.setPowerFactor(BigDecimal.valueOf(0.94));
        summary.setEstimatedBillInr(overallCost);
        summary.setTotalAppliances(appliances.size());
        summary.setActiveAppliances((int) appliances.stream().filter(a -> Boolean.TRUE.equals(a.getIsOn())).count());
        summary.setDeviceStatus("SIMULATED_ACTIVE");
        summary.setLastDataUpdate(LocalDateTime.now());
        summary.setAppliances(appliances);

        telemetryBroadcaster.broadcastDashboard(summary);
    }

    private double calculateRealisticAppliancePower(Appliance appliance) {
        String type = appliance.getApplianceType().toUpperCase();
        double nominal = appliance.getPowerRatingWatts().doubleValue();
        double noise = (random.nextDouble() - 0.5) * 0.05 * nominal;

        return switch (type) {
            case "LIGHT" -> Math.max(5.0, nominal + noise);
            case "FAN" -> Math.max(10.0, nominal + (random.nextGaussian() * 2.0));
            case "TV" -> Math.max(15.0, nominal + ((random.nextDouble() - 0.5) * 18.0)); // Scene brightness variation
            case "REFRIGERATOR" -> fridgeCompressorActive ? (nominal * 0.85 + (random.nextDouble() * 15.0)) : 6.5; // Idle electronics
            case "AC" -> nominal * 0.90 + (random.nextDouble() * 50.0);
            default -> nominal + noise;
        };
    }

    private double getPowerFactorForType(String type) {
        return switch (type.toUpperCase()) {
            case "LIGHT" -> 0.98;
            case "FAN" -> 0.87;
            case "TV" -> 0.93;
            case "REFRIGERATOR" -> fridgeCompressorActive ? 0.82 : 0.95;
            case "AC" -> 0.85;
            default -> 0.92;
        };
    }

    private void checkAndTriggerAlert(String title, String message, String type, String severity, Appliance appliance) {
        if (alertRepository.countByIsReadFalse() < 10) {
            Alert alert = new Alert(title, message, type, severity, appliance, true);
            alertRepository.save(alert);
            telemetryBroadcaster.broadcastAlert(alert);
        }
    }
}

