import { ChangeType } from './types.js';
import type { FileChange, PackageMapping } from './types.js';

export interface ReplaceResult {
  newContent: string;
  changes: FileChange[];
}

/**
 * 替换 Java 文件中的 package 声明和 import 语句
 */
export function replaceJavaContent(content: string, mapping: PackageMapping, filePath = ''): ReplaceResult {
  const changes: FileChange[] = [];
  const lines = content.split('\n');
  const newLines: string[] = [];

  for (const line of lines) {
    let newLine = line;
    let changed = false;

    // 替换 package 声明: package top.flobby.admin 或 package top.flobby.admin.xxx
    const packageRegex = new RegExp(
      `^(\\s*package\\s+)${escapeRegex(mapping.oldPackage)}(\\b.*)$`
    );
    const packageMatch = line.match(packageRegex);
    if (packageMatch) {
      newLine = `${packageMatch[1]}${mapping.newPackage}${packageMatch[2]}`;
      changes.push({
        filePath,
        changeType: ChangeType.PACKAGE_DECLARATION,
        description: `Replace package declaration`,
        before: line.trim(),
        after: newLine.trim(),
      });
      changed = true;
    }

    // 替换 import 语句: import top.flobby.admin.xxx 或 import static top.flobby.admin.xxx
    if (!changed) {
      const importRegex = new RegExp(
        `^(\\s*import\\s+(?:static\\s+)?)${escapeRegex(mapping.oldPackage)}(\\b.*)$`
      );
      const importMatch = line.match(importRegex);
      if (importMatch) {
        newLine = `${importMatch[1]}${mapping.newPackage}${importMatch[2]}`;
        changes.push({
          filePath,
          changeType: ChangeType.IMPORT_STATEMENT,
          description: `Replace import statement`,
          before: line.trim(),
          after: newLine.trim(),
        });
      }
    }

    newLines.push(newLine);
  }

  return {
    newContent: newLines.join('\n'),
    changes,
  };
}

/**
 * 替换 pom.xml 中的 groupId 和 mainClass
 */
export function replacePomContent(content: string, mapping: PackageMapping, filePath = ''): ReplaceResult {
  const changes: FileChange[] = [];
  let newContent = content;

  // 替换 <groupId>oldGroupId</groupId> — 精确值匹配
  const groupIdRegex = new RegExp(
    `(<groupId>)${escapeRegex(mapping.oldGroupId)}(</groupId>)`,
    'g'
  );
  newContent = newContent.replace(groupIdRegex, (match, prefix, suffix) => {
    changes.push({
      filePath,
      changeType: ChangeType.GROUP_ID,
      description: `Replace groupId`,
      before: match,
      after: `${prefix}${mapping.newGroupId}${suffix}`,
    });
    return `${prefix}${mapping.newGroupId}${suffix}`;
  });

  // 替换 <mainClass>oldPackage.xxx</mainClass>
  const mainClassRegex = new RegExp(
    `(<mainClass>)${escapeRegex(mapping.oldPackage)}(\\b.*?)(</mainClass>)`,
    'g'
  );
  newContent = newContent.replace(mainClassRegex, (match, prefix, rest, suffix) => {
    const replacement = `${prefix}${mapping.newPackage}${rest}${suffix}`;
    changes.push({
      filePath,
      changeType: ChangeType.MAIN_CLASS,
      description: `Replace mainClass reference`,
      before: match,
      after: replacement,
    });
    return replacement;
  });

  return {
    newContent,
    changes,
  };
}

/**
 * 替换配置文件中的包名引用（纯文本替换）
 */
export function replaceConfigContent(content: string, mapping: PackageMapping, filePath = ''): ReplaceResult {
  const changes: FileChange[] = [];
  let newContent = content;

  // 全文本替换旧包名（包括子包引用如 top.flobby.admin.generator）
  const packageRegex = new RegExp(escapeRegex(mapping.oldPackage), 'g');
  newContent = newContent.replace(packageRegex, (match) => {
    changes.push({
      filePath,
      changeType: ChangeType.CONFIG_REFERENCE,
      description: `Replace config reference`,
      before: match,
      after: mapping.newPackage,
    });
    return mapping.newPackage;
  });

  return {
    newContent,
    changes,
  };
}

/**
 * 检测 Java 文件中字符串常量里的旧包名引用
 */
export function detectWarnings(content: string, filePath: string, mapping: PackageMapping) {
  const warnings: { filePath: string; line: number; content: string; reason: string }[] = [];
  const lines = content.split('\n');

  for (let i = 0; i < lines.length; i++) {
    const line = lines[i];
    // 跳过 package 和 import 声明（这些是正常替换目标）
    if (/^\s*(package|import)\s+/.test(line)) {
      continue;
    }
    // 检查字符串常量中的旧包名
    const stringLiteralRegex = new RegExp(`"[^"]*${escapeRegex(mapping.oldPackage)}[^"]*"`, 'g');
    let match;
    while ((match = stringLiteralRegex.exec(line)) !== null) {
      warnings.push({
        filePath,
        line: i + 1,
        content: line.trim(),
        reason: 'String literal contains old package name (may need manual review)',
      });
    }
  }

  return warnings;
}

/**
 * 转义正则表达式中的特殊字符
 */
function escapeRegex(str: string): string {
  return str.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}
