<template>
  <div class="region-container">
    <!-- 搜索区域 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item label="区域名称">
          <el-input
            v-model="queryParams.name"
            placeholder="请输入区域名称"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="区域编码">
          <el-input
            v-model="queryParams.regionCode"
            placeholder="请输入区域编码"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="父级编码">
          <el-input
            v-model="queryParams.parentCode"
            placeholder="请输入父级编码"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
            <el-option label="全部" :value="undefined" />
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="resetQuery">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作区域 -->
    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>区域表列表</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增
          </el-button>
        </div>
      </template>

      <!-- 表格 -->
      <el-table v-loading="loading" :data="dataList" border stripe>
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="name" label="区域名称" min-width="120" />
        <el-table-column prop="regionCode" label="区域编码" min-width="120" />
        <el-table-column prop="parentCode" label="父级编码" min-width="120" />
        <el-table-column prop="level" label="层级：1-省，2-市，3-区县" min-width="120" />
        <el-table-column prop="sortOrder" label="排序" min-width="120" />
        <el-table-column prop="status" label="状态：0-禁用，1-启用" min-width="120" />
        <el-table-column prop="createTime" label="创建时间" width="180" align="center" />
        <el-table-column prop="updateTime" label="更新时间" width="180" align="center" />
        <el-table-column prop="createBy" label="创建人" min-width="120" />
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button type="danger" link @click="handleDelete(row)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <RegionDialog v-model:visible="dialogVisible" :id="currentId" @success="handleSuccess" />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Edit, Delete } from '@element-plus/icons-vue'
import { listRegions, deleteRegion, type RegionVO } from '@/api/region'
import RegionDialog from './components/RegionDialog.vue'

// 查询参数
const queryParams = reactive({
  name: undefined as string | undefined,
  regionCode: undefined as string | undefined,
  parentCode: undefined as string | undefined,
  status: undefined as number | undefined,
})

// 状态
const loading = ref(false)
const dataList = ref<RegionVO[]>([])
const dialogVisible = ref(false)
const currentId = ref<number | undefined>(undefined)

// 获取列表
const fetchList = async () => {
  loading.value = true
  try {
    const res: any = await listRegions()
    dataList.value = res || []
  } catch (error) {
    console.error('Failed to fetch list:', error)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleQuery = () => {
  fetchList()
}

// 重置
const resetQuery = () => {
  queryParams.name = undefined
  queryParams.regionCode = undefined
  queryParams.parentCode = undefined
  queryParams.status = undefined
  fetchList()
}

// 新增
const handleAdd = () => {
  currentId.value = undefined
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row: RegionVO) => {
  currentId.value = row.id
  dialogVisible.value = true
}

// 删除
const handleDelete = async (row: RegionVO) => {
  try {
    await ElMessageBox.confirm('确定要删除该区域表吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteRegion(row.id)
    ElMessage.success('删除成功')
    fetchList()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 操作成功回调
const handleSuccess = () => {
  fetchList()
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.region-container {
  padding: 20px;
}

.search-card {
  margin-bottom: 20px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
