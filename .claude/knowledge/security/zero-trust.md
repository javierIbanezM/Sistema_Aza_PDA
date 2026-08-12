# Zero Trust

> Ver [`defense-in-depth.md`](./defense-in-depth.md) para el principio general del que Zero Trust es una aplicación estricta, y [`../aws-network/vpc.md`](../aws-network/vpc.md) para los controles de red (NetworkPolicy, Security Groups) que implementan la microsegmentación descrita aquí.

## El cambio de modelo: de "confiar en la red interna" a "nunca confiar, siempre verificar"

El modelo perimetral clásico asume que todo lo que está "dentro" de la red corporativa/VPC es confiable, y el esfuerzo de seguridad se concentra en el perímetro — Zero Trust asume que **cualquier** componente, interno o externo, puede estar comprometido, y por tanto cada petición se autentica y autoriza independientemente de su origen, incluida una petición de un servicio interno a otro.

## Por qué el modelo perimetral ya no es suficiente

- Arquitecturas de microservicios modernas tienen tanto tráfico interno (servicio a servicio) como externo — un atacante que compromete un único servicio interno, en un modelo perimetral, tiene acceso implícito a todo lo demás "dentro" del perímetro.
- Empleados/servicios acceden desde ubicaciones y dispositivos variables, sin un perímetro de red físico claro que defender.
- Cadenas de suministro de software (ver [`supply-chain-security.md`](./supply-chain-security.md)) introducen riesgo desde dentro del propio código, no solo desde fuera de la red.

## Microsegmentación: el perímetro se mueve a cada componente

```yaml
# NetworkPolicy: pedidos-service SOLO acepta tráfico de api-gateway, ni siquiera de otros servicios internos
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
spec:
  podSelector: { matchLabels: { app: pedidos-service } }
  ingress: [{ from: [{ podSelector: { matchLabels: { app: api-gateway } } }] }]
```

Ver [`../kubernetes/kubernetes.md`](../kubernetes/kubernetes.md) — en lugar de un único perímetro grande (la VPC completa), cada servicio define explícitamente de quién acepta tráfico, reduciendo el radio de impacto de cualquier servicio comprometido a exactamente lo que ese servicio puede alcanzar, no a toda la red interna.

## Identidad fuerte en cada llamada, incluida servicio-a-servicio

```
Cada llamada interna lleva su propia identidad verificable (mTLS, JWT de servicio) —
NUNCA "confiamos en esta llamada porque viene de la red interna"
```

Ver [`../aws-security/iam.md`](../aws-security/iam.md) (IRSA, roles por servicio) y [`../../decisions/jwt-vs-session.md`](../../decisions/jwt-vs-session.md) — el mismo rigor de verificación de identidad aplicado a usuarios externos se aplica a llamadas entre servicios internos.

## Verificación continua, no solo en el login

```
Autenticación inicial: verifica identidad en el login
Zero Trust: cada petición posterior también verifica que el token/sesión sigue siendo válido,
            no asume que "ya se autenticó una vez, confío en el resto de la sesión sin más verificación"
```

## Cuándo Zero Trust completo está justificado

Implementar Zero Trust de forma exhaustiva (mTLS en cada conexión interna, verificación continua, microsegmentación granular) tiene coste operativo real — apropiado para sistemas con datos de alta sensibilidad o superficie de ataque significativa; para sistemas internos pequeños de bajo riesgo, aplicar los principios (nunca confiar implícitamente, verificar explícitamente) de forma proporcional, sin la infraestructura completa si el riesgo no lo justifica (ver [`../architecture/dry-kiss-yagni.md`](../architecture/dry-kiss-yagni.md)).

## Checklist

- [ ] ¿El tráfico interno servicio-a-servicio se autentica y autoriza explícitamente, sin confiar implícitamente en el origen "interno"?
- [ ] ¿Existe microsegmentación (NetworkPolicy/Security Groups) limitando qué puede hablar con qué, más allá de un único perímetro?
- [ ] ¿El nivel de rigor de Zero Trust aplicado es proporcional al riesgo real del sistema?

Ver también: [`defense-in-depth.md`](./defense-in-depth.md), [`../aws-network/vpc.md`](../aws-network/vpc.md), [`authentication-vs-authorization.md`](./authentication-vs-authorization.md).
