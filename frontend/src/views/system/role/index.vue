<template>
  <div class="role-container">
    <!-- 搜索区域 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item label="角色名称">
          <el-input
            v-model="queryParams.roleName"
            placeholder="请输入角色名称"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="角色编码">
          <el-input
            v-model="queryParams.roleCode"
            placeholder="请输入角色编码"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
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
          <span>角色列表</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增
          </el-button>
        </div>
      </template>

      <!-- 表格 -->
      <el-table v-loading="loading" :data="roleList" border stripe>
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="roleName" label="角色名称" min-width="120" />
        <el-table-column prop="roleCode" label="角色编码" min-width="120" />
        <el-table-column prop="dataScope" label="数据权限" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getDataScopeType(row.dataScope)">
              {{ getDataScopeLabel(row.dataScope) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" align="center" />
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
    </el-card>

    <!-- 新增/编辑对话框 -->
    <RoleDialog
      v-model:visible="dialogVisible"
      :role-id="currentRoleId"
      @success="handleSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Edit, Delete } from '@element-plus/icons-vue'
import { listRoles, deleteRole, updateRoleStatus, type RoleVO } from '@/api/role'
import RoleDialog from './components/RoleDialog.vue'

// 查询参数
const queryParams = reactive({
  roleName: '',
  roleCode: '',
  status: undefined as number | undefined
})

// 状态
const loading = ref(false)
const roleList = ref<RoleVO[]>([])
const dialogVisible = ref(false)
const currentRoleId = ref<number | undefined>(undefined)

// 数据权限映射
const dataScopeMap: Record<number, { label: string; type: string }> = {
  1: { label: '全部数据', type: 'success' },
  2: { label: '本部门及下级', type: 'primary' },
  3: { label: '仅本部门', type: 'warning' },
  4: { label: '仅本人', type: 'info' }
}

const getDataScopeLabel = (scope: number) => dataScopeMap[scope]?.label || '未知'
const getDataScopeType = (scope: number) => dataScopeMap[scope]?.type || 'info'

// 获取角色列表
const fetchRoleList = async () => {
  loading.value = true
  try {
    const res: any = await listRoles()
    let list = res || []

    // 前端过滤
    if (queryParams.roleName) {
      list = list.filter((item: RoleVO) =>
        item.roleName.includes(queryParams.roleName)
      )
    }
    if (queryParams.roleCode) {
      list = list.filter((item: RoleVO) =>
        item.roleCode.includes(queryParams.roleCode)
      )
    }
    if (queryParams.status !== undefined) {
      list = list.filter((item: RoleVO) => item.status === queryParams.status)
    }

    roleList.value = list
  } catch (error) {
    console.error('Failed to fetch role list:', error)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleQuery = () => {
  fetchRoleList()
}

// 重置
const resetQuery = () => {
  queryParams.roleName = ''
  queryParams.roleCode = ''
  queryParams.status = undefined
  fetchRoleList()
}

// 新增
const handleAdd = () => {
  currentRoleId.value = undefined
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row: RoleVO) => {
  currentRoleId.value = row.id
  dialogVisible.value = true
}

// 删除
const handleDelete = async (row: RoleVO) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除角色"${row.roleName}"吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await deleteRole(row.id)
    ElMessage.success('删除成功')
    fetchRoleList()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 状态变更
const handleStatusChange = async (row: RoleVO) => {
  try {
    await updateRoleStatus(row.id, row.status)
    ElMessage.success('状态修改成功')
  } catch (error: any) {
    // 恢复原状态
    row.status = row.status === 1 ? 0 : 1
    ElMessage.error(error.message || '状态修改失败')
  }
}

// 操作成功回调
const handleSuccess = () => {
  fetchRoleList()
}

onMounted(() => {
  fetchRoleList()
})
</script>

<style scoped>
.role-container {
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
</style>
