@echo off
title PolyChat Mobile - Emulator Launcher
echo ============================================
echo   PolyChat Mobile - Emulator Launcher
echo ============================================
echo.

REM === Configuration ===
set ANDROID_SDK=%LOCALAPPDATA%\Android\Sdk
set EMULATOR=%ANDROID_SDK%\emulator\emulator.exe
set ADB=%ANDROID_SDK%\platform-tools\adb.exe
set AVD_NAME=Pixel_9
set PORT=8080
set MOBILE_DIR=%~dp0

REM === Step 1: Check if emulator is already running ===
echo [1/4] Checking emulator status...
"%ADB%" devices 2>nul | findstr /C:"emulator" >nul
if %errorlevel%==0 (
    echo       Emulator is already running.
    goto :wait_boot
)

REM === Step 2: Start the Android Emulator ===
echo [2/4] Starting Android Emulator (%AVD_NAME%)...
start "" "%EMULATOR%" -avd %AVD_NAME% -no-snapshot-load
echo       Emulator is starting... Waiting for boot...

:wait_boot
echo [2/4] Waiting for emulator to fully boot...
:check_boot
timeout /t 3 /nobreak >nul
"%ADB%" shell getprop sys.boot_completed 2>nul | findstr /C:"1" >nul
if %errorlevel% neq 0 (
    echo       Still booting...
    goto :check_boot
)
echo       Emulator booted successfully!

REM === Step 3: Start local HTTP server ===
echo [3/4] Starting HTTP server on port %PORT%...

REM Kill any existing server on the port
for /f "tokens=5" %%a in ('netstat -aon ^| findstr ":%PORT% " ^| findstr "LISTENING"') do (
    taskkill /PID %%a /F >nul 2>&1
)

REM Start Node.js server with Neon DB connection
start /b "" node "%MOBILE_DIR%server.js"

timeout /t 2 /nobreak >nul
echo       HTTP server started on http://localhost:%PORT%

REM === Step 4: Open in emulator browser ===
echo [4/4] Opening PolyChat Mobile in emulator browser...

REM Forward port from emulator to host
"%ADB%" reverse tcp:%PORT% tcp:%PORT%

REM Open Chrome in the emulator
"%ADB%" shell am start -a android.intent.action.VIEW -d "http://localhost:%PORT%/" com.android.chrome

echo.
echo ============================================
echo   PolyChat Mobile is running!
echo   URL: http://localhost:%PORT%
echo   Press Ctrl+C to stop the server.
echo ============================================
echo.

REM Keep the script alive
pause
