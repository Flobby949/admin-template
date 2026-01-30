import request from '@/utils/request'

/**
 * Department View Object
 */
export interface DeptVO {
  id: number
  parentId: number
  deptName: string
  sortOrder: number
  leader: string
  phone: string
  email: string
  status: number // 1: Normal, 0: Disabled
  createTime: string
  children?: DeptVO[]
  hasChildren?: boolean // For lazy loading if needed, though usually we load full tree
}

/**
 * Department Form Object
 */
export interface DeptForm {
  id?: number
  parentId?: number
  deptName: string
  sortOrder: number
  leader?: string
  phone?: string
  email?: string
  status: number
}

/**
 * Department Query Object
 */
export interface DeptQuery {
  deptName?: string
  status?: number
}

/**
 * Get Department Tree
 */
export function getDeptTree(query?: DeptQuery) {
  return request<DeptVO[]>({
    url: '/system/departments/tree',
    method: 'get',
    params: query
  })
}

/**
 * Get Department List (Flat)
 */
export function getDeptList(query?: DeptQuery) {
  return request<DeptVO[]>({
    url: '/system/departments/list',
    method: 'get',
    params: query
  })
}

/**
 * Get Department Details
 */
export function getDept(id: number) {
  return request<DeptVO>({
    url: `/system/departments/${id}`,
    method: 'get'
  })
}

/**
 * Create Department
 */
export function addDept(data: DeptForm) {
  return request<void>({
    url: '/system/departments',
    method: 'post',
    data
  })
}

/**
 * Update Department
 */
export function updateDept(id: number, data: DeptForm) {
  return request<void>({
    url: `/system/departments/${id}`,
    method: 'put',
    data
  })
}

/**
 * Delete Department
 */
export function delDept(id: number) {
  return request<void>({
    url: `/system/departments/${id}`,
    method: 'delete'
  })
}

/**
 * Update Department Status
 */
export function changeDeptStatus(id: number, status: number) {
  return request<void>({
    url: `/system/departments/${id}/status`,
    method: 'patch',
    params: { status }
  })
}
