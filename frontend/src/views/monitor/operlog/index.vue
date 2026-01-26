<template>
  <div class="operlog-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>操作日志</span>
          <el-button type="danger" @click="handleClear">清空日志</el-button>
        </div>
      </template>

      <!-- 搜索表单 -->
      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item label="操作模块">
          <el-input
            v-model="queryParams.title"
            placeholder="请输入操作模块"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="操作人员">
          <el-input
            v-model="queryParams.operName"
            placeholder="请输入操作人员"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="操作状态">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
            <el-option label="成功" :value="1" />
            <el-option label="失败" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作时间">
          <el-date-picker
            v-model="queryParams.operTime"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" @click="handleExport">导出</el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格 -->
      <el-table :data="logList" border stripe>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="title" label="操作模块" width="150" show-overflow-tooltip />
        <el-table-column prop="businessType" label="业务类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getBusinessTypeTag(row.businessType)">
              {{ getBusinessTypeName(row.businessType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="method" label="请求方法" width="200" show-overflow-tooltip />
        <el-table-column prop="requestMethod" label="请求方式" width="100" align="center" />
        <el-table-column prop="operName" label="操作人员" width="120" />
        <el-table-column prop="operIp" label="操作IP" width="130" />
        <el-table-column prop="operLocation" label="操作地点" width="120" />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operTime" label="操作时间" width="180" />
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleDetail(row)">详情</el-button>
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
  title: '',
  operName: '',
  status: undefined as number | undefined,
  operTime: []
})

// 日志列表
const logList = ref([])
const total = ref(0)

// 业务类型映射
const getBusinessTypeName = (type: number) => {
  const typeMap: Record<number, string> = {
    0: '其他',
    1: '新增',
    2: '修改',
    3: '删除',
    4: '查询',
    5: '导出',
    6: '导入'
  }
  return typeMap[type] || '未知'
}

const getBusinessTypeTag = (type: number) => {
  const tagMap: Record<number, string> = {
    0: 'info',
    1: 'success',
    2: 'warning',
    3: 'danger',
    4: 'primary',
    5: 'success',
    6: 'warning'
  }
  return tagMap[type] || 'info'
}

// 查询日志列表
const handleQuery = () => {
  // TODO: 调用 API 获取日志列表
  console.log('查询操作日志', queryParams)
  ElMessage.info('操作日志功能开发中...')
}

// 重置查询
const handleReset = () => {
  queryParams.pageNum = 1
  queryParams.pageSize = 10
  queryParams.title = ''
  queryParams.operName = ''
  queryParams.status = undefined
  queryParams.operTime = []
  handleQuery()
}

// 查看详情
const handleDetail = (row: any) => {
  console.log('查看日志详情', row)
  ElMessage.info('查看详情功能开发中...')
}

// 导出日志
const handleExport = () => {
  ElMessage.info('导出日志功能开发中...')
}

// 清空日志
const handleClear = () => {
  ElMessageBox.confirm('确认清空所有操作日志吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    ElMessage.success('清空成功')
  }).catch(() => {
    ElMessage.info('已取消清空')
  })
}

onMounted(() => {
  handleQuery()
})
</script>

<style scoped lang="scss">
.operlog-container {
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
