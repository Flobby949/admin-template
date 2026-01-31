<template>
  <div class="data-scope-select">
    <!-- 数据权限范围选择 -->
    <el-select
      :model-value="modelValue"
      placeholder="请选择数据权限范围"
      style="width: 100%"
      @update:model-value="handleScopeChange"
    >
      <el-option
        v-for="item in scopeOptions"
        :key="item.value"
        :label="item.label"
        :value="item.value"
      >
        <span>{{ item.label }}</span>
        <el-tooltip :content="item.description" placement="right">
          <el-icon style="margin-left: 8px; color: #909399">
            <QuestionFilled />
          </el-icon>
        </el-tooltip>
      </el-option>
    </el-select>

    <!-- 预留：自定义部门选择（未来扩展） -->
    <!-- <div v-if="modelValue === 5" class="mt-2">
      <el-tree
        :data="deptTree"
        :props="{ label: 'name', children: 'children' }"
        show-checkbox
        node-key="id"
        @check="handleDeptCheck"
      />
    </div> -->
  </div>
</template>

<script setup lang="ts">
import { QuestionFilled } from '@element-plus/icons-vue'

// Props
interface Props {
  modelValue: number
  deptIds?: number[] // 预留：自定义部门ID列表
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: 3, // 默认值改为"仅本部门"(3)，更安全
  deptIds: () => []
})

// Emits
interface Emits {
  (e: 'update:modelValue', value: number): void
  (e: 'update:deptIds', value: number[]): void // 预留：自定义部门更新
}

const emit = defineEmits<Emits>()

// 数据权限选项
const scopeOptions = [
  {
    value: 1,
    label: '全部数据权限',
    description: '可以查看和操作系统中的所有数据'
  },
  {
    value: 2,
    label: '本部门及以下数据权限',
    description: '可以查看和操作本部门及其下级部门的数据'
  },
  {
    value: 3,
    label: '本部门数据权限',
    description: '只能查看和操作本部门的数据'
  },
  {
    value: 4,
    label: '仅本人数据权限',
    description: '只能查看和操作自己创建的数据'
  }
  // 预留：自定义数据权限
  // {
  //   value: 5,
  //   label: '自定义数据权限',
  //   description: '自定义选择可访问的部门'
  // }
]

// 处理权限范围变更
const handleScopeChange = (value: number) => {
  emit('update:modelValue', value)
}

// 预留：处理自定义部门选择
// const handleDeptCheck = (data: any, checked: any) => {
//   const checkedKeys = checked.checkedKeys
//   emit('update:deptIds', checkedKeys)
// }
</script>

<style scoped lang="scss">
.data-scope-select {
  width: 100%;
}
</style>
