# Waits for HMS backend to be ready (health check)
$BackendUrl = "http://127.0.0.1:8080/api/actuator/health"
$MaxWaitSeconds = 90
$PollIntervalSeconds = 2

$elapsed = 0
while ($elapsed -lt $MaxWaitSeconds) {
    try {
        $response = Invoke-WebRequest -Uri $BackendUrl -Method GET -TimeoutSec 3 -UseBasicParsing -ErrorAction Stop
        if ($response.StatusCode -eq 200) {
            Write-Host "Backend is ready!"
            exit 0
        }
    } catch { }
    Start-Sleep -Seconds $PollIntervalSeconds
    $elapsed += $PollIntervalSeconds
    Write-Host "  Waiting... ($elapsed s)"
}
Write-Host "ERROR: Backend did not become ready in ${MaxWaitSeconds}s"
exit 1
