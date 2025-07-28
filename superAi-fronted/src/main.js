import './assets/design-system.css'
import { createApp } from 'vue'
import router from './router'
import pinia from './stores'
import { useAuthStore } from './stores/auth'
import axios from 'axios'
import App from './App.vue'

// 性能监控和验证工具
import { initPerformanceMonitoring } from './utils/performance'
import { runFullValidation } from './utils/validation'

// 初始化性能监控
const performanceMonitor = initPerformanceMonitoring()

// 配置axios默认设置
axios.defaults.baseURL = ''
axios.defaults.timeout = 30000

// axios拦截器设置
axios.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('auth_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

axios.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response?.status === 401) {
      const authStore = useAuthStore()
      await authStore.logout()
      router.push('/login')
    }
    return Promise.reject(error)
  }
)

const app = createApp(App)

app.use(pinia)
app.use(router)

// 初始化认证状态
const authStore = useAuthStore()
authStore.initializeAuth()

app.mount('#app')

// 在开发环境下运行验证
if (import.meta.env.DEV) {
  // 延迟执行验证，确保应用完全加载
  setTimeout(() => {
    runFullValidation()
  }, 2000)
}
