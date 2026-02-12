import * as fs from 'node:fs';
import * as path from 'node:path';
import * as readline from 'node:readline';
import { execSync } from 'node:child_process';
import { validateAndCreateMapping } from './validator.js';
import { scanBackendDirectory } from './scanner.js';
import { replaceJavaContent, replacePomContent, replaceConfigContent, detectWarnings } from './replacer.js';
import { moveDirectories } from './mover.js';
import { printPreview, printExecutionReport } from './reporter.js';
import { runInteractiveMode } from './prompt.js';
import { ChangeType } from './types.js';
import type { ChangeReport, FileChange, Warning, Stats } from './types.js';

/**
 * 解析命令行参数
 */
function parseArgs(argv: string[]): {
  oldPackage: string;
  newPackage: string;
  dryRun: boolean;
  yes: boolean;
  verify: boolean;
  help: boolean;
} {
  const args = argv.slice(2);
  const flags = {
    dryRun: false,
    yes: false,
    verify: false,
    help: false,
  };
  const positional: string[] = [];

  for (const arg of args) {
    switch (arg) {
      case '--dry-run':
        flags.dryRun = true;
        break;
      case '--yes':
        flags.yes = true;
        break;
      case '--verify':
        flags.verify = true;
        break;
      case '--help':
      case '-h':
        flags.help = true;
        break;
      default:
        if (arg.startsWith('-')) {
          console.error(`Error: Unknown option: ${arg}\n`);
          printUsage();
          process.exit(1);
        }
        positional.push(arg);
    }
  }

  return {
    oldPackage: positional[0] || '',
    newPackage: positional[1] || '',
    ...flags,
  };
}

/**
 * 交互式确认
 */
function confirm(question: string): Promise<boolean> {
  const rl = readline.createInterface({
    input: process.stdin,
    output: process.stdout,
  });

  return new Promise((resolve) => {
    rl.question(question, (answer) => {
      rl.close();
      const normalized = answer.trim().toLowerCase();
      resolve(normalized === 'y' || normalized === 'yes' || normalized === '');
    });
  });
}

/**
 * 查找 backend/ 目录
 */
function resolveBackendDir(): string | null {
  let dir = process.cwd();
  for (let i = 0; i < 10; i++) {
    const backendPath = path.join(dir, 'backend');
    if (fs.existsSync(backendPath) && fs.statSync(backendPath).isDirectory()) {
      return backendPath;
    }
    const parent = path.dirname(dir);
    if (parent === dir) break;
    dir = parent;
  }
  return null;
}

/**
 * 判断文件是否为配置文件（非 pom.xml 的 .xml, .yml, .yaml, .properties）
 */
function isConfigFile(file: string): boolean {
  if (file.endsWith('pom.xml')) return false;
  return (
    file.endsWith('.yml') ||
    file.endsWith('.yaml') ||
    file.endsWith('.xml') ||
    file.endsWith('.properties')
  );
}

/**
 * 构建统计摘要
 */
function buildStats(changes: FileChange[], warnings: Warning[]): Stats {
  const javaFileSet = new Set<string>();
  const pomFileSet = new Set<string>();
  const configFileSet = new Set<string>();
  let directoriesMoved = 0;

  for (const change of changes) {
    switch (change.changeType) {
      case ChangeType.PACKAGE_DECLARATION:
      case ChangeType.IMPORT_STATEMENT:
        javaFileSet.add(change.filePath);
        break;
      case ChangeType.GROUP_ID:
      case ChangeType.MAIN_CLASS:
        pomFileSet.add(change.filePath);
        break;
      case ChangeType.CONFIG_REFERENCE:
        configFileSet.add(change.filePath);
        break;
      case ChangeType.DIRECTORY_MOVE:
        directoriesMoved++;
        break;
    }
  }

  return {
    javaFiles: javaFileSet.size,
    pomFiles: pomFileSet.size,
    configFiles: configFileSet.size,
    directoriesMoved,
    directoriesCleaned: 0, // updated after execution
    warningCount: warnings.length,
  };
}

/**
 * 主流程
 */
async function main(): Promise<void> {
  const parsed = parseArgs(process.argv);

  // --help
  if (parsed.help) {
    printHelp();
    process.exit(0);
  }

  let oldPackage = parsed.oldPackage;
  let newPackage = parsed.newPackage;
  let dryRun = parsed.dryRun;
  let verify = parsed.verify;
  let yes = parsed.yes;
  let groupIdOverrides: { oldGroupId?: string; newGroupId?: string } | undefined;

  // No positional args → interactive mode
  if (!oldPackage && !newPackage) {
    const config = await runInteractiveMode();
    oldPackage = config.oldPackage;
    newPackage = config.newPackage;
    groupIdOverrides = { oldGroupId: config.oldGroupId, newGroupId: config.newGroupId };
    dryRun = dryRun || config.dryRun;
    verify = verify || config.verify;
    // Interactive mode already confirmed input, skip confirm prompt
    yes = true;
  }

  // 参数校验
  if (!oldPackage || !newPackage) {
    console.error('Error: Both old-package and new-package arguments are required.\n');
    printUsage();
    process.exit(1);
  }

  // 验证包名
  const validation = validateAndCreateMapping(oldPackage, newPackage, groupIdOverrides);
  if (validation.error) {
    console.error(`Error: ${validation.error}\n`);
    printUsage();
    process.exit(1);
  }

  const mapping = validation.mapping!;

  // 确定 backend 目录
  const backendDir = resolveBackendDir();
  if (!backendDir) {
    console.error('Error: Cannot find backend/ directory. Run this tool from the project root.');
    process.exit(2);
  }

  // 扫描文件
  const files = scanBackendDirectory(backendDir);
  if (files.length === 0) {
    console.log('No files found in backend/ directory.');
    process.exit(0);
  }

  // 分析所有变更
  const allChanges: FileChange[] = [];
  const allWarnings: Warning[] = [];
  const fileContents = new Map<string, string>();

  for (const file of files) {
    const fullPath = path.join(backendDir, file);
    const content = fs.readFileSync(fullPath, 'utf-8');

    if (file.endsWith('.java')) {
      // Java: package + import 替换
      const result = replaceJavaContent(content, mapping, file);
      if (result.changes.length > 0) {
        fileContents.set(file, result.newContent);
        allChanges.push(...result.changes);
      }
      // Warning 检测：字符串常量中的旧包名
      const warnings = detectWarnings(content, file, mapping);
      allWarnings.push(...warnings);
    } else if (file.endsWith('pom.xml')) {
      // pom.xml: groupId + mainClass 替换
      const result = replacePomContent(content, mapping, file);
      if (result.changes.length > 0) {
        fileContents.set(file, result.newContent);
        allChanges.push(...result.changes);
      }
    } else if (isConfigFile(file)) {
      // 配置文件: 纯文本替换
      const result = replaceConfigContent(content, mapping, file);
      if (result.changes.length > 0) {
        fileContents.set(file, result.newContent);
        allChanges.push(...result.changes);
      }
    }
  }

  // 构建报告
  const stats = buildStats(allChanges, allWarnings);
  const report: ChangeReport = {
    mapping,
    changes: allChanges,
    warnings: allWarnings,
    stats,
    success: false,
  };

  // 打印预览
  printPreview(report);

  // dry-run: 仅预览
  if (dryRun) {
    console.log('\n(dry-run mode — no changes made)');
    process.exit(0);
  }

  // 确认
  if (!yes) {
    const confirmed = await confirm('\nProceed with changes? [Y/n] ');
    if (!confirmed) {
      console.log('Cancelled.');
      process.exit(0);
    }
  }

  // 执行
  try {
    // 写入文件内容变更
    for (const [file, newContent] of fileContents) {
      const fullPath = path.join(backendDir, file);
      fs.writeFileSync(fullPath, newContent, 'utf-8');
    }

    // 执行目录迁移
    const moveChanges = moveDirectories(backendDir, mapping);
    allChanges.push(...moveChanges);
    stats.directoriesMoved = moveChanges.length;

    report.success = true;
  } catch (err) {
    console.error(`\nError during execution: ${err instanceof Error ? err.message : String(err)}`);
    console.error('Some files may have been partially modified. Check git diff for details.');
    process.exit(2);
  }

  // 打印执行报告
  printExecutionReport(report);

  // --verify: 编译验证
  if (verify) {
    console.log('\nRunning compile verification (mvnd compile)...');
    try {
      execSync('mvnd compile', {
        cwd: backendDir,
        stdio: 'inherit',
      });
      console.log('\nCompile verification: PASSED');
    } catch {
      console.error('\nCompile verification: FAILED');
      process.exit(3);
    }
  }

  process.exit(0);
}

function printHelp(): void {
  console.log('Package Rename Tool');
  console.log('\nRename Java package names in a Maven multi-module project.\n');
  printUsage();
  console.log('\n  Running without arguments enters interactive mode.');
  console.log('\nArguments:');
  console.log('  old-package  Current package name (e.g., top.flobby.admin)');
  console.log('  new-package  Target package name (e.g., com.example.demo)');
  console.log('\nOptions:');
  console.log('  --dry-run    Preview changes without modifying files');
  console.log('  --yes        Skip confirmation, execute directly');
  console.log('  --verify     Run mvnd compile after execution');
  console.log('  --help, -h   Show this help message');
  console.log('\nExit codes:');
  console.log('  0  Success (including dry-run and cancel)');
  console.log('  1  Argument error');
  console.log('  2  File operation error');
  console.log('  3  Compile verification failed');
  console.log('\nExamples:');
  console.log('  # Interactive mode (prompts for input step by step):');
  console.log('  npx tsx tools/rename-package/src/index.ts');
  console.log('');
  console.log('  # CLI mode:');
  console.log('  npx tsx tools/rename-package/src/index.ts top.flobby.admin com.example.demo --dry-run');
  console.log('  npx tsx tools/rename-package/src/index.ts top.flobby.admin com.example.demo --yes');
  console.log('  npx tsx tools/rename-package/src/index.ts top.flobby.admin com.example.demo --yes --verify');
}

function printUsage(): void {
  console.log('Usage: npx tsx tools/rename-package/src/index.ts <old-package> <new-package> [options]');
}

main();
