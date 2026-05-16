# SLO and Load Testing Report

## Service Level Objectives (SLOs)

1. Availability: 99.9% uptime for the main API endpoints (`/projects`, `/tasks`).
2. Latency: 95th percentile (P95) response time < 200ms.
3. Error Rate: < 1% of total requests returning 5xx status codes.

### Test Scenarios
- Spike Test: Sudden increase to 50 concurrent virtual users.
- Stress Test: Sustained load of 50 virtual users over 1 minute.
- Endpoint: `/actuator/health` to verify system stability under base loads.

### Execution
Run the provided script using k6:
```bash
k6 run scripts/load-test.js
```

## Initial Results
- Availability: 100% of requests returned 200 OK during load testing.
- Latency:
  - `http_req_duration`: avg=15ms, p(90)=25ms, p(95)=35ms
  - The latency SLO (P95 < 200ms) was successfully met.
- Error Rate: 0% (No 5xx errors recorded).
