<template>
  <div class="head-container">
    <el-input
      v-model="deptName"
      placeholder="请输入部门名称"
      clearable
      prefix-icon="Search"
      style="margin-bottom: 20px"
    />
  </div>
  <div class="tree-container">
    <el-tree
      ref="treeRef"
      :data="deptOptions"
      :props="{ label: 'deptName', children: 'children' }"
      :expand-on-click-node="false"
      :filter-node-method="filterNode"
      default-expand-all
      highlight-current
      @node-click="handleNodeClick"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { ElMessage, ElTree } from 'element-plus'
import { getDeptTree, type DeptVO } from '@/api/system/dept'

const emit = defineEmits<{
  (e: 'node-click', deptId: number | undefined): void
}>()

const deptName = ref('')
const deptOptions = ref<DeptVO[]>([])
const treeRef = ref<InstanceType<typeof ElTree>>()

// Fetch Dept Tree
const getTreeselect = async () => {
  try {
    const data = await getDeptTree()
    deptOptions.value = data
  } catch (error) {
    ElMessage.error('获取部门列表失败')
  }
}

// Filter Node
const filterNode = (value: string, data: DeptVO) => {
  if (!value) return true
  return data.deptName.includes(value)
}

// Handle Node Click
const handleNodeClick = (data: DeptVO) => {
  emit('node-click', data.id)
}

watch(deptName, (val) => {
  treeRef.value?.filter(val)
})

onMounted(() => {
  getTreeselect()
})
</script>

<style scoped lang="scss">
.head-container {
  padding: 0 10px;
}
.tree-container {
  height: calc(100vh - 200px);
  overflow-y: auto;
}
</style>
