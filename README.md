# WorkHub SaaS Enterprise Backend

[![Build Status](https://github.com/besology512/Enterprise-Application-Project/actions/workflows/ci-cd.yml/badge.svg)](https://github.com/besology512/Enterprise-Application-Project/actions)

WorkHub is a production-ready, multi-tenant project management SaaS backend built with **Java 17** and **Spring Boot 3.2.4**. It is designed for high scalability, strict tenant isolation, and cloud-native observability.

---

## 🚀 Project Status: FINAL (Phases 1-3 Complete)

This project has evolved through three major hardening phases, transitioning from a foundation to a cloud-ready enterprise system.

### Phase 1: Foundation Release
- **Layered Architecture**: Clean separation between controllers, services, and repositories.
- **JWT Authentication**: Stateless security with role-based access control (RBAC).
- **Multi-Tenancy**: Shared-database, shared-schema foundation.

### Phase 2: SaaS Hardening
- **Strict Tenant Isolation**: Automated protection via **Hibernate `@Filter`** and **AOP (`TenantAspect`)**. Zero-leak guarantee at the DB layer.
- **Messaging Workflows**: Integrated **RabbitMQ** for decoupled, asynchronous report generation.
- **Distributed Tracing**: Implementation of `CorrelationFilter` for end-to-end trace propagation (MDC).
- **Concurrency Safety**: Optimistic locking using JPA `@Version` on `Task` entities.

### Phase 3: Cloud-Native Delivery & IaC
- **Orchestration**: Production-grade **Kubernetes (K8s)** manifests for self-healing and scaling.
- **Observability**: Integrated **Prometheus** metrics export and custom health probes (Liveness/Readiness).
- **CI/CD**: Automated GitHub Actions pipeline for building and validating deployments.

---

## 🛠 Tech Stack
- **Languages**: Java 17, SQL.
- **Framework**: Spring Boot 3.2.4 (Security, Data JPA, AOP, AMQP).
- **Database**: PostgreSQL.
- **Messaging**: RabbitMQ.
- **Infrastructure**: Docker, Docker-Compose, Kubernetes (Minikube/Cloud).
- **Monitoring**: Spring Actuator, Prometheus, Micrometer.

---

## 📦 How to Run Locally

### Using Docker-Compose (Recommended)
The fastest way to start the entire stack (App, DB, RabbitMQ):
```bash
docker-compose up -d --build
```
The application will be available at `http://localhost:8081`.

### Pre-seeded Accounts
Two tenants are automatically seeded for testing:
- **Tenant A (Admin)**: `admin@tenantA.com` / `password`
- **Tenant B (Admin)**: `admin@tenantB.com` / `password`

---

## ✅ Verification & Auditing (Master Suite)

We provide a **Universal Production Verification Suite** that audits for data leaks, messaging health, and observability:

```powershell
# Run the audit against your local Docker-Compose setup
./verify-production.ps1 -BaseUrl "http://localhost:8081"
```

Refer to the [**DEPLOYMENT-GUIDE.md**](./DEPLOYMENT-GUIDE.md) for remote server auditing.

---

## 📂 Documentation Guides
- [**Deployment Guide**](./DEPLOYMENT-GUIDE.md): Transitioning to production servers and K8s.
- [**Tenant Isolation Proof**](./TENANT-ISOLATION-PROOF.md): How our AOP-based filter prevents data leaks.
- [**Observability Guide**](./OBSERVABILITY.md): How to use Prometheus and Correlation IDs.

---

## 👥 Team Collaboration
To maintain a clean workflow:
1. **`main` Branch**: Reserved for the foundational Spring Boot setup. NO Phase 1-3 code here.
2. **`bassam-learning` Branch**: Contains the complete, hardened implementation.
3. **New Features**: All team members should branch off `main` for their respective feature development.

---

## 📜 License
This project is for educational purposes at **Senior Year Enterprise Application Course**.
