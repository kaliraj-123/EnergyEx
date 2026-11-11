@echo off
cd /d "%~dp0"
title EnergyEx Virtual ESP32 Hardware Emulator
echo ==============================================================
echo   Starting Virtual ESP32 Node (Simulating PZEM-004T + Relays)
echo   Target Backend: http://localhost:8080/api/v1/iot/telemetry
echo ==============================================================
"C:\Program Files\PostgreSQL\18\pgAdmin 4\python\python.exe" firmware\simulate_esp32_device.py
pause

