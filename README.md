# 🛒 高并发秒杀系统 (分布式软件原理与技术项目)

## 📖 项目简介
本项目是一个基于微服务架构设计的高并发秒杀系统，为《分布式软件原理与技术》课程的个人核心项目。系统核心目标是应对瞬时高并发流量，保证数据一致性和系统稳定性。项目严格按照课程设计要求，拆分为**用户、商品、库存、订单**四个核心微服务，并整合了分布式缓存、消息队列、动静分离、容器化部署等技术来应对秒杀场景下的挑战。

---

## 🛠 技术栈概述
- **编程语言**：Java 21 (OpenJDK Temurin LTS)
- **开发工具**：Visual Studio Code + IntelliJ IDEA
- **核心框架**：Spring Boot 3.2.x, Spring Cloud
- **持久层框架**：MyBatis-Plus 3.5.5
- **数据库**：MySQL 8.0 (部署于阿里云云数据库 RDS)
- **缓存中间件**：Redis (Lettuce客户端)
- **对象存储**：阿里云 OSS (商品图片存储)
- **消息队列**：RabbitMQ (待集成)
- **容器化**：Docker + Docker Compose
- **负载均衡**：Nginx (动静分离 + 反向代理)
- **项目构建工具**：Apache Maven 3.9+
- **版本控制**：Git + GitHub

---

## 🏛 一、 系统架构设计

本项目采用经典的分层微服务架构，目前已成功实现容器化部署和动静分离。

### 1. 当前架构实现图
```
┌─────────────────────────────────────────────────────────────────┐
│                          客户端层                                │
│                     Web浏览器  / 移动端                          │
│                  访问域名：http://localhost                      │
└────────────────────────────┬────────────────────────────────────┘
                             │
┌────────────────────────────▼────────────────────────────────────┐
│                      接入层 (Nginx 网关)                         │
├────────────────────────────────────────────────────────────────┤
│                       【动静分离核心】                            │
│  ┌───────────────────────────────┐  ┌─────────────────────────┐ │
│  │   静态资源路由 (location /)    │  │   API动态路由 (location) │ │
│  │   • 服务HTML/CSS/JS/图片       │  │   • /api/users → 用户服务│ │
│  │   • 缓存策略: max-age=30d      │  │   • /api/products → 商品 │ │
│  │   • Vue应用history模式支持      │  │   • 负载均衡轮询分发      │ │
│  └───────────────┬───────────────┘  └───────────┬─────────────┘ │
└──────────────────┼────────────────────────────────┼──────────────┘
                   │                                │
┌──────────────────▼────────────────────────────────▼──────────────┐
│                   静态资源层 (Nginx本地)           动态代理层      │
│    挂载目录：/usr/share/nginx/html                (负载均衡)      │
│    ./nginx-html (本地挂载)                   ┌──────────┴─────┐  │
│         │                                    │                │  │
│         ▼                                    ▼                ▼  │
│  [index.html]                       ┌──────────────┐  ┌─────────┴┐
│  [assets/*.js]                       │ 用户服务集群  │  │商品服务   │
│  [assets/*.css]                       ├──────────────┤  │product-  │
│  [静态图片资源]                         │ user-service-1│  │service   │
│         │                              │  (8081→8080) │  │(8083→8081)│
│         │                              ├──────────────┤  └─────────┘
│         │                              │ user-service-2│    端口:8083
│         │                              │  (8082→8080) │
│         └──────────────────────────────┴──────────────┘
│                                                  负载均衡轮询
└────────────────────────────────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                     中间件层 (缓存加速)                           │
│  ┌─────────────────────┐          ┌───────────────────────────┐ │
│  │    Redis 缓存       │          │    阿里云 OSS 对象存储     │ │
│  │  • 商品数据预热      │          │  • 商品主图存储            │ │
│  │  • 会话状态管理      │          │  • 静态资源CDN加速         │ │
│  │  • 秒杀库存预扣减    │          │  • 防盗链/CORS配置         │ │
│  │  端口:6379          │          │  bucket: mall-seckill-oss │ │
│  └──────────┬──────────┘          └───────────────┬───────────┘ │
└─────────────┼────────────────────────────────────┼───────────────┘
              │                                    │
              ▼                                    ▼
┌─────────────────────────────────────────────────────────────────┐
│                      数据层 (持久化)                             │
│             阿里云RDS MySQL 8.0 (seckill_db)                    │
│  ┌──────────────┬──────────────┬──────────────┬──────────────┐  │
│  │  用户表      │   商品表      │  秒杀活动表  │   订单表     │  │
│  │  user       │  product      │seckill_product│ orders      │  │
│  └──────────────┴──────────────┴──────────────┴──────────────┘  │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                        基础设施层                                │
│  ┌────────────────────────┐    ┌─────────────────────────────┐  │
│  │   Docker容器化部署     │    │    版本控制与CI/CD           │  │
│  │  • 5个独立容器服务     │    │  • Git 版本管理             │  │
│  │  • 自定义网络隔离      │    │  • GitHub 远程仓库          │  │
│  │  • 数据卷持久化        │    │  • 多阶段Dockerfile构建     │  │
│  │  • 镜像备份与恢复      │    │  • 环境变量配置管理         │  │
│  └────────────────────────┘    └─────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

### 2. 核心请求链路说明
1. **客户端层：** 用户通过 Web浏览器访问 `http://localhost`
2. **接入层 (Nginx 网关)：**
   - 静态请求 (`/assets/*`)：直接返回 `/usr/share/nginx/html` 下的文件
   - 动态请求 (`/api/*`)：反向代理到后端服务集群
   - 负载均衡：`user-service` 双实例轮询分发
3. **微服务层：**
   - `User-Service` (双实例)：处理用户注册、登录鉴权、JWT Token 生成
   - `Product-Service`：处理商品列表、秒杀商品查询
4. **中间件层：**
   - **Redis 缓存**：商品数据预热、会话管理
   - **阿里云 OSS**：商品图片存储与分发
5. **数据层：** 阿里云 RDS MySQL 持久化存储

---

## 🔌 二、 各服务 API 接口定义 (RESTful 规范)

### 用户服务 (User Service) - 双实例负载均衡
| 方法 | 接口路径 | 功能描述 | 实际访问 |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/users/register` | 用户注册 | → Nginx → user-service-1/2 |
| `POST` | `/api/users/login` | 用户登录 | → Nginx → user-service-1/2 |
| `GET`  | `/api/users/{id}` | 获取用户信息 | → Nginx → user-service-1/2 |

### 商品服务 (Product Service)
| 方法 | 接口路径 | 功能描述 | 实际访问 |
| :--- | :--- | :--- | :--- |
| `GET`  | `/api/products/normal/list` | 普通商品列表 | → Nginx → product-service |
| `GET`  | `/api/products/seckill/list` | 秒杀商品列表 | → Nginx → product-service |
| `GET`  | `/api/products/{id}` | 商品详情 | → Nginx → product-service |

---

## 🗄️ 三、 数据库设计 (6张核心表)

本项目基于阿里云 RDS 建立 `seckill_db` 数据库，包含6张精心设计的核心表，完整支持秒杀业务场景。

### 1. 实体关系图 (ER 图)

```mermaid
erDiagram
    USER ||--o{ ORDERS : places
    PRODUCT ||--o{ SECKILL_PRODUCT : "participates in"
    PRODUCT ||--|| INVENTORY : has
    PRODUCT ||--o{ PRODUCT_IMAGE : contains
    SECKILL_PRODUCT ||--o{ ORDERS : generates

    USER {
        bigint id PK "雪花算法"
        varchar username UK
        varchar password "BCrypt加密"
        varchar phone UK
        datetime created_at
    }

    PRODUCT {
        bigint id PK "雪花算法"
        varchar name
        decimal price
        varchar main_image "OSS URL"
        varchar description
        tinyint status
        datetime created_time
        datetime updated_time
    }

    PRODUCT_IMAGE {
        bigint id PK
        bigint product_id FK
        varchar image_url "OSS URL"
        int sort_order
        tinyint type "1-轮播,2-详情"
    }

    INVENTORY {
        bigint id PK
        bigint product_id FK UK
        int total_stock
        int available_stock
        int locked_stock
        int sold_stock
        int alert_threshold
        tinyint is_alerted
        datetime last_alert_time
        int version "乐观锁"
    }

    SECKILL_PRODUCT {
        bigint id PK "雪花算法"
        bigint product_id FK
        varchar main_image "秒杀专属图"
        decimal seckill_price
        int seckill_stock "活动总库存"
        int available_stock "剩余库存"
        int limit_per_user "限购数量"
        datetime start_time
        datetime end_time
        tinyint status "0-未开始,1-进行中,2-已结束"
        int version "乐观锁"
    }

    ORDERS {
        bigint id PK "雪花算法"
        varchar order_no UK "业务订单号"
        bigint user_id FK
        bigint product_id FK
        bigint seckill_id FK
        tinyint order_type "0-普通,1-秒杀"
        decimal pay_amount "快照金额"
        int quantity
        tinyint status "0-待支付,1-已支付,2-已取消"
        tinyint payment_method
        varchar transaction_id "支付流水号"
        datetime create_time
        datetime pay_time
        datetime expire_time "超时时间"
    }
```

### 2. 核心表设计亮点

| 表名 | 核心字段 | 设计亮点 |
|------|----------|----------|
| **user** | `id`, `username`, `phone` | 雪花算法ID，手机号唯一约束，BCrypt加密 |
| **product** | `id`, `name`, `main_image` | 与秒杀活动解耦，冗余主图提升性能 |
| **product_image** | `product_id`, `image_url` | 多图存储，复合索引优化查询 |
| **inventory** | `product_id`, `available_stock` | 库存预警机制，乐观锁防并发 |
| **seckill_product** | `product_id`, `seckill_price` | 独立库存，时间窗口控制，复合索引 |
| **orders** | `order_no`, `user_id`, `seckill_id` | 唯一约束防重复秒杀，多维度索引 |

---

## 🚀 四、 已完成工作 (截止2026-03-15)

### ✅ 第一阶段：容器化部署与负载均衡
- [x] Dockerfile 编写（user-service / product-service）
- [x] docker-compose.yml 多服务编排（5个容器）
- [x] Nginx 负载均衡配置（user-service 双实例轮询）
- [x] Redis 缓存集成
- [x] Docker 镜像备份与恢复脚本
- [x] 容器管理脚本（start/stop/status）

### ✅ 第二阶段：动静分离与前端集成
- [x] Vue 项目打包（npm run build）
- [x] Nginx 动静分离配置（location / 静态资源）
- [x] MIME 类型优化（解决 JS 加载问题）
- [x] 阿里云 OSS 图片存储集成
- [x] 防盗链与 CORS 配置
- [x] 前端页面完整展示（登录/商品列表/秒杀列表）

### ✅ 核心业务功能
- [x] 用户注册（BCrypt加密）
- [x] 用户登录（JWT token）
- [x] 普通商品列表接口
- [x] 秒杀商品列表接口
- [x] 商品图片 OSS 存储
- [x] 秒杀状态定时任务框架

---

## 🔜 五、 后续开发计划

### 第三阶段：Redis 缓存优化
- [ ] 商品详情缓存实现
- [ ] 缓存穿透防护（布隆过滤器）
- [ ] 缓存击穿防护（分布式锁）
- [ ] 缓存雪崩防护（随机过期时间）
- [ ] 缓存预热机制

### 第四阶段：秒杀核心业务
- [ ] 库存服务实现
- [ ] 订单服务实现
- [ ] Redis 预扣库存（Lua脚本）
- [ ] RabbitMQ 异步订单处理
- [ ] 分布式事务最终一致性
- [ ] 接口限流（令牌桶算法）
- [ ] JMeter 高并发压测

---

## 📊 六、 性能优化策略

| 优化维度 | 当前实现 | 待优化目标 |
|:---|:---|:---|
| **静态资源** | Nginx 直接服务，30天缓存 | CDN 加速 |
| **图片存储** | 阿里云 OSS，公共读 | 签名URL私有访问 |
| **负载均衡** | Nginx 轮询分发 | 加权轮询 + 健康检查 |
| **数据库** | 索引优化 | 读写分离 |
| **缓存** | Redis 基础集成 | 三级缓存（本地+Redis） |
| **并发控制** | 乐观锁 | Lua 脚本原子操作 |

---

## 🔧 七、 本地开发环境配置

### 前置要求
- JDK 21 (OpenJDK Temurin)
- Maven 3.9+
- Docker Desktop
- Git

### 快速启动（容器化部署）

```bash
# 1. 克隆项目
git clone https://github.com/ayako17/mall-seckill.git
cd mall-seckill

# 2. 配置环境变量
# 编辑 .env 文件，填入数据库密码等敏感信息

# 3. 启动所有服务
./start-containers.ps1  # Windows PowerShell
# 或
docker-compose up -d

# 4. 访问应用
# 前端页面：http://localhost
# API测试：http://localhost/api/products/normal/list
```

### 配置文件说明
| 文件 | 用途 | 注意事项 |
|------|------|----------|
| `.env` | 环境变量 | **不提交 Git**，包含数据库密码 |
| `docker-compose.yml` | 容器编排 | 定义5个服务 |
| `nginx.conf` | 动静分离配置 | 已优化 MIME 类型 |
| `Dockerfile` | 镜像构建 | user-service/product-service |

---

## 📝 八、 项目总结与亮点

### 🏆 已完成核心亮点
1. **微服务架构**：用户、商品服务独立拆分，职责单一
2. **容器化部署**：Docker 容器化 + Docker Compose 编排
3. **负载均衡**：Nginx 双实例轮询，高可用保障
4. **动静分离**：静态资源 Nginx 直接服务，性能提升
5. **对象存储**：阿里云 OSS 图片存储，CDN 加速
6. **数据库设计**：6张核心表，完整支持秒杀业务
7. **安全实践**：环境变量管理敏感信息，密钥不提交 Git

### 🎯 技术前瞻
- **Java 21 虚拟线程**：已在服务日志中验证，为高并发提供语言级支持
- **Redis 缓存**：已集成，待实现三级缓存策略
- **消息队列**：RabbitMQ 待集成，实现异步削峰

---

## 📧 九、 联系方式

- **GitHub**: [@ayako17](https://github.com/ayako17)
- **项目地址**: [https://github.com/ayako17/mall-seckill](https://github.com/ayako17/mall-seckill)

---

**最后更新时间：2026年3月15日 (第二阶段完成)**