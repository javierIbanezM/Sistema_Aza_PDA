# Recipe: Crear un WebSocket (Node)

Aplica: [`../knowledge/node/event-loop.md`](../knowledge/node/event-loop.md), [`../knowledge/node/error-handling.md`](../knowledge/node/error-handling.md), `CLAUDE.md` sección 5.5.

## Pasos

### 1. Autenticar en el handshake, no confiar en mensajes posteriores

```typescript
import { WebSocketServer, WebSocket } from 'ws';

const wss = new WebSocketServer({ noServer: true });

server.on('upgrade', (request, socket, head) => {
  const token = extraerTokenDeQuery(request.url);

  jwtService.validarYExtraerClaims(token)
    .then(claims => {
      wss.handleUpgrade(request, socket, head, (ws) => {
        (ws as AuthenticatedWebSocket).usuario = claims;
        wss.emit('connection', ws, request);
      });
    })
    .catch(() => {
      socket.write('HTTP/1.1 401 Unauthorized\r\n\r\n');
      socket.destroy();
    });
});
```

Ver `CLAUDE.md` sección 5.5 — la autenticación debe verificarse **en el handshake** de conexión, nunca confiando en que el primer mensaje enviado por el cliente sea una credencial válida (un cliente malicioso puede omitirlo o falsificarlo).

### 2. Definir el protocolo de mensajes con un discriminated union

```typescript
type MensajeCliente =
  | { tipo: 'suscribir'; canal: string }
  | { tipo: 'desuscribir'; canal: string }
  | { tipo: 'ping' };

type MensajeServidor =
  | { tipo: 'pedido-actualizado'; pedidoId: string; estado: string }
  | { tipo: 'pong' }
  | { tipo: 'error'; mensaje: string };
```

Ver [`../knowledge/node/typescript.md`](../knowledge/node/typescript.md) — discriminated unions permiten un `switch` exhaustivo al procesar mensajes entrantes, detectando en compilación si se añade un tipo de mensaje nuevo sin manejarlo.

### 3. Manejar mensajes con validación explícita

```typescript
wss.on('connection', (ws: AuthenticatedWebSocket) => {
  const suscripciones = new Set<string>();

  ws.on('message', (data) => {
    let mensaje: MensajeCliente;
    try {
      mensaje = JSON.parse(data.toString()); // nunca confiar en el formato sin validar
    } catch {
      enviar(ws, { tipo: 'error', mensaje: 'JSON inválido' });
      return;
    }

    switch (mensaje.tipo) {
      case 'suscribir':
        if (!tieneAccesoAlCanal(ws.usuario, mensaje.canal)) {
          enviar(ws, { tipo: 'error', mensaje: 'Sin acceso a este canal' });
          return;
        }
        suscripciones.add(mensaje.canal);
        break;
      case 'desuscribir':
        suscripciones.delete(mensaje.canal);
        break;
      case 'ping':
        enviar(ws, { tipo: 'pong' });
        break;
    }
  });

  ws.on('close', () => {
    suscripciones.clear(); // limpieza explícita, evita fugas de memoria — ver anti-patterns/memory-leak.md
  });

  ws.on('error', (error) => {
    logger.error('Error en conexión WebSocket', { usuario: ws.usuario.id, error });
  });
});

function enviar(ws: WebSocket, mensaje: MensajeServidor): void {
  if (ws.readyState === WebSocket.OPEN) ws.send(JSON.stringify(mensaje)); // verificar estado antes de enviar
}
```

### 4. Verificar autorización por canal/recurso, no solo autenticación general

Cada suscripción a un canal debe verificar que el usuario autenticado tiene permiso sobre ese recurso específico (`tieneAccesoAlCanal`) — estar autenticado no implica tener acceso a cualquier dato (ver `CLAUDE.md` sección 12.1, Broken Access Control).

### 5. Publicar eventos de dominio hacia los clientes suscritos

```typescript
eventBus.subscribe('pedido.confirmado', (evento: PedidoConfirmadoEvent) => {
  const clientesSuscritos = obtenerClientesSuscritosA(`pedidos:${evento.pedidoId}`);
  clientesSuscritos.forEach(ws => enviar(ws, {
    tipo: 'pedido-actualizado',
    pedidoId: evento.pedidoId,
    estado: 'CONFIRMADO',
  }));
});
```

Conecta el mecanismo de eventos de dominio (ver [`../patterns/observer.md`](../patterns/observer.md)) con la notificación en tiempo real a clientes conectados.

### 6. Limpieza y límites: evitar fugas de memoria y saturación

```typescript
wss.on('connection', (ws) => {
  ws.isAlive = true;
  ws.on('pong', () => { ws.isAlive = true; });
});

setInterval(() => {
  wss.clients.forEach((ws: any) => {
    if (!ws.isAlive) return ws.terminate(); // conexión colgada, sin respuesta a pings previos
    ws.isAlive = false;
    ws.ping();
  });
}, 30000);
```

Un heartbeat periódico detecta y cierra conexiones "zombie" (el cliente se desconectó sin un cierre limpio TCP) que de otro modo acumularían recursos indefinidamente — ver [`../anti-patterns/memory-leak.md`](../anti-patterns/memory-leak.md).

## Checklist final

- [ ] ¿La autenticación se verifica en el handshake, no en el primer mensaje?
- [ ] ¿El protocolo de mensajes está tipado con discriminated unions?
- [ ] ¿Cada suscripción verifica autorización específica sobre el recurso, no solo autenticación general?
- [ ] ¿Existe un heartbeat que detecta y cierra conexiones colgadas?
- [ ] Ejecutar [`../checklists/architecture.md`](../checklists/architecture.md).

Ver también: [`../knowledge/node/error-handling.md`](../knowledge/node/error-handling.md), [`../patterns/observer.md`](../patterns/observer.md), [`../anti-patterns/memory-leak.md`](../anti-patterns/memory-leak.md).
