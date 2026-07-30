<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">Tableau de bord Responsable Qualite</h1>
        <p class="text-gray-500 mt-1">Pilotage de la qualite et metriques de performance ILU</p>
      </div>
      <div class="text-sm text-gray-400">{{ currentDate }}</div>
    </div>

    <!-- Quality Performance KPIs -->
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
      <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-5">
        <div class="flex items-center justify-between">
          <div><p class="text-sm font-medium text-gray-500">Taux de Certification</p><p class="text-3xl font-bold mt-1" :class="certificationRate >= 70 ? 'text-emerald-600' : certificationRate >= 40 ? 'text-amber-600' : 'text-red-600'">{{ certificationRate }}%</p></div>
          <div class="w-12 h-12 bg-emerald-50 rounded-xl flex items-center justify-center"><svg class="w-6 h-6 text-emerald-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg></div>
        </div>
      </div>
      <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-5">
        <div class="flex items-center justify-between">
          <div><p class="text-sm font-medium text-gray-500">Niveau ILU Moyen</p><p class="text-3xl font-bold text-blue-600 mt-1">{{ averageLevel }}</p></div>
          <div class="w-12 h-12 bg-blue-50 rounded-xl flex items-center justify-center"><svg class="w-6 h-6 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6"></path></svg></div>
        </div>
      </div>
      <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-5">
        <div class="flex items-center justify-between">
          <div><p class="text-sm font-medium text-gray-500">Formations en Retard</p><p class="text-3xl font-bold text-red-600 mt-1">{{ delayedCount }}</p></div>
          <div class="w-12 h-12 bg-red-50 rounded-xl flex items-center justify-center"><svg class="w-6 h-6 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg></div>
        </div>
      </div>
      <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-5">
        <div class="flex items-center justify-between">
          <div><p class="text-sm font-medium text-gray-500">Postes Operationnels</p><p class="text-3xl font-bold text-purple-600 mt-1">{{ stats.totalWorkstations ?? 0 }}</p></div>
          <div class="w-12 h-12 bg-purple-50 rounded-xl flex items-center justify-center"><svg class="w-6 h-6 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4"></path></svg></div>
        </div>
      </div>
    </div>

    <!-- Workstation Quality Overview -->
    <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
      <div class="flex items-center justify-between mb-4">
        <h2 class="text-lg font-semibold text-gray-900">Qualite par Poste de Travail</h2>
        <router-link to="/structure" class="text-sm text-emerald-600 hover:underline">Voir la structure</router-link>
      </div>
      <div v-if="loading" class="flex items-center justify-center py-12"><div class="w-8 h-8 border-4 border-emerald-200 border-t-emerald-600 rounded-full animate-spin"></div></div>
      <div v-else-if="workstationQuality.length > 0" class="overflow-x-auto">
        <table class="w-full text-sm">
          <thead class="bg-gray-50"><tr><th class="text-left py-3 px-4 font-medium text-gray-500">Poste</th><th class="text-left py-3 px-4 font-medium text-gray-500">Zone</th><th class="text-left py-3 px-4 font-medium text-gray-500">Niveau Cible</th><th class="text-left py-3 px-4 font-medium text-gray-500">Certifies</th><th class="text-left py-3 px-4 font-medium text-gray-500">En Cours</th><th class="text-left py-3 px-4 font-medium text-gray-500">Conformite</th></tr></thead>
          <tbody>
            <tr v-for="ws in workstationQuality" :key="ws.name" class="border-b border-gray-50 hover:bg-gray-50">
              <td class="py-3 px-4 font-medium">{{ ws.name }}</td>
              <td class="py-3 px-4 text-gray-500">{{ ws.zone || '-' }}</td>
              <td class="py-3 px-4">{{ ws.targetLevel || '-' }}</td>
              <td class="py-3 px-4"><span class="text-emerald-600 font-medium">{{ ws.certified }}</span></td>
              <td class="py-3 px-4"><span class="text-amber-600 font-medium">{{ ws.inProgress }}</span></td>
              <td class="py-3 px-4">
                <div class="flex items-center gap-2">
                  <div class="w-16 bg-gray-100 rounded-full h-2"><div class="h-2 rounded-full" :class="ws.rate >= 70 ? 'bg-emerald-500' : ws.rate >= 40 ? 'bg-amber-500' : 'bg-red-500'" :style="{ width: ws.rate + '%' }"></div></div>
                  <span class="text-xs font-medium">{{ ws.rate }}%</span>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div v-else class="text-center py-12 text-gray-400">Aucune donnee disponible</div>
    </div>

    <!-- Formation Status Distribution & Critical Items -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
        <h2 class="text-lg font-semibold text-gray-900 mb-4">Repartition Globale</h2>
        <div v-if="loading" class="flex items-center justify-center py-12"><div class="w-8 h-8 border-4 border-emerald-200 border-t-emerald-600 rounded-full animate-spin"></div></div>
        <div v-else class="space-y-4">
          <div class="flex items-center justify-center py-4">
            <div class="relative w-40 h-40">
              <svg viewBox="0 0 36 36" class="w-full h-full -rotate-90">
                <circle cx="18" cy="18" r="15.915" fill="none" stroke="#e5e7eb" stroke-width="3"></circle>
                <circle cx="18" cy="18" r="15.915" fill="none" stroke="#f59e0b" stroke-width="3" :stroke-dasharray="inProgressPercent + ' ' + (100 - inProgressPercent)" stroke-dashoffset="0"></circle>
                <circle cx="18" cy="18" r="15.915" fill="none" stroke="#10b981" stroke-width="3" :stroke-dasharray="completedPercent + ' ' + (100 - completedPercent)" :stroke-dashoffset="0 - inProgressPercent"></circle>
              </svg>
              <div class="absolute inset-0 flex items-center justify-center"><span class="text-2xl font-bold text-gray-900">{{ totalFormations }}</span></div>
            </div>
          </div>
          <div class="flex justify-center gap-6">
            <div class="flex items-center gap-2"><div class="w-3 h-3 rounded-full bg-amber-500"></div><span class="text-sm text-gray-600">En Cours ({{ inProgressCount }})</span></div>
            <div class="flex items-center gap-2"><div class="w-3 h-3 rounded-full bg-emerald-500"></div><span class="text-sm text-gray-600">Terminees ({{ completedCount }})</span></div>
            <div class="flex items-center gap-2"><div class="w-3 h-3 rounded-full bg-gray-300"></div><span class="text-sm text-gray-600">Planifiees ({{ plannedCount }})</span></div>
          </div>
        </div>
      </div>

      <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
        <h2 class="text-lg font-semibold text-gray-900 mb-4">Postes a Risque Qualite</h2>
        <div v-if="loading" class="flex items-center justify-center py-12"><div class="w-8 h-8 border-4 border-emerald-200 border-t-emerald-600 rounded-full animate-spin"></div></div>
        <div v-else-if="riskWorkstations.length > 0" class="space-y-2 max-h-80 overflow-y-auto">
          <div v-for="ws in riskWorkstations" :key="ws.name" class="p-3 rounded-lg border" :class="ws.rate < 30 ? 'border-red-200 bg-red-50' : 'border-amber-200 bg-amber-50'">
            <div class="flex items-center justify-between"><span class="text-sm font-medium text-gray-900">{{ ws.name }}</span><span class="text-xs font-bold" :class="ws.rate < 30 ? 'text-red-600' : 'text-amber-600'">{{ ws.rate }}% conformite</span></div>
            <p class="text-xs text-gray-500 mt-1">{{ ws.certified }} certifies / {{ ws.total }} total</p>
          </div>
        </div>
        <div v-else class="text-center py-12 text-gray-400">Tous les postes sont conformes</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { trainingApi, structureApi } from '@/api/endpoints.js'

const loading = ref(true)
const formations = ref([])
const workstations = ref([])
const stats = ref({})

const currentDate = computed(() => new Date().toLocaleDateString('fr-FR', { day: 'numeric', month: 'long', year: 'numeric' }))
const activeFormations = computed(() => formations.value.filter(f => f.status === 'IN_PROGRESS'))
const completedFormations = computed(() => formations.value.filter(f => f.status === 'COMPLETED'))
const plannedFormations = computed(() => formations.value.filter(f => f.status === 'PLANNED'))
const inProgressCount = computed(() => activeFormations.value.length)
const completedCount = computed(() => completedFormations.value.length)
const plannedCount = computed(() => plannedFormations.value.length)
const totalFormations = computed(() => formations.value.length)

const inProgressPercent = computed(() => totalFormations.value > 0 ? Math.round((inProgressCount.value / totalFormations.value) * 100) : 0)
const completedPercent = computed(() => totalFormations.value > 0 ? Math.round((completedCount.value / totalFormations.value) * 100) : 0)

const certificationRate = computed(() => {
  const total = stats.value.totalOperators || 0
  if (total === 0) return 0
  return Math.round(((stats.value.operatorsCertified || 0) / total) * 100)
})

const averageLevel = computed(() => {
  const active = activeFormations.value
  if (active.length === 0) return '0'
  const sum = active.reduce((s, f) => s + (f.achievedLevel || 0), 0)
  return (sum / active.length).toFixed(1)
})

const delayedCount = computed(() => activeFormations.value.filter(f => (f.achievedLevel || 0) === 0).length)

const workstationQuality = computed(() => {
  return workstations.value.map(ws => {
    const wsFormations = formations.value.filter(f => f.workstationId === ws.id)
    const certified = wsFormations.filter(f => f.status === 'COMPLETED').length
    const inProgress = wsFormations.filter(f => f.status === 'IN_PROGRESS').length
    const total = wsFormations.length
    const rate = total > 0 ? Math.round((certified / total) * 100) : 0
    return { name: ws.name, zone: ws.zoneName, targetLevel: ws.targetIluLevel, certified, inProgress, total, rate }
  })
})

const riskWorkstations = computed(() => workstationQuality.value.filter(ws => ws.rate < 50 && ws.total > 0).sort((a, b) => a.rate - b.rate))

onMounted(async () => {
  loading.value = true
  try {
    const [f, s, w] = await Promise.all([trainingApi.getFormations(), trainingApi.getStatistics(), structureApi.getWorkstations()])
    formations.value = f.data
    stats.value = s.data
    workstations.value = w.data
  } catch (e) { console.error(e) } finally { loading.value = false }
})
</script>