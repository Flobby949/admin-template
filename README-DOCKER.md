# Docker 部署指南

本文档提供基于 Docker 和 Docker Compose 的完整部署方案，包含前后端应用、MySQL 数据库和 Redis 缓存。

## 📋 目录

- [系统要求](#系统要求)
- [快速开始](#快速开始)
- [架构说明](#架构说明)
- [配置管理](#配置管理)
- [构建与部署](#构建与部署)
- [健康检查](#健康检查)
- [日志管理](#日志管理)
- [常见问题](#常见问题)

---

## 系统要求

- Docker 20.10+
- Docker Compose 2.0+
- 至少 4GB 可用内存
- 至少 10GB 可用磁盘空间

---

## 快速开始

### 1. 准备环境变量

复制环境变量模板并填写实际值：

```bash
cp .env.example .env
```

编辑 `.env` 文件，设置以下关键变量：

```env
MYSQL_ROOT_PASSWORD=your_secure_root_password
MYSQL_DATABASE=admin_system
MYSQL_USER=admin
MYSQL_PASSWORD=your_secure_password
JWT_SECRET=your_jwt_secret_at_least_32_characters_long
```

**安全提示**：
- 使用强密码（至少 16 字符，包含大小写字母、数字和特殊字符）
- JWT_SECRET 至少 32 字符
- 不要将 `.env` 文件提交到版本控制系统

### 2. 构建镜像

```bash
# 构建所有服务
docker compose build

# 或分别构建
docker compose build backend
docker compose build frontend
```

### 3. 启动服务

```bash
# 启动所有服务（后台运行）
docker compose up -d

# 查看启动日志
docker compose logs -f

# 查看特定服务日志
docker compose logs -f backend
docker compose logs -f frontend
```

### 4. 验证部署

**健康检查**：

```bash
# 检查所有服务状态
docker compose ps

# 后端健康检查
curl http://localhost:8080/actuator/health

# 前端健康检查
curl http://localhost/health
```

**访问应用**：

- 前端：http://localhost
- 后端 API：http://localhost:8080
- API 文档：http://localhost:8080/doc.html

---

## 架构说明

### 服务组成

| 服务 | 镜像 | 端口 | 说明 |
|------|------|------|------|
| mysql | mysql:8.0 | 3306 | 数据库服务 |
| redis | redis:7-alpine | 6379 | 缓存服务 |
| backend | 自定义构建 | 8080 | Spring Boot 后端 |
| frontend | 自定义构建 | 80 | Vue + Nginx 前端 |

### 网络架构

```
┌─────────────────────────────────────────────┐
│              Docker Network                 │
│           (admin-network)                   │
│                                             │
│  ┌──────────┐    ┌──────────┐             │
│  │  MySQL   │    │  Redis   │             │
│  │  :3306   │    │  :6379   │             │
│  └────┬─────┘    └────┬─────┘             │
│       │               │                     │
│       └───────┬───────┘                     │
│               │                             │
│         ┌─────▼─────┐                       │
│         │  Backend  │                       │
│         │   :8080   │                       │
│         └─────┬─────┘                       │
│               │                             │
│         ┌─────▼─────┐                       │
│         │ Frontend  │                       │
│         │   :80     │                       │
│         └─────┬─────┘                       │
└───────────────┼─────────────────────────────┘
                │
         ┌──────▼──────┐
         │   Browser   │
         └─────────────┘
```

### 数据持久化

- `mysql-data`：MySQL 数据目录
- `redis-data`：Redis 持久化数据
- `backend-logs`：后端应用日志

---

## 配置管理

### 环境变量映射

| Spring 配置项 | 环境变量 | 说明 |
|--------------|---------|------|
| spring.datasource.url | SPRING_DATASOURCE_URL | 数据库连接 URL |
| spring.datasource.username | SPRING_DATASOURCE_USERNAME | 数据库用户名 |
| spring.datasource.password | SPRING_DATASOURCE_PASSWORD | 数据库密码 |
| spring.data.redis.host | SPRING_REDIS_HOST | Redis 主机 |
| spring.data.redis.port | SPRING_REDIS_PORT | Redis 端口 |
| jwt.secret | JWT_SECRET | JWT 签名密钥 |

### 数据库初始化

- 首次启动时，MySQL 会自动执行 `schema.sql` 和 `data.sql`
- 初始化脚本仅在数据卷为空时执行
- 如需重新初始化，删除数据卷后重启：

```bash
docker compose down -v
docker compose up -d
```

---

## 构建与部署

### 开发环境

```bash
# 启动服务
docker compose up -d

# 重新构建并启动
docker compose up -d --build

# 停止服务
docker compose down

# 停止并删除数据卷（谨慎使用）
docker compose down -v
```

### 生产环境

**推荐使用 Docker Swarm 或 Kubernetes 进行生产部署**。

#### 使用 Docker Swarm

```bash
# 初始化 Swarm
docker swarm init

# 部署服务栈
docker stack deploy -c docker-compose.yml admin-stack

# 查看服务状态
docker stack services admin-stack

# 扩展服务
docker service scale admin-stack_backend=3
docker service scale admin-stack_frontend=2

# 删除服务栈
docker stack rm admin-stack
```

#### 镜像推送到私有仓库

```bash
# 标记镜像
docker tag backend-template-backend:latest registry.example.com/admin-backend:1.0.0
docker tag backend-template-frontend:latest registry.example.com/admin-frontend:1.0.0

# 推送镜像
docker push registry.example.com/admin-backend:1.0.0
docker push registry.example.com/admin-frontend:1.0.0
```

---

## 健康检查

### 自动健康检查

所有服务都配置了健康检查，Docker 会自动监控服务状态：

- **MySQL**：每 10 秒检查一次，超时 3 秒，重试 10 次
- **Redis**：每 10 秒检查一次，超时 3 秒，重试 10 次
- **Backend**：每 30 秒检查一次，启动等待 40 秒，重试 3 次
- **Frontend**：每 30 秒检查一次，启动等待 10 秒，重试 3 次

### 手动健康检查

```bash
# 查看所有服务健康状态
docker compose ps

# 后端健康检查
curl http://localhost:8080/actuator/health

# 预期输出
{"status":"UP"}

# 前端健康检查
curl http://localhost/health

# 预期输出
healthy
```

---

## 日志管理

### 查看日志

```bash
# 查看所有服务日志
docker compose logs

# 实时跟踪日志
docker compose logs -f

# 查看特定服务日志
docker compose logs backend
docker compose logs frontend

# 查看最近 100 行日志
docker compose logs --tail=100 backend
```

### 日志持久化

后端日志默认写入 `/opt/logs/admin-api/`，已挂载到 Docker 卷 `backend-logs`。

**查看持久化日志**：

```bash
# 进入后端容器
docker compose exec backend sh

# 查看日志文件
ls -lh /opt/logs/admin-api/
tail -f /opt/logs/admin-api/admin-api.log
```

**导出日志到宿主机**：

```bash
docker compose cp backend:/opt/logs/admin-api ./logs
```

---

## 常见问题

### 1. 启动失败：端口被占用

**错误信息**：
```
Error starting userland proxy: listen tcp4 0.0.0.0:80: bind: address already in use
```

**解决方案**：
```bash
# 查看占用端口的进程
lsof -i :80
lsof -i :8080

# 停止占用端口的服务或修改 docker-compose.yml 中的端口映射
```

### 2. 后端无法连接数据库

**错误信息**：
```
Communications link failure
```

**解决方案**：
- 检查 MySQL 是否健康：`docker compose ps`
- 检查环境变量是否正确：`docker compose config`
- 查看 MySQL 日志：`docker compose logs mysql`

### 3. 前端 API 请求 404

**原因**：Nginx 反向代理配置错误。

**解决方案**：
- 检查 `frontend/nginx.conf` 中的 `proxy_pass` 配置
- 确认后端服务名为 `backend`（与 docker-compose.yml 一致）
- 重启前端服务：`docker compose restart frontend`

### 4. 数据库初始化脚本未执行

**原因**：数据卷已存在数据。

**解决方案**：
```bash
# 删除数据卷并重新启动
docker compose down -v
docker compose up -d
```

### 5. 构建速度慢

**优化方案**：

1. **启用 BuildKit**：
```bash
export DOCKER_BUILDKIT=1
docker compose build
```

2. **使用国内镜像源**：

编辑 `backend/docker/Dockerfile`，在 Maven 构建阶段添加：
```dockerfile
RUN mkdir -p /root/.m2 && \
    echo '<settings><mirrors><mirror><id>aliyun</id><url>https://maven.aliyun.com/repository/public</url><mirrorOf>central</mirrorOf></mirror></mirrors></settings>' > /root/.m2/settings.xml
```

编辑 `frontend/Dockerfile`，在 npm install 前添加：
```dockerfile
RUN npm config set registry https://registry.npmmirror.com
```

### 6. 内存不足

**错误信息**：
```
Cannot allocate memory
```

**解决方案**：
- 增加 Docker 可用内存（Docker Desktop 设置）
- 减少后端 JVM 内存：修改 `JAVA_OPTS="-XX:MaxRAMPercentage=50"`

---

## 维护操作

### 备份数据

```bash
# 备份 MySQL 数据
docker compose exec mysql mysqldump -u root -p${MYSQL_ROOT_PASSWORD} ${MYSQL_DATABASE} > backup.sql

# 备份 Redis 数据
docker compose exec redis redis-cli SAVE
docker compose cp redis:/data/dump.rdb ./redis-backup.rdb
```

### 恢复数据

```bash
# 恢复 MySQL 数据
docker compose exec -T mysql mysql -u root -p${MYSQL_ROOT_PASSWORD} ${MYSQL_DATABASE} < backup.sql

# 恢复 Redis 数据
docker compose cp ./redis-backup.rdb redis:/data/dump.rdb
docker compose restart redis
```

### 更新服务

```bash
# 拉取最新代码
git pull

# 重新构建并启动
docker compose up -d --build

# 滚动更新（零停机）
docker compose up -d --no-deps --build backend
docker compose up -d --no-deps --build frontend
```

---

## 安全建议

1. **不要在生产环境暴露数据库端口**：
   - 移除 `docker-compose.yml` 中 MySQL 和 Redis 的 `ports` 配置

2. **使用 Docker Secrets**（Swarm 模式）：
   ```bash
   echo "your_password" | docker secret create mysql_password -
   ```

3. **启用 HTTPS**：
   - 使用 Nginx 反向代理（如 Traefik、Caddy）
   - 配置 SSL 证书（Let's Encrypt）

4. **定期更新镜像**：
   ```bash
   docker compose pull
   docker compose up -d
   ```

---

## 技术支持

如有问题，请查看：
- 后端日志：`docker compose logs backend`
- 前端日志：`docker compose logs frontend`
- 数据库日志：`docker compose logs mysql`

或提交 Issue 到项目仓库。
