<template>
  <div class="space-y-6">
    <div class="flex flex-col sm:flex-row sm:items-start sm:justify-between gap-3">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">MATRICE DE POLYVALENCE{{ selectedProjectName ? ' — ' + selectedProjectName : '' }}</h1>
        <p class="text-sm text-gray-500 mt-1">Indicateur de polyvalence: Minimum 6 personnes formees par poste => 6 personnes en L</p>
      </div>
      <div class="flex flex-col sm:flex-row items-start sm:items-center gap-3">
        <div v-if="showProjectFilter" class="flex items-center gap-2">
          <label class="text-sm font-medium text-gray-600 whitespace-nowrap">Projet:</label>
          <select v-model="selectedProject" class="px-3 py-2 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 outline-none min-w-[180px]">
            <option value="">Tous les projets</option>
            <option v-for="p in projectList" :key="p.id" :value="p.id">{{ p.name }}</option>
          </select>
        </div>
        <div class="flex items-center gap-3 text-xs">
          <span class="flex items-center gap-1"><span class="w-4 h-4 rounded bg-gray-100 border inline-block"></span> Non forme</span>
          <span class="flex items-center gap-1"><span class="w-4 h-4 rounded bg-amber-400 inline-block text-white"></span> Niveau I</span>
          <span class="flex items-center gap-1"><span class="w-4 h-4 rounded bg-blue-500 inline-block text-white"></span> Niveau L</span>
          <span class="flex items-center gap-1"><span class="w-4 h-4 rounded bg-green-600 inline-block text-white"></span> Niveau U</span>
        </div>
      </div>
    </div>

    <div v-if="loading" class="text-center py-12 text-gray-400">Chargement de la matrice...</div>
    <div v-else-if="errorMsg" class="bg-red-50 border border-red-200 rounded-xl p-6 text-center">
      <p class="text-red-700 font-semibold">Erreur de chargement</p>
      <p class="text-red-500 text-sm mt-1">{{ errorMsg }}</p>
      <button @click="loadMatrix" class="mt-3 bg-red-600 text-white px-4 py-2 rounded-lg text-sm hover:bg-red-700">Reessayer</button>
    </div>

    <template v-else>
      <div class="bg-white rounded-xl border overflow-x-auto shadow-sm">
        <table class="min-w-full border-collapse text-xs text-gray-700">
          <thead>
            <tr class="bg-gray-100 border-b border-gray-200">
              <th rowspan="3" class="px-4 py-3 text-left font-bold text-gray-700 sticky left-0 bg-gray-100 z-20 border-r border-gray-200 min-w-[180px]">Operateur</th>
              <th rowspan="3" class="px-3 py-2 text-center font-bold text-gray-700 border-r border-gray-200 min-w-[120px] bg-gray-100">Zone</th>
              <th v-for="z in zones" :key="z.name" :colspan="z.workstations.length" class="px-3 py-2 text-center font-bold text-gray-800 border-r border-gray-200 bg-gray-50">{{ z.name }}</th>
              <th rowspan="3" class="px-4 py-3 text-center font-bold text-gray-700 border-l border-gray-200 bg-gray-50 max-w-[150px] whitespace-normal">Nombre des postes sur lesquels est forme un operateur</th>
            </tr>
            <tr class="bg-gray-100 border-b border-gray-200">
              <th v-for="col in allColumns" :key="col.id" class="px-3 py-2 text-center font-semibold text-gray-700 border-r border-gray-200 min-w-[90px]">{{ col.name }}</th>
            </tr>
            <tr class="bg-gray-50 border-b border-gray-300 text-gray-500">
              <th v-for="col in allColumns" :key="col.id + '-target'" class="px-3 py-1.5 text-center font-medium border-r border-gray-200">{{ formatNiveau(col.targetLevel || 'L') }}</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-200">
            <template v-for="op in matrixData.operators" :key="op.operatorId">
              <tr class="hover:bg-gray-50">
                <td rowspan="3" class="px-4 py-3 sticky left-0 bg-white z-10 font-bold text-gray-900 border-r border-gray-200">
                  {{ op.operatorName }}
                  <div class="text-[10px] text-gray-400 font-normal">{{ op.employeeId }} · {{ op.seniorityMonths }} mois</div>
                </td>
                <td class="px-3 py-2 font-medium text-gray-500 bg-gray-50 border-r border-gray-200 text-left">Date d'évaluation</td>
                <td v-for="col in allColumns" :key="col.id + '-date'" class="px-2 py-2 text-center border-r border-gray-200 text-[10px] text-gray-600">{{ getColumnDate(op, col) }}</td>
                <td rowspan="3" class="px-4 py-3 text-center border-l border-gray-200 font-bold text-lg text-slate-800 bg-slate-50">{{ getTrainedCount(op) }}</td>
              </tr>
              <tr class="hover:bg-gray-50 border-b border-gray-300">
                <td class="px-3 py-2 font-medium text-gray-500 bg-gray-50 border-r border-gray-200 text-left">Niveau compétence</td>
                <td v-for="col in allColumns" :key="col.id + '-level'" class="px-2 py-2 text-center border-r border-gray-200">
                  <span :class="niveauBgClass(formatNiveau(getColumnLevel(op, col)))" class="inline-block w-8 h-8 leading-8 rounded-lg text-white font-bold text-xs shadow-sm">{{ formatNiveau(getColumnLevel(op, col)) }}</span>
                </td>
              </tr>
              <tr class="hover:bg-orange-50/30 border-b border-gray-300">
                <td class="px-3 py-2 font-medium text-orange-700 bg-orange-50 border-r border-gray-200 text-left">Résultat recyclage</td>
                <td v-for="col in allColumns" :key="col.id + '-recyclage'" class="px-2 py-2 text-center border-r border-gray-200 text-[10px]" :class="recyclageClass(getRecyclageStatus(op, col))">{{ getRecyclageDisplay(op, col) }}</td>
              </tr>
            </template>
            <tr v-if="!matrixData.operators?.length"><td colspan="99" class="px-4 py-8 text-center text-gray-400">Aucun operateur trouve</td></tr>
          </tbody>
          <tfoot v-if="matrixData.operators?.length" class="bg-gray-100 border-t-2 border-gray-300 font-medium">
            <tr class="border-b"><td colspan="2" class="px-4 py-3 text-left font-semibold text-amber-700 bg-amber-50/50">Nombres de personnes au niveau I</td><td v-for="col in allColumns" :key="col.id + '-sumI'" class="px-2 py-3 text-center font-bold text-amber-700 bg-amber-50/30 border-r border-gray-200">{{ getCountPerNiveau(col, 'I') }}</td><td class="bg-gray-100"></td></tr>
            <tr class="border-b"><td colspan="2" class="px-4 py-3 text-left font-semibold text-blue-700 bg-blue-50/50">Nombres de personnes au niveau L</td><td v-for="col in allColumns" :key="col.id + '-sumL'" class="px-2 py-3 text-center font-bold text-blue-700 bg-blue-50/30 border-r border-gray-200">{{ getCountPerNiveau(col, 'L') }}</td><td class="bg-gray-100"></td></tr>
            <tr><td colspan="2" class="px-4 py-3 text-left font-semibold text-green-700 bg-green-50/50">Nombres de personnes au niveau U</td><td v-for="col in allColumns" :key="col.id + '-sumU'" class="px-2 py-3 text-center font-bold text-green-700 bg-green-50/30 border-r border-gray-200">{{ getCountPerNiveau(col, 'U') }}</td><td class="bg-gray-100"></td></tr>
          </tfoot>
        </table>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { evaluationApi, structureApi } from '@/api/endpoints'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const loading = ref(true)
const errorMsg = ref('')
const matrixData = ref({ operators: [], workstations: [] })

const projects = ref([])
const selectedProject = ref('')
const selectedProjectName = ref('')

const isMultiProjectRole = computed(() =>
  authStore.hasAnyRole(['RESP_QUALITE', 'AGENT_QUALITE', 'SUPERVISEUR', 'RESP_HSE', 'ADMIN', 'RH', 'CHEF_EQUIPE'])
)
const showProjectFilter = computed(() => isMultiProjectRole.value && projectList.value.length >= 1)
const projectList = computed(() => projects.value.map(p => ({ id: p.id, name: p.name })).sort((a, b) => a.name.localeCompare(b.name)))

const workstations = computed(() => matrixData.value.workstations || [])

const zones = computed(() => {
  const map = {}
  map['Partie Generique'] = {
    name: 'Partie Generique',
    workstations: [
      { id: 'generic_security', name: 'Securite/5s', isGeneric: true, targetLevel: 'L' },
      { id: 'generic_quality', name: 'Qualite', isGeneric: true, targetLevel: 'L' }
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

function getColumnDate(op, col) {
  if (col.isGeneric) {
    return op.genericPassed ? formatDate(op.genericDate) : '-'
  }
  return op.workstations?.[col.id]?.date ? formatDate(op.workstations[col.id].date) : '-'
}

function getColumnLevel(op, col) {
  if (col.isGeneric) {
    return op.genericPassed ? (op.genericLevel || 'L') : ''
  }
  return op.workstations?.[col.id]?.level || ''
}

function getRecyclageStatus(op, col) {
  return col.isGeneric ? '' : (op.workstations?.[col.id]?.recyclageStatus || '')
}

function getRecyclageDisplay(op, col) {
  if (col.isGeneric) return '-'
  const recyclage = op.workstations?.[col.id]
  if (!recyclage?.recyclageStatus) return '-'
  const labels = { PLANIFIEE: 'Planifie', EN_COURS: 'En cours', TERMINEE: 'Termine', ANNULEE: 'Annule' }
  const result = recyclage.recyclageLevel ? ` · ${recyclage.recyclageLevel}` : ''
  return `${labels[recyclage.recyclageStatus] || recyclage.recyclageStatus} (${formatDate(recyclage.recyclageDate)})${result}`
}

function recyclageClass(status) {
  return {
    PLANIFIEE: 'text-orange-700',
    EN_COURS: 'text-blue-700',
    TERMINEE: 'text-green-700 font-semibold',
    ANNULEE: 'text-gray-400',
  }[status] || 'text-gray-400'
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

function formatDate(dateStr) {
  if (!dateStr) return '-'
  if (typeof dateStr !== 'string') return String(dateStr)
  if (dateStr.includes('/')) return dateStr
  try {
    const d = new Date(dateStr)
    if (isNaN(d.getTime())) return dateStr
    return d.toLocaleDateString('fr-FR', { day: '2-digit', month: '2-digit', year: 'numeric' })
  } catch {
    return dateStr
  }
}

function formatNiveau(level) {
  if (!level) return ''
  const upper = level.toUpperCase().trim()
  if (upper === 'I' || upper === 'NIVEAU_1' || upper === '1') return 'I'
  if (upper === 'L' || upper === 'NIVEAU_2' || upper === '2') return 'L'
  if (upper === 'U' || upper === 'NIVEAU_3' || upper === '3') return 'U'
  if (['I', 'L', 'U'].includes(upper.charAt(0))) return upper.charAt(0)
  return level
}

async function loadMatrix() {
  loading.value = true
  errorMsg.value = ''
  if (!authStore.user && authStore.isAuthenticated) authStore.restoreFromToken()
  try {
    const projectId = selectedProject.value ? Number(selectedProject.value) : null
    const res = await evaluationApi.getMatrix(projectId)
    matrixData.value = res.data || { operators: [], workstations: [] }
    selectedProjectName.value = res.data?.projectName || ''
  } catch (e) {
    console.error('Error loading matrix', e)
    errorMsg.value = e.response?.data?.error || e.response?.data?.message || e.message || 'Erreur inconnue'
  }
  loading.value = false
}

async function loadProjects() {
  if (!isMultiProjectRole.value) return
  try {
    const res = await structureApi.getAll()
    projects.value = res.data || []
    if (projects.value.length === 1) {
      selectedProject.value = projects.value[0].id
    }
  } catch (e) {
    console.error('Error loading projects', e)
  }
}

watch(selectedProject, () => {
  loadMatrix()
})

onMounted(async () => {
  await loadProjects()
  await loadMatrix()
})
</script>