# CloudFront vs. ALB

## Contexto

Ambos pueden servir tráfico HTTP/HTTPS hacia los usuarios finales, con propósitos y arquitecturas fundamentalmente distintos — no siempre son alternativas mutuamente excluyentes.

## Comparativa

| Criterio | CloudFront (CDN) | ALB (balanceador) |
|---|---|---|
| Ubicación | Distribuido globalmente, en el borde cercano al usuario | Regional, en la VPC |
| Cacheo | Nativo, diseñado para ello | Ninguno — reenvía cada petición al backend |
| Reducción de latencia geográfica | Alta — el usuario conecta al punto de borde más cercano | Ninguna — la latencia depende de la distancia a la región |
| Balanceo entre múltiples instancias/contenedores | No es su función | Su función principal |
| Protección DDoS | Alta, absorbe ataques en el borde antes de llegar al origen | Menor, aunque integrable con AWS Shield |
| Coste | Por transferencia de datos + peticiones | Por hora + peticiones procesadas |

## No son mutuamente excluyentes: patrón habitual combinado

```
Usuario → CloudFront (cacheo + protección DDoS + distribución global) → ALB (balanceo entre instancias) → Backend
```

El patrón más común en producción no es elegir uno u otro, sino combinarlos: CloudFront como capa de entrada global con cacheo y protección, ALB como balanceador regional detrás de él — cada uno resolviendo el problema para el que fue diseñado.

## Cuándo usar solo ALB, sin CloudFront

- Tráfico interno dentro de la VPC/organización, sin usuarios finales distribuidos geográficamente que se beneficien de un punto de borde cercano.
- APIs internas B2B con un número reducido de consumidores conocidos, donde el cacheo en el borde no aporta valor (contenido siempre dinámico y específico por consumidor).

## Cuándo usar solo CloudFront (sin ALB, origen directo S3/API Gateway)

- Contenido puramente estático servido desde S3, sin necesidad de balanceo entre instancias de cómputo.
- API completamente serverless (API Gateway + Lambda) donde no existe un conjunto de instancias que balancear.

## Recomendación por defecto de este framework

**Combinar ambos para aplicaciones web/API públicas con usuarios finales distribuidos**: CloudFront delante de ALB para contenido dinámico con necesidad de protección/distribución global, ALB solo cuando el tráfico es interno o el cacheo de borde no aporta valor real al caso de uso.

Ver también: [`../knowledge/aws-network/cloudfront.md`](../knowledge/aws-network/cloudfront.md), [`../knowledge/aws-network/alb.md`](../knowledge/aws-network/alb.md).
