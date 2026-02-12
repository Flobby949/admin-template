import type { ChangeReport, FileChange } from './types.js';
import { ChangeType } from './types.js';

// ANSI colors
const BOLD = '\x1b[1m';
const RESET = '\x1b[0m';
const GREEN = '\x1b[32m';
const YELLOW = '\x1b[33m';
const CYAN = '\x1b[36m';
const DIM = '\x1b[2m';

const TAG_COLORS: Record<string, string> = {
  PACKAGE: '\x1b[34m',   // blue
  IMPORT: '\x1b[36m',    // cyan
  GROUPID: '\x1b[35m',   // magenta
  MAINCLASS: '\x1b[35m', // magenta
  CONFIG: '\x1b[33m',    // yellow
  MOVE: '\x1b[32m',      // green
};

function changeTypeTag(change: FileChange): string {
  switch (change.changeType) {
    case ChangeType.PACKAGE_DECLARATION: return 'PACKAGE';
    case ChangeType.IMPORT_STATEMENT: return 'IMPORT';
    case ChangeType.GROUP_ID: return 'GROUPID';
    case ChangeType.MAIN_CLASS: return 'MAINCLASS';
    case ChangeType.CONFIG_REFERENCE: return 'CONFIG';
    case ChangeType.DIRECTORY_MOVE: return 'MOVE';
    default: return 'OTHER';
  }
}

/**
 * 打印变更预览摘要（执行前）
 */
export function printPreview(report: ChangeReport): void {
  const { mapping, changes, warnings, stats } = report;

  console.log(`\n${BOLD}Package Rename Tool${RESET}\n`);
  console.log(`${BOLD}Mapping:${RESET}`);
  console.log(`  Package:   ${CYAN}${mapping.oldPackage}${RESET} -> ${GREEN}${mapping.newPackage}${RESET}`);
  console.log(`  GroupId:   ${CYAN}${mapping.oldGroupId}${RESET} -> ${GREEN}${mapping.newGroupId}${RESET}`);
  console.log(`  Directory: ${CYAN}${mapping.oldPath}${RESET} -> ${GREEN}${mapping.newPath}${RESET}`);

  console.log(`\n${BOLD}Changes Summary:${RESET}`);
  console.log(`  Java files (package/import):  ${stats.javaFiles}`);
  console.log(`  pom.xml (groupId/mainClass):  ${stats.pomFiles}`);
  console.log(`  Config files:                 ${stats.configFiles}`);
  console.log(`  Directories to move:          ${stats.directoriesMoved}`);

  if (warnings.length > 0) {
    console.log(`\n${YELLOW}${BOLD}Warnings (require manual review): ${warnings.length}${RESET}`);
    for (const w of warnings) {
      console.log(`  ${DIM}[${w.filePath}:${w.line}]${RESET} ${w.content}`);
      console.log(`    ${YELLOW}${w.reason}${RESET}`);
    }
  }
}

/**
 * 打印执行报告（执行后）
 */
export function printExecutionReport(report: ChangeReport): void {
  const { changes, stats, warnings } = report;

  console.log(`\n${GREEN}${BOLD}Execution Complete${RESET}\n`);

  // 按文件分组，去重
  const fileSet = new Set<string>();
  for (const change of changes) {
    const tag = changeTypeTag(change);
    const color = TAG_COLORS[tag] || '';
    if (change.changeType === ChangeType.DIRECTORY_MOVE) {
      console.log(`  ${color}[${tag}]${RESET}     ${change.before} -> ${change.after}`);
    } else {
      if (!fileSet.has(`${tag}:${change.filePath}`)) {
        console.log(`  ${color}[${tag}]${RESET}  ${change.filePath}`);
        fileSet.add(`${tag}:${change.filePath}`);
      }
    }
  }

  console.log(`\n${BOLD}Stats:${RESET}`);
  console.log(`  Java files:    ${stats.javaFiles}`);
  console.log(`  pom.xml:       ${stats.pomFiles}`);
  console.log(`  Config files:  ${stats.configFiles}`);
  console.log(`  Dirs moved:    ${stats.directoriesMoved}`);
  console.log(`  Dirs cleaned:  ${stats.directoriesCleaned}`);
  console.log(`  Warnings:      ${stats.warningCount}`);

  if (warnings.length > 0) {
    console.log(`\n${YELLOW}${BOLD}Warnings:${RESET}`);
    for (const w of warnings) {
      console.log(`  ${DIM}[${w.filePath}:${w.line}]${RESET} ${w.content}`);
      console.log(`    ${YELLOW}${w.reason}${RESET}`);
    }
  }
}
