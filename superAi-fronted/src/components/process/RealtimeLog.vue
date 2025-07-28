<template>
  <div class="realtime-log-container" v-if="processingStore.hasLogs">
    <!-- 日志标题栏 -->
    <div class="log-header">
      <div class="header-left">
        <h4 class="log-title">
          <i class="fas fa-terminal"></i>
          实时执行日志
        </h4>
        <div class="log-summary">
          <span class="log-count">{{ processingStore.filteredLogs.length }}</span>
          <span class="summary-text">条记录</span>
        </div>
      </div>
      
      <div class="log-controls">
        <button 
          class="log-control-btn" 
          @click="toggleAutoScroll" 
          :class="{ active: processingStore.autoScroll }"
          title="自动滚动"
        >
          <i class="fas fa-arrow-down"></i>
          <span class="btn-text">自动滚动</span>
        </button>
        <button 
          class="log-control-btn" 
          @click="clearLogs"
          title="清空日志"
        >
          <i class="fas fa-trash"></i>
          <span class="btn-text">清空</span>
        </button>
        <button 
          class="log-control-btn" 
          @click="exportLogs"
          title="导出日志"
        >
          <i class="fas fa-download"></i>
          <span class="btn-text">导出</span>
        </button>
        <button 
          class="log-control-btn" 
          @click="toggleFilters"
          :class="{ active: showFilters }"
          title="过滤器"
        >
          <i class="fas fa-filter"></i>
          <span class="btn-text">过滤</span>
        </button>
      </div>
    </div>

    <!-- 日志过滤器 -->
    <div class="log-filters" v-if="showFilters">
      <div class="filter-section">
        <span class="filter-label">日志级别:</span>
        <div class="filter-buttons">
          <button 
            class="filter-btn"
            v-for="level in processingStore.logFilters"
            :key="level"
            :class="{ 
              active: processingStore.activeFilters.includes(level),
              [level]: true 
            }"
            @click="toggleFilter(level)"
          >
            <i :class="getLevelIcon(level)"></i>
            <span>{{ level.toUpperCase() }}</span>
            <span class="filter-count">({{ getLevelCount(level) }})</span>
          </button>
        </div>
      </div>
      
      <div class="filter-section">
        <span class="filter-label">搜索:</span>
        <div class="search-input-wrapper">
          <input 
            type="text" 
            v-model="searchQuery"
            placeholder="搜索日志内容..."
            class="search-input"
          >
          <i class="fas fa-search search-icon"></i>
        </div>
      </div>
    </div>

    <!-- 日志内容 -->
    <div class="log-content" ref="logContainer" @scroll="handleScroll">
      <div class="log-virtual-container" :style="{ height: virtualHeight + 'px' }">
        <div 
          class="log-line"
          v-for="(log, index) in visibleLogs"
          :key="`log-${log.id}`"
          :class="`log-level-${log.level}`"
          :style="{ transform: `translateY(${getLogOffset(index)}px)` }"
        >
          <!-- 日志时间戳 -->
          <span class="log-timestamp">{{ formatTimestamp(log.timestamp) }}</span>
          
          <!-- 日志级别徽章 -->
          <span class="log-level-badge" :class="`level-${log.level}`">
            <i :class="getLevelIcon(log.level)"></i>
            <span class="level-text">{{ log.level.toUpperCase() }}</span>
          </span>
          
          <!-- 日志来源 -->
          <span class="log-source" v-if="log.source">[{{ log.source }}]</span>
          
          <!-- 日志消息 -->
          <span class="log-message" v-html="highlightSearch(log.message)"></span>
          
          <!-- 日志操作 -->
          <div class="log-actions">
            <button 
              class="log-action-btn" 
              @click="copyLogLine(log)"
              title="复制日志"
            >
              <i class="fas fa-copy"></i>
            </button>
            <button 
              class="log-action-btn" 
              @click="showLogDetail(log)"
              title="查看详情"
              v-if="log.details"
            >
              <i class="fas fa-info-circle"></i>
            </button>
          </div>
        </div>
      </div>
      
      <!-- 空状态 -->
      <div class="log-empty" v-if="filteredLogs.length === 0">
        <i class="fas fa-inbox"></i>
        <p>{{ searchQuery ? '没有找到匹配的日志' : '暂无日志记录' }}</p>
      </div>
      
      <!-- 加载更多 -->
      <div class="log-loader" v-if="isLoading">
        <div class="loading-spinner"></div>
        <span>加载中...</span>
      </div>
    </div>

    <!-- 日志统计信息 -->
    <div class="log-footer" v-if="showStats">
      <div class="log-stats">
        <div class="stat-item" v-for="level in ['info', 'warning', 'error']" :key="level">
          <span class="stat-icon" :class="`level-${level}`">
            <i :class="getLevelIcon(level)"></i>
          </span>
          <span class="stat-count">{{ getLevelCount(level) }}</span>
        </div>
      </div>
      
      <div class="log-performance">
        <span class="perf-item">
          总计: {{ processingStore.logs.length }} 条
        </span>
        <span class="perf-item">
          显示: {{ processingStore.filteredLogs.length }} 条
        </span>
        <span class="perf-item" v-if="lastUpdateTime">
          更新: {{ formatTimestamp(lastUpdateTime) }}
        </span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick, onMounted, onUnmounted } from 'vue'
import { useProcessingStore } from '@/stores/processing'

const props = defineProps({
  maxVisibleLogs: {
    type: Number,
    default: 100
  },
  showStats: {
    type: Boolean,
    default: true
  },
  virtualScrolling: {
    type: Boolean,
    default: true
  },
  lineHeight: {
    type: Number,
    default: 32
  }
})

const processingStore = useProcessingStore()

// 响应式数据
const showFilters = ref(false)
const searchQuery = ref('')
const isLoading = ref(false)
const lastUpdateTime = ref(null)
const scrollTop = ref(0)

// 引用
const logContainer = ref(null)

// 过滤后的日志
const filteredLogs = computed(() => {
  let logs = processingStore.filteredLogs
  
  if (searchQuery.value.trim()) {
    const query = searchQuery.value.toLowerCase()
    logs = logs.filter(log => 
      log.message.toLowerCase().includes(query) ||
      (log.source && log.source.toLowerCase().includes(query))
    )
  }
  
  return logs
})

// 虚拟滚动相关
const startIndex = computed(() => {
  if (!props.virtualScrolling) return 0
  return Math.floor(scrollTop.value / props.lineHeight)
})

const endIndex = computed(() => {
  if (!props.virtualScrolling) return filteredLogs.value.length
  const visibleCount = Math.ceil(window.innerHeight / props.lineHeight) + 5
  return Math.min(startIndex.value + visibleCount, filteredLogs.value.length)
})

const visibleLogs = computed(() => {
  if (!props.virtualScrolling) {
    return filteredLogs.value.slice(-props.maxVisibleLogs)
  }
  return filteredLogs.value.slice(startIndex.value, endIndex.value)
})

const virtualHeight = computed(() => {
  return filteredLogs.value.length * props.lineHeight
})

// 获取日志偏移量
const getLogOffset = (index) => {
  if (!props.virtualScrolling) return 0
  return (startIndex.value + index) * props.lineHeight
}

// 获取级别图标
const getLevelIcon = (level) => {
  const iconMap = {
    info: 'fas fa-info-circle',
    warning: 'fas fa-exclamation-triangle',
    error: 'fas fa-times-circle',
    debug: 'fas fa-bug',
    success: 'fas fa-check-circle'
  }
  return iconMap[level] || 'fas fa-circle'
}

// 获取级别计数
const getLevelCount = (level) => {
  return processingStore.logs.filter(log => log.level === level).length
}

// 格式化时间戳
const formatTimestamp = (timestamp) => {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  return date.toLocaleTimeString('zh-CN', { 
    hour12: false,
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    fractionalSecondDigits: 3
  })
}

// 高亮搜索结果
const highlightSearch = (text) => {
  if (!searchQuery.value.trim()) return text
  
  const query = searchQuery.value.trim()
  const regex = new RegExp(`(${query})`, 'gi')
  return text.replace(regex, '<mark class="search-highlight">$1</mark>')
}

// 切换自动滚动
const toggleAutoScroll = () => {
  processingStore.toggleAutoScroll()
}

// 切换过滤器
const toggleFilters = () => {
  showFilters.value = !showFilters.value
}

// 切换日志级别过滤
const toggleFilter = (level) => {
  processingStore.toggleLogFilter(level)
}

// 清空日志
const clearLogs = () => {
  if (confirm('确定要清空所有日志吗？')) {
    processingStore.clearLogs()
  }
}

// 导出日志
const exportLogs = () => {
  processingStore.exportLogs()
}

// 复制日志行
const copyLogLine = async (log) => {
  const logText = `[${formatTimestamp(log.timestamp)}] [${log.level.toUpperCase()}] ${log.source ? `[${log.source}] ` : ''}${log.message}`
  
  try {
    await navigator.clipboard.writeText(logText)
    // 这里可以添加复制成功的提示
  } catch (error) {
    console.error('复制失败:', error)
  }
}

// 显示日志详情
const showLogDetail = (log) => {
  // 这里可以打开一个模态框显示详细信息
  alert(`日志详情:\n\n时间: ${formatTimestamp(log.timestamp)}\n级别: ${log.level}\n来源: ${log.source || '未知'}\n消息: ${log.message}`)
}

// 处理滚动事件
const handleScroll = (event) => {
  scrollTop.value = event.target.scrollTop
  
  // 检查是否滚动到底部
  const { scrollTop: top, scrollHeight, clientHeight } = event.target
  const isAtBottom = top + clientHeight >= scrollHeight - 10
  
  if (isAtBottom && processingStore.autoScroll) {
    // 已经在底部，保持自动滚动状态
  } else if (!isAtBottom && processingStore.autoScroll) {
    // 用户向上滚动，暂时禁用自动滚动
    processingStore.autoScroll = false
    setTimeout(() => {
      processingStore.autoScroll = true
    }, 3000) // 3秒后重新启用
  }
}

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (logContainer.value) {
      logContainer.value.scrollTop = logContainer.value.scrollHeight
    }
  })
}

// 监听日志变化，自动滚动到底部
watch(() => processingStore.logs.length, () => {
  lastUpdateTime.value = new Date()
  
  if (processingStore.autoScroll) {
    scrollToBottom()
  }
})

// 监听窗口大小变化
const handleResize = () => {
  // 重新计算可见日志数量
}

onMounted(() => {
  window.addEventListener('resize', handleResize)
  scrollToBottom()
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.realtime-log-container {
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  margin: var(--spacing-md) var(--spacing-lg);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  max-height: 500px;
}

.log-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--spacing-md) var(--spacing-lg);
  background: var(--bg-primary);
  border-bottom: 1px solid var(--border-light);
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.log-title {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  font-size: var(--font-lg);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
  margin: 0;
}

.log-title i {
  color: var(--success-color);
}

.log-summary {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  font-size: var(--font-sm);
  color: var(--text-secondary);
}

.log-count {
  color: var(--text-primary);
  font-weight: var(--font-medium);
}

.log-controls {
  display: flex;
  gap: var(--spacing-xs);
}

.log-control-btn {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  padding: var(--spacing-xs) var(--spacing-sm);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  background: var(--bg-secondary);
  color: var(--text-secondary);
  font-size: var(--font-sm);
  cursor: pointer;
  transition: all var(--duration-fast) var(--ease-in-out);
}

.log-control-btn:hover {
  background: var(--primary-color);
  color: var(--text-white);
  border-color: var(--primary-color);
}

.log-control-btn.active {
  background: var(--primary-color);
  color: var(--text-white);
  border-color: var(--primary-color);
}

.log-filters {
  padding: var(--spacing-md) var(--spacing-lg);
  background: var(--bg-primary);
  border-bottom: 1px solid var(--border-light);
  flex-shrink: 0;
}

.filter-section {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-md);
}

.filter-section:last-child {
  margin-bottom: 0;
}

.filter-label {
  font-size: var(--font-sm);
  font-weight: var(--font-medium);
  color: var(--text-secondary);
  min-width: 60px;
}

.filter-buttons {
  display: flex;
  gap: var(--spacing-xs);
  flex-wrap: wrap;
}

.filter-btn {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  padding: var(--spacing-xs) var(--spacing-sm);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  background: var(--bg-secondary);
  color: var(--text-secondary);
  font-size: var(--font-xs);
  cursor: pointer;
  transition: all var(--duration-fast) var(--ease-in-out);
}

.filter-btn:hover {
  border-color: var(--primary-color);
}

.filter-btn.active {
  background: var(--primary-color);
  color: var(--text-white);
  border-color: var(--primary-color);
}

.filter-btn.info.active { background: var(--info-color); border-color: var(--info-color); }
.filter-btn.warning.active { background: var(--warning-color); border-color: var(--warning-color); }
.filter-btn.error.active { background: var(--danger-color); border-color: var(--danger-color); }
.filter-btn.success.active { background: var(--success-color); border-color: var(--success-color); }

.filter-count {
  font-size: var(--font-xs);
  opacity: 0.8;
}

.search-input-wrapper {
  position: relative;
  flex: 1;
  max-width: 300px;
}

.search-input {
  width: 100%;
  padding: var(--spacing-xs) var(--spacing-sm);
  padding-right: 32px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  font-size: var(--font-sm);
  background: var(--bg-secondary);
  color: var(--text-primary);
}

.search-input:focus {
  outline: none;
  border-color: var(--primary-color);
}

.search-icon {
  position: absolute;
  right: var(--spacing-sm);
  top: 50%;
  transform: translateY(-50%);
  color: var(--text-light);
  pointer-events: none;
}

.log-content {
  flex: 1;
  overflow-y: auto;
  font-family: var(--font-mono);
  font-size: var(--font-sm);
  position: relative;
}

.log-virtual-container {
  position: relative;
}

.log-line {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-xs) var(--spacing-md);
  border-bottom: 1px solid var(--border-light);
  animation: log-appear var(--duration-normal) var(--ease-out);
  position: absolute;
  width: 100%;
  height: 32px;
  left: 0;
}

.log-line:hover {
  background: var(--bg-primary);
}

.log-level-info { border-left: 3px solid var(--info-color); }
.log-level-warning { border-left: 3px solid var(--warning-color); }
.log-level-error { border-left: 3px solid var(--danger-color); }
.log-level-success { border-left: 3px solid var(--success-color); }
.log-level-debug { border-left: 3px solid var(--text-light); }

.log-timestamp {
  color: var(--text-light);
  font-size: var(--font-xs);
  min-width: 80px;
  font-family: var(--font-mono);
}

.log-level-badge {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  padding: 2px var(--spacing-xs);
  border-radius: var(--radius-sm);
  font-size: var(--font-xs);
  font-weight: var(--font-medium);
  min-width: 60px;
  justify-content: center;
}

.level-info { background: rgba(6, 182, 212, 0.1); color: var(--info-color); }
.level-warning { background: rgba(245, 158, 11, 0.1); color: var(--warning-color); }
.level-error { background: rgba(239, 68, 68, 0.1); color: var(--danger-color); }
.level-success { background: rgba(16, 185, 129, 0.1); color: var(--success-color); }
.level-debug { background: rgba(107, 114, 128, 0.1); color: var(--text-light); }

.log-source {
  color: var(--text-secondary);
  font-size: var(--font-xs);
  background: var(--bg-primary);
  padding: 2px var(--spacing-xs);
  border-radius: var(--radius-sm);
  min-width: 60px;
  text-align: center;
}

.log-message {
  flex: 1;
  color: var(--text-primary);
  word-break: break-all;
  line-height: 1.4;
}

.log-message :deep(.search-highlight) {
  background: var(--warning-color);
  color: var(--text-white);
  padding: 1px 2px;
  border-radius: var(--radius-sm);
}

.log-actions {
  display: flex;
  gap: var(--spacing-xs);
  opacity: 0;
  transition: opacity var(--duration-fast) var(--ease-in-out);
}

.log-line:hover .log-actions {
  opacity: 1;
}

.log-action-btn {
  width: 24px;
  height: 24px;
  border: none;
  border-radius: var(--radius-sm);
  background: var(--bg-primary);
  color: var(--text-secondary);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--font-xs);
  transition: all var(--duration-fast) var(--ease-in-out);
}

.log-action-btn:hover {
  background: var(--primary-color);
  color: var(--text-white);
}

.log-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-2xl);
  color: var(--text-light);
  text-align: center;
}

.log-empty i {
  font-size: var(--font-3xl);
  margin-bottom: var(--spacing-md);
  opacity: 0.5;
}

.log-loader {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-md);
  color: var(--text-secondary);
}

.log-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--spacing-sm) var(--spacing-lg);
  background: var(--bg-primary);
  border-top: 1px solid var(--border-light);
  font-size: var(--font-xs);
  color: var(--text-secondary);
  flex-shrink: 0;
}

.log-stats {
  display: flex;
  gap: var(--spacing-md);
}

.stat-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
}

.stat-icon {
  width: 16px;
  height: 16px;
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 10px;
}

.stat-icon.level-info { background: var(--info-color); color: var(--text-white); }
.stat-icon.level-warning { background: var(--warning-color); color: var(--text-white); }
.stat-icon.level-error { background: var(--danger-color); color: var(--text-white); }

.stat-count {
  font-weight: var(--font-medium);
}

.log-performance {
  display: flex;
  gap: var(--spacing-md);
}

.perf-item {
  color: var(--text-light);
}

/* 移动端适配 */
@media (max-width: 767px) {
  .realtime-log-container {
    margin: var(--spacing-sm) var(--spacing-md);
  }
  
  .log-header {
    padding: var(--spacing-sm) var(--spacing-md);
    flex-direction: column;
    gap: var(--spacing-sm);
    align-items: stretch;
  }
  
  .log-controls {
    justify-content: space-between;
  }
  
  .log-control-btn .btn-text {
    display: none;
  }
  
  .filter-section {
    flex-direction: column;
    align-items: stretch;
    gap: var(--spacing-sm);
  }
  
  .log-line {
    flex-direction: column;
    height: auto;
    min-height: 32px;
    align-items: flex-start;
    position: static;
  }
  
  .log-timestamp {
    min-width: auto;
  }
  
  .log-footer {
    flex-direction: column;
    gap: var(--spacing-sm);
    align-items: stretch;
    text-align: center;
  }
}
</style>