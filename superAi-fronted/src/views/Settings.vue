<template>
  <div class="settings-page">
    <div class="settings-container">
      <div class="settings-header">
        <h1 class="page-title">系统设置</h1>
        <p class="page-subtitle">个性化配置您的AI助手体验</p>
      </div>
      
      <div class="settings-content">
        <!-- 账户设置 -->
        <div class="settings-section">
          <div class="section-header">
            <i class="fas fa-user-cog"></i>
            <h2 class="section-title">账户设置</h2>
          </div>
          
          <div class="settings-group">
            <div class="setting-item">
              <div class="setting-info">
                <h3 class="setting-title">个人信息</h3>
                <p class="setting-desc">管理您的基本账户信息</p>
              </div>
              <router-link to="/profile" class="setting-action">
                <span>编辑</span>
                <i class="fas fa-chevron-right"></i>
              </router-link>
            </div>
            
            <div class="setting-item">
              <div class="setting-info">
                <h3 class="setting-title">安全设置</h3>
                <p class="setting-desc">密码、两步验证等安全选项</p>
              </div>
              <button class="setting-action" @click="openSecuritySettings">
                <span>管理</span>
                <i class="fas fa-chevron-right"></i>
              </button>
            </div>
          </div>
        </div>
        
        <!-- 应用偏好 -->
        <div class="settings-section">
          <div class="section-header">
            <i class="fas fa-sliders-h"></i>
            <h2 class="section-title">应用偏好</h2>
          </div>
          
          <div class="settings-group">
            <div class="setting-item">
              <div class="setting-info">
                <h3 class="setting-title">默认应用</h3>
                <p class="setting-desc">选择启动时默认打开的应用</p>
              </div>
              <select v-model="settings.defaultApp" class="setting-select">
                <option value="fitness">AI健身助手</option>
                <option value="love">AI爱情大师</option>
                <option value="manus">AI超级助手</option>
              </select>
            </div>
            
            <div class="setting-item">
              <div class="setting-info">
                <h3 class="setting-title">消息历史</h3>
                <p class="setting-desc">保存聊天记录的时长</p>
              </div>
              <select v-model="settings.historyDays" class="setting-select">
                <option value="7">7天</option>
                <option value="30">30天</option>
                <option value="90">90天</option>
                <option value="-1">永久保存</option>
              </select>
            </div>
          </div>
        </div>
        
        <!-- 界面设置 -->
        <div class="settings-section">
          <div class="section-header">
            <i class="fas fa-palette"></i>
            <h2 class="section-title">界面设置</h2>
          </div>
          
          <div class="settings-group">
            <div class="setting-item">
              <div class="setting-info">
                <h3 class="setting-title">深色模式</h3>
                <p class="setting-desc">切换到深色主题界面</p>
              </div>
              <label class="toggle-switch">
                <input type="checkbox" v-model="settings.darkMode" @change="toggleDarkMode">
                <span class="toggle-slider"></span>
              </label>
            </div>
            
            <div class="setting-item">
              <div class="setting-info">
                <h3 class="setting-title">字体大小</h3>
                <p class="setting-desc">调整界面文字大小</p>
              </div>
              <div class="font-size-control">
                <button 
                  v-for="size in fontSizes"
                  :key="size.value"
                  class="font-size-btn"
                  :class="{ active: settings.fontSize === size.value }"
                  @click="settings.fontSize = size.value"
                >
                  {{ size.label }}
                </button>
              </div>
            </div>
            
            <div class="setting-item">
              <div class="setting-info">
                <h3 class="setting-title">动画效果</h3>
                <p class="setting-desc">启用界面动画和过渡效果</p>
              </div>
              <label class="toggle-switch">
                <input type="checkbox" v-model="settings.animations">
                <span class="toggle-slider"></span>
              </label>
            </div>
          </div>
        </div>
        
        <!-- 通知设置 -->
        <div class="settings-section">
          <div class="section-header">
            <i class="fas fa-bell"></i>
            <h2 class="section-title">通知设置</h2>
          </div>
          
          <div class="settings-group">
            <div class="setting-item">
              <div class="setting-info">
                <h3 class="setting-title">桌面通知</h3>
                <p class="setting-desc">接收系统桌面通知</p>
              </div>
              <label class="toggle-switch">
                <input type="checkbox" v-model="settings.desktopNotifications" @change="requestNotificationPermission">
                <span class="toggle-slider"></span>
              </label>
            </div>
            
            <div class="setting-item">
              <div class="setting-info">
                <h3 class="setting-title">声音提醒</h3>
                <p class="setting-desc">新消息到达时播放提示音</p>
              </div>
              <label class="toggle-switch">
                <input type="checkbox" v-model="settings.soundNotifications">
                <span class="toggle-slider"></span>
              </label>
            </div>
          </div>
        </div>
        
        <!-- 隐私设置 -->
        <div class="settings-section">
          <div class="section-header">
            <i class="fas fa-shield-alt"></i>
            <h2 class="section-title">隐私设置</h2>
          </div>
          
          <div class="settings-group">
            <div class="setting-item">
              <div class="setting-info">
                <h3 class="setting-title">数据收集</h3>
                <p class="setting-desc">允许收集匿名使用数据以改进服务</p>
              </div>
              <label class="toggle-switch">
                <input type="checkbox" v-model="settings.dataCollection">
                <span class="toggle-slider"></span>
              </label>
            </div>
            
            <div class="setting-item">
              <div class="setting-info">
                <h3 class="setting-title">自动保存</h3>
                <p class="setting-desc">自动保存聊天记录到本地</p>
              </div>
              <label class="toggle-switch">
                <input type="checkbox" v-model="settings.autoSave">
                <span class="toggle-slider"></span>
              </label>
            </div>
          </div>
        </div>
        
        <!-- 高级设置 -->
        <div class="settings-section">
          <div class="section-header">
            <i class="fas fa-cogs"></i>
            <h2 class="section-title">高级设置</h2>
          </div>
          
          <div class="settings-group">
            <div class="setting-item">
              <div class="setting-info">
                <h3 class="setting-title">开发者模式</h3>
                <p class="setting-desc">显示调试信息和日志</p>
              </div>
              <label class="toggle-switch">
                <input type="checkbox" v-model="settings.developerMode">
                <span class="toggle-slider"></span>
              </label>
            </div>
            
            <div class="setting-item">
              <div class="setting-info">
                <h3 class="setting-title">实验性功能</h3>
                <p class="setting-desc">启用测试阶段的新功能</p>
              </div>
              <label class="toggle-switch">
                <input type="checkbox" v-model="settings.experimentalFeatures">
                <span class="toggle-slider"></span>
              </label>
            </div>
          </div>
        </div>
        
        <!-- 操作按钮 -->
        <div class="settings-actions">
          <button class="action-btn save-btn" @click="saveSettings" :disabled="isSaving">
            <i class="fas fa-save"></i>
            <span>{{ isSaving ? '保存中...' : '保存设置' }}</span>
          </button>
          
          <button class="action-btn reset-btn" @click="resetSettings">
            <i class="fas fa-undo"></i>
            <span>重置默认</span>
          </button>
          
          <button class="action-btn export-btn" @click="exportSettings">
            <i class="fas fa-download"></i>
            <span>导出设置</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'

// 响应式数据
const isSaving = ref(false)

const settings = ref({
  defaultApp: 'fitness',
  historyDays: 30,
  darkMode: false,
  fontSize: 'medium',
  animations: true,
  desktopNotifications: false,
  soundNotifications: true,
  dataCollection: true,
  autoSave: true,
  developerMode: false,
  experimentalFeatures: false
})

const fontSizes = ref([
  { value: 'small', label: '小' },
  { value: 'medium', label: '中' },
  { value: 'large', label: '大' }
])

// 切换深色模式
const toggleDarkMode = () => {
  // 这里实现深色模式切换逻辑
  console.log('切换深色模式:', settings.value.darkMode)
}

// 请求通知权限
const requestNotificationPermission = async () => {
  if (settings.value.desktopNotifications) {
    if ('Notification' in window) {
      const permission = await Notification.requestPermission()
      if (permission !== 'granted') {
        settings.value.desktopNotifications = false
        alert('通知权限被拒绝，无法启用桌面通知')
      }
    } else {
      settings.value.desktopNotifications = false
      alert('您的浏览器不支持桌面通知')
    }
  }
}

// 打开安全设置
const openSecuritySettings = () => {
  // 这里可以打开安全设置模态框或跳转到安全设置页面
  console.log('打开安全设置')
}

// 保存设置
const saveSettings = async () => {
  isSaving.value = true
  
  try {
    // 模拟保存到服务器
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // 保存到本地存储
    localStorage.setItem('app_settings', JSON.stringify(settings.value))
    
    // 显示成功提示
    console.log('设置保存成功')
  } catch (error) {
    console.error('保存设置失败:', error)
    alert('保存设置失败，请重试')
  } finally {
    isSaving.value = false
  }
}

// 重置设置
const resetSettings = () => {
  if (confirm('确定要重置所有设置到默认值吗？')) {
    settings.value = {
      defaultApp: 'fitness',
      historyDays: 30,
      darkMode: false,
      fontSize: 'medium',
      animations: true,
      desktopNotifications: false,
      soundNotifications: true,
      dataCollection: true,
      autoSave: true,
      developerMode: false,
      experimentalFeatures: false
    }
    
    saveSettings()
  }
}

// 导出设置
const exportSettings = () => {
  const dataStr = JSON.stringify(settings.value, null, 2)
  const dataBlob = new Blob([dataStr], { type: 'application/json' })
  const url = URL.createObjectURL(dataBlob)
  const link = document.createElement('a')
  link.href = url
  link.download = `ai-assistant-settings-${new Date().toISOString().split('T')[0]}.json`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
}

// 监听设置变化，自动保存
watch(settings, () => {
  // 防抖保存
  clearTimeout(window.settingsTimeout)
  window.settingsTimeout = setTimeout(() => {
    localStorage.setItem('app_settings', JSON.stringify(settings.value))
  }, 1000)
}, { deep: true })

onMounted(() => {
  // 从本地存储加载设置
  const savedSettings = localStorage.getItem('app_settings')
  if (savedSettings) {
    try {
      const parsed = JSON.parse(savedSettings)
      settings.value = { ...settings.value, ...parsed }
    } catch (error) {
      console.error('加载设置失败:', error)
    }
  }
})
</script>

<style scoped>
.settings-page {
  padding: var(--spacing-xl) var(--spacing-lg);
  max-width: 800px;
  margin: 0 auto;
  background: var(--bg-primary);
  min-height: 100vh;
}

.settings-container {
  background: var(--bg-secondary);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-md);
  overflow: hidden;
}

.settings-header {
  padding: var(--spacing-2xl);
  background: var(--gradient-primary);
  color: var(--text-white);
  text-align: center;
}

.page-title {
  font-size: var(--font-3xl);
  font-weight: var(--font-bold);
  margin-bottom: var(--spacing-sm);
}

.page-subtitle {
  font-size: var(--font-lg);
  opacity: 0.9;
}

.settings-content {
  padding: var(--spacing-xl);
}

.settings-section {
  margin-bottom: var(--spacing-2xl);
}

.settings-section:last-child {
  margin-bottom: 0;
}

.section-header {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-lg);
  padding-bottom: var(--spacing-md);
  border-bottom: 2px solid var(--border-light);
}

.section-header i {
  font-size: var(--font-xl);
  color: var(--primary-color);
}

.section-title {
  font-size: var(--font-xl);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
}

.settings-group {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

.setting-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--spacing-lg);
  background: var(--bg-primary);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-light);
  transition: all var(--duration-fast) var(--ease-in-out);
}

.setting-item:hover {
  border-color: var(--primary-color);
  box-shadow: var(--shadow-sm);
}

.setting-info {
  flex: 1;
}

.setting-title {
  font-size: var(--font-base);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
  margin-bottom: var(--spacing-xs);
}

.setting-desc {
  font-size: var(--font-sm);
  color: var(--text-secondary);
  line-height: 1.4;
}

.setting-action {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  padding: var(--spacing-sm) var(--spacing-md);
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  color: var(--text-primary);
  text-decoration: none;
  font-size: var(--font-sm);
  cursor: pointer;
  transition: all var(--duration-fast) var(--ease-in-out);
}

.setting-action:hover {
  background: var(--primary-color);
  color: var(--text-white);
  border-color: var(--primary-color);
}

.setting-select {
  padding: var(--spacing-sm) var(--spacing-md);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  background: var(--bg-secondary);
  color: var(--text-primary);
  font-size: var(--font-sm);
  cursor: pointer;
  min-width: 120px;
}

.setting-select:focus {
  outline: none;
  border-color: var(--primary-color);
}

.toggle-switch {
  position: relative;
  display: inline-block;
  width: 50px;
  height: 24px;
}

.toggle-switch input {
  opacity: 0;
  width: 0;
  height: 0;
}

.toggle-slider {
  position: absolute;
  cursor: pointer;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: var(--border-color);
  transition: var(--duration-fast);
  border-radius: var(--radius-full);
}

.toggle-slider:before {
  position: absolute;
  content: "";
  height: 18px;
  width: 18px;
  left: 3px;
  bottom: 3px;
  background-color: var(--text-white);
  transition: var(--duration-fast);
  border-radius: var(--radius-full);
}

.toggle-switch input:checked + .toggle-slider {
  background-color: var(--primary-color);
}

.toggle-switch input:checked + .toggle-slider:before {
  transform: translateX(26px);
}

.font-size-control {
  display: flex;
  gap: var(--spacing-xs);
}

.font-size-btn {
  padding: var(--spacing-xs) var(--spacing-sm);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  background: var(--bg-secondary);
  color: var(--text-primary);
  font-size: var(--font-sm);
  cursor: pointer;
  transition: all var(--duration-fast) var(--ease-in-out);
}

.font-size-btn:hover {
  border-color: var(--primary-color);
}

.font-size-btn.active {
  background: var(--primary-color);
  color: var(--text-white);
  border-color: var(--primary-color);
}

.settings-actions {
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

.export-btn {
  background: var(--info-color);
  color: var(--text-white);
}

.export-btn:hover {
  background: rgba(6, 182, 212, 0.8);
  transform: translateY(-2px);
}

/* 移动端适配 */
@media (max-width: 767px) {
  .settings-page {
    padding: var(--spacing-md);
  }
  
  .settings-header {
    padding: var(--spacing-xl);
  }
  
  .page-title {
    font-size: var(--font-2xl);
  }
  
  .settings-content {
    padding: var(--spacing-md);
  }
  
  .setting-item {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--spacing-md);
  }
  
  .settings-actions {
    flex-direction: column;
  }
  
  .action-btn {
    justify-content: center;
  }
}
</style>