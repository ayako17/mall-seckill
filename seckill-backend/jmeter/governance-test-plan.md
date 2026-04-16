# JMeter Governance Test Plan

## 1) Gateway rate limit test
- Thread Group: 100 users, ramp-up 2s, loop 20
- HTTP Request: `GET http://localhost:8088/api/orders/result/mock-order-no`
- Header Manager: `X-Forwarded-For=${__threadNum}.10.0.1`
- Assertions:
  - 200 for first part of requests
  - 429 appears after threshold (>10 req/s per IP)

## 2) Order circuit breaker test
- Prepare a fault switch in order-service (e.g. set invalid seckillId repeatedly)
- Thread Group: 50 users, ramp-up 1s, loop 30
- HTTP Request: `POST http://localhost:8088/api/orders/seckill`
- Body: invalid payload to increase exception ratio
- Observe:
  - first period: business errors
  - after threshold (>50%), fast-fail responses from Sentinel fallback for about 10s

## 3) Inventory degrade test
- Simulate DB/Redis fault (stop redis container or break DB connection)
- HTTP Request: `GET http://localhost:8088/api/inventory/check/1001`
- Assertions:
  - JSON contains `"degraded": true`
  - service still returns 200 with friendly message

## Metrics analysis
- Response Time: check p95 and p99 before/after governance rules
- Throughput: compare TPS with and without rate limit/circuit-breaker
- Error Codes: count 429/5xx ratio, verify most failures are controlled failures
- Stability: check whether upstream services avoid cascading crash under pressure
