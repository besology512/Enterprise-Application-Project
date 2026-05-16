## Files
- `main.tf`: Contains the Kubernetes provider configuration and resource definitions (Namespace, ConfigMap, Secret, Deployment, Service).
- `variables.tf`: Input variables for namespace, replicas, and image tag.
- `outputs.tf`: Outputs exposed after apply.
- `terraform.tfvars.example`: Example values for variables.

## How to use

1. **Initialize Terraform:**
   Downloads required providers (kubernetes).
   ```bash
   terraform init
   ```

2. **Plan infrastructure:**
   See what Terraform will create.
   ```bash
   terraform plan
   ```

3. **Apply changes:**
   Deploy the resources to your Kubernetes cluster.
   ```bash
   terraform apply
   ```

4. **Verify Deployment:**
   ```bash
   kubectl get all -n workhub
   ```

5. **Destroy:**
   When done, you can clean up the resources.
   ```bash
   terraform destroy
   ```
