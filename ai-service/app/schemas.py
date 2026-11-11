# pyrefly: ignore-errors
# pyright: reportMissingImports=false, reportUnusedImport=false, reportAttributeAccessIssue=false
# type: ignore
from pydantic import BaseModel
from typing import List, Optional

class DailyForecastItem(BaseModel):
    predictionType: str = "DAILY_7_DAYS"
    forecastDate: str
    predictedConsumptionKwh: float
    predictedCostInr: float
    confidenceScore: float = 88.5
    isDemoPrediction: bool = True
    modelVersion: str = "RandomForestRegressor-v1.0"
    notes: Optional[str] = None

class PredictionResponse(BaseModel):
    predictionType: str = "DAILY_7_DAYS"
    forecastDays: List[DailyForecastItem]
    totalForecastedKwh: float
    totalForecastedCostInr: float
    confidenceScore: float = 88.5
    isDemoPrediction: bool = True
    modelVersion: str = "RandomForestRegressor-v1.0"
    energySavingTips: List[str]
    detectedAnomalies: List[str]

class AnomalyDetectionRequest(BaseModel):
    powerReadings: List[float]
    thresholdZScore: float = 2.5

class AnomalyItem(BaseModel):
    index: int
    value: float
    zScore: float
    message: str

class AnomalyDetectionResponse(BaseModel):
    hasAnomalies: bool
    anomaliesCount: int
    anomalies: List[AnomalyItem]
    status: str
