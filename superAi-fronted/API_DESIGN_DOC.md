# AI运动助手 - 前后端接口设计文档

## 📋 项目概述

### 项目信息
- **项目名称**: AI运动助手 (Super AI)
- **前端技术栈**: Vue.js 3 + Pinia + Vue Router + Vite
- **后端技术栈**: 待实现 (推荐 FastAPI/Django/Express.js)
- **数据库**: 推荐 PostgreSQL/MySQL + Redis
- **部署方式**: Docker容器化部署

### 应用模块
1. **AI健身助手** (`/fitness`) - 健身训练指导、运动计划制定
2. **AI超级助手** (`/manus`) - 通用AI助手、学习工作生活支持

---

## 🔐 认证系统

### 1.1 用户注册
**接口**: `POST /api/auth/register`

**请求体**:
```json
{
  "name": "用户姓名",
  "email": "user@example.com",
  "password": "密码",
  "fitnessLevel": "初级|中级|高级", // 健身水平
  "profile": {
    "age": 28,
    "gender": "male|female|other",
    "height": 175, // cm
    "weight": 70,  // kg
    "goals": ["muscle_gain", "weight_loss", "strength", "endurance"],
    "bio": "个人简介"
  }
}
```

**响应体**:
```json
{
  "success": true,
  "data": {
    "token": "jwt_token_string",
    "user": {
      "id": 1,
      "name": "用户姓名",
      "email": "user@example.com",
      "role": "user",
      "fitnessLevel": "中级",
      "profile": {
        "age": 28,
        "gender": "male",
        "height": 175,
        "weight": 70,
        "goals": ["muscle_gain", "strength"],
        "bio": "热爱健身的用户"
      },
      "createdAt": "2024-01-01T00:00:00Z",
      "updatedAt": "2024-01-01T00:00:00Z"
    }
  },
  "message": "注册成功"
}
```

### 1.2 用户登录
**接口**: `POST /api/auth/login`

**请求体**:
```json
{
  "email": "user@example.com",
  "password": "密码"
}
```

**响应体**:
```json
{
  "success": true,
  "data": {
    "token": "jwt_token_string",
    "user": {
      "id": 1,
      "name": "用户姓名",
      "email": "user@example.com",
      "role": "user",
      "fitnessLevel": "中级",
      "profile": { /* 用户资料 */ }
    }
  },
  "message": "登录成功"
}
```

### 1.3 刷新Token
**接口**: `POST /api/auth/refresh`

**请求头**: `Authorization: Bearer {token}`

**响应体**:
```json
{
  "success": true,
  "data": {
    "token": "new_jwt_token_string"
  }
}
```

### 1.4 获取用户信息
**接口**: `GET /api/auth/user`

**请求头**: `Authorization: Bearer {token}`

**响应体**:
```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "用户姓名",
    "email": "user@example.com",
    "role": "user",
    "fitnessLevel": "中级",
    "profile": { /* 完整用户资料 */ }
  }
}
```

### 1.5 用户登出
**接口**: `POST /api/auth/logout`

**请求头**: `Authorization: Bearer {token}`

**响应体**:
```json
{
  "success": true,
  "message": "登出成功"
}
```

---

## 💬 聊天系统

### 2.1 AI健身助手聊天
**接口**: `POST /api/fitness/chat`

**请求体**:
```json
{
  "message": "我想制定一个健身计划",
  "attachments": [
    {
      "id": "file_id",
      "name": "health_report.pdf",
      "type": "pdf",
      "url": "file_url",
      "size": 1024000
    }
  ],
  "chatId": "chat_id_optional", // 可选，续接对话
  "app": "fitness"
}
```

**响应体**:
```json
{
  "success": true,
  "data": {
    "message": {
      "id": "msg_id",
      "type": "assistant",
      "content": "AI回复内容",
      "timestamp": "2024-01-01T00:00:00Z",
      "status": "received"
    },
    "chatId": "chat_id",
    "processingInfo": {
      "thinkingSteps": ["分析用户需求", "制定计划", "生成建议"],
      "toolsUsed": ["fitness_planner", "exercise_database"],
      "processingTime": 2500
    }
  }
}
```

### 2.2 AI超级助手聊天
**接口**: `POST /api/manus/chat`

**请求体**: (同AI健身助手，app字段为"manus")

**响应体**: (同AI健身助手)

### 2.3 流式聊天接口 (SSE)
**接口**: `GET /api/fitness/chat-stream` 或 `GET /api/manus/chat-stream`

**查询参数**:
- `message`: 用户消息内容 (URL编码)
- `app`: 应用类型 (fitness/manus)
- `chatId`: 对话ID (可选)

**SSE事件流**:
```javascript
// 思考过程事件
data: {"type": "thinking", "content": "正在分析您的问题...", "stage": "analyzing"}

// 工具调用开始
data: {"type": "tool_start", "tool": {"id": "tool_1", "name": "fitness_planner", "description": "制定健身计划"}}

// 工具调用更新
data: {"type": "tool_update", "toolId": "tool_1", "update": {"status": "running", "progress": 50}}

// 工具调用完成
data: {"type": "tool_complete", "toolId": "tool_1", "result": "计划制定完成"}

// AI回复内容流
data: {"type": "content", "content": "根据您的"}
data: {"type": "content", "content": "情况，我建议"}
data: {"type": "content", "content": "您从基础训练开始..."}

// 回复完成
data: {"type": "complete", "messageId": "msg_id", "chatId": "chat_id"}

// 错误处理
data: {"type": "error", "error": "处理失败，请重试"}
```

### 2.4 获取聊天历史
**接口**: `GET /api/chat/{chatId}`

**请求头**: `Authorization: Bearer {token}`

**响应体**:
```json
{
  "success": true,
  "data": {
    "id": "chat_id",
    "title": "如何开始健身训练？",
    "app": "fitness",
    "messages": [
      {
        "id": "msg_1",
        "type": "user",
        "content": "我想开始健身训练",
        "timestamp": "2024-01-01T00:00:00Z",
        "attachments": []
      },
      {
        "id": "msg_2", 
        "type": "assistant",
        "content": "很好的决定！让我为您制定计划...",
        "timestamp": "2024-01-01T00:01:00Z"
      }
    ],
    "createdAt": "2024-01-01T00:00:00Z",
    "updatedAt": "2024-01-01T00:01:00Z"
  }
}
```

### 2.5 获取最近聊天列表
**接口**: `GET /api/chat/recent`

**查询参数**:
- `limit`: 限制数量 (默认20)
- `app`: 筛选应用 (可选)

**请求头**: `Authorization: Bearer {token}`

**响应体**:
```json
{
  "success": true,
  "data": {
    "chats": [
      {
        "id": "chat_1",
        "title": "如何开始健身训练？",
        "app": "fitness",
        "lastMessage": "建议从基础动作开始，每周3-4次训练...",
        "updatedAt": "2024-01-01T00:00:00Z",
        "messageCount": 8
      },
      {
        "id": "chat_2",
        "title": "学习新技能的最佳方法",
        "app": "manus", 
        "lastMessage": "制定明确的学习计划很重要...",
        "updatedAt": "2024-01-01T00:00:00Z",
        "messageCount": 6
      }
    ],
    "total": 15,
    "hasMore": true
  }
}
```

### 2.6 删除聊天
**接口**: `DELETE /api/chat/{chatId}`

**请求头**: `Authorization: Bearer {token}`

**响应体**:
```json
{
  "success": true,
  "message": "聊天记录已删除"
}
```

---

## 📁 文件上传系统

### 3.1 文件上传
**接口**: `POST /api/upload`

**请求体**: `multipart/form-data`
- `files`: 文件数组
- `type`: 文件类型 (image/document/other)

**请求头**: `Authorization: Bearer {token}`

**响应体**:
```json
{
  "success": true,
  "data": {
    "files": [
      {
        "id": "file_id_1",
        "name": "workout_plan.pdf",
        "originalName": "我的健身计划.pdf",
        "type": "pdf",
        "size": 1024000,
        "url": "https://storage.example.com/files/file_id_1.pdf",
        "thumbnailUrl": "https://storage.example.com/thumbnails/file_id_1.jpg",
        "uploadedAt": "2024-01-01T00:00:00Z"
      }
    ]
  }
}
```

### 3.2 获取文件信息
**接口**: `GET /api/upload/{fileId}`

**请求头**: `Authorization: Bearer {token}`

**响应体**:
```json
{
  "success": true,
  "data": {
    "id": "file_id_1",
    "name": "workout_plan.pdf",
    "originalName": "我的健身计划.pdf", 
    "type": "pdf",
    "size": 1024000,
    "url": "https://storage.example.com/files/file_id_1.pdf",
    "downloadUrl": "https://api.example.com/api/upload/file_id_1/download",
    "metadata": {
      "pages": 5,
      "author": "用户姓名"
    },
    "uploadedAt": "2024-01-01T00:00:00Z"
  }
}
```

### 3.3 文件下载
**接口**: `GET /api/upload/{fileId}/download`

**请求头**: `Authorization: Bearer {token}`

**响应**: 文件二进制流

---

## ⚡ AI处理过程接口

### 4.1 获取MCP服务器状态
**接口**: `GET /api/mcp/status`

**请求头**: `Authorization: Bearer {token}`

**响应体**:
```json
{
  "success": true,
  "data": {
    "servers": [
      {
        "name": "fitness-tools",
        "displayName": "健身工具服务",
        "type": "fitness",
        "status": "connected",
        "connectedAt": "2024-01-01T00:00:00Z",
        "lastCall": "2024-01-01T00:05:00Z",
        "version": "1.0.0",
        "capabilities": ["exercise_planning", "nutrition_advice"],
        "metrics": {
          "uptime": 3600,
          "totalCalls": 150,
          "avgResponseTime": 250
        }
      },
      {
        "name": "manus-tools",
        "displayName": "超级助手工具服务",
        "type": "manus",
        "status": "connected",
        "connectedAt": "2024-01-01T00:00:00Z",
        "lastCall": "2024-01-01T00:03:00Z",
        "capabilities": ["web_search", "code_analysis", "text_processing"]
      }
    ]
  }
}
```

### 4.2 获取处理日志
**接口**: `GET /api/processing/logs`

**查询参数**:
- `limit`: 限制数量 (默认50)
- `level`: 日志级别筛选 (info/warning/error/debug)
- `since`: 起始时间 (ISO格式)

**请求头**: `Authorization: Bearer {token}`

**响应体**:
```json
{
  "success": true,
  "data": {
    "logs": [
      {
        "id": "log_1",
        "level": "info",
        "message": "开始AI思考过程: analyzing",
        "source": "fitness-chat",
        "timestamp": "2024-01-01T00:00:00Z",
        "metadata": {
          "userId": 1,
          "chatId": "chat_1",
          "processingTime": 150
        }
      }
    ],
    "total": 100,
    "hasMore": true
  }
}
```

---

## 👤 用户管理

### 5.1 更新用户资料
**接口**: `PUT /api/user/profile`

**请求头**: `Authorization: Bearer {token}`

**请求体**:
```json
{
  "name": "新姓名",
  "fitnessLevel": "高级",
  "profile": {
    "age": 29,
    "weight": 68,
    "goals": ["strength", "endurance"],
    "bio": "更新的个人简介"
  }
}
```

**响应体**:
```json
{
  "success": true,
  "data": {
    "user": {
      "id": 1,
      "name": "新姓名",
      "email": "user@example.com",
      "fitnessLevel": "高级",
      "profile": { /* 更新后的完整资料 */ },
      "updatedAt": "2024-01-01T00:00:00Z"
    }
  },
  "message": "资料更新成功"
}
```

### 5.2 获取用户统计信息
**接口**: `GET /api/user/stats`

**请求头**: `Authorization: Bearer {token}`

**响应体**:
```json
{
  "success": true,
  "data": {
    "chatStats": {
      "totalChats": 25,
      "totalMessages": 150,
      "fitnessChats": 15,
      "manusChats": 10,
      "avgMessagesPerChat": 6
    },
    "activityStats": {
      "dailyActive": true,
      "weeklyActiveDays": 5,
      "lastActiveAt": "2024-01-01T00:00:00Z",
      "joinedAt": "2023-12-01T00:00:00Z"
    },
    "usageStats": {
      "totalProcessingTime": 15000,
      "averageResponseTime": 1200,
      "toolsUsedCount": 45,
      "favoriteFeatures": ["fitness_planning", "exercise_guidance"]
    }
  }
}
```

---

## 🛠️ 后端技术架构设计

### 6.1 推荐技术栈

#### 后端框架
- **Python**: FastAPI + SQLAlchemy + Alembic
- **Node.js**: Express.js + Prisma + TypeScript  
- **Java**: Spring Boot + JPA + PostgreSQL

#### 数据库设计
- **主数据库**: PostgreSQL (用户数据、聊天记录)
- **缓存**: Redis (会话、临时数据)
- **文件存储**: AWS S3 / MinIO / 阿里云OSS

#### AI集成
- **大模型接入**: OpenAI API / Claude API / 国产大模型
- **向量数据库**: Pinecone / Weaviate / Chroma (知识库检索)
- **MCP协议**: 实现Model Context Protocol

### 6.2 数据库表设计

#### 用户表 (users)
```sql
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) DEFAULT 'user',
    fitness_level VARCHAR(20),
    profile JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE
);
```

#### 聊天记录表 (chats)
```sql
CREATE TABLE chats (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(200) NOT NULL,
    app VARCHAR(20) NOT NULL, -- 'fitness' or 'manus'
    message_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_archived BOOLEAN DEFAULT FALSE
);

CREATE INDEX idx_chats_user_app ON chats(user_id, app);
CREATE INDEX idx_chats_updated_at ON chats(updated_at DESC);
```

#### 消息表 (messages)
```sql
CREATE TABLE messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    chat_id UUID REFERENCES chats(id) ON DELETE CASCADE,
    type VARCHAR(20) NOT NULL, -- 'user', 'assistant', 'system'
    content TEXT NOT NULL,
    attachments JSONB,
    metadata JSONB, -- 处理信息、工具调用等
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) DEFAULT 'sent'
);

CREATE INDEX idx_messages_chat_id ON messages(chat_id, created_at);
```

#### 文件表 (files)
```sql
CREATE TABLE files (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
    original_name VARCHAR(255) NOT NULL,
    filename VARCHAR(255) NOT NULL,
    file_type VARCHAR(50),
    file_size BIGINT,
    storage_path VARCHAR(500),
    url VARCHAR(500),
    thumbnail_url VARCHAR(500),
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE
);
```

#### 处理日志表 (processing_logs)
```sql
CREATE TABLE processing_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id INTEGER REFERENCES users(id),
    chat_id UUID REFERENCES chats(id),
    level VARCHAR(20) NOT NULL,
    message TEXT NOT NULL,
    source VARCHAR(100),
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_processing_logs_user_time ON processing_logs(user_id, created_at DESC);
```

### 6.3 API安全设计

#### JWT认证
```python
# JWT Payload 示例
{
    "user_id": 1,
    "email": "user@example.com",
    "role": "user",
    "exp": 1640995200,  # 过期时间
    "iat": 1640908800,  # 签发时间
    "jti": "unique_token_id"  # Token唯一标识
}
```

#### 权限控制
- **用户级别**: 只能访问自己的数据
- **管理员级别**: 可以访问所有用户数据
- **API限流**: 每用户每分钟最多60次请求
- **文件上传限制**: 单文件最大10MB，每用户总计100MB

#### 数据验证
```python
from pydantic import BaseModel, validator

class ChatMessage(BaseModel):
    message: str
    app: str
    attachments: List[FileAttachment] = []
    
    @validator('message')
    def message_not_empty(cls, v):
        if not v.strip():
            raise ValueError('Message cannot be empty')
        return v
    
    @validator('app')
    def valid_app(cls, v):
        if v not in ['fitness', 'manus']:
            raise ValueError('Invalid app type')
        return v
```

### 6.4 AI集成架构

#### MCP服务器实现
```python
class FitnessToolsServer:
    def __init__(self):
        self.tools = {
            'exercise_planner': self.plan_exercise,
            'nutrition_calculator': self.calculate_nutrition,
            'progress_tracker': self.track_progress
        }
    
    async def plan_exercise(self, user_profile: dict, goals: list):
        # 实现健身计划制定逻辑
        pass
    
    async def calculate_nutrition(self, weight: float, activity_level: str):
        # 实现营养计算逻辑
        pass
```

#### 流式响应实现
```python
async def chat_stream(message: str, app: str, user_id: int):
    # 1. 思考阶段
    yield {"type": "thinking", "content": "正在分析您的问题..."}
    
    # 2. 工具调用
    if needs_tools:
        tool_id = await start_tool_execution()
        yield {"type": "tool_start", "tool": tool_info}
        
        result = await execute_tool(tool_id)
        yield {"type": "tool_complete", "toolId": tool_id, "result": result}
    
    # 3. 生成回复
    async for chunk in ai_generate_stream(message, context):
        yield {"type": "content", "content": chunk}
    
    # 4. 完成
    yield {"type": "complete", "messageId": message_id}
```

### 6.5 部署架构

#### Docker Compose 示例
```yaml
version: '3.8'
services:
  backend:
    build: ./backend
    ports:
      - "8000:8000"
    environment:
      - DATABASE_URL=postgresql://user:pass@db:5432/superai
      - REDIS_URL=redis://redis:6379
    depends_on:
      - db
      - redis
  
  db:
    image: postgres:15
    environment:
      - POSTGRES_DB=superai
      - POSTGRES_USER=user
      - POSTGRES_PASSWORD=pass
    volumes:
      - postgres_data:/var/lib/postgresql/data
  
  redis:
    image: redis:7-alpine
    
  nginx:
    image: nginx:alpine
    ports:
      - "80:80"
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf
      - ../frontend/dist:/usr/share/nginx/html
```

### 6.6 性能优化建议

#### 数据库优化
- 使用连接池管理数据库连接
- 对聊天记录按时间分表
- 实现消息的软删除和归档
- 使用Redis缓存热门数据

#### API优化  
- 实现响应缓存
- 使用CDN加速文件访问
- 异步处理耗时操作
- 实现请求去重

#### 监控告警
- 使用Prometheus + Grafana监控
- 集成Sentry错误跟踪
- 实现健康检查接口
- 日志聚合与分析

---

## 📚 开发指南

### 7.1 环境配置
1. 安装依赖和设置数据库
2. 配置环境变量
3. 运行数据库迁移
4. 启动开发服务器

### 7.2 测试策略
- 单元测试：覆盖核心业务逻辑
- 集成测试：测试API接口
- 端到端测试：测试完整用户流程
- 性能测试：压力测试和负载测试

### 7.3 上线检查清单
- [ ] 所有API接口实现并测试通过
- [ ] 数据库表结构和索引优化
- [ ] 安全策略实施（认证、授权、限流）
- [ ] 错误处理和日志记录完善
- [ ] 性能优化和缓存策略
- [ ] 监控和告警系统配置
- [ ] 备份和灾难恢复方案
- [ ] 文档更新和交接

---

## 🔧 附录

### A.1 错误码定义
```javascript
const ERROR_CODES = {
  // 认证相关 1000-1099
  1001: '用户不存在',
  1002: '密码错误', 
  1003: 'Token无效',
  1004: 'Token过期',
  1005: '权限不足',
  
  // 业务相关 2000-2099
  2001: '聊天记录不存在',
  2002: '消息发送失败',
  2003: '文件上传失败',
  2004: '文件格式不支持',
  2005: '文件大小超限',
  
  // 系统相关 5000-5099
  5001: '服务器内部错误',
  5002: '数据库连接失败',
  5003: '外部服务不可用',
  5004: '请求超时',
  5005: '服务维护中'
};
```

### A.2 环境变量配置
```bash
# 数据库配置
DATABASE_URL=postgresql://username:password@localhost:5432/superai
REDIS_URL=redis://localhost:6379

# JWT配置
JWT_SECRET=your_jwt_secret_key
JWT_EXPIRES_IN=7d

# AI服务配置  
OPENAI_API_KEY=your_openai_api_key
OPENAI_BASE_URL=https://api.openai.com/v1

# 文件存储配置
STORAGE_TYPE=local # local/s3/oss
UPLOAD_MAX_SIZE=10485760 # 10MB
UPLOAD_ALLOWED_TYPES=pdf,doc,docx,txt,jpg,jpeg,png,gif

# 服务配置
PORT=8000
HOST=0.0.0.0
DEBUG=false
LOG_LEVEL=info
```

### A.3 前端配置更新
确保前端 `vite.config.js` 中的代理配置指向正确的后端地址：

```javascript
export default defineConfig({
  plugins: [vue()],
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8000', // 后端服务地址
        changeOrigin: true
      }
    }
  }
})
```

---

## 📞 联系方式

如有任何技术问题或需要进一步讨论，请联系开发团队。

**文档版本**: v1.0  
**最后更新**: 2024-01-01  
**维护者**: AI运动助手开发团队