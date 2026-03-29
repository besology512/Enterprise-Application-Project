$ErrorActionPreference = "Stop"

$BASE_URL = "http://localhost:8081"
$JWT_SECRET = "NDA0RTYzNTI2NjU1Nko1WDZOMnIyNTd1OHg4MkY0MTNGNDQyODQ3MkI0QjYyNTA2NDUzNjc1NjZCNTk3MA=="

function Test-Feature {
    param($Name, $ScriptBlock)
    Write-Host "`n[Testing Feature: $Name]" -ForegroundColor Cyan
    try {
        & $ScriptBlock
        Write-Host "SUCCESS" -ForegroundColor Green
    } catch {
        Write-Host "FAILED: $($_.Exception.Message)" -ForegroundColor Red
        exit 1
    }
}

# 1. Auth & Identity
Test-Feature "JWT Login & /auth/me" {
    $loginBody = @{ email = "admin@tenantA.com"; password = "password" } | ConvertTo-Json
    $loginResp = Invoke-RestMethod -Uri "$BASE_URL/auth/login" -Method Post -Body $loginBody -ContentType "application/json"
    $token = $loginResp.token
    
    $headers = @{ Authorization = "Bearer $token" }
    $meResp = Invoke-RestMethod -Uri "$BASE_URL/auth/me" -Method Get -Headers $headers
    
    if ($meResp.email -ne "admin@tenantA.com") { throw "Identity mismatch" }
    if ($meResp.tenantId -ne 1) { throw "Tenant ID mismatch" }
    $Global:TokenA = $token
}

# 2. Multi-tenancy Isolation
Test-Feature "Strict Tenant Isolation (Filter/AOP)" {
    $headers = @{ Authorization = "Bearer $Global:TokenA" }
    
    # Create project for Tenant A
    $projBody = @{ name = "Cloud Project A"; description = "K8s Managed" } | ConvertTo-Json
    $projResp = Invoke-RestMethod -Uri "$BASE_URL/projects" -Method Post -Body $projBody -ContentType "application/json" -Headers $headers
    $projId = $projResp.id
    
    # Try to access as Tenant B (Login first)
    $loginBBody = @{ email = "admin@tenantB.com"; password = "password" } | ConvertTo-Json
    $loginBResp = Invoke-RestMethod -Uri "$BASE_URL/auth/login" -Method Post -Body $loginBBody -ContentType "application/json"
    $tokenB = $loginBResp.token
    
    try {
        Invoke-RestMethod -Uri "$BASE_URL/projects/$projId" -Method Get -Headers @{ Authorization = "Bearer $tokenB" }
        throw "LEAK DETECTED: Tenant B accessed Tenant A's project!"
    } catch [System.Net.WebException] {
        if ($_.Exception.Response.StatusCode -eq 404) {
            Write-Host "Isolation Verified: Tenant B sees 404 for Tenant A's project."
        } else {
            throw "Unexpected status: $($_.Exception.Response.StatusCode)"
        }
    }
}

# 3. Async Workflows (RabbitMQ)
Test-Feature "Async Reporting & Messaging" {
    $headers = @{ Authorization = "Bearer $Global:TokenA" }
    Invoke-RestMethod -Uri "$BASE_URL/projects/1/report" -Method Post -Headers $headers
    Write-Host "Report triggered. Waiting for worker logs..."
    Start-Sleep -Seconds 5
    $logs = kubectl logs -l app=workhub-app --tail 50
    if ($logs -match "Processing report for project 1") {
        Write-Host "RabbitMQ Consumer verified: Job processed."
    } else {
        throw "Report processing log not found."
    }
}

# 4. Observability
Test-Feature "Observability (Probes & Prometheus)" {
    $health = Invoke-RestMethod -Uri "$BASE_URL/actuator/health"
    if ($health.status -ne "UP") { throw "Health is Down" }
    
    $prometheus = Invoke-RestMethod -Uri "$BASE_URL/actuator/prometheus"
    if ($prometheus -notmatch "jvm_memory_used_bytes") { throw "Prometheus metrics missing" }
}

Write-Host "`n========================================" -ForegroundColor Yellow
Write-Host "ALL FEATURES VERIFIED ON MINIKUBE CLUSTER" -ForegroundColor Yellow
Write-Host "========================================`n" -ForegroundColor Yellow
