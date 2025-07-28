<template>
  <div class="login-page">
    <div class="login-container">
      <!-- 背景装饰 -->
      <div class="bg-decoration">
        <div class="decoration-circle circle-1"></div>
        <div class="decoration-circle circle-2"></div>
        <div class="decoration-circle circle-3"></div>
      </div>
      
      <!-- 登录卡片 -->
      <div class="login-card">
        <!-- Logo和标题 -->
        <div class="login-header">
          <div class="logo">
            <i class="fas fa-dumbbell"></i>
          </div>
          <h1 class="login-title">AI 运动助手</h1>
          <p class="login-subtitle">开启您的智能健身之旅</p>
        </div>
        
        <!-- 登录表单 -->
        <form class="login-form" @submit.prevent="handleLogin">
          <!-- 邮箱输入 -->
          <div class="form-group">
            <label class="form-label">邮箱地址</label>
            <div class="input-wrapper">
              <i class="fas fa-envelope input-icon"></i>
              <input
                type="email"
                v-model="loginForm.email"
                class="form-input"
                placeholder="请输入您的邮箱"
                :class="{ error: errors.email }"
                @blur="validateEmail"
                @input="clearError('email')"
                required
              >
            </div>
            <div class="error-message" v-if="errors.email">{{ errors.email }}</div>
          </div>
          
          <!-- 密码输入 -->
          <div class="form-group">
            <label class="form-label">密码</label>
            <div class="input-wrapper">
              <i class="fas fa-lock input-icon"></i>
              <input
                :type="showPassword ? 'text' : 'password'"
                v-model="loginForm.password"
                class="form-input"
                placeholder="请输入您的密码"
                :class="{ error: errors.password }"
                @blur="validatePassword"
                @input="clearError('password')"
                required
              >
              <button
                type="button"
                class="password-toggle"
                @click="togglePassword"
              >
                <i :class="showPassword ? 'fas fa-eye-slash' : 'fas fa-eye'"></i>
              </button>
            </div>
            <div class="error-message" v-if="errors.password">{{ errors.password }}</div>
          </div>
          
          <!-- 记住我和忘记密码 -->
          <div class="form-options">
            <label class="checkbox-wrapper">
              <input type="checkbox" v-model="loginForm.rememberMe">
              <span class="checkmark"></span>
              <span class="checkbox-text">记住我</span>
            </label>
            <router-link to="/forgot-password" class="forgot-link">
              忘记密码？
            </router-link>
          </div>
          
          <!-- 错误提示 -->
          <div class="form-error" v-if="loginError">
            <i class="fas fa-exclamation-circle"></i>
            <span>{{ loginError }}</span>
          </div>
          
          <!-- 登录按钮 -->
          <button
            type="submit"
            class="login-btn"
            :disabled="isLoading || !isFormValid"
            :class="{ loading: isLoading }"
          >
            <div class="loading-spinner" v-if="isLoading"></div>
            <i class="fas fa-sign-in-alt" v-else></i>
            <span>{{ isLoading ? '登录中...' : '登录' }}</span>
          </button>
        </form>
        
        <!-- 第三方登录 -->
        <div class="social-login" v-if="showSocialLogin">
          <div class="divider">
            <span>或者</span>
          </div>
          <div class="social-buttons">
            <button class="social-btn google" @click="loginWithGoogle">
              <i class="fab fa-google"></i>
              <span>Google 登录</span>
            </button>
            <button class="social-btn wechat" @click="loginWithWechat">
              <i class="fab fa-weixin"></i>
              <span>微信登录</span>
            </button>
          </div>
        </div>
        
        <!-- 注册链接 -->
        <div class="register-link">
          <span>还没有账户？</span>
          <router-link to="/register" class="register-btn">立即注册</router-link>
        </div>
      </div>
      
      <!-- 底部信息 -->
      <div class="login-footer">
        <p>&copy; 2024 AI运动助手. 保留所有权利.</p>
        <div class="footer-links">
          <a href="#" class="footer-link">隐私政策</a>
          <a href="#" class="footer-link">服务条款</a>
          <a href="#" class="footer-link">帮助中心</a>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

// 响应式数据
const loginForm = ref({
  email: '',
  password: '',
  rememberMe: false
})

const errors = ref({
  email: '',
  password: ''
})

const showPassword = ref(false)
const isLoading = ref(false)
const loginError = ref('')
const showSocialLogin = ref(true)

// 计算属性
const isFormValid = computed(() => {
  return loginForm.value.email &&
         loginForm.value.password &&
         !errors.value.email &&
         !errors.value.password
})

// 表单验证
const validateEmail = () => {
  const email = loginForm.value.email.trim()
  if (!email) {
    errors.value.email = '请输入邮箱地址'
    return false
  }
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
    errors.value.email = '请输入有效的邮箱地址'
    return false
  }
  errors.value.email = ''
  return true
}

const validatePassword = () => {
  const password = loginForm.value.password
  if (!password) {
    errors.value.password = '请输入密码'
    return false
  }
  if (password.length < 6) {
    errors.value.password = '密码至少需要6个字符'
    return false
  }
  errors.value.password = ''
  return true
}

// 清除错误
const clearError = (field) => {
  errors.value[field] = ''
  if (loginError.value) {
    loginError.value = ''
  }
}

// 切换密码显示
const togglePassword = () => {
  showPassword.value = !showPassword.value
}

// 处理登录
const handleLogin = async () => {
  // 验证表单
  if (!validateEmail() || !validatePassword()) {
    return
  }
  
  isLoading.value = true
  loginError.value = ''
  
  try {
    const result = await authStore.login({
      email: loginForm.value.email.trim(),
      password: loginForm.value.password,
      rememberMe: loginForm.value.rememberMe
    })
    
    if (result.success) {
      // 登录成功，跳转到主页
      router.push('/')
    } else {
      loginError.value = result.error || '登录失败，请检查您的邮箱和密码'
    }
  } catch (error) {
    console.error('登录错误:', error)
    loginError.value = '网络错误，请稍后重试'
  } finally {
    isLoading.value = false
  }
}

// 第三方登录
const loginWithGoogle = async () => {
  try {
    // 这里实现Google登录逻辑
    console.log('Google登录')
  } catch (error) {
    loginError.value = 'Google登录失败'
  }
}

const loginWithWechat = async () => {
  try {
    // 这里实现微信登录逻辑
    console.log('微信登录')
  } catch (error) {
    loginError.value = '微信登录失败'
  }
}

// 键盘事件处理
const handleKeyDown = (event) => {
  if (event.key === 'Enter' && isFormValid.value) {
    handleLogin()
  }
}

onMounted(() => {
  // 如果已经登录，直接跳转
  if (authStore.isLoggedIn) {
    router.push('/')
    return
  }
  
  // 绑定键盘事件
  document.addEventListener('keydown', handleKeyDown)
  
  // 从本地存储恢复记住的邮箱
  const rememberedEmail = localStorage.getItem('remembered_email')
  if (rememberedEmail) {
    loginForm.value.email = rememberedEmail
    loginForm.value.rememberMe = true
  }
})
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  background: var(--gradient-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-lg);
  position: relative;
  overflow: hidden;
}

.login-container {
  width: 100%;
  max-width: 400px;
  position: relative;
  z-index: 2;
}

.bg-decoration {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1;
}

.decoration-circle {
  position: absolute;
  border-radius: var(--radius-full);
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
}

.circle-1 {
  width: 300px;
  height: 300px;
  top: -150px;
  right: -150px;
  animation: float 6s ease-in-out infinite;
}

.circle-2 {
  width: 200px;
  height: 200px;
  bottom: -100px;
  left: -100px;
  animation: float 8s ease-in-out infinite reverse;
}

.circle-3 {
  width: 150px;
  height: 150px;
  top: 50%;
  left: -75px;
  animation: float 10s ease-in-out infinite;
}

.login-card {
  background: var(--bg-secondary);
  border-radius: var(--radius-2xl);
  padding: var(--spacing-2xl);
  box-shadow: var(--shadow-xl);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.login-header {
  text-align: center;
  margin-bottom: var(--spacing-2xl);
}

.logo {
  width: 80px;
  height: 80px;
  background: var(--gradient-fitness);
  border-radius: var(--radius-2xl);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--font-2xl);
  color: var(--text-white);
  margin: 0 auto var(--spacing-lg);
  box-shadow: var(--shadow-lg);
}

.login-title {
  font-size: var(--font-2xl);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  margin-bottom: var(--spacing-xs);
}

.login-subtitle {
  color: var(--text-secondary);
  font-size: var(--font-base);
}

.login-form {
  margin-bottom: var(--spacing-xl);
}

.form-group {
  margin-bottom: var(--spacing-lg);
}

.form-label {
  display: block;
  font-size: var(--font-sm);
  font-weight: var(--font-medium);
  color: var(--text-primary);
  margin-bottom: var(--spacing-xs);
}

.input-wrapper {
  position: relative;
}

.input-icon {
  position: absolute;
  left: var(--spacing-md);
  top: 50%;
  transform: translateY(-50%);
  color: var(--text-light);
  font-size: var(--font-sm);
}

.form-input {
  width: 100%;
  padding: var(--spacing-md) var(--spacing-md) var(--spacing-md) 44px;
  border: 2px solid var(--border-color);
  border-radius: var(--radius-lg);
  font-size: var(--font-base);
  background: var(--bg-secondary);
  color: var(--text-primary);
  transition: all var(--duration-fast) var(--ease-in-out);
}

.form-input:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgb(59 130 246 / 0.1);
}

.form-input.error {
  border-color: var(--danger-color);
}

.form-input.error:focus {
  box-shadow: 0 0 0 3px rgb(239 68 68 / 0.1);
}

.password-toggle {
  position: absolute;
  right: var(--spacing-md);
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  color: var(--text-light);
  cursor: pointer;
  padding: var(--spacing-xs);
  border-radius: var(--radius-sm);
  transition: all var(--duration-fast) var(--ease-in-out);
}

.password-toggle:hover {
  color: var(--text-primary);
  background: var(--bg-primary);
}

.error-message {
  color: var(--danger-color);
  font-size: var(--font-sm);
  margin-top: var(--spacing-xs);
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
}

.form-options {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--spacing-lg);
}

.checkbox-wrapper {
  display: flex;
  align-items: center;
  cursor: pointer;
  font-size: var(--font-sm);
  color: var(--text-secondary);
}

.checkbox-wrapper input[type="checkbox"] {
  display: none;
}

.checkmark {
  width: 18px;
  height: 18px;
  border: 2px solid var(--border-color);
  border-radius: var(--radius-sm);
  margin-right: var(--spacing-xs);
  position: relative;
  transition: all var(--duration-fast) var(--ease-in-out);
}

.checkbox-wrapper input[type="checkbox"]:checked + .checkmark {
  background: var(--primary-color);
  border-color: var(--primary-color);
}

.checkbox-wrapper input[type="checkbox"]:checked + .checkmark::after {
  content: '✓';
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  color: var(--text-white);
  font-size: var(--font-xs);
  font-weight: var(--font-bold);
}

.forgot-link {
  color: var(--primary-color);
  text-decoration: none;
  font-size: var(--font-sm);
  transition: all var(--duration-fast) var(--ease-in-out);
}

.forgot-link:hover {
  color: var(--primary-dark);
  text-decoration: underline;
}

.form-error {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  color: var(--danger-color);
  font-size: var(--font-sm);
  background: rgba(239, 68, 68, 0.1);
  padding: var(--spacing-md);
  border-radius: var(--radius-md);
  margin-bottom: var(--spacing-lg);
  border: 1px solid rgba(239, 68, 68, 0.2);
}

.login-btn {
  width: 100%;
  padding: var(--spacing-md);
  background: var(--gradient-fitness);
  color: var(--text-white);
  border: none;
  border-radius: var(--radius-lg);
  font-size: var(--font-base);
  font-weight: var(--font-semibold);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-sm);
  transition: all var(--duration-fast) var(--ease-in-out);
  min-height: 48px;
}

.login-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: var(--shadow-lg);
}

.login-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
  transform: none;
}

.login-btn.loading {
  pointer-events: none;
}

.social-login {
  margin-bottom: var(--spacing-xl);
}

.divider {
  text-align: center;
  margin: var(--spacing-xl) 0;
  position: relative;
  color: var(--text-light);
  font-size: var(--font-sm);
}

.divider::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  height: 1px;
  background: var(--border-color);
  z-index: 1;
}

.divider span {
  background: var(--bg-secondary);
  padding: 0 var(--spacing-md);
  position: relative;
  z-index: 2;
}

.social-buttons {
  display: flex;
  gap: var(--spacing-sm);
}

.social-btn {
  flex: 1;
  padding: var(--spacing-md);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  background: var(--bg-secondary);
  color: var(--text-primary);
  font-size: var(--font-sm);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-xs);
  transition: all var(--duration-fast) var(--ease-in-out);
}

.social-btn:hover {
  border-color: var(--primary-color);
  transform: translateY(-1px);
}

.social-btn.google:hover {
  color: #db4437;
  border-color: #db4437;
}

.social-btn.wechat:hover {
  color: #07c160;
  border-color: #07c160;
}

.register-link {
  text-align: center;
  font-size: var(--font-sm);
  color: var(--text-secondary);
}

.register-btn {
  color: var(--primary-color);
  text-decoration: none;
  font-weight: var(--font-medium);
  margin-left: var(--spacing-xs);
  transition: all var(--duration-fast) var(--ease-in-out);
}

.register-btn:hover {
  color: var(--primary-dark);
  text-decoration: underline;
}

.login-footer {
  text-align: center;
  margin-top: var(--spacing-xl);
  color: rgba(255, 255, 255, 0.8);
  font-size: var(--font-sm);
}

.footer-links {
  display: flex;
  justify-content: center;
  gap: var(--spacing-lg);
  margin-top: var(--spacing-sm);
}

.footer-link {
  color: rgba(255, 255, 255, 0.6);
  text-decoration: none;
  transition: all var(--duration-fast) var(--ease-in-out);
}

.footer-link:hover {
  color: rgba(255, 255, 255, 0.9);
  text-decoration: underline;
}

/* 动画 */
@keyframes float {
  0%, 100% { transform: translateY(0px); }
  50% { transform: translateY(-20px); }
}

/* 移动端适配 */
@media (max-width: 767px) {
  .login-page {
    padding: var(--spacing-md);
  }
  
  .login-card {
    padding: var(--spacing-xl);
  }
  
  .logo {
    width: 64px;
    height: 64px;
    font-size: var(--font-xl);
  }
  
  .login-title {
    font-size: var(--font-xl);
  }
  
  .social-buttons {
    flex-direction: column;
  }
  
  .footer-links {
    flex-direction: column;
    gap: var(--spacing-sm);
  }
  
  .form-options {
    flex-direction: column;
    gap: var(--spacing-sm);
    align-items: flex-start;
  }
}

@media (max-width: 480px) {
  .login-container {
    max-width: 100%;
  }
  
  .login-card {
    padding: var(--spacing-lg);
  }
}
</style>