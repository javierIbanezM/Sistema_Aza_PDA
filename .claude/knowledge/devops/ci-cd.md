# CI/CD

> Ver `CLAUDE.md` sección 9.3 para el pipeline mínimo ya definido a nivel de framework. Este documento profundiza en el diseño de pipeline como disciplina de ingeniería, no repite esa base.

## Principio: el pipeline es la única vía a producción

Ningún despliegue manual "por esta vez" — si el pipeline no puede hacer algo (una migración compleja, un cambio de configuración), el pipeline se mejora para poder hacerlo, no se salta. Un despliegue manual rompe la trazabilidad (¿qué se desplegó exactamente, quién lo aprobó, con qué versión de cada dependencia) que es precisamente el valor central de tener un pipeline.

## Etapas y su propósito específico

```
Lint → Build → Unit Tests → SAST → Build Artefacto/Imagen → Integration Tests →
Deploy Staging → E2E Tests → Aprobación (si aplica) → Deploy Producción → Smoke Tests
```

Cada etapa tiene un propósito de **detección temprana**: fallar en Lint es más barato que fallar en producción — el pipeline está ordenado deliberadamente de más rápido/barato a más lento/caro, para dar feedback lo antes posible ante cualquier problema.

## Pipeline como código, versionado junto al código que construye

```yaml
# .github/workflows/ci.yml vive en el MISMO repositorio que la aplicación
```

El pipeline nunca se configura solo en la UI de la herramienta de CI/CD — se define como código, versionado, revisado en PR igual que cualquier otro cambio (ver [`../../standards/git.md`](../../standards/git.md)), permitiendo rastrear cuándo y por qué cambió el proceso de build/deploy.

## Builds reproducibles: mismo commit, mismo artefacto

```
Tag de imagen = SHA del commit, NUNCA "latest" en producción (ver CLAUDE.md sección 9.3)
```

Un build no determinista (dependencias con rango de versión abierto, timestamp embebido que cambia el hash) rompe la capacidad de verificar que "lo que está en producción es exactamente lo que pasó por el pipeline" — ver [`../maven/maven.md`](../maven/maven.md)/[`../gradle/gradle.md`](../gradle/gradle.md) para la disciplina de versiones fijas que sostiene esto.

## Paralelización de etapas independientes

```
        ┌── Unit Tests (Java) ──┐
Build ──┤                        ├── Build Artefacto
        └── SAST ────────────────┘
```

Etapas sin dependencia real entre sí (tests unitarios y análisis estático, por ejemplo) se ejecutan en paralelo, no secuencialmente — reduce el tiempo total del pipeline sin sacrificar ninguna verificación.

## Feedback rápido: fallar rápido, informar claro

Un pipeline que tarda 40 minutos en decir "falló el lint" desalienta la disciplina de arreglarlo inmediatamente — ordenar las etapas de más rápidas a más lentas, y que el mensaje de fallo sea específico y accionable (qué falló, dónde, cómo reproducirlo localmente), no un log genérico que obliga a bucear.

## Checklist

- [ ] ¿Todo despliegue pasa por el pipeline, sin excepciones manuales?
- [ ] ¿El pipeline está definido como código, versionado junto a la aplicación?
- [ ] ¿Los builds son reproducibles (versiones fijas, tag de imagen = SHA de commit)?
- [ ] ¿Las etapas independientes se ejecutan en paralelo?

Ver también: [`deployment-strategies.md`](./deployment-strategies.md), [`release-management.md`](./release-management.md), `CLAUDE.md` sección 9.3.
