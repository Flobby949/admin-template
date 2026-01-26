<template>
  <component :is="tag" v-if="hasPermission">
    <slot />
  </component>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { usePermissionStore } from '@/stores/permission'

const props = withDefaults(
  defineProps<{
    // 需要的权限，可以是单个权限字符串或权限数组
    permission: string | string[]
    // 权限逻辑：and-需要所有权限，or-需要任一权限
    logic?: 'and' | 'or'
    // 包装标签，默认为 span
    tag?: string
  }>(),
  {
    logic: 'or',
    tag: 'span'
  }
)

const permissionStore = usePermissionStore()

const hasPermission = computed(() => {
  const { permission, logic } = props

  if (!permission) {
    return true
  }

  // 超级管理员拥有所有权限
  if (permissionStore.permissions.includes('*:*:*')) {
    return true
  }

  // 单个权限
  if (typeof permission === 'string') {
    return permissionStore.hasPermission(permission)
  }

  // 权限数组
  if (Array.isArray(permission)) {
    if (logic === 'and') {
      return permissionStore.hasAllPermissions(permission)
    } else {
      return permissionStore.hasAnyPermission(permission)
    }
  }

  return false
})
</script>
