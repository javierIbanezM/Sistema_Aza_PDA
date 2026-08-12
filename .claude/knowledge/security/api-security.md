# API Security

> Ver [`../../standards/api.md`](../../standards/api.md) para las convenciones generales de API del framework y [`../../standards/validation.md`](../../standards/validation.md) para validación de entrada — este documento cubre las amenazas **específicas** de APIs (OWASP API Security Top 10) no cubiertas por esos documentos de convención.

## BOLA (Broken Object Level Authorization): la vulnerabilidad de API más común

Ver [`authentication-vs-authorization.md`](./authentication-vs-authorization.md) para el detalle completo — un endpoint que acepta un identificador de recurso sin verificar que el usuario autenticado tiene permiso sobre **ese** recurso específico. Verificable sistemáticamente: para cada endpoint que recibe un ID de recurso, confirmar explícitamente que existe una comprobación de propiedad/permiso antes de devolver o modificar el dato.

## Mass Assignment: aceptar más campos de los que se pretendía

```java
// Antipatrón: el DTO se mapea directamente a la entidad completa
@PutMapping("/usuarios/{id}")
public Usuario actualizar(@PathVariable String id, @RequestBody Usuario usuario) {
    return usuarioRepository.guardar(usuario); // si Usuario tiene un campo "rol", el cliente podría enviarlo y escalar privilegios
}

// Correcto: DTO explícito con solo los campos que el cliente debe poder modificar
public record ActualizarUsuarioRequest(String nombre, String email) { } // sin "rol"

@PutMapping("/usuarios/{id}")
public Usuario actualizar(@PathVariable String id, @RequestBody ActualizarUsuarioRequest request) {
    Usuario usuario = usuarioRepository.buscarPorId(id);
    usuario.actualizarDatos(request.nombre(), request.email()); // rol nunca se toca desde esta petición
    return usuarioRepository.guardar(usuario);
}
```

Ver [`../../standards/dto.md`](../../standards/dto.md) — un DTO de entrada explícito con solo los campos permitidos (nunca mapear el body completo directamente a la entidad de dominio) es la mitigación estructural, no una validación adicional a posteriori.

## Rate Limiting por recurso, no solo global

```
Rate limit global: 1000 req/min por IP — protege contra abuso general
Rate limit específico en /auth/login: 5 intentos/15min por cuenta — protege contra fuerza bruta específicamente
```

Ver `CLAUDE.md` sección 12.1 — un único rate limit global no protege adecuadamente endpoints sensibles (login, recuperación de contraseña) que necesitan límites mucho más estrictos que el tráfico general de la API.

## Excesiva exposición de datos: devolver solo lo necesario

```java
// Antipatrón: devuelve la entidad completa, incluidos campos internos/sensibles
return usuarioRepository.buscarPorId(id); // incluye hash de contraseña, tokens internos...

// Correcto: DTO de salida explícito con solo los campos que el cliente necesita
return new UsuarioResponseDto(usuario.getId(), usuario.getNombre(), usuario.getEmail());
```

Mismo principio que Mass Assignment mirado desde la dirección opuesta — un DTO de salida explícito evita exponer campos internos que la aplicación cliente nunca debería recibir, incluso si actualmente "no los usa" (una API de terceros que inspecciona la respuesta completa podría capturarlos igualmente).

## Falta de límite de recursos: paginación como control de seguridad, no solo de rendimiento

Ver [`../../standards/api.md`](../../standards/api.md) — un endpoint de colección sin límite máximo de página permite a un cliente (malicioso o no) solicitar volúmenes de datos desproporcionados en una única petición, un vector tanto de degradación de rendimiento como de exfiltración masiva de datos.

## Checklist

- [ ] ¿Cada endpoint con un identificador de recurso verifica autorización sobre ese recurso específico (BOLA)?
- [ ] ¿Los endpoints de escritura usan DTOs de entrada explícitos, nunca mapeo directo del body a la entidad completa (Mass Assignment)?
- [ ] ¿Los endpoints sensibles (login, recuperación de contraseña) tienen rate limiting específico más estricto que el global?
- [ ] ¿Las respuestas usan DTOs de salida explícitos, sin exponer campos internos innecesarios?

Ver también: [`authentication-vs-authorization.md`](./authentication-vs-authorization.md), [`../../standards/api.md`](../../standards/api.md), [`../../standards/dto.md`](../../standards/dto.md), `CLAUDE.md` sección 12.
