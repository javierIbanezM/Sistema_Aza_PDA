# Recipe: Desplegar Infraestructura AWS con Terraform

Aplica: [`../knowledge/terraform/terraform.md`](../knowledge/terraform/terraform.md), [`../knowledge/devops/infrastructure-as-code.md`](../knowledge/devops/infrastructure-as-code.md).

## Pasos

### 1. Backend remoto con bloqueo, antes de cualquier recurso

```hcl
terraform {
  backend "s3" {
    bucket         = "empresa-terraform-state"
    key            = "pedidos-service/terraform.tfstate"
    region         = "eu-west-1"
    dynamodb_table = "terraform-locks"
    encrypt        = true
  }
  required_providers {
    aws = { source = "hashicorp/aws", version = "~> 5.0" }  # versión fijada, no rango abierto
  }
}
```

### 2. Estructura modular

```
terraform/
├── modules/vpc-estandar/
├── modules/ecs-service/
└── environments/
    ├── staging/main.tf   (usa los módulos, parametrizado)
    └── production/main.tf
```

### 3. Variables con validación y documentación

```hcl
variable "environment" {
  type        = string
  description = "Entorno de despliegue"
  validation {
    condition     = contains(["staging", "production"], var.environment)
    error_message = "environment debe ser 'staging' o 'production'."
  }
}
```

### 4. Plan explícito, revisado antes de aplicar

```bash
terraform plan -var-file="environments/production.tfvars" -out=plan.tfplan
# revisar el diff: ¿algún "destroy and recreate" inesperado?
terraform apply plan.tfplan
```

Ver [`../knowledge/devops/infrastructure-as-code.md`](../knowledge/devops/infrastructure-as-code.md) — nunca `apply` directo sin revisar el plan en producción.

### 5. Integrar en el pipeline con aprobación

```yaml
  terraform-plan:
    runs-on: ubuntu-latest
    steps:
      - run: terraform plan -out=plan.tfplan
      - run: terraform show -no-color plan.tfplan > plan.txt
      # publicar plan.txt como comentario del PR para revisión humana

  terraform-apply:
    needs: terraform-plan
    environment: production  # aprobación requerida
    steps:
      - run: terraform apply plan.tfplan
```

### 6. Verificar el estado tras aplicar

```bash
terraform show | grep -A2 "aws_ecs_service"
aws ecs describe-services --cluster main --services pedidos-service
```

## Checklist final

- [ ] ¿El backend usa S3 + DynamoDB (o equivalente) con bloqueo, nunca estado local?
- [ ] ¿Las versiones de providers están fijadas, no en rango abierto?
- [ ] ¿El plan se revisa explícitamente (humano o en PR) antes de aplicar en producción?
- [ ] Ejecutar [`../checklists/architecture.md`](../checklists/architecture.md).

Ver también: [`../snippets/devops/terraform.tf`](../snippets/devops/terraform.tf), [`../recipes/aws-setup-vpc.md`](./aws-setup-vpc.md).
