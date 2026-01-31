# Admin Backend

基于 Spring Boot 3 + JPA + MySQL + Redis 的后台管理系统后端。

## 技术栈

- **框架**: Spring Boot 3.5.9
- **JDK**: Java 21
- **ORM**: Spring Data JPA
- **数据库**: MySQL 8.0+
- **缓存**: Redis 7.0+
- **认证**: JWT (jjwt 0.12.5)
- **API 文档**: SpringDoc OpenAPI 2.8

## 项目结构

采用 Maven 多模块架构，遵循 DDD 分层设计：

```
backend/
├── admin-common/         # 通用模块：工具类、异常处理、注解
├── admin-shared-kernel/  # 共享内核：领域公共概念
├── admin-system/         # 系统模块：用户、角色、菜单、部门、字典
├── admin-monitor/        # 监控模块：操作日志、系统监控
├── admin-boot/           # 启动模块：Spring Boot 启动类、配置
├── checkstyle.xml        # Checkstyle 代码规范配置
├── spotbugs-exclude.xml  # SpotBugs 排除规则
└── pom.xml               # 父 POM
```

### 模块依赖关系

```
admin-boot
├── admin-system
│   ├── admin-common
│   └── admin-shared-kernel
└── admin-monitor
    └── admin-common
```

## 快速开始

### 环境要求

- JDK 21+
- Maven 3.9+ 或 mvnd
- MySQL 8.0+
- Redis 7.0+

### 数据库配置

1. 创建数据库：

```sql
CREATE DATABASE admin_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 执行初始化脚本：

```bash
mysql -u root -p admin_system < admin-boot/src/main/resources/schema.sql
mysql -u root -p admin_system < admin-boot/src/main/resources/data.sql
```

### 配置文件

修改 `admin-boot/src/main/resources/application-dev.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/admin_system?useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
  data:
    redis:
      host: localhost
      port: 6379
```

### 启动应用

```bash
# 使用 Maven
mvn -f backend/pom.xml spring-boot:run -pl admin-boot

# 使用 mvnd（更快）
mvnd -f backend/pom.xml spring-boot:run -pl admin-boot
```

访问 http://localhost:8080

### API 文档

启动后访问：
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

## 开发指南

### 代码规范

项目配置了 Checkstyle（阿里巴巴 P3C 规范）和 SpotBugs 静态分析：

```bash
# 运行 Checkstyle 检查
mvnd checkstyle:check

# 运行 SpotBugs 检查
mvnd spotbugs:check
```

### 构建打包

```bash
# 打包
mvnd clean package -DskipTests

# 运行 JAR
java -jar admin-boot/target/admin-boot-1.0.0-SNAPSHOT.jar
```

### 测试

```bash
# 运行所有测试
mvnd test

# 运行指定模块测试
mvnd test -pl admin-system
```

## 核心功能

### 认证授权

- JWT Token 认证
- 基于角色的权限控制（RBAC）
- 数据权限控制
- 登录失败锁定机制

### 系统管理

- 用户管理
- 角色管理
- 菜单管理
- 部门管理
- 字典管理

### 系统监控

- 操作日志
- 健康检查

## API 端点

| 模块 | 路径 | 描述 |
|------|------|------|
| 认证 | `/api/auth/*` | 登录、登出、刷新 Token |
| 用户 | `/api/system/users/*` | 用户 CRUD |
| 角色 | `/api/system/roles/*` | 角色 CRUD |
| 菜单 | `/api/system/menus/*` | 菜单 CRUD |
| 部门 | `/api/system/depts/*` | 部门 CRUD |
| 字典 | `/api/system/dicts/*` | 字典 CRUD |
| 日志 | `/api/monitor/operlog/*` | 操作日志查询 |

## 默认账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | admin123 | 超级管理员 |

## 许可证

MIT
