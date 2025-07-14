import { createRouter, createWebHistory } from 'vue-router';

const Home = () => import('../pages/Home.vue');
const LoveChat = () => import('../pages/LoveChat.vue');
const ManusChat = () => import('../pages/ManusChat.vue');

const routes = [
  { path: '/', component: Home, name: 'Home' },
  { path: '/love', component: LoveChat, name: 'LoveChat' },
  { path: '/manus', component: ManusChat, name: 'ManusChat' },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

export default router; 