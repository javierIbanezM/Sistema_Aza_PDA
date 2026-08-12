# SAST vs. DAST

## Contexto

Ambas son técnicas de testing de seguridad automatizado, aplicadas en momentos distintos del ciclo de vida (ver [`../knowledge/security/secure-sdlc.md`](../knowledge/security/secure-sdlc.md)) — no son alternativas excluyentes, sino complementarias con distinta cobertura.

## Comparativa

| Criterio | SAST (Static) | DAST (Dynamic) |
|---|---|---|
| Cuándo se ejecuta | Sobre el código fuente, sin ejecutar la aplicación (en cada PR/build) | Contra la aplicación en ejecución (staging/pre-producción) |
| Qué detecta | Patrones de código inseguro (SQL Injection potencial, uso de funciones peligrosas) | Vulnerabilidades explotables en tiempo de ejecución (respuestas reales del servidor) |
| Falsos positivos | Más frecuentes — detecta patrones sin confirmar explotabilidad real | Menos frecuentes — confirma el comportamiento real observado |
| Cobertura de lógica de negocio | Limitada — no entiende el flujo de negocio completo | Mayor — puede probar flujos completos de la aplicación real |
| Velocidad | Rápido, integrable en cada PR | Más lento, requiere un entorno desplegado |
| Detecta configuración de despliegue insegura | No — solo analiza código | Sí — prueba la configuración real del entorno desplegado |

## Cuándo priorizar SAST

- Feedback rápido en cada Pull Request, antes de que el código llegue a ningún entorno desplegado (shift left, ver [`../knowledge/security/secure-sdlc.md`](../knowledge/security/secure-sdlc.md)).
- Detección de patrones de código inseguro conocidos (uso de funciones criptográficas débiles, concatenación de SQL) de forma sistemática en todo el código fuente.

## Cuándo priorizar DAST

- Verificación de vulnerabilidades explotables en el comportamiento real de la aplicación desplegada, incluida su configuración de infraestructura.
- Pruebas de flujos de negocio completos donde el contexto dinámico (sesión, estado) es relevante para la vulnerabilidad.
- Como verificación complementaria antes de un despliegue a producción de alto riesgo (ver [`../playbooks/new-api.md`](../playbooks/new-api.md)).

## Recomendación por defecto de este framework

**Ambos, en momentos distintos del pipeline** — SAST en cada Pull Request como gate bloqueante (ver [`../knowledge/devops/artifact-management.md`](../knowledge/devops/artifact-management.md), mismo principio que el escaneo de dependencias ya definido), DAST contra el entorno de staging antes del despliegue a producción para cambios de riesgo medio-alto. Ninguno sustituye al otro — SAST sin DAST no detecta problemas de configuración de despliegue; DAST sin SAST llega demasiado tarde en el ciclo para el feedback rápido que el desarrollo necesita.

Ver también: [`../knowledge/security/secure-sdlc.md`](../knowledge/security/secure-sdlc.md), [`../knowledge/devops/artifact-management.md`](../knowledge/devops/artifact-management.md).
