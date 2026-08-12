# Logging (plataforma de agregación)

> Ver [`../architecture/logging.md`](../architecture/logging.md) para cómo debe **generar** logs una aplicación — este documento cubre cómo la plataforma DevOps **recolecta, transporta, almacena y hace consultable** esos logs a escala de toda la organización.

## Pipeline de logs: de la aplicación a la plataforma consultable

```
Aplicación (stdout, formato JSON) → Agente de recolección (Fluent Bit/CloudWatch Agent) →
Transporte → Almacén centralizado (CloudWatch Logs/ELK/Loki) → Índice consultable
```

Las aplicaciones nunca escriben directamente a un almacén de logs centralizado desde su propio código — escriben a `stdout`/`stderr` (o a un archivo local rotado) en formato estructurado, y un agente de recolección se encarga del transporte, desacoplando la aplicación de los detalles de la plataforma de logging.

## Log shipping sin pérdida ante picos de volumen

```
Agente con buffer local + backpressure: ante un pico de volumen, el agente amortigua
en lugar de perder logs o bloquear la aplicación que los genera
```

Un pico de errores (precisamente el momento donde los logs son más críticos) suele generar también un pico de volumen de logs — el pipeline de recolección debe diseñarse para no perder logs ni ralentizar la aplicación exactamente cuando más se necesita esa visibilidad.

## Retención escalonada: coste vs. necesidad de consulta

```
Logs de los últimos 7 días: almacenamiento caliente, consulta rápida
Logs de 7-90 días: almacenamiento más económico, consulta más lenta
Logs > 90 días: archivo frío o eliminados, según requisito de auditoría (ver knowledge/aws-storage/s3.md, lifecycle policies)
```

Mismo principio que [`../sqlserver/backup.md`](../sqlserver/backup.md) aplica a backups — retener todo indefinidamente en almacenamiento caliente es innecesariamente costoso; escalonar según la necesidad real de consulta.

## Estructuración e índices: buscar por campo, no por texto libre

```
Índice sobre trace_id, usuario_id, service_name, level
→ Búsqueda: "todos los ERROR del servicio pedidos-service con trace_id=X" en milisegundos
```

Logs almacenados como JSON estructurado (ver [`../architecture/logging.md`](../architecture/logging.md)) permiten indexar campos específicos, convirtiendo búsquedas que serían regex costosas sobre texto libre en consultas indexadas directas — la razón principal por la que el framework exige logs estructurados desde el nivel de aplicación.

## Enmascarado de datos sensibles en el pipeline, como red de seguridad adicional

```
Regla en el agente de recolección: enmascarar patrones de tarjeta de crédito/email antes de indexar
```

Además de la disciplina de nunca loguear datos sensibles desde la aplicación (ver [`../../standards/logging-standard.md`](../../standards/logging-standard.md)), un filtro adicional a nivel de plataforma actúa como red de seguridad ante un error humano que sí logueó algo sensible — defensa en profundidad, no sustituto de la disciplina en origen.

## Checklist

- [ ] ¿Las aplicaciones escriben a stdout/archivo local, delegando el transporte a un agente, sin escribir directamente al almacén centralizado?
- [ ] ¿El pipeline de recolección tolera picos de volumen sin pérdida de logs ni impacto en la aplicación origen?
- [ ] ¿La retención está escalonada según necesidad real de consulta, no indefinida en almacenamiento caliente?
- [ ] ¿Existe un filtro de enmascarado a nivel de plataforma como red de seguridad adicional?

Ver también: [`../architecture/logging.md`](../architecture/logging.md), [`observability.md`](./observability.md), [`../../standards/logging-standard.md`](../../standards/logging-standard.md).
