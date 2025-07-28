<template>
  <div class="app-container">
    <!-- 侧边栏 -->
    <Sidebar />
    
    <!-- 移动端遮罩 -->
    <div 
      class="overlay" 
      :class="{ active: chatStore.isMobileMenuOpen }"
      @click="chatStore.closeMobileMenu"
    ></div>
    
    <!-- 主内容区 -->
    <div class="main-layout">
      <div class="content-area">
        <!-- 顶部导航 -->
        <TopNav />
        
        <!-- 页面内容 -->
        <div class="page-content">
          <slot />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted } from 'vue'
import Sidebar from './Sidebar.vue'
import TopNav from './TopNav.vue'
import { useChatStore } from '@/stores/chat'

const chatStore = useChatStore()

// 处理键盘快捷键
const handleKeyDown = (event) => {
  // Ctrl/Cmd + K 打开新对话
  if ((event.ctrlKey || event.metaKey) && event.key === 'k') {
    event.preventDefault()
    chatStore.startNewChat()
  }
  
  // ESC 关闭移动端菜单
  if (event.key === 'Escape') {
    chatStore.closeMobileMenu()
  }
}

// 处理窗口大小变化
const handleResize = () => {
  // 桌面端自动关闭移动端菜单
  if (window.innerWidth >= 768 && chatStore.isMobileMenuOpen) {
    chatStore.closeMobileMenu()
  }
}

onMounted(() => {
  document.addEventListener('keydown', handleKeyDown)
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  document.removeEventListener('keydown', handleKeyDown)
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.main-layout {
  flex: 1;
  margin-left: var(--sidebar-width);
  transition: margin-left var(--duration-normal) var(--ease-in-out);
}

.page-content {
  flex: 1;
  overflow: auto;
  background: var(--bg-primary);
}

/* 移动端样式 */
@media (max-width: 767px) {
  .main-layout {
    margin-left: 0;
  }
}

/* 平板端样式 */
@media (min-width: 768px) and (max-width: 1023px) {
  .main-layout {
    margin-left: var(--sidebar-width-tablet);
  }
}
</style>