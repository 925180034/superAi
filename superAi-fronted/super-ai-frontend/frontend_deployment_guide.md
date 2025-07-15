# SuperAI 前端项目部署流程文档

## 项目概述

本文档描述了 SuperAI 前端项目的完整部署流程，包括生产环境配置、Nginx 配置、Docker 容器化部署等步骤。

**项目信息：**
- 后端项目：SuperAI
- 后端端口：8123
- API 路径：/api
- 后端部署地址：https://superiai-backend1-173372-4-1369330039.sh.run.tcloudbase.com
- 后端部署方式：微信云托管
- 前端技术栈：Vue.js / React（请根据实际情况调整）

## 部署流程

### 1. 前端生产环境配置

#### 1.1 修改 API 基础地址

需要修改前端代码中的请求地址配置文件（通常在 `api/index.js` 或 `config/index.js` 文件中）：

```javascript
// 根据环境变量设置 API 基础 URL
const API_BASE_URL = process.env.NODE_ENV === 'production' 
  ? 'https://superiai-backend1-173372-4-1369330039.sh.run.tcloudbase.com/api'  // 生产环境域名
  : 'http://localhost:8123/api';   // 开发环境本地后端服务

export default API_BASE_URL;
```

#### 1.2 环境变量配置

创建或修改 `.env.production` 文件：

```bash
# 生产环境配置
NODE_ENV=production
VUE_APP_API_BASE_URL=https://superiai-backend1-173372-4-1369330039.sh.run.tcloudbase.com/api
# 或者对于 React 项目
REACT_APP_API_BASE_URL=https://superiai-backend1-173372-4-1369330039.sh.run.tcloudbase.com/api
```

### 2. Nginx 配置

#### 2.1 创建 nginx.conf 文件

在项目根目录创建 `nginx.conf` 文件：

```nginx
server {
    listen 80;
    server_name localhost;

    # 前端静态文件目录
    root /usr/share/nginx/html;

    # 所有HTML资源走到 index.html（支持Vue路由的404页面）
    location / {
        index index.html index.htm;
        try_files $uri $uri/ /index.html;
    }

    # API请求反向代理配置
    location ^~ /api/ {
        # 设置反向代理地址 - SuperAI 后端服务地址（微信云托管）
        proxy_pass https://superiai-backend1-173372-4-1369330039.sh.run.tcloudbase.com/api/;

        # 设置请求头
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        # SSE (Server-Sent Events)配置
        proxy_set_header Connection "";
        proxy_http_version 1.1;
        proxy_buffering off;
        proxy_cache off;
        chunked_transfer_encoding off;
        proxy_read_timeout 600s;

        # 处理跨域问题
        proxy_intercept_errors off;
    }

    # 处理静态资源缓存
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ {
        expires 1y;
        access_log off;
        add_header Cache-Control "public";
    }

    # 处理错误页面
    error_page 500 502 503 504 /50x.html;
    location = /50x.html {
        root /usr/share/nginx/html;
    }
}
```

#### 2.2 配置说明

- **静态文件服务**：Nginx 直接服务前端构建后的静态文件
- **API 反向代理**：将 `/api/` 开头的请求转发到微信云托管的后端服务
- **SPA 路由支持**：`try_files` 配置确保前端路由正常工作
- **缓存优化**：对静态资源设置长期缓存
- **SSE 支持**：针对实时通信进行了优化配置
- **微信云托管集成**：后端服务部署在微信云托管，具备高可用性和自动扩缩容能力

### 3. Docker 容器镜像构建

#### 3.1 创建 Dockerfile

在项目根目录创建 `Dockerfile`：

```dockerfile
# 前端构建阶段
FROM node:20-alpine AS build

WORKDIR /app
COPY . .

# 安装依赖并构建
RUN npm install
RUN npm run build

# 运行阶段 - 使用 nginx 镜像静态文件
FROM nginx:alpine

# 复制构建产物到 nginx 静态文件目录
COPY --from=build /app/dist /usr/share/nginx/html

# 复制自定义 nginx 配置文件替换默认配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 暴露端口
EXPOSE 80

# 启动 Nginx
CMD ["nginx", "-g", "daemon off;"]
```

#### 3.2 构建和运行说明

- **多阶段构建**：先构建前端项目，再将产物复制到 Nginx 镜像
- **轻量化**：使用 Alpine 镜像减少容器大小
- **自定义配置**：使用项目中的 nginx.conf 替换默认配置

### 4. Docker 忽略文件配置

#### 4.1 创建 .dockerignore 文件

为了提高构建效率，可以创建 `.dockerignore` 文件忽略不必要的文件：

```plaintext
# 依赖目录
node_modules
npm-debug.log
yarn-debug.log
yarn-error.log

# 编译输出
/dist
/build

# 本地环境文件
.env
.env.local
.env.development.local
.env.test.local
.env.production.local

# 编辑器目录和配置
/.idea
/.vscode
*.suo
*.ntvs*
*.njsproj
*.sln
*.sw?

# 操作系统文件
.DS_Store
Thumbs.db

# 测试覆盖率报告
/coverage

# 缓存
.npm
.eslintcache

# 日志
logs
*.log
```

### 5. 部署命令

#### 5.1 本地构建和测试

```bash
# 1. 构建 Docker 镜像
docker build -t superai-frontend:latest .

# 2. 运行容器进行测试
docker run -d -p 8080:80 --name superai-frontend superai-frontend:latest

# 3. 访问测试
curl http://localhost:8080
```

#### 5.2 生产环境部署

```bash
# 1. 构建生产镜像（带版本标签）
docker build -t superai-frontend:v1.0.0 .

# 2. 推送到镜像仓库（如果使用）
docker tag superai-frontend:v1.0.0 your-registry/superai-frontend:v1.0.0
docker push your-registry/superai-frontend:v1.0.0

# 3. 生产环境运行
docker run -d \
  -p 80:80 \
  --name superai-frontend-prod \
  --restart unless-stopped \
  superai-frontend:v1.0.0
```

### 6. Docker Compose 部署（推荐）

#### 6.1 创建 docker-compose.yml

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

#### 6.2 使用 Docker Compose 部署

```bash
# 启动前端服务
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f frontend

# 停止服务
docker-compose down
```

**注意**：由于后端已部署在微信云托管平台，此 Docker Compose 配置只包含前端服务。

### 7. 部署前检查清单

#### 7.1 配置检查

- [ ] API 基础地址已修改为生产环境地址
- [ ] 环境变量配置正确
- [ ] Nginx 配置中的后端代理地址正确
- [ ] SSL 证书配置（如果需要 HTTPS）

#### 7.2 功能测试

- [ ] 静态资源加载正常
- [ ] API 请求能够正常响应
- [ ] 前端路由跳转正常
- [ ] 跨域问题已解决
- [ ] 后端服务连通性测试

**后端连通性测试**：
```bash
# 测试微信云托管后端服务是否可用
curl https://superiai-backend1-173372-4-1369330039.sh.run.tcloudbase.com/api/health

# 或者在浏览器中访问
# https://superiai-backend1-173372-4-1369330039.sh.run.tcloudbase.com/api

# 测试 API 接口响应
curl -X GET https://superiai-backend1-173372-4-1369330039.sh.run.tcloudbase.com/api/test
```

#### 7.3 性能优化

- [ ] 静态资源缓存配置
- [ ] Gzip 压缩启用
- [ ] 图片和字体文件优化

### 8. 故障排查

#### 8.1 常见问题

**问题1：API 请求 404**
```bash
# 检查 Nginx 配置
docker exec -it superai-frontend nginx -t

# 查看 Nginx 日志
docker logs superai-frontend
```

**问题2：前端路由 404**
```bash
# 确认 nginx.conf 中有 try_files 配置
location / {
    try_files $uri $uri/ /index.html;
}
```

**问题3：跨域问题**
```bash
# 检查后端是否配置了正确的 CORS
# 检查 Nginx 代理配置是否正确
```

### 9. 维护和更新

#### 9.1 更新部署

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

#### 9.2 日志监控

```bash
# 查看容器日志
docker logs -f superai-frontend

# 查看 Nginx 访问日志
docker exec -it superai-frontend tail -f /var/log/nginx/access.log
```

### 10. 备注

1. **后端服务**：后端已部署在微信云托管平台，域名为 `https://superiai-backend1-173372-4-1369330039.sh.run.tcloudbase.com`
2. **云托管特性**：微信云托管提供容器化部署，具有自动扩缩容和负载均衡能力
3. **跨域配置**：由于前后端分离部署，已在 Nginx 配置中处理跨域问题
4. 生产环境建议使用 HTTPS，需要额外配置 SSL 证书
5. 可以考虑使用 CDN 加速静态资源加载
6. 建议配置健康检查和监控告警
7. **重要**：如果微信云托管域名发生变化，需要同步更新前端配置中的 API 地址
8. **微信生态**：使用微信云托管有利于后续接入微信小程序等微信生态服务

---

**说明**：此文档基于通用的前端项目部署流程编写，请根据 SuperAI 项目的具体需求进行相应调整。