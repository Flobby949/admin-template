<template>
  <div class="operlog-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>操作日志</span>
          <el-button
            v-permission="'monitor:operlog:remove'"
            type="danger"
            plain
            icon="Delete"
            @click="handleClean"
          >
            清空日志
          </el-button>
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
        <el-form-item label="状态">
          <el-select
            v-model="queryParams.status"
            placeholder="请选择状态"
            clearable
            style="width: 150px"
          >
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
            value-format="YYYY-MM-DD HH:mm:ss"
            :default-time="[new Date(2000, 1, 1, 0, 0, 0), new Date(2000, 1, 1, 23, 59, 59)]"
            style="width: 240px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
          <el-button icon="Refresh" @click="handleReset">重置</el-button>
          <el-button
            v-permission="'monitor:operlog:export'"
            type="warning"
            icon="Download"
            @click="handleExport"
          >
            导出
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格 -->
      <el-table
        v-loading="loading"
        :data="logList"
        border
        stripe
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column prop="id" label="日志编号" width="80" align="center" />
        <el-table-column prop="title" label="操作模块" min-width="150" show-overflow-tooltip />
        <el-table-column prop="businessType" label="业务类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getBusinessTypeTag(row.businessType)">
              {{ getBusinessTypeName(row.businessType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="requestMethod" label="请求方式" width="100" align="center" />
        <el-table-column prop="operName" label="操作人员" min-width="120" />
        <el-table-column prop="operIp" label="操作IP" min-width="130" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operTime" label="操作时间" min-width="180" align="center" />
        <el-table-column prop="costTime" label="消耗时间" width="100" align="center">
          <template #default="{ row }"> {{ row.costTime }}ms </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              v-permission="'monitor:operlog:query'"
              type="primary"
              link
              icon="View"
              @click="handleDetail(row)"
            >
              详情
            </el-button>
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

    <!-- 详情抽屉 -->
    <log-detail v-model="dialog.visible" :data="dialog.data" />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getOperLogList,
  cleanOperLog,
  exportOperLog,
  type OperLogQuery,
  type OperLogVO
} from '@/api/monitor/operlog'
import LogDetail from './components/LogDetail.vue'

// 查询参数
const queryParams = reactive<OperLogQuery>({
  pageNum: 1,
  pageSize: 10,
  title: undefined,
  operName: undefined,
  status: undefined,
  operTime: []
})

// 数据列表
const loading = ref(false)
const logList = ref<OperLogVO[]>([])
const total = ref(0)
const selectedIds = ref<number[]>([])

// 详情控制
const dialog = reactive({
  visible: false,
  data: {} as OperLogVO
})

// 业务类型映射
const getBusinessTypeName = (type: number) => {
  const typeMap: Record<number, string> = {
    0: '其他',
    1: '新增',
    2: '修改',
    3: '删除',
    4: '授权',
    5: '导出',
    6: '导入',
    7: '强退',
    8: '生成代码',
    9: '清空数据'
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
    5: 'warning',
    6: 'warning',
    7: 'danger',
    8: 'info',
    9: 'danger'
  }
  return tagMap[type] || 'info'
}

// 查询日志列表
const handleQuery = async () => {
  loading.value = true
  try {
    const data = await getOperLogList(queryParams)
    logList.value = data.list
    total.value = data.total
  } catch (error) {
    ElMessage.error('查询操作日志失败')
  } finally {
    loading.value = false
  }
}

// 重置查询
const handleReset = () => {
  queryParams.pageNum = 1
  queryParams.pageSize = 10
  queryParams.title = undefined
  queryParams.operName = undefined
  queryParams.status = undefined
  queryParams.operTime = []
  handleQuery()
}

// 选中变化
const handleSelectionChange = (selection: OperLogVO[]) => {
  selectedIds.value = selection.map(item => item.id)
}

// 查看详情
const handleDetail = (row: OperLogVO) => {
  dialog.data = row
  dialog.visible = true
}

// 清空日志
const handleClean = () => {
  ElMessageBox.confirm('确认清空所有操作日志吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      try {
        await cleanOperLog()
        ElMessage.success('清空成功')
        handleQuery()
      } catch (error) {
        ElMessage.error('清空失败')
      }
    })
    .catch(() => {})
}

// 导出日志
const handleExport = async () => {
  try {
    const res = await exportOperLog(queryParams)
    const blob = new Blob([res], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    })
    const link = document.createElement('a')
    link.href = window.URL.createObjectURL(blob)
    link.download = `operation_logs_${new Date().getTime()}.xlsx`
    link.click()
    window.URL.revokeObjectURL(link.href)
  } catch (error) {
    ElMessage.error('导出失败')
  }
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
