# Standard: Exceptions

> Norma prescriptiva de manejo de errores/excepciones, transversal a todos los lenguajes del framework.

## Jerarquía de excepciones

- Distinguir siempre **excepciones de dominio** (violaciones de reglas de negocio, esperadas y parte del contrato de la operación — `InsufficientFundsException`, `PedidoYaConfirmadoException`) de **excepciones técnicas/de infraestructura** (fallos inesperados — timeout de red, conexión de base de datos perdida).
- Las excepciones de dominio extienden una clase base propia del proyecto (`DomainException`), nunca directamente `RuntimeException`/`Exception` sin tipar, para poder distinguirlas y mapearlas a códigos HTTP apropiados en el borde de la aplicación (ver `CLAUDE.md` sección 15.1).
- Nunca usar excepciones para control de flujo normal (una búsqueda que no encuentra resultados no siempre es una excepción — evaluar si el caso "no encontrado" es parte del flujo esperado, en cuyo caso un `Optional`/`null` tipado o un resultado explícito es más apropiado que lanzar y capturar).

## Reglas de manejo

1. **Nunca capturar una excepción sin hacer nada con ella** (`catch (Exception e) {}` vacío está prohibido). Como mínimo, loguear con contexto suficiente (ver [`logging-standard.md`](./logging-standard.md)).
2. **Nunca capturar `Exception`/`Throwable` genérico** salvo en el manejador global de errores del borde de la aplicación (`@ControllerAdvice`, middleware de Express). En el resto del código, capturar el tipo específico esperado.
3. **No perder la causa raíz**: al relanzar una excepción envuelta en otra, siempre pasar la excepción original como `cause` (`throw new ServicioException("...", e)` en Java; `throw new Error("...", { cause: e })` en JS/TS moderno) — perder el stack trace original dificulta la depuración (ver [`../knowledge/architecture/debugging.md`](../knowledge/architecture/debugging.md)).
4. **Mensajes de excepción descriptivos y accionables**: incluir el identificador relevante (`"Pedido p-123 no encontrado"`, no solo `"No encontrado"`).
5. **No exponer detalles internos en la respuesta al cliente**: un stacktrace completo, nombres de tablas o mensajes de excepción de infraestructura nunca deben llegar en la respuesta HTTP al consumidor final (ver `CLAUDE.md` sección 12, Security Misconfiguration). El mensaje interno se loguea; el cliente recibe un mensaje de error controlado (ver formato en `CLAUDE.md` sección 15.1).

## Manejo centralizado en el borde

- Java/Spring: `@ControllerAdvice` + `@ExceptionHandler` por tipo de excepción de dominio, mapeando a códigos HTTP (`400`, `404`, `409`...).
- Node/NestJS: `ExceptionFilter` global, con filtros específicos por tipo de excepción de dominio si el mapeo a HTTP difiere.
- Node/Express: middleware de manejo de errores al final de la cadena (`(err, req, res, next) => {...}`).

## Retries y excepciones transitorias

- Distinguir errores **transitorios** (timeout de red puntual, deadlock de base de datos) de errores **permanentes** (validación fallida, recurso no encontrado). Solo los transitorios son candidatos a reintento automático (con backoff exponencial), nunca los permanentes — reintentar una validación fallida no la va a hacer pasar.
- Ver [`../patterns/circuit-breaker.md`](../patterns/circuit-breaker.md) para protección ante fallos repetidos de un servicio externo.

## Checklist

- [ ] ¿Las excepciones de dominio están tipadas y extienden una base común identificable?
- [ ] ¿Ningún `catch` está vacío o silencia el error sin logging?
- [ ] ¿Se preserva la causa raíz al envolver una excepción?
- [ ] ¿El manejador global mapea cada tipo de excepción a un código HTTP apropiado, sin exponer detalles internos?
- [ ] ¿Los reintentos automáticos solo aplican a errores transitorios, no a errores de validación/negocio?

Ver también: [`../knowledge/architecture/debugging.md`](../knowledge/architecture/debugging.md), [`logging-standard.md`](./logging-standard.md), `CLAUDE.md` sección 15.1.
