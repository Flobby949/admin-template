<template>
  <el-drawer
    v-model="visible"
    title="操作日志详情"
    direction="rtl"
    size="50%"
    destroy-on-close
  >
    <el-descriptions :column="1" border>
      <el-descriptions-item label="操作模块">
        {{ data.title }} / {{ getBusinessTypeName(data.businessType) }}
      </el-descriptions-item>
      <el-descriptions-item label="登录信息">
        {{ data.operName }} / {{ data.operIp }} / {{ data.operLocation }}
      </el-descriptions-item>
      <el-descriptions-item label="请求地址">
        {{ data.operUrl }}
        <el-tag size="small">{{ data.requestMethod }}</el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="操作方法">
        {{ data.method }}
      </el-descriptions-item>
      <el-descriptions-item label="请求参数">
        <div class="code-box">
          <pre>{{ formatJson(data.operParam) }}</pre>
          <el-button
            v-if="data.operParam"
            type="primary"
            link
            icon="CopyDocument"
            class="copy-btn"
            @click="handleCopy(data.operParam)"
          >
            复制
          </el-button>
        </div>
      </el-descriptions-item>
      <el-descriptions-item label="返回参数">
        <div class="code-box">
          <pre>{{ formatJson(data.jsonResult) }}</pre>
          <el-button
            v-if="data.jsonResult"
            type="primary"
            link
            icon="CopyDocument"
            class="copy-btn"
            @click="handleCopy(data.jsonResult)"
          >
            复制
          </el-button>
        </div>
      </el-descriptions-item>
      <el-descriptions-item label="状态">
        <div v-if="data.status === 1">正常</div>
        <div v-else class="text-danger">失败</div>
      </el-descriptions-item>
      <el-descriptions-item label="消耗时间">
        {{ data.costTime }}ms
      </el-descriptions-item>
      <el-descriptions-item label="操作时间">
        {{ data.operTime }}
      </el-descriptions-item>
      <el-descriptions-item label="异常信息" v-if="data.status === 0">
        <div class="code-box error-box">
          {{ data.errorMsg }}
        </div>
      </el-descriptions-item>
    </el-descriptions>
  </el-drawer>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { ElMessage } from 'element-plus'
import type { OperLogVO } from '@/api/monitor/operlog'
import { useClipboard } from '@vueuse/core'

const props = defineProps<{
  modelValue: boolean
  data: OperLogVO
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
}>()

const { copy, isSupported } = useClipboard()

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

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

const formatJson = (json: string) => {
  if (!json) return ''
  try {
    return JSON.stringify(JSON.parse(json), null, 2)
  } catch (e) {
    return json
  }
}

const handleCopy = async (text: string) => {
  if (!isSupported.value) {
    ElMessage.error('当前浏览器不支持复制功能')
    return
  }
  try {
    await copy(text)
    ElMessage.success('复制成功')
  } catch (e) {
    ElMessage.error('复制失败')
  }
}
</script>

<style scoped lang="scss">
.code-box {
  position: relative;
  background-color: #f5f7fa;
  padding: 10px;
  border-radius: 4px;
  overflow: auto;
  max-height: 300px;
  font-family: monospace;
  white-space: pre-wrap;
  word-break: break-all;

  .copy-btn {
    position: absolute;
    top: 5px;
    right: 5px;
  }
}

.error-box {
  color: #f56c6c;
  background-color: #fef0f0;
}

.text-danger {
  color: #f56c6c;
}
</style>
