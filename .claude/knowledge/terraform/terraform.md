# Terraform

> Ver [`../devops/infrastructure-as-code.md`](../devops/infrastructure-as-code.md) para los principios generales de IaC — este documento cubre la sintaxis y gestión de estado específicas de Terraform. Ver [`../../decisions/terraform-vs-cloudformation.md`](../../decisions/terraform-vs-cloudformation.md) para la comparativa con CloudFormation.

## Backend remoto con bloqueo: obligatorio en equipo

```hcl
terraform {
  backend "s3" {
    bucket         = "empresa-terraform-state"
    key            = "pedidos-service/terraform.tfstate"
    region         = "eu-west-1"
    dynamodb_table = "terraform-locks"  # bloqueo, evita aplicaciones concurrentes conflictivas
    encrypt        = true
  }
}
```

Ver [`../devops/infrastructure-as-code.md`](../devops/infrastructure-as-code.md) — el estado local (por defecto) es inviable en equipo; el backend remoto con bloqueo es el primer paso de cualquier proyecto Terraform real.

## Módulos: reutilización parametrizada

```hcl
module "vpc" {
  source   = "./modules/vpc-estandar"
  cidr     = "10.0.0.0/16"
  az_count = 3
  proyecto = "pedidos"
}
```

Ver [`../devops/infrastructure-as-code.md`](../devops/infrastructure-as-code.md) — un módulo probado y versionado evita duplicar (y potencialmente desalinear) definiciones de infraestructura similares entre proyectos.

## Variables y outputs: contrato explícito del módulo

```hcl
variable "cidr" {
  type        = string
  description = "Rango CIDR de la VPC"
}

output "vpc_id" {
  value       = aws_vpc.main.id
  description = "ID de la VPC creada, para uso de otros módulos"
}
```

Documentar `description` en cada variable/output — un módulo sin documentación obliga a leer su implementación completa para saber cómo usarlo, mismo principio que un contrato de API sin documentar (ver [`../../standards/api.md`](../../standards/api.md)).

## Workspaces: mismo código, múltiples instancias (con cautela)

```bash
terraform workspace new staging
terraform workspace select staging
```

Los workspaces permiten mantener múltiples estados independientes con el mismo código — apropiados para variaciones ligeras del mismo módulo (ej. distintas regiones), pero para entornos con configuración sustancialmente distinta (dev/staging/prod con topologías diferentes), preferir directorios/módulos raíz separados con sus propias variables, más explícito que workspaces implícitos.

## `terraform plan` como revisión obligatoria antes de aplicar

```bash
terraform plan -out=plan.tfplan
# revisar el diff explícitamente, especialmente cualquier "-/+ destroy and recreate"
terraform apply plan.tfplan
```

Ver [`../devops/infrastructure-as-code.md`](../devops/infrastructure-as-code.md) — nunca `terraform apply` directo sin revisar el plan primero en un cambio que afecta a infraestructura con datos o de producción; un "destroy and recreate" inesperado en el plan es la señal más común de un cambio que parecía trivial pero no lo era.

## Checklist

- [ ] ¿El estado usa un backend remoto con bloqueo, nunca local en equipo?
- [ ] ¿Los módulos tienen variables/outputs documentados con `description`?
- [ ] ¿Se revisa `terraform plan` explícitamente antes de aplicar, especialmente ante "destroy and recreate"?

Ver también: [`../devops/infrastructure-as-code.md`](../devops/infrastructure-as-code.md), [`../../decisions/terraform-vs-cloudformation.md`](../../decisions/terraform-vs-cloudformation.md), [`../../recipes/terraform-deploy-aws.md`](../../recipes/terraform-deploy-aws.md).
