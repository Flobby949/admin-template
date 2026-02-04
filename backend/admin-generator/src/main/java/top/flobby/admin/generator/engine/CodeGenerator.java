package top.flobby.admin.generator.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.flobby.admin.generator.config.EntityConfig;
import top.flobby.admin.generator.config.GeneratorConfig;
import top.flobby.admin.generator.config.MenuConfig;
import top.flobby.admin.generator.writer.CodeWriter;

import java.util.HashMap;
import java.util.Map;

/**
 * 代码生成器核心类
 */
public class CodeGenerator {

    private static final Logger log = LoggerFactory.getLogger(CodeGenerator.class);

    private final TemplateEngine templateEngine;
    private final CodeWriter codeWriter;
    private final GeneratorConfig config;
    private boolean generateMenu = false;
    private int entityIndex = 0;

    public CodeGenerator(GeneratorConfig config, String projectRoot) {
        this.config = config;
        this.templateEngine = new TemplateEngine();
        this.codeWriter = new CodeWriter(projectRoot);

        // 设置自定义模板路径
        if (config.getGlobal().getTemplatePath() != null) {
            templateEngine.setCustomTemplatePath(config.getGlobal().getTemplatePath());
        }

        // 设置覆盖选项
        codeWriter.setOverwrite(config.getOptions().getOverwrite());
    }

    /**
     * 生成所有实体的代码
     */
    public void generate() {
        log.info("开始生成代码...");
        log.info("包名: {}", config.getGlobal().getPackageName());
        log.info("模块名: {}", config.getGlobal().getModuleName());

        entityIndex = 0;
        for (EntityConfig entity : config.getEntities()) {
            generateEntity(entity);
        }

        codeWriter.printReport();
    }

    /**
     * 生成单个实体的代码
     */
    public void generateEntity(EntityConfig entity) {
        log.info("生成实体: {} ({})", entity.getClassName(), entity.getTableName());

        // 构建数据模型
        Map<String, Object> dataModel = buildDataModel(entity);

        // 生成后端代码
        if (config.getOptions().getGenerateBackend()) {
            generateBackend(entity, dataModel);
        }

        // 生成前端代码
        if (config.getOptions().getGenerateFrontend()) {
            generateFrontend(entity, dataModel);
        }

        // 生成菜单SQL
        if (generateMenu || Boolean.TRUE.equals(config.getMenu().getEnabled())) {
            generateMenuSql(entity, dataModel, entityIndex);
        }

        entityIndex++;
    }

    /**
     * 生成菜单SQL
     */
    private void generateMenuSql(EntityConfig entity, Map<String, Object> dataModel, int index) {
        MenuConfig menu = config.getMenu();

        // 计算菜单ID和按钮ID
        Integer menuId = menu.getNextMenuId(index);
        Integer btnBaseId = menu.getNextBtnBaseId(index);
        Integer menuSort = menu.getNextMenuSort(index);

        if (menuId == null || btnBaseId == null) {
            log.warn("菜单配置不完整，跳过菜单SQL生成: {}", entity.getClassName());
            return;
        }

        // 添加菜单相关数据到模型
        dataModel.put("menu", menu);
        dataModel.put("menuId", menuId);
        dataModel.put("btnBaseId", btnBaseId);
        dataModel.put("menuSort", menuSort);

        String moduleName = entity.getModuleName();
        String classNameLower = entity.getClassNameLower();

        generateFile("sql/menu.ftl", dataModel,
                "admin-" + moduleName + "/src/main/resources/sql/menu_" + classNameLower + ".sql");
    }

    /**
     * 构建模板数据模型
     */
    private Map<String, Object> buildDataModel(EntityConfig entity) {
        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("packageName", config.getGlobal().getPackageName());
        dataModel.put("moduleName", entity.getModuleName());
        dataModel.put("author", config.getGlobal().getAuthor());
        dataModel.put("date", config.getGlobal().getFormattedDate());
        dataModel.put("entity", entity);
        dataModel.put("fields", entity.getFields());
        return dataModel;
    }

    /**
     * 生成后端代码
     */
    private void generateBackend(EntityConfig entity, Map<String, Object> dataModel) {
        String moduleName = entity.getModuleName();
        String className = entity.getClassName();
        String packagePath = config.getGlobal().getPackageName().replace(".", "/");

        // 基础路径（不包含 backend/ 前缀，因为 projectRoot 已经指向 backend 目录）
        String basePath = String.format("admin-%s/src/main/java/%s/%s",
                moduleName, packagePath, moduleName);

        // Entity
        generateFile("backend/entity.ftl", dataModel,
                basePath + "/domain/entity/" + className + ".java");

        // Repository 接口
        generateFile("backend/repository.ftl", dataModel,
                basePath + "/domain/repository/" + className + "Repository.java");

        // JPA Repository
        generateFile("backend/jpa-repository.ftl", dataModel,
                basePath + "/infrastructure/repository/Jpa" + className + "Repository.java");

        // Repository 实现
        if (config.getOptions().getGenerateRepositoryImpl()) {
            generateFile("backend/repository-impl.ftl", dataModel,
                    basePath + "/infrastructure/repository/" + className + "RepositoryImpl.java");
        }

        // Service
        generateFile("backend/service.ftl", dataModel,
                basePath + "/application/" + className + "Service.java");

        // Controller
        generateFile("backend/controller.ftl", dataModel,
                basePath + "/interfaces/controller/" + className + "Controller.java");

        // DTO
        generateFile("backend/dto.ftl", dataModel,
                basePath + "/interfaces/dto/" + className + "DTO.java");

        // VO
        generateFile("backend/vo.ftl", dataModel,
                basePath + "/interfaces/vo/" + className + "VO.java");

        // Query
        if (config.getOptions().getGenerateQuery() && entity.hasQueryFields()) {
            generateFile("backend/query.ftl", dataModel,
                    basePath + "/interfaces/query/" + className + "Query.java");
        }
    }

    /**
     * 生成前端代码
     */
    private void generateFrontend(EntityConfig entity, Map<String, Object> dataModel) {
        String moduleName = entity.getModuleName();
        String className = entity.getClassName();
        String classNameLower = entity.getClassNameLower();

        // API（相对于 backend 目录，前端在上一级的 frontend 目录）
        generateFile("frontend/api.ftl", dataModel,
                "../frontend/src/api/" + classNameLower + ".ts");

        // 列表页面
        generateFile("frontend/index.ftl", dataModel,
                "../frontend/src/views/" + moduleName + "/" + classNameLower + "/index.vue");

        // 对话框组件
        generateFile("frontend/dialog.ftl", dataModel,
                "../frontend/src/views/" + moduleName + "/" + classNameLower + "/components/" + className + "Dialog.vue");
    }

    /**
     * 生成单个文件
     */
    private void generateFile(String templateName, Map<String, Object> dataModel, String outputPath) {
        try {
            String content = templateEngine.render(templateName, dataModel);
            codeWriter.write(outputPath, content);
        } catch (Exception e) {
            log.error("生成文件失败: {} - {}", outputPath, e.getMessage());
        }
    }

    /**
     * 设置预览模式
     */
    public void setPreviewMode(boolean previewMode) {
        codeWriter.setPreviewMode(previewMode);
    }

    /**
     * 获取代码写入器
     */
    public CodeWriter getCodeWriter() {
        return codeWriter;
    }

    /**
     * 设置是否生成菜单SQL
     */
    public void setGenerateMenu(boolean generateMenu) {
        this.generateMenu = generateMenu;
    }
}
