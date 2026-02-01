<template>
  <div class="category-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>分类管理</span>
          <el-button
            v-permission="'cms:category:add'"
            type="primary"
            plain
            icon="Plus"
            @click="handleAdd()"
          >
            新增分类
          </el-button>
        </div>
      </template>

      <!-- 搜索表单 -->
      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item label="分类名称">
          <el-input
            v-model="queryParams.categoryName"
            placeholder="请输入分类名称"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="queryParams.status"
            placeholder="请选择状态"
            clearable
            style="width: 200px"
          >
            <el-option label="正常" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
          <el-button icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 树形表格 -->
      <el-table
        v-loading="loading"
        :data="filteredList"
        row-key="id"
        border
        stripe
        default-expand-all
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        :row-style="tableRowStyle"
      >
        <el-table-column prop="categoryName" label="分类名称" width="260" />
        <el-table-column prop="sortOrder" label="排序" width="100" align="center" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              v-permission="'cms:category:edit'"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" align="center" />
        <el-table-column label="操作" width="220" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              v-permission="'cms:category:edit'"
              type="primary"
              link
              icon="Edit"
              @click="handleEdit(row)"
            >
              编辑
            </el-button>
            <el-button
              v-permission="'cms:category:add'"
              type="primary"
              link
              icon="Plus"
              @click="handleAdd(row)"
            >
              新增
            </el-button>
            <el-button
              v-permission="'cms:category:delete'"
              type="danger"
              link
              icon="Delete"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 弹窗 -->
    <category-dialog
      v-model="dialog.visible"
      :title="dialog.title"
      :is-edit="dialog.isEdit"
      :category-id="dialog.categoryId"
      :parent-id="dialog.parentId"
      @success="handleQuery"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getCategoryTree,
  deleteCategory,
  updateCategoryStatus,
  type CategoryVO
} from '@/api/cms/category'
import CategoryDialog from './components/CategoryDialog.vue'

interface QueryParams {
  categoryName?: string
  status?: number
}

const queryParams = reactive<QueryParams>({
  categoryName: undefined,
  status: undefined
})

const loading = ref(false)
const categoryList = ref<CategoryVO[]>([])

const dialog = reactive({
  visible: false,
  title: '',
  isEdit: false,
  categoryId: undefined as number | undefined,
  parentId: undefined as number | undefined
})

// 过滤后的列表
const filteredList = computed(() => {
  if (!queryParams.categoryName && queryParams.status === undefined) {
    return categoryList.value
  }
  return filterTree(categoryList.value)
})

// 递归过滤树
const filterTree = (list: CategoryVO[]): CategoryVO[] => {
  return list
    .map(item => {
      const children = item.children ? filterTree(item.children) : []
      const nameMatch = !queryParams.categoryName || item.categoryName.includes(queryParams.categoryName)
      const statusMatch = queryParams.status === undefined || item.status === queryParams.status

      if (nameMatch && statusMatch) {
        return { ...item, children }
      }
      if (children.length > 0) {
        return { ...item, children }
      }
      return null
    })
    .filter(Boolean) as CategoryVO[]
}

const handleQuery = async () => {
  loading.value = true
  try {
    const data = await getCategoryTree()
    categoryList.value = data
  } catch (error) {
    ElMessage.error('查询分类列表失败')
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  queryParams.categoryName = undefined
  queryParams.status = undefined
  handleQuery()
}

const handleAdd = (row?: CategoryVO) => {
  dialog.title = '新增分类'
  dialog.isEdit = false
  dialog.categoryId = undefined
  dialog.parentId = row ? row.id : undefined
  dialog.visible = true
}

const handleEdit = (row: CategoryVO) => {
  dialog.title = '编辑分类'
  dialog.isEdit = true
  dialog.categoryId = row.id
  dialog.parentId = row.parentId
  dialog.visible = true
}

const handleDelete = (row: CategoryVO) => {
  ElMessageBox.confirm(`确认删除分类"${row.categoryName}"吗？`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      try {
        await deleteCategory(row.id)
        ElMessage.success('删除成功')
        handleQuery()
      } catch (error) {
        ElMessage.error('删除失败')
      }
    })
    .catch(() => {})
}

const handleStatusChange = async (row: CategoryVO) => {
  const text = row.status === 0 ? '停用' : '启用'
  try {
    await updateCategoryStatus(row.id, row.status)
    ElMessage.success(`${text}成功`)
    handleQuery()
  } catch (error) {
    ElMessage.error(`${text}失败`)
    row.status = row.status === 0 ? 1 : 0
  }
}

const tableRowStyle = ({ row }: { row: CategoryVO }) => {
  if (row.status === 0) {
    return { color: '#a8abb2' }
  }
  return {}
}

onMounted(() => {
  handleQuery()
})
</script>

<style scoped lang="scss">
.category-container {
  padding: 20px;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .search-form {
    margin-bottom: 20px;
  }
}
</style>
