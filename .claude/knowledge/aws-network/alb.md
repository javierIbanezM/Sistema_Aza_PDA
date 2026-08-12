# ALB (Application Load Balancer)

> Ver [`../../decisions/cloudfront-vs-alb.md`](../../decisions/cloudfront-vs-alb.md) para la comparativa con CloudFront. Ver [`nlb.md`](./nlb.md) para el balanceador de nivel de red complementario.

## Qué es: balanceo de carga a nivel de aplicación (capa 7)

Distribuye tráfico HTTP/HTTPS entre múltiples destinos (instancias EC2, contenedores ECS, IPs) con capacidad de enrutamiento basado en contenido de la petición (path, host, headers) — capa 7 del modelo OSI, con visibilidad del contenido de la petición, a diferencia del NLB de capa 4.

## Enrutamiento basado en reglas

```
Regla 1: path /api/pedidos/* → Target Group "pedidos-service"
Regla 2: path /api/inventario/* → Target Group "inventario-service"
Regla 3: host admin.empresa.com → Target Group "admin-service"
```

Permite servir múltiples servicios/microservicios detrás de un único ALB, enrutando según la petición — reduce el número de balanceadores necesarios frente a uno por servicio, con el consiguiente ahorro de coste y simplicidad de gestión de certificados/DNS.

## Health Checks: detección y exclusión automática de destinos no saludables

```
Health check: GET /health cada 30s, 2 fallos consecutivos → destino marcado unhealthy, excluido del balanceo
```

Mismo principio que cualquier orquestador del framework (ver `CLAUDE.md` sección 7.2, ECS/EKS) — el ALB deja de enviar tráfico a un destino que falla su health check, redirigiendo automáticamente a destinos saludables restantes.

## Terminación TLS y redirección HTTP→HTTPS

```
Listener 443 (HTTPS): certificado ACM, termina TLS aquí, tráfico interno al Target Group puede ser HTTP
Listener 80 (HTTP): regla de redirección automática a 443
```

El ALB termina TLS con un certificado gestionado por ACM (renovación automática) — el tráfico entre el ALB y los destinos internos puede viajar sin TLS si están dentro de la VPC privada (reduciendo overhead de cifrado interno), aunque cifrar también ese tramo es una opción de mayor seguridad en profundidad si el modelo de amenaza lo justifica.

## Sticky Sessions: cuándo son necesarias (y cuándo evitarlas)

```
Cookie de afinidad: dirige al mismo destino durante la duración de la sesión del usuario
```

Necesarias solo si la aplicación mantiene estado en memoria local no compartido entre instancias (antipatrón en general, ver principio de servicios stateless) — preferible diseñar la aplicación sin dependencia de sticky sessions (estado compartido en Redis/ElastiCache, ver [`../aws-database/elasticache.md`](../aws-database/elasticache.md)) para permitir balanceo verdaderamente uniforme y tolerancia a fallo de cualquier instancia individual.

## Checklist

- [ ] ¿Se aprovecha el enrutamiento basado en contenido para consolidar servicios detrás de un único ALB donde tiene sentido?
- [ ] ¿Existe un health check que excluye automáticamente destinos no saludables?
- [ ] ¿TLS se termina en el ALB con certificado gestionado por ACM, con redirección HTTP→HTTPS?
- [ ] ¿Se ha evitado depender de sticky sessions diseñando la aplicación como stateless?

Ver también: [`nlb.md`](./nlb.md), [`../../decisions/cloudfront-vs-alb.md`](../../decisions/cloudfront-vs-alb.md), [`vpc.md`](./vpc.md).
