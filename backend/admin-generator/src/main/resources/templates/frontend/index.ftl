<template>
  <div class="${entity.classNameLower}-container">
    <!-- 搜索区域 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="queryParams" class="search-form">
<#list entity.queryFields as field>
<#if field.formType == "select">
        <el-form-item label="${field.comment!field.fieldName}">
          <el-select v-model="queryParams.${field.fieldName}" placeholder="请选择${field.comment!field.fieldName}" clearable>
            <el-option label="全部" :value="undefined" />
            <!-- TODO: 添加选项 -->
          </el-select>
        </el-form-item>
<#elseif field.formType == "datetime" || field.formType == "date">
        <el-form-item label="${field.comment!field.fieldName}">
          <el-date-picker
            v-model="queryParams.${field.fieldName}"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            clearable
          />
        </el-form-item>
<#else>
        <el-form-item label="${field.comment!field.fieldName}">
          <el-input
            v-model="queryParams.${field.fieldName}"
            placeholder="请输入${field.comment!field.fieldName}"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
</#if>
</#list>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="resetQuery">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作区域 -->
    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>${entity.comment!entity.className}列表</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增
          </el-button>
        </div>
      </template>

      <!-- 表格 -->
      <el-table v-loading="loading" :data="dataList" border stripe>
<#list entity.listFields as field>
<#if field.isPrimaryKey>
        <el-table-column prop="${field.fieldName}" label="ID" width="80" align="center" />
<#elseif field.formType == "select" && field.dictType??>
        <el-table-column prop="${field.fieldName}" label="${field.comment!field.fieldName}" width="100" align="center">
          <template #default="{ row }">
            <el-tag>{{ row.${field.fieldName} }}</el-tag>
          </template>
        </el-table-column>
<#elseif field.fieldType == "LocalDateTime">
        <el-table-column prop="${field.fieldName}" label="${field.comment!field.fieldName}" width="180" align="center" />
<#else>
        <el-table-column prop="${field.fieldName}" label="${field.comment!field.fieldName}" min-width="120" />
</#if>
</#list>
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button type="danger" link @click="handleDelete(row)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleQuery"
          @current-change="handleQuery"
        />
      </div>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <${entity.className}Dialog v-model:visible="dialogVisible" :id="currentId" @success="handleSuccess" />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Edit, Delete } from '@element-plus/icons-vue'
import { list${entity.className}s, delete${entity.className}, type ${entity.className}VO } from '@/api/${entity.classNameLower}'
import ${entity.className}Dialog from './components/${entity.className}Dialog.vue'

// 查询参数
const queryParams = reactive({
<#list entity.queryFields as field>
  ${field.fieldName}: undefined as <#if field.fieldType == "Long" || field.fieldType == "Integer">number<#elseif field.fieldType == "Boolean">boolean<#else>string</#if> | undefined,
</#list>
  pageNum: 1,
  pageSize: 10
})

// 状态
const loading = ref(false)
const dataList = ref<${entity.className}VO[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const currentId = ref<number | undefined>(undefined)

// 获取列表
const fetchList = async () => {
  loading.value = true
  try {
    const res: any = await list${entity.className}s(queryParams)
    dataList.value = res?.list || []
    total.value = res?.total || 0
  } catch (error) {
    console.error('Failed to fetch list:', error)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleQuery = () => {
  queryParams.pageNum = 1
  fetchList()
}

// 重置
const resetQuery = () => {
<#list entity.queryFields as field>
  queryParams.${field.fieldName} = undefined
</#list>
  queryParams.pageNum = 1
  fetchList()
}

// 新增
const handleAdd = () => {
  currentId.value = undefined
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row: ${entity.className}VO) => {
  currentId.value = row.id
  dialogVisible.value = true
}

// 删除
const handleDelete = async (row: ${entity.className}VO) => {
  try {
    await ElMessageBox.confirm('确定要删除该${entity.comment!entity.className}吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await delete${entity.className}(row.id)
    ElMessage.success('删除成功')
    fetchList()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 操作成功回调
const handleSuccess = () => {
  fetchList()
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.${entity.classNameLower}-container {
  padding: 20px;
}

.search-card {
  margin-bottom: 20px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
