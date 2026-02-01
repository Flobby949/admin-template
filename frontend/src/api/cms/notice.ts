import request from '@/utils/request'
import type { PageResult } from '@/types/common'

/**
 * 公告 VO
 */
export interface NoticeVO {
  id: number
  title: string
  content?: string
  status: number
  publishTime?: string
  revokeTime?: string
  deptId?: number
  publisherId?: number
  sortOrder: number
  createTime: string
  createBy?: string
  readCount?: number
  read?: boolean
}

/**
 * 公告表单
 */
export interface NoticeForm {
  title: string
  content?: string
  sortOrder?: number
  deptId?: number
}

/**
 * 公告查询参数
 */
export interface NoticeQuery {
  title?: string
  status?: number
  pageNum: number
  pageSize: number
}

/**
 * 分页查询公告列表
 */
export function getNoticeList(query: NoticeQuery) {
  return request<PageResult<NoticeVO>>({
    url: '/cms/notices/list',
    method: 'post',
    data: query
  })
}

/**
 * 获取用户未读公告列表
 */
export function getUnreadNotices(pageNum: number = 1, pageSize: number = 10) {
  return request<PageResult<NoticeVO>>({
    url: '/cms/notices/unread',
    method: 'get',
    params: { pageNum, pageSize }
  })
}

/**
 * 获取公告详情
 */
export function getNoticeById(id: number) {
  return request<NoticeVO>({
    url: `/cms/notices/${id}`,
    method: 'get'
  })
}

/**
 * 新增公告
 */
export function createNotice(data: NoticeForm) {
  return request<number>({
    url: '/cms/notices',
    method: 'post',
    data
  })
}

/**
 * 更新公告
 */
export function updateNotice(id: number, data: NoticeForm) {
  return request({
    url: `/cms/notices/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除公告
 */
export function deleteNotice(id: number) {
  return request({
    url: `/cms/notices/${id}`,
    method: 'delete'
  })
}

/**
 * 发布公告
 */
export function publishNotice(id: number) {
  return request({
    url: `/cms/notices/${id}/publish`,
    method: 'put'
  })
}

/**
 * 撤回公告
 */
export function revokeNotice(id: number) {
  return request({
    url: `/cms/notices/${id}/revoke`,
    method: 'put'
  })
}

/**
 * 标记公告已读
 */
export function markNoticeAsRead(id: number) {
  return request({
    url: `/cms/notices/${id}/read`,
    method: 'post'
  })
}

/**
 * 获取公告已读数量
 */
export function getNoticeReadCount(id: number) {
  return request<number>({
    url: `/cms/notices/${id}/read-count`,
    method: 'get'
  })
}
