<template>
  <div class="app-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <div class="header-title">
            <el-button link icon="ArrowLeft" @click="handleClose">返回</el-button>
            <span style="margin-left: 10px; font-weight: bold;">字典数据 - {{ currentDictType?.dictName }} ({{ currentDictType?.dictType }})</span>
          </div>
          <el-button type="primary" icon="Plus" @click="handleAdd">新增数据</el-button>
        </div>
      </template>

      <!-- 搜索表单 -->
      <el-form :inline="true" :model="queryParams" ref="queryFormRef" class="search-form">
        <el-form-item label="字典标签" prop="dictLabel">
          <el-input
            v-model="queryParams.dictLabel"
            placeholder="请输入字典标签"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="queryParams.status" placeholder="状态" clearable style="width: 200px">
            <el-option label="正常" value="1" />
            <el-option label="停用" value="0" />
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
        :data="dataList"
        border
        stripe
      >
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="dictLabel" label="字典标签">
          <template #default="{ row }">
            <dict-tag :options="dataList" :value="row.dictValue" />
          </template>
        </el-table-column>
        <el-table-column prop="dictValue" label="字典键值" />
        <el-table-column prop="dictSort" label="字典排序" align="center" sortable />
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
            <el-button type="primary" link icon="Edit" @click="handleEdit(row)">编辑</el-button>
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

      <!-- 添加或修改字典数据对话框 -->
      <el-dialog :title="title" v-model="open" width="500px" append-to-body>
        <el-form ref="dictFormRef" :model="form" :rules="rules" label-width="80px">
          <el-form-item label="字典类型">
            <el-input v-model="form.dictType" disabled />
          </el-form-item>
          <el-form-item label="数据标签" prop="dictLabel">
            <el-input v-model="form.dictLabel" placeholder="请输入数据标签" />
          </el-form-item>
          <el-form-item label="数据键值" prop="dictValue">
            <el-input v-model="form.dictValue" placeholder="请输入数据键值" />
          </el-form-item>
          <el-form-item label="样式属性" prop="cssClass">
            <el-input v-model="form.cssClass" placeholder="请输入CSS样式属性" />
          </el-form-item>
          <el-form-item label="回显样式" prop="listClass">
            <el-select v-model="form.listClass" placeholder="请选择回显样式" clearable>
              <el-option
                v-for="item in listClassOptions"
                :key="item.value"
                :label="item.label + ' (' + item.value + ')'"
                :value="item.value"
              >
                <span style="float: left">{{ item.label }}</span>
                <span style="float: right; color: #8492a6; font-size: 13px">
                   <el-tag :type="item.value === 'default' ? '' : item.value as any">{{ item.label }}</el-tag>
                </span>
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="显示排序" prop="dictSort">
            <el-input-number v-model="form.dictSort" controls-position="right" :min="0" />
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
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, FormInstance } from 'element-plus'
import { listData, getData, addData, updateData, delData, DictDataVO, DictDataQuery } from '@/api/system/dict/data'
import { getType, DictTypeVO } from '@/api/system/dict/type'
import useDictStore from '@/stores/dict'
import DictTag from '@/components/DictTag/index.vue'

const route = useRoute()
const router = useRouter()
const dictStore = useDictStore()

const dictFormRef = ref<FormInstance>()
const queryFormRef = ref<FormInstance>()

const loading = ref(true)
const total = ref(0)
const dataList = ref<DictDataVO[]>([])
const title = ref('')
const open = ref(false)
const currentDictType = ref<DictTypeVO>()

const listClassOptions = [
  { value: 'default', label: '默认' },
  { value: 'primary', label: '主要' },
  { value: 'success', label: '成功' },
  { value: 'info', label: '信息' },
  { value: 'warning', label: '警告' },
  { value: 'danger', label: '危险' }
]

const data = reactive({
  form: {
    id: undefined,
    dictLabel: '',
    dictValue: '',
    dictSort: 0,
    dictType: '',
    cssClass: '',
    listClass: 'default',
    isDefault: 0,
    status: 1,
    remark: ''
  } as Partial<DictDataVO>,
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    dictName: undefined,
    dictType: '',
    status: '',
    dictLabel: ''
  } as DictDataQuery,
  rules: {
    dictLabel: [{ required: true, message: '数据标签不能为空', trigger: 'blur' }],
    dictValue: [{ required: true, message: '数据键值不能为空', trigger: 'blur' }],
    dictSort: [{ required: true, message: '数据顺序不能为空', trigger: 'blur' }]
  }
})

const { form, queryParams, rules } = toRefs(data)

/** 初始化 */
async function init() {
  const dictId = route.params.dictId
  if (dictId) {
    try {
      const response = await getType(Number(dictId))
      currentDictType.value = response
      queryParams.value.dictType = response.dictType
      handleQuery()
    } catch (error) {
      ElMessage.error('获取字典类型详情失败')
    }
  }
}

/** 查询字典数据列表 */
function handleQuery() {
  loading.value = true
  listData(queryParams.value).then(response => {
    dataList.value = response.list
    total.value = response.total
    loading.value = false
  })
}

/** 重置按钮操作 */
function handleReset() {
  queryFormRef.value?.resetFields()
  queryParams.value.dictType = currentDictType.value?.dictType || ''
  handleQuery()
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  form.value.dictType = currentDictType.value?.dictType
  open.value = true
  title.value = '添加字典数据'
}

/** 编辑按钮操作 */
function handleEdit(row: DictDataVO) {
  reset()
  getData(row.id).then(response => {
    form.value = response
    open.value = true
    title.value = '修改字典数据'
  })
}

/** 提交按钮 */
function submitForm() {
  dictFormRef.value?.validate(valid => {
    if (valid) {
      if (form.value.id !== undefined) {
        updateData(form.value).then(() => {
          dictStore.removeDict(form.value.dictType!)
          ElMessage.success('修改成功')
          open.value = false
          handleQuery()
        })
      } else {
        addData(form.value).then(() => {
          dictStore.removeDict(form.value.dictType!)
          ElMessage.success('新增成功')
          open.value = false
          handleQuery()
        })
      }
    }
  })
}

/** 删除按钮操作 */
function handleDelete(row: DictDataVO) {
  ElMessageBox.confirm('是否确认删除字典标签为"' + row.dictLabel + '"的数据项？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    return delData(row.id)
  }).then(() => {
    dictStore.removeDict(row.dictType)
    handleQuery()
    ElMessage.success('删除成功')
  }).catch(() => {})
}

/** 返回按钮 */
function handleClose() {
  router.push('/system/dict')
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
    dictLabel: '',
    dictValue: '',
    dictSort: 0,
    dictType: currentDictType.value?.dictType || '',
    cssClass: '',
    listClass: 'default',
    isDefault: 0,
    status: 1,
    remark: ''
  }
}

onMounted(() => {
  init()
})
</script>

<style scoped lang="scss">
.app-container {
  padding: 20px;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .header-title {
      display: flex;
      align-items: center;
    }
  }

  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>
