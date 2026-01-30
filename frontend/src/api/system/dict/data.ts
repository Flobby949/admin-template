import request from '@/utils/request'

export interface DictDataVO {
  id: number
  dictSort: number
  dictLabel: string
  dictValue: string
  dictType: string
  cssClass?: string
  listClass?: string // default/primary/success/warning/danger
  isDefault: number // 0=否, 1=是
  status: number // 0=停用, 1=正常
  remark?: string
  createTime?: string
}

export interface DictDataQuery {
  pageNum?: number
  pageSize?: number
  dictName?: string
  dictType?: string
  status?: string
  dictLabel?: string
}

export interface PageResult<T> {
  list: T[]
  total: number
}

// 查询字典数据列表
export function listData(query: DictDataQuery) {
  return request<PageResult<DictDataVO>>({
    url: '/system/dict/data',
    method: 'get',
    params: query
  })
}

// 查询字典数据详细
export function getData(id: number) {
  return request<DictDataVO>({
    url: `/system/dict/data/${id}`,
    method: 'get'
  })
}

// 根据字典类型查询字典数据信息
export function getDicts(dictType: string) {
  return request<DictDataVO[]>({
    url: `/system/dict/data/type/${dictType}`,
    method: 'get'
  })
}

// 新增字典数据
export function addData(data: Partial<DictDataVO>) {
  return request({
    url: '/system/dict/data',
    method: 'post',
    data
  })
}

// 修改字典数据
export function updateData(data: Partial<DictDataVO>) {
  return request({
    url: '/system/dict/data',
    method: 'put',
    data
  })
}

// 删除字典数据
export function delData(id: number) {
  return request({
    url: `/system/dict/data/${id}`,
    method: 'delete'
  })
}
