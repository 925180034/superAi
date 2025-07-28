import { defineStore } from 'pinia'
import axios from 'axios'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: null,
    token: localStorage.getItem('auth_token'),
    isLoading: false,
    loginAttempts: 0,
    isAuthenticated: false
  }),
  
  getters: {
    userInitials: (state) => {
      if (!state.user?.name) return 'U'
      return state.user.name.split(' ').map(n => n[0]).join('').toUpperCase()
    },
    userRole: (state) => state.user?.role || 'user',
    isLoggedIn: (state) => !!state.token && !!state.user,
    userName: (state) => state.user?.name || '用户',
    userFitnessLevel: (state) => state.user?.fitnessLevel || '初级'
  },
  
  actions: {
    async login(credentials) {
      this.isLoading = true
      try {
        const response = await axios.post('/api/auth/login', credentials)
        const { token, user } = response.data
        
        this.token = token
        this.user = user
        this.isAuthenticated = true
        this.loginAttempts = 0
        
        localStorage.setItem('auth_token', token)
        axios.defaults.headers.common['Authorization'] = `Bearer ${token}`
        
        return { success: true }
      } catch (error) {
        this.loginAttempts++
        return { 
          success: false, 
          error: error.response?.data?.message || '登录失败' 
        }
      } finally {
        this.isLoading = false
      }
    },
    
    async register(userData) {
      this.isLoading = true
      try {
        const response = await axios.post('/api/auth/register', userData)
        const { token, user } = response.data
        
        this.token = token
        this.user = user
        this.isAuthenticated = true
        
        localStorage.setItem('auth_token', token)
        axios.defaults.headers.common['Authorization'] = `Bearer ${token}`
        
        return { success: true }
      } catch (error) {
        return { 
          success: false, 
          error: error.response?.data?.message || '注册失败' 
        }
      } finally {
        this.isLoading = false
      }
    },
    
    async logout() {
      try {
        if (this.token) {
          await axios.post('/api/auth/logout')
        }
      } catch (error) {
        console.error('Logout error:', error)
      } finally {
        this.token = null
        this.user = null
        this.isAuthenticated = false
        
        localStorage.removeItem('auth_token')
        delete axios.defaults.headers.common['Authorization']
      }
    },
    
    async refreshToken() {
      if (!this.token) return false
      
      try {
        const response = await axios.post('/api/auth/refresh')
        const { token } = response.data
        
        this.token = token
        localStorage.setItem('auth_token', token)
        axios.defaults.headers.common['Authorization'] = `Bearer ${token}`
        
        return true
      } catch (error) {
        this.logout()
        return false
      }
    },
    
    async fetchUser() {
      if (!this.token) return false
      
      try {
        const response = await axios.get('/api/auth/user')
        this.user = response.data
        this.isAuthenticated = true
        return true
      } catch (error) {
        this.logout()
        return false
      }
    },
    
    updateProfile(profileData) {
      if (this.user) {
        this.user = { ...this.user, ...profileData }
      }
    },
    
    initializeAuth() {
      // 为了前端界面检查，暂时设置演示用户（生产环境需要删除）
      if (import.meta.env.DEV) {
        this.token = 'demo-token'
        this.user = {
          id: 1,
          name: '演示用户',
          email: 'demo@example.com',
          role: 'user',
          fitnessLevel: '中级',
          profile: {
            age: 28,
            gender: 'male',
            height: 175,
            weight: 70,
            goals: ['muscle_gain', 'strength'],
            bio: '热爱健身的演示用户'
          }
        }
        this.isAuthenticated = true
        localStorage.setItem('auth_token', this.token)
        return
      }
      
      // 正常的认证逻辑
      const token = localStorage.getItem('auth_token')
      if (token) {
        this.token = token
        axios.defaults.headers.common['Authorization'] = `Bearer ${token}`
        this.fetchUser()
      }
    }
  }
})