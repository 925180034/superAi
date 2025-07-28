<template>
  <div class="mcp-status-container" v-if="processingStore.mcpServers.length > 0">
    <!-- MCP状态标题 -->
    <div class="mcp-header">
      <div class="header-left">
        <h4 class="mcp-title">
          <i class="fas fa-network-wired"></i>
          MCP 服务状态
        </h4>
        <div class="mcp-summary">
          <div class="status-badge online">
            <div class="status-dot"></div>
            <span>{{ processingStore.onlineServers }}</span>
          </div>
          <span class="separator">/</span>
          <div class="status-badge total">
            <span>{{ processingStore.mcpServers.length }}</span>
          </div>
          <span class="summary-text">在线</span>
        </div>
      </div>
      
      <div class="header-actions">
        <button 
          class="action-btn"
          @click="refreshServers"
          title="刷新状态"
          :disabled="isRefreshing"
        >
          <i class="fas fa-sync-alt" :class="{ spinning: isRefreshing }"></i>
        </button>
        <button 
          class="action-btn"
          @click="toggleLayout"
          :title="isGridLayout ? '切换到列表视图' : '切换到网格视图'"
        >
          <i :class="isGridLayout ? 'fas fa-list' : 'fas fa-th'"></i>
        </button>
      </div>
    </div>

    <!-- 服务器状态网格/列表 -->
    <div class="mcp-servers" :class="{ 'grid-layout': isGridLayout, 'list-layout': !isGridLayout }">
      <div 
        class="mcp-server"
        v-for="server in processingStore.mcpServers"
        :key="server.name"
        :class="`server-${server.status}`"
        @click="toggleServerDetails(server.name)"
      >
        <!-- 服务器图标 -->
        <div class="server-icon">
          <i :class="getServerIcon(server.type)"></i>
          <div class="server-status-dot" :class="server.status"></div>
        </div>
        
        <!-- 服务器信息 -->
        <div class="server-info">
          <div class="server-name">{{ server.displayName }}</div>
          <div class="server-type">{{ getServerTypeText(server.type) }}</div>
        </div>
        
        <!-- 状态指示器 -->
        <div class="server-status-indicator">
          <div class="status-dot" :class="server.status"></div>
          <span class="status-text">{{ getStatusText(server.status) }}</span>
        </div>

        <!-- 展开的详细信息 -->
        <div 
          class="server-details" 
          v-if="expandedServers.has(server.name)"
          v-show="expandedServers.has(server.name)"
        >
          <!-- 连接信息 -->
          <div class="detail-section" v-if="server.status === 'connected'">
            <div class="detail-row">
              <i class="fas fa-clock"></i>
              <span class="detail-label">连接时长:</span>
              <span class="detail-value">{{ getUptime(server.connectedAt) }}</span>
            </div>
            <div class="detail-row" v-if="server.lastCall">
              <i class="fas fa-history"></i>
              <span class="detail-label">最后调用:</span>
              <span class="detail-value">{{ getLastCall(server.lastCall) }}</span>
            </div>
            <div class="detail-row" v-if="server.version">
              <i class="fas fa-tag"></i>
              <span class="detail-label">版本:</span>
              <span class="detail-value">{{ server.version }}</span>
            </div>
          </div>

          <!-- 错误信息 -->
          <div class="detail-section error-section" v-if="server.status === 'error'">
            <div class="error-header">
              <i class="fas fa-exclamation-circle"></i>
              <span>错误详情</span>
            </div>
            <div class="error-message">
              {{ server.error || '连接失败' }}
            </div>
            <button class="retry-btn" @click.stop="retryConnection(server)">
              <i class="fas fa-redo"></i>
              重新连接
            </button>
          </div>

          <!-- 连接中状态 -->
          <div class="detail-section connecting-section" v-if="server.status === 'connecting'">
            <div class="connecting-info">
              <div class="loading-spinner"></div>
              <span>正在连接服务器...</span>
            </div>
            <div class="connecting-progress">
              <div class="progress-bar">
                <div class="progress-fill" :style="{ width: getConnectingProgress(server) + '%' }"></div>
              </div>
            </div>
          </div>

          <!-- 服务器操作 -->
          <div class="server-actions" v-if="server.status === 'connected'">
            <button class="server-action-btn" @click.stop="testConnection(server)">
              <i class="fas fa-check"></i>
              测试连接
            </button>
            <button class="server-action-btn danger" @click.stop="disconnectServer(server)">
              <i class="fas fa-plug"></i>
              断开连接
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 全局状态摘要 -->
    <div class="mcp-summary-footer" v-if="showSummary">
      <div class="summary-stats">
        <div class="stat-item">
          <span class="stat-value">{{ processingStore.onlineServers }}</span>
          <span class="stat-label">在线</span>
        </div>
        <div class="stat-item">
          <span class="stat-value">{{ processingStore.offlineServers }}</span>
          <span class="stat-label">离线</span>
        </div>
        <div class="stat-item" v-if="totalCalls > 0">
          <span class="stat-value">{{ totalCalls }}</span>
          <span class="stat-label">总调用</span>
        </div>
      </div>
      
      <div class="summary-actions">
        <button class="summary-btn" @click="connectAllServers" v-if="processingStore.offlineServers > 0">
          <i class="fas fa-power-off"></i>
          连接所有
        </button>
        <button class="summary-btn" @click="exportStatus">
          <i class="fas fa-download"></i>
          导出状态
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useProcessingStore } from '@/stores/processing'

const props = defineProps({
  showSummary: {
    type: Boolean,
    default: true
  },
  autoRefresh: {
    type: Boolean,
    default: false
  },
  refreshInterval: {
    type: Number,
    default: 30000
  }
})

const processingStore = useProcessingStore()

// 响应式状态
const isGridLayout = ref(true)
const isRefreshing = ref(false)
const expandedServers = ref(new Set())

// 计算属性
const totalCalls = computed(() => {
  return processingStore.mcpServers.reduce((total, server) => {
    return total + (server.callCount || 0)
  }, 0)
})

// 获取服务器图标
const getServerIcon = (type) => {
  const iconMap = {
    fitness: 'fas fa-dumbbell',
    database: 'fas fa-database',
    api: 'fas fa-plug',
    web: 'fas fa-globe',
    file: 'fas fa-folder',
    calculation: 'fas fa-calculator',
    ai: 'fas fa-robot',
    unknown: 'fas fa-server'
  }
  return iconMap[type] || iconMap.unknown
}

// 获取服务器类型文本
const getServerTypeText = (type) => {
  const typeMap = {
    fitness: '健身服务',
    database: '数据库服务',
    api: 'API服务',
    web: '网络服务',
    file: '文件服务',
    calculation: '计算服务',
    ai: 'AI服务',
    unknown: '未知服务'
  }
  return typeMap[type] || typeMap.unknown
}

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    connected: '已连接',
    disconnected: '已断开',
    connecting: '连接中',
    error: '错误',
    timeout: '超时'
  }
  return statusMap[status] || status
}

// 获取运行时间
const getUptime = (connectedAt) => {
  if (!connectedAt) return '未知'
  
  const now = new Date()
  const connected = new Date(connectedAt)
  const diff = now - connected
  
  if (diff < 60000) return '刚刚连接'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时`
  return `${Math.floor(diff / 86400000)}天`
}

// 获取最后调用时间
const getLastCall = (lastCall) => {
  if (!lastCall) return '无调用记录'
  
  const now = new Date()
  const call = new Date(lastCall)
  const diff = now - call
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  return call.toLocaleDateString()
}

// 获取连接进度
const getConnectingProgress = (server) => {
  // 模拟连接进度
  const elapsed = Date.now() - (server.connectStartTime || Date.now())
  return Math.min(90, Math.floor(elapsed / 100))
}

// 切换布局
const toggleLayout = () => {
  isGridLayout.value = !isGridLayout.value
}

// 切换服务器详情
const toggleServerDetails = (serverName) => {
  if (expandedServers.value.has(serverName)) {
    expandedServers.value.delete(serverName)
  } else {
    expandedServers.value.add(serverName)
  }
}

// 刷新服务器状态
const refreshServers = async () => {
  isRefreshing.value = true
  try {
    // 模拟刷新操作
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // 这里应该调用实际的刷新逻辑
    processingStore.addLog('info', 'MCP服务器状态已刷新')
  } catch (error) {
    processingStore.addLog('error', `刷新MCP状态失败: ${error.message}`)
  } finally {
    isRefreshing.value = false
  }
}

// 重试连接
const retryConnection = async (server) => {
  processingStore.updateMCPServer(server.name, { 
    status: 'connecting',
    connectStartTime: Date.now(),
    error: null
  })
  
  try {
    // 模拟重连操作
    await new Promise(resolve => setTimeout(resolve, 2000))
    
    processingStore.updateMCPServer(server.name, {
      status: 'connected',
      connectedAt: new Date(),
      connectStartTime: null
    })
    
    processingStore.addLog('success', `MCP服务器 ${server.displayName} 重连成功`)
  } catch (error) {
    processingStore.updateMCPServer(server.name, {
      status: 'error',
      error: error.message,
      connectStartTime: null
    })
    
    processingStore.addLog('error', `MCP服务器 ${server.displayName} 重连失败`)
  }
}

// 测试连接
const testConnection = async (server) => {
  processingStore.addLog('info', `正在测试 ${server.displayName} 连接...`)
  
  try {
    // 模拟测试操作
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    processingStore.updateMCPServer(server.name, {
      lastCall: new Date()
    })
    
    processingStore.addLog('success', `${server.displayName} 连接测试成功`)
  } catch (error) {
    processingStore.addLog('error', `${server.displayName} 连接测试失败: ${error.message}`)
  }
}

// 断开服务器
const disconnectServer = async (server) => {
  if (!confirm(`确定要断开 ${server.displayName} 的连接吗？`)) return
  
  processingStore.updateMCPServer(server.name, {
    status: 'disconnected',
    connectedAt: null
  })
  
  processingStore.addLog('info', `${server.displayName} 已断开连接`)
}

// 连接所有服务器
const connectAllServers = async () => {
  const offlineServers = processingStore.mcpServers.filter(s => s.status !== 'connected')
  
  for (const server of offlineServers) {
    await retryConnection(server)
  }
}

// 导出状态
const exportStatus = () => {
  const statusData = processingStore.mcpServers.map(server => ({
    name: server.name,
    displayName: server.displayName,
    type: server.type,
    status: server.status,
    connectedAt: server.connectedAt,
    lastCall: server.lastCall,
    error: server.error
  }))
  
  const blob = new Blob([JSON.stringify(statusData, null, 2)], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `mcp-status-${new Date().toISOString().split('T')[0]}.json`
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
  
  processingStore.addLog('info', 'MCP状态导出完成')
}
</script>

<style scoped>
.mcp-status-container {
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  margin: var(--spacing-md) var(--spacing-lg);
  overflow: hidden;
}

.mcp-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--spacing-md) var(--spacing-lg);
  background: var(--bg-primary);
  border-bottom: 1px solid var(--border-light);
}

.header-left {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.mcp-title {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  font-size: var(--font-lg);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
  margin: 0;
}

.mcp-title i {
  color: var(--info-color);
}

.mcp-summary {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  font-size: var(--font-sm);
}

.status-badge {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  padding: 2px var(--spacing-xs);
  border-radius: var(--radius-sm);
  font-weight: var(--font-medium);
}

.status-badge.online {
  background: rgba(16, 185, 129, 0.1);
  color: var(--success-color);
}

.status-badge.total {
  color: var(--text-primary);
}

.separator {
  color: var(--text-light);
}

.summary-text {
  color: var(--text-secondary);
}

.header-actions {
  display: flex;
  gap: var(--spacing-xs);
}

.action-btn {
  width: 32px;
  height: 32px;
  border: none;
  border-radius: var(--radius-md);
  background: var(--bg-secondary);
  color: var(--text-secondary);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--duration-fast) var(--ease-in-out);
}

.action-btn:hover:not(:disabled) {
  background: var(--primary-color);
  color: var(--text-white);
}

.action-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.spinning {
  animation: spin 1s linear infinite;
}

.mcp-servers {
  padding: var(--spacing-lg);
}

.mcp-servers.grid-layout {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: var(--spacing-md);
}

.mcp-servers.list-layout {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

.mcp-server {
  background: var(--bg-primary);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-lg);
  padding: var(--spacing-md);
  cursor: pointer;
  transition: all var(--duration-fast) var(--ease-in-out);
  position: relative;
}

.mcp-server:hover {
  border-color: var(--border-color);
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

.server-connected {
  border-left: 4px solid var(--success-color);
}

.server-error {
  border-left: 4px solid var(--danger-color);
}

.server-connecting {
  border-left: 4px solid var(--info-color);
}

.server-disconnected {
  border-left: 4px solid var(--text-light);
}

.grid-layout .mcp-server {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.list-layout .mcp-server {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.server-icon {
  position: relative;
  width: 48px;
  height: 48px;
  background: var(--bg-secondary);
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--font-xl);
  color: var(--text-secondary);
  flex-shrink: 0;
}

.server-status-dot {
  position: absolute;
  top: -4px;
  right: -4px;
  width: 16px;
  height: 16px;
  border-radius: var(--radius-full);
  border: 2px solid var(--bg-primary);
}

.server-status-dot.connected {
  background: var(--success-color);
}

.server-status-dot.error {
  background: var(--danger-color);
}

.server-status-dot.connecting {
  background: var(--info-color);
  animation: pulse 2s infinite;
}

.server-status-dot.disconnected {
  background: var(--text-light);
}

.server-info {
  flex: 1;
}

.server-name {
  font-size: var(--font-base);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
  margin-bottom: var(--spacing-xs);
}

.server-type {
  font-size: var(--font-sm);
  color: var(--text-secondary);
}

.server-status-indicator {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  flex-shrink: 0;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: var(--radius-full);
}

.status-dot.connected {
  background: var(--success-color);
}

.status-dot.error {
  background: var(--danger-color);
}

.status-dot.connecting {
  background: var(--info-color);
  animation: pulse 2s infinite;
}

.status-dot.disconnected {
  background: var(--text-light);
}

.status-text {
  font-size: var(--font-sm);
  font-weight: var(--font-medium);
}

.server-details {
  margin-top: var(--spacing-md);
  padding-top: var(--spacing-md);
  border-top: 1px solid var(--border-light);
  animation: slideDown var(--duration-normal) var(--ease-out);
}

.detail-section {
  margin-bottom: var(--spacing-md);
}

.detail-section:last-child {
  margin-bottom: 0;
}

.detail-row {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  margin-bottom: var(--spacing-xs);
  font-size: var(--font-sm);
}

.detail-row:last-child {
  margin-bottom: 0;
}

.detail-row i {
  width: 16px;
  color: var(--text-secondary);
}

.detail-label {
  color: var(--text-secondary);
  min-width: 60px;
}

.detail-value {
  color: var(--text-primary);
  font-weight: var(--font-medium);
}

.error-section {
  background: rgba(239, 68, 68, 0.05);
  border: 1px solid var(--danger-color);
  border-radius: var(--radius-md);
  padding: var(--spacing-md);
}

.error-header {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  font-size: var(--font-sm);
  font-weight: var(--font-medium);
  color: var(--danger-color);
  margin-bottom: var(--spacing-sm);
}

.error-message {
  font-size: var(--font-sm);
  color: var(--danger-color);
  margin-bottom: var(--spacing-md);
  white-space: pre-wrap;
}

.retry-btn {
  background: var(--danger-color);
  color: var(--text-white);
  border: none;
  padding: var(--spacing-xs) var(--spacing-md);
  border-radius: var(--radius-md);
  font-size: var(--font-sm);
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  transition: all var(--duration-fast) var(--ease-in-out);
}

.retry-btn:hover {
  background: rgba(239, 68, 68, 0.8);
}

.connecting-section {
  text-align: center;
}

.connecting-info {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-md);
  font-size: var(--font-sm);
  color: var(--info-color);
}

.connecting-progress {
  margin-top: var(--spacing-sm);
}

.progress-bar {
  height: 4px;
  background: var(--border-light);
  border-radius: var(--radius-full);
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: var(--info-color);
  border-radius: var(--radius-full);
  transition: width var(--duration-normal) var(--ease-out);
}

.server-actions {
  display: flex;
  gap: var(--spacing-sm);
  margin-top: var(--spacing-md);
}

.server-action-btn {
  flex: 1;
  padding: var(--spacing-xs) var(--spacing-sm);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  background: var(--bg-secondary);
  color: var(--text-primary);
  font-size: var(--font-sm);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-xs);
  transition: all var(--duration-fast) var(--ease-in-out);
}

.server-action-btn:hover {
  background: var(--primary-color);
  color: var(--text-white);
  border-color: var(--primary-color);
}

.server-action-btn.danger:hover {
  background: var(--danger-color);
  border-color: var(--danger-color);
}

.mcp-summary-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--spacing-md) var(--spacing-lg);
  background: var(--bg-primary);
  border-top: 1px solid var(--border-light);
}

.summary-stats {
  display: flex;
  gap: var(--spacing-lg);
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--spacing-xs);
}

.stat-value {
  font-size: var(--font-xl);
  font-weight: var(--font-bold);
  color: var(--text-primary);
}

.stat-label {
  font-size: var(--font-xs);
  color: var(--text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.summary-actions {
  display: flex;
  gap: var(--spacing-sm);
}

.summary-btn {
  padding: var(--spacing-sm) var(--spacing-md);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  background: var(--bg-secondary);
  color: var(--text-primary);
  font-size: var(--font-sm);
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  transition: all var(--duration-fast) var(--ease-in-out);
}

.summary-btn:hover {
  background: var(--primary-color);
  color: var(--text-white);
  border-color: var(--primary-color);
}

/* 移动端适配 */
@media (max-width: 767px) {
  .mcp-status-container {
    margin: var(--spacing-sm) var(--spacing-md);
  }
  
  .mcp-header {
    padding: var(--spacing-sm) var(--spacing-md);
    flex-direction: column;
    gap: var(--spacing-sm);
    align-items: flex-start;
  }
  
  .header-left {
    width: 100%;
    justify-content: space-between;
  }
  
  .mcp-servers.grid-layout {
    grid-template-columns: 1fr;
  }
  
  .mcp-servers {
    padding: var(--spacing-md);
  }
  
  .mcp-summary-footer {
    flex-direction: column;
    gap: var(--spacing-md);
    align-items: stretch;
  }
  
  .summary-stats {
    justify-content: space-around;
  }
  
  .summary-actions {
    justify-content: center;
  }
}
</style>