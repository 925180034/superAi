# 前端API连接问题排查指南

## 🚨 **问题现象**
- 前端界面正常显示"健康养生助手"
- 后端服务已启动成功
- 前端发送消息时返回 **503 Service Unavailable** 错误
- 浏览器开发者工具显示请求失败

## 🔍 **核心问题分析**

**根本原因**：前端请求的API路径与后端实际提供的接口不匹配

**已确认正确的配置**：
- ✅ `superAi-fronted/super-ai-frontend/src/config/api.js` - API配置正确
- ✅ 后端接口存在：`/api/ai/health/chat/sse/emitter`
- ✅ 后端服务运行正常

**问题所在**：某个前端文件没有使用正确的API配置

## 📂 **需要检查的文件列表**

### **高优先级文件**
1. `superAi-fronted/super-ai-frontend/src/pages/HealthChat.vue`
2. `superAi-fronted/super-ai-frontend/src/utils/sseRequest.js`
3. `superAi-fronted/super-ai-frontend/src/utils/request.js`

### **中优先级文件**
4. `superAi-fronted/super-ai-frontend/src/pages/AISuperChat.vue`
5. `superAi-fronted/super-ai-frontend/src/components/` 目录下的所有 `.vue` 文件

### **低优先级文件**
6. 任何其他包含API调用的 `.js` 或 `.vue` 文件

## 🔎 **搜索关键词和模式**

### **错误模式搜索**
```
搜索以下可能的错误API路径：
- "health/chat/stream"
- "health/chat/messages" 
- "/api/health/chat/stream"
- "/api/health/chat/messages"
- "stream/messages"
- 任何不是 "/ai/health/chat/sse/emitter" 的健康相关API路径
```

### **API调用方式搜索**
```
搜索以下API调用模式：
- "fetch("
- "axios("
- ".post("
- ".get("
- "XMLHttpRequest"
- "EventSource"
```

### **配置使用搜索**
```
检查是否正确使用了配置：
- "API_ENDPOINTS"
- "API_BASE_URL"
- "import.*api"
- "from.*config/api"
```

## 🎯 **具体排查步骤**

### **步骤1：检查 HealthChat.vue**
**搜索内容**：
- 文件中的 `sendMessage` 方法
- 任何包含 `health` 的API调用
- `fetch` 或 `axios` 调用
- 硬编码的 `/api/` 路径

**预期发现**：可能直接硬编码了错误的API路径

### **步骤2：检查 sseRequest.js**
**搜索内容**：
- SSE连接的具体实现
- API路径的拼接逻辑
- 是否使用了正确的 `API_ENDPOINTS.healthChat`

**预期发现**：SSE请求的具体实现可能有问题

### **步骤3：检查导入语句**
**搜索内容**：
```javascript
- "import.*from.*config/api"
- "import.*API_ENDPOINTS"
- "import.*API_BASE_URL"
```

**预期发现**：某些文件可能没有正确导入配置

### **步骤4：检查方法调用**
**搜索内容**：
- `API_ENDPOINTS.healthChat` 的使用
- 任何拼接API路径的代码
- HTTP方法是否正确（POST vs GET）

## 🚨 **常见错误模式**

### **模式1：硬编码路径**
```javascript
// 错误示例
fetch('/api/health/chat/stream/messages', {
  method: 'POST',
  // ...
})

// 正确应该是
fetch(`${API_BASE_URL}${API_ENDPOINTS.healthChat}`, {
  method: 'GET', // 注意：后端接口是GET方法
  // ...
})
```

### **模式2：方法不匹配**
```javascript
// 错误：使用POST方法
fetch(url, { method: 'POST', body: JSON.stringify({...}) })

// 正确：使用GET方法，参数放在URL中
fetch(`${url}?message=${encodeURIComponent(message)}&chatId=${chatId}`)
```

### **模式3：未使用配置文件**
```javascript
// 错误：直接硬编码
const response = await fetch('/api/some/wrong/path')

// 正确：使用配置
import { API_ENDPOINTS } from '@/config/api'
const response = await fetch(`${API_BASE_URL}${API_ENDPOINTS.healthChat}`)
```

## 🛠️ **修复方案模板**

### **如果发现硬编码路径**
```javascript
// 修复前
fetch('/api/health/chat/stream/messages', {
  method: 'POST',
  body: JSON.stringify({ message, chatId })
})

// 修复后
import API_BASE_URL, { API_ENDPOINTS } from '@/config/api'

const url = `${API_BASE_URL}${API_ENDPOINTS.healthChat}?message=${encodeURIComponent(message)}&chatId=${chatId}`
fetch(url, {
  method: 'GET',
  headers: {
    'Accept': 'text/event-stream',
    'Cache-Control': 'no-cache'
  }
})
```

### **如果发现SSE实现问题**
```javascript
// 正确的SSE请求示例
export const sendHealthMessage = async (message, chatId, callbacks) => {
  const url = `${API_BASE_URL}/ai/health/chat/sse/emitter?message=${encodeURIComponent(message)}&chatId=${chatId}`
  
  const response = await fetch(url, {
    method: 'GET',
    headers: {
      'Accept': 'text/event-stream',
      'Cache-Control': 'no-cache'
    }
  })
  
  if (!response.ok) {
    throw new Error(`HTTP ${response.status}: ${response.statusText}`)
  }
  
  const reader = response.body.getReader()
  const decoder = new TextDecoder()
  
  while (true) {
    const { done, value } = await reader.read()
    if (done) break
    
    const chunk = decoder.decode(value)
    // 处理SSE数据...
  }
}
```

## ✅ **验证修复**

修复后应该看到：
1. **浏览器开发者工具**：请求URL为 `/api/ai/health/chat/sse/emitter?message=...&chatId=...`
2. **HTTP状态码**：200 OK
3. **响应类型**：text/event-stream
4. **功能正常**：AI助手能够回复消息

## 📝 **修复完成检查清单**

- [ ] 确认使用了正确的API路径：`/ai/health/chat/sse/emitter`
- [ ] 确认使用了正确的HTTP方法：`GET`
- [ ] 确认参数通过URL传递，不是POST body
- [ ] 确认导入了正确的配置文件
- [ ] 确认SSE流处理逻辑正确
- [ ] 测试功能正常工作

---

**请使用这个指南系统性地排查前端代码，找到具体的问题文件和代码行，然后应用相应的修复方案。**