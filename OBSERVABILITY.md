# Observability - WorkHub SaaS

## 1. Spring Actuator
All required observability endpoints are enabled:
- **Health**: `/actuator/health` (with database and rabbitmq status).
- **Probes**: `/actuator/health/liveness` and `/actuator/health/readiness`.
- **Metrics**: `/actuator/metrics`.

## 2. Distributed Tracing (Correlation ID)
Every HTTP request is assigned a `X-Correlation-ID`.
- **Generation**: `MdcInterceptor` generates a UUID if the header is missing.
- **Propagation**: The ID is added to the response header.
- **Logging**: The ID is stored in MDC and included in every log line via `logback-spring.xml`.

Example Log:
```text
2026-03-29 18:42:07 INFO  [...] [req-abcd-1234] Report generation triggered for project 1
```

## 3. Verification
- Hit any endpoint and check the `X-Correlation-ID` response header.
- Check the application logs in the `workhub-app` container to see the correlation ID between brackets in the log pattern.
