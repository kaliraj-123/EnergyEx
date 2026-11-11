package com.energyex.controller;

import com.energyex.dto.DeviceSetupDTO;
import com.energyex.dto.SystemModeDTO;
import com.energyex.entity.Device;
import com.energyex.entity.Tariff;
import com.energyex.entity.User;
import com.energyex.repository.DeviceRepository;
import com.energyex.repository.TariffRepository;
import com.energyex.repository.UserRepository;
import com.energyex.service.EnergyCalculationService;
import com.energyex.simulator.IoTSimulatorService;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/system")
@CrossOrigin(origins = "*")
public class SystemController {

    private final IoTSimulatorService simulatorService;
    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;
    private final TariffRepository tariffRepository;
    private final EnergyCalculationService calculationService;

    public SystemController(IoTSimulatorService simulatorService,
                            DeviceRepository deviceRepository,
                            UserRepository userRepository,
                            TariffRepository tariffRepository,
                            EnergyCalculationService calculationService) {
        this.simulatorService = simulatorService;
        this.deviceRepository = deviceRepository;
        this.userRepository = userRepository;
        this.tariffRepository = tariffRepository;
        this.calculationService = calculationService;
    }

    @GetMapping("/mode")
    public ResponseEntity<SystemModeDTO> getSystemMode() {
        boolean isSimulated = simulatorService.isSimulationActive();
        String mode = simulatorService.getSystemMode();
        String label = isSimulated ? "DEMO DATA (SIMULATION MODE)" : "LIVE IOT MODE (ESP32 SENSORS)";
        String message = isSimulated
                ? "Running with simulated domestic load profiles (Light, Fan, TV, Refrigerator). Realistic voltage and current are generated."
                : "Awaiting or streaming live sensor telemetry from ESP32 + PZEM-004T.";

        return ResponseEntity.ok(new SystemModeDTO(mode, label, isSimulated, message));
    }

    @PostMapping("/mode")
    public ResponseEntity<SystemModeDTO> switchSystemMode(@RequestBody Map<String, String> body) {
        String targetMode = body.getOrDefault("mode", "DEMO").toUpperCase();
        simulatorService.setSystemMode(targetMode);

        // Update device record
        deviceRepository.findByDeviceId("ESP32_ENERGYEX_01").ifPresent(device -> {
            device.setIsLiveMode("LIVE_IOT".equals(targetMode));
            device.setStatus("LIVE_IOT".equals(targetMode) ? "CONNECTING" : "SIMULATED_ACTIVE");
            deviceRepository.save(device);
        });

        return getSystemMode();
    }

    @GetMapping("/device")
    public ResponseEntity<DeviceSetupDTO> getDeviceStatus() {
        Device device = deviceRepository.findFirstByOrderByIdAsc()
                .orElseGet(() -> {
                    Device d = new Device();
                    d.setDeviceId("ESP32_ENERGYEX_01");
                    d.setDeviceName("Home Energy Monitor (ESP32 + PZEM)");
                    d.setStatus(simulatorService.isSimulationActive() ? "SIMULATED_ACTIVE" : "DISCONNECTED");
                    d.setAuthToken("ENERGYEX_IOT_SECURE_TOKEN_2026");
                    return d;
                });

        DeviceSetupDTO dto = new DeviceSetupDTO();
        dto.setDeviceId(device.getDeviceId());
        dto.setDeviceName(device.getDeviceName());
        dto.setIpAddress(device.getIpAddress());
        dto.setAuthToken(device.getAuthToken());
        dto.setStatus(device.getStatus());
        dto.setIsLiveMode(device.getIsLiveMode());
        dto.setLastSeenAt(device.getLastSeenAt());
        dto.setWifiSsid("EnergyEx-Home-WiFi");

        return ResponseEntity.ok(dto);
    }

    @PostMapping("/device/setup")
    @Transactional
    public ResponseEntity<DeviceSetupDTO> configureDevice(@RequestBody DeviceSetupDTO dto) {
        Device device = deviceRepository.findByDeviceId(dto.getDeviceId() != null ? dto.getDeviceId() : "ESP32_ENERGYEX_01")
                .orElseGet(() -> {
                    Device d = new Device();
                    d.setDeviceId(dto.getDeviceId() != null ? dto.getDeviceId() : "ESP32_ENERGYEX_01");
                    return d;
                });

        if (dto.getDeviceName() != null) device.setDeviceName(dto.getDeviceName());
        if (dto.getAuthToken() != null) device.setAuthToken(dto.getAuthToken());
        if (dto.getIpAddress() != null) device.setIpAddress(dto.getIpAddress());
        if (dto.getIsLiveMode() != null) device.setIsLiveMode(dto.getIsLiveMode());
        device.setLastSeenAt(LocalDateTime.now());

        Device saved = deviceRepository.save(device);

        dto.setDeviceId(saved.getDeviceId());
        dto.setDeviceName(saved.getDeviceName());
        dto.setStatus(saved.getStatus());
        dto.setLastSeenAt(saved.getLastSeenAt());
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/user")
    public ResponseEntity<User> getCurrentUser() {
        User user = userRepository.findFirstByOrderByIdAsc()
                .orElseGet(() -> {
                    User u = new User("Student Engineer", "student@energyex.local");
                    return userRepository.save(u);
                });
        return ResponseEntity.ok(user);
    }

    @PostMapping("/user/setup")
    @Transactional
    public ResponseEntity<Map<String, Object>> setupFirstTimeUser(@RequestBody Map<String, Object> body) {
        String name = (String) body.getOrDefault("name", "Student Engineer");
        Double tariffDouble = body.containsKey("tariffRate") ? Double.valueOf(body.get("tariffRate").toString()) : 7.50;

        User user = userRepository.findFirstByOrderByIdAsc()
                .orElseGet(() -> new User(name, "student@energyex.local"));
        user.setName(name);
        userRepository.save(user);

        Tariff tariff = calculationService.getActiveTariff();
        tariff.setRatePerKwh(BigDecimal.valueOf(tariffDouble));
        tariffRepository.save(tariff);

        Map<String, Object> resp = new java.util.HashMap<>();
        resp.put("status", "SUCCESS");
        resp.put("name", user.getName() != null ? user.getName() : "EnergyEx User");
        resp.put("tariffRatePerKwh", tariff.getRatePerKwh());
        resp.put("message", "First-time setup completed successfully!");
        return ResponseEntity.ok(resp);
    }
}

