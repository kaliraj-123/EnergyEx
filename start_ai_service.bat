@echo off
cd /d "%~dp0"
title EnergyEx AI Prediction Service
echo ==============================================================
echo   Starting EnergyEx AI Service on http://localhost:8000
echo   Model: Random Forest Regressor + IQR Spike Anomaly Detector
echo   Swagger UI: http://localhost:8000/docs
echo ==============================================================
"C:\Program Files\PostgreSQL\18\pgAdmin 4\python\python.exe" ai-service\run_ai_service.py
pause
