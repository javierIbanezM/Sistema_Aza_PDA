# JWT vs. Sesiones de Servidor

## Contexto

Ambos resuelven cómo mantener a un usuario autenticado a través de múltiples peticiones HTTP. Ver [`../knowledge/spring/spring-security.md`](../knowledge/spring/spring-security.md) para la implementación concreta de JWT en Spring.

## Comparativa

| Criterio | JWT (stateless) | Sesión de servidor (stateful) |
|---|---|---|
| Dónde vive el estado de autenticación | En el propio token, firmado, en el cliente | En el servidor (memoria, Redis, base de datos), el cliente solo tiene un ID de sesión |
| Escalabilidad horizontal | Alta — cualquier instancia del servidor puede validar el token sin coordinación | Requiere sesión compartida entre instancias (Redis, sticky sessions) |
| Revocación inmediata (logout forzado, cuenta comprometida) | Difícil — el token es válido hasta su expiración salvo lista de revocación adicional | Trivial — se elimina la sesión del almacén central inmediatamente |
| Tamaño de la petición | Mayor (el token viaja completo en cada petición, típicamente en el header) | Menor (solo un ID de sesión, normalmente en una cookie) |
| Exposición de información | El payload del JWT es legible (base64, no cifrado) salvo que se use JWE — nunca poner datos sensibles sin cifrar en el payload | El servidor decide qué datos expone, nada viaja "de más" |
| Uso típico | APIs stateless consumidas por SPAs, apps móviles, servicios entre sí | Aplicaciones web tradicionales renderizadas en servidor |

## Cuándo elegir JWT

- API consumida por clientes diversos (app móvil, SPA, otros servicios) donde el servidor no debe mantener estado de sesión por escalabilidad.
- Arquitectura de microservicios donde varios servicios necesitan validar la identidad del usuario sin consultar un almacén de sesión centralizado en cada petición (ver `CLAUDE.md` sección 17).
- Se acepta el compromiso de revocación no inmediata, mitigado con expiración corta (ver [`../knowledge/spring/spring-security.md`](../knowledge/spring/spring-security.md), 15 minutos de access token) + refresh tokens rotables y revocables por separado.

## Cuándo elegir Sesión de servidor

- Aplicación web tradicional con un único backend (o pocos, con sesión centralizada en Redis) donde la revocación inmediata (logout, bloqueo de cuenta) es un requisito de seguridad crítico.
- El tamaño de la cookie de sesión y la simplicidad de invalidación pesan más que la escalabilidad stateless.

## Híbrido: JWT de vida corta + revocación centralizada

Un enfoque intermedio habitual: access tokens JWT de vida muy corta (minutos) que minimizan la ventana de exposición ante revocación no inmediata, combinados con refresh tokens almacenados y revocables centralizadamente (en Redis o base de datos) — el sistema obtiene la escalabilidad de JWT para el tráfico de alta frecuencia (access token, verificado sin consultar almacén central) y la capacidad de revocación del modelo de sesión para el flujo de menor frecuencia (refresh token, sí consultado centralmente).

## Recomendación por defecto de este framework

**JWT con access token de vida corta (15 minutos) + refresh token rotable y revocable centralizadamente**, según lo documentado en [`../knowledge/spring/spring-security.md`](../knowledge/spring/spring-security.md) y `CLAUDE.md` sección 5.5 — el enfoque híbrido descrito arriba, no JWT "puro" sin ningún mecanismo de revocación.

Ver también: [`../knowledge/spring/spring-security.md`](../knowledge/spring/spring-security.md), `CLAUDE.md` sección 12.1 (Identification and Authentication Failures).
