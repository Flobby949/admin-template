import request from '@/utils/request'

// 角色列表
export function listRoles() {
  return request({
    url: '/system/roles',
    method: 'get'
  })
}

// 角色详情
export function getRole(id: number) {
  return request({
    url: `/system/roles/${id}`,
    method: 'get'
  })
}

// 创建角色
export function createRole(data: RoleForm) {
  return request({
    url: '/system/roles',
    method: 'post',
    data
  })
}

// 更新角色
export function updateRole(id: number, data: RoleForm) {
  return request({
    url: `/system/roles/${id}`,
    method: 'put',
    data
  })
}

// 删除角色
export function deleteRole(id: number) {
  return request({
    url: `/system/roles/${id}`,
    method: 'delete'
  })
}

// 修改角色状态
export function updateRoleStatus(id: number, status: number) {
  return request({
    url: `/system/roles/${id}/status`,
    method: 'put',
    params: { status }
  })
}

// 获取角色菜单ID列表
export function getRoleMenuIds(id: number) {
  return request({
    url: `/system/roles/${id}/menus`,
    method: 'get'
  })
}

// 获取菜单树
export function getMenuTree() {
  return request({
    url: '/system/roles/menus/tree',
    method: 'get'
  })
}

// 获取启用的角色列表（下拉选择）
export function listEnabledRoles() {
  return request({
    url: '/system/roles/enabled',
    method: 'get'
  })
}

// 类型定义
export interface RoleForm {
  id?: number
  roleName: string
  roleCode: string
  dataScope?: number
  status?: number
  remark?: string
  menuIds?: number[]
}

export interface RoleVO {
  id: number
  roleName: string
  roleCode: string
  dataScope: number
  status: number
  remark: string
  createTime: string
  updateTime: string
  menuIds?: number[]
}

export interface MenuTreeVO {
  id: number
  parentId: number
  menuName: string
  menuType: number
  routePath: string
  component: string
  permission: string
  icon: string
  sortOrder: number
  visible: number
  status: number
  children?: MenuTreeVO[]
}
