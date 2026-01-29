import { defineStore } from 'pinia'
import { getMenuTree, type MenuTreeVO } from '@/api/role'
import { getRouters, getPermissions, type RouterVO } from '@/api/auth'
import type { RouteRecordRaw } from 'vue-router'

interface PermissionState {
  permissions: string[]
  menuTree: MenuTreeVO[]
  routes: RouteRecordRaw[]
  dynamicRoutes: RouteRecordRaw[]
}

// 动态导入组件的模块映射
const modules = import.meta.glob('@/views/**/*.vue')

export const usePermissionStore = defineStore('permission', {
  state: (): PermissionState => ({
    permissions: [],
    menuTree: [],
    routes: [],
    dynamicRoutes: []
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

    // 从后端加载动态路由
    async loadRoutes(): Promise<RouteRecordRaw[]> {
      try {
        // 并行加载路由和权限
        const [routerRes, permissionRes] = await Promise.all([
          getRouters(),
          getPermissions()
        ])

        // 设置权限列表
        const permissions: string[] = (permissionRes as any).data || []
        this.permissions = permissions

        // 转换路由
        const routerData: RouterVO[] = (routerRes as any).data || []
        const routes = this.convertToRoutes(routerData)
        this.dynamicRoutes = routes
        return routes
      } catch (error) {
        console.error('Failed to load routes:', error)
        return []
      }
    },

    // 将后端路由数据转换为 Vue Router 格式
    convertToRoutes(routers: RouterVO[]): RouteRecordRaw[] {
      return routers.map(router => {
        const route = {
          path: router.path,
          name: router.name,
          meta: {
            title: router.meta?.title,
            icon: router.meta?.icon,
            hidden: router.meta?.hidden,
            keepAlive: router.meta?.keepAlive,
            permissions: router.meta?.permissions
          },
          component: this.loadComponent(router.component),
          children: router.children ? this.convertToRoutes(router.children) : undefined
        } as RouteRecordRaw

        if (router.redirect) {
          (route as any).redirect = router.redirect
        }

        return route
      })
    },

    // 动态加载组件
    loadComponent(component: string): any {
      if (!component) return undefined

      // Layout 组件特殊处理
      if (component === 'Layout') {
        return () => import('@/layout/index.vue')
      }

      // 动态加载 views 下的组件
      const componentPath = `/src/views/${component}.vue`
      if (modules[componentPath]) {
        return modules[componentPath]
      }

      // 尝试添加 index.vue
      const indexPath = `/src/views/${component}/index.vue`
      if (modules[indexPath]) {
        return modules[indexPath]
      }

      console.warn(`Component not found: ${component}`)
      return undefined
    },

    // 生成路由（兼容旧方法）
    generateRoutes(menus: MenuTreeVO[]) {
      const routes = this.filterAsyncRoutes(menus)
      this.routes = routes
      return routes
    },

    // 过滤异步路由（兼容旧方法）
    filterAsyncRoutes(menus: MenuTreeVO[]): RouteRecordRaw[] {
      const routes: RouteRecordRaw[] = []

      menus.forEach(menu => {
        // 只处理目录和菜单，不处理按钮
        if (menu.menuType === 3) return

        const route = {
          path: menu.routePath || '',
          name: menu.menuName,
          meta: {
            title: menu.menuName,
            icon: menu.icon,
            permission: menu.permission,
            hidden: menu.visible === 0
          },
          component: menu.component ? this.loadComponent(menu.component) : undefined,
          children: menu.children && menu.children.length > 0
            ? this.filterAsyncRoutes(menu.children)
            : undefined
        } as RouteRecordRaw

        routes.push(route)
      })

      return routes
    },

    // 重置权限
    resetPermission() {
      this.permissions = []
      this.menuTree = []
      this.routes = []
      this.dynamicRoutes = []
    }
  }
})
