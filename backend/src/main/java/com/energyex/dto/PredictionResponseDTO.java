package com.energyex.dto;

import com.energyex.entity.Prediction;

import java.math.BigDecimal;
import java.util.List;

public class PredictionResponseDTO {

    private String predictionType;
    private List<Prediction> forecastDays;
    private BigDecimal totalForecastedKwh;
    private BigDecimal totalForecastedCostInr;
    private BigDecimal confidenceScore;
    private Boolean isDemoPrediction;
    private String modelVersion;
    private List<String> energySavingTips;
    private List<String> detectedAnomalies;

    public PredictionResponseDTO() {}

    public String getPredictionType() { return predictionType; }
    public void setPredictionType(String predictionType) { this.predictionType = predictionType; }

    public List<Prediction> getForecastDays() { return forecastDays; }
    public void setForecastDays(List<Prediction> forecastDays) { this.forecastDays = forecastDays; }

    public BigDecimal getTotalForecastedKwh() { return totalForecastedKwh; }
    public void setTotalForecastedKwh(BigDecimal totalForecastedKwh) { this.totalForecastedKwh = totalForecastedKwh; }

    public BigDecimal getTotalForecastedCostInr() { return totalForecastedCostInr; }
    public void setTotalForecastedCostInr(BigDecimal totalForecastedCostInr) { this.totalForecastedCostInr = totalForecastedCostInr; }

    public BigDecimal getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(BigDecimal confidenceScore) { this.confidenceScore = confidenceScore; }

    public Boolean getIsDemoPrediction() { return isDemoPrediction; }
    public void setIsDemoPrediction(Boolean isDemoPrediction) { this.isDemoPrediction = isDemoPrediction; }

    public String getModelVersion() { return modelVersion; }
    public void setModelVersion(String modelVersion) { this.modelVersion = modelVersion; }

    public List<String> getEnergySavingTips() { return energySavingTips; }
    public void setEnergySavingTips(List<String> energySavingTips) { this.energySavingTips = energySavingTips; }

    public List<String> getDetectedAnomalies() { return detectedAnomalies; }
    public void setDetectedAnomalies(List<String> detectedAnomalies) { this.detectedAnomalies = detectedAnomalies; }
}

