# Terraform vs. CloudFormation

## Contexto

Ambos son herramientas de infraestructura como código (ver [`../knowledge/devops/infrastructure-as-code.md`](../knowledge/devops/infrastructure-as-code.md)); CloudFormation es nativo de AWS, Terraform es agnóstico de proveedor.

## Comparativa

| Criterio | Terraform | CloudFormation |
|---|---|---|
| Portabilidad multi-cloud | Sí — mismo lenguaje (HCL) para AWS, GCP, Azure, y proveedores no-cloud (Datadog, GitHub) | No — exclusivo de AWS |
| Integración nativa con AWS | Buena, con cierto retraso en soportar features nuevas de AWS | Inmediata — mantenido por el propio AWS |
| Coste operativo | Ninguno adicional (herramienta gratuita, backend de estado propio) | Ninguno — completamente gestionado por AWS, sin necesidad de gestionar estado |
| Gestión de estado | Responsabilidad del usuario (backend remoto, ver [`../knowledge/terraform/terraform.md`](../knowledge/terraform/terraform.md)) | Gestionado automáticamente por AWS, sin configuración |
| Ecosistema de módulos | Amplio (Terraform Registry, comunidad extensa) | Más limitado, aunque creciente con CDK |
| Curva de aprendizaje | Media (HCL, conceptos propios) | Baja si ya se conoce AWS (aunque YAML/JSON verboso) |

## Cuándo elegir Terraform

- Estrategia multi-cloud, o probabilidad razonable de necesitar portabilidad en el futuro.
- Se gestiona infraestructura de proveedores no-AWS además de AWS (Datadog, GitHub, Cloudflare) desde el mismo flujo de trabajo.
- El equipo ya tiene experiencia Terraform consolidada.

## Cuándo elegir CloudFormation

- Infraestructura exclusivamente AWS sin necesidad de portabilidad, donde la integración inmediata con features nuevas de AWS es valiosa.
- Se prefiere no gestionar el backend de estado explícitamente (CloudFormation lo gestiona de forma transparente).
- Uso combinado con AWS CDK, que genera CloudFormation desde un lenguaje de programación de propósito general (TypeScript, Python), preferible para equipos que priorizan expresividad de lenguaje sobre el DSL declarativo de HCL/YAML.

## Recomendación por defecto de este framework

**Terraform por defecto** — la portabilidad y el ecosistema de módulos compensan la ligera fricción adicional de gestionar el backend de estado, y evita el acoplamiento total a un único proveedor incluso si hoy el proyecto es exclusivamente AWS; **CloudFormation/CDK** cuando el proyecto es y seguirá siendo exclusivamente AWS de forma confirmada, y se valora la integración inmediata con features nuevas del proveedor.

Ver también: [`../knowledge/terraform/terraform.md`](../knowledge/terraform/terraform.md), [`../knowledge/devops/infrastructure-as-code.md`](../knowledge/devops/infrastructure-as-code.md).
