@echo off
echo ===================================================
echo   Levantando servicios de PolyChat
echo ===================================================

cd /d "%~dp0"
echo [1/2] Iniciando Node.js (my-neon-app)...
cd my-neon-app
start "PolyChat - React Server" cmd /k "npm run dev"
cd ..

echo.
echo [2/2] Iniciando JavaFX (PolyChat Desktop)...
set JAVA_HOME=C:\Users\Albert\.jdks\corretto-18.0.2
call mvnw.cmd clean javafx:run

echo.
echo Gracias por usar PolyChat. Puedes cerrar la ventana de Node.js manualmente.
pause
