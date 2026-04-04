# WorkHub SaaS - Enterprise Application Project

## Team Project Overview
This repository is for the WorkHub SaaS backend, an enterprise-grade project management application.

## Repository Structure
- **main**: Core Spring Boot setup and baseline configuration. (Clean Slate)
- **bassam-learning**: Bassam's implementation of Phase 1 and 2 (Hardening, Multi-tenancy, Messaging).
- **feat/[name]**: Feature branches for other team members.

## 🚀 Phase 1: Setup & Identity (Complete)
The foundational skeleton and authentication system are now implemented on the `main` branch.

### Key Features
- **Multi-tenancy Support**: `Tenant` and `User` domain models with identity isolation.
- **JWT Authentication**: Secure login and profile access using JSON Web Tokens.
- **In-Memory H2 Database**: Configured for rapid development and verification.
- **Automatic Data Seeding**: Initial tenants and users are created automatically on startup.

### 📚 API Documentation (OpenAPI / Swagger)
The project is fully documented using OpenAPI 3. You can explore the API using the interactive Swagger UI:

- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI JSON Docs**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

### 🧪 Test Accounts
| Email | Password | Role | Tenant |
|-------|----------|------|--------|
| `admin@tenantA.com` | `password` | `TENANT_ADMIN` | Tenant A (FREE) |
| `user@tenantA.com` | `password` | `TENANT_USER` | Tenant A (FREE) |
| `admin@tenantB.com` | `password` | `TENANT_ADMIN` | Tenant B (PREMIUM) |

## How to run locally
1. Clone the repository.
```bash
git clone https://github.com/besology512/Enterprise-Application-Project.git
```
2. Build the application archive: 
```bash
mvn clean package -DskipTests
```
3. Ensure ports availability: Ensure that no local instance of PostgreSQL is running on port 5432

4. Run with docker compose:
```
docker-compose up --build
```
5. Verify that the app is running via Postman by testing the endpoints. (base URL: localhost:8081)