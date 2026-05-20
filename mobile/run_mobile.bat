@echo off
title PolyChat Mobile - Emulator Launcher
echo ============================================
echo   PolyChat Mobile - Emulator Launcher
echo ============================================
echo.

REM === Configuration ===
set PORT=8080
set MOBILE_DIR=%~dp0

REM === Step 1: Start local HTTP server ===
echo [1/2] Starting HTTP server on port %PORT%...

REM Kill any existing server on the port
for /f "tokens=5" %%a in ('netstat -aon ^| findstr ":%PORT% " ^| findstr "LISTENING"') do (
    taskkill /PID %%a /F >nul 2>&1
)

REM Start Node.js server
start /b "" node "%MOBILE_DIR%server.js"

timeout /t 2 /nobreak >nul
echo       HTTP server started on http://localhost:%PORT%

REM === Step 2: Open in mobile emulator (Brave or Default Browser) ===
set "BRAVE_PATH=C:\Program Files\BraveSoftware\Brave-Browser\Application\brave.exe"
if exist "%BRAVE_PATH%" (
    echo [2/2] Opening PolyChat Mobile in Brave Mobile Emulator...
    start "" "%BRAVE_PATH%" --app="http://localhost:%PORT%/" --window-size=375,812 --user-agent="Mozilla/5.0 (iPhone; CPU iPhone OS 16_6 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.6 Mobile/15E148 Safari/604.1"
) else (
    echo [2/2] Brave not found. Opening PolyChat Mobile in default browser...
    explorer "http://localhost:%PORT%/"
)

echo.
echo ============================================
echo   PolyChat Mobile is running!
echo   URL: http://localhost:%PORT%
echo ============================================
echo.

pause
