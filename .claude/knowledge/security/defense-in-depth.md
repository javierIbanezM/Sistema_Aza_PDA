# Defense in Depth

> Este documento no repite los controles individuales (ya documentados en sus dominios: [`../aws-network/vpc.md`](../aws-network/vpc.md), [`../aws-security/iam.md`](../aws-security/iam.md), [`../../standards/validation.md`](../../standards/validation.md)...) — cubre el **principio de composición**: por qué ninguna capa individual debe considerarse suficiente por sí sola.

## Ninguna capa es infalible: el diseño asume que cada una puede fallar

```
Capa 1: WAF/Security Group — filtra tráfico malicioso antes de llegar a la aplicación
Capa 2: Validación de entrada en la aplicación — nunca confía en que la Capa 1 ya filtró todo
Capa 3: Parametrización de queries — nunca confía en que la validación de entrada ya es suficiente
Capa 4: Permisos de base de datos de menor privilegio — nunca confía en que el código de aplicación es perfecto
Capa 5: Auditoría/detección — asume que, pese a todo lo anterior, algo podría pasar desapercibido
```

Cada capa se diseña **como si las anteriores no existieran** — un Security Group bien configurado no es excusa para omitir validación de entrada en la aplicación; una aplicación bien validada no es excusa para dar permisos amplios a la base de datos. Esta redundancia deliberada es la esencia de defense in depth: un atacante que evade una capa se encuentra con la siguiente, en lugar de tener acceso completo tras un único fallo.

## El error más común: tratar la capa perimetral como suficiente

```
Antipatrón: "Ya tenemos WAF, no hace falta validar en el backend"
```

Un perímetro bien defendido (WAF, VPC con subredes privadas, ver [`../aws-network/vpc.md`](../aws-network/vpc.md)) reduce el riesgo, pero nunca lo elimina — amenazas internas, configuraciones erróneas del propio perímetro, o vulnerabilidades zero-day en la capa perimetral misma siguen siendo posibles. Ver [`zero-trust.md`](./zero-trust.md) para el modelo que lleva este principio a su conclusión lógica: no confiar en ninguna capa, ni siquiera la interna.

## Defense in depth aplicado a datos sensibles: ejemplo completo

```
1. Red: base de datos en subred privada, sin acceso directo desde internet (ver ../aws-network/vpc.md)
2. Autenticación: usuario de aplicación con credencial rotada automáticamente (ver ../aws-security/secrets-manager.md)
3. Autorización: permisos mínimos, solo las tablas/operaciones necesarias (ver ../sqlserver/security.md)
4. Cifrado: en tránsito (TLS) y en reposo (ver ../aws-security/kms.md)
5. Validación de aplicación: nunca confía en que los datos ya fueron sanitizados antes de llegar
6. Auditoría: registro de accesos a las tablas con datos sensibles (ver ../sqlserver/security.md)
```

Seis capas independientes protegiendo el mismo activo — un atacante necesitaría comprometer varias, no una única, para acceder a los datos sin detección.

## Coste de defense in depth: proporcional al valor del activo protegido

Aplicar seis capas de protección a un dato público sin valor es sobre-ingeniería (ver [`../architecture/dry-kiss-yagni.md`](../architecture/dry-kiss-yagni.md)) — el número y profundidad de capas debe ser proporcional al riesgo real del activo (ver [`risk-assessment.md`](./risk-assessment.md)), no aplicado uniformemente a todo el sistema sin diferenciar.

## Checklist

- [ ] ¿Cada capa de seguridad está diseñada asumiendo que las anteriores podrían fallar, sin depender de ellas?
- [ ] ¿Existe validación en la aplicación aunque ya exista un WAF/perímetro?
- [ ] ¿El número de capas aplicadas es proporcional al riesgo real del activo protegido?

Ver también: [`zero-trust.md`](./zero-trust.md), [`risk-assessment.md`](./risk-assessment.md), [`security-by-design.md`](./security-by-design.md).
