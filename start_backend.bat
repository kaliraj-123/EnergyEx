@echo off
cd /d "%~dp0"
title EnergyEx Spring Boot Backend
set "JAVA_EXE=C:\Program Files\Android\Android Studio\jbr\bin\java.exe"
if exist "%JAVA_EXE%" (
    set "JAVA_CMD=%JAVA_EXE%"
) else (
    set "JAVA_CMD=java"
)
echo ===================================================
echo   Starting EnergyEx Backend on http://localhost:8080
echo ===================================================
if exist "backend\target\energyex-backend-1.0.0.jar" (
    "%JAVA_CMD%" -jar "backend\target\energyex-backend-1.0.0.jar"
) else (
    set "JAVA_HOME=C:\Program Files\Android\Android Studio\jbr"
    cd backend
    .\mvnw.bat spring-boot:run
)
pause
