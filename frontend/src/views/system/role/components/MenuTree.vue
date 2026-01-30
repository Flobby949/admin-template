<template>
  <div class="menu-tree-container">
    <div class="tree-header">
      <el-checkbox
        v-model="expandAll"
        @change="handleExpandAll"
      >
        展开/折叠
      </el-checkbox>
      <el-checkbox
        v-model="checkAll"
        @change="handleCheckAll"
      >
        全选/全不选
      </el-checkbox>
      <el-checkbox v-model="checkStrictly">
        联动子节点
      </el-checkbox>
    </div>
    <el-tree
      ref="treeRef"
      :data="menuTree"
      :props="treeProps"
      show-checkbox
      node-key="id"
      :default-expand-all="expandAll"
      check-strictly
      :default-checked-keys="modelValue"
      @check="handleCheck"
    >
      <template #default="{ node, data }">
        <span class="tree-node">
          <el-icon v-if="data.icon" class="node-icon">
            <component :is="getIconComponent(data.icon)" />
          </el-icon>
          <span>{{ node.label }}</span>
          <el-tag
            v-if="data.menuType === 3"
            type="info"
            size="small"
            class="node-tag"
          >
            按钮
          </el-tag>
        </span>
      </template>
    </el-tree>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, nextTick } from 'vue'
import type { ElTree } from 'element-plus'
import type { MenuTreeVO } from '@/api/role'
import {
  Setting,
  User,
  UserFilled,
  Menu,
  Document,
  Monitor,
  Edit,
  Delete,
  Search,
  Refresh,
  List,
  Grid,
  Folder,
  FolderOpened,
  Files,
  Collection,
  Tickets,
  DataLine,
  DataBoard,
  DataAnalysis,
  PieChart,
  TrendCharts,
  Histogram,
  Odometer,
  Connection,
  Link,
  Operation,
  Tools,
  SetUp,
  Management,
  Plus as PlusIcon
} from '@element-plus/icons-vue'

const props = defineProps<{
  modelValue: number[]
  menuTree: MenuTreeVO[]
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: number[]): void
}>()

// 树引用
const treeRef = ref<InstanceType<typeof ElTree>>()

// 状态
const expandAll = ref(true)
const checkAll = ref(false)
const checkStrictly = ref(true)

// 树配置
const treeProps = {
  children: 'children',
  label: 'menuName'
}

// 图标映射
const iconMap: Record<string, any> = {
  setting: Setting,
  user: User,
  peoples: UserFilled,
  menu: Menu,
  document: Document,
  monitor: Monitor,
  edit: Edit,
  delete: Delete,
  plus: PlusIcon,
  search: Search,
  refresh: Refresh,
  list: List,
  grid: Grid,
  folder: Folder,
  'folder-opened': FolderOpened,
  files: Files,
  collection: Collection,
  tickets: Tickets,
  'data-line': DataLine,
  'data-board': DataBoard,
  'data-analysis': DataAnalysis,
  'pie-chart': PieChart,
  'trend-charts': TrendCharts,
  histogram: Histogram,
  odometer: Odometer,
  connection: Connection,
  link: Link,
  operation: Operation,
  tools: Tools,
  'set-up': SetUp,
  management: Management,
  tree: Folder,
  'tree-table': Grid,
  form: Document,
  dict: Collection
}

// 获取图标组件
const getIconComponent = (icon: string) => {
  return iconMap[icon] || Document
}

// 展开/折叠全部
const handleExpandAll = (val: boolean) => {
  const nodes = treeRef.value?.store?.nodesMap
  if (nodes) {
    Object.values(nodes).forEach((node: any) => {
      node.expanded = val
    })
  }
}

// 全选/全不选
const handleCheckAll = (val: boolean) => {
  if (val) {
    const allKeys = getAllKeys(props.menuTree)
    treeRef.value?.setCheckedKeys(allKeys)
    emit('update:modelValue', allKeys)
  } else {
    treeRef.value?.setCheckedKeys([])
    emit('update:modelValue', [])
  }
}

// 获取所有节点ID
const getAllKeys = (tree: MenuTreeVO[]): number[] => {
  const keys: number[] = []
  const traverse = (nodes: MenuTreeVO[]) => {
    nodes.forEach(node => {
      keys.push(node.id)
      if (node.children) {
        traverse(node.children)
      }
    })
  }
  traverse(tree)
  return keys
}

// 获取节点的所有子节点ID
const getChildKeys = (node: MenuTreeVO): number[] => {
  const keys: number[] = []
  const traverse = (children: MenuTreeVO[] | undefined) => {
    if (!children) return
    children.forEach(child => {
      keys.push(child.id)
      traverse(child.children)
    })
  }
  traverse(node.children)
  return keys
}

// 根据ID查找节点
const findNodeById = (tree: MenuTreeVO[], id: number): MenuTreeVO | null => {
  for (const node of tree) {
    if (node.id === id) return node
    if (node.children) {
      const found = findNodeById(node.children, id)
      if (found) return found
    }
  }
  return null
}

// 选中变化 - 实现只向下联动子节点
const handleCheck = (data: MenuTreeVO, { checkedKeys }: { checkedKeys: number[] }) => {
  if (!checkStrictly.value) {
    // 不联动模式，直接更新
    emit('update:modelValue', checkedKeys)
    return
  }

  // 联动模式：只向下联动子节点
  const node = findNodeById(props.menuTree, data.id)
  if (!node) {
    emit('update:modelValue', checkedKeys)
    return
  }

  const childKeys = getChildKeys(node)
  const isChecked = checkedKeys.includes(data.id)

  let newCheckedKeys: number[]
  if (isChecked) {
    // 选中时，同时选中所有子节点
    newCheckedKeys = [...new Set([...checkedKeys, ...childKeys])]
  } else {
    // 取消选中时，同时取消所有子节点
    const childKeySet = new Set(childKeys)
    newCheckedKeys = checkedKeys.filter(key => !childKeySet.has(key))
  }

  treeRef.value?.setCheckedKeys(newCheckedKeys)
  emit('update:modelValue', newCheckedKeys)
}

// 获取选中的节点ID
const getCheckedKeys = () => {
  return treeRef.value?.getCheckedKeys(false) as number[]
}

// 设置选中的节点
const setCheckedKeys = (keys: number[]) => {
  nextTick(() => {
    treeRef.value?.setCheckedKeys(keys)
  })
}

// 监听 modelValue 变化
watch(
  () => props.modelValue,
  (val) => {
    if (val && val.length > 0) {
      setCheckedKeys(val)
    }
  },
  { immediate: true }
)

// 暴露方法
defineExpose({
  getCheckedKeys,
  setCheckedKeys
})
</script>

<style scoped>
.menu-tree-container {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 10px;
  max-height: 300px;
  overflow-y: auto;
}

.tree-header {
  display: flex;
  gap: 20px;
  margin-bottom: 10px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}

.tree-node {
  display: flex;
  align-items: center;
  gap: 5px;
}

.node-icon {
  font-size: 14px;
  color: #909399;
}

.node-tag {
  margin-left: 5px;
}
</style>
