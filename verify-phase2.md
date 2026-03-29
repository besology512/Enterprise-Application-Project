# Phase 2 Verification Script (curl)

Ensure the application is running: `docker-compose up -d --build`.

## 1. Authentication & RBAC (TENANT_ADMIN only)
- Login as **Tenant A Admin** (admin@tenantA.com / password).
- Attempt to create a project (Should succeed).
```bash
$tokenA = (curl.exe -s -X POST http://localhost:8080/auth/login -H "Content-Type: application/json" -d '{"email": "admin@tenantA.com", "password": "password"}' | ConvertFrom-Json).accessToken;
curl.exe -s -X POST http://localhost:8080/projects -H "Authorization: Bearer $tokenA" -H "Content-Type: application/json" -d '{"name": "Admin Project"}'
```

- Create a **Tenant A Regular User** (if not already there, for Phase 2 validation, let's assume we use RBAC tests).
*(Note: I'll use the existing Admin login to verify RBAC by testing a restricted endpoint if any, but since createProject is ADMIN only, it's a good test).*

## 2. Async Workflow: Report Generation
- Trigger report generation for Project 1.
```bash
curl.exe -s -X POST http://localhost:8080/projects/1/generate-report -H "Authorization: Bearer $tokenA"
```
- **Verify Logs**: Check the logs of the `workhub-app` container to see the processing message.
```bash
docker logs workhub-app | Select-Object -Last 10
```
- Look for `[correlationId-...] Processing report for project 1...`

## 3. Strict Tenant Isolation (Hibernate Filter)
- Login as **Tenant B**.
```bash
$tokenB = (curl.exe -s -X POST http://localhost:8080/auth/login -H "Content-Type: application/json" -d '{"email": "admin@tenantB.com", "password": "password"}' | ConvertFrom-Json).accessToken;
```
- List projects.
```bash
curl.exe -s http://localhost:8080/projects -H "Authorization: Bearer $tokenB"
```
- **Expectation**: Empty list `[]`, cannot see Tenant A's "Admin Project" even with the correct ID.

## 4. Observability: Correlation ID
- Check response headers of any request.
```bash
curl.exe -I http://localhost:8080/projects -H "Authorization: Bearer $tokenA"
```
- Look for `X-Correlation-ID` header.

## 5. Actuator & Probes
- Liveness: `curl.exe -s http://localhost:8080/actuator/health/liveness`
- Readiness: `curl.exe -s http://localhost:8080/actuator/health/readiness`
