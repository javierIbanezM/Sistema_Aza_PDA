# Cognito

> Ver [`../../decisions/jwt-vs-session.md`](../../decisions/jwt-vs-session.md) para el marco general de autenticación del framework — Cognito implementa el modelo JWT gestionado a nivel de AWS.

## Qué es: gestión de identidad y autenticación como servicio

Cognito gestiona registro, login, MFA, recuperación de contraseña y emisión de tokens (JWT) — evita implementar y mantener la infraestructura de autenticación propia (ver [`../spring/spring-security.md`](../spring/spring-security.md) para la alternativa autogestionada del framework) cuando el caso de uso encaja con lo que Cognito ofrece de forma nativa.

## User Pools vs. Identity Pools: propósitos distintos

| | User Pools | Identity Pools |
|---|---|---|
| Propósito | Directorio de usuarios + autenticación (login/registro) | Credenciales temporales AWS para acceder a recursos AWS directamente desde el cliente |
| Salida | Tokens JWT (ID token, access token, refresh token) | Credenciales temporales de IAM |
| Uso típico | Autenticar usuarios de una aplicación | Permitir que un cliente autenticado suba directamente a S3, sin pasar por un backend propio |

Ambos pueden combinarse: un User Pool autentica al usuario, y un Identity Pool intercambia ese token por credenciales AWS temporales con permisos acotados para acceso directo a recursos.

## Federación con proveedores externos

```
Cognito User Pool ← federado con → Google, Facebook, SAML corporativo, OIDC de terceros
```

Delega la autenticación a un proveedor de identidad externo (login social, SSO corporativo) mientras Cognito sigue emitiendo tokens JWT propios de la aplicación — el backend de la aplicación no necesita conocer los detalles de cada proveedor externo, solo validar los tokens emitidos por Cognito.

## Tokens: mismo modelo que el framework ya documenta

```
ID Token: información de identidad del usuario, para el cliente
Access Token: usado para autorizar peticiones a APIs (verificado por el backend)
Refresh Token: para renovar los anteriores sin re-autenticar
```

Ver [`../../decisions/jwt-vs-session.md`](../../decisions/jwt-vs-session.md) — mismo patrón de access token de vida corta + refresh token que el framework recomienda por defecto, gestionado por Cognito en lugar de implementado manualmente.

## Grupos y atributos personalizados: autorización básica

```
Grupo "administradores" → claim en el token → verificado por el backend para autorización
```

Cognito puede incluir grupos/atributos personalizados en el token para autorización básica basada en rol — para reglas de autorización más ricas dependientes del estado del sistema, el backend propio sigue siendo responsable (ver `CLAUDE.md` sección 12, Cognito resuelve autenticación, no reemplaza toda la lógica de autorización de negocio).

## Checklist

- [ ] ¿Se ha elegido User Pool, Identity Pool, o ambos según si se necesita autenticar usuarios, dar acceso directo a recursos AWS, o ambas cosas?
- [ ] ¿La federación con proveedores externos evita que el backend propio gestione los detalles de cada proveedor?
- [ ] ¿La autorización de negocio compleja sigue viviendo en el backend propio, no delegada por completo a los grupos/atributos de Cognito?

Ver también: [`../../decisions/jwt-vs-session.md`](../../decisions/jwt-vs-session.md), [`../spring/spring-security.md`](../spring/spring-security.md), `CLAUDE.md` sección 12.
