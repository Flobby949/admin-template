import request from '@/utils/request'

/**
 * 分类 VO
 */
export interface CategoryVO {
  id: number
  parentId: number
  categoryName: string
  sortOrder: number
  status: number
  deptId?: number
  createTime: string
  children?: CategoryVO[]
}

/**
 * 分类表单
 */
export interface CategoryForm {
  parentId?: number
  categoryName: string
  sortOrder?: number
  status?: number
  deptId?: number
}

/**
 * 获取分类树
 */
export function getCategoryTree() {
  return request<CategoryVO[]>({
    url: '/cms/categories/tree',
    method: 'get'
  })
}

/**
 * 获取分类详情
 */
export function getCategoryById(id: number) {
  return request<CategoryVO>({
    url: `/cms/categories/${id}`,
    method: 'get'
  })
}

/**
 * 新增分类
 */
export function createCategory(data: CategoryForm) {
  return request<number>({
    url: '/cms/categories',
    method: 'post',
    data
  })
}

/**
 * 更新分类
 */
export function updateCategory(id: number, data: CategoryForm) {
  return request({
    url: `/cms/categories/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除分类
 */
export function deleteCategory(id: number) {
  return request({
    url: `/cms/categories/${id}`,
    method: 'delete'
  })
}

/**
 * 更新分类状态
 */
export function updateCategoryStatus(id: number, status: number) {
  return request({
    url: `/cms/categories/${id}/status`,
    method: 'put',
    params: { status }
  })
}
