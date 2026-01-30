<template>
  <div class="dept-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>部门管理</span>
          <el-button type="primary" plain icon="Plus" @click="handleAdd()" v-permission="'system:dept:add'">
            新增部门
          </el-button>
        </div>
      </template>

      <!-- 搜索表单 -->
      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item label="部门名称">
          <el-input
            v-model="queryParams.deptName"
            placeholder="请输入部门名称"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 200px">
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
        :data="deptList"
        row-key="id"
        border
        stripe
        default-expand-all
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
      >
        <el-table-column prop="deptName" label="部门名称" width="260" />
        <el-table-column prop="sortOrder" label="排序" width="100" align="center" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="leader" label="负责人" width="120" align="center" />
        <el-table-column prop="phone" label="联系电话" width="150" align="center" />
        <el-table-column prop="email" label="邮箱" show-overflow-tooltip align="center" />
        <el-table-column prop="createTime" label="创建时间" width="180" align="center" />
        <el-table-column label="操作" width="220" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link icon="Edit" @click="handleEdit(row)" v-permission="'system:dept:edit'">
              编辑
            </el-button>
            <el-button type="primary" link icon="Plus" @click="handleAdd(row)" v-permission="'system:dept:add'">
              新增
            </el-button>
            <el-button type="danger" link icon="Delete" @click="handleDelete(row)" v-permission="'system:dept:remove'">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 弹窗 -->
    <dept-dialog
      v-model="dialog.visible"
      :title="dialog.title"
      :is-edit="dialog.isEdit"
      :dept-id="dialog.deptId"
      :parent-id="dialog.parentId"
      @success="handleQuery"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDeptTree, delDept, type DeptVO, type DeptQuery } from '@/api/system/dept'
import DeptDialog from './components/DeptDialog.vue'

// 查询参数
const queryParams = reactive<DeptQuery>({
  deptName: undefined,
  status: undefined
})

const loading = ref(false)
const deptList = ref<DeptVO[]>([])

// 弹窗控制
const dialog = reactive({
  visible: false,
  title: '',
  isEdit: false,
  deptId: undefined as number | undefined,
  parentId: undefined as number | undefined
})

// 查询部门列表
const handleQuery = async () => {
  loading.value = true
  try {
    const data = await getDeptTree(queryParams)
    deptList.value = data
  } catch (error) {
    ElMessage.error('查询部门列表失败')
  } finally {
    loading.value = false
  }
}

// 重置查询
const handleReset = () => {
  queryParams.deptName = undefined
  queryParams.status = undefined
  handleQuery()
}

// 新增部门
const handleAdd = (row?: DeptVO) => {
  dialog.title = '新增部门'
  dialog.isEdit = false
  dialog.deptId = undefined
  dialog.parentId = row ? row.id : undefined
  dialog.visible = true
}

// 编辑部门
const handleEdit = (row: DeptVO) => {
  dialog.title = '编辑部门'
  dialog.isEdit = true
  dialog.deptId = row.id
  dialog.parentId = row.parentId
  dialog.visible = true
}

// 删除部门
const handleDelete = (row: DeptVO) => {
  ElMessageBox.confirm(`确认删除部门"${row.deptName}"及其子部门吗？`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await delDept(row.id)
      ElMessage.success('删除成功')
      handleQuery()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

onMounted(() => {
  handleQuery()
})
</script>

<style scoped lang="scss">
.dept-container {
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