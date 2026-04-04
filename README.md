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

### 🛠️ Getting Started
1. Clone the repository.
2. Build and run the project using Maven:
   ```bash
   mvn clean spring-boot:run
   ```
3. Use the **Swagger "Authorize"** button to paste your Bearer token and test protected endpoints like `/auth/me`.

