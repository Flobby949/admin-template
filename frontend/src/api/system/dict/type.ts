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

// 获取字典选择框列表（使用列表接口）
export function optionselect() {
  return request<PageResult<DictTypeVO>>({
    url: '/system/dict/types',
    method: 'get',
    params: { pageNum: 1, pageSize: 1000 }
  }).then(res => res.list)
}

// 刷新字典缓存（前端模拟，后端暂无此接口）
export function refreshCache() {
  // 后端暂无刷新缓存接口，返回成功
  return Promise.resolve()
}
