<template>
  <div class="message" :class="`message-${message.type}`" :data-message-id="message.id">
    <!-- 消息头像 -->
    <div class="message-avatar">
      <div v-if="message.type === 'user'" class="avatar-user">
        {{ getUserInitials() }}
      </div>
      <div v-else-if="message.type === 'assistant'" class="avatar-ai">
        <i class="fas fa-robot"></i>
      </div>
      <div v-else class="avatar-system">
        <i class="fas fa-exclamation-triangle"></i>
      </div>
    </div>

    <!-- 消息内容区 -->
    <div class="message-content">
      <!-- 发送者名称 -->
      <div class="message-sender" v-if="showSender">
        {{ getSenderName() }}
      </div>

      <!-- 消息文本 -->
      <div class="message-text" v-if="message.content">
        <div v-html="formatMessage(message.content)"></div>
      </div>
      
      <!-- 附件显示 -->
      <div class="message-attachments" v-if="message.attachments?.length">
        <div 
          class="attachment"
          v-for="attachment in message.attachments"
          :key="attachment.id"
        >
          <div class="attachment-icon">
            <i :class="getAttachmentIcon(attachment.type)"></i>
          </div>
          <div class="attachment-info">
            <div class="attachment-name">{{ attachment.name }}</div>
            <div class="attachment-size" v-if="attachment.size">
              {{ formatFileSize(attachment.size) }}
            </div>
          </div>
          <button class="attachment-download" @click="downloadAttachment(attachment)">
            <i class="fas fa-download"></i>
          </button>
        </div>
      </div>

      <!-- 加载状态 -->
      <div class="message-loading" v-if="message.status === 'typing'">
        <div class="typing-indicator">
          <div class="typing-dot"></div>
          <div class="typing-dot"></div>
          <div class="typing-dot"></div>
        </div>
        <span class="typing-text">AI正在思考...</span>
      </div>

      <!-- 错误状态 -->
      <div class="message-error" v-if="message.status === 'error'">
        <i class="fas fa-exclamation-circle"></i>
        <span>消息发送失败</span>
        <button class="retry-btn" @click="retryMessage">
          <i class="fas fa-redo"></i>
          重试
        </button>
      </div>

      <!-- 消息元数据 -->
      <div class="message-meta">
        <span class="message-time">{{ formatTime(message.timestamp) }}</span>
        <span class="message-status" v-if="message.status && message.type === 'user'">
          <i :class="getStatusIcon(message.status)"></i>
        </span>
      </div>
    </div>

    <!-- 消息操作按钮 -->
    <div class="message-actions" v-if="showActions">
      <button class="action-btn" @click="copyMessage" title="复制消息">
        <i class="fas fa-copy"></i>
      </button>
      <button 
        class="action-btn" 
        @click="replyMessage" 
        title="回复消息"
        v-if="message.type === 'assistant'"
      >
        <i class="fas fa-reply"></i>
      </button>
      <button 
        class="action-btn" 
        @click="likeMessage" 
        title="点赞"
        v-if="message.type === 'assistant'"
        :class="{ liked: message.liked }"
      >
        <i class="fas fa-thumbs-up"></i>
      </button>
      <button 
        class="action-btn danger" 
        @click="deleteMessage" 
        title="删除消息"
        v-if="canDelete"
      >
        <i class="fas fa-trash"></i>
      </button>
    </div>
  </div>
</template>

<script setup>
import { computed, defineEmits } from 'vue'
import { useAuthStore } from '@/stores/auth'

const props = defineProps({
  message: {
    type: Object,
    required: true
  },
  showSender: {
    type: Boolean,
    default: true
  },
  showActions: {
    type: Boolean,
    default: true
  },
  canDelete: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['retry', 'reply', 'like', 'delete'])
const authStore = useAuthStore()

// 获取用户首字母
const getUserInitials = () => {
  return authStore.userInitials
}

// 获取发送者名称
const getSenderName = () => {
  switch (props.message.type) {
    case 'user':
      return authStore.userName
    case 'assistant':
      return 'AI助手'
    case 'system':
      return '系统'
    default:
      return '未知'
  }
}

// 格式化消息内容
const formatMessage = (content) => {
  if (!content) return ''
  
  // 处理换行
  let formatted = content.replace(/\n/g, '<br>')
  
  // 处理代码块
  formatted = formatted.replace(/```(\w+)?\n?([\s\S]*?)```/g, (match, lang, code) => {
    return `<div class="code-block">
      ${lang ? `<div class="code-lang">${lang}</div>` : ''}
      <pre><code>${code.trim()}</code></pre>
    </div>`
  })
  
  // 处理行内代码
  formatted = formatted.replace(/`([^`]+)`/g, '<code class="inline-code">$1</code>')
  
  // 处理链接
  formatted = formatted.replace(
    /(https?:\/\/[^\s<>"{}|\\^`\[\]]+)/g, 
    '<a href="$1" target="_blank" rel="noopener noreferrer">$1</a>'
  )
  
  // 处理粗体
  formatted = formatted.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
  
  // 处理斜体
  formatted = formatted.replace(/\*(.*?)\*/g, '<em>$1</em>')
  
  return formatted
}

// 获取附件图标
const getAttachmentIcon = (type) => {
  const iconMap = {
    image: 'fas fa-image',
    video: 'fas fa-video',
    audio: 'fas fa-music',
    pdf: 'fas fa-file-pdf',
    doc: 'fas fa-file-word',
    excel: 'fas fa-file-excel',
    zip: 'fas fa-file-archive',
    code: 'fas fa-code',
    text: 'fas fa-file-alt'
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

// 获取状态图标
const getStatusIcon = (status) => {
  const iconMap = {
    sending: 'fas fa-clock',
    sent: 'fas fa-check',
    delivered: 'fas fa-check-double',
    error: 'fas fa-exclamation-triangle'
  }
  return iconMap[status] || ''
}

// 格式化时间
const formatTime = (timestamp) => {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  const now = new Date()
  const diff = now - date
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  
  return date.toLocaleTimeString('zh-CN', { 
    hour: '2-digit', 
    minute: '2-digit' 
  })
}

// 复制消息
const copyMessage = async () => {
  if (!props.message.content) return
  
  try {
    await navigator.clipboard.writeText(props.message.content)
    // 这里可以添加提示
  } catch (error) {
    console.error('复制失败:', error)
  }
}

// 重试发送消息
const retryMessage = () => {
  emit('retry', props.message)
}

// 回复消息
const replyMessage = () => {
  emit('reply', props.message)
}

// 点赞消息
const likeMessage = () => {
  emit('like', props.message)
}

// 删除消息
const deleteMessage = () => {
  if (confirm('确定要删除这条消息吗？')) {
    emit('delete', props.message)
  }
}

// 下载附件
const downloadAttachment = (attachment) => {
  const link = document.createElement('a')
  link.href = attachment.url
  link.download = attachment.name
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}
</script>

<style scoped>
.message {
  display: flex;
  gap: var(--spacing-md);
  padding: var(--spacing-md) var(--spacing-lg);
  animation: message-fade-in var(--duration-normal) var(--ease-out);
  position: relative;
}

.message:hover .message-actions {
  opacity: 1;
}

.message-user {
  flex-direction: row-reverse;
}

.message-user .message-content {
  background: var(--primary-color);
  color: var(--text-white);
  border-radius: var(--radius-lg) var(--radius-lg) var(--radius-sm) var(--radius-lg);
}

.message-assistant .message-content {
  background: var(--bg-secondary);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-lg) var(--radius-lg) var(--radius-lg) var(--radius-sm);
}

.message-error .message-content {
  background: rgba(239, 68, 68, 0.1);
  border: 1px solid var(--danger-color);
  color: var(--danger-color);
}

.message-avatar {
  flex-shrink: 0;
}

.avatar-user, .avatar-ai, .avatar-system {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: var(--font-bold);
  font-size: var(--font-sm);
}

.avatar-user {
  background: var(--primary-color);
  color: var(--text-white);
}

.avatar-ai {
  background: var(--info-color);
  color: var(--text-white);
}

.avatar-system {
  background: var(--warning-color);
  color: var(--text-white);
}

.message-content {
  flex: 1;
  max-width: calc(100% - 80px);
  padding: var(--spacing-md);
  position: relative;
  box-shadow: var(--shadow-sm);
}

.message-sender {
  font-size: var(--font-xs);
  font-weight: var(--font-medium);
  color: var(--text-secondary);
  margin-bottom: var(--spacing-xs);
}

.message-user .message-sender {
  color: rgba(255, 255, 255, 0.8);
}

.message-text {
  font-size: var(--font-base);
  line-height: 1.6;
  word-wrap: break-word;
}

.message-text :deep(a) {
  color: inherit;
  text-decoration: underline;
}

.message-user .message-text :deep(a) {
  color: rgba(255, 255, 255, 0.9);
}

.message-text :deep(.code-block) {
  background: rgba(0, 0, 0, 0.05);
  border-radius: var(--radius-md);
  margin: var(--spacing-sm) 0;
  overflow: hidden;
}

.message-text :deep(.code-lang) {
  background: var(--text-light);
  color: var(--text-white);
  padding: var(--spacing-xs) var(--spacing-sm);
  font-size: var(--font-xs);
  font-weight: var(--font-medium);
}

.message-text :deep(pre) {
  padding: var(--spacing-md);
  overflow-x: auto;
  margin: 0;
}

.message-text :deep(.inline-code) {
  background: rgba(0, 0, 0, 0.1);
  padding: 2px var(--spacing-xs);
  border-radius: var(--radius-sm);
  font-family: var(--font-mono);
  font-size: 0.9em;
}

.message-attachments {
  margin-top: var(--spacing-sm);
}

.attachment {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-sm);
  background: rgba(0, 0, 0, 0.05);
  border-radius: var(--radius-md);
  margin-bottom: var(--spacing-xs);
}

.attachment:last-child {
  margin-bottom: 0;
}

.attachment-icon {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--primary-color);
  color: var(--text-white);
  border-radius: var(--radius-md);
  flex-shrink: 0;
}

.attachment-info {
  flex: 1;
  min-width: 0;
}

.attachment-name {
  font-size: var(--font-sm);
  font-weight: var(--font-medium);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.attachment-size {
  font-size: var(--font-xs);
  color: var(--text-secondary);
}

.attachment-download {
  background: none;
  border: none;
  color: var(--text-secondary);
  cursor: pointer;
  padding: var(--spacing-xs);
  border-radius: var(--radius-sm);
  transition: all var(--duration-fast) var(--ease-in-out);
}

.attachment-download:hover {
  background: var(--primary-color);
  color: var(--text-white);
}

.message-loading {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  color: var(--text-secondary);
}

.typing-indicator {
  display: flex;
  gap: 4px;
}

.typing-dot {
  width: 6px;
  height: 6px;
  border-radius: var(--radius-full);
  background: var(--text-secondary);
  animation: bounce-dots 1.4s infinite ease-in-out;
}

.typing-dot:nth-child(1) { animation-delay: -0.32s; }
.typing-dot:nth-child(2) { animation-delay: -0.16s; }
.typing-dot:nth-child(3) { animation-delay: 0s; }

.typing-text {
  font-size: var(--font-sm);
}

.message-error {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  font-size: var(--font-sm);
}

.retry-btn {
  background: var(--danger-color);
  color: var(--text-white);
  border: none;
  padding: var(--spacing-xs) var(--spacing-sm);
  border-radius: var(--radius-sm);
  font-size: var(--font-xs);
  cursor: pointer;
  transition: all var(--duration-fast) var(--ease-in-out);
}

.retry-btn:hover {
  background: rgba(239, 68, 68, 0.8);
}

.message-meta {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  margin-top: var(--spacing-sm);
  font-size: var(--font-xs);
  color: var(--text-light);
}

.message-user .message-meta {
  color: rgba(255, 255, 255, 0.7);
}

.message-actions {
  position: absolute;
  top: -10px;
  right: var(--spacing-md);
  display: flex;
  gap: var(--spacing-xs);
  opacity: 0;
  transition: opacity var(--duration-fast) var(--ease-in-out);
}

.action-btn {
  width: 32px;
  height: 32px;
  border: none;
  border-radius: var(--radius-full);
  background: var(--bg-secondary);
  color: var(--text-secondary);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--font-sm);
  box-shadow: var(--shadow-md);
  transition: all var(--duration-fast) var(--ease-in-out);
}

.action-btn:hover {
  background: var(--primary-color);
  color: var(--text-white);
  transform: translateY(-1px);
}

.action-btn.liked {
  background: var(--primary-color);
  color: var(--text-white);
}

.action-btn.danger:hover {
  background: var(--danger-color);
}

/* 移动端适配 */
@media (max-width: 767px) {
  .message {
    padding: var(--spacing-sm) var(--spacing-md);
    gap: var(--spacing-sm);
  }
  
  .message-content {
    max-width: calc(100% - 60px);
    padding: var(--spacing-md);
    font-size: var(--font-sm);
    line-height: 1.6;
  }
  
  .avatar-user, .avatar-ai, .avatar-system {
    width: 32px;
    height: 32px;
    font-size: var(--font-xs);
    flex-shrink: 0;
  }
  
  .message-actions {
    position: static;
    opacity: 1;
    margin-top: var(--spacing-sm);
    justify-content: flex-end;
  }
  
  .action-btn {
    width: 40px;
    height: 40px;
    font-size: var(--font-base);
  }
  
  .attachment {
    padding: var(--spacing-md);
  }
  
  .attachment-icon {
    width: 36px;
    height: 36px;
  }
  
  .attachment-download {
    padding: var(--spacing-md);
    min-width: 44px;
    min-height: 44px;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  
  .retry-btn {
    padding: var(--spacing-sm) var(--spacing-md);
    min-height: 44px;
    font-size: var(--font-sm);
  }
  
  .message-text :deep(pre) {
    font-size: var(--font-sm);
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;
  }
  
  .message-text :deep(code) {
    font-size: var(--font-xs);
    word-break: break-all;
  }
}

/* 平板端适配 */
@media (min-width: 768px) and (max-width: 1023px) {
  .message-content {
    max-width: calc(100% - 80px);
  }
  
  .action-btn {
    width: 36px;
    height: 36px;
  }
}

/* 触控设备优化 */
@media (pointer: coarse) {
  .action-btn, .attachment-download, .retry-btn {
    min-width: 44px;
    min-height: 44px;
  }
  
  .message:hover .message-actions {
    opacity: 1; /* 触控设备上始终显示操作按钮 */
  }
}
</style>