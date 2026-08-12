# Authentication vs. Authorization

> Ver [`../../decisions/jwt-vs-session.md`](../../decisions/jwt-vs-session.md) para el mecanismo de autenticación, [`../aws-security/cognito.md`](../aws-security/cognito.md) para autenticación gestionada, y [`../aws-security/iam.md`](../aws-security/iam.md) para autorización a nivel de infraestructura — este documento cubre la **distinción conceptual** entre ambas, que un error frecuente confunde con consecuencias de seguridad reales.

## La distinción que se confunde con demasiada frecuencia

- **Autenticación (AuthN)**: ¿quién eres? — verifica identidad.
- **Autorización (AuthZ)**: ¿qué puedes hacer? — verifica permiso, dado que ya se sabe quién eres.

Un sistema puede autenticar correctamente (sabe que el usuario es "Ana") y aun así fallar en autorización (permite que Ana acceda a datos de otro usuario) — son verificaciones **independientes**, y un fallo de autorización tras una autenticación exitosa es tan grave como un fallo de autenticación, aunque se detecte con menos frecuencia en revisión.

## El error más costoso: autenticar y asumir que eso basta

```java
// Antipatrón: verifica que el usuario está autenticado, pero no que TIENE PERMISO sobre ESTE recurso concreto
@GetMapping("/pedidos/{id}")
public Pedido obtener(@PathVariable String id, @AuthenticationPrincipal Usuario usuario) {
    return pedidoRepository.buscarPorId(id); // cualquier usuario autenticado puede ver CUALQUIER pedido
}

// Correcto: autenticación (quién eres) + autorización (tienes permiso sobre ESTE recurso)
@GetMapping("/pedidos/{id}")
public Pedido obtener(@PathVariable String id, @AuthenticationPrincipal Usuario usuario) {
    Pedido pedido = pedidoRepository.buscarPorId(id);
    if (!pedido.perteneceA(usuario.getId())) throw new AccesoNoAutorizadoException();
    return pedido;
}
```

Este patrón — verificar autenticación pero omitir la verificación de que el recurso específico pertenece/está permitido para ese usuario — es la causa raíz de **Broken Object Level Authorization (BOLA)**, la vulnerabilidad más común en OWASP API Security Top 10, ver [`api-security.md`](./api-security.md).

## RBAC vs. ABAC: modelos de autorización

```
RBAC (Role-Based): permisos asignados a roles, usuarios asignados a roles
  "El rol ADMIN puede eliminar pedidos"

ABAC (Attribute-Based): permisos evaluados según atributos del contexto en tiempo real
  "Un usuario puede eliminar un pedido SI es su propio pedido Y el pedido está en estado BORRADOR"
```

RBAC es más simple y suficiente para la mayoría de casos; ABAC es necesario cuando el permiso depende de condiciones dinámicas (propiedad del recurso, estado, momento) que un rol estático no puede expresar — el ejemplo de BOLA de arriba requiere, en la práctica, una verificación de tipo ABAC (propiedad del recurso), no solo RBAC.

## Autorización verificada en cada capa, no solo en el borde

```
API Gateway: verifica autenticación (¿token válido?)
Aplicación: verifica autorización de recurso (¿este usuario puede acceder a ESTE pedido concreto?)
Base de datos: permisos de menor privilegio como última barrera (ver ../sqlserver/security.md)
```

Mismo principio de defense in depth ([`defense-in-depth.md`](./defense-in-depth.md)) — la autorización no debe vivir únicamente en una capa (ej. solo en el frontend, o solo en el API Gateway), sino verificarse en cada capa que pueda tomar una decisión relevante.

## Checklist

- [ ] ¿Cada endpoint que accede a un recurso específico verifica que el usuario autenticado tiene permiso sobre ESE recurso concreto, no solo que está autenticado?
- [ ] ¿El modelo de autorización (RBAC/ABAC) corresponde a la complejidad real de las reglas de negocio?
- [ ] ¿La autorización se verifica en más de una capa, no confiando únicamente en el frontend o el API Gateway?

Ver también: [`api-security.md`](./api-security.md), [`../../decisions/jwt-vs-session.md`](../../decisions/jwt-vs-session.md), [`defense-in-depth.md`](./defense-in-depth.md).
