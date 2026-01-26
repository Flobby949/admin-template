import request from '@/utils/request'

// 获取菜单树
export function listMenuTree() {
  return request({
    url: '/system/menus/tree',
    method: 'get'
  })
}

// 获取菜单详情
export function getMenuById(id: number) {
  return request({
    url: `/system/menus/${id}`,
    method: 'get'
  })
}

// 创建菜单
export function createMenu(data: MenuDTO) {
  return request({
    url: '/system/menus',
    method: 'post',
    data
  })
}

// 更新菜单
export function updateMenu(id: number, data: MenuDTO) {
  return request({
    url: `/system/menus/${id}`,
    method: 'put',
    data
  })
}

// 删除菜单
export function deleteMenu(id: number) {
  return request({
    url: `/system/menus/${id}`,
    method: 'delete'
  })
}

// 更新菜单状态
export function updateMenuStatus(id: number, status: number) {
  return request({
    url: `/system/menus/${id}/status`,
    method: 'put',
    params: { status }
  })
}

// 类型定义
export interface MenuDTO {
  parentId?: number
  menuName: string
  menuType: number
  routePath?: string
  component?: string
  permission?: string
  icon?: string
  sortOrder?: number
  visible?: number
  status?: number
}

export interface MenuVO {
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
  createTime: string
  updateTime: string
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
