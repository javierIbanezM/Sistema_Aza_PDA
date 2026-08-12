# snippets/aws/vpc-terraform.tf
# VPC con subredes públicas/privadas, NAT Gateway y Security Groups en cadena — ver recipes/aws-setup-vpc.md.

resource "aws_vpc" "main" {
  cidr_block           = "10.0.0.0/16"
  enable_dns_support   = true
  enable_dns_hostnames = true
}

resource "aws_subnet" "publica_a" {
  vpc_id                  = aws_vpc.main.id
  cidr_block               = "10.0.1.0/24"
  availability_zone        = "eu-west-1a"
  map_public_ip_on_launch  = true
}

resource "aws_subnet" "privada_a" {
  vpc_id            = aws_vpc.main.id
  cidr_block         = "10.0.11.0/24"
  availability_zone  = "eu-west-1a"
}

resource "aws_subnet" "privada_b" {
  vpc_id            = aws_vpc.main.id
  cidr_block         = "10.0.12.0/24"
  availability_zone  = "eu-west-1b"
}

resource "aws_internet_gateway" "main" {
  vpc_id = aws_vpc.main.id
}

resource "aws_eip" "nat" {
  domain = "vpc"
}

resource "aws_nat_gateway" "main" {
  allocation_id = aws_eip.nat.id
  subnet_id     = aws_subnet.publica_a.id
}

resource "aws_route_table" "privada" {
  vpc_id = aws_vpc.main.id
  route {
    cidr_block     = "0.0.0.0/0"
    nat_gateway_id = aws_nat_gateway.main.id
  }
}

# Security Groups en cadena: internet -> ALB -> app -> base de datos, sin saltos directos
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
    security_groups = [aws_security_group.alb.id]
  }
  egress { from_port = 0; to_port = 0; protocol = "-1"; cidr_blocks = ["0.0.0.0/0"] }
}

resource "aws_security_group" "database" {
  vpc_id = aws_vpc.main.id
  ingress {
    from_port       = 5432
    to_port         = 5432
    protocol        = "tcp"
    security_groups = [aws_security_group.app.id]
  }
}

resource "aws_cloudwatch_log_group" "vpc_flow_logs" {
  name              = "/vpc/flow-logs"
  retention_in_days = 90
}

resource "aws_flow_log" "main" {
  vpc_id          = aws_vpc.main.id
  traffic_type    = "ALL"
  log_destination = aws_cloudwatch_log_group.vpc_flow_logs.arn
}
