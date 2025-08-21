import { createRouter, createWebHistory } from 'vue-router';

const Home = () => import('../pages/Home.vue');
const HealthChat = () => import('../pages/HealthChat.vue');
const AISuperChat = () => import('../pages/AISuperChat.vue');

const routes = [
  {
    path: '/',
    component: Home,
    name: 'Home',
    meta: { 
      title: '健康养生助手 - 专业健康咨询平台',
      description: '专业的健康咨询与养生指导平台'
    }
  },
  {
    path: '/health',
    component: HealthChat,
    name: 'HealthChat',
    meta: { 
      title: '健康咨询 - 专业医疗建议和健康指导',
      description: '获取专业的健康咨询和医疗建议'
    }
  },
  {
    path: '/ai-super',
    component: AISuperChat,
    name: 'AISuperChat',
    meta: { 
      title: 'AI超级智能体 - 全能工具助手',
      description: '功能强大的AI智能体，支持工具调用和复杂任务处理'
    }
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

// 强化的路由守卫 - Windows系统优化
router.beforeEach((to, from, next) => {
  // 设置页面标题
  if (to.meta.title) {
    document.title = to.meta.title;
  }
  
  // 设置meta描述
  if (to.meta.description) {
    let metaDescription = document.querySelector('meta[name="description"]');
    if (!metaDescription) {
      metaDescription = document.createElement('meta');
      metaDescription.name = 'description';
      document.head.appendChild(metaDescription);
    }
    metaDescription.content = to.meta.description;
  }
  
  // Windows系统特殊处理
  if (navigator.platform.includes('Win')) {
    document.body.classList.add('windows-system');
    console.log('🖥️ Windows系统已优化');
  }
  
  next();
});

export default router;