<template>
  <div class="manus-chat">
    <!-- 欢迎界面 -->
    <div class="welcome-screen" v-if="!chatStore.hasMessages && !processingStore.isActive">
      <div class="welcome-content">
        <!-- AI头像 -->
        <div class="ai-avatar-large">
          <i class="fas fa-robot"></i>
          <div class="avatar-pulse"></div>
        </div>
        
        <!-- 欢迎信息 -->
        <div class="welcome-info">
          <h1 class="welcome-title">AI 超级助手</h1>
          <p class="welcome-subtitle">
            全能的AI助手，为您提供学习辅导、工作协助、生活建议等全方位支持
          </p>
        </div>
        
        <!-- 快速开始卡片 -->
        <div class="quick-start-section">
          <h3 class="section-title">智能助手服务</h3>
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
              <div class="card-tags">
                <span class="tag" v-for="tag in item.tags" :key="tag">{{ tag }}</span>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 能力展示 -->
        <div class="capabilities-section">
          <h3 class="section-title">AI能力</h3>
          <div class="capabilities-grid">
            <div class="capability-item" v-for="capability in capabilities" :key="capability.id">
              <div class="capability-icon">
                <i :class="capability.icon"></i>
              </div>
              <div class="capability-content">
                <h4 class="capability-title">{{ capability.title }}</h4>
                <p class="capability-description">{{ capability.description }}</p>
                <div class="capability-examples">
                  <span 
                    v-for="example in capability.examples" 
                    :key="example"
                    class="example-tag"
                  >
                    {{ example }}
                  </span>
                </div>
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
        placeholder="向AI超级助手提问，获得全方位帮助..."
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
import ToolTimeline from '@/components/process/ToolTimeline.vue'
import MCPStatus from '@/components/process/MCPStatus.vue'
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
    title: '学习辅导',
    description: '提供学科知识解答、学习方法指导和作业辅助',
    icon: 'fas fa-graduation-cap',
    color: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    tags: ['知识问答', '学习方法', '作业辅导'],
    prompt: '我需要学习方面的帮助，请为我提供学习指导和知识解答'
  },
  {
    id: 2,
    title: '工作协助',
    description: '协助处理工作任务、提供解决方案和效率建议',
    icon: 'fas fa-briefcase',
    color: 'linear-gradient(135deg, #ffeaa7 0%, #fab1a0 100%)',
    tags: ['任务规划', '效率提升', '问题解决'],
    prompt: '我在工作中遇到了一些挑战，希望得到专业的建议和解决方案'
  },
  {
    id: 3,
    title: '创意写作',
    description: '协助创作文章、故事、诗歌等各类文本内容',
    icon: 'fas fa-feather-alt',
    color: 'linear-gradient(135deg, #a29bfe 0%, #6c5ce7 100%)',
    tags: ['文章创作', '故事编写', '内容优化'],
    prompt: '我想进行创意写作，请帮我构思内容和优化文本'
  },
  {
    id: 4,
    title: '数据分析',
    description: '分析数据趋势、制作图表和提供数据洞察',
    icon: 'fas fa-chart-bar',
    color: 'linear-gradient(135deg, #fd79a8 0%, #fdcb6e 100%)',
    tags: ['数据处理', '趋势分析', '可视化'],
    prompt: '我有一些数据需要分析，请帮我找出其中的规律和趋势'
  },
  {
    id: 5,
    title: '编程助手',
    description: '代码编写、调试、优化和技术问题解答',
    icon: 'fas fa-code',
    color: 'linear-gradient(135deg, #00b894 0%, #00cec9 100%)',
    tags: ['代码编写', '程序调试', '技术咨询'],
    prompt: '我在编程方面需要帮助，请协助我解决技术问题'
  },
  {
    id: 6,
    title: '生活建议',
    description: '提供日常生活、健康、理财等方面的实用建议',
    icon: 'fas fa-lightbulb',
    color: 'linear-gradient(135deg, #e17055 0%, #f39c12 100%)',
    tags: ['生活技巧', '健康建议', '理财规划'],
    prompt: '我想获得一些生活方面的建议和实用技巧'
  }
])

// AI能力展示
const capabilities = ref([
  {
    id: 1,
    title: '知识问答',
    description: '回答各领域问题，提供准确可靠的信息',
    icon: 'fas fa-question-circle',
    examples: ['科学常识', '历史事件', '文学知识', '技术问题']
  },
  {
    id: 2,
    title: '文本处理',
    description: '编写、翻译、总结和优化各类文本内容',
    icon: 'fas fa-file-alt',
    examples: ['文章写作', '语言翻译', '内容总结', '格式转换']
  },
  {
    id: 3,
    title: '逻辑推理',
    description: '进行复杂的逻辑分析和推理计算',
    icon: 'fas fa-brain',
    examples: ['数学计算', '逻辑分析', '决策支持', '问题拆解']
  },
  {
    id: 4,
    title: '创意协助',
    description: '激发创意灵感，协助创作和设计',
    icon: 'fas fa-palette',
    examples: ['创意构思', '方案设计', '内容创作', '头脑风暴']
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
  
  processingStore.startThinking('正在分析您的问题...', 'analyzing')
  
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
  chatStore.switchApp('manus')
  
  // 聚焦输入框
  nextTick(() => {
    chatInputRef.value?.focus()
  })
})
</script>

<style scoped>
.manus-chat {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: linear-gradient(135deg, #f0f2ff 0%, #f8faff 100%);
}

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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
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

.capabilities-section {
  margin-bottom: var(--spacing-2xl);
}

.capabilities-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: var(--spacing-lg);
}

.capability-item {
  display: flex;
  align-items: flex-start;
  gap: var(--spacing-md);
  padding: var(--spacing-xl);
  background: var(--bg-secondary);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-light);
  text-align: left;
}

.capability-icon {
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

.capability-content {
  flex: 1;
}

.capability-title {
  font-size: var(--font-lg);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
  margin-bottom: var(--spacing-xs);
}

.capability-description {
  font-size: var(--font-sm);
  color: var(--text-secondary);
  line-height: 1.5;
  margin-bottom: var(--spacing-md);
}

.capability-examples {
  display: flex;
  gap: var(--spacing-xs);
  flex-wrap: wrap;
}

.example-tag {
  padding: 2px var(--spacing-xs);
  background: rgba(59, 130, 246, 0.1);
  color: var(--primary-color);
  border-radius: var(--radius-sm);
  font-size: var(--font-xs);
  font-weight: var(--font-medium);
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
  
  .capabilities-grid {
    grid-template-columns: 1fr;
    gap: var(--spacing-md);
  }
  
  .capability-item {
    padding: var(--spacing-md);
  }
}
</style>