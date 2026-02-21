# HMS Startup Script - Starts backend first, waits for it to be ready, then starts frontend
# Usage: .\start.ps1

$ErrorActionPreference = "Stop"

Write-Host "=== HMS Hospital Management System ===" -ForegroundColor Cyan
Write-Host ""

# Start backend in a new window
Write-Host "[1/3] Starting backend (Spring Boot)..." -ForegroundColor Yellow
Start-Process -FilePath "mvn" -ArgumentList "spring-boot:run", "-f", "backend/pom.xml" -WorkingDirectory $PSScriptRoot -WindowStyle Normal

# Wait for backend to be ready
Write-Host "[2/3] Waiting for backend to be ready (max 90s)..." -ForegroundColor Yellow
& "$PSScriptRoot\scripts\wait-for-backend.ps1"
if ($LASTEXITCODE -ne 0) {
    Write-Host "You can still start the frontend manually: cd frontend; npm run dev" -ForegroundColor Yellow
    exit 1
}

# Start frontend
Write-Host "[3/3] Starting frontend (Vite)..." -ForegroundColor Yellow
Set-Location "$PSScriptRoot\frontend"
npm run dev
