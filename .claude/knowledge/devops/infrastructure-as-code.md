# Infrastructure as Code

> Ver `CLAUDE.md` sección 8.1 (infraestructura como código en AWS) — este documento cubre el principio general, independiente del proveedor cloud. Ver [`../terraform/terraform.md`](../terraform/terraform.md) para la herramienta específica.

## Principio: la infraestructura es un artefacto versionado, no un estado tribal

Ningún recurso de infraestructura crítico se crea manualmente en la consola — se define como código, en el mismo repositorio (o uno dedicado) con el mismo rigor de revisión que el código de aplicación (ver [`../../standards/git.md`](../../standards/git.md)). El objetivo: poder reconstruir el entorno completo desde cero a partir del código, sin depender de que una persona concreta recuerde los pasos manuales que se hicieron una vez.

## Idempotencia: aplicar el mismo código dos veces no debe cambiar nada

```
terraform apply (primera vez): crea los recursos
terraform apply (segunda vez, sin cambios): "No changes. Infrastructure is up-to-date."
```

Mismo principio que [`../integration/idempotency.md`](../integration/idempotency.md) aplicado a infraestructura — una herramienta de IaC declarativa (Terraform, CloudFormation) calcula el diff entre el estado deseado (el código) y el estado real, aplicando solo los cambios necesarios.

## Módulos reutilizables: evitar duplicar definiciones de infraestructura

```
modulo "vpc-estandar": parametrizado por CIDR, número de AZs, nombre de proyecto
→ reutilizado por cada equipo/proyecto en lugar de copiar-pegar la definición completa
```

Mismo principio DRY que [`../architecture/dry-kiss-yagni.md`](../architecture/dry-kiss-yagni.md) aplica al código de aplicación — un módulo de infraestructura probado y estandarizado reduce tanto el esfuerzo de definir infraestructura nueva como el riesgo de configuraciones inconsistentes entre proyectos (ej. un Security Group mal configurado copiado y pegado repetidamente).

## Estado remoto y bloqueo: evitar aplicaciones concurrentes conflictivas

```
Terraform state en S3 + bloqueo vía DynamoDB — nunca el estado local en la máquina de una persona
```

El estado de la infraestructura (qué existe realmente, mapeado al código) debe vivir en un backend remoto compartido, con bloqueo para evitar que dos personas apliquen cambios simultáneamente sobre el mismo estado, lo que podría corromperlo o producir resultados inconsistentes.

## Entornos como instancias del mismo código, no código duplicado

```
mismo módulo Terraform, parametrizado por entorno (dev/staging/prod) vía variables,
NUNCA una copia del código con diferencias manuales entre entornos
```

Ver [`environments.md`](./environments.md) — un entorno de staging que diverge del código de producción en formas no explícitas (alguien hizo un cambio manual "solo en staging" hace meses) deja de ser representativo, invalidando el propósito de tener staging.

## Revisión de cambios de infraestructura antes de aplicar

```
terraform plan → revisar el diff explícito antes de terraform apply
Plan Output en el PR → revisión humana antes de merge, igual que código de aplicación
```

Un cambio de infraestructura (especialmente uno que implica destruir y recrear un recurso con datos) requiere el mismo nivel de revisión que un cambio de esquema de base de datos (ver [`../../playbooks/database-change.md`](../../playbooks/database-change.md)) — nunca aplicado a producción sin que alguien haya revisado explícitamente el plan de cambios.

## Checklist

- [ ] ¿Toda la infraestructura crítica está definida como código, sin recursos creados manualmente en consola?
- [ ] ¿El estado se gestiona en un backend remoto con bloqueo, no local?
- [ ] ¿Existen módulos reutilizables para patrones de infraestructura repetidos entre proyectos/equipos?
- [ ] ¿El plan de cambios se revisa explícitamente antes de aplicar en producción?

Ver también: [`../terraform/terraform.md`](../terraform/terraform.md), [`environments.md`](./environments.md), `CLAUDE.md` sección 8.1.
