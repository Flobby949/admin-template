<template>
  <el-drawer
    v-model="visible"
    :title="isEdit ? '编辑文章' : '新增文章'"
    direction="rtl"
    size="100%"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <div class="article-editor">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="16">
            <el-form-item label="文章标题" prop="title">
              <el-input v-model="form.title" placeholder="请输入文章标题" maxlength="200" show-word-limit />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="所属分类" prop="categoryId">
              <el-tree-select
                v-model="form.categoryId"
                :data="categoryOptions"
                :props="{ label: 'categoryName', value: 'id', children: 'children' }"
                placeholder="请选择分类"
                check-strictly
                clearable
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="文章摘要" prop="summary">
          <el-input
            v-model="form.summary"
            type="textarea"
            :rows="3"
            placeholder="请输入文章摘要"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="封面图片">
          <el-input v-model="form.coverUrl" placeholder="请输入封面图片URL" />
        </el-form-item>

        <el-form-item label="文章内容" prop="content">
          <div class="editor-container">
            <Toolbar
              :editor="editorRef"
              :default-config="toolbarConfig"
              mode="default"
              style="border-bottom: 1px solid #ccc"
            />
            <Editor
              v-model="form.content"
              :default-config="editorConfig"
              mode="default"
              style="height: 500px; overflow-y: hidden"
              @on-created="handleCreated"
            />
          </div>
        </el-form-item>

        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" :max="999" />
        </el-form-item>
      </el-form>
    </div>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
    </template>
  </el-drawer>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, shallowRef, onBeforeUnmount } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'
import type { IDomEditor, IToolbarConfig, IEditorConfig } from '@wangeditor/editor'
import '@wangeditor/editor/dist/css/style.css'
import {
  getArticleById,
  createArticle,
  updateArticle,
  type ArticleForm
} from '@/api/cms/article'
import { getCategoryTree, type CategoryVO } from '@/api/cms/category'

const props = defineProps<{
  modelValue: boolean
  articleId?: number
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'success'): void
}>()

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const isEdit = computed(() => !!props.articleId)

const formRef = ref<FormInstance>()
const submitting = ref(false)
const categoryOptions = ref<CategoryVO[]>([])

const form = reactive<ArticleForm>({
  title: '',
  summary: '',
  content: '',
  categoryId: undefined,
  coverUrl: '',
  sortOrder: 0
})

const rules: FormRules = {
  title: [
    { required: true, message: '请输入文章标题', trigger: 'blur' },
    { max: 200, message: '标题长度不能超过200', trigger: 'blur' }
  ]
}

// 编辑器实例
const editorRef = shallowRef<IDomEditor>()

const toolbarConfig: Partial<IToolbarConfig> = {}

const editorConfig: Partial<IEditorConfig> = {
  placeholder: '请输入文章内容...'
}

const handleCreated = (editor: IDomEditor) => {
  editorRef.value = editor
}

// 加载分类树
const loadCategoryTree = async () => {
  try {
    categoryOptions.value = await getCategoryTree()
  } catch (error) {
    ElMessage.error('加载分类树失败')
  }
}

// 加载文章详情
const loadArticleDetail = async (id: number) => {
  try {
    const data = await getArticleById(id)
    form.title = data.title
    form.summary = data.summary || ''
    form.content = data.content || ''
    form.categoryId = data.categoryId
    form.coverUrl = data.coverUrl || ''
    form.sortOrder = data.sortOrder
  } catch (error) {
    ElMessage.error('加载文章详情失败')
  }
}

// 重置表单
const resetForm = () => {
  form.title = ''
  form.summary = ''
  form.content = ''
  form.categoryId = undefined
  form.coverUrl = ''
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
        if (isEdit.value && props.articleId) {
          await updateArticle(props.articleId, form)
          ElMessage.success('更新成功')
        } else {
          await createArticle(form)
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
  if (val) {
    loadCategoryTree()
    if (isEdit.value && props.articleId) {
      loadArticleDetail(props.articleId)
    }
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
.article-editor {
  padding: 20px;

  .editor-container {
    border: 1px solid #ccc;
    border-radius: 4px;
    overflow: hidden;
  }
}
</style>
