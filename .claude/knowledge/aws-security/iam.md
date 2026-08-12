# IAM (Identity and Access Management)

> Ver `CLAUDE.md` sección 12 para el marco general de seguridad y menor privilegio que IAM implementa a nivel de AWS.

## Principio de menor privilegio: la base de todo IAM

```json
{
  "Effect": "Allow",
  "Action": ["s3:GetObject"],
  "Resource": "arn:aws:s3:::bucket-pedidos/exports/*"
}
```

Nunca `"Action": "*"` ni `"Resource": "*"` salvo un rol de administración muy restringido y auditado — cada política debe conceder exactamente las acciones sobre exactamente los recursos que ese rol/usuario necesita, mismo principio que el resto del framework aplica a nivel de base de datos (ver [`../sqlserver/security.md`](../sqlserver/security.md)) y de aplicación.

## Roles vs. Usuarios: roles para todo lo que no sea una persona

```
Usuario IAM: para personas que acceden a la consola/CLI directamente (con MFA obligatorio)
Rol IAM: para servicios (EC2, Lambda, ECS) y para acceso temporal federado
```

Un servicio (una instancia EC2, una función Lambda) nunca debe usar credenciales de un usuario IAM de larga duración embebidas en su configuración — asume un rol IAM, que provee credenciales temporales gestionadas automáticamente por AWS, eliminando el riesgo de credenciales estáticas filtradas en código o configuración.

## Políticas basadas en identidad vs. basadas en recurso

```json
// Política de recurso (en el bucket S3 mismo): quién puede acceder A ESTE bucket
{ "Effect": "Allow", "Principal": { "AWS": "arn:aws:iam::123456789:role/pedidos-service" }, "Action": "s3:GetObject" }
```

Las políticas basadas en recurso (bucket policies de S3, políticas de KMS) permiten conceder acceso cross-account sin necesidad de asumir un rol en la cuenta del recurso — complementan (no sustituyen) las políticas basadas en identidad adjuntas a roles/usuarios; el acceso efectivo es la intersección de ambas.

## MFA obligatorio para acciones sensibles

```json
"Condition": { "Bool": { "aws:MultiFactorAuthPresent": "true" } }
```

Requerir MFA explícitamente en políticas para acciones de alto impacto (eliminar recursos críticos, modificar políticas IAM) — una condición adicional de defensa en profundidad más allá del login inicial con MFA.

## IAM Access Analyzer: detectar acceso no intencionado

Analiza automáticamente políticas de recursos (buckets S3, roles) y señala cuándo un recurso es accesible desde fuera de la cuenta/organización de forma que probablemente no fue intencionada — herramienta de verificación continua, no solo revisión manual puntual de políticas.

## Checklist

- [ ] ¿Cada política concede exactamente las acciones/recursos necesarios, sin comodines amplios sin justificar?
- [ ] ¿Los servicios (EC2, Lambda, ECS) usan roles IAM, nunca credenciales de usuario embebidas?
- [ ] ¿MFA es obligatorio tanto para usuarios humanos como condición adicional en acciones de alto impacto?
- [ ] ¿IAM Access Analyzer está habilitado y revisado periódicamente?

Ver también: [`kms.md`](./kms.md), [`secrets-manager.md`](./secrets-manager.md), `CLAUDE.md` sección 12.
