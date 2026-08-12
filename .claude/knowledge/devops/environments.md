# Environments

> Ver [`infrastructure-as-code.md`](./infrastructure-as-code.md) para el principio de que los entornos son instancias parametrizadas del mismo código, nunca copias divergentes.

## Jerarquía típica de entornos y su propósito

```
dev → staging/preprod → producción
```

- **dev**: iteración rápida, datos sintéticos, tolerante a inestabilidad, cada desarrollador puede tener su propia instancia o compartir una.
- **staging/preprod**: réplica lo más fiel posible de producción (misma versión de infraestructura, datos representativos aunque no reales), última verificación antes de desplegar — ver [`../../playbooks/new-feature.md`](../../playbooks/new-feature.md).
- **producción**: el entorno real, con las máximas garantías de estabilidad, seguridad y observabilidad.

## Paridad entre entornos: staging debe parecerse a producción de verdad

Una diferencia de configuración entre staging y producción (versión de base de datos distinta, recursos infra-dimensionados en staging "para ahorrar") invalida el propósito de staging — un problema que solo se manifiesta con el volumen/configuración real de producción nunca se detecta antes del despliegue si staging no lo replica razonablemente. Paridad no significa tamaño idéntico (staging puede tener menos capacidad) pero sí misma versión de software y misma topología de red/servicios.

## Datos de prueba: nunca datos reales de producción sin anonimizar

```
Proceso de refresco de staging: copia de producción → anonimización automática (nombres, emails, tarjetas)
→ carga en staging
```

Copiar datos de producción directamente a un entorno de menor seguridad (staging, típicamente con más accesos y menos controles) es una violación de `CLAUDE.md` sección 12 — cualquier proceso de refresco de datos de prueba desde producción debe incluir anonimización automática como paso obligatorio, no opcional.

## Aislamiento entre entornos: nunca compartir recursos críticos

```
Cada entorno: su propia base de datos, sus propias colas, su propia VPC/namespace —
NUNCA una base de datos compartida entre staging y producción "para simplificar"
```

Compartir infraestructura entre entornos introduce el riesgo de que una prueba en staging afecte a producción (un job mal configurado que borra datos, una carga de test que satura una cola compartida) — el aislamiento completo es la norma, incluso a costa de mayor coste/complejidad de gestionar entornos separados.

## Efímeros por feature: entornos de revisión (Review Apps)

```
Cada PR abre un entorno efímero propio → se destruye automáticamente al cerrar el PR
```

Para equipos con alto volumen de cambios en paralelo, un entorno efímero por Pull Request permite revisar visualmente/funcionalmente cada cambio de forma aislada, sin competir por un único entorno de staging compartido entre todo el equipo — evalúa el trade-off de coste/complejidad de infraestructura efímera automatizada frente al beneficio de paralelismo.

## Checklist

- [ ] ¿Staging tiene paridad razonable con producción (misma versión, misma topología), no solo el nombre?
- [ ] ¿Los datos de prueba en entornos de menor seguridad están anonimizados automáticamente, nunca copiados en crudo?
- [ ] ¿Cada entorno tiene sus propios recursos críticos aislados, sin compartir con otros entornos?

Ver también: [`infrastructure-as-code.md`](./infrastructure-as-code.md), [`configuration-management.md`](./configuration-management.md), `CLAUDE.md` sección 12.
