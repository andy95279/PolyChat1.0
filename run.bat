@echo off
echo ===================================================
echo   Levantando servicios de PolyChat
echo ===================================================

echo [1/2] Iniciando Node.js (my-neon-app)...
cd my-neon-app
echo Instalando dependencias (puede tardar un poco la primera vez)...
call npm install
start "PolyChat - React Server" cmd /k "npm run dev"
cd ..

echo.
echo [2/2] Iniciando JavaFX (PolyChat Desktop)...
call mvnw.cmd clean javafx:run

echo.
echo Gracias por usar PolyChat. Puedes cerrar la ventana de Node.js manualmente.
pause
