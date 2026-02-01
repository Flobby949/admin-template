package top.flobby.admin.generator;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import top.flobby.admin.generator.command.GenerateCommand;
import top.flobby.admin.generator.command.ReverseCommand;

/**
 * 代码生成器命令行入口
 *
 * @author Code Generator
 */
@Command(
        name = "admin-generator",
        description = "后台管理系统代码生成器",
        version = "1.0.0",
        mixinStandardHelpOptions = true,
        subcommands = {
                GenerateCommand.class,
                ReverseCommand.class
        }
)
public class GeneratorApplication implements Runnable {

    public static void main(String[] args) {
        int exitCode = new CommandLine(new GeneratorApplication())
                .execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        // 没有子命令时显示帮助
        CommandLine.usage(this, System.out);
    }
}
