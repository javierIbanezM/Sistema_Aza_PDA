# Monitoring

> Ver [`observability.md`](./observability.md) para la estrategia general. Este documento cubre la recolección y agregación de **métricas** a escala de plataforma. Ver [`../aws-monitoring/cloudwatch.md`](../aws-monitoring/cloudwatch.md) para la implementación nativa en AWS.

## Qué monitorizar más allá de una aplicación individual

Además de los golden signals por servicio (ver [`../architecture/logging.md`](../architecture/logging.md)), DevOps monitoriza la capa de infraestructura compartida: saturación de clústeres (CPU/memoria agregada de todo el clúster de contenedores, no solo un servicio), capacidad de red, salud de la plataforma de CI/CD en sí, coste de infraestructura en tiempo real.

## Métricas de infraestructura vs. métricas de aplicación: ambas necesarias

```
Métrica de aplicación: latencia de /api/pedidos (responsabilidad del equipo del servicio)
Métrica de infraestructura: % de capacidad del clúster ECS/EKS usado, saturación de IPs disponibles en la VPC
```

Un servicio puede tener latencia perfecta mientras la infraestructura subyacente está cerca de su límite de capacidad — sin métricas de infraestructura, ese riesgo es invisible hasta que la capacidad se agota y **todos** los servicios del clúster se ven afectados simultáneamente.

## Dashboards por audiencia, no un único dashboard genérico

```
Dashboard de equipo de servicio: golden signals de SU servicio
Dashboard de plataforma: capacidad agregada, coste, salud de CI/CD
Dashboard ejecutivo: SLOs de negocio de alto nivel, sin detalle técnico
```

Un único dashboard que intenta servir a todas las audiencias termina siendo poco útil para cualquiera de ellas — diseñar explícitamente para quién es cada dashboard y qué decisión debe permitir tomar a esa audiencia concreta.

## Auto-descubrimiento de servicios a monitorizar

```
Nuevo servicio desplegado en el clúster → automáticamente detectado y añadido al scraping de métricas,
sin configuración manual por cada servicio nuevo
```

Una plataforma de monitorización que requiere configuración manual por cada servicio nuevo (registrarlo explícitamente) es una fuente constante de "puntos ciegos" — servicios nuevos que alguien olvidó añadir al monitoring. Preferir auto-descubrimiento basado en convenciones (labels/tags estándar) sobre registro manual.

## Checklist

- [ ] ¿Existen métricas de infraestructura compartida (capacidad de clúster, red), no solo por servicio individual?
- [ ] ¿Los dashboards están diseñados explícitamente para audiencias distintas, no un único dashboard genérico?
- [ ] ¿Los servicios nuevos se incorporan al monitoring automáticamente, sin registro manual propenso a olvidos?

Ver también: [`observability.md`](./observability.md), [`alerting.md`](./alerting.md), [`../aws-monitoring/cloudwatch.md`](../aws-monitoring/cloudwatch.md).
