# Logging y Observabilidad

> Documento único de referencia. La configuración específica de cada framework (Logback/SLF4J en Java, Winston/Pino en Node, Timber en Android) se documenta en la Skill correspondiente, enlazando aquí para los principios generales.

## Las tres señales de observabilidad

La observabilidad moderna se apoya en tres tipos de señal complementarios, no intercambiables:

| Señal | Responde a | Ejemplo |
|---|---|---|
| **Logs** | ¿Qué pasó exactamente, con qué detalle? | "Usuario 4821 intentó pagar el pedido 9931 y falló: fondos insuficientes" |
| **Métricas** | ¿Cómo se comporta el sistema en agregado, a lo largo del tiempo? | Latencia p95 del endpoint `/checkout` en los últimos 5 minutos |
| **Trazas distribuidas** | ¿Por dónde pasó una petición concreta a través de varios servicios? | Petición X pasó por API Gateway → Servicio de Pedidos → Servicio de Pagos, con latencia en cada salto |

Un sistema con solo logs y sin métricas/trazas obliga a "bucear" en volúmenes enormes de texto para entender problemas de rendimiento o de flujo entre servicios. Un sistema con métricas pero sin logs detallados permite ver *que* algo falla, pero no *por qué*.

## Logging

### Niveles de log y cuándo usarlos

- **ERROR**: algo ha fallado y requiere atención (una excepción no controlada, un fallo de integración con un servicio externo crítico). Debe ser accionable — si un `ERROR` se repite constantemente sin que nadie actúe, deja de tener valor de señal.
- **WARN**: una situación anómala pero recuperable (un reintento que tuvo éxito en el segundo intento, un valor de configuración fuera de rango que se corrigió con un valor por defecto).
- **INFO**: eventos de negocio relevantes en el flujo normal (pedido creado, usuario registrado, pago procesado). Debe ser el nivel activo por defecto en producción.
- **DEBUG**: detalle técnico útil para depurar, desactivado en producción por defecto pero activable puntualmente (por servicio o por request, ver logging dinámico más abajo) sin necesidad de redesplegar.

### Logging estructurado

En producción, los logs deben emitirse en un formato estructurado (JSON) parseable por el sistema de agregación de logs (ELK, CloudWatch Logs Insights, Datadog), no como texto libre.

```json
{
  "timestamp": "2026-07-30T10:15:32.481Z",
  "level": "INFO",
  "service": "pedidos-service",
  "trace_id": "4f8a1c2e-9b3d-4e21-8f6a-1234567890ab",
  "event": "pedido.creado",
  "pedido_id": "p-9931",
  "usuario_id": "4821",
  "message": "Pedido creado correctamente"
}
```

Frente al equivalente en texto libre:

```
2026-07-30 10:15:32 INFO Pedido p-9931 creado por usuario 4821
```

El formato estructurado permite filtrar, agregar y correlacionar (`WHERE event = 'pedido.creado' AND usuario_id = '4821'`) de forma directa en el sistema de logs, algo que el texto libre solo permite mediante expresiones regulares frágiles.

### Correlación de peticiones

Toda petición que entra al sistema debe llevar (o generar, si no existe) un identificador de correlación (`trace-id`/`correlation-id`), propagado:

- En la cabecera HTTP de las llamadas salientes a otros servicios (`X-Correlation-Id` o el estándar de W3C Trace Context `traceparent`).
- En el contexto de logging de cada log emitido durante el procesamiento de esa petición (MDC — Mapped Diagnostic Context — en Java/Logback; `AsyncLocalStorage` en Node.js; `ThreadLocal` equivalente en otras plataformas).

```java
// Java con SLF4J + MDC
MDC.put("traceId", traceId);
try {
    log.info("Procesando pedido {}", pedidoId); // el traceId se incluye automáticamente
    servicioPedidos.procesar(pedidoId);
} finally {
    MDC.clear();
}
```

Sin este identificador, reconstruir el flujo completo de una petición que atravesó cinco microservicios a partir de logs individuales de cada servicio es prácticamente imposible.

### Qué nunca debe loguearse

- Contraseñas, tokens de sesión/API, claves privadas.
- Números de tarjeta de crédito completos (solo los últimos 4 dígitos si es necesario para soporte).
- Datos personales sensibles sin enmascarar (DNI/NIF completo, historial médico) — enmascarar parcialmente (`***-**-1234`) si el dato es necesario para depuración.
- Objetos completos sin filtrar cuando contienen campos sensibles anidados — loguear campos explícitos, no `log.info(usuario)` sobre un objeto que incluye el hash de contraseña.

Ver también [`.../seguridad-owasp/`](.../) para la relación con *Security Logging Failures* del OWASP Top 10 (documentado en la Skill de seguridad).

### `console.log`/`System.out.println` está prohibido en producción

Estos mecanismos no respetan niveles de log, no son estructurados, no se enrutan al sistema de agregación de logs y no incluyen contexto de correlación. Usar siempre el logger configurado del framework (SLF4J/Logback en Java/Spring, Winston/Pino en Node, Timber en Android).

## Métricas

### Los cuatro "golden signals" (Google SRE)

1. **Latencia**: tiempo que tarda en responder una petición (distinguir latencia de peticiones exitosas de latencia de peticiones fallidas — una petición que falla rápido no es lo mismo que una lenta).
2. **Tráfico**: volumen de demanda sobre el sistema (peticiones/segundo, mensajes/segundo).
3. **Errores**: tasa de peticiones que fallan (por código de estado, por tipo de excepción).
4. **Saturación**: qué tan cerca está el sistema de su límite de capacidad (uso de CPU/memoria, profundidad de una cola, conexiones de pool agotadas).

### SLIs y SLOs

- **SLI (Service Level Indicator)**: una métrica concreta y medible (ej. "porcentaje de peticiones al endpoint `/checkout` que responden en menos de 300ms").
- **SLO (Service Level Objective)**: el objetivo sobre ese indicador (ej. "99% de las peticiones a `/checkout` deben responder en menos de 300ms, medido en ventanas de 30 días").

Las alertas deben definirse sobre SLOs relevantes para el negocio, no sobre umbrales arbitrarios de infraestructura sin contexto ("CPU al 80%" no dice nada por sí solo si el sistema sigue cumpliendo su SLO de latencia).

## Trazas distribuidas

OpenTelemetry es el estándar de facto para instrumentación de trazas, exportable a backends como Jaeger, Zipkin, Tempo o proveedores comerciales (Datadog, New Relic).

```typescript
// Ejemplo conceptual de instrumentación manual con OpenTelemetry (Node.js)
const span = tracer.startSpan('procesar-pago');
try {
  await servicioPagos.procesar(pedido);
  span.setStatus({ code: SpanStatusCode.OK });
} catch (error) {
  span.recordException(error);
  span.setStatus({ code: SpanStatusCode.ERROR });
  throw error;
} finally {
  span.end();
}
```

Cada *span* representa una unidad de trabajo (una llamada a otro servicio, una query a base de datos); el conjunto de spans relacionados por el mismo `trace-id` forma una *traza* que se puede visualizar como una línea de tiempo del recorrido completo de la petición.

## Dashboards y alertas

- Los dashboards deben organizarse por servicio/dominio de negocio, con los cuatro golden signals visibles de un vistazo, no como una colección desordenada de gráficas técnicas.
- Las alertas deben ser **accionables**: quien la recibe debe poder hacer algo al respecto. Alertas que se disparan constantemente sin acción posible generan "fatiga de alertas" y hacen que se ignoren también las alertas importantes.
- Preferir alertar sobre síntomas visibles para el usuario (tasa de error, latencia) sobre alertar solo por causas internas (uso de CPU) que no siempre se traducen en impacto real.

## Checklist

- [ ] ¿Los logs de producción están en formato estructurado (JSON) y se enrutan al sistema de agregación configurado?
- [ ] ¿Toda petición lleva un identificador de correlación propagado entre servicios y presente en cada log relacionado?
- [ ] ¿Se ha verificado que ningún log expone contraseñas, tokens o PII sin enmascarar?
- [ ] ¿Existen métricas de los cuatro golden signals para cada servicio crítico?
- [ ] ¿Las alertas configuradas son accionables y están vinculadas a SLOs relevantes para el negocio?

Ver también: [`debugging.md`](./debugging.md), Skill de seguridad (`seguridad-owasp/`) para la relación con OWASP A09 (Security Logging and Monitoring Failures).
