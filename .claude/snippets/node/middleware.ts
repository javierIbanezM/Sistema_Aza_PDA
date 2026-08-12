// snippets/node/middleware.ts
// Middleware de correlación + manejo global de errores — ver recipes/node-create-middleware.md.

import { Request, Response, NextFunction } from 'express';

export function correlationIdMiddleware(req: Request, res: Response, next: NextFunction): void {
  const traceId = (req.headers['x-correlation-id'] as string) || crypto.randomUUID();
  (req as any).traceId = traceId;
  res.setHeader('X-Correlation-Id', traceId);
  next();
}

export function asyncHandler(fn: (req: Request, res: Response, next: NextFunction) => Promise<void>) {
  return (req: Request, res: Response, next: NextFunction): void => {
    fn(req, res, next).catch(next);
  };
}

export function errorHandlerMiddleware(err: Error, req: Request, res: Response, next: NextFunction): void {
  if (err instanceof DomainException) {
    const status = mapearCodigoHttp(err);
    res.status(status).json({ error: { code: err.code, message: err.message, details: [] } });
    return;
  }

  logger.error('Error no controlado', err);
  res.status(500).json({ error: { code: 'INTERNAL_ERROR', message: 'Error inesperado', details: [] } });
}

function mapearCodigoHttp(err: DomainException): number {
  if (err instanceof PedidoNoEncontradoException) return 404;
  if (err instanceof PedidoYaConfirmadoException) return 409;
  return 500;
}
