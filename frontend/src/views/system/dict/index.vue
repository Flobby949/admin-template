<template>
  <div class="app-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>字典管理</span>
          <div class="right-menu">
            <el-button type="danger" plain icon="Refresh" @click="handleRefreshCache">刷新缓存</el-button>
            <el-button type="primary" icon="Plus" @click="handleAdd">新增字典</el-button>
          </div>
        </div>
      </template>

      <!-- 搜索表单 -->
      <el-form :inline="true" :model="queryParams" ref="queryFormRef" class="search-form">
        <el-form-item label="字典名称" prop="dictName">
          <el-input
            v-model="queryParams.dictName"
            placeholder="请输入字典名称"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="字典类型" prop="dictType">
          <el-input
            v-model="queryParams.dictType"
            placeholder="请输入字典类型"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="queryParams.status" placeholder="字典状态" clearable style="width: 200px">
            <el-option label="正常" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
          <el-button icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格 -->
      <el-table
        v-loading="loading"
        :data="typeList"
        border
        stripe
      >
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="dictName" label="字典名称" show-overflow-tooltip />
        <el-table-column prop="dictType" label="字典类型" show-overflow-tooltip>
          <template #default="{ row }">
            <router-link :to="'/system/dict-data/' + row.id" class="link-type">
              <span>{{ row.dictType }}</span>
            </router-link>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="250" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link icon="Edit" @click="handleEdit(row)">编辑</el-button>
            <el-button type="primary" link icon="List" @click="handleData(row)">数据管理</el-button>
            <el-button type="danger" link icon="Delete" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-if="total > 0"
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleQuery"
          @current-change="handleQuery"
        />
      </div>

      <!-- 添加或修改字典对话框 -->
      <el-dialog :title="title" v-model="open" width="500px" append-to-body>
        <el-form ref="dictFormRef" :model="form" :rules="rules" label-width="80px">
          <el-form-item label="字典名称" prop="dictName">
            <el-input v-model="form.dictName" placeholder="请输入字典名称" />
          </el-form-item>
          <el-form-item label="字典类型" prop="dictType">
            <el-input v-model="form.dictType" placeholder="请输入字典类型" />
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-radio-group v-model="form.status">
              <el-radio :value="1">正常</el-radio>
              <el-radio :value="0">停用</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="备注" prop="remark">
            <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" />
          </el-form-item>
        </el-form>
        <template #footer>
          <div class="dialog-footer">
            <el-button type="primary" @click="submitForm">确 定</el-button>
            <el-button @click="cancel">取 消</el-button>
          </div>
        </template>
      </el-dialog>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, toRefs } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, FormInstance } from 'element-plus'
import { listType, getType, addType, updateType, delType, refreshCache, DictTypeVO, DictTypeQuery } from '@/api/system/dict/type'

const router = useRouter()
const dictFormRef = ref<FormInstance>()
const queryFormRef = ref<FormInstance>()

const loading = ref(true)
const total = ref(0)
const typeList = ref<DictTypeVO[]>([])
const title = ref('')
const open = ref(false)

const data = reactive({
  form: {
    id: undefined,
    dictName: '',
    dictType: '',
    status: 1,
    remark: ''
  } as Partial<DictTypeVO>,
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    dictName: '',
    dictType: '',
    status: ''
  } as DictTypeQuery,
  rules: {
    dictName: [{ required: true, message: '字典名称不能为空', trigger: 'blur' }],
    dictType: [{ required: true, message: '字典类型不能为空', trigger: 'blur' }]
  }
})

const { form, queryParams, rules } = toRefs(data)

/** 查询字典类型列表 */
function handleQuery() {
  loading.value = true
  listType(queryParams.value).then(response => {
    typeList.value = response.list
    total.value = response.total
    loading.value = false
  })
}

/** 重置按钮操作 */
function handleReset() {
  queryFormRef.value?.resetFields()
  handleQuery()
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = '添加字典类型'
}

/** 编辑按钮操作 */
function handleEdit(row: DictTypeVO) {
  reset()
  const id = row.id
  getType(id).then(response => {
    form.value = response
    open.value = true
    title.value = '修改字典类型'
  })
}

/** 提交按钮 */
function submitForm() {
  dictFormRef.value?.validate(valid => {
    if (valid) {
      if (form.value.id !== undefined) {
        updateType(form.value).then(() => {
          ElMessage.success('修改成功')
          open.value = false
          handleQuery()
        })
      } else {
        addType(form.value).then(() => {
          ElMessage.success('新增成功')
          open.value = false
          handleQuery()
        })
      }
    }
  })
}

/** 删除按钮操作 */
function handleDelete(row: DictTypeVO) {
  ElMessageBox.confirm('是否确认删除字典编号为"' + row.id + '"的数据项？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    return delType(row.id)
  }).then(() => {
    handleQuery()
    ElMessage.success('删除成功')
  }).catch(() => {})
}

/** 刷新缓存按钮操作 */
function handleRefreshCache() {
  refreshCache().then(() => {
    ElMessage.success('刷新成功')
  })
}

/** 数据管理 */
function handleData(row: DictTypeVO) {
  router.push(`/system/dict-data/${row.id}`)
}

/** 取消按钮 */
function cancel() {
  open.value = false
  reset()
}

/** 表单重置 */
function reset() {
  form.value = {
    id: undefined,
    dictName: '',
    dictType: '',
    status: 1,
    remark: ''
  }
}

onMounted(() => {
  handleQuery()
})
</script>

<style scoped lang="scss">
.app-container {
  padding: 20px;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .right-menu {
    display: flex;
    gap: 10px;
  }

  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
  
  .link-type {
    color: var(--el-color-primary);
    text-decoration: none;
    
    &:hover {
      text-decoration: underline;
    }
  }
}
</style>