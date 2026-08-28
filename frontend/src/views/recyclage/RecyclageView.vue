<template>
  <div class="space-y-6">
    <!-- Global error banner (replaces alert()) -->
    <div v-if="errorMsg" class="bg-red-50 border border-red-200 rounded-lg p-4 flex items-start justify-between gap-3">
      <p class="text-sm text-red-800">{{ errorMsg }}</p>
      <button @click="errorMsg = ''" class="text-red-400 hover:text-red-600 font-bold">×</button>
    </div>

    <!-- Header -->
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">Planification Recyclage</h1>
        <p class="text-sm text-gray-500 mt-1">Gestion des evaluations initiales et de recyclage</p>
      </div>
      <button @click="openManualModal" class="inline-flex items-center gap-2 rounded-lg bg-emerald-600 px-4 py-2 text-sm font-medium text-white hover:bg-emerald-700">
        Activer un recyclage
      </button>
    </div>

    <div v-if="showManualModal" class="fixed inset-0 z-50 flex items-center justify-center bg-black/50" @click.self="showManualModal = false">
      <div class="w-full max-w-lg rounded-xl bg-white p-6 shadow-xl">
        <h3 class="text-lg font-bold text-gray-900">Activer un recyclage</h3>
        <p class="mt-1 text-sm text-gray-500">Sélectionnez l'opérateur et le poste concernés.</p>
        <div class="mt-4 grid grid-cols-1 gap-4 sm:grid-cols-2">
          <select v-model="manualForm.projectId" @change="manualForm.zoneId = null; manualForm.workstationId = null" class="rounded-lg border border-gray-300 px-3 py-2 text-sm"><option :value="null">Sélectionner un projet</option><option v-for="project in scopedProjects" :key="project.id" :value="project.id">{{ project.name }}</option></select>
          <select v-model="manualForm.zoneId" @change="manualForm.workstationId = null" :disabled="!manualForm.projectId" class="rounded-lg border border-gray-300 px-3 py-2 text-sm disabled:bg-gray-100"><option :value="null">Sélectionner une zone</option><option v-for="zone in manualZones" :key="zone.id" :value="zone.id">{{ zone.name }}</option></select>
          <select v-model="manualForm.workstationId" :disabled="!manualForm.zoneId" class="rounded-lg border border-gray-300 px-3 py-2 text-sm disabled:bg-gray-100"><option :value="null">Sélectionner un poste</option><option v-for="workstation in manualWorkstations" :key="workstation.id" :value="workstation.id">{{ workstation.name }}</option></select>
          <select v-model="manualForm.operatorId" class="rounded-lg border border-gray-300 px-3 py-2 text-sm"><option :value="null">Sélectionner un opérateur</option><option v-for="operator in scopedActiveOperators" :key="operator.id" :value="operator.id">{{ operator.lastName }} {{ operator.firstName }} — {{ operator.employeeId }}</option></select>
          <input v-model="manualForm.scheduledDate" type="date" class="rounded-lg border border-gray-300 px-3 py-2 text-sm sm:col-span-2">
        </div>
        <div class="mt-6 flex justify-end gap-3"><button @click="showManualModal = false" class="rounded-lg border px-4 py-2 text-sm">Annuler</button><button @click="createManualRecyclage" :disabled="loading || !manualForm.operatorId || !manualForm.workstationId" class="rounded-lg bg-emerald-600 px-4 py-2 text-sm text-white disabled:opacity-50">Activer</button></div>
      </div>
    </div>

    <!-- Filters -->
    <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-4">
      <div class="grid grid-cols-1 sm:grid-cols-4 gap-4">
        <div>
          <label class="block text-xs font-medium text-gray-500 mb-1">Projet</label>
          <select v-model="filters.projectId" @change="loadPlanning" class="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-emerald-500 focus:border-emerald-500">
            <option :value="null">Tous</option>
            <option v-for="p in scopedProjects" :key="p.id" :value="p.id">{{ p.name }}</option>
          </select>
        </div>
        
        <div>
          <label class="block text-xs font-medium text-gray-500 mb-1">Statut</label>
          <select v-model="filters.status" @change="loadPlanning" class="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-emerald-500 focus:border-emerald-500">
            <option value="">Tous</option>
            <option value="PLANIFIEE">Planifiee</option>
            <option value="EN_COURS">En Cours</option>
            <option value="TERMINEE">Terminee</option>
          </select>
        </div>

        <div>
          <label class="block text-xs font-medium text-gray-500 mb-1">Période / Type</label>
          <select v-model="filters.type" @change="loadPlanning" class="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-emerald-500 focus:border-emerald-500">
            <option value="">Tous</option>
            <option value="INITIALE">Éval. Initiale (1er Semestre)</option>
            <option value="RECYCLAGE">Recyclage (2ème Semestre)</option>
          </select>
        </div>

        <div>
          <label class="block text-xs font-medium text-gray-500 mb-1">Recherche</label>
          <input v-model="filters.search" type="text" placeholder="Nom operateur..." class="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-emerald-500 focus:border-emerald-500">
        </div>
      </div>
    </div>

    <!-- Table -->
    <div class="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
      <div class="overflow-x-auto">
        <table class="min-w-full divide-y divide-gray-200">
          <thead class="bg-gray-50">
            <tr>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Operateur</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Poste</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Type</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Source</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Date Planifiee</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Statut</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Niveau</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Actions</th>
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
            <tr v-for="item in filteredItems" :key="item.id" class="hover:bg-gray-50">
              <td class="px-4 py-3 text-sm font-medium text-gray-900">
                {{ item.operatorName }}
                <span class="block text-xs text-gray-400">{{ item.operatorEmployeeId || '' }}</span>
              </td>
              <td class="px-4 py-3 text-sm text-gray-700">{{ item.workstationName }}</td>
              <td class="px-4 py-3">
                <span :class="typeBadge(item.type)" class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium">
                  {{ typeLabel(item.type) }}
                </span>
              </td>
              <td class="px-4 py-3 text-sm text-gray-500">
                {{ sourceLabel(item.source) }}
              </td>
              <td class="px-4 py-3 text-sm text-gray-700">{{ formatDate(item.scheduledDate) }}</td>
              <td class="px-4 py-3">
                <span :class="statusBadge(item.status)" class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium">
                  {{ statusLabel(item.status) }}
                </span>
              </td>
              <td class="px-4 py-3 text-sm font-medium">
                <span v-if="item.niveauObtenu" :class="niveauBadge(item.niveauObtenu)">{{ item.niveauObtenu }}</span>
                <span v-else class="text-gray-300">-</span>
              </td>
              <td class="px-4 py-3 text-sm">
                <div class="flex items-center gap-3">
                  <button v-if="['PLANIFIEE', 'EN_COURS'].includes(item.status)" @click="startPlanningEvaluation(item)" :disabled="loading" class="inline-flex items-center gap-1.5 text-emerald-600 hover:text-emerald-800 font-medium disabled:opacity-50" title="Demarrer les questions de l'evaluation">
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 5v14l11-7z"/></svg>
                    Évaluer
                  </button>
                  <button v-if="item.status === 'TERMINEE' && item.evaluationSessionId" @click="$router.push({ name: 'evaluation-session', params: { id: item.evaluationSessionId } })" class="inline-flex items-center gap-1.5 text-blue-600 hover:text-blue-800 font-medium" title="Voir la session d'évaluation">
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"/></svg>
                    Voir
                  </button>
                </div>
              </td>
            </tr>
            <tr v-if="loading">
              <td colspan="8" class="px-4 py-12 text-center text-gray-400">Chargement des planifications...</td>
            </tr>
            <tr v-else-if="loadFailed">
              <td colspan="8" class="px-4 py-12 text-center">
                <p class="text-red-500 text-sm">Erreur de chargement des planifications.</p>
                <button @click="loadPlanning" class="mt-2 text-sm text-emerald-600 hover:underline font-medium">Réessayer</button>
              </td>
            </tr>
            <tr v-else-if="filteredItems.length === 0">
              <td colspan="8" class="px-4 py-12 text-center text-gray-400">Aucune planification trouvee</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { recyclageApi } from '@/services/recyclageApi'
import { evaluationApi, structureApi, operatorsApi } from '@/api/endpoints'
import { useUserScope } from '@/composables/useUserScope'

const router = useRouter()
const { loadUserProjects, filterOperators, isRestrictedRole, filterProjects } = useUserScope()
const allOperators = ref([])
const rawItems = ref([])

const projects = ref([])
const scopedProjects = computed(() => filterProjects(projects.value))
const projectList = computed(() => scopedProjects.value.map(p => ({ id: p.id, name: p.name })).sort((a, b) => a.name.localeCompare(b.name)))
const loading = ref(false)
const loadFailed = ref(false)
const errorMsg = ref('')
const showManualModal = ref(false)
const activeOperators = ref([])
const manualForm = ref({ projectId: null, zoneId: null, workstationId: null, operatorId: null, scheduledDate: '' })

const manualZones = computed(() => projects.value.find(project => project.id === manualForm.value.projectId)?.zones || [])
const manualWorkstations = computed(() => manualZones.value.find(zone => zone.id === manualForm.value.zoneId)?.workstations || [])

const filters = ref({
  status: '',
  type: '',
  projectId: null,
  search: '',
})

const items = computed(() => {
  const scopedOps = filterOperators(allOperators.value)
  if (isRestrictedRole.value && scopedOps.length === 0) return []
  const scopedOpIds = new Set(scopedOps.map(o => o.id))
  return rawItems.value.filter(item => {
    // Exclude the absolute first-time INITIALE evaluation type
    if (item.type === 'INITIALE') return false
    
    const itemOpId = item.operatorId || item.operator?.id
    if (itemOpId) {
      return scopedOpIds.has(itemOpId)
    }
    return !isRestrictedRole.value
  })
})

const filteredItems = computed(() => {
  let result = items.value
  
  if (filters.value.type) {
    if (filters.value.type === 'INITIALE') {
      const initialTypes = ['INITIALE_NOUVELLE_RECRUE', 'EVALUATION_ANNUELLE_MOIS_1']
      result = result.filter(i => initialTypes.includes(i.type))
    } else if (filters.value.type === 'RECYCLAGE') {
      const recyclageTypes = ['RECYCLAGE', 'RECYCLAGE_NOUVELLE_RECRUE', 'EVALUATION_ANNUELLE_MOIS_7']
      result = result.filter(i => recyclageTypes.includes(i.type))
    }
  }
  
  if (filters.value.status) {
    result = result.filter(i => i.status === filters.value.status)
  } else {
    result = result.filter(i => i.status !== 'ANNULEE')
  }
  if (filters.value.search) {
    const s = filters.value.search.toLowerCase()
    result = result.filter(i => (i.operatorName || '').toLowerCase().includes(s))
  }
  return result
})

async function loadPlanning() {
  loading.value = true
  loadFailed.value = false
  try {
    const params = {}
    if (filters.value.status) params.status = filters.value.status
    if (filters.value.projectId) params.projectId = filters.value.projectId
    const res = await recyclageApi.getPlanning(params)
    rawItems.value = res.data || []
  } catch (e) {
    console.error('Error loading planning:', e)
    loadFailed.value = true
  } finally {
    loading.value = false
  }
}

async function loadProjects() {
  try {
    const res = await structureApi.getAll()
    projects.value = Array.isArray(res.data) ? res.data : (res.data?.projects || [])
  } catch (e) {
    console.error('Error loading projects:', e)
  }
}

async function openManualModal() {
  manualForm.value = { projectId: filters.value.projectId, zoneId: null, workstationId: null, operatorId: null, scheduledDate: new Date().toISOString().slice(0, 10) }
  showManualModal.value = true
  if (!activeOperators.value.length) activeOperators.value = (await operatorsApi.getActive()).data || []
}

async function createManualRecyclage() {
  loading.value = true
  errorMsg.value = ''
  try {
    await recyclageApi.createManual(manualForm.value)
    showManualModal.value = false
    await loadPlanning()
  } catch (e) {
    errorMsg.value = e.response?.data?.message || e.response?.data?.error || "Impossible d'activer le recyclage"
  } finally {
    loading.value = false
  }
}

async function startPlanningEvaluation(item) {
  loading.value = true
  errorMsg.value = ''
  try {
    const config = (await recyclageApi.startEvaluation(item.id)).data
    const session = (await evaluationApi.startEvaluation({
      operatorId: config.operatorId,
      templateId: config.templateId,
      mode: config.mode,
      nextTemplateId: config.nextTemplateId,
      planningId: item.id,
    })).data
    router.push({ name: 'evaluation-session', params: { id: session.sessionId }, query: { planningId: item.id } })
  } catch (e) {
    errorMsg.value = e.response?.data?.message || e.response?.data?.error || "Impossible de demarrer les questions de l'evaluation"
  } finally {
    loading.value = false
  }
}

function formatDate(d) {
  if (!d) return '-'
  if (typeof d === 'string' && /^\d{2}\/\d{2}\/\d{4}$/.test(d)) {
    const [day, month, year] = d.split('/').map(Number)
    return new Date(year, month - 1, day).toLocaleDateString('fr-FR', { day: '2-digit', month: '2-digit', year: 'numeric' })
  }
  const date = new Date(d)
  return date.toLocaleDateString('fr-FR', { day: '2-digit', month: '2-digit', year: 'numeric' })
}

function statusLabel(s) {
  const m = { PLANIFIEE: 'Planifiee', EN_COURS: 'En Cours', TERMINEE: 'Terminee', ANNULEE: 'Annulee' }
  return m[s] || s
}

function statusBadge(s) {
  const m = {
    PLANIFIEE: 'bg-blue-100 text-blue-800',
    EN_COURS: 'bg-yellow-100 text-yellow-800',
    TERMINEE: 'bg-green-100 text-green-800',
    ANNULEE: 'bg-gray-100 text-gray-600',
  }
  return m[s] || 'bg-gray-100 text-gray-600'
}

function typeBadge(t) {
  const badges = {
    INITIALE_NOUVELLE_RECRUE: 'bg-purple-100 text-purple-800',
    RECYCLAGE_NOUVELLE_RECRUE: 'bg-pink-100 text-pink-800',
    EVALUATION_ANNUELLE_MOIS_1: 'bg-blue-100 text-blue-800',
    EVALUATION_ANNUELLE_MOIS_7: 'bg-orange-100 text-orange-800',
    RECYCLAGE: 'bg-teal-100 text-teal-800',
    INITIALE: 'bg-gray-100 text-gray-800',
  }
  return badges[t] || 'bg-gray-100 text-gray-800'
}

function typeLabel(t) {
  const labels = {
    INITIALE_NOUVELLE_RECRUE: 'Évaluation initiale (nouvelle recrue)',
    RECYCLAGE_NOUVELLE_RECRUE: 'Recyclage (nouvelle recrue - 6 mois)',
    EVALUATION_ANNUELLE_MOIS_1: 'Évaluation initiale (déjà en poste - Janvier)',
    EVALUATION_ANNUELLE_MOIS_7: 'Recyclage (déjà en poste - Juillet)',
    RECYCLAGE: 'Recyclage (reprise d\'absence / ancien)',
    INITIALE: 'Initiale (ancien)',
  }
  return labels[t] || t
}

function niveauBadge(n) {
  const m = { I: 'bg-yellow-400 text-white', L: 'bg-blue-500 text-white', U: 'bg-emerald-500 text-white' }
  return m[n] || 'bg-gray-200 text-gray-600'
}

function sourceLabel(s) {
  const m = { ANNUELLE: 'Annuelle', NOUVELLE_RECRUE: 'Nouvelle Recrue', REPRISE_ABSENCE: 'Reprise Absence', CHEF_EQUIPE: 'Chef d\'equipe' }
  return m[s] || s
}

const scopedActiveOperators = computed(() => {
  return filterOperators(activeOperators.value)
})


onMounted(async () => {
  try {
    await loadUserProjects()
    const opsRes = await operatorsApi.getAll()
    allOperators.value = opsRes.data || []
  } catch (e) {
    console.error(e)
  }
  loadProjects()
  loadPlanning()
})
</script>
