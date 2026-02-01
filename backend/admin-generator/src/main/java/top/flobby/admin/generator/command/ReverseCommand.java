package top.flobby.admin.generator.command;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import top.flobby.admin.generator.config.EntityConfig;
import top.flobby.admin.generator.config.GeneratorConfig;
import top.flobby.admin.generator.engine.CodeGenerator;
import top.flobby.admin.generator.reader.ConfigReader;
import top.flobby.admin.generator.reader.DatabaseReader;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * 数据库逆向生成命令
 */
@Command(
        name = "reverse",
        aliases = {"rev", "r"},
        description = "从数据库表逆向生成代码",
        mixinStandardHelpOptions = true
)
public class ReverseCommand implements Callable<Integer> {

    @Parameters(
            description = "要生成的表名（支持通配符 *，多个表用逗号分隔）",
            defaultValue = ""
    )
    private String tables;

    @Option(
            names = {"-c", "--config"},
            description = "配置文件路径（用于数据库连接信息）",
            defaultValue = "generator.yml"
    )
    private File configFile;

    @Option(
            names = {"-o", "--output"},
            description = "输出目录（项目根目录）",
            defaultValue = "."
    )
    private File outputDir;

    @Option(
            names = {"-m", "--module"},
            description = "模块名",
            defaultValue = "demo"
    )
    private String moduleName;

    @Option(
            names = {"-p", "--preview"},
            description = "预览模式（不实际生成文件）"
    )
    private boolean preview;

    @Option(
            names = {"--overwrite"},
            description = "覆盖已存在的文件"
    )
    private boolean overwrite;

    @Option(
            names = {"--list"},
            description = "列出所有表"
    )
    private boolean listTables;

    @Option(
            names = {"--url"},
            description = "数据库 URL"
    )
    private String dbUrl;

    @Option(
            names = {"--username", "-u"},
            description = "数据库用户名"
    )
    private String dbUsername;

    @Option(
            names = {"--password", "-P"},
            description = "数据库密码"
    )
    private String dbPassword;

    @Option(
            names = {"--table-prefix", "-t"},
            description = "表前缀（生成类名时去除，多个前缀用逗号分隔，如 t_,sys_）"
    )
    private String tablePrefix;

    @Override
    public Integer call() {
        try {
            System.out.println("========== 数据库逆向生成 ==========");

            // 读取配置
            GeneratorConfig config;
            if (configFile.exists()) {
                ConfigReader configReader = new ConfigReader();
                config = configReader.readFromYaml(configFile.getAbsolutePath());
            } else {
                config = new GeneratorConfig();
            }

            // 命令行参数覆盖配置文件
            if (dbUrl != null) {
                config.getDatabase().setUrl(dbUrl);
            }
            if (dbUsername != null) {
                config.getDatabase().setUsername(dbUsername);
            }
            if (dbPassword != null) {
                config.getDatabase().setPassword(dbPassword);
            }
            if (moduleName != null) {
                config.getGlobal().setModuleName(moduleName);
            }
            if (overwrite) {
                config.getOptions().setOverwrite(true);
            }
            if (tablePrefix != null) {
                config.getGlobal().setTablePrefix(tablePrefix);
            }

            // 验证数据库配置
            if (config.getDatabase().getUrl() == null || config.getDatabase().getUrl().isEmpty()) {
                System.err.println("错误: 未配置数据库连接信息");
                System.err.println("请在配置文件中配置 database.url/username/password，或使用 --url/--username/--password 参数");
                return 1;
            }

            // 创建数据库读取器
            DatabaseReader dbReader = new DatabaseReader(
                    config.getDatabase(),
                    config.getGlobal().getTablePrefix()
            );

            // 测试连接
            System.out.println("测试数据库连接...");
            if (!dbReader.testConnection()) {
                System.err.println("错误: 数据库连接失败");
                return 1;
            }
            System.out.println("数据库连接成功！");

            // 列出所有表
            if (listTables) {
                System.out.println("\n可用的表:");
                List<String> allTables = dbReader.getAllTables();
                for (String table : allTables) {
                    System.out.println("  - " + table);
                }
                System.out.println("\n共 " + allTables.size() + " 个表");
                return 0;
            }

            // 获取要生成的表
            List<String> tablePatterns;
            if (tables != null && !tables.isEmpty()) {
                tablePatterns = Arrays.asList(tables.split(","));
            } else if (config.getDatabase().getTables() != null && !config.getDatabase().getTables().isEmpty()) {
                tablePatterns = config.getDatabase().getTables();
            } else {
                System.err.println("错误: 未指定要生成的表");
                System.err.println("请使用参数指定表名，或在配置文件中配置 database.tables");
                return 1;
            }

            // 匹配表名
            List<String> matchedTables = dbReader.getMatchingTables(tablePatterns);
            if (matchedTables.isEmpty()) {
                System.err.println("错误: 没有匹配的表");
                return 1;
            }

            System.out.println("\n将生成以下表的代码:");
            for (String table : matchedTables) {
                System.out.println("  - " + table);
            }
            System.out.println();

            // 读取表结构
            List<EntityConfig> entities = dbReader.readTables(matchedTables, config.getGlobal().getModuleName());
            config.setEntities(entities);

            // 创建生成器
            CodeGenerator generator = new CodeGenerator(config, outputDir.getAbsolutePath());
            generator.setPreviewMode(preview);

            // 执行生成
            generator.generate();

            System.out.println("====================================");
            System.out.println("代码生成完成！");
            return 0;
        } catch (Exception e) {
            System.err.println("错误: " + e.getMessage());
            e.printStackTrace();
            return 1;
        }
    }
}
