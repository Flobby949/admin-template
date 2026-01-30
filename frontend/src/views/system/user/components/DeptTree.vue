<template>
  <div class="head-container">
    <el-input
      v-model="deptName"
      placeholder="请输入部门名称"
      clearable
      prefix-icon="Search"
      class="filter-item"
      aria-label="搜索部门"
    />
  </div>
  <div class="tree-container">
    <el-tree
      ref="treeRef"
      class="filter-tree"
      :data="deptOptions"
      :props="{ label: 'deptName', children: 'children' }"
      :expand-on-click-node="false"
      :filter-node-method="filterNode"
      default-expand-all
      highlight-current
      @node-click="handleNodeClick"
    >
      <template #default="{ node, data }">
        <span class="custom-tree-node" :class="{ 'is-disabled': data.status === 0 }">
          <el-icon class="tree-icon" aria-hidden="true">
            <CircleClose v-if="data.status === 0" />
            <OfficeBuilding v-else />
          </el-icon>
          <span class="tree-label" :title="node.label">
            {{ node.label }}
            <span v-if="data.status === 0" class="disabled-tag">(已停用)</span>
          </span>
        </span>
      </template>
    </el-tree>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { ElMessage, ElTree } from 'element-plus'
import { OfficeBuilding, CircleClose } from '@element-plus/icons-vue'
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
  padding: 0 4px;
  margin-bottom: 20px;
}

.tree-container {
  height: calc(100vh - 200px);
  overflow-y: auto;
}

.filter-tree {
  background: transparent;

  :deep(.el-tree-node__content) {
    height: 36px;
    border-radius: 8px;
    margin-bottom: 4px;
    padding: 0 8px !important;
    transition: background-color 0.2s;

    &:hover {
      background-color: var(--el-fill-color-light);
    }
  }

  // 子节点缩进
  :deep(.el-tree-node__children .el-tree-node__content) {
    padding-left: 24px !important;
  }

  :deep(.el-tree-node__children .el-tree-node__children .el-tree-node__content) {
    padding-left: 40px !important;
  }

  :deep(.el-tree-node.is-current > .el-tree-node__content) {
    background-color: var(--el-color-primary-light-9);
    color: var(--el-color-primary);
    font-weight: 500;

    .tree-icon {
      color: var(--el-color-primary);
    }
  }
}

.custom-tree-node {
  display: flex;
  align-items: center;
  width: 100%;

  .tree-icon {
    margin-right: 8px;
    font-size: 16px;
    color: var(--el-text-color-secondary);
  }

  .tree-label {
    flex: 1;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;

    .disabled-tag {
      margin-left: 4px;
      font-size: 12px;
      opacity: 0.8;
    }
  }

  &.is-disabled {
    color: var(--el-text-color-placeholder);

    .tree-icon {
      color: var(--el-text-color-placeholder);
    }
  }
}
</style>