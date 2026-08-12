# Pitfalls (Security) — Errores frecuentes

## Empezar por herramientas en lugar de por el threat model

```
"Vamos a poner un WAF" — sin haber identificado antes qué amenazas concretas mitiga ese WAF
```

Ver [`threat-modeling.md`](./threat-modeling.md) — cualquier control sin threat model previo es una suposición, no una decisión informada.

## Fail open ante un error inesperado

```java
try { if (verificarPermiso()) return acceso(); }
catch (Exception e) { return acceso(); } // cualquier excepción concede acceso — peligroso
```

Ver [`security-by-design.md`](./security-by-design.md).

## Autenticar sin verificar autorización de recurso específico (BOLA)

```java
public Pedido obtener(String id) { return pedidoRepository.buscarPorId(id); } // cualquier usuario ve cualquier pedido
```

Ver [`authentication-vs-authorization.md`](./authentication-vs-authorization.md), [`api-security.md`](./api-security.md).

## Mass Assignment: mapear el body completo a la entidad

```java
usuarioRepository.guardar(usuarioDelBody); // el cliente podría enviar un campo "rol" no previsto
```

Ver [`api-security.md`](./api-security.md).

## Confiar implícitamente en tráfico "interno"

```
"Esta llamada viene de otro servicio dentro de la VPC, no hace falta verificar identidad"
```

Ver [`zero-trust.md`](./zero-trust.md).

## Aceptar "cumplimos OWASP Top 10" como criterio de cierre

Una checklist genérica satisfecha no implica que las amenazas específicas del sistema estén cubiertas — ver [`secure-architecture-review.md`](./secure-architecture-review.md).

## Checklist de revisión rápida

- [ ] ¿Se ha propuesto algún control sin threat model previo que lo justifique?
- [ ] ¿Existe algún endpoint que verifica autenticación pero no autorización sobre el recurso específico?
- [ ] ¿Hay tráfico interno confiado implícitamente sin verificación de identidad?

Ver también: [`best-practices.md`](./best-practices.md).
