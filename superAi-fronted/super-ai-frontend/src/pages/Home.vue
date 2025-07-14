<template>
  <div class="home">
    <div class="welcome-banner">
      <h2>欢迎使用 Super AI 应用</h2>
      <p>在这里，您可以体验到前沿的 AI 技术，选择一个应用开始您的智能之旅吧！</p>
    </div>
    <div class="app-cards">
      <router-link to="/love" class="card">
        <h3>❤️ AI 恋爱大师</h3>
        <p>您的贴心伙伴，随时倾听，提供情感支持与建议。</p>
      </router-link>
      <router-link to="/manus" class="card">
        <h3>🤖 AI 超级智能体</h3>
        <p>您的全能助手，连接多领域工具，高效完成各项任务。</p>
      </router-link>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Home',
};
</script>

<style scoped>
.home {
  text-align: center;
}

.welcome-banner {
  margin-bottom: 2.5rem;
}

.welcome-banner h2 {
  font-size: 2rem;
  font-weight: 600;
  margin-bottom: 0.5rem;
}

.welcome-banner p {
  font-size: 1.1rem;
  color: var(--text-color-secondary);
}

.app-cards {
  display: flex;
  justify-content: center;
  gap: 2rem;
}

.card {
  background-color: var(--card-bg-color);
  padding: 2rem;
  border-radius: 12px;
  box-shadow: var(--box-shadow);
  text-decoration: none;
  color: inherit;
  width: 300px;
  transition: transform 0.3s, box-shadow 0.3s;
}

.card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
}

.card h3 {
  font-size: 1.5rem;
  font-weight: 600;
  margin-bottom: 1rem;
  color: var(--primary-color);
}

.card p {
  color: var(--text-color-secondary);
}

@media (max-width: 768px) {
  .app-cards {
    flex-direction: column;
    align-items: center;
  }
  .card {
    width: 100%;
    max-width: 340px;
  }
}
</style> 