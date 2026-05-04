# Strict Tenant Isolation Proof

This document outlines the strict tenant isolation mechanism implemented in the WorkHub SaaS application, along with the steps and expected results to verify its correctness.

## 1. Architectural Overview

The application enforces a **Siloed/Pool Data Model** where all tenants share the same database and tables, but every row is strictly associated with a specific `tenant_id`. Data access is horizontally partitioned at the application level.

### Key Components:
1. **`TenantFilter`**: Intercepts incoming HTTP requests, extracts the user's `tenant_id` (e.g., from the authenticated context or JWT token), and stores it in a thread-local context.
2. **`TenantContext`**: A `ThreadLocal` wrapper that makes the current `tenant_id` available anywhere within the current request thread without passing it explicitly through method parameters.
3. **Service Layer Enforcement**: Services are responsible for querying and associating data with the tenant. Controllers do not handle tenant logic.
4. **Repository Data Filtering**: All Spring Data JPA queries are strictly constrained by `tenantId`.

## 2. Evidence of Implementation

### Service Layer Centralization
In our services (e.g., `ProjectService`, `JobService`, `TaskService`), tenant context is applied natively.

**Example from `JobService.java`**:
```java
public Job createReportJob(Long projectId, JobRequest request) {
    String tenantId = TenantContext.getTenantId(); // Automatically resolved
    Job job = new Job();
    job.setTenantId(tenantId); // Forced tenant association on creation
    // ...
}

public Job getJobStatus(Long jobId) {
    String tenantId = TenantContext.getTenantId();
    // Strict lookup requiring both ID and Tenant ID
    return jobRepository.findByIdAndTenantId(jobId, tenantId)
            .orElseThrow(() -> new RuntimeException("Job not found or access denied"));
}
```

### Repository Level Constraints
Repositories do not allow global `findById` or `findAll`. They enforce tenant boundaries using composite queries.

**Example from `ProjectRepository.java`**:
```java
// equals SELECT * FROM projects WHERE tenant_id = ?
List<Project> findByTenantId(String tenantId);

// equals SELECT * FROM projects WHERE id = ? AND tenant_id = ?
Optional<Project> findByIdAndTenantId(Long id, String tenantId);
```

## 3. Verification Steps & Expected Results

To mathematically prove that strict tenant isolation is working, you can perform the following tests via Postman, cURL, or any API client.

### Prerequisites
Assume the system has two seeded tenants:
* **Tenant A** (Users: `user1@tenanta.com`)
* **Tenant B** (Users: `user2@tenantb.com`)

### Test Case 1: Data Creation Isolation
**Action**: 
1. Authenticate as `user1@tenanta.com` to receive `Token A`.
2. Send a `POST /projects` request using `Token A` to create a project named "Alpha Launch".
**Expected Result**: 
The database will record the project with `tenant_id` pointing to Tenant A. The user does not specify the tenant in the JSON payload; it is securely inferred by the backend context.

### Test Case 2: Data Retrieval Isolation (List)
**Action**:
1. Authenticate as `user2@tenantb.com` to receive `Token B`.
2. Send a `GET /projects` request using `Token B`.
**Expected Result**:
The response **must not** contain "Alpha Launch". The user will only see projects belonging to Tenant B. The SQL executed in the background will explicitly append `WHERE tenant_id = 'Tenant B'`.

### Test Case 3: Data Access Prevention (Direct ID Lookup)
**Action**:
1. Retrieve the exact Database ID of the "Alpha Launch" project created by Tenant A (e.g., ID `101`).
2. Using `Token B`, attempt to fetch the project directly via `GET /projects/101`.
**Expected Result**:
The application must return a **404 Not Found** (or Access Denied) error. Even though the project exists globally in the database, `ProjectRepository.findByIdAndTenantId(101, "Tenant B")` will return empty, preventing Cross-Tenant IDOR (Insecure Direct Object Reference).

### Test Case 4: Background Job Isolation
**Action**:
1. Tenant A triggers a report generation job via `POST /projects/101/generate-report` with `Token A`.
2. Tenant B attempts to view the job status via `GET /projects/jobs/{jobId}` using `Token B`.
**Expected Result**:
Tenant B receives an error (`Job not found or access denied`). The `ReportConsumer` processes messages and updates job status independently, but retrieval is strictly gated by the tenant ID bound to the consumer's request lookup.
