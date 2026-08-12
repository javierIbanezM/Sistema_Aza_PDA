# Supply Chain Security

> Ver [`../devops/artifact-management.md`](../devops/artifact-management.md) para el escaneo de vulnerabilidades ya integrado en el pipeline — este documento cubre la seguridad de la cadena de suministro de software de forma más amplia: procedencia, integridad y confianza en cada dependencia y herramienta que participa en construir el artefacto final.

## El problema: el software que se ejecuta rara vez es 100% código propio

Un servicio típico depende de decenas o cientos de librerías de terceros (directas e indirectas), imágenes base de contenedor, y herramientas de build — cada una es un vector potencial de compromiso que no está bajo el control directo del equipo, y que un escaneo de vulnerabilidades conocidas (CVEs) por sí solo no cubre completamente (un paquete comprometido deliberadamente por un atacante no tiene necesariamente un CVE público).

## SBOM (Software Bill of Materials): inventario verificable de qué hay realmente en el artefacto

```bash
# Generar un SBOM en formato estándar (CycloneDX/SPDX) como parte del build
syft packages docker:pedidos-service:abc123f -o cyclonedx-json > sbom.json
```

Un SBOM lista **todas** las dependencias (directas e indirectas) que componen el artefacto final, con su versión exacta — permite responder rápidamente "¿este servicio usa la librería X con la versión vulnerable Y?" ante una vulnerabilidad recién descubierta, sin tener que auditar manualmente cada proyecto.

## Procedencia verificable: de dónde viene realmente cada artefacto

```
Attestation firmada criptográficamente: "este artefacto se construyó desde el commit X,
en el pipeline Y, con estas dependencias exactas" — verificable antes de desplegar
```

Ver [`../devops/artifact-management.md`](../devops/artifact-management.md) (firma de artefactos) — la procedencia verificable responde no solo "¿qué contiene este artefacto?" sino "¿puedo confiar en que se construyó de la forma que digo que se construyó, sin manipulación en el camino?".

## Pinning de dependencias: versiones exactas, no rangos abiertos

```json
// Antipatrón: rango abierto, la versión instalada puede cambiar sin control explícito
"dependencies": { "libreria-x": "^2.0.0" }

// Preferido: versión exacta + lockfile committeado
"dependencies": { "libreria-x": "2.4.1" }
```

Ver [`../maven/maven.md`](../maven/maven.md) (Java) o el equivalente `package-lock.json` de Node.js (ver [`../node/README.md`](../node/README.md)) — un rango abierto significa que un build futuro puede instalar una versión distinta (potencialmente comprometida) sin que nadie lo decida explícitamente; fijar versiones exactas con lockfile versionado da control total sobre qué se instala exactamente.

## Verificar la reputación de una dependencia antes de adoptarla

```
Antes de añadir una dependencia nueva:
¿Tiene mantenimiento activo? ¿Cuántos mantenedores? ¿Historial de respuesta a vulnerabilidades?
¿Es una dependencia con muchas transitivas propias, ampliando la superficie de ataque indirectamente?
```

Añadir una dependencia es, en la práctica, extender confianza a su cadena completa de mantenedores y de sus propias dependencias — una evaluación mínima antes de adoptarla (no solo "resuelve mi problema") reduce el riesgo de heredar código malicioso o mal mantenido.

## Checklist

- [ ] ¿Se genera un SBOM como parte del pipeline, no solo un escaneo puntual de vulnerabilidades conocidas?
- [ ] ¿Las dependencias usan versiones exactas con lockfile, no rangos abiertos?
- [ ] ¿Se evalúa la reputación/mantenimiento de una dependencia nueva antes de adoptarla, no solo su funcionalidad?
- [ ] ¿Existe procedencia verificable (attestation) de que el artefacto desplegado se construyó como se espera?

Ver también: [`../devops/artifact-management.md`](../devops/artifact-management.md), [`vulnerability-management.md`](./vulnerability-management.md), [`secure-sdlc.md`](./secure-sdlc.md).
