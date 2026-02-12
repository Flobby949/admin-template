import * as fs from 'node:fs';
import * as path from 'node:path';

/** 需要排除的目录名 */
const EXCLUDED_DIRS = new Set(['target', '.idea', 'node_modules']);

/** 需要收集的文件扩展名 */
const INCLUDED_EXTENSIONS = new Set(['.java', '.xml', '.yml', '.yaml', '.properties']);

/** 需要排除的文件扩展名 */
const EXCLUDED_EXTENSIONS = new Set(['.ftl']);

/**
 * 判断文件是否应该被收集
 */
function shouldIncludeFile(fileName: string): boolean {
  if (EXCLUDED_EXTENSIONS.has(path.extname(fileName))) {
    return false;
  }

  // pom.xml 是特殊的 .xml 文件，也需要收集
  if (fileName === 'pom.xml') {
    return true;
  }

  return INCLUDED_EXTENSIONS.has(path.extname(fileName));
}

/**
 * 递归扫描目录，收集需要处理的文件
 * @param baseDir 根目录（backend/ 的绝对路径）
 * @param currentDir 当前扫描的目录（绝对路径）
 * @param results 收集的文件列表（相对于 baseDir 的路径）
 */
function scanRecursive(baseDir: string, currentDir: string, results: string[]): void {
  let entries: fs.Dirent[];
  try {
    entries = fs.readdirSync(currentDir, { withFileTypes: true });
  } catch {
    // 目录不可读，跳过
    return;
  }

  for (const entry of entries) {
    if (entry.isDirectory()) {
      if (!EXCLUDED_DIRS.has(entry.name)) {
        scanRecursive(baseDir, path.join(currentDir, entry.name), results);
      }
    } else if (entry.isFile()) {
      if (shouldIncludeFile(entry.name)) {
        const relativePath = path.relative(baseDir, path.join(currentDir, entry.name));
        results.push(relativePath);
      }
    }
  }
}

/**
 * 扫描 backend 目录，收集所有需要处理的文件
 * @param backendDir backend/ 目录的绝对路径
 * @returns 相对于 backendDir 的文件路径列表
 */
export function scanBackendDirectory(backendDir: string): string[] {
  const results: string[] = [];
  scanRecursive(backendDir, backendDir, results);
  return results.sort();
}
