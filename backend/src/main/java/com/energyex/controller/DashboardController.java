package com.energyex.controller;

import com.energyex.dto.DashboardSummaryDTO;
import com.energyex.entity.Appliance;
import com.energyex.entity.EnergyReading;
import com.energyex.entity.Tariff;
import com.energyex.repository.AlertRepository;
import com.energyex.repository.ApplianceRepository;
import com.energyex.repository.EnergyReadingRepository;
import com.energyex.service.EnergyCalculationService;
import com.energyex.simulator.IoTSimulatorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    private final ApplianceRepository applianceRepository;
    private final EnergyReadingRepository readingRepository;
    private final AlertRepository alertRepository;
    private final EnergyCalculationService calculationService;
    private final IoTSimulatorService simulatorService;

    public DashboardController(ApplianceRepository applianceRepository,
                               EnergyReadingRepository readingRepository,
                               AlertRepository alertRepository,
                               EnergyCalculationService calculationService,
                               IoTSimulatorService simulatorService) {
        this.applianceRepository = applianceRepository;
        this.readingRepository = readingRepository;
        this.alertRepository = alertRepository;
        this.calculationService = calculationService;
        this.simulatorService = simulatorService;
    }

    @GetMapping
    public ResponseEntity<DashboardSummaryDTO> getDashboardSummary() {
        List<Appliance> appliances = applianceRepository.findAllByOrderByIdAsc();
        Tariff tariff = calculationService.getActiveTariff();

        BigDecimal totalKwh = appliances.stream()
                .map(Appliance::getCumulativeEnergyKwh)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(4, RoundingMode.HALF_UP);

        BigDecimal currentWatts = appliances.stream()
                .filter(a -> Boolean.TRUE.equals(a.getIsOn()))
                .map(Appliance::getCurrentPowerW)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal totalCurrentA = appliances.stream()
                .filter(a -> Boolean.TRUE.equals(a.getIsOn()))
                .map(Appliance::getCurrentA)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(3, RoundingMode.HALF_UP);

        BigDecimal estimatedCost = calculationService.calculateCost(totalKwh, tariff.getRatePerKwh());

        boolean isSimulated = simulatorService.isSimulationActive();

        DashboardSummaryDTO summary = new DashboardSummaryDTO();
        summary.setSystemMode(isSimulated ? "DEMO" : "LIVE_IOT");
        summary.setDataLabel(isSimulated ? "DEMO DATA (SIMULATION MODE)" : "LIVE IOT SENSOR DATA (PZEM-004T)");
        summary.setTotalEnergyKwh(totalKwh);
        summary.setTodayEnergyKwh(totalKwh.multiply(BigDecimal.valueOf(0.18)).setScale(2, RoundingMode.HALF_UP));
        summary.setCurrentPowerWatts(currentWatts);
        summary.setCurrentVoltageV(BigDecimal.valueOf(230.20));
        summary.setCurrentCurrentA(totalCurrentA);
        summary.setFrequencyHz(BigDecimal.valueOf(50.00));
        summary.setPowerFactor(BigDecimal.valueOf(0.94));
        summary.setEstimatedBillInr(estimatedCost);
        summary.setTotalAppliances(appliances.size());
        summary.setActiveAppliances((int) appliances.stream().filter(a -> Boolean.TRUE.equals(a.getIsOn())).count());
        summary.setDeviceStatus(isSimulated ? "SIMULATED_ACTIVE" : "CONNECTED");
        summary.setLastDataUpdate(LocalDateTime.now());
        summary.setAppliances(appliances);
        summary.setRecentAlerts(alertRepository.findTop20ByOrderByCreatedAtDesc());

        return ResponseEntity.ok(summary);
    }

    @GetMapping("/history")
    public ResponseEntity<List<EnergyReading>> getConsumptionHistory(
            @RequestParam(defaultValue = "daily") String range) {
        LocalDateTime since = switch (range.toLowerCase()) {
            case "weekly" -> LocalDateTime.now().minusDays(7);
            case "monthly" -> LocalDateTime.now().minusDays(30);
            default -> LocalDateTime.now().minusDays(1);
        };

        List<EnergyReading> readings = readingRepository.findReadingsSince(since);
        return ResponseEntity.ok(readings);
    }
}

