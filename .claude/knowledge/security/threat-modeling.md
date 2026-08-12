# Threat Modeling

> Ver [`../architecture/ddd.md`](../architecture/ddd.md) para el modelo de dominio sobre el que se aplica el threat modeling — modelar amenazas requiere primero entender qué activos y flujos de datos existen.

## Por qué threat modeling precede a todo lo demás

Aplicar controles de seguridad sin haber modelado antes qué puede salir mal es optimizar sin saber qué se está optimizando — el 90% del valor de la seguridad de un sistema se decide en el diseño, antes de escribir una línea de código, identificando qué amenazas son reales para **este** sistema concreto, no una lista genérica de OWASP aplicada sin criterio.

## STRIDE: categorías de amenaza por elemento del sistema

| Categoría | Pregunta | Ejemplo |
|---|---|---|
| **S**poofing | ¿Puede alguien hacerse pasar por otro? | Un atacante reutiliza un token de otro usuario |
| **T**ampering | ¿Puede alguien modificar datos sin autorización? | Modificar el precio de un pedido en tránsito |
| **R**epudiation | ¿Puede alguien negar haber hecho una acción? | Sin auditoría, nadie puede probar quién canceló un pedido |
| **I**nformation Disclosure | ¿Puede alguien ver datos que no debería? | Un endpoint expone el email de otro usuario por IDOR |
| **D**enial of Service | ¿Puede alguien degradar la disponibilidad? | Un endpoint sin rate limiting saturado deliberadamente |
| **E**levation of Privilege | ¿Puede alguien obtener más permisos de los que tiene? | Un usuario normal accede a un endpoint de administración |

## Proceso: diagrama de flujo de datos primero, amenazas después

```
1. Dibujar el sistema: actores, procesos, almacenes de datos, límites de confianza (trust boundaries)
2. Para cada elemento que cruza un límite de confianza, aplicar STRIDE sistemáticamente
3. Priorizar las amenazas identificadas por riesgo real (ver risk-assessment.md), no tratarlas todas igual
4. Definir la mitigación para cada amenaza priorizada, ANTES de implementar
```

Los límites de confianza (trust boundaries) son el punto crítico — cualquier dato que cruza de una zona de menor confianza a una de mayor confianza (input de usuario hacia el backend, un servicio externo hacia el sistema propio) es donde las amenazas STRIDE se materializan con más frecuencia.

## Ejemplo aplicado: flujo de confirmación de pedido

```
Actor: Cliente → [límite de confianza: internet → API] → API Gateway → [límite: red pública → red interna]
→ Servicio de Pedidos → [límite: aplicación → base de datos] → SQL Server

Amenazas identificadas en el límite Cliente → API:
- Spoofing: ¿se verifica la identidad del cliente en cada petición? → JWT (ver ../../decisions/jwt-vs-session.md)
- Tampering: ¿puede el cliente manipular el total del pedido enviado? → recalcular en servidor, nunca confiar en el precio del cliente
- DoS: ¿hay rate limiting en el endpoint de creación de pedidos? → ver ../express/middleware.md
```

## Threat modeling como actividad recurrente, no un documento único al inicio

Un modelo de amenazas creado una vez al diseñar el sistema y nunca revisado queda obsoleto en cuanto el sistema evoluciona — revisarlo ante cada cambio arquitectónico significativo (nueva integración externa, nuevo tipo de dato sensible, ver [`../../playbooks/new-api.md`](../../playbooks/new-api.md)), no solo en el diseño inicial.

## Checklist

- [ ] ¿Existe un diagrama de flujo de datos con límites de confianza claramente marcados?
- [ ] ¿Se ha aplicado STRIDE a cada elemento que cruza un límite de confianza, no genéricamente a todo el sistema?
- [ ] ¿Las amenazas identificadas están priorizadas por riesgo real, no tratadas todas con la misma urgencia?
- [ ] ¿El modelo se revisa ante cambios arquitectónicos significativos, no solo una vez al inicio?

Ver también: [`risk-assessment.md`](./risk-assessment.md), [`attack-surface-analysis.md`](./attack-surface-analysis.md), `CLAUDE.md` sección 12.
