import { defineStore } from 'pinia'

export const useProcessingStore = defineStore('processing', {
  state: () => ({
    // 思考状态
    isThinking: false,
    thinkingText: '',
    thinkingProgress: 0,
    thinkingStage: '', // analyzing, planning, executing, responding
    
    // 工具调用
    tools: [],
    activeToolId: null,
    toolsCompleted: 0,
    
    // MCP服务器状态
    mcpServers: [
      {
        name: 'fitness-tools',
        displayName: '健身工具服务',
        type: 'fitness',
        status: 'connected', // connected, disconnected, error, connecting
        connectedAt: new Date(),
        lastCall: null,
        error: null
      },
      {
        name: 'manus-tools',
        displayName: '超级助手工具服务',
        type: 'manus',
        status: 'connected',
        connectedAt: new Date(),
        lastCall: null,
        error: null
      },
      {
        name: 'database',
        displayName: '数据库服务',
        type: 'database',
        status: 'connected',
        connectedAt: new Date(),
        lastCall: null,
        error: null
      }
    ],
    
    // 实时日志
    logs: [],
    maxLogs: 100,
    autoScroll: true,
    logFilters: ['info', 'warning', 'error', 'debug'],
    activeFilters: ['info', 'warning', 'error'],
    
    // 整体处理状态
    isProcessing: false,
    processingStep: '',
    processingError: null
  }),
  
  getters: {
    // 思考相关
    showThinking: (state) => state.isThinking && state.thinkingText,
    thinkingProgressPercentage: (state) => Math.min(100, Math.max(0, state.thinkingProgress)),
    
    // 工具相关
    hasActiveTools: (state) => state.tools.length > 0,
    toolsProgress: (state) => {
      if (state.tools.length === 0) return 0
      return Math.round((state.toolsCompleted / state.tools.length) * 100)
    },
    pendingTools: (state) => state.tools.filter(tool => tool.status === 'pending'),
    runningTools: (state) => state.tools.filter(tool => tool.status === 'running'),
    completedTools: (state) => state.tools.filter(tool => tool.status === 'completed'),
    errorTools: (state) => state.tools.filter(tool => tool.status === 'error'),
    
    // MCP相关
    onlineServers: (state) => state.mcpServers.filter(server => server.status === 'connected').length,
    offlineServers: (state) => state.mcpServers.filter(server => server.status !== 'connected').length,
    hasServerErrors: (state) => state.mcpServers.some(server => server.status === 'error'),
    
    // 日志相关
    filteredLogs: (state) => {
      return state.logs.filter(log => state.activeFilters.includes(log.level))
    },
    hasLogs: (state) => state.logs.length > 0,
    recentLogs: (state) => state.logs.slice(-10),
    
    // 处理状态
    isActive: (state) => state.isProcessing || state.isThinking || state.tools.some(t => t.status === 'running')
  },
  
  actions: {
    // 思考状态管理
    startThinking(text = '', stage = 'analyzing') {
      this.isThinking = true
      this.thinkingText = text
      this.thinkingStage = stage
      this.thinkingProgress = 0
      this.addLog('info', `开始AI思考过程: ${stage}`)
    },
    
    updateThinking(text, progress = null, stage = null) {
      if (this.isThinking) {
        this.thinkingText = text
        if (progress !== null) {
          this.thinkingProgress = Math.min(100, Math.max(0, progress))
        }
        if (stage) {
          this.thinkingStage = stage
        }
      }
    },
    
    completeThinking() {
      this.isThinking = false
      this.thinkingText = ''
      this.thinkingProgress = 100
      this.addLog('info', 'AI思考过程完成')
    },
    
    // 工具管理
    addTool(toolData) {
      const tool = {
        id: toolData.id || Date.now() + Math.random(),
        name: toolData.name,
        displayName: toolData.displayName || toolData.name,
        description: toolData.description || '',
        parameters: toolData.parameters || {},
        status: 'pending',
        result: null,
        error: null,
        startTime: null,
        endTime: null,
        executionTime: null,
        ...toolData
      }
      
      this.tools.push(tool)
      this.addLog('info', `添加工具: ${tool.displayName}`)
      return tool.id
    },
    
    updateTool(toolId, updates) {
      const toolIndex = this.tools.findIndex(tool => tool.id === toolId)
      if (toolIndex === -1) return
      
      const tool = this.tools[toolIndex]
      const oldStatus = tool.status
      
      // 更新工具状态
      Object.assign(tool, updates)
      
      // 处理状态变化
      if (updates.status && updates.status !== oldStatus) {
        switch (updates.status) {
          case 'running':
            tool.startTime = new Date()
            this.activeToolId = toolId
            this.addLog('info', `开始执行工具: ${tool.displayName}`)
            break
          case 'completed':
            tool.endTime = new Date()
            if (tool.startTime) {
              tool.executionTime = tool.endTime - tool.startTime
            }
            this.toolsCompleted++
            if (this.activeToolId === toolId) {
              this.activeToolId = null
            }
            this.addLog('success', `工具执行完成: ${tool.displayName}`)
            break
          case 'error':
            tool.endTime = new Date()
            if (this.activeToolId === toolId) {
              this.activeToolId = null
            }
            this.addLog('error', `工具执行失败: ${tool.displayName} - ${tool.error}`)
            break
        }
      }
    },
    
    removeTool(toolId) {
      const toolIndex = this.tools.findIndex(tool => tool.id === toolId)
      if (toolIndex !== -1) {
        const tool = this.tools[toolIndex]
        this.tools.splice(toolIndex, 1)
        this.addLog('info', `移除工具: ${tool.displayName}`)
      }
    },
    
    clearTools() {
      this.tools = []
      this.activeToolId = null
      this.toolsCompleted = 0
      this.addLog('info', '清空所有工具')
    },
    
    // MCP管理
    updateMCPServer(serverName, updates) {
      const serverIndex = this.mcpServers.findIndex(server => server.name === serverName)
      if (serverIndex !== -1) {
        const server = this.mcpServers[serverIndex]
        const oldStatus = server.status
        
        Object.assign(server, updates)
        
        if (updates.status && updates.status !== oldStatus) {
          this.addLog('info', `MCP服务器 ${server.displayName} 状态变更: ${oldStatus} -> ${updates.status}`)
        }
        
        // 记录最后调用时间
        if (updates.lastCall) {
          server.lastCall = new Date()
        }
      }
    },
    
    addMCPServer(serverData) {
      const server = {
        name: serverData.name,
        displayName: serverData.displayName || serverData.name,
        type: serverData.type || 'unknown',
        status: 'connecting',
        connectedAt: null,
        lastCall: null,
        error: null,
        ...serverData
      }
      
      this.mcpServers.push(server)
      this.addLog('info', `添加MCP服务器: ${server.displayName}`)
    },
    
    // 日志管理
    addLog(level, message, source = null) {
      const log = {
        id: Date.now() + Math.random(),
        level, // info, warning, error, debug, success
        message,
        source,
        timestamp: new Date()
      }
      
      this.logs.push(log)
      
      // 限制日志数量
      if (this.logs.length > this.maxLogs) {
        this.logs = this.logs.slice(-this.maxLogs)
      }
    },
    
    clearLogs() {
      this.logs = []
      this.addLog('info', '日志已清空')
    },
    
    toggleLogFilter(level) {
      const index = this.activeFilters.indexOf(level)
      if (index > -1) {
        this.activeFilters.splice(index, 1)
      } else {
        this.activeFilters.push(level)
      }
    },
    
    toggleAutoScroll() {
      this.autoScroll = !this.autoScroll
    },
    
    exportLogs() {
      const logsData = this.logs.map(log => ({
        timestamp: log.timestamp.toISOString(),
        level: log.level,
        message: log.message,
        source: log.source
      }))
      
      const blob = new Blob([JSON.stringify(logsData, null, 2)], { type: 'application/json' })
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = `ai-assistant-logs-${new Date().toISOString().split('T')[0]}.json`
      document.body.appendChild(a)
      a.click()
      document.body.removeChild(a)
      URL.revokeObjectURL(url)
      
      this.addLog('info', '日志导出完成')
    },
    
    // 整体处理状态管理
    startProcessing(step = '') {
      this.isProcessing = true
      this.processingStep = step
      this.processingError = null
      this.addLog('info', `开始处理: ${step}`)
    },
    
    updateProcessingStep(step) {
      this.processingStep = step
      this.addLog('info', `处理步骤: ${step}`)
    },
    
    completeProcessing() {
      this.isProcessing = false
      this.processingStep = ''
      this.processingError = null
      this.addLog('info', '处理完成')
    },
    
    errorProcessing(error) {
      this.isProcessing = false
      this.processingError = error
      this.addLog('error', `处理错误: ${error}`)
    },
    
    // 重置所有状态
    reset() {
      this.isThinking = false
      this.thinkingText = ''
      this.thinkingProgress = 0
      this.thinkingStage = ''
      
      this.tools = []
      this.activeToolId = null
      this.toolsCompleted = 0
      
      this.isProcessing = false
      this.processingStep = ''
      this.processingError = null
      
      this.addLog('info', '处理状态已重置')
    },
    
    // 完整的处理流程
    async complete() {
      this.completeThinking()
      this.completeProcessing()
      
      // 等待一段时间后清理
      setTimeout(() => {
        if (!this.isActive) {
          this.clearTools()
        }
      }, 5000)
    }
  }
})