import API_BASE_URL from '@/config/api';

// 全局SSE连接管理器 - 防止重复连接
class GlobalSSEManager {
  constructor() {
    this.connections = new Map(); // 存储所有活跃连接
    this.locks = new Map(); // 请求锁，防止重复
  }

  // 获取连接键
  getConnectionKey(endpoint, message, chatId) {
    return `${endpoint}_${message}_${chatId || 'default'}`;
  }

  // 检查是否已有相同请求
  hasActiveConnection(key) {
    return this.connections.has(key) || this.locks.has(key);
  }

  // 设置请求锁
  setLock(key) {
    this.locks.set(key, true);
  }

  // 释放请求锁
  releaseLock(key) {
    this.locks.delete(key);
  }

  // 添加连接
  addConnection(key, connection) {
    this.connections.set(key, connection);
  }

  // 移除连接
  removeConnection(key) {
    const connection = this.connections.get(key);
    if (connection) {
      if (connection.eventSource) {
        connection.eventSource.close();
      }
      if (connection.controller) {
        connection.controller.abort();
      }
    }
    this.connections.delete(key);
    this.releaseLock(key);
  }

  // 清理所有连接
  clearAll() {
    for (const key of this.connections.keys()) {
      this.removeConnection(key);
    }
  }
}

// 全局实例
const globalManager = new GlobalSSEManager();

export class SSERequest {
  constructor() {
    this.baseURL = API_BASE_URL;
  }

  async connect(endpoint, message, chatId = null, callbacks = {}) {
    const connectionKey = globalManager.getConnectionKey(endpoint, message, chatId);
    
    // 检查是否已有相同请求
    if (globalManager.hasActiveConnection(connectionKey)) {
      console.warn('⚠️ 检测到重复请求，已忽略:', connectionKey);
      return;
    }

    // 设置请求锁
    globalManager.setLock(connectionKey);

    try {
      if (endpoint.includes('/manus/chat')) {
        await this.connectWithFetch(connectionKey, endpoint, { message, chatId }, callbacks);
      } else {
        // 手动构建查询参数，确保正确的URL格式
        const encodedMessage = encodeURIComponent(message);
        const encodedChatId = encodeURIComponent(chatId || 'default');
        const url = `${this.baseURL}${endpoint}?message=${encodedMessage}&chatId=${encodedChatId}`;
        
        console.log('🔍 构建的完整URL:', url);
        console.log('📝 参数详情:', { 
          baseURL: this.baseURL, 
          endpoint, 
          message: encodedMessage, 
          chatId: encodedChatId 
        });
        
        await this.connectWithEventSource(connectionKey, url, callbacks);
      }
    } catch (error) {
      console.error('❌ SSE连接失败:', error);
      globalManager.releaseLock(connectionKey);
      if (callbacks.onError) {
        callbacks.onError(error);
      }
    }
  }

  connectWithEventSource(connectionKey, url, callbacks) {
    console.log('🔗 建立EventSource连接:', url);

    const eventSource = new EventSource(url);
    
    // 注册连接
    globalManager.addConnection(connectionKey, { eventSource });

    eventSource.onmessage = (event) => {
      console.log('📩 收到EventSource消息:', event.data);
      
      if (event.data && event.data.trim() && callbacks.onMessage) {
        callbacks.onMessage({ content: event.data.trim() });
      }
    };

    eventSource.onopen = () => {
      console.log('✅ EventSource连接已建立');
      globalManager.releaseLock(connectionKey);
    };

    eventSource.onerror = (error) => {
      console.error('❌ EventSource错误:', error);
      
      if (eventSource.readyState === EventSource.CLOSED) {
        console.log('🏁 EventSource连接已关闭');
        globalManager.removeConnection(connectionKey);
        if (callbacks.onComplete) {
          callbacks.onComplete();
        }
      } else {
        globalManager.removeConnection(connectionKey);
        if (callbacks.onError) {
          callbacks.onError(error);
        }
      }
    };
  }

  async connectWithFetch(connectionKey, endpoint, data, callbacks) {
    const url = `${this.baseURL}${endpoint}`;
    console.log('🔗 建立Fetch SSE连接:', url);

    try {
      const controller = new AbortController();
      
      // 注册连接
      globalManager.addConnection(connectionKey, { controller });
      
      const response = await fetch(url, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'text/event-stream',
        },
        body: JSON.stringify(data),
        signal: controller.signal
      });

      globalManager.releaseLock(connectionKey);

      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
      }

      const reader = response.body.getReader();
      const decoder = new TextDecoder();
      let buffer = '';

      const processStream = async () => {
        try {
          while (true) {
            const { done, value } = await reader.read();
            
            if (done) {
              console.log('✅ Fetch SSE流结束');
              globalManager.removeConnection(connectionKey);
              if (callbacks.onComplete) {
                callbacks.onComplete();
              }
              break;
            }

            const chunk = decoder.decode(value, { stream: true });
            buffer += chunk;
            
            const lines = buffer.split('\n');
            buffer = lines.pop();

            for (const line of lines) {
              if (line.trim() === '') continue;
              
              if (line.startsWith('data: ')) {
                const data = line.slice(6).trim();
                if (data && data !== '[DONE]' && callbacks.onMessage) {
                  callbacks.onMessage({ content: data });
                }
              } else if (line.trim() && callbacks.onMessage) {
                callbacks.onMessage({ content: line.trim() });
              }
            }
          }
        } catch (streamError) {
          if (streamError.name !== 'AbortError') {
            console.error('❌ Fetch SSE流处理错误:', streamError);
            globalManager.removeConnection(connectionKey);
            if (callbacks.onError) {
              callbacks.onError(streamError);
            }
          }
        }
      };

      processStream();

    } catch (fetchError) {
      if (fetchError.name !== 'AbortError') {
        console.error('❌ Fetch SSE连接错误:', fetchError);
        globalManager.removeConnection(connectionKey);
        if (callbacks.onError) {
          callbacks.onError(fetchError);
        }
      }
    }
  }

  disconnect() {
    // 清理所有连接
    globalManager.clearAll();
    console.log('🔌 所有SSE连接已断开');
  }
}

export const sseRequest = new SSERequest();

// 页面卸载时清理所有连接
if (typeof window !== 'undefined') {
  window.addEventListener('beforeunload', () => {
    globalManager.clearAll();
  });
}