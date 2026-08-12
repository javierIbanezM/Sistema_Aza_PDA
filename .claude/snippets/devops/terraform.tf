# snippets/devops/terraform.tf
# Backend remoto + módulo reutilizable + variables validadas — ver recipes/terraform-deploy-aws.md.

terraform {
  backend "s3" {
    bucket         = "empresa-terraform-state"
    key            = "pedidos-service/terraform.tfstate"
    region         = "eu-west-1"
    dynamodb_table = "terraform-locks"
    encrypt        = true
  }
  required_providers {
    aws = { source = "hashicorp/aws", version = "~> 5.0" }
  }
}

variable "environment" {
  type        = string
  description = "Entorno de despliegue (staging|production)"
  validation {
    condition     = contains(["staging", "production"], var.environment)
    error_message = "environment debe ser 'staging' o 'production'."
  }
}

module "vpc" {
  source   = "./modules/vpc-estandar"
  cidr     = var.environment == "production" ? "10.0.0.0/16" : "10.1.0.0/16"
  az_count = 3
  proyecto = "pedidos"
}

module "ecs_service" {
  source          = "./modules/ecs-service"
  service_name    = "pedidos-service"
  vpc_id          = module.vpc.vpc_id
  private_subnets = module.vpc.private_subnet_ids
  image_tag       = var.image_tag
  desired_count   = var.environment == "production" ? 4 : 2
}

output "service_url" {
  value       = module.ecs_service.load_balancer_dns
  description = "URL del servicio desplegado"
}
