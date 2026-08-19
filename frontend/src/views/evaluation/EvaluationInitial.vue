<template>
  <div class="p-6">
    <h1 class="text-2xl font-bold text-gray-800 mb-6">Évaluation Initiale</h1>

    <!-- Filters -->
    <div class="mb-4 flex flex-col sm:flex-row gap-3 items-start sm:items-center">
      <div class="relative flex-1 max-w-md">
        <svg class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" /></svg>
        <input
          v-model="search"
          type="text"
          placeholder="Rechercher par nom ou matricule..."
          class="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none"
        />
      </div>
      <div v-if="showProjectFilter" class="flex items-center gap-2">
        <label class="text-sm font-medium text-gray-600 whitespace-nowrap">Projet:</label>
        <select
          v-model="selectedProject"
          class="px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none min-w-[180px]"
        >
          <option value="">Tous les projets</option>
          <option v-for="p in projectList" :key="p.id" :value="p.id">{{ p.name }}</option>
        </select>
      </div>
      <div v-if="selectedProject" class="flex items-center gap-2">
        <label class="text-sm font-medium text-gray-600 whitespace-nowrap">Zone:</label>
        <select v-model="selectedZone" class="px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none min-w-[160px]" @change="selectedPoste = ''">
          <option value="">Toutes les zones</option>
          <option v-for="z in availableZones" :key="z.id" :value="z.id">{{ z.name }}</option>
        </select>
      </div>
      <div v-if="selectedProject && (selectedZone || !selectedZone)" class="flex items-center gap-2">
        <label class="text-sm font-medium text-gray-600 whitespace-nowrap">Poste:</label>
        <select v-model="selectedPoste" class="px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none min-w-[180px]">
          <option value="">Tous les postes</option>
          <option v-for="w in availablePostes" :key="w.id" :value="w.id">{{ w.name }}</option>
        </select>
      </div>
    </div>

    <!-- Project filter active header -->
    <div v-if="selectedProject && !loading" class="mb-3 px-4 py-2 bg-blue-50 border border-blue-200 rounded-lg text-sm font-medium text-blue-800">
      <svg class="w-4 h-4 inline -mt-0.5 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 7v10a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-6l-2-2H5a2 2 0 00-2 2z" /></svg>
      {{ selectedProjectName }} — {{ filteredOperators.length }} operateur(s)
    </div>

    <!-- Loading -->
    <div v-if="loading" class="flex items-center justify-center py-12">
      <svg class="animate-spin h-8 w-8 text-blue-500" viewBox="0 0 24 24">
        <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" fill="none"/>
        <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
      </svg>
      <span class="ml-3 text-gray-500">Chargement...</span>
    </div>

    <!-- Empty -->
    <div v-else-if="filteredOperators.length === 0" class="text-center py-12 text-gray-400">
      <svg class="mx-auto h-16 w-16 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M9 5H7a2 2 0 00-2 2v10a2 2 0 002 2h8a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"/>
      </svg>
      <p v-if="search">Aucun resultat pour "{{ search }}"</p>
      <p v-else>Aucun operateur en attente d'évaluation</p>
    </div>

    <!-- Table -->
    <div v-else class="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
      <table class="min-w-full divide-y divide-gray-200">
        <thead class="bg-gray-50">
          <tr>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Opérateur</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Matricule</th>
            <th v-if="showProjectColumn" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Projet</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Poste</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Zone</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Fin Formation</th>
            <th class="px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider">Action</th>
          </tr>
        </thead>
        <tbody class="bg-white divide-y divide-gray-200">
          <tr v-for="op in filteredOperators" :key="op.operatorId + '-' + op.formationId" class="hover:bg-gray-50">
            <td class="px-6 py-4 whitespace-nowrap">
              <div class="text-sm font-medium text-gray-900">{{ op.operatorName }}</div>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-600">{{ op.operatorEmployeeId }}</td>
            <td v-if="showProjectColumn" class="px-6 py-4 whitespace-nowrap"><span class="text-xs bg-blue-50 text-blue-700 px-2 py-0.5 rounded-full">{{ getProjectForOperator(op.operatorId) }}</span></td>
            <td class="px-6 py-4 whitespace-nowrap">
              <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
                {{ op.workstationName }}
              </span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap">
              <span class="text-xs bg-gray-100 text-gray-600 px-2 py-0.5 rounded-full">{{ getZoneForWorkstation(op.workstationName) }}</span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-600">
              {{ op.formationEndDate || '-' }}
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-center">
              <button
                @click="startInitialEvaluation(op)"
                :disabled="op.loading"
                class="inline-flex items-center px-4 py-2 bg-blue-600 text-white text-sm font-medium rounded-lg hover:bg-blue-700 focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
              >
                <svg v-if="op.loading" class="animate-spin -ml-1 mr-2 h-4 w-4" viewBox="0 0 24 24">
                  <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" fill="none"/>
                  <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
                </svg>
                <span v-else class="mr-1">&#9654;</span>
                Commencer
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Error Toast -->
    <div v-if="errorMsg" class="fixed bottom-6 right-6 bg-red-500 text-white px-6 py-3 rounded-lg shadow-lg max-w-md">
      <div class="flex items-start">
        <svg class="h-5 w-5 mt-0.5 mr-3 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
        </svg>
        <div>
          <p class="text-sm font-medium">Erreur</p>
          <p class="text-sm mt-1 opacity-90">{{ errorMsg }}</p>
        </div>
        <button @click="errorMsg = ''" class="ml-3 flex-shrink-0 text-white hover:text-red-100">&times;</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { evaluationApi, structureApi, operatorsApi } from '@/api/endpoints'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const operators = ref([])
const search = ref('')
const loading = ref(true)
const errorMsg = ref('')
const saveSuccess = ref(false)

// Project filter
const projects = ref([])
const teams = ref([])
const selectedProject = ref('')
const workstations = ref([])
const selectedZone = ref('')
const selectedPoste = ref('')

const isMultiProjectRole = computed(() =>
  authStore.hasAnyRole(['RESP_QUALITE', 'AGENT_QUALITE', 'SUPERVISEUR', 'RESP_HSE', 'ADMIN', 'RH'])
)
const showProjectFilter = computed(() => isMultiProjectRole.value && projectList.value.length > 1)
const showProjectColumn = computed(() => isMultiProjectRole.value)
const projectList = computed(() => projects.value.map(p => ({ id: p.id, name: p.name })).sort((a, b) => a.name.localeCompare(b.name)))
const selectedProjectName = computed(() => {
  if (!selectedProject.value) return ''
  return projects.value.find(p => p.id === Number(selectedProject.value))?.name || ''
})

const availableZones = computed(() => {
  if (!selectedProject.value) return []
  const p = projects.value.find(pr => pr.id === Number(selectedProject.value))
  return (p?.zones || []).map(z => ({ id: z.id, name: z.name }))
})
const availablePostes = computed(() => {
  if (!selectedZone.value) {
    // If project selected but no zone, show all workstations in project
    if (!selectedProject.value) return []
    const p = projects.value.find(pr => pr.id === Number(selectedProject.value))
    const allWs = []
    for (const z of (p?.zones || [])) {
      for (const w of (z.workstations || [])) allWs.push({ id: w.id, name: w.name })
    }
    return allWs
  }
  const zone = availableZones.value.find(z => z.id === Number(selectedZone.value))
  return (zone?.workstations || []).map(w => ({ id: w.id, name: w.name }))
})

// Build team->project map for operator project lookup
const teamProjectMap = computed(() => {
  const map = {}
  for (const team of teams.value) {
    if (team.projects && team.projects.length) {
      map[team.id] = team.projects.map(p => p.name)
    }
  }
  return map
})

const getZoneForWorkstation = (wsName) => {
  const ws = workstations.value.find(w => w.name === wsName)
  return ws?.zoneName || '-'
}

const getProjectForOperator = (operatorId) => {
  // Find the operator in our full operators list
  const op = allOperatorsData.value.find(o => o.id === operatorId)
  if (!op?.team?.id) return '-'
  const projNames = teamProjectMap.value[op.team.id]
  return projNames?.length ? projNames.join(', ') : '-'
}

// We need a separate list of all operators with team info for project lookup
const allOperatorsData = ref([])

const filteredOperators = computed(() => {
  let result = operators.value
  // Filter by project
  if (selectedProject.value) {
    const pid = Number(selectedProject.value)
    const pName = projects.value.find(p => p.id === pid)?.name
    if (pName) {
      result = result.filter(op => {
        const projNames = getProjectNamesForEvalOperator(op.operatorId)
        return projNames.includes(pName)
      })
    }
  }
  // Filter by zone
  if (selectedZone.value) {
    const zId = Number(selectedZone.value)
    const zone = availableZones.value.find(z => z.id === zId)
    const wsNames = new Set((zone?.workstations || []).map(w => w.name))
    result = result.filter(op => wsNames.has(op.workstationName))
  }
  // Filter by poste
  if (selectedPoste.value) {
    const poste = availablePostes.value.find(w => w.id === Number(selectedPoste.value))
    if (poste) result = result.filter(op => op.workstationName === poste.name)
  }
  // Filter by search
  if (search.value) {
    const q = search.value.toLowerCase()
    result = result.filter(op =>
      op.operatorName?.toLowerCase().includes(q) ||
      op.operatorEmployeeId?.toLowerCase().includes(q) ||
      op.workstationName?.toLowerCase().includes(q)
    )
  }
  return result
})

// Get project names for an operator using the allOperators data
const getProjectNamesForEvalOperator = (operatorId) => {
  const op = allOperatorsData.value.find(o => o.id === operatorId)
  if (!op?.team?.id) return []
  return teamProjectMap.value[op.team.id] || []
}

async function fetchPending() {
  loading.value = true
  try {
    const res = await evaluationApi.getAllPendingEvaluations()
    operators.value = res.data.map(op => ({ ...op, loading: false }))
  } catch (err) {
    errorMsg.value = err.response?.data?.message || 'Erreur de chargement'
  } finally {
    loading.value = false
  }
}

async function startInitialEvaluation(op) {
  op.loading = true
  errorMsg.value = ''
  try {
    // Step 1: Auto-resolve templates (generic + production)
    const resolveRes = await evaluationApi.resolveTemplates(op.operatorId, op.formationId)
    const data = resolveRes.data

    let templateId, mode, nextTemplateId

    if (data.startWithProduction) {
      // Already passed generic, go straight to production
      templateId = data.productionTemplateId
      mode = 'INITIAL'
      nextTemplateId = null
    } else {
      // Start with generic, link to production via nextTemplateId
      templateId = data.genericTemplateId
      mode = 'INITIAL'
      nextTemplateId = data.productionTemplateId
    }

    // Step 2: Start the evaluation session (backend will resume if IN_PROGRESS exists)
    const startRes = await evaluationApi.startEvaluation({
      operatorId: op.operatorId,
      templateId: templateId,
      formationId: op.formationId,
      mode: mode,
      nextTemplateId: nextTemplateId
    })

    const sessionId = startRes.data.sessionId

    // If resumed, show a brief notification
    if (startRes.data.resumed) {
      saveSuccess.value = true
      setTimeout(() => { saveSuccess.value = false }, 2000)
    }

    router.push({ name: 'evaluation-session', params: { id: sessionId } })
  } catch (err) {
    const msg = err.response?.data?.message || err.response?.data || 'Erreur inconnue'
    errorMsg.value = typeof msg === 'string' ? msg : JSON.stringify(msg)
  } finally {
    op.loading = false
  }
}

onMounted(async () => {
  await Promise.allSettled([fetchPending(), fetchProjectsAndTeams()])
})

async function fetchProjectsAndTeams() {
  try {
    const [projRes, teamsRes, opsRes, wsRes] = await Promise.allSettled([
      structureApi.getAll(),
      structureApi.getTeams(),
      operatorsApi.getAll(),
      structureApi.getWorkstations(),
    ])
    if (projRes.status === 'fulfilled') projects.value = projRes.value.data || []
    if (teamsRes.status === 'fulfilled') teams.value = teamsRes.value.data || []
    if (opsRes.status === 'fulfilled') allOperatorsData.value = opsRes.value.data || []
    if (wsRes.status === 'fulfilled') workstations.value = wsRes.value.data || []
  } catch (e) {
    console.error('Error loading project/teams data', e)
  }
}
</script>