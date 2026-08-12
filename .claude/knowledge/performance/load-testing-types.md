# Load Testing Types

> Ver [`benchmarking.md`](./benchmarking.md) para el rigor estadístico que aplica a cualquiera de estos tipos de prueba. Este documento distingue los cuatro tipos, con frecuencia confundidos o usados indistintamente sin precisión sobre qué pregunta responde cada uno.

## Cuatro preguntas distintas, cuatro tipos de prueba distintos

| Tipo | Pregunta que responde |
|---|---|
| Load Testing | ¿El sistema soporta la carga **esperada** normal/pico? |
| Stress Testing | ¿Dónde está el **límite** del sistema, y cómo falla al superarlo? |
| Spike Testing | ¿El sistema sobrevive a un **pico repentino** de tráfico, y se recupera después? |
| Soak Testing | ¿El sistema es estable durante un periodo **prolongado** de carga sostenida? |

Usar el tipo incorrecto para la pregunta que realmente se necesita responder produce una falsa sensación de seguridad — un Load Test exitoso no dice nada sobre cómo se comporta el sistema ante un pico repentino (eso lo responde Spike Testing) ni sobre fugas de memoria que solo se manifiestan tras horas de carga (eso lo responde Soak Testing).

## Load Testing: validar la capacidad esperada

```
Carga objetivo: el volumen esperado en producción (normal y pico conocido, ver capacity-planning.md)
Duración: suficiente para alcanzar estado estable, no solo un pico instantáneo
Criterio de éxito: cumple el performance budget (ver performance-budget.md) bajo esa carga
```

## Stress Testing: encontrar el punto de ruptura

```
Carga: incrementada progresivamente MÁS ALLÁ de lo esperado, hasta que el sistema falla
Objetivo: identificar el límite real de capacidad, y CÓMO falla (degradación gradual vs. caída abrupta)
```

Un sistema que se degrada gradualmente (latencia aumenta progresivamente) es preferible a uno que colapsa abruptamente al superar un umbral — Stress Testing revela cuál de los dos comportamientos tiene el sistema real, información crítica para diseñar Circuit Breakers y límites de capacidad apropiados (ver [`../../patterns/circuit-breaker.md`](../../patterns/circuit-breaker.md)).

## Spike Testing: picos repentinos, no incremento gradual

```
Carga: salto BRUSCO de tráfico normal a un múltiplo alto (ej. 10x) en segundos, luego vuelta a normal
Objetivo: ¿el autoescalado reacciona a tiempo? ¿el sistema se recupera limpiamente tras el pico,
          o queda en un estado degradado incluso después de que el tráfico vuelve a la normalidad?
```

Distinto de Stress Testing (incremento gradual hasta el límite) — Spike Testing evalúa específicamente la velocidad de reacción del sistema (autoescalado, ver [`../devops/orchestration.md`](../devops/orchestration.md)) ante un cambio súbito, no gradual.

## Soak Testing: estabilidad bajo carga sostenida prolongada

```
Carga: nivel moderado-alto (no el límite), mantenida durante horas (no minutos)
Objetivo: detectar fugas de memoria (ver ../../anti-patterns/memory-leak.md), agotamiento de
          recursos (conexiones de pool no liberadas), degradación progresiva que no se manifiesta
          en pruebas cortas
```

Muchos problemas de estabilidad (fugas de memoria, conexiones no cerradas, crecimiento no acotado de caché) son invisibles en una prueba de 10 minutos y solo se manifiestan tras horas de operación sostenida — Soak Testing es la única forma sistemática de detectarlos antes de que ocurran en producción.

## Checklist

- [ ] ¿Se ha identificado qué pregunta específica se necesita responder antes de elegir el tipo de prueba?
- [ ] ¿Se ha ejecutado Stress Testing para conocer el comportamiento del sistema más allá de la capacidad esperada, no solo confirmado que soporta la carga normal?
- [ ] ¿Se ha ejecutado Soak Testing para sistemas de larga duración, no solo pruebas cortas de minutos?

Ver también: [`benchmarking.md`](./benchmarking.md), [`../../recipes/canary-deployment.md`](../../recipes/canary-deployment.md), [`performance-regression.md`](./performance-regression.md).
