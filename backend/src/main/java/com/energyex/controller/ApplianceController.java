package com.energyex.controller;

import com.energyex.dto.ApplianceControlRequestDTO;
import com.energyex.dto.ApplianceDTO;
import com.energyex.entity.Appliance;
import com.energyex.entity.DeviceCommand;
import com.energyex.service.ApplianceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/appliances")
@CrossOrigin(origins = "*")
public class ApplianceController {

    private final ApplianceService applianceService;

    public ApplianceController(ApplianceService applianceService) {
        this.applianceService = applianceService;
    }

    @GetMapping
    public ResponseEntity<List<Appliance>> getAllAppliances() {
        return ResponseEntity.ok(applianceService.getAllAppliances());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Appliance> getApplianceById(@PathVariable Long id) {
        return applianceService.getApplianceById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Appliance> createAppliance(@RequestBody ApplianceDTO dto) {
        Appliance created = applianceService.createAppliance(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Appliance> updateAppliance(@PathVariable Long id, @RequestBody ApplianceDTO dto) {
        return applianceService.updateAppliance(id, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAppliance(@PathVariable Long id) {
        if (applianceService.deleteAppliance(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/control")
    public ResponseEntity<DeviceCommand> controlAppliance(
            @PathVariable Long id,
            @RequestBody ApplianceControlRequestDTO request) {
        DeviceCommand command = applianceService.controlAppliance(id, request);
        return ResponseEntity.ok(command);
    }
}

