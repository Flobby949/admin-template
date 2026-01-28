import request from '@/utils/request'
import type { PageResult } from '@/api/user'

/**
 * Operation Log View Object
 */
export interface OperLogVO {
  id: number
  title: string        // Module Name
  businessType: number // 0:Other, 1:Insert, 2:Update, 3:Delete, 4:Grant, 5:Export, 6:Import, 7:Force, 8:GenCode, 9:Clean
  method: string       // Class + Method name
  requestMethod: string // GET, POST, etc.
  operName: string     // Operator Username
  operUrl: string
  operIp: string
  operLocation: string
  operParam: string    // Request Params (JSON string)
  jsonResult: string   // Response (JSON string)
  status: number       // 1:Success, 0:Fail
  errorMsg: string
  operTime: string
  costTime: number     // Time consumed (ms)
}

/**
 * Operation Log Query Object
 */
export interface OperLogQuery {
  pageNum: number
  pageSize: number
  title?: string
  operName?: string
  businessType?: number
  status?: number
  operTime?: string[] // Date Range [start, end]
}

/**
 * Get Operation Log List
 */
export function getOperLogList(query: OperLogQuery) {
  return request<PageResult<OperLogVO>>({
    url: '/monitor/operation-logs',
    method: 'get',
    params: {
      ...query,
      // Handle array params if backend expects simplified format or ensure axios serializes correctly
      // Typically axios serializes arrays as operTime[]=...&operTime[]=...
      // If backend needs comma separated: operTime: query.operTime?.join(',')
    }
  })
}

/**
 * Delete Operation Log(s)
 */
export function delOperLog(ids: number[]) {
  return request<void>({
    url: '/monitor/operation-logs',
    method: 'delete',
    data: ids
  })
}

/**
 * Clean All Operation Logs
 */
export function cleanOperLog() {
  return request<void>({
    url: '/monitor/operation-logs/clean',
    method: 'delete'
  })
}

/**
 * Export Operation Logs
 */
export function exportOperLog(query: OperLogQuery) {
  return request<Blob>({
    url: '/monitor/operation-logs/export',
    method: 'post',
    data: query,
    responseType: 'blob'
  })
}
