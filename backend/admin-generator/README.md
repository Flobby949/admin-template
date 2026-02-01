# Admin Code Generator

代码生成器模块，用于快速生成 CRUD 代码。

## 功能特性

- 从配置文件生成代码
- 从数据库表逆向生成代码
- 支持自定义模板
- 生成完整的后端代码（Entity、Repository、Service、Controller、DTO、VO）
- 生成完整的前端代码（API、列表页面、表单组件）

## 快速开始

### 构建

```bash
cd backend
mvnd clean package -pl admin-generator -am -DskipTests
```

### 使用方式

#### 1. 从配置文件生成

```bash
# 查看帮助
java -jar admin-generator/target/admin-generator-1.0.0-SNAPSHOT.jar generate --help

# 从配置文件生成代码
java -jar admin-generator/target/admin-generator-1.0.0-SNAPSHOT.jar generate generator.yml -o /path/to/project

# 预览模式（不实际生成文件）
java -jar admin-generator/target/admin-generator-1.0.0-SNAPSHOT.jar generate generator.yml -p
```

#### 2. 从数据库逆向生成

```bash
# 查看帮助
java -jar admin-generator/target/admin-generator-1.0.0-SNAPSHOT.jar reverse --help

# 列出所有表
java -jar admin-generator/target/admin-generator-1.0.0-SNAPSHOT.jar reverse --list -c generator.yml

# 生成指定表的代码
java -jar admin-generator/target/admin-generator-1.0.0-SNAPSHOT.jar reverse demo_product,demo_order -c generator.yml -m demo

# 使用通配符
java -jar admin-generator/target/admin-generator-1.0.0-SNAPSHOT.jar reverse "demo_*" -c generator.yml -m demo

# 直接指定数据库连接
java -jar admin-generator/target/admin-generator-1.0.0-SNAPSHOT.jar reverse demo_product \
  --url "jdbc:mysql://localhost:3306/admin_system" \
  --username root \
  --password your_password \
  -m demo
```

## 配置文件说明

参考 `src/main/resources/generator.yml` 示例配置。

### 全局配置

```yaml
global:
  packageName: top.flobby.admin  # 包名前缀
  moduleName: demo               # 模块名
  author: Code Generator         # 作者
  tablePrefix: sys_,cms_         # 表前缀（生成类名时去除）
```

### 数据库配置

```yaml
database:
  url: jdbc:mysql://localhost:3306/admin_system
  username: root
  password: your_password
  tables:
    - demo_*  # 支持通配符
```

### 实体配置

```yaml
entities:
  - tableName: demo_product
    className: Product
    comment: 商品
    fields:
      - columnName: id
        fieldName: id
        fieldType: Long
        isPrimaryKey: true
        # ... 更多字段配置
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

## 自定义模板

1. 导出默认模板到指定目录
2. 修改模板文件
3. 在配置文件中指定自定义模板目录

```yaml
global:
  templatePath: /path/to/custom/templates
```

模板使用 FreeMarker 语法，可用变量参考 `plan.md` 中的 Template Variables 部分。
