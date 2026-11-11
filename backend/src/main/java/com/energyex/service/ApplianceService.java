package com.energyex.service;

import com.energyex.dto.ApplianceControlRequestDTO;
import com.energyex.dto.ApplianceDTO;
import com.energyex.entity.Alert;
import com.energyex.entity.Appliance;
import com.energyex.entity.DeviceCommand;
import com.energyex.repository.AlertRepository;
import com.energyex.repository.ApplianceRepository;
import com.energyex.repository.DeviceCommandRepository;
import com.energyex.simulator.IoTSimulatorService;
import com.energyex.websocket.TelemetryBroadcaster;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ApplianceService {

    private final ApplianceRepository applianceRepository;
    private final DeviceCommandRepository commandRepository;
    private final AlertRepository alertRepository;
    private final IoTSimulatorService simulatorService;
    private final TelemetryBroadcaster telemetryBroadcaster;

    public ApplianceService(ApplianceRepository applianceRepository,
                            DeviceCommandRepository commandRepository,
                            AlertRepository alertRepository,
                            IoTSimulatorService simulatorService,
                            TelemetryBroadcaster telemetryBroadcaster) {
        this.applianceRepository = applianceRepository;
        this.commandRepository = commandRepository;
        this.alertRepository = alertRepository;
        this.simulatorService = simulatorService;
        this.telemetryBroadcaster = telemetryBroadcaster;
    }

    public List<Appliance> getAllAppliances() {
        return applianceRepository.findAllByOrderByIdAsc();
    }

    public Optional<Appliance> getApplianceById(Long id) {
        return applianceRepository.findById(id);
    }

    @Transactional
    public Appliance createAppliance(ApplianceDTO dto) {
        Appliance appliance = new Appliance();
        appliance.setName(dto.getName());
        appliance.setApplianceType(dto.getApplianceType().toUpperCase());
        appliance.setRelayChannel(dto.getRelayChannel() != null ? dto.getRelayChannel() : 1);
        appliance.setSensorChannel(dto.getSensorChannel() != null ? dto.getSensorChannel() : appliance.getRelayChannel());
        appliance.setPowerRatingWatts(dto.getPowerRatingWatts() != null ? dto.getPowerRatingWatts() : BigDecimal.valueOf(100.0));
        appliance.setIsOn(Boolean.TRUE.equals(dto.getIsOn()));
        appliance.setIsSimulated(simulatorService.isSimulationActive());
        appliance.setCumulativeEnergyKwh(BigDecimal.ZERO);
        appliance.setEstimatedCostInr(BigDecimal.ZERO);
        appliance.setConnectionStatus("ONLINE");

        Appliance saved = applianceRepository.save(appliance);
        telemetryBroadcaster.broadcastApplianceUpdate(saved);
        return saved;
    }

    @Transactional
    public Optional<Appliance> updateAppliance(Long id, ApplianceDTO dto) {
        return applianceRepository.findById(id).map(appliance -> {
            if (dto.getName() != null) appliance.setName(dto.getName());
            if (dto.getApplianceType() != null) appliance.setApplianceType(dto.getApplianceType().toUpperCase());
            if (dto.getPowerRatingWatts() != null) appliance.setPowerRatingWatts(dto.getPowerRatingWatts());
            if (dto.getRelayChannel() != null) appliance.setRelayChannel(dto.getRelayChannel());
            if (dto.getSensorChannel() != null) appliance.setSensorChannel(dto.getSensorChannel());
            Appliance saved = applianceRepository.save(appliance);
            telemetryBroadcaster.broadcastApplianceUpdate(saved);
            return saved;
        });
    }

    @Transactional
    public boolean deleteAppliance(Long id) {
        if (applianceRepository.existsById(id)) {
            applianceRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * Remote ON/OFF appliance switching.
     * In DEMO mode: simulated hardware confirmation is applied immediately.
     * In LIVE IoT mode: command is queued for ESP32 relay module.
     */
    @Transactional
    public DeviceCommand controlAppliance(Long applianceId, ApplianceControlRequestDTO request) {
        Appliance appliance = applianceRepository.findById(applianceId)
                .orElseThrow(() -> new IllegalArgumentException("Appliance not found with ID: " + applianceId));

        String targetState = request.getState().toUpperCase();
        boolean turnOn = "ON".equals(targetState);
        boolean isDemo = simulatorService.isSimulationActive();

        DeviceCommand command = new DeviceCommand(
                "ESP32_ENERGYEX_01",
                appliance,
                appliance.getRelayChannel(),
                targetState,
                isDemo
        );

        if (isDemo) {
            // Instant simulated relay execution
            appliance.setIsOn(turnOn);
            if (!turnOn) {
                appliance.setCurrentPowerW(BigDecimal.ZERO);
                appliance.setCurrentA(BigDecimal.ZERO);
            }
            applianceRepository.save(appliance);

            command.setExecutionStatus("CONFIRMED");
            command.setAcknowledgedAt(LocalDateTime.now());
            commandRepository.save(command);

            telemetryBroadcaster.broadcastApplianceUpdate(appliance);

            Alert alert = new Alert(
                    "Relay Switched (Simulation): " + appliance.getName(),
                    appliance.getName() + " was switched " + targetState + " via remote control (Demo Mode).",
                    "SCHEDULE_TRIGGER", "INFO", appliance, true
            );
            alertRepository.save(alert);
            telemetryBroadcaster.broadcastAlert(alert);
        } else {
            // Live IoT mode: wait for ESP32 hardware acknowledgment
            command.setExecutionStatus("PENDING");
            commandRepository.save(command);
        }

        return command;
    }
}

