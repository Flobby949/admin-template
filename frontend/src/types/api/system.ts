/**
 * 系统模块 API 类型定义
 */

import type { PageQuery, Status, MenuType, DataScope } from '../common'

// ==================== 用户管理 ====================

/**
 * 用户查询参数
 */
export interface UserQuery extends PageQuery {
  username?: string
  realName?: string
  phone?: string
  status?: Status
  deptId?: number
}

/**
 * 用户 DTO
 */
export interface UserDTO {
  id?: number
  username: string
  password?: string
  realName?: string
  email?: string
  phone?: string
  avatar?: string
  status?: Status
  roleIds?: number[]
  deptIds?: number[]
}

/**
 * 用户 VO
 */
export interface UserVO {
  id: number
  username: string
  realName?: string
  email?: string
  phone?: string
  avatar?: string
  status: Status
  createTime?: string
  updateTime?: string
  createBy?: string
  updateBy?: string
  roles: RoleInfo[]
  depts: DeptInfo[]
}

/**
 * 角色简要信息
 */
export interface RoleInfo {
  id: number
  name: string
  code: string
}

/**
 * 部门简要信息
 */
export interface DeptInfo {
  id: number
  name: string
}

// ==================== 角色管理 ====================

/**
 * 角色查询参数
 */
export interface RoleQuery extends PageQuery {
  roleName?: string
  roleCode?: string
  status?: Status
}

/**
 * 角色 DTO
 */
export interface RoleDTO {
  id?: number
  roleName: string
  roleCode: string
  sort?: number
  status?: Status
  dataScope?: DataScope
  deptIds?: number[]
  menuIds?: number[]
  remark?: string
}

/**
 * 角色 VO
 */
export interface RoleVO {
  id: number
  roleName: string
  roleCode: string
  sort: number
  status: Status
  dataScope: DataScope
  deptIds?: number[]
  menuIds?: number[]
  remark?: string
  createTime?: string
  updateTime?: string
}

// ==================== 菜单管理 ====================

/**
 * 菜单 DTO
 */
export interface MenuDTO {
  id?: number
  parentId?: number
  menuName: string
  menuType: MenuType
  path?: string
  component?: string
  permission?: string
  icon?: string
  sort?: number
  visible?: Status
  status?: Status
}

/**
 * 菜单 VO
 */
export interface MenuVO {
  id: number
  parentId?: number
  menuName: string
  menuType: MenuType
  path?: string
  component?: string
  permission?: string
  icon?: string
  sort: number
  visible: Status
  status: Status
  createTime?: string
  children?: MenuVO[]
}

// ==================== 部门管理 ====================

/**
 * 部门 DTO
 */
export interface DeptDTO {
  id?: number
  parentId?: number
  deptName: string
  sort?: number
  leader?: string
  phone?: string
  email?: string
  status?: Status
}

/**
 * 部门 VO
 */
export interface DeptVO {
  id: number
  parentId?: number
  deptName: string
  sort: number
  leader?: string
  phone?: string
  email?: string
  status: Status
  createTime?: string
  children?: DeptVO[]
}

// ==================== 字典管理 ====================

/**
 * 字典类型 DTO
 */
export interface DictTypeDTO {
  id?: number
  dictName: string
  dictType: string
  status?: Status
  remark?: string
}

/**
 * 字典类型 VO
 */
export interface DictTypeVO {
  id: number
  dictName: string
  dictType: string
  status: Status
  remark?: string
  createTime?: string
}

/**
 * 字典数据 DTO
 */
export interface DictDataDTO {
  id?: number
  dictType: string
  dictLabel: string
  dictValue: string
  sort?: number
  cssClass?: string
  listClass?: string
  status?: Status
  remark?: string
}

/**
 * 字典数据 VO
 */
export interface DictDataVO {
  id: number
  dictType: string
  dictLabel: string
  dictValue: string
  sort: number
  cssClass?: string
  listClass?: string
  status: Status
  remark?: string
  createTime?: string
}
