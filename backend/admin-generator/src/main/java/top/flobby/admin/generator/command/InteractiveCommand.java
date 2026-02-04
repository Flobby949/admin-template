package top.flobby.admin.generator.command;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import top.flobby.admin.generator.config.EntityConfig;
import top.flobby.admin.generator.config.GeneratorConfig;
import top.flobby.admin.generator.config.MenuConfig;
import top.flobby.admin.generator.engine.CodeGenerator;
import top.flobby.admin.generator.reader.ConfigReader;
import top.flobby.admin.generator.reader.DatabaseReader;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Callable;

/**
 * 交互式代码生成命令
 */
@Command(
        name = "admin-generator",
        description = "交互式配置并生成代码",
        mixinStandardHelpOptions = true
)
public class InteractiveCommand implements Callable<Integer> {

    @Option(
            names = {"-c", "--config"},
            description = "配置文件路径（用于数据库连接信息）"
    )
    private File configFile;

    private final Scanner scanner = new Scanner(System.in);
    private GeneratorConfig config;
    private DatabaseReader dbReader;

    /**
     * 自动搜索配置文件的路径列表
     */
    private static final String[] CONFIG_SEARCH_PATHS = {
            "generator.yml",
            "src/main/resources/generator.yml",
            "admin-generator/src/main/resources/generator.yml",
            "../admin-generator/src/main/resources/generator.yml",
            "backend/admin-generator/src/main/resources/generator.yml"
    };

    @Override
    public Integer call() {
        try {
            printBanner();

            // 自动搜索配置文件
            if (configFile == null) {
                configFile = findConfigFile();
            }

            // 步骤1: 加载或配置数据库连接
            if (!setupDatabase()) {
                return 1;
            }

            // 步骤2: 选择要生成的表
            List<String> selectedTables = selectTables();
            if (selectedTables.isEmpty()) {
                System.out.println("未选择任何表，退出。");
                return 0;
            }

            // 步骤3: 配置模块信息
            configureModule();

            // 步骤4: 配置生成选项
            configureOptions();

            // 步骤5: 配置菜单SQL（可选）
            configureMenu();

            // 步骤6: 配置输出目录
            String outputDir = configureOutput();

            // 步骤7: 确认并生成
            if (!confirmAndGenerate(selectedTables, outputDir)) {
                System.out.println("已取消生成。");
                return 0;
            }

            return 0;
        } catch (Exception e) {
            System.err.println("错误: " + e.getMessage());
            e.printStackTrace();
            return 1;
        }
    }

    private void printBanner() {
        System.out.println();
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║           Admin Code Generator - 交互式配置向导            ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        System.out.println();
    }

    private boolean setupDatabase() {
        System.out.println("【步骤 1/7】数据库配置");
        System.out.println("─────────────────────────────────────────");

        // 尝试加载配置文件
        if (configFile.exists()) {
            System.out.println("检测到配置文件: " + configFile.getAbsolutePath());
            System.out.print("是否使用该配置文件? [Y/n]: ");
            String answer = scanner.nextLine().trim();
            if (answer.isEmpty() || answer.equalsIgnoreCase("y") || answer.equalsIgnoreCase("yes")) {
                ConfigReader configReader = new ConfigReader();
                config = configReader.readFromYaml(configFile.getAbsolutePath());
            }
        }

        if (config == null) {
            config = new GeneratorConfig();
        }

        // 如果没有数据库配置，手动输入
        if (config.getDatabase().getUrl() == null || config.getDatabase().getUrl().isEmpty()) {
            System.out.println("\n请输入数据库连接信息:");
            System.out.print("  数据库URL: ");
            config.getDatabase().setUrl(scanner.nextLine().trim());
            System.out.print("  用户名: ");
            config.getDatabase().setUsername(scanner.nextLine().trim());
            System.out.print("  密码: ");
            config.getDatabase().setPassword(scanner.nextLine().trim());
        } else {
            System.out.println("使用配置文件中的数据库连接: " + maskUrl(config.getDatabase().getUrl()));
        }

        // 测试连接
        System.out.println("\n测试数据库连接...");
        dbReader = new DatabaseReader(config.getDatabase(), config.getGlobal().getTablePrefix());
        if (!dbReader.testConnection()) {
            System.err.println("数据库连接失败！请检查配置。");
            return false;
        }
        System.out.println("数据库连接成功！");
        System.out.println();
        return true;
    }

    private List<String> selectTables() {
        System.out.println("【步骤 2/7】选择要生成的表");
        System.out.println("─────────────────────────────────────────");

        List<String> allTables = dbReader.getAllTables();
        System.out.println("数据库中共有 " + allTables.size() + " 个表:\n");

        // 显示表列表
        for (int i = 0; i < allTables.size(); i++) {
            System.out.printf("  [%2d] %s%n", i + 1, allTables.get(i));
        }

        System.out.println("\n选择方式:");
        System.out.println("  - 输入序号（多个用逗号分隔，如: 1,3,5）");
        System.out.println("  - 输入表名或通配符（如: demo_* 或 demo_product,demo_order）");
        System.out.println("  - 输入 'all' 选择所有表");
        System.out.println("  - 输入 'q' 退出");
        System.out.print("\n请选择: ");

        String input = scanner.nextLine().trim();
        if (input.equalsIgnoreCase("q")) {
            return new ArrayList<>();
        }

        List<String> selectedTables = new ArrayList<>();

        if (input.equalsIgnoreCase("all")) {
            selectedTables.addAll(allTables);
        } else if (input.matches("^[\\d,\\s]+$")) {
            // 按序号选择
            String[] indices = input.split(",");
            for (String idx : indices) {
                int index = Integer.parseInt(idx.trim()) - 1;
                if (index >= 0 && index < allTables.size()) {
                    selectedTables.add(allTables.get(index));
                }
            }
        } else {
            // 按表名或通配符选择
            String[] patterns = input.split(",");
            List<String> patternList = new ArrayList<>();
            for (String p : patterns) {
                patternList.add(p.trim());
            }
            selectedTables = dbReader.getMatchingTables(patternList);
        }

        if (!selectedTables.isEmpty()) {
            System.out.println("\n已选择 " + selectedTables.size() + " 个表:");
            for (String table : selectedTables) {
                System.out.println("  - " + table);
            }
        }
        System.out.println();
        return selectedTables;
    }

    private void configureModule() {
        System.out.println("【步骤 3/7】模块配置");
        System.out.println("─────────────────────────────────────────");

        System.out.print("模块名 [" + config.getGlobal().getModuleName() + "]: ");
        String moduleName = scanner.nextLine().trim();
        if (!moduleName.isEmpty()) {
            config.getGlobal().setModuleName(moduleName);
        }

        System.out.print("包名前缀 [" + config.getGlobal().getPackageName() + "]: ");
        String packageName = scanner.nextLine().trim();
        if (!packageName.isEmpty()) {
            config.getGlobal().setPackageName(packageName);
        }

        System.out.print("作者 [" + config.getGlobal().getAuthor() + "]: ");
        String author = scanner.nextLine().trim();
        if (!author.isEmpty()) {
            config.getGlobal().setAuthor(author);
        }

        String currentPrefix = config.getGlobal().getTablePrefix();
        System.out.print("表前缀（生成类名时去除）[" + (currentPrefix.isEmpty() ? "无" : currentPrefix) + "]: ");
        String tablePrefix = scanner.nextLine().trim();
        if (!tablePrefix.isEmpty()) {
            config.getGlobal().setTablePrefix(tablePrefix);
        }
        System.out.println();
    }

    private void configureOptions() {
        System.out.println("【步骤 4/7】生成选项");
        System.out.println("─────────────────────────────────────────");

        System.out.print("生成后端代码? [Y/n]: ");
        String backend = scanner.nextLine().trim();
        config.getOptions().setGenerateBackend(
                backend.isEmpty() || backend.equalsIgnoreCase("y") || backend.equalsIgnoreCase("yes")
        );

        System.out.print("生成前端代码? [Y/n]: ");
        String frontend = scanner.nextLine().trim();
        config.getOptions().setGenerateFrontend(
                frontend.isEmpty() || frontend.equalsIgnoreCase("y") || frontend.equalsIgnoreCase("yes")
        );

        System.out.print("覆盖已存在的文件? [y/N]: ");
        String overwrite = scanner.nextLine().trim();
        config.getOptions().setOverwrite(
                overwrite.equalsIgnoreCase("y") || overwrite.equalsIgnoreCase("yes")
        );
        System.out.println();
    }

    private void configureMenu() {
        System.out.println("【步骤 5/7】菜单权限SQL配置");
        System.out.println("─────────────────────────────────────────");

        System.out.print("是否生成菜单权限SQL? [y/N]: ");
        String genMenu = scanner.nextLine().trim();
        boolean generateMenu = genMenu.equalsIgnoreCase("y") || genMenu.equalsIgnoreCase("yes");

        if (!generateMenu) {
            config.getMenu().setEnabled(false);
            System.out.println();
            return;
        }

        config.getMenu().setEnabled(true);
        MenuConfig menu = config.getMenu();

        System.out.print("是否创建一级目录（新模块需要）? [y/N]: ");
        String createDir = scanner.nextLine().trim();
        menu.setCreateDirectory(createDir.equalsIgnoreCase("y") || createDir.equalsIgnoreCase("yes"));

        System.out.print("一级目录ID: ");
        String dirIdStr = scanner.nextLine().trim();
        if (!dirIdStr.isEmpty()) {
            menu.setDirId(Integer.parseInt(dirIdStr));
        }

        if (Boolean.TRUE.equals(menu.getCreateDirectory())) {
            System.out.print("一级目录名称: ");
            String dirName = scanner.nextLine().trim();
            if (!dirName.isEmpty()) {
                menu.setDirName(dirName);
            }

            System.out.print("一级目录图标 [Folder]: ");
            String dirIcon = scanner.nextLine().trim();
            if (!dirIcon.isEmpty()) {
                menu.setDirIcon(dirIcon);
            }

            System.out.print("一级目录排序 [10]: ");
            String dirSort = scanner.nextLine().trim();
            if (!dirSort.isEmpty()) {
                menu.setDirSort(Integer.parseInt(dirSort));
            }
        }

        System.out.print("二级菜单起始ID: ");
        String menuIdStart = scanner.nextLine().trim();
        if (!menuIdStart.isEmpty()) {
            menu.setMenuIdStart(Integer.parseInt(menuIdStart));
        }

        System.out.print("菜单图标 [List]: ");
        String menuIcon = scanner.nextLine().trim();
        if (!menuIcon.isEmpty()) {
            menu.setMenuIcon(menuIcon);
        }

        System.out.println();
    }

    private String configureOutput() {
        System.out.println("【步骤 6/7】输出配置");
        System.out.println("─────────────────────────────────────────");

        System.out.print("输出目录（项目根目录）[.]: ");
        String outputDir = scanner.nextLine().trim();
        if (outputDir.isEmpty()) {
            outputDir = ".";
        }

        System.out.print("预览模式（不实际生成文件）? [y/N]: ");
        String preview = scanner.nextLine().trim();
        boolean previewMode = preview.equalsIgnoreCase("y") || preview.equalsIgnoreCase("yes");
        if (previewMode) {
            System.out.println("  [预览模式已启用]");
        }

        System.out.println();
        return outputDir + (previewMode ? ":preview" : "");
    }

    private boolean confirmAndGenerate(List<String> selectedTables, String outputConfig) {
        System.out.println("【步骤 7/7】确认配置");
        System.out.println("─────────────────────────────────────────");

        boolean previewMode = outputConfig.endsWith(":preview");
        String outputDir = previewMode ? outputConfig.replace(":preview", "") : outputConfig;

        System.out.println("配置摘要:");
        System.out.println("  模块名: " + config.getGlobal().getModuleName());
        System.out.println("  包名: " + config.getGlobal().getPackageName());
        System.out.println("  作者: " + config.getGlobal().getAuthor());
        System.out.println("  表前缀: " + (config.getGlobal().getTablePrefix().isEmpty() ? "无" : config.getGlobal().getTablePrefix()));
        System.out.println("  生成后端: " + (config.getOptions().getGenerateBackend() ? "是" : "否"));
        System.out.println("  生成前端: " + (config.getOptions().getGenerateFrontend() ? "是" : "否"));
        System.out.println("  覆盖文件: " + (config.getOptions().getOverwrite() ? "是" : "否"));
        System.out.println("  生成菜单SQL: " + (config.getMenu().getEnabled() ? "是" : "否"));
        if (Boolean.TRUE.equals(config.getMenu().getEnabled())) {
            System.out.println("    - 创建一级目录: " + (config.getMenu().getCreateDirectory() ? "是" : "否"));
            System.out.println("    - 目录ID: " + config.getMenu().getDirId());
            if (Boolean.TRUE.equals(config.getMenu().getCreateDirectory())) {
                System.out.println("    - 目录名称: " + config.getMenu().getDirName());
            }
            System.out.println("    - 菜单起始ID: " + config.getMenu().getMenuIdStart());
        }
        System.out.println("  输出目录: " + new File(outputDir).getAbsolutePath());
        System.out.println("  预览模式: " + (previewMode ? "是" : "否"));
        System.out.println("  选择的表: " + selectedTables.size() + " 个");
        for (String table : selectedTables) {
            System.out.println("    - " + table);
        }

        System.out.print("\n确认生成? [Y/n]: ");
        String confirm = scanner.nextLine().trim();
        if (!confirm.isEmpty() && !confirm.equalsIgnoreCase("y") && !confirm.equalsIgnoreCase("yes")) {
            return false;
        }

        System.out.println("\n开始生成代码...\n");

        // 读取表结构
        List<EntityConfig> entities = dbReader.readTables(selectedTables, config.getGlobal().getModuleName());
        config.setEntities(entities);

        // 创建生成器
        CodeGenerator generator = new CodeGenerator(config, new File(outputDir).getAbsolutePath());
        generator.setPreviewMode(previewMode);
        if (Boolean.TRUE.equals(config.getMenu().getEnabled())) {
            generator.setGenerateMenu(true);
        }

        // 执行生成
        generator.generate();

        System.out.println("\n════════════════════════════════════════");
        System.out.println("代码生成完成！");
        System.out.println("════════════════════════════════════════");

        return true;
    }

    private String maskUrl(String url) {
        // 隐藏密码部分
        return url.replaceAll("password=[^&]*", "password=***");
    }

    /**
     * 自动搜索配置文件
     */
    private File findConfigFile() {
        // 从当前目录和常见位置搜索配置文件
        String userDir = System.getProperty("user.dir");

        for (String searchPath : CONFIG_SEARCH_PATHS) {
            Path path = Paths.get(userDir, searchPath);
            File file = path.toFile();
            if (file.exists() && file.isFile()) {
                return file;
            }
        }

        // 没找到，返回默认路径（可能不存在）
        return new File("generator.yml");
    }
}
