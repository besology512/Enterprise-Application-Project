# Design Notes - WorkHub SaaS Enterprise Architecture

This document outlines the technical design decisions and architecture patterns used in the WorkHub SaaS backend.

## 1. Multi-Tenant Architecture
We utilize a **Shared Database, Shared Schema** approach with discriminant `tenant_id` columns.

### Strict Isolation (Phase 2 Hardening)
- **Automatic Filtration**: We moved from manual service filtering to automated **Hibernate Filters** (`@FilterDef`).
- **Aspect-Oriented Activation**: A custom **`TenantAspect`** intercepts repository calls and enables the `tenantFilter` using the active `TenantContext`.
- **Zero-Leak Guarantee**: This ensures that even if a developer forgets a `where` clause in a new service, the Hibernate session itself remains bound to the tenant.

## 2. Asynchronous Messaging (Phase 2)
The application leverages **RabbitMQ** for decoupled, long-running processes:
- **Producer**: `ReportProducer` publishes messages to the `workhub.reports` queue.
- **Consumer**: `ReportConsumer` processes the data asynchronously.
- **Trace Propagation**: A custom header in the RabbitMQ message carries the `correlationId` from the originating HTTP request to the worker thread.

## 3. Distributed Tracing & Logging
- **Correlation ID**: Every request is assigned a `X-Correlation-ID` via `CorrelationFilter`.
- **MDC (Mapped Diagnostic Context)**: The ID is stored in the SLF4J MDC, enabling centralized log aggregation to trace a single request across the API and Messaging workers.

## 4. Concurrency Safety
- **Optimistic Locking**: The `Task` entity is protected by JPA **`@Version`**, preventing "Last-Write-Wins" data loss when multiple users edit the same task simultaneously.

## 5. Cloud-Native Readiness (Phase 3)
- **Kubernetes**: Orchestrated using a 3-replica **Deployment** with a **Service (LoadBalancer)**.
- **Self-Healing**: Configured with `Liveness` and `Readiness` probes to automatically restart failing pods.
- **Monitoring**: Real-time performance metrics are exported to **Prometheus** via Micrometer.

## 6. Security (JWT + RBAC)
- **Role-Based Access Control**: Granular endpoint protection using `@PreAuthorize` (e.g., `hasRole('ADMIN')`).
- **Stateless Tokens**: JWTs carry both user roles and the `tenantId` claim for high-performance authentication.
