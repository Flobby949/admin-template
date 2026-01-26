import request from '@/utils/request'

export function login(data: any) {
  return request({
    url: 'auth/login',
    method: 'post',
    data
  })
}

export function getInfo() {
  return request({
    url: 'auth/info',
    method: 'get'
  })
}

export function logout() {
  return request({
    url: 'auth/logout',
    method: 'post'
  })
}

// 获取用户路由
export function getRouters() {
  return request({
    url: 'auth/routers',
    method: 'get'
  })
}

// 路由类型定义
export interface RouterMeta {
  title: string
  icon?: string
  hidden?: boolean
  keepAlive?: boolean
  permissions?: string[]
}

export interface RouterVO {
  path: string
  name: string
  component: string
  redirect?: string
  meta: RouterMeta
  children?: RouterVO[]
}
