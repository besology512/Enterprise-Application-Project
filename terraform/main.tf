terraform {
  required_providers {
    kubernetes = {
      source  = "hashicorp/kubernetes"
      version = "~> 2.23"
    }
  }
}

provider "kubernetes" {
  config_path = "~/.kube/config"
}

resource "kubernetes_namespace" "workhub" {
  metadata {
    name = var.namespace
  }
}

resource "kubernetes_config_map" "workhub_config" {
  metadata {
    name      = "workhub-config"
    namespace = kubernetes_namespace.workhub.metadata[0].name
  }

  data = {
    SPRING_PROFILES_ACTIVE = "prod"
    SPRING_DATASOURCE_URL  = "jdbc:postgresql://workhub-db:5432/workhub"
    SPRING_RABBITMQ_HOST   = "workhub-rabbitmq"
    SPRING_RABBITMQ_PORT   = "5672"
  }
}

resource "kubernetes_secret" "workhub_secret" {
  metadata {
    name      = "workhub-secret"
    namespace = kubernetes_namespace.workhub.metadata[0].name
  }

  type = "Opaque"
  data = {
    SPRING_DATASOURCE_USERNAME = "postgres"
    SPRING_DATASOURCE_PASSWORD = "password"
    SPRING_RABBITMQ_USERNAME   = "guest"
    SPRING_RABBITMQ_PASSWORD   = "guest"
    WORKHUB_JWT_SECRET         = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970"
  }
}

resource "kubernetes_deployment" "workhub_app" {
  metadata {
    name      = "workhub-app"
    namespace = kubernetes_namespace.workhub.metadata[0].name
    labels = {
      app = "workhub"
    }
  }

  spec {
    replicas = var.app_replicas
    selector {
      match_labels = {
        app = "workhub"
      }
    }
    template {
      metadata {
        labels = {
          app = "workhub"
        }
      }
      spec {
        container {
          name              = "workhub-app"
          image             = "workhub-app:${var.image_tag}"
          image_pull_policy = "IfNotPresent"

          port {
            container_port = 8080
          }

          env_from {
            config_map_ref {
              name = kubernetes_config_map.workhub_config.metadata[0].name
            }
          }
          env_from {
            secret_ref {
              name = kubernetes_secret.workhub_secret.metadata[0].name
            }
          }

          liveness_probe {
            http_get {
              path = "/actuator/health/liveness"
              port = 8080
            }
            initial_delay_seconds = 60
            period_seconds        = 15
          }

          readiness_probe {
            http_get {
              path = "/actuator/health/readiness"
              port = 8080
            }
            initial_delay_seconds = 60
            period_seconds        = 10
          }
        }
      }
    }
  }
}

resource "kubernetes_service" "workhub_app" {
  metadata {
    name      = "workhub-service"
    namespace = kubernetes_namespace.workhub.metadata[0].name
  }

  spec {
    type = "NodePort"
    selector = {
      app = "workhub"
    }
    port {
      port        = 8080
      target_port = 8080
      node_port   = 30080
    }
  }
}
