# AI运动助手前端设计文档

## 📋 项目概述

### 项目信息
- **项目名称**: AI运动助手前端
- **技术栈**: Vue.js 3 + Pinia + Vue Router + Vite
- **UI框架**: 基于现代化设计系统，自定义组件
- **主要功能**: AI聊天界面、思考过程可视化、用户管理
- **设计风格**: 现代简约、运动活力主题

### 核心特性
- 响应式设计，支持桌面端、平板、手机
- 实时AI思考过程可视化
- 工具调用过程展示
- MCP服务状态监控
- 实时日志显示
- 多模块聊天功能

## 🎨 设计系统

### 颜色规范
```css
:root {
  /* 主色调 */
  --primary-color: #3b82f6;      /* 主蓝色 */
  --primary-light: #60a5fa;      /* 浅蓝色 */
  --primary-dark: #1d4ed8;       /* 深蓝色 */
  
  /* 功能色 */
  --success-color: #10b981;      /* 成功绿 */
  --warning-color: #f59e0b;      /* 警告橙 */
  --danger-color: #ef4444;       /* 错误红 */
  --info-color: #06b6d4;         /* 信息青 */
  
  /* 背景色 */
  --bg-primary: #f8fafc;         /* 主背景 */
  --bg-secondary: #ffffff;       /* 次背景 */
  --bg-sidebar: #1e293b;         /* 侧边栏背景 */
  
  /* 文字色 */
  --text-primary: #1f2937;       /* 主文字 */
  --text-secondary: #6b7280;     /* 次文字 */
  --text-light: #9ca3af;         /* 浅色文字 */
  
  /* 边框和阴影 */
  --border-color: #e5e7eb;
  --shadow-sm: 0 1px 2px 0 rgb(0 0 0 / 0.05);
  --shadow-md: 0 4px 6px -1px rgb(0 0 0 / 0.1);
  --shadow-lg: 0 10px 15px -3px rgb(0 0 0 / 0.1);
  
  /* 渐变色 */
  --gradient-primary: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  --gradient-fitness: linear-gradient(135deg, #ff7e5f 0%, #feb47b 100%);
}
```

### 字体规范
```css
/* 主字体 */
font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;

/* 代码字体 */
font-family: 'Monaco', 'Consolas', 'SF Mono', monospace;

/* 字体大小 */
--font-xs: 12px;
--font-sm: 14px;
--font-base: 16px;
--font-lg: 18px;
--font-xl: 20px;
--font-2xl: 24px;
--font-3xl: 30px;
```

### 间距规范
```css
--spacing-xs: 4px;
--spacing-sm: 8px;
--spacing-md: 16px;
--spacing-lg: 24px;
--spacing-xl: 32px;
--spacing-2xl: 48px;
```

### 圆角规范
```css
--radius-sm: 6px;
--radius-md: 8px;
--radius-lg: 12px;
--radius-xl: 16px;
--radius-full: 9999px;
```

## 📱 布局设计

### 整体布局
```
┌─────────────────────────────────────────────────┐
│                   应用容器                        │
├─────────────────┬───────────────────────────────┤
│                 │           顶部导航              │
│     侧边栏       ├───────────────────────────────┤
│                 │                               │
│   - Logo区域     │         主内容区               │
│   - 用户信息     │                               │
│   - 新对话      │       - 欢迎界面               │
│   - 功能菜单     │       - 聊天消息               │
│   - 历史对话     │       - 思考过程               │
│   - 设置菜单     │       - 工具调用               │
│                 │       - MCP状态               │
│                 │       - 实时日志               │
│                 │                               │
│                 ├───────────────────────────────┤
│                 │          输入区域              │
└─────────────────┴───────────────────────────────┘
```

### 响应式断点
- **桌面端**: >= 1024px
- **平板端**: 768px - 1023px  
- **手机端**: < 768px

### 移动端适配
- 侧边栏变为抽屉式（从左侧滑出）
- 主内容区占满全屏
- 触摸友好的按钮尺寸（最小44px）
- 优化的输入体验

## 🧩 组件架构

### 1. 路由组件 (views/)

#### 1.1 认证页面
```
views/auth/
├── Login.vue          # 登录页面
├── Register.vue       # 注册页面  
└── ForgotPassword.vue # 忘记密码页面
```

**Login.vue 设计要求**:
- 居中卡片式布局
- 运动主题背景
- 表单验证
- 记住密码功能
- 第三方登录（可选）

**Register.vue 设计要求**:
- 多步骤注册流程
- 健身水平选择
- 目标设定
- 邮箱验证

#### 1.2 主功能页面
```
views/fitness/
├── FitnessChat.vue      # 主聊天界面
├── TrainingPlans.vue    # 训练计划管理
├── ExerciseLibrary.vue  # 动作库
└── DataAnalysis.vue     # 数据分析
```

**FitnessChat.vue 设计要求**:
- 集成所有可视化组件
- 消息列表滚动优化
- 输入框自适应高度
- 文件上传支持

#### 1.3 用户页面
```
views/user/
├── Profile.vue        # 个人资料
├── Settings.vue       # 设置页面
└── ChatHistory.vue    # 聊天历史
```

### 2. 布局组件 (components/layout/)

#### 2.1 侧边栏组件 (Sidebar.vue)
```vue
<template>
  <div class="sidebar" :class="{ active: isMobileMenuOpen }">
    <!-- Logo区域 -->
    <div class="sidebar-header">
      <div class="logo">
        <i class="fas fa-dumbbell"></i>
        <span>AI 运动助手</span>
      </div>
      <div class="status-indicator status-online">
        <div class="status-dot"></div>
        在线服务
      </div>
    </div>

    <!-- 用户信息 -->
    <div class="user-info">
      <div class="user-avatar">{{ userInitials }}</div>
      <div class="user-details">
        <h4>{{ user.name }}</h4>
        <p>{{ user.fitnessLevel }}</p>
      </div>
    </div>

    <!-- 新对话按钮 -->
    <button class="new-chat-btn" @click="startNewChat">
      <i class="fas fa-plus"></i>
      新建对话
    </button>

    <!-- 功能菜单 -->
    <div class="sidebar-menu">
      <div class="menu-section">
        <div class="menu-title">应用功能</div>
        <div class="menu-item" 
             v-for="app in apps" 
             :key="app.id"
             :class="{ active: currentApp === app.id }"
             @click="switchApp(app.id)">
          <i :class="app.icon"></i>
          <span>{{ app.name }}</span>
        </div>
      </div>

      <!-- 历史对话 -->
      <div class="menu-section">
        <div class="menu-title">最近对话</div>
        <div class="chat-history">
          <div class="chat-item"
               v-for="chat in recentChats"
               :key="chat.id"
               :class="{ active: currentChatId === chat.id }"
               @click="loadChat(chat.id)">
            <div class="chat-title">{{ chat.title }}</div>
            <div class="chat-time">{{ formatTime(chat.updatedAt) }}</div>
          </div>
        </div>
      </div>

      <!-- 设置菜单 -->
      <div class="menu-section">
        <div class="menu-title">设置</div>
        <div class="menu-item" @click="openSettings">
          <i class="fas fa-cog"></i>
          <span>偏好设置</span>
        </div>
        <div class="menu-item" @click="exportData">
          <i class="fas fa-download"></i>
          <span>导出数据</span>
        </div>
        <div class="menu-item" @click="logout">
          <i class="fas fa-sign-out-alt"></i>
          <span>退出登录</span>
        </div>
      </div>
    </div>
  </div>
</template>
```

**样式要求**:
- 固定宽度280px
- 深色主题背景
- 悬停效果和活跃状态
- 移动端抽屉动画

#### 2.2 顶部导航 (TopNav.vue)
```vue
<template>
  <div class="top-nav">
    <div class="nav-left">
      <i class="fas fa-bars mobile-menu-toggle" @click="toggleMobileMenu"></i>
      <div class="nav-title">{{ currentPageTitle }}</div>
    </div>
    
    <div class="nav-actions">
      <button class="nav-btn" @click="clearChat" title="清空对话">
        <i class="fas fa-trash"></i>
        <span class="btn-text">清空对话</span>
      </button>
      <button class="nav-btn" @click="shareChat" title="分享对话">
        <i class="fas fa-share"></i>
        <span class="btn-text">分享</span>
      </button>
      <div class="user-menu" @click="toggleUserMenu">
        <div class="user-avatar-small">{{ userInitials }}</div>
        <i class="fas fa-chevron-down"></i>
      </div>
    </div>
  </div>
</template>
```

#### 2.3 聊天输入 (ChatInput.vue)
```vue
<template>
  <div class="chat-input-container">
    <div class="chat-input-wrapper">
      <!-- 文件上传区域 -->
      <div class="file-upload" v-if="selectedFiles.length > 0">
        <div class="file-list">
          <div class="file-item" v-for="file in selectedFiles" :key="file.id">
            <i class="fas fa-file"></i>
            <span>{{ file.name }}</span>
            <button @click="removeFile(file.id)">
              <i class="fas fa-times"></i>
            </button>
          </div>
        </div>
      </div>

      <!-- 输入工具栏 -->
      <div class="input-toolbar">
        <button class="toolbar-btn" @click="openFileUpload" title="上传文件">
          <i class="fas fa-paperclip"></i>
        </button>
        <button class="toolbar-btn" @click="openImageUpload" title="上传图片">
          <i class="fas fa-image"></i>
        </button>
        <button class="toolbar-btn" @click="insertTemplate" title="快速模板">
          <i class="fas fa-magic"></i>
        </button>
      </div>

      <!-- 输入框 -->
      <textarea 
        class="chat-input" 
        v-model="message"
        @keydown="handleKeyDown"
        @input="adjustHeight"
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
        :class="{ 'has-message': message.trim() }"
      >
        <i class="fas fa-paper-plane"></i>
      </button>
    </div>

    <!-- 输入提示 -->
    <div class="input-hints" v-if="showHints">
      <div class="hint-item" v-for="hint in hints" :key="hint.id" @click="useHint(hint)">
        <i :class="hint.icon"></i>
        <span>{{ hint.text }}</span>
      </div>
    </div>
  </div>
</template>
```

### 3. 思考过程组件 (components/process/)

#### 3.1 思考过程组件 (ThinkingProcess.vue)
```vue
<template>
  <div class="thinking-process" v-show="isThinking">
    <div class="thinking-bubble" :class="{ active: isThinking }">
      <div class="thinking-header">
        <i class="fas fa-brain thinking-icon"></i>
        <span class="thinking-label">AI 正在思考</span>
        <div class="thinking-dots">
          <div class="thinking-dot"></div>
          <div class="thinking-dot"></div>
          <div class="thinking-dot"></div>
        </div>
      </div>
      <div class="thinking-content">
        <div class="thinking-text">{{ displayText }}</div>
        <div class="thinking-progress" v-if="showProgress">
          <div class="progress-bar">
            <div class="progress-fill" :style="{ width: progress + '%' }"></div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
```

**交互特性**:
- 打字机效果显示思考内容
- 脉冲动画边框
- 可选进度条显示
- 平滑淡入淡出动画

#### 3.2 工具时间线组件 (ToolTimeline.vue)
```vue
<template>
  <div class="tool-timeline-container" v-if="tools.length > 0">
    <!-- 时间线标题 -->
    <div class="timeline-header">
      <h4><i class="fas fa-tools"></i> 工具调用过程</h4>
      <div class="timeline-summary">
        <span class="completed-count">{{ completedTools }}</span>
        <span class="separator">/</span>
        <span class="total-count">{{ tools.length }}</span>
        <span class="summary-text">完成</span>
      </div>
    </div>

    <!-- 总体进度 -->
    <div class="overall-progress">
      <div class="progress-bar">
        <div class="progress-fill" :style="{ width: progressPercentage + '%' }"></div>
      </div>
      <span class="progress-text">{{ Math.round(progressPercentage) }}%</span>
    </div>

    <!-- 工具步骤 -->
    <div class="tool-timeline">
      <div 
        class="tool-step"
        v-for="(tool, index) in tools"
        :key="`tool-${index}-${tool.id}`"
        :class="getStepClass(tool)"
      >
        <!-- 时间线节点 -->
        <div class="timeline-node">
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
          </div>

          <div class="tool-description">{{ tool.description }}</div>

          <!-- 参数展示 -->
          <div class="tool-params" v-if="tool.parameters">
            <div class="params-header">
              <i class="fas fa-cog"></i>
              参数:
            </div>
            <div class="params-content">
              <div class="param-item" 
                   v-for="(value, key) in tool.parameters" 
                   :key="key">
                <span class="param-key">{{ key }}:</span>
                <span class="param-value">{{ formatParamValue(value) }}</span>
              </div>
            </div>
          </div>

          <!-- 执行结果 -->
          <div class="tool-result" v-if="tool.result">
            <div class="result-header">
              <i class="fas fa-check-circle"></i>
              执行结果:
            </div>
            <div class="result-content">{{ tool.result }}</div>
          </div>

          <!-- 错误信息 -->
          <div class="tool-error" v-if="tool.error">
            <div class="error-header">
              <i class="fas fa-exclamation-triangle"></i>
              错误信息:
            </div>
            <div class="error-content">{{ tool.error }}</div>
          </div>

          <!-- 执行时间 -->
          <div class="tool-timing" v-if="tool.executionTime">
            <i class="fas fa-clock"></i>
            执行时间: {{ tool.executionTime }}ms
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
```

**视觉特性**:
- 垂直时间线布局
- 状态颜色编码（pending/running/completed/error）
- 动画进度指示
- 展开/折叠详细信息

#### 3.3 MCP状态组件 (MCPStatus.vue)
```vue
<template>
  <div class="mcp-status-container" v-if="servers.length > 0">
    <div class="mcp-header">
      <h4><i class="fas fa-network-wired"></i> MCP 服务状态</h4>
      <div class="mcp-summary">
        <span class="online-count">{{ onlineServers }}</span>
        <span class="separator">/</span>
        <span class="total-count">{{ servers.length }}</span>
        <span class="summary-text">在线</span>
      </div>
    </div>

    <div class="mcp-servers-grid">
      <div 
        class="mcp-server"
        v-for="server in servers"
        :key="server.name"
        :class="`server-${server.status}`"
      >
        <div class="server-icon">
          <i :class="getServerIcon(server.type)"></i>
        </div>
        
        <div class="server-info">
          <div class="server-name">{{ server.displayName }}</div>
          <div class="server-type">{{ server.type }}</div>
        </div>
        
        <div class="server-status">
          <div class="status-dot" :class="server.status"></div>
          <span class="status-text">{{ getStatusText(server.status) }}</span>
        </div>

        <!-- 连接详情 -->
        <div class="server-details" v-if="server.status === 'connected'">
          <div class="detail-row">
            <i class="fas fa-clock"></i>
            <span>连接 {{ getUptime(server.connectedAt) }}</span>
          </div>
          <div class="detail-row" v-if="server.lastCall">
            <i class="fas fa-history"></i>
            <span>{{ getLastCall(server.lastCall) }}</span>
          </div>
        </div>

        <!-- 错误信息 -->
        <div class="server-error" v-if="server.status === 'error'">
          <i class="fas fa-exclamation-circle"></i>
          <span>{{ server.error || '连接失败' }}</span>
        </div>
      </div>
    </div>
  </div>
</template>
```

#### 3.4 实时日志组件 (RealtimeLog.vue)
```vue
<template>
  <div class="realtime-log-container" v-if="logs.length > 0">
    <div class="log-header">
      <h4><i class="fas fa-terminal"></i> 实时执行日志</h4>
      <div class="log-controls">
        <button class="log-control-btn" @click="toggleAutoScroll" :class="{ active: autoScroll }">
          <i class="fas fa-arrow-down"></i>
          <span>自动滚动</span>
        </button>
        <button class="log-control-btn" @click="clearLogs">
          <i class="fas fa-trash"></i>
          <span>清空日志</span>
        </button>
        <button class="log-control-btn" @click="downloadLogs">
          <i class="fas fa-download"></i>
          <span>下载日志</span>
        </button>
      </div>
    </div>

    <div class="log-content" ref="logContainer">
      <div 
        class="log-line"
        v-for="(log, index) in logs"
        :key="`log-${index}-${log.timestamp}`"
        :class="`log-level-${log.level}`"
      >
        <span class="log-timestamp">{{ formatTimestamp(log.timestamp) }}</span>
        <span class="log-level-badge">{{ log.level.toUpperCase() }}</span>
        <span class="log-message">{{ log.message }}</span>
        <span class="log-source" v-if="log.source">[{{ log.source }}]</span>
      </div>
    </div>

    <!-- 日志过滤器 -->
    <div class="log-filters" v-if="showFilters">
      <button 
        class="filter-btn"
        v-for="level in logLevels"
        :key="level"
        :class="{ active: activeFilters.includes(level) }"
        @click="toggleFilter(level)"
      >
        {{ level.toUpperCase() }}
      </button>
    </div>
  </div>
</template>
```

### 4. 通用组件 (components/common/)

#### 4.1 消息气泡 (MessageBubble.vue)
```vue
<template>
  <div class="message" :class="`message-${message.type}`">
    <div class="message-avatar">
      <img v-if="message.avatar" :src="message.avatar" :alt="message.sender" />
      <div v-else class="avatar-placeholder">
        {{ getAvatarText(message) }}
      </div>
    </div>

    <div class="message-content">
      <!-- 消息内容 -->
      <div class="message-text" v-html="formatMessage(message.content)"></div>
      
      <!-- 附件 -->
      <div class="message-attachments" v-if="message.attachments">
        <div 
          class="attachment"
          v-for="attachment in message.attachments"
          :key="attachment.id"
        >
          <i :class="getAttachmentIcon(attachment.type)"></i>
          <span>{{ attachment.name }}</span>
        </div>
      </div>

      <!-- 消息元数据 -->
      <div class="message-meta">
        <span class="message-time">{{ formatTime(message.timestamp) }}</span>
        <span class="message-status" v-if="message.status">
          <i :class="getStatusIcon(message.status)"></i>
        </span>
      </div>
    </div>

    <!-- 消息操作 -->
    <div class="message-actions" v-if="showActions">
      <button class="action-btn" @click="copyMessage" title="复制">
        <i class="fas fa-copy"></i>
      </button>
      <button class="action-btn" @click="replyMessage" title="回复">
        <i class="fas fa-reply"></i>
      </button>
      <button class="action-btn" @click="likeMessage" title="点赞">
        <i class="fas fa-thumbs-up"></i>
      </button>
    </div>
  </div>
</template>
```

#### 4.2 加载指示器 (LoadingSpinner.vue)
```vue
<template>
  <div class="loading-container" :class="{ inline, centered }">
    <div class="loading-spinner" :class="size">
      <div class="spinner-ring"></div>
      <div class="spinner-ring"></div>
      <div class="spinner-ring"></div>
    </div>
    <div class="loading-text" v-if="text">{{ text }}</div>
  </div>
</template>
```

#### 4.3 快速开始卡片 (QuickStartCard.vue)
```vue
<template>
  <div class="quick-start-grid">
    <div 
      class="quick-card"
      v-for="item in quickStartItems"
      :key="item.id"
      @click="selectQuickStart(item)"
    >
      <div class="card-icon">
        <i :class="item.icon"></i>
      </div>
      <h3 class="card-title">{{ item.title }}</h3>
      <p class="card-description">{{ item.description }}</p>
      <div class="card-tags">
        <span class="tag" v-for="tag in item.tags" :key="tag">{{ tag }}</span>
      </div>
    </div>
  </div>
</template>
```

## 🗂️ 状态管理 (Pinia Stores)

### 1. 认证状态 (stores/auth.js)
```javascript
export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: null,
    token: null,
    isLoading: false,
    loginAttempts: 0
  }),
  
  getters: {
    isAuthenticated: (state) => !!state.token,
    userInitials: (state) => {
      if (!state.user?.name) return 'U'
      return state.user.name.split(' ').map(n => n[0]).join('').toUpperCase()
    },
    userRole: (state) => state.user?.role || 'user'
  },
  
  actions: {
    async login(credentials) { /* 实现登录逻辑 */ },
    async register(userData) { /* 实现注册逻辑 */ },
    async logout() { /* 实现登出逻辑 */ },
    async refreshToken() { /* 实现token刷新 */ }
  }
})
```

### 2. 聊天状态 (stores/chat.js)
```javascript
export const useChatStore = defineStore('chat', {
  state: () => ({
    currentChatId: null,
    currentMessages: [],
    recentChats: [],
    isLoading: false,
    isMobileMenuOpen: false
  }),
  
  actions: {
    async sendMessage(content) { /* 发送消息逻辑 */ },
    async loadChat(chatId) { /* 加载聊天记录 */ },
    startNewChat() { /* 开始新对话 */ },
    addMessage(message) { /* 添加消息到当前对话 */ }
  }
})
```

### 3. 处理状态 (stores/processing.js)
```javascript
export const useProcessingStore = defineStore('processing', {
  state: () => ({
    isThinking: false,
    thinkingText: '',
    tools: [],
    mcpServers: [],
    logs: [],
    isProcessing: false
  }),
  
  actions: {
    updateThinking(text) { /* 更新思考内容 */ },
    addTool(tool) { /* 添加工具 */ },
    updateTool(toolId, update) { /* 更新工具状态 */ },
    addLog(log) { /* 添加日志 */ },
    reset() { /* 重置所有状态 */ }
  }
})
```

## 📱 响应式设计

### 断点定义
```css
/* 移动端 */
@media (max-width: 767px) {
  .sidebar {
    position: fixed;
    left: -280px;
    z-index: 1000;
    transition: transform 0.3s ease;
  }
  
  .sidebar.active {
    transform: translateX(280px);
  }
  
  .main-content {
    margin-left: 0;
  }
}

/* 平板端 */
@media (min-width: 768px) and (max-width: 1023px) {
  .sidebar {
    width: 260px;
  }
  
  .quick-start-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

/* 桌面端 */
@media (min-width: 1024px) {
  .sidebar {
    width: 280px;
  }
  
  .main-content {
    margin-left: 280px;
  }
}
```

### 触摸优化
- 按钮最小尺寸44px x 44px
- 增大触摸目标间距
- 支持滑动手势
- 防止误触设计

## 🎭 动画和交互

### 关键动画
```css
/* 思考气泡脉冲 */
@keyframes thinking-pulse {
  0%, 100% { border-color: var(--primary-color); }
  50% { border-color: var(--info-color); }
}

/* 工具节点旋转 */
@keyframes tool-loading {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 消息淡入 */
@keyframes message-fade-in {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

/* 日志滚动出现 */
@keyframes log-appear {
  from { opacity: 0; transform: translateX(-10px); }
  to { opacity: 1; transform: translateX(0); }
}
```

### 交互状态
- **Hover**: 轻微上移 + 阴影加深
- **Active**: 轻微下压效果
- **Focus**: 明显边框高亮
- **Disabled**: 透明度50% + 禁用光标

## 📡 API 集成

### SSE 连接处理
```javascript
// SSE事件处理示例
const connectSSE = (message) => {
  const eventSource = new EventSource(`/api/fitness/chat-stream?message=${encodeURIComponent(message)}`)
  
  eventSource.onmessage = (event) => {
    const data = JSON.parse(event.data)
    
    switch (data.type) {
      case 'thinking':
        processingStore.updateThinking(data.content)
        break
      case 'tool_start':
        processingStore.addTool(data.tool)
        break
      case 'tool_update':
        processingStore.updateTool(data.toolId, data.update)
        break
      case 'response':
        chatStore.addMessage(data.message)
        processingStore.complete()
        eventSource.close()
        break
    }
  }
  
  eventSource.onerror = (error) => {
    console.error('SSE连接错误:', error)
    processingStore.error('连接中断')
    eventSource.close()
  }
}
```

## 🛠️ 开发工具配置

### package.json
```json
{
  "name": "ai-fitness-assistant",
  "version": "1.0.0",
  "dependencies": {
    "vue": "^3.3.0",
    "vue-router": "^4.2.0",
    "pinia": "^2.1.0",
    "axios": "^1.4.0",
    "@fortawesome/fontawesome-free": "^6.4.0"
  },
  "devDependencies": {
    "@vitejs/plugin-vue": "^4.2.0",
    "vite": "^4.3.0",
    "sass": "^1.62.0"
  }
}
```

### vite.config.js
```javascript
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  },
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8123',
        changeOrigin: true
      }
    }
  }
})
```

## 📋 功能检查清单

### 基础功能
- [ ] 用户登录/注册
- [ ] 侧边栏导航
- [ ] 聊天界面
- [ ] 消息发送/接收
- [ ] 文件上传
- [ ] 响应式布局

### 可视化功能  
- [ ] AI思考过程显示
- [ ] 工具调用时间线
- [ ] MCP服务状态
- [ ] 实时日志显示
- [ ] 进度指示器

### 交互功能
- [ ] 打字机效果
- [ ] 自动滚动
- [ ] 状态动画
- [ ] 错误处理
- [ ] 加载状态

### 优化功能
- [ ] 虚拟滚动（大量消息）
- [ ] 图片懒加载
- [ ] 缓存策略
- [ ] 离线支持
- [ ] PWA功能

这份文档提供了完整的前端设计规范，AI开发者可以根据这个文档完整实现所有功能。需要我继续创建后端开发文档吗？