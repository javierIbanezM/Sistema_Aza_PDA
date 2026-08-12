# Lambda

> Ver [`../../decisions/ec2-vs-lambda.md`](../../decisions/ec2-vs-lambda.md) para la comparativa completa. Ver [`../aws-integration/api-gateway.md`](../aws-integration/api-gateway.md) para exponer Lambda como API HTTP, y [`../aws-integration/eventbridge.md`](../aws-integration/eventbridge.md)/[`../aws-integration/sqs.md`](../aws-integration/sqs.md) para invocación por eventos.

## Modelo de ejecución: sin servidor que gestionar

AWS gestiona el aprovisionamiento, escalado y parcheo del runtime — el código se ejecuta en respuesta a un evento (petición HTTP, mensaje de cola, cambio en un bucket S3, cron) y se paga por tiempo de ejecución real, no por capacidad reservada.

## Cold start: la limitación más relevante a diseñar

```
Primera invocación tras inactividad: inicialización del runtime + código (cold start, cientos de ms a segundos)
Invocaciones siguientes mientras el contenedor sigue "caliente": sin ese coste adicional
```

Mitigaciones: minimizar dependencias cargadas en el handler, usar runtimes con arranque rápido (Node.js/Python sobre JVM para latencia crítica), Provisioned Concurrency para mantener instancias precalentadas ante tráfico predecible con requisito estricto de latencia.

## Límites a diseñar explícitamente

- **Timeout máximo**: 15 minutos — cualquier proceso más largo requiere otro servicio (Step Functions para orquestación larga, ECS/Batch para procesamiento extenso).
- **Memoria/CPU acoplados**: más memoria asignada también proporciona más CPU proporcional — a veces aumentar memoria reduce el coste total al completar más rápido, contraintuitivamente.
- **Tamaño de paquete**: límite de despliegue — dependencias pesadas pueden requerir Lambda Layers o contenedores en lugar del paquete zip estándar.

## Idempotencia: crítica dada la semántica "al menos una vez"

```python
def handler(event, context):
    event_id = event['id']
    if ya_procesado(event_id):  # ver knowledge/integration/idempotency.md
        return {'statusCode': 200}
    procesar(event)
    marcar_procesado(event_id)
```

Lambda invocada por SQS/EventBridge puede reintentar la misma invocación ante fallo — ver [`../integration/idempotency.md`](../integration/idempotency.md), mismo principio que cualquier consumidor de eventos del framework.

## Permisos: rol de ejecución con mínimo privilegio

```json
{ "Effect": "Allow", "Action": ["dynamodb:GetItem", "dynamodb:PutItem"], "Resource": "arn:aws:dynamodb:*:*:table/Pedidos" }
```

Cada función Lambda tiene su propio rol de ejecución IAM — nunca un rol genérico compartido con permisos amplios "por si acaso" (ver [`../aws-security/iam.md`](../aws-security/iam.md)), sino exactamente los permisos que esa función concreta necesita.

## Checklist

- [ ] ¿Se ha evaluado el impacto de cold start para el caso de uso (crítico para latencia vs. tolerante)?
- [ ] ¿La función es idempotente ante reintento de la misma invocación?
- [ ] ¿El rol de ejecución tiene únicamente los permisos que esa función concreta necesita?
- [ ] ¿Se ha verificado que el timeout máximo de 15 minutos es suficiente para el proceso, o se necesita otro servicio?

Ver también: [`ec2.md`](./ec2.md), [`../../decisions/ec2-vs-lambda.md`](../../decisions/ec2-vs-lambda.md), [`../aws-security/iam.md`](../aws-security/iam.md).
