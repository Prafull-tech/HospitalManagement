@echo off
setlocal EnableDelayedExpansion
title HMS - Hospital Management System

:: =============================================================================
:: HMS Launcher - Starts Backend (Spring Boot) and Frontend (React/Vite)
:: Run from project root. Safe to re-run: frees ports 8080 and 3000 if in use.
:: =============================================================================

set "SCRIPT_DIR=%~dp0"
set "BACKEND_DIR=%SCRIPT_DIR%backend"
set "FRONTEND_DIR=%SCRIPT_DIR%frontend"
set "BACKEND_PORT=8080"
set "FRONTEND_PORT=3000"
set "BACKEND_MAX_WAIT=120"
set "BACKEND_POLL_INTERVAL=3"

echo.
echo [HMS] ============================================================
echo [HMS]   Hospital Management System - Launcher
echo [HMS] ============================================================
echo.

:: -----------------------------------------------------------------------------
:: 1. Check and kill processes on port 8080 (Spring Boot)
:: -----------------------------------------------------------------------------
echo [HMS] Checking port %BACKEND_PORT% (Spring Boot)...
powershell -NoProfile -Command "$c = Get-NetTCPConnection -LocalPort %BACKEND_PORT% -State Listen -ErrorAction SilentlyContinue; if ($c) { $c | ForEach-Object { Write-Host '  Killing PID' $_.OwningProcess; Stop-Process -Id $_.OwningProcess -Force -ErrorAction SilentlyContinue } } else { Write-Host '  Port %BACKEND_PORT% is free.' }"
echo.

:: -----------------------------------------------------------------------------
:: 2. Check and kill processes on port 3000 (React/Vite)
:: -----------------------------------------------------------------------------
echo [HMS] Checking port %FRONTEND_PORT% (React/Vite)...
powershell -NoProfile -Command "$c = Get-NetTCPConnection -LocalPort %FRONTEND_PORT% -State Listen -ErrorAction SilentlyContinue; if ($c) { $c | ForEach-Object { Write-Host '  Killing PID' $_.OwningProcess; Stop-Process -Id $_.OwningProcess -Force -ErrorAction SilentlyContinue } } else { Write-Host '  Port %FRONTEND_PORT% is free.' }"
echo.

:: Give OS a moment to release ports
timeout /t 2 /nobreak >nul

:: -----------------------------------------------------------------------------
:: 3. Verify backend and frontend directories exist
:: -----------------------------------------------------------------------------
if not exist "%BACKEND_DIR%\pom.xml" (
  echo [HMS] ERROR: Backend not found. Expected: %BACKEND_DIR%\pom.xml
  goto :error
)
if not exist "%FRONTEND_DIR%\package.json" (
  echo [HMS] ERROR: Frontend not found. Expected: %FRONTEND_DIR%\package.json
  goto :error
)
echo [HMS] Backend and frontend paths OK.
echo.

:: -----------------------------------------------------------------------------
:: 4. Start Spring Boot backend (new window)
:: -----------------------------------------------------------------------------
echo [HMS] Starting Spring Boot backend on port %BACKEND_PORT%...
start "HMS Backend (Spring Boot)" /d "%BACKEND_DIR%" cmd /k run-backend.bat
echo [HMS]   Backend window opened. Waiting until ready...
echo.

:: -----------------------------------------------------------------------------
:: 5. Wait until backend is ready (port listening)
:: -----------------------------------------------------------------------------
set "WAIT_COUNT=0"
:wait_backend
netstat -ano 2>nul | findstr ":%BACKEND_PORT%" | findstr "LISTENING" >nul 2>&1
if %errorlevel% equ 0 goto backend_ready

set /a WAIT_COUNT+=%BACKEND_POLL_INTERVAL%
if %WAIT_COUNT% geq %BACKEND_MAX_WAIT% (
  echo [HMS] ERROR: Backend did not start within %BACKEND_MAX_WAIT% seconds.
  goto :error
)

timeout /t %BACKEND_POLL_INTERVAL% /nobreak >nul
echo [HMS]   Waiting for backend... %WAIT_COUNT%/%BACKEND_MAX_WAIT% sec
goto wait_backend

:backend_ready
echo [HMS]   Backend is ready on http://localhost:%BACKEND_PORT%
echo.

:: -----------------------------------------------------------------------------
:: 6. Start React/Vite frontend (new window)
:: -----------------------------------------------------------------------------
echo [HMS] Starting React (Vite) frontend on port %FRONTEND_PORT%...
start "HMS Frontend (React)" /d "%FRONTEND_DIR%" cmd /k "%FRONTEND_DIR%\run-frontend.bat"
echo [HMS]   Frontend window opened.
echo.

:: -----------------------------------------------------------------------------
:: 7. Summary
:: -----------------------------------------------------------------------------
echo [HMS] ============================================================
echo [HMS]   All services started.
echo [HMS]   Backend:  http://localhost:%BACKEND_PORT%/api
echo [HMS]   Frontend: http://localhost:%FRONTEND_PORT%
echo [HMS]   Close the Backend and Frontend windows to stop.
echo [HMS] ============================================================
echo.
goto :eof

:error
echo [HMS] Launcher stopped with errors.
exit /b 1
