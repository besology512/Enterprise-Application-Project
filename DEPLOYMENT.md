# Deployment Guide

This application can be deployed using three distinct methods: Local Docker Compose, Kubernetes manifests, and Terraform via Minikube (Free Deployment Option).

## 1. Local Docker Compose (Minimum Requirement)

### Prerequisites
- Docker & Docker Compose installed

### Steps
1. Navigate to the project root directory.
2. Build and start the containers in detached mode:
   ```bash
   docker-compose up -d --build
   ```
3. Verify the services are running:
   ```bash
   docker-compose ps
   ```
4. Access the application at `http://localhost:8081` (The API port mapped in docker-compose.yml).
5. To stop the application:
   ```bash
   docker-compose down
   ```

## 2. Kubernetes Manifests

### Prerequisites
- A running Kubernetes cluster (e.g., Docker Desktop with Kubernetes, Minikube, or kind)
- `kubectl` configured to communicate with your cluster
- PostgreSQL and RabbitMQ accessible by the application (either inside or outside the cluster).

### Steps
1. Navigate to the `k8s` directory.
   ```bash
   cd k8s
   ```
2. Apply the ConfigMap and Secret:
   ```bash
   kubectl apply -f configmap.yaml
   kubectl apply -f secret.yaml
   ```
3. Apply the Deployment and Service:
   ```bash
   kubectl apply -f deployment.yaml
   kubectl apply -f service.yaml
   ```
4. Verify the deployment:
   ```bash
   kubectl get pods
   kubectl get svc workhub-service
   ```
5. Depending on your Kubernetes setup, access the NodePort service (typically on port `30080` of your cluster node IP).

## 3. Local Kubernetes (Minikube/Kind) + Terraform Kubernetes Provider

### Prerequisites
- Terraform (>= 1.5.0) installed
- Minikube or Kind cluster running
- `kubectl` context set to the active cluster (`~/.kube/config`)

### Steps
1. Navigate to the `terraform` directory:
   ```bash
   cd terraform
   ```
2. Initialize Terraform:
   ```bash
   terraform init
   ```
3. Review the execution plan:
   ```bash
   terraform plan
   ```
4. Apply the configuration to create the resources:
   ```bash
   terraform apply
   ```
   *Type `yes` when prompted to confirm.*
5. Retrieve outputs (e.g., namespace, service name):
   ```bash
   terraform output
   ```
6. Access the application. If using Minikube, you can expose the NodePort service:
   ```bash
   minikube service workhub-service -n workhub --url
   ```
7. To clean up and destroy the resources:
   ```bash
   terraform destroy
   ```
