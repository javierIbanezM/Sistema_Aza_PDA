# EC2 (Elastic Compute Cloud)

> Ver [`../../decisions/ec2-vs-lambda.md`](../../decisions/ec2-vs-lambda.md) para cuándo elegir EC2 frente a cómputo serverless.

## Qué es y cuándo encaja

Máquinas virtuales con control total del sistema operativo — apropiado cuando se necesita un runtime/entorno específico no soportado por opciones serverless, procesos de larga duración con estado en memoria significativo, o cargas con patrón de uso constante donde el coste de instancias reservadas/Savings Plans es más eficiente que pagar por invocación.

## Tipos de instancia: elegir por perfil de carga

| Familia | Perfil | Uso típico |
|---|---|---|
| `t3`/`t4g` | Burstable, coste bajo | Cargas variables con picos ocasionales, entornos de desarrollo |
| `m5`/`m6i` | Balanceada CPU/memoria | Aplicaciones de propósito general |
| `c5`/`c6i` | Optimizada a CPU | Procesamiento intensivo, servidores de cálculo |
| `r5`/`r6i` | Optimizada a memoria | Bases de datos in-memory, caché grande |
| Instancias `g`/`p` | GPU | Machine learning, procesamiento gráfico |

## Modelos de precio

- **On-Demand**: sin compromiso, más caro por hora — apropiado para cargas impredecibles o de corta duración.
- **Reserved/Savings Plans**: descuento significativo (hasta ~70%) a cambio de compromiso de 1-3 años — apropiado para cargas base predecibles y constantes.
- **Spot**: descuento muy alto (hasta ~90%) sobre capacidad no utilizada, con riesgo de interrupción con 2 minutos de aviso — apropiado para cargas tolerantes a interrupción (batch, procesamiento distribuido con checkpointing).

## Auto Scaling Group: elasticidad basada en demanda real

```
Política de escalado: CPU > 70% durante 5 min → +1 instancia
                       CPU < 30% durante 10 min → -1 instancia
Mínimo: 2 (alta disponibilidad multi-AZ) | Máximo: 10 (control de coste)
```

Nunca desplegar una carga de producción crítica en una única instancia sin Auto Scaling Group — sin él, un fallo de la instancia (o del hardware subyacente) produce indisponibilidad total sin recuperación automática.

## Seguridad: Security Groups como firewall de instancia

```
Regla de entrada: puerto 443 desde 0.0.0.0/0 (tráfico HTTPS público)
Regla de entrada: puerto 22 SOLO desde la IP del bastión/VPN, nunca 0.0.0.0/0
```

Ver `CLAUDE.md` sección 12 — principio de menor privilegio aplicado a reglas de red: cada puerto abierto debe justificarse explícitamente, nunca abrir SSH/RDP a internet completo.

## Checklist

- [ ] ¿La instancia está en un Auto Scaling Group, no como instancia única sin recuperación automática?
- [ ] ¿El tipo de instancia corresponde al perfil de carga real (CPU/memoria/burstable), no elegido por defecto?
- [ ] ¿Se ha evaluado Reserved/Savings Plans para cargas base predecibles, en lugar de On-Demand indefinidamente?
- [ ] ¿Los Security Groups siguen el principio de menor privilegio, sin puertos administrativos abiertos a internet?

Ver también: [`lambda.md`](./lambda.md), [`../aws-network/vpc.md`](../aws-network/vpc.md), [`../../decisions/ec2-vs-lambda.md`](../../decisions/ec2-vs-lambda.md).
