# Standard: Branching

> Norma prescriptiva de nomenclatura y estrategia de ramas. Ver [`git.md`](./git.md) y [`commits.md`](./commits.md).

## Estrategia base: Trunk-Based / GitHub Flow simplificado

- `main` es la única rama de larga duración, siempre desplegable.
- Toda funcionalidad, fix o cambio se desarrolla en una rama de corta duración creada desde `main`, y vuelve a `main` vía Pull Request.
- Si el proyecto necesita un entorno de staging con ciclo de despliegue distinto, usar una rama `develop` adicional (GitFlow simplificado) — solo si hay una razón real (releases coordinadas, ventanas de QA), no por defecto.

## Nomenclatura de ramas

| Prefijo | Uso | Ejemplo |
|---|---|---|
| `feature/` | Nueva funcionalidad | `feature/PED-123-cancelar-pedido-confirmado` |
| `fix/` | Corrección de bug (no urgente) | `fix/PED-456-descuento-vip-calculo-incorrecto` |
| `hotfix/` | Corrección urgente sobre producción | `hotfix/PED-789-caida-checkout` |
| `refactor/` | Refactor sin cambio funcional | `refactor/PED-234-extraer-pedido-manager` |
| `chore/` | Mantenimiento (dependencias, tooling) | `chore/actualizar-spring-boot-3.3` |
| `docs/` | Solo documentación | `docs/actualizar-readme-instalacion` |

Formato: `<prefijo>/<identificador-de-ticket-si-existe>-<descripción-corta-en-kebab-case>`. Sin ticket asociado, omitir el identificador pero mantener el prefijo y la descripción.

## Ciclo de vida de una rama

1. Crear desde `main` actualizado (`git pull origin main` antes de crear la rama).
2. Commits atómicos siguiendo [`commits.md`](./commits.md).
3. Rebase interactivo para limpiar el historial antes de abrir el PR (ver [`git.md`](./git.md)).
4. Pull Request con la plantilla correspondiente (`.claude/templates/`), revisión y aprobación según `CLAUDE.md` sección 9.2.
5. Merge a `main` (no rebase, para preservar trazabilidad de la feature completa).
6. Eliminar la rama tras el merge — ramas de feature no deben acumularse indefinidamente en el repositorio remoto.

## Ramas protegidas

- `main` (y `develop` si existe): requieren Pull Request, al menos una aprobación, build/tests en verde antes de permitir el merge. Ver `CLAUDE.md` sección 9.2 (branch policies).
- Prohibido commitear directamente a una rama protegida, incluso para cambios triviales — todo pasa por PR, sin excepción, para mantener el historial de revisión consistente.

## Hotfixes

- Se crean desde `main` (no desde la rama de feature en curso), se prueban de forma aislada, y se despliegan con el mismo pipeline de CI/CD que cualquier otro cambio — nunca se salta el pipeline "porque es urgente" (ver `CLAUDE.md` sección 9.3).
- Tras el hotfix, verificar si la misma corrección debe aplicarse también en `develop` u otras ramas de larga duración si existen, para no perderla en el siguiente merge.

## Checklist

- [ ] ¿El nombre de la rama sigue el prefijo y formato correctos?
- [ ] ¿La rama se creó desde `main` actualizado?
- [ ] ¿El historial está limpio (rebase) antes de abrir el PR?
- [ ] ¿La rama se elimina tras el merge?
