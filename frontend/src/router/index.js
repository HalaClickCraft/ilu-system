import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import MainLayout from '@/views/layout/MainLayout.vue'

const routes = [
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/LoginView.vue'),
    meta: { guest: true },
  },
  {
    path: '/change-password',
    name: 'change-password',
    component: () => import('@/views/ChangePasswordView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/',
    component: MainLayout,
    meta: { requiresAuth: true },
    children: [
      // Dashboard routes (role-based redirect handled in component)
      { path: '', name: 'dashboard', component: () => import('@/views/dashboard/DashboardRouter.vue') },
      // Operator routes
      { path: 'operators', name: 'operators', component: () => import('@/views/operators/OperatorsList.vue') },
      { path: 'operators/:id', name: 'operator-detail', component: () => import('@/views/operators/OperatorDetail.vue') },
      // Training routes
      { path: 'training', name: 'training', component: () => import('@/views/training/TrainingView.vue') },
      { path: 'training/:id', name: 'formation-detail', component: () => import('@/views/training/FormationDetail.vue') },
      // Structure routes
      { path: 'structure', name: 'structure', component: () => import('@/views/structure/StructureView.vue') },
      // Admin routes
      { path: 'admin/users', name: 'admin-users', component: () => import('@/views/admin/UsersList.vue'), meta: { roles: ['ADMIN'] } },
      // HR routes
      { path: 'rh/recrutement', name: 'rh-recrutement', component: () => import('@/views/operators/OperatorsList.vue'), meta: { roles: ['ADMIN', 'RH'] } },
      // Teams route
      { path: 'teams', name: 'teams', component: () => import('@/views/structure/TeamsView.vue') },
           
      { path: 'onboarding', name: 'onboarding', component: () => import('@/views/onboarding/OnboardingView.vue') },
    
    ],
  },
]

const router = createRouter({ history: createWebHistory(), routes })

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    next('/login')
  } else if (to.meta.guest && authStore.isAuthenticated) {
    next('/')
  } else if (to.meta.roles && !authStore.hasAnyRole(to.meta.roles)) {
    next('/')
  } else {
    next()
  }
})

export default router