<template>
  <el-dialog v-model="visible" :title="title" width="500px" @close="handleClose">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="上级分类">
        <el-tree-select
          v-model="form.parentId"
          :data="categoryOptions"
          :props="{ label: 'categoryName', value: 'id', children: 'children' }"
          placeholder="请选择上级分类"
          check-strictly
          clearable
          style="width: 100%"
        />
      </el-form-item>
      <el-form-item label="分类名称" prop="categoryName">
        <el-input v-model="form.categoryName" placeholder="请输入分类名称" />
      </el-form-item>
      <el-form-item label="排序" prop="sortOrder">
        <el-input-number v-model="form.sortOrder" :min="0" :max="999" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="form.status">
          <el-radio :value="1">正常</el-radio>
          <el-radio :value="0">停用</el-radio>
        </el-radio-group>
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
import {
  getCategoryTree,
  getCategoryById,
  createCategory,
  updateCategory,
  type CategoryVO,
  type CategoryForm
} from '@/api/cms/category'

const props = defineProps<{
  modelValue: boolean
  title: string
  isEdit: boolean
  categoryId?: number
  parentId?: number
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'success'): void
}>()

const visible = computed({
  get: () => props.modelValue,
  set: val => emit('update:modelValue', val)
})

const formRef = ref<FormInstance>()
const submitting = ref(false)
const categoryOptions = ref<CategoryVO[]>([])

const form = reactive<CategoryForm>({
  parentId: 0,
  categoryName: '',
  sortOrder: 0,
  status: 1
})

const rules: FormRules = {
  categoryName: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { max: 100, message: '分类名称长度不能超过100', trigger: 'blur' }
  ]
}

// 加载分类树
const loadCategoryTree = async () => {
  try {
    const data = await getCategoryTree()
    categoryOptions.value = [{ id: 0, categoryName: '顶级分类', children: data } as CategoryVO]
  } catch (error) {
    ElMessage.error('加载分类树失败')
  }
}

// 加载分类详情
const loadCategoryDetail = async (id: number) => {
  try {
    const data = await getCategoryById(id)
    form.parentId = data.parentId
    form.categoryName = data.categoryName
    form.sortOrder = data.sortOrder
    form.status = data.status
  } catch (error) {
    ElMessage.error('加载分类详情失败')
  }
}

// 重置表单
const resetForm = () => {
  form.parentId = props.parentId || 0
  form.categoryName = ''
  form.sortOrder = 0
  form.status = 1
  formRef.value?.resetFields()
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async valid => {
    if (valid) {
      submitting.value = true
      try {
        if (props.isEdit && props.categoryId) {
          await updateCategory(props.categoryId, form)
          ElMessage.success('更新成功')
        } else {
          await createCategory(form)
          ElMessage.success('创建成功')
        }
        emit('success')
        handleClose()
      } catch (error) {
        ElMessage.error(props.isEdit ? '更新失败' : '创建失败')
      } finally {
        submitting.value = false
      }
    }
  })
}

const handleClose = () => {
  resetForm()
  visible.value = false
}

watch(visible, val => {
  if (val) {
    loadCategoryTree()
    if (props.isEdit && props.categoryId) {
      loadCategoryDetail(props.categoryId)
    } else {
      form.parentId = props.parentId || 0
    }
  }
})
</script>
