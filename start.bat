@echo off
REM HMS Startup Script - Starts backend first, waits for it to be ready, then starts frontend
REM Usage: start.bat

echo === HMS Hospital Management System ===
echo.

REM Start backend in a new window
echo [1/3] Starting backend (Spring Boot)...
start "HMS Backend" cmd /k "cd /d %~dp0backend && mvn spring-boot:run"

REM Wait for backend to be ready
echo [2/3] Waiting for backend to be ready (max 90s)...
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\wait-for-backend.ps1"
if %errorlevel% neq 0 (
    echo You can still start frontend manually: cd frontend ^&^& npm run dev
    pause
    exit /b 1
)

:start_frontend
echo [3/3] Starting frontend (Vite)...
cd /d %~dp0frontend
npm run dev
