<template>
  <div class="home-container">
    <!-- 头部 - 清理掉恋爱大师等内容 -->
    <header class="home-header">
      <nav class="top-nav">
        <div class="logo">健康养生助手</div>
        <div class="nav-links">
          <router-link to="/" class="nav-link active">主页</router-link>
          <router-link to="/health" class="nav-link">健康咨询</router-link>
          <router-link to="/ai-super" class="nav-link">AI智能体</router-link>
        </div>
      </nav>
    </header>

    <!-- 主要内容 -->
    <main class="main-content">
      <div class="hero-section">
        <div class="hero-icon">🏥</div>
        <h1 class="hero-title">健康养生助手</h1>
        <p class="hero-subtitle">专业的健康咨询与养生指导平台</p>
      </div>

      <!-- 只保留2个功能卡片 -->
      <div class="features-grid">
        <router-link to="/health" class="feature-card health-card">
          <div class="card-icon">🩺</div>
          <h3>健康咨询助手</h3>
          <p>专业健康问题解答，疾病预防指导</p>
          <div class="card-badge">专业</div>
          <div class="card-tech">SSE流式输出</div>
        </router-link>
        
        <router-link to="/ai-super" class="feature-card ai-card">
          <div class="card-icon">🤖</div>
          <h3>AI超级智能体</h3>
          <p>全能助手，支持工具调用和复杂任务</p>
          <div class="card-badge">强大</div>
          <div class="card-tech">工具调用可视化</div>
        </router-link>
      </div>

      <!-- 简化的特色说明 -->
      <section class="platform-features">
        <h2>🌟 平台特色</h2>
        <div class="features-list">
          <div class="feature-item">
            <div class="feature-icon">🔥</div>
            <h4>实时流式响应</h4>
            <p>SSE技术支持，AI回复实时显示</p>
          </div>
          <div class="feature-item">
            <div class="feature-icon">🛠️</div>
            <h4>工具调用能力</h4>
            <p>AI智能体支持多种工具调用</p>
          </div>
          <div class="feature-item">
            <div class="feature-icon">🎯</div>
            <h4>专业健康指导</h4>
            <p>基于医疗知识的专业建议</p>
          </div>
        </div>
      </section>
    </main>
  </div>
</template>

<script>
export default {
  name: 'Home',
  mounted() {
    document.title = '健康养生助手 - 专业健康咨询平台';
  }
}
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.home-header {
  padding: 1rem 2rem;
  background: rgba(255,255,255,0.1);
  backdrop-filter: blur(10px);
}

.top-nav {
  display: flex;
  justify-content: space-between;
  align-items: center;
  max-width: 1200px;
  margin: 0 auto;
}

.logo {
  font-size: 1.5rem;
  font-weight: 700;
  color: white;
}

.nav-links {
  display: flex;
  gap: 2rem;
}

.nav-link {
  color: rgba(255,255,255,0.8);
  text-decoration: none;
  font-weight: 500;
  transition: all 0.2s;
}

.nav-link:hover,
.nav-link.active {
  color: white;
}

.main-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 3rem 2rem;
}

.hero-section {
  text-align: center;
  margin-bottom: 4rem;
  color: white;
}

.hero-icon {
  font-size: 4rem;
  margin-bottom: 1rem;
}

.hero-title {
  font-size: 3rem;
  font-weight: 700;
  margin-bottom: 1rem;
  text-shadow: 2px 2px 4px rgba(0,0,0,0.3);
}

.hero-subtitle {
  font-size: 1.3rem;
  opacity: 0.9;
  font-weight: 300;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 2rem;
  margin-bottom: 4rem;
}

.feature-card {
  background: white;
  border-radius: 20px;
  padding: 2.5rem;
  text-decoration: none;
  color: #333;
  box-shadow: 0 10px 40px rgba(0,0,0,0.1);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.feature-card:hover {
  transform: translateY(-10px);
  box-shadow: 0 20px 60px rgba(0,0,0,0.2);
}

.feature-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 5px;
}

.health-card::before {
  background: linear-gradient(90deg, #4CAF50, #45a049);
}

.ai-card::before {
  background: linear-gradient(90deg, #6c5ce7, #5a4fcf);
}

.card-icon {
  font-size: 3rem;
  margin-bottom: 1.5rem;
  display: block;
}

.feature-card h3 {
  font-size: 1.8rem;
  font-weight: 700;
  margin-bottom: 1rem;
  color: #2c3e50;
}

.feature-card p {
  color: #7f8c8d;
  line-height: 1.6;
  font-size: 1.1rem;
  margin-bottom: 1rem;
}

.card-badge {
  position: absolute;
  top: 1.5rem;
  right: 1.5rem;
  background: #e74c3c;
  color: white;
  padding: 0.5rem 1rem;
  border-radius: 15px;
  font-size: 0.8rem;
  font-weight: 600;
}

.card-tech {
  background: #f8f9fa;
  color: #6c757d;
  padding: 0.5rem 1rem;
  border-radius: 12px;
  font-size: 0.85rem;
  display: inline-block;
  margin-top: 0.5rem;
}

.platform-features {
  text-align: center;
  color: white;
}

.platform-features h2 {
  font-size: 2.5rem;
  margin-bottom: 3rem;
  font-weight: 600;
}

.features-list {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 2rem;
}

.feature-item {
  background: rgba(255,255,255,0.1);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  padding: 2rem;
  border: 1px solid rgba(255,255,255,0.2);
}

.feature-icon {
  font-size: 2.5rem;
  margin-bottom: 1rem;
}

.feature-item h4 {
  font-size: 1.3rem;
  font-weight: 600;
  margin-bottom: 0.5rem;
}

.feature-item p {
  opacity: 0.9;
  line-height: 1.6;
}

@media (max-width: 768px) {
  .top-nav {
    flex-direction: column;
    gap: 1rem;
  }
  
  .nav-links {
    gap: 1rem;
  }
  
  .hero-title {
    font-size: 2rem;
  }
  
  .features-grid {
    grid-template-columns: 1fr;
    gap: 1.5rem;
  }
  
  .feature-card {
    padding: 2rem;
  }
}
</style> 