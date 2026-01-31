<template>
  <div class="user-page">
    <el-row :gutter="20">
      <!-- 部门树 -->
      <el-col :span="4" :xs="24">
        <div class="dept-tree-container">
          <dept-tree @node-click="handleDeptClick" />
        </div>
      </el-col>

      <!-- 用户数据 -->
      <el-col :span="20" :xs="24">
        <!-- 页面标题 -->
        <div class="page-header">
          <div class="header-left">
            <h2 class="page-title">用户管理</h2>
            <span class="page-subtitle">管理系统用户及权限分配</span>
          </div>
          <div class="header-actions">
            <el-button
              v-permission="'system:user:add'"
              type="primary"
              icon="Plus"
              @click="handleAdd"
            >
              新增用户
            </el-button>
            <el-button
              v-permission="'system:user:remove'"
              type="danger"
              plain
              icon="Delete"
              :disabled="selectedIds.length === 0"
              @click="handleBatchDelete"
            >
              批量删除
            </el-button>
          </div>
        </div>

        <!-- 主内容卡片 -->
        <div class="main-card">
          <!-- 搜索区域 -->
          <div class="filter-container">
            <el-form :model="queryForm" :inline="true" class="filter-form">
              <el-form-item label="用户名">
                <el-input
                  v-model="queryForm.username"
                  placeholder="请输入用户名"
                  clearable
                  @keyup.enter="handleQuery"
                />
              </el-form-item>
              <el-form-item label="姓名">
                <el-input
                  v-model="queryForm.realName"
                  placeholder="请输入姓名"
                  clearable
                  @keyup.enter="handleQuery"
                />
              </el-form-item>
              <el-form-item label="手机号">
                <el-input
                  v-model="queryForm.phone"
                  placeholder="请输入手机号"
                  clearable
                  @keyup.enter="handleQuery"
                />
              </el-form-item>
              <el-form-item label="状态">
                <el-select
                  v-model="queryForm.status"
                  placeholder="全部状态"
                  clearable
                  style="width: 140px"
                >
                  <el-option label="启用" :value="1" />
                  <el-option label="禁用" :value="0" />
                </el-select>
              </el-form-item>
              <el-form-item class="filter-actions">
                <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
                <el-button icon="Refresh" @click="handleReset">重置</el-button>
              </el-form-item>
            </el-form>
          </div>

          <!-- 数据表格 -->
          <el-table
            v-loading="loading"
            :data="userList"
            class="modern-table"
            @selection-change="handleSelectionChange"
          >
            <el-table-column type="selection" width="50" align="center" />
            <el-table-column label="用户ID" prop="id" width="80" align="center" />
            <el-table-column label="用户名" prop="username" min-width="120" show-overflow-tooltip>
              <template #default="{ row }">
                <span class="user-name">{{ row.username }}</span>
              </template>
            </el-table-column>
            <el-table-column label="姓名" prop="realName" min-width="100" show-overflow-tooltip />
            <el-table-column label="部门" prop="depts" min-width="150" show-overflow-tooltip>
              <template #default="{ row }">
                <el-tag v-for="dept in row.depts" :key="dept.id" size="small" class="dept-tag">
                  {{ dept.name }}
                </el-tag>
                <span v-if="!row.depts?.length" class="text-placeholder">-</span>
              </template>
            </el-table-column>
            <el-table-column label="邮箱" prop="email" min-width="180" show-overflow-tooltip />
            <el-table-column label="手机号" prop="phone" width="130" />
            <el-table-column label="状态" width="90" align="center">
              <template #default="{ row }">
                <el-switch
                  v-model="row.status"
                  v-permission="'system:user:edit'"
                  :active-value="1"
                  :inactive-value="0"
                  @change="handleStatusChange(row)"
                />
              </template>
            </el-table-column>
            <el-table-column label="创建时间" prop="createTime" width="170" />
            <el-table-column label="操作" fixed="right" width="200" align="center">
              <template #default="{ row }">
                <div class="action-buttons">
                  <el-button
                    v-permission="'system:user:edit'"
                    type="primary"
                    link
                    icon="Edit"
                    @click="handleEdit(row)"
                  >
                    编辑
                  </el-button>
                  <el-button
                    v-permission="'system:user:resetPwd'"
                    type="primary"
                    link
                    icon="Key"
                    @click="handleResetPassword(row)"
                  >
                    重置
                  </el-button>
                  <el-button
                    v-permission="'system:user:remove'"
                    type="danger"
                    link
                    icon="Delete"
                    @click="handleDelete(row)"
                  >
                    删除
                  </el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>

          <!-- 分页 -->
          <div class="pagination-container">
            <el-pagination
              v-model:current-page="queryForm.pageNum"
              v-model:page-size="queryForm.pageSize"
              :total="total"
              :page-sizes="[10, 20, 50, 100]"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleQuery"
              @current-change="handleQuery"
            />
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 用户对话框 -->
    <UserDialog v-model="dialogVisible" :user-id="currentUserId" @success="handleQuery" />

    <!-- 重置密码对话框 -->
    <el-dialog
      v-model="passwordDialogVisible"
      title="重置密码"
      width="420px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="passwordFormRef"
        :model="passwordForm"
        :rules="passwordRules"
        label-width="90px"
      >
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            placeholder="请输入新密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            show-password
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmResetPassword">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  getUserList,
  deleteUser,
  batchDeleteUsers,
  resetPassword,
  changeStatus,
  type UserQuery,
  type UserVO
} from '@/api/user'
import UserDialog from './components/UserDialog.vue'
import DeptTree from './components/DeptTree.vue'

// 查询表单
const queryForm = reactive<UserQuery>({
  username: '',
  realName: '',
  phone: '',
  status: undefined,
  deptId: undefined,
  pageNum: 1,
  pageSize: 10
})

// 数据列表
const loading = ref(false)
const userList = ref<UserVO[]>([])
const total = ref(0)
const selectedIds = ref<number[]>([])

// 对话框
const dialogVisible = ref(false)
const currentUserId = ref<number>()

// 重置密码对话框
const passwordDialogVisible = ref(false)
const passwordFormRef = ref<FormInstance>()
const passwordForm = reactive({
  userId: 0,
  newPassword: '',
  confirmPassword: ''
})

const passwordRules: FormRules = {
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度必须在6-20个字符之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 查询用户列表
const handleQuery = async () => {
  loading.value = true
  try {
    const data = await getUserList(queryForm)
    userList.value = data.list
    total.value = data.total
  } catch (error) {
    ElMessage.error('查询用户列表失败')
  } finally {
    loading.value = false
  }
}

// 重置查询
const handleReset = () => {
  queryForm.username = ''
  queryForm.realName = ''
  queryForm.phone = ''
  queryForm.status = undefined
  queryForm.deptId = undefined
  queryForm.pageNum = 1
  queryForm.pageSize = 10
  handleQuery()
}

// Handle Dept Click
const handleDeptClick = (deptId: number | undefined) => {
  queryForm.deptId = deptId
  handleQuery()
}

// 新增用户
const handleAdd = () => {
  currentUserId.value = undefined
  dialogVisible.value = true
}

// 编辑用户
const handleEdit = (row: UserVO) => {
  currentUserId.value = row.id
  dialogVisible.value = true
}

// 删除用户
const handleDelete = async (row: UserVO) => {
  try {
    await ElMessageBox.confirm(`确定要删除用户"${row.username}"吗？`, '提示', {
      type: 'warning'
    })
    await deleteUser(row.id)
    ElMessage.success('删除成功')
    handleQuery()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 批量删除
const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(`确定要删除选中的${selectedIds.value.length}个用户吗？`, '提示', {
      type: 'warning'
    })
    await batchDeleteUsers(selectedIds.value)
    ElMessage.success('删除成功')
    handleQuery()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 重置密码
const handleResetPassword = (row: UserVO) => {
  passwordForm.userId = row.id
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  passwordDialogVisible.value = true
}

// 确认重置密码
const handleConfirmResetPassword = async () => {
  if (!passwordFormRef.value) return
  await passwordFormRef.value.validate(async valid => {
    if (valid) {
      try {
        await resetPassword(passwordForm.userId, passwordForm.newPassword)
        ElMessage.success('密码重置成功')
        passwordDialogVisible.value = false
      } catch (error) {
        ElMessage.error('密码重置失败')
      }
    }
  })
}

// 修改状态
const handleStatusChange = async (row: UserVO) => {
  try {
    await changeStatus(row.id, row.status)
    ElMessage.success('状态修改成功')
  } catch (error) {
    ElMessage.error('状态修改失败')
    row.status = row.status === 1 ? 0 : 1
  }
}

// 选择变化
const handleSelectionChange = (selection: UserVO[]) => {
  selectedIds.value = selection.map(item => item.id)
}

// 初始化
onMounted(() => {
  handleQuery()
})
</script>

<style scoped lang="scss">
.user-page {
  height: 100%;
}

.dept-tree-container {
  padding: 16px 16px 16px 0;
  border-right: 1px solid var(--border-light);
  height: calc(100vh - 64px - 48px);
  overflow: hidden;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;

  .header-left {
    .page-title {
      font-size: 22px;
      font-weight: 700;
      color: var(--text-main);
      margin: 0 0 4px 0;
    }

    .page-subtitle {
      font-size: 14px;
      color: var(--text-secondary);
    }
  }

  .header-actions {
    display: flex;
    gap: 12px;
  }
}

.main-card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-card);
  border: 1px solid var(--border-light);
  padding: 24px;
}

.filter-container {
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px dashed var(--border-color);

  .filter-form {
    display: flex;
    flex-wrap: wrap;
    gap: 8px 16px;

    :deep(.el-form-item) {
      margin-bottom: 0;
      margin-right: 0;
    }

    :deep(.el-form-item__label) {
      font-weight: 500;
    }

    .filter-actions {
      margin-left: auto;
    }
  }
}

.modern-table {
  :deep(.el-table__header th) {
    background-color: var(--bg-body) !important;
  }

  .user-name {
    font-weight: 500;
    color: var(--text-main);
  }

  .dept-tag {
    margin-right: 4px;
    margin-bottom: 2px;
  }

  .text-placeholder {
    color: var(--text-placeholder);
  }

  .action-buttons {
    display: flex;
    justify-content: center;
    gap: 4px;
  }
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

/* 响应式 */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    gap: 16px;

    .header-actions {
      width: 100%;
      justify-content: flex-start;
    }
  }

  .filter-container .filter-form {
    .filter-actions {
      margin-left: 0;
      width: 100%;
    }
  }

  .dept-tree-container {
    height: auto;
    max-height: 300px;
    margin-bottom: 16px;
    border-right: none;
    padding: 0 0 16px 0;
  }
}
</style>
