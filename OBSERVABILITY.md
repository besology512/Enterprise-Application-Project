# Observability - WorkHub SaaS

## 1. Spring Actuator & Cloud Probes
All required observability endpoints are enabled for orchestration:
- **Health**: `/actuator/health` (aggregated status of DB and RabbitMQ).
- **Liveness**: `/actuator/health/liveness` (Self-healing indicator).
- **Readiness**: `/actuator/health/readiness` (Traffic routing indicator).

## 2. Distributed Tracing (Correlation ID)
Every HTTP request is assigned a unique `X-Correlation-ID`.
- **Generation**: `CorrelationFilter` generates a UUID and injects it into MDC.
- **Propagation**: The ID is propagated from the REST controller to the **RabbitMQ Consumer** for asynchronous worker tracing.
- **Logging**: The ID is included in every log line via the `%X{correlationId}` pattern.

Example Log:
```text
2026-03-29 18:42:07 INFO  [...] [req-abcd-1234] Processing report for project 1
```

## 3. Performance Monitoring (Prometheus)
The application exports real-time performance metrics to **Prometheus**:
- **Endpoint**: `/actuator/prometheus`
- **Metrics**: JVM memory, CPU usage, HTTP request latencies, and Hibernate statistics.

## 4. Verification
Run the **Master Production Verification Suite** to check all probes and metrics:

```powershell
./verify-production.ps1
```
Alternatively, hit `http://localhost:8081/actuator/prometheus` to view raw metrics.
