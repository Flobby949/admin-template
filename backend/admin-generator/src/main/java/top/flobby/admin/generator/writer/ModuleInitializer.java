package top.flobby.admin.generator.writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 模块初始化器
 * 负责创建新模块的 pom.xml 并注册到父 pom
 */
public class ModuleInitializer {

    private static final Logger log = LoggerFactory.getLogger(ModuleInitializer.class);

    private final String projectRoot;

    public ModuleInitializer(String projectRoot) {
        this.projectRoot = projectRoot;
    }

    /**
     * 初始化模块
     * 如果模块不存在，创建 pom.xml 并注册到父 pom
     *
     * @param moduleName 模块名（如 demo）
     * @return 是否成功
     */
    public boolean initModule(String moduleName) {
        String moduleDir = "admin-" + moduleName;
        Path modulePath = Paths.get(projectRoot, moduleDir);
        Path pomPath = modulePath.resolve("pom.xml");

        // 检查模块 pom.xml 是否存在
        if (Files.exists(pomPath)) {
            log.debug("模块 {} 已存在，跳过初始化", moduleName);
            return true;
        }

        log.info("检测到新模块 {}，开始初始化...", moduleName);

        // 创建模块目录
        try {
            Files.createDirectories(modulePath);
        } catch (IOException e) {
            log.error("创建模块目录失败: {}", e.getMessage());
            return false;
        }

        // 创建 pom.xml
        if (!createModulePom(moduleName, pomPath)) {
            return false;
        }

        // 注册到父 pom
        if (!registerToParentPom(moduleName)) {
            return false;
        }

        log.info("模块 {} 初始化完成", moduleName);
        return true;
    }

    /**
     * 创建模块 pom.xml
     */
    private boolean createModulePom(String moduleName, Path pomPath) {
        String pomContent = generatePomContent(moduleName);
        try {
            Files.writeString(pomPath, pomContent, StandardCharsets.UTF_8);
            log.info("创建模块 pom.xml: admin-{}/pom.xml", moduleName);
            return true;
        } catch (IOException e) {
            log.error("创建 pom.xml 失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 生成 pom.xml 内容
     */
    private String generatePomContent(String moduleName) {
        String moduleNameCapitalized = capitalize(moduleName);
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0"
                         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
                    <modelVersion>4.0.0</modelVersion>

                    <parent>
                        <groupId>top.flobby</groupId>
                        <artifactId>admin-parent</artifactId>
                        <version>1.0.0-SNAPSHOT</version>
                    </parent>

                    <artifactId>admin-%s</artifactId>
                    <packaging>jar</packaging>

                    <name>Admin %s</name>
                    <description>%s模块</description>

                    <dependencies>
                        <dependency>
                            <groupId>top.flobby</groupId>
                            <artifactId>admin-common</artifactId>
                            <version>${project.version}</version>
                        </dependency>
                        <dependency>
                            <groupId>top.flobby</groupId>
                            <artifactId>admin-shared-kernel</artifactId>
                            <version>${project.version}</version>
                        </dependency>
                        <dependency>
                            <groupId>org.springframework.boot</groupId>
                            <artifactId>spring-boot-starter-data-jpa</artifactId>
                        </dependency>
                        <dependency>
                            <groupId>org.springframework.boot</groupId>
                            <artifactId>spring-boot-starter-security</artifactId>
                        </dependency>
                        <dependency>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                            <optional>true</optional>
                        </dependency>
                        <dependency>
                            <groupId>org.springframework.boot</groupId>
                            <artifactId>spring-boot-starter-test</artifactId>
                            <scope>test</scope>
                        </dependency>
                    </dependencies>

                    <build>
                        <plugins>
                            <plugin>
                                <groupId>org.apache.maven.plugins</groupId>
                                <artifactId>maven-compiler-plugin</artifactId>
                                <configuration>
                                    <annotationProcessorPaths>
                                        <path>
                                            <groupId>org.projectlombok</groupId>
                                            <artifactId>lombok</artifactId>
                                        </path>
                                    </annotationProcessorPaths>
                                </configuration>
                            </plugin>
                        </plugins>
                    </build>
                </project>
                """.formatted(moduleName, moduleNameCapitalized, moduleNameCapitalized);
    }

    /**
     * 注册模块到父 pom.xml
     */
    private boolean registerToParentPom(String moduleName) {
        Path parentPomPath = Paths.get(projectRoot, "pom.xml");
        if (!Files.exists(parentPomPath)) {
            log.error("父 pom.xml 不存在: {}", parentPomPath);
            return false;
        }

        try {
            String content = Files.readString(parentPomPath, StandardCharsets.UTF_8);
            String moduleEntry = "admin-" + moduleName;

            // 检查是否已注册
            if (content.contains("<module>" + moduleEntry + "</module>")) {
                log.debug("模块 {} 已在父 pom 中注册", moduleName);
                return true;
            }

            // 在 admin-generator 之前插入新模块
            Pattern pattern = Pattern.compile("(<module>admin-generator</module>)");
            Matcher matcher = pattern.matcher(content);
            if (matcher.find()) {
                String newContent = matcher.replaceFirst(
                        "<module>" + moduleEntry + "</module>\n        $1"
                );
                Files.writeString(parentPomPath, newContent, StandardCharsets.UTF_8);
                log.info("已将模块 {} 注册到父 pom.xml", moduleName);
                return true;
            } else {
                log.warn("无法在父 pom.xml 中找到插入位置，请手动添加模块");
                return false;
            }
        } catch (IOException e) {
            log.error("更新父 pom.xml 失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 首字母大写
     */
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
