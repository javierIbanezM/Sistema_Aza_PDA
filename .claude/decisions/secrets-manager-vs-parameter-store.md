# Secrets Manager vs. Parameter Store

## Contexto

Ambos almacenan configuración/secretos de forma centralizada en AWS (Parameter Store es parte de Systems Manager) — la elección entre ambos depende de si el valor es genuinamente un secreto sensible o configuración general.

## Comparativa

| Criterio | Secrets Manager | Parameter Store (SecureString) |
|---|---|---|
| Coste | Por secreto almacenado + por API call | Nivel estándar gratuito generoso; nivel avanzado con coste menor |
| Rotación automática nativa | Sí, con integración directa a RDS/Redshift/DocumentDB | No nativa — requiere automatización propia (Lambda + EventBridge) |
| Cifrado | Sí, vía KMS | Sí, vía KMS (en el tipo SecureString) |
| Versionado | Sí | Sí |
| Tamaño máximo de valor | Mayor | Menor (4KB en el nivel estándar) |
| Uso típico | Credenciales de base de datos, API keys de terceros con rotación | Configuración de aplicación, flags, valores no rotables o rotados manualmente |

## Cuándo elegir Secrets Manager

- El secreto requiere rotación automática, especialmente si es un tipo con integración nativa (credenciales RDS).
- El coste adicional está justificado por el riesgo de negocio de esa credencial concreta (ej. credenciales de un sistema de pagos).
- Se necesita almacenar secretos de tamaño superior al límite de Parameter Store.

## Cuándo elegir Parameter Store

- Configuración de aplicación no sensible o sensible pero sin necesidad de rotación automática (una URL de servicio externo, un flag de feature).
- Se prioriza minimizar coste sobre las funcionalidades avanzadas de rotación de Secrets Manager.
- Volumen alto de parámetros de configuración donde el coste por-secreto de Secrets Manager sería significativo a esa escala.

## Recomendación por defecto de este framework

**Secrets Manager para credenciales genuinamente sensibles con necesidad de rotación** (bases de datos, API keys de proveedores externos críticos, ver `CLAUDE.md` sección 12.2); **Parameter Store (SecureString) para el resto de configuración**, sensible o no, que no requiere rotación automática nativa — ambos cumplen el principio del framework de nunca hardcodear configuración/secretos en código o en variables de entorno de despliegue en texto plano.

Ver también: [`../knowledge/aws-security/secrets-manager.md`](../knowledge/aws-security/secrets-manager.md), [`../knowledge/aws-security/kms.md`](../knowledge/aws-security/kms.md), `CLAUDE.md` sección 12.2.
