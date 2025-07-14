<script setup>
import HelloWorld from './components/HelloWorld.vue'
import TheWelcome from './components/TheWelcome.vue'
</script>

<template>
  <header class="app-header">
    <div class="logo-title">
      <h1>Super AI</h1>
    </div>
    <nav class="app-nav">
      <router-link to="/" class="nav-link">主页</router-link>
      <router-link to="/love" class="nav-link">AI 恋爱大师</router-link>
      <router-link to="/manus" class="nav-link">AI 超级智能体</router-link>
    </nav>
  </header>
  <main class="app-main">
    <router-view />
  </main>
</template>

<script>
export default {
  name: 'App',
};
</script>

<style>
.app-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 2rem;
  background-color: var(--header-bg-color);
  border-bottom: 1px solid var(--border-color);
  box-shadow: var(--box-shadow);
  margin-bottom: 2rem;
  border-radius: 8px;
}

.logo-title h1 {
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--text-color);
}

.app-nav {
  display: flex;
  gap: 1.5rem;
}

.nav-link {
  text-decoration: none;
  color: var(--text-color-secondary);
  font-weight: 500;
  transition: color 0.3s;
}

.nav-link:hover {
  color: var(--primary-color);
}

.nav-link.router-link-exact-active {
  color: var(--primary-color);
  font-weight: 600;
}

.app-main {
  padding: 0 2rem;
}

@media (max-width: 768px) {
  .app-header {
    flex-direction: column;
    gap: 1rem;
    padding: 1rem;
  }
  .app-main {
    padding: 0 1rem;
  }
}
</style>
