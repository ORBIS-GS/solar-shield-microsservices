/**
 * Solar Shield — Smoke Test k6
 *
 * Executa: k6 run k6/smoke-test.js
 * Requisitos: k6 instalado (https://k6.io/docs/getting-started/installation/)
 *             docker-compose up rodando na porta 8080
 *
 * Cenário: 10 usuários virtuais durante 10 segundos
 *   - 70% das requisições: GET /api/alerts       (leitura com cache Redis)
 *   - 30% das requisições: GET /health           (health check do gateway)
 *
 * Critérios de sucesso (thresholds):
 *   - 95% das requisições respondem em menos de 500ms
 *   - Taxa de erros HTTP < 5%
 *   - Todos os status 200 ou 429 (rate limit é comportamento esperado)
 */

import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter, Rate, Trend } from 'k6/metrics';

// ── Métricas customizadas ────────────────────────────────────────────────────
const rateLimitedRequests = new Counter('rate_limited_requests');
const alertsResponseTime  = new Trend('alerts_response_time_ms');
const errorRate           = new Rate('error_rate');

// ── Configuração do teste ────────────────────────────────────────────────────
export const options = {
  vus:      10,   // 10 usuários virtuais simultâneos
  duration: '10s', // duração total do teste

  thresholds: {
    // 95% das requisições devem responder em menos de 500ms
    http_req_duration: ['p(95)<500'],
    // Taxa de erros deve ser menor que 5%
    error_rate: ['rate<0.05'],
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

// ── Cenário principal ────────────────────────────────────────────────────────
export default function () {
  const rand = Math.random();

  if (rand < 0.70) {
    // 70% → GET /api/alerts (valida cache Redis + rate limiting)
    const res = http.get(`${BASE_URL}/api/alerts`, {
      tags: { endpoint: 'alerts' },
    });

    alertsResponseTime.add(res.timings.duration);

    const ok = check(res, {
      'GET /api/alerts — status 200 ou 429': (r) =>
        r.status === 200 || r.status === 429,
      'GET /api/alerts — resposta em menos de 500ms': (r) =>
        r.timings.duration < 500,
    });

    if (res.status === 429) {
      rateLimitedRequests.add(1);
    }

    if (!ok) {
      errorRate.add(1);
    } else {
      errorRate.add(0);
    }

  } else {
    // 30% → GET /health (valida gateway Nginx)
    const res = http.get(`${BASE_URL}/health`, {
      tags: { endpoint: 'health' },
    });

    const ok = check(res, {
      'GET /health — status 200': (r) => r.status === 200,
      'GET /health — corpo contém status UP': (r) =>
        r.body.includes('UP'),
    });

    if (!ok) errorRate.add(1);
    else      errorRate.add(0);
  }

  // Pequena pausa para simular comportamento real de usuário
  sleep(0.1);
}

// ── Sumário final ────────────────────────────────────────────────────────────
export function handleSummary(data) {
  const summary = {
    timestamp:         new Date().toISOString(),
    vus:               10,
    duration:          '10s',
    total_requests:    data.metrics.http_reqs?.values?.count    ?? 0,
    failed_requests:   data.metrics.http_req_failed?.values?.count ?? 0,
    p95_duration_ms:   data.metrics.http_req_duration?.values?.['p(95)'] ?? 0,
    avg_duration_ms:   data.metrics.http_req_duration?.values?.avg ?? 0,
    rate_limited:      data.metrics.rate_limited_requests?.values?.count ?? 0,
    thresholds_passed: Object.values(data.thresholds ?? {}).every(t => !t.ok === false),
  };

  console.log('\n========== SOLAR SHIELD — SMOKE TEST RESULT ==========');
  console.log(`Timestamp:        ${summary.timestamp}`);
  console.log(`VUs / Duration:   ${summary.vus} VUs / ${summary.duration}`);
  console.log(`Total requests:   ${summary.total_requests}`);
  console.log(`Failed requests:  ${summary.failed_requests}`);
  console.log(`P95 duration:     ${summary.p95_duration_ms.toFixed(2)} ms`);
  console.log(`Avg duration:     ${summary.avg_duration_ms.toFixed(2)} ms`);
  console.log(`Rate limited:     ${summary.rate_limited} req (HTTP 429)`);
  console.log('======================================================\n');

  // Salva em /scripts/results.json (caminho dentro do container Docker)
  // que é mapeado para k6/results.json no host via volume -v
  return {
    '/scripts/results.json': JSON.stringify(summary, null, 2),
    stdout: '',
  };
}