# Recipe: Configurar una VPC Segura (AWS)

Aplica: [`../knowledge/aws-network/vpc.md`](../knowledge/aws-network/vpc.md), [`../knowledge/aws-security/iam.md`](../knowledge/aws-security/iam.md), `CLAUDE.md` sección 12.

## Pasos

### 1. Definir la VPC con subredes públicas y privadas separadas

```hcl
resource "aws_vpc" "main" {
  cidr_block = "10.0.0.0/16"
}

resource "aws_subnet" "publica_a" {
  vpc_id                  = aws_vpc.main.id
  cidr_block               = "10.0.1.0/24"
  availability_zone        = "eu-west-1a"
  map_public_ip_on_launch  = true  # solo subredes que realmente necesitan exposición directa
}

resource "aws_subnet" "privada_a" {
  vpc_id            = aws_vpc.main.id
  cidr_block         = "10.0.11.0/24"
  availability_zone  = "eu-west-1a"
  # sin map_public_ip_on_launch — recursos internos, ver knowledge/aws-network/vpc.md
}
```

Réplica en `eu-west-1b` para multi-AZ — nunca una VPC de producción en una única zona de disponibilidad.

### 2. NAT Gateway para salida controlada desde subredes privadas

```hcl
resource "aws_nat_gateway" "main" {
  allocation_id = aws_eip.nat.id
  subnet_id     = aws_subnet.publica_a.id
}

resource "aws_route" "privada_salida" {
  route_table_id         = aws_route_table.privada.id
  destination_cidr_block = "0.0.0.0/0"
  nat_gateway_id          = aws_nat_gateway.main.id
}
```

### 3. Security Groups con menor privilegio explícito

```hcl
resource "aws_security_group" "alb" {
  vpc_id = aws_vpc.main.id
  ingress { from_port = 443; to_port = 443; protocol = "tcp"; cidr_blocks = ["0.0.0.0/0"] }
  egress  { from_port = 0;   to_port = 0;   protocol = "-1"; cidr_blocks = ["0.0.0.0/0"] }
}

resource "aws_security_group" "app" {
  vpc_id = aws_vpc.main.id
  ingress {
    from_port       = 8080
    to_port         = 8080
    protocol        = "tcp"
    security_groups = [aws_security_group.alb.id]  # SOLO desde el ALB, nunca 0.0.0.0/0
  }
}

resource "aws_security_group" "database" {
  vpc_id = aws_vpc.main.id
  ingress {
    from_port       = 5432
    to_port         = 5432
    protocol        = "tcp"
    security_groups = [aws_security_group.app.id]  # SOLO desde la capa de aplicación
  }
}
```

Ver [`../knowledge/aws-network/vpc.md`](../knowledge/aws-network/vpc.md) — cada capa (ALB → app → base de datos) solo acepta tráfico de la capa inmediatamente anterior, nunca abierto a internet más allá del ALB.

### 4. VPC Flow Logs para auditoría de tráfico

```hcl
resource "aws_flow_log" "main" {
  vpc_id          = aws_vpc.main.id
  traffic_type    = "ALL"
  log_destination = aws_cloudwatch_log_group.vpc_flow_logs.arn
}
```

Ver [`../knowledge/aws-monitoring/cloudtrail.md`](../knowledge/aws-monitoring/cloudtrail.md) — los Flow Logs son el equivalente a nivel de red de la auditoría de CloudTrail a nivel de API, esenciales para investigar tráfico anómalo.

### 5. Verificación de aislamiento

```bash
# Confirmar que ningún recurso de base de datos tiene IP pública asignada
aws ec2 describe-instances --filters "Name=subnet-id,Values=subnet-privada-a" \
  --query 'Reservations[].Instances[].PublicIpAddress'
```

## Checklist final

- [ ] ¿Los recursos internos (bases de datos, backend) viven en subredes privadas, sin IP pública?
- [ ] ¿Los Security Groups siguen menor privilegio en cadena (ALB → app → BD), sin saltos directos?
- [ ] ¿La VPC abarca múltiples AZs, no una única zona de disponibilidad?
- [ ] ¿VPC Flow Logs está habilitado?
- [ ] Ejecutar [`../checklists/architecture.md`](../checklists/architecture.md).

Ver también: [`../knowledge/aws-network/vpc.md`](../knowledge/aws-network/vpc.md), `CLAUDE.md` sección 12.
