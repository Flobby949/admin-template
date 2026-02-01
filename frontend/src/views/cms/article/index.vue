<template>
  <div class="article-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>文章管理</span>
          <el-button
            v-permission="'cms:article:add'"
            type="primary"
            icon="Plus"
            @click="handleAdd"
          >
            新增文章
          </el-button>
        </div>
      </template>

      <!-- 搜索表单 -->
      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item label="文章标题">
          <el-input
            v-model="queryParams.title"
            placeholder="请输入文章标题"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="分类">
          <el-tree-select
            v-model="queryParams.categoryId"
            :data="categoryOptions"
            :props="{ label: 'categoryName', value: 'id', children: 'children' }"
            placeholder="请选择分类"
            check-strictly
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="queryParams.status"
            placeholder="请选择状态"
            clearable
            style="width: 150px"
          >
            <el-option label="草稿" :value="0" />
            <el-option label="待审核" :value="1" />
            <el-option label="已发布" :value="2" />
            <el-option label="已下架" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
          <el-button icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格 -->
      <el-table v-loading="loading" :data="articleList" border stripe>
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="categoryName" label="分类" width="120" align="center" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="viewCount" label="浏览量" width="100" align="center" />
        <el-table-column prop="createBy" label="作者" width="100" align="center" />
        <el-table-column prop="createTime" label="创建时间" width="170" align="center" />
        <el-table-column label="操作" width="280" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              v-permission="'cms:article:edit'"
              type="primary"
              link
              icon="Edit"
              @click="handleEdit(row)"
            >
              编辑
            </el-button>
            <el-button
              v-if="row.status === 0"
              v-permission="'cms:article:edit'"
              type="warning"
              link
              icon="Upload"
              @click="handleSubmit(row)"
            >
              提交
            </el-button>
            <el-button
              v-if="row.status === 1 || row.status === 3"
              v-permission="'cms:article:publish'"
              type="success"
              link
              icon="Check"
              @click="handlePublish(row)"
            >
              发布
            </el-button>
            <el-button
              v-if="row.status === 1"
              v-permission="'cms:article:publish'"
              type="warning"
              link
              icon="Close"
              @click="handleReject(row)"
            >
              驳回
            </el-button>
            <el-button
              v-if="row.status === 2"
              v-permission="'cms:article:publish'"
              type="danger"
              link
              icon="Download"
              @click="handleRevoke(row)"
            >
              下架
            </el-button>
            <el-button
              v-permission="'cms:article:delete'"
              type="danger"
              link
              icon="Delete"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleQuery"
          @current-change="handleQuery"
        />
      </div>
    </el-card>

    <!-- 编辑抽屉 -->
    <article-drawer
      v-model="drawer.visible"
      :article-id="drawer.articleId"
      @success="handleQuery"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getArticleList,
  deleteArticle,
  submitArticle,
  publishArticle,
  rejectArticle,
  revokeArticle,
  type ArticleVO,
  type ArticleQuery
} from '@/api/cms/article'
import { getCategoryTree, type CategoryVO } from '@/api/cms/category'
import ArticleDrawer from './components/ArticleDrawer.vue'

const queryParams = reactive<ArticleQuery>({
  title: undefined,
  categoryId: undefined,
  status: undefined,
  pageNum: 1,
  pageSize: 10
})

const loading = ref(false)
const articleList = ref<ArticleVO[]>([])
const total = ref(0)
const categoryOptions = ref<CategoryVO[]>([])

const drawer = reactive({
  visible: false,
  articleId: undefined as number | undefined
})

const getStatusType = (status: number) => {
  const types: Record<number, string> = {
    0: 'info',
    1: 'warning',
    2: 'success',
    3: 'danger'
  }
  return types[status] || 'info'
}

const getStatusText = (status: number) => {
  const texts: Record<number, string> = {
    0: '草稿',
    1: '待审核',
    2: '已发布',
    3: '已下架'
  }
  return texts[status] || '未知'
}

const loadCategoryTree = async () => {
  try {
    categoryOptions.value = await getCategoryTree()
  } catch (error) {
    console.error('加载分类树失败', error)
  }
}

const handleQuery = async () => {
  loading.value = true
  try {
    const data = await getArticleList(queryParams)
    articleList.value = data.list
    total.value = Number(data.total)
  } catch (error) {
    ElMessage.error('查询文章列表失败')
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  queryParams.title = undefined
  queryParams.categoryId = undefined
  queryParams.status = undefined
  queryParams.pageNum = 1
  handleQuery()
}

const handleAdd = () => {
  drawer.articleId = undefined
  drawer.visible = true
}

const handleEdit = (row: ArticleVO) => {
  drawer.articleId = row.id
  drawer.visible = true
}

const handleDelete = (row: ArticleVO) => {
  ElMessageBox.confirm(`确认删除文章"${row.title}"吗？`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      try {
        await deleteArticle(row.id)
        ElMessage.success('删除成功')
        handleQuery()
      } catch (error) {
        ElMessage.error('删除失败')
      }
    })
    .catch(() => {})
}

const handleSubmit = async (row: ArticleVO) => {
  try {
    await submitArticle(row.id)
    ElMessage.success('提交审核成功')
    handleQuery()
  } catch (error) {
    ElMessage.error('提交审核失败')
  }
}

const handlePublish = async (row: ArticleVO) => {
  try {
    await publishArticle(row.id)
    ElMessage.success('发布成功')
    handleQuery()
  } catch (error) {
    ElMessage.error('发布失败')
  }
}

const handleReject = async (row: ArticleVO) => {
  try {
    await rejectArticle(row.id)
    ElMessage.success('驳回成功')
    handleQuery()
  } catch (error) {
    ElMessage.error('驳回失败')
  }
}

const handleRevoke = async (row: ArticleVO) => {
  try {
    await revokeArticle(row.id)
    ElMessage.success('下架成功')
    handleQuery()
  } catch (error) {
    ElMessage.error('下架失败')
  }
}

onMounted(() => {
  loadCategoryTree()
  handleQuery()
})
</script>

<style scoped lang="scss">
.article-container {
  padding: 20px;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .search-form {
    margin-bottom: 20px;
  }

  .pagination-container {
    display: flex;
    justify-content: flex-end;
    margin-top: 20px;
  }
}
</style>
