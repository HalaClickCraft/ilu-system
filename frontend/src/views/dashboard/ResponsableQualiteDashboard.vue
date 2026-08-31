<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">Tableau de bord Responsable Qualité</h1>
        <p class="text-gray-500 mt-1">Pilotage de la qualité et metriques de performance ILU</p>
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
      <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 mb-4">
        <h2 class="text-lg font-semibold text-gray-900">Qualité par Poste de Travail</h2>
        <div class="flex flex-wrap items-center gap-2">
          <input v-model="wsSearch" type="text" placeholder="Rechercher un poste..." class="px-3 py-1.5 border border-gray-200 rounded-lg text-xs outline-none focus:ring-2 focus:ring-emerald-500 w-44" />
          <select v-model="wsProjectFilter" class="px-3 py-1.5 border border-gray-200 rounded-lg text-xs outline-none focus:ring-2 focus:ring-emerald-500">
            <option value="">Tous les projets</option>
            <option v-for="p in projectList" :key="p.id" :value="p.id">{{ p.name }}</option>
          </select>
          <select v-model="wsPageSize" class="px-2 py-1.5 border border-gray-200 rounded-lg text-xs outline-none focus:ring-2 focus:ring-emerald-500">
            <option :value="10">10 / page</option>
            <option :value="20">20 / page</option>
            <option :value="50">50 / page</option>
          </select>
          <router-link to="/structure" class="text-xs bg-gray-50 border border-gray-200 text-gray-600 px-3 py-1.5 rounded-lg hover:bg-gray-100 font-medium">Voir la structure</router-link>
        </div>
      </div>
      <div v-if="loading" class="flex items-center justify-center py-12"><div class="w-8 h-8 border-4 border-emerald-200 border-t-emerald-600 rounded-full animate-spin"></div></div>
      <div v-else-if="filteredWorkstationQuality.length > 0" class="space-y-4">
        <div class="overflow-x-auto">
          <table class="w-full text-sm">
            <thead class="bg-gray-50 sticky top-0 z-10"><tr><th class="text-left py-2.5 px-4 font-medium text-gray-500 text-xs">Poste</th><th class="text-left py-2.5 px-4 font-medium text-gray-500 text-xs">Zone</th><th class="text-left py-2.5 px-4 font-medium text-gray-500 text-xs">Niveau Cible</th><th class="text-left py-2.5 px-4 font-medium text-gray-500 text-xs">Certifiés</th><th class="text-left py-2.5 px-4 font-medium text-gray-500 text-xs">En Cours</th><th class="text-left py-2.5 px-4 font-medium text-gray-500 text-xs">Conformité</th></tr></thead>
            <tbody>
              <tr v-for="ws in paginatedWorkstationQuality" :key="ws.name" class="border-b border-gray-50 hover:bg-gray-50">
                <td class="py-2.5 px-4 font-medium">{{ ws.name }}</td>
                <td class="py-2.5 px-4 text-gray-500 text-xs">{{ ws.zone || '-' }}</td>
                <td class="py-2.5 px-4 text-xs font-semibold">{{ ws.targetLevel || '-' }}</td>
                <td class="py-2.5 px-4"><span class="text-emerald-600 font-semibold text-sm">{{ ws.certified }}</span></td>
                <td class="py-2.5 px-4"><span class="text-amber-600 font-semibold text-sm">{{ ws.inProgress }}</span></td>
                <td class="py-2.5 px-4">
                  <div class="flex items-center gap-2">
                    <div class="w-16 bg-gray-100 rounded-full h-2"><div class="h-2 rounded-full" :class="ws.rate >= 70 ? 'bg-emerald-500' : ws.rate >= 40 ? 'bg-amber-500' : 'bg-red-500'" :style="{ width: ws.rate + '%' }"></div></div>
                    <span class="text-xs font-semibold">{{ ws.rate }}%</span>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Pagination Footer -->
        <div v-if="wsTotalPages > 1" class="flex justify-between items-center text-xs text-gray-500 font-medium pt-3 border-t">
          <span>Affichage de {{ (wsCurrentPage - 1) * wsPageSize + 1 }} à {{ Math.min(wsCurrentPage * wsPageSize, filteredWorkstationQuality.length) }} sur {{ filteredWorkstationQuality.length }} poste(s)</span>
          <div class="flex gap-1">
            <button :disabled="wsCurrentPage === 1" @click="wsCurrentPage--" class="px-2 py-1 bg-white border border-gray-200 rounded hover:bg-gray-50 disabled:opacity-50 font-semibold">Précédent</button>
            <span class="px-3 py-1 bg-gray-100 rounded flex items-center font-semibold">Page {{ wsCurrentPage }} / {{ wsTotalPages }}</span>
            <button :disabled="wsCurrentPage === wsTotalPages" @click="wsCurrentPage++" class="px-2 py-1 bg-white border border-gray-200 rounded hover:bg-gray-50 disabled:opacity-50 font-semibold">Suivant</button>
          </div>
        </div>
      </div>
      <div v-else class="text-center py-12 text-gray-400">Aucune donnée disponible</div>
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
        <h2 class="text-lg font-semibold text-gray-900 mb-4">Postes a Risque Qualité</h2>
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
import { ref, computed, onMounted, watch } from 'vue'
import { trainingApi, structureApi, operatorsApi } from '@/api/endpoints.js'
import { useUserScope } from '@/composables/useUserScope'

const { loadUserProjects, filterFormations, filterWorkstations, filterOperators, filterProjects } = useUserScope()

const loading = ref(true)
const rawFormations = ref([])
const rawWorkstations = ref([])
const allOperators = ref([])
const rawProjects = ref([])
const stats = ref({})

const wsSearch = ref('')
const wsProjectFilter = ref('')
const wsCurrentPage = ref(1)
const wsPageSize = ref(10)

const formations = computed(() => filterFormations(rawFormations.value, allOperators.value))
const workstations = computed(() => filterWorkstations(rawWorkstations.value))
const projectList = computed(() => filterProjects(rawProjects.value))

const workstationProjectMap = computed(() => {
  const map = {}
  for (const p of rawProjects.value) {
    for (const z of (p.zones || [])) {
      for (const w of (z.workstations || [])) {
        map[w.id] = { projectId: p.id, projectName: p.name }
      }
    }
  }
  return map
})

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
  const scopedOps = filterOperators(allOperators.value)
  const total = scopedOps.length
  if (total === 0) return 0
  const certified = completedFormations.value.length
  return Math.round((certified / total) * 100)
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
    const projInfo = workstationProjectMap.value[ws.id] || {}
    return { name: ws.name, zone: ws.zoneName, targetLevel: ws.targetIluLevel, certified, inProgress, total, rate, projectId: projInfo.projectId }
  })
})

const filteredWorkstationQuality = computed(() => {
  return workstationQuality.value.filter(ws => {
    const matchesSearch = ws.name.toLowerCase().includes(wsSearch.value.toLowerCase())
    const matchesProject = !wsProjectFilter.value || ws.projectId === Number(wsProjectFilter.value)
    return matchesSearch && matchesProject
  })
})

const paginatedWorkstationQuality = computed(() => {
  const start = (wsCurrentPage.value - 1) * wsPageSize.value
  const end = start + wsPageSize.value
  return filteredWorkstationQuality.value.slice(start, end)
})

const wsTotalPages = computed(() => {
  return Math.ceil(filteredWorkstationQuality.value.length / wsPageSize.value) || 1
})

watch([wsSearch, wsProjectFilter, wsPageSize], () => {
  wsCurrentPage.value = 1
})

const riskWorkstations = computed(() => workstationQuality.value.filter(ws => ws.rate < 50 && ws.total > 0).sort((a, b) => a.rate - b.rate))

onMounted(async () => {
  loading.value = true
  try {
    await loadUserProjects()
    const [f, s, w, o, p] = await Promise.all([
      trainingApi.getFormations(),
      trainingApi.getStatistics(),
      structureApi.getWorkstations(),
      operatorsApi.getAll(),
      structureApi.getAll()
    ])
    rawFormations.value = f.data
    stats.value = s.data
    rawWorkstations.value = w.data
    allOperators.value = o.data
    rawProjects.value = p.data
  } catch (e) { console.error(e) } finally { loading.value = false }
})
</script>