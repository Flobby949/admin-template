import type { PackageMapping } from './types.js';

/**
 * Java 包名格式正则：全小写，以字母开头，用 . 分隔，每段以字母开头，至少 2 段
 */
const PACKAGE_NAME_REGEX = /^[a-z][a-z0-9]*(\.[a-z][a-z0-9]*)+$/;

export interface ValidationResult {
  mapping?: PackageMapping;
  error?: string;
}

/**
 * 验证包名格式
 */
function validatePackageName(name: string, label: string): string | undefined {
  if (!name) {
    return `${label} cannot be empty`;
  }
  if (!PACKAGE_NAME_REGEX.test(name)) {
    return `${label} "${name}" is not a valid Java package name. Must be lowercase, dot-separated, each segment starting with a letter, at least 2 segments. Example: com.example.app`;
  }
  return undefined;
}

/**
 * 从包名推导 groupId（去掉最后一段）
 * top.flobby.admin → top.flobby
 */
export function deriveGroupId(packageName: string): string {
  const parts = packageName.split('.');
  return parts.slice(0, -1).join('.');
}

/**
 * 将包名转换为目录路径
 * top.flobby.admin → top/flobby/admin
 */
function packageToPath(packageName: string): string {
  return packageName.replace(/\./g, '/');
}

/**
 * 验证输入并创建 PackageMapping
 * 可选传入自定义 groupId 覆盖自动推导值
 */
export function validateAndCreateMapping(
  oldPackage: string,
  newPackage: string,
  overrides?: { oldGroupId?: string; newGroupId?: string },
): ValidationResult {
  const oldError = validatePackageName(oldPackage, 'Old package name');
  if (oldError) {
    return { error: oldError };
  }

  const newError = validatePackageName(newPackage, 'New package name');
  if (newError) {
    return { error: newError };
  }

  if (oldPackage === newPackage) {
    return { error: 'Old and new package names are the same. No changes needed.' };
  }

  return {
    mapping: {
      oldPackage,
      newPackage,
      oldGroupId: overrides?.oldGroupId || deriveGroupId(oldPackage),
      newGroupId: overrides?.newGroupId || deriveGroupId(newPackage),
      oldPath: packageToPath(oldPackage),
      newPath: packageToPath(newPackage),
    },
  };
}
