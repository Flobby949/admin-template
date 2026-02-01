<template>
  <el-dialog
    v-model="visible"
    :title="isEdit ? '编辑区域表' : '新增区域表'"
    width="600px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="rules"
      label-width="100px"
    >
      <el-form-item label="区域名称" prop="name">
        <el-input v-model="formData.name" placeholder="请输入区域名称" />
      </el-form-item>
      <el-form-item label="区域编码" prop="regionCode">
        <el-input v-model="formData.regionCode" placeholder="请输入区域编码" />
      </el-form-item>
      <el-form-item label="父级编码" prop="parentCode">
        <el-input v-model="formData.parentCode" placeholder="请输入父级编码" />
      </el-form-item>
      <el-form-item label="层级：1-省，2-市，3-区县" prop="level">
        <el-input-number
          v-model="formData.level"
          :min="0"
          placeholder="请输入层级：1-省，2-市，3-区县"
          style="width: 100%"
        />
      </el-form-item>
      <el-form-item label="排序" prop="sortOrder">
        <el-input-number
          v-model="formData.sortOrder"
          :min="0"
          placeholder="请输入排序"
          style="width: 100%"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="formData.status" placeholder="请选择状态" style="width: 100%">
          <el-option label="启用" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch, computed } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { getRegion, createRegion, updateRegion, type RegionDTO } from '@/api/region'

const props = defineProps<{
  visible: boolean
  id?: number
}>()

const emit = defineEmits<{
  'update:visible': [value: boolean]
  success: []
}>()

const formRef = ref<FormInstance>()
const submitting = ref(false)

const isEdit = computed(() => props.id !== undefined)

// 表单数据
const formData = reactive<RegionDTO>({
  name: '',
  regionCode: '',
  parentCode: '',
  level: 0,
  sortOrder: 0,
  status: 0,
})

// 表单验证规则
const rules: FormRules = {
  name: [
    { required: true, message: '请输入区域名称', trigger: 'blur' }
  ],
  regionCode: [
    { required: true, message: '请输入区域编码', trigger: 'blur' }
  ],
  level: [
    { required: true, message: '请输入层级：1-省，2-市，3-区县', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请输入状态：0-禁用，1-启用', trigger: 'blur' }
  ],
}

// 监听 id 变化，加载数据
watch(
  () => props.id,
  async (newId) => {
    if (newId !== undefined) {
      try {
        const res: any = await getRegion(newId)
        Object.assign(formData, res)
      } catch (error) {
        console.error('Failed to fetch detail:', error)
      }
    } else {
      resetForm()
    }
  },
  { immediate: true }
)

// 重置表单
const resetForm = () => {
  formData.name = ''
  formData.regionCode = ''
  formData.parentCode = ''
  formData.level = 0
  formData.sortOrder = 0
  formData.status = 0
}

// 关闭对话框
const handleClose = () => {
  emit('update:visible', false)
  resetForm()
  formRef.value?.resetFields()
}

// 提交表单
const handleSubmit = async () => {
  const valid = await formRef.value?.validate()
  if (!valid) return

  submitting.value = true
  try {
    if (isEdit.value) {
      await updateRegion(props.id!, formData)
      ElMessage.success('更新成功')
    } else {
      await createRegion(formData)
      ElMessage.success('创建成功')
    }
    emit('success')
    handleClose()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    submitting.value = false
  }
}
</script>
