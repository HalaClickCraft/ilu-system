<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">Tableau de bord Responsable HSE</h1>
        <p class="text-gray-500 mt-1">Hygiene, Securite et Environnement - Suivi des formations de securite</p>
      </div>
      <div class="text-sm text-gray-400">{{ currentDate }}</div>
    </div>

    <!-- HSE KPI Cards -->
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
      <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-5">
        <div class="flex items-center justify-between">
          <div><p class="text-sm font-medium text-gray-500">Opérateurs Formes HSE</p><p class="text-3xl font-bold text-emerald-600 mt-1">{{ hseCertified }}</p></div>
          <div class="w-12 h-12 bg-emerald-50 rounded-xl flex items-center justify-center"><svg class="w-6 h-6 text-emerald-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z"></path></svg></div>
        </div>
      </div>
      <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-5">
        <div class="flex items-center justify-between">
          <div><p class="text-sm font-medium text-gray-500">Formation en Cours</p><p class="text-3xl font-bold text-amber-600 mt-1">{{ inProgressCount }}</p></div>
          <div class="w-12 h-12 bg-amber-50 rounded-xl flex items-center justify-center"><svg class="w-6 h-6 text-amber-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg></div>
        </div>
      </div>
      <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-5">
        <div class="flex items-center justify-between">
          <div><p class="text-sm font-medium text-gray-500">Taux de Conformite</p><p class="text-3xl font-bold mt-1" :class="conformityRate >= 80 ? 'text-emerald-600' : conformityRate >= 50 ? 'text-amber-600' : 'text-red-600'">{{ conformityRate }}%</p></div>
          <div class="w-12 h-12 bg-blue-50 rounded-xl flex items-center justify-center"><svg class="w-6 h-6 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6"></path></svg></div>
        </div>
      </div>
      <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-5">
        <div class="flex items-center justify-between">
          <div><p class="text-sm font-medium text-gray-500">Total Opérateurs</p><p class="text-3xl font-bold text-gray-900 mt-1">{{ totalOperators }}</p></div>
          <div class="w-12 h-12 bg-gray-100 rounded-xl flex items-center justify-center"><svg class="w-6 h-6 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z"></path></svg></div>
        </div>
      </div>
    </div>

    <!-- Safety Compliance & Formation Progress -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
        <h2 class="text-lg font-semibold text-gray-900 mb-4">Conformite par Équipe</h2>
        <div v-if="loading" class="flex items-center justify-center py-12"><div class="w-8 h-8 border-4 border-emerald-200 border-t-emerald-600 rounded-full animate-spin"></div></div>
        <div v-else-if="teamCompliance.length > 0" class="space-y-3">
          <div v-for="team in teamCompliance" :key="team.name" class="p-3 rounded-lg border" :class="team.rate >= 80 ? 'border-emerald-200 bg-emerald-50' : team.rate >= 50 ? 'border-amber-200 bg-amber-50' : 'border-red-200 bg-red-50'">
            <div class="flex items-center justify-between mb-2"><span class="text-sm font-medium text-gray-900">{{ team.name }}</span><span class="text-sm font-bold" :class="team.rate >= 80 ? 'text-emerald-600' : team.rate >= 50 ? 'text-amber-600' : 'text-red-600'">{{ team.rate }}%</span></div>
            <div class="w-full bg-white rounded-full h-2"><div class="h-2 rounded-full" :class="team.rate >= 80 ? 'bg-emerald-500' : team.rate >= 50 ? 'bg-amber-500' : 'bg-red-500'" :style="{ width: team.rate + '%' }"></div></div>
            <p class="text-xs text-gray-500 mt-1">{{ team.certified }} / {{ team.total }} formes</p>
          </div>
        </div>
        <div v-else class="text-center py-12 text-gray-400">Aucune donnee</div>
      </div>

      <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
        <h2 class="text-lg font-semibold text-gray-900 mb-4">Formations HSE en Cours</h2>
        <div v-if="loading" class="flex items-center justify-center py-12"><div class="w-8 h-8 border-4 border-emerald-200 border-t-emerald-600 rounded-full animate-spin"></div></div>
        <div v-else-if="activeFormations.length > 0" class="space-y-2 max-h-80 overflow-y-auto">
          <div v-for="f in activeFormations.slice(0, 8)" :key="f.id" class="flex items-center justify-between p-3 rounded-lg bg-gray-50 hover:bg-gray-100 transition">
            <div class="flex items-center gap-3">
              <div class="w-8 h-8 bg-amber-100 rounded-full flex items-center justify-center text-sm font-medium text-amber-600">{{ f.achievedLevel || 0 }}</div>
              <div><p class="text-sm font-medium text-gray-900">{{ f.operatorName }}</p><p class="text-xs text-gray-500">{{ f.workstationName }}</p></div>
            </div>
            <router-link :to="'/training/' + f.id" class="text-sm text-emerald-600 hover:underline">Suivre</router-link>
          </div>
        </div>
        <div v-else class="text-center py-12 text-gray-400">Aucune formation en cours</div>
      </div>
    </div>

    <!-- Safety Alerts -->
    <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
      <h2 class="text-lg font-semibold text-gray-900 mb-4">Alertes Securite</h2>
      <div v-if="loading" class="flex items-center justify-center py-8"><div class="w-8 h-8 border-4 border-emerald-200 border-t-emerald-600 rounded-full animate-spin"></div></div>
      <div v-else-if="nonCertifiedOperators.length > 0" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3">
        <div v-for="op in nonCertifiedOperators.slice(0, 9)" :key="op.id" class="p-4 rounded-lg border border-red-200 bg-red-50">
          <div class="flex items-center gap-3 mb-2">
            <div class="w-8 h-8 bg-red-100 rounded-full flex items-center justify-center"><svg class="w-4 h-4 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4.5c-.77-.833-2.694-.833-3.464 0L3.34 16.5c-.77.833.192 2.5 1.732 2.5z"></path></svg></div>
            <p class="text-sm font-medium text-gray-900">{{ op.lastName }} {{ op.firstName }}</p>
          </div>
          <p class="text-xs text-red-600 font-medium">Non forme - Formation requise</p>
          <p class="text-xs text-gray-500 mt-1">Matricule: {{ op.employeeId }}</p>
        </div>
      </div>
      <div v-else class="text-center py-8 text-emerald-600 font-medium">Tous les operateurs sont conformes</div>
    </div>

    <!-- Quick Actions -->
    <div class="grid grid-cols-2 sm:grid-cols-3 gap-3">
      <router-link to="/training" class="flex items-center gap-3 p-4 rounded-xl border border-gray-200 hover:border-emerald-300 hover:bg-emerald-50 transition group">
        <div class="w-10 h-10 bg-emerald-100 rounded-lg flex items-center justify-center group-hover:bg-emerald-200 transition"><svg class="w-5 h-5 text-emerald-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253"></path></svg></div>
        <div><p class="text-sm font-medium text-gray-700">Planifier Formation</p><p class="text-xs text-gray-400">Nouvelle formation HSE</p></div>
      </router-link>
      <router-link to="/operators" class="flex items-center gap-3 p-4 rounded-xl border border-gray-200 hover:border-blue-300 hover:bg-blue-50 transition group">
        <div class="w-10 h-10 bg-blue-100 rounded-lg flex items-center justify-center group-hover:bg-blue-200 transition"><svg class="w-5 h-5 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z"></path></svg></div>
        <div><p class="text-sm font-medium text-gray-700">Liste Opérateurs</p><p class="text-xs text-gray-400">Verifier les statuts</p></div>
      </router-link>
      <router-link to="/structure" class="flex items-center gap-3 p-4 rounded-xl border border-gray-200 hover:border-purple-300 hover:bg-purple-50 transition group">
        <div class="w-10 h-10 bg-purple-100 rounded-lg flex items-center justify-center group-hover:bg-purple-200 transition"><svg class="w-5 h-5 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4"></path></svg></div>
        <div><p class="text-sm font-medium text-gray-700">Structure Usine</p><p class="text-xs text-gray-400">Zones et postes</p></div>
      </router-link>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { trainingApi, operatorsApi } from '@/api/endpoints'

const loading = ref(true)
const formations = ref([])
const operators = ref([])
const stats = ref({})

const currentDate = computed(() => new Date().toLocaleDateString('fr-FR', { day: 'numeric', month: 'long', year: 'numeric' }))
const activeFormations = computed(() => formations.value.filter(f => f.status === 'IN_PROGRESS'))
const completedFormations = computed(() => formations.value.filter(f => f.status === 'COMPLETED'))
const inProgressCount = computed(() => activeFormations.value.length)
const hseCertified = computed(() => completedFormations.value.length)
const totalOperators = computed(() => operators.value.length)
const conformityRate = computed(() => { const t = totalOperators.value; return t > 0 ? Math.round((hseCertified.value / t) * 100) : 0 })

const nonCertifiedOperators = computed(() => {
  const certifiedIds = new Set(completedFormations.value.map(f => f.operatorId))
  return operators.value.filter(o => o.active !== false && !certifiedIds.has(o.id))
})

const teamCompliance = computed(() => {
  const teams = {}
  operators.value.filter(o => o.active !== false).forEach(op => {
    const teamName = op.team?.name || 'Non assigne'
    if (!teams[teamName]) teams[teamName] = { name: teamName, total: 0, certified: 0 }
    teams[teamName].total++
  })
  completedFormations.value.forEach(f => {
    const op = operators.value.find(o => o.id === f.operatorId)
    if (op) {
      const teamName = op.team?.name || 'Non assigne'
      if (teams[teamName]) teams[teamName].certified++
    }
  })
  return Object.values(teams).map(t => ({ ...t, rate: t.total > 0 ? Math.round((t.certified / t.total) * 100) : 0 }))
})

onMounted(async () => {
  loading.value = true
  try {
    const [f, s, o] = await Promise.all([trainingApi.getFormations(), trainingApi.getStatistics(), operatorsApi.getAll()])
    formations.value = f.data
    stats.value = s.data
    operators.value = o.data
  } catch (e) { console.error(e) } finally { loading.value = false }
})
</script>