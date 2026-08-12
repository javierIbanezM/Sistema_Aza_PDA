# Recipe: Conducir una Sesión de Threat Modeling

Aplica: [`../knowledge/security/threat-modeling.md`](../knowledge/security/threat-modeling.md), [`../knowledge/security/risk-assessment.md`](../knowledge/security/risk-assessment.md).

## Pasos

### 1. Reunir a los participantes correctos

Software Architect (diseño del sistema), Tech Lead (detalle de implementación), quien vaya a ejecutar la funcionalidad, y opcionalmente Senior Business Analyst si hay implicaciones regulatorias — nunca una sesión de threat modeling en solitario, la diversidad de perspectivas detecta amenazas que una única persona no anticipa.

### 2. Dibujar el diagrama de flujo de datos

```
Actor → [proceso] → [almacén de datos] → [proceso] → Actor
Marcar explícitamente cada límite de confianza (trust boundary) que el flujo cruza
```

Ver [`../knowledge/security/threat-modeling.md`](../knowledge/security/threat-modeling.md) — sin este diagrama previo, la sesión degenera en una lluvia de ideas desordenada sin cobertura sistemática.

### 3. Aplicar STRIDE a cada elemento que cruza un límite de confianza

```
Para cada flecha del diagrama que cruza un trust boundary:
¿Spoofing? ¿Tampering? ¿Repudiation? ¿Information Disclosure? ¿DoS? ¿Elevation of Privilege?
```

Documentar cada amenaza identificada, aunque parezca poco probable en ese momento — la priorización viene después, no durante la identificación.

### 4. Priorizar con Risk Assessment

```
Para cada amenaza: Probabilidad (Alta/Media/Baja) × Impacto (Alto/Medio/Bajo) → prioridad
```

Ver [`../knowledge/security/risk-assessment.md`](../knowledge/security/risk-assessment.md).

### 5. Definir mitigación explícita para cada amenaza priorizada

```
Amenaza: Tampering del monto del pedido en tránsito cliente → API
Mitigación: recalcular el total en servidor, nunca confiar en el valor enviado por el cliente
```

Ninguna amenaza de prioridad Alta/Crítica queda sin mitigación definida antes de cerrar la sesión — las de prioridad Baja pueden quedar como riesgo aceptado explícito (ver [`../knowledge/security/risk-assessment.md`](../knowledge/security/risk-assessment.md)).

### 6. Documentar el resultado

```
Formato: templates/ADR.md + sección "Amenazas consideradas y mitigaciones"
```

### 7. Programar revisión ante el próximo cambio arquitectónico significativo

No es un documento de una única vez — ver [`../knowledge/security/threat-modeling.md`](../knowledge/security/threat-modeling.md).

## Checklist final

- [ ] ¿Participaron al menos dos perspectivas distintas (arquitectura + implementación)?
- [ ] ¿El diagrama de flujo de datos precedió a la identificación de amenazas?
- [ ] ¿Cada amenaza de prioridad Alta/Crítica tiene mitigación explícita definida?
- [ ] ¿El resultado está documentado de forma auditable?

Ver también: [`security-review-architecture.md`](./security-review-architecture.md).
