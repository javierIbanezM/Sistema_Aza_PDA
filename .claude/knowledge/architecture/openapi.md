# Documentación de APIs con OpenAPI / Swagger

> Documento único de referencia. Las anotaciones específicas de cada framework (springdoc-openapi en Spring, `@nestjs/swagger` en NestJS) se documentan en la Skill de esa tecnología, enlazando aquí para los conceptos del estándar.

## OpenAPI vs. Swagger — aclaración de términos

**OpenAPI** es el nombre de la especificación (actualmente en versión 3.1) que define cómo describir una API REST de forma estandarizada, legible tanto por humanos como por máquinas. **Swagger** es el nombre histórico (y de las herramientas asociadas: Swagger UI, Swagger Editor, Swagger Codegen) del proyecto que originó la especificación antes de donarse a la Linux Foundation bajo el nombre OpenAPI Initiative. En la práctica, "documentar con Swagger" y "documentar con OpenAPI" se usan como sinónimos.

## Por qué documentar la API como contrato, no como texto libre

Un documento OpenAPI (`openapi.yaml`/`openapi.json`) es:

- **Legible por máquinas**: permite generar automáticamente clientes SDK, validar peticiones/respuestas en tiempo de ejecución, generar mocks de servidor para desarrollo en paralelo de frontend/backend, y generar tests de contrato.
- **La única fuente de verdad**: si el contrato se define aquí y el código se genera/valida contra él (o se genera automáticamente a partir de las anotaciones del código), se elimina la duplicación entre "lo que la API realmente hace" y "lo que la documentación dice que hace" — un problema crónico de la documentación mantenida a mano por separado.

## Estructura de un documento OpenAPI

```yaml
openapi: 3.1.0
info:
  title: API de Pedidos
  version: 1.2.0
  description: Gestión de pedidos, líneas de pedido y su ciclo de vida.

servers:
  - url: https://api.ejemplo.com/v1
    description: Producción
  - url: https://staging-api.ejemplo.com/v1
    description: Staging

paths:
  /pedidos:
    post:
      summary: Crear un nuevo pedido
      operationId: crearPedido
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/CrearPedidoRequest'
      responses:
        '201':
          description: Pedido creado correctamente
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/PedidoResponse'
        '400':
          description: Datos de entrada inválidos
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'

  /pedidos/{pedidoId}:
    get:
      summary: Obtener un pedido por su identificador
      operationId: obtenerPedido
      parameters:
        - name: pedidoId
          in: path
          required: true
          schema:
            type: string
            format: uuid
      responses:
        '200':
          description: Pedido encontrado
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/PedidoResponse'
        '404':
          description: Pedido no encontrado
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'

components:
  schemas:
    CrearPedidoRequest:
      type: object
      required: [clienteId, lineas]
      properties:
        clienteId:
          type: string
          format: uuid
        lineas:
          type: array
          minItems: 1
          items:
            $ref: '#/components/schemas/LineaPedidoRequest'

    LineaPedidoRequest:
      type: object
      required: [productoId, cantidad]
      properties:
        productoId:
          type: string
          format: uuid
        cantidad:
          type: integer
          minimum: 1

    PedidoResponse:
      type: object
      properties:
        id:
          type: string
          format: uuid
        estado:
          type: string
          enum: [BORRADOR, CONFIRMADO, CANCELADO]
        total:
          type: number
          format: decimal

    ErrorResponse:
      type: object
      properties:
        error:
          type: object
          properties:
            code:
              type: string
              example: ORDER_NOT_FOUND
            message:
              type: string
            details:
              type: array
              items:
                type: string

  securitySchemes:
    bearerAuth:
      type: http
      scheme: bearer
      bearerFormat: JWT

security:
  - bearerAuth: []
```

### Elementos clave a no omitir

- **`operationId`** único por operación: es lo que usan los generadores de clientes para nombrar los métodos generados (`crearPedido()`, `obtenerPedido()`).
- **Todos los códigos de respuesta relevantes**, no solo el "camino feliz" — un consumidor de la API necesita saber qué esperar en `400`, `401`, `404`, `409`, no solo en `200`/`201`.
- **`required`** explícito en cada schema — la ausencia de este campo es una de las causas más comunes de contratos ambiguos.
- **Ejemplos (`example`/`examples`)** en los campos no triviales, especialmente en enums y formatos específicos (fechas, UUIDs, monedas).
- **Esquema de seguridad** (`securitySchemes`) declarado explícitamente, no asumido implícitamente por los consumidores.

## Generación desde código vs. especificación primero (code-first vs. spec-first)

| Enfoque | Cómo funciona | Ventaja | Riesgo |
|---|---|---|---|
| **Code-first** | Se anota el código (`@Operation`, `@ApiProperty`) y se genera el `openapi.yaml` a partir de él | El contrato nunca se desincroniza del código real | El diseño del contrato queda subordinado a la implementación, en vez de diseñarse pensando en el consumidor |
| **Spec-first** | Se escribe el `openapi.yaml` primero, y se genera el esqueleto de código (controladores, DTOs) a partir de él | Permite diseñar el contrato pensando en el consumidor antes de implementar; habilita trabajo en paralelo frontend/backend con mocks generados desde la spec | Requiere disciplina para mantener la spec como fuente de verdad y no dejar que el código generado se desvíe sin regenerar |

Este framework recomienda **spec-first para APIs públicas o consumidas por varios equipos** (el contrato es un producto en sí mismo que merece diseñarse deliberadamente) y **code-first para APIs internas de un único servicio con un único consumidor conocido**, donde el coste de mantener la spec por separado no se justifica.

## Versionado de la API

- Versión mayor en la URL (`/v1/`, `/v2/`) cuando el cambio rompe compatibilidad con clientes existentes.
- Cambios aditivos y no disruptivos (nuevo campo opcional en la respuesta, nuevo endpoint) no requieren nueva versión — mantener el principio de **compatibilidad hacia atrás** por defecto.
- El campo `info.version` del documento OpenAPI sigue versionado semántico (`MAJOR.MINOR.PATCH`) independientemente de la versión de la URL, para rastrear cambios del contrato con más granularidad que solo `v1`/`v2`.

## Swagger UI / herramientas de exploración

Exponer el documento OpenAPI a través de Swagger UI (o Redoc, Stoplight Elements) en entornos de desarrollo/staging permite a los consumidores explorar y probar la API interactivamente sin herramientas adicionales (Postman, curl). En producción, evaluar si exponerlo públicamente es apropiado según si la API es pública o interna — para APIs internas sensibles, restringir el acceso a la documentación igual que se restringe el acceso a la propia API.

## Checklist

- [ ] ¿Todas las operaciones tienen `operationId` único?
- [ ] ¿Se documentan todos los códigos de respuesta relevantes, incluidos los de error?
- [ ] ¿Los campos requeridos están marcados explícitamente con `required`?
- [ ] ¿El esquema de seguridad está declarado y aplicado a las operaciones que lo necesitan?
- [ ] ¿El documento se genera o valida automáticamente contra el código, evitando que se desincronice?
- [ ] ¿Un cambio que rompe compatibilidad está reflejado en un incremento de versión mayor?

Ver también: [`documentacion-tecnica.md`](./technical-documentation.md), sección 15 de `CLAUDE.md` (APIs REST y GraphQL).
