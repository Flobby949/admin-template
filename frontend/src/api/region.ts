import request from '@/utils/request'

/**
 * 区域表API
 * @author Code Generator
 * @date 2026-02-01
 */

export interface RegionVO {
  /** 主键ID */
  id: number
  /** 区域名称 */
  name: string
  /** 区域编码 */
  regionCode: string
  /** 父级编码 */
  parentCode: string
  /** 层级：1-省，2-市，3-区县 */
  level: number
  /** 排序 */
  sortOrder: number
  /** 状态：0-禁用，1-启用 */
  status: number
  /** 创建时间 */
  createTime: string
  /** 更新时间 */
  updateTime: string
  /** 创建人 */
  createBy: string
}

export interface RegionDTO {
  /** 区域名称 */
  name: string
  /** 区域编码 */
  regionCode: string
  /** 父级编码 */
  parentCode?: string
  /** 层级：1-省，2-市，3-区县 */
  level: number
  /** 排序 */
  sortOrder?: number
  /** 状态：0-禁用，1-启用 */
  status: number
}

export interface RegionQuery {
  /** 区域名称 */
  name?: string
  /** 区域编码 */
  regionCode?: string
  /** 父级编码 */
  parentCode?: string
  /** 状态：0-禁用，1-启用 */
  status?: number
  /** 页码 */
  pageNum?: number
  /** 每页数量 */
  pageSize?: number
}

/**
 * 获取区域表列表
 */
export function listRegions() {
  return request<RegionVO[]>({
    url: '/system/regions',
    method: 'get'
  })
}

/**
 * 获取区域表详情
 */
export function getRegion(id: number) {
  return request<RegionVO>({
    url: `/system/regions/${id}`,
    method: 'get'
  })
}

/**
 * 创建区域表
 */
export function createRegion(data: RegionDTO) {
  return request<number>({
    url: '/system/regions',
    method: 'post',
    data
  })
}

/**
 * 更新区域表
 */
export function updateRegion(id: number, data: RegionDTO) {
  return request<void>({
    url: `/system/regions/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除区域表
 */
export function deleteRegion(id: number) {
  return request<void>({
    url: `/system/regions/${id}`,
    method: 'delete'
  })
}
