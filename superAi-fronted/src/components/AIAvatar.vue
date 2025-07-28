<template>
  <div class="ai-avatar">
    <svg v-if="type === 'love'" width="40" height="40" viewBox="0 0 24 24">
      <path fill="var(--primary-color)" d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/>
    </svg>
    <svg v-else-if="type === 'manus'" width="40" height="40" viewBox="0 0 24 24">
      <path fill="var(--primary-color)" d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zM9.5 18c-.83 0-1.5-.67-1.5-1.5s.67-1.5 1.5-1.5 1.5.67 1.5 1.5S10.33 18 9.5 18zm5-1.5c.83 0 1.5.67 1.5 1.5s-.67 1.5-1.5 1.5-1.5-.67-1.5-1.5.67-1.5 1.5-1.5zm2.5-6.5H7c-.55 0-1-.45-1-1s.45-1 1-1h10c.55 0 1 .45 1 1s-.45 1-1 1zm-1-4H8c-.55 0-1-.45-1-1s.45-1 1-1h8c.55 0 1 .45 1 1s-.45 1-1 1z"/>
    </svg>
  </div>
</template>

<script>
export default {
  name: 'AIAvatar',
  props: {
    type: {
      type: String,
      required: true, // 'love' or 'manus'
    },
  },
};
</script>

<style scoped>
.ai-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: var(--ai-message-bg);
  display: flex;
  justify-content: center;
  align-items: center;
  flex-shrink: 0;
  align-self: flex-start; /* Avatar aligns to top */
}
</style> 