<template>
  <template v-if="!item.meta?.hidden">
    <!-- 没有子菜单或只有一个子菜单且无嵌套 -->
    <template v-if="!hasVisibleChildren">
      <el-menu-item :index="resolvePath(onlyOneChild?.path || item.path)">
        <el-icon v-if="icon">
          <component :is="icon" />
        </el-icon>
        <template #title>
          <span>{{ onlyOneChild?.meta?.title || item.meta?.title }}</span>
        </template>
      </el-menu-item>
    </template>

    <!-- 有多个子菜单 -->
    <el-sub-menu v-else :index="resolvePath(item.path)">
      <template #title>
        <el-icon v-if="icon">
          <component :is="icon" />
        </el-icon>
        <span>{{ item.meta?.title }}</span>
      </template>
      <sidebar-item
        v-for="child in visibleChildren"
        :key="child.path"
        :item="child"
        :base-path="resolvePath(item.path)"
        :is-collapse="isCollapse"
      />
    </el-sub-menu>
  </template>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { RouteRecordRaw } from 'vue-router'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

interface Props {
  item: RouteRecordRaw
  basePath?: string
  isCollapse?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  basePath: '',
  isCollapse: false
})

// 获取可见的子菜单
const visibleChildren = computed(() => {
  if (!props.item.children) return []
  return props.item.children.filter(child => !child.meta?.hidden)
})

// 是否有可见的子菜单
// 只要有子菜单就显示为子菜单结构
const hasVisibleChildren = computed(() => {
  return visibleChildren.value.length >= 1
})

// 唯一的子菜单（当只有一个可见子菜单时）
const onlyOneChild = computed(() => {
  if (visibleChildren.value.length === 1) {
    return visibleChildren.value[0]
  }
  return null
})

// 获取图标组件
const icon = computed(() => {
  const iconName = onlyOneChild.value?.meta?.icon || props.item.meta?.icon
  if (!iconName || typeof iconName !== 'string') return null

  // 从 Element Plus Icons 中获取图标
  const icons = ElementPlusIconsVue as Record<string, any>
  return icons[iconName] || null
})

// 解析路径
function resolvePath(path: string): string {
  if (!path) return props.basePath
  if (path.startsWith('/')) return path
  if (!props.basePath) return '/' + path
  return props.basePath + '/' + path
}
</script>
