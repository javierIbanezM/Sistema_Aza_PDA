# Recipe: Desplegar un Servicio en ECS Fargate (AWS)

Aplica: [`../knowledge/aws-compute/ecs.md`](../knowledge/aws-compute/ecs.md), [`../knowledge/aws-network/alb.md`](../knowledge/aws-network/alb.md), [`../knowledge/aws-security/iam.md`](../knowledge/aws-security/iam.md).

## Pasos

### 1. Task Definition con recursos y health check explícitos

```hcl
resource "aws_ecs_task_definition" "pedidos_service" {
  family                   = "pedidos-service"
  requires_compatibilities = ["FARGATE"]
  network_mode             = "awsvpc"
  cpu                      = 256
  memory                   = 512
  execution_role_arn       = aws_iam_role.ecs_execution.arn
  task_role_arn             = aws_iam_role.pedidos_service_task.arn  # permisos de la APLICACIÓN, distinto del execution role

  container_definitions = jsonencode([{
    name  = "app"
    image = "${aws_ecr_repository.pedidos.repository_url}:${var.image_tag}"
    portMappings = [{ containerPort = 8080 }]
    healthCheck = {
      command  = ["CMD-SHELL", "curl -f http://localhost:8080/actuator/health || exit 1"]
      interval = 30
      retries  = 3
    }
    secrets = [
      { name = "DB_PASSWORD", valueFrom = aws_secretsmanager_secret.db_password.arn }
    ]
    logConfiguration = {
      logDriver = "awslogs"
      options   = { "awslogs-group" = "/ecs/pedidos-service", "awslogs-region" = var.region }
    }
  }])
}
```

Distinguir `execution_role` (permisos para que ECS arranque el contenedor: pull de ECR, escritura de logs) de `task_role` (permisos de la propia aplicación: acceso a DynamoDB, S3) — ver [`../knowledge/aws-security/iam.md`](../knowledge/aws-security/iam.md), principio de menor privilegio aplicado a cada uno por separado.

### 2. Service con Auto Scaling y despliegue gradual

```hcl
resource "aws_ecs_service" "pedidos_service" {
  name            = "pedidos-service"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.pedidos_service.arn
  desired_count   = 2  # mínimo 2 para alta disponibilidad, nunca 1 en producción
  launch_type     = "FARGATE"

  deployment_controller { type = "ECS" }
  deployment_minimum_healthy_percent = 100
  deployment_maximum_percent         = 200  # rolling deployment, nunca todas las tasks reemplazadas a la vez

  load_balancer {
    target_group_arn = aws_lb_target_group.pedidos.arn
    container_name    = "app"
    container_port    = 8080
  }
}

resource "aws_appautoscaling_target" "pedidos_service" {
  max_capacity       = 10
  min_capacity       = 2
  resource_id        = "service/${aws_ecs_cluster.main.name}/${aws_ecs_service.pedidos_service.name}"
  scalable_dimension = "ecs:service:DesiredCount"
  service_namespace  = "ecs"
}
```

### 3. Target Group con health check en el ALB

Ver [`../knowledge/aws-network/alb.md`](../knowledge/aws-network/alb.md) — el health check del Target Group debe coincidir con el endpoint real de salud de la aplicación.

### 4. Pipeline de despliegue (referencia conceptual, ver `standards/git.md`)

```
Build imagen → push a ECR con tag = SHA del commit (nunca "latest" en producción, ver CLAUDE.md sección 9.3)
→ Actualizar Task Definition con el nuevo tag
→ ECS despliega gradualmente (deployment_maximum_percent controla el ritmo)
→ Health checks verifican cada task antes de enrutar tráfico
```

### 5. Verificar observabilidad

Confirmar que el Log Group tiene retención configurada (ver [`../knowledge/aws-monitoring/cloudwatch.md`](../knowledge/aws-monitoring/cloudwatch.md)) y que X-Ray está habilitado si la aplicación participa en trazas distribuidas (ver [`../knowledge/aws-monitoring/xray.md`](../knowledge/aws-monitoring/xray.md)).

## Checklist final

- [ ] ¿`desired_count` es ≥ 2 para alta disponibilidad?
- [ ] ¿Execution role y Task role están separados, cada uno con permisos mínimos?
- [ ] ¿Los secretos se inyectan desde Secrets Manager, nunca hardcodeados en la Task Definition?
- [ ] ¿El tag de imagen es inmutable (SHA de commit), nunca `latest`?
- [ ] Ejecutar [`../checklists/architecture.md`](../checklists/architecture.md).

Ver también: [`../snippets/aws/ecs-service-terraform.tf`](../snippets/aws/ecs-service-terraform.tf).
