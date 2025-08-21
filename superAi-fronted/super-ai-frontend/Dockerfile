# Stage 1: Build stage
FROM node:20-alpine AS build
WORKDIR /app

# 添加时间戳强制重新构建
ARG CACHE_BUST=1
RUN echo "Cache bust: ${CACHE_BUST}"

# 清理npm缓存
RUN npm cache clean --force

COPY package*.json ./
RUN npm install

COPY . .
RUN npm run build

# Stage 2: Production stage
FROM nginx:stable-alpine

# 清理nginx默认内容
RUN rm -rf /usr/share/nginx/html/*

# 复制新的构建产物
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 创建日志目录
RUN mkdir -p /var/log/nginx

# 禁用nginx缓存
RUN echo "add_header Cache-Control 'no-cache, no-store, must-revalidate';" >> /etc/nginx/conf.d/default.conf
RUN echo "add_header Pragma 'no-cache';" >> /etc/nginx/conf.d/default.conf
RUN echo "add_header Expires '0';" >> /etc/nginx/conf.d/default.conf

# 暴露端口
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]