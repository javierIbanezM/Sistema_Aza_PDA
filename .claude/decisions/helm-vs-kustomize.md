# Helm vs. Kustomize

## Contexto

Ambos gestionan variaciones de manifiestos Kubernetes entre entornos (ver [`../knowledge/kubernetes/kubernetes.md`](../knowledge/kubernetes/kubernetes.md)), con filosofías opuestas: plantillas parametrizadas (Helm) vs. superposiciones declarativas (Kustomize).

## Comparativa

| Criterio | Helm | Kustomize |
|---|---|---|
| Enfoque | Plantillas con variables (templating) | Superposiciones (overlays) sobre manifiestos YAML base, sin templating |
| Curva de aprendizaje | Media — sintaxis de templating propia (Go templates) | Baja — YAML puro, sin lenguaje de plantillas adicional |
| Empaquetado/distribución | Charts versionados, reutilizables, publicables en repositorios de Charts | Sin concepto de paquete distribuible — cada proyecto mantiene sus propios overlays |
| Gestión de releases (historial, rollback) | Nativa (`helm rollback`) | No nativa — depende de Git/herramientas externas |
| Integración nativa con `kubectl` | Requiere el binario/plugin de Helm | Integrado nativamente en `kubectl` desde Kubernetes 1.14+ |
| Apropiado para distribuir software de terceros | Sí — el modelo estándar del ecosistema (Helm Hub/Artifact Hub) | No diseñado para ese caso de uso |

## Cuándo elegir Helm

- Se necesita instalar software de terceros que ya se distribuye como Helm Chart (la mayoría del ecosistema Kubernetes: cert-manager, Prometheus, ingress-nginx).
- Se necesita gestión de releases con historial y rollback nativo (`helm rollback` a una revisión anterior).
- El proyecto propio se beneficia de empaquetar y versionar su configuración como un Chart reutilizable entre múltiples clústeres/entornos.

## Cuándo elegir Kustomize

- Se prefiere evitar la complejidad de un lenguaje de templating adicional — YAML puro con overlays es más simple de leer y depurar.
- El caso de uso es principalmente gestionar variaciones entre entornos (dev/staging/prod) de manifiestos propios, sin necesidad de distribuir el paquete a terceros.
- Se valora la integración nativa sin dependencias adicionales (`kubectl apply -k`).

## Uso combinado: no son mutuamente excluyentes

```
Helm para instalar dependencias de terceros (cert-manager, ingress-nginx)
Kustomize para gestionar las variaciones de entorno de los manifiestos propios de la aplicación
```

Un patrón habitual y perfectamente razonable combina ambos según su fortaleza respectiva, en lugar de forzar una única herramienta para todos los casos.

## Recomendación por defecto de este framework

**Kustomize para manifiestos propios de la aplicación** (variaciones dev/staging/prod, ver [`../knowledge/devops/environments.md`](../knowledge/devops/environments.md)) — menor complejidad, sin lenguaje de templating adicional que aprender; **Helm para instalar dependencias de terceros del ecosistema Kubernetes**, donde es el estándar de facto y la única opción práctica en la mayoría de casos.

Ver también: [`../knowledge/kubernetes/kubernetes.md`](../knowledge/kubernetes/kubernetes.md), [`../knowledge/devops/environments.md`](../knowledge/devops/environments.md).
