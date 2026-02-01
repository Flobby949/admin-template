package top.flobby.admin.generator.command;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import top.flobby.admin.generator.config.GeneratorConfig;
import top.flobby.admin.generator.engine.CodeGenerator;
import top.flobby.admin.generator.reader.ConfigReader;

import java.io.File;
import java.util.concurrent.Callable;

/**
 * 代码生成命令
 */
@Command(
        name = "generate",
        aliases = {"gen", "g"},
        description = "从配置文件生成代码",
        mixinStandardHelpOptions = true
)
public class GenerateCommand implements Callable<Integer> {

    @Parameters(
            index = "0",
            description = "配置文件路径",
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
            names = {"--backend-only"},
            description = "只生成后端代码"
    )
    private boolean backendOnly;

    @Option(
            names = {"--frontend-only"},
            description = "只生成前端代码"
    )
    private boolean frontendOnly;

    @Override
    public Integer call() {
        try {
            System.out.println("========== 代码生成器 ==========");
            System.out.println("配置文件: " + configFile.getAbsolutePath());
            System.out.println("输出目录: " + outputDir.getAbsolutePath());

            // 读取配置
            ConfigReader configReader = new ConfigReader();
            GeneratorConfig config = configReader.readFromYaml(configFile.getAbsolutePath());

            // 应用命令行选项
            if (overwrite) {
                config.getOptions().setOverwrite(true);
            }
            if (backendOnly) {
                config.getOptions().setGenerateFrontend(false);
            }
            if (frontendOnly) {
                config.getOptions().setGenerateBackend(false);
            }

            // 验证配置
            configReader.validate(config);

            // 创建生成器
            CodeGenerator generator = new CodeGenerator(config, outputDir.getAbsolutePath());
            generator.setPreviewMode(preview);

            // 执行生成
            generator.generate();

            System.out.println("================================");
            System.out.println("代码生成完成！");
            return 0;
        } catch (Exception e) {
            System.err.println("错误: " + e.getMessage());
            e.printStackTrace();
            return 1;
        }
    }
}
