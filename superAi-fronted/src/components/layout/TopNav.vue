<template>
  <div class="top-nav">
    <div class="nav-left">
      <button 
        class="mobile-menu-toggle mobile-flex desktop-hidden" 
        @click="toggleMobileMenu"
      >
        <i class="fas fa-bars"></i>
      </button>
      <div class="nav-title">{{ currentPageTitle }}</div>
    </div>
    
    <div class="nav-actions">
      <button 
        class="nav-btn" 
        @click="clearChat" 
        title="清空对话"
        :disabled="!chatStore.hasMessages"
      >
        <i class="fas fa-trash"></i>
        <span class="btn-text mobile-hidden">清空对话</span>
      </button>
      
      <button 
        class="nav-btn" 
        @click="shareChat" 
        title="分享对话"
        :disabled="!chatStore.hasMessages"
      >
        <i class="fas fa-share"></i>
        <span class="btn-text mobile-hidden">分享</span>
      </button>
      
      <div class="nav-separator mobile-hidden"></div>
      
      <div class="user-menu" @click="toggleUserMenu" v-if="authStore.isLoggedIn">
        <div class="user-avatar-small">{{ authStore.userInitials }}</div>
        <i class="fas fa-chevron-down" :class="{ rotated: showUserMenu }"></i>
        
        <!-- 用户菜单下拉 -->
        <div class="user-menu-dropdown" v-show="showUserMenu">
          <div class="menu-item" @click="goToProfile">
            <i class="fas fa-user"></i>
            <span>个人资料</span>
          </div>
          <div class="menu-item" @click="goToSettings">
            <i class="fas fa-cog"></i>
            <span>设置</span>
          </div>
          <div class="menu-divider"></div>
          <div class="menu-item logout" @click="logout">
            <i class="fas fa-sign-out-alt"></i>
            <span>退出登录</span>
          </div>
        </div>
      </div>
      
      <!-- 未登录状态 -->
      <div class="auth-buttons" v-else>
        <router-link to="/login" class="nav-btn btn-primary">
          <i class="fas fa-sign-in-alt"></i>
          <span>登录</span>
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useChatStore } from '@/stores/chat'
import { useProcessingStore } from '@/stores/processing'

const router = useRouter()
const authStore = useAuthStore()
const chatStore = useChatStore()
const processingStore = useProcessingStore()

const showUserMenu = ref(false)

// 当前页面标题
const currentPageTitle = computed(() => {
  const route = router.currentRoute.value
  if (route.path === '/') return chatStore.currentAppName
  if (route.path === '/login') return '用户登录'
  if (route.path === '/register') return '用户注册'
  if (route.path === '/settings') return '系统设置'
  if (route.path === '/profile') return '个人资料'
  return chatStore.currentAppName
})

// 切换移动端菜单
const toggleMobileMenu = () => {
  chatStore.toggleMobileMenu()
}

// 清空对话
const clearChat = () => {
  if (confirm('确定要清空当前对话吗？')) {
    chatStore.clearCurrentChat()
    processingStore.reset()
  }
}

// 分享对话
const shareChat = async () => {
  if (!chatStore.hasMessages) return
  
  try {
    const shareData = {
      title: '我的AI对话',
      text: `来看看我和${chatStore.currentAppName}的对话吧！`,
      url: window.location.href
    }
    
    if (navigator.share) {
      await navigator.share(shareData)
    } else {
      // 降级方案：复制到剪贴板
      await navigator.clipboard.writeText(shareData.url)
      alert('链接已复制到剪贴板！')
    }
  } catch (error) {
    console.error('分享失败:', error)
  }
}

// 切换用户菜单
const toggleUserMenu = () => {
  showUserMenu.value = !showUserMenu.value
}

// 关闭用户菜单
const closeUserMenu = () => {
  showUserMenu.value = false
}

// 去个人资料页面
const goToProfile = () => {
  router.push('/profile')
  closeUserMenu()
}

// 去设置页面
const goToSettings = () => {
  router.push('/settings')
  closeUserMenu()
}

// 退出登录
const logout = async () => {
  if (confirm('确定要退出登录吗？')) {
    await authStore.logout()
    router.push('/login')
    closeUserMenu()
  }
}

// 点击其他地方关闭用户菜单
const handleClickOutside = (event) => {
  const userMenu = document.querySelector('.user-menu')
  if (userMenu && !userMenu.contains(event.target)) {
    closeUserMenu()
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<style scoped>
.top-nav {
  height: var(--topnav-height);
  background: var(--bg-secondary);
  border-bottom: 1px solid var(--border-color);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 var(--spacing-lg);
  position: sticky;
  top: 0;
  z-index: 100;
}

.nav-left {
  display: flex;
  align-items: center;
}

.mobile-menu-toggle {
  background: none;
  border: none;
  font-size: var(--font-lg);
  color: var(--text-primary);
  cursor: pointer;
  padding: var(--spacing-sm);
  margin-right: var(--spacing-md);
  border-radius: var(--radius-md);
  transition: all var(--duration-fast) var(--ease-in-out);
}

.mobile-menu-toggle:hover {
  background: var(--bg-primary);
}

.nav-title {
  font-size: var(--font-lg);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
}

.nav-actions {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.nav-btn {
  display: flex;
  align-items: center;
  padding: var(--spacing-sm) var(--spacing-md);
  background: none;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  color: var(--text-secondary);
  font-size: var(--font-sm);
  cursor: pointer;
  transition: all var(--duration-fast) var(--ease-in-out);
  text-decoration: none;
}

.nav-btn:hover:not(:disabled) {
  background: var(--bg-primary);
  color: var(--text-primary);
  transform: translateY(-1px);
}

.nav-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.nav-btn.btn-primary {
  background: var(--primary-color);
  color: var(--text-white);
  border-color: var(--primary-color);
}

.nav-btn.btn-primary:hover {
  background: var(--primary-dark);
  border-color: var(--primary-dark);
}

.nav-btn i {
  margin-right: var(--spacing-xs);
}

.nav-separator {
  width: 1px;
  height: 24px;
  background: var(--border-color);
}

.user-menu {
  position: relative;
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: var(--spacing-xs) var(--spacing-sm);
  border-radius: var(--radius-md);
  transition: all var(--duration-fast) var(--ease-in-out);
}

.user-menu:hover {
  background: var(--bg-primary);
}

.user-avatar-small {
  width: 32px;
  height: 32px;
  background: var(--primary-color);
  color: var(--text-white);
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--font-sm);
  font-weight: var(--font-bold);
  margin-right: var(--spacing-xs);
}

.user-menu i {
  font-size: var(--font-xs);
  color: var(--text-light);
  transition: transform var(--duration-fast) var(--ease-in-out);
}

.user-menu i.rotated {
  transform: rotate(180deg);
}

.user-menu-dropdown {
  position: absolute;
  top: 100%;
  right: 0;
  min-width: 180px;
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg);
  padding: var(--spacing-sm) 0;
  z-index: 1000;
  margin-top: var(--spacing-xs);
}

.user-menu-dropdown .menu-item {
  display: flex;
  align-items: center;
  padding: var(--spacing-sm) var(--spacing-md);
  color: var(--text-primary);
  font-size: var(--font-sm);
  cursor: pointer;
  transition: all var(--duration-fast) var(--ease-in-out);
}

.user-menu-dropdown .menu-item:hover {
  background: var(--bg-primary);
}

.user-menu-dropdown .menu-item.logout {
  color: var(--danger-color);
}

.user-menu-dropdown .menu-item.logout:hover {
  background: var(--danger-color);
  color: var(--text-white);
}

.user-menu-dropdown .menu-item i {
  width: 16px;
  margin-right: var(--spacing-sm);
}

.menu-divider {
  height: 1px;
  background: var(--border-color);
  margin: var(--spacing-xs) 0;
}

.auth-buttons {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

/* 移动端适配 */
@media (max-width: 767px) {
  .top-nav {
    padding: 0 var(--spacing-md);
    min-height: 56px; /* 增加高度以适配移动端 */
  }
  
  .nav-title {
    font-size: var(--font-base);
    flex: 1;
    text-align: center;
    margin-left: -44px; /* 补偿菜单按钮宽度 */
  }
  
  .nav-btn .btn-text {
    display: none;
  }
  
  .nav-btn {
    padding: var(--spacing-md);
    min-width: 44px; /* iOS 推荐触控面积 */
    min-height: 44px;
    justify-content: center;
  }
  
  .mobile-menu-toggle {
    min-width: 44px;
    min-height: 44px;
    padding: var(--spacing-md);
  }
  
  .user-menu {
    min-width: 44px;
    min-height: 44px;
    padding: var(--spacing-sm);
  }
  
  .user-avatar {
    width: 32px;
    height: 32px;
    font-size: var(--font-sm);
  }
  
  .user-menu-dropdown {
    right: -10px;
    min-width: 200px;
    max-width: calc(100vw - 32px);
  }
  
  .user-menu-dropdown .menu-item {
    padding: var(--spacing-md);
    min-height: 44px;
    font-size: var(--font-base);
  }
  
  .auth-buttons .nav-btn {
    font-size: var(--font-sm);
  }
}

/* 平板端适配 */
@media (min-width: 768px) and (max-width: 1023px) {
  .top-nav {
    padding: 0 var(--spacing-lg);
  }
  
  .nav-btn {
    min-width: 40px;
    min-height: 40px;
  }
}

@media (min-width: 768px) {
  .mobile-menu-toggle {
    display: none;
  }
}

/* 触控设备优化 */
@media (pointer: coarse) {
  .nav-btn, .mobile-menu-toggle, .user-menu {
    min-width: 44px;
    min-height: 44px;
  }
  
  .user-menu-dropdown .menu-item {
    min-height: 44px;
  }
}
</style>