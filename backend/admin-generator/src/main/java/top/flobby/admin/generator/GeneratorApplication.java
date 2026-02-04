package top.flobby.admin.generator;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import top.flobby.admin.generator.command.InteractiveCommand;

/**
 * 代码生成器命令行入口
 * <p>
 * 使用方式：
 * 1. IDE 中直接运行 main 方法（自动启动交互式向导）
 * 2. 命令行: java -jar admin-generator.jar
 * 3. 命令行: java -jar admin-generator.jar -c generator.yml
 *
 * @author Code Generator
 */
@Command(
        name = "admin-generator",
        description = "后台管理系统代码生成器",
        version = "1.0.0",
        mixinStandardHelpOptions = true
)
public class GeneratorApplication implements Runnable {

    public static void main(String[] args) {
        // 没有参数时，直接启动交互式模式
        if (args.length == 0) {
            InteractiveCommand interactive = new InteractiveCommand();
            System.exit(interactive.call());
        } else {
            // 有参数时，使用 picocli 解析
            int exitCode = new CommandLine(new InteractiveCommand())
                    .execute(args);
            System.exit(exitCode);
        }
    }

    @Override
    public void run() {
        // 直接启动交互式模式
        InteractiveCommand interactive = new InteractiveCommand();
        interactive.call();
    }
}
