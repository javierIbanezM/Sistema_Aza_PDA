# Recipe: Ejecutar una Revisión de Arquitectura Segura

Aplica: [`../knowledge/security/secure-architecture-review.md`](../knowledge/security/secure-architecture-review.md), [`../knowledge/security/threat-modeling.md`](../knowledge/security/threat-modeling.md).

## Pasos

### 1. Confirmar que existe threat model — condición de entrada, no opcional

```
Si NO existe threat model para este diseño → ejecutar security-threat-model.md primero,
NUNCA proceder con la revisión de arquitectura sin él
```

### 2. Recorrer el checklist de 8 puntos en orden

```
1. Threat model existente ✓ (confirmado en paso 1)
2. Límites de confianza identificados en el diseño
3. Clasificación de datos sensibles explícita
4. Autenticación/autorización en cada punto de acceso
5. Defense in depth, no una única capa
6. Gestión de secretos según el proceso establecido
7. Nueva superficie de ataque introducida, evaluada
8. Plan de observabilidad/detección específico
```

Ver [`../knowledge/security/secure-architecture-review.md`](../knowledge/security/secure-architecture-review.md) para el detalle de cada punto.

### 3. Para cada punto, verificar contra el threat model específico, no una checklist genérica

```
No: "¿Cumple con OWASP Top 10?" (checklist genérica sin contexto)
Sí: "¿Este diseño mitiga la amenaza de Tampering identificada en el paso 3 del threat model?"
```

### 4. Clasificar el resultado: aprobado, aprobado con condiciones, o rechazado

```
Aprobado con condiciones: "El diseño se aprueba, sujeto a implementar rate limiting específico
en /auth/login antes de desplegar a producción" — condición verificable, con propietario asignado
```

### 5. Documentar la revisión

```
Formato: templates/ADR.md + sección "Amenazas consideradas y mitigaciones"
```

### 6. Verificar el cumplimiento de las condiciones antes del despliegue final

Ver [`../playbooks/new-feature.md`](../playbooks/new-feature.md)/[`new-api.md`](../playbooks/new-api.md) — una condición de aprobación sin verificación posterior de cumplimiento no es una condición real, es una recomendación ignorable.

## Checklist final

- [ ] ¿La revisión partió de un threat model confirmado?
- [ ] ¿Se recorrieron los 8 puntos en orden, contra el threat model específico, no una checklist genérica?
- [ ] ¿Las condiciones de aprobación (si las hay) tienen propietario y verificación posterior?
- [ ] Ejecutar [`../checklists/architecture.md`](../checklists/architecture.md).

Ver también: [`security-threat-model.md`](./security-threat-model.md), [`../knowledge/security/secure-architecture-review.md`](../knowledge/security/secure-architecture-review.md).
