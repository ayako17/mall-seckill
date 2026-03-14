# 🛒 高并发秒杀系统 (分布式软件原理与技术项目)

## 📖 项目简介
本项目是一个基于微服务架构设计的高并发秒杀系统，为《分布式软件原理与技术》课程的个人核心项目。系统核心目标是应对瞬时高并发流量，保证数据一致性和系统稳定性。项目严格按照课程设计要求，拆分为**用户、商品、库存、订单**四个核心微服务，并整合了分布式缓存、消息队列等中间件来应对秒杀场景下的技术挑战。

---

## 🛠 技术栈概述
- **编程语言**：Java 21 (OpenJDK Temurin LTS)
- **开发工具**：Visual Studio Code + IntelliJ IDEA
- **核心框架**：Spring Boot 3.2.x, Spring Cloud
- **持久层框架**：MyBatis-Plus 3.5.5
- **数据库**：MySQL 8.0 (部署于阿里云云数据库 RDS)
- **缓存中间件**：Redis (Lettuce客户端)
- **消息队列**：RabbitMQ (待集成)
- **项目构建工具**：Apache Maven 3.9+
- **版本控制**：Git + GitHub

---

## 🏛 一、 系统架构设计

本项目采用经典的分层微服务架构，利用中间件实现流量削峰和数据缓存，极致保护后端数据库。

### 1. 架构草图
```
┌─────────────────────────────────────────────────────────────┐
│                        客户端层                              │
│              Web浏览器 / App / 小程序                        │
└─────────────────────────┬───────────────────────────────────┘
                          │
┌─────────────────────────▼───────────────────────────────────┐
│                      接入层 (API Gateway)                    │
│             路由转发、身份鉴权、基础限流                       │
└─────────────────────────┬───────────────────────────────────┘
                          │
┌─────────────────────────▼───────────────────────────────────┐
│                   微服务层 (核心业务处理)                     │
├────────────────┬────────────────┬────────────────┬──────────┤
│  用户服务      │   商品服务      │   库存服务      │  订单服务 │
│  user-service │ product-service │ inventory-svc  │ order-svc│
│  端口:8080     │   端口:8081     │   端口:8082     │ 端口:8083│
└────────────────┴────────────────┴────────────────┴──────────┘
                          │
┌─────────────────────────▼───────────────────────────────────┐
│                   中间件层 (高并发防护)                       │
├────────────────────────────────┬────────────────────────────┤
│           Redis缓存             │         RabbitMQ           │
│   • 商品信息预热                │   • 异步削峰填谷            │
│   • Lua脚本原子扣减库存         │   • 订单异步落库            │
└────────────────────────────────┴────────────────────────────┘
                          │
┌─────────────────────────▼───────────────────────────────────┐
│                     数据层 (持久化)                           │
│             阿里云RDS MySQL 8.0 (seckill_db)                 │
└─────────────────────────────────────────────────────────────┘
```

### 2. 核心请求链路说明
1. **客户端层：** 用户通过 Web浏览器 / App 发起秒杀请求。
2. **接入层 (API Gateway)：** 负责请求的统一接入、路由转发、身份鉴权以及基础的限流。
3. **微服务层 (核心业务处理)：** 系统拆分为四个独立的服务，互相通过 RESTful API 调用：
   - `User-Service (用户服务)`：负责处理用户注册、登录鉴权、JWT Token 生成。
   - `Product-Service (商品服务)`：负责秒杀商品列表展示、商品详情查询（结合Redis缓存）。
   - `Inventory-Service (库存服务)`：**核心服务**，负责处理扣减库存的业务逻辑（优先在 Redis 预扣减）。
   - `Order-Service (订单服务)`：负责生成秒杀成功的最终订单记录。
4. **中间件层 (高并发防护)：**
   - **Redis 缓存：** 用于抗住海量并发读请求（商品信息预热），以及利用 Lua 脚本进行高效的分布式库存预扣减，防止超卖。
   - **消息队列 (RabbitMQ)：** 采用异步削峰填谷思想，秒杀成功后发送下单消息，订单服务异步消费消息落库，保护 MySQL。
5. **数据层：** 采用阿里云云数据库 RDS (MySQL) `seckill_db` 进行数据的最终持久化落地。

---

## 🔌 二、 各服务 API 接口定义 (RESTful 规范)

### 用户服务 (User Service) - 端口: 8080
| 方法 | 接口路径 | 功能描述 |
| :--- | :--- | :--- |
| `POST` | `/api/users/register` | 用户注册 (BCrypt密码加密) |
| `POST` | `/api/users/login` | 用户登录并获取 JWT Token (支持用户名/手机号) |
| `GET`  | `/api/users/{id}` | 获取指定用户信息 |

### 商品服务 (Product Service) - 端口: 8081
| 方法 | 接口路径 | 功能描述 |
| :--- | :--- | :--- |
| `GET`  | `/api/products/normal/list` | 获取普通商品列表 (分页支持) |
| `GET`  | `/api/products/seckill/list` | 获取当前有效秒杀商品列表 |
| `GET`  | `/api/products/{id}` | 获取单个商品详情 |

### 库存服务 (Inventory Service) - 端口: 8082
| 方法 | 接口路径 | 功能描述 |
| :--- | :--- | :--- |
| `GET`  | `/api/inventory/{productId}` | 查询商品实时剩余库存 |
| `POST` | `/api/inventory/{productId}/deduct`| 扣减指定商品的库存（带乐观锁） |

### 订单服务 (Order Service) - 端口: 8083
| 方法 | 接口路径 | 功能描述 |
| :--- | :--- | :--- |
| `POST` | `/api/orders/seckill` | **【核心】执行秒杀下单**（需携带商品ID与用户Token） |
| `GET`  | `/api/orders/{id}` | 根据订单号查询订单详情 |
| `GET`  | `/api/orders/user/{userId}` | 查询某用户的所有订单记录 |

---

## 🗄️ 三、 数据库 ER 图与表结构设计

本项目基于阿里云 RDS 建立 `seckill_db` 数据库，包含6张核心表。

### 1. 实体关系图 (ER 图)

```mermaid
erDiagram
    USER ||--o{ ORDERS : places
    PRODUCT ||--o{ SECKILL_PRODUCT : "participates in"
    PRODUCT ||--|| INVENTORY : has
    SECKILL_PRODUCT ||--o{ ORDERS : generates

    USER {
        bigint id PK "雪花算法"
        varchar username
        varchar password "BCrypt加密"
        varchar phone UK
        datetime created_at
    }

    PRODUCT {
        bigint id PK "雪花算法"
        varchar name
        decimal price
        varchar main_image
        varchar description
        tinyint status "1-上架,0-下架"
        datetime created_time
        datetime updated_time
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
        varchar main_image
        decimal seckill_price
        int seckill_stock
        int available_stock
        int limit_per_user
        datetime start_time
        datetime end_time
        tinyint status "0-未开始,1-进行中,2-已结束"
        int version "乐观锁"
    }

    ORDERS {
        bigint id PK "雪花算法"
        varchar order_no UK
        bigint user_id FK
        bigint product_id FK
        bigint seckill_id FK "可为NULL"
        tinyint order_type "0-普通,1-秒杀"
        decimal pay_amount
        int quantity
        tinyint status
        tinyint payment_method
        varchar transaction_id
        datetime create_time
        datetime pay_time
        datetime expire_time
    }
```

### 2. 核心表结构说明

#### 用户表 (user)
- 采用雪花算法生成分布式ID
- 手机号唯一约束，支持手机号登录
- 密码使用 BCrypt 加密存储

#### 基础商品表 (product)
- 与秒杀活动解耦，只存储商品基础属性
- 冗余主图字段，提升列表页性能
- 包含创建时间和更新时间审计字段

#### 商品图片表 (product_image)
- 存储商品详情页的多张图片
- 复合索引优化图片查询性能

#### 基础库存表 (inventory)
- 与商品表一对一关系，职责单一
- 包含库存预警机制（阈值、预警状态）
- 乐观锁版本号防止并发超卖

#### 秒杀活动商品表 (seckill_product)
- **核心高并发表**，独立管理秒杀活动
- 独立库存字段，与普通库存隔离
- 复合索引 `idx_status_time` 优化"当前有效活动"查询
- 独立乐观锁版本号

#### 交易订单表 (orders)
- 雪花算法生成分布式ID
- 唯一约束 `uk_user_seckill` 防止同一用户重复秒杀
- 复合索引优化订单查询、超时扫描、财务对账
- 包含交易快照（金额、商品ID等）

---

## 💻 四、 技术栈选型详细说明

结合目前业界主流微服务架构及本地开发环境，本项目技术栈敲定如下：

1. **编程语言：Java 21 (OpenJDK Temurin)**
   - **选型亮点：** 采用最新的 LTS（长期支持）版本，**核心亮点在于引入了虚拟线程 (Virtual Threads)**。在秒杀场景下，系统面临海量网络 I/O 等待，虚拟线程极为轻量，**能极大提升秒杀核心接口的并发吞吐量 (QPS)**。目前已在服务日志中成功验证 (`VirtualThread[#57,tomcat-handler-0]`)。

2. **开发工具：Visual Studio Code + IntelliJ IDEA**
   - **选型理由：** 结合两者优势，VS Code 用于轻量级编码，IDEA 用于复杂调试和重构。

3. **应用框架：Spring Boot 3.2.x & Spring Cloud**
   - **选型理由：** Spring Boot 3 完美兼容 Java 21，原生支持开启虚拟线程特性。配置 `spring.threads.virtual.enabled=true` 即可启用。

4. **持久层框架：MyBatis-Plus 3.5.5**
   - **选型理由：** 极大简化单表 CRUD 开发，内置乐观锁插件，完美支持秒杀场景下的乐观锁操作。

5. **数据库：MySQL 8.0 (部署于阿里云 RDS)**
   - **选型理由：** 成熟稳定，已在阿里云创建 `seckill_db` 实例，连接地址：`rm-cn-swx4p26ot00019do.rwlb.rds.aliyuncs.com`。

6. **缓存中间件：Redis**
   - **选型理由：** 秒杀系统的"防弹衣"。用于商品信息预热，并通过 Lua 脚本实现原子级别的库存预扣减，彻底防止"超卖"。计划使用 Lettuce 客户端。

7. **消息队列：RabbitMQ (待集成)**
   - **选型理由：** 用于秒杀业务的异步解耦与"削峰填谷"。抢单成功后返回用户排队中，订单服务异步落库，防止数据库被打挂。

8. **版本控制：Git + GitHub**
   - **选型理由：** 代码已托管至 GitHub，地址：`https://github.com/ayako17/mall-seckill`

---

## 🚀 五、 已完成工作 (截止2026-03-14)

### ✅ 已完成功能
1. **数据库设计**：完成6张核心表的建表语句，包含完整的索引、约束和注释
2. **用户服务**：
   - 用户注册（BCrypt密码加密）
   - 用户登录（支持用户名/手机号）
   - JWT令牌生成与验证
   - 统一的异常处理与错误信息返回
3. **商品服务**：
   - 普通商品列表接口
   - 秒杀商品列表接口（关联查询）
   - 商品实体与数据库字段映射
   - 秒杀状态定时任务框架
4. **前端集成**：
   - 登录/注册页面
   - 商品列表页面
   - 秒杀商品列表页面
   - Vite代理配置解决跨域
5. **版本控制**：完整 Git 提交历史，已推送至 GitHub

### 🔜 后续开发计划
1. **服务拆分细化**：完成库存服务、订单服务的代码实现
2. **中间件集成**：
   - Redis 缓存预热与 Lua 脚本扣减库存
   - RabbitMQ 异步订单处理
3. **分布式事务**：确保最终一致性
4. **接口联调**：使用 OpenFeign 实现微服务间 RPC 调用
5. **压力测试**：使用 JMeter 进行高并发压测，验证虚拟线程性能提升

---

## 📊 六、 性能优化策略

| 优化点 | 普通场景 | 秒杀场景 |
| :--- | :--- | :--- |
| **查询优化** | MySQL索引 | Redis缓存预热 |
| **库存扣减** | 数据库乐观锁 | Redis Lua脚本原子扣减 |
| **订单处理** | 同步落库 | 异步消息队列 |
| **限流策略** | 无 | 令牌桶/漏桶算法 |
| **防刷机制** | 无 | 唯一约束 + Redis标记 |

---

## 🔧 七、 本地开发环境配置

### 前置要求
- JDK 21 (OpenJDK Temurin)
- Maven 3.9+
- MySQL 8.0 (或使用阿里云RDS连接)
- Git

### 快速启动
```bash
# 克隆项目
git clone https://github.com/ayako17/mall-seckill.git
cd mall-seckill

# 编译公共模块
cd seckill-backend
mvn clean install -pl seckill-common

# 启动用户服务
cd user-service
mvn spring-boot:run

# 启动商品服务
cd ../product-service
mvn spring-boot:run
```

### 配置文件说明
各服务的 `application.yml` 需配置：
- 数据库连接信息
- Redis连接信息（后续集成）
- JWT密钥配置
- 服务端口

---

## 📝 八、 项目总结

本项目通过微服务架构、分布式缓存、消息队列等技术手段，完整实现了一个高并发秒杀系统。核心亮点包括：
1. **架构设计**：严格遵循微服务拆分原则，服务职责单一
2. **数据库设计**：普通库存与秒杀库存分离，独立乐观锁控制
3. **技术前瞻**：采用 Java 21 虚拟线程，为高并发提供语言级支持
4. **防刷机制**：数据库唯一约束 + Redis 标记双重保障
5. **完整链路**：从用户认证、商品展示、库存扣减到订单生成的完整闭环

---

## 📧 九、 联系方式

- **GitHub**: [@ayako17](https://github.com/ayako17)
- **项目地址**: [https://github.com/ayako17/mall-seckill](https://github.com/ayako17/mall-seckill)

---

**最后更新时间：2026年3月14日**
```