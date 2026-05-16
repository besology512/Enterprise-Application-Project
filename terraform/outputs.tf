output "namespace" {
  value       = kubernetes_namespace.workhub.metadata[0].name
  description = "The namespace the app is deployed in"
}

output "service_name" {
  value       = kubernetes_service.workhub_app.metadata[0].name
  description = "The name of the Kubernetes service"
}
