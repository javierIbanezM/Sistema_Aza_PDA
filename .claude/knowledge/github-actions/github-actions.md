# GitHub Actions

> Ver [`../devops/ci-cd.md`](../devops/ci-cd.md) para los principios generales de pipeline — este documento cubre la sintaxis específica de GitHub Actions. Ver [`../../decisions/github-actions-vs-jenkins.md`](../../decisions/github-actions-vs-jenkins.md) para la comparativa.

## Workflow completo aplicando las etapas del framework

```yaml
name: CI/CD
on:
  push: { branches: [main] }
  pull_request: { branches: [main] }

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { java-version: '21', distribution: 'temurin' }
      - run: mvn -B verify   # lint + build + unit + integration tests, ver devops/ci-cd.md

  security-scan:
    runs-on: ubuntu-latest
    needs: test
    steps:
      - uses: actions/checkout@v4
      - uses: aquasecurity/trivy-action@master  # SAST/escaneo de dependencias, ver CLAUDE.md 12.1
        with: { scan-type: 'fs' }

  build-and-push:
    runs-on: ubuntu-latest
    needs: [test, security-scan]
    if: github.ref == 'refs/heads/main'
    steps:
      - uses: actions/checkout@v4
      - run: |
          docker build -t $ECR_REPO:${{ github.sha }} .   # tag = SHA, nunca latest, ver devops/artifact-management.md
          docker push $ECR_REPO:${{ github.sha }}

  deploy-staging:
    needs: build-and-push
    runs-on: ubuntu-latest
    environment: staging   # gates de aprobación configurables por environment
    steps:
      - run: ./deploy.sh staging ${{ github.sha }}
```

## Jobs paralelos con dependencias explícitas (`needs`)

Ver [`../devops/ci-cd.md`](../devops/ci-cd.md) — `test` y `security-scan` corren en paralelo salvo dependencia explícita; `build-and-push` espera a ambos (`needs: [test, security-scan]`) antes de ejecutarse, aplicando el principio de paralelizar etapas independientes.

## `environments` de GitHub: aprobación manual antes de producción

```yaml
deploy-production:
  needs: deploy-staging
  environment: production   # configurado en GitHub con "required reviewers"
  runs-on: ubuntu-latest
  steps:
    - run: ./deploy.sh production ${{ github.sha }}
```

Un `environment` con revisores requeridos añade una puerta de aprobación manual antes de que el job se ejecute — mismo principio que `CLAUDE.md` sección 9.3 exige para despliegues a producción, con trazabilidad de quién aprobó.

## Secrets de GitHub: nunca en el YAML del workflow

```yaml
env:
  DB_PASSWORD: ${{ secrets.DB_PASSWORD }}  # gestionado en GitHub Secrets, nunca hardcodeado
```

Ver [`../devops/secrets-management.md`](../devops/secrets-management.md) — los secrets de GitHub Actions se enmascaran automáticamente en los logs del pipeline si aparecen accidentalmente en la salida, defensa adicional (no sustituto) de nunca loguearlos explícitamente.

## Reusable workflows: evitar duplicar pipelines entre repositorios

```yaml
jobs:
  ci:
    uses: empresa/workflows-compartidos/.github/workflows/java-ci.yml@v1
    with: { java-version: '21' }
```

Mismo principio DRY que [`../architecture/dry-kiss-yagni.md`](../architecture/dry-kiss-yagni.md) aplicado a pipelines — un workflow reusable centralizado evita que cada repositorio mantenga su propia copia (potencialmente desalineada) del mismo proceso de CI estándar.

## Checklist

- [ ] ¿Las etapas independientes (tests, escaneo de seguridad) corren en paralelo?
- [ ] ¿El despliegue a producción requiere aprobación explícita vía `environment` con revisores?
- [ ] ¿Todos los secretos usan GitHub Secrets, nunca hardcodeados en el YAML?
- [ ] ¿Se usan reusable workflows para pipelines compartidos entre repositorios, evitando duplicación?

Ver también: [`../devops/ci-cd.md`](../devops/ci-cd.md), [`../../decisions/github-actions-vs-jenkins.md`](../../decisions/github-actions-vs-jenkins.md), [`../../recipes/create-github-actions-pipeline.md`](../../recipes/create-github-actions-pipeline.md).
