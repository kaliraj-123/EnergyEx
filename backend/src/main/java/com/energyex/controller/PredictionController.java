package com.energyex.controller;

import com.energyex.dto.PredictionResponseDTO;
import com.energyex.service.AIServiceClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/predictions")
@CrossOrigin(origins = "*")
public class PredictionController {

    private final AIServiceClient aiServiceClient;

    public PredictionController(AIServiceClient aiServiceClient) {
        this.aiServiceClient = aiServiceClient;
    }

    @GetMapping
    public ResponseEntity<PredictionResponseDTO> getPredictions() {
        PredictionResponseDTO response = aiServiceClient.getPredictionsAndAnalysis();
        return ResponseEntity.ok(response);
    }
}

