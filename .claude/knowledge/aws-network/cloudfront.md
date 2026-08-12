# CloudFront

> Ver [`../../decisions/cloudfront-vs-alb.md`](../../decisions/cloudfront-vs-alb.md) para la comparativa con ALB.

## Qué es: CDN global con cacheo en el borde

Distribuye contenido (estático o dinámico) desde ubicaciones de borde cercanas geográficamente al usuario final, reduciendo latencia y descargando tráfico del origen — apropiado para contenido servido a usuarios geográficamente distribuidos, no solo archivos estáticos.

## Origen: S3, ALB, o cualquier servidor HTTP

```
CloudFront → Origen S3 (contenido estático: assets, imágenes)
CloudFront → Origen ALB (contenido dinámico: API, con caché selectivo o deshabilitado)
```

CloudFront no está limitado a servir contenido estático desde S3 — puede actuar como capa de CDN + WAF delante de una API dinámica servida por un ALB, cacheando solo las respuestas que sea seguro cachear (ver Cache Policies).

## Cache Policies: qué cachear y durante cuánto

```
Cache Policy "estáticos": TTL 1 día, cachea por defecto
Cache Policy "api-dinamica": TTL 0 por defecto, respeta Cache-Control del origen
```

Para contenido dinámico/personalizado por usuario, nunca cachear por defecto sin verificar explícitamente qué es seguro compartir entre usuarios — un error de cacheo de contenido personalizado (mostrar los datos de un usuario a otro por una clave de caché mal diseñada) es un incidente de seguridad grave (ver `CLAUDE.md` sección 12).

## Origin Access Control: bucket S3 privado servido solo vía CloudFront

```
Bucket S3: Block Public Access habilitado, política de bucket permite acceso SOLO desde CloudFront
```

Servir contenido de S3 a través de CloudFront sin hacer el bucket público (ver [`../aws-storage/s3.md`](../aws-storage/s3.md)) — Origin Access Control garantiza que el contenido solo es accesible vía la distribución CloudFront, no directamente contra la URL de S3, permitiendo aplicar controles adicionales (firmado de URLs, WAF) en el punto de entrada único.

## Invalidación de caché: coste y alternativas

```
Invalidación explícita: aws cloudfront create-invalidation --paths "/assets/*"
```

Invalidar caché tiene coste (por número de rutas invalidadas) y no es instantáneo globalmente — para contenido que cambia con versión (assets con hash en el nombre de archivo, `app.a1b2c3.js`), preferir versionado del nombre de archivo sobre invalidación explícita, evitando el problema por diseño en lugar de gestionarlo reactivamente.

## WAF integrado: protección en el borde

Asociar AWS WAF a la distribución CloudFront permite filtrar tráfico malicioso (inyección SQL, XSS, bots) en el punto más cercano al usuario, antes de que la petición llegue siquiera al origen — ver `CLAUDE.md` sección 12 para el marco general de seguridad que esto complementa.

## Checklist

- [ ] ¿El bucket S3 origen usa Origin Access Control, sin ser públicamente accesible directamente?
- [ ] ¿Las Cache Policies distinguen explícitamente contenido cacheable de contenido personalizado que nunca debe compartirse entre usuarios?
- [ ] ¿Se prefiere versionado de nombre de archivo sobre invalidación explícita para contenido con cambios frecuentes?
- [ ] ¿WAF está asociado a la distribución para filtrar tráfico malicioso en el borde?

Ver también: [`../aws-storage/s3.md`](../aws-storage/s3.md), [`alb.md`](./alb.md), [`../../decisions/cloudfront-vs-alb.md`](../../decisions/cloudfront-vs-alb.md).
