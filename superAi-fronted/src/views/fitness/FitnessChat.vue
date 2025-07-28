<template>
  <div class="fitness-chat">
    <!-- 欢迎界面 -->
    <div class="welcome-screen" v-if="!chatStore.hasMessages && !processingStore.isActive">
      <div class="welcome-content">
        <!-- AI头像 -->
        <div class="ai-avatar-large animate-energy">
          <i class="fas fa-dumbbell animate-dumbbell"></i>
          <div class="avatar-pulse animate-pulse-ring"></div>
        </div>
        
        <!-- 欢迎信息 -->
        <div class="welcome-info">
          <h1 class="welcome-title motivational-text">AI 运动助手</h1>
          <p class="welcome-subtitle">
            您的专业健身伴侣，提供个性化训练方案、运动指导和健康建议
          </p>
        </div>
        
        <!-- 快速开始卡片 -->
        <div class="quick-start-section">
          <h3 class="section-title">快速开始</h3>
          <div class="quick-start-grid">
            <div 
              class="quick-card interactive-element"
              v-for="item in quickStartItems"
              :key="item.id"
              @click="selectQuickStart(item)"
            >
              <div class="card-icon" :style="{ background: item.color }">
                <i :class="item.icon"></i>
              </div>
              <h4 class="card-title">{{ item.title }}</h4>
              <p class="card-description">{{ item.description }}</p>
              <div class="card-tags">
                <span class="tag" v-for="tag in item.tags" :key="tag">{{ tag }}</span>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 特性介绍 -->
        <div class="features-section">
          <h3 class="section-title">AI助手特性</h3>
          <div class="features-grid">
            <div class="feature-item" v-for="feature in features" :key="feature.id">
              <div class="feature-icon">
                <i :class="feature.icon"></i>
              </div>
              <div class="feature-content">
                <h4 class="feature-title">{{ feature.title }}</h4>
                <p class="feature-description">{{ feature.description }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 聊天区域 -->
    <div class="chat-area" v-else>
      <div class="chat-messages" ref="messagesContainer">
        <!-- AI思考过程 -->
        <ThinkingProcess v-if="processingStore.showThinking" />
        
        <!-- 工具调用过程 -->
        <ToolTimeline v-if="processingStore.hasActiveTools" />
        
        <!-- MCP服务状态 -->
        <MCPStatus v-if="processingStore.mcpServers.length > 0" />
        
        <!-- 实时日志 -->
        <RealtimeLog v-if="processingStore.hasLogs && showLogs" />
        
        <!-- 消息列表 -->
        <div class="messages-list">
          <MessageBubble
            v-for="(message, index) in chatStore.currentMessages"
            :key="`message-${message.id || index}`"
            :message="message"
            :show-sender="shouldShowSender(index)"
            :can-delete="canDeleteMessage(message)"
            @retry="retryMessage"
            @reply="replyToMessage"
            @like="likeMessage"
            @delete="deleteMessage"
          />
        </div>
        
        <!-- 滚动到底部按钮 -->
        <div 
          class="scroll-to-bottom" 
          v-if="showScrollButton"
          @click="scrollToBottom"
        >
          <i class="fas fa-chevron-down"></i>
          <span class="new-message-count" v-if="unreadCount > 0">{{ unreadCount }}</span>
        </div>
      </div>
    </div>
    
    <!-- 输入区域 -->
    <div class="chat-input-area">
      <ChatInput
        ref="chatInputRef"
        :disabled="processingStore.isActive"
        :placeholder="getInputPlaceholder()"
        @send="handleSendMessage"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, nextTick, onMounted, onUnmounted, watch } from 'vue'
import { useChatStore } from '@/stores/chat'
import { useProcessingStore } from '@/stores/processing'
import { useAuthStore } from '@/stores/auth'

// 组件导入
import ThinkingProcess from '@/components/process/ThinkingProcess.vue'
import ToolTimeline from '@/components/process/ToolTimeline.vue'
import MCPStatus from '@/components/process/MCPStatus.vue'
import RealtimeLog from '@/components/process/RealtimeLog.vue'
import MessageBubble from '@/components/common/MessageBubble.vue'
import ChatInput from '@/components/common/ChatInput.vue'

const chatStore = useChatStore()
const processingStore = useProcessingStore()
const authStore = useAuthStore()

// 响应式数据
const showLogs = ref(false)
const showScrollButton = ref(false)
const unreadCount = ref(0)
const messagesContainer = ref(null)
const chatInputRef = ref(null)

// 快速开始项目
const quickStartItems = ref([
  {
    id: 1,
    title: '制定训练计划',
    description: '根据您的身体状况和目标制定个性化训练方案',
    icon: 'fas fa-calendar-alt',
    color: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    tags: ['个性化', '科学训练', '目标导向'],
    prompt: '我想制定一个适合我的健身训练计划，请帮我分析一下我的身体状况和目标'
  },
  {
    id: 2,
    title: '动作指导',
    description: '学习正确的运动姿势，避免运动伤害',
    icon: 'fas fa-running',
    color: 'linear-gradient(135deg, #ff7e5f 0%, #feb47b 100%)',
    tags: ['动作标准', '安全运动', '技巧提升'],
    prompt: '请教我一些基础的健身动作，包括正确的姿势和注意事项'
  },
  {
    id: 3,
    title: '营养建议',
    description: '获得专业的饮食和营养搭配建议',
    icon: 'fas fa-apple-alt',
    color: 'linear-gradient(135deg, #84fab0 0%, #8fd3f4 100%)',
    tags: ['营养均衡', '健康饮食', '体重管理'],
    prompt: '请为我推荐一些健康的饮食搭配，帮助我达到健身目标'
  },
  {
    id: 4,
    title: '数据分析',
    description: '分析您的运动数据，提供改进建议',
    icon: 'fas fa-chart-line',
    color: 'linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%)',
    tags: ['数据驱动', '进度跟踪', '效果评估'],
    prompt: '帮我分析一下我的运动数据和训练效果，给出改进建议'
  }
])

// 特性介绍
const features = ref([
  {
    id: 1,
    title: '智能分析',
    description: '基于AI技术分析您的身体状况和运动需求',
    icon: 'fas fa-brain'
  },
  {
    id: 2,
    title: '个性定制',
    description: '根据个人目标和能力制定专属训练方案',
    icon: 'fas fa-user-cog'
  },
  {
    id: 3,
    title: '实时指导',
    description: '提供运动过程中的实时指导和建议',
    icon: 'fas fa-comments'
  },
  {
    id: 4,
    title: '进度跟踪',
    description: '持续监测训练效果，调整优化方案',
    icon: 'fas fa-chart-bar'
  }
])

// 计算属性
const getInputPlaceholder = () => {
  if (processingStore.isActive) {
    return 'AI正在处理中，请稍候...'
  }
  if (!authStore.isLoggedIn) {
    return '请先登录后开始对话...'
  }
  return '输入您的健身问题，获得专业指导...'
}

// 选择快速开始项目
const selectQuickStart = (item) => {
  if (chatInputRef.value) {
    chatInputRef.value.clearInput()
  }
  handleSendMessage({ content: item.prompt, attachments: [] })
}

// 处理发送消息
const handleSendMessage = async ({ content, attachments }) => {
  if (!content.trim() && attachments.length === 0) return
  
  // 开始AI思考过程
  processingStore.startThinking('正在理解您的问题...', 'analyzing')
  
  try {
    // 发送消息到聊天store
    await chatStore.sendMessageStream(content, attachments)
    
    // 滚动到底部
    scrollToBottom()
    
  } catch (error) {
    console.error('发送消息失败:', error)
    processingStore.addLog('error', `发送消息失败: ${error.message}`)
    processingStore.errorProcessing(error.message)
  }
}

// 重试消息
const retryMessage = (message) => {
  handleSendMessage({ 
    content: message.content, 
    attachments: message.attachments || [] 
  })
}

// 回复消息
const replyToMessage = (message) => {
  if (chatInputRef.value) {
    const replyText = `回复: "${message.content.substring(0, 50)}..."\n\n`
    chatInputRef.value.clearInput()
    // 这里需要设置输入框的值，但ChatInput组件需要暴露相应方法
  }
}

// 点赞消息
const likeMessage = (message) => {
  // 这里可以实现点赞功能
  console.log('点赞消息:', message.id)
}

// 删除消息
const deleteMessage = (message) => {
  const index = chatStore.currentMessages.findIndex(m => m.id === message.id)
  if (index > -1) {
    chatStore.currentMessages.splice(index, 1)
  }
}

// 判断是否显示发送者
const shouldShowSender = (index) => {
  if (index === 0) return true
  const current = chatStore.currentMessages[index]
  const previous = chatStore.currentMessages[index - 1]
  return current.type !== previous.type
}

// 判断是否可以删除消息
const canDeleteMessage = (message) => {
  return message.type === 'user' || authStore.userRole === 'admin'
}

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
      showScrollButton.value = false
      unreadCount.value = 0
    }
  })
}

// 处理滚动事件
const handleScroll = () => {
  if (!messagesContainer.value) return
  
  const { scrollTop, scrollHeight, clientHeight } = messagesContainer.value
  const isNearBottom = scrollTop + clientHeight >= scrollHeight - 100
  
  showScrollButton.value = !isNearBottom && chatStore.hasMessages
}

// 监听新消息，更新未读计数
watch(() => chatStore.currentMessages.length, (newLength, oldLength) => {
  if (newLength > oldLength && showScrollButton.value) {
    unreadCount.value++
  }
})

// 监听处理状态变化
watch(() => processingStore.isActive, (isActive) => {
  if (!isActive) {
    // 处理完成后，聚焦输入框
    nextTick(() => {
      chatInputRef.value?.focus()
    })
  }
})

// 键盘快捷键
const handleKeyDown = (event) => {
  // Ctrl/Cmd + D 切换日志显示
  if ((event.ctrlKey || event.metaKey) && event.key === 'd') {
    event.preventDefault()
    showLogs.value = !showLogs.value
  }
  
  // Ctrl/Cmd + End 滚动到底部
  if ((event.ctrlKey || event.metaKey) && event.key === 'End') {
    event.preventDefault()
    scrollToBottom()
  }
}

onMounted(() => {
  // 设置当前应用
  chatStore.switchApp('fitness')
  
  // 绑定事件监听器
  document.addEventListener('keydown', handleKeyDown)
  if (messagesContainer.value) {
    messagesContainer.value.addEventListener('scroll', handleScroll)
  }
  
  // 聚焦输入框
  nextTick(() => {
    chatInputRef.value?.focus()
  })
})

onUnmounted(() => {
  document.removeEventListener('keydown', handleKeyDown)
  if (messagesContainer.value) {
    messagesContainer.value.removeEventListener('scroll', handleScroll)
  }
})
</script>

<style scoped>
.fitness-chat {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: var(--bg-chat);
}

/* 欢迎界面样式 */
.welcome-screen {
  flex: 1;
  overflow-y: auto;
  padding: var(--spacing-2xl) var(--spacing-lg);
}

.welcome-content {
  max-width: 1200px;
  margin: 0 auto;
  text-align: center;
}

.ai-avatar-large {
  position: relative;
  width: 120px;
  height: 120px;
  margin: 0 auto var(--spacing-xl);
  background: var(--gradient-fitness);
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--font-4xl);
  color: var(--text-white);
  box-shadow: var(--shadow-xl);
}

.avatar-pulse {
  position: absolute;
  top: -10px;
  left: -10px;
  right: -10px;
  bottom: -10px;
  border-radius: var(--radius-full);
  background: var(--gradient-fitness);
  opacity: 0.3;
  animation: pulse-ring 3s infinite;
}

.welcome-info {
  margin-bottom: var(--spacing-2xl);
}

.welcome-title {
  font-size: var(--font-4xl);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  margin-bottom: var(--spacing-md);
  background: var(--gradient-fitness);
  background-clip: text;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.welcome-subtitle {
  font-size: var(--font-lg);
  color: var(--text-secondary);
  line-height: 1.6;
  max-width: 600px;
  margin: 0 auto;
}

.section-title {
  font-size: var(--font-2xl);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
  margin-bottom: var(--spacing-xl);
}

.quick-start-section {
  margin-bottom: var(--spacing-3xl);
}

.quick-start-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: var(--spacing-lg);
  margin-bottom: var(--spacing-2xl);
}

.quick-card {
  background: var(--bg-secondary);
  border-radius: var(--radius-xl);
  padding: var(--spacing-xl);
  box-shadow: var(--shadow-md);
  cursor: pointer;
  transition: all var(--duration-normal) var(--ease-in-out);
  border: 1px solid var(--border-light);
  text-align: center;
}

.quick-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-xl);
  border-color: var(--primary-color);
}

.card-icon {
  width: 64px;
  height: 64px;
  margin: 0 auto var(--spacing-lg);
  border-radius: var(--radius-xl);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--font-2xl);
  color: var(--text-white);
  box-shadow: var(--shadow-md);
}

.card-title {
  font-size: var(--font-xl);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
  margin-bottom: var(--spacing-sm);
}

.card-description {
  font-size: var(--font-base);
  color: var(--text-secondary);
  line-height: 1.6;
  margin-bottom: var(--spacing-md);
}

.card-tags {
  display: flex;
  justify-content: center;
  gap: var(--spacing-xs);
  flex-wrap: wrap;
}

.tag {
  padding: var(--spacing-xs) var(--spacing-sm);
  background: var(--bg-primary);
  color: var(--text-secondary);
  border-radius: var(--radius-full);
  font-size: var(--font-xs);
  font-weight: var(--font-medium);
}

.features-section {
  margin-bottom: var(--spacing-2xl);
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: var(--spacing-lg);
}

.feature-item {
  display: flex;
  align-items: flex-start;
  gap: var(--spacing-md);
  padding: var(--spacing-lg);
  background: var(--bg-secondary);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-light);
}

.feature-icon {
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
}

.feature-content {
  text-align: left;
}

.feature-title {
  font-size: var(--font-lg);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
  margin-bottom: var(--spacing-xs);
}

.feature-description {
  font-size: var(--font-sm);
  color: var(--text-secondary);
  line-height: 1.5;
}

/* 聊天区域样式 */
.chat-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  position: relative;
}

.messages-list {
  min-height: 100%;
  display: flex;
  flex-direction: column;
}

.scroll-to-bottom {
  position: fixed;
  bottom: 120px;
  right: var(--spacing-xl);
  width: 48px;
  height: 48px;
  background: var(--primary-color);
  color: var(--text-white);
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: var(--shadow-lg);
  transition: all var(--duration-fast) var(--ease-in-out);
  z-index: 100;
}

.scroll-to-bottom:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-xl);
}

.new-message-count {
  position: absolute;
  top: -8px;
  right: -8px;
  background: var(--danger-color);
  color: var(--text-white);
  border-radius: var(--radius-full);
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--font-xs);
  font-weight: var(--font-bold);
}

.chat-input-area {
  flex-shrink: 0;
}

/* 移动端适配 */
@media (max-width: 767px) {
  .welcome-screen {
    padding: var(--spacing-xl) var(--spacing-md);
  }
  
  .ai-avatar-large {
    width: 80px;
    height: 80px;
    font-size: var(--font-3xl);
  }
  
  .welcome-title {
    font-size: var(--font-3xl);
  }
  
  .welcome-subtitle {
    font-size: var(--font-base);
  }
  
  .section-title {
    font-size: var(--font-xl);
  }
  
  .quick-start-grid {
    grid-template-columns: 1fr;
    gap: var(--spacing-md);
  }
  
  .quick-card {
    padding: var(--spacing-lg);
  }
  
  .card-icon {
    width: 48px;
    height: 48px;
    font-size: var(--font-xl);
  }
  
  .features-grid {
    grid-template-columns: 1fr;
    gap: var(--spacing-md);
  }
  
  .feature-item {
    padding: var(--spacing-md);
  }
  
  .scroll-to-bottom {
    right: var(--spacing-md);
    bottom: 100px;
    width: 40px;
    height: 40px;
  }
}

@media (max-width: 480px) {
  .welcome-content {
    padding: 0;
  }
  
  .quick-card {
    padding: var(--spacing-md);
  }
  
  .card-title {
    font-size: var(--font-lg);
  }
  
  .feature-item {
    flex-direction: column;
    text-align: center;
  }
  
  .feature-content {
    text-align: center;
  }
}
</style>