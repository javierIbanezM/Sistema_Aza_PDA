# KMS (Key Management Service)

> Ver [`iam.md`](./iam.md) para el control de acceso a las claves gestionadas aquí, y [`../aws-storage/s3.md`](../aws-storage/s3.md) para un consumidor típico de cifrado KMS.

## Qué es: gestión centralizada de claves de cifrado

KMS gestiona el ciclo de vida de claves de cifrado (creación, rotación, política de acceso, auditoría de uso) usadas para cifrar datos en reposo a través de la mayoría de servicios AWS (S3, RDS, EBS, Secrets Manager) sin que cada servicio necesite implementar su propia gestión de claves.

## Claves gestionadas por AWS vs. gestionadas por el cliente (CMK)

| Tipo | Control | Uso |
|---|---|---|
| AWS managed key | AWS gestiona rotación y política, sin visibilidad de uso individual | Cifrado por defecto sin requisitos específicos de auditoría |
| Customer managed key (CMK) | El equipo controla política de acceso, rotación, y puede auditar cada uso individual | Datos sensibles con requisito de auditoría granular o control explícito de quién puede descifrar |

Usar una CMK cuando se necesita **auditar exactamente quién descifró un dato concreto y cuándo** (relevante para cumplimiento normativo) o cuando distintos consumidores del mismo dato cifrado deben tener permisos de descifrado diferenciados — el uso de una AWS managed key no permite ese nivel de control granular.

## Rotación automática de claves

```
CMK con rotación automática habilitada: AWS genera material criptográfico nuevo anualmente,
manteniendo el material anterior para poder descifrar datos cifrados previamente
```

La rotación no re-cifra los datos existentes (seguirían siendo válidos con el material de clave anterior, retenido automáticamente) — reduce el riesgo asociado a un único material criptográfico usado indefinidamente, sin requerir un proceso de re-cifrado masivo de datos existentes.

## Envelope Encryption: cómo funciona realmente el cifrado a gran escala

```
1. KMS genera una Data Key única para el objeto a cifrar
2. Los datos se cifran localmente con esa Data Key (rápido, sin llamada a KMS por cada byte)
3. La Data Key se cifra con la CMK y se almacena junto a los datos cifrados
4. Para descifrar: se pide a KMS descifrar la Data Key, luego se usa localmente para descifrar los datos
```

KMS no cifra directamente grandes volúmenes de datos (tiene límites de tamaño por llamada) — el patrón estándar (usado internamente por S3/RDS/EBS) es *envelope encryption*: una clave de datos única por objeto, cifrada a su vez por la CMK, minimizando las llamadas directas a KMS necesarias.

## Política de clave: quién puede administrar vs. quién puede usar

```json
{ "Effect": "Allow", "Principal": { "AWS": "arn:...role/pedidos-service" }, "Action": ["kms:Decrypt"] }
{ "Effect": "Allow", "Principal": { "AWS": "arn:...role/administradores-seguridad" }, "Action": "kms:*" }
```

Separar explícitamente quién puede **usar** la clave (cifrar/descifrar) de quién puede **administrarla** (rotar, cambiar política, eliminar) — mismo principio de menor privilegio que [`iam.md`](./iam.md), aplicado específicamente a la gestión de claves criptográficas.

## Checklist

- [ ] ¿Se usa una CMK cuando se necesita auditoría granular o control diferenciado de descifrado, en lugar de una clave gestionada por AWS por defecto sin ese control?
- [ ] ¿La rotación automática está habilitada para claves de larga vida?
- [ ] ¿La política de clave separa explícitamente permisos de uso de permisos de administración?

Ver también: [`iam.md`](./iam.md), [`secrets-manager.md`](./secrets-manager.md), [`../aws-storage/s3.md`](../aws-storage/s3.md).
