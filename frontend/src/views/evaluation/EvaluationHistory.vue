<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">Historique des Evaluations</h1>
        <p class="text-sm text-gray-500 mt-1">Toutes les sessions d'evaluation passees et en cours</p>
      </div>
      <router-link to="/evaluation/matrix" class="inline-flex items-center gap-1.5 text-sm text-blue-600 hover:text-blue-800 font-medium">
        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" /></svg>
        Voir la Matrice de Polyvalence
      </router-link>
    </div>

    <div v-if="historyLoading" class="text-center py-12 text-gray-400">Chargement de l'historique...</div>
    <template v-else>
      <!-- Stats cards -->
      <div class="grid grid-cols-2 md:grid-cols-5 gap-3">
        <div class="bg-white rounded-lg border p-3 text-center"><p class="text-2xl font-bold text-gray-900">{{ scopedHistory.length }}</p><p class="text-xs text-gray-500">Total</p></div>
        <div class="bg-green-50 rounded-lg border border-green-200 p-3 text-center"><p class="text-2xl font-bold text-green-600">{{ scopedHistory.filter(h => h.status === 'PASSED').length }}</p><p class="text-xs text-green-700">Reussi</p></div>
        <div class="bg-red-50 rounded-lg border border-red-200 p-3 text-center"><p class="text-2xl font-bold text-red-600">{{ scopedHistory.filter(h => h.status === 'FAILED').length }}</p><p class="text-xs text-red-700">Echoue</p></div>
        <div class="bg-orange-50 rounded-lg border border-orange-200 p-3 text-center"><p class="text-2xl font-bold text-orange-600">{{ scopedHistory.filter(h => h.status === 'BLOCKED').length }}</p><p class="text-xs text-orange-700">Bloque</p></div>
        <div class="bg-purple-50 rounded-lg border border-purple-200 p-3 text-center"><p class="text-2xl font-bold text-purple-600">{{ scopedHistory.filter(h => h.isSecondChance).length }}</p><p class="text-xs text-purple-700">2eme chance</p></div>
      </div>

      <!-- Waiting for production alert -->
      <div v-if="historyData.waitingForProduction?.length" class="bg-amber-50 border border-amber-200 rounded-xl p-4">
        <div class="flex items-center gap-2 mb-2">
          <svg class="w-5 h-5 text-amber-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4.5c-.77-.833-2.694-.833-3.464 0L3.34 16.5c-.77.833.192 2.5 1.732 2.5z" /></svg>
          <h3 class="font-bold text-amber-800">En attente de Production ({{ historyData.waitingForProduction.length }})</h3>
        </div>
        <p class="text-xs text-amber-600 mb-2">Ces operateurs ont reussi la partie Generique mais n'ont pas encore passe la partie Production.</p>
        <div class="flex flex-wrap gap-2">
          <span v-for="w in historyData.waitingForProduction" :key="w.operatorId" class="inline-flex items-center gap-1 px-3 py-1.5 bg-amber-100 border border-amber-300 rounded-full text-xs font-medium text-amber-800">
            {{ w.operatorName }} <span class="text-amber-500">({{ w.employeeId }})</span>
            <span v-if="w.genericPassedDate" class="text-amber-500">- Generic OK le {{ formatDate(w.genericPassedDate) }}</span>
          </span>
        </div>
      </div>

      <!-- Filters -->
      <div class="bg-white rounded-xl border p-4">
        <div class="flex flex-wrap items-center gap-3">
          <div class="flex-1 min-w-[200px]">
            <input v-model="historySearch" type="text" placeholder="Rechercher par nom ou matricule..." class="w-full px-3 py-2 border rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
          </div>
          <select v-model="historyFilterType" class="px-3 py-2 border rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500">
            <option value="">Tous les types</option>
            <option value="GENERIC_COMMON">Generique</option>
            <option value="POSTE_PRODUCTION">Production</option>
          </select>
          <select v-model="historyFilterStatus" class="px-3 py-2 border rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500">
            <option value="">Tous les statuts</option>
            <option value="PASSED">Reussi</option>
            <option value="FAILED">Echoue</option>
            <option value="BLOCKED">Bloque</option>
          </select>
          <label class="flex items-center gap-2 text-sm text-gray-600 cursor-pointer">
            <input v-model="historyFilterSecondChance" type="checkbox" class="rounded" />
            2eme chance uniquement
          </label>
        </div>
      </div>

      <!-- History table -->
      <div class="bg-white rounded-xl border overflow-x-auto">
        <table class="w-full text-sm">
          <thead class="bg-gray-50">
            <tr>
              <th class="px-3 py-3 text-left text-xs font-semibold text-gray-500 uppercase">Date</th>
              <th class="px-3 py-3 text-left text-xs font-semibold text-gray-500 uppercase">Operateur</th>
              <th class="px-3 py-3 text-left text-xs font-semibold text-gray-500 uppercase">Type</th>
              <th class="px-3 py-3 text-left text-xs font-semibold text-gray-500 uppercase">Poste</th>
              <th class="px-3 py-3 text-center text-xs font-semibold text-gray-500 uppercase">Statut</th>
              <th class="px-3 py-3 text-center text-xs font-semibold text-gray-500 uppercase">Score</th>
              <th class="px-3 py-3 text-center text-xs font-semibold text-gray-500 uppercase">Niveau</th>
              <th class="px-3 py-3 text-center text-xs font-semibold text-gray-500 uppercase">Tentative</th>
              <th class="px-3 py-3 text-left text-xs font-semibold text-gray-500 uppercase">Evaluateur</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-200">
            <tr v-for="h in paginatedHistory" :key="h.sessionId" class="hover:bg-blue-50/40 cursor-pointer transition-colors" @click="$router.push({ name: 'evaluation-session', params: { id: h.sessionId } })">
              <td class="px-3 py-3 text-xs text-gray-600 whitespace-nowrap">{{ formatDate(h.completedAt || h.createdAt) }}</td>
              <td class="px-3 py-3"><p class="font-medium text-gray-900 text-sm">{{ h.operatorName }}</p><p class="text-xs text-gray-400">{{ h.employeeId }}</p></td>
              <td class="px-3 py-3">
                <span :class="h.templateType === 'GENERIC_COMMON' ? 'bg-indigo-50 text-indigo-700 border-indigo-200' : 'bg-blue-50 text-blue-700 border-blue-200'" class="inline-flex px-2 py-1 rounded text-xs font-medium border">{{ h.templateType === 'GENERIC_COMMON' ? 'Generique' : 'Production' }}</span>
              </td>
              <td class="px-3 py-3 text-sm text-gray-700">{{ h.workstationName || '-' }}</td>
              <td class="px-3 py-3 text-center">
                <span :class="statusClass(h.status)" class="inline-flex px-2.5 py-1 rounded-full text-xs font-bold">{{ statusLabel(h.status) }}</span>
                <span v-if="h.isSecondChance" class="ml-1 inline-flex px-1.5 py-0.5 rounded text-[10px] font-bold bg-purple-100 text-purple-700 border border-purple-300">2CH</span>
              </td>
              <td class="px-3 py-3 text-center">
                <div class="text-sm font-semibold" :class="scoreColor(h.scorePercentage)">{{ h.scorePercentage }}%</div>
                <div v-if="h.templateType === 'GENERIC_COMMON' && h.genericPercentage > 0" class="text-[10px] text-gray-400">Gen: {{ h.genericPercentage }}%</div>
                <div v-if="h.templateType === 'POSTE_PRODUCTION' && h.productionPercentage > 0" class="text-[10px] text-gray-400">Prod: {{ h.productionPercentage }}%</div>
              </td>
              <td class="px-3 py-3 text-center">
                <span v-if="h.niveau && h.niveau !== '-' && h.niveau !== 'NON_APTE' && h.niveau !== 'NON_VALIDE'" :class="niveauTextClass(h.niveau)" class="inline-block w-8 h-8 leading-8 rounded-lg text-white font-bold text-sm">{{ h.niveau }}</span>
                <span v-else-if="h.niveau === 'NON_APTE'" class="text-xs text-red-500 font-medium">NON APTE</span>
                <span v-else-if="h.niveau === 'NON_VALIDE'" class="text-xs text-red-500 font-medium">NON VALIDE</span>
                <span v-else class="text-gray-300">-</span>
              </td>
              <td class="px-3 py-3 text-center">
                <span :class="h.isSecondChance ? 'bg-purple-100 text-purple-700 font-bold' : 'text-gray-500'" class="inline-flex items-center justify-center w-7 h-7 rounded-full text-xs">#{{ h.attemptNumber }}</span>
              </td>
              <td class="px-3 py-3 text-xs text-gray-500">{{ h.evaluatorName || '-' }}</td>
            </tr>
            <tr v-if="!filteredHistory.length"><td colspan="9" class="px-4 py-8 text-center text-gray-400">Aucun historique trouve</td></tr>
          </tbody>
        </table>
      </div>

      <!-- Pagination Footer -->
      <div v-if="totalPages > 1" class="px-6 py-3.5 bg-gray-50 border border-gray-200 border-t-0 rounded-b-xl flex flex-col sm:flex-row justify-between items-center gap-3 text-xs text-gray-500 font-medium">
        <span>Affichage de {{ (currentPage - 1) * pageSize + 1 }} à {{ Math.min(currentPage * pageSize, filteredHistory.length) }} sur {{ filteredHistory.length }} évaluation(s)</span>
        <div class="flex gap-1">
          <button :disabled="currentPage === 1" @click="currentPage--" class="px-2.5 py-1.5 bg-white border border-gray-200 rounded-lg hover:bg-gray-50 disabled:opacity-50 font-semibold text-gray-700">Précédent</button>
          <span class="px-3 py-1.5 bg-gray-100 rounded-lg flex items-center font-semibold text-gray-800">Page {{ currentPage }} sur {{ totalPages }}</span>
          <button :disabled="currentPage === totalPages" @click="currentPage++" class="px-2.5 py-1.5 bg-white border border-gray-200 rounded-lg hover:bg-gray-50 disabled:opacity-50 font-semibold text-gray-700">Suivant</button>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { evaluationApi } from '@/api/endpoints'

const historyLoading = ref(true)
const historyData = ref({ history: [], waitingForProduction: [], totalSessions: 0, totalPassed: 0, totalFailed: 0, totalBlocked: 0, totalSecondChance: 0 })
const historySearch = ref('')
const historyFilterType = ref('')
const historyFilterStatus = ref('')
const historyFilterSecondChance = ref(false)

const statusClass = (status) => ({
  PASSED: 'bg-green-100 text-green-800',
  FAILED: 'bg-red-100 text-red-800',
  BLOCKED: 'bg-orange-100 text-orange-800',
  COMPLETED: 'bg-blue-100 text-blue-800'
}[status] || 'bg-gray-100 text-gray-600')

const statusLabel = (status) => ({
  PASSED: 'REUSSI',
  FAILED: 'ECHOUE',
  BLOCKED: 'BLOQUE',
  COMPLETED: 'COMPLETE'
}[status] || status)

const scoreColor = (score) => {
  if (score >= 100) return 'text-green-600'
  if (score >= 80) return 'text-blue-600'
  if (score >= 60) return 'text-amber-600'
  return 'text-red-600'
}

const niveauTextClass = (n) => ({
  I: 'bg-amber-400',
  L: 'bg-blue-500',
  U: 'bg-green-600'
}[n] || 'bg-gray-200 text-gray-500')

function formatDate(dateStr) {
  if (!dateStr) return '-'
  try {
    const d = new Date(dateStr)
    return d.toLocaleDateString('fr-FR', { day: '2-digit', month: '2-digit', year: 'numeric' })
  } catch {
    return dateStr
  }
}

import { useUserScope } from '@/composables/useUserScope'
import { operatorsApi } from '@/api/endpoints'

const { loadUserProjects, filterOperators } = useUserScope()
const allOperatorsData = ref([])

const scopedHistory = computed(() => {
  const scopedOps = filterOperators(allOperatorsData.value)
  const scopedOpIds = new Set(scopedOps.map(o => o.id))
  return (historyData.value.history || []).filter(h => scopedOpIds.has(h.operatorId))
})

const filteredHistory = computed(() => {
  let list = scopedHistory.value
  if (historySearch.value) {
    const q = historySearch.value.toLowerCase()
    list = list.filter(h => h.operatorName?.toLowerCase().includes(q) || h.employeeId?.toLowerCase().includes(q))
  }
  if (historyFilterType.value) list = list.filter(h => h.templateType === historyFilterType.value)
  if (historyFilterStatus.value) list = list.filter(h => h.status === historyFilterStatus.value)
  if (historyFilterSecondChance.value) list = list.filter(h => h.isSecondChance)
  return list
})

const currentPage = ref(1)
const pageSize = ref(15)

const paginatedHistory = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredHistory.value.slice(start, end)
})

const totalPages = computed(() => {
  return Math.ceil(filteredHistory.value.length / pageSize.value) || 1
})

import { watch } from 'vue'
watch([historySearch, historyFilterType, historyFilterStatus, historyFilterSecondChance, pageSize], () => {
  currentPage.value = 1
})

async function loadHistory() {
  historyLoading.value = true
  try {
    const [res, opsRes] = await Promise.all([evaluationApi.getHistory(), operatorsApi.getAll()])
    historyData.value = res.data || { history: [], waitingForProduction: [] }
    allOperatorsData.value = opsRes.data || []
  } catch (e) {
    console.error('Error loading history', e)
  }
  historyLoading.value = false
}

onMounted(async () => {
  await loadUserProjects()
  await loadHistory()
})
</script>
