@echo off
cd /d "%~dp0"
title EnergyEx GitHub Publisher
echo =======================================================================
echo               EnergyEx: Push to GitHub (kaliraj-123)                   
echo =======================================================================
echo.
echo Target Repository: https://github.com/kaliraj-123/EnergyEx
echo.
echo [1/2] Ensuring remote is configured...
git remote set-url origin https://github.com/kaliraj-123/EnergyEx.git
git branch -M main

echo.
echo [2/2] Pushing code to GitHub...
git push -u origin main

if %errorlevel% neq 0 (
    echo.
    echo =======================================================================
    echo If the push failed because the repository does not exist on GitHub yet:
    echo 1. Open your browser to: https://github.com/new
    echo 2. Set Repository name to: EnergyEx
    echo 3. Click "Create repository" (leave all checkboxes blank)
    echo 4. Run this script again!
    echo =======================================================================
) else (
    echo.
    echo =======================================================================
    echo [SUCCESS] Code published successfully to:
    echo https://github.com/kaliraj-123/EnergyEx
    echo =======================================================================
)
echo.
pause
