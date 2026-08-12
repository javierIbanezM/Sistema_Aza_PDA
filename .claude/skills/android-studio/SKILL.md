# Skill: Android Studio

## Rol

Senior Android Developer. Guía la implementación de aplicaciones Android, aplicando siempre primero las reglas transversales de `architecture` (SOLID, DDD, Clean Architecture) y, sobre ellas, el conocimiento específico de cada tecnología del ecosistema Android — cada una en su propia carpeta de `knowledge/`, nunca mezcladas.

## Cuándo usar / no usar

**Usar** ante: cualquier tarea de implementación, revisión o diagnóstico sobre una app Android (pantallas Compose, ViewModels, persistencia Room, red Retrofit, DI Hilt, trabajo en segundo plano WorkManager, Firebase).

**No usar** ante: decisiones de arquitectura general no específicas de Android (ir primero a `skills/architecture/SKILL.md`), lógica de backend consumida por la app (ir a `skills/java/`/`skills/node/`/`skills/sqlserver/`).

## Flujo

1. Cargar primero `skills/architecture/SKILL.md` si la tarea implica una decisión de diseño no trivial.
2. Identificar qué tecnología concreta está en juego (¿Kotlin puro? ¿Android platform? ¿Compose? ¿Room? ¿Retrofit? ¿Hilt? ¿WorkManager? ¿Firebase? ¿Material 3?) y cargar el `README.md` de `knowledge/<tecnología>/` correspondiente.
3. Consultar el documento específico dentro de esa carpeta.
4. Consultar `patterns/` y `anti-patterns/` (globales) antes de implementar.
5. Aplicar `standards/` (globales).
6. Usar la `recipes/android-*.md` correspondiente si la tarea coincide con una receta ya definida.
7. Implementar con el cambio mínimo necesario.
8. Ejecutar `checklists/architecture.md` antes de cerrar la tarea.

## Documentación a consultar

| Tecnología | Carpeta |
|---|---|
| Kotlin (lenguaje) | [`../../knowledge/kotlin/`](../../knowledge/kotlin/) |
| Android (plataforma) | [`../../knowledge/android/`](../../knowledge/android/) |
| Jetpack Compose | [`../../knowledge/compose/`](../../knowledge/compose/) |
| Room | [`../../knowledge/room/`](../../knowledge/room/) |
| Retrofit | [`../../knowledge/retrofit/`](../../knowledge/retrofit/) |
| Firebase | [`../../knowledge/firebase/`](../../knowledge/firebase/) |
| Hilt | [`../../knowledge/hilt/`](../../knowledge/hilt/) |
| WorkManager | [`../../knowledge/workmanager/`](../../knowledge/workmanager/) |
| Material Design 3 | [`../../knowledge/material3/`](../../knowledge/material3/) |

Para principios transversales: [`../../knowledge/architecture/`](../../knowledge/architecture/) — no duplicado aquí.

## Patrones a consultar

[`../../patterns/`](../../patterns/) (global): `repository.md`, `observer.md`, `adapter.md`.

## Anti-patrones a consultar

[`../../anti-patterns/`](../../anti-patterns/) (global): `memory-leak.md` (fugas de Context), `god-object.md`.

## Standards a aplicar

[`../../standards/`](../../standards/) (global): `naming.md`, `dto.md`, `api.md`, `exceptions.md`, `logging-standard.md`, `validation.md`.

## Checklists a ejecutar

[`../../checklists/architecture.md`](../../checklists/architecture.md).

## Prompts a utilizar

[`../../prompts/`](../../prompts/) (global): `review.md`, `refactor.md`, `generate-tests.md`, `debug.md`, `performance.md`.

## Recipes a utilizar

[`../../recipes/`](../../recipes/) (plano, prefijo `android-`): `android-create-compose-screen.md`, `android-create-room-database.md`, `android-create-webview.md`, `android-create-login.md`.

## Snippets a reutilizar

[`../../snippets/android/`](../../snippets/android/).

## Decisions relevantes

[`../../decisions/`](../../decisions/): `jwt-vs-session.md`, `rest-vs-graphql.md`. Pendiente `compose-vs-xml.md`.

## Ejemplos

[`../../knowledge/android/examples.md`](../../knowledge/android/examples.md).
