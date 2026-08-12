# Playbook: Security Issue

## Cuándo usar este playbook

Una vulnerabilidad detectada (por auditoría, escaneo automatizado, reporte externo, o revisión de código) o un incidente de seguridad activo. [`../agents/senior-security-engineer.md`](../agents/senior-security-engineer.md) actúa como Incident Commander de todo el proceso, coordinando las fases siguientes. Ver `CLAUDE.md` sección 12 (OWASP) como marco de referencia transversal.

## Fases

### 1. Senior Debugger (evaluación inicial de alcance, no espera al análisis completo)

- **Hace**: si hay un incidente activo (explotación en curso), prioriza contener el impacto inmediato (ej. deshabilitar el endpoint afectado, revocar credenciales comprometidas) antes que el análisis completo — equivalente a la fase 1 de [`hotfix.md`](./hotfix.md) pero con foco en seguridad.
- **Produce**: alcance inicial del incidente (¿qué está comprometido, desde cuándo?) y contención inmediata si aplica.
- **Entrega a**: Senior Business Analyst (para evaluar el impacto de negocio) y Software Architect (para el análisis técnico de la vulnerabilidad) en paralelo.

### 2. Senior Business Analyst (en paralelo)

- **Hace**: evalúa el impacto de negocio (¿qué datos, qué usuarios, qué obligaciones regulatorias de notificación aplican?).
- **Produce**: evaluación de impacto e identificación de si se requiere notificación a usuarios/autoridades según normativa aplicable.
- **Entrega a**: dirección/legal si aplica (fuera del alcance técnico de este playbook, pero debe señalarse explícitamente).

### 3. Software Architect

- **Hace**: analiza la causa raíz de la vulnerabilidad contra el marco de `CLAUDE.md` sección 12 y [`../anti-patterns/sql-injection.md`](../anti-patterns/sql-injection.md)/[`../standards/validation.md`](../standards/validation.md)/[`../standards/api.md`](../standards/api.md) según el tipo. Evalúa si es un caso aislado o un patrón sistémico (¿existe el mismo fallo en otros endpoints/módulos?).
- **Produce**: causa raíz + alcance real (¿aislado o sistémico?) + diseño de la corrección.
- **Entrega a**: Tech Lead.

### 4. Tech Lead

- **Hace**: si el problema es sistémico, planifica la corrección en todos los puntos afectados, no solo el reportado inicialmente — prioriza según severidad (CVSS o criterio equivalente del equipo).
- **Produce**: plan de corrección, priorizado por severidad e impacto.
- **Entrega a**: el agente técnico correspondiente a la tecnología afectada (ver `docs/orchestrator.md` Paso 2b).

### 5. Agente técnico (según tecnología detectada)

- **Hace**: implementa la corrección siguiendo `standards/` y `anti-patterns/` relevantes (validación, parametrización, permisos mínimos según [`../knowledge/sqlserver/security.md`](../knowledge/sqlserver/security.md)/[`../knowledge/spring/spring-security.md`](../knowledge/spring/spring-security.md) según el stack), bajo la supervisión de [`../agents/senior-security-engineer.md`](../agents/senior-security-engineer.md).
- **Produce**: fix + test que demuestra que la vulnerabilidad ya no es explotable (no solo que el código "se ve mejor").
- **Entrega a**: Senior Code Reviewer.

### 6. Senior Code Reviewer (revisión con foco de seguridad explícito)

- **Hace**: revisión centrada específicamente en si la corrección cierra realmente el vector de ataque, y si introduce alguno nuevo — nunca aprueba solo por confiar en que "ya se corrigió".
- **Produce**: aprobación o bloqueo.
- **Entrega a**: despliegue prioritario.

### 7. Post-mortem (obligatorio en todo incidente de seguridad, sin excepción)

- **Hace**: igual que en [`hotfix.md`](./hotfix.md), pero además evalúa si el proceso de desarrollo (revisión, análisis estático, checklist) debería haber detectado esto antes — ver `CLAUDE.md` sección 12.1 (Security Logging and Monitoring Failures) para el criterio de qué debería haberse registrado/alertado.
- **Produce**: acción de seguimiendo (mejora de checklist, nueva regla de `standards/`, análisis estático adicional en CI).

## Diagrama de flujo

```
Senior Debugger (contención inmediata si hay explotación activa)
        │
        ├──→ Senior Business Analyst (impacto de negocio/regulatorio)
        │
        └──→ Software Architect (causa raíz + alcance: ¿aislado o sistémico?)
                    ↓
              Tech Lead (plan priorizado)
                    ↓
              Agente técnico (fix + test de explotabilidad cerrada, supervisado por Security Engineer)
                    ↓
              Senior Code Reviewer (foco de seguridad)
                    ↓
              [Producción, prioritario]
                    ↓
              Post-mortem (obligatorio, sin excepción)
```

## Regla de oro de este playbook

Nunca se cierra un incidente de seguridad sin verificar si el mismo patrón de vulnerabilidad existe en otro lugar del sistema — corregir el síntoma reportado sin buscar el patrón sistémico es la forma más común de que el mismo tipo de vulnerabilidad reaparezca en otro endpoint semanas después.
