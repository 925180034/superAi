<template>
  <div class="chat-input-container">
    <!-- 文件上传预览 -->
    <div class="file-upload-preview" v-if="selectedFiles.length > 0">
      <div class="file-list">
        <div 
          class="file-item" 
          v-for="file in selectedFiles" 
          :key="file.id"
        >
          <div class="file-icon">
            <i :class="getFileIcon(file.type)"></i>
          </div>
          <div class="file-info">
            <div class="file-name">{{ file.name }}</div>
            <div class="file-size">{{ formatFileSize(file.size) }}</div>
          </div>
          <button class="remove-file-btn" @click="removeFile(file.id)">
            <i class="fas fa-times"></i>
          </button>
        </div>
      </div>
    </div>

    <!-- 快速提示/模板 -->
    <div class="quick-suggestions" v-if="showSuggestions && suggestions.length > 0">
      <div class="suggestions-header">
        <span>快速开始</span>
        <button class="close-suggestions" @click="closeSuggestions">
          <i class="fas fa-times"></i>
        </button>
      </div>
      <div class="suggestions-grid">
        <button 
          class="suggestion-item"
          v-for="suggestion in suggestions"
          :key="suggestion.id"
          @click="useSuggestion(suggestion)"
        >
          <i :class="suggestion.icon"></i>
          <span>{{ suggestion.text }}</span>
        </button>
      </div>
    </div>

    <!-- 输入区域 -->
    <div class="chat-input-wrapper">
      <!-- 工具栏 -->
      <div class="input-toolbar">
        <button 
          class="toolbar-btn" 
          @click="openFileUpload" 
          title="上传文件"
          :disabled="disabled"
        >
          <i class="fas fa-paperclip"></i>
        </button>
        <button 
          class="toolbar-btn" 
          @click="openImageUpload" 
          title="上传图片"
          :disabled="disabled"
        >
          <i class="fas fa-image"></i>
        </button>
        <button 
          class="toolbar-btn" 
          @click="toggleSuggestions" 
          title="快速模板"
          :class="{ active: showSuggestions }"
          :disabled="disabled"
        >
          <i class="fas fa-magic"></i>
        </button>
        <button 
          class="toolbar-btn" 
          @click="clearInput" 
          title="清空输入"
          :disabled="disabled || !message.trim()"
        >
          <i class="fas fa-eraser"></i>
        </button>
      </div>

      <!-- 输入框 -->
      <div class="input-area">
        <textarea 
          class="chat-input" 
          v-model="message"
          @keydown="handleKeyDown"
          @input="adjustHeight"
          @paste="handlePaste"
          :placeholder="placeholder"
          :disabled="disabled"
          ref="inputRef"
          rows="1"
        ></textarea>

        <!-- 发送按钮 -->
        <button 
          class="send-btn" 
          @click="sendMessage"
          :disabled="!canSend"
          :class="{ 'has-message': message.trim() || selectedFiles.length > 0 }"
        >
          <i class="fas fa-paper-plane" v-if="!isLoading"></i>
          <div class="loading-spinner" v-else></div>
        </button>
      </div>
    </div>

    <!-- 字数统计和提示 -->
    <div class="input-footer" v-if="showFooter">
      <div class="input-stats">
        <span class="char-count" :class="{ warning: isNearLimit, danger: isOverLimit }">
          {{ message.length }}/{{ maxLength }}
        </span>
        <span class="file-count" v-if="selectedFiles.length > 0">
          {{ selectedFiles.length }} 个文件
        </span>
      </div>
      <div class="input-tips">
        <span class="tip">{{ currentTip }}</span>
      </div>
    </div>

    <!-- 隐藏的文件输入 -->
    <input
      type="file"
      ref="fileInputRef"
      multiple
      accept="*/*"
      @change="handleFileSelect"
      style="display: none"
    >
    
    <input
      type="file"
      ref="imageInputRef"
      multiple
      accept="image/*"
      @change="handleImageSelect"
      style="display: none"
    >
  </div>
</template>

<script setup>
import { ref, computed, nextTick, onMounted, onUnmounted, watch } from 'vue'
import { useChatStore } from '@/stores/chat'

const props = defineProps({
  placeholder: {
    type: String,
    default: '输入你的问题，按 Enter 发送，Shift+Enter 换行...'
  },
  disabled: {
    type: Boolean,
    default: false
  },
  maxLength: {
    type: Number,
    default: 2000
  },
  showFooter: {
    type: Boolean,
    default: true
  },
  allowFiles: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['send'])
const chatStore = useChatStore()

// 响应式数据
const message = ref('')
const selectedFiles = ref([])
const showSuggestions = ref(false)
const isLoading = ref(false)

// 引用
const inputRef = ref(null)
const fileInputRef = ref(null)
const imageInputRef = ref(null)

// 快速建议列表
const suggestions = ref([
  {
    id: 1,
    text: '制定一周健身计划',
    icon: 'fas fa-calendar-alt'
  },
  {
    id: 2,
    text: '推荐适合新手的运动',
    icon: 'fas fa-running'
  },
  {
    id: 3,
    text: '分析我的运动数据',
    icon: 'fas fa-chart-line'
  },
  {
    id: 4,
    text: '如何提高运动效果',
    icon: 'fas fa-arrow-up'
  }
])

// 输入提示列表
const tips = [
  '按 Enter 发送消息，Shift+Enter 换行',
  '支持上传图片和文件',
  '可以使用快速模板快速开始对话',
  '支持 Markdown 格式'
]

const currentTip = ref(tips[0])

// 计算属性
const canSend = computed(() => {
  return !props.disabled && 
         !isLoading.value && 
         (message.value.trim() || selectedFiles.value.length > 0) &&
         !isOverLimit.value
})

const isNearLimit = computed(() => {
  return message.value.length > props.maxLength * 0.8
})

const isOverLimit = computed(() => {
  return message.value.length > props.maxLength
})

// 监听聊天状态
watch(() => chatStore.isLoading, (newVal) => {
  isLoading.value = newVal
})

// 自动调整输入框高度
const adjustHeight = () => {
  nextTick(() => {
    const textarea = inputRef.value
    if (textarea) {
      textarea.style.height = 'auto'
      const maxHeight = 120 // 最大高度
      const newHeight = Math.min(textarea.scrollHeight, maxHeight)
      textarea.style.height = newHeight + 'px'
    }
  })
}

// 处理键盘事件
const handleKeyDown = (event) => {
  // Enter 发送，Shift+Enter 换行
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault()
    sendMessage()
  }
  
  // Escape 清空输入
  if (event.key === 'Escape') {
    clearInput()
  }
  
  // Ctrl/Cmd + / 显示快速建议
  if ((event.ctrlKey || event.metaKey) && event.key === '/') {
    event.preventDefault()
    toggleSuggestions()
  }
}

// 处理粘贴事件
const handlePaste = (event) => {
  const items = event.clipboardData?.items
  if (!items) return
  
  const files = []
  for (let i = 0; i < items.length; i++) {
    const item = items[i]
    if (item.kind === 'file') {
      const file = item.getAsFile()
      if (file) {
        files.push(file)
      }
    }
  }
  
  if (files.length > 0) {
    event.preventDefault()
    addFiles(files)
  }
}

// 发送消息
const sendMessage = async () => {
  if (!canSend.value) return
  
  const content = message.value.trim()
  const attachments = [...selectedFiles.value]
  
  if (!content && attachments.length === 0) return
  
  // 清空输入
  message.value = ''
  selectedFiles.value = []
  showSuggestions.value = false
  
  // 重置输入框高度
  adjustHeight()
  
  // 发送消息
  emit('send', { content, attachments })
  
  // 聚焦输入框
  nextTick(() => {
    inputRef.value?.focus()
  })
}

// 清空输入
const clearInput = () => {
  message.value = ''
  selectedFiles.value = []
  showSuggestions.value = false
  adjustHeight()
  inputRef.value?.focus()
}

// 打开文件选择
const openFileUpload = () => {
  if (!props.allowFiles) return
  fileInputRef.value?.click()
}

// 打开图片选择
const openImageUpload = () => {
  if (!props.allowFiles) return
  imageInputRef.value?.click()
}

// 处理文件选择
const handleFileSelect = (event) => {
  const files = Array.from(event.target.files || [])
  addFiles(files)
  event.target.value = '' // 清空input值，允许重复选择同一文件
}

// 处理图片选择
const handleImageSelect = (event) => {
  const files = Array.from(event.target.files || [])
  addFiles(files)
  event.target.value = ''
}

// 添加文件
const addFiles = (files) => {
  const maxFileSize = 10 * 1024 * 1024 // 10MB
  const maxFileCount = 5
  
  for (const file of files) {
    if (selectedFiles.value.length >= maxFileCount) {
      alert(`最多只能上传 ${maxFileCount} 个文件`)
      break
    }
    
    if (file.size > maxFileSize) {
      alert(`文件 "${file.name}" 超过最大限制 (10MB)`)
      continue
    }
    
    const fileData = {
      id: Date.now() + Math.random(),
      name: file.name,
      size: file.size,
      type: getFileType(file.type),
      file: file,
      url: URL.createObjectURL(file)
    }
    
    selectedFiles.value.push(fileData)
  }
}

// 移除文件
const removeFile = (fileId) => {
  const index = selectedFiles.value.findIndex(f => f.id === fileId)
  if (index > -1) {
    const file = selectedFiles.value[index]
    URL.revokeObjectURL(file.url) // 释放内存
    selectedFiles.value.splice(index, 1)
  }
}

// 获取文件类型
const getFileType = (mimeType) => {
  if (mimeType.startsWith('image/')) return 'image'
  if (mimeType.startsWith('video/')) return 'video'
  if (mimeType.startsWith('audio/')) return 'audio'
  if (mimeType.includes('pdf')) return 'pdf'
  if (mimeType.includes('word')) return 'doc'
  if (mimeType.includes('excel') || mimeType.includes('spreadsheet')) return 'excel'
  if (mimeType.includes('zip') || mimeType.includes('archive')) return 'zip'
  if (mimeType.includes('text') || mimeType.includes('code')) return 'text'
  return 'file'
}

// 获取文件图标
const getFileIcon = (type) => {
  const iconMap = {
    image: 'fas fa-image',
    video: 'fas fa-video',
    audio: 'fas fa-music',
    pdf: 'fas fa-file-pdf',
    doc: 'fas fa-file-word',
    excel: 'fas fa-file-excel',
    zip: 'fas fa-file-archive',
    text: 'fas fa-file-alt',
    file: 'fas fa-file'
  }
  return iconMap[type] || 'fas fa-file'
}

// 格式化文件大小
const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 Bytes'
  const k = 1024
  const sizes = ['Bytes', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

// 切换建议显示
const toggleSuggestions = () => {
  showSuggestions.value = !showSuggestions.value
}

// 关闭建议
const closeSuggestions = () => {
  showSuggestions.value = false
}

// 使用建议
const useSuggestion = (suggestion) => {
  message.value = suggestion.text
  showSuggestions.value = false
  adjustHeight()
  inputRef.value?.focus()
}

// 循环显示提示
let tipInterval
const startTipRotation = () => {
  tipInterval = setInterval(() => {
    const currentIndex = tips.indexOf(currentTip.value)
    const nextIndex = (currentIndex + 1) % tips.length
    currentTip.value = tips[nextIndex]
  }, 5000)
}

// 聚焦输入框
const focus = () => {
  inputRef.value?.focus()
}

// 暴露方法给父组件
defineExpose({
  focus,
  clearInput
})

onMounted(() => {
  startTipRotation()
  inputRef.value?.focus()
})

onUnmounted(() => {
  if (tipInterval) {
    clearInterval(tipInterval)
  }
  
  // 清理文件URL
  selectedFiles.value.forEach(file => {
    if (file.url) {
      URL.revokeObjectURL(file.url)
    }
  })
})
</script>

<style scoped>
.chat-input-container {
  background: var(--bg-secondary);
  border-top: 1px solid var(--border-color);
  padding: var(--spacing-md) var(--spacing-lg);
}

.file-upload-preview {
  margin-bottom: var(--spacing-md);
}

.file-list {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-sm);
}

.file-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  background: var(--bg-primary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  padding: var(--spacing-sm);
  max-width: 250px;
}

.file-icon {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--primary-color);
  color: var(--text-white);
  border-radius: var(--radius-sm);
  flex-shrink: 0;
}

.file-info {
  flex: 1;
  min-width: 0;
}

.file-name {
  font-size: var(--font-sm);
  font-weight: var(--font-medium);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.file-size {
  font-size: var(--font-xs);
  color: var(--text-secondary);
}

.remove-file-btn {
  background: none;
  border: none;
  color: var(--text-secondary);
  cursor: pointer;
  padding: var(--spacing-xs);
  border-radius: var(--radius-sm);
  transition: all var(--duration-fast) var(--ease-in-out);
}

.remove-file-btn:hover {
  background: var(--danger-color);
  color: var(--text-white);
}

.quick-suggestions {
  background: var(--bg-primary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: var(--spacing-md);
  margin-bottom: var(--spacing-md);
}

.suggestions-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--spacing-sm);
  font-size: var(--font-sm);
  font-weight: var(--font-medium);
  color: var(--text-secondary);
}

.close-suggestions {
  background: none;
  border: none;
  color: var(--text-light);
  cursor: pointer;
  padding: var(--spacing-xs);
  border-radius: var(--radius-sm);
  transition: all var(--duration-fast) var(--ease-in-out);
}

.close-suggestions:hover {
  background: var(--bg-secondary);
  color: var(--text-primary);
}

.suggestions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: var(--spacing-sm);
}

.suggestion-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-sm) var(--spacing-md);
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  color: var(--text-primary);
  font-size: var(--font-sm);
  cursor: pointer;
  transition: all var(--duration-fast) var(--ease-in-out);
  text-align: left;
}

.suggestion-item:hover {
  background: var(--primary-color);
  color: var(--text-white);
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
}

.chat-input-wrapper {
  background: var(--bg-secondary);
  border: 2px solid var(--border-color);
  border-radius: var(--radius-xl);
  overflow: hidden;
  transition: all var(--duration-fast) var(--ease-in-out);
}

.chat-input-wrapper:focus-within {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgb(59 130 246 / 0.1);
}

.input-toolbar {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  padding: var(--spacing-sm) var(--spacing-md);
  border-bottom: 1px solid var(--border-light);
}

.toolbar-btn {
  width: 32px;
  height: 32px;
  border: none;
  border-radius: var(--radius-md);
  background: none;
  color: var(--text-secondary);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--duration-fast) var(--ease-in-out);
}

.toolbar-btn:hover:not(:disabled) {
  background: var(--primary-color);
  color: var(--text-white);
}

.toolbar-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.toolbar-btn.active {
  background: var(--primary-color);
  color: var(--text-white);
}

.input-area {
  display: flex;
  align-items: flex-end;
  gap: var(--spacing-sm);
  padding: var(--spacing-sm) var(--spacing-md);
}

.chat-input {
  flex: 1;
  border: none;
  outline: none;
  background: none;
  font-size: var(--font-base);
  font-family: inherit;
  line-height: 1.5;
  resize: none;
  min-height: 24px;
  max-height: 120px;
  overflow-y: auto;
}

.chat-input::placeholder {
  color: var(--text-light);
}

.chat-input:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.send-btn {
  width: 40px;
  height: 40px;
  border: none;
  border-radius: var(--radius-full);
  background: var(--text-light);
  color: var(--text-white);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--duration-fast) var(--ease-in-out);
  flex-shrink: 0;
}

.send-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
}

.send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.send-btn.has-message {
  background: var(--primary-color);
}

.send-btn.has-message:hover:not(:disabled) {
  background: var(--primary-dark);
}

.input-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: var(--spacing-sm);
  font-size: var(--font-xs);
  color: var(--text-light);
}

.input-stats {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.char-count.warning {
  color: var(--warning-color);
}

.char-count.danger {
  color: var(--danger-color);
}

.input-tips {
  flex: 1;
  text-align: right;
}

.tip {
  opacity: 0.7;
}

/* 移动端适配 */
@media (max-width: 767px) {
  .chat-input-container {
    padding: var(--spacing-sm) var(--spacing-md);
  }
  
  .suggestions-grid {
    grid-template-columns: 1fr;
  }
  
  .suggestion-item {
    padding: var(--spacing-md);
    min-height: 44px; /* iOS 推荐触控面积 */
  }
  
  .input-footer {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--spacing-xs);
  }
  
  .input-tips {
    text-align: left;
  }
  
  .input-toolbar {
    padding: var(--spacing-sm);
    gap: var(--spacing-sm);
  }
  
  .toolbar-btn {
    width: 44px; /* iOS 推荐触控面积 */
    height: 44px;
    font-size: var(--font-lg);
  }
  
  .send-btn {
    width: 48px;
    height: 48px;
    font-size: var(--font-lg);
  }
  
  .chat-input {
    font-size: 16px; /* 防止 iOS Safari 缩放 */
    min-height: 44px;
    padding: var(--spacing-sm) 0;
  }
  
  .file-item {
    max-width: 100%;
    padding: var(--spacing-md);
  }
  
  .file-icon {
    width: 40px;
    height: 40px;
  }
  
  .remove-file-btn {
    padding: var(--spacing-sm);
    min-width: 44px;
    min-height: 44px;
  }
}

/* 平板端适配 */
@media (min-width: 768px) and (max-width: 1023px) {
  .suggestions-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .toolbar-btn {
    width: 36px;
    height: 36px;
  }
  
  .send-btn {
    width: 42px;
    height: 42px;
  }
}

/* 触控设备优化 */
@media (pointer: coarse) {
  .toolbar-btn, .suggestion-item, .remove-file-btn {
    min-height: 44px;
    min-width: 44px;
  }
  
  .send-btn {
    min-width: 48px;
    min-height: 48px;
  }
}
</style>