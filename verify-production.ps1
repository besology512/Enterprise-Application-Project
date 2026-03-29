$ErrorActionPreference = "Continue"

# Configure Target Here
$BaseUrl = "http://localhost:8081"
$ContainerName = "workhub-app"

Write-Host "==========================================================" -ForegroundColor Yellow
Write-Host "   WORKHUB SAAS - MASTER PRODUCTION VERIFICATION SUITE   " -ForegroundColor Yellow
Write-Host "==========================================================" -ForegroundColor Yellow
Write-Host "Target: $BaseUrl" -ForegroundColor Gray

# --- 1. JWT Authentication ---
Write-Host "`n[P1-01] JWT Authentication" -ForegroundColor Cyan
try {
    $loginBody = @{ email = "admin@tenantA.com"; password = "password" } | ConvertTo-Json
    $loginResp = Invoke-RestMethod -Uri "$BaseUrl/auth/login" -Method Post -Body $loginBody -ContentType "application/json"
    $TokenA = $loginResp.token
    Write-Host "✓ PASSED" -ForegroundColor Green
} catch {
    Write-Host "✗ FAILED: $($_.Exception.Message)" -ForegroundColor Red
}

# --- 2. Identity Context ---
Write-Host "`n[P1-02] Identity Context (/auth/me)" -ForegroundColor Cyan
try {
    $meResp = Invoke-RestMethod -Uri "$BaseUrl/auth/me" -Method Get -Headers @{ Authorization = "Bearer $TokenA" }
    if ($meResp.tenantId -ne 1) { throw "Tenant ID mismatch" }
    Write-Host "✓ PASSED (Tenant: $($meResp.tenantId))" -ForegroundColor Green
} catch {
    Write-Host "✗ FAILED: $($_.Exception.Message)" -ForegroundColor Red
}

# --- 3. Tenant Isolation ---
Write-Host "`n[P2-01] Strict Tenant Isolation" -ForegroundColor Cyan
try {
    $projResp = Invoke-RestMethod -Uri "$BaseUrl/projects" -Method Post -Body (@{ name = "Isolation Test"; description = "P2" } | ConvertTo-Json) -ContentType "application/json" -Headers @{ Authorization = "Bearer $TokenA" }
    $pid = $projResp.id

    $loginB = Invoke-RestMethod -Uri "$BaseUrl/auth/login" -Method Post -Body (@{ email = "admin@tenantB.com"; password = "password" } | ConvertTo-Json) -ContentType "application/json"
    $tokenB = $loginB.token
    
    try {
        Invoke-RestMethod -Uri "$BaseUrl/projects/$pid" -Method Get -Headers @{ Authorization = "Bearer $tokenB" }
        Write-Host "✗ LEAK DETECTED!" -ForegroundColor Red
    } catch {
        Write-Host "✓ PASSED (Access denied as expected)" -ForegroundColor Green
    }
} catch {
    Write-Host "✗ FAILED: $($_.Exception.Message)" -ForegroundColor Red
}

# --- 4. Messaging Workers ---
Write-Host "`n[P2-02] Async Messaging (RabbitMQ)" -ForegroundColor Cyan
try {
    Invoke-RestMethod -Uri "$BaseUrl/projects/1/report" -Method Post -Headers @{ Authorization = "Bearer $TokenA" }
    Write-Host "Waiting for worker..."
    Start-Sleep -Seconds 5
    $logs = docker logs $ContainerName --tail 100
    if ($logs -match "Processing report for project 1") {
        Write-Host "✓ PASSED" -ForegroundColor Green
    } else {
        throw "Logs not found"
    }
} catch {
    Write-Host "✗ FAILED: $($_.Exception.Message)" -ForegroundColor Red
}

# --- 5. Health & Monitoring ---
Write-Host "`n[P3-01] Health Probes" -ForegroundColor Cyan
try {
    $h = Invoke-RestMethod -Uri "$BaseUrl/actuator/health"
    if ($h.status -ne "UP") { throw "App Down" }
    Write-Host "✓ PASSED" -ForegroundColor Green
} catch {
    Write-Host "✗ FAILED: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n[P3-02] Prometheus Export" -ForegroundColor Cyan
try {
    $m = Invoke-WebRequest -Uri "$BaseUrl/actuator/prometheus"
    if ($m.Content -notmatch "jvm_memory") { throw "Metrics missing" }
    Write-Host "✓ PASSED" -ForegroundColor Green
} catch {
    Write-Host "✗ FAILED: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n==========================================================" -ForegroundColor Yellow
Write-Host "   VERIFICATION COMPLETE                                  " -ForegroundColor Yellow
Write-Host "==========================================================" -ForegroundColor Yellow
