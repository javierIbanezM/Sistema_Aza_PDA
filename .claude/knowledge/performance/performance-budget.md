# Performance Budget

> Ver [`performance-engineering.md`](./performance-engineering.md) para el método general — un performance budget es lo que convierte "ser rápido" en un requisito verificable y defendible frente a otras prioridades.

## Qué es: un límite explícito, acordado antes de que se necesite

```
Presupuesto de latencia: p95 de /api/pedidos < 300ms
Presupuesto de tamaño de payload: respuesta JSON < 50KB
Presupuesto de tiempo de carga: Time to Interactive < 3s en conexión 4G
Presupuesto de recursos: < 512MB de memoria por instancia bajo carga normal
```

Un presupuesto de rendimiento es un límite acordado **antes** de que el sistema lo necesite, no un objetivo que se define reactivamente cuando ya hay una queja de lentitud — funciona como un contrato que cualquier cambio debe respetar, igual que un test que puede fallar.

## Presupuestos como gate en el pipeline, no solo como aspiración

```yaml
# Job de CI que falla si el presupuesto se excede, mismo principio que un test funcional
- name: Verificar performance budget
  run: |
    RESULTADO=$(k6 run --summary-export=resultado.json load-test.js)
    P95=$(jq '.metrics.http_req_duration.p95' resultado.json)
    if (( $(echo "$P95 > 300" | bc -l) )); then exit 1; fi
```

Ver [`performance-regression.md`](./performance-regression.md) — sin un gate automatizado, un presupuesto es solo una intención documentada que nadie verifica sistemáticamente hasta que ya se ha violado en producción.

## Distribuir el presupuesto entre componentes

```
Presupuesto total del endpoint: 300ms
  - Autenticación/autorización: 20ms
  - Lógica de negocio: 50ms
  - Query a base de datos: 150ms
  - Llamada a servicio externo: 60ms
  - Margen/overhead de red: 20ms
```

Un presupuesto total sin distribuir entre sus componentes no ayuda a decidir dónde optimizar cuando se excede — descomponerlo permite identificar rápidamente qué parte del sistema está consumiendo más de lo asignado, sin necesidad de repetir todo el análisis de [`bottleneck-analysis.md`](./bottleneck-analysis.md) desde cero cada vez.

## Presupuestos por percentil, nunca solo por promedio

```
Promedio engañoso: latencia media de 100ms — pero el p99 es 4 segundos, afectando al 1% de usuarios
```

Ver [`latency-and-throughput.md`](./latency-and-throughput.md) — un presupuesto definido solo en promedio oculta sistemáticamente los peores casos; definir presupuestos en p95/p99, que reflejan la experiencia de los usuarios menos afortunados, no solo del caso típico.

## Revisar y ajustar el presupuesto conscientemente

Un presupuesto no es inmutable — a medida que el sistema, el volumen de usuarios, o las expectativas del negocio cambian, el presupuesto debe revisarse explícitamente (ver [`../../agents/senior-business-analyst.md`](../../agents/senior-business-analyst.md) para la conversación de negocio que justifica el nuevo valor), nunca relajado silenciosamente solo porque el sistema actual no lo cumple.

## Checklist

- [ ] ¿Existe un presupuesto de rendimiento explícito y cuantificado para cada requisito crítico, no solo "debe ser rápido"?
- [ ] ¿El presupuesto se verifica automáticamente en el pipeline, no solo revisado manualmente de forma ocasional?
- [ ] ¿Está definido en percentiles (p95/p99), no solo en promedio?
- [ ] ¿Cualquier cambio al presupuesto es una decisión consciente y documentada, no una relajación silenciosa?

Ver también: [`performance-regression.md`](./performance-regression.md), [`latency-and-throughput.md`](./latency-and-throughput.md), [`capacity-planning.md`](./capacity-planning.md).
