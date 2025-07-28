<template>
  <div class="register-page">
    <div class="register-container">
      <!-- 背景装饰 -->
      <div class="bg-decoration">
        <div class="decoration-circle circle-1"></div>
        <div class="decoration-circle circle-2"></div>
        <div class="decoration-circle circle-3"></div>
      </div>
      
      <!-- 注册卡片 -->
      <div class="register-card">
        <!-- Logo和标题 -->
        <div class="register-header">
          <div class="logo">
            <i class="fas fa-dumbbell"></i>
          </div>
          <h1 class="register-title">加入AI运动助手</h1>
          <p class="register-subtitle">开始您的个性化健身之旅</p>
        </div>
        
        <!-- 步骤指示器 -->
        <div class="step-indicator">
          <div 
            v-for="(step, index) in steps"
            :key="step.id"
            class="step-item"
            :class="{ 
              active: currentStep === index + 1,
              completed: currentStep > index + 1
            }"
          >
            <div class="step-number">
              <i v-if="currentStep > index + 1" class="fas fa-check"></i>
              <span v-else>{{ index + 1 }}</span>
            </div>
            <span class="step-label">{{ step.label }}</span>
          </div>
        </div>
        
        <!-- 注册表单 -->
        <form class="register-form" @submit.prevent="handleSubmit">
          <!-- 第一步：基本信息 -->
          <div class="form-step" v-if="currentStep === 1">
            <h3 class="step-title">基本信息</h3>
            
            <div class="form-group">
              <label class="form-label">姓名 *</label>
              <div class="input-wrapper">
                <i class="fas fa-user input-icon"></i>
                <input
                  type="text"
                  v-model="registerForm.name"
                  class="form-input"
                  placeholder="请输入您的姓名"
                  :class="{ error: errors.name }"
                  @blur="validateName"
                  @input="clearError('name')"
                  required
                >
              </div>
              <div class="error-message" v-if="errors.name">{{ errors.name }}</div>
            </div>
            
            <div class="form-group">
              <label class="form-label">邮箱地址 *</label>
              <div class="input-wrapper">
                <i class="fas fa-envelope input-icon"></i>
                <input
                  type="email"
                  v-model="registerForm.email"
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
            
            <div class="form-group">
              <label class="form-label">密码 *</label>
              <div class="input-wrapper">
                <i class="fas fa-lock input-icon"></i>
                <input
                  :type="showPassword ? 'text' : 'password'"
                  v-model="registerForm.password"
                  class="form-input"
                  placeholder="请输入密码（至少6位）"
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
            
            <div class="form-group">
              <label class="form-label">确认密码 *</label>
              <div class="input-wrapper">
                <i class="fas fa-lock input-icon"></i>
                <input
                  type="password"
                  v-model="registerForm.confirmPassword"
                  class="form-input"
                  placeholder="请再次输入密码"
                  :class="{ error: errors.confirmPassword }"
                  @blur="validateConfirmPassword"
                  @input="clearError('confirmPassword')"
                  required
                >
              </div>
              <div class="error-message" v-if="errors.confirmPassword">{{ errors.confirmPassword }}</div>
            </div>
          </div>
          
          <!-- 第二步：健身信息 -->
          <div class="form-step" v-if="currentStep === 2">
            <h3 class="step-title">健身信息</h3>
            
            <div class="form-group">
              <label class="form-label">年龄</label>
              <div class="input-wrapper">
                <i class="fas fa-calendar input-icon"></i>
                <input
                  type="number"
                  v-model.number="registerForm.age"
                  class="form-input"
                  placeholder="请输入您的年龄"
                  min="16"
                  max="100"
                >
              </div>
            </div>
            
            <div class="form-row">
              <div class="form-group">
                <label class="form-label">身高 (cm)</label>
                <div class="input-wrapper">
                  <i class="fas fa-ruler-vertical input-icon"></i>
                  <input
                    type="number"
                    v-model.number="registerForm.height"
                    class="form-input"
                    placeholder="身高"
                    min="100"
                    max="250"
                  >
                </div>
              </div>
              
              <div class="form-group">
                <label class="form-label">体重 (kg)</label>
                <div class="input-wrapper">
                  <i class="fas fa-weight input-icon"></i>
                  <input
                    type="number"
                    v-model.number="registerForm.weight"
                    class="form-input"
                    placeholder="体重"
                    min="30"
                    max="300"
                  >
                </div>
              </div>
            </div>
            
            <div class="form-group">
              <label class="form-label">健身水平</label>
              <div class="radio-group">
                <label 
                  v-for="level in fitnessLevels"
                  :key="level.value"
                  class="radio-item"
                  :class="{ selected: registerForm.fitnessLevel === level.value }"
                >
                  <input
                    type="radio"
                    :value="level.value"
                    v-model="registerForm.fitnessLevel"
                  >
                  <div class="radio-content">
                    <i :class="level.icon"></i>
                    <div class="radio-text">
                      <div class="radio-title">{{ level.label }}</div>
                      <div class="radio-desc">{{ level.description }}</div>
                    </div>
                  </div>
                </label>
              </div>
            </div>
          </div>
          
          <!-- 第三步：健身目标 -->
          <div class="form-step" v-if="currentStep === 3">
            <h3 class="step-title">健身目标</h3>
            
            <div class="form-group">
              <label class="form-label">主要目标（可多选）</label>
              <div class="checkbox-grid">
                <label 
                  v-for="goal in fitnessGoals"
                  :key="goal.value"
                  class="checkbox-item"
                  :class="{ selected: registerForm.goals.includes(goal.value) }"
                >
                  <input
                    type="checkbox"
                    :value="goal.value"
                    v-model="registerForm.goals"
                  >
                  <div class="checkbox-content">
                    <i :class="goal.icon"></i>
                    <div class="checkbox-text">
                      <div class="checkbox-title">{{ goal.label }}</div>
                      <div class="checkbox-desc">{{ goal.description }}</div>
                    </div>
                  </div>
                </label>
              </div>
            </div>
            
            <div class="form-group">
              <label class="form-label">可投入时间（每周）</label>
              <div class="time-options">
                <button
                  v-for="time in timeOptions"
                  :key="time.value"
                  type="button"
                  class="time-btn"
                  :class="{ selected: registerForm.weeklyTime === time.value }"
                  @click="registerForm.weeklyTime = time.value"
                >
                  <i :class="time.icon"></i>
                  <span>{{ time.label }}</span>
                </button>
              </div>
            </div>
          </div>
          
          <!-- 错误提示 -->
          <div class="form-error" v-if="registerError">
            <i class="fas fa-exclamation-circle"></i>
            <span>{{ registerError }}</span>
          </div>
          
          <!-- 操作按钮 -->
          <div class="form-actions">
            <button
              v-if="currentStep > 1"
              type="button"
              class="prev-btn"
              @click="prevStep"
            >
              <i class="fas fa-chevron-left"></i>
              <span>上一步</span>
            </button>
            
            <button
              v-if="currentStep < steps.length"
              type="button"
              class="next-btn"
              @click="nextStep"
              :disabled="!canProceed"
            >
              <span>下一步</span>
              <i class="fas fa-chevron-right"></i>
            </button>
            
            <button
              v-if="currentStep === steps.length"
              type="submit"
              class="register-btn"
              :disabled="isLoading || !canRegister"
              :class="{ loading: isLoading }"
            >
              <div class="loading-spinner" v-if="isLoading"></div>
              <i class="fas fa-user-plus" v-else></i>
              <span>{{ isLoading ? '注册中...' : '完成注册' }}</span>
            </button>
          </div>
        </form>
        
        <!-- 登录链接 -->
        <div class="login-link">
          <span>已有账户？</span>
          <router-link to="/login" class="login-btn">立即登录</router-link>
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
const currentStep = ref(1)
const showPassword = ref(false)
const isLoading = ref(false)
const registerError = ref('')

const registerForm = ref({
  name: '',
  email: '',
  password: '',
  confirmPassword: '',
  age: null,
  height: null,
  weight: null,
  fitnessLevel: 'beginner',
  goals: [],
  weeklyTime: '3-5h'
})

const errors = ref({
  name: '',
  email: '',
  password: '',
  confirmPassword: ''
})

// 步骤配置
const steps = ref([
  { id: 1, label: '基本信息' },
  { id: 2, label: '健身信息' },
  { id: 3, label: '健身目标' }
])

// 健身水平选项
const fitnessLevels = ref([
  {
    value: 'beginner',
    label: '初学者',
    description: '刚开始健身或很少运动',
    icon: 'fas fa-seedling'
  },
  {
    value: 'intermediate',
    label: '中级',
    description: '有一定健身基础，定期运动',
    icon: 'fas fa-running'
  },
  {
    value: 'advanced',
    label: '高级',
    description: '经验丰富，有系统训练习惯',
    icon: 'fas fa-medal'
  }
])

// 健身目标选项
const fitnessGoals = ref([
  {
    value: 'weight_loss',
    label: '减重瘦身',
    description: '降低体重和体脂率',
    icon: 'fas fa-weight'
  },
  {
    value: 'muscle_gain',
    label: '增肌塑形',
    description: '增加肌肉量，改善体型',
    icon: 'fas fa-dumbbell'
  },
  {
    value: 'endurance',
    label: '提升耐力',
    description: '增强心肺功能和体能',
    icon: 'fas fa-heart'
  },
  {
    value: 'flexibility',
    label: '增强柔韧',
    description: '提高身体柔韧性和平衡',
    icon: 'fas fa-yoga'
  },
  {
    value: 'strength',
    label: '力量训练',
    description: '增强肌肉力量和爆发力',
    icon: 'fas fa-fist-raised'
  },
  {
    value: 'health',
    label: '健康养生',
    description: '保持身体健康，预防疾病',
    icon: 'fas fa-leaf'
  }
])

// 时间投入选项
const timeOptions = ref([
  { value: '1-2h', label: '1-2小时', icon: 'fas fa-clock' },
  { value: '3-5h', label: '3-5小时', icon: 'fas fa-business-time' },
  { value: '5-7h', label: '5-7小时', icon: 'fas fa-calendar-week' },
  { value: '7h+', label: '7小时以上', icon: 'fas fa-fire' }
])

// 计算属性
const canProceed = computed(() => {
  switch (currentStep.value) {
    case 1:
      return registerForm.value.name &&
             registerForm.value.email &&
             registerForm.value.password &&
             registerForm.value.confirmPassword &&
             !Object.values(errors.value).some(error => error)
    case 2:
      return true // 第二步都是可选项
    case 3:
      return registerForm.value.goals.length > 0
    default:
      return false
  }
})

const canRegister = computed(() => {
  return canProceed.value && currentStep.value === steps.value.length
})

// 表单验证
const validateName = () => {
  const name = registerForm.value.name.trim()
  if (!name) {
    errors.value.name = '请输入姓名'
    return false
  }
  if (name.length < 2) {
    errors.value.name = '姓名至少需要2个字符'
    return false
  }
  errors.value.name = ''
  return true
}

const validateEmail = () => {
  const email = registerForm.value.email.trim()
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
  const password = registerForm.value.password
  if (!password) {
    errors.value.password = '请输入密码'
    return false
  }
  if (password.length < 6) {
    errors.value.password = '密码至少需要6个字符'
    return false
  }
  if (!/(?=.*[a-zA-Z])(?=.*\d)/.test(password)) {
    errors.value.password = '密码需要包含字母和数字'
    return false
  }
  errors.value.password = ''
  return true
}

const validateConfirmPassword = () => {
  const confirmPassword = registerForm.value.confirmPassword
  if (!confirmPassword) {
    errors.value.confirmPassword = '请确认密码'
    return false
  }
  if (confirmPassword !== registerForm.value.password) {
    errors.value.confirmPassword = '两次输入的密码不一致'
    return false
  }
  errors.value.confirmPassword = ''
  return true
}

// 清除错误
const clearError = (field) => {
  errors.value[field] = ''
  if (registerError.value) {
    registerError.value = ''
  }
}

// 切换密码显示
const togglePassword = () => {
  showPassword.value = !showPassword.value
}

// 步骤控制
const nextStep = () => {
  if (canProceed.value && currentStep.value < steps.value.length) {
    currentStep.value++
  }
}

const prevStep = () => {
  if (currentStep.value > 1) {
    currentStep.value--
  }
}

// 处理注册
const handleSubmit = async () => {
  if (!canRegister.value) return
  
  isLoading.value = true
  registerError.value = ''
  
  try {
    const userData = {
      name: registerForm.value.name.trim(),
      email: registerForm.value.email.trim(),
      password: registerForm.value.password,
      profile: {
        age: registerForm.value.age,
        height: registerForm.value.height,
        weight: registerForm.value.weight,
        fitnessLevel: registerForm.value.fitnessLevel,
        goals: registerForm.value.goals,
        weeklyTime: registerForm.value.weeklyTime
      }
    }
    
    const result = await authStore.register(userData)
    
    if (result.success) {
      // 注册成功，跳转到主页
      router.push('/')
    } else {
      registerError.value = result.error || '注册失败，请稍后重试'
    }
  } catch (error) {
    console.error('注册错误:', error)
    registerError.value = '网络错误，请稍后重试'
  } finally {
    isLoading.value = false
  }
}

onMounted(() => {
  // 如果已经登录，直接跳转
  if (authStore.isLoggedIn) {
    router.push('/')
  }
})
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  background: var(--gradient-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-lg);
  position: relative;
  overflow: hidden;
}

.register-container {
  width: 100%;
  max-width: 500px;
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

.register-card {
  background: var(--bg-secondary);
  border-radius: var(--radius-2xl);
  padding: var(--spacing-2xl);
  box-shadow: var(--shadow-xl);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.register-header {
  text-align: center;
  margin-bottom: var(--spacing-xl);
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

.register-title {
  font-size: var(--font-2xl);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  margin-bottom: var(--spacing-xs);
}

.register-subtitle {
  color: var(--text-secondary);
  font-size: var(--font-base);
}

.step-indicator {
  display: flex;
  justify-content: space-between;
  margin-bottom: var(--spacing-2xl);
  position: relative;
}

.step-indicator::before {
  content: '';
  position: absolute;
  top: 20px;
  left: 20px;
  right: 20px;
  height: 2px;
  background: var(--border-light);
  z-index: 1;
}

.step-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  z-index: 2;
}

.step-number {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-full);
  background: var(--bg-secondary);
  border: 2px solid var(--border-color);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: var(--font-semibold);
  color: var(--text-secondary);
  margin-bottom: var(--spacing-xs);
  transition: all var(--duration-fast) var(--ease-in-out);
}

.step-item.active .step-number {
  background: var(--primary-color);
  border-color: var(--primary-color);
  color: var(--text-white);
}

.step-item.completed .step-number {
  background: var(--success-color);
  border-color: var(--success-color);
  color: var(--text-white);
}

.step-label {
  font-size: var(--font-sm);
  color: var(--text-secondary);
  text-align: center;
}

.step-item.active .step-label {
  color: var(--text-primary);
  font-weight: var(--font-medium);
}

.register-form {
  margin-bottom: var(--spacing-xl);
}

.step-title {
  font-size: var(--font-xl);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
  margin-bottom: var(--spacing-lg);
  text-align: center;
}

.form-group {
  margin-bottom: var(--spacing-lg);
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--spacing-md);
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
}

.radio-group {
  display: grid;
  gap: var(--spacing-md);
}

.radio-item {
  border: 2px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: var(--spacing-md);
  cursor: pointer;
  transition: all var(--duration-fast) var(--ease-in-out);
}

.radio-item input[type="radio"] {
  display: none;
}

.radio-item.selected {
  border-color: var(--primary-color);
  background: rgba(59, 130, 246, 0.05);
}

.radio-content {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.radio-content i {
  font-size: var(--font-xl);
  color: var(--primary-color);
  width: 32px;
}

.radio-title {
  font-weight: var(--font-semibold);
  color: var(--text-primary);
  margin-bottom: var(--spacing-xs);
}

.radio-desc {
  font-size: var(--font-sm);
  color: var(--text-secondary);
}

.checkbox-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: var(--spacing-sm);
}

.checkbox-item {
  border: 2px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: var(--spacing-md);
  cursor: pointer;
  transition: all var(--duration-fast) var(--ease-in-out);
}

.checkbox-item input[type="checkbox"] {
  display: none;
}

.checkbox-item.selected {
  border-color: var(--primary-color);
  background: rgba(59, 130, 246, 0.05);
}

.checkbox-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: var(--spacing-sm);
}

.checkbox-content i {
  font-size: var(--font-xl);
  color: var(--primary-color);
}

.checkbox-title {
  font-weight: var(--font-semibold);
  color: var(--text-primary);
}

.checkbox-desc {
  font-size: var(--font-xs);
  color: var(--text-secondary);
}

.time-options {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: var(--spacing-sm);
}

.time-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--spacing-xs);
  padding: var(--spacing-md);
  border: 2px solid var(--border-color);
  border-radius: var(--radius-lg);
  background: var(--bg-secondary);
  color: var(--text-primary);
  cursor: pointer;
  transition: all var(--duration-fast) var(--ease-in-out);
}

.time-btn:hover {
  border-color: var(--primary-color);
}

.time-btn.selected {
  border-color: var(--primary-color);
  background: rgba(59, 130, 246, 0.05);
  color: var(--primary-color);
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

.form-actions {
  display: flex;
  gap: var(--spacing-md);
  justify-content: space-between;
}

.prev-btn, .next-btn, .register-btn {
  flex: 1;
  padding: var(--spacing-md);
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

.prev-btn {
  background: var(--bg-primary);
  color: var(--text-primary);
  border: 1px solid var(--border-color);
}

.prev-btn:hover {
  background: var(--border-color);
}

.next-btn, .register-btn {
  background: var(--gradient-fitness);
  color: var(--text-white);
}

.next-btn:hover:not(:disabled), .register-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: var(--shadow-lg);
}

.next-btn:disabled, .register-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
  transform: none;
}

.login-link {
  text-align: center;
  font-size: var(--font-sm);
  color: var(--text-secondary);
}

.login-btn {
  color: var(--primary-color);
  text-decoration: none;
  font-weight: var(--font-medium);
  margin-left: var(--spacing-xs);
  transition: all var(--duration-fast) var(--ease-in-out);
}

.login-btn:hover {
  color: var(--primary-dark);
  text-decoration: underline;
}

/* 动画 */
@keyframes float {
  0%, 100% { transform: translateY(0px); }
  50% { transform: translateY(-20px); }
}

/* 移动端适配 */
@media (max-width: 767px) {
  .register-page {
    padding: var(--spacing-md);
  }
  
  .register-card {
    padding: var(--spacing-xl);
  }
  
  .logo {
    width: 64px;
    height: 64px;
    font-size: var(--font-xl);
  }
  
  .register-title {
    font-size: var(--font-xl);
  }
  
  .step-indicator {
    margin-bottom: var(--spacing-xl);
  }
  
  .step-number {
    width: 32px;
    height: 32px;
    font-size: var(--font-sm);
  }
  
  .step-label {
    font-size: var(--font-xs);
  }
  
  .form-row {
    grid-template-columns: 1fr;
  }
  
  .checkbox-grid {
    grid-template-columns: 1fr;
  }
  
  .time-options {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .form-actions {
    flex-direction: column;
  }
}

@media (max-width: 480px) {
  .register-container {
    max-width: 100%;
  }
  
  .register-card {
    padding: var(--spacing-lg);
  }
  
  .step-indicator::before {
    display: none;
  }
  
  .step-item {
    flex-direction: row;
    gap: var(--spacing-xs);
  }
  
  .step-number {
    width: 24px;
    height: 24px;
    font-size: var(--font-xs);
    margin-bottom: 0;
  }
}
</style>