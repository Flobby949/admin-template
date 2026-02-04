package top.flobby.admin.generator.engine;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

/**
 * FreeMarker 模板引擎封装
 */
public class TemplateEngine {

    private static final Logger log = LoggerFactory.getLogger(TemplateEngine.class);

    private final Configuration configuration;
    private String customTemplatePath;

    public TemplateEngine() {
        this.configuration = new Configuration(Configuration.VERSION_2_3_32);
        this.configuration.setDefaultEncoding("UTF-8");
        this.configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        this.configuration.setLogTemplateExceptions(false);
        this.configuration.setWrapUncheckedExceptions(true);
        this.configuration.setFallbackOnNullLoopVariable(false);

        // 默认从 classpath 加载模板
        this.configuration.setClassLoaderForTemplateLoading(
                getClass().getClassLoader(), "templates");
    }

    /**
     * 设置自定义模板目录
     *
     * @param templatePath 自定义模板目录路径
     */
    public void setCustomTemplatePath(String templatePath) {
        this.customTemplatePath = templatePath;
        if (templatePath != null && !templatePath.isEmpty()) {
            try {
                File templateDir = new File(templatePath);
                if (templateDir.exists() && templateDir.isDirectory()) {
                    this.configuration.setDirectoryForTemplateLoading(templateDir);
                    log.info("使用自定义模板目录: {}", templatePath);
                } else {
                    log.warn("自定义模板目录不存在，使用默认模板: {}", templatePath);
                }
            } catch (IOException e) {
                log.error("设置自定义模板目录失败: {}", e.getMessage());
            }
        }
    }

    /**
     * 渲染模板
     *
     * @param templateName 模板名称（如 backend/entity.ftl）
     * @param dataModel    数据模型
     * @return 渲染后的内容
     */
    public String render(String templateName, Map<String, Object> dataModel) {
        try {
            Template template = configuration.getTemplate(templateName);
            StringWriter writer = new StringWriter();
            template.process(dataModel, writer);
            return writer.toString();
        } catch (IOException e) {
            log.error("加载模板失败: {} - {}", templateName, e.getMessage());
            throw new RuntimeException("加载模板失败: " + templateName, e);
        } catch (TemplateException e) {
            log.error("渲染模板失败: {} - {}", templateName, e.getMessage());
            throw new RuntimeException("渲染模板失败: " + templateName, e);
        }
    }

    /**
     * 验证模板语法
     *
     * @param templateName 模板名称
     * @return 是否有效
     */
    public boolean validateTemplate(String templateName) {
        try {
            configuration.getTemplate(templateName);
            return true;
        } catch (IOException e) {
            log.error("模板验证失败: {} - {}", templateName, e.getMessage());
            return false;
        }
    }

    /**
     * 获取模板
     *
     * @param templateName 模板名称
     * @return 模板对象
     */
    public Template getTemplate(String templateName) throws IOException {
        return configuration.getTemplate(templateName);
    }

    /**
     * 获取自定义模板路径
     */
    public String getCustomTemplatePath() {
        return customTemplatePath;
    }

    /**
     * 重置为默认模板
     */
    public void resetToDefault() {
        this.customTemplatePath = null;
        this.configuration.setClassLoaderForTemplateLoading(
                getClass().getClassLoader(), "templates");
        log.info("已重置为默认模板");
    }
}
