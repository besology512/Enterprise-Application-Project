# Phase 2 Final Verification Script (Windows PowerShell)

# 1. Login Tenant A
$loginA = curl.exe -s -X POST http://localhost:8081/auth/login -H "Content-Type: application/json" -d '{\"email\": \"admin@tenantA.com\", \"password\": \"password\"}';
$tokenA = ($loginA | ConvertFrom-Json).accessToken;
echo "Tenant A Admin Token acquired.";

# 2. Create Project
$projA = curl.exe -s -X POST http://localhost:8081/projects -H "Authorization: Bearer $tokenA" -H "Content-Type: application/json" -d '{\"name\": \"PH2 Final Project\"}';
$projectId = ($projA | ConvertFrom-Json).id;
echo "Created Project ID: $projectId";

# 3. Trigger Async Report
echo "Triggering Async Report Generation...";
$tempFile = Join-Path $env:TEMP "curl_out.txt";
curl.exe -s -v -X POST http://localhost:8081/projects/$projectId/generate-report -H "Authorization: Bearer $tokenA" 2>&1 | Out-File -FilePath $tempFile;
echo "Response saved to $tempFile";

# 4. Wait and Verify
echo "Waiting for consumer...";
Start-Sleep -Seconds 5;
echo "--- Logs Snippet ---";
docker logs workhub-app | Select-String "Processing report" | Select-Object -Last 1;
echo "`n--- Header check ---";
Get-Content $tempFile | Select-String "X-Correlation-ID";

# 5. Tenant Isolation
$loginB = curl.exe -s -X POST http://localhost:8081/auth/login -H "Content-Type: application/json" -d '{\"email\": \"admin@tenantB.com\", \"password\": \"password\"}';
$tokenB = ($loginB | ConvertFrom-Json).accessToken;
echo "Tenant B Projects (Strict):";
curl.exe -s http://localhost:8081/projects -H "Authorization: Bearer $tokenB";
