<template>
  <div class="thinking-process" v-show="processingStore.showThinking">
    <div class="thinking-bubble" :class="{ active: processingStore.isThinking }">
      <!-- 思考头部 -->
      <div class="thinking-header">
        <div class="thinking-icon-wrapper">
          <i class="fas fa-brain thinking-icon"></i>
          <div class="thinking-pulse"></div>
        </div>
        <div class="thinking-info">
          <span class="thinking-label">AI 正在思考</span>
          <span class="thinking-stage">{{ getStageText(processingStore.thinkingStage) }}</span>
        </div>
        <div class="thinking-dots">
          <div class="thinking-dot"></div>
          <div class="thinking-dot"></div>
          <div class="thinking-dot"></div>
        </div>
      </div>
      
      <!-- 思考内容 -->
      <div class="thinking-content" v-if="processingStore.thinkingText">
        <div class="thinking-text">{{ displayText }}</div>
        
        <!-- 进度条 -->
        <div class="thinking-progress" v-if="showProgress">
          <div class="progress-track">
            <div 
              class="progress-fill" 
              :style="{ width: processingStore.thinkingProgressPercentage + '%' }"
            ></div>
          </div>
          <span class="progress-text">
            {{ Math.round(processingStore.thinkingProgressPercentage) }}%
          </span>
        </div>
      </div>
      
      <!-- 思考阶段指示器 -->
      <div class="thinking-stages" v-if="showStages">
        <div 
          class="stage-item"
          v-for="stage in stages"
          :key="stage.key"
          :class="{ 
            active: processingStore.thinkingStage === stage.key,
            completed: isStageCompleted(stage.key)
          }"
        >
          <div class="stage-icon">
            <i :class="stage.icon"></i>
          </div>
          <span class="stage-name">{{ stage.name }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { useProcessingStore } from '@/stores/processing'

const props = defineProps({
  showProgress: {
    type: Boolean,
    default: true
  },
  showStages: {
    type: Boolean,
    default: true
  },
  typewriterSpeed: {
    type: Number,
    default: 50
  }
})

const processingStore = useProcessingStore()

// 打字机效果的显示文本
const displayText = ref('')
const typewriterTimer = ref(null)

// 思考阶段定义
const stages = ref([
  {
    key: 'analyzing',
    name: '分析问题',
    icon: 'fas fa-search'
  },
  {
    key: 'planning',
    name: '制定计划',
    icon: 'fas fa-list-ul'
  },
  {
    key: 'executing',
    name: '执行操作',
    icon: 'fas fa-cogs'
  },
  {
    key: 'responding',
    name: '生成回答',
    icon: 'fas fa-comment'
  }
])

// 获取阶段文本
const getStageText = (stage) => {
  const stageMap = {
    analyzing: '正在分析您的问题...',
    planning: '正在制定解决方案...',
    executing: '正在执行相关操作...',
    responding: '正在整理回答...'
  }
  return stageMap[stage] || '正在思考中...'
}

// 判断阶段是否完成
const isStageCompleted = (stageKey) => {
  const stageOrder = ['analyzing', 'planning', 'executing', 'responding']
  const currentIndex = stageOrder.indexOf(processingStore.thinkingStage)
  const stageIndex = stageOrder.indexOf(stageKey)
  return stageIndex < currentIndex
}

// 打字机效果
const startTypewriter = (text) => {
  displayText.value = ''
  let index = 0
  
  const type = () => {
    if (index < text.length) {
      displayText.value += text.charAt(index)
      index++
      typewriterTimer.value = setTimeout(type, props.typewriterSpeed)
    }
  }
  
  type()
}

// 停止打字机效果
const stopTypewriter = () => {
  if (typewriterTimer.value) {
    clearTimeout(typewriterTimer.value)
    typewriterTimer.value = null
  }
}

// 监听思考文本变化
watch(() => processingStore.thinkingText, (newText, oldText) => {
  if (newText !== oldText && newText) {
    stopTypewriter()
    startTypewriter(newText)
  }
})

// 监听思考状态变化
watch(() => processingStore.isThinking, (isThinking) => {
  if (!isThinking) {
    stopTypewriter()
    // 延迟清空显示文本，让用户能看到最终结果
    setTimeout(() => {
      displayText.value = ''
    }, 2000)
  }
})

onUnmounted(() => {
  stopTypewriter()
})
</script>

<style scoped>
.thinking-process {
  padding: var(--spacing-md) var(--spacing-lg);
  background: var(--bg-chat);
}

.thinking-bubble {
  background: var(--bg-secondary);
  border: 2px solid var(--border-color);
  border-radius: var(--radius-xl);
  padding: var(--spacing-lg);
  box-shadow: var(--shadow-sm);
  transition: all var(--duration-normal) var(--ease-in-out);
  opacity: 0;
  transform: translateY(10px);
}

.thinking-bubble.active {
  opacity: 1;
  transform: translateY(0);
  border-color: var(--primary-color);
  animation: thinking-pulse 2s infinite;
}

.thinking-header {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-md);
}

.thinking-icon-wrapper {
  position: relative;
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.thinking-icon {
  font-size: var(--font-2xl);
  color: var(--primary-color);
  z-index: 2;
  position: relative;
}

.thinking-pulse {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  border-radius: var(--radius-full);
  background: var(--primary-color);
  opacity: 0.2;
  animation: pulse-ring 2s infinite;
}

.thinking-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xs);
}

.thinking-label {
  font-size: var(--font-lg);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
}

.thinking-stage {
  font-size: var(--font-sm);
  color: var(--text-secondary);
}

.thinking-dots {
  display: flex;
  gap: var(--spacing-xs);
}

.thinking-dot {
  width: 8px;
  height: 8px;
  border-radius: var(--radius-full);
  background: var(--primary-color);
  animation: bounce-dots 1.4s infinite ease-in-out;
}

.thinking-dot:nth-child(1) { animation-delay: -0.32s; }
.thinking-dot:nth-child(2) { animation-delay: -0.16s; }
.thinking-dot:nth-child(3) { animation-delay: 0s; }

.thinking-content {
  margin-bottom: var(--spacing-lg);
}

.thinking-text {
  background: var(--bg-primary);
  border-radius: var(--radius-lg);
  padding: var(--spacing-md);
  font-size: var(--font-base);
  line-height: 1.6;
  color: var(--text-primary);
  min-height: 60px;
  position: relative;
  border-left: 4px solid var(--primary-color);
}

.thinking-text::after {
  content: '|';
  color: var(--primary-color);
  animation: blink 1s infinite;
  font-weight: var(--font-bold);
}

.thinking-progress {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  margin-top: var(--spacing-md);
}

.progress-track {
  flex: 1;
  height: 6px;
  background: var(--border-light);
  border-radius: var(--radius-full);
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, var(--primary-color), var(--info-color));
  border-radius: var(--radius-full);
  transition: width var(--duration-normal) var(--ease-out);
  position: relative;
}

.progress-fill::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.3), transparent);
  animation: shimmer 2s infinite;
}

.progress-text {
  font-size: var(--font-sm);
  font-weight: var(--font-medium);
  color: var(--text-secondary);
  min-width: 40px;
}

.thinking-stages {
  display: flex;
  justify-content: space-between;
  gap: var(--spacing-sm);
  padding: var(--spacing-md);
  background: var(--bg-primary);
  border-radius: var(--radius-lg);
}

.stage-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--spacing-xs);
  flex: 1;
  padding: var(--spacing-sm);
  border-radius: var(--radius-md);
  transition: all var(--duration-fast) var(--ease-in-out);
  position: relative;
}

.stage-item.active {
  background: var(--primary-color);
  color: var(--text-white);
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.stage-item.completed {
  background: var(--success-color);
  color: var(--text-white);
}

.stage-item.completed .stage-icon i::before {
  content: '\f00c'; /* FontAwesome check icon */
}

.stage-icon {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.1);
  border-radius: var(--radius-full);
  font-size: var(--font-sm);
}

.stage-item.active .stage-icon,
.stage-item.completed .stage-icon {
  background: rgba(255, 255, 255, 0.2);
}

.stage-name {
  font-size: var(--font-xs);
  font-weight: var(--font-medium);
  text-align: center;
}

/* 动画定义 */
@keyframes pulse-ring {
  0% {
    transform: scale(0.8);
    opacity: 0.8;
  }
  50% {
    transform: scale(1.2);
    opacity: 0.3;
  }
  100% {
    transform: scale(1.4);
    opacity: 0;
  }
}

@keyframes blink {
  0%, 50% { opacity: 1; }
  51%, 100% { opacity: 0; }
}

@keyframes shimmer {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(100%); }
}

/* 移动端适配 */
@media (max-width: 767px) {
  .thinking-process {
    padding: var(--spacing-sm) var(--spacing-md);
  }
  
  .thinking-bubble {
    padding: var(--spacing-md);
  }
  
  .thinking-header {
    gap: var(--spacing-sm);
  }
  
  .thinking-icon-wrapper {
    width: 40px;
    height: 40px;
  }
  
  .thinking-icon {
    font-size: var(--font-xl);
  }
  
  .thinking-label {
    font-size: var(--font-base);
  }
  
  .thinking-stages {
    flex-wrap: wrap;
  }
  
  .stage-item {
    min-width: calc(50% - var(--spacing-xs));
  }
  
  .stage-name {
    font-size: 10px;
  }
}

@media (max-width: 480px) {
  .thinking-stages {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: var(--spacing-sm);
  }
}
</style>