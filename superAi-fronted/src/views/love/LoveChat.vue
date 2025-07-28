<template>
  <div class="love-chat">
    <!-- 欢迎界面 -->
    <div class="welcome-screen" v-if="!chatStore.hasMessages && !processingStore.isActive">
      <div class="welcome-content">
        <!-- AI头像 -->
        <div class="ai-avatar-large">
          <i class="fas fa-heart"></i>
          <div class="avatar-pulse"></div>
        </div>
        
        <!-- 欢迎信息 -->
        <div class="welcome-info">
          <h1 class="welcome-title">AI 爱情大师</h1>
          <p class="welcome-subtitle">
            专业的情感咨询师，为您提供恋爱指导、关系建议和情感支持
          </p>
        </div>
        
        <!-- 快速开始卡片 -->
        <div class="quick-start-section">
          <h3 class="section-title">情感咨询</h3>
          <div class="quick-start-grid">
            <div 
              class="quick-card"
              v-for="item in quickStartItems"
              :key="item.id"
              @click="selectQuickStart(item)"
            >
              <div class="card-icon" :style="{ background: item.color }">
                <i :class="item.icon"></i>
              </div>
              <h4 class="card-title">{{ item.title }}</h4>
              <p class="card-description">{{ item.description }}</p>
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
        
        <!-- 消息列表 -->
        <div class="messages-list">
          <MessageBubble
            v-for="(message, index) in chatStore.currentMessages"
            :key="`message-${message.id || index}`"
            :message="message"
            :show-sender="shouldShowSender(index)"
            @retry="retryMessage"
            @reply="replyToMessage"
            @like="likeMessage"
          />
        </div>
      </div>
    </div>
    
    <!-- 输入区域 -->
    <div class="chat-input-area">
      <ChatInput
        ref="chatInputRef"
        :disabled="processingStore.isActive"
        placeholder="分享您的情感困惑，获得专业建议..."
        @send="handleSendMessage"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted } from 'vue'
import { useChatStore } from '@/stores/chat'
import { useProcessingStore } from '@/stores/processing'

// 组件导入
import ThinkingProcess from '@/components/process/ThinkingProcess.vue'
import MessageBubble from '@/components/common/MessageBubble.vue'
import ChatInput from '@/components/common/ChatInput.vue'

const chatStore = useChatStore()
const processingStore = useProcessingStore()

// 响应式数据
const messagesContainer = ref(null)
const chatInputRef = ref(null)

// 快速开始项目
const quickStartItems = ref([
  {
    id: 1,
    title: '恋爱技巧',
    description: '学习如何开始和维持一段美好的恋爱关系',
    icon: 'fas fa-heart-circle-plus',
    color: 'linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%)',
    prompt: '我想了解一些恋爱技巧，如何能更好地与喜欢的人相处？'
  },
  {
    id: 2,
    title: '关系修复',
    description: '解决情感问题，修复受损的恋爱关系',
    icon: 'fas fa-heart-crack',
    color: 'linear-gradient(135deg, #ffeaa7 0%, #fab1a0 100%)',
    prompt: '我和恋人之间出现了一些问题，希望能得到修复关系的建议'
  },
  {
    id: 3,
    title: '脱单指南',
    description: '提升个人魅力，增加遇到合适伴侣的机会',
    icon: 'fas fa-user-plus',
    color: 'linear-gradient(135deg, #a29bfe 0%, #6c5ce7 100%)',
    prompt: '我想脱单，请给我一些提升个人魅力和找到合适伴侣的建议'
  },
  {
    id: 4,
    title: '情感分析',
    description: '分析情感状况，提供个性化的情感建议',
    icon: 'fas fa-brain',
    color: 'linear-gradient(135deg, #fd79a8 0%, #fdcb6e 100%)',
    prompt: '请帮我分析一下我目前的情感状况，给出一些建议'
  }
])

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
  
  processingStore.startThinking('正在思考您的情感问题...', 'analyzing')
  
  try {
    await chatStore.sendMessageStream(content, attachments)
    scrollToBottom()
  } catch (error) {
    console.error('发送消息失败:', error)
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
  // 实现回复逻辑
}

// 点赞消息
const likeMessage = (message) => {
  // 实现点赞逻辑
}

// 判断是否显示发送者
const shouldShowSender = (index) => {
  if (index === 0) return true
  const current = chatStore.currentMessages[index]
  const previous = chatStore.currentMessages[index - 1]
  return current.type !== previous.type
}

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

onMounted(() => {
  // 设置当前应用
  chatStore.switchApp('love')
  
  // 聚焦输入框
  nextTick(() => {
    chatInputRef.value?.focus()
  })
})
</script>

<style scoped>
.love-chat {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: linear-gradient(135deg, #ffeef8 0%, #fff0f5 100%);
}

.welcome-screen {
  flex: 1;
  overflow-y: auto;
  padding: var(--spacing-2xl) var(--spacing-lg);
}

.welcome-content {
  max-width: 1000px;
  margin: 0 auto;
  text-align: center;
}

.ai-avatar-large {
  position: relative;
  width: 120px;
  height: 120px;
  margin: 0 auto var(--spacing-xl);
  background: linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%);
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
  background: linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%);
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
  background: linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%);
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

.quick-start-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: var(--spacing-lg);
}

.quick-card {
  background: var(--bg-secondary);
  border-radius: var(--radius-xl);
  padding: var(--spacing-xl);
  box-shadow: var(--shadow-md);
  cursor: pointer;
  transition: all var(--duration-normal) var(--ease-in-out);
  border: 1px solid rgba(255, 154, 158, 0.2);
  text-align: center;
}

.quick-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-xl);
  border-color: rgba(255, 154, 158, 0.4);
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
}

.chat-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
}

.messages-list {
  min-height: 100%;
  display: flex;
  flex-direction: column;
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
  
  .quick-start-grid {
    grid-template-columns: 1fr;
    gap: var(--spacing-md);
  }
  
  .quick-card {
    padding: var(--spacing-lg);
  }
}
</style>