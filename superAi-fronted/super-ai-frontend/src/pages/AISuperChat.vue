<template>
  <div class="ai-super-chat-container">
    <!-- 页面头部 -->
    <header class="chat-header">
      <router-link to="/" class="back-btn">
        <span>←</span> 返回首页
      </router-link>
      <div class="header-info">
        <div class="header-icon">🤖</div>
        <div class="header-text">
          <h1>AI超级智能体</h1>
          <p>全能助手，支持工具调用和复杂任务</p>
        </div>
      </div>
    </header>

    <!-- 聊天区域 -->
    <main class="chat-main">
      <div class="chat-messages" ref="messagesContainer">
        <!-- 欢迎消息 -->
        <div v-if="messages.length === 0" class="welcome-message">
          <div class="welcome-avatar">🤖</div>
          <div class="welcome-content">
            <h3>您好！我是AI超级智能体</h3>
            <p>我具备以下能力：</p>
            <ul>
              <li>🛠️ 工具调用 - 可使用各种工具完成任务</li>
              <li>📊 数据分析 - 处理和分析复杂数据</li>
              <li>💡 创意生成 - 创作文案、故事、方案等</li>
              <li>🔍 信息查询 - 搜索和整理信息</li>
              <li>🎯 任务规划 - 制定详细执行方案</li>
            </ul>
            <p class="ai-note">🚀 我会根据任务需要智能选择合适的工具</p>
          </div>
        </div>

        <!-- 消息列表 -->
        <div v-for="(message, index) in messages" :key="index" 
             :class="['message', message.role]">
          <div class="message-avatar">
            <span v-if="message.role === 'user'">👤</span>
            <span v-else>🤖</span>
          </div>
          <div class="message-content">
            <!-- 使用安全的Vue组件化方式渲染消息 -->
            <div class="message-text">
              <!-- 遍历解析后的内容块，并根据类型渲染 -->
              <div v-for="(block, blockIndex) in parseMessageBlocks(message.content)" :key="blockIndex">
                
                <!-- 渲染 Manus 步骤卡片 -->
                <div v-if="block.type === 'manusStep'" class="manus-step-card" :class="block.toolType">
                  <div class="step-header">
                    <div class="step-info">
                      <span class="step-icon">{{ block.icon }}</span>
                      <span class="step-number">步骤 {{ block.number }}</span>
                    </div>
                    <div class="step-status">执行中</div>
                  </div>
                  <div class="step-content">
                    <pre>{{ block.content }}</pre>
                  </div>
                </div>

                <!-- 渲染工具结果卡片 -->
                <div v-else-if="block.type === 'toolResult'" class="tool-result-card success">
                  <div class="result-header">
                    <span class="result-icon">✅</span>
                    <span class="result-title">工具 {{ block.toolName }} 完成了</span>
                  </div>
                  <div class="result-content">
                    <!-- 折叠/展开逻辑 -->
                    <template v-if="block.content.length > 300 && !block.isExpanded">
                      <div class="result-preview">
                        <pre>{{ block.content.substring(0, 300) }}...</pre>
                      </div>
                      <!-- 使用 @click 安全地绑定 Vue 方法 -->
                      <div class="result-toggle" @click="toggleResultExpansion(block)">
                        <span class="toggle-text">查看完整结果 ▼</span>
                      </div>
                    </template>
                    <div v-else class="result-full">
                      <pre>{{ block.content }}</pre>
                    </div>
                  </div>
                </div>

                <!-- 渲染普通段落文本 -->
                <p v-else-if="block.type === 'paragraph'">{{ block.text }}</p>

              </div>
            </div>
            
            <div class="message-time">{{ formatTime(message.timestamp) }}</div>
          </div>
        </div>

        <!-- 加载状态 -->
        <div v-if="isLoading" class="message assistant">
          <div class="message-avatar">🤖</div>
          <div class="message-content">
            <div class="thinking-indicator">
              <div class="thinking-text">AI正在思考...</div>
              <div class="thinking-animation">
                <span></span><span></span><span></span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 输入区域 -->
      <footer class="chat-input-area">
        <div class="quick-tasks">
          <h4>⚡ 快速任务：</h4>
          <div class="quick-buttons">
            <button @click="sendQuickTask('帮我分析一下当前的股票市场趋势')" 
                    :disabled="isLoading">市场分析</button>
            <button @click="sendQuickTask('制定一个健身计划，包括饮食和运动')" 
                    :disabled="isLoading">健身方案</button>
            <button @click="sendQuickTask('帮我写一份项目总结报告')" 
                    :disabled="isLoading">报告撰写</button>
            <button @click="sendQuickTask('设计一个学习计划，提高英语水平')" 
                    :disabled="isLoading">学习规划</button>
          </div>
        </div>
        
        <div class="input-container">
          <textarea 
            v-model="inputMessage"
            @keydown.enter.prevent="handleEnterKey"
            placeholder="请告诉我您想要完成的任务，我会智能选择合适的工具来帮助您..."
            :disabled="isLoading"
            ref="messageInput"
            rows="3"
          ></textarea>
          <button @click="sendMessage" :disabled="!inputMessage.trim() || isLoading" 
                  class="send-btn">
            <span v-if="!isLoading">发送 🚀</span>
            <span v-else>处理中...</span>
          </button>
        </div>
      </footer>
    </main>
  </div>
</template>

<script>
import { sseRequest } from '@/utils/sseRequest';
import { API_ENDPOINTS } from '@/config/api';

export default {
  name: 'AISuperChat',
  data() {
    return {
      messages: [],
      inputMessage: '',
      isLoading: false,
      sessionId: null,
      lastRequestTime: 0,
      requestMinInterval: 2000
    }
  },
  
  mounted() {
    this.initSession();
    this.$refs.messageInput?.focus();
    
    // Windows系统特殊处理
    if (navigator.platform.includes('Win')) {
      console.log('🖥️ 检测到Windows系统，已应用优化配置');
      document.body.classList.add('windows-system');
    }
  },
  
  beforeUnmount() {
    if (sseRequest) {
      sseRequest.disconnect();
    }
  },
  
  methods: {
    initSession() {
      this.sessionId = `ai_super_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
      console.log('🤖 AI超级智能体会话初始化:', this.sessionId);
    },

    async sendMessage() {
      // 多重防重复检查
      if (!this.inputMessage.trim() || this.isLoading) {
        console.warn('⚠️ 消息为空或正在发送中');
        return;
      }

      // 时间间隔防重复
      const now = Date.now();
      if (now - this.lastRequestTime < this.requestMinInterval) {
        console.warn('⚠️ 请求过于频繁，已忽略');
        return;
      }
      this.lastRequestTime = now;

      const userMessage = {
        role: 'user',
        content: this.inputMessage.trim(),
        timestamp: new Date()
      };

      this.messages.push(userMessage);
      const messageToSend = this.inputMessage.trim();
      this.inputMessage = '';
      this.isLoading = true;

      // 确保清理之前的连接
      sseRequest.disconnect();
      await new Promise(resolve => setTimeout(resolve, 100));

      const assistantMessage = {
        role: 'assistant',
        content: '',
        timestamp: new Date(),
        toolCalls: []
      };
      this.messages.push(assistantMessage);

      try {
        this.$nextTick(() => this.scrollToBottom());

        const uniqueChatId = `${this.sessionId}_${Date.now()}`;

        await sseRequest.connect(
          API_ENDPOINTS.aiSuperChat,
          messageToSend,
          uniqueChatId,
          {
            onMessage: (data) => {
              if (data.content && data.content.trim()) {
                const lastMessage = this.messages[this.messages.length - 1];
                if (lastMessage.role === 'assistant') {
                  lastMessage.content += data.content.trim();
                  this.$nextTick(() => this.scrollToBottom());
                }
              }
            },
            onError: (error) => {
              console.error('❌ SSE错误:', error);
              const lastMessage = this.messages[this.messages.length - 1];
              if (lastMessage.role === 'assistant' && !lastMessage.content) {
                lastMessage.content = 'AI超级智能体服务暂时不可用，请稍后重试。';
              }
              this.isLoading = false;
            },
            onComplete: () => {
              console.log('✅ AI超级智能体完成');
              this.isLoading = false;
              this.$nextTick(() => {
                this.scrollToBottom();
                this.$refs.messageInput?.focus();
              });
            }
          }
        );

      } catch (error) {
        console.error('❌ AI超级智能体失败:', error);
        
        if (this.messages.length > 0) {
          const lastMessage = this.messages[this.messages.length - 1];
          if (lastMessage.role === 'assistant' && !lastMessage.content) {
            this.messages.pop();
          }
        }
        
        this.isLoading = false;
        this.$nextTick(() => {
          this.scrollToBottom();
          this.$refs.messageInput?.focus();
        });
      }
    },

    sendQuickTask(task) {
      if (this.isLoading) return;
      this.inputMessage = task;
      this.sendMessage();
    },

    handleEnterKey(event) {
      if (event.shiftKey) {
        return; // Shift+Enter 换行
      }
      if (!this.isLoading) {
        this.sendMessage();
      }
    },

    /**
     * 将原始文本消息解析为结构化的块数组，供模板渲染 - 安全的Vue组件化方式
     * @param {string} content - 原始消息内容
     * @returns {Array<Object>} 结构化的内容块数组
     */
    parseMessageBlocks(content) {
      if (!content) return [];

      const blocks = [];
      // 使用正则表达式按块（步骤、结果、普通文本）分割内容
      const regex = /(data:Step\s*\d+:[\s\S]*?)(?=data:Step|工具\s*\w+\s*完成|$)|\s*(工具\s*\w+\s*完成了[\s\S]*?)(?=data:Step|工具\s*\w+\s*完成|$)/g;
      
      let lastIndex = 0;
      let match;

      while ((match = regex.exec(content)) !== null) {
        // 添加匹配前的普通文本
        const precedingText = content.substring(lastIndex, match.index).trim();
        if (precedingText) {
          blocks.push({ type: 'paragraph', text: precedingText });
        }

        // 处理匹配到的步骤或结果块
        if (match[1]) { // 匹配到步骤 (Step)
          const stepContent = match[1];
          const stepMatch = stepContent.match(/data:Step\s*(\d+):\s*([\s\S]*)/);
          if (stepMatch) {
            const toolType = this.detectToolType(stepMatch[2]);
            blocks.push({
              type: 'manusStep',
              number: stepMatch[1],
              content: stepMatch[2].trim(),
              toolType: toolType,
              icon: this.getToolIcon(toolType)
            });
          }
        } else if (match[2]) { // 匹配到结果 (Result)
          const resultMatch = match[2].match(/工具\s*(\w+)\s*完成了([\s\S]*)/);
          if (resultMatch) {
            blocks.push({
              type: 'toolResult',
              toolName: resultMatch[1],
              content: resultMatch[2].trim(),
              isSuccess: true,
              isExpanded: false // 用于控制折叠状态
            });
          }
        }
        lastIndex = regex.lastIndex;
      }

      // 添加最后一个匹配项之后的剩余文本
      const remainingText = content.substring(lastIndex).trim();
      if (remainingText) {
        blocks.push({ type: 'paragraph', text: remainingText });
      }

      return blocks;
    },

    /**
     * 切换工具结果的展开/折叠状态
     * @param {Object} block - The block object to toggle.
     */
    toggleResultExpansion(block) {
      block.isExpanded = !block.isExpanded;
    },
    
    /**
     * 根据内容检测工具类型
     * @param {string} content
     * @returns {string}
     */
    detectToolType(content) {
      if (content.includes('searchWeb') || content.includes('搜索')) return 'search';
      if (content.includes('writeFile') || content.includes('文件')) return 'file';
      if (content.includes('terminal') || content.includes('命令')) return 'terminal';
      if (content.includes('scrapeWebpage') || content.includes('爬取')) return 'scrape';
      return 'general';
    },

    /**
     * 根据工具类型获取对应的图标
     * @param {string} toolType
     * @returns {string}
     */
    getToolIcon(toolType) {
      const icons = {
        search: '🔍',
        file: '📁',
        terminal: '💻',
        scrape: '🕷️',
        general: '🔧'
      };
      return icons[toolType];
    },

    formatTime(timestamp) {
      return new Date(timestamp).toLocaleTimeString('zh-CN', {
        hour: '2-digit',
        minute: '2-digit'
      });
    },

    scrollToBottom() {
      const container = this.$refs.messagesContainer;
      if (container) {
        container.scrollTop = container.scrollHeight;
      }
    }
  }
}
</script>

<style scoped>
/* === Windows系统字体优化 === */
* {
  font-family: 'Microsoft YaHei', 'PingFang SC', 'Helvetica Neue', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

/* === 基础布局 === */
.ai-super-chat-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
}

.chat-header {
  background: white;
  padding: 1.2rem 2rem;
  border-bottom: 1px solid #e0e6ed;
  display: flex;
  align-items: center;
  gap: 1rem;
}

.back-btn {
  color: #6c5ce7;
  text-decoration: none;
  font-weight: 500;
  font-size: 1.1rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.header-info {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.header-icon {
  font-size: 2.2rem;
}

.header-text h1 {
  font-size: 1.6rem;
  color: #2c3e50;
  margin: 0;
  font-weight: 600;
}

.header-text p {
  color: #7f8c8d;
  margin: 0;
  font-size: 1rem;
}

/* === 聊天区域 === */
.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 1.5rem 2rem;
  display: flex;
  flex-direction: column;
  gap: 1.2rem;
}

/* === 消息样式 - Windows优化 === */
.message {
  display: flex;
  gap: 1rem;
  max-width: 75%;
  font-size: 1.05rem;
  line-height: 1.7;
}

.message.user {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.message-avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.3rem;
  flex-shrink: 0;
}

.message.user .message-avatar {
  background: #3498db;
  color: white;
}

.message.assistant .message-avatar {
  background: #6c5ce7;
  color: white;
}

.message-content {
  flex: 1;
}

.message-text {
  background: white;
  padding: 1.3rem;
  border-radius: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  line-height: 1.7;
  word-wrap: break-word;
  font-size: 1.05rem;
}

.message.user .message-text {
  background: #3498db;
  color: white;
}

/* === Manus步骤卡片样式 === */
.message-text .manus-step-card {
  background: linear-gradient(135deg, #f8f9ff 0%, #e8f2ff 100%);
  border: 2px solid #4CAF50;
  border-radius: 16px;
  margin: 1.2rem 0;
  overflow: hidden;
  box-shadow: 0 8px 24px rgba(76, 175, 80, 0.15);
  transition: all 0.3s ease;
}

.message-text .manus-step-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 32px rgba(76, 175, 80, 0.25);
}

.message-text .manus-step-card.search { 
  border-color: #2196F3; 
  background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%); 
}

.message-text .manus-step-card.file { 
  border-color: #FF9800; 
  background: linear-gradient(135deg, #fff3e0 0%, #ffe0b2 100%); 
}

.message-text .manus-step-card.terminal { 
  border-color: #9C27B0; 
  background: linear-gradient(135deg, #f3e5f5 0%, #e1bee7 100%); 
}

.message-text .manus-step-card.scrape { 
  border-color: #FF5722; 
  background: linear-gradient(135deg, #fbe9e7 0%, #ffccbc 100%); 
}

.message-text .step-header {
  background: linear-gradient(135deg, #4CAF50 0%, #45a049 100%);
  color: white; 
  padding: 1rem 1.5rem; 
  display: flex; 
  justify-content: space-between; 
  align-items: center;
}

.message-text .manus-step-card.search .step-header { 
  background: linear-gradient(135deg, #2196F3 0%, #1976D2 100%); 
}

.message-text .manus-step-card.file .step-header { 
  background: linear-gradient(135deg, #FF9800 0%, #F57C00 100%); 
}

.message-text .manus-step-card.terminal .step-header { 
  background: linear-gradient(135deg, #9C27B0 0%, #7B1FA2 100%); 
}

.message-text .manus-step-card.scrape .step-header { 
  background: linear-gradient(135deg, #FF5722 0%, #D84315 100%); 
}

.message-text .step-info { 
  display: flex; 
  align-items: center; 
  gap: 0.8rem; 
}

.message-text .step-icon { 
  font-size: 1.4rem; 
}

.message-text .step-number { 
  font-size: 1.1rem; 
  font-weight: 700; 
}

.message-text .step-status { 
  background: rgba(255,255,255,0.2); 
  padding: 0.3rem 0.8rem; 
  border-radius: 12px; 
  font-size: 0.85rem; 
}

.message-text .step-content { 
  padding: 1.5rem; 
  background: white; 
  color: #2c3e50; 
  line-height: 1.6; 
}

.message-text .step-content pre { 
  white-space: pre-wrap; 
  word-wrap: break-word; 
  font-family: 'Consolas', 'Monaco', monospace; 
}

/* === 工具结果卡片样式 === */
.message-text .tool-result-card { 
  border-radius: 12px; 
  margin: 1rem 0; 
  overflow: hidden; 
  box-shadow: 0 4px 16px rgba(0,0,0,0.1); 
}

.message-text .tool-result-card.success { 
  border: 2px solid #4CAF50; 
  background: linear-gradient(135deg, #e8f5e8 0%, #c8e6c9 100%); 
}

.message-text .result-header { 
  padding: 1rem 1.5rem; 
  display: flex; 
  align-items: center; 
  gap: 0.8rem; 
  font-weight: 600; 
  background: #4CAF50; 
  color: white; 
}

.message-text .result-content { 
  padding: 1.2rem 1.5rem; 
  background: white; 
  color: #2c3e50; 
  line-height: 1.6; 
}

.message-text .result-content pre { 
  white-space: pre-wrap; 
  word-wrap: break-word; 
  font-family: 'Consolas', 'Monaco', monospace; 
  font-size: 0.9rem; 
}

.message-text .result-toggle { 
  text-align: center; 
  padding: 0.8rem; 
  background: #f8f9fa; 
  cursor: pointer; 
  border-top: 1px solid #e9ecef; 
  transition: background 0.2s; 
}

.message-text .result-toggle:hover { 
  background: #e9ecef; 
}

.message-text .toggle-text { 
  color: #007bff; 
  font-weight: 500; 
}

.message-time {
  font-size: 0.75rem;
  color: #95a5a6;
  margin-top: 0.5rem;
  text-align: right;
}

.message.user .message-time {
  text-align: left;
}

.welcome-message {
  display: flex;
  gap: 1rem;
  max-width: 600px;
}

.welcome-avatar {
  font-size: 2rem;
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #6c5ce7;
  border-radius: 50%;
  flex-shrink: 0;
}

.welcome-content {
  background: white;
  padding: 1.5rem;
  border-radius: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.welcome-content h3 {
  color: #2c3e50;
  margin: 0 0 1rem 0;
}

.welcome-content ul {
  margin: 1rem 0;
  padding-left: 1rem;
}

.welcome-content li {
  margin: 0.5rem 0;
  color: #34495e;
}

.ai-note {
  background: #e8f4fd;
  border: 1px solid #74b9ff;
  padding: 0.75rem;
  border-radius: 8px;
  color: #0984e3;
  font-size: 0.9rem;
  margin-top: 1rem;
}

.thinking-indicator {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  background: white;
  border-radius: 16px;
}

.thinking-text {
  color: #6c5ce7;
  font-weight: 500;
}

.thinking-animation {
  display: flex;
  gap: 4px;
}

.thinking-animation span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #6c5ce7;
  animation: thinking 1.4s infinite ease-in-out;
}

.thinking-animation span:nth-child(1) { animation-delay: 0s; }
.thinking-animation span:nth-child(2) { animation-delay: 0.2s; }
.thinking-animation span:nth-child(3) { animation-delay: 0.4s; }

@keyframes thinking {
  0%, 60%, 100% { transform: scale(1); }
  30% { transform: scale(1.2); }
}

.chat-input-area {
  background: rgba(255,255,255,0.95);
  backdrop-filter: blur(10px);
  border-top: 1px solid #e0e6ed;
  padding: 1.5rem 2rem;
}

.quick-tasks {
  margin-bottom: 1rem;
}

.quick-tasks h4 {
  font-size: 1rem;
  color: #7f8c8d;
  margin: 0 0 0.8rem 0;
}

.quick-buttons {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.quick-buttons button {
  background: #ecf0f1;
  border: none;
  padding: 0.6rem 1.2rem;
  border-radius: 20px;
  font-size: 0.95rem;
  color: #34495e;
  cursor: pointer;
  transition: all 0.2s;
  margin: 0.3rem 0.3rem 0.3rem 0;
}

.quick-buttons button:hover:not(:disabled) {
  background: #6c5ce7;
  color: white;
}

.quick-buttons button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.input-container {
  display: flex;
  gap: 1rem;
  align-items: flex-end;
}

.input-container textarea {
  flex: 1;
  border: 2px solid #e0e6ed;
  border-radius: 12px;
  padding: 1.2rem;
  font-size: 1.05rem;
  font-family: inherit;
  resize: none;
  transition: border-color 0.2s;
  line-height: 1.5;
}

.input-container textarea:focus {
  outline: none;
  border-color: #6c5ce7;
}

.send-btn {
  background: #6c5ce7;
  color: white;
  border: none;
  padding: 1.2rem 1.8rem;
  border-radius: 12px;
  font-size: 1.05rem;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
  font-weight: 500;
}

.send-btn:hover:not(:disabled) {
  background: #5a4fcf;
  transform: translateY(-1px);
}

.send-btn:disabled {
  background: #bdc3c7;
  cursor: not-allowed;
  transform: none;
}

/* === 响应式适配 === */
@media (max-width: 768px) {
  .chat-header {
    padding: 1rem;
  }
  
  .chat-messages {
    padding: 1rem;
  }
  
  .message {
    max-width: 92%;
    font-size: 1rem;
  }
  
  .message-text {
    padding: 1rem;
    font-size: 1rem;
  }
  
  .input-container {
    flex-direction: column;
    align-items: stretch;
  }
  
  .send-btn {
    align-self: flex-end;
  }
}

/* === Windows系统特殊优化 === */
@media screen and (-ms-high-contrast: active), (-ms-high-contrast: none) {
  * {
    font-family: 'Microsoft YaHei', 'Segoe UI', Tahoma, sans-serif;
  }
}

/* === 滚动条美化（Windows） === */
.chat-messages::-webkit-scrollbar {
  width: 8px;
}

.chat-messages::-webkit-scrollbar-track {
  background: rgba(0,0,0,0.05);
  border-radius: 4px;
}

.chat-messages::-webkit-scrollbar-thumb {
  background: rgba(0,0,0,0.2);
  border-radius: 4px;
}

.chat-messages::-webkit-scrollbar-thumb:hover {
  background: rgba(0,0,0,0.3);
}
</style>