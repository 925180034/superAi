<template>
  <div class="exercise-chat-container">
    <!-- 页面头部 -->
    <header class="chat-header">
      <router-link to="/" class="back-btn">
        <span>←</span> 返回首页
      </router-link>
      <div class="header-info">
        <div class="header-icon">💪</div>
        <div class="header-text">
          <h1>运动健身建议</h1>
          <p>科学运动方案，健康体魄塑造</p>
        </div>
      </div>
    </header>

    <!-- 聊天区域 -->
    <main class="chat-main">
      <div class="chat-messages" ref="messagesContainer">
        <!-- 欢迎消息 -->
        <div v-if="messages.length === 0" class="welcome-message">
          <div class="welcome-avatar">💪</div>
          <div class="welcome-content">
            <h3>欢迎来到运动健身中心！</h3>
            <p>我是您的专业健身教练，可以为您提供：</p>
            <ul>
              <li>🏃‍♂️ 个性化运动计划</li>
              <li>💪 力量训练指导</li>
              <li>🧘‍♀️ 柔韧性训练</li>
              <li>🍎 运动营养建议</li>
              <li>🩹 运动损伤预防</li>
            </ul>
            <div class="exercise-tips">
              <h4>⚡ 今日运动小贴士</h4>
              <p>运动是良医！建议每周进行至少150分钟中等强度有氧运动，搭配2-3次力量训练。</p>
            </div>
          </div>
        </div>

        <!-- 消息列表 -->
        <div v-for="(message, index) in messages" :key="index" 
             :class="['message', message.role]">
          <div class="message-avatar">
            <span v-if="message.role === 'user'">👤</span>
            <span v-else>💪</span>
          </div>
          <div class="message-content">
            <div class="message-text" v-html="formatMessage(message.content)"></div>
            <div class="message-time">{{ formatTime(message.timestamp) }}</div>
          </div>
        </div>

        <!-- 加载状态 -->
        <div v-if="isLoading" class="message assistant">
          <div class="message-avatar">💪</div>
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
          <h4>🏃‍♂️ 热门咨询：</h4>
          <div class="quick-buttons">
            <button @click="sendQuickQuestion('新手应该如何开始健身？')" 
                    :disabled="isLoading">新手指南</button>
            <button @click="sendQuickQuestion('如何制定减脂计划？')" 
                    :disabled="isLoading">减脂计划</button>
            <button @click="sendQuickQuestion('居家无器械训练方案')" 
                    :disabled="isLoading">居家训练</button>
            <button @click="sendQuickQuestion('运动后如何拉伸？')" 
                    :disabled="isLoading">拉伸指导</button>
          </div>
        </div>
        
        <div class="input-container">
          <textarea 
            v-model="inputMessage"
            @keydown.enter.prevent="handleEnterKey"
            placeholder="请描述您的运动需求，我将为您制定专业的健身方案..."
            :disabled="isLoading"
            ref="messageInput"
            rows="3"
          ></textarea>
          <button @click="sendMessage" :disabled="!inputMessage.trim() || isLoading" 
                  class="send-btn">
            <span v-if="!isLoading">发送 ⚡</span>
            <span v-else>规划中...</span>
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
  name: 'ExerciseChat',
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
      this.sessionId = 'exercise_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
      console.log('💪 运动咨询会话初始化:', this.sessionId);
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

        const response = await request.post(HEALTH_API_ENDPOINTS.exercise, {
          message: messageToSend,
          sessionId: this.sessionId,
          type: 'exercise_consultation',
          context: {
            previousMessages: this.messages.slice(-5)
          }
        });

        const assistantMessage = {
          role: 'assistant',
          content: response.message || response.content || '抱歉，我现在无法提供运动建议，请稍后再试。',
          timestamp: new Date()
        };

        this.messages.push(assistantMessage);

      } catch (error) {
        console.error('❌ 运动咨询API调用失败:', error);
        
        const errorMessage = {
          role: 'assistant',
          content: '抱歉，运动咨询服务暂时不可用。请检查网络连接或稍后重试。',
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
.exercise-chat-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(135deg, #90caf9 0%, #bbdefb 100%);
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
  color: #2196F3;
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
  background: #2196F3;
  border-radius: 50%;
  flex-shrink: 0;
}

.welcome-content {
  background: white;
  padding: 1.5rem;
  border-radius: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.exercise-tips {
  background: #e3f2fd;
  border: 1px solid #90caf9;
  padding: 1rem;
  border-radius: 8px;
  margin-top: 1rem;
}

.exercise-tips h4 {
  color: #1565c0;
  margin: 0 0 0.5rem 0;
  font-size: 1rem;
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
  background: #2196F3;
  color: white;
}

.message.assistant .message-avatar {
  background: #4CAF50;
  color: white;
}

.message.user .message-text {
  background: #2196F3;
  color: white;
}

.send-btn {
  background: #2196F3;
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
  background: #1976d2;
  transform: translateY(-1px);
}

.input-container textarea:focus {
  outline: none;
  border-color: #2196F3;
}

/* 其他样式与 NutritionChat 类似 */
.message {
  display: flex;
  gap: 1rem;
  max-width: 70%;
}

.message.user {
  align-self: flex-end;
  flex-direction: row-reverse;
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
  background: #e3f2fd;
  border: 1px solid #90caf9;
  padding: 0.5rem 1rem;
  border-radius: 20px;
  font-size: 0.85rem;
  color: #1565c0;
  cursor: pointer;
  transition: all 0.2s;
}

.quick-buttons button:hover:not(:disabled) {
  background: #2196F3;
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

.send-btn:disabled {
  background: #bdc3c7;
  cursor: not-allowed;
  transform: none;
}
</style>