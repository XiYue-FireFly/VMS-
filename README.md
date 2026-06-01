# XingChen WMS - 仓储管理系统

基于 JeecgBoot 3.8.1 低代码开发平台构建的仓储管理系统（WMS）Java 项目。
### 项目组成

| 项目名 | 说明 |
|--------|------|
| `xincheng-wms-java-course` | 后端源码（SpringBoot 架构） |
| `jeecgboot-vue3` | 前端源码（Vue3 + Vite5 + TypeScript） |

## 技术架构

### 后端技术栈

| 分类 | 技术 |
|------|------|
| 基础框架 | Spring Boot 3.4.5 |
| 微服务框架 | Spring Cloud Alibaba 2023.0.3.2 |
| 持久层 | MyBatis-Plus 3.5.12 |
| 安全框架 | Apache Shiro 2.0.4 + JWT 4.5.0 |
| 数据库 | MySQL 8.0+（支持 PostgreSQL、Oracle、达梦等） |
| 缓存 | Redis 5.0+ |
| 连接池 | Druid 1.2.24 |
| Java 版本 | JDK 17 |

### 前端技术栈

- Vue 3.0 + TypeScript + Vite 5
- Ant Design Vue 4 + Pinia
- 动态菜单、权限校验、按钮级别权限控制

### 数据库支持

| 数据库 | 支持 |
|--------|------|
| MySQL | √ |
| PostgreSQL | √ |
| Oracle 11g | √ |
| SQL Server 2017 | √ |
| 达梦 | √ |
| 人大金仓 | √ |

## 项目结构

```
xincheng-wms-java-course/
├── jeecg-boot-base-core/              # 核心模块（工具类、配置、权限、查询过滤器）
├── jeecg-boot-module/                 # 业务模块
│   └── jeecg-module-demo/             # 示例代码
├── jeecg-module-system/               # 系统管理模块
│   ├── jeecg-system-api/              # 系统API接口
│   │   ├── jeecg-system-cloud-api/    # 微服务接口
│   │   └── jeecg-system-local-api/    # 单体接口
│   ├── jeecg-system-biz/              # 系统业务逻辑
│   └── jeecg-system-start/            # 单体启动项目（端口：8080）
├── jeecg-module-wms/                  # WMS 仓储管理模块
├── jeecg-server-cloud/                # 微服务模块
│   ├── jeecg-cloud-gateway/           # 网关（端口：9999）
│   ├── jeecg-cloud-nacos/             # Nacos 注册中心（端口：8848）
│   ├── jeecg-system-cloud-start/      # System 微服务启动（端口：7001）
│   └── jeecg-visual/                  # 可视化工具
│       ├── jeecg-cloud-monitor/       # 微服务监控（端口：9111）
│       ├── jeecg-cloud-xxljob/        # 定时任务（端口：9080）
│       └── jeecg-cloud-sentinel/      # 流量控制（端口：9000）
├── db/                                # 数据库脚本
├── docker-compose.yml                 # Docker 部署配置
└── pom.xml                            # Maven 父 POM
```

## 快速启动

### 方式一：本地开发环境

#### 环境要求
- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Redis 5.0+
- Node.js 16+（前端开发）

#### 1. 初始化数据库

```sql
CREATE DATABASE xingchenwms DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

导入数据脚本：
```bash
mysql -u root -p xingchenwms < db/xingchenwms-20250912.sql
mysql -u root -p xingchenwms < db/xingchenwms-20251114.sql
```

#### 2. 修改配置

编辑 `jeecg-module-system/jeecg-system-start/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/xingchenwms?useUnicode=true&characterEncoding=utf8&autoReconnect=true
    username: root
    password: your_password
  redis:
    host: localhost
    port: 6379
```

#### 3. 启动后端

```bash
# 编译项目
mvn clean install -DskipTests

# 启动系统模块
cd jeecg-module-system/jeecg-system-start
mvn spring-boot:run
```

#### 4. 启动前端

```bash
# 克隆前端项目
git clone https://github.com/jeecgboot/jeecgboot-vue3.git
cd jeecgboot-vue3

# 安装依赖并启动
npm install
npm run dev
```

启动后访问：
- 后端 API：http://localhost:8080/jeecg-boot
- 前端界面：http://localhost:3000
- 默认账号：`admin` / `123456`

### 方式二：Docker 部署

```bash
docker-compose up -d
```

包含服务：MySQL、Redis、System 应用

## 微服务模式

### 启动步骤

```bash
# 1. 启动 Nacos 注册中心
cd jeecg-server-cloud/jeecg-cloud-nacos
mvn spring-boot:run

# 2. 启动网关
cd jeecg-server-cloud/jeecg-cloud-gateway
mvn spring-boot:run

# 3. 启动业务服务
cd jeecg-server-cloud/jeecg-system-cloud-start
mvn spring-boot:run
```

### 微服务组件

| 组件 | 说明 |
|------|------|
| Nacos | 服务注册发现 + 配置中心 |
| Gateway | 路由网关（三种加载方式） |
| Sentinel | 熔断降级限流 |
| Seata | 分布式事务 |
| Skywalking | 链路跟踪 |
| RabbitMQ | 消息中间件 |
| XXL-Job | 分布式任务调度 |
| MinIO | 分布式文件存储 |

### 微服务架构图

![微服务架构图](https://jeecgos.oss-cn-beijing.aliyuncs.com/files/jeecgboot_springcloud2022.png)

## WMS 功能模块

- **仓库管理**：仓库信息维护、库区划分
- **库位管理**：库位编码、容量管理
- **库存管理**：实时库存查询、库存预警
- **入库管理**：采购入库、生产入库、退货入库
- **出库管理**：销售出库、生产领料
- **库存盘点**：盘点任务、差异处理

## 开发指南

### 代码生成器

1. 登录后台 → 在线开发 → 代码生成器
2. 选择数据表 → 配置生成策略
3. 一键生成前后端代码

### SQL 脚本规范

| 类型 | 前缀 | 说明 |
|------|------|------|
| 增量脚本 | `V` | 仅执行一次，如 `V20240104_1__wms_add_table.sql` |
| 重复脚本 | `R` | 内容变化时执行，如 `R__init_data.sql` |

## 相关资源

- [JeecgBoot 官方文档](http://www.jeecg.com)
- [快速入门指南](http://www.jeecg.com/doc/quickstart)
- [在线演示](http://boot3.jeecg.com)

## 许可证

[Apache License 2.0](LICENSE)
