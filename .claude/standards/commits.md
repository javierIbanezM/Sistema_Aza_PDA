# Standard: Commits (Conventional Commits)

> Norma prescriptiva del formato de mensajes de commit. Ver [`git.md`](./git.md) para reglas generales de uso de Git.

## Formato

```
<tipo>(<alcance opcional>): <descripción en imperativo, minúscula, sin punto final>

<cuerpo opcional: qué y por qué, no cómo>

<footer opcional: BREAKING CHANGE, referencias a issues>
```

## Tipos permitidos

| Tipo | Uso |
|---|---|
| `feat` | Nueva funcionalidad visible para el usuario/consumidor de la API |
| `fix` | Corrección de un bug |
| `refactor` | Cambio de estructura interna sin alterar comportamiento observable (ver [`../knowledge/architecture/refactoring.md`](../knowledge/architecture/refactoring.md)) |
| `test` | Añadir o corregir tests, sin cambiar código de producción |
| `docs` | Cambios solo de documentación |
| `chore` | Tareas de mantenimiento sin impacto en código de producción (actualizar dependencias, configuración de tooling) |
| `perf` | Cambio que mejora el rendimiento (ver [`../prompts/performance.md`](../prompts/performance.md)) |
| `build` | Cambios en el sistema de build o dependencias externas |
| `ci` | Cambios en la configuración de CI/CD |

## Ejemplos

```
feat(pedidos): permitir cancelar un pedido confirmado dentro de las 24h

fix(auth): corregir expiración prematura del refresh token

refactor(pedidos): extraer PedidoManager en casos de uso independientes

Divide la clase PedidoManager (God Object, ver anti-patterns/god-object.md)
en CrearPedidoUseCase, ConfirmarPedidoUseCase y NotificadorPedidoEmail,
comunicados mediante PedidoConfirmadoEvent.

test(pedidos): añadir casos límite de confirmación sin líneas

docs(readme): actualizar instrucciones de instalación local

perf(pedidos): eliminar N+1 query en GET /pedidos mediante JOIN FETCH

Ver anti-patterns/n-plus-one.md — reduce de 1+N a 1 query por petición.
```

## Reglas

- Descripción en imperativo ("añade", no "añadido" ni "añadiendo"), en minúscula, sin punto final.
- Máximo ~72 caracteres en la primera línea; el detalle adicional va en el cuerpo, separado por una línea en blanco.
- `BREAKING CHANGE:` en el footer, con explicación, cuando el commit rompe compatibilidad hacia atrás (API, contrato de datos, CLI).
- Un commit no debe mezclar tipos (`feat` + `fix` + `refactor` en el mismo commit) — dividir en commits separados incluso si van en el mismo PR.
- El `<alcance>` (scope) opcional identifica el módulo/dominio afectado (`pedidos`, `auth`, `api`), útil para generar changelogs automáticos filtrados por área.

## Por qué este formato

Los mensajes estructurados permiten generar changelogs automáticamente (herramientas como `semantic-release` o `conventional-changelog`), determinar automáticamente el siguiente número de versión semántica (un `feat` sube MINOR, un `fix` sube PATCH, un `BREAKING CHANGE` sube MAJOR), y facilitan búsquedas en el historial (`git log --grep="^fix"`).

## Checklist

- [ ] ¿El tipo del commit es correcto y único (no mezcla feat+fix+refactor)?
- [ ] ¿La descripción está en imperativo y es específica, no genérica ("fix bug", "update code")?
- [ ] ¿Se documenta un `BREAKING CHANGE:` si el cambio rompe compatibilidad?
