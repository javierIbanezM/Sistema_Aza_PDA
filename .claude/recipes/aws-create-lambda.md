# Recipe: Crear una Función Lambda (AWS)

Aplica: [`../knowledge/aws-compute/lambda.md`](../knowledge/aws-compute/lambda.md), [`../knowledge/aws-security/iam.md`](../knowledge/aws-security/iam.md), [`../knowledge/integration/idempotency.md`](../knowledge/integration/idempotency.md).

## Pasos

### 1. Definir el rol de ejecución con mínimo privilegio (Terraform)

```hcl
resource "aws_iam_role" "lambda_pedidos" {
  name = "pedidos-service-lambda"
  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{ Action = "sts:AssumeRole", Effect = "Allow", Principal = { Service = "lambda.amazonaws.com" } }]
  })
}

resource "aws_iam_role_policy" "lambda_pedidos_policy" {
  role = aws_iam_role.lambda_pedidos.id
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect   = "Allow"
      Action   = ["dynamodb:GetItem", "dynamodb:PutItem"]
      Resource = aws_dynamodb_table.pedidos.arn
    }]
  })
}
```

Ver [`../knowledge/aws-security/iam.md`](../knowledge/aws-security/iam.md) — nunca un rol genérico compartido, exactamente los permisos que esta función necesita.

### 2. Implementar el handler con idempotencia

```java
public class ConfirmarPedidoHandler implements RequestHandler<SQSEvent, Void> {

    @Override
    public Void handleRequest(SQSEvent event, Context context) {
        for (SQSEvent.SQSMessage mensaje : event.getRecords()) {
            String eventId = mensaje.getMessageAttributes().get("eventId").getStringValue();
            if (yaProcesado(eventId)) continue; // ver knowledge/integration/idempotency.md

            confirmarPedidoUseCase.ejecutar(deserializar(mensaje.getBody()));
            marcarProcesado(eventId);
        }
        return null;
    }
}
```

### 3. Definir la función Lambda con configuración explícita

```hcl
resource "aws_lambda_function" "confirmar_pedido" {
  function_name = "confirmar-pedido"
  role          = aws_iam_role.lambda_pedidos.arn
  runtime       = "java21"
  handler       = "com.empresa.pedidos.ConfirmarPedidoHandler::handleRequest"
  memory_size   = 512  # también determina la CPU proporcional, ver lambda.md
  timeout       = 30

  environment {
    variables = { TABLE_NAME = aws_dynamodb_table.pedidos.name }
  }
}
```

### 4. Conectar el trigger (SQS en este ejemplo)

```hcl
resource "aws_lambda_event_source_mapping" "sqs_trigger" {
  event_source_arn = aws_sqs_queue.pedidos_confirmados.arn
  function_name    = aws_lambda_function.confirmar_pedido.arn
  batch_size       = 10
}
```

Ver [`../knowledge/aws-integration/sqs.md`](../knowledge/aws-integration/sqs.md) — asegurar que la cola tiene una Dead Letter Queue configurada antes de conectar el trigger.

### 5. Instrumentar observabilidad

```hcl
resource "aws_lambda_function" "confirmar_pedido" {
  # ...
  tracing_config { mode = "Active" }  # habilita X-Ray, ver knowledge/aws-monitoring/xray.md
}
```

### 6. Test local con SAM/simulador de eventos

```java
@Test
void debeIgnorarUnMensajeYaProcesado() {
    when(processedEventRepository.yaProcesado(anyString())).thenReturn(true);
    handler.handleRequest(eventoDePrueba(), contextoDePrueba());
    verify(confirmarPedidoUseCase, never()).ejecutar(any());
}
```

## Checklist final

- [ ] ¿El rol de ejecución tiene únicamente los permisos que esta función necesita?
- [ ] ¿El handler es idempotente ante reentrega del trigger?
- [ ] ¿Memoria/timeout están calibrados según el trabajo real, no valores por defecto sin ajustar?
- [ ] ¿X-Ray está habilitado para trazabilidad?
- [ ] Ejecutar [`../checklists/architecture.md`](../checklists/architecture.md).

Ver también: [`../snippets/aws/lambda-terraform.tf`](../snippets/aws/lambda-terraform.tf).
