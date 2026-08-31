<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">Tableau de bord Agent Qualité</h1>
        <p class="text-gray-500 mt-1">Controle qualité et suivi des niveaux ILU</p>
      </div>
      <div class="text-sm text-gray-400">{{ currentDate }}</div>
    </div>

    <!-- Quality KPIs -->
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
      <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-5">
        <div class="flex items-center justify-between">
          <div><p class="text-sm font-medium text-gray-500">Formations en Cours</p><p class="text-3xl font-bold text-amber-600 mt-1">{{ inProgressCount }}</p></div>
          <div class="w-12 h-12 bg-amber-50 rounded-xl flex items-center justify-center"><svg class="w-6 h-6 text-amber-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253"></path></svg></div>
        </div>
      </div>
      <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-5">
        <div class="flex items-center justify-between">
          <div><p class="text-sm font-medium text-gray-500">Niveau Moyen Atteint</p><p class="text-3xl font-bold text-blue-600 mt-1">{{ averageLevel }}</p></div>
          <div class="w-12 h-12 bg-blue-50 rounded-xl flex items-center justify-center"><svg class="w-6 h-6 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"></path></svg></div>
        </div>
      </div>
      <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-5">
        <div class="flex items-center justify-between">
          <div><p class="text-sm font-medium text-gray-500">Non Conformes</p><p class="text-3xl font-bold text-red-600 mt-1">{{ nonConformingCount }}</p></div>
          <div class="w-12 h-12 bg-red-50 rounded-xl flex items-center justify-center"><svg class="w-6 h-6 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4.5c-.77-.833-2.694-.833-3.464 0L3.34 16.5c-.77.833.192 2.5 1.732 2.5z"></path></svg></div>
        </div>
      </div>
      <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-5">
        <div class="flex items-center justify-between">
          <div><p class="text-sm font-medium text-gray-500">Certifies</p><p class="text-3xl font-bold text-emerald-600 mt-1">{{ completedCount }}</p></div>
          <div class="w-12 h-12 bg-emerald-50 rounded-xl flex items-center justify-center"><svg class="w-6 h-6 text-emerald-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg></div>
        </div>
      </div>
    </div>

    <!-- Level Distribution & Workstation Quality -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
        <h2 class="text-lg font-semibold text-gray-900 mb-4">Repartition par Niveau ILU</h2>
        <div v-if="loading" class="flex items-center justify-center py-12"><div class="w-8 h-8 border-4 border-emerald-200 border-t-emerald-600 rounded-full animate-spin"></div></div>
        <div v-else class="space-y-3">
          <div v-for="level in levelDistribution" :key="level.level" class="flex items-center gap-3">
            <div class="w-16 text-sm font-medium text-gray-700">Niveau {{ level.level }}</div>
            <div class="flex-1 bg-gray-100 rounded-full h-3"><div class="h-3 rounded-full transition-all duration-500" :class="level.color" :style="{ width: level.percent + '%' }"></div></div>
            <span class="text-sm text-gray-600 w-8 text-right">{{ level.count }}</span>
          </div>
        </div>
      </div>

      <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
        <h2 class="text-lg font-semibold text-gray-900 mb-4">Formations Necessitant une Attention</h2>
        <div v-if="loading" class="flex items-center justify-center py-12"><div class="w-8 h-8 border-4 border-emerald-200 border-t-emerald-600 rounded-full animate-spin"></div></div>
        <div v-else-if="attentionFormations.length > 0" class="space-y-2 max-h-80 overflow-y-auto">
          <div v-for="f in attentionFormations" :key="f.id" class="flex items-center justify-between p-3 rounded-lg bg-red-50 border border-red-100">
            <div><p class="text-sm font-medium text-gray-900">{{ f.operatorName }}</p><p class="text-xs text-gray-500">{{ f.workstationName }} - Cible: {{ mapLevelToSymbol(f.targetLevel) }}</p></div>
            <router-link :to="'/training/' + f.id" class="text-sm text-red-600 hover:underline font-medium">Controle</router-link>
          </div>
        </div>
        <div class="text-center py-12 text-gray-400" v-else>Aucune formation critique</div>
      </div>
    </div>

    <!-- Active Formations Table -->
    <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
      <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 mb-4">
        <h2 class="text-lg font-semibold text-gray-900">Formations Actives - Contrôle Qualité</h2>
        <div class="flex items-center gap-2">
          <input v-model="opSearch" type="text" placeholder="Rechercher..." class="px-3 py-1.5 border border-gray-200 rounded-lg text-xs outline-none focus:ring-2 focus:ring-emerald-500 w-44" />
          <select v-model="activePageSize" class="px-2 py-1.5 border border-gray-200 rounded-lg text-xs outline-none focus:ring-2 focus:ring-emerald-500">
            <option :value="5">5 / page</option>
            <option :value="10">10 / page</option>
            <option :value="20">20 / page</option>
          </select>
          <router-link to="/training" class="text-xs bg-gray-50 border border-gray-200 text-gray-600 px-3 py-1.5 rounded-lg hover:bg-gray-100 font-medium whitespace-nowrap">Toutes les formations</router-link>
        </div>
      </div>
      <div v-if="loading" class="flex items-center justify-center py-8"><div class="w-8 h-8 border-4 border-emerald-200 border-t-emerald-600 rounded-full animate-spin"></div></div>
      <div v-else-if="filteredActiveFormations.length > 0" class="space-y-4">
        <div class="overflow-x-auto">
          <table class="w-full text-sm">
            <thead class="bg-gray-50 sticky top-0 z-10"><tr><th class="text-left py-2.5 px-4 font-medium text-gray-500 text-xs">Opérateur</th><th class="text-left py-2.5 px-4 font-medium text-gray-500 text-xs">Poste</th><th class="text-left py-2.5 px-4 font-medium text-gray-500 text-xs">Atteint</th><th class="text-left py-2.5 px-4 font-medium text-gray-500 text-xs">Cible</th><th class="text-left py-2.5 px-4 font-medium text-gray-500 text-xs">Écart</th><th class="text-right py-2.5 px-4 font-medium text-gray-500 text-xs">Action</th></tr></thead>
            <tbody>
              <tr v-for="f in paginatedActiveFormations" :key="f.id" class="border-b border-gray-50 hover:bg-gray-50">
                <td class="py-2.5 px-4 font-medium text-sm">{{ f.operatorName }}</td>
                <td class="py-2.5 px-4 text-gray-500 text-xs">{{ f.workstationName }}</td>
                <td class="py-2.5 px-4 font-semibold text-sm">{{ mapLevelToSymbol(f.achievedLevel ?? 0) }}</td>
                <td class="py-2.5 px-4 text-xs font-medium">{{ mapLevelToSymbol(f.targetLevel) }}</td>
                <td class="py-2.5 px-4"><span class="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-semibold" :class="gapClass(f)">{{ gapLabel(f) }}</span></td>
                <td class="py-2.5 px-4 text-right"><router-link :to="'/training/' + f.id" class="text-emerald-600 hover:underline text-sm font-semibold">Détails</router-link></td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Pagination Controls -->
        <div v-if="activeTotalPages > 1" class="flex justify-between items-center text-xs text-gray-500 font-medium pt-3 border-t">
          <span>Affichage de {{ (activeCurrentPage - 1) * activePageSize + 1 }} à {{ Math.min(activeCurrentPage * activePageSize, filteredActiveFormations.length) }} sur {{ filteredActiveFormations.length }} formation(s)</span>
          <div class="flex gap-1">
            <button :disabled="activeCurrentPage === 1" @click="activeCurrentPage--" class="px-2 py-1 bg-white border border-gray-200 rounded hover:bg-gray-50 disabled:opacity-50 font-semibold">Précédent</button>
            <span class="px-3 py-1 bg-gray-100 rounded flex items-center font-semibold">Page {{ activeCurrentPage }} / {{ activeTotalPages }}</span>
            <button :disabled="activeCurrentPage === activeTotalPages" @click="activeCurrentPage++" class="px-2 py-1 bg-white border border-gray-200 rounded hover:bg-gray-50 disabled:opacity-50 font-semibold">Suivant</button>
          </div>
        </div>
      </div>
      <div v-else class="text-center py-8 text-gray-400">Aucune formation active</div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { trainingApi, operatorsApi } from '@/api/endpoints'
import { useUserScope } from '@/composables/useUserScope'

const { loadUserProjects, filterFormations } = useUserScope()

const loading = ref(true)
const rawFormations = ref([])
const operators = ref([])
const stats = ref({})

const opSearch = ref('')
const activeCurrentPage = ref(1)
const activePageSize = ref(10)

const formations = computed(() => filterFormations(rawFormations.value, operators.value))

const currentDate = computed(() => new Date().toLocaleDateString('fr-FR', { day: 'numeric', month: 'long', year: 'numeric' }))
const activeFormations = computed(() => formations.value.filter(f => f.status === 'IN_PROGRESS'))
const filteredActiveFormations = computed(() => {
  return activeFormations.value.filter(f =>
    (f.operatorName || '').toLowerCase().includes(opSearch.value.toLowerCase()) ||
    (f.workstationName || '').toLowerCase().includes(opSearch.value.toLowerCase())
  )
})

const paginatedActiveFormations = computed(() => {
  const start = (activeCurrentPage.value - 1) * activePageSize.value
  const end = start + activePageSize.value
  return filteredActiveFormations.value.slice(start, end)
})

const activeTotalPages = computed(() => {
  return Math.ceil(filteredActiveFormations.value.length / activePageSize.value) || 1
})

watch([opSearch, activePageSize], () => {
  activeCurrentPage.value = 1
})
const completedFormations = computed(() => formations.value.filter(f => f.status === 'COMPLETED'))
const inProgressCount = computed(() => activeFormations.value.length)
const completedCount = computed(() => completedFormations.value.length)

const mapLevelToSymbol = (val) => {
  if (!val) return 'I'
  if (val === 'U' || val === 3 || val === '3') return 'U'
  if (val === 'L' || val === 2 || val === '2') return 'L'
  return 'I'
}

const averageLevel = computed(() => {
  const active = activeFormations.value
  if (active.length === 0) return 'L'
  const uCount = active.filter(f => mapLevelToSymbol(f.achievedLevel || f.niveau) === 'U').length
  const lCount = active.filter(f => mapLevelToSymbol(f.achievedLevel || f.niveau) === 'L').length
  if (uCount >= lCount) return 'U'
  if (lCount > 0) return 'L'
  return 'I'
})

const nonConformingCount = computed(() => {
  return activeFormations.value.filter(f => {
    const lvl = mapLevelToSymbol(f.achievedLevel || f.niveau)
    return lvl === 'I'
  }).length
})

const attentionFormations = computed(() => {
  return activeFormations.value.filter(f => {
    const lvl = mapLevelToSymbol(f.achievedLevel || f.niveau)
    return lvl === 'I'
  }).slice(0, 5)
})

const levelDistribution = computed(() => {
  const levels = ['I', 'L', 'U']
  const colors = ['bg-amber-400', 'bg-blue-500', 'bg-emerald-600']
  return levels.map((level, i) => {
    const count = formations.value.filter(f => mapLevelToSymbol(f.achievedLevel || f.niveau) === level).length
    return { level, count, percent: formations.value.length > 0 ? Math.round((count / formations.value.length) * 100) : 0, color: colors[i] }
  })
})

const gapLabel = (f) => { const gap = (f.targetLevel || 5) - (f.achievedLevel || 0); return gap <= 1 ? 'OK' : gap <= 2 ? 'Moyen' : 'Critique' }
const gapClass = (f) => { const gap = (f.targetLevel || 5) - (f.achievedLevel || 0); return gap <= 1 ? 'bg-emerald-100 text-emerald-700' : gap <= 2 ? 'bg-amber-100 text-amber-700' : 'bg-red-100 text-red-700' }

onMounted(async () => {
  loading.value = true
  try {
    await loadUserProjects()
    const [f, s, o] = await Promise.all([trainingApi.getFormations(), trainingApi.getStatistics(), operatorsApi.getAll()])
    rawFormations.value = f.data
    stats.value = s.data
    operators.value = o.data
  } catch (e) { console.error(e) } finally { loading.value = false }
})
</script>