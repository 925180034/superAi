<template>
  <div class="tool-timeline-container" v-if="processingStore.hasActiveTools">
    <!-- 时间线标题 -->
    <div class="timeline-header">
      <div class="header-left">
        <h4 class="timeline-title">
          <i class="fas fa-tools"></i>
          工具调用过程
        </h4>
        <div class="timeline-summary">
          <span class="completed-count">{{ processingStore.toolsCompleted }}</span>
          <span class="separator">/</span>
          <span class="total-count">{{ processingStore.tools.length }}</span>
          <span class="summary-text">完成</span>
        </div>
      </div>
      
      <div class="header-actions">
        <button 
          class="action-btn"
          @click="toggleExpanded"
          :title="isExpanded ? '收起详情' : '展开详情'"
        >
          <i :class="isExpanded ? 'fas fa-chevron-up' : 'fas fa-chevron-down'"></i>
        </button>
        <button 
          class="action-btn"
          @click="clearCompleted"
          title="清除已完成"
          v-if="processingStore.completedTools.length > 0"
        >
          <i class="fas fa-broom"></i>
        </button>
      </div>
    </div>

    <!-- 总体进度 -->
    <div class="overall-progress">
      <div class="progress-bar">
        <div 
          class="progress-fill" 
          :style="{ width: processingStore.toolsProgress + '%' }"
        ></div>
      </div>
      <span class="progress-text">{{ processingStore.toolsProgress }}%</span>
    </div>

    <!-- 工具步骤 -->
    <div class="tool-timeline" :class="{ expanded: isExpanded }">
      <div 
        class="tool-step"
        v-for="(tool, index) in displayedTools"
        :key="`tool-${index}-${tool.id}`"
        :class="getStepClass(tool)"
      >
        <!-- 时间线节点 -->
        <div class="timeline-node">
          <div class="node-connector" v-if="index > 0"></div>
          <div class="node-icon">
            <i :class="getToolIcon(tool)" v-if="tool.status !== 'running'"></i>
            <div class="loading-spinner" v-else></div>
          </div>
        </div>

        <!-- 工具内容 -->
        <div class="tool-content">
          <div class="tool-header">
            <div class="tool-name">{{ tool.displayName || tool.name }}</div>
            <div class="tool-status" :class="tool.status">
              {{ getStatusText(tool.status) }}
            </div>
            <div class="tool-time" v-if="tool.executionTime">
              <i class="fas fa-clock"></i>
              {{ tool.executionTime }}ms
            </div>
          </div>

          <div class="tool-description" v-if="tool.description">
            {{ tool.description }}
          </div>

          <!-- 参数展示 -->
          <div class="tool-params" v-if="tool.parameters && isExpanded">
            <div class="params-header" @click="toggleParams(tool.id)">
              <i class="fas fa-cog"></i>
              <span>参数</span>
              <i class="fas fa-chevron-down toggle-icon" :class="{ rotated: expandedParams.has(tool.id) }"></i>
            </div>
            <div class="params-content" v-show="expandedParams.has(tool.id)">
              <div class="param-grid">
                <div 
                  class="param-item" 
                  v-for="(value, key) in tool.parameters" 
                  :key="key"
                >
                  <span class="param-key">{{ key }}:</span>
                  <span class="param-value">{{ formatParamValue(value) }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 执行结果 -->
          <div class="tool-result" v-if="tool.result && isExpanded">
            <div class="result-header" @click="toggleResult(tool.id)">
              <i class="fas fa-check-circle"></i>
              <span>执行结果</span>
              <i class="fas fa-chevron-down toggle-icon" :class="{ rotated: expandedResults.has(tool.id) }"></i>
            </div>
            <div class="result-content" v-show="expandedResults.has(tool.id)">
              <pre class="result-text">{{ formatResult(tool.result) }}</pre>
            </div>
          </div>

          <!-- 错误信息 -->
          <div class="tool-error" v-if="tool.error">
            <div class="error-header">
              <i class="fas fa-exclamation-triangle"></i>
              <span>错误信息</span>
            </div>
            <div class="error-content">
              <pre class="error-text">{{ tool.error }}</pre>
            </div>
          </div>

          <!-- 进度指示器 -->
          <div class="tool-progress" v-if="tool.status === 'running' && tool.progress">
            <div class="progress-bar-small">
              <div class="progress-fill-small" :style="{ width: tool.progress + '%' }"></div>
            </div>
            <span class="progress-text-small">{{ tool.progress }}%</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 查看更多按钮 -->
    <div class="timeline-footer" v-if="processingStore.tools.length > maxDisplayItems && !showAll">
      <button class="show-more-btn" @click="showAll = true">
        <i class="fas fa-chevron-down"></i>
        查看更多 ({{ processingStore.tools.length - maxDisplayItems }} 个)
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useProcessingStore } from '@/stores/processing'

const props = defineProps({
  maxDisplayItems: {
    type: Number,
    default: 5
  },
  autoExpand: {
    type: Boolean,
    default: false
  }
})

const processingStore = useProcessingStore()

// 响应式状态
const isExpanded = ref(props.autoExpand)
const showAll = ref(false)
const expandedParams = ref(new Set())
const expandedResults = ref(new Set())

// 显示的工具列表
const displayedTools = computed(() => {
  const tools = processingStore.tools
  if (showAll.value || tools.length <= props.maxDisplayItems) {
    return tools
  }
  return tools.slice(0, props.maxDisplayItems)
})

// 获取步骤样式类
const getStepClass = (tool) => {
  return {
    'step-pending': tool.status === 'pending',
    'step-running': tool.status === 'running',
    'step-completed': tool.status === 'completed',
    'step-error': tool.status === 'error'
  }
}

// 获取工具图标
const getToolIcon = (tool) => {
  const iconMap = {
    completed: 'fas fa-check-circle',
    error: 'fas fa-exclamation-circle',
    pending: 'fas fa-clock'
  }
  
  // 根据工具类型返回特定图标
  const typeIconMap = {
    search: 'fas fa-search',
    database: 'fas fa-database',
    api: 'fas fa-plug',
    file: 'fas fa-file',
    calculation: 'fas fa-calculator',
    web: 'fas fa-globe',
    fitness: 'fas fa-dumbbell'
  }
  
  return typeIconMap[tool.type] || iconMap[tool.status] || 'fas fa-cog'
}

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    pending: '等待中',
    running: '执行中',
    completed: '已完成',
    error: '失败'
  }
  return statusMap[status] || status
}

// 格式化参数值
const formatParamValue = (value) => {
  if (typeof value === 'object') {
    return JSON.stringify(value, null, 2)
  }
  if (typeof value === 'string' && value.length > 100) {
    return value.substring(0, 100) + '...'
  }
  return String(value)
}

// 格式化结果
const formatResult = (result) => {
  if (typeof result === 'object') {
    return JSON.stringify(result, null, 2)
  }
  return String(result || '')
}

// 切换展开状态
const toggleExpanded = () => {
  isExpanded.value = !isExpanded.value
}

// 切换参数展开
const toggleParams = (toolId) => {
  if (expandedParams.value.has(toolId)) {
    expandedParams.value.delete(toolId)
  } else {
    expandedParams.value.add(toolId)
  }
}

// 切换结果展开
const toggleResult = (toolId) => {
  if (expandedResults.value.has(toolId)) {
    expandedResults.value.delete(toolId)
  } else {
    expandedResults.value.add(toolId)
  }
}

// 清除已完成的工具
const clearCompleted = () => {
  processingStore.tools = processingStore.tools.filter(tool => tool.status !== 'completed')
}
</script>

<style scoped>
.tool-timeline-container {
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  margin: var(--spacing-md) var(--spacing-lg);
  overflow: hidden;
}

.timeline-header {
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

.timeline-title {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  font-size: var(--font-lg);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
  margin: 0;
}

.timeline-title i {
  color: var(--primary-color);
}

.timeline-summary {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  font-size: var(--font-sm);
  color: var(--text-secondary);
}

.completed-count {
  color: var(--success-color);
  font-weight: var(--font-medium);
}

.total-count {
  color: var(--text-primary);
  font-weight: var(--font-medium);
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

.action-btn:hover {
  background: var(--primary-color);
  color: var(--text-white);
}

.overall-progress {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  padding: var(--spacing-md) var(--spacing-lg);
  background: var(--bg-primary);
}

.progress-bar {
  flex: 1;
  height: 8px;
  background: var(--border-light);
  border-radius: var(--radius-full);
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, var(--success-color), var(--primary-color));
  border-radius: var(--radius-full);
  transition: width var(--duration-normal) var(--ease-out);
}

.progress-text {
  font-size: var(--font-sm);
  font-weight: var(--font-medium);
  color: var(--text-secondary);
  min-width: 40px;
}

.tool-timeline {
  padding: var(--spacing-lg);
  max-height: 300px;
  overflow-y: auto;
  transition: max-height var(--duration-normal) var(--ease-in-out);
}

.tool-timeline.expanded {
  max-height: none;
}

.tool-step {
  display: flex;
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-lg);
  position: relative;
}

.tool-step:last-child {
  margin-bottom: 0;
}

.timeline-node {
  position: relative;
  flex-shrink: 0;
}

.node-connector {
  position: absolute;
  top: -20px;
  left: 50%;
  transform: translateX(-50%);
  width: 2px;
  height: 20px;
  background: var(--border-color);
}

.node-icon {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--font-sm);
  position: relative;
  z-index: 2;
}

.step-pending .node-icon {
  background: var(--text-light);
  color: var(--text-white);
}

.step-running .node-icon {
  background: var(--info-color);
  color: var(--text-white);
  animation: pulse 2s infinite;
}

.step-completed .node-icon {
  background: var(--success-color);
  color: var(--text-white);
}

.step-error .node-icon {
  background: var(--danger-color);
  color: var(--text-white);
}

.tool-content {
  flex: 1;
  background: var(--bg-primary);
  border-radius: var(--radius-lg);
  padding: var(--spacing-md);
  border: 1px solid var(--border-light);
}

.step-running .tool-content {
  border-color: var(--info-color);
  box-shadow: 0 0 0 1px rgba(6, 182, 212, 0.1);
}

.step-completed .tool-content {
  border-color: var(--success-color);
}

.step-error .tool-content {
  border-color: var(--danger-color);
  background: rgba(239, 68, 68, 0.05);
}

.tool-header {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-sm);
  flex-wrap: wrap;
}

.tool-name {
  font-size: var(--font-base);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
  flex: 1;
  min-width: 0;
}

.tool-status {
  padding: var(--spacing-xs) var(--spacing-sm);
  border-radius: var(--radius-full);
  font-size: var(--font-xs);
  font-weight: var(--font-medium);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.tool-status.pending {
  background: var(--text-light);
  color: var(--text-white);
}

.tool-status.running {
  background: var(--info-color);
  color: var(--text-white);
}

.tool-status.completed {
  background: var(--success-color);
  color: var(--text-white);
}

.tool-status.error {
  background: var(--danger-color);
  color: var(--text-white);
}

.tool-time {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  font-size: var(--font-xs);
  color: var(--text-secondary);
}

.tool-description {
  font-size: var(--font-sm);
  color: var(--text-secondary);
  margin-bottom: var(--spacing-sm);
}

.tool-params, .tool-result {
  margin-top: var(--spacing-md);
}

.params-header, .result-header {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  font-size: var(--font-sm);
  font-weight: var(--font-medium);
  color: var(--text-primary);
  cursor: pointer;
  padding: var(--spacing-xs) 0;
  border-bottom: 1px solid var(--border-light);
}

.toggle-icon {
  margin-left: auto;
  transition: transform var(--duration-fast) var(--ease-in-out);
}

.toggle-icon.rotated {
  transform: rotate(180deg);
}

.params-content, .result-content {
  margin-top: var(--spacing-sm);
  animation: slideDown var(--duration-normal) var(--ease-out);
}

.param-grid {
  display: grid;
  gap: var(--spacing-xs);
}

.param-item {
  display: flex;
  gap: var(--spacing-sm);
  font-size: var(--font-sm);
  padding: var(--spacing-xs) 0;
}

.param-key {
  font-weight: var(--font-medium);
  color: var(--text-secondary);
  min-width: 80px;
}

.param-value {
  color: var(--text-primary);
  word-break: break-all;
}

.result-text, .error-text {
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  padding: var(--spacing-md);
  font-size: var(--font-sm);
  font-family: var(--font-mono);
  color: var(--text-primary);
  white-space: pre-wrap;
  word-wrap: break-word;
  max-height: 200px;
  overflow-y: auto;
}

.tool-error {
  margin-top: var(--spacing-md);
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

.error-text {
  border-color: var(--danger-color);
  background: rgba(239, 68, 68, 0.05);
  color: var(--danger-color);
}

.tool-progress {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  margin-top: var(--spacing-sm);
}

.progress-bar-small {
  flex: 1;
  height: 4px;
  background: var(--border-light);
  border-radius: var(--radius-full);
  overflow: hidden;
}

.progress-fill-small {
  height: 100%;
  background: var(--info-color);
  border-radius: var(--radius-full);
  transition: width var(--duration-normal) var(--ease-out);
}

.progress-text-small {
  font-size: var(--font-xs);
  color: var(--text-secondary);
  min-width: 30px;
}

.timeline-footer {
  padding: var(--spacing-md) var(--spacing-lg);
  background: var(--bg-primary);
  border-top: 1px solid var(--border-light);
  text-align: center;
}

.show-more-btn {
  background: none;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  padding: var(--spacing-sm) var(--spacing-md);
  color: var(--text-secondary);
  font-size: var(--font-sm);
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  margin: 0 auto;
  transition: all var(--duration-fast) var(--ease-in-out);
}

.show-more-btn:hover {
  background: var(--primary-color);
  color: var(--text-white);
  border-color: var(--primary-color);
}

/* 动画 */
@keyframes slideDown {
  from {
    opacity: 0;
    max-height: 0;
  }
  to {
    opacity: 1;
    max-height: 300px;
  }
}

/* 移动端适配 */
@media (max-width: 767px) {
  .tool-timeline-container {
    margin: var(--spacing-sm) var(--spacing-md);
  }
  
  .timeline-header {
    padding: var(--spacing-sm) var(--spacing-md);
    flex-direction: column;
    gap: var(--spacing-sm);
    align-items: flex-start;
  }
  
  .header-left {
    width: 100%;
    justify-content: space-between;
  }
  
  .overall-progress {
    padding: var(--spacing-sm) var(--spacing-md);
  }
  
  .tool-timeline {
    padding: var(--spacing-md);
  }
  
  .tool-step {
    gap: var(--spacing-sm);
  }
  
  .node-icon {
    width: 32px;
    height: 32px;
  }
  
  .tool-header {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--spacing-sm);
  }
  
  .timeline-title {
    font-size: var(--font-base);
  }
}
</style>