# NLB (Network Load Balancer)

> Ver [`alb.md`](./alb.md) para el balanceador de capa 7 — el NLB cubre el caso complementario de máximo rendimiento a nivel de red.

## Qué es: balanceo de carga a nivel de red (capa 4)

Distribuye tráfico TCP/UDP con latencia ultra-baja y capacidad de millones de peticiones por segundo, sin inspeccionar el contenido de la petición (capa 4, no capa 7 como el ALB) — apropiado cuando se necesita rendimiento extremo o protocolos no HTTP.

## Cuándo NLB en lugar de ALB

- Protocolos no HTTP (TCP/UDP puro) — bases de datos expuestas internamente, protocolos de mensajería propietarios, gRPC de alto rendimiento donde el enrutamiento de capa 7 no es necesario.
- Latencia crítica extrema, donde el procesamiento adicional de capa 7 del ALB (inspección de headers, enrutamiento por path) no es tolerable.
- Se necesita preservar la IP de origen del cliente sin modificación, o una IP estática para el balanceador (el NLB soporta IPs elásticas fijas, el ALB no).
- Integración con AWS PrivateLink para exponer un servicio a otras VPCs/cuentas de forma privada.

## Preservación de IP de origen

A diferencia del ALB (que por defecto reemplaza la IP de origen con la suya propia, requiriendo el header `X-Forwarded-For` para recuperar la IP real del cliente), el NLB puede preservar la IP de origen real del cliente de forma nativa — relevante si la aplicación necesita esa IP directamente sin depender de headers adicionales.

## Health Checks: mismo principio, menor overhead

Igual que el ALB (ver [`alb.md`](./alb.md)), el NLB excluye automáticamente destinos no saludables — con menor overhead de procesamiento al no inspeccionar contenido de capa 7, contribuyendo a su capacidad de mayor throughput.

## Checklist

- [ ] ¿Se necesita genuinamente capa 4 (TCP/UDP, latencia extrema, IP estática) frente al enrutamiento de capa 7 que ofrece el ALB?
- [ ] ¿Se ha verificado si la aplicación necesita la IP de origen real preservada nativamente?

Ver también: [`alb.md`](./alb.md), [`../../decisions/cloudfront-vs-alb.md`](../../decisions/cloudfront-vs-alb.md).
