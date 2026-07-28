import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../features/auth/views/LoginView.vue'
import { useAuthStore } from '../stores/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'login',
      component: LoginView,
    },
    {
      path: '/dashboard',
      name: 'dashboard',
      component: () => import('../features/dashboard/views/DashboardView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/formations',
      name: 'formations',
      component: () => import('../features/formations/views/FormationsListView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/formations/new',
      name: 'create-formation',
      component: () => import('../features/formations/views/CreateFormationView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/formations/tracking/:id',
      name: 'formation-tracking',
      component: () => import('../features/formations/views/FormationTrackingView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/formations/templates',
      name: 'formation-templates',
      component: () => import('../features/formations/views/FormationTemplatesView.vue'),
      meta: { requiresAuth: true },
    },
  ],
})

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    next({ name: 'login' })
  } else if (to.name === 'login' && authStore.isAuthenticated) {
    next({ name: 'dashboard' })
  } else {
    next()
  }
})

export default router
