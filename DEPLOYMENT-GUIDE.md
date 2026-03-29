# Production Deployment & Verification Guide

This guide explains how to deploy the WorkHub SaaS backend and verify its status on a remote production server.

## 1. Prerequisites
- Docker & Docker Compose installed on the server.
- Firewall rules allowing ingress on port `8081` (or your mapped port).

## 2. Deployment
1. Transfer the project files (or clone the repository).
2. Build and start the infrastructure:
   ```bash
   docker-compose up -d --build
   ```
3. Check logs to ensure clean startup:
   ```bash
   docker logs -f workhub-app
   ```

## 3. Remote Verification
To verify the deployment from your local machine, run the `verify-production.ps1` script pointing to the server's IP:

```powershell
./verify-production.ps1 -BaseUrl "http://[YOUR-SERVER-IP]:8081"
```

## 4. Troubleshooting
- **Messaging (P2-02) Fails**: Ensure the RabbitMQ container is `Healthy` in `docker-compose ps`.
- **Monitoring (P3-02) Fails**: Check `application.yml` for Prometheus enablement.
- **Port Conflicts**: If the server port is already in use, update the mapping in `docker-compose.yml`.

## 5. Kubernetes Transition
The `k8s/` directory contains ready-to-use manifests for transitioning to an orchestration platform once available. Use `kubectl apply -f k8s/` to deploy.
