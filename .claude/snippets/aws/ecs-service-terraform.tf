# snippets/aws/ecs-service-terraform.tf
# Task Definition + Service + Auto Scaling en ECS Fargate — ver recipes/aws-deploy-ecs-service.md.

resource "aws_iam_role" "ecs_execution" {
  name = "pedidos-service-execution"
  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{ Action = "sts:AssumeRole", Effect = "Allow", Principal = { Service = "ecs-tasks.amazonaws.com" } }]
  })
}

resource "aws_iam_role_policy_attachment" "ecs_execution_managed" {
  role       = aws_iam_role.ecs_execution.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

resource "aws_iam_role" "pedidos_service_task" {
  name = "pedidos-service-task"
  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{ Action = "sts:AssumeRole", Effect = "Allow", Principal = { Service = "ecs-tasks.amazonaws.com" } }]
  })
}

resource "aws_iam_role_policy" "pedidos_service_task_policy" {
  role = aws_iam_role.pedidos_service_task.id
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{ Effect = "Allow", Action = ["dynamodb:GetItem", "dynamodb:PutItem"], Resource = aws_dynamodb_table.pedidos.arn }]
  })
}

resource "aws_ecs_task_definition" "pedidos_service" {
  family                   = "pedidos-service"
  requires_compatibilities = ["FARGATE"]
  network_mode             = "awsvpc"
  cpu                      = 256
  memory                   = 512
  execution_role_arn       = aws_iam_role.ecs_execution.arn
  task_role_arn             = aws_iam_role.pedidos_service_task.arn

  container_definitions = jsonencode([{
    name  = "app"
    image = "${aws_ecr_repository.pedidos.repository_url}:${var.image_tag}"
    portMappings = [{ containerPort = 8080 }]
    healthCheck = {
      command  = ["CMD-SHELL", "curl -f http://localhost:8080/actuator/health || exit 1"]
      interval = 30
      retries  = 3
    }
    secrets = [{ name = "DB_PASSWORD", valueFrom = aws_secretsmanager_secret.db_password.arn }]
    logConfiguration = {
      logDriver = "awslogs"
      options   = { "awslogs-group" = "/ecs/pedidos-service", "awslogs-region" = var.region, "awslogs-stream-prefix" = "app" }
    }
  }])
}

resource "aws_ecs_service" "pedidos_service" {
  name            = "pedidos-service"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.pedidos_service.arn
  desired_count   = 2
  launch_type     = "FARGATE"

  network_configuration {
    subnets         = [aws_subnet.privada_a.id, aws_subnet.privada_b.id]
    security_groups = [aws_security_group.app.id]
  }

  deployment_minimum_healthy_percent = 100
  deployment_maximum_percent         = 200

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

resource "aws_appautoscaling_policy" "cpu" {
  name               = "pedidos-service-cpu-scaling"
  policy_type        = "TargetTrackingScaling"
  resource_id        = aws_appautoscaling_target.pedidos_service.resource_id
  scalable_dimension = aws_appautoscaling_target.pedidos_service.scalable_dimension
  service_namespace  = aws_appautoscaling_target.pedidos_service.service_namespace

  target_tracking_scaling_policy_configuration {
    predefined_metric_specification { predefined_metric_type = "ECSServiceAverageCPUUtilization" }
    target_value = 70.0
  }
}
