@echo off
cd /d "%~dp0"
title EnergyEx Spring Boot Backend
set "JAVA_HOME=C:\Program Files\Android\Android Studio\jbr"
cd backend
if exist target attrib -r target\*.* /s /d >nul 2>&1
echo ===================================================
echo   Starting EnergyEx Backend on http://localhost:8080
echo ===================================================
.\mvnw.bat spring-boot:run
pause
