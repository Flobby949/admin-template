# Implementation Plan: 代码生成器

**Branch**: `003-code-generator` | **Date**: 2026-02-01 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/003-code-generator/spec.md`

## Summary

构建一个代码生成器模块，支持通过配置文件或数据库逆向生成完整的 CRUD 代码，包括后端（Entity、Repository、Service、Controller、DTO、VO）和前端（API、页面、组件）。采用 FreeMarker 模板引擎，支持模板自定义。

## Technical Context

**Language/Version**:
- 后端: Java 21
- 模板引擎: FreeMarker 2.3.x

**Primary Dependencies**:
- FreeMarker: 模板引擎
- MySQL Connector: 数据库元数据读取
- SnakeYAML: YAML 配置解析
- Picocli: 命令行参数解析

**Target Platform**: 命令行工具 + 可选 Web 界面

**Project Type**: Maven 模块（admin-generator）

**Constraints**:
- 生成的代码必须符合项目现有架构规范
- 模板必须可自定义
- 支持增量生成（不覆盖已有文件）

## Project Structure

### Documentation (this feature)

```text
specs/003-code-generator/
├── plan.md              # This file
├── spec.md              # Feature specification
└── tasks.md             # Task list
```

### Source Code (repository root)

```text
backend/
├── admin-generator/                     # 代码生成器模块
│   ├── pom.xml
│   └── src/main/java/top/flobby/admin/generator/
│       ├── GeneratorApplication.java    # 命令行入口
│       ├── config/                      # 配置类
│       │   ├── GeneratorConfig.java     # 全局配置
│       │   ├── EntityConfig.java        # 实体配置
│       │   └── FieldConfig.java         # 字段配置
│       ├── engine/                      # 模板引擎
│       │   ├── TemplateEngine.java      # 模板引擎封装
│       │   └── CodeGenerator.java       # 代码生成器核心
│       ├── reader/                      # 元数据读取
│       │   ├── DatabaseReader.java      # 数据库元数据读取
│       │   └── ConfigReader.java        # 配置文件读取
│       ├── writer/                      # 代码输出
│       │   └── CodeWriter.java          # 代码文件写入
│       └── util/                        # 工具类
│           ├── NameConverter.java       # 命名转换
│           └── TypeMapper.java          # 类型映射
│   └── src/main/resources/
│       ├── templates/                   # 默认模板
│       │   ├── backend/
│       │   │   ├── entity.ftl           # Entity 模板
│       │   │   ├── repository.ftl       # Repository 模板
│       │   │   ├── jpa-repository.ftl   # JPA Repository 模板
│       │   │   ├── repository-impl.ftl  # Repository 实现模板
│       │   │   ├── service.ftl          # Service 模板
│       │   │   ├── controller.ftl       # Controller 模板
│       │   │   ├── dto.ftl              # DTO 模板
│       │   │   └── vo.ftl               # VO 模板
│       │   └── frontend/
│       │       ├── api.ftl              # API 接口模板
│       │       ├── index.ftl            # 列表页面模板
│       │       └── dialog.ftl           # 表单对话框模板
│       └── generator.yml                # 默认配置示例
```

## Architecture Decisions

### ADR-001: 选择 FreeMarker 作为模板引擎

**决策**: 采用 FreeMarker 作为代码生成模板引擎

**理由**:
- 成熟稳定，社区活跃
- 语法简洁，易于学习
- 支持复杂的模板逻辑
- 与 Java 集成良好

**替代方案**: Velocity（较老）、Thymeleaf（更适合 HTML）

### ADR-002: 选择 Picocli 作为命令行框架

**决策**: 采用 Picocli 作为命令行参数解析框架

**理由**:
- 注解驱动，代码简洁
- 自动生成帮助信息
- 支持子命令
- 支持 GraalVM Native Image

**替代方案**: Apache Commons CLI（较老）、JCommander（功能类似）

### ADR-003: 配置文件格式选择 YAML

**决策**: 采用 YAML 作为配置文件格式

**理由**:
- 可读性好，适合复杂配置
- 支持注释
- 与 Spring Boot 配置风格一致

**替代方案**: JSON（不支持注释）、Properties（不适合嵌套结构）

## Type Mapping

### MySQL to Java Type Mapping

| MySQL Type | Java Type | 说明 |
|------------|-----------|------|
| BIGINT | Long | 主键、外键 |
| INT, INTEGER | Integer | 整数 |
| TINYINT | Integer | 状态、标记 |
| VARCHAR, CHAR | String | 字符串 |
| TEXT, LONGTEXT | String | 长文本 |
| DECIMAL, NUMERIC | BigDecimal | 金额 |
| DATETIME, TIMESTAMP | LocalDateTime | 时间 |
| DATE | LocalDate | 日期 |
| TIME | LocalTime | 时间 |
| BIT, BOOLEAN | Boolean | 布尔 |

### Field to Form Type Mapping

| 字段特征 | 表单类型 | 说明 |
|----------|----------|------|
| 名称包含 status | select | 状态选择 |
| 名称包含 type | select | 类型选择 |
| 名称包含 content | textarea | 富文本 |
| 名称包含 remark | textarea | 备注 |
| 名称包含 time/date | datetime | 时间选择 |
| 其他 String | input | 文本输入 |
| Integer/Long | number | 数字输入 |

## Template Variables

### Common Variables

| 变量名 | 类型 | 说明 |
|--------|------|------|
| packageName | String | 包名前缀 |
| moduleName | String | 模块名 |
| author | String | 作者 |
| date | String | 生成日期 |
| entity | EntityConfig | 实体配置 |
| fields | List<FieldConfig> | 字段列表 |

### Entity Variables

| 变量名 | 类型 | 说明 |
|--------|------|------|
| entity.tableName | String | 表名 |
| entity.className | String | 类名（PascalCase） |
| entity.classNameLower | String | 类名（camelCase） |
| entity.comment | String | 实体注释 |
| entity.moduleName | String | 模块名 |

### Field Variables

| 变量名 | 类型 | 说明 |
|--------|------|------|
| field.columnName | String | 列名 |
| field.fieldName | String | 字段名（camelCase） |
| field.fieldType | String | Java 类型 |
| field.comment | String | 字段注释 |
| field.isPrimaryKey | Boolean | 是否主键 |
| field.isNullable | Boolean | 是否可空 |
| field.isQuery | Boolean | 是否查询条件 |
| field.isList | Boolean | 是否列表显示 |
| field.isForm | Boolean | 是否表单字段 |
| field.formType | String | 表单类型 |
