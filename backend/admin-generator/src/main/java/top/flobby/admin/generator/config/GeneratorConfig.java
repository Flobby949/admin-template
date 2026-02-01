package top.flobby.admin.generator.config;

import lombok.Data;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 代码生成器全局配置
 */
@Data
public class GeneratorConfig {

    /**
     * 全局配置
     */
    private GlobalConfig global = new GlobalConfig();

    /**
     * 数据库配置
     */
    private DatabaseConfig database = new DatabaseConfig();

    /**
     * 实体配置列表
     */
    private List<EntityConfig> entities = new ArrayList<>();

    /**
     * 生成选项
     */
    private OptionsConfig options = new OptionsConfig();

    /**
     * 全局配置
     */
    @Data
    public static class GlobalConfig {
        /**
         * 包名前缀
         */
        private String packageName = "top.flobby.admin";

        /**
         * 模块名
         */
        private String moduleName = "demo";

        /**
         * 作者
         */
        private String author = "Code Generator";

        /**
         * 日期格式
         */
        private String dateFormat = "yyyy-MM-dd";

        /**
         * 后端输出路径
         */
        private String backendOutputPath = "backend/admin-${moduleName}/src/main/java";

        /**
         * 前端输出路径
         */
        private String frontendOutputPath = "frontend/src";

        /**
         * 自定义模板目录
         */
        private String templatePath;

        /**
         * 表前缀（生成类名时会去除）
         */
        private String tablePrefix = "";

        /**
         * 获取格式化的日期
         */
        public String getFormattedDate() {
            return LocalDate.now().format(DateTimeFormatter.ofPattern(dateFormat));
        }

        /**
         * 获取实际的后端输出路径
         */
        public String getActualBackendOutputPath() {
            return backendOutputPath.replace("${moduleName}", moduleName);
        }
    }

    /**
     * 数据库配置
     */
    @Data
    public static class DatabaseConfig {
        /**
         * 数据库 URL
         */
        private String url;

        /**
         * 用户名
         */
        private String username;

        /**
         * 密码
         */
        private String password;

        /**
         * 要生成的表（支持通配符）
         */
        private List<String> tables = new ArrayList<>();
    }

    /**
     * 生成选项
     */
    @Data
    public static class OptionsConfig {
        /**
         * 是否生成后端代码
         */
        private Boolean generateBackend = true;

        /**
         * 是否生成前端代码
         */
        private Boolean generateFrontend = true;

        /**
         * 是否覆盖已存在的文件
         */
        private Boolean overwrite = false;

        /**
         * 是否生成 Repository 实现类
         */
        private Boolean generateRepositoryImpl = true;

        /**
         * 是否生成 Query 查询对象
         */
        private Boolean generateQuery = true;
    }

    /**
     * 为实体设置默认模块名
     */
    public void applyDefaults() {
        for (EntityConfig entity : entities) {
            if (entity.getModuleName() == null || entity.getModuleName().isEmpty()) {
                entity.setModuleName(global.getModuleName());
            }
        }
    }
}
