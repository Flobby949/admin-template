import request from '@/utils/request'
import type { PageResult } from '@/types/common'

/**
 * 文章 VO
 */
export interface ArticleVO {
  id: number
  title: string
  summary?: string
  content?: string
  categoryId?: number
  categoryName?: string
  coverUrl?: string
  status: number
  publishTime?: string
  revokeTime?: string
  auditBy?: string
  auditTime?: string
  deptId?: number
  authorId?: number
  viewCount: number
  sortOrder: number
  createTime: string
  createBy?: string
}

/**
 * 文章表单
 */
export interface ArticleForm {
  title: string
  summary?: string
  content?: string
  categoryId?: number
  coverUrl?: string
  sortOrder?: number
  deptId?: number
}

/**
 * 文章查询参数
 */
export interface ArticleQuery {
  title?: string
  categoryId?: number
  status?: number
  authorId?: number
  pageNum: number
  pageSize: number
}

/**
 * 分页查询文章列表
 */
export function getArticleList(query: ArticleQuery) {
  return request<PageResult<ArticleVO>>({
    url: '/cms/articles/list',
    method: 'post',
    data: query
  })
}

/**
 * 获取文章详情
 */
export function getArticleById(id: number) {
  return request<ArticleVO>({
    url: `/cms/articles/${id}`,
    method: 'get'
  })
}

/**
 * 获取文章详情（增加浏览量）
 */
export function getArticleDetail(id: number) {
  return request<ArticleVO>({
    url: `/cms/articles/${id}/detail`,
    method: 'get'
  })
}

/**
 * 新增文章
 */
export function createArticle(data: ArticleForm) {
  return request<number>({
    url: '/cms/articles',
    method: 'post',
    data
  })
}

/**
 * 更新文章
 */
export function updateArticle(id: number, data: ArticleForm) {
  return request({
    url: `/cms/articles/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除文章
 */
export function deleteArticle(id: number) {
  return request({
    url: `/cms/articles/${id}`,
    method: 'delete'
  })
}

/**
 * 提交审核
 */
export function submitArticle(id: number) {
  return request({
    url: `/cms/articles/${id}/submit`,
    method: 'put'
  })
}

/**
 * 发布文章
 */
export function publishArticle(id: number) {
  return request({
    url: `/cms/articles/${id}/publish`,
    method: 'put'
  })
}

/**
 * 驳回文章
 */
export function rejectArticle(id: number) {
  return request({
    url: `/cms/articles/${id}/reject`,
    method: 'put'
  })
}

/**
 * 下架文章
 */
export function revokeArticle(id: number) {
  return request({
    url: `/cms/articles/${id}/revoke`,
    method: 'put'
  })
}
