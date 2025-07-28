# SuperAI 前端项目部署流程文档

## 📋 项目概述

**项目信息：**
- 项目名称：SuperAI 前端
- 后端服务：SuperAI 后端
- 后端地址：https://superiai-backend1-173372-4-1369330039.sh.run.tcloudbase.com
- 后端部署方式：微信云托管
- API 路径：/api
- 前端技术栈：Vue.js + Vite

---

## 🚨 当前问题快速修复

### 针对 502/431 错误的解决方案

**问题分析：**
- 502 Bad Gateway：后端连接失败
- 431 Request Header Fields Too Large：HTTP头部过大
- SSE连接异常：Server-Sent Events 配置问题

**立即修复步骤：**

1. **停止现有容器**
```bash
docker stop superai-frontend
docker rm superai-frontend
```

2. **创建正确的 nginx.conf**（覆盖原文件）
3. **重新构建和部署**
```bash
docker build --no-cache -t superai-frontend:latest .
docker run -d -p 80:80 --name superai-frontend superai-frontend:latest
```

---

## 📁 文件配置

### 1. 前端环境配置

#### 1.1 API 基础地址配置
修改前端项目中的 API 配置文件：

```javascript
// 在 api/index.js 或 config/index.js 中
const API_BASE_URL = process.env.NODE_ENV === 'production' 
  ? 'https://superiai-backend1-173372-4-1369330039.sh.run.tcloudbase.com/api'
  : 'http://localhost:8123/api';

export default API_BASE_URL;
```

#### 1.2 环境变量文件
创建 `.env.production` 文件：

```bash
# 生产环境配置
NODE_ENV=production
VUE_APP_API_BASE_URL=https://superiai-backend1-173372-4-1369330039.sh.run.tcloudbase.com/api
# 对于 React 项目使用：
REACT_APP_API_BASE_URL=https://superiai-backend1-173372-4-1369330039.sh.run.tcloudbase.com/api
```

### 2. Nginx 配置文件

创建 `nginx.conf` 文件（重要：解决当前所有问题）：

```nginx
server {
    listen 80;
    server_name localhost;

    # 全局配置 - 解决 431 错误
    client_header_buffer_size 64k;
    large_client_header_buffers 4 64k;
    client_max_body_size 50m;

    # 前端静态文件目录
    root /usr/share/nginx/html;
    index index.html index.htm;

    # 前端路由支持（SPA）
    location / {
        try_files $uri $uri/ /index.html;
    }

    # API 反向代理 - 修复后端连接
    location ^~ /api/ {
        # 正确的后端地址
        proxy_pass https://superiai-backend1-173372-4-1369330039.sh.run.tcloudbase.com/api/;

        # 基础代理配置
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        # 解决大头部问题
        proxy_buffer_size 64k;
        proxy_buffers 32 32k;
        proxy_busy_buffers_size 64k;

        # SSE 流式传输优化
        proxy_buffering off;
        proxy_cache off;
        proxy_http_version 1.1;
        proxy_set_header Connection "";
        chunked_transfer_encoding off;

        # 超时配置
        proxy_connect_timeout 60s;
        proxy_send_timeout 600s;
        proxy_read_timeout 600s;

        # 错误重试
        proxy_next_upstream error timeout invalid_header http_500 http_502 http_503 http_504;
        proxy_next_upstream_tries 3;
        proxy_next_upstream_timeout 10s;

        # 跨域处理
        proxy_intercept_errors off;
    }

    # 静态资源缓存
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ {
        expires 1y;
        access_log off;
        add_header Cache-Control "public";
    }

    # 错误页面
    error_page 500 502 503 504 /50x.html;
    location = /50x.html {
        root /usr/share/nginx/html;
    }
}
```

### 3. Dockerfile

创建 `Dockerfile`：

```dockerfile
# 构建阶段
FROM node:20-alpine AS build

WORKDIR /app
COPY package*.json ./
RUN npm install

COPY . .
RUN npm run build

# 运行阶段
FROM nginx:alpine

# 复制构建产物
COPY --from=build /app/dist /usr/share/nginx/html

# 复制 Nginx 配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 暴露端口
EXPOSE 80

# 启动 Nginx
CMD ["nginx", "-g", "daemon off;"]
```

### 4. Docker 忽略文件

创建 `.dockerignore`：

```plaintext
node_modules
npm-debug.log
yarn-debug.log
yarn-error.log
/dist
/build
.env*
.DS_Store
Thumbs.db
/.idea
/.vscode
*.log
/coverage
.npm
.eslintcache
logs
```

---

## 🚀 部署步骤

### 步骤 1：准备文件
确保以下文件已创建并配置正确：
- [ ] `.env.production`
- [ ] `nginx.conf`
- [ ] `Dockerfile`
- [ ] `.dockerignore`

### 步骤 2：构建镜像
```bash
# 构建 Docker 镜像
docker build -t superai-frontend:latest .

# 如果需要清除缓存重新构建
docker build --no-cache -t superai-frontend:latest .
```

### 步骤 3：运行容器
```bash
# 停止现有容器（如果存在）
docker stop superai-frontend 2>/dev/null || true
docker rm superai-frontend 2>/dev/null || true

# 运行新容器
docker run -d \
  -p 80:80 \
  --name superai-frontend \
  --restart unless-stopped \
  superai-frontend:latest
```

### 步骤 4：验证部署
```bash
# 检查容器状态
docker ps | grep superai-frontend

# 查看容器日志
docker logs superai-frontend

# 测试前端访问
curl -I http://localhost

# 测试 API 代理
curl -I http://localhost/api/health
```

---

## 🔧 故障排查

### 常见错误解决

**1. 502 Bad Gateway**
```bash
# 检查后端服务可用性
curl -I https://superiai-backend1-173372-4-1369330039.sh.run.tcloudbase.com/api

# 检查 Nginx 配置
docker exec superai-frontend nginx -t

# 查看详细错误日志
docker logs superai-frontend
```

**2. 431 Request Header Fields Too Large**
```bash
# 确认 nginx.conf 中包含以下配置：
client_header_buffer_size 64k;
large_client_header_buffers 4 64k;
proxy_buffer_size 64k;
proxy_buffers 32 32k;
```

**3. SSE 连接问题**
```bash
# 确认 nginx.conf 中包含：
proxy_buffering off;
proxy_cache off;
proxy_http_version 1.1;
proxy_set_header Connection "";
```

### 调试命令

```bash
# 进入容器调试
docker exec -it superai-frontend /bin/sh

# 查看 Nginx 配置
docker exec superai-frontend cat /etc/nginx/conf.d/default.conf

# 实时查看访问日志
docker exec superai-frontend tail -f /var/log/nginx/access.log

# 查看错误日志
docker exec superai-frontend tail -f /var/log/nginx/error.log

# 重新加载 Nginx 配置
docker exec superai-frontend nginx -s reload
```

---

## 🐳 Docker Compose 部署

创建 `docker-compose.yml`：

```yaml
version: '3.8'

services:
  frontend:
    build:
      context: .
      dockerfile: Dockerfile
    ports:
      - "80:80"
    restart: unless-stopped
    container_name: superai-frontend
    networks:
      - superai-network

networks:
  superai-network:
    driver: bridge
```

使用 Docker Compose：
```bash
# 启动服务
docker-compose up -d

# 查看状态
docker-compose ps

# 查看日志
docker-compose logs -f frontend

# 停止服务
docker-compose down
```

---

## ✅ 部署检查清单

### 部署前检查
- [ ] 前端 API 地址配置正确
- [ ] nginx.conf 文件配置完整
- [ ] Dockerfile 文件存在
- [ ] .dockerignore 文件存在

### 部署后验证
- [ ] 容器成功启动
- [ ] 前端页面可以访问
- [ ] API 请求正常
- [ ] SSE 连接正常
- [ ] 没有 502/431 错误

### 功能测试
- [ ] 页面路由跳转正常
- [ ] AI 对话功能正常
- [ ] 静态资源加载正常
- [ ] 浏览器控制台无错误

---

## 📊 监控和维护

### 日志监控
```bash
# 持续监控日志
docker logs -f superai-frontend

# 监控访问日志
docker exec superai-frontend tail -f /var/log/nginx/access.log
```

### 健康检查
```bash
# 定期检查服务状态
curl -I http://localhost/api/health

# 检查容器资源使用
docker stats superai-frontend
```

### 更新部署
```bash
# 1. 重新构建镜像
docker build -t superai-frontend:v1.0.1 .

# 2. 停止旧容器
docker stop superai-frontend

# 3. 删除旧容器
docker rm superai-frontend

# 4. 启动新容器
docker run -d -p 80:80 --name superai-frontend superai-frontend:v1.0.1
```

---

## 📝 重要说明

1. **后端服务地址**：已配置为微信云托管地址，如地址变更需同步更新
2. **SSE 优化**：针对 AI 对话的实时流式传输进行了专门优化
3. **错误处理**：增加了完整的错误重试和处理机制
4. **性能优化**：配置了静态资源缓存和传输优化

**部署完成后，你的 SuperAI 前端应用将能够：**
- ✅ 正常访问和使用
- ✅ 与后端 API 正常通信
- ✅ 支持 AI 对话的实时流式响应
- ✅ 处理各种网络异常情况

按照此文档执行，应该能完全解决当前的部署问题！