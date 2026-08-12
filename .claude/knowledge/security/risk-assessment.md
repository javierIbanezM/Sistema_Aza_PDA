# Risk Assessment

> Ver [`threat-modeling.md`](./threat-modeling.md) — este documento cubre cómo **priorizar** las amenazas ya identificadas, convirtiendo una lista de "cosas que podrían pasar" en una lista ordenada de "qué se mitiga primero, y por qué".

## Riesgo = Probabilidad × Impacto, nunca uno solo

```
Amenaza de alta probabilidad, bajo impacto: mitigar, pero no es la prioridad máxima
Amenaza de baja probabilidad, alto impacto: sigue mereciendo mitigación (ver Attack Surface Analysis)
Amenaza de alta probabilidad, alto impacto: máxima prioridad, sin excepción
```

Evaluar solo la probabilidad (¿qué tan fácil es explotar esto?) sin el impacto (¿qué pasa si se explota?) — o viceversa — produce priorización sesgada; ambas dimensiones son necesarias.

## Matriz de riesgo: herramienta de comunicación, no solo de cálculo

```
                    Impacto Bajo    Impacto Medio    Impacto Alto
Probabilidad Alta      Medio           Alto            Crítico
Probabilidad Media     Bajo            Medio           Alto
Probabilidad Baja      Bajo            Bajo            Medio
```

El valor de la matriz no es solo calcular un número — es dar a personas no técnicas (dirección, negocio) una forma visual de entender por qué una amenaza técnica concreta merece inversión de tiempo/presupuesto, conectando con [`../../agents/senior-business-analyst.md`](../../agents/senior-business-analyst.md) para esa traducción.

## Factores que modifican la probabilidad real (no solo teórica)

- **Exposición**: ¿el componente afectado es accesible desde internet, o solo desde una red interna? (ver [`attack-surface-analysis.md`](./attack-surface-analysis.md))
- **Habilidad requerida**: ¿explotarlo requiere un atacante sofisticado, o es automatizable con herramientas públicas?
- **Motivación del atacante**: ¿el activo afectado (datos de pago, PII) es atractivo para un atacante con motivación económica?
- **Controles existentes**: ¿ya existe alguna mitigación parcial (WAF, rate limiting) que reduce la probabilidad real, aunque no la elimine?

## Factores que modifican el impacto real

- **Datos afectados**: ¿son datos públicos, internos, o sensibles/regulados (ver `CLAUDE.md` sección 12)?
- **Alcance**: ¿afecta a un usuario, a un tenant, o a todos los usuarios del sistema?
- **Reversibilidad**: ¿el daño es reversible (un servicio caído que se reinicia) o irreversible (datos filtrados que no se pueden "des-filtrar")?
- **Impacto regulatorio/legal**: ¿el incidente dispara obligaciones de notificación (ver [`../../playbooks/security-issue.md`](../../playbooks/security-issue.md))?

## Riesgo aceptado: una decisión explícita, no una omisión

```
Riesgo: "Endpoint interno sin rate limiting" — Probabilidad Baja (red interna, sin exposición pública), Impacto Medio
Decisión: Riesgo ACEPTADO explícitamente por [responsable], revisar en 6 meses o si cambia la exposición
```

No todo riesgo identificado se mitiga inmediatamente — algunos se aceptan conscientemente cuando el coste de mitigación supera el riesgo real, pero esa aceptación debe ser una **decisión explícita y documentada** con un responsable nombrado, nunca un riesgo simplemente olvidado sin que nadie decidiera activamente vivir con él.

## Checklist

- [ ] ¿Cada amenaza tiene una evaluación explícita de probabilidad E impacto, no solo una intuición general?
- [ ] ¿Se han considerado los factores reales de exposición y controles existentes, no solo el riesgo teórico en abstracto?
- [ ] ¿Los riesgos no mitigados están explícitamente aceptados por un responsable, no simplemente ignorados?

Ver también: [`threat-modeling.md`](./threat-modeling.md), [`attack-surface-analysis.md`](./attack-surface-analysis.md), [`vulnerability-management.md`](./vulnerability-management.md).
