// API 基础配置
const API_BASE_URL = import.meta.env.VUE_APP_API_BASE_URL || 
  (import.meta.env.MODE === 'production' 
    ? 'https://superiai-backend1-173372-4-1369330039.sh.run.tcloudbase.com/api'
    : 'http://localhost:8123/api');

export default API_BASE_URL;

// 简化的API端点 - 只有2个功能
export const API_ENDPOINTS = {
  // 健康咨询 - 使用 SseEmitter 接口 (更稳定)
  healthChat: '/ai/health/chat/sse/emitter',
  
  // AI超级智能体 - Manus接口
  aiSuperChat: '/ai/manus/chat'
};

export const API_CONFIG = {
  timeout: 60000,
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'text/event-stream',
    'X-Client-Type': 'health-assistant-web'
  }
};