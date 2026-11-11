@echo off
cd /d "%~dp0"
title EnergyEx Master Launcher
echo =======================================================================
echo          EnergyEx: Smart Electricity Monitoring & Prediction          
echo                  One-Click System Master Launcher                      
echo =======================================================================
echo.
echo [1/3] Launching Python AI Prediction Microservice on Port 8000...
start "EnergyEx AI Service (Port 8000)" cmd /c "start_ai_service.bat"

echo [2/3] Launching Spring Boot Backend on Port 8080 (REST + STOMP WebSocket)...
start "EnergyEx Spring Boot Backend (Port 8080)" cmd /c "start_backend.bat"

echo.
echo Waiting 6 seconds for backend and AI service to initialize...
timeout /t 6 /nobreak >nul

echo [3/3] Opening Live Web Dashboard in default browser...
start http://localhost:8080/

echo.
echo =======================================================================
echo   SYSTEM ONLINE:
echo   - Web Console:   http://localhost:8080/
echo   - AI Service:    http://localhost:8000/
echo   - AI Swagger:    http://localhost:8000/docs
echo   - H2 Console:    http://localhost:8080/h2-console
echo   - Android Target: http://10.0.2.2:8080/ (Emulator) or Local IP (Phone)
echo =======================================================================
echo.
pause

