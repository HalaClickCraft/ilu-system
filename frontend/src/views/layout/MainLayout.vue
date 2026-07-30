<template>
  <div class="min-h-screen flex bg-gray-100">
    <aside :class="[sidebarOpen ? 'w-64' : 'w-20']" class="bg-slate-900 text-white transition-all duration-300 flex flex-col fixed h-full z-30">
      <div class="flex items-center justify-between p-4 border-b border-slate-700">
        <div v-if="sidebarOpen" class="flex items-center gap-2">
          <div class="w-8 h-8 bg-emerald-500 rounded-lg flex items-center justify-center">
            <svg class="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4"></path></svg>
          </div>
          <span class="font-bold text-lg">ILU</span>
        </div>
        <button @click="sidebarOpen = !sidebarOpen" class="p-1 rounded hover:bg-slate-700 transition">
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16"></path></svg>
        </button>
      </div>
      <nav class="flex-1 py-4 space-y-1 px-3 overflow-y-auto">
        <!-- Dashboard - always visible -->
        <router-link to="/" class="flex items-center gap-3 px-3 py-2.5 rounded-lg transition-colors hover:bg-slate-800" :class="{ 'bg-emerald-600 hover:bg-emerald-700': $route.path === '/' }">
          <svg class="w-5 h-5 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2V6zm10 0a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2V6zM4 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2v-2zm10 0a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2v-2z"></path></svg>
          <span v-if="sidebarOpen">{{ dashboardLabel }}</span>
        </router-link>
        <!-- Operators -->
        <router-link to="/operators" class="flex items-center gap-3 px-3 py-2.5 rounded-lg transition-colors hover:bg-slate-800" :class="{ 'bg-emerald-600 hover:bg-emerald-700': $route.path.startsWith('/operators') }">
          <svg class="w-5 h-5 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z"></path></svg>
          <span v-if="sidebarOpen">Operateurs</span>
        </router-link>
        <!-- Training -->
        <router-link to="/training" class="flex items-center gap-3 px-3 py-2.5 rounded-lg transition-colors hover:bg-slate-800" :class="{ 'bg-emerald-600 hover:bg-emerald-700': $route.path.startsWith('/training') }">
          <svg class="w-5 h-5 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253"></path></svg>
          <span v-if="sidebarOpen">Formation</span>
        </router-link>
        <!-- Structure -->
        <router-link to="/structure" class="flex items-center gap-3 px-3 py-2.5 rounded-lg transition-colors hover:bg-slate-800" :class="{ 'bg-emerald-600 hover:bg-emerald-700': $route.path === '/structure' }">
          <svg class="w-5 h-5 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4"></path></svg>
          <span v-if="sidebarOpen">Structure</span>
        </router-link>
        <!-- Teams -->
        <router-link to="/teams" class="flex items-center gap-3 px-3 py-2.5 rounded-lg transition-colors hover:bg-slate-800" :class="{ 'bg-emerald-600 hover:bg-emerald-700': $route.path === '/teams' }">
          <svg class="w-5 h-5 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4"></path></svg>
          <span v-if="sidebarOpen">Equipes</span>
        </router-link>
        <!-- Admin Users - ADMIN only -->
        <router-link v-if="authStore.isAdmin" to="/admin/users" class="flex items-center gap-3 px-3 py-2.5 rounded-lg transition-colors hover:bg-slate-800" :class="{ 'bg-emerald-600 hover:bg-emerald-700': $route.path === '/admin/users' }">
          <svg class="w-5 h-5 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.066 2.573c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.573 1.066c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.066-2.573c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z"></path><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"></path></svg>
          <span v-if="sidebarOpen">Gestion Utilisateurs</span>
        </router-link>
      </nav>
      <div class="p-4 border-t border-slate-700">
        <div class="flex items-center gap-3">
          <div class="w-8 h-8 bg-slate-600 rounded-full flex items-center justify-center text-sm font-medium">{{ userInitials }}</div>
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
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const sidebarOpen = ref(true)

const roleLabels = {
  ADMIN: 'Administrateur',
  RH: 'RH',
  AGENT_QUALITE: 'Agent Qualite',
  RESP_HSE: 'Resp. HSE',
  RESP_QUALITE: 'Resp. Qualite',
  CHEF_EQUIPE: "Chef d'Equipe",
  SUPERVISEUR: 'Superviseur',
}

const dashboardLabels = {
  ADMIN: 'Tableau de bord',
  RH: 'Tableau de bord RH',
  AGENT_QUALITE: 'Qualite',
  RESP_HSE: 'HSE',
  RESP_QUALITE: 'Qualite',
  CHEF_EQUIPE: 'Mon Equipe',
  SUPERVISEUR: 'Mon Equipe',
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