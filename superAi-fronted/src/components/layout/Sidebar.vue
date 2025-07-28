<template>
  <div class="sidebar" :class="{ active: chatStore.isMobileMenuOpen }">
    <!-- Logo区域 -->
    <div class="sidebar-header">
      <div class="logo">
        <div class="logo-icon-wrapper">
          <i class="fas fa-dumbbell logo-icon"></i>
        </div>
        <span class="logo-text">AI 运动助手</span>
      </div>
      <div class="status-indicator">
        <div class="status-dot status-online"></div>
        <span class="status-text">在线服务</span>
      </div>
    </div>

    <!-- 用户信息 -->
    <div class="user-info" v-if="authStore.isLoggedIn">
      <div class="user-avatar">{{ authStore.userInitials }}</div>
      <div class="user-details">
        <h4 class="user-name">{{ authStore.userName }}</h4>
        <p class="user-level">{{ authStore.userFitnessLevel }}</p>
      </div>
    </div>

    <!-- 新对话按钮 -->
    <button class="new-chat-btn" @click="startNewChat">
      <i class="fas fa-plus"></i>
      <span>新建对话</span>
    </button>

    <!-- 功能菜单 -->
    <div class="sidebar-menu">
      <div class="menu-section">
        <div class="menu-title">应用功能</div>
        <div 
          class="menu-item" 
          v-for="app in apps" 
          :key="app.id"
          :class="{ active: chatStore.currentApp === app.id }"
          @click="switchApp(app.id)"
        >
          <div class="menu-icon-wrapper" :style="{ background: app.gradient }">
            <i :class="app.icon" class="menu-icon"></i>
          </div>
          <span>{{ app.name }}</span>
        </div>
      </div>

      <!-- 历史对话 -->
      <div class="menu-section" v-if="chatStore.recentChats.length > 0">
        <div class="menu-title">最近对话</div>
        <div class="chat-history">
          <div 
            class="chat-item"
            v-for="chat in chatStore.recentChats.slice(0, 8)"
            :key="chat.id"
            :class="{ active: chatStore.currentChatId === chat.id }"
            @click="loadChat(chat.id)"
          >
            <div class="chat-title">{{ chat.title }}</div>
            <div class="chat-meta">
              <span class="chat-time">{{ formatTime(chat.updatedAt) }}</span>
              <button class="delete-chat-btn" @click.stop="deleteChat(chat.id)">
                <i class="fas fa-times"></i>
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- 设置菜单 -->
      <div class="menu-section">
        <div class="menu-title">设置</div>
        <div class="menu-item" @click="openSettings">
          <i class="fas fa-cog"></i>
          <span>偏好设置</span>
        </div>
        <div class="menu-item" @click="exportData">
          <i class="fas fa-download"></i>
          <span>导出数据</span>
        </div>
        <div class="menu-item" @click="logout" v-if="authStore.isLoggedIn">
          <i class="fas fa-sign-out-alt"></i>
          <span>退出登录</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useChatStore } from '@/stores/chat'
import { useProcessingStore } from '@/stores/processing'

const router = useRouter()
const authStore = useAuthStore()
const chatStore = useChatStore()
const processingStore = useProcessingStore()

// 应用列表
const apps = ref([
  {
    id: 'fitness',
    name: 'AI健身助手',
    icon: 'fas fa-dumbbell',
    svgIcon: '/icons/fitness-logo.svg',
    gradient: 'linear-gradient(135deg, #ff7e5f 0%, #feb47b 100%)'
  },
  {
    id: 'manus',
    name: 'AI超级助手',
    icon: 'fas fa-robot',
    svgIcon: '/icons/manus-logo.svg',
    gradient: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
  }
])

// 开始新对话
const startNewChat = () => {
  chatStore.startNewChat()
  processingStore.reset()
  chatStore.closeMobileMenu()
  
  // 根据当前应用导航到对应页面
  const routeMap = {
    fitness: '/fitness',
    manus: '/manus'
  }
  
  const targetRoute = routeMap[chatStore.currentApp] || '/fitness'
  if (router.currentRoute.value.path !== targetRoute) {
    router.push(targetRoute)
  }
}

// 切换应用
const switchApp = (appId) => {
  if (appId !== chatStore.currentApp) {
    chatStore.switchApp(appId)
    processingStore.reset()
    
    // 导航到对应的应用页面
    const routeMap = {
      fitness: '/fitness',
      manus: '/manus'
    }
    
    const targetRoute = routeMap[appId] || '/fitness'
    router.push(targetRoute)
  }
  chatStore.closeMobileMenu()
}

// 加载聊天记录
const loadChat = async (chatId) => {
  // 先找到对应的聊天记录
  const chat = chatStore.recentChats.find(c => c.id === chatId)
  if (chat) {
    // 先切换到对应的应用
    if (chat.app !== chatStore.currentApp) {
      chatStore.switchApp(chat.app)
      
      // 导航到对应的应用页面
      const routeMap = {
        fitness: '/fitness',
        manus: '/manus'
      }
      
      const targetRoute = routeMap[chat.app] || '/fitness'
      router.push(targetRoute)
    }
  }
  
  await chatStore.loadChat(chatId)
  processingStore.reset()
  chatStore.closeMobileMenu()
}

// 删除聊天记录
const deleteChat = async (chatId) => {
  if (confirm('确定要删除这个对话吗？')) {
    await chatStore.deleteChat(chatId)
  }
}

// 打开设置
const openSettings = () => {
  router.push('/settings')
  chatStore.closeMobileMenu()
}

// 导出数据
const exportData = () => {
  processingStore.exportLogs()
  chatStore.closeMobileMenu()
}

// 退出登录
const logout = async () => {
  if (confirm('确定要退出登录吗？')) {
    await authStore.logout()
    router.push('/login')
  }
}

// 格式化时间
const formatTime = (date) => {
  if (!date) return ''
  const now = new Date()
  const time = new Date(date)
  const diff = now - time
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`
  
  return time.toLocaleDateString()
}

// 组件挂载时加载最近聊天
onMounted(() => {
  chatStore.loadRecentChats()
})
</script>

<style scoped>
.sidebar {
  width: var(--sidebar-width);
  height: 100vh;
  background: var(--bg-sidebar);
  color: var(--text-white);
  display: flex;
  flex-direction: column;
  position: fixed;
  left: 0;
  top: 0;
  z-index: 1000;
  transition: transform var(--duration-normal) var(--ease-in-out);
  overflow-y: auto;
}

.sidebar-header {
  padding: var(--spacing-lg);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.logo {
  display: flex;
  align-items: center;
  margin-bottom: var(--spacing-md);
}

.logo-icon-wrapper {
  width: 40px;
  height: 40px;
  margin-right: var(--spacing-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.1);
  border-radius: var(--radius-lg);
  padding: var(--spacing-xs);
}

.logo-icon {
  font-size: 24px;
  color: var(--text-white);
}

.logo-text {
  font-size: var(--font-lg);
  font-weight: var(--font-bold);
}

.status-indicator {
  display: flex;
  align-items: center;
  font-size: var(--font-sm);
  color: var(--text-light);
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: var(--radius-full);
  margin-right: var(--spacing-xs);
}

.user-info {
  display: flex;
  align-items: center;
  padding: var(--spacing-lg);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.user-avatar {
  width: 40px;
  height: 40px;
  background: var(--primary-color);
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: var(--font-bold);
  margin-right: var(--spacing-md);
}

.user-details {
  flex: 1;
}

.user-name {
  font-size: var(--font-base);
  font-weight: var(--font-medium);
  margin-bottom: 2px;
}

.user-level {
  font-size: var(--font-sm);
  color: var(--text-light);
}

.new-chat-btn {
  margin: var(--spacing-lg);
  padding: var(--spacing-md);
  background: var(--primary-color);
  color: var(--text-white);
  border: none;
  border-radius: var(--radius-md);
  font-weight: var(--font-medium);
  cursor: pointer;
  transition: all var(--duration-fast) var(--ease-in-out);
  display: flex;
  align-items: center;
  justify-content: center;
}

.new-chat-btn:hover {
  background: var(--primary-dark);
  transform: translateY(-1px);
}

.new-chat-btn i {
  margin-right: var(--spacing-xs);
}

.sidebar-menu {
  flex: 1;
  padding: 0 var(--spacing-lg);
}

.menu-section {
  margin-bottom: var(--spacing-lg);
}

.menu-title {
  font-size: var(--font-sm);
  font-weight: var(--font-medium);
  color: var(--text-light);
  margin-bottom: var(--spacing-sm);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: var(--spacing-sm) var(--spacing-md);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--duration-fast) var(--ease-in-out);
  margin-bottom: 4px;
}

.menu-icon-wrapper {
  width: 32px;
  height: 32px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: var(--spacing-sm);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.menu-icon {
  font-size: 20px;
  color: var(--text-white);
}

.menu-item:hover {
  background: rgba(255, 255, 255, 0.1);
}

.menu-item.active {
  background: var(--primary-color);
  color: var(--text-white);
}

.menu-item i {
  width: 20px;
  text-align: center;
  margin-right: var(--spacing-sm);
}

.chat-history {
  max-height: 300px;
  overflow-y: auto;
}

.chat-item {
  padding: var(--spacing-sm) var(--spacing-md);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--duration-fast) var(--ease-in-out);
  margin-bottom: 4px;
}

.chat-item:hover {
  background: rgba(255, 255, 255, 0.05);
}

.chat-item.active {
  background: rgba(59, 130, 246, 0.2);
}

.chat-title {
  font-size: var(--font-sm);
  font-weight: var(--font-medium);
  margin-bottom: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.chat-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.chat-time {
  font-size: var(--font-xs);
  color: var(--text-light);
}

.delete-chat-btn {
  opacity: 0;
  background: none;
  border: none;
  color: var(--danger-color);
  cursor: pointer;
  padding: 2px;
  border-radius: var(--radius-sm);
  transition: all var(--duration-fast) var(--ease-in-out);
}

.chat-item:hover .delete-chat-btn {
  opacity: 1;
}

.delete-chat-btn:hover {
  background: var(--danger-color);
  color: var(--text-white);
}

/* 移动端样式 */
@media (max-width: 767px) {
  .sidebar {
    transform: translateX(-100%);
    width: min(280px, 80vw); /* 响应式宽度 */
  }
  
  .sidebar.active {
    transform: translateX(0);
    box-shadow: var(--shadow-xl);
  }
  
  .sidebar-header {
    padding: var(--spacing-lg) var(--spacing-md);
  }
  
  .app-title {
    font-size: var(--font-lg);
  }
  
  .user-section {
    padding: var(--spacing-sm) var(--spacing-md);
  }
  
  .user-avatar {
    width: 36px;
    height: 36px;
    font-size: var(--font-base);
  }
  
  .nav-section {
    padding: 0 var(--spacing-md);
  }
  
  .nav-item {
    padding: var(--spacing-md);
    min-height: 48px; /* 增大触控面积 */
  }
  
  .nav-icon {
    font-size: var(--font-lg);
  }
  
  .chat-history {
    padding: 0 var(--spacing-md);
  }
  
  .chat-item {
    padding: var(--spacing-sm) var(--spacing-md);
    min-height: 44px;
  }
  
  .sidebar-footer {
    padding: var(--spacing-md);
  }
  
  .footer-btn {
    padding: var(--spacing-md);
    min-height: 48px;
  }
}

/* 平板端样式 */
@media (min-width: 768px) and (max-width: 1023px) {
  .sidebar {
    width: var(--sidebar-width-tablet);
  }
  
  .nav-item, .chat-item {
    min-height: 40px;
  }
}

/* 触控设备优化 */
@media (pointer: coarse) {
  .nav-item, .chat-item, .footer-btn {
    min-height: 44px;
  }
  
  .user-avatar {
    min-width: 44px;
    min-height: 44px;
  }
}
</style>