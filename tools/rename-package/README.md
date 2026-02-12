# Package Rename Tool

一键重命名后端 Maven 多模块项目的 Java 包名。自动完成 Java 源文件的 package/import 替换、pom.xml groupId 更新、配置文件包名引用替换、以及目录结构迁移。

## 前置条件

- Node.js >= 18
- 从项目根目录运行（工具自动查找 `backend/` 目录）

## 快速开始

```bash
# 安装依赖（首次使用）
cd tools/rename-package && npm install && cd ../..

# 方式一：交互式（推荐，无需记忆参数）
npx tsx tools/rename-package/src/index.ts

# 方式二：命令行直接执行
npx tsx tools/rename-package/src/index.ts top.flobby.admin com.example.demo --yes --verify
```

## 两种使用模式

工具支持**交互式模式**和**命令行模式**，两种模式底层执行逻辑完全一致（validate → scan → preview → execute → report），仅入口的参数收集方式不同。

### 交互式模式（无参数启动）

不带任何参数运行工具，逐步提示输入：

```bash
npx tsx tools/rename-package/src/index.ts
```

交互流程：

```
Package Rename Tool — Interactive Mode

Old package name: top.flobby.admin
New package name: com.example.demo
Preview only (dry-run)? [y/N] n
Verify compile after execution? [y/N] y
```

- 包名格式不合法时会提示错误并重新要求输入（不限次数）
- 新旧包名相同时会要求重新输入新包名
- 收集完成后自动进入预览 → 执行流程（无需再次确认）

### 命令行模式（带参数启动）

直接传入旧包名和新包名：

```bash
npx tsx tools/rename-package/src/index.ts <old-package> <new-package> [options]
```

#### 参数

| 参数 | 必填 | 说明 | 示例 |
|------|------|------|------|
| `old-package` | 是 | 当前项目的包名 | `top.flobby.admin` |
| `new-package` | 是 | 目标新包名 | `com.example.demo` |

包名要求：全小写、以字母开头、用 `.` 分隔、至少 2 段。

#### 选项

| 选项 | 说明 |
|------|------|
| `--dry-run` | 仅预览变更，不修改任何文件 |
| `--yes` | 跳过确认提示，直接执行 |
| `--verify` | 执行后自动运行 `mvnd compile` 验证编译 |
| `--help` | 显示帮助信息 |

### 退出码

| 退出码 | 含义 |
|--------|------|
| 0 | 成功（包括 dry-run 和用户取消） |
| 1 | 参数错误（缺少参数、格式不合法、新旧相同） |
| 2 | 文件操作错误 |
| 3 | 编译验证失败（`--verify` 模式） |

## 典型使用流程

### 第一步：预览

```bash
# 交互式
npx tsx tools/rename-package/src/index.ts
# 在 "Preview only (dry-run)?" 提示时输入 y

# 或命令行
npx tsx tools/rename-package/src/index.ts top.flobby.admin com.example.demo --dry-run
```

输出示例：

```
Package Rename Tool

Mapping:
  Package:   top.flobby.admin -> com.example.demo
  GroupId:   top.flobby -> com.example
  Directory: top/flobby/admin -> com/example/demo

Changes Summary:
  Java files (package/import):  166
  pom.xml (groupId/mainClass):  8
  Config files:                 2
  Directories to move:          0

Warnings (require manual review): 1
  [admin-generator/.../GeneratorConfig.java:49] private String packageName = "top.flobby.admin";
    String literal contains old package name (may need manual review)

(dry-run mode — no changes made)
```

### 第二步：执行

```bash
# 交互式
npx tsx tools/rename-package/src/index.ts
# 在 "Preview only (dry-run)?" 提示时输入 n

# 或命令行
npx tsx tools/rename-package/src/index.ts top.flobby.admin com.example.demo --yes
```

### 第三步：验证

```bash
# 交互式
npx tsx tools/rename-package/src/index.ts
# 在 "Verify compile after execution?" 提示时输入 y

# 或命令行
npx tsx tools/rename-package/src/index.ts top.flobby.admin com.example.demo --yes --verify
```

### 第四步：处理 Warnings

检查报告中的 Warnings 列表，手动处理字符串常量中的旧包名引用（如反射调用、配置默认值等）。

## 处理范围

工具仅处理 `backend/` 目录下的文件：

| 文件类型 | 处理方式 |
|---------|---------|
| `*.java` | 替换 `package` 声明和 `import` 语句中的旧包名前缀 |
| `pom.xml` | 替换项目 `<groupId>`（精确匹配）和 `<mainClass>` 中的旧包名 |
| `*.yml` / `*.yaml` | 替换配置值中的旧包名引用 |
| `*.xml`（非 pom） | 替换属性值中的旧包名引用（如 logback logger name） |
| `*.properties` | 替换值中的旧包名引用 |

### 排除范围

- `target/` — Maven 构建输出
- `.idea/` — IDE 配置
- `node_modules/` — Node 依赖
- `*.ftl` — FreeMarker 模板（使用变量引用，无需替换）

### 安全机制

- **pom.xml groupId 精确匹配**：只替换值等于旧 groupId 的 `<groupId>` 标签，外部依赖（Spring Boot、MySQL 等）不受影响
- **字符串常量检测**：Java 文件中字符串字面量里的旧包名会被标记为 Warning，不自动替换
- **非空目录保留**：目录迁移后如旧目录仍有非 Java 文件（如 `.gitkeep`），保留并提示

## groupId 推导规则

包名去掉最后一段作为 groupId：

```
top.flobby.admin → groupId: top.flobby
com.example.demo → groupId: com.example
com.example      → groupId: com
```

## 开发与测试

```bash
cd tools/rename-package

# 运行测试
npx vitest run

# Watch 模式
npx vitest

# 类型检查
npx tsc --noEmit
```
