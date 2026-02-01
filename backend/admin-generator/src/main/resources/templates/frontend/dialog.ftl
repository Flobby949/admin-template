<template>
  <el-dialog
    v-model="visible"
    :title="isEdit ? '编辑${entity.comment!entity.className}' : '新增${entity.comment!entity.className}'"
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
<#list entity.formFields as field>
<#if field.formType == "textarea">
      <el-form-item label="${field.comment!field.fieldName}" prop="${field.fieldName}">
        <el-input
          v-model="formData.${field.fieldName}"
          type="textarea"
          :rows="4"
          placeholder="请输入${field.comment!field.fieldName}"
        />
      </el-form-item>
<#elseif field.formType == "select">
      <el-form-item label="${field.comment!field.fieldName}" prop="${field.fieldName}">
        <el-select v-model="formData.${field.fieldName}" placeholder="请选择${field.comment!field.fieldName}" style="width: 100%">
          <!-- TODO: 添加选项 -->
          <el-option label="选项1" :value="1" />
          <el-option label="选项2" :value="2" />
        </el-select>
      </el-form-item>
<#elseif field.formType == "number">
      <el-form-item label="${field.comment!field.fieldName}" prop="${field.fieldName}">
        <el-input-number
          v-model="formData.${field.fieldName}"
          :min="0"
          placeholder="请输入${field.comment!field.fieldName}"
          style="width: 100%"
        />
      </el-form-item>
<#elseif field.formType == "switch">
      <el-form-item label="${field.comment!field.fieldName}" prop="${field.fieldName}">
        <el-switch v-model="formData.${field.fieldName}" />
      </el-form-item>
<#elseif field.formType == "datetime">
      <el-form-item label="${field.comment!field.fieldName}" prop="${field.fieldName}">
        <el-date-picker
          v-model="formData.${field.fieldName}"
          type="datetime"
          placeholder="请选择${field.comment!field.fieldName}"
          style="width: 100%"
        />
      </el-form-item>
<#elseif field.formType == "date">
      <el-form-item label="${field.comment!field.fieldName}" prop="${field.fieldName}">
        <el-date-picker
          v-model="formData.${field.fieldName}"
          type="date"
          placeholder="请选择${field.comment!field.fieldName}"
          style="width: 100%"
        />
      </el-form-item>
<#else>
      <el-form-item label="${field.comment!field.fieldName}" prop="${field.fieldName}">
        <el-input v-model="formData.${field.fieldName}" placeholder="请输入${field.comment!field.fieldName}" />
      </el-form-item>
</#if>
</#list>
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
import { get${entity.className}, create${entity.className}, update${entity.className}, type ${entity.className}DTO } from '@/api/${entity.classNameLower}'

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
const formData = reactive<${entity.className}DTO>({
<#list entity.formFields as field>
  ${field.fieldName}: <#if field.fieldType == "String">''<#elseif field.fieldType == "Boolean">false<#elseif field.fieldType == "Integer" || field.fieldType == "Long">0<#else>undefined</#if>,
</#list>
})

// 表单验证规则
const rules: FormRules = {
<#list entity.formFields as field>
<#if !field.isNullable>
  ${field.fieldName}: [
    { required: true, message: '请输入${field.comment!field.fieldName}', trigger: 'blur' }
  ],
</#if>
</#list>
}

// 监听 id 变化，加载数据
watch(
  () => props.id,
  async (newId) => {
    if (newId !== undefined) {
      try {
        const res: any = await get${entity.className}(newId)
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
<#list entity.formFields as field>
  formData.${field.fieldName} = <#if field.fieldType == "String">''<#elseif field.fieldType == "Boolean">false<#elseif field.fieldType == "Integer" || field.fieldType == "Long">0<#else>undefined as any</#if>
</#list>
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
      await update${entity.className}(props.id!, formData)
      ElMessage.success('更新成功')
    } else {
      await create${entity.className}(formData)
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
