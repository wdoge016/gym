import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: () => import('@/layout/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', name: 'Dashboard', component: () => import('@/views/dashboard/index.vue'), meta: { title: '仪表盘' }},
      { path: 'members', name: 'Members', component: () => import('@/views/member/index.vue'), meta: { title: '会员管理' }},
      { path: 'coaches', name: 'Coaches', component: () => import('@/views/coach/index.vue'), meta: { title: '教练管理' }},
      { path: 'courses', name: 'Courses', component: () => import('@/views/course/index.vue'), meta: { title: '课程管理' }},
      { path: 'schedules', name: 'Schedules', component: () => import('@/views/schedule/index.vue'), meta: { title: '排课管理' }},
      { path: 'reservations', name: 'Reservations', component: () => import('@/views/reservation/index.vue'), meta: { title: '预约管理' }},
      { path: 'orders', name: 'Orders', component: () => import('@/views/order/index.vue'), meta: { title: '订单管理' }},
      { path: 'equipment', name: 'Equipment', component: () => import('@/views/equipment/index.vue'), meta: { title: '器材管理' }},
      { path: 'employees', name: 'Employees', component: () => import('@/views/employee/index.vue'), meta: { title: '员工管理' }},
      { path: 'cards', name: 'Cards', component: () => import('@/views/card/index.vue'), meta: { title: '会员卡' }},
      { path: 'attendance', name: 'Attendance', component: () => import('@/views/attendance/index.vue'), meta: { title: '考勤管理' }},
      { path: 'settings', name: 'Settings', component: () => import('@/views/settings/index.vue'), meta: { title: '系统设置' }}
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
