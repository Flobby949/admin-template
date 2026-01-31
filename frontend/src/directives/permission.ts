import { Directive, DirectiveBinding } from 'vue'
import { usePermissionStore } from '@/stores/permission'

/**
 * v-permission
 * 支持两种用法：
 * 1. 权限字符串: <button v-permission="'system:user:add'">Add</button>
 * 2. 权限数组: <button v-permission="['system:user:add', 'system:user:edit']">Action</button>
 */
export const permission: Directive = {
  mounted(el: HTMLElement, binding: DirectiveBinding) {
    const { value } = binding
    const permissionStore = usePermissionStore()

    if (!value) {
      return
    }

    // 将单个字符串转换为数组统一处理
    const requiredPermissions = Array.isArray(value) ? value : [value]

    if (requiredPermissions.length === 0) {
      return
    }

    // 检查是否有任一权限
    const hasPermission = requiredPermissions.some(permission => {
      return permissionStore.hasPermission(permission)
    })

    if (!hasPermission) {
      el.parentNode?.removeChild(el)
    }
  }
}
