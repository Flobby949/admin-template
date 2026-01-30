<template>
  <el-dialog
    :title="title"
    :model-value="modelValue"
    width="600px"
    @update:model-value="handleUpdateModelValue"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
    >
      <el-form-item label="上级部门" prop="parentId">
        <el-tree-select
          v-model="form.parentId"
          :data="deptOptions"
          :props="{ value: 'id', label: 'deptName', children: 'children' }"
          value-key="id"
          placeholder="选择上级部门"
          check-strictly
          :render-after-expand="false"
          style="width: 100%"
        />
      </el-form-item>
      <el-form-item label="部门名称" prop="deptName">
        <el-input v-model="form.deptName" placeholder="请输入部门名称" />
      </el-form-item>
      <el-form-item label="显示排序" prop="sortOrder">
        <el-input-number v-model="form.sortOrder" :min="0" :max="9999" />
      </el-form-item>
      <el-form-item label="负责人" prop="leader">
        <el-input v-model="form.leader" placeholder="请输入负责人" maxlength="20" />
      </el-form-item>
      <el-form-item label="联系电话" prop="phone">
        <el-input v-model="form.phone" placeholder="请输入联系电话" maxlength="11" />
      </el-form-item>
      <el-form-item label="邮箱" prop="email">
        <el-input v-model="form.email" placeholder="请输入邮箱" maxlength="50" />
      </el-form-item>
      <el-form-item label="部门状态" prop="status">
        <el-radio-group v-model="form.status">
          <el-radio :label="1">正常</el-radio>
          <el-radio :label="0">停用</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取 消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="loading">确 定</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch, toRefs } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { getDeptTree, getDept, addDept, updateDept, type DeptForm, type DeptVO } from '@/api/system/dept'

const props = defineProps<{
  modelValue: boolean
  title: string
  isEdit: boolean
  deptId?: number
  parentId?: number
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'success'): void
}>()

const { modelValue, deptId, parentId } = toRefs(props)
const formRef = ref<FormInstance>()
const loading = ref(false)
const deptOptions = ref<DeptVO[]>([])

const form = reactive<DeptForm>({
  id: undefined,
  parentId: undefined,
  deptName: '',
  sortOrder: 0,
  leader: '',
  phone: '',
  email: '',
  status: 1
})

const rules: FormRules = {
  parentId: [{ required: true, message: '请选择上级部门', trigger: 'change' }],
  deptName: [{ required: true, message: '请输入部门名称', trigger: 'blur' }],
  sortOrder: [{ required: true, message: '请输入显示排序', trigger: 'blur' }],
  email: [{ type: 'email', message: '请输入正确的邮箱地址', trigger: ['blur', 'change'] }],
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: ['blur', 'change'] }]
}

// Reset form
const reset = () => {
  form.id = undefined
  form.parentId = undefined
  form.deptName = ''
  form.sortOrder = 0
  form.leader = ''
  form.phone = ''
  form.email = ''
  form.status = 1
  formRef.value?.resetFields()
}

// Handle dialog open
watch(() => props.modelValue, async (val) => {
  if (val) {
    reset()
    await getTreeselect()
    if (props.isEdit && props.deptId) {
      await loadDept(props.deptId)
    } else if (props.parentId) {
      form.parentId = props.parentId
    }
  }
})

// Get Dept Tree for Select
const getTreeselect = async () => {
  try {
    const data = await getDeptTree()
    deptOptions.value = [{ id: 0, deptName: '顶级部门', children: [] } as any].concat(data)
    
    // If editing, disable current node and children to prevent cycle
    if (props.isEdit && props.deptId) {
      disableDeptTree(deptOptions.value, props.deptId)
    }
  } catch (error) {
    ElMessage.error('获取部门列表失败')
  }
}

// Disable current node and children
const disableDeptTree = (tree: any[], targetId: number) => {
  for (const node of tree) {
    if (node.id === targetId) {
      node.disabled = true
      if (node.children) {
        disableChildren(node.children)
      }
    } else if (node.children) {
      disableDeptTree(node.children, targetId)
    }
  }
}

const disableChildren = (children: any[]) => {
  for (const child of children) {
    child.disabled = true
    if (child.children) {
      disableChildren(child.children)
    }
  }
}

// Load dept details
const loadDept = async (id: number) => {
  try {
    const data = await getDept(id)
    Object.assign(form, data)
  } catch (error) {
    ElMessage.error('获取部门详情失败')
  }
}

const handleUpdateModelValue = (val: boolean) => {
  emit('update:modelValue', val)
}

const handleClose = () => {
  emit('update:modelValue', false)
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        if (props.isEdit) {
          await updateDept(form.id!, form)
          ElMessage.success('修改成功')
        } else {
          await addDept(form)
          ElMessage.success('新增成功')
        }
        emit('success')
        handleClose()
      } catch (error) {
        ElMessage.error(props.isEdit ? '修改失败' : '新增失败')
      } finally {
        loading.value = false
      }
    }
  })
}
</script>
