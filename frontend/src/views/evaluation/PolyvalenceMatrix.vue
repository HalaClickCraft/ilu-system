<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">MATRICE DE POLYVALENCE KJ92</h1>
        <p class="text-sm text-gray-500 mt-1">Indicateur de polyvalence: Minimum 6 personnes formées par poste => 6 personnes en L</p>
      </div>
      <div class="flex items-center gap-3 text-xs">
        <span class="flex items-center gap-1"><span class="w-4 h-4 rounded bg-gray-100 border inline-block"></span> Non formé</span>
        <span class="flex items-center gap-1"><span class="w-4 h-4 rounded bg-amber-400 inline-block text-white"></span> Niveau I</span>
        <span class="flex items-center gap-1"><span class="w-4 h-4 rounded bg-blue-500 inline-block text-white"></span> Niveau L</span>
        <span class="flex items-center gap-1"><span class="w-4 h-4 rounded bg-green-600 inline-block text-white"></span> Niveau U</span>
      </div>
    </div>

    <div v-if="loading" class="text-center py-12 text-gray-400">Chargement de la matrice...</div>
    <div v-else-if="errorMsg" class="bg-red-50 border border-red-200 rounded-xl p-6 text-center">
      <p class="text-red-700 font-semibold">Erreur de chargement</p>
      <p class="text-red-500 text-sm mt-1">{{ errorMsg }}</p>
      <button @click="loadAll" class="mt-3 bg-red-600 text-white px-4 py-2 rounded-lg text-sm hover:bg-red-700">Reessayer</button>
    </div>

    <template v-else>
      <!-- ==================== MATRIX TABLE ==================== -->
      <div class="bg-white rounded-xl border overflow-x-auto shadow-sm">
        <table class="min-w-full border-collapse text-xs text-gray-700">
          <thead>
            <tr class="bg-gray-100 border-b border-gray-200">
              <th rowspan="3" class="px-4 py-3 text-left font-bold text-gray-700 sticky left-0 bg-gray-100 z-20 border-r border-gray-200 min-w-[180px]">Opérateur</th>
              <th rowspan="1" class="px-3 py-2 text-center font-bold text-gray-700 border-r border-gray-200 min-w-[120px] bg-gray-100">Zone</th>
              <th v-for="z in zones" :key="z.name" :colspan="z.workstations.length" class="px-3 py-2 text-center font-bold text-gray-800 border-r border-gray-200 bg-gray-50">{{ z.name }}</th>
              <th rowspan="3" class="px-4 py-3 text-center font-bold text-gray-700 border-l border-gray-200 bg-gray-50 max-w-[150px] whitespace-normal">Nombre des postes sur lesquels est formé un opérateur</th>
            </tr>
            <tr class="bg-gray-100 border-b border-gray-200">
              <th class="px-3 py-2 text-center font-semibold text-gray-600 border-r border-gray-200 bg-gray-100">Poste</th>
              <th v-for="col in allColumns" :key="col.id" class="px-3 py-2 text-center font-semibold text-gray-700 border-r border-gray-200 min-w-[90px]">{{ col.name }}</th>
            </tr>
            <tr class="bg-gray-50 border-b border-gray-300 text-gray-500">
              <th class="px-3 py-1.5 text-center font-medium text-gray-500 border-r border-gray-200 bg-gray-50">Target</th>
              <th v-for="col in allColumns" :key="col.id + '-target'" class="px-3 py-1.5 text-center font-medium border-r border-gray-200">{{ col.targetLevel || 'L' }}</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-200">
            <template v-for="op in matrixData.operators" :key="op.operatorId">
              <tr class="hover:bg-gray-50">
                <td rowspan="2" class="px-4 py-3 sticky left-0 bg-white z-10 font-bold text-gray-900 border-r border-gray-200">
                  {{ op.operatorName }}
                  <div class="text-[10px] text-gray-400 font-normal">{{ op.employeeId }} · {{ op.seniorityMonths }} mois</div>
                </td>
                <td class="px-3 py-2 font-medium text-gray-500 bg-gray-50 border-r border-gray-200 text-left">{{ getOperatorLabelRow1(op) }}</td>
                <td v-for="col in allColumns" :key="col.id + '-date'" class="px-2 py-2 text-center border-r border-gray-150 text-[10px] text-gray-600">{{ getColumnDate(op, col) }}</td>
                <td rowspan="2" class="px-4 py-3 text-center border-l border-gray-200 font-bold text-lg text-slate-800 bg-slate-50">{{ getTrainedCount(op) }}</td>
              </tr>
              <tr class="hover:bg-gray-50 border-b border-gray-300">
                <td class="px-3 py-2 font-medium text-gray-500 bg-gray-50 border-r border-gray-200 text-left">{{ getOperatorLabelRow2(op) }}</td>
                <td v-for="col in allColumns" :key="col.id + '-level'" class="px-2 py-2 text-center border-r border-gray-150">
                  <span :class="niveauBgClass(getColumnLevel(op, col))" class="inline-block w-8 h-8 leading-8 rounded-lg text-white font-bold text-xs shadow-sm">{{ getColumnLevel(op, col) }}</span>
                </td>
              </tr>
            </template>
            <tr v-if="!matrixData.operators?.length"><td colspan="99" class="px-4 py-8 text-center text-gray-400">Aucun opérateur trouvé</td></tr>
          </tbody>
          <tfoot v-if="matrixData.operators?.length" class="bg-gray-100 border-t-2 border-gray-300 font-medium">
            <tr class="border-b"><td colspan="2" class="px-4 py-3 text-left font-semibold text-amber-700 bg-amber-50/50">Nombres de personnes au niveau I</td><td v-for="col in allColumns" :key="col.id + '-sumI'" class="px-2 py-3 text-center font-bold text-amber-700 bg-amber-50/30 border-r border-gray-200">{{ getCountPerNiveau(col, 'I') }}</td><td class="bg-gray-100"></td></tr>
            <tr class="border-b"><td colspan="2" class="px-4 py-3 text-left font-semibold text-blue-700 bg-blue-50/50">Nombres de personnes au niveau L</td><td v-for="col in allColumns" :key="col.id + '-sumL'" class="px-2 py-3 text-center font-bold text-blue-700 bg-blue-50/30 border-r border-gray-200">{{ getCountPerNiveau(col, 'L') }}</td><td class="bg-gray-100"></td></tr>
            <tr><td colspan="2" class="px-4 py-3 text-left font-semibold text-green-700 bg-green-50/50">Nombres de personnes au niveau U</td><td v-for="col in allColumns" :key="col.id + '-sumU'" class="px-2 py-3 text-center font-bold text-green-700 bg-green-50/30 border-r border-gray-200">{{ getCountPerNiveau(col, 'U') }}</td><td class="bg-gray-100"></td></tr>
          </tfoot>
        </table>
      </div>

      <!-- ==================== HISTORIQUE SECTION ==================== -->
      <div class="mt-8">
        <div class="flex items-center justify-between mb-4">
          <h2 class="text-xl font-bold text-gray-900">Historique des Evaluations</h2>
          <button @click="showHistory = !showHistory" class="flex items-center gap-2 px-4 py-2 rounded-lg text-sm font-medium transition-colors" :class="showHistory ? 'bg-gray-200 text-gray-700 hover:bg-gray-300' : 'bg-blue-600 text-white hover:bg-blue-700'">
            <svg v-if="!showHistory" class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" /></svg>
            <svg v-else class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 15l7-7 7 7" /></svg>
            {{ showHistory ? 'Masquer' : 'Afficher' }} l'historique
          </button>
        </div>
        <div v-if="showHistory">
          <div class="grid grid-cols-2 md:grid-cols-5 gap-3 mb-4">
            <div class="bg-white rounded-lg border p-3 text-center"><p class="text-2xl font-bold text-gray-900">{{ historyData.totalSessions || 0 }}</p><p class="text-xs text-gray-500">Total</p></div>
            <div class="bg-green-50 rounded-lg border border-green-200 p-3 text-center"><p class="text-2xl font-bold text-green-600">{{ historyData.totalPassed || 0 }}</p><p class="text-xs text-green-700">Reussi</p></div>
            <div class="bg-red-50 rounded-lg border border-red-200 p-3 text-center"><p class="text-2xl font-bold text-red-600">{{ historyData.totalFailed || 0 }}</p><p class="text-xs text-red-700">Echoue</p></div>
            <div class="bg-orange-50 rounded-lg border border-orange-200 p-3 text-center"><p class="text-2xl font-bold text-orange-600">{{ historyData.totalBlocked || 0 }}</p><p class="text-xs text-orange-700">Bloque</p></div>
            <div class="bg-purple-50 rounded-lg border border-purple-200 p-3 text-center"><p class="text-2xl font-bold text-purple-600">{{ historyData.totalSecondChance || 0 }}</p><p class="text-xs text-purple-700">2eme chance</p></div>
          </div>
          <div v-if="historyData.waitingForProduction?.length" class="bg-amber-50 border border-amber-200 rounded-xl p-4 mb-4">
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
          <div class="bg-white rounded-xl border p-4 mb-4">
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
          <div v-if="historyLoading" class="text-center py-8 text-gray-400">Chargement de l'historique...</div>
          <div v-else class="bg-white rounded-xl border overflow-x-auto">
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
                <tr v-for="h in filteredHistory" :key="h.sessionId" class="hover:bg-gray-50">
                  <td class="px-3 py-3 text-xs text-gray-600 whitespace-nowrap">{{ formatDate(h.completedAt || h.createdAt) }}</td>
                  <td class="px-3 py-3"><p class="font-medium text-gray-900 text-sm">{{ h.operatorName }}</p><p class="text-xs text-gray-400">{{ h.employeeId }}</p></td>
                  <td class="px-3 py-3">
                    <span :class="h.templateType === 'GENERIC_COMMON' ? 'bg-indigo-50 text-indigo-700 border-indigo-200' : 'bg-blue-50 text-blue-700 border-blue-200'" class="inline-flex px-2 py-1 rounded text-xs font-medium border">{{ h.templateType === 'GENERIC_COMMON' ? 'Generique' : 'Production' }}</span>
                  </td>
                  <td class="px-3 py-3 text-sm text-gray-700">{{ h.workstationName || '—' }}</td>
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
                    <span v-else class="text-gray-300">—</span>
                  </td>
                  <td class="px-3 py-3 text-center">
                    <span :class="h.isSecondChance ? 'bg-purple-100 text-purple-700 font-bold' : 'text-gray-500'" class="inline-flex items-center justify-center w-7 h-7 rounded-full text-xs">#{{ h.attemptNumber }}</span>
                  </td>
                  <td class="px-3 py-3 text-xs text-gray-500">{{ h.evaluatorName || '—' }}</td>
                </tr>
                <tr v-if="!filteredHistory.length"><td colspan="9" class="px-4 py-8 text-center text-gray-400">Aucun historique trouve</td></tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { evaluationApi } from '@/api/endpoints'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const loading = ref(true)
const historyLoading = ref(true)
const errorMsg = ref('')
const matrixData = ref({ operators: [], workstations: [] })
const historyData = ref({ history: [], waitingForProduction: [], totalSessions: 0, totalPassed: 0, totalFailed: 0, totalBlocked: 0, totalSecondChance: 0 })

const showHistory = ref(false)
const historySearch = ref('')
const historyFilterType = ref('')
const historyFilterStatus = ref('')
const historyFilterSecondChance = ref(false)

const workstations = computed(() => matrixData.value.workstations || [])

const zones = computed(() => {
  const map = {}
  map['Partie Générique'] = {
    name: 'Partie Générique',
    workstations: [
      { id: 'generic_security', name: 'Securité/5s', isGeneric: true, targetLevel: 'L' },
      { id: 'generic_quality', name: 'Qualité', isGeneric: true, targetLevel: 'L' }
    ]
  }
  workstations.value.forEach(ws => {
    const zoneName = ws.zoneName || 'Autres'
    if (!map[zoneName]) {
      map[zoneName] = { name: zoneName, workstations: [] }
    }
    map[zoneName].workstations.push({ ...ws, isGeneric: false })
  })
  return Object.values(map)
})

const allColumns = computed(() => zones.value.flatMap(z => z.workstations))

function getOperatorLabelRow1(op) {
  const hasRecyclage = Object.values(op.workstations || {}).some(w => w?.mode === 'RECYCLAGE') || op.genericMode === 'RECYCLAGE'
  return hasRecyclage ? 'Date recyclage' : "Date d'évaluation"
}

function getOperatorLabelRow2(op) {
  const hasRecyclage = Object.values(op.workstations || {}).some(w => w?.mode === 'RECYCLAGE') || op.genericMode === 'RECYCLAGE'
  return hasRecyclage ? 'Résultat recyclage' : 'Niveau compétence'
}

function getColumnDate(op, col) {
  if (col.isGeneric) {
    return op.genericPassed ? formatDate(op.genericDate) : '—'
  }
  return op.workstations?.[col.id]?.date ? formatDate(op.workstations[col.id].date) : '—'
}

function getColumnLevel(op, col) {
  if (col.isGeneric) {
    return op.genericPassed ? (op.genericLevel || 'L') : ''
  }
  return op.workstations?.[col.id]?.level || ''
}

function getTrainedCount(op) {
  if (!op.workstations) return 0
  let count = 0
  Object.values(op.workstations).forEach(ws => {
    if (['I', 'L', 'U'].includes(ws.level)) count++
  })
  return count
}

function getCountPerNiveau(col, niveau) {
  return (matrixData.value.operators || []).filter(op => getColumnLevel(op, col) === niveau).length
}

const niveauBgClass = (n) => ({
  I: 'bg-amber-400 text-white',
  L: 'bg-blue-500 text-white',
  U: 'bg-green-600 text-white'
}[n] || 'bg-gray-100 text-gray-400 border border-gray-200')

const niveauTextClass = (n) => ({
  I: 'bg-amber-400',
  L: 'bg-blue-500',
  U: 'bg-green-600'
}[n] || 'bg-gray-200 text-gray-500')

const statusClass = (status) => ({
  PASSED: 'bg-green-100 text-green-800',
  FAILED: 'bg-red-100 text-red-800',
  BLOCKED: 'bg-orange-100 text-orange-800',
  COMPLETED: 'bg-blue-100 text-blue-800'
}[status] || 'bg-gray-100 text-gray-600')

const statusLabel = (status) => ({
  PASSED: 'REUSSI',
  FAILED: 'ECHOUÉ',
  BLOCKED: 'BLOQUÉ',
  COMPLETED: 'COMPLETÉ'
}[status] || status)

const scoreColor = (score) => {
  if (score >= 100) return 'text-green-600'
  if (score >= 80) return 'text-blue-600'
  if (score >= 60) return 'text-amber-600'
  return 'text-red-600'
}

function formatDate(dateStr) {
  if (!dateStr) return '—'
  try {
    const d = new Date(dateStr)
    return d.toLocaleDateString('fr-FR', { day: '2-digit', month: '2-digit', year: 'numeric' })
  } catch {
    return dateStr
  }
}

const filteredHistory = computed(() => {
  let list = historyData.value.history || []
  if (historySearch.value) {
    const q = historySearch.value.toLowerCase()
    list = list.filter(h => h.operatorName?.toLowerCase().includes(q) || h.employeeId?.toLowerCase().includes(q))
  }
  if (historyFilterType.value) list = list.filter(h => h.templateType === historyFilterType.value)
  if (historyFilterStatus.value) list = list.filter(h => h.status === historyFilterStatus.value)
  if (historyFilterSecondChance.value) list = list.filter(h => h.isSecondChance)
  return list
})

async function loadMatrix() {
  loading.value = true
  errorMsg.value = ''
  if (!authStore.user && authStore.isAuthenticated) authStore.restoreFromToken()
  try {
    const res = await evaluationApi.getMatrix()
    matrixData.value = res.data || { operators: [], workstations: [] }
  } catch (e) {
    console.error('Error loading matrix', e)
    errorMsg.value = e.response?.data?.error || e.response?.data?.message || e.message || 'Erreur inconnue'
  }
  loading.value = false
}

async function loadHistory() {
  historyLoading.value = true
  try {
    const res = await evaluationApi.getHistory()
    historyData.value = res.data || { history: [], waitingForProduction: [] }
  } catch (e) {
    console.error('Error loading history', e)
  }
  historyLoading.value = false
}

async function loadAll() {
  await Promise.all([loadMatrix(), loadHistory()])
}

onMounted(loadAll)
</script>