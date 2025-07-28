<template>
  <div class="not-found-page">
    <div class="not-found-container">
      <!-- 404图标 -->
      <div class="error-icon">
        <div class="icon-wrapper">
          <i class="fas fa-robot"></i>
          <div class="error-code">404</div>
        </div>
      </div>
      
      <!-- 错误信息 -->
      <div class="error-info">
        <h1 class="error-title">页面走丢了</h1>
        <p class="error-message">
          抱歉，您访问的页面不存在或已被移动。<br>
          让我们帮您找到正确的路径！
        </p>
      </div>
      
      <!-- 建议操作 -->
      <div class="suggestions">
        <h3 class="suggestions-title">您可以尝试：</h3>
        <div class="suggestions-list">
          <div class="suggestion-item" @click="goHome">
            <div class="suggestion-icon">
              <i class="fas fa-home"></i>
            </div>
            <div class="suggestion-content">
              <h4>返回首页</h4>
              <p>回到AI运动助手主页</p>
            </div>
          </div>
          
          <div class="suggestion-item" @click="goBack">
            <div class="suggestion-icon">
              <i class="fas fa-arrow-left"></i>
            </div>
            <div class="suggestion-content">
              <h4>返回上页</h4>
              <p>回到您之前访问的页面</p>
            </div>
          </div>
          
          <div class="suggestion-item" @click="refreshPage">
            <div class="suggestion-icon">
              <i class="fas fa-sync-alt"></i>
            </div>
            <div class="suggestion-content">
              <h4>刷新页面</h4>
              <p>重新加载当前页面</p>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 快速导航 -->
      <div class="quick-nav">
        <h3 class="quick-nav-title">快速导航</h3>
        <div class="nav-grid">
          <router-link to="/" class="nav-item">
            <i class="fas fa-dumbbell"></i>
            <span>AI健身助手</span>
          </router-link>
          
          
          <router-link to="/manus" class="nav-item">
            <i class="fas fa-robot"></i>
            <span>AI超级助手</span>
          </router-link>
          
          <router-link to="/settings" class="nav-item">
            <i class="fas fa-cog"></i>
            <span>系统设置</span>
          </router-link>
        </div>
      </div>
      
      <!-- 联系支持 -->
      <div class="support-section">
        <p class="support-text">
          如果问题持续存在，请联系我们的技术支持团队
        </p>
        <div class="support-actions">
          <button class="support-btn" @click="reportIssue">
            <i class="fas fa-bug"></i>
            <span>反馈问题</span>
          </button>
          
          <button class="support-btn" @click="contactSupport">
            <i class="fas fa-headset"></i>
            <span>联系客服</span>
          </button>
        </div>
      </div>
    </div>
    
    <!-- 背景装饰 -->
    <div class="bg-decoration">
      <div class="floating-shape shape-1"></div>
      <div class="floating-shape shape-2"></div>
      <div class="floating-shape shape-3"></div>
      <div class="floating-shape shape-4"></div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

// 返回首页
const goHome = () => {
  router.push('/')
}

// 返回上一页
const goBack = () => {
  if (window.history.length > 1) {
    router.go(-1)
  } else {
    router.push('/')
  }
}

// 刷新页面
const refreshPage = () => {
  window.location.reload()
}

// 反馈问题
const reportIssue = () => {
  // 这里可以打开问题反馈表单或跳转到反馈页面
  const issueData = {
    url: window.location.href,
    userAgent: navigator.userAgent,
    timestamp: new Date().toISOString(),
    error: '404 - Page Not Found'
  }
  
  console.log('反馈问题:', issueData)
  alert('问题反馈已记录，我们会尽快处理！')
}

// 联系客服
const contactSupport = () => {
  // 这里可以打开客服聊天窗口或显示联系方式
  alert('客服邮箱：support@ai-assistant.com\n工作时间：周一至周五 9:00-18:00')
}
</script>

<style scoped>
.not-found-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-lg);
  position: relative;
  overflow: hidden;
}

.not-found-container {
  background: var(--bg-secondary);
  border-radius: var(--radius-2xl);
  padding: var(--spacing-3xl);
  box-shadow: var(--shadow-xl);
  max-width: 600px;
  width: 100%;
  text-align: center;
  position: relative;
  z-index: 2;
}

.error-icon {
  margin-bottom: var(--spacing-2xl);
}

.icon-wrapper {
  position: relative;
  display: inline-block;
}

.icon-wrapper i {
  font-size: 120px;
  color: var(--primary-color);
  opacity: 0.8;
}

.error-code {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: var(--font-4xl);
  font-weight: var(--font-extrabold);
  color: var(--text-white);
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.3);
}

.error-info {
  margin-bottom: var(--spacing-2xl);
}

.error-title {
  font-size: var(--font-3xl);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  margin-bottom: var(--spacing-md);
}

.error-message {
  font-size: var(--font-lg);
  color: var(--text-secondary);
  line-height: 1.6;
}

.suggestions {
  margin-bottom: var(--spacing-2xl);
}

.suggestions-title {
  font-size: var(--font-xl);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
  margin-bottom: var(--spacing-lg);
}

.suggestions-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.suggestion-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  padding: var(--spacing-lg);
  background: var(--bg-primary);
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: all var(--duration-fast) var(--ease-in-out);
  border: 1px solid var(--border-light);
}

.suggestion-item:hover {
  background: var(--primary-color);
  color: var(--text-white);
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.suggestion-icon {
  width: 48px;
  height: 48px;
  background: var(--primary-color);
  color: var(--text-white);
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--font-xl);
  flex-shrink: 0;
  transition: all var(--duration-fast) var(--ease-in-out);
}

.suggestion-item:hover .suggestion-icon {
  background: var(--text-white);
  color: var(--primary-color);
}

.suggestion-content {
  text-align: left;
  flex: 1;
}

.suggestion-content h4 {
  font-size: var(--font-base);
  font-weight: var(--font-semibold);
  margin-bottom: var(--spacing-xs);
}

.suggestion-content p {
  font-size: var(--font-sm);
  color: var(--text-secondary);
}

.suggestion-item:hover .suggestion-content p {
  color: rgba(255, 255, 255, 0.8);
}

.quick-nav {
  margin-bottom: var(--spacing-2xl);
}

.quick-nav-title {
  font-size: var(--font-xl);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
  margin-bottom: var(--spacing-lg);
}

.nav-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: var(--spacing-md);
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-lg);
  background: var(--bg-primary);
  border-radius: var(--radius-lg);
  color: var(--text-primary);
  text-decoration: none;
  transition: all var(--duration-fast) var(--ease-in-out);
  border: 1px solid var(--border-light);
}

.nav-item:hover {
  background: var(--primary-color);
  color: var(--text-white);
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.nav-item i {
  font-size: var(--font-2xl);
  margin-bottom: var(--spacing-xs);
}

.nav-item span {
  font-size: var(--font-sm);
  font-weight: var(--font-medium);
  text-align: center;
}

.support-section {
  padding-top: var(--spacing-xl);
  border-top: 1px solid var(--border-light);
}

.support-text {
  font-size: var(--font-base);
  color: var(--text-secondary);
  margin-bottom: var(--spacing-lg);
}

.support-actions {
  display: flex;
  gap: var(--spacing-md);
  justify-content: center;
}

.support-btn {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  padding: var(--spacing-md) var(--spacing-lg);
  background: var(--info-color);
  color: var(--text-white);
  border: none;
  border-radius: var(--radius-md);
  font-size: var(--font-sm);
  font-weight: var(--font-medium);
  cursor: pointer;
  transition: all var(--duration-fast) var(--ease-in-out);
}

.support-btn:hover {
  background: rgba(6, 182, 212, 0.8);
  transform: translateY(-1px);
}

/* 背景装饰 */
.bg-decoration {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1;
  pointer-events: none;
}

.floating-shape {
  position: absolute;
  border-radius: var(--radius-full);
  background: rgba(255, 255, 255, 0.1);
  animation: float 6s ease-in-out infinite;
}

.shape-1 {
  width: 80px;
  height: 80px;
  top: 20%;
  left: 10%;
  animation-delay: 0s;
}

.shape-2 {
  width: 120px;
  height: 120px;
  top: 60%;
  right: 15%;
  animation-delay: 2s;
}

.shape-3 {
  width: 60px;
  height: 60px;
  bottom: 30%;
  left: 20%;
  animation-delay: 4s;
}

.shape-4 {
  width: 100px;
  height: 100px;
  top: 10%;
  right: 30%;
  animation-delay: 1s;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0px) rotate(0deg);
  }
  33% {
    transform: translateY(-20px) rotate(120deg);
  }
  66% {
    transform: translateY(20px) rotate(240deg);
  }
}

/* 移动端适配 */
@media (max-width: 767px) {
  .not-found-page {
    padding: var(--spacing-md);
  }
  
  .not-found-container {
    padding: var(--spacing-xl);
  }
  
  .icon-wrapper i {
    font-size: 80px;
  }
  
  .error-code {
    font-size: var(--font-2xl);
  }
  
  .error-title {
    font-size: var(--font-2xl);
  }
  
  .error-message {
    font-size: var(--font-base);
  }
  
  .suggestion-item {
    flex-direction: column;
    text-align: center;
    gap: var(--spacing-sm);
  }
  
  .suggestion-content {
    text-align: center;
  }
  
  .nav-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .support-actions {
    flex-direction: column;
  }
}

@media (max-width: 480px) {
  .nav-grid {
    grid-template-columns: 1fr;
  }
  
  .suggestions-list {
    gap: var(--spacing-sm);
  }
  
  .suggestion-item {
    padding: var(--spacing-md);
  }
}
</style>