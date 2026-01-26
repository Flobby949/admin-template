import request from '@/utils/request'

/**
 * 用户查询参数
 */
export interface UserQuery {
  username?: string
  realName?: string
  phone?: string
  email?: string
  status?: number
  deptId?: number
  roleId?: number
  startTime?: string
  endTime?: string
  pageNum?: number
  pageSize?: number
}

/**
 * 用户DTO
 */
export interface UserDTO {
  id?: number
  username: string
  password?: string
  realName?: string
  email?: string
  phone?: string
  avatar?: string
  status: number
  roleIds?: number[]
  deptIds?: number[]
  remark?: string
}

/**
 * 角色信息
 */
export interface RoleInfo {
  id: number
  name: string
  code: string
}

/**
 * 部门信息
 */
export interface DeptInfo {
  id: number
  name: string
}

/**
 * 用户VO
 */
export interface UserVO {
  id: number
  username: string
  realName?: string
  email?: string
  phone?: string
  avatar?: string
  status: number
  roles?: RoleInfo[]
  depts?: DeptInfo[]
  createTime: string
  updateTime: string
  createBy?: string
  updateBy?: string
  remark?: string
}

/**
 * 分页结果
 */
export interface PageResult<T> {
  list: T[]
  total: number
}

/**
 * 分页查询用户列表
 */
export function getUserList(query: UserQuery) {
  return request<PageResult<UserVO>>({
    url: '/system/users/list',
    method: 'post',
    data: query
  })
}

/**
 * 根据ID获取用户详情
 */
export function getUserById(id: number) {
  return request<UserVO>({
    url: `/system/users/${id}`,
    method: 'get'
  })
}

/**
 * 创建用户
 */
export function createUser(data: UserDTO) {
  return request<number>({
    url: '/system/users',
    method: 'post',
    data
  })
}

/**
 * 更新用户
 */
export function updateUser(id: number, data: UserDTO) {
  return request<void>({
    url: `/system/users/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除用户
 */
export function deleteUser(id: number) {
  return request<void>({
    url: `/system/users/${id}`,
    method: 'delete'
  })
}

/**
 * 批量删除用户
 */
export function batchDeleteUsers(ids: number[]) {
  return request<void>({
    url: '/system/users/batch',
    method: 'delete',
    data: ids
  })
}

/**
 * 重置用户密码
 */
export function resetPassword(id: number, newPassword: string) {
  return request<void>({
    url: `/system/users/${id}/password`,
    method: 'put',
    params: { newPassword }
  })
}

/**
 * 修改用户状态
 */
export function changeStatus(id: number, status: number) {
  return request<void>({
    url: `/system/users/${id}/status`,
    method: 'put',
    params: { status }
  })
}
