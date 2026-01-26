import router from './index'
import { useUserStore } from '@/stores/modules/user'
import { getToken } from '@/utils/auth'

const whiteList = ['/login'] // no redirect whitelist

router.beforeEach(async (to, _from, next) => {
  const userStore = useUserStore()
  const hasToken = getToken()

  if (hasToken) {
    if (to.path === '/login') {
      // if is logged in, redirect to the home page
      next({ path: '/' })
    } else {
      // determine whether the user has obtained his permission roles through getInfo
      const hasRoles = userStore.roles && userStore.roles.length > 0
      if (hasRoles) {
        next()
      } else {
        try {
          // get user info
          await userStore.getInfo()
          next()
        } catch (error) {
          // remove token and go to login page to re-login
          await userStore.resetToken()
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
