import { defineStore } from 'pinia'
import { login, logout, getInfo } from '@/api/auth'
import { getToken, setToken, removeToken } from '@/utils/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: getToken(),
    name: '',
    avatar: '',
    roles: [] as string[]
  }),
  actions: {
    // User Login
    async login(userInfo: any) {
      const { username, password } = userInfo
      // Mocking actual API call structure for now
      // In real app: const res = await login({ username: username.trim(), password: password })
      // For scaffold prototype, we might simulate success if backend isn't ready,
      // but the code below assumes a working backend or at least the API call structure.
      try {
        const res: any = await login({ username: username.trim(), password })
        const token = res?.token || 'mock-token' // Fallback for dev without backend
        this.token = token
        setToken(token)
        return res
      } catch (error) {
        return Promise.reject(error)
      }
    },

    // Get User Info
    async getInfo() {
      try {
        const res: any = await getInfo()
        const { roles, name, avatar } = res
        if (!roles || roles.length <= 0) {
          throw new Error('getInfo: roles must be a non-null array!')
        }
        this.roles = roles
        this.name = name
        this.avatar = avatar
        return res
      } catch (error) {
        return Promise.reject(error)
      }
    },

    // User Logout
    async logout() {
      try {
        await logout()
        this.token = ''
        this.roles = []
        removeToken()
      } catch (error) {
        return Promise.reject(error)
      }
    },

    // Remove token
    resetToken() {
      return new Promise<void>((resolve) => {
        this.token = ''
        this.roles = []
        removeToken()
        resolve()
      })
    }
  }
})
