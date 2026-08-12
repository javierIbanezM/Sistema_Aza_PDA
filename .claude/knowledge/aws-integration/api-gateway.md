# API Gateway

> Ver [`../aws-compute/lambda.md`](../aws-compute/lambda.md) para el backend serverless típico detrás de API Gateway, y [`../../standards/api.md`](../../standards/api.md) para las convenciones REST del framework que API Gateway expone.

## Qué es: puerta de entrada gestionada para APIs

Gestiona el ciclo de vida completo de exposición de una API (routing, autenticación, throttling, transformación de peticiones/respuestas, versionado por stage) delante de Lambda, contenedores, o cualquier endpoint HTTP — sin gestionar infraestructura de servidor propia para esta capa.

## REST API vs. HTTP API: cuál elegir

| | REST API | HTTP API |
|---|---|---|
| Coste | Mayor | Significativamente menor |
| Funcionalidades | Completas (transformación de request/response, caché integrado, WAF nativo) | Subconjunto más simple, suficiente para la mayoría de casos |
| Latencia | Ligeramente mayor | Menor |

HTTP API como opción por defecto para la mayoría de APIs REST estándar (menor coste y latencia); REST API cuando se necesita alguna funcionalidad avanzada específica no disponible en HTTP API (transformación compleja de payload, caché de respuesta integrado).

## Autorizadores: quién puede llamar a qué

```
Authorizer tipo Cognito: valida el JWT emitido por Cognito automáticamente
Authorizer tipo Lambda: lógica de autorización custom, para casos no cubiertos por Cognito/IAM directamente
```

Ver [`../aws-security/cognito.md`](../aws-security/cognito.md) — API Gateway puede validar tokens JWT de Cognito nativamente sin código adicional en el backend, o delegar a una función Lambda de autorización personalizada para lógica más compleja (verificar contra un sistema de permisos propio, por ejemplo).

## Throttling y quotas: proteger el backend

```
Rate limit: 1000 req/s por defecto, Burst: 2000
Usage Plan por API Key: 10000 req/día para el plan "básico"
```

Ver `CLAUDE.md` sección 12.1 (mitigación de abuso) — el throttling a nivel de API Gateway protege al backend (Lambda, contenedores) de sobrecarga antes de que la petición siquiera llegue a él, complementando cualquier rate limiting adicional a nivel de aplicación.

## Transformación de petición/respuesta sin tocar el backend

```
Mapping Template: transforma el payload de entrada al formato que el backend Lambda espera,
sin que el backend necesite conocer el formato original del cliente
```

Útil para desacoplar el contrato público de la API del formato interno que consume el backend, o para adaptar clientes legados sin modificar el backend — usar con moderación, ya que la lógica de transformación en Mapping Templates (VTL) es menos mantenible/testeable que código de aplicación normal.

## Checklist

- [ ] ¿Se ha elegido HTTP API por defecto, REST API solo si se necesita una funcionalidad avanzada específica?
- [ ] ¿Existe un autorizador (Cognito o Lambda) que valida la identidad antes de que la petición llegue al backend?
- [ ] ¿El throttling está configurado para proteger al backend de sobrecarga/abuso?

Ver también: [`../aws-compute/lambda.md`](../aws-compute/lambda.md), [`../aws-security/cognito.md`](../aws-security/cognito.md), [`../../standards/api.md`](../../standards/api.md).
