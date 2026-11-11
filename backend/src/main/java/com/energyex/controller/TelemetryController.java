package com.energyex.controller;

import com.energyex.dto.TelemetryRequestDTO;
import com.energyex.entity.DeviceCommand;
import com.energyex.repository.DeviceCommandRepository;
import com.energyex.service.TelemetryService;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/iot")
@CrossOrigin(origins = "*")
public class TelemetryController {

    private final TelemetryService telemetryService;
    private final DeviceCommandRepository commandRepository;

    public TelemetryController(TelemetryService telemetryService,
                               DeviceCommandRepository commandRepository) {
        this.telemetryService = telemetryService;
        this.commandRepository = commandRepository;
    }

    @PostMapping("/telemetry")
    public ResponseEntity<Map<String, Object>> ingestTelemetry(@RequestBody TelemetryRequestDTO dto) {
        boolean success = telemetryService.ingestHardwareTelemetry(dto);
        Map<String, Object> resp = new java.util.HashMap<>();
        resp.put("status", success ? "SUCCESS" : "REJECTED");
        resp.put("deviceId", dto.getDeviceId() != null ? dto.getDeviceId() : "UNKNOWN");
        resp.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/commands")
    public ResponseEntity<List<DeviceCommand>> getPendingCommands(
            @RequestParam(defaultValue = "ESP32_ENERGYEX_01") String deviceId) {
        List<DeviceCommand> pending = commandRepository.findByDeviceIdAndExecutionStatus(deviceId, "PENDING");
        return ResponseEntity.ok(pending);
    }

    @PostMapping("/commands/{id}/ack")
    @Transactional
    public ResponseEntity<Map<String, Object>> acknowledgeCommand(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {
        return commandRepository.findById(id).map(command -> {
            String status = payload.getOrDefault("status", "CONFIRMED");
            command.setExecutionStatus(status);
            command.setAcknowledgedAt(LocalDateTime.now());
            commandRepository.save(command);

            java.util.Map<String, Object> resp = new java.util.HashMap<>();
            resp.put("commandId", command.getId());
            resp.put("status", status);
            resp.put("acknowledgedAt", LocalDateTime.now().toString());
            return ResponseEntity.ok(resp);
        }).orElse(ResponseEntity.notFound().build());
    }
}
