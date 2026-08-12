# EFS (Elastic File System)

> Ver [`../../decisions/s3-vs-efs.md`](../../decisions/s3-vs-efs.md) para la comparativa completa con S3.

## Qué es: sistema de archivos compartido con semántica POSIX

A diferencia de S3 (almacenamiento de objetos), EFS es un sistema de archivos de red con semántica POSIX completa (permisos, bloqueos, jerarquía de directorios editable) montable simultáneamente por múltiples instancias EC2/contenedores — apropiado cuando la aplicación necesita un sistema de archivos compartido tradicional, no un almacén de objetos.

## Cuándo EFS en lugar de S3

- La aplicación (o una librería de terceros que no se puede modificar) espera un sistema de archivos POSIX real, no una API de objetos.
- Múltiples instancias necesitan lectura/escritura concurrente sobre los mismos archivos con semántica de bloqueo de archivo.
- Directorios de trabajo compartidos entre contenedores/instancias (ej. una cola de archivos siendo procesados por múltiples workers que necesitan verse entre sí).

## Modos de rendimiento y throughput

| Modo | Uso |
|---|---|
| General Purpose | La mayoría de casos, baja latencia |
| Max I/O | Alta concurrencia (miles de conexiones simultáneas), a costa de latencia individual ligeramente mayor |
| Bursting Throughput | Throughput escala con el tamaño almacenado |
| Provisioned Throughput | Throughput fijo independiente del tamaño, para cargas con necesidad de throughput alto y poco almacenamiento |

## Integración con ECS/EKS: volúmenes compartidos entre tasks/pods

```json
"volumes": [{
  "name": "datos-compartidos",
  "efsVolumeConfiguration": { "fileSystemId": "fs-0123456789abcdef0" }
}]
```

Permite que múltiples tasks de ECS o pods de Kubernetes compartan el mismo almacenamiento persistente — relevante para migrar aplicaciones legadas que ya esperan un sistema de archivos compartido, sin rediseñar su capa de almacenamiento.

## Coste: significativamente mayor que S3 por GB

EFS es notablemente más caro por GB almacenado que S3 — usar EFS solo cuando la semántica de sistema de archivos POSIX es genuinamente necesaria, no como almacenamiento general de archivos donde S3 sería suficiente y considerablemente más económico.

## Checklist

- [ ] ¿La aplicación necesita genuinamente semántica POSIX (bloqueos, permisos de archivo), o S3 sería suficiente y más económico?
- [ ] ¿El modo de rendimiento (General Purpose/Max I/O) corresponde al patrón real de concurrencia?
- [ ] ¿Se ha evaluado el coste por GB frente a S3 antes de elegir EFS por defecto?

Ver también: [`s3.md`](./s3.md), [`../../decisions/s3-vs-efs.md`](../../decisions/s3-vs-efs.md), [`../aws-compute/ecs.md`](../aws-compute/ecs.md).
