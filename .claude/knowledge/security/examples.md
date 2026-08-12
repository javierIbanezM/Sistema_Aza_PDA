# Examples (Security)

## Ejemplo 1 — Threat modeling de una nueva integración con un proveedor de pagos

**Contexto**: se va a integrar un nuevo proveedor de pagos externo.

**Flujo aplicado**: siguiendo [`threat-modeling.md`](./threat-modeling.md), se dibuja el flujo con el límite de confianza entre el sistema propio y el proveedor externo. Se aplica STRIDE: Tampering (¿puede manipularse el monto entre la creación del pedido y el cobro?), Information Disclosure (¿se loguea accidentalmente el número de tarjeta?, ver [`../../standards/logging-standard.md`](../../standards/logging-standard.md)), Repudiation (¿hay auditoría suficiente del cobro para disputas?). Se prioriza con [`risk-assessment.md`](./risk-assessment.md) — Information Disclosure de datos de pago tiene impacto crítico (regulatorio, PCI-DSS), máxima prioridad. Se diseñan mitigaciones (nunca almacenar el número de tarjeta completo, usar tokenización del proveedor) antes de implementar, siguiendo [`../../playbooks/new-api.md`](../../playbooks/new-api.md).

---

## Ejemplo 2 — Revisión de arquitectura segura de un nuevo microservicio

**Contexto**: nuevo servicio que expone un endpoint público para consulta de pedidos.

**Flujo aplicado**, siguiendo [`secure-architecture-review.md`](./secure-architecture-review.md): se confirma que existe threat model. Se verifica que cada endpoint valida autorización de recurso específico (ver [`authentication-vs-authorization.md`](./authentication-vs-authorization.md)) — se detecta que el endpoint de detalle de pedido no verificaba propiedad, corrigiéndose antes de aprobar. Se revisa defense in depth: WAF + validación de aplicación + permisos mínimos de base de datos, no solo una capa. Aprobado con una condición: añadir rate limiting específico antes de producción (ver [`api-security.md`](./api-security.md)).

---

## Ejemplo 3 — Gestión de una vulnerabilidad crítica recién publicada

**Contexto**: se publica un CVE crítico en una librería usada por varios servicios.

**Flujo aplicado**, siguiendo [`vulnerability-management.md`](./vulnerability-management.md): se consulta el SBOM (ver [`supply-chain-security.md`](./supply-chain-security.md)) para identificar exactamente qué servicios usan la versión afectada, sin auditar manualmente cada repositorio. Se hace triage real: en dos servicios la librería solo se usa en un script interno sin exposición (riesgo bajo pese al CVSS alto); en un tercero procesa directamente input de usuario en un endpoint público (riesgo crítico real). Se aplica mitigación compensatoria inmediata en el tercero (regla de WAF específica) mientras se prepara el parche, siguiendo el SLA de 24-72h para severidad crítica real.

---

## Ejemplo 4 — Incidente de seguridad: acceso no autorizado detectado

**Contexto**: se detecta acceso anómalo a datos de clientes.

**Flujo aplicado**: se ejecuta [`../../playbooks/security-issue.md`](../../playbooks/security-issue.md) completo (contención, análisis de causa raíz, alcance sistémico) — este documento no repite ese proceso. Durante el análisis de causa raíz, se aplica [`authentication-vs-authorization.md`](./authentication-vs-authorization.md) para confirmar si fue un fallo de autenticación (credencial comprometida) o de autorización (BOLA no detectado en revisión) — la distinción determina si la corrección es rotar credenciales o corregir código, respectivamente.
