# Recipe: Crear un Pipeline de GitHub Actions

Aplica: [`../knowledge/github-actions/github-actions.md`](../knowledge/github-actions/github-actions.md), [`../knowledge/devops/ci-cd.md`](../knowledge/devops/ci-cd.md), [`../knowledge/devops/artifact-management.md`](../knowledge/devops/artifact-management.md).

## Pasos

### 1. Etapas de verificación, en paralelo donde no hay dependencia

```yaml
name: CI/CD
on:
  push: { branches: [main] }
  pull_request: { branches: [main] }

jobs:
  lint-and-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { java-version: '21', distribution: 'temurin' }
      - run: mvn -B verify

  security-scan:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: aquasecurity/trivy-action@master
        with: { scan-type: 'fs', severity: 'CRITICAL,HIGH', exit-code: '1' }
```

### 2. Build y publicación de artefacto, solo tras verificaciones exitosas

```yaml
  build-and-push:
    runs-on: ubuntu-latest
    needs: [lint-and-test, security-scan]
    if: github.ref == 'refs/heads/main'
    permissions: { id-token: write, contents: read }  # OIDC, sin credenciales AWS de larga duración
    steps:
      - uses: actions/checkout@v4
      - uses: aws-actions/configure-aws-credentials@v4
        with: { role-to-assume: ${{ vars.AWS_ROLE_ARN }}, aws-region: eu-west-1 }
      - run: |
          docker build -t $ECR_REPO:${{ github.sha }} .
          docker push $ECR_REPO:${{ github.sha }}
```

`permissions: id-token: write` + OIDC evita almacenar credenciales AWS de larga duración como secret — ver [`../knowledge/devops/secrets-management.md`](../knowledge/devops/secrets-management.md).

### 3. Despliegue a staging automático, producción con aprobación

```yaml
  deploy-staging:
    needs: build-and-push
    runs-on: ubuntu-latest
    environment: staging
    steps:
      - run: ./deploy.sh staging ${{ github.sha }}

  deploy-production:
    needs: deploy-staging
    runs-on: ubuntu-latest
    environment: production  # configurado con "required reviewers" en GitHub
    steps:
      - run: ./deploy.sh production ${{ github.sha }}
```

### 4. Configurar el `environment` de producción con revisores requeridos

En GitHub → Settings → Environments → `production` → añadir revisores obligatorios — ver [`../knowledge/devops/ci-cd.md`](../knowledge/devops/ci-cd.md).

### 5. Notificación de resultado

```yaml
      - if: failure()
        run: curl -X POST $SLACK_WEBHOOK -d '{"text":"Pipeline falló en ${{ github.ref }}"}'
```

## Checklist final

- [ ] ¿Las etapas independientes (tests, escaneo) corren en paralelo?
- [ ] ¿Se usa OIDC en lugar de credenciales AWS de larga duración almacenadas como secret?
- [ ] ¿El despliegue a producción requiere aprobación explícita vía `environment`?
- [ ] ¿El tag de imagen es el SHA del commit, nunca `latest`?
- [ ] Ejecutar [`../checklists/architecture.md`](../checklists/architecture.md).

Ver también: [`../snippets/devops/github-actions.yml`](../snippets/devops/github-actions.yml).
