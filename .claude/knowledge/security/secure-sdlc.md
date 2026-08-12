# Secure SDLC

> Ver [`../devops/ci-cd.md`](../devops/ci-cd.md) para el pipeline base y [`../devops/artifact-management.md`](../devops/artifact-management.md) para el escaneo de vulnerabilidades ya integrado en él — este documento cubre cómo la seguridad se integra en **todo** el ciclo de vida de desarrollo, no solo en el pipeline de build.

## Seguridad en cada fase, no solo al final

```
Diseño:        Threat modeling (ver threat-modeling.md) antes de escribir código
Desarrollo:    SAST en cada PR, revisión de código con foco de seguridad (ver ../../agents/senior-code-reviewer.md)
Build:         Escaneo de dependencias/SCA, generación de SBOM (ver supply-chain-security.md)
Test:          DAST contra el entorno de staging (ver ../../decisions/sast-vs-dast.md)
Despliegue:    Verificación de configuración segura antes de producción (ver secure-architecture-review.md)
Operación:     Monitorización de vulnerabilidades continua (ver vulnerability-management.md)
```

"Shift left": cuanto antes en el ciclo se detecta un problema de seguridad, más barato es corregirlo — un problema de diseño detectado en threat modeling cuesta una conversación; el mismo problema detectado en producción cuesta un incidente completo (ver [`../../playbooks/security-issue.md`](../../playbooks/security-issue.md)).

## Gates de seguridad bloqueantes en el pipeline

Ver [`../devops/artifact-management.md`](../devops/artifact-management.md) — el escaneo de vulnerabilidades ya está definido como bloqueante antes de publicar un artefacto; secure SDLC extiende ese mismo principio a SAST en cada PR (no solo en el build final) y a revisión de código con checklist de seguridad explícito, no genérico.

## Revisión de código con foco de seguridad, no solo funcional

```
Además de la revisión estándar de senior-code-reviewer.md:
¿Este cambio introduce un nuevo input no validado?
¿Este cambio expone un nuevo endpoint sin autenticación/autorización verificada?
¿Este cambio introduce una nueva dependencia sin evaluar su procedencia?
```

Ver [`../../agents/senior-code-reviewer.md`](../../agents/senior-code-reviewer.md) — la revisión de seguridad es una capa adicional de atención dentro del mismo proceso de revisión, no un proceso paralelo separado que duplica el flujo de PR.

## Formación continua, no un curso único al onboarding

Las amenazas y técnicas evolucionan — un equipo formado una vez en seguridad al incorporarse y nunca más queda desactualizado frente a vectores de ataque nuevos; sesiones periódicas breves sobre incidentes reales del sector (no necesariamente propios) mantienen la disciplina de seguridad viva en el equipo.

## Métricas de un Secure SDLC maduro

```
Tiempo medio de detección de una vulnerabilidad (¿en diseño, en PR, en producción?)
Tiempo medio de remediación tras detección (ver vulnerability-management.md)
% de PRs con hallazgos de seguridad bloqueantes (tendencia a la baja indica mejora del proceso, no solo del código)
```

## Checklist

- [ ] ¿Existen gates de seguridad en cada fase relevante del ciclo de vida, no solo al final del pipeline?
- [ ] ¿La revisión de código incluye explícitamente un foco de seguridad, no solo funcional?
- [ ] ¿Se mide el tiempo de detección/remediación como indicador de madurez del proceso?

Ver también: [`security-by-design.md`](./security-by-design.md), [`../devops/ci-cd.md`](../devops/ci-cd.md), [`vulnerability-management.md`](./vulnerability-management.md).
