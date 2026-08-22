<template>
  <div class="min-h-screen flex bg-gray-100">
    <aside
      :class="[sidebarOpen ? 'w-64' : 'w-20']"
      class="bg-slate-900 text-white transition-all duration-300 flex flex-col fixed h-full z-30"
    >
      <div class="flex items-center justify-between p-4 border-b border-slate-700">
        <div v-if="sidebarOpen" class="flex items-center gap-2">
          <div class="w-8 h-8 bg-emerald-500 rounded-lg flex items-center justify-center">
            <svg class="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4"></path>
            </svg>
          </div>
          <span class="font-bold text-lg">ILU</span>
        </div>
        <button @click="sidebarOpen = !sidebarOpen" class="p-1 rounded hover:bg-slate-700 transition">
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16"></path>
          </svg>
        </button>
      </div>

      <nav class="flex-1 py-3 overflow-y-auto">
        <!-- ===== SECTION: MAIN ===== -->
        <div v-if="sidebarOpen" class="px-4 pt-2 pb-1"><span class="text-[10px] font-semibold uppercase tracking-wider text-slate-500">Principal</span></div>

        <router-link v-if="!isDeptOnly" to="/" class="nav-item" :class="{ active: $route.path === '/' }">
          <svg class="w-5 h-5 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2V6zm10 0a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2V6zM4 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2v-2zm10 0a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2v-2z"></path></svg>
          <span v-if="sidebarOpen">{{ dashboardLabel }}</span>
        </router-link>

        <router-link v-if="!isDeptOnly" to="/operators" class="nav-item" :class="{ active: $route.path.startsWith('/operators') }">
          <svg class="w-5 h-5 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z"></path></svg>
          <span v-if="sidebarOpen">Operateurs</span>
        </router-link>

        <router-link v-if="!isDeptOnly && !authStore.isRespHse" to="/training" class="nav-item" :class="{ active: $route.path.startsWith('/training') }">
          <svg class="w-5 h-5 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253"></path></svg>
          <span v-if="sidebarOpen">Formation</span>
        </router-link>

        <router-link to="/onboarding" class="nav-item" :class="{ active: $route.path.startsWith('/onboarding') }">
          <svg class="w-5 h-5 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4"></path></svg>
          <span v-if="sidebarOpen">Onboarding</span>
        </router-link>

        <router-link v-if="canAccessRecyclage" to="/recyclage" class="nav-item" :class="{ active: $route.path === '/recyclage' }">
          <svg class="w-5 h-5 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"/></svg>
          <span v-if="sidebarOpen">Recyclage</span>
        </router-link>

        <router-link v-if="canAccessRecyclage" to="/recyclage/calendar" class="nav-item" :class="{ active: $route.path === '/recyclage/calendar' }">
          <svg class="w-5 h-5 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3M4 11h16M5 5h14a1 1 0 011 1v13a1 1 0 01-1 1H5a1 1 0 01-1-1V6a1 1 0 011-1z"/></svg>
          <span v-if="sidebarOpen">Calendrier recyclage</span>
        </router-link>

        <router-link v-if="canManageAbsences" to="/absences" class="nav-item" :class="{ active: $route.path === '/absences' }">
          <svg class="w-5 h-5 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3M4 11h16M5 5h14a1 1 0 011 1v13a1 1 0 01-1 1H5a1 1 0 01-1-1V6a1 1 0 011-1zM8 15h8"></path></svg>
          <span v-if="sidebarOpen">Absences et départs</span>
        </router-link>

        <!-- ===== SECTION: EVALUATION ===== -->
        <div v-if="showEvaluationSection" class="mt-2">
          <div v-if="sidebarOpen" class="px-4 pt-3 pb-1"><span class="text-[10px] font-semibold uppercase tracking-wider text-slate-500">Evaluation</span></div>
          <div v-else class="my-2 mx-4 border-t border-slate-700"></div>

          <router-link to="/evaluation/initial" class="nav-item" :class="{ active: $route.path === '/evaluation/initial' }">
            <svg class="w-5 h-5 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
            <span v-if="sidebarOpen">Evaluation Initiale</span>
          </router-link>

          <router-link to="/evaluation/matrix" class="nav-item" :class="{ active: $route.path === '/evaluation/matrix' }">
            <svg class="w-5 h-5 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 10h18M3 14h18M3 6h18M3 18h18M10 3v18M14 3v18"></path></svg>
            <span v-if="sidebarOpen">Matrice Polyvalence</span>
          </router-link>

          <router-link to="/evaluation/history" class="nav-item" :class="{ active: $route.path === '/evaluation/history' }">
            <svg class="w-5 h-5 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
            <span v-if="sidebarOpen">Historique Evaluations</span>
          </router-link>

          <router-link v-if="canManageQuestions" to="/evaluation/questions" class="nav-item" :class="{ active: $route.path.startsWith('/evaluation/questions') || $route.path.startsWith('/evaluation/validate') }">
            <svg class="w-5 h-5 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4"></path></svg>
            <span v-if="sidebarOpen">Validation Questions</span>
          </router-link>

          <router-link v-if="canAccessEvaluation" to="/evaluation/templates" class="nav-item" :class="{ active: $route.path.startsWith('/evaluation/templates') }">
            <svg class="w-5 h-5 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 5a2 2 0 012-2h12a2 2 0 012 2v14a2 2 0 01-2 2H6a2 2 0 01-2-2V5zm4 3h8m-8 4h8m-8 4h5"></path></svg>
            <span v-if="sidebarOpen">Templates Questions</span>
          </router-link>

          <router-link v-if="showDoubleFailures" to="/evaluation/double-failures" class="nav-item" :class="{ active: $route.path === '/evaluation/double-failures' }">
            <svg class="w-5 h-5 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4.5c-.77-.833-2.694-.833-3.464 0L3.34 16.5c-.77.833.192 2.5 1.732 2.5z"></path></svg>
            <span v-if="sidebarOpen">Double Echecs</span>
          </router-link>
        </div>

        <!-- ===== SECTION: ORGANISATION ===== -->
        <div v-if="!isDeptOnly" class="mt-2">
          <div v-if="sidebarOpen" class="px-4 pt-3 pb-1"><span class="text-[10px] font-semibold uppercase tracking-wider text-slate-500">Organisation</span></div>
          <div v-else class="my-2 mx-4 border-t border-slate-700"></div>

          <router-link to="/structure" class="nav-item" :class="{ active: $route.path === '/structure' }">
            <svg class="w-5 h-5 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 5a1 1 0 011-1h4a1 1 0 011 1v4a1 1 0 01-1 1H5a1 1 0 01-1-1V5zm10 0a1 1 0 011-1h4a1 1 0 011 1v4a1 1 0 01-1 1h-4a1 1 0 01-1-1V5zM4 15a1 1 0 011-1h4a1 1 0 011 1v4a1 1 0 01-1 1H5a1 1 0 01-1-1v-4zm10-1h4m-4 0a1 1 0 00-1 1v4a1 1 0 001 1h4a1 1 0 001-1v-4a1 1 0 00-1-1m-4 0h4"></path></svg>
            <span v-if="sidebarOpen">Structure</span>
          </router-link>

          <router-link v-if="showProjectAssignments" to="/teams" class="nav-item" :class="{ active: $route.path === '/teams' }">
            <svg class="w-5 h-5 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"></path></svg>
            <span v-if="sidebarOpen">Repartition Projets</span>
          </router-link>
        </div>

        <!-- ===== SECTION: ADMIN ===== -->
        <div v-if="authStore.isAdmin" class="mt-2">
          <div v-if="sidebarOpen" class="px-4 pt-3 pb-1"><span class="text-[10px] font-semibold uppercase tracking-wider text-slate-500">Administration</span></div>
          <div v-else class="my-2 mx-4 border-t border-slate-700"></div>

          <router-link to="/admin/users" class="nav-item" :class="{ active: $route.path === '/admin/users' }">
            <svg class="w-5 h-5 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.066 2.573c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.573 1.066c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.066-2.573c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z"></path><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"></path></svg>
            <span v-if="sidebarOpen">Gestion Utilisateurs</span>
          </router-link>
        </div>
      </nav>

      <!-- User area at bottom -->
      <div class="p-4 border-t border-slate-700">
        <div class="flex items-center gap-3">
          <div class="w-8 h-8 bg-slate-600 rounded-full flex items-center justify-center text-sm font-medium">
            {{ userInitials }}
          </div>
          <div v-if="sidebarOpen" class="flex-1 min-w-0">
            <p class="text-sm font-medium truncate">{{ authStore.fullName }}</p>
            <p class="text-xs text-slate-400">{{ roleLabel }}</p>
          </div>
          <button @click="handleLogout" class="p-1.5 rounded hover:bg-slate-700 transition" title="Deconnexion">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"></path></svg>
          </button>
        </div>
      </div>
    </aside>
    <main :class="[sidebarOpen ? 'ml-64' : 'ml-20']" class="flex-1 transition-all duration-300">
      <div class="p-6">
        <router-view />
        <ConfirmDialog />
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import ConfirmDialog from '@/components/ConfirmDialog.vue'

const router = useRouter()
const authStore = useAuthStore()
const sidebarOpen = ref(true)

const DEPT_ONLY_ROLES = ['DEPT_PROCESS', 'DEPT_MAINTENANCE', 'DEPT_DGT_MANUFACTURING']

const isDeptOnly = computed(() => authStore.hasAnyRole(DEPT_ONLY_ROLES))

const showEvaluationSection = computed(() =>
  authStore.hasAnyRole(['ADMIN', 'AGENT_QUALITE', 'RESP_QUALITE', 'RESP_HSE', 'CHEF_EQUIPE', 'SUPERVISEUR', 'RH'])
)
const canAccessEvaluation = computed(() =>
  authStore.hasAnyRole(['ADMIN', 'CHEF_EQUIPE', 'RESP_HSE', 'AGENT_QUALITE', 'RESP_QUALITE'])
)
const canManageQuestions = computed(() => authStore.hasAnyRole(['ADMIN', 'RESP_QUALITE']))
const showDoubleFailures = computed(() =>
  authStore.hasAnyRole(['ADMIN', 'RH', 'RESP_QUALITE', 'AGENT_QUALITE', 'CHEF_EQUIPE', 'SUPERVISEUR'])
)
const canAccessRecyclage = computed(() =>
  authStore.hasAnyRole(['ADMIN', 'RH', 'CHEF_EQUIPE', 'SUPERVISEUR', 'RESP_QUALITE'])
)
const canManageAbsences = computed(() =>
  authStore.hasAnyRole(['ADMIN', 'RH', 'CHEF_EQUIPE', 'SUPERVISEUR'])
)
// "Repartition Projets" - who works on which project. Primarily for RH/Superviseur
// (need it to affect people to projects) but Chef Equipe can see their own project too.
const showProjectAssignments = computed(() =>
  authStore.hasAnyRole(['ADMIN', 'RH', 'SUPERVISEUR', 'CHEF_EQUIPE'])
)

const roleLabels = {
  ADMIN: 'Administrateur',
  RH: 'RH',
  AGENT_QUALITE: 'Agent Qualite',
  RESP_HSE: 'Resp. HSE',
  RESP_QUALITE: 'Resp. Qualite',
  CHEF_EQUIPE: "Chef d'Equipe",
  SUPERVISEUR: 'Superviseur',
  DEPT_PROCESS: 'Chef Dept Process',
  DEPT_MAINTENANCE: 'Chef Dept Maintenance',
  DEPT_DGT_MANUFACTURING: 'Chef Dept DGT Mfg',
}

const dashboardLabels = {
  ADMIN: 'Tableau de bord',
  RH: 'Tableau de bord RH',
  AGENT_QUALITE: 'Qualite',
  RESP_HSE: 'HSE',
  RESP_QUALITE: 'Qualite',
  CHEF_EQUIPE: 'Tableau de bord',
  SUPERVISEUR: 'Tableau de bord',
  DEPT_PROCESS: 'Onboarding',
  DEPT_MAINTENANCE: 'Onboarding',
  DEPT_DGT_MANUFACTURING: 'Onboarding',
}

const roleLabel = computed(() => roleLabels[authStore.primaryRole] || authStore.primaryRole || '')
const dashboardLabel = computed(() => dashboardLabels[authStore.primaryRole] || 'Tableau de bord')

const userInitials = computed(() => {
  if (!authStore.user?.name) return 'U'
  const parts = authStore.user.name.split(' ')
  if (parts.length >= 2) return (parts[0][0] + parts[1][0]).toUpperCase()
  return parts[0].substring(0, 2).toUpperCase()
})

const handleLogout = () => {
  authStore.logout()
  router.push('/login')
}

onMounted(() => {
  if (!authStore.user && authStore.isAuthenticated) {
    authStore.restoreFromToken()
  }
})
</script>

<style scoped>
.nav-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.625rem 0.75rem;
  margin: 2px 0.5rem;
  border-radius: 0.5rem;
  color: #cbd5e1;
  font-size: 0.875rem;
  transition: all 0.15s ease;
  text-decoration: none;
}
.nav-item:hover {
  background: #1e293b;
  color: #f1f5f9;
}
.nav-item.active {
  background: #059669;
  color: #fff;
  font-weight: 500;
}
</style>
