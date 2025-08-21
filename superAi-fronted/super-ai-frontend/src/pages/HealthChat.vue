<template>
  <div class="health-chat-container">
    <!-- 页面头部 -->
    <header class="chat-header">
      <router-link to="/" class="back-btn">
        <span>←</span> 返回首页
      </router-link>
      <div class="header-info">
        <div class="header-icon">🩺</div>
        <div class="header-text">
          <h1>健康咨询助手</h1>
          <p>专业医疗知识，贴心健康建议</p>
        </div>
      </div>
    </header>

    <!-- 聊天区域 -->
    <main class="chat-main">
      <div class="chat-messages" ref="messagesContainer">
        <!-- 欢迎消息 -->
        <div v-if="messages.length === 0" class="welcome-message">
          <div class="welcome-avatar">🩺</div>
          <div class="welcome-content">
            <h3>您好！我是您的健康咨询助手</h3>
            <p>我可以为您提供：</p>
            <ul>
              <li>🔍 症状分析与建议</li>
              <li>💊 用药指导</li>
              <li>🏥 就医建议</li>
              <li>🔬 体检报告解读</li>
              <li>🛡️ 疾病预防知识</li>
            </ul>
            <p class="disclaimer">⚠️ 本服务仅供参考，不能替代专业医生诊断</p>
          </div>
        </div>

        <!-- 消息列表 -->
        <div v-for="(message, index) in messages" :key="index" 
             :class="['message', message.role]">
          <div class="message-avatar">
            <span v-if="message.role === 'user'">👤</span>
            <span v-else>🩺</span>
          </div>
          <div class="message-content">
            <div class="message-text">{{ message.content }}</div>
            <div class="message-time">{{ formatTime(message.timestamp) }}</div>
          </div>
        </div>

        <!-- 加载状态 -->
        <div v-if="isLoading" class="message assistant">
          <div class="message-avatar">🩺</div>
          <div class="message-content">
            <div class="typing-indicator">
              <span></span><span></span><span></span>
            </div>
          </div>
        </div>
      </div>

      <!-- 输入区域 -->
      <footer class="chat-input-area">
        <div class="quick-questions">
          <h4>💡 常见问题：</h4>
          <div class="quick-buttons">
            <button @click="sendQuickQuestion('头痛怎么缓解？')" 
                    :disabled="isLoading">头痛缓解</button>
            <button @click="sendQuickQuestion('感冒了怎么办？')" 
                    :disabled="isLoading">感冒处理</button>
            <button @click="sendQuickQuestion('失眠如何改善？')" 
                    :disabled="isLoading">改善失眠</button>
            <button @click="sendQuickQuestion('体检报告怎么看？')" 
                    :disabled="isLoading">体检解读</button>
          </div>
        </div>
        
        <div class="input-container">
          <textarea 
            v-model="inputMessage"
            @keydown.enter.prevent="handleEnterKey"
            placeholder="请描述您的健康问题，我将为您提供专业建议..."
            :disabled="isLoading"
            ref="messageInput"
            rows="3"
          ></textarea>
          <button @click="sendMessage" :disabled="!inputMessage.trim() || isLoading" 
                  class="send-btn">
            <span v-if="!isLoading">发送 📤</span>
            <span v-else>发送中...</span>
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
  name: 'HealthChat',
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
      this.sessionId = `health_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
      console.log('🏥 健康咨询会话初始化:', this.sessionId);
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
        timestamp: new Date()
      };
      this.messages.push(assistantMessage);

      try {
        this.$nextTick(() => this.scrollToBottom());

        const uniqueChatId = `${this.sessionId}_${Date.now()}`;

        await sseRequest.connect(
          API_ENDPOINTS.healthChat,
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
                lastMessage.content = '健康咨询服务暂时不可用，请稍后重试。';
              }
              this.isLoading = false;
            },
            onComplete: () => {
              console.log('✅ 健康咨询完成');
              this.isLoading = false;
              this.$nextTick(() => {
                this.scrollToBottom();
                this.$refs.messageInput?.focus();
              });
            }
          }
        );

      } catch (error) {
        console.error('❌ 健康咨询失败:', error);
        
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

    sendQuickQuestion(question) {
      if (this.isLoading) return;
      this.inputMessage = question;
      this.sendMessage();
    },

    handleEnterKey(event) {
      if (event.shiftKey) {
        return;
      }
      if (!this.isLoading) {
        this.sendMessage();
      }
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
.health-chat-container {
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
  color: #4CAF50;
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
  background: #4CAF50;
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

/* === 简化的消息样式 - 纯文本显示 === */
.message-text {
  white-space: pre-wrap; /* 保留换行和空格 */
  word-wrap: break-word; /* 长文本自动换行 */
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
  background: #4CAF50;
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

.disclaimer {
  background: #fff3cd;
  border: 1px solid #ffeaa7;
  padding: 0.75rem;
  border-radius: 8px;
  color: #856404;
  font-size: 0.9rem;
  margin-top: 1rem;
}

.typing-indicator {
  display: flex;
  gap: 4px;
  padding: 1rem;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #bdc3c7;
  animation: typing 1.4s infinite ease-in-out;
}

.typing-indicator span:nth-child(1) { animation-delay: 0s; }
.typing-indicator span:nth-child(2) { animation-delay: 0.2s; }
.typing-indicator span:nth-child(3) { animation-delay: 0.4s; }

@keyframes typing {
  0%, 60%, 100% { transform: translateY(0); }
  30% { transform: translateY(-10px); }
}

/* === 输入区域 === */
.chat-input-area {
  background: rgba(255,255,255,0.95);
  backdrop-filter: blur(10px);
  border-top: 1px solid #e0e6ed;
  padding: 1.5rem 2rem;
}

.quick-questions {
  margin-bottom: 1rem;
}

.quick-questions h4 {
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
  background: #4CAF50;
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
  border-color: #4CAF50;
}

.send-btn {
  background: #4CAF50;
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
  background: #45a049;
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