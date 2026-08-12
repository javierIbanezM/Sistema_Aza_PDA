# S3 vs. EFS

## Contexto

Ambos almacenan datos de forma duradera y gestionada en AWS, con modelos de acceso fundamentalmente distintos.

## Comparativa

| Criterio | S3 (objetos) | EFS (sistema de archivos POSIX) |
|---|---|---|
| Modelo de acceso | API de objetos (GET/PUT por clave completa) | Sistema de archivos jerárquico, montable, con bloqueos |
| Edición parcial de archivos grandes | No eficiente (hay que reescribir el objeto completo) | Sí, edición nativa como cualquier archivo |
| Coste por GB | Bajo (con clases de almacenamiento escalonadas) | Significativamente mayor |
| Acceso concurrente multi-instancia | Sí, pero sin semántica de bloqueo de archivo | Sí, con semántica POSIX completa (bloqueos, permisos) |
| Escalabilidad | Prácticamente ilimitada | Alta, pero con coste creciente proporcional |
| Compatibilidad con aplicaciones legadas que esperan un filesystem | Ninguna sin adaptación | Total, sin cambios en la aplicación |

## Cuándo elegir S3

- Almacenamiento de archivos que se escriben una vez y se leen muchas (assets estáticos, backups, exports, data lake).
- Contenido servido a través de CDN (ver [`../knowledge/aws-network/cloudfront.md`](../knowledge/aws-network/cloudfront.md)).
- Cualquier caso donde la aplicación puede diseñarse (o ya está diseñada) para usar una API de objetos en lugar de un sistema de archivos tradicional.

## Cuándo elegir EFS

- La aplicación (o una librería/framework de terceros no modificable) requiere específicamente un sistema de archivos POSIX.
- Múltiples instancias necesitan lectura/escritura concurrente sobre los mismos archivos con semántica de bloqueo real.
- Migración de una aplicación legada que ya espera un directorio compartido tradicional, sin presupuesto/tiempo para rediseñar su capa de almacenamiento.

## Recomendación por defecto de este framework

**S3 por defecto** para cualquier necesidad de almacenamiento de archivos en una aplicación nueva, dado su menor coste y mayor escalabilidad — **EFS solo cuando existe una razón técnica concreta** (semántica POSIX genuinamente necesaria, aplicación legada no modificable) que S3 no puede satisfacer, nunca elegido por familiaridad con sistemas de archivos tradicionales cuando S3 sería igualmente viable con el diseño adecuado.

Ver también: [`../knowledge/aws-storage/s3.md`](../knowledge/aws-storage/s3.md), [`../knowledge/aws-storage/efs.md`](../knowledge/aws-storage/efs.md), [`../knowledge/aws-storage/fsx.md`](../knowledge/aws-storage/fsx.md).
