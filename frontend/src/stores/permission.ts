import { defineStore } from 'pinia'
import { getMenuTree, type MenuTreeVO } from '@/api/role'

interface PermissionState {
  permissions: string[]
  menuTree: MenuTreeVO[]
  routes: any[]
}

export const usePermissionStore = defineStore('permission', {
  state: (): PermissionState => ({
    permissions: [],
    menuTree: [],
    routes: []
  }),

  getters: {
    // 检查是否有指定权限
    hasPermission: (state) => (permission: string) => {
      return state.permissions.includes(permission) || state.permissions.includes('*:*:*')
    },

    // 检查是否有任一权限
    hasAnyPermission: (state) => (permissions: string[]) => {
      if (state.permissions.includes('*:*:*')) return true
      return permissions.some(p => state.permissions.includes(p))
    },

    // 检查是否有所有权限
    hasAllPermissions: (state) => (permissions: string[]) => {
      if (state.permissions.includes('*:*:*')) return true
      return permissions.every(p => state.permissions.includes(p))
    }
  },

  actions: {
    // 设置权限列表
    setPermissions(permissions: string[]) {
      this.permissions = permissions
    },

    // 加载菜单树
    async loadMenuTree() {
      try {
        const res: any = await getMenuTree()
        this.menuTree = res.data || []
        return this.menuTree
      } catch (error) {
        console.error('Failed to load menu tree:', error)
        return []
      }
    },

    // 生成路由
    generateRoutes(menus: MenuTreeVO[]) {
      const routes = this.filterAsyncRoutes(menus)
      this.routes = routes
      return routes
    },

    // 过滤异步路由
    filterAsyncRoutes(menus: MenuTreeVO[]): any[] {
      const routes: any[] = []

      menus.forEach(menu => {
        // 只处理目录和菜单，不处理按钮
        if (menu.menuType === 3) return

        const route: any = {
          path: menu.routePath || '',
          name: menu.menuName,
          meta: {
            title: menu.menuName,
            icon: menu.icon,
            permission: menu.permission,
            hidden: menu.visible === 0
          }
        }

        // 设置组件
        if (menu.component) {
          if (menu.component === 'Layout') {
            route.component = () => import('@/layout/index.vue')
          } else {
            route.component = () => import(`@/views/${menu.component}.vue`)
          }
        }

        // 递归处理子菜单
        if (menu.children && menu.children.length > 0) {
          route.children = this.filterAsyncRoutes(menu.children)
        }

        routes.push(route)
      })

      return routes
    },

    // 重置权限
    resetPermission() {
      this.permissions = []
      this.menuTree = []
      this.routes = []
    }
  }
})
