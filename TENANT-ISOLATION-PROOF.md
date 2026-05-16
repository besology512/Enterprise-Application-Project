# Tenant Isolation Proof

## 1. Multi-Tenant Isolation

### Steps to Verify
1. **Database Partitioning**: Ensure all entities have a `tenantId` field and repositories use it to filter queries.
2. **Context Injection**: Use `TenantFilter` to extract `tenantId` from JWT and set it in `TenantContext`.
3. **Automated Testing**: Run `TenantIsolationIntegrationTest` to verify that:
    - Users can't read projects/tasks belonging to other tenants (Returns 404).
    - Listing operations are filtered to only show resources belonging to the authenticated tenant.

### Expected Results
- Cross-tenant access attempts return `404 Not Found`.
- Resource lists are partitioned by `tenantId`.

### Evidence
```text
[INFO] Running com.workhub.controller.TenantIsolationIntegrationTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 15.56 s
[INFO] BUILD SUCCESS
```