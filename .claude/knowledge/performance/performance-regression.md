# Performance Regression

> Ver [`performance-budget.md`](./performance-budget.md) para el umbral contra el que se detecta una regresión, y [`../devops/ci-cd.md`](../devops/ci-cd.md) para el pipeline donde este proceso se integra.

## El problema: el rendimiento se degrada gradualmente, casi nunca de golpe

Un sistema rara vez pierde rendimiento en un único cambio dramático — más habitual es una degradación acumulativa, un pequeño porcentaje en cada release (una query un poco menos eficiente aquí, una dependencia un poco más pesada allá), invisible en cualquier comparación "antes/después" de un único cambio pero significativa acumulada tras meses de releases sin control.

## Testing de regresión de rendimiento en el pipeline, no solo funcional

```yaml
# Extiende el pipeline de knowledge/devops/ci-cd.md con una etapa de verificación de rendimiento
performance-regression-check:
  needs: build-and-push
  steps:
    - run: k6 run --summary-export=resultado.json load-test.js
    - run: |
        P95_ACTUAL=$(jq '.metrics.http_req_duration.p95' resultado.json)
        P95_BASELINE=$(cat baseline-p95.txt)
        # Fallar si la degradación supera un umbral tolerado (ej. 10%)
        python3 -c "exit(0 if $P95_ACTUAL <= $P95_BASELINE * 1.10 else 1)"
```

Ver [`performance-budget.md`](./performance-budget.md) — mismo principio de gate automatizado, aplicado específicamente a comparar contra una línea base histórica, no solo contra un umbral absoluto fijo.

## Línea base: actualizada conscientemente, nunca a la deriva

```
Línea base actual: p95 = 280ms (medida en la última release aprobada)
Cambio propuesto: mejora deliberada que reduce a 220ms
→ Actualizar la línea base explícitamente a 220ms, con la razón documentada (no solo "mejoró")
```

La línea base de comparación debe actualizarse deliberadamente ante una mejora real confirmada — nunca dejar que se actualice implícitamente release tras release sin verificación, lo que permitiría que una degradación gradual pase desapercibida porque cada release individual "no empeoró mucho respecto a la anterior".

## Bisección para localizar cuándo se introdujo una regresión

```bash
git bisect start
git bisect bad  # el commit actual tiene la regresión
git bisect good v2.3.0  # este commit anterior no la tenía
git bisect run ./ejecutar-benchmark-y-verificar-umbral.sh
```

Mismo principio que [`../../agents/senior-debugger.md`](../../agents/senior-debugger.md) aplica a bugs funcionales — cuando una regresión de rendimiento se detecta pero no se sabe en qué cambio se introdujo, la bisección automatizada contra un script de benchmark reduce el espacio de búsqueda exponencialmente.

## Atribuir la causa, no solo detectar el síntoma

Una vez localizado el commit/cambio responsable (vía bisección o revisión del cambio específico), aplicar [`bottleneck-analysis.md`](./bottleneck-analysis.md) para entender **por qué** ese cambio degradó el rendimiento — la corrección apropiada depende de esa causa raíz, no solo de revertir el cambio sin entenderlo (que podría no ser viable si el cambio también aportaba una funcionalidad necesaria).

## Checklist

- [ ] ¿Existe una verificación automatizada de regresión de rendimiento en el pipeline, no solo pruebas funcionales?
- [ ] ¿La línea base de comparación se actualiza deliberadamente ante mejoras confirmadas, no a la deriva?
- [ ] ¿Se usa bisección para localizar el commit responsable cuando la causa no es obvia?

Ver también: [`performance-budget.md`](./performance-budget.md), [`bottleneck-analysis.md`](./bottleneck-analysis.md), [`../devops/ci-cd.md`](../devops/ci-cd.md).
