# Rollback

> Ver [`deployment-strategies.md`](./deployment-strategies.md) para cómo cada estrategia condiciona la velocidad de rollback disponible, y [`../sqlserver/restore.md`](../sqlserver/restore.md) para el mismo principio aplicado a datos.

## Rollback probado, no solo teórico

Un plan de rollback nunca ejecutado en un simulacro no es un plan de rollback, es una suposición — mismo principio que [`../sqlserver/backup.md`](../sqlserver/backup.md) exige para restauraciones de base de datos. Ejecutar el rollback al menos una vez en un entorno no productivo antes de asumir que funcionará bajo presión durante un incidente real.

## Rollback de código vs. rollback de datos: problemas distintos

```
Rollback de código: revertir a la imagen/versión anterior — rápido con Blue/Green o revert de despliegue
Rollback de datos: una migración de esquema ya aplicada, o datos ya escritos por la versión nueva,
                    no siempre se revierten limpiamente solo con revertir el código
```

El rollback de código es, en la mayoría de estrategias modernas (ver [`deployment-strategies.md`](./deployment-strategies.md)), relativamente directo — el rollback de datos es la parte genuinamente difícil, y requiere diseño explícito **antes** del despliegue, no improvisado durante el incidente (ver [`../../playbooks/database-change.md`](../../playbooks/database-change.md) para el patrón de migración expansiva/contractiva que hace esto posible).

## Migraciones expansivas antes que contractivas: la clave del rollback seguro

```
1. Añadir columna nueva (aditivo) — código viejo y nuevo conviven sin problema
2. Desplegar código que escribe en ambas columnas
3. Backfill de datos históricos
4. Desplegar código que lee solo de la columna nueva
5. Solo entonces, en un despliegue POSTERIOR y SEPARADO: eliminar la columna antigua
```

Si el rollback del paso 4 es necesario, el código anterior (paso 2-3) sigue siendo compatible con el esquema — el rollback de datos genuinamente irreversible solo ocurre en el paso 5, que se ejecuta únicamente cuando ya no hay ninguna duda de que el cambio fue exitoso.

## Rollback automático basado en métricas (no solo decisión manual)

```
Tras desplegar v2: si tasa de error > 5% durante 3 minutos → rollback automático a v1, sin esperar intervención humana
```

Para sistemas críticos con requisito de mínimo downtime, un rollback automático basado en umbrales de las mismas métricas que ya se instrumentan (ver [`observability.md`](./observability.md)) reduce el tiempo de detección+reacción de minutos (humano revisando dashboards) a segundos.

## Comunicación durante un rollback

Un rollback en producción, especialmente si es visible para usuarios, requiere comunicación explícita al equipo/stakeholders — mismo principio que [`../../playbooks/hotfix.md`](../../playbooks/hotfix.md), nunca un rollback silencioso del que el resto del equipo se entera después por casualidad.

## Checklist

- [ ] ¿El rollback se ha probado en un simulacro, no solo diseñado teóricamente?
- [ ] ¿Las migraciones de esquema siguen el patrón expansivo/contractivo, permitiendo rollback de código sin romper compatibilidad con datos?
- [ ] ¿Existe rollback automático basado en métricas para sistemas críticos, no solo decisión manual?
- [ ] ¿El rollback se comunica explícitamente al equipo, nunca de forma silenciosa?

Ver también: [`deployment-strategies.md`](./deployment-strategies.md), [`../../playbooks/rollback-production.md`](../../playbooks/rollback-production.md), [`../sqlserver/restore.md`](../sqlserver/restore.md).
