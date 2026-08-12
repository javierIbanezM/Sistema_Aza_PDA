# Standard: Git

> Norma prescriptiva de uso de Git. Ver [`commits.md`](./commits.md) para el formato de mensajes y [`branching.md`](./branching.md) para la estrategia de ramas — no se duplican aquí.

## Reglas generales

- Commits atómicos: un commit representa un único cambio lógico coherente, no una mezcla de refactor + feature + fix.
- Nunca `git push --force` sobre ramas compartidas (`main`, `develop`) salvo incidente crítico coordinado explícitamente con el equipo. Sobre ramas propias de feature no compartidas, `git push --force-with-lease` es aceptable tras un rebase interactivo.
- Nunca commitear secretos, credenciales, claves privadas o archivos de configuración local con datos sensibles — verificar `.gitignore` antes del primer commit de un proyecto nuevo, y usar herramientas de detección de secretos (`git-secrets`, `gitleaks`) en el hook de pre-commit.
- Nunca commitear archivos generados (`node_modules/`, `target/`, `build/`, `dist/`, `.env` con valores reales) — deben estar en `.gitignore` desde el primer commit.

## Rebase vs. Merge

- **Rebase interactivo** (`git rebase -i`) para limpiar el historial de una rama de feature propia antes de abrir el Pull Request (squash de commits "WIP", reordenar, reescribir mensajes) — nunca sobre una rama que otros ya han basado trabajo encima.
- **Merge** (no rebase) al integrar una rama de feature completa a `main`/`develop`, preservando la trazabilidad de qué conjunto de commits pertenece a qué feature (`git merge --no-ff` si se quiere preservar el commit de merge explícito).

## `git bisect` para localizar regresiones

Ver [`../knowledge/architecture/debugging.md`](../knowledge/architecture/debugging.md) para el uso completo de `git bisect` como técnica de depuración por bisección.

## Resolución de conflictos

- Al resolver un conflicto de merge/rebase, verificar que el resultado compila y que los tests pasan antes de continuar — un conflicto "resuelto" que rompe el build silenciosamente es peor que el conflicto sin resolver.
- Preferir resolver conflictos con el propio autor del cambio en conflicto cuando la resolución no es obvia, en lugar de adivinar la intención.

## Archivos grandes / binarios

- Usar Git LFS para archivos binarios grandes (imágenes de alta resolución, assets de diseño, modelos de ML) — no commitear binarios grandes directamente en el historial de Git estándar, ya que infla el repositorio de forma permanente incluso si el archivo se borra después.

## Checklist

- [ ] ¿El commit representa un único cambio lógico coherente?
- [ ] ¿Se ha verificado que no hay secretos ni archivos generados en el commit?
- [ ] ¿El historial de la rama de feature está limpio (rebase interactivo) antes de abrir el PR?
- [ ] ¿Se ha evitado `push --force` sobre una rama compartida?

Ver también: [`commits.md`](./commits.md), [`branching.md`](./branching.md), `CLAUDE.md` sección 9.
