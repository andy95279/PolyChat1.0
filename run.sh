#!/bin/bash

# Este script arranca el frontend de React y el cliente JavaFX simultáneamente.
# Cierra el servidor de React al cerrar la ventana de Java.

echo "Iniciando my-neon-app (React)..."
cd my-neon-app
npm run dev &
REACT_PID=$!
cd ..

echo "Iniciando PolyChat Desktop (JavaFX)..."
export JAVA_HOME="C:\Users\Albert\.jdks\corretto-18.0.2"
"C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2025.2.1\plugins\maven\lib\maven3\bin\mvn.cmd" clean javafx:run

echo "Cerrando servicios en segundo plano..."
kill $REACT_PID
