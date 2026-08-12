# Recipe: Configurar Escaneo de Dependencias y SBOM

Aplica: [`../knowledge/security/supply-chain-security.md`](../knowledge/security/supply-chain-security.md), [`../knowledge/devops/artifact-management.md`](../knowledge/devops/artifact-management.md).

## Pasos

### 1. Fijar versiones exactas, nunca rangos abiertos

```json
// package.json — verificar que no hay ^ ni ~ en dependencias de producción críticas
"dependencies": { "express": "4.19.2" }
```

```bash
# Confirmar que el lockfile está committeado
git ls-files | grep -E "package-lock.json|pom.xml|*.lock"
```

Ver [`../knowledge/security/supply-chain-security.md`](../knowledge/security/supply-chain-security.md).

### 2. Añadir el escaneo de vulnerabilidades como gate bloqueante

```yaml
# Extiende el pipeline ya definido en knowledge/devops/artifact-management.md
security-scan:
  runs-on: ubuntu-latest
  steps:
    - uses: actions/checkout@v4
    - uses: aquasecurity/trivy-action@master
      with: { scan-type: 'fs', severity: 'CRITICAL,HIGH', exit-code: '1' }
```

### 3. Generar SBOM como artefacto del build

```yaml
    - name: Generar SBOM
      run: syft packages dir:. -o cyclonedx-json > sbom.json
    - uses: actions/upload-artifact@v4
      with: { name: sbom, path: sbom.json }
```

### 4. Publicar el SBOM junto al artefacto

```bash
# Adjuntar el SBOM al artefacto publicado, para poder consultarlo ante una vulnerabilidad futura
# sin tener que re-analizar el proyecto
```

Ver [`../knowledge/security/supply-chain-security.md`](../knowledge/security/supply-chain-security.md).

### 5. Configurar re-escaneo periódico, no solo en el build

```yaml
# Job programado, independiente del pipeline de CI, para detectar CVEs publicados
# DESPUÉS de que el artefacto ya está en producción
on:
  schedule:
    - cron: '0 6 * * *'  # diario
jobs:
  rescan-produccion:
    steps:
      - run: trivy image --severity CRITICAL,HIGH $ECR_REPO:produccion-actual
```

Ver [`../knowledge/security/vulnerability-management.md`](../knowledge/security/vulnerability-management.md) — el escaneo en el pipeline no detecta vulnerabilidades descubiertas después del build.

### 6. Alertar ante hallazgos, con severidad real (no solo CVSS)

Ver [`../knowledge/security/vulnerability-management.md`](../knowledge/security/vulnerability-management.md) — el hallazgo del re-escaneo pasa por triage antes de determinar urgencia de remediación.

## Checklist final

- [ ] ¿Las dependencias usan versiones exactas con lockfile committeado?
- [ ] ¿El escaneo de vulnerabilidades es bloqueante en el pipeline, no solo informativo?
- [ ] ¿Se genera y publica un SBOM junto a cada artefacto?
- [ ] ¿Existe re-escaneo periódico de lo ya desplegado en producción, no solo en el momento del build?

Ver también: [`../snippets/security/dependency-scan.yml`](../snippets/security/dependency-scan.yml), [`../knowledge/devops/artifact-management.md`](../knowledge/devops/artifact-management.md).
