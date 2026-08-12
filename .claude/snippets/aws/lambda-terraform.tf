# snippets/aws/lambda-terraform.tf
# Función Lambda con rol de menor privilegio + trigger SQS + X-Ray — ver recipes/aws-create-lambda.md.

resource "aws_iam_role" "lambda_execution" {
  name = "confirmar-pedido-lambda"
  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{ Action = "sts:AssumeRole", Effect = "Allow", Principal = { Service = "lambda.amazonaws.com" } }]
  })
}

resource "aws_iam_role_policy" "lambda_permissions" {
  role = aws_iam_role.lambda_execution.id
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect   = "Allow"
        Action   = ["dynamodb:GetItem", "dynamodb:PutItem"]
        Resource = aws_dynamodb_table.pedidos.arn
      },
      {
        Effect   = "Allow"
        Action   = ["xray:PutTraceSegments", "xray:PutTelemetryRecords"]
        Resource = "*"
      }
    ]
  })
}

resource "aws_lambda_function" "confirmar_pedido" {
  function_name = "confirmar-pedido"
  role          = aws_iam_role.lambda_execution.arn
  runtime       = "java21"
  handler       = "com.empresa.pedidos.ConfirmarPedidoHandler::handleRequest"
  memory_size   = 512
  timeout       = 30
  filename      = "target/confirmar-pedido.jar"

  environment {
    variables = { TABLE_NAME = aws_dynamodb_table.pedidos.name }
  }

  tracing_config { mode = "Active" }
}

resource "aws_lambda_event_source_mapping" "sqs_trigger" {
  event_source_arn = aws_sqs_queue.pedidos_confirmados.arn
  function_name    = aws_lambda_function.confirmar_pedido.arn
  batch_size       = 10
}

resource "aws_sqs_queue" "pedidos_confirmados" {
  name                       = "pedidos-confirmados"
  visibility_timeout_seconds = 60
  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.pedidos_confirmados_dlq.arn
    maxReceiveCount      = 3
  })
}

resource "aws_sqs_queue" "pedidos_confirmados_dlq" {
  name = "pedidos-confirmados-dlq"
}
