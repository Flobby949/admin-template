<template>
  <el-dialog
    v-model="visible"
    :title="isEdit ? '编辑公告' : '新增公告'"
    width="700px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="公告标题" prop="title">
        <el-input v-model="form.title" placeholder="请输入公告标题" maxlength="200" show-word-limit />
      </el-form-item>

      <el-form-item label="公告内容" prop="content">
        <div class="editor-container">
          <Toolbar
            :editor="editorRef"
            :default-config="toolbarConfig"
            mode="simple"
            style="border-bottom: 1px solid #ccc"
          />
          <Editor
            v-model="form.content"
            :default-config="editorConfig"
            mode="simple"
            style="height: 300px; overflow-y: hidden"
            @on-created="handleCreated"
          />
        </div>
      </el-form-item>

      <el-form-item label="排序">
        <el-input-number v-model="form.sortOrder" :min="0" :max="999" />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, shallowRef, onBeforeUnmount } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'
import type { IDomEditor, IToolbarConfig, IEditorConfig } from '@wangeditor/editor'
import '@wangeditor/editor/dist/css/style.css'
import {
  getNoticeById,
  createNotice,
  updateNotice,
  type NoticeForm
} from '@/api/cms/notice'

const props = defineProps<{
  modelValue: boolean
  noticeId?: number
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'success'): void
}>()

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const isEdit = computed(() => !!props.noticeId)

const formRef = ref<FormInstance>()
const submitting = ref(false)

const form = reactive<NoticeForm>({
  title: '',
  content: '',
  sortOrder: 0
})

const rules: FormRules = {
  title: [
    { required: true, message: '请输入公告标题', trigger: 'blur' },
    { max: 200, message: '标题长度不能超过200', trigger: 'blur' }
  ]
}

// 编辑器实例
const editorRef = shallowRef<IDomEditor>()

const toolbarConfig: Partial<IToolbarConfig> = {
  excludeKeys: ['uploadVideo', 'insertVideo', 'group-video']
}

const editorConfig: Partial<IEditorConfig> = {
  placeholder: '请输入公告内容...'
}

const handleCreated = (editor: IDomEditor) => {
  editorRef.value = editor
}

// 加载公告详情
const loadNoticeDetail = async (id: number) => {
  try {
    const data = await getNoticeById(id)
    form.title = data.title
    form.content = data.content || ''
    form.sortOrder = data.sortOrder
  } catch (error) {
    ElMessage.error('加载公告详情失败')
  }
}

// 重置表单
const resetForm = () => {
  form.title = ''
  form.content = ''
  form.sortOrder = 0
  formRef.value?.resetFields()
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        if (isEdit.value && props.noticeId) {
          await updateNotice(props.noticeId, form)
          ElMessage.success('更新成功')
        } else {
          await createNotice(form)
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

const handleClose = () => {
  resetForm()
  visible.value = false
}

watch(visible, (val) => {
  if (val && isEdit.value && props.noticeId) {
    loadNoticeDetail(props.noticeId)
  }
})

// 组件销毁时销毁编辑器
onBeforeUnmount(() => {
  const editor = editorRef.value
  if (editor) {
    editor.destroy()
  }
})
</script>

<style scoped lang="scss">
.editor-container {
  border: 1px solid #ccc;
  border-radius: 4px;
  overflow: hidden;
}
</style>
