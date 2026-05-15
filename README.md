# WorkHub SaaS - Enterprise Application Project

[![CI/CD Pipeline](https://github.com/besology512/Enterprise-Application-Project/actions/workflows/ci.yml/badge.svg)](https://github.com/besology512/Enterprise-Application-Project/actions/workflows/ci.yml)

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

---

## CI/CD & Deployment

### Pipeline Overview

Every push or pull request to `main` triggers a three-job GitHub Actions pipeline:

| Job | Trigger | What it does |
|-----|---------|-------------|
| **Build & Test** | push + PR | Compiles with Maven, runs all tests against **real** PostgreSQL + RabbitMQ service containers |
| **Docker Build & Push** | push to `main` only | Builds multi-stage Docker image, pushes to `ghcr.io` tagged `latest` + git SHA |
| **Security Scan** | push to `main` only | Trivy CVE scan on the published image; results uploaded to GitHub Security tab |

View pipeline runs: [Actions tab](https://github.com/besology512/Enterprise-Application-Project/actions)

Published images: `ghcr.io/besology512/enterprise-application-project`

---

### Docker Image (Multi-Stage Build)

The `Dockerfile` uses a two-stage build:
- **Stage 1 (builder)** — `eclipse-temurin:17-jdk-alpine`: runs `mvn package` inside the container
- **Stage 2 (runtime)** — `eclipse-temurin:17-jre-alpine`: copies only the JAR, runs as a non-root user

Build locally:
```bash
docker build -t workhub:local .
docker run -p 8080:8080 workhub:local
```

---

### 🟦🟩 Blue/Green Deployment

The Blue/Green strategy maintains two identical application slots (**blue** and **green**) behind an Nginx reverse proxy. Only one slot serves live traffic at a time; the idle slot is the instant rollback target.

```
Internet ──► Nginx :80 ──► [ACTIVE slot]
                        ╲─► [IDLE slot]  ← rollback target
```

#### Start the full Blue/Green stack

```bash
docker compose -f docker-compose.blue-green.yml up -d
```

Verify everything is healthy:
```bash
docker ps
curl -s http://localhost/actuator/health
```

#### Deploy a new image (zero-downtime)

```bash
# Replace <sha> with the Git commit short SHA from the Actions run
./scripts/deploy-blue-green.sh ghcr.io/besology512/enterprise-application-project:<sha>
```

The script:
1. Pulls the new image
2. Recreates only the **idle** slot container — live traffic is unaffected
3. Waits for the idle slot's `/actuator/health` to report `UP`
4. Updates the Nginx upstream config and reloads Nginx (graceful, zero-downtime)
5. Keeps the old slot running for 30 s as a rollback target

#### Instant rollback

```bash
./scripts/switch-traffic.sh rollback
```

#### Manual traffic switch (advanced)

```bash
./scripts/switch-traffic.sh   # auto-detects current slot and switches to the other
```
