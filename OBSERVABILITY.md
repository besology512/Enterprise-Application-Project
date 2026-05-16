# Observability

This backend exposes Spring Boot Actuator health and metrics endpoints, plus request correlation IDs in logs.

## Endpoints

| Endpoint | Purpose |
| --- | --- |
| `/actuator/health` | Overall application health. Expected status is `UP`. |
| `/actuator/prometheus` | Prometheus-formatted Micrometer metrics. |
| `/actuator/health/readiness` | Readiness probe for checking whether the app can receive traffic. |
| `/actuator/health/liveness` | Liveness probe for checking whether the app process is alive. |
| `http://localhost:16686` | Jaeger UI for viewing distributed traces. |

The Actuator health and metrics endpoints are public so monitoring tools can call them without a JWT. Business endpoints remain protected by the existing security rules.

RabbitMQ health is disabled for now because messaging infrastructure is owned by the messaging deliverable and RabbitMQ is not currently defined in `docker-compose.yml`. This keeps `/actuator/health` focused on infrastructure that exists in this phase.

## Run The App

With Docker:

```powershell
docker-compose up -d --build
```



## Verify Health And Metrics

```powershell
curl.exe http://localhost:8080/actuator/health
curl.exe http://localhost:8080/actuator/health/readiness
curl.exe http://localhost:8080/actuator/health/liveness
curl.exe http://localhost:8080/actuator/metrics
curl.exe http://localhost:8080/actuator/prometheus
```

Expected results:

- `/actuator/health` returns HTTP `200 OK` with `"status":"UP"`.
- Readiness and liveness endpoints return HTTP `200 OK`.
- `/actuator/metrics` returns the available Micrometer metric names.
- `/actuator/prometheus` returns Prometheus text output.

## Verify Correlation IDs

The app reads `X-Correlation-ID` from each request. If the header is missing, the app generates one. The same value is added to the response and written into logs through the `correlationId` MDC field.

Check the response header:

```powershell
curl.exe -I http://localhost:8080/actuator/health
```

Send a custom correlation ID:

```powershell
curl.exe -I http://localhost:8080/actuator/health -H "X-Correlation-ID: demo-correlation-id"
```

Expected result:

X-Correlation-ID: demo-correlation-id
```

## Verify Tracing (Jaeger)

1. Start the infrastructure: `docker-compose up -d`
2. Send some requests to the app (e.g., call `/actuator/health`).
3. Open Jaeger UI at `http://localhost:16686`.
4. Select `workhub-saas` service and click "Find Traces".
5. You should see traces for the requests you made.

Check logs when running with Docker:

```powershell
docker logs workhub-app
```

The console log pattern includes the MDC value with `%X{correlationId:-}`, so request logs should show the correlation ID in square brackets.

## Automated Test

Run:

```powershell
mvn test
```

The integration test verifies:

- `/actuator/health` returns `200 OK`.
- `/actuator/health` contains status `UP`.
- `/actuator/health/readiness` is accessible.
- `/actuator/health/liveness` is accessible.
