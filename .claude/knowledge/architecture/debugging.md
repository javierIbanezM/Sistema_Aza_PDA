# Debugging sistemático

> Documento único de referencia sobre metodología de depuración. Herramientas específicas por plataforma (Chrome DevTools, Android Profiler, `jdb`, `node --inspect`) se documentan en la Skill de cada tecnología.

## Por qué una metodología, y no "probar cosas"

Depurar sin método consiste en cambiar código al azar hasta que el síntoma desaparece, sin entender la causa raíz. Esto produce "arreglos" que ocultan el síntoma sin resolver el problema, o que introducen efectos secundarios nuevos. La metodología sistemática reduce el tiempo de resolución y garantiza que el fallo se entiende, no solo se enmascara.

## El proceso

### 1. Reproducir el fallo de forma fiable

Antes de tocar nada, se necesita un caso reproducible, idealmente automatizado (un test que falla). Un fallo que "a veces pasa" sin un patrón identificado no se puede depurar con garantías — el primer objetivo es encontrar las condiciones que lo hacen reproducible de forma consistente.

- Capturar el input exacto, el estado del sistema y los pasos que llevan al fallo.
- Si el fallo es intermitente, buscar patrones: ¿ocurre bajo carga? ¿en un orden de ejecución concreto? ¿con datos concretos?
- Convertir la reproducción en un test automatizado (test de caracterización, ver [`refactoring.md`](./refactoring.md)) en cuanto sea posible: es la forma más eficiente de iterar y la evidencia de que el fallo está realmente resuelto al final.

### 2. Leer el error completo, no solo el mensaje

El stack trace completo, los logs alrededor del momento del fallo y el estado de las variables relevantes contienen más información que el mensaje de error aislado. Un error de "NullPointerException en línea 42" no dice nada sobre *por qué* esa referencia era nula; el stack trace y el flujo de llamadas sí.

### 3. Formular una hipótesis explícita

Antes de cambiar código, se escribe (mentalmente o literalmente) una hipótesis concreta y falsable: *"Creo que el campo `usuario` es nulo porque el método `buscarUsuario()` devuelve `null` cuando el email tiene mayúsculas, y la comparación en la query es sensible a mayúsculas"*.

Una hipótesis vaga ("algo está mal con la búsqueda de usuarios") no es depurable — hay que reducirla a algo que se pueda confirmar o refutar con una única observación.

### 4. Diseñar el experimento mínimo que confirma o refuta la hipótesis

- Añadir un log/breakpoint en el punto exacto que la hipótesis predice.
- Escribir un test unitario mínimo que aísla justo esa condición.
- Evitar cambiar múltiples cosas a la vez: si se modifican tres variables y el síntoma cambia, no se sabe cuál de las tres fue la causa.

### 5. Bisección (binary search) para acotar el origen

Cuando el fallo aparece en un flujo largo (un pipeline de procesamiento, una cadena de commits, un historial de datos), la bisección reduce el espacio de búsqueda exponencialmente en lugar de linealmente:

- **Bisección de código**: `git bisect` para encontrar el commit exacto que introdujo una regresión, en lugar de revisar el historial commit por commit.
- **Bisección de datos**: si un job falla procesando un lote de 10.000 registros, dividir el lote a la mitad repetidamente hasta aislar el registro concreto que causa el fallo.
- **Bisección de pipeline**: en un flujo de varias etapas (validación → transformación → persistencia → notificación), insertar puntos de verificación intermedios para localizar en qué etapa el estado deja de ser el esperado.

```bash
# Ejemplo de git bisect
git bisect start
git bisect bad                  # el commit actual tiene el bug
git bisect good v1.4.0          # esta versión anterior no lo tenía
# git bisect ejecuta un checkout intermedio automáticamente en cada paso
git bisect run npm test         # automatiza el "good"/"bad" con el resultado de los tests
```

### 6. Confirmar la causa raíz, no solo el punto de fallo

El punto donde el programa lanza la excepción no siempre es la causa raíz — a menudo es solo donde el problema se hace visible. Ejemplo: un `NullPointerException` en la capa de presentación puede tener su causa raíz en un mapeo incorrecto tres capas más abajo, en la capa de persistencia. Seguir la cadena de causalidad hacia atrás hasta el origen real antes de aplicar la corrección.

### 7. Corregir, con test que demuestre la corrección

- El test de caracterización/reproducción escrito en el paso 1 debe pasar de rojo a verde con el fix, y quedarse en la suite como test de regresión permanente.
- La corrección debe atacar la causa raíz identificada en el paso 6, no el síntoma superficial.

### 8. Post-mortem breve para fallos significativos

Para fallos en producción con impacto relevante, documentar brevemente: qué pasó, cómo se detectó, causa raíz, por qué no lo detectaron los tests existentes, y qué test/salvaguarda se añade para prevenir la recurrencia. No es necesario un proceso formal pesado para cada bug — sí para incidentes que afectaron a usuarios reales o datos de producción.

## Técnicas de aislamiento útiles

- **Rubber duck debugging**: explicar el código línea a línea a un tercero (o en voz alta) suele revelar suposiciones incorrectas que pasaban desapercibidas al leer el código en silencio.
- **Comentar/deshabilitar código por bloques**: para localizar en qué sección del código aparece el comportamiento anómalo, cuando no hay un stack trace claro que lo indique.
- **Comparar entornos**: cuando algo falla en un entorno y no en otro (producción vs. local), listar explícitamente las diferencias (versión de dependencias, variables de entorno, datos, configuración de red) en lugar de asumir cuál es la relevante.
- **Logging temporal de alta verbosidad**: añadir logs detallados alrededor del área sospechosa, ejecutar, y retirarlos una vez resuelto (o convertirlos en logs `DEBUG` permanentes si aportan valor futuro — ver [`logging-observabilidad.md`](./logging.md)).

## Errores comunes al depurar

- Cambiar código "a ver si así funciona" sin hipótesis, dejando cambios sin explicación en el código si "parece que ayudó".
- Dar por resuelto un fallo intermitente solo porque no ha vuelto a aparecer en las últimas ejecuciones, sin identificar la causa.
- Corregir el síntoma en la capa donde se detectó, sin rastrear la causa raíz real en una capa distinta.
- No convertir la reproducción en un test automatizado, dejando el sistema vulnerable a que el mismo bug reaparezca sin detección.

## Checklist

- [ ] ¿El fallo se puede reproducir de forma fiable y, si es posible, con un test automatizado?
- [ ] ¿Se ha leído el stack trace/logs completos, no solo el mensaje de error?
- [ ] ¿Existe una hipótesis explícita y falsable antes de modificar código?
- [ ] ¿Se ha identificado la causa raíz, no solo el punto donde el fallo se hace visible?
- [ ] ¿La corrección incluye un test de regresión que falla sin el fix y pasa con él?

Ver también: [`refactoring.md`](./refactoring.md), [`testing-strategy.md`](./testing.md), [`logging-observabilidad.md`](./logging.md).
