# Mall Seckill Backend

## 项目简介
这是一个面向高并发秒杀场景的微服务后端，已实现：
- 用户注册/登录（JWT）
- 商品详情缓存（Redis，含防穿透/击穿/雪崩）
- 秒杀下单（Redis Lua 预扣减 + Kafka 异步订单）
- 库存预留与订单最终一致性（Kafka 补偿链路）
- TCC 分布式事务示例（订单 + 库存）
- 支付回调 + 本地消息表 + 库存确认
- Nacos 注册/配置中心、Gateway 路由、Sentinel 流量治理
- Prometheus 监控与 OpenTelemetry 链路追踪接入

## 技术栈
- Java 21
- Spring Boot 3.2.4
- Spring Cloud 2023.0.1
- Spring Cloud Alibaba 2023.0.1.0
- MyBatis-Plus 3.5.5
- ShardingSphere JDBC Starter 5.4.1
- MySQL 8.0 / Redis 7 / Kafka 3.7.1 / Elasticsearch
- Nacos 2.2.3 / Sentinel 1.8.6 / Spring Cloud Gateway
- Micrometer + Prometheus
- OpenTelemetry + Jaeger

## 目录结构
- `seckill-backend/user-service`
- `seckill-backend/product-service`
- `seckill-backend/order-service`
- `seckill-backend/inventory-service`
- `seckill-backend/gateway-service`
- `seckill-backend/nacos-config`（Nacos Data ID 配置文件）
- `seckill-backend/jmeter`（压测脚本）
- `observability`（Prometheus / OTel Collector 配置）

## 快速启动

### 1) 构建服务
```bash
cd seckill-backend
mvn clean package -DskipTests
```

### 2) 准备 OpenTelemetry Java Agent
将 `opentelemetry-javaagent.jar` 放到：
- `./observability/opentelemetry-javaagent.jar`

可选下载示例（Windows PowerShell）：
```powershell
Invoke-WebRequest -Uri https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar -OutFile .\observability\opentelemetry-javaagent.jar
```

### 3) 启动基础环境与服务
```bash
cd ..
docker-compose up -d --build
```

### 4) 关键访问地址
- Gateway: `http://localhost:8088`
- Nacos: `http://localhost:8848/nacos`
- Sentinel Dashboard: `http://localhost:8858`
- Prometheus: `http://localhost:9090`
- Jaeger: `http://localhost:16686`

## 网关路由规则
- `/api/users/**` -> `user-service`
- `/api/products/**` -> `product-service`
- `/api/orders/**` -> `order-service`
- `/api/inventory/**` -> `inventory-service`

## 核心接口示例

### 1) 秒杀下单
```bash
curl -X POST 'http://localhost:8088/api/orders/seckill' \
  -H 'Authorization: Bearer <JWT_TOKEN>' \
  -H 'Content-Type: application/json' \
  -d '{"seckillId":1001,"quantity":1}'
```

### 2) 查询下单结果
```bash
curl 'http://localhost:8088/api/orders/result/MS202604160001'
```

### 3) 支付回调
```bash
curl -X POST 'http://localhost:8088/api/orders/pay/callback' \
  -H 'Content-Type: application/json' \
  -d '{"orderNo":"MS202604160001","paymentMethod":1,"transactionId":"WX-20260416-0001"}'
```

## Nacos 配置说明

### Data ID 命名规范
- 共享配置：`common-infra.yaml`
- 服务配置：`<service-name>-dev.yaml`
  - `user-service-dev.yaml`
  - `product-service-dev.yaml`
  - `order-service-dev.yaml`
  - `inventory-service-dev.yaml`
  - `gateway-service-dev.yaml`

### Sentinel 规则 Data ID
- 网关限流：`gateway-service-gw-flow-rules.json`
- 订单熔断：`order-service-degrade-rules.json`
- 库存降级：`inventory-service-degrade-rules.json`

配置文件位置：`seckill-backend/nacos-config/`

## 监控与追踪

### Prometheus 抓取目标
配置文件：`observability/prometheus.yml`
抓取路径：`/actuator/prometheus`

### OpenTelemetry
- Java Agent 通过 `JAVA_TOOL_OPTIONS=-javaagent:/otel/opentelemetry-javaagent.jar` 注入
- OTel Collector 配置：`observability/otel-collector-config.yaml`
- Trace 最终导入 Jaeger

## 压测

### JMeter GUI
```bash
jmeter
```
打开：`seckill-backend/jmeter/governance-test-plan.jmx`

### 命令行执行
```bash
jmeter -n -t seckill-backend/jmeter/governance-test-plan.jmx -l seckill-backend/jmeter/result.jtl
```

## 常见排查
- 网关 404：检查路径是否使用复数前缀（`/api/users|products|orders|inventory`）
- Nacos 配置不生效：确认 Data ID、Group、Namespace 与 `bootstrap.yml` 一致
- OTel 未上报：确认 `observability/opentelemetry-javaagent.jar` 文件存在
- Prometheus 无数据：检查服务 `/actuator/prometheus` 是否可访问
