import router, { add404Route } from './index'
import { useUserStore } from '@/stores/modules/user'
import { usePermissionStore } from '@/stores/permission'
import { getToken } from '@/utils/auth'

const whiteList = ['/login'] // no redirect whitelist

// 标记是否已加载动态路由
let dynamicRoutesLoaded = false

router.beforeEach(async (to, _from, next) => {
  const userStore = useUserStore()
  const permissionStore = usePermissionStore()
  const hasToken = getToken()

  if (hasToken) {
    if (to.path === '/login') {
      // if is logged in, redirect to the home page
      next({ path: '/' })
    } else {
      // determine whether the user has obtained his permission roles through getInfo
      const hasRoles = userStore.roles && userStore.roles.length > 0
      if (hasRoles) {
        // 如果已有角色但未加载动态路由，则加载
        if (!dynamicRoutesLoaded) {
          try {
            // 加载动态路由
            const routes = await permissionStore.loadRoutes()
            // 动态添加路由
            routes.forEach(route => {
              router.addRoute(route)
            })
            // 动态路由加载完成后添加 404 路由
            add404Route()
            dynamicRoutesLoaded = true
            // 重新导航到目标路由
            next({ ...to, replace: true })
          } catch (error) {
            console.error('Failed to load dynamic routes:', error)
            next()
          }
        } else {
          next()
        }
      } else {
        try {
          // get user info
          await userStore.getInfo()
          // 加载动态路由
          const routes = await permissionStore.loadRoutes()
          // 动态添加路由
          routes.forEach(route => {
            router.addRoute(route)
          })
          // 动态路由加载完成后添加 404 路由
          add404Route()
          dynamicRoutesLoaded = true
          // 重新导航到目标路由
          next({ ...to, replace: true })
        } catch (error) {
          // remove token and go to login page to re-login
          await userStore.resetToken()
          permissionStore.resetPermission()
          dynamicRoutesLoaded = false
          next(`/login?redirect=${to.path}`)
        }
      }
    }
  } else {
    /* has no token */
    if (whiteList.indexOf(to.path) !== -1) {
      // in the free login whitelist, go directly
      next()
    } else {
      // other pages that do not have permission to access are redirected to the login page.
      next(`/login?redirect=${to.path}`)
    }
  }
})

// 重置动态路由加载状态（用于登出时调用）
export function resetDynamicRoutes() {
  dynamicRoutesLoaded = false
}
