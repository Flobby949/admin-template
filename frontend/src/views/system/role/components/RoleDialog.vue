<template>
  <el-dialog
    :model-value="visible"
    :title="roleId ? '编辑角色' : '新增角色'"
    width="600px"
    :close-on-click-modal="false"
    @update:model-value="$emit('update:visible', $event)"
    @open="handleOpen"
    @close="handleClose"
  >
    <el-form ref="formRef" v-loading="loading" :model="formData" :rules="rules" label-width="100px">
      <el-form-item label="角色名称" prop="roleName">
        <el-input v-model="formData.roleName" placeholder="请输入角色名称" />
      </el-form-item>
      <el-form-item label="角色编码" prop="roleCode">
        <el-input v-model="formData.roleCode" placeholder="请输入角色编码" :disabled="!!roleId" />
      </el-form-item>
      <el-form-item label="数据权限" prop="dataScope">
        <DataScopeSelect v-model="formData.dataScope" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="formData.status">
          <el-radio :value="1">启用</el-radio>
          <el-radio :value="0">禁用</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="菜单权限" prop="menuIds">
        <MenuTree ref="menuTreeRef" v-model="selectedMenuIds" :menu-tree="menuTree" />
      </el-form-item>
      <el-form-item label="备注" prop="remark">
        <el-input v-model="formData.remark" type="textarea" :rows="3" placeholder="请输入备注" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="$emit('update:visible', false)">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit"> 确定 </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch, computed } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import {
  getRole,
  createRole,
  updateRole,
  getMenuTree,
  type RoleForm,
  type MenuTreeVO
} from '@/api/role'
import MenuTree from './MenuTree.vue'
import DataScopeSelect from './DataScopeSelect.vue'

const props = defineProps<{
  visible: boolean
  roleId?: number
}>()

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'success'): void
}>()

// 表单引用
const formRef = ref<FormInstance>()
const menuTreeRef = ref()

// 状态
const loading = ref(false)
const submitting = ref(false)
const menuTree = ref<MenuTreeVO[]>([])

// 表单数据
const formData = reactive<RoleForm>({
  roleName: '',
  roleCode: '',
  dataScope: 3, // 默认值改为"仅本部门"(3)，更安全
  status: 1,
  remark: '',
  menuIds: []
})

// 计算属性：确保 menuIds 始终是数组
const selectedMenuIds = computed({
  get: () => formData.menuIds || [],
  set: (val: number[]) => {
    formData.menuIds = val
  }
})

// 表单验证规则
const rules: FormRules = {
  roleName: [
    { required: true, message: '请输入角色名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  roleCode: [
    { required: true, message: '请输入角色编码', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' },
    {
      pattern: /^[a-zA-Z][a-zA-Z0-9_]*$/,
      message: '必须以字母开头，只能包含字母、数字和下划线',
      trigger: 'blur'
    }
  ],
  dataScope: [{ required: true, message: '请选择数据权限', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

// 重置表单
const resetForm = () => {
  formData.roleName = ''
  formData.roleCode = ''
  formData.dataScope = 3 // 默认值改为"仅本部门"(3)
  formData.status = 1
  formData.remark = ''
  formData.menuIds = []
  formRef.value?.resetFields()
}

// 打开对话框
const handleOpen = async () => {
  loading.value = true
  try {
    // 加载菜单树
    const menuRes: any = await getMenuTree()
    menuTree.value = menuRes || []

    // 如果是编辑，加载角色数据
    if (props.roleId) {
      const role: any = await getRole(props.roleId)
      formData.roleName = role.roleName
      formData.roleCode = role.roleCode
      formData.dataScope = role.dataScope
      formData.status = role.status
      formData.remark = role.remark
      formData.menuIds = role.menuIds || []
    }
  } catch (error) {
    console.error('Failed to load data:', error)
  } finally {
    loading.value = false
  }
}

// 关闭对话框
const handleClose = () => {
  resetForm()
}

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value?.validate()

    submitting.value = true

    // 获取选中的菜单ID
    const menuIds = menuTreeRef.value?.getCheckedKeys() || formData.menuIds

    const data: RoleForm = {
      ...formData,
      menuIds
    }

    if (props.roleId) {
      await updateRole(props.roleId, data)
      ElMessage.success('更新成功')
    } else {
      await createRole(data)
      ElMessage.success('创建成功')
    }

    emit('update:visible', false)
    emit('success')
  } catch (error: any) {
    if (error !== 'cancel' && error?.message) {
      ElMessage.error(error.message)
    }
  } finally {
    submitting.value = false
  }
}

// 监听 roleId 变化
watch(
  () => props.roleId,
  () => {
    if (props.visible) {
      handleOpen()
    }
  }
)
</script>

<style scoped>
:deep(.el-select) {
  width: 100%;
}
</style>
