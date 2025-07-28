<template>
  <div class="chat-container">
    <h2 class="chat-title">{{ title }}</h2>
    <div class="chat-history" ref="chatHistory">
      <div v-for="(msg, idx) in messages" :key="idx" :class="['message-wrapper', msg.role === 'user' ? 'user-message' : 'ai-message']">
        <AIAvatar v-if="msg.role === 'ai'" :type="aiAvatarType" />
        <div class="message-bubble">
          <span v-html="msg.content"></span>
        </div>
      </div>
    </div>
    <div class="chat-input-area">
      <input v-model="input" @keyup.enter="sendMessage" placeholder="输入消息..." class="chat-input" />
      <button @click="sendMessage" class="send-button">
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" width="24" height="24"><path d="M2.01 21L23 12 2.01 3 2 10l15 2-15 2z"/></svg>
      </button>
    </div>
  </div>
</template>

<script>
import { ref, nextTick, onMounted, onUnmounted } from 'vue';
import AIAvatar from './AIAvatar.vue';

export default {
  name: 'Chat',
  components: { AIAvatar },
  props: {
    title: {
      type: String,
      required: true,
    },
    apiUrl: {
      type: String,
      required: true,
    },
    generateChatId: {
      type: Boolean,
      default: false,
    },
    streamMode: {
      type: String,
      default: 'append', // 'append' or 'newMessage'
    },
    aiAvatarType: {
      type: String,
      default: 'love',
    },
  },
  setup(props) {
    const input = ref('');
    const messages = ref([]);
    const chatHistory = ref(null);
    const chatId = ref(props.generateChatId ? `chat_${Math.random().toString(36).substr(2, 9)}` : null);
    let eventSource = null;
    let isClosedIntentionally = false;
    let hasReceivedContent = false;

    const scrollToBottom = () => {
      nextTick(() => {
        if (chatHistory.value) {
          chatHistory.value.scrollTop = chatHistory.value.scrollHeight;
        }
      });
    };
    
    const sendMessage = () => {
      const trimmedInput = input.value.trim();
      if (!trimmedInput) return;

      messages.value.push({ role: 'user', content: trimmedInput });
      input.value = '';
      scrollToBottom();
      
      let url = `${props.apiUrl}?message=${encodeURIComponent(trimmedInput)}`;
      if (chatId.value) {
        url += `&chatId=${chatId.value}`;
      }

      if (eventSource) {
        eventSource.close();
      }
      isClosedIntentionally = false;
      hasReceivedContent = false;

      eventSource = new EventSource(url);
      
      if (props.streamMode === 'append') {
        messages.value.push({ role: 'ai', content: '...' });
        scrollToBottom();
      }
      
      eventSource.onmessage = (event) => {
        hasReceivedContent = true;
        if (event.data === '[DONE]') {
          isClosedIntentionally = true;
          eventSource.close();
          if (props.streamMode === 'append') {
            const lastMessage = messages.value[messages.value.length - 1];
            if (lastMessage && lastMessage.role === 'ai' && lastMessage.content === '...') {
                messages.value.pop();
            }
          }
          return;
        }

        let chunk = event.data;
        try {
          const parsed = JSON.parse(chunk);
          if (typeof parsed.content === 'string') {
            chunk = parsed.content;
          }
        } catch (e) {
          // Not JSON, assume plain text
        }
        
        if (props.streamMode === 'append') {
            const aiMessageProxy = messages.value[messages.value.length - 1];
            if (aiMessageProxy && aiMessageProxy.role === 'ai') {
                if (aiMessageProxy.content === '...') {
                    aiMessageProxy.content = '';
                }
                aiMessageProxy.content += chunk;
            }
        } else {
            messages.value.push({ role: 'ai', content: chunk + '\n' });
        }
        scrollToBottom();
      };

      eventSource.onerror = () => {
        if (isClosedIntentionally) {
          return; // Closed intentionally via [DONE] signal
        }

        if (hasReceivedContent) {
          if (eventSource) eventSource.close();
          return; // Assume graceful close if any content was ever received
        }

        // Only show error if the connection failed before ANY content was received.
        const lastMessage = messages.value[messages.value.length - 1];
        if (props.streamMode === 'append' && lastMessage && lastMessage.role === 'ai' && lastMessage.content === '...') {
          lastMessage.content = '抱歉，连接出错了，请稍后再试。';
        } else {
          messages.value.push({ role: 'ai', content: '抱歉，连接出错了，请稍后再试。' });
        }
        
        if (eventSource) {
          eventSource.close();
        }
        scrollToBottom();
      };
    };

    onMounted(() => {
        messages.value.push({role: 'ai', content: `你好！我是${props.title}，有什么可以帮助你的吗？`});
    });

    onUnmounted(() => {
      if (eventSource) {
        eventSource.close();
      }
    });

    return { input, messages, sendMessage, chatHistory };
  },
};
</script>

<style scoped>
.chat-container {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 160px);
  max-width: 800px;
  margin: 0 auto;
  background-color: var(--card-bg-color);
  border-radius: 12px;
  box-shadow: var(--box-shadow);
  overflow: hidden;
}
.chat-title {
  padding: 1rem;
  text-align: center;
  font-weight: 600;
  border-bottom: 1px solid var(--border-color);
  background-color: var(--header-bg-color);
}
.chat-history {
  flex-grow: 1;
  overflow-y: auto;
  padding: 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}
.message-wrapper {
  display: flex;
  max-width: 75%;
}
.message-bubble {
  padding: 0.75rem 1rem;
  border-radius: 18px;
  line-height: 1.4;
  white-space: pre-wrap;
  word-wrap: break-word;
}
.user-message {
  align-self: flex-end;
}
.user-message .message-bubble {
  background-color: var(--user-message-bg);
  color: var(--user-message-text);
  border-bottom-right-radius: 4px;
}
.ai-message {
  align-self: flex-start;
  align-items: flex-start;
}
.ai-message .message-bubble {
  background-color: var(--ai-message-bg);
  color: var(--ai-message-text);
  border-bottom-left-radius: 4px;
  text-align: left;
}
.chat-input-area {
  display: flex;
  padding: 1rem;
  border-top: 1px solid var(--border-color);
  background-color: var(--header-bg-color);
}
.chat-input {
  flex-grow: 1;
  border: 1px solid var(--border-color);
  border-radius: 20px;
  padding: 0.5rem 1rem;
  font-size: 1rem;
  outline: none;
}
.chat-input:focus {
  border-color: var(--primary-color);
}
.send-button {
  background: none;
  border: none;
  cursor: pointer;
  padding: 0 0 0 1rem;
  color: var(--primary-color);
  display: flex;
  align-items: center;
  justify-content: center;
}

@media (max-width: 768px) {
  .chat-container {
    height: calc(100vh - 150px);
    border-radius: 0;
  }
  .message-wrapper {
    max-width: 90%;
  }
}
</style> 