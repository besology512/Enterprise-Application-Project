# DESIGN-NOTES: WorkHub SaaS Foundation

## Architecture Overview
The project follows a standard Spring Boot layered architecture:
- **Web Layer**: REST Controllers handling HTTP requests and DTO mapping.
- **Service Layer**: Business logic and transaction boundaries.
- **Repository Layer**: Data access using Spring Data JPA.
- **Security Layer**: JWT-based authentication and role-based access control.

## Multi-Tenancy Strategy
We use a **Shared Database, Shared Schema** approach with a `tenant_id` column for isolation.
- **Tenant Context**: A `TenantContext` utility using `ThreadLocal` holds the `tenantId` for the duration of the request.
- **Extraction**: A `TenantFilter` interceptor extracts the `tenantId` claim from the JWT and populates the `TenantContext`.
- **Isolation**: Every query and write in the repository/service layer explicitly filters by the `tenantId` from the context. (Note: Phase 2 will introduce more automated filtering).

## Transaction Management
Transactional integrity is managed via Spring's `@Transactional` annotation.
- **Use Case**: `ProjectService.createProjectWithTasks` handles project creation and multiple task insertions in a single transaction.
- **Rollback**: If any part of the process fails (e.g., a "fail" task title), the entire transaction is rolled back, ensuring no partial data (like a project without its tasks) is left in the database.

## Security and RBAC
- **JWT**: Stateless authentication using JSON Web Tokens.
- **Claims**: The JWT contains a `tenantId` claim to ensure the user is bound to their organization.
- **Roles**: Supports `TENANT_ADMIN` and `TENANT_USER`. Authorization is enforced via Spring Security configuration.

## Error Handling
Consistent error responses are provided via a `GlobalExceptionHandler` using `@ControllerAdvice`. It captures `MethodArgumentNotValidException` for DTO validation and provides a structured JSON response with field-level error details.
