/**
 * 认证模块 API 类型定义
 */

/**
 * 登录请求参数
 */
export interface LoginDTO {
  username: string
  password: string
}

/**
 * 登录响应
 */
export interface LoginVO {
  accessToken: string
  refreshToken?: string
  expiresIn?: number
}

/**
 * 用户信息
 */
export interface UserInfo {
  id: number
  username: string
  realName?: string
  avatar?: string
  email?: string
  phone?: string
  roles: string[]
  permissions: string[]
}

/**
 * 修改密码请求参数
 */
export interface ChangePasswordDTO {
  oldPassword: string
  newPassword: string
  confirmPassword: string
}
