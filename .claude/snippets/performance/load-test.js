// snippets/performance/load-test.js
// Script k6 con rampa, estado estable y thresholds = performance budget — ver recipes/performance-run-load-test.md.

import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  scenarios: {
    load_test: {
      executor: 'ramping-vus',
      stages: [
        { duration: '2m', target: 500 },  // rampa hasta la carga objetivo
        { duration: '5m', target: 500 },  // estado estable — el warm-up (rampa) se excluye del análisis
        { duration: '2m', target: 0 },
      ],
    },
  },
  thresholds: {
    http_req_duration: ['p(95)<300', 'p(99)<800'], // performance budget, ver knowledge/performance/performance-budget.md
    http_req_failed: ['rate<0.001'],
  },
};

export default function () {
  const res = http.get(`${__ENV.BASE_URL}/api/pedidos`, {
    headers: { Authorization: `Bearer ${__ENV.TOKEN}` },
  });

  check(res, {
    'status es 200': (r) => r.status === 200,
    'latencia bajo 1s': (r) => r.timings.duration < 1000,
  });

  sleep(1);
}

// Uso:
// k6 run -e BASE_URL=https://staging.empresa.com -e TOKEN=$TOKEN --summary-export=resultado.json load-test.js
