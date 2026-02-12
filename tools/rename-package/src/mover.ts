import * as fs from 'node:fs';
import * as path from 'node:path';
import { ChangeType } from './types.js';
import type { FileChange, PackageMapping } from './types.js';

/**
 * 递归查找所有包含旧包路径的 Java 源目录
 * 搜索 src/main/java/old/path 和 src/test/java/old/path
 */
function findOldPackageDirs(backendDir: string, mapping: PackageMapping): string[] {
  const results: string[] = [];
  const entries = fs.readdirSync(backendDir, { withFileTypes: true });

  for (const entry of entries) {
    if (!entry.isDirectory()) continue;

    const moduleDir = path.join(backendDir, entry.name);
    // 检查 src/main/java 和 src/test/java
    for (const srcType of ['src/main/java', 'src/test/java']) {
      const oldPkgDir = path.join(moduleDir, srcType, mapping.oldPath);
      if (fs.existsSync(oldPkgDir)) {
        results.push(oldPkgDir);
      }
    }
  }

  // 也检查根目录下直接的 src/main/java（如果存在）
  for (const srcType of ['src/main/java', 'src/test/java']) {
    const oldPkgDir = path.join(backendDir, srcType, mapping.oldPath);
    if (fs.existsSync(oldPkgDir)) {
      results.push(oldPkgDir);
    }
  }

  return results;
}

/**
 * 递归收集目录下所有文件（相对路径）
 */
function collectFiles(dir: string, basePath = ''): string[] {
  const results: string[] = [];
  const entries = fs.readdirSync(dir, { withFileTypes: true });

  for (const entry of entries) {
    const relativePath = basePath ? path.join(basePath, entry.name) : entry.name;
    if (entry.isDirectory()) {
      results.push(...collectFiles(path.join(dir, entry.name), relativePath));
    } else if (entry.isFile()) {
      results.push(relativePath);
    }
  }

  return results;
}

/**
 * 自底向上清理空目录
 * 从 startDir 开始向上清理，直到 stopDir（不含 stopDir）
 */
function cleanEmptyDirsBottomUp(startDir: string, stopDir: string): number {
  let cleaned = 0;
  let current = startDir;

  while (current !== stopDir && current.startsWith(stopDir)) {
    if (!fs.existsSync(current)) {
      current = path.dirname(current);
      continue;
    }

    try {
      const entries = fs.readdirSync(current);
      if (entries.length === 0) {
        fs.rmdirSync(current);
        cleaned++;
        current = path.dirname(current);
      } else {
        break;
      }
    } catch {
      break;
    }
  }

  return cleaned;
}

/**
 * 移动目录结构：从旧包路径迁移到新包路径
 * @param backendDir backend/ 目录的绝对路径
 * @param mapping 包名映射
 * @returns 变更记录
 */
export function moveDirectories(backendDir: string, mapping: PackageMapping): FileChange[] {
  const changes: FileChange[] = [];
  const oldPkgDirs = findOldPackageDirs(backendDir, mapping);

  for (const oldPkgDir of oldPkgDirs) {
    // 计算对应的新目录路径
    const parentDir = oldPkgDir.slice(0, oldPkgDir.length - mapping.oldPath.length);
    const newPkgDir = path.join(parentDir, mapping.newPath);

    // 收集旧目录中的所有 Java 文件
    const files = collectFiles(oldPkgDir);
    const javaFiles = files.filter(f => f.endsWith('.java'));

    for (const file of javaFiles) {
      const oldFilePath = path.join(oldPkgDir, file);
      const newFilePath = path.join(newPkgDir, file);

      // 创建新目录
      fs.mkdirSync(path.dirname(newFilePath), { recursive: true });

      // 移动文件
      fs.renameSync(oldFilePath, newFilePath);

      const relativeOld = path.relative(backendDir, oldFilePath);
      const relativeNew = path.relative(backendDir, newFilePath);

      changes.push({
        filePath: relativeNew,
        changeType: ChangeType.DIRECTORY_MOVE,
        description: `Move file`,
        before: relativeOld,
        after: relativeNew,
      });
    }

    // 清理旧包目录树中的空子目录
    cleanEmptyDirsFromTree(oldPkgDir, parentDir);
    // 向上清理旧包路径的父目录链（如 top/flobby → top）
    cleanEmptyDirsBottomUp(oldPkgDir, parentDir);
  }

  return changes;
}

/**
 * 清理整棵目录树中的空目录（自底向上）
 */
function cleanEmptyDirsFromTree(dir: string, stopDir: string): void {
  if (!fs.existsSync(dir)) return;

  try {
    const entries = fs.readdirSync(dir, { withFileTypes: true });

    // 先递归处理子目录
    for (const entry of entries) {
      if (entry.isDirectory()) {
        cleanEmptyDirsFromTree(path.join(dir, entry.name), stopDir);
      }
    }

    // 重新检查当前目录是否为空
    const remaining = fs.readdirSync(dir);
    if (remaining.length === 0 && dir !== stopDir) {
      fs.rmdirSync(dir);
    }
  } catch {
    // 忽略错误
  }
}
