import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

// 布局组件
const MainLayout = () => import('@/components/layout/MainLayout.vue')

// 主要页面
const FitnessChat = () => import('@/views/fitness/FitnessChat.vue')

// 认证页面
const Login = () => import('@/views/auth/Login.vue')
const Register = () => import('@/views/auth/Register.vue')

// 其他页面
const Settings = () => import('@/views/Settings.vue')
const Profile = () => import('@/views/Profile.vue')
const NotFound = () => import('@/views/NotFound.vue')

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { 
      requiresAuth: false,
      hideForAuth: true, // 已登录用户访问时重定向
      title: '登录 - AI运动助手'
    }
  },
  {
    path: '/register',
    name: 'Register', 
    component: Register,
    meta: { 
      requiresAuth: false,
      hideForAuth: true,
      title: '注册 - AI运动助手'
    }
  },
  {
    path: '/',
    component: MainLayout,
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'Home',
        component: FitnessChat,
        meta: { 
          title: 'AI运动助手',
          app: 'fitness'
        }
      },
      {
        path: 'fitness',
        name: 'Fitness',
        component: FitnessChat,
        meta: { 
          title: 'AI健身助手',
          app: 'fitness'
        }
      },
      {
        path: 'manus',
        name: 'Manus',
        component: () => import('@/views/manus/ManusChat.vue'),
        meta: { 
          title: 'AI超级助手',
          app: 'manus'
        }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: Profile,
        meta: { 
          title: '个人资料 - AI运动助手'
        }
      },
      {
        path: 'settings',
        name: 'Settings',
        component: Settings,
        meta: { 
          title: '设置 - AI运动助手'
        }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: NotFound,
    meta: { 
      title: '页面未找到 - AI运动助手'
    }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    } else {
      return { top: 0 }
    }
  }
})

// 全局前置守卫
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  
  // 设置页面标题
  if (to.meta.title) {
    document.title = to.meta.title
  }
  
  // 检查是否需要认证
  if (to.meta.requiresAuth && !authStore.isLoggedIn) {
    // 需要登录但未登录，重定向到登录页
    next({
      name: 'Login',
      query: { redirect: to.fullPath }
    })
    return
  }
  
  // 已登录用户访问登录/注册页面时重定向到首页
  if (to.meta.hideForAuth && authStore.isLoggedIn) {
    next({ name: 'Home' })
    return
  }
  
  next()
})

// 全局后置钩子
router.afterEach((to, from) => {
  // 页面切换后可以添加一些全局逻辑
  // 比如埋点统计、页面访问记录等
})

export default router