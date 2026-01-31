# Admin Frontend

基于 Vue 3 + TypeScript + Vite + Element Plus 的后台管理系统前端。

## 技术栈

- **框架**: Vue 3.5 + TypeScript 5.7
- **构建工具**: Vite 6
- **UI 组件库**: Element Plus 2.9
- **状态管理**: Pinia 2.3
- **路由**: Vue Router 4.5
- **HTTP 客户端**: Axios 1.7
- **代码规范**: ESLint 9 + Prettier 3

## 核心特性

- ⚡️ Vite 极速构建和热更新
- 🖖 Vue 3 Composition API + Script Setup
- 🍍 Pinia 状态管理
- 🎨 Element Plus UI 组件库
- 🌙 深色模式支持
- 🔐 基于角色的权限控制
- 📱 响应式布局

## 快速开始

### 环境要求

- Node.js >= 18.0.0
- npm >= 9.0.0

### 安装依赖

```bash
npm install
```

### 启动开发服务器

```bash
npm run dev
```

访问 http://localhost:5173

### 构建生产环境

```bash
npm run build
```

### 代码检查

```bash
npm run lint
```

### 运行测试

```bash
npm test
```

## 目录结构

```
frontend/
├── src/
│   ├── api/              # API 接口定义
│   ├── assets/           # 静态资源
│   ├── components/       # 公共组件
│   ├── composables/      # 组合式函数
│   ├── directives/       # 自定义指令
│   ├── layout/           # 布局组件
│   ├── router/           # 路由配置
│   ├── stores/           # Pinia 状态管理
│   ├── styles/           # 全局样式
│   ├── types/            # TypeScript 类型定义
│   ├── utils/            # 工具函数
│   └── views/            # 页面组件
├── public/               # 公共静态资源
├── tests/                # 测试文件
├── eslint.config.js      # ESLint 配置
├── tsconfig.json         # TypeScript 配置
├── vite.config.ts        # Vite 配置
└── package.json          # 项目配置
```

## 开发规范

### 命名规范

- 组件文件：PascalCase（如 `UserDialog.vue`）
- 工具函数：camelCase（如 `formatDate.ts`）
- 常量：UPPER_SNAKE_CASE（如 `MAX_PAGE_SIZE`）

### 代码风格

- 使用 ESLint + Prettier 进行代码格式化
- 使用 TypeScript 严格模式
- 组件使用 `<script setup>` 语法

### Git 提交规范

```
feat: 新功能
fix: 修复 bug
docs: 文档更新
style: 代码格式调整
refactor: 代码重构
test: 测试相关
chore: 构建/工具相关
```

## 环境变量

在项目根目录创建 `.env.local` 文件：

```env
VITE_API_BASE_URL=/api
VITE_APP_TITLE=Admin System
```

## 许可证

MIT
