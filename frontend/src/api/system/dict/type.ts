import request from '@/utils/request'

export interface DictTypeVO {
  id: number
  dictName: string
  dictType: string
  status: number
  remark?: string
  createTime: string
  updateTime?: string
}

export interface DictTypeQuery {
  pageNum?: number
  pageSize?: number
  dictName?: string
  dictType?: string
  status?: string
}

export interface PageResult<T> {
  list: T[]
  total: number
}

// 查询字典类型列表
export function listType(query: DictTypeQuery) {
  return request<PageResult<DictTypeVO>>({
    url: '/system/dict/types',
    method: 'get',
    params: query
  })
}

// 查询字典类型详细
export function getType(dictId: number) {
  return request<DictTypeVO>({
    url: `/system/dict/types/${dictId}`,
    method: 'get'
  })
}

// 新增字典类型
export function addType(data: Partial<DictTypeVO>) {
  return request({
    url: '/system/dict/types',
    method: 'post',
    data
  })
}

// 修改字典类型
export function updateType(data: Partial<DictTypeVO>) {
  return request({
    url: `/system/dict/types/${data.id}`,
    method: 'put',
    data
  })
}

// 删除字典类型
export function delType(dictId: number) {
  return request({
    url: `/system/dict/types/${dictId}`,
    method: 'delete'
  })
}

// 刷新字典缓存
export function refreshCache() {
  return request({
    url: '/system/dict/types/refresh',
    method: 'delete'
  })
}

// 获取字典选择框列表
export function optionselect() {
  return request<DictTypeVO[]>({
    url: '/system/dict/types/optionselect',
    method: 'get'
  })
}
