<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">Tableau de bord Chef d'Equipe</h1>
        <p class="text-gray-500 mt-1">Suivi quotidien des operateurs et formations de la zone</p>
      </div>
      <div class="text-sm text-gray-400">{{ currentDate }}</div>
    </div>

    <!-- KPIs -->
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
      <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-5">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm font-medium text-gray-500">Operateurs dans la Zone</p>
            <p class="text-3xl font-bold text-emerald-600 mt-1">{{ activeOperators.length }}</p>
          </div>
          <div class="w-12 h-12 bg-emerald-50 rounded-xl flex items-center justify-center">
            <svg class="w-6 h-6 text-emerald-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z"></path></svg>
          </div>
        </div>
      </div>
      <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-5">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm font-medium text-gray-500">En Integration (12j)</p>
            <p class="text-3xl font-bold text-amber-600 mt-1">{{ integrationOperators.length }}</p>
          </div>
          <div class="w-12 h-12 bg-amber-50 rounded-xl flex items-center justify-center">
            <svg class="w-6 h-6 text-amber-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
          </div>
        </div>
      </div>
      <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-5">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm font-medium text-gray-500">Niveau ILU Moyen</p>
            <p class="text-3xl font-bold text-blue-600 mt-1">{{ averageLevel }}</p>
          </div>
          <div class="w-12 h-12 bg-blue-50 rounded-xl flex items-center justify-center">
            <svg class="w-6 h-6 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6"></path></svg>
          </div>
        </div>
      </div>
      <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-5">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm font-medium text-gray-500">Suivis Aujourd'hui</p>
            <p class="text-3xl font-bold text-purple-600 mt-1">{{ todayTrackingsCount }}</p>
          </div>
          <div class="w-12 h-12 bg-purple-50 rounded-xl flex items-center justify-center">
            <svg class="w-6 h-6 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4"></path></svg>
          </div>
        </div>
      </div>
    </div>

    <!-- 12-Day Integration Tracking -->
    <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
      <div class="flex items-center justify-between mb-4">
        <h2 class="text-lg font-semibold text-gray-900">Suivi Integration 12 Jours</h2>
        <div class="flex items-center gap-2 text-xs">
          <span class="flex items-center gap-1"><span class="w-3 h-3 rounded bg-emerald-500"></span> Jour complete</span>
          <span class="flex items-center gap-1"><span class="w-3 h-3 rounded bg-amber-400"></span> En cours</span>
          <span class="flex items-center gap-1"><span class="w-3 h-3 rounded bg-gray-200"></span> Restant</span>
        </div>
      </div>
      <div v-if="loading" class="flex items-center justify-center py-12"><div class="w-8 h-8 border-4 border-emerald-200 border-t-emerald-600 rounded-full animate-spin"></div></div>
      <div v-else-if="integrationOperators.length > 0" class="space-y-4 max-h-96 overflow-y-auto">
        <div v-for="op in integrationOperators" :key="op.formationId" class="border border-gray-100 rounded-lg p-4">
          <div class="flex items-center justify-between mb-2">
            <div class="flex items-center gap-2">
              <div class="w-8 h-8 bg-amber-100 rounded-full flex items-center justify-center text-sm font-bold text-amber-700">{{ op.initials }}</div>
              <div>
                <p class="text-sm font-medium text-gray-900">{{ op.name }}</p>
                <p class="text-xs text-gray-500">{{ op.workstationName }} - Nv.{{ op.achievedLevel }}/{{ op.targetLevel }}</p>
              </div>
            </div>
            <span class="text-xs font-medium px-2 py-1 rounded-full" :class="op.dayCount >= 12 ? 'bg-emerald-100 text-emerald-700' : 'bg-amber-100 text-amber-700'">Jour {{ op.dayCount }}/12</span>
          </div>
          <div class="flex gap-1">
            <div v-for="d in 12" :key="d" class="h-6 flex-1 rounded-sm text-center text-xs leading-6 font-medium" :class="getDayClass(op, d)">{{ d }}</div>
          </div>
        </div>
      </div>
      <div v-else class="text-center py-12 text-gray-400">Aucun operateur en periode d'integration</div>
    </div>

    <!-- Operator Quick Overview + Actions -->
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <!-- Team List -->
      <div class="lg:col-span-2 bg-white rounded-xl shadow-sm border border-gray-200 p-6">
        <div class="flex items-center justify-between mb-4">
          <h2 class="text-lg font-semibold text-gray-900">Equipe - Postes et Niveaux</h2>
          <router-link to="/operators" class="text-sm text-emerald-600 hover:underline">Voir tout</router-link>
        </div>
        <div v-if="loading" class="flex items-center justify-center py-12"><div class="w-8 h-8 border-4 border-emerald-200 border-t-emerald-600 rounded-full animate-spin"></div></div>
        <div v-else-if="activeOperators.length > 0" class="overflow-x-auto">
          <table class="w-full text-sm">
            <thead class="bg-gray-50">
              <tr>
                <th class="text-left py-2.5 px-3 font-medium text-gray-500">Operateur</th>
                <th class="text-left py-2.5 px-3 font-medium text-gray-500">Poste</th>
                <th class="text-center py-2.5 px-3 font-medium text-gray-500">Niveau ILU</th>
                <th class="text-center py-2.5 px-3 font-medium text-gray-500">Statut</th>
                <th class="text-right py-2.5 px-3 font-medium text-gray-500">Action</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="op in activeOperators" :key="op.id" class="border-b border-gray-50 hover:bg-gray-50">
                <td class="py-2.5 px-3">
                  <p class="text-sm font-medium text-gray-900">{{ op.lastName }} {{ op.firstName }}</p>
                  <p class="text-xs text-gray-400">{{ op.employeeId }}</p>
                </td>
                <td class="py-2.5 px-3 text-gray-600">{{ getWorkstation(op) }}</td>
                <td class="py-2.5 px-3 text-center">
                  <span class="inline-flex items-center justify-center w-8 h-8 rounded-full text-xs font-bold" :class="getLevelClass(op)">{{ getLevel(op) }}</span>
                </td>
                <td class="py-2.5 px-3 text-center">
                  <span class="text-xs font-medium px-2 py-0.5 rounded-full" :class="getStatusClass(op)">{{ getStatus(op) }}</span>
                </td>
                <td class="py-2.5 px-3 text-right">
                  <router-link :to="'/training/' + getFormationId(op)" class="text-emerald-600 hover:text-emerald-700 text-xs font-medium hover:underline">Suivi</router-link>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <div v-else class="text-center py-12 text-gray-400">Aucun operateur actif</div>
      </div>

      <!-- Quick Actions -->
      <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
        <h2 class="text-lg font-semibold text-gray-900 mb-4">Actions Rapides</h2>
        <div class="space-y-3">
          <router-link to="/training" class="flex items-center gap-3 p-3 rounded-xl border border-gray-200 hover:border-emerald-300 hover:bg-emerald-50 transition group">
            <div class="w-10 h-10 bg-emerald-100 rounded-lg flex items-center justify-center group-hover:bg-emerald-200 transition">
              <svg class="w-5 h-5 text-emerald-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"></path></svg>
            </div>
            <div>
              <p class="text-sm font-medium text-gray-700">Nouvelle Formation</p>
              <p class="text-xs text-gray-400">Demarrer une formation</p>
            </div>
          </router-link>
          <router-link to="/training" class="flex items-center gap-3 p-3 rounded-xl border border-gray-200 hover:border-amber-300 hover:bg-amber-50 transition group">
            <div class="w-10 h-10 bg-amber-100 rounded-lg flex items-center justify-center group-hover:bg-amber-200 transition">
              <svg class="w-5 h-5 text-amber-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"></path></svg>
            </div>
            <div>
              <p class="text-sm font-medium text-gray-700">Suivi Quotidien</p>
              <p class="text-xs text-gray-400">Evaluer niveau du jour</p>
            </div>
          </router-link>
          <router-link to="/operators" class="flex items-center gap-3 p-3 rounded-xl border border-gray-200 hover:border-blue-300 hover:bg-blue-50 transition group">
            <div class="w-10 h-10 bg-blue-100 rounded-lg flex items-center justify-center group-hover:bg-blue-200 transition">
              <svg class="w-5 h-5 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"></path></svg>
            </div>
            <div>
              <p class="text-sm font-medium text-gray-700">Affecter Operateur</p>
              <p class="text-xs text-gray-400">Assigner a un poste</p>
            </div>
          </router-link>
          <router-link to="/structure" class="flex items-center gap-3 p-3 rounded-xl border border-gray-200 hover:border-purple-300 hover:bg-purple-50 transition group">
            <div class="w-10 h-10 bg-purple-100 rounded-lg flex items-center justify-center group-hover:bg-purple-200 transition">
              <svg class="w-5 h-5 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4"></path></svg>
            </div>
            <div>
              <p class="text-sm font-medium text-gray-700">Ma Zone</p>
              <p class="text-xs text-gray-400">Voir postes et structure</p>
            </div>
          </router-link>
        </div>
      </div>
    </div>

    <!-- Recent Tracking Activity -->
    <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
      <div class="flex items-center justify-between mb-4">
        <h2 class="text-lg font-semibold text-gray-900">Derniers Suivis Enregistres</h2>
        <router-link to="/training" class="text-sm text-emerald-600 hover:underline">Voir tout</router-link>
      </div>
      <div v-if="loading" class="flex items-center justify-center py-12"><div class="w-8 h-8 border-4 border-emerald-200 border-t-emerald-600 rounded-full animate-spin"></div></div>
      <div v-else-if="recentTrackings.length > 0" class="space-y-2">
        <div v-for="t in recentTrackings" :key="t.id" class="flex items-center justify-between p-3 rounded-lg bg-gray-50">
          <div class="flex items-center gap-3">
            <div class="w-8 h-8 rounded-full flex items-center justify-center text-xs font-bold" :class="t.dailyLevel >= 3 ? 'bg-emerald-100 text-emerald-700' : t.dailyLevel >= 1 ? 'bg-amber-100 text-amber-700' : 'bg-red-100 text-red-700'">{{ t.dailyLevel }}</div>
            <div>
              <p class="text-sm font-medium text-gray-900">{{ t.operatorName }}</p>
              <p class="text-xs text-gray-500">{{ t.workstationName }} - {{ t.trackingDate }}</p>
            </div>
          </div>
          <span v-if="t.comment" class="text-xs text-gray-500 max-w-48 truncate">{{ t.comment }}</span>
        </div>
      </div>
      <div v-else class="text-center py-12 text-gray-400">Aucun suivi enregistre</div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { trainingApi, operatorsApi } from '@/api/endpoints.js'

const loading = ref(true)
const operators = ref([])
const formations = ref([])
const allTrackings = ref([])

const currentDate = computed(() => new Date().toLocaleDateString('fr-FR', { day: 'numeric', month: 'long', year: 'numeric' }))
const activeOperators = computed(() => operators.value.filter(o => o.active !== false))
const activeFormations = computed(() => formations.value.filter(f => f.status === 'IN_PROGRESS'))

// Operators currently in their 12-day integration period
const integrationOperators = computed(() => {
  return activeFormations.value.map(f => {
    const startDate = f.startDate ? new Date(f.startDate) : null
    const today = new Date()
    let dayCount = 0
    if (startDate) {
      const diff = Math.floor((today - startDate) / (1000 * 60 * 60 * 24)) + 1
      dayCount = Math.max(0, Math.min(diff, 12))
    }
    const op = operators.value.find(o => o.id === f.operatorId)
    return {
      formationId: f.id,
      name: op ? `${op.lastName} ${op.firstName}` : 'Inconnu',
      initials: op ? `${(op.lastName || '')[0]}${(op.firstName || '')[0]}` : '?',
      workstationName: f.workstationName || '-',
      achievedLevel: f.achievedLevel || 0,
      targetLevel: f.targetLevel || 0,
      dayCount,
      trackingDays: getTrackingDaysForFormation(f.id),
    }
  })
})

const averageLevel = computed(() => {
  const active = activeFormations.value
  if (active.length === 0) return '0'
  const sum = active.reduce((s, f) => s + (f.achievedLevel || 0), 0)
  return (sum / active.length).toFixed(1)
})

const todayTrackingsCount = computed(() => {
  const today = new Date().toISOString().split('T')[0]
  return allTrackings.value.filter(t => t.trackingDate === today).length
})

const recentTrackings = computed(() => {
  return allTrackings.value.slice(0, 8).map(t => {
    const f = formations.value.find(fo => fo.id === t.formationId)
    const op = f ? operators.value.find(o => o.id === f.operatorId) : null
    return {
      ...t,
      operatorName: op ? `${op.lastName} ${op.firstName}` : 'Inconnu',
      workstationName: f?.workstationName || '-',
    }
  })
})

function getTrackingDaysForFormation(formationId) {
  return allTrackings.value.filter(t => t.formationId === formationId).map(t => t.trackingDate)
}

function getDayClass(op, day) {
  const startDate = op.formationId ? formations.value.find(f => f.id === op.formationId)?.startDate : null
  if (!startDate) return 'bg-gray-100 text-gray-400'
  const start = new Date(startDate)
  const dayDate = new Date(start)
  dayDate.setDate(dayDate.getDate() + day - 1)
  const dateStr = dayDate.toISOString().split('T')[0]
  const today = new Date().toISOString().split('T')[0]
  if (op.trackingDays.includes(dateStr)) return 'bg-emerald-500 text-white'
  if (dateStr <= today) return 'bg-red-200 text-red-700'
  return 'bg-gray-100 text-gray-400'
}

function getLevel(op) {
  const f = activeFormations.value.find(fo => fo.operatorId === op.id)
  return f ? (f.achievedLevel || 0) : '-'
}

function getLevelClass(op) {
  const level = getLevel(op)
  if (typeof level !== 'number') return 'bg-gray-100 text-gray-500'
  if (level >= 4) return 'bg-emerald-100 text-emerald-700'
  if (level >= 2) return 'bg-amber-100 text-amber-700'
  return 'bg-red-100 text-red-700'
}

function getWorkstation(op) {
  const f = formations.value.find(fo => fo.operatorId === op.id && fo.status === 'IN_PROGRESS')
  return f ? f.workstationName : '-'
}

function getFormationId(op) {
  const f = formations.value.find(fo => fo.operatorId === op.id && fo.status === 'IN_PROGRESS')
  return f ? f.id : ''
}

function getStatus(op) {
  const f = activeFormations.value.find(fo => fo.operatorId === op.id)
  if (!f) return 'AUCUNE'
  const startDate = f.startDate ? new Date(f.startDate) : null
  if (startDate) {
    const diff = Math.floor((new Date() - startDate) / (1000 * 60 * 60 * 24)) + 1
    if (diff > 12) return 'TERMINEE'
    return `J${diff}/12`
  }
  return 'EN COURS'
}

function getStatusClass(op) {
  const status = getStatus(op)
  if (status === 'TERMINEE') return 'bg-emerald-100 text-emerald-700'
  if (status === 'AUCUNE') return 'bg-gray-100 text-gray-500'
  return 'bg-amber-100 text-amber-700'
}

onMounted(async () => {
  loading.value = true
  try {
    const [o, f] = await Promise.all([operatorsApi.getAll(), trainingApi.getFormations()])
    operators.value = o.data
    formations.value = f.data
    // Fetch tracking for each active formation
    const trackingPromises = activeFormations.value.map(fo => trainingApi.getTracking(fo.id))
    const trackingResults = await Promise.all(trackingPromises)
    allTrackings.value = trackingResults.flatMap(r => r.data).sort((a, b) => b.trackingDate?.localeCompare(a.trackingDate))
  } catch (e) { console.error(e) } finally { loading.value = false }
})
</script>