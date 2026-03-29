# Verification Script (Postman/curl)

## 1. Login (Tenant A)
```bash
curl -X POST http://localhost:8080/auth/login \
-H "Content-Type: application/json" \
-d '{"email": "admin@tenantA.com", "password": "password"}'
```
*Note: Save the `accessToken` from the response.*

## 2. Get Identity
```bash
curl -X GET http://localhost:8080/auth/me \
-H "Authorization: Bearer <TOKEN>"
```

## 3. Create Project (Success)
```bash
curl -X POST http://localhost:8080/projects \
-H "Authorization: Bearer <TOKEN>" \
-H "Content-Type: application/json" \
-d '{"name": "Project Alpha", "initialTasks": ["Design API", "Fix Auth"]}'
```

## 4. List Projects
```bash
curl -X GET http://localhost:8080/projects \
-H "Authorization: Bearer <TOKEN>"
```

## 5. Transaction Rollback (Failure)
```bash
curl -X POST http://localhost:8080/projects \
-H "Authorization: Bearer <TOKEN>" \
-H "Content-Type: application/json" \
-d '{"name": "Ghost Project", "initialTasks": ["Task 1", "fail"]}'
```
*Observe the 500 error. Re-run Step 4 to verify "Ghost Project" was NOT created.*

## 6. Tenant Isolation Check
- Login as `admin@tenantB.com`.
- List projects (Step 4).
- Verify that "Project Alpha" from Tenant A is NOT visible.
```bash
curl -X POST http://localhost:8080/auth/login \
-H "Content-Type: application/json" \
-d '{"email": "admin@tenantB.com", "password": "password"}'
```
*Use the new TOKEN to list projects.*
