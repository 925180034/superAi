<template>
  <Chat 
    title="AI 恋爱大师" 
    api-url="http://localhost:8123/api/ai/love_app/chat/sse"
    :generate-chat-id="true"
    ai-avatar-type="love"
  />
</template>

<script>
import Chat from '../components/Chat.vue';

export default {
  name: 'LoveChat',
  components: {
    Chat,
  },
};
</script>

<style scoped>
.chat-container {
  max-width: 600px;
  margin: 0 auto;
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 20px;
  background: #fafbfc;
}
.chat-history {
  min-height: 300px;
  max-height: 400px;
  overflow-y: auto;
  margin-bottom: 16px;
  background: #fff;
  border-radius: 4px;
  padding: 10px;
  border: 1px solid #e0e0e0;
}
.msg-user {
  text-align: right;
  margin: 8px 0;
  color: #1976d2;
}
.msg-ai {
  text-align: left;
  margin: 8px 0;
  color: #e91e63;
}
.chat-input {
  display: flex;
  gap: 8px;
}
.chat-input input {
  flex: 1;
  padding: 8px;
  border-radius: 4px;
  border: 1px solid #ccc;
}
.chat-input button {
  padding: 8px 16px;
  border: none;
  background: #42b983;
  color: #fff;
  border-radius: 4px;
  cursor: pointer;
}
</style> 