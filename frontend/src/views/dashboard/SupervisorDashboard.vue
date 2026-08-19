<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">Tableau de bord Superviseur</h1>
        <p class="text-gray-500 mt-1">Suivi quotidien de l'équipe et avancement des formations</p>
      </div>
      <div class="text-sm text-gray-400">{{ currentDate }}</div>
    </div>

    <!-- Supervisor KPIs -->
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
      <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-5">
        <div class="flex items-center justify-between">
          <div><p class="text-sm font-medium text-gray-500">Opérateurs Actifs</p><p class="text-3xl font-bold text-emerald-600 mt-1">{{ activeOperators.length }}</p></div>
          <div class="w-12 h-12 bg-emerald-50 rounded-xl flex items-center justify-center"><svg class="w-6 h-6 text-emerald-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z"></path></svg></div>
        </div>
      </div>
      <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-5">
        <div class="flex items-center justify-between">
          <div><p class="text-sm font-medium text-gray-500">Formations du Jour</p><p class="text-3xl font-bold text-blue-600 mt-1">{{ todayFormations.length }}</p></div>
          <div class="w-12 h-12 bg-blue-50 rounded-xl flex items-center justify-center"><svg class="w-6 h-6 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"></path></svg></div>
        </div>
      </div>
      <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-5">
        <div class="flex items-center justify-between">
          <div><p class="text-sm font-medium text-gray-500">Niveau Moyen Équipe</p><p class="text-3xl font-bold text-amber-600 mt-1">{{ teamAverageLevel }}</p></div>
          <div class="w-12 h-12 bg-amber-50 rounded-xl flex items-center justify-center"><svg class="w-6 h-6 text-amber-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"></path></svg></div>
        </div>
      </div>
      <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-5">
        <div class="flex items-center justify-between">
          <div><p class="text-sm font-medium text-gray-500">Suivis a Faire</p><p class="text-3xl font-bold text-red-600 mt-1">{{ pendingTrackings }}</p></div>
          <div class="w-12 h-12 bg-red-50 rounded-xl flex items-center justify-center"><svg class="w-6 h-6 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9"></path></svg></div>
        </div>
      </div>
    </div>

    <!-- My Team Overview -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
        <div class="flex items-center justify-between mb-4">
          <h2 class="text-lg font-semibold text-gray-900">Mon Équipe</h2>
          <router-link to="/operators" class="text-sm text-emerald-600 hover:underline">Voir tout</router-link>
        </div>
        <div v-if="loading" class="flex items-center justify-center py-12"><div class="w-8 h-8 border-4 border-emerald-200 border-t-emerald-600 rounded-full animate-spin"></div></div>
        <div v-else-if="activeOperators.length > 0" class="space-y-2 max-h-96 overflow-y-auto">
          <div v-for="op in activeOperators.slice(0, 12)" :key="op.id" class="flex items-center justify-between p-3 rounded-lg hover:bg-gray-50 transition">
            <div class="flex items-center gap-3">
              <div class="w-8 h-8 bg-emerald-100 rounded-full flex items-center justify-center text-sm font-medium text-emerald-700">{{ op.firstName?.[0] || '' }}{{ op.lastName?.[0] || '' }}</div>
              <div><p class="text-sm font-medium text-gray-900">{{ op.lastName }} {{ op.firstName }}</p><p class="text-xs text-gray-500">{{ op.employeeId }}</p></div>
            </div>
            <div class="flex items-center gap-2">
              <span class="text-xs text-gray-400">{{ getOperatorLevel(op) }}</span>
              <router-link :to="'/operators/' + op.id" class="text-emerald-600 hover:text-emerald-700"><svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"></path></svg></router-link>
            </div>
          </div>
        </div>
        <div v-else class="text-center py-12 text-gray-400">Aucun operateur actif</div>
      </div>

      <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
        <div class="flex items-center justify-between mb-4">
          <h2 class="text-lg font-semibold text-gray-900">Formations Actives</h2>
          <router-link to="/training" class="text-sm text-emerald-600 hover:underline">Voir toutes</router-link>
        </div>
        <div v-if="loading" class="flex items-center justify-center py-12"><div class="w-8 h-8 border-4 border-emerald-200 border-t-emerald-600 rounded-full animate-spin"></div></div>
        <div v-else-if="activeFormations.length > 0" class="space-y-2 max-h-96 overflow-y-auto">
          <div v-for="f in activeFormations.slice(0, 8)" :key="f.id" class="flex items-center justify-between p-3 rounded-lg bg-gray-50 hover:bg-gray-100 transition">
            <div class="flex items-center gap-3">
              <div class="w-8 h-8 bg-amber-100 rounded-lg flex items-center justify-center text-sm font-bold text-amber-700">{{ f.achievedLevel || 0 }}/{{ f.targetLevel }}</div>
              <div><p class="text-sm font-medium text-gray-900">{{ f.operatorName }}</p><p class="text-xs text-gray-500">{{ f.workstationName }}</p></div>
            </div>
            <router-link :to="'/training/' + f.id" class="inline-flex items-center gap-1 text-sm text-emerald-600 hover:underline">
              Suivi<svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"></path></svg>
            </router-link>
          </div>
        </div>
        <div v-else class="text-center py-12 text-gray-400">Aucune formation active</div>
      </div>
    </div>

    <!-- Daily Tasks -->
    <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
      <h2 class="text-lg font-semibold text-gray-900 mb-4">Actions du Jour</h2>
      <div class="grid grid-cols-1 sm:grid-cols-3 gap-3">
        <router-link to="/training" class="flex items-center gap-3 p-4 rounded-xl border border-gray-200 hover:border-emerald-300 hover:bg-emerald-50 transition group">
          <div class="w-10 h-10 bg-amber-100 rounded-lg flex items-center justify-center group-hover:bg-amber-200 transition"><svg class="w-5 h-5 text-amber-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"></path></svg></div>
          <div><p class="text-sm font-medium text-gray-700">Ajouter Suivi Quotidien</p><p class="text-xs text-gray-400">Enregistrer les progres</p></div>
        </router-link>
        <router-link to="/training" class="flex items-center gap-3 p-4 rounded-xl border border-gray-200 hover:border-blue-300 hover:bg-blue-50 transition group">
          <div class="w-10 h-10 bg-blue-100 rounded-lg flex items-center justify-center group-hover:bg-blue-200 transition"><svg class="w-5 h-5 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg></div>
          <div><p class="text-sm font-medium text-gray-700">Valider Formation</p><p class="text-xs text-gray-400">Marquer comme terminee</p></div>
        </router-link>
        <router-link to="/operators" class="flex items-center gap-3 p-4 rounded-xl border border-gray-200 hover:border-purple-300 hover:bg-purple-50 transition group">
          <div class="w-10 h-10 bg-purple-100 rounded-lg flex items-center justify-center group-hover:bg-purple-200 transition"><svg class="w-5 h-5 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z"></path></svg></div>
          <div><p class="text-sm font-medium text-gray-700">Consulter Équipe</p><p class="text-xs text-gray-400">Détails des operateurs</p></div>
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { trainingApi, operatorsApi } from '@/api/endpoints'

const loading = ref(true)
const operators = ref([])
const formations = ref([])
const stats = ref({})

const currentDate = computed(() => new Date().toLocaleDateString('fr-FR', { day: 'numeric', month: 'long', year: 'numeric' }))
const activeOperators = computed(() => operators.value.filter(o => o.active !== false))
const activeFormations = computed(() => formations.value.filter(f => f.status === 'IN_PROGRESS'))

const todayFormations = computed(() => {
  const today = new Date().toISOString().split('T')[0]
  return activeFormations.value.filter(f => f.startDate === today)
})

const teamAverageLevel = computed(() => {
  const active = activeFormations.value
  if (active.length === 0) return '0'
  const sum = active.reduce((s, f) => s + (f.achievedLevel || 0), 0)
  return (sum / active.length).toFixed(1)
})

const pendingTrackings = computed(() => activeFormations.value.length)

const getOperatorLevel = (op) => {
  const opFormations = formations.value.filter(f => f.operatorId === op.id && f.status === 'IN_PROGRESS')
 if (opFormations.length === 0) return '-'
 return 'Nv.' + (opFormations[0].achievedLevel || 0)
}

onMounted(async () => {
  loading.value = true
  try {
    const [o, f, s] = await Promise.all([operatorsApi.getAll(), trainingApi.getFormations(), trainingApi.getStatistics()])
    operators.value = o.data
    formations.value = f.data
    stats.value = s.data
  } catch (e) { console.error(e) } finally { loading.value = false }
})
</script>