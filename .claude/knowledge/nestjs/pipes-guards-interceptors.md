# Pipes, Guards, Interceptors, Filters (NestJS)

> Ver [`architecture.md`](./architecture.md) para la organización general de módulos. Estos cuatro mecanismos son el equivalente NestJS a `@ExceptionHandler`, `@PreAuthorize` y AOP de Spring (ver [`../spring/spring-web.md`](../spring/spring-web.md), [`../spring/spring-security.md`](../spring/spring-security.md), [`../spring/spring-aop.md`](../spring/spring-aop.md)).

## Orden de ejecución en la cadena de una petición

```
Middleware → Guard → Interceptor (antes) → Pipe → Controller → Interceptor (después) → Exception Filter (si hay error)
```

## Guards: autorización

```typescript
@Injectable()
export class RolesGuard implements CanActivate {
  constructor(private reflector: Reflector) {}

  canActivate(context: ExecutionContext): boolean {
    const rolesRequeridos = this.reflector.get<string[]>('roles', context.getHandler());
    if (!rolesRequeridos) return true; // sin decorador @Roles, no restringe

    const { usuario } = context.switchToHttp().getRequest();
    return rolesRequeridos.some(rol => usuario.roles.includes(rol));
  }
}

export const Roles = (...roles: string[]) => SetMetadata('roles', roles);
```

```typescript
@Post()
@UseGuards(AuthGuard, RolesGuard)
@Roles('ADMIN')
async eliminar(@Param('id') id: string): Promise<void> { /* ... */ }
```

Un Guard decide **si** la petición puede continuar (`true`/`false`) — nunca modifica la petición ni la respuesta, solo autoriza o rechaza.

## Pipes: transformación y validación de entrada

```typescript
@Post()
async crear(@Body(new ValidationPipe({ whitelist: true })) dto: CrearPedidoDto) { /* ... */ }

// Pipe global (aplicado a todos los endpoints)
app.useGlobalPipes(new ValidationPipe({ whitelist: true, forbidNonWhitelisted: true, transform: true }));
```

Ver [`validation.md`](./validation.md) para el detalle completo de `ValidationPipe`. Un pipe personalizado puede transformar el valor además de validarlo:

```typescript
@Injectable()
export class ParseUuidPipe implements PipeTransform<string> {
  transform(value: string): string {
    if (!isUUID(value)) throw new BadRequestException(`${value} no es un UUID válido`);
    return value;
  }
}
```

## Interceptors: lógica transversal alrededor de la ejecución

```typescript
@Injectable()
export class LoggingInterceptor implements NestInterceptor {
  intercept(context: ExecutionContext, next: CallHandler): Observable<any> {
    const inicio = Date.now();
    const request = context.switchToHttp().getRequest();

    return next.handle().pipe(
      tap(() => logger.info(`${request.method} ${request.url} - ${Date.now() - inicio}ms`))
    );
  }
}
```

Equivalente conceptual a un `@Around` de Spring AOP (ver [`../spring/spring-aop.md`](../spring/spring-aop.md)) — puede ejecutar lógica antes y después de la llamada real, e incluso transformar la respuesta:

```typescript
@Injectable()
export class TransformResponseInterceptor implements NestInterceptor {
  intercept(context: ExecutionContext, next: CallHandler): Observable<any> {
    return next.handle().pipe(
      map(data => ({ data, timestamp: new Date().toISOString() })) // envuelve toda respuesta en un formato común
    );
  }
}
```

## Exception Filters: manejo centralizado de errores

```typescript
@Catch(DomainException)
export class DomainExceptionFilter implements ExceptionFilter {
  catch(exception: DomainException, host: ArgumentsHost): void {
    const response = host.switchToHttp().getResponse<Response>();
    const status = this.mapearCodigoHttp(exception);

    response.status(status).json({
      error: { code: exception.code, message: exception.message, details: [] },
    });
  }

  private mapearCodigoHttp(exception: DomainException): number {
    if (exception instanceof PedidoNoEncontradoException) return 404;
    if (exception instanceof PedidoYaConfirmadoException) return 409;
    return 500;
  }
}
```

```typescript
app.useGlobalFilters(new DomainExceptionFilter());
```

Equivalente a `@RestControllerAdvice` de Spring — ver [`../spring/spring-web.md`](../spring/spring-web.md). Formato de respuesta consistente con [`../../standards/api.md`](../../standards/api.md).

## Checklist

- [ ] ¿Los Guards solo autorizan/rechazan, sin lógica de negocio adicional?
- [ ] ¿Existe un Exception Filter global que mapea cada excepción de dominio a su código HTTP?
- [ ] ¿Los Interceptors de logging/transformación son genuinamente transversales, no lógica específica de un endpoint?
- [ ] ¿El `ValidationPipe` global usa `whitelist`/`forbidNonWhitelisted` según [`validation.md`](./validation.md)?

Ver también: [`architecture.md`](./architecture.md), [`validation.md`](./validation.md), [`../spring/spring-aop.md`](../spring/spring-aop.md).
