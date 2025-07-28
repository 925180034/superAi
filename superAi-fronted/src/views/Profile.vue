<template>
  <div class="profile-page">
    <div class="profile-container">
      <!-- 个人资料卡片 -->
      <div class="profile-card">
        <!-- 用户头像区域 -->
        <div class="avatar-section">
          <div class="avatar-wrapper">
            <div class="user-avatar" v-if="!authStore.user?.avatar">
              {{ authStore.userInitials }}
            </div>
            <img 
              v-else
              :src="authStore.user.avatar" 
              :alt="authStore.userName"
              class="user-avatar-img"
            >
            <button class="avatar-edit-btn" @click="openAvatarUpload">
              <i class="fas fa-camera"></i>
            </button>
          </div>
          <h1 class="user-name">{{ authStore.userName }}</h1>
          <p class="user-email">{{ authStore.user?.email }}</p>
        </div>
        
        <!-- 基本信息 -->
        <div class="info-section">
          <h2 class="section-title">基本信息</h2>
          
          <form class="profile-form" @submit.prevent="updateProfile">
            <div class="form-row">
              <div class="form-group">
                <label class="form-label">姓名</label>
                <input
                  type="text"
                  v-model="profileForm.name"
                  class="form-input"
                  placeholder="请输入姓名"
                  required
                >
              </div>
              
              <div class="form-group">
                <label class="form-label">邮箱</label>
                <input
                  type="email"
                  v-model="profileForm.email"
                  class="form-input"
                  placeholder="请输入邮箱"
                  required
                >
              </div>
            </div>
            
            <div class="form-row">
              <div class="form-group">
                <label class="form-label">年龄</label>
                <input
                  type="number"
                  v-model.number="profileForm.age"
                  class="form-input"
                  placeholder="年龄"
                  min="16"
                  max="100"
                >
              </div>
              
              <div class="form-group">
                <label class="form-label">性别</label>
                <select v-model="profileForm.gender" class="form-select">
                  <option value="">请选择</option>
                  <option value="male">男</option>
                  <option value="female">女</option>
                  <option value="other">其他</option>
                </select>
              </div>
            </div>
            
            <div class="form-group">
              <label class="form-label">个人简介</label>
              <textarea
                v-model="profileForm.bio"
                class="form-textarea"
                placeholder="介绍一下自己..."
                rows="3"
              ></textarea>
            </div>
          </form>
        </div>
        
        <!-- 健身信息 -->
        <div class="info-section">
          <h2 class="section-title">健身信息</h2>
          
          <div class="fitness-info">
            <div class="form-row">
              <div class="form-group">
                <label class="form-label">身高 (cm)</label>
                <input
                  type="number"
                  v-model.number="profileForm.height"
                  class="form-input"
                  placeholder="身高"
                  min="100"
                  max="250"
                >
              </div>
              
              <div class="form-group">
                <label class="form-label">体重 (kg)</label>
                <input
                  type="number"
                  v-model.number="profileForm.weight"
                  class="form-input"
                  placeholder="体重"
                  min="30"
                  max="300"
                >
              </div>
            </div>
            
            <div class="form-group">
              <label class="form-label">健身水平</label>
              <div class="radio-group">
                <label 
                  v-for="level in fitnessLevels"
                  :key="level.value"
                  class="radio-item"
                  :class="{ selected: profileForm.fitnessLevel === level.value }"
                >
                  <input
                    type="radio"
                    :value="level.value"
                    v-model="profileForm.fitnessLevel"
                  >
                  <div class="radio-content">
                    <i :class="level.icon"></i>
                    <span>{{ level.label }}</span>
                  </div>
                </label>
              </div>
            </div>
            
            <div class="form-group">
              <label class="form-label">健身目标</label>
              <div class="checkbox-group">
                <label 
                  v-for="goal in fitnessGoals"
                  :key="goal.value"
                  class="checkbox-item"
                  :class="{ selected: profileForm.goals.includes(goal.value) }"
                >
                  <input
                    type="checkbox"
                    :value="goal.value"
                    v-model="profileForm.goals"
                  >
                  <div class="checkbox-content">
                    <i :class="goal.icon"></i>
                    <span>{{ goal.label }}</span>
                  </div>
                </label>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 统计信息 -->
        <div class="info-section">
          <h2 class="section-title">使用统计</h2>
          
          <div class="stats-grid">
            <div class="stat-item">
              <div class="stat-icon">
                <i class="fas fa-comments"></i>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ userStats.totalChats }}</div>
                <div class="stat-label">总对话数</div>
              </div>
            </div>
            
            <div class="stat-item">
              <div class="stat-icon">
                <i class="fas fa-calendar-day"></i>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ userStats.activeDays }}</div>
                <div class="stat-label">活跃天数</div>
              </div>
            </div>
            
            <div class="stat-item">
              <div class="stat-icon">
                <i class="fas fa-star"></i>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ userStats.level }}</div>
                <div class="stat-label">用户等级</div>
              </div>
            </div>
            
            <div class="stat-item">
              <div class="stat-icon">
                <i class="fas fa-trophy"></i>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ userStats.achievements }}</div>
                <div class="stat-label">获得成就</div>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 操作按钮 -->
        <div class="profile-actions">
          <button 
            class="action-btn save-btn" 
            @click="updateProfile"
            :disabled="isUpdating"
          >
            <div class="loading-spinner" v-if="isUpdating"></div>
            <i class="fas fa-save" v-else></i>
            <span>{{ isUpdating ? '保存中...' : '保存更改' }}</span>
          </button>
          
          <button class="action-btn reset-btn" @click="resetForm">
            <i class="fas fa-undo"></i>
            <span>重置</span>
          </button>
          
          <button class="action-btn danger-btn" @click="openDeleteAccount">
            <i class="fas fa-user-times"></i>
            <span>删除账户</span>
          </button>
        </div>
      </div>
    </div>
    
    <!-- 头像上传模态框 -->
    <div class="modal-overlay" v-if="showAvatarModal" @click="closeAvatarModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>更换头像</h3>
          <button class="modal-close" @click="closeAvatarModal">
            <i class="fas fa-times"></i>
          </button>
        </div>
        <div class="modal-body">
          <div class="avatar-upload">
            <input 
              type="file" 
              ref="avatarInput"
              accept="image/*"
              @change="handleAvatarSelect"
              style="display: none"
            >
            <div class="upload-area" @click="$refs.avatarInput.click()">
              <i class="fas fa-cloud-upload-alt"></i>
              <p>点击上传图片或拖拽图片到此处</p>
              <small>支持 JPG、PNG 格式，文件大小不超过 5MB</small>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()

// 响应式数据
const isUpdating = ref(false)
const showAvatarModal = ref(false)

const profileForm = reactive({
  name: '',
  email: '',
  age: null,
  gender: '',
  bio: '',
  height: null,
  weight: null,
  fitnessLevel: 'beginner',
  goals: []
})

const userStats = ref({
  totalChats: 127,
  activeDays: 23,
  level: 5,
  achievements: 8
})

// 健身水平选项
const fitnessLevels = ref([
  { value: 'beginner', label: '初学者', icon: 'fas fa-seedling' },
  { value: 'intermediate', label: '中级', icon: 'fas fa-running' },
  { value: 'advanced', label: '高级', icon: 'fas fa-medal' }
])

// 健身目标选项
const fitnessGoals = ref([
  { value: 'weight_loss', label: '减重', icon: 'fas fa-weight' },
  { value: 'muscle_gain', label: '增肌', icon: 'fas fa-dumbbell' },
  { value: 'endurance', label: '耐力', icon: 'fas fa-heart' },
  { value: 'flexibility', label: '柔韧', icon: 'fas fa-yoga' },
  { value: 'strength', label: '力量', icon: 'fas fa-fist-raised' },
  { value: 'health', label: '健康', icon: 'fas fa-leaf' }
])

// 打开头像上传
const openAvatarUpload = () => {
  showAvatarModal.value = true
}

// 关闭头像上传
const closeAvatarModal = () => {
  showAvatarModal.value = false
}

// 处理头像选择
const handleAvatarSelect = (event) => {
  const file = event.target.files[0]
  if (!file) return
  
  // 验证文件类型
  if (!file.type.startsWith('image/')) {
    alert('请选择图片文件')
    return
  }
  
  // 验证文件大小
  if (file.size > 5 * 1024 * 1024) {
    alert('图片大小不能超过 5MB')
    return
  }
  
  // 这里可以实现图片上传逻辑
  console.log('上传头像:', file)
  closeAvatarModal()
}

// 更新个人资料
const updateProfile = async () => {
  isUpdating.value = true
  
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // 更新本地store
    authStore.updateProfile(profileForm)
    
    alert('个人资料更新成功！')
  } catch (error) {
    console.error('更新失败:', error)
    alert('更新失败，请重试')
  } finally {
    isUpdating.value = false
  }
}

// 重置表单
const resetForm = () => {
  if (confirm('确定要重置所有更改吗？')) {
    loadUserData()
  }
}

// 删除账户
const openDeleteAccount = () => {
  const confirmText = '确定要删除账户吗？此操作无法撤销，所有数据将被permanently删除。'
  if (confirm(confirmText)) {
    // 这里实现删除账户逻辑
    console.log('删除账户')
  }
}

// 加载用户数据
const loadUserData = () => {
  const user = authStore.user
  if (user) {
    profileForm.name = user.name || ''
    profileForm.email = user.email || ''
    profileForm.age = user.profile?.age || null
    profileForm.gender = user.profile?.gender || ''
    profileForm.bio = user.profile?.bio || ''
    profileForm.height = user.profile?.height || null
    profileForm.weight = user.profile?.weight || null
    profileForm.fitnessLevel = user.profile?.fitnessLevel || 'beginner'
    profileForm.goals = user.profile?.goals || []
  }
}

onMounted(() => {
  loadUserData()
})
</script>

<style scoped>
.profile-page {
  padding: var(--spacing-xl) var(--spacing-lg);
  max-width: 800px;
  margin: 0 auto;
  background: var(--bg-primary);
  min-height: 100vh;
}

.profile-container {
  background: var(--bg-secondary);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-md);
  overflow: hidden;
}

.profile-card {
  padding: var(--spacing-2xl);
}

.avatar-section {
  text-align: center;
  margin-bottom: var(--spacing-2xl);
  padding-bottom: var(--spacing-2xl);
  border-bottom: 1px solid var(--border-light);
}

.avatar-wrapper {
  position: relative;
  display: inline-block;
  margin-bottom: var(--spacing-lg);
}

.user-avatar, .user-avatar-img {
  width: 120px;
  height: 120px;
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--font-3xl);
  font-weight: var(--font-bold);
  color: var(--text-white);
  background: var(--gradient-primary);
  box-shadow: var(--shadow-lg);
}

.user-avatar-img {
  object-fit: cover;
}

.avatar-edit-btn {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 40px;
  height: 40px;
  border-radius: var(--radius-full);
  background: var(--primary-color);
  color: var(--text-white);
  border: 3px solid var(--bg-secondary);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--duration-fast) var(--ease-in-out);
}

.avatar-edit-btn:hover {
  background: var(--primary-dark);
  transform: scale(1.1);
}

.user-name {
  font-size: var(--font-2xl);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  margin-bottom: var(--spacing-xs);
}

.user-email {
  font-size: var(--font-base);
  color: var(--text-secondary);
}

.info-section {
  margin-bottom: var(--spacing-2xl);
}

.section-title {
  font-size: var(--font-xl);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
  margin-bottom: var(--spacing-lg);
  padding-bottom: var(--spacing-sm);
  border-bottom: 2px solid var(--border-light);
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--spacing-lg);
  margin-bottom: var(--spacing-lg);
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

.form-input, .form-select, .form-textarea {
  width: 100%;
  padding: var(--spacing-md);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  font-size: var(--font-base);
  background: var(--bg-primary);
  color: var(--text-primary);
  transition: all var(--duration-fast) var(--ease-in-out);
}

.form-input:focus, .form-select:focus, .form-textarea:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgb(59 130 246 / 0.1);
}

.form-textarea {
  resize: vertical;
  min-height: 80px;
}

.radio-group, .checkbox-group {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: var(--spacing-sm);
}

.radio-item, .checkbox-item {
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--duration-fast) var(--ease-in-out);
}

.radio-item input, .checkbox-item input {
  display: none;
}

.radio-item.selected, .checkbox-item.selected {
  border-color: var(--primary-color);
  background: rgba(59, 130, 246, 0.05);
}

.radio-content, .checkbox-content {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-md);
}

.radio-content i, .checkbox-content i {
  color: var(--primary-color);
  width: 20px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: var(--spacing-lg);
}

.stat-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  padding: var(--spacing-lg);
  background: var(--bg-primary);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-light);
}

.stat-icon {
  width: 48px;
  height: 48px;
  background: var(--primary-color);
  color: var(--text-white);
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--font-xl);
}

.stat-value {
  font-size: var(--font-2xl);
  font-weight: var(--font-bold);
  color: var(--text-primary);
}

.stat-label {
  font-size: var(--font-sm);
  color: var(--text-secondary);
}

.profile-actions {
  display: flex;
  gap: var(--spacing-md);
  padding-top: var(--spacing-xl);
  border-top: 1px solid var(--border-light);
  justify-content: center;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  padding: var(--spacing-md) var(--spacing-lg);
  border: none;
  border-radius: var(--radius-lg);
  font-size: var(--font-base);
  font-weight: var(--font-medium);
  cursor: pointer;
  transition: all var(--duration-fast) var(--ease-in-out);
}

.save-btn {
  background: var(--success-color);
  color: var(--text-white);
}

.save-btn:hover:not(:disabled) {
  background: rgba(16, 185, 129, 0.8);
  transform: translateY(-2px);
}

.save-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.reset-btn {
  background: var(--warning-color);
  color: var(--text-white);
}

.reset-btn:hover {
  background: rgba(245, 158, 11, 0.8);
  transform: translateY(-2px);
}

.danger-btn {
  background: var(--danger-color);
  color: var(--text-white);
}

.danger-btn:hover {
  background: rgba(239, 68, 68, 0.8);
  transform: translateY(-2px);
}

/* 模态框样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: var(--bg-secondary);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-xl);
  width: 90%;
  max-width: 500px;
  max-height: 90vh;
  overflow: hidden;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--spacing-lg) var(--spacing-xl);
  border-bottom: 1px solid var(--border-light);
}

.modal-header h3 {
  font-size: var(--font-lg);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
}

.modal-close {
  width: 32px;
  height: 32px;
  border: none;
  border-radius: var(--radius-full);
  background: var(--bg-primary);
  color: var(--text-secondary);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--duration-fast) var(--ease-in-out);
}

.modal-close:hover {
  background: var(--danger-color);
  color: var(--text-white);
}

.modal-body {
  padding: var(--spacing-xl);
}

.upload-area {
  border: 2px dashed var(--border-color);
  border-radius: var(--radius-lg);
  padding: var(--spacing-2xl);
  text-align: center;
  cursor: pointer;
  transition: all var(--duration-fast) var(--ease-in-out);
}

.upload-area:hover {
  border-color: var(--primary-color);
  background: rgba(59, 130, 246, 0.05);
}

.upload-area i {
  font-size: var(--font-3xl);
  color: var(--primary-color);
  margin-bottom: var(--spacing-md);
}

.upload-area p {
  font-size: var(--font-base);
  color: var(--text-primary);
  margin-bottom: var(--spacing-sm);
}

.upload-area small {
  font-size: var(--font-sm);
  color: var(--text-secondary);
}

/* 移动端适配 */
@media (max-width: 767px) {
  .profile-page {
    padding: var(--spacing-md);
  }
  
  .profile-card {
    padding: var(--spacing-lg);
  }
  
  .user-avatar, .user-avatar-img {
    width: 80px;
    height: 80px;
    font-size: var(--font-2xl);
  }
  
  .avatar-edit-btn {
    width: 32px;
    height: 32px;
    font-size: var(--font-sm);
  }
  
  .form-row {
    grid-template-columns: 1fr;
  }
  
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .profile-actions {
    flex-direction: column;
  }
  
  .modal-content {
    width: 95%;
  }
  
  .modal-header, .modal-body {
    padding: var(--spacing-lg);
  }
}
</style>