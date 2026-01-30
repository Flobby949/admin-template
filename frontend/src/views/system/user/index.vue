<template>
  <div class="app-container">
    <el-row :gutter="20">
      <!-- 部门树 -->
      <el-col :span="4" :xs="24">
        <div class="head-container">
          <dept-tree @node-click="handleDeptClick" />
        </div>
      </el-col>
      <!-- 用户数据 -->
      <el-col :span="20" :xs="24">
        <div class="user-container">
          <!-- 搜索栏 -->
          <el-card class="search-card" shadow="never">
            <el-form :model="queryForm" :inline="true" label-width="68px">
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
                <el-select v-model="queryForm.status" placeholder="请选择状态" clearable style="width: 240px">
                  <el-option label="启用" :value="1" />
                  <el-option label="禁用" :value="0" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
                <el-button icon="Refresh" @click="handleReset">重置</el-button>
              </el-form-item>
            </el-form>
          </el-card>

          <!-- 操作栏 -->
          <el-card class="toolbar-card" shadow="never">
            <el-row :gutter="10">
              <el-col :span="1.5">
                <el-button
                  type="primary"
                  plain
                  icon="Plus"
                  @click="handleAdd"
                  v-permission="'system:user:add'"
                >
                  新增
                </el-button>
              </el-col>
              <el-col :span="1.5">
                <el-button
                  type="danger"
                  plain
                  icon="Delete"
                  :disabled="selectedIds.length === 0"
                  @click="handleBatchDelete"
                  v-permission="'system:user:remove'"
                >
                  批量删除
                </el-button>
              </el-col>
            </el-row>
          </el-card>

          <!-- 数据表格 -->
          <el-card class="table-card" shadow="never">
            <el-table
              v-loading="loading"
              :data="userList"
              @selection-change="handleSelectionChange"
              border
              stripe
            >
              <el-table-column type="selection" width="55" align="center" />
              <el-table-column label="用户ID" prop="id" width="80" align="center" />
              <el-table-column label="用户名" prop="username" width="120" show-overflow-tooltip />
              <el-table-column label="姓名" prop="realName" width="120" show-overflow-tooltip />
              <el-table-column label="部门" prop="depts" width="150" show-overflow-tooltip>
                <template #default="{ row }">
                  <span v-for="(dept, index) in row.depts" :key="dept.id">
                    {{ dept.name }}{{ index < row.depts.length - 1 ? ', ' : '' }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column label="邮箱" prop="email" width="180" show-overflow-tooltip />
              <el-table-column label="手机号" prop="phone" width="120" />
              <el-table-column label="状态" width="80" align="center">
                <template #default="{ row }">
                  <el-switch
                    v-model="row.status"
                    :active-value="1"
                    :inactive-value="0"
                    @change="handleStatusChange(row)"
                    v-permission="'system:user:edit'"
                  />
                </template>
              </el-table-column>
              <el-table-column label="创建时间" prop="createTime" width="180" />
              <el-table-column label="操作" fixed="right" width="240" align="center">
                <template #default="{ row }">
                  <el-button
                    type="primary"
                    link
                    icon="Edit"
                    @click="handleEdit(row)"
                    v-permission="'system:user:edit'"
                  >
                    编辑
                  </el-button>
                  <el-button
                    type="primary"
                    link
                    icon="Key"
                    @click="handleResetPassword(row)"
                    v-permission="'system:user:resetPwd'"
                  >
                    重置
                  </el-button>
                  <el-button
                    type="danger"
                    link
                    icon="Delete"
                    @click="handleDelete(row)"
                    v-permission="'system:user:remove'"
                  >
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>

            <!-- 分页 -->
            <el-pagination
              v-model:current-page="queryForm.pageNum"
              v-model:page-size="queryForm.pageSize"
              :total="total"
              :page-sizes="[10, 20, 50, 100]"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleQuery"
              @current-change="handleQuery"
            />
          </el-card>

          <!-- 用户对话框 -->
          <UserDialog
            v-model="dialogVisible"
            :user-id="currentUserId"
            @success="handleQuery"
          />

          <!-- 重置密码对话框 -->
          <el-dialog
            v-model="passwordDialogVisible"
            title="重置密码"
            width="400px"
          >
            <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="80px">
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
      </el-col>
    </el-row>
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
  // Note: we usually don't reset deptId when resetting search form if user selected it from tree
  // But standard "Reset" button usually resets everything.
  // Let's reset everything for now.
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
  await passwordFormRef.value.validate(async (valid) => {
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
    // 恢复原状态
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
.app-container {
  padding: 20px;
}
.user-container {
  .search-card,
  .toolbar-card,
  .table-card {
    margin-bottom: 20px;
  }

  .el-pagination {
    margin-top: 20px;
    justify-content: flex-end;
  }
}
</style>