# Attack Surface Analysis

> Ver [`../aws-network/vpc.md`](../aws-network/vpc.md) para el control de exposición de red que reduce superficie de ataque a nivel de infraestructura, y [`../aws-integration/api-gateway.md`](../aws-integration/api-gateway.md) para el punto de entrada típico de la superficie de ataque de una API.

## Qué es la superficie de ataque

El conjunto completo de puntos por los que un atacante podría intentar interactuar con el sistema — cada endpoint expuesto, cada puerto abierto, cada dependencia de terceros, cada credencial, cada input de usuario. Reducir la superficie de ataque significa eliminar puntos de entrada innecesarios, no solo defender mejor los que existen.

## Inventario: no se puede proteger lo que no se sabe que existe

```
¿Cuántos endpoints públicos expone el sistema? ¿Todos están documentados/versionados?
¿Cuántos puertos están abiertos en cada instancia/contenedor? ¿Cada uno está justificado?
¿Cuántas dependencias de terceros tiene el proyecto, directas e indirectas?
¿Cuántos servicios/sistemas externos están integrados, cada uno con sus propias credenciales?
```

Un inventario desactualizado (un endpoint de debug olvidado en producción, un puerto abierto "temporalmente" hace meses) es la causa más común de superficie de ataque no gestionada — mantener un inventario activo, no reconstruido solo cuando ocurre un incidente.

## Minimizar antes de defender

```
Antes: ¿este endpoint/puerto/dependencia es genuinamente necesario?
Después: si es necesario, ¿está protegido adecuadamente?
```

La forma más efectiva de reducir riesgo no siempre es añadir más controles — a menudo es **eliminar** lo innecesario: un endpoint de administración que nadie usa, una dependencia con una única función que podría reimplementarse en pocas líneas evitando una cadena de transitivas, un puerto de depuración abierto en producción. Cada elemento eliminado es una amenaza que deja de aplicar por completo, más robusto que cualquier control añadido sobre algo que no debería existir.

## Superficie de ataque en cada capa

| Capa | Elementos típicos de superficie |
|---|---|
| Red | Puertos abiertos, Security Groups permisivos (ver [`../aws-network/vpc.md`](../aws-network/vpc.md)) |
| API | Endpoints públicos, parámetros de entrada, headers procesados |
| Dependencias | Librerías directas e indirectas, cada una con su propio historial de CVEs (ver [`vulnerability-management.md`](./vulnerability-management.md)) |
| Identidad | Cada credencial/token/API key emitido, cada método de autenticación soportado |
| Datos | Cada almacén de datos accesible, cada backup, cada log que podría contener información sensible |
| Cadena de suministro | Cada proveedor/servicio externo integrado (ver [`supply-chain-security.md`](./supply-chain-security.md)) |

## Escaneo continuo de superficie externa

```
Herramienta de reconocimiento externo (equivalente a lo que un atacante vería):
puertos abiertos, subdominios expuestos, certificados TLS, tecnologías detectables
```

Ver la superficie de ataque desde la perspectiva de un atacante externo (sin acceso privilegiado a la arquitectura interna) revela con frecuencia elementos olvidados que el propio equipo, familiarizado con el sistema, ya no percibe como "expuestos".

## Checklist

- [ ] ¿Existe un inventario activo y actualizado de endpoints, puertos y dependencias expuestas?
- [ ] ¿Se ha evaluado eliminar elementos innecesarios antes de añadir controles sobre ellos?
- [ ] ¿Se ha revisado la superficie desde la perspectiva externa (reconocimiento), no solo desde el conocimiento interno del sistema?

Ver también: [`threat-modeling.md`](./threat-modeling.md), [`risk-assessment.md`](./risk-assessment.md), [`vulnerability-management.md`](./vulnerability-management.md).
