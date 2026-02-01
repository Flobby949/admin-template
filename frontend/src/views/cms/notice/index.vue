<template>
  <div class="notice-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>通知公告</span>
          <el-button
            v-permission="'cms:notice:add'"
            type="primary"
            icon="Plus"
            @click="handleAdd"
          >
            新增公告
          </el-button>
        </div>
      </template>

      <!-- 搜索表单 -->
      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item label="公告标题">
          <el-input
            v-model="queryParams.title"
            placeholder="请输入公告标题"
            clearable
            @keyup.enter="handleQuery"
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
            <el-option label="已发布" :value="1" />
            <el-option label="已撤回" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
          <el-button icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格 -->
      <el-table v-loading="loading" :data="noticeList" border stripe>
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="readCount" label="已读人数" width="100" align="center" />
        <el-table-column prop="createBy" label="创建人" width="100" align="center" />
        <el-table-column prop="publishTime" label="发布时间" width="170" align="center" />
        <el-table-column prop="createTime" label="创建时间" width="170" align="center" />
        <el-table-column label="操作" width="240" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              v-permission="'cms:notice:edit'"
              type="primary"
              link
              icon="Edit"
              @click="handleEdit(row)"
            >
              编辑
            </el-button>
            <el-button
              v-if="row.status === 0 || row.status === 2"
              v-permission="'cms:notice:publish'"
              type="success"
              link
              icon="Check"
              @click="handlePublish(row)"
            >
              发布
            </el-button>
            <el-button
              v-if="row.status === 1"
              v-permission="'cms:notice:publish'"
              type="warning"
              link
              icon="Close"
              @click="handleRevoke(row)"
            >
              撤回
            </el-button>
            <el-button
              v-permission="'cms:notice:delete'"
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

    <!-- 编辑弹窗 -->
    <notice-dialog
      v-model="dialog.visible"
      :notice-id="dialog.noticeId"
      @success="handleQuery"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getNoticeList,
  deleteNotice,
  publishNotice,
  revokeNotice,
  type NoticeVO,
  type NoticeQuery
} from '@/api/cms/notice'
import NoticeDialog from './components/NoticeDialog.vue'

const queryParams = reactive<NoticeQuery>({
  title: undefined,
  status: undefined,
  pageNum: 1,
  pageSize: 10
})

const loading = ref(false)
const noticeList = ref<NoticeVO[]>([])
const total = ref(0)

const dialog = reactive({
  visible: false,
  noticeId: undefined as number | undefined
})

const getStatusType = (status: number) => {
  const types: Record<number, string> = {
    0: 'info',
    1: 'success',
    2: 'danger'
  }
  return types[status] || 'info'
}

const getStatusText = (status: number) => {
  const texts: Record<number, string> = {
    0: '草稿',
    1: '已发布',
    2: '已撤回'
  }
  return texts[status] || '未知'
}

const handleQuery = async () => {
  loading.value = true
  try {
    const data = await getNoticeList(queryParams)
    noticeList.value = data.list
    total.value = Number(data.total)
  } catch (error) {
    ElMessage.error('查询公告列表失败')
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  queryParams.title = undefined
  queryParams.status = undefined
  queryParams.pageNum = 1
  handleQuery()
}

const handleAdd = () => {
  dialog.noticeId = undefined
  dialog.visible = true
}

const handleEdit = (row: NoticeVO) => {
  dialog.noticeId = row.id
  dialog.visible = true
}

const handleDelete = (row: NoticeVO) => {
  ElMessageBox.confirm(`确认删除公告"${row.title}"吗？`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      try {
        await deleteNotice(row.id)
        ElMessage.success('删除成功')
        handleQuery()
      } catch (error) {
        ElMessage.error('删除失败')
      }
    })
    .catch(() => {})
}

const handlePublish = async (row: NoticeVO) => {
  try {
    await publishNotice(row.id)
    ElMessage.success('发布成功')
    handleQuery()
  } catch (error) {
    ElMessage.error('发布失败')
  }
}

const handleRevoke = async (row: NoticeVO) => {
  try {
    await revokeNotice(row.id)
    ElMessage.success('撤回成功')
    handleQuery()
  } catch (error) {
    ElMessage.error('撤回失败')
  }
}

onMounted(() => {
  handleQuery()
})
</script>

<style scoped lang="scss">
.notice-container {
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
