@echo off
cd /d "%~dp0"
title EnergyEx Physical Android Device Deployment Tool
echo =======================================================================
echo          EnergyEx: Physical Android Phone Deployment Utility           
echo =======================================================================
echo.

set "ADB_PATH=C:\Users\kalir\AppData\Local\Android\Sdk\platform-tools\adb.exe"

if not exist "%ADB_PATH%" (
    echo [ERROR] ADB not found at: %ADB_PATH%
    echo Please ensure Android Studio SDK Platform-Tools are installed.
    pause
    exit /b 1
)

echo [1/3] Checking connected Android devices via ADB...
echo.
"%ADB_PATH%" devices -l
echo.

echo [2/3] Setting up USB Port Forwarding (adb reverse)...
echo Routing phone's localhost:8080 to your computer's backend...
"%ADB_PATH%" reverse tcp:8080 tcp:8080
"%ADB_PATH%" reverse tcp:8000 tcp:8000
echo.
echo [PORT FORWARDING OK]
echo The mobile app can now connect to your computer using:
echo   - http://localhost:8080/ (over USB cable)
echo   - or http://10.47.247.16:8080/ (over shared Wi-Fi)
echo.

echo =======================================================================
echo Next Steps:
echo 1. Open Android Studio.
echo 2. Open folder: "%CD%\android"
echo 3. In the top toolbar, select your physical phone in the device dropdown.
echo 4. Click the Green Play Button (Run 'app') or press Shift + F10.
echo =======================================================================
echo.
pause

