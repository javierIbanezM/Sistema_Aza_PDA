# Prompt: Generar Suite de Tests

## Propósito

Guiar la generación de una suite de tests completa para una unidad de código (función, clase, caso de uso), siguiendo la estrategia documentada en [`../testing.md`](../knowledge/architecture/testing.md).

## Instrucciones para Claude Code

Al recibir este prompt junto con el código a testear:

1. **Identifica el tipo de test apropiado** según lo que se está testeando (ver pirámide de testing en [`../testing.md`](../knowledge/architecture/testing.md)):
   - Lógica de dominio / caso de uso puro → Unit test, sin I/O real.
   - Repositorio / integración con infraestructura real → Integration test (Testcontainers u equivalente).
   - Flujo de negocio crítico end-to-end → E2E test (solo si se solicita explícitamente, no por defecto).

2. **Enumera los escenarios a cubrir** antes de escribir código, en esta categoría mínima:
   - Camino feliz (al menos uno, con datos válidos representativos).
   - Validación fallida (cada regla de negocio/invariante que puede violarse).
   - Casos límite (colecciones vacías, valores en el borde de un rango, valores nulos/opcionales).
   - Concurrencia, si la unidad bajo test tiene estado compartido relevante.

3. **Usa dobles de test apropiados** (ver tabla Mock/Stub/Fake/Spy en [`../testing.md`](../knowledge/architecture/testing.md)): preferir Fakes/Stubs cuando el test solo verifica un resultado; reservar Mocks para verificar interacciones/efectos secundarios que son el propósito explícito del test.

4. **Nombra cada test** siguiendo `should_<resultado_esperado>_when_<condición>` o `given/when/then` anidado, de forma que el nombre por sí solo comunique qué falló sin leer el cuerpo.

5. **Verifica determinismo**: si el código depende de tiempo (`Date.now()`, `Instant.now()`), fija un reloj/mock explícito. No uses `sleep`/`setTimeout` arbitrarios para sincronizar asincronía.

## Formato de salida esperado

```
## Escenarios identificados
- [Camino feliz] ...
- [Validación] ...
- [Límite] ...

## Tests generados
[Código de los tests, agrupados por escenario con describe/it o @Nested según el framework del proyecto]
```

## Restricciones

- No testear detalles de implementación privados; testear comportamiento observable público.
- No generar tests triviales de getters/setters sin lógica.
- Si el código bajo test tiene un anti-patrón que dificulta el testing (ej. [Anemic Domain Model](../anti-patterns/anemic-domain-model.md) con lógica externa difícil de aislar, o dependencias no inyectadas que impiden sustituir colaboradores), señálalo explícitamente antes de generar los tests, en lugar de generar tests frágiles alrededor del problema estructural.

Ver también: [`../testing.md`](../knowledge/architecture/testing.md), [`.../checklists/architecture.md`](../checklists/architecture.md).
