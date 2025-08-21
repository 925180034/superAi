// 简单的API测试脚本
import API_BASE_URL, { API_ENDPOINTS } from './src/config/api.js';

console.log('=== API配置调试 ===');
console.log('API_BASE_URL:', API_BASE_URL);
console.log('Health Chat端点:', API_ENDPOINTS.healthChat);
console.log('完整URL:', `${API_BASE_URL}${API_ENDPOINTS.healthChat}`);
console.log('环境模式:', import.meta.env.MODE);
console.log('环境变量 VUE_APP_API_BASE_URL:', import.meta.env.VUE_APP_API_BASE_URL);

// 测试API调用
async function testAPI() {
    const url = `${API_BASE_URL}${API_ENDPOINTS.healthChat}?message=测试&chatId=debug123`;
    console.log('\n=== 测试API调用 ===');
    console.log('请求URL:', url);
    
    try {
        const response = await fetch(url, {
            method: 'GET',
            headers: {
                'Accept': 'text/event-stream',
                'Cache-Control': 'no-cache'
            }
        });
        
        console.log('响应状态:', response.status);
        console.log('响应状态文本:', response.statusText);
        console.log('响应头:', Object.fromEntries(response.headers.entries()));
        
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }
        
        console.log('✅ API连接成功');
        
    } catch (error) {
        console.error('❌ API测试失败:', error);
    }
}

testAPI();