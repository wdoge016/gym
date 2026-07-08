import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/login', name: 'Login', component: () => import('@/views/login/index.vue') },
  {
    path: '/',
    redirect: '/home',
    component: () => import('@/views/layout/MemberLayout.vue'),
    children: [
      { path: 'home', name: 'Home', component: () => import('@/views/home/index.vue') },
      { path: 'reservations', name: 'Reservations', component: () => import('@/views/reservations/index.vue') },
      { path: 'card', name: 'Card', component: () => import('@/views/card/index.vue') },
      { path: 'coach', name: 'Coach', component: () => import('@/views/coach/index.vue') },
      { path: 'ai', name: 'Ai', component: () => import('@/views/ai/index.vue') },
      { path: 'profile', name:'Profile',component: () => import('@/views/profile/index.vue') },
      { path: 'course', name:'Course',component: () => import('@/views/course/index.vue') },
    ]
  }
]

const router = createRouter({ history: createWebHistory(), routes })

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('memberToken')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
