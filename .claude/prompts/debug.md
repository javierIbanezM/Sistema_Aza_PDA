# Prompt: Depuración Sistemática

## Propósito

Guiar la depuración de un fallo siguiendo la metodología documentada en [`../debugging.md`](../knowledge/architecture/debugging.md), evitando cambios especulativos sin diagnóstico.

## Instrucciones para Claude Code

Al recibir este prompt junto con la descripción de un fallo (mensaje de error, stack trace, comportamiento inesperado):

1. **Confirma la reproducción**: pide (o construye, si es posible con la información disponible) los pasos exactos, el input y el estado que reproducen el fallo de forma fiable. Si el fallo es intermitente, identifica primero el patrón que lo hace reproducible antes de continuar.

2. **Lee toda la información disponible** antes de plantear ninguna hipótesis: stack trace completo, logs alrededor del momento del fallo (ver [`../logging.md`](../knowledge/architecture/logging.md)), y el código de las funciones involucradas en la cadena de llamadas.

3. **Formula una hipótesis explícita y falsable** sobre la causa, en una frase concreta (no "algo falla en la validación", sino "el campo X llega nulo porque el mapeo Y no lo asigna cuando Z").

4. **Propón el experimento mínimo** que confirma o refuta la hipótesis (un log puntual, un test aislado, un breakpoint) antes de proponer ninguna corrección.

5. **Si el fallo aparece en un flujo largo o un historial**, aplica bisección: `git bisect` para regresiones de código, división del lote de datos a la mitad para fallos de procesamiento masivo.

6. **Distingue el punto donde el fallo se hace visible de la causa raíz real.** Sigue la cadena de causalidad hacia atrás (capas, servicios, transformaciones de datos) hasta el origen genuino antes de proponer una corrección.

7. **Propón la corrección** atacando la causa raíz confirmada, junto con un test de regresión que falla sin la corrección y pasa con ella.

## Formato de salida esperado

```
## Reproducción confirmada
[Pasos / input / estado que reproducen el fallo]

## Hipótesis
[Frase concreta y falsable]

## Evidencia recogida
[Qué se observó al probar la hipótesis: logs, resultado del test aislado, etc.]

## Causa raíz
[Explicación de la causa raíz real, distinta del punto donde el fallo se manifestó si aplica]

## Corrección propuesta
[Código de la corrección + test de regresión]
```

## Restricciones

- No propongas cambios de código como "prueba a ver si esto lo arregla" sin una hipótesis explícita respaldada por evidencia.
- No des por resuelto un fallo intermitente solo por ausencia de recurrencia en pocas ejecuciones; requiere identificar la causa.

Ver también: [`../debugging.md`](../knowledge/architecture/debugging.md), [`../logging.md`](../knowledge/architecture/logging.md).
