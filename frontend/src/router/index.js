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
      // Dashboard
      {
        path: '',
        name: 'dashboard',
        component: () => import('@/views/dashboard/DashboardRouter.vue'),
      },
      // Operators
      {
        path: 'operators',
        name: 'operators',
        component: () => import('@/views/operators/OperatorsList.vue'),
      },
      {
        path: 'operators/:id',
        name: 'operator-detail',
        component: () => import('@/views/operators/OperatorDetail.vue'),
      },
      // Training
      {
        path: 'training',
        name: 'training',
        component: () => import('@/views/training/TrainingView.vue'),
        meta: { roles: ['ADMIN', 'RH', 'AGENT_QUALITE', 'RESP_QUALITE', 'CHEF_EQUIPE', 'SUPERVISEUR'] },
      },
      {
        path: 'training/:id',
        name: 'formation-detail',
        component: () => import('@/views/training/FormationDetail.vue'),
      },
      // Structure
      {
        path: 'structure',
        name: 'structure',
        component: () => import('@/views/structure/StructureView.vue'),
      },
      {
        path: 'teams',
        name: 'teams',
        component: () => import('@/views/structure/TeamsView.vue'),
        meta: { roles: ['ADMIN', 'RH', 'SUPERVISEUR', 'CHEF_EQUIPE'] },
      },
      // Admin
      {
        path: 'admin/users',
        name: 'admin-users',
        component: () => import('@/views/admin/UsersList.vue'),
        meta: { roles: ['ADMIN'] },
      },
      // RH
      {
        path: 'rh/recrutement',
        name: 'rh-recrutement',
        component: () => import('@/views/operators/OperatorsList.vue'),
        meta: { roles: ['ADMIN', 'RH'] },
      },
      // Onboarding
      {
        path: 'onboarding',
        name: 'onboarding',
        component: () => import('@/views/onboarding/OnboardingView.vue'),
      },
      // Evaluation
      {
        path: 'evaluation/initial',
        name: 'evaluation-initial',
        component: () => import('@/views/evaluation/EvaluationInitial.vue'),
      },
      {
        path: 'evaluation/matrix',
        name: 'evaluation-matrix',
        component: () => import('@/views/evaluation/PolyvalenceMatrix.vue'),
      },
      {
        path: 'evaluation/history',
        name: 'evaluation-history',
        component: () => import('@/views/evaluation/EvaluationHistory.vue'),
      },
      {
        path: 'evaluation/templates',
        name: 'evaluation-templates',
        component: () => import('@/views/evaluation/EvaluationTemplates.vue'),
      },
      {
        path: 'evaluation/questions',
        name: 'evaluation-questions',
        component: () => import('@/views/evaluation/QuestionValidation.vue'),
        meta: { roles: ['RESP_QUALITE', 'ADMIN'] },
      },
      {
        path: 'evaluation/validate',
        name: 'evaluation-validate',
        component: () => import('@/views/evaluation/QuestionValidation.vue'),
        meta: { roles: ['RESP_QUALITE', 'ADMIN'] },
        alias: '/evaluation/questions',
      },
      {
        path: 'evaluation/session/:id',
        name: 'evaluation-session',
        component: () => import('@/views/evaluation/EvaluationSession.vue'),
      },
      // Double echecs (RH + Admin + Qualite + Chef Equipe + Agent Qualite + Superviseur)
      {
        path: 'evaluation/double-failures',
        name: 'evaluation-double-failures',
        component: () => import('@/views/operators/DoubleFailuresList.vue'),
        meta: { roles: ['ADMIN', 'RH', 'RESP_QUALITE', 'AGENT_QUALITE', 'CHEF_EQUIPE', 'SUPERVISEUR'] },
      },
      {
  path: 'recyclage',
  name: 'recyclage',
  component: () => import('@/views/recyclage/RecyclageView.vue'),
  meta: { roles: ['ADMIN', 'RH', 'CHEF_EQUIPE', 'SUPERVISEUR', 'RESP_QUALITE'] },
},
{
  path: 'recyclage/calendar',
  name: 'recyclage-calendar',
  component: () => import('@/views/recyclage/CalendrierRecyclage.vue'),
  meta: { roles: ['ADMIN', 'RH', 'CHEF_EQUIPE', 'SUPERVISEUR', 'RESP_QUALITE'] },
},
{
  path: 'absences',
  name: 'absences',
  component: () => import('@/views/absence/GestionAbsences.vue'),
  meta: { roles: ['ADMIN', 'RH', 'CHEF_EQUIPE', 'SUPERVISEUR'] },
},

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
