import { defineStore } from 'pinia'
import axios from 'axios'

export const useChatStore = defineStore('chat', {
  state: () => ({
    currentChatId: null,
    currentMessages: [],
    recentChats: [],
    isLoading: false,
    isMobileMenuOpen: false,
    currentApp: 'fitness', // fitness, love, manus
    isTyping: false,
    typingText: '',
    lastMessageId: null
  }),
  
  getters: {
    hasMessages: (state) => state.currentMessages.length > 0,
    lastMessage: (state) => {
      return state.currentMessages[state.currentMessages.length - 1] || null
    },
    messageCount: (state) => state.currentMessages.length,
    canSendMessage: (state) => !state.isLoading && !state.isTyping,
    currentAppName: (state) => {
      const appNames = {
        fitness: 'AI运动助手',
        love: 'AI爱情大师',
        manus: 'AI超级助手'
      }
      return appNames[state.currentApp] || 'AI助手'
    }
  },
  
  actions: {
    async sendMessage(content, attachments = []) {
      if (!content.trim() && attachments.length === 0) return
      
      this.isLoading = true
      
      // 添加用户消息
      const userMessage = {
        id: Date.now() + Math.random(),
        type: 'user',
        content: content.trim(),
        attachments,
        timestamp: new Date(),
        status: 'sent'
      }
      
      this.currentMessages.push(userMessage)
      
      // 为开发环境提供模拟AI回复
      if (import.meta.env.DEV) {
        setTimeout(() => {
          const demoResponses = {
            fitness: [
              '很好的问题！作为您的AI健身助手，我建议您从基础动作开始训练。首先进行热身运动，然后逐步增加训练强度。记住，坚持比强度更重要！💪',
              '根据您的情况，我推荐以下训练计划：\n\n1. 热身：5-10分钟轻松运动\n2. 力量训练：20-30分钟\n3. 有氧运动：15-20分钟\n4. 拉伸放松：5-10分钟\n\n记得循序渐进，听从身体的反应。',
              '健身不仅仅是身体的锻炼，更是意志力的培养。建议您制定合理的目标，记录训练进度，保持营养均衡。如果需要具体的动作指导，我随时为您提供帮助！🏋️‍♂️'
            ],
            love: [
              '在感情关系中，沟通确实是最重要的。真诚地表达自己的感受，同时也要学会倾听对方的心声。每个人都希望被理解和被重视。💕',
              '爱情需要两个人共同努力经营。建议您：\n\n1. 保持开放和诚实的沟通\n2. 尊重彼此的个人空间\n3. 共同创造美好回忆\n4. 在困难时互相支持\n\n记住，真正的爱情是两个独立个体的相互选择和成长。',
              '每段关系都会遇到挑战，这是正常的。重要的是如何一起面对和解决问题。试着从对方的角度思考，用爱心和耐心来处理分歧。相信你们能够找到属于你们的幸福之路！❤️'
            ],
            manus: [
              '作为您的AI超级助手，我很乐意帮助您解决各种问题！无论是学习、工作还是生活中的困惑，我都会尽力为您提供有用的建议和解决方案。🤖',
              '您提到的问题很有意思。让我从多个角度来分析：\n\n• 实用性角度：考虑最直接有效的解决方法\n• 长远角度：思考可持续的发展策略\n• 创新角度：寻找新颖的解决思路\n\n我建议您结合具体情况选择最适合的方案。',
              '智能助手的价值在于为您提供个性化的帮助。我会持续学习和改进，以更好地理解您的需求。如果您有任何疑问或需要进一步的帮助，请随时告诉我！✨'
            ]
          }
          
          const responses = demoResponses[this.currentApp] || demoResponses.manus
          const randomResponse = responses[Math.floor(Math.random() * responses.length)]
          
          const aiMessage = {
            id: Date.now() + Math.random(),
            type: 'ai',
            content: randomResponse,
            timestamp: new Date(),
            status: 'received'
          }
          
          this.currentMessages.push(aiMessage)
          this.isLoading = false
          this.updateChatHistory()
        }, 1000 + Math.random() * 2000) // 1-3秒的随机延迟，模拟真实体验
        return
      }
      this.lastMessageId = userMessage.id
      
      try {
        // 根据当前应用选择不同的API端点
        const endpoint = this.getApiEndpoint()
        
        const response = await axios.post(endpoint, {
          message: content,
          attachments,
          chatId: this.currentChatId,
          app: this.currentApp
        })
        
        // 处理AI响应
        const aiMessage = {
          id: Date.now() + Math.random() + 1,
          type: 'assistant',
          content: response.data.message,
          timestamp: new Date(),
          status: 'received'
        }
        
        this.currentMessages.push(aiMessage)
        this.lastMessageId = aiMessage.id
        
        // 更新聊天记录
        this.updateChatHistory()
        
      } catch (error) {
        console.error('Send message error:', error)
        // 添加错误消息
        const errorMessage = {
          id: Date.now() + Math.random() + 2,
          type: 'error',
          content: '抱歉，发送消息时出现错误，请稍后重试。',
          timestamp: new Date(),
          status: 'error'
        }
        this.currentMessages.push(errorMessage)
      } finally {
        this.isLoading = false
      }
    },
    
    async sendMessageStream(content, attachments = []) {
      if (!content.trim() && attachments.length === 0) return
      
      this.isLoading = true
      
      // 添加用户消息
      const userMessage = {
        id: Date.now() + Math.random(),
        type: 'user',
        content: content.trim(),
        attachments,
        timestamp: new Date(),
        status: 'sent'
      }
      
      this.currentMessages.push(userMessage)
      
      // 添加AI消息占位符
      const aiMessage = {
        id: Date.now() + Math.random() + 1,
        type: 'assistant',
        content: '',
        timestamp: new Date(),
        status: 'typing'
      }
      
      this.currentMessages.push(aiMessage)
      
      try {
        const endpoint = this.getStreamEndpoint()
        const eventSource = new EventSource(`${endpoint}?message=${encodeURIComponent(content)}&app=${this.currentApp}`)
        
        eventSource.onmessage = (event) => {
          const data = JSON.parse(event.data)
          this.handleStreamEvent(data, aiMessage.id)
        }
        
        eventSource.onerror = (error) => {
          console.error('SSE error:', error)
          eventSource.close()
          this.isLoading = false
          
          // 更新消息状态为错误
          const msgIndex = this.currentMessages.findIndex(m => m.id === aiMessage.id)
          if (msgIndex !== -1) {
            this.currentMessages[msgIndex].content = '连接中断，请重新尝试。'
            this.currentMessages[msgIndex].status = 'error'
          }
        }
        
      } catch (error) {
        console.error('Stream error:', error)
        this.isLoading = false
      }
    },
    
    handleStreamEvent(data, messageId) {
      const messageIndex = this.currentMessages.findIndex(m => m.id === messageId)
      if (messageIndex === -1) return
      
      const message = this.currentMessages[messageIndex]
      
      switch (data.type) {
        case 'content':
          message.content += data.content
          break
        case 'complete':
          message.status = 'received'
          this.isLoading = false
          this.updateChatHistory()
          break
        case 'error':
          message.content = data.error || '处理请求时出现错误'
          message.status = 'error'
          this.isLoading = false
          break
      }
    },
    
    async loadChat(chatId) {
      this.isLoading = true
      try {
        const response = await axios.get(`/api/chat/${chatId}`)
        this.currentChatId = chatId
        this.currentMessages = response.data.messages || []
      } catch (error) {
        console.error('Load chat error:', error)
      } finally {
        this.isLoading = false
      }
    },
    
    startNewChat() {
      this.currentChatId = null
      this.currentMessages = []
      this.lastMessageId = null
    },
    
    addMessage(message) {
      this.currentMessages.push({
        ...message,
        id: message.id || Date.now() + Math.random(),
        timestamp: message.timestamp || new Date()
      })
    },
    
    switchApp(appId) {
      this.currentApp = appId
      this.startNewChat()
    },
    
    toggleMobileMenu() {
      this.isMobileMenuOpen = !this.isMobileMenuOpen
    },
    
    closeMobileMenu() {
      this.isMobileMenuOpen = false
    },
    
    async loadRecentChats() {
      // 为开发环境提供演示数据
      if (import.meta.env.DEV) {
        this.recentChats = [
          {
            id: 1,
            title: '如何开始健身训练？',
            app: 'fitness',
            lastMessage: '建议从基础动作开始，每周3-4次训练...',
            updatedAt: new Date(Date.now() - 1000 * 60 * 30),
            messageCount: 8
          },
          {
            id: 2,
            title: '恋爱关系中的沟通技巧',
            app: 'love',
            lastMessage: '有效沟通的关键是倾听和理解...',
            updatedAt: new Date(Date.now() - 1000 * 60 * 60 * 2),
            messageCount: 12
          },
          {
            id: 3,
            title: '学习新技能的最佳方法',
            app: 'manus',
            lastMessage: '制定明确的学习计划很重要...',
            updatedAt: new Date(Date.now() - 1000 * 60 * 60 * 24),
            messageCount: 6
          }
        ]
        return
      }
      
      try {
        const response = await axios.get('/api/chat/recent')
        this.recentChats = response.data.chats || []
      } catch (error) {
        console.error('Load recent chats error:', error)
        this.recentChats = []
      }
    },
    
    async deleteChat(chatId) {
      try {
        await axios.delete(`/api/chat/${chatId}`)
        this.recentChats = this.recentChats.filter(chat => chat.id !== chatId)
        
        if (this.currentChatId === chatId) {
          this.startNewChat()
        }
      } catch (error) {
        console.error('Delete chat error:', error)
      }
    },
    
    updateChatHistory() {
      if (!this.currentMessages.length) return
      
      const lastMessage = this.lastMessage
      const title = this.generateChatTitle()
      
      const chatData = {
        id: this.currentChatId || Date.now(),
        title,
        app: this.currentApp,
        lastMessage: lastMessage?.content?.substring(0, 50) + '...',
        updatedAt: new Date(),
        messageCount: this.currentMessages.length
      }
      
      // 更新或添加到最近聊天列表
      const existingIndex = this.recentChats.findIndex(chat => chat.id === chatData.id)
      if (existingIndex !== -1) {
        this.recentChats[existingIndex] = chatData
      } else {
        this.recentChats.unshift(chatData)
      }
      
      // 限制最近聊天数量
      if (this.recentChats.length > 20) {
        this.recentChats = this.recentChats.slice(0, 20)
      }
      
      this.currentChatId = chatData.id
    },
    
    generateChatTitle() {
      const firstUserMessage = this.currentMessages.find(m => m.type === 'user')
      if (firstUserMessage) {
        return firstUserMessage.content.substring(0, 30) + '...'
      }
      return `${this.currentAppName}对话`
    },
    
    getApiEndpoint() {
      const endpoints = {
        fitness: '/api/fitness/chat',
        love: '/api/love/chat',
        manus: '/api/manus/chat'
      }
      return endpoints[this.currentApp] || '/api/chat'
    },
    
    getStreamEndpoint() {
      const endpoints = {
        fitness: '/api/fitness/chat-stream',
        love: '/api/love/chat-stream',
        manus: '/api/manus/chat-stream'
      }
      return endpoints[this.currentApp] || '/api/chat-stream'
    },
    
    clearCurrentChat() {
      this.currentMessages = []
      this.currentChatId = null
      this.lastMessageId = null
    },
    
    setTyping(isTyping, text = '') {
      this.isTyping = isTyping
      this.typingText = text
    }
  }
})