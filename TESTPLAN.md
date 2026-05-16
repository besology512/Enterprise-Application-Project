# WorkHub Test Plan

This file explains the automated tests for my part: **Enterprise Testing & Reliability**.

The main goal is simple: prove that the important enterprise features work, and prove that the tests run in CI.

## How to Run the Tests

For local testing, run:

```powershell
mvn test "-Dspring.profiles.active=test"
```

For the full Maven verification step, run:

```powershell
mvn verify "-Dspring.profiles.active=test"
```

In GitHub Actions, the CI pipeline runs tests before building the Docker image. This is important because the project document says the Phase 3 CI score becomes zero if integration tests do not run in CI.

## What the Tests Cover

| Area | What we prove | Test class |
|---|---|---|
| Tenant isolation | One tenant cannot read, update, or list another tenant's data. Cross-tenant access returns `404`. | `TenantIsolationIntegrationTest` |
| RBAC | Missing token returns `401`, wrong role returns `403`, and admins can use admin endpoints. | `RBACIntegrationTest` |
| Transaction rollback | If creating a project with tasks fails halfway, the database keeps no partial project or task rows. | `TransactionRollbackIntegrationTest` |
| Concurrency | Two users updating the same task at the same time cannot silently overwrite each other. | `TaskConcurrencyIntegrationTest` |
| Messaging reliability | Duplicate report messages are ignored using the `processed_messages` table. | `ReportConsumerIdempotencyIntegrationTest` |
| RabbitMQ integration | The report message can also be tested through RabbitMQ/Testcontainers when Docker is available. | `MessagingReliabilityIntegrationTest` |
| Observability | Health, readiness, liveness, Prometheus metrics, and correlation IDs are available. | `ActuatorEndpointsIntegrationTest` |

## Notes for Defense

- Tenant isolation returns `404` instead of `403` so users cannot even know another tenant's resource exists.
- Concurrency is protected with JPA optimistic locking using `@Version` on `Task`.
- A stale concurrent update returns `409 Conflict`.
- Messaging reliability uses an idempotency record in `processed_messages`, so duplicate messages do not process the same report twice.
- The tests are integration tests because they start Spring and test real controllers, services, repositories, transactions, and filters.

## Expected Result

A successful local run should end with:

```text
Tests run: 29, Failures: 0, Errors: 0
BUILD SUCCESS
```

One RabbitMQ Testcontainers test may be skipped locally if Docker is not available to Maven. The non-skipped idempotency test still proves the messaging reliability behavior.
