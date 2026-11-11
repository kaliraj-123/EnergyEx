package com.energyex.controller;

import com.energyex.entity.Alert;
import com.energyex.repository.AlertRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/alerts")
@CrossOrigin(origins = "*")
public class AlertController {

    private final AlertRepository alertRepository;

    public AlertController(AlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    @GetMapping
    public ResponseEntity<List<Alert>> getAllAlerts() {
        return ResponseEntity.ok(alertRepository.findTop20ByOrderByCreatedAtDesc());
    }

    @PutMapping("/{id}/read")
    @Transactional
    public ResponseEntity<Alert> markAsRead(@PathVariable Long id) {
        return alertRepository.findById(id).map(alert -> {
            alert.setIsRead(true);
            return ResponseEntity.ok(alertRepository.save(alert));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> dismissAlert(@PathVariable Long id) {
        if (alertRepository.existsById(id)) {
            alertRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}

