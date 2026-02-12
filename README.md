# Admin System Template

前后端分离的后台管理系统模板，包含 Spring Boot 后端与 Vue 前端，提供用户、角色、菜单、部门、字典等常见管理能力，并内置 Docker 一键部署方案。

**快速导航**
- Docker 部署：[README-DOCKER.md](README-DOCKER.md)
- 后端说明：[backend/README.md](backend/README.md)
- 前端说明：[frontend/README.md](frontend/README.md)
- 包名重命名工具：[tools/rename-package/README.md](tools/rename-package/README.md)

**技术栈**
- 后端：Java 21、Spring Boot 3.5.9、Spring Data JPA、MySQL 8、Redis 7、JWT、SpringDoc OpenAPI
- 前端：Vue 3.5、TypeScript 5.7、Vite 6、Element Plus 2.9、Pinia、Vue Router、Axios
- 部署：Docker、Docker Compose

**目录结构**
- `backend/` 后端多模块工程（Spring Boot）
- `frontend/` 前端工程（Vue 3 + Vite）
- `tools/rename-package/` 包名重命名工具（Node.js CLI）
- [README-DOCKER.md](README-DOCKER.md) Docker 部署指南

**快速开始（Docker 推荐）**
1. 复制环境变量文件：`cp .env.example .env`
1. 按需修改 `.env` 里的数据库密码与 `JWT_SECRET`
1. 构建镜像：`docker compose build`
1. 启动服务：`docker compose up -d`

**服务地址**
- 前端（Docker）：http://localhost:88
- 后端 API：http://localhost:8080
- Swagger UI：http://localhost:8080/swagger-ui.html
- OpenAPI JSON：http://localhost:8080/v3/api-docs

**本地开发（不使用 Docker）**
后端：
1. 准备环境：JDK 21、Maven 3.9+、MySQL 8、Redis 7
1. 初始化数据库：
```sql
CREATE DATABASE admin_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```
```bash
mysql -u root -p admin_system < backend/admin-boot/src/main/resources/schema.sql
mysql -u root -p admin_system < backend/admin-boot/src/main/resources/data.sql
```
1. 修改配置：`backend/admin-boot/src/main/resources/application-dev.yml`
1. 启动：`mvnd -f backend/pom.xml spring-boot:run -pl admin-boot`

前端：
1. 准备环境：Node.js >= 18、npm >= 9
1. 安装依赖：`npm --prefix frontend install`
1. 启动开发：`npm --prefix frontend run dev`
1. 访问：http://localhost:5173

**默认账号**
- 用户名：`admin`
- 密码：`admin123`

**配置说明**
- Docker 环境变量：`.env`（参考 `.env.example`）
- 前端环境变量：`frontend/.env.local`
- 后端开发配置：`backend/admin-boot/src/main/resources/application-dev.yml`

**包名重命名工具**

基于模板创建新项目后，用此工具一键替换包名（Java package/import、pom.xml groupId、配置文件引用、目录结构）：

```bash
# 安装依赖（首次）
cd tools/rename-package && npm install && cd ../..

# 交互式（推荐）— 逐步提示输入，支持确认/覆盖 groupId
npx tsx tools/rename-package/src/index.ts

# 命令行 — 直接指定参数执行
npx tsx tools/rename-package/src/index.ts top.flobby.admin com.example.demo --yes --verify

# 命令行 — 自定义 groupId（两段包名时推导值可能不对）
npx tsx tools/rename-package/src/index.ts top.flobby.admin com.lavaclone --group-id com.lavaclone --yes
```

详见 [tools/rename-package/README.md](tools/rename-package/README.md)

**许可证**
MIT
