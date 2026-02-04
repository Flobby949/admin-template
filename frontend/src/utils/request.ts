import axios, { InternalAxiosRequestConfig, AxiosResponse, AxiosError, AxiosRequestConfig } from 'axios'
import { getToken } from '@/utils/auth'

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
  (error: AxiosError) => {
    return Promise.reject(error)
  }
)

// Response interceptor
service.interceptors.response.use(
  (response: AxiosResponse) => {
    // Handle binary data (blob, arraybuffer) - return raw response
    if (
      response.request.responseType === 'blob' ||
      response.request.responseType === 'arraybuffer'
    ) {
      return response.data
    }

    const res = response.data

    // If the custom code is not 200, it is judged as an error.
    // Adjust this logic based on your actual backend response structure.
    if (res.code && res.code !== 200) {
      // 不在这里显示 ElMessage，由业务代码决定是否显示
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
  (error: AxiosError) => {
    console.error('err' + error) // for debug

    // 提取后端返回的错误消息
    let message = error.message
    const responseData = error.response?.data as { message?: string } | undefined
    if (responseData?.message) {
      message = responseData.message
    }

    // 将错误消息附加到 error 对象上，供业务代码使用
    error.message = message

    // 不在这里显示 ElMessage，由业务代码决定是否显示
    return Promise.reject(error)
  }
)

// Type-safe request wrapper
// The interceptor returns res.data, so we need to override the return type
interface RequestInstance {
  <T = any>(config: AxiosRequestConfig): Promise<T>
  get<T = any>(url: string, config?: AxiosRequestConfig): Promise<T>
  delete<T = any>(url: string, config?: AxiosRequestConfig): Promise<T>
  head<T = any>(url: string, config?: AxiosRequestConfig): Promise<T>
  options<T = any>(url: string, config?: AxiosRequestConfig): Promise<T>
  post<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T>
  put<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T>
  patch<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T>
}

export default service as RequestInstance
