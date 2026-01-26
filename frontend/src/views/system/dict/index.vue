<template>
  <div class="dict-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>字典管理</span>
          <el-button type="primary" @click="handleAdd">新增字典</el-button>
        </div>
      </template>

      <!-- 搜索表单 -->
      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item label="字典名称">
          <el-input
            v-model="queryParams.dictName"
            placeholder="请输入字典名称"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="字典类型">
          <el-input
            v-model="queryParams.dictType"
            placeholder="请输入字典类型"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格 -->
      <el-table :data="dictList" border stripe>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="dictName" label="字典名称" />
        <el-table-column prop="dictType" label="字典类型" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleQuery"
        @current-change="handleQuery"
      />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  dictName: '',
  dictType: ''
})

// 字典列表
const dictList = ref([])
const total = ref(0)

// 查询字典列表
const handleQuery = () => {
  // TODO: 调用 API 获取字典列表
  console.log('查询字典列表', queryParams)
  ElMessage.info('字典管理功能开发中...')
}

// 重置查询
const handleReset = () => {
  queryParams.pageNum = 1
  queryParams.pageSize = 10
  queryParams.dictName = ''
  queryParams.dictType = ''
  handleQuery()
}

// 新增字典
const handleAdd = () => {
  ElMessage.info('新增字典功能开发中...')
}

// 编辑字典
const handleEdit = (row: any) => {
  console.log('编辑字典', row)
  ElMessage.info('编辑字典功能开发中...')
}

// 删除字典
const handleDelete = (row: any) => {
  ElMessageBox.confirm('确认删除该字典吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    console.log('删除字典', row)
    ElMessage.success('删除成功')
  }).catch(() => {
    ElMessage.info('已取消删除')
  })
}

onMounted(() => {
  handleQuery()
})
</script>

<style scoped lang="scss">
.dict-container {
  padding: 20px;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .search-form {
    margin-bottom: 20px;
  }

  .el-pagination {
    margin-top: 20px;
    justify-content: flex-end;
  }
}
</style>
