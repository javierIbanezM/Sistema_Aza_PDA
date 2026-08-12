# Prompt: Plan de Refactor Seguro

## Propósito

Guiar la planificación y ejecución de un refactor siguiendo la metodología documentada en [`../refactoring.md`](../knowledge/architecture/refactoring.md), minimizando el riesgo de regresión.

## Instrucciones para Claude Code

Al recibir este prompt junto con el código a refactorizar y el objetivo del refactor:

1. **Verifica la red de seguridad**: ¿existen tests que cubren el comportamiento actual del código a modificar?
   - Si sí, ejecútalos y confirma que pasan antes de tocar nada.
   - Si no, escribe primero tests de caracterización (ver [`../refactoring.md`](../knowledge/architecture/refactoring.md)) que documenten el comportamiento actual, incluidos posibles bugs conocidos (márcalos explícitamente como tal, sin corregirlos en este paso).

2. **Identifica el/los anti-patrón(es) presentes**, si los hay, consultando [`../anti-patterns/`](../anti-patterns/), y el patrón de destino apropiado en [`../patterns/`](../patterns/).

3. **Divide el refactor en pasos pequeños e independientes.** Cada paso debe:
   - Ser un único tipo de cambio (extraer método, extraer clase, renombrar, introducir interfaz...).
   - Mantener los tests en verde al finalizar el paso.
   - Ser lo suficientemente pequeño para revisarse y revertirse de forma aislada si algo falla.

4. **Presenta el plan antes de ejecutarlo** en el siguiente formato, y espera confirmación si el refactor es de envergadura media/alta:

```
## Objetivo del refactor
[Qué problema resuelve, referenciando el anti-patrón/principio aplicable]

## Red de seguridad
[Tests existentes / tests de caracterización a escribir antes de empezar]

## Pasos propuestos
1. [Descripción del paso] — tipo: [Extract Method / Extract Class / Rename / ...]
2. ...

## Riesgo estimado
[Bajo/Medio/Alto, y por qué]
```

5. **Ejecuta paso a paso**, ejecutando los tests tras cada paso y deteniéndote si algún test falla, para diagnosticar antes de continuar.

6. **Al finalizar**, confirma explícitamente que el comportamiento observable externo no ha cambiado (los tests de caracterización/existentes siguen en verde) y resume qué mejoró estructuralmente.

## Restricciones

- No mezclar el refactor con ninguna funcionalidad nueva en los mismos pasos/commits.
- No renombrar ni reestructurar más allá del alcance necesario para el objetivo declarado del refactor.

Ver también: [`../refactoring.md`](../knowledge/architecture/refactoring.md), [`../testing.md`](../knowledge/architecture/testing.md).
