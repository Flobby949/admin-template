# Admin Code Generator

代码生成器模块，从数据库表逆向生成 CRUD 代码。

## 功能特性

- 交互式配置向导
- 从数据库表逆向生成代码
- 支持自定义模板
- 生成完整的后端代码（Entity、Repository、Service、Controller、DTO、VO）
- 生成完整的前端代码（API、列表页面、表单组件）
- 生成菜单权限SQL（支持一级目录、二级菜单、三级按钮权限）

## 快速开始

### 构建

```bash
cd backend
mvnd clean package -pl admin-generator -am -DskipTests
```

### 使用

**方式一：IDE 中直接运行**

直接运行 `GeneratorApplication.main()` 方法，会自动启动交互式向导并搜索配置文件。

**方式二：命令行运行**

```bash
# 直接运行（自动搜索配置文件）
java -jar admin-generator/target/admin-generator.jar

# 指定配置文件
java -jar admin-generator/target/admin-generator.jar -c generator.yml
```

### 交互式向导流程

```
╔════════════════════════════════════════════════════════════╗
║           Admin Code Generator - 交互式配置向导            ║
╚════════════════════════════════════════════════════════════╝

【步骤 1/7】数据库配置
─────────────────────────────────────────
检测到配置文件: generator.yml
是否使用该配置文件? [Y/n]:
数据库连接成功！

【步骤 2/7】选择要生成的表
─────────────────────────────────────────
数据库中共有 15 个表:
  [ 1] demo_product
  [ 2] demo_order
  ...
选择方式:
  - 输入序号（多个用逗号分隔，如: 1,3,5）
  - 输入表名或通配符（如: demo_* 或 demo_product,demo_order）
  - 输入 'all' 选择所有表
请选择: 1

【步骤 3/7】模块配置
─────────────────────────────────────────
模块名 [demo]:
包名前缀 [top.flobby.admin]:
作者 [Code Generator]:
表前缀 [sys_,cms_,t_,demo_]:

【步骤 4/7】生成选项
─────────────────────────────────────────
生成后端代码? [Y/n]:
生成前端代码? [Y/n]:
覆盖已存在的文件? [y/N]:

【步骤 5/7】菜单权限SQL配置
─────────────────────────────────────────
是否生成菜单权限SQL? [y/N]: y
是否创建一级目录（新模块需要）? [y/N]:
一级目录ID: 300
二级菜单起始ID: 310
菜单图标 [List]:

【步骤 6/7】输出配置
─────────────────────────────────────────
输出目录（项目根目录）[.]: ..
预览模式（不实际生成文件）? [y/N]:

【步骤 7/7】确认配置
─────────────────────────────────────────
配置摘要:
  模块名: demo
  包名: top.flobby.admin
  ...
确认生成? [Y/n]:

开始生成代码...
代码生成完成！
```

## 配置文件说明

配置文件 `generator.yml` 用于提供数据库连接和默认配置。程序会自动在以下位置搜索配置文件：

1. `generator.yml`
2. `src/main/resources/generator.yml`
3. `admin-generator/src/main/resources/generator.yml`
4. `backend/admin-generator/src/main/resources/generator.yml`

```yaml
# 全局配置（默认值）
global:
  packageName: top.flobby.admin  # 包名前缀
  moduleName: demo               # 默认模块名
  author: Code Generator         # 作者
  tablePrefix: sys_,cms_         # 表前缀（生成类名时去除）

# 数据库配置
database:
  url: jdbc:mysql://localhost:3306/admin_system
  username: root
  password: your_password
```

## 生成的代码结构

### 后端

```
backend/admin-{module}/src/main/java/{package}/{module}/
├── domain/
│   ├── entity/{Entity}.java
│   └── repository/{Entity}Repository.java
├── infrastructure/
│   └── repository/
│       ├── Jpa{Entity}Repository.java
│       └── {Entity}RepositoryImpl.java
├── application/
│   └── {Entity}Service.java
└── interfaces/
    ├── controller/{Entity}Controller.java
    ├── dto/{Entity}DTO.java
    ├── vo/{Entity}VO.java
    └── query/{Entity}Query.java
```

### 前端

```
frontend/src/
├── api/{entity}.ts
└── views/{module}/{entity}/
    ├── index.vue
    └── components/{Entity}Dialog.vue
```

### 菜单SQL

```
backend/admin-{module}/src/main/resources/sql/
└── menu_{entity}.sql
```

## 菜单权限SQL生成

### ID分配规则

| 模块 | 一级目录ID | 二级菜单ID范围 |
|------|-----------|---------------|
| system | 1 | 2-23 |
| monitor | 100 | 101-104 |
| cms | 200 | 201-224 |
| demo | 300 | 301-399 |
| **新模块** | 400+ | 按需分配 |

**建议**: 每个模块预留100个ID。

### 生成的SQL示例

```sql
-- 菜单权限SQL - Product
-- 模块: demo
-- 实体: 商品

-- 1. 一级目录（新模块需要）
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, route_path, component, permission, icon, sort_order, visible, status, deleted)
VALUES (300, 0, '演示模块', 1, '/demo', 'Layout', NULL, 'Promotion', 5, 1, 1, 0)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name), route_path = VALUES(route_path), icon = VALUES(icon);

-- 2. 二级菜单
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, route_path, component, permission, icon, sort_order, visible, status, deleted)
VALUES (310, 300, '商品管理', 2, 'product', 'demo/product/index', 'demo:product:list', 'Goods', 1, 1, 1, 0)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name), component = VALUES(component), permission = VALUES(permission);

-- 3. 三级按钮权限
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, route_path, component, permission, icon, sort_order, visible, status, deleted)
VALUES
    (311, 310, '商品新增', 3, NULL, NULL, 'demo:product:add', NULL, 1, 1, 1, 0),
    (312, 310, '商品编辑', 3, NULL, NULL, 'demo:product:edit', NULL, 2, 1, 1, 0),
    (313, 310, '商品删除', 3, NULL, NULL, 'demo:product:delete', NULL, 3, 1, 1, 0)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name), permission = VALUES(permission);
```

## 自定义模板

1. 复制默认模板到指定目录
2. 修改模板文件
3. 在配置文件中指定自定义模板目录

```yaml
global:
  templatePath: /path/to/custom/templates
```

模板使用 FreeMarker 语法。

## 完整使用流程

```bash
# 1. 构建
cd backend
mvnd clean package -pl admin-generator -am -DskipTests

# 2. 运行（以下任选一种）
# IDE: 直接运行 GeneratorApplication.main()
# 命令行: java -jar admin-generator/target/admin-generator.jar

# 3. 按提示完成配置

# 4. 执行生成的SQL（如果生成了菜单SQL）

# 5. 如果是新模块，手动添加到 admin-boot/pom.xml

# 6. 验证
mvnd clean compile
```
