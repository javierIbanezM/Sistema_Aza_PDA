# Batch

> Ver [`lambda.md`](./lambda.md) para procesamiento asíncrono de corta duración (hasta 15 min) — Batch cubre el caso complementario: cargas de procesamiento largo o de gran escala.

## Qué es y cuándo encaja

Ejecuta trabajos de procesamiento por lotes (procesamiento de datos masivo, renderizado, cálculo científico) sin gestionar la infraestructura de cómputo subyacente — AWS aprovisiona y desaprovisiona automáticamente la capacidad (EC2 o Fargate) según la cola de trabajos pendientes.

## Cuándo Batch en lugar de Lambda o ECS

- El procesamiento excede el límite de 15 minutos de Lambda (ver [`lambda.md`](./lambda.md)).
- La carga es genuinamente por lotes (un conjunto de trabajos independientes a procesar, no un servicio de larga duración esperando peticiones) — a diferencia de ECS/EKS, pensados para servicios continuos.
- Se necesita escalado automático de la capacidad de cómputo según el volumen de trabajos en cola, sin gestionar manualmente cuántas instancias mantener activas.

## Job Definition y Job Queue

```json
{
  "jobDefinitionName": "procesar-importacion-masiva",
  "containerProperties": {
    "image": "123456789.dkr.ecr.region.amazonaws.com/importador:latest",
    "vcpus": 4,
    "memory": 8192,
    "retryStrategy": { "attempts": 3 }
  }
}
```

`retryStrategy` gestiona reintentos automáticos ante fallo del job — la misma disciplina de [`../integration/retries-and-backoff.md`](../integration/retries-and-backoff.md) aplica: el procesamiento del job debe ser idempotente (ver [`../integration/idempotency.md`](../integration/idempotency.md)) para que un reintento tras fallo parcial no duplique efectos.

## Compute Environment: EC2 (incluido Spot) o Fargate

Batch puede usar instancias Spot para reducir significativamente el coste de procesamiento tolerante a interrupción (ver [`ec2.md`](./ec2.md)) — apropiado para la mayoría de cargas batch, que típicamente pueden reintentarse sin problema ante una interrupción de Spot, a diferencia de un servicio interactivo con usuarios esperando respuesta inmediata.

## Checklist

- [ ] ¿El procesamiento excede los límites de Lambda o tiene un patrón genuinamente por lotes, no un servicio continuo?
- [ ] ¿El job es idempotente ante reintento tras fallo parcial?
- [ ] ¿Se ha evaluado Spot para reducir coste en cargas tolerantes a interrupción?

Ver también: [`lambda.md`](./lambda.md), [`../integration/idempotency.md`](../integration/idempotency.md), [`../integration/retries-and-backoff.md`](../integration/retries-and-backoff.md).
