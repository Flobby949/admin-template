import axios, { InternalAxiosRequestConfig, AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import { getToken } from '@/utils/auth'
import 'element-plus/theme-chalk/el-message.css'

// Create axios instance
const service = axios.create({
  baseURL: '/api', // Proxy target defined in vite.config.ts
  timeout: 5000
})

// Request interceptor
service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    // Add token to headers if it exists
    const token = getToken()
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error: any) => {
    return Promise.reject(error)
  }
)

// Response interceptor
service.interceptors.response.use(
  (response: AxiosResponse) => {
    // Handle binary data (blob, arraybuffer) - return raw response
    if (response.request.responseType === 'blob' || response.request.responseType === 'arraybuffer') {
      return response.data
    }

    const res = response.data

    // If the custom code is not 200, it is judged as an error.
    // Adjust this logic based on your actual backend response structure.
    if (res.code && res.code !== 200) {
      ElMessage({
        message: res.message || 'Error',
        type: 'error',
        duration: 5 * 1000
      })

      // 401: Illegal token; 50012: Other clients logged in; 50014: Token expired;
      if (res.code === 401 || res.code === 50012 || res.code === 50014) {
        // Handle logout or re-login logic here
        // For example, redirect to login page
      }
      return Promise.reject(new Error(res.message || 'Error'))
    } else {
      // Return data directly instead of the whole response wrapper
      return res.data
    }
  },
  (error: any) => {
    console.error('err' + error) // for debug
    ElMessage({
      message: error.message,
      type: 'error',
      duration: 5 * 1000
    })
    return Promise.reject(error)
  }
)

export default service
