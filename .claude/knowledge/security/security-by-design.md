# Security by Design

> Ver [`threat-modeling.md`](./threat-modeling.md) para la técnica que hace esto posible en la práctica, y [`secure-sdlc.md`](./secure-sdlc.md) para cómo se integra en el proceso de desarrollo completo.

## El principio: seguridad como requisito de diseño, no como capa añadida después

Un sistema diseñado sin considerar seguridad, al que se le "añade seguridad" al final, produce controles superficiales que no encajan con la arquitectura real (un WAF que compensa validación ausente, permisos amplios porque el modelo de datos no separó los límites de confianza) — la seguridad diseñada desde el principio se integra en las decisiones estructurales mismas, no se superpone.

## Seguridad como parte de "Definition of Done", no una fase separada

Ver `CLAUDE.md` sección 21 — ningún diseño ni implementación se considera completo sin haber pasado por threat modeling (para cambios significativos) y sin cumplir los standards de seguridad aplicables ([`../../standards/validation.md`](../../standards/validation.md), [`../../standards/logging-standard.md`](../../standards/logging-standard.md)) — mismo nivel de exigencia que tests o revisión de código, no una verificación opcional "si hay tiempo".

## Fail secure: el estado por defecto ante un fallo es el más restrictivo

```java
// Antipatrón: si la verificación de permisos falla por una excepción inesperada, permite el acceso
try {
    if (verificarPermiso(usuario, recurso)) return acceso();
} catch (Exception e) {
    return acceso(); // fail open — PELIGROSO
}

// Correcto: ante cualquier duda o fallo, denegar por defecto
try {
    if (verificarPermiso(usuario, recurso)) return acceso();
    return denegado();
} catch (Exception e) {
    logger.error("Error verificando permiso", e);
    return denegado(); // fail secure — el estado por defecto ante error es restrictivo
}
```

Un sistema que "falla abierto" (fail open) convierte cualquier bug o excepción inesperada en una vulnerabilidad de bypass de seguridad — diseñar explícitamente para que el estado por defecto ante cualquier ambigüedad o error sea el más restrictivo, nunca el más permisivo.

## Principio de menor privilegio como decisión de diseño, no de configuración posterior

Ver [`../aws-security/iam.md`](../aws-security/iam.md) y [`../sqlserver/security.md`](../sqlserver/security.md) para la aplicación concreta — el diseño del sistema debe contemplar desde el inicio **qué componente necesita qué permiso exacto**, no diseñar componentes con acceso amplio "por conveniencia de desarrollo" con la intención de restringir después (intención que en la práctica rara vez se cumple).

## Superficies de confianza explícitas en el diseño

```
Todo input de un límite de confianza distinto (usuario externo, sistema de terceros, incluso
otro servicio interno) se trata como no confiable hasta validarse — nunca "confiamos en que
el frontend ya valida" o "confiamos en que el otro equipo ya sanitiza"
```

Ver [`zero-trust.md`](./zero-trust.md) — el diseño nunca asume implícitamente que otro componente ya hizo la validación necesaria, incluso si en teoría debería haberlo hecho.

## Checklist

- [ ] ¿La seguridad se consideró en el diseño inicial, no añadida como capa posterior?
- [ ] ¿El sistema falla de forma segura (deniega) ante errores/excepciones inesperadas, nunca permite por defecto?
- [ ] ¿Los componentes se diseñaron con el permiso mínimo necesario desde el inicio, no con acceso amplio a restringir "después"?

Ver también: [`threat-modeling.md`](./threat-modeling.md), [`secure-sdlc.md`](./secure-sdlc.md), [`defense-in-depth.md`](./defense-in-depth.md).
