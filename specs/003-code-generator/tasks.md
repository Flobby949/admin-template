# Tasks: 代码生成器

**Input**: Design documents from `/specs/003-code-generator/`
**Prerequisites**: plan.md (required), spec.md (required for user stories)

**Tests**: 未明确要求测试，本任务列表不包含测试任务。

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

- **Backend**: `backend/admin-generator/src/main/java/top/flobby/admin/generator/`
- **Templates**: `backend/admin-generator/src/main/resources/templates/`
- **Frontend**: `frontend/src/views/tool/generator/`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: 创建 admin-generator Maven 模块，配置依赖

- [ ] T001 创建 admin-generator 模块目录结构 in `backend/admin-generator/`
- [ ] T002 创建 admin-generator 模块 pom.xml，添加 FreeMarker、Picocli、SnakeYAML、MySQL Connector 依赖 in `backend/admin-generator/pom.xml`
- [ ] T003 在父 pom.xml 中添加 admin-generator 模块 in `backend/pom.xml`
- [ ] T004 [P] 创建默认配置文件示例 in `backend/admin-generator/src/main/resources/generator.yml`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: 核心基础设施，所有用户故事都依赖这些组件

**⚠️ CRITICAL**: 必须完成此阶段才能开始用户故事实现

### 配置类

- [ ] T005 [P] 创建 FieldConfig 字段配置类 in `backend/admin-generator/src/main/java/top/flobby/admin/generator/config/FieldConfig.java`
- [ ] T006 [P] 创建 EntityConfig 实体配置类 in `backend/admin-generator/src/main/java/top/flobby/admin/generator/config/EntityConfig.java`
- [ ] T007 [P] 创建 GeneratorConfig 全局配置类 in `backend/admin-generator/src/main/java/top/flobby/admin/generator/config/GeneratorConfig.java`

### 工具类

- [ ] T008 [P] 创建 NameConverter 命名转换工具类（下划线转驼峰、驼峰转下划线等）in `backend/admin-generator/src/main/java/top/flobby/admin/generator/util/NameConverter.java`
- [ ] T009 [P] 创建 TypeMapper 类型映射工具类（MySQL 类型到 Java 类型）in `backend/admin-generator/src/main/java/top/flobby/admin/generator/util/TypeMapper.java`

### 模板引擎

- [ ] T010 创建 TemplateEngine 模板引擎封装类 in `backend/admin-generator/src/main/java/top/flobby/admin/generator/engine/TemplateEngine.java`

### 代码输出

- [ ] T011 创建 CodeWriter 代码文件写入类 in `backend/admin-generator/src/main/java/top/flobby/admin/generator/writer/CodeWriter.java`

**Checkpoint**: 基础设施就绪，可以开始用户故事实现

---

## Phase 3: User Story 1 - 基础 CRUD 代码生成 (Priority: P1) 🎯 MVP

**Goal**: 通过配置文件生成完整的后端和前端 CRUD 代码

**Independent Test**: 创建配置文件，执行生成命令，验证生成的代码可编译运行

### 配置读取

- [ ] T012 [US1] 创建 ConfigReader 配置文件读取类（支持 YAML/JSON）in `backend/admin-generator/src/main/java/top/flobby/admin/generator/reader/ConfigReader.java`

### 后端模板

- [ ] T013 [P] [US1] 创建 Entity 实体类模板 in `backend/admin-generator/src/main/resources/templates/backend/entity.ftl`
- [ ] T014 [P] [US1] 创建 Repository 接口模板 in `backend/admin-generator/src/main/resources/templates/backend/repository.ftl`
- [ ] T015 [P] [US1] 创建 JpaRepository 接口模板 in `backend/admin-generator/src/main/resources/templates/backend/jpa-repository.ftl`
- [ ] T016 [P] [US1] 创建 RepositoryImpl 实现类模板 in `backend/admin-generator/src/main/resources/templates/backend/repository-impl.ftl`
- [ ] T017 [P] [US1] 创建 Service 服务类模板 in `backend/admin-generator/src/main/resources/templates/backend/service.ftl`
- [ ] T018 [P] [US1] 创建 Controller 控制器模板 in `backend/admin-generator/src/main/resources/templates/backend/controller.ftl`
- [ ] T019 [P] [US1] 创建 DTO 数据传输对象模板 in `backend/admin-generator/src/main/resources/templates/backend/dto.ftl`
- [ ] T020 [P] [US1] 创建 VO 视图对象模板 in `backend/admin-generator/src/main/resources/templates/backend/vo.ftl`
- [ ] T021 [P] [US1] 创建 Query 查询对象模板 in `backend/admin-generator/src/main/resources/templates/backend/query.ftl`

### 前端模板

- [ ] T022 [P] [US1] 创建 API 接口模板 in `backend/admin-generator/src/main/resources/templates/frontend/api.ftl`
- [ ] T023 [P] [US1] 创建列表页面模板 in `backend/admin-generator/src/main/resources/templates/frontend/index.ftl`
- [ ] T024 [P] [US1] 创建表单对话框组件模板 in `backend/admin-generator/src/main/resources/templates/frontend/dialog.ftl`

### 代码生成器核心

- [ ] T025 [US1] 创建 CodeGenerator 代码生成器核心类 in `backend/admin-generator/src/main/java/top/flobby/admin/generator/engine/CodeGenerator.java`

### 命令行入口

- [ ] T026 [US1] 创建 GeneratorApplication 命令行入口类（使用 Picocli）in `backend/admin-generator/src/main/java/top/flobby/admin/generator/GeneratorApplication.java`
- [ ] T027 [US1] 创建 generate 子命令（从配置文件生成代码）in `backend/admin-generator/src/main/java/top/flobby/admin/generator/command/GenerateCommand.java`

**Checkpoint**: 可以通过配置文件生成完整的 CRUD 代码

---

## Phase 4: User Story 2 - 数据库逆向生成 (Priority: P1)

**Goal**: 从现有数据库表结构逆向生成代码

**Independent Test**: 连接数据库，指定表名，验证能正确读取表结构并生成代码

### 数据库元数据读取

- [ ] T028 [US2] 创建 DatabaseReader 数据库元数据读取类 in `backend/admin-generator/src/main/java/top/flobby/admin/generator/reader/DatabaseReader.java`
- [ ] T029 [US2] 实现 MySQL 表结构读取（表名、字段、类型、注释、主键、索引）in `backend/admin-generator/src/main/java/top/flobby/admin/generator/reader/DatabaseReader.java`
- [ ] T030 [US2] 实现表结构到 EntityConfig 的转换 in `backend/admin-generator/src/main/java/top/flobby/admin/generator/reader/DatabaseReader.java`

### 命令行扩展

- [ ] T031 [US2] 创建 reverse 子命令（从数据库逆向生成）in `backend/admin-generator/src/main/java/top/flobby/admin/generator/command/ReverseCommand.java`
- [ ] T032 [US2] 支持批量选择多个表生成 in `backend/admin-generator/src/main/java/top/flobby/admin/generator/command/ReverseCommand.java`

**Checkpoint**: 可以从数据库表逆向生成 CRUD 代码

---

## Phase 5: User Story 3 - 模板自定义 (Priority: P2)

**Goal**: 支持用户自定义代码生成模板

**Independent Test**: 创建自定义模板，验证生成器使用自定义模板生成代码

### 模板管理

- [ ] T033 [US3] 扩展 TemplateEngine 支持自定义模板目录 in `backend/admin-generator/src/main/java/top/flobby/admin/generator/engine/TemplateEngine.java`
- [ ] T034 [US3] 实现模板优先级（自定义模板 > 默认模板）in `backend/admin-generator/src/main/java/top/flobby/admin/generator/engine/TemplateEngine.java`
- [ ] T035 [US3] 添加模板语法校验和错误提示 in `backend/admin-generator/src/main/java/top/flobby/admin/generator/engine/TemplateEngine.java`

### 命令行扩展

- [ ] T036 [US3] 创建 template 子命令（列出可用模板）in `backend/admin-generator/src/main/java/top/flobby/admin/generator/command/TemplateCommand.java`
- [ ] T037 [US3] 支持导出默认模板到指定目录 in `backend/admin-generator/src/main/java/top/flobby/admin/generator/command/TemplateCommand.java`

**Checkpoint**: 可以使用自定义模板生成代码

---

## Phase 6: User Story 4 - Web 界面生成 (Priority: P3)

**Goal**: 提供 Web 界面进行代码生成配置和操作

**Independent Test**: 访问 Web 界面，配置实体，预览并生成代码

### 后端 API

- [ ] T038 [P] [US4] 创建 GeneratorController 代码生成器 API in `backend/admin-system/src/main/java/top/flobby/admin/system/interfaces/controller/GeneratorController.java`
- [ ] T039 [P] [US4] 创建 GeneratorService 代码生成器服务 in `backend/admin-system/src/main/java/top/flobby/admin/system/application/GeneratorService.java`
- [ ] T040 [US4] 实现获取数据库表列表 API in `backend/admin-system/src/main/java/top/flobby/admin/system/interfaces/controller/GeneratorController.java`
- [ ] T041 [US4] 实现获取表字段信息 API in `backend/admin-system/src/main/java/top/flobby/admin/system/interfaces/controller/GeneratorController.java`
- [ ] T042 [US4] 实现代码预览 API in `backend/admin-system/src/main/java/top/flobby/admin/system/interfaces/controller/GeneratorController.java`
- [ ] T043 [US4] 实现代码生成 API in `backend/admin-system/src/main/java/top/flobby/admin/system/interfaces/controller/GeneratorController.java`

### 前端页面

- [ ] T044 [P] [US4] 创建代码生成器 API 接口 in `frontend/src/api/generator.ts`
- [ ] T045 [US4] 创建代码生成器列表页面 in `frontend/src/views/tool/generator/index.vue`
- [ ] T046 [US4] 创建代码生成配置对话框 in `frontend/src/views/tool/generator/components/GeneratorDialog.vue`
- [ ] T047 [US4] 创建代码预览对话框 in `frontend/src/views/tool/generator/components/PreviewDialog.vue`
- [ ] T048 [US4] 添加代码生成器菜单配置 in `backend/admin-boot/src/main/resources/data.sql`

**Checkpoint**: 可以通过 Web 界面配置和生成代码

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: 完善和优化

- [ ] T049 [P] 创建代码生成器使用文档 in `backend/admin-generator/README.md`
- [ ] T050 [P] 创建配置文件示例和说明 in `backend/admin-generator/src/main/resources/examples/`
- [ ] T051 添加生成前文件存在检查和用户确认 in `backend/admin-generator/src/main/java/top/flobby/admin/generator/writer/CodeWriter.java`
- [ ] T052 添加生成日志和进度显示 in `backend/admin-generator/src/main/java/top/flobby/admin/generator/engine/CodeGenerator.java`
- [ ] T053 支持预览模式（不实际写入文件）in `backend/admin-generator/src/main/java/top/flobby/admin/generator/engine/CodeGenerator.java`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Story 1 (Phase 3)**: Depends on Foundational - MVP 核心功能
- **User Story 2 (Phase 4)**: Depends on Foundational - 可与 US1 并行
- **User Story 3 (Phase 5)**: Depends on US1 完成（需要模板引擎基础）
- **User Story 4 (Phase 6)**: Depends on US1 + US2 完成（需要生成器核心功能）
- **Polish (Phase 7)**: Depends on all user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: 核心功能，无依赖其他故事
- **User Story 2 (P1)**: 可与 US1 并行开发，共享基础设施
- **User Story 3 (P2)**: 依赖 US1 的模板引擎基础
- **User Story 4 (P3)**: 依赖 US1 + US2 的生成器核心功能

### Within Each User Story

- 配置类/工具类优先
- 模板文件可并行创建
- 核心逻辑依赖配置和模板
- 命令行/API 最后实现

### Parallel Opportunities

- Phase 2 中所有配置类和工具类可并行（T005-T009）
- Phase 3 中所有模板文件可并行（T013-T024）
- Phase 6 中后端 API 和前端 API 接口可并行（T038-T039, T044）

---

## Parallel Example: User Story 1 Templates

```bash
# Launch all backend templates together:
Task: "创建 Entity 实体类模板 in templates/backend/entity.ftl"
Task: "创建 Repository 接口模板 in templates/backend/repository.ftl"
Task: "创建 Service 服务类模板 in templates/backend/service.ftl"
Task: "创建 Controller 控制器模板 in templates/backend/controller.ftl"
Task: "创建 DTO 数据传输对象模板 in templates/backend/dto.ftl"
Task: "创建 VO 视图对象模板 in templates/backend/vo.ftl"

# Launch all frontend templates together:
Task: "创建 API 接口模板 in templates/frontend/api.ftl"
Task: "创建列表页面模板 in templates/frontend/index.ftl"
Task: "创建表单对话框组件模板 in templates/frontend/dialog.ftl"
```

---

## Implementation Strategy

### MVP First (User Story 1 + 2)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational
3. Complete Phase 3: User Story 1 (配置文件生成)
4. Complete Phase 4: User Story 2 (数据库逆向生成)
5. **STOP and VALIDATE**: 测试命令行工具可正常生成代码
6. 可交付使用

### Incremental Delivery

1. Setup + Foundational → 基础设施就绪
2. User Story 1 → 配置文件生成 → 可用 (MVP!)
3. User Story 2 → 数据库逆向生成 → 更实用
4. User Story 3 → 模板自定义 → 更灵活
5. User Story 4 → Web 界面 → 更易用

---

## Summary

| 指标 | 数值 |
|------|------|
| 总任务数 | 53 |
| Phase 1 (Setup) | 4 |
| Phase 2 (Foundational) | 7 |
| Phase 3 (US1 - 基础生成) | 16 |
| Phase 4 (US2 - 逆向生成) | 5 |
| Phase 5 (US3 - 模板自定义) | 5 |
| Phase 6 (US4 - Web 界面) | 11 |
| Phase 7 (Polish) | 5 |
| 可并行任务 | 25 |
| MVP 范围 | Phase 1-4 (32 tasks) |

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- 建议先完成 MVP（US1 + US2），再根据需要实现 US3 和 US4
- 模板文件是核心，需要参考现有代码模式精心设计
- 命令行工具优先，Web 界面作为增强功能
