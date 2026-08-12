# VPC (Virtual Private Cloud)

> Ver [`../aws-security/iam.md`](../aws-security/iam.md) para el control de acceso a nivel de identidad, complementario al control de acceso a nivel de red que documenta este archivo.

## Qué es: red privada aislada dentro de AWS

Una VPC define el espacio de red (rango de IPs, subredes, tablas de rutas) donde viven los recursos de AWS — el equivalente cloud a diseñar la topología de red de un datacenter propio, pero definido como código (Terraform/CloudFormation) en lugar de cableado físico.

## Subredes públicas vs. privadas

```
Subred pública (10.0.1.0/24): ruta hacia un Internet Gateway — recursos con IP pública, accesibles desde internet
Subred privada (10.0.2.0/24): sin ruta directa a internet — recursos internos (bases de datos, servicios backend)
```

Principio de menor exposición: solo los recursos que genuinamente necesitan ser alcanzables desde internet (load balancers, bastiones) viven en subredes públicas — bases de datos, servicios internos, workers de procesamiento viven en subredes privadas, inalcanzables directamente desde fuera de la VPC.

## NAT Gateway: salida a internet desde subredes privadas

```
Recurso en subred privada → NAT Gateway (en subred pública) → Internet Gateway → Internet
```

Un recurso en subred privada que necesita salida a internet (descargar dependencias, llamar a una API externa) pero no debe ser alcanzable desde internet usa un NAT Gateway — permite tráfico saliente sin exponer el recurso a conexiones entrantes no solicitadas.

## Security Groups vs. Network ACLs

| | Security Group | Network ACL |
|---|---|---|
| Nivel | Por instancia/recurso | Por subred |
| Estado | Stateful (el tráfico de respuesta se permite automáticamente) | Stateless (reglas de entrada y salida independientes) |
| Reglas | Solo `allow` | `allow` y `deny` explícitos |

Security Groups como primera línea de defensa (mismo principio que EC2, ver [`../aws-compute/ec2.md`](../aws-compute/ec2.md)); Network ACLs como capa adicional a nivel de subred, útil para bloquear explícitamente rangos de IP conocidos como maliciosos a nivel de red, antes de que el tráfico llegue a evaluarse por Security Group.

## VPC Peering y Transit Gateway: conectar VPCs entre sí

- **VPC Peering**: conexión punto a punto entre dos VPCs — simple, pero no transitivo (A-B y B-C no implica A-C automáticamente) y se vuelve difícil de gestionar con muchas VPCs.
- **Transit Gateway**: hub centralizado que conecta múltiples VPCs (y conexiones on-premise vía VPN/Direct Connect) — apropiado a partir de un número moderado de VPCs que necesitan interconectarse, evitando la complejidad combinatoria del peering punto a punto.

## Checklist

- [ ] ¿Los recursos sin necesidad de exposición directa a internet viven en subredes privadas?
- [ ] ¿Existe NAT Gateway para salida controlada a internet desde subredes privadas, sin exponerlas a tráfico entrante?
- [ ] ¿Se usa Transit Gateway en lugar de peering punto a punto cuando el número de VPCs a interconectar crece?

Ver también: [`alb.md`](./alb.md), [`../aws-compute/ec2.md`](../aws-compute/ec2.md), [`../aws-security/iam.md`](../aws-security/iam.md).
