package com.energyex.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "predictions")
public class Prediction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "prediction_type", length = 50)
    private String predictionType = "DAILY_7_DAYS"; // DAILY_7_DAYS, MONTHLY_BILL

    @Column(name = "forecast_date", nullable = false)
    private LocalDate forecastDate;

    @Column(name = "predicted_consumption_kwh", nullable = false, precision = 10, scale = 2)
    private BigDecimal predictedConsumptionKwh;

    @Column(name = "predicted_cost_inr", nullable = false, precision = 10, scale = 2)
    private BigDecimal predictedCostInr;

    @Column(name = "confidence_score", precision = 5, scale = 2)
    private BigDecimal confidenceScore = BigDecimal.valueOf(85.0);

    @Column(name = "is_demo_prediction")
    private Boolean isDemoPrediction = true;

    @Column(name = "model_version", length = 50)
    private String modelVersion = "RandomForestRegressor-v1.0";

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Prediction() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPredictionType() { return predictionType; }
    public void setPredictionType(String predictionType) { this.predictionType = predictionType; }

    public LocalDate getForecastDate() { return forecastDate; }
    public void setForecastDate(LocalDate forecastDate) { this.forecastDate = forecastDate; }

    public BigDecimal getPredictedConsumptionKwh() { return predictedConsumptionKwh; }
    public void setPredictedConsumptionKwh(BigDecimal predictedConsumptionKwh) { this.predictedConsumptionKwh = predictedConsumptionKwh; }

    public BigDecimal getPredictedCostInr() { return predictedCostInr; }
    public void setPredictedCostInr(BigDecimal predictedCostInr) { this.predictedCostInr = predictedCostInr; }

    public BigDecimal getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(BigDecimal confidenceScore) { this.confidenceScore = confidenceScore; }

    public Boolean getIsDemoPrediction() { return isDemoPrediction; }
    public void setIsDemoPrediction(Boolean isDemoPrediction) { this.isDemoPrediction = isDemoPrediction; }

    public String getModelVersion() { return modelVersion; }
    public void setModelVersion(String modelVersion) { this.modelVersion = modelVersion; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

