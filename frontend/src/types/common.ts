/**
 * 通用类型定义
 */

/**
 * 分页请求参数
 */
export interface PageQuery {
  pageNum: number
  pageSize: number
}

/**
 * 分页响应结果
 */
export interface PageResult<T> {
  list: T[]
  total: number
  pageNum: number
  pageSize: number
}

/**
 * API 响应结构
 */
export interface ApiResponse<T = unknown> {
  code: number
  message: string
  data: T
}

/**
 * 树形节点
 */
export interface TreeNode {
  id: number
  label: string
  children?: TreeNode[]
}

/**
 * 选项类型
 */
export interface Option {
  label: string
  value: string | number
}

/**
 * 状态枚举
 */
export enum Status {
  DISABLED = 0,
  ENABLED = 1
}

/**
 * 菜单类型枚举
 */
export enum MenuType {
  DIRECTORY = 0,
  MENU = 1,
  BUTTON = 2
}

/**
 * 数据权限范围枚举
 */
export enum DataScope {
  ALL = 1,
  CUSTOM = 2,
  DEPT = 3,
  DEPT_AND_CHILD = 4,
  SELF = 5
}
