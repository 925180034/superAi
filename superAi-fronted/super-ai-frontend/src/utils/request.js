import axios from 'axios';
import API_BASE_URL, { API_CONFIG } from '@/config/api';

// 创建axios实例
const request = axios.create({
  baseURL: API_BASE_URL,
  timeout: API_CONFIG.timeout,
  headers: API_CONFIG.headers
});

// 请求拦截器
request.interceptors.request.use(
  config => {
    // 添加时间戳防止缓存
    config.headers['X-Request-Time'] = Date.now();
    
    // 添加用户标识（如果有）
    const userId = localStorage.getItem('health_user_id');
    if (userId) {
      config.headers['X-User-ID'] = userId;
    }
    
    console.log('🏥 健康助手API请求:', config.url);
    return config;
  },
  error => {
    console.error('❌ 请求配置错误:', error);
    return Promise.reject(error);
  }
);

// 响应拦截器
request.interceptors.response.use(
  response => {
    console.log('✅ 健康助手API响应:', response.config.url, response.status);
    return response.data;
  },
  error => {
    console.error('❌ 健康助手API错误:', error.config?.url, error.response?.status);
    
    // 处理常见错误
    if (error.response?.status === 404) {
      console.warn('⚠️ API接口不存在，请检查后端服务');
    } else if (error.response?.status >= 500) {
      console.error('🔥 服务器错误，请稍后重试');
    }
    
    return Promise.reject(error);
  }
);

export default request;