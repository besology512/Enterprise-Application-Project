# WorkHub Test Plan

This file explains the automated tests:
 **Enterprise Testing & Reliability**.

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

In GitHub Actions, the CI pipeline runs tests before building the Docker image. 

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

## Bonus: Outbox Pattern

I also added the **outbox pattern** for the bonus requirement.

Before this, `JobService` saved the report job and immediately tried to publish to RabbitMQ. That is risky because the database save can succeed while RabbitMQ publishing fails.

Now the flow is:

1. Save the report job.
2. Save an `outbox_messages` row in the same database transaction.
3. A scheduled publisher reads pending outbox rows.
4. The publisher sends the saved payload to RabbitMQ.
5. If publishing works, the row becomes `PUBLISHED`.
6. If publishing fails, the row stays `PENDING` and records the error so it can be retried.

| Bonus area | What we prove | Test class |
|---|---|---|
| Outbox producer reliability | Creating a report job stores a pending outbox message in the same database transaction. | `OutboxPatternIntegrationTest` |
| Outbox publish success | The publisher sends a pending message to RabbitMQ and marks it `PUBLISHED`. | `OutboxPatternIntegrationTest` |
| Outbox retry behavior | If RabbitMQ publishing fails, the message stays `PENDING` for retry. | `OutboxPatternIntegrationTest` |

## Expected Result

A successful local run should end with:

```text
Tests run: 32, Failures: 0, Errors: 0
BUILD SUCCESS
```

One RabbitMQ Testcontainers test may be skipped locally if Docker is not available to Maven. The non-skipped idempotency test still proves the messaging reliability behavior.

