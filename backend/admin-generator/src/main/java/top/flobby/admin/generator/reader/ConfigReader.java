package top.flobby.admin.generator.reader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import top.flobby.admin.generator.config.GeneratorConfig;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 配置文件读取器
 */
public class ConfigReader {

    private static final Logger log = LoggerFactory.getLogger(ConfigReader.class);

    /**
     * 从 YAML 文件读取配置
     *
     * @param configPath 配置文件路径
     * @return 生成器配置
     */
    public GeneratorConfig readFromYaml(String configPath) {
        Path path = Paths.get(configPath);
        if (!Files.exists(path)) {
            throw new RuntimeException("配置文件不存在: " + configPath);
        }

        try (InputStream inputStream = new FileInputStream(configPath)) {
            Yaml yaml = new Yaml();
            GeneratorConfig config = yaml.loadAs(inputStream, GeneratorConfig.class);
            if (config == null) {
                config = new GeneratorConfig();
            }
            config.applyDefaults();
            log.info("成功读取配置文件: {}", configPath);
            return config;
        } catch (IOException e) {
            log.error("读取配置文件失败: {} - {}", configPath, e.getMessage());
            throw new RuntimeException("读取配置文件失败: " + configPath, e);
        }
    }

    /**
     * 从 classpath 读取默认配置
     *
     * @return 生成器配置
     */
    public GeneratorConfig readDefault() {
        try (InputStream inputStream = getClass().getClassLoader()
                .getResourceAsStream("generator.yml")) {
            if (inputStream == null) {
                log.warn("未找到默认配置文件，使用空配置");
                return new GeneratorConfig();
            }
            Yaml yaml = new Yaml();
            GeneratorConfig config = yaml.loadAs(inputStream, GeneratorConfig.class);
            if (config == null) {
                config = new GeneratorConfig();
            }
            config.applyDefaults();
            return config;
        } catch (IOException e) {
            log.error("读取默认配置失败: {}", e.getMessage());
            return new GeneratorConfig();
        }
    }

    /**
     * 验证配置
     *
     * @param config 配置对象
     */
    public void validate(GeneratorConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("配置不能为空");
        }
        if (config.getGlobal() == null) {
            throw new IllegalArgumentException("全局配置不能为空");
        }
        if (config.getGlobal().getPackageName() == null || config.getGlobal().getPackageName().isEmpty()) {
            throw new IllegalArgumentException("包名不能为空");
        }
        if (config.getGlobal().getModuleName() == null || config.getGlobal().getModuleName().isEmpty()) {
            throw new IllegalArgumentException("模块名不能为空");
        }
        if (config.getEntities() == null || config.getEntities().isEmpty()) {
            log.warn("未配置实体，将使用数据库逆向生成");
        }
    }
}
