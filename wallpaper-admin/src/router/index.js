import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue')
  },
  {
    path: '/',
    component: () => import('@/layout/Index.vue'),
    redirect: '/index',
    children: [
      {
        path: 'index',
        name: 'Index',
        component: () => import('@/views/index.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'wallpapers',
        name: 'Wallpapers',
        component: () => import('@/views/wallpaper/Wallpapers.vue'),
        meta: { title: '壁纸管理' }
      },
      {
        path: 'categories',
        name: 'Categories',
        component: () => import('@/views/category/Categories.vue'),
        meta: { title: '分类管理' }
      },
      {
        path: 'works',
        name: 'Works',
        component: () => import('@/views/works/Works.vue'),
        meta: { title: '作品管理' }
      },
      {
        path: 'authors',
        name: 'Authors',
        component: () => import('@/views/author/Authors.vue'),
        meta: { title: '创作者管理' }
      },
      {
        path: 'audit',
        name: 'WorksAudit',
        component: () => import('@/views/audit/WorksAudit.vue'),
        meta: { title: '作品审核' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  if (to.path !== '/login' && !userStore.token) {
    next('/login')
  } else if (to.path === '/login' && userStore.token) {
    next('/')
  } else {
    next()
  }
})

export default router
