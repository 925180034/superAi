<template>
  <div class="nutrition-chat-container">
    <!-- 页面头部 -->
    <header class="chat-header">
      <router-link to="/" class="back-btn">
        <span>←</span> 返回首页
      </router-link>
      <div class="header-info">
        <div class="header-icon">🥗</div>
        <div class="header-text">
          <h1>营养饮食指导</h1>
          <p>科学营养搭配，健康生活方式</p>
        </div>
      </div>
    </header>

    <!-- 聊天区域 -->
    <main class="chat-main">
      <div class="chat-messages" ref="messagesContainer">
        <!-- 欢迎消息 -->
        <div v-if="messages.length === 0" class="welcome-message">
          <div class="welcome-avatar">🥗</div>
          <div class="welcome-content">
            <h3>欢迎来到营养指导中心！</h3>
            <p>我是您的专业营养顾问，可以为您提供：</p>
            <ul>
              <li>🍎 食谱推荐与搭配</li>
              <li>⚖️ 体重管理建议</li>
              <li>🥛 营养素补充指导</li>
              <li>🚫 饮食禁忌提醒</li>
              <li>📊 膳食营养分析</li>
            </ul>
            <div class="nutrition-tips">
              <h4>💡 今日营养小贴士</h4>
              <p>均衡饮食是健康的基础，建议每日摄入多样化的食物，包括蔬菜、水果、全谷物、蛋白质和健康脂肪。</p>
            </div>
          </div>
        </div>

        <!-- 消息列表 -->
        <div v-for="(message, index) in messages" :key="index" 
             :class="['message', message.role]">
          <div class="message-avatar">
            <span v-if="message.role === 'user'">👤</span>
            <span v-else>🥗</span>
          </div>
          <div class="message-content">
            <div class="message-text" v-html="formatMessage(message.content)"></div>
            <div class="message-time">{{ formatTime(message.timestamp) }}</div>
          </div>
        </div>

        <!-- 加载状态 -->
        <div v-if="isLoading" class="message assistant">
          <div class="message-avatar">🥗</div>
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
          <h4>🍽️ 热门咨询：</h4>
          <div class="quick-buttons">
            <button @click="sendQuickQuestion('如何科学减肥？')" 
                    :disabled="isLoading">科学减肥</button>
            <button @click="sendQuickQuestion('孕期应该怎么吃？')" 
                    :disabled="isLoading">孕期营养</button>
            <button @click="sendQuickQuestion('老人饮食注意什么？')" 
                    :disabled="isLoading">老人饮食</button>
            <button @click="sendQuickQuestion('高血压饮食禁忌')" 
                    :disabled="isLoading">疾病饮食</button>
            <button @click="sendQuickQuestion('儿童营养补充')" 
                    :disabled="isLoading">儿童营养</button>
          </div>
        </div>
        
        <div class="input-container">
          <textarea 
            v-model="inputMessage"
            @keydown.enter.prevent="handleEnterKey"
            placeholder="请描述您的营养需求，我将为您制定专业的饮食建议..."
            :disabled="isLoading"
            ref="messageInput"
            rows="3"
          ></textarea>
          <button @click="sendMessage" :disabled="!inputMessage.trim() || isLoading" 
                  class="send-btn">
            <span v-if="!isLoading">发送 🍃</span>
            <span v-else>分析中...</span>
          </button>
        </div>
      </footer>
    </main>
  </div>
</template>

<script>
import request from '@/utils/request';
import { HEALTH_API_ENDPOINTS } from '@/config/api';

export default {
  name: 'NutritionChat',
  data() {
    return {
      messages: [],
      inputMessage: '',
      isLoading: false,
      sessionId: null
    }
  },
  mounted() {
    this.initSession();
    this.$refs.messageInput?.focus();
  },
  methods: {
    initSession() {
      this.sessionId = 'nutrition_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
      console.log('🥗 营养咨询会话初始化:', this.sessionId);
    },

    async sendMessage() {
      if (!this.inputMessage.trim() || this.isLoading) return;

      const userMessage = {
        role: 'user',
        content: this.inputMessage.trim(),
        timestamp: new Date()
      };

      this.messages.push(userMessage);
      const messageToSend = this.inputMessage.trim();
      this.inputMessage = '';
      this.isLoading = true;

      try {
        this.$nextTick(() => this.scrollToBottom());

        const response = await request.post(HEALTH_API_ENDPOINTS.nutrition, {
          message: messageToSend,
          sessionId: this.sessionId,
          type: 'nutrition_consultation',
          context: {
            previousMessages: this.messages.slice(-5)
          }
        });

        const assistantMessage = {
          role: 'assistant',
          content: response.message || response.content || '抱歉，我现在无法提供营养建议，请稍后再试。',
          timestamp: new Date()
        };

        this.messages.push(assistantMessage);

      } catch (error) {
        console.error('❌ 营养咨询API调用失败:', error);
        
        const errorMessage = {
          role: 'assistant',
          content: '抱歉，营养咨询服务暂时不可用。请检查网络连接或稍后重试。',
          timestamp: new Date()
        };
        
        this.messages.push(errorMessage);
      } finally {
        this.isLoading = false;
        this.$nextTick(() => {
          this.scrollToBottom();
          this.$refs.messageInput?.focus();
        });
      }
    },

    sendQuickQuestion(question) {
      this.inputMessage = question;
      this.sendMessage();
    },

    handleEnterKey(event) {
      if (event.shiftKey) {
        return;
      }
      this.sendMessage();
    },

    formatMessage(content) {
      return content
        .replace(/\n/g, '<br>')
        .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
        .replace(/\*(.*?)\*/g, '<em>$1</em>');
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
.nutrition-chat-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(135deg, #a8e6cf 0%, #dcedc8 100%);
}

.chat-header {
  background: white;
  padding: 1rem 2rem;
  border-bottom: 1px solid #e0e6ed;
  display: flex;
  align-items: center;
  gap: 1rem;
}

.back-btn {
  color: #FF9800;
  text-decoration: none;
  font-weight: 500;
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
  font-size: 2rem;
}

.header-text h1 {
  font-size: 1.5rem;
  color: #2c3e50;
  margin: 0;
}

.header-text p {
  color: #7f8c8d;
  margin: 0;
  font-size: 0.9rem;
}

.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 1rem 2rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
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
  background: #FF9800;
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

.nutrition-tips {
  background: #e8f5e8;
  border: 1px solid #c8e6c9;
  padding: 1rem;
  border-radius: 8px;
  margin-top: 1rem;
}

.nutrition-tips h4 {
  color: #2e7d32;
  margin: 0 0 0.5rem 0;
  font-size: 1rem;
}

.nutrition-tips p {
  color: #388e3c;
  margin: 0;
  font-size: 0.9rem;
  line-height: 1.5;
}

.message {
  display: flex;
  gap: 1rem;
  max-width: 70%;
}

.message.user {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.message-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2rem;
  flex-shrink: 0;
}

.message.user .message-avatar {
  background: #FF9800;
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
  padding: 1rem;
  border-radius: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  line-height: 1.6;
  word-wrap: break-word;
}

.message.user .message-text {
  background: #FF9800;
  color: white;
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

.chat-input-area {
  background: rgba(255,255,255,0.95);
  backdrop-filter: blur(10px);
  border-top: 1px solid #e0e6ed;
  padding: 1rem 2rem;
}

.quick-questions {
  margin-bottom: 1rem;
}

.quick-questions h4 {
  font-size: 0.9rem;
  color: #7f8c8d;
  margin: 0 0 0.5rem 0;
}

.quick-buttons {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.quick-buttons button {
  background: #fff3e0;
  border: 1px solid #ffcc02;
  padding: 0.5rem 1rem;
  border-radius: 20px;
  font-size: 0.85rem;
  color: #f57c00;
  cursor: pointer;
  transition: all 0.2s;
}

.quick-buttons button:hover:not(:disabled) {
  background: #FF9800;
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
  padding: 1rem;
  font-size: 1rem;
  font-family: inherit;
  resize: none;
  transition: border-color 0.2s;
  background: white;
}

.input-container textarea:focus {
  outline: none;
  border-color: #FF9800;
}

.send-btn {
  background: #FF9800;
  color: white;
  border: none;
  padding: 1rem 1.5rem;
  border-radius: 12px;
  font-size: 1rem;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}

.send-btn:hover:not(:disabled) {
  background: #f57c00;
  transform: translateY(-1px);
}

.send-btn:disabled {
  background: #bdc3c7;
  cursor: not-allowed;
  transform: none;
}

@media (max-width: 768px) {
  .chat-header {
    padding: 1rem;
  }
  
  .chat-messages {
    padding: 1rem;
  }
  
  .chat-input-area {
    padding: 1rem;
  }
  
  .message {
    max-width: 90%;
  }
  
  .quick-buttons {
    justify-content: center;
  }
  
  .input-container {
    flex-direction: column;
    align-items: stretch;
  }
  
  .send-btn {
    align-self: flex-end;
  }
}
</style>