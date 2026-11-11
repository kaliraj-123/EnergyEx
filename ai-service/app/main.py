# pyrefly: ignore-errors
# pyright: reportMissingImports=false, reportUnusedImport=false, reportAttributeAccessIssue=false, reportPrivateUsage=false
# type: ignore
"""
EnergyEx AI Service - Main FastAPI Application
Provides endpoints for Random Forest electricity consumption forecasting,
anomaly detection (Z-Score + IQR), and energy saving recommendations.
"""

from fastapi import FastAPI, Query  # type: ignore
from fastapi.middleware.cors import CORSMiddleware  # type: ignore

try:
    from .schemas import (  # type: ignore
        PredictionResponse,
        AnomalyDetectionRequest,
        AnomalyDetectionResponse
    )
    from .model import EnergyPredictor, AnomalyDetector  # type: ignore
except (ImportError, ValueError):
    try:
        from schemas import (  # type: ignore
            PredictionResponse,
            AnomalyDetectionRequest,
            AnomalyDetectionResponse
        )
        from model import EnergyPredictor, AnomalyDetector  # type: ignore
    except (ImportError, ValueError):
        from app.schemas import (  # type: ignore
            PredictionResponse,
            AnomalyDetectionRequest,
            AnomalyDetectionResponse
        )
        from app.model import EnergyPredictor, AnomalyDetector  # type: ignore

app = FastAPI(
    title="EnergyEx AI Prediction and Anomaly Service",
    description="Machine Learning Service powered by Random Forest Regression and Anomaly Detection for EnergyEx",
    version="1.0.0"
)

# CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Global models
predictor = EnergyPredictor()
anomaly_detector = AnomalyDetector()

@app.get("/")
def root():
    return {
        "service": "EnergyEx AI Service",
        "status": "ONLINE",
        "model": predictor.model_version,
        "confidence_score": predictor.confidence_score,
        "is_trained": predictor.is_trained
    }

@app.get("/predict/daily", response_model=PredictionResponse)
def predict_daily_consumption(
    days: int = Query(7, ge=1, le=30, description="Forecast horizon in days"),
    rate: float = Query(7.50, ge=1.0, description="Electricity tariff rate in ₹/kWh")
):
    """
    Predicts day-by-day electricity consumption (kWh) and bill (₹)
    using Random Forest Regression.
    """
    result = predictor.predict_future(days=days, tariff_rate=rate)
    return result

@app.post("/anomaly/detect", response_model=AnomalyDetectionResponse)
def detect_consumption_anomalies(request: AnomalyDetectionRequest):
    """
    Detects unusual electricity usage spikes from a series of power readings.
    """
    result = anomaly_detector.detect_spikes(
        readings=request.powerReadings,
        threshold_z=request.thresholdZScore
    )
    return result

@app.get("/recommendations")
def get_energy_saving_recommendations():
    """
    Returns energy efficiency recommendations based on appliance profiles.
    """
    return {
        "recommendations": predictor.generate_tips(),
        "baseline_domestic_kwh": 6.2,
        "potential_monthly_savings_inr": 450.00
    }

if __name__ == "__main__":
    import importlib  # type: ignore
    _uvicorn = importlib.import_module("uvicorn")
    _uvicorn.run("app.main:app", host="0.0.0.0", port=8000, reload=True)
