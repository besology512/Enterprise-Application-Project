# Tenant Isolation Proof - WorkHub SaaS

## Strategy: Hibernate Filters + AOP
We have moved from manual filtering in the service layer to automated strict isolation using Hibernate Filters and Aspect-Oriented Programming (AOP).

1. **Hibernate Filter Definition**: Entities (`Project`, `Task`) are annotated with `@FilterDef` and `@Filter`, restricting results by `tenant_id`.
2. **Automated Activation**: `TenantAspect` interceptes all repository calls (AOP) and enables the filter using the `tenantId` from the `TenantContext`.
3. **Double Guard**: The `TenantFilter` ensures that every request has a valid `tenantId` extracted from the JWT.

## Evidence of Isolation
- **Read/List Isolation**: Any `findAll()` call automatically appends `AND tenant_id = ?` to the SQL query.
- **Write Isolation**: The AOP aspect ensures that the session is filtered before any repository interaction.
- **Cross-Tenant Prevention**: If Tenant A tries to access `GET /projects/{id_of_tenant_b}`, the Hibernate filter will result in zero records found, returning a 404/Null effectively at the data layer level.

## Verification
You can verify this isolation using the **Master Production Verification Suite**:

```powershell
# This script specifically checks for cross-tenant leaks (P2-01)
./verify-production.ps1
```

Or manually:
1. Create a project `Alpha` under Tenant A.
2. Attempt to list projects with Tenant B's JWT.
3. Result: Empty list `[]` (Verified via Phase 2 integration tests).
