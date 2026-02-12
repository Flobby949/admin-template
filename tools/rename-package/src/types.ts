/**
 * 包名映射关系，由用户输入的旧包名和新包名推导出所有需要的映射。
 */
export interface PackageMapping {
  /** 旧包名（用户输入），如 top.flobby.admin */
  oldPackage: string;
  /** 新包名（用户输入），如 com.example.demo */
  newPackage: string;
  /** 旧 groupId（oldPackage 去掉最后一段），如 top.flobby */
  oldGroupId: string;
  /** 新 groupId（newPackage 去掉最后一段），如 com.example */
  newGroupId: string;
  /** 旧包名对应的目录路径，如 top/flobby/admin */
  oldPath: string;
  /** 新包名对应的目录路径，如 com/example/demo */
  newPath: string;
}

/**
 * 变更类型枚举
 */
export enum ChangeType {
  /** Java package 声明替换 */
  PACKAGE_DECLARATION = 'PACKAGE_DECLARATION',
  /** Java import 语句替换 */
  IMPORT_STATEMENT = 'IMPORT_STATEMENT',
  /** pom.xml groupId 替换 */
  GROUP_ID = 'GROUP_ID',
  /** pom.xml mainClass 引用替换 */
  MAIN_CLASS = 'MAIN_CLASS',
  /** 配置文件中的包名引用替换 */
  CONFIG_REFERENCE = 'CONFIG_REFERENCE',
  /** 目录/文件移动 */
  DIRECTORY_MOVE = 'DIRECTORY_MOVE',
}

/**
 * 单个文件的变更记录
 */
export interface FileChange {
  /** 文件相对路径（相对于 backend/） */
  filePath: string;
  /** 变更类型 */
  changeType: ChangeType;
  /** 变更描述 */
  description: string;
  /** 变更前内容片段（可选，用于预览） */
  before?: string;
  /** 变更后内容片段（可选，用于预览） */
  after?: string;
}

/**
 * 需人工确认的风险项
 */
export interface Warning {
  /** 文件路径 */
  filePath: string;
  /** 行号 */
  line: number;
  /** 包含旧包名的内容 */
  content: string;
  /** 标记原因 */
  reason: string;
}

/**
 * 变更统计摘要
 */
export interface Stats {
  /** 修改的 Java 文件数 */
  javaFiles: number;
  /** 修改的 pom.xml 文件数 */
  pomFiles: number;
  /** 修改的配置文件数 */
  configFiles: number;
  /** 移动的目录数 */
  directoriesMoved: number;
  /** 清理的空目录数 */
  directoriesCleaned: number;
  /** 风险项数量 */
  warningCount: number;
}

/**
 * 变更报告，汇总所有变更
 */
export interface ChangeReport {
  /** 使用的包名映射 */
  mapping: PackageMapping;
  /** 所有变更记录 */
  changes: FileChange[];
  /** 需人工确认的风险项 */
  warnings: Warning[];
  /** 统计摘要 */
  stats: Stats;
  /** 是否全部成功 */
  success: boolean;
}
