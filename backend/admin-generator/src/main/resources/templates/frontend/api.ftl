import request from '@/utils/request'

/**
 * ${entity.comment!entity.className}API
 * @author ${author}
 * @date ${date}
 */

export interface ${entity.className}VO {
<#list entity.listFields as field>
  /** ${field.comment!field.fieldName} */
  ${field.fieldName}: <#if field.fieldType == "Long" || field.fieldType == "Integer">number<#elseif field.fieldType == "Boolean">boolean<#elseif field.fieldType == "BigDecimal">number<#else>string</#if>
</#list>
}

export interface ${entity.className}DTO {
<#list entity.formFields as field>
  /** ${field.comment!field.fieldName} */
  ${field.fieldName}<#if field.isNullable>?</#if>: <#if field.fieldType == "Long" || field.fieldType == "Integer">number<#elseif field.fieldType == "Boolean">boolean<#elseif field.fieldType == "BigDecimal">number<#else>string</#if>
</#list>
}

<#if entity.hasQueryFields()>
export interface ${entity.className}Query {
<#list entity.queryFields as field>
  /** ${field.comment!field.fieldName} */
  ${field.fieldName}?: <#if field.fieldType == "Long" || field.fieldType == "Integer">number<#elseif field.fieldType == "Boolean">boolean<#else>string</#if>
</#list>
  /** 页码 */
  pageNum?: number
  /** 每页数量 */
  pageSize?: number
}

</#if>
/**
 * 获取${entity.comment!entity.className}列表
 */
export function list${entity.className}s() {
  return request<${entity.className}VO[]>({
    url: '/api/${moduleName}/${entity.apiPath}s',
    method: 'get'
  })
}

/**
 * 获取${entity.comment!entity.className}详情
 */
export function get${entity.className}(id: number) {
  return request<${entity.className}VO>({
    url: `/api/${moduleName}/${entity.apiPath}s/${'$'}{id}`,
    method: 'get'
  })
}

/**
 * 创建${entity.comment!entity.className}
 */
export function create${entity.className}(data: ${entity.className}DTO) {
  return request<number>({
    url: '/api/${moduleName}/${entity.apiPath}s',
    method: 'post',
    data
  })
}

/**
 * 更新${entity.comment!entity.className}
 */
export function update${entity.className}(id: number, data: ${entity.className}DTO) {
  return request<void>({
    url: `/api/${moduleName}/${entity.apiPath}s/${'$'}{id}`,
    method: 'put',
    data
  })
}

/**
 * 删除${entity.comment!entity.className}
 */
export function delete${entity.className}(id: number) {
  return request<void>({
    url: `/api/${moduleName}/${entity.apiPath}s/${'$'}{id}`,
    method: 'delete'
  })
}
