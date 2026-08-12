# Standard: Logging

> Norma prescriptiva. Para el razonamiento completo, niveles, correlación y observabilidad, ver [`../knowledge/architecture/logging.md`](../knowledge/architecture/logging.md) — este documento resume la regla aplicable en cada situación concreta.

## Reglas obligatorias

1. **Nunca** `console.log`/`System.out.println`/`print()` en código de producción. Usar siempre el logger configurado del framework (SLF4J/Logback en Java/Spring, Winston/Pino en Node, Timber en Android).
2. **Todo log en producción es estructurado (JSON)**, nunca texto libre concatenado.
3. **Todo log incluye el `trace-id`/`correlation-id`** de la petición en curso cuando exista (ver `MDC` en Java, `AsyncLocalStorage` en Node).
4. **Nunca loguear**: contraseñas, tokens, claves de API, números de tarjeta completos, PII sin enmascarar.
5. **Nivel por defecto en producción: `INFO`**. `DEBUG` desactivado salvo activación puntual y temporal.
6. **Todo `catch` que suprime una excepción sin relanzarla debe loguear como mínimo en `WARN`**, indicando explícitamente por qué se considera seguro ignorarla — un `catch` vacío o que solo imprime sin contexto está prohibido.

## Formato mínimo de un log estructurado

```json
{
  "timestamp": "ISO-8601",
  "level": "INFO|WARN|ERROR|DEBUG",
  "service": "<nombre-del-servicio>",
  "trace_id": "<uuid-de-correlación>",
  "event": "<dominio>.<accion>",
  "message": "<mensaje legible>"
}
```

Campos adicionales específicos del evento (`pedido_id`, `usuario_id`...) se añaden como propiedades adicionales del objeto, nunca interpolados dentro del `message`.

## Qué loguear obligatoriamente

- Todo evento de negocio relevante (creación, confirmación, cancelación de una entidad de dominio principal).
- Toda excepción no controlada antes de propagarse al manejador global.
- Todo intento de autenticación fallido (con el identificador del intento, nunca la contraseña).
- Todo cambio de estado de un Circuit Breaker (ver [`../patterns/circuit-breaker.md`](../patterns/circuit-breaker.md)).
- Toda llamada saliente a un servicio externo que falla, con el código de error devuelto.

## Checklist

- [ ] ¿Se usa el logger del framework, no un `print`/`console.log` directo?
- [ ] ¿El log está en formato estructurado en producción?
- [ ] ¿Incluye `trace-id` cuando aplica?
- [ ] ¿Se ha verificado que no expone contraseñas, tokens o PII sin enmascarar?
- [ ] ¿Todo `catch` que suprime la excepción loguea el motivo?

Ver también: [`../knowledge/architecture/logging.md`](../knowledge/architecture/logging.md), [`exceptions.md`](./exceptions.md).
