#!/bin/bash

# Este script arranca el frontend de React y el cliente JavaFX simultáneamente.
# Cierra el servidor de React al cerrar la ventana de Java.

echo "Iniciando my-neon-app (React)..."
cd my-neon-app
# Instalamos dependencias por si es la primera vez que se descarga
npm install
npm run dev &
REACT_PID=$!
cd ..

echo "Iniciando PolyChat Desktop (JavaFX)..."
# Usamos el wrapper de Maven incluido en el proyecto
./mvnw clean javafx:run

echo "Cerrando servicios en segundo plano..."
kill $REACT_PID
