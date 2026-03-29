# WorkHub SaaS Backend - Phase 1 Foundation

WorkHub is a multi-tenant project management SaaS backend built with Spring Boot.

## Features (Phase 1)
- **Clean Layering**: controller -> service -> repository.
- **JWT Authentication**: Login and identity retrieval.
- **Tenant Isolation**: Automatic tenant context extraction from JWT.
- **Transactional Rollback**: Demonstrated in project creation.
- **DTO Validation**: Standardized error responses.
- **Observability**: Spring Actuator enabled.

## How to Run Locally

### Prerequisites
- Java 17+
- Maven
- PostgreSQL (or use H2 for quick testing by changing `application.yml`)

### Steps
1. **Clone the repository**.
2. **Configure Database**: Update `src/main/resources/application.yml` with your PostgreSQL credentials.
3. **Build and Run**:
   ```bash
   mvn spring-boot:run
   ```
4. **Initial Data**: The application automatically seeds two tenants and two admin users:
   - **Tenant A**: `admin@tenantA.com` / `password`
   - **Tenant B**: `admin@tenantB.com` / `password`

## API Endpoints

### Auth
- `POST /auth/login`: Authenticate and get JWT + tenantId.
- `GET /auth/me`: Get current user details.

### Projects
- `POST /projects`: Create a project (and optionally initial tasks).
- `GET /projects`: List all projects for the current tenant.
- `GET /projects/{id}`: Get project by ID (tenant-isolated).

### Tasks
- `PATCH /tasks/{id}?status=DONE`: Update task status.

## Transaction Rollback Demonstration
To see the transaction rollback in action:
- Send a `POST` request to `/projects` with an initial task named `"fail"`.
- Example Request:
  ```json
  {
    "name": "Failing Project",
    "initialTasks": ["task1", "fail"]
  }
  ```
- The API will return a 500 error.
- Check the database: no "Failing Project" will be created, demonstrating that the entire operation rolled back.

## Observability
- Health: `http://localhost:8080/actuator/health`
- Metrics: `http://localhost:8080/actuator/metrics`
