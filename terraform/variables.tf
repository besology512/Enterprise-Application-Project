variable "namespace" {
  type        = string
  description = "Kubernetes namespace to deploy into"
  default     = "workhub"
}

variable "app_replicas" {
  type        = number
  description = "Number of application replicas"
  default     = 1
}

variable "image_tag" {
  type        = string
  description = "Docker image tag for the application"
  default     = "latest"
}
