# Feature Specification: 代码生成器

**Feature Branch**: `003-code-generator`
**Created**: 2026-02-01
**Status**: Draft
**Input**: 实现一个代码生成器，用于快速启动新功能模块开发，生成基础 CRUD 代码

## Clarifications

### Session 2026-02-01

- Q: 代码生成器的运行方式？ → A: 命令行工具 + 可选的 Web 界面
- Q: 生成代码的范围？ → A: 后端（Entity、Repository、Service、Controller、DTO/VO）+ 前端（API、页面、组件）
- Q: 是否支持自定义模板？ → A: 是，使用 FreeMarker 模板引擎，支持用户自定义模板

## User Scenarios & Testing *(mandatory)*

### User Story 1 - 基础 CRUD 代码生成 (Priority: P1)

作为开发者，我需要通过简单的配置生成完整的 CRUD 代码，以便快速启动新功能模块的开发。

**Why this priority**: 这是代码生成器的核心功能，直接影响开发效率提升。

**Independent Test**: 可以独立测试通过配置文件生成完整的后端和前端代码。

**Acceptance Scenarios**:

1. **Given** 开发者定义了实体配置（表名、字段、类型）, **When** 执行代码生成命令, **Then** 生成完整的后端代码（Entity、Repository、Service、Controller、DTO、VO）
2. **Given** 开发者定义了实体配置, **When** 执行代码生成命令, **Then** 生成完整的前端代码（API 接口、列表页面、表单组件）
3. **Given** 生成的代码, **When** 编译运行, **Then** 代码无编译错误，CRUD 功能可正常使用

---

### User Story 2 - 数据库逆向生成 (Priority: P1)

作为开发者，我需要从现有数据库表结构逆向生成代码，以便快速为已有表创建管理功能。

**Why this priority**: 很多场景需要为已有数据库表创建管理界面，逆向生成是常见需求。

**Independent Test**: 可以独立测试从数据库表读取结构并生成代码。

**Acceptance Scenarios**:

1. **Given** 数据库中存在表, **When** 执行逆向生成命令并指定表名, **Then** 自动读取表结构并生成对应代码
2. **Given** 表包含多种字段类型, **When** 逆向生成, **Then** 正确映射 MySQL 类型到 Java 类型
3. **Given** 表有外键关联, **When** 逆向生成, **Then** 生成正确的关联关系代码

---

### User Story 3 - 模板自定义 (Priority: P2)

作为开发者，我需要自定义代码生成模板，以便生成符合团队规范的代码。

**Why this priority**: 不同团队有不同的代码规范，模板自定义是高级功能。

**Independent Test**: 可以独立测试自定义模板的加载和使用。

**Acceptance Scenarios**:

1. **Given** 开发者创建了自定义模板, **When** 执行代码生成, **Then** 使用自定义模板生成代码
2. **Given** 自定义模板有语法错误, **When** 执行代码生成, **Then** 给出清晰的错误提示
3. **Given** 模板目录, **When** 查看可用模板, **Then** 显示所有可用模板及其说明

---

### User Story 4 - Web 界面生成 (Priority: P3)

作为开发者，我需要通过 Web 界面配置和生成代码，以便更直观地操作。

**Why this priority**: Web 界面是锦上添花的功能，命令行已能满足基本需求。

**Independent Test**: 可以独立测试 Web 界面的配置和生成功能。

**Acceptance Scenarios**:

1. **Given** 开发者访问代码生成器 Web 界面, **When** 填写实体配置, **Then** 可以预览将要生成的代码
2. **Given** 配置完成, **When** 点击生成按钮, **Then** 代码生成到指定目录
3. **Given** 数据库连接配置, **When** 选择表, **Then** 自动填充字段配置

---

### Edge Cases

- 表名或字段名包含特殊字符时如何处理？（默认：转换为合法的 Java 标识符）
- 生成的文件已存在时如何处理？（默认：提示用户选择覆盖或跳过）
- 模块名与现有模块冲突时如何处理？（默认：提示用户修改模块名）

## Requirements *(mandatory)*

### Functional Requirements

**代码生成**
- **FR-001**: 系统 MUST 支持通过 YAML/JSON 配置文件定义实体结构
- **FR-002**: 系统 MUST 生成符合项目 DDD 分层架构的后端代码
- **FR-003**: 系统 MUST 生成符合项目前端规范的 Vue 3 代码
- **FR-004**: 系统 MUST 支持从 MySQL 数据库逆向读取表结构
- **FR-005**: 系统 MUST 支持批量生成多个实体的代码

**模板引擎**
- **FR-006**: 系统 MUST 使用 FreeMarker 作为模板引擎
- **FR-007**: 系统 MUST 提供默认模板集（Entity、Repository、Service、Controller、DTO、VO、Vue 页面）
- **FR-008**: 系统 MUST 支持用户自定义模板覆盖默认模板

**配置管理**
- **FR-009**: 系统 MUST 支持全局配置（包名、作者、日期格式等）
- **FR-010**: 系统 MUST 支持字段级配置（是否查询、是否列表显示、表单类型等）

**输出控制**
- **FR-011**: 系统 MUST 支持预览模式（不实际写入文件）
- **FR-012**: 系统 MUST 支持选择性生成（只生成后端/只生成前端/全部）
- **FR-013**: 系统 MUST 在生成前检查目标文件是否存在并提示用户

### Key Entities

- **GeneratorConfig（生成器配置）**: 全局配置，包含包名前缀、作者、模板路径、输出路径
- **EntityConfig（实体配置）**: 实体定义，包含表名、类名、模块名、字段列表
- **FieldConfig（字段配置）**: 字段定义，包含字段名、类型、注释、是否主键、是否查询、表单类型
- **Template（模板）**: 代码模板，包含模板名称、模板路径、输出路径模式

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 生成一个完整的 CRUD 模块（后端+前端）耗时不超过 30 秒
- **SC-002**: 生成的代码编译通过率 100%
- **SC-003**: 生成的代码符合项目 Checkstyle 规范
- **SC-004**: 新增一个 CRUD 功能，代码量减少 80% 以上（对比手写）
- **SC-005**: 开发者可以在 10 分钟内完成首次代码生成

## Assumptions

- 代码生成器作为独立的 Maven 模块（admin-generator）集成到项目中
- 默认模板基于现有项目代码模式设计
- 命令行工具优先实现，Web 界面作为可选功能
- 生成的代码需要开发者根据业务需求进行微调
