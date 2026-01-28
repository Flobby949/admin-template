<template>
  <el-dialog
    v-model="visible"
    :title="isEdit ? '编辑用户' : '新增用户'"
    width="600px"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
    >
      <el-form-item label="用户名" prop="username">
        <el-input
          v-model="form.username"
          placeholder="请输入用户名"
          :disabled="isEdit"
        />
      </el-form-item>
      <el-form-item label="密码" prop="password" v-if="!isEdit">
        <el-input
          v-model="form.password"
          type="password"
          placeholder="请输入密码"
          show-password
        />
      </el-form-item>
      <el-form-item label="姓名" prop="realName">
        <el-input
          v-model="form.realName"
          placeholder="请输入姓名"
        />
      </el-form-item>
      <el-form-item label="邮箱" prop="email">
        <el-input
          v-model="form.email"
          placeholder="请输入邮箱"
        />
      </el-form-item>
      <el-form-item label="手机号" prop="phone">
        <el-input
          v-model="form.phone"
          placeholder="请输入手机号"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="form.status">
          <el-radio :label="1">启用</el-radio>
          <el-radio :label="0">禁用</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="归属部门" prop="deptIds">
        <el-tree-select
          v-model="form.deptIds"
          :data="deptOptions"
          :props="{ value: 'id', label: 'deptName', children: 'children' }"
          value-key="id"
          multiple
          placeholder="请选择归属部门"
          check-strictly
          :render-after-expand="false"
          style="width: 100%"
        />
      </el-form-item>
      <el-form-item label="角色" prop="roleIds">
        <el-select
          v-model="form.roleIds"
          multiple
          placeholder="请选择角色"
          style="width: 100%"
        >
          <el-option
            v-for="role in roleList"
            :key="role.id"
            :label="role.name || role.roleName"
            :value="role.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="备注" prop="remark">
        <el-input
          v-model="form.remark"
          type="textarea"
          :rows="3"
          placeholder="请输入备注"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="submitting">
        确定
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch, computed } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { getUserById, createUser, updateUser, type UserDTO } from '@/api/user'
import { getRoleList, type RoleVO } from '@/api/role'
import { getDeptTree, type DeptVO } from '@/api/system/dept'

interface Props {
  modelValue: boolean
  userId?: number
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
  (e: 'success'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const isEdit = computed(() => !!props.userId)

// 表单
const formRef = ref<FormInstance>()
const form = reactive<UserDTO>({
  username: '',
  password: '',
  realName: '',
  email: '',
  phone: '',
  avatar: '',
  status: 1,
  roleIds: [],
  deptIds: [], // Keep array if backend supports multiple, but usually user belongs to one main dept.
  remark: ''
})
// Usually `deptId` (single) is better for primary department. 
// However, UserDTO interface in `src/api/user.ts` has `deptIds?: number[]`.
// Let's assume for now we select one department but store it in array or handle accordingly.
// Wait, standard RBAC often has one Dept per user. 
// Let's check `src/api/user.ts` again. It has `deptIds?: number[]`.
// And `UserVO` has `depts?: DeptInfo[]`.
// If the backend supports multiple departments, we should use multiple select. 
// But the prompt says "Department Selection" and usually it's single in basic implementations.
// Let's stick to the interface. I will add `deptId` to form if I need single select, or use `deptIds` for multiple.
// Given `UserDTO` has `deptIds`, I'll use that. 
// But typically users have one primary department. 
// I will check standard implementations. 
// If I use `deptIds`, I should use `multiple` in tree-select.
// However, the `DeptTree` on the left implies a single hierarchy. 
// I will assume single selection for simplicity and main organization structure, unless `deptIds` forces me.
// Actually, I'll modify `form` to use `deptIds` as array for compatibility.

// 角色列表
const roleList = ref<RoleVO[]>([])
// 部门列表
const deptOptions = ref<DeptVO[]>([])

// 提交状态
const submitting = ref(false)

// 加载角色列表
const loadRoleList = async () => {
  try {
    const { data } = await getRoleList({ pageNum: 1, pageSize: 100 })
    roleList.value = data.list
  } catch (error) {
    ElMessage.error('加载角色列表失败')
  }
}

// 加载部门列表
const loadDeptTree = async () => {
  try {
    const { data } = await getDeptTree()
    deptOptions.value = data
  } catch (error) {
    ElMessage.error('加载部门列表失败')
  }
}

// 加载用户详情
const loadUserDetail = async () => {
  if (!props.userId) return
  try {
    const { data } = await getUserById(props.userId)
    form.id = data.id
    form.username = data.username
    form.realName = data.realName || ''
    form.email = data.email || ''
    form.phone = data.phone || ''
    form.avatar = data.avatar || ''
    form.status = data.status
    form.roleIds = data.roles?.map(r => r.id) || []
    form.deptIds = data.depts?.map(d => d.id) || []
    form.remark = data.remark || ''
  } catch (error) {
    ElMessage.error('加载用户详情失败')
  }
}

// 重置表单
const resetForm = () => {
  form.id = undefined
  form.username = ''
  form.password = ''
  form.realName = ''
  form.email = ''
  form.phone = ''
  form.avatar = ''
  form.status = 1
  form.roleIds = []
  form.deptIds = []
  form.remark = ''
  formRef.value?.resetFields()
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        if (isEdit.value) {
          await updateUser(props.userId!, form)
          ElMessage.success('更新成功')
        } else {
          await createUser(form)
          ElMessage.success('创建成功')
        }
        emit('success')
        handleClose()
      } catch (error) {
        ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
      } finally {
        submitting.value = false
      }
    }
  })
}

// 关闭对话框
const handleClose = () => {
  resetForm()
  visible.value = false
}

// 监听对话框打开
watch(visible, (val) => {
  if (val) {
    loadRoleList()
    loadDeptTree()
    if (isEdit.value) {
      loadUserDetail()
    }
  }
})
</script>
