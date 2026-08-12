# Recipe: Ejecutar un Ciclo de Pruebas de Carga

Aplica: [`../knowledge/performance/load-testing-types.md`](../knowledge/performance/load-testing-types.md), [`../knowledge/performance/benchmarking.md`](../knowledge/performance/benchmarking.md), [`../knowledge/performance/performance-budget.md`](../knowledge/performance/performance-budget.md).

## Pasos

### 1. Definir el performance budget objetivo, antes de escribir ningún script

```
p95 < 300ms, p99 < 800ms, con 500 usuarios concurrentes, tasa de error < 0.1%
```

Ver [`../knowledge/performance/performance-budget.md`](../knowledge/performance/performance-budget.md) — sin este objetivo explícito, no hay criterio para interpretar el resultado de la prueba.

### 2. Elegir el tipo de prueba según la pregunta a responder

Ver [`../knowledge/performance/load-testing-types.md`](../knowledge/performance/load-testing-types.md) — Load (¿soporta la carga esperada?), Stress (¿dónde está el límite?), Spike (¿reacciona a tiempo a un pico?), Soak (¿es estable durante horas?).

### 3. Script de Load Test con k6

```javascript
import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '2m', target: 500 },  // rampa hasta la carga objetivo
    { duration: '5m', target: 500 },  // sostenida — estado estable, excluye warm-up del análisis
    { duration: '2m', target: 0 },    // rampa de bajada
  ],
  thresholds: {
    http_req_duration: ['p(95)<300', 'p(99)<800'], // el propio performance budget como criterio de éxito
    http_req_failed: ['rate<0.001'],
  },
};

export default function () {
  const res = http.get('https://staging.empresa.com/api/pedidos');
  check(res, { 'status 200': (r) => r.status === 200 });
  sleep(1);
}
```

### 4. Ejecutar contra un entorno representativo (nunca contra producción sin coordinación)

```bash
k6 run --summary-export=resultado.json load-test.js
```

Ejecutar contra staging con paridad real respecto a producción (ver [`../knowledge/devops/environments.md`](../knowledge/devops/environments.md)) — un resultado contra un entorno infra-dimensionado no es representativo.

### 5. Analizar contra el performance budget, no solo "si pasó"

```bash
jq '.metrics.http_req_duration' resultado.json
# revisar p95/p99 explícitamente, no solo el exit code del threshold
```

### 6. Si no cumple el budget, aplicar el método completo de diagnóstico

Ver [`../knowledge/performance/performance-engineering.md`](../knowledge/performance/performance-engineering.md) — Medir → Localizar cuello de botella → Hipótesis → Experimento, no ajustar parámetros al azar hasta que el número mejore.

### 7. Repetir Stress/Spike/Soak según lo que aún no se ha verificado

```
Si solo se ejecutó Load Test: ejecutar también Stress Test para conocer el límite real
Si el sistema tendrá carga sostenida prolongada: ejecutar Soak Test antes de producción
```

## Checklist final

- [ ] ¿El performance budget está definido antes de ejecutar cualquier prueba?
- [ ] ¿El tipo de prueba corresponde a la pregunta específica que se necesita responder?
- [ ] ¿Se ejecutó contra un entorno con paridad real respecto a producción?
- [ ] ¿Un resultado que no cumple el budget se diagnosticó con el método completo, no ajustado a ciegas?

Ver también: [`../snippets/performance/load-test.js`](../snippets/performance/load-test.js), [`../knowledge/performance/performance-engineering.md`](../knowledge/performance/performance-engineering.md).
