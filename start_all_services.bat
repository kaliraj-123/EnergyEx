@echo off
cd /d "%~dp0"
title EnergyEx Master Launcher
echo =======================================================================
echo          EnergyEx: Smart Electricity Monitoring & Prediction          
echo                  One-Click System Master Launcher                      
echo =======================================================================
echo.
echo [1/4] Launching Python AI Prediction Microservice on Port 8000...
start "EnergyEx AI Service (Port 8000)" cmd /c "start_ai_service.bat"

echo [2/4] Launching Spring Boot Backend on Port 8080 (REST + STOMP WebSocket)...
start "EnergyEx Spring Boot Backend (Port 8080)" cmd /c "start_backend.bat"

echo.
echo [3/4] Launching Virtual ESP32 Sensor Simulator (Phase 1 Demo Mode)...
start "EnergyEx Virtual ESP32 (Phase 1)" cmd /c "start_virtual_esp32.bat"

echo.
echo Waiting 8 seconds for backend and AI services to initialize...
timeout /t 8 /nobreak >nul

echo [4/4] Opening Live Web Dashboard in default browser...
start http://localhost:8080/

echo.
echo =======================================================================
echo   ALL SERVICES ARE NOW ONLINE LOCALLY:
echo   - Web Console:    http://localhost:8080/
echo   - AI Service:     http://localhost:8000/
echo   - AI Swagger API: http://localhost:8000/docs
echo   - H2 Database:    http://localhost:8080/h2-console
echo   - Android Phone:  Run 'run_on_phone.bat' to map USB port forwarding
echo =======================================================================
echo.
pause
