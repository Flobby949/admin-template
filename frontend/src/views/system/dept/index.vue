<template>
  <div class="dept-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>部门管理</span>
          <el-button type="primary" @click="handleAdd">新增部门</el-button>
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
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
            <el-option label="正常" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 树形表格 -->
      <el-table
        :data="deptList"
        row-key="id"
        border
        stripe
        default-expand-all
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
      >
        <el-table-column prop="deptName" label="部门名称" width="200" />
        <el-table-column prop="orderNum" label="排序" width="100" align="center" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="leader" label="负责人" width="120" />
        <el-table-column prop="phone" label="联系电话" width="150" />
        <el-table-column prop="email" label="邮箱" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="success" link @click="handleAdd(row)">新增</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

// 查询参数
const queryParams = reactive({
  deptName: '',
  status: undefined as number | undefined
})

// 部门列表
const deptList = ref([])

// 查询部门列表
const handleQuery = () => {
  // TODO: 调用 API 获取部门列表
  console.log('查询部门列表', queryParams)
  ElMessage.info('部门管理功能开发中...')
}

// 重置查询
const handleReset = () => {
  queryParams.deptName = ''
  queryParams.status = undefined
  handleQuery()
}

// 新增部门
const handleAdd = (row?: any) => {
  if (row) {
    console.log('新增子部门', row)
    ElMessage.info(`新增 ${row.deptName} 的子部门功能开发中...`)
  } else {
    ElMessage.info('新增部门功能开发中...')
  }
}

// 编辑部门
const handleEdit = (row: any) => {
  console.log('编辑部门', row)
  ElMessage.info('编辑部门功能开发中...')
}

// 删除部门
const handleDelete = (row: any) => {
  ElMessageBox.confirm('确认删除该部门吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    console.log('删除部门', row)
    ElMessage.success('删除成功')
  }).catch(() => {
    ElMessage.info('已取消删除')
  })
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
