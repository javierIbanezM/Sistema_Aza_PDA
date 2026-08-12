# Route 53

> Ver [`alb.md`](./alb.md)/[`../aws-network/cloudfront.md`](./cloudfront.md) para los destinos típicos que Route 53 resuelve.

## Qué es: DNS gestionado con capacidades de enrutamiento avanzadas

Más allá de resolución DNS estándar, Route 53 soporta políticas de enrutamiento que deciden **qué respuesta dar según criterios distintos de una simple resolución fija**.

## Políticas de enrutamiento

| Política | Comportamiento |
|---|---|
| Simple | Resolución DNS estándar, un único destino |
| Weighted | Reparte tráfico entre destinos según pesos configurados — útil para despliegues canary (5% a la versión nueva) |
| Latency-based | Dirige al usuario a la región con menor latencia desde su ubicación |
| Failover | Dirige al destino primario; ante fallo de su health check, conmuta automáticamente al secundario |
| Geolocation | Dirige según la ubicación geográfica del usuario (cumplimiento normativo, contenido localizado) |
| Multivalue Answer | Devuelve múltiples IPs saludables, balanceo simple del lado del cliente |

## Health Checks integrados con Failover

```
Route 53 Health Check → verifica el endpoint primario cada 30s
Si falla → política Failover conmuta automáticamente el DNS al endpoint secundario
```

Combinado con una política Failover, permite recuperación ante desastre a nivel de región sin intervención manual — el DNS deja de resolver hacia la región caída automáticamente, aunque el tiempo de propagación de DNS (dependiente del TTL configurado) introduce cierta latencia hasta que todos los clientes lo reflejan.

## TTL: trade-off entre velocidad de cambio y carga de consultas

```
TTL bajo (60s): cambios de DNS se propagan rápido, pero más consultas DNS repetidas
TTL alto (3600s): menos consultas DNS, pero un cambio tarda más en propagarse completamente
```

Reducir el TTL temporalmente antes de un cambio planificado de infraestructura (migración, failover programado) es una práctica habitual para minimizar el tiempo de propagación cuando el cambio ocurra.

## Registros de alias hacia recursos AWS

```
empresa.com → ALIAS → ALB (sin coste adicional de consulta DNS, a diferencia de un CNAME)
```

Los registros ALIAS de Route 53 apuntan directamente a recursos AWS (ALB, CloudFront, S3) sin el coste/limitación de un CNAME estándar (que no puede usarse en el registro raíz del dominio) — preferibles sobre CNAME cuando el destino es un recurso AWS.

## Checklist

- [ ] ¿La política de enrutamiento corresponde al objetivo real (failover, latencia, distribución ponderada)?
- [ ] ¿Existe un Health Check configurado si se usa política Failover?
- [ ] ¿El TTL está calibrado según la frecuencia esperada de cambios, reducido temporalmente antes de un cambio planificado?

Ver también: [`alb.md`](./alb.md), [`cloudfront.md`](./cloudfront.md).
