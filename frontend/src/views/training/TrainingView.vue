<template>
  <div class="space-y-6">
    <div class="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">Formation</h1>
        <p class="mt-1 text-gray-500">Suivi et planification des formations ILU</p>
      </div>
      <div v-if="canContribute" class="flex gap-2">
        <button v-if="inProgressCount" @click="openDailyBatch" class="rounded-lg border border-emerald-600 px-4 py-2.5 text-sm font-medium text-emerald-700 hover:bg-emerald-50">Saisie quotidienne</button>
      </div>
    </div>

    <div v-if="error" class="rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">{{ error }}</div>

    <!-- Stats cards -->
    <div class="grid grid-cols-2 gap-4 md:grid-cols-4">
      <div class="rounded-xl border border-gray-200 bg-white p-5 shadow-sm"><p class="text-sm text-gray-500">Opérateurs</p><p class="mt-1 text-2xl font-bold">{{ operatorGroups.length }}</p></div>
      <div class="rounded-xl border border-gray-200 bg-white p-5 shadow-sm"><p class="text-sm text-gray-500">En cours</p><p class="mt-1 text-2xl font-bold text-amber-600">{{ inProgressCount }}</p></div>
      <div class="rounded-xl border border-gray-200 bg-white p-5 shadow-sm"><p class="text-sm text-gray-500">Réussies</p><p class="mt-1 text-2xl font-bold text-emerald-600">{{ completedCount }}</p></div>
      <div class="rounded-xl border border-gray-200 bg-white p-5 shadow-sm"><p class="text-sm text-gray-500">Échouées</p><p class="mt-1 text-2xl font-bold text-red-600">{{ failedCount }}</p></div>
    </div>

    <!-- Eligible operators -->
    <div v-if="canContribute" class="rounded-xl border border-gray-200 bg-white shadow-sm">
      <div class="border-b border-gray-100 p-4">
        <h2 class="font-semibold text-gray-900">Opérateurs éligibles à la formation pratique</h2>
        <p class="mt-1 text-sm text-gray-500">Modules théoriques terminés. Sélectionnez un poste pour démarrer.</p>
      </div>
      <div v-if="eligibleOperators.length" class="overflow-x-auto">
        <table class="w-full text-sm">
          <thead class="bg-gray-50"><tr><th class="px-4 py-3 text-left font-medium text-gray-500">Opérateur</th><th class="px-4 py-3 text-left font-medium text-gray-500">Matricule</th><th class="px-4 py-3 text-right font-medium text-gray-500">Action</th></tr></thead>
          <tbody>
            <tr v-for="operator in eligibleOperators" :key="operator.id" class="border-b border-gray-50">
              <td class="px-4 py-3 font-medium">{{ operator.lastName }} {{ operator.firstName }}</td>
              <td class="px-4 py-3 text-gray-500">{{ operator.employeeId }}</td>
              <td class="px-4 py-3 text-right"><button @click="startFormation(operator)" class="text-emerald-600 hover:underline">Démarrer au poste</button></td>
            </tr>
          </tbody>
        </table>
      </div>
      <div v-else class="p-6 text-center text-sm text-gray-400">Aucun opérateur n'est actuellement éligible.</div>
    </div>

    <!-- ===== MAIN TABLE: Grouped by operator ===== -->
    <div class="rounded-xl border border-gray-200 bg-white shadow-sm">
      <!-- Tabs + Search -->
      <div class="border-b border-gray-100 p-4">
        <div class="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
          <div class="flex gap-1 rounded-lg bg-gray-100 p-1">
            <button v-for="tab in tabs" :key="tab.value" @click="activeTab = tab.value" class="px-3 py-1.5 rounded-md text-sm font-medium transition-colors" :class="activeTab === tab.value ? 'bg-white text-gray-900 shadow-sm' : 'text-gray-500 hover:text-gray-700'">{{ tab.label }}<span class="ml-1.5 text-xs opacity-60">{{ tab.count }}</span></button>
          </div>
          <input v-model="search" type="text" placeholder="Rechercher un opérateur..." class="rounded-lg border border-gray-200 px-4 py-2 text-sm outline-none focus:ring-2 focus:ring-emerald-500 sm:w-64">
        </div>
      </div>

      <div v-if="loading" class="py-16 text-center text-gray-400">Chargement…</div>

      <!-- Operator rows -->
      <div v-else-if="filteredGroups.length" class="divide-y divide-gray-100">
        <div v-for="op in filteredGroups" :key="op.operatorId">
          <!-- Operator header row -->
          <div class="flex items-center gap-4 px-4 py-3 cursor-pointer hover:bg-gray-50 transition-colors" @click="toggleOperator(op.operatorId)">
            <svg class="w-4 h-4 text-gray-400 transition-transform flex-shrink-0" :class="{ 'rotate-90': expandedOperators.has(op.operatorId) }" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"></path></svg>
            <div class="flex-1 min-w-0">
              <span class="font-medium text-gray-900">{{ op.operatorName }}</span>
              <span class="ml-2 text-xs text-gray-400">{{ op.matricule }}</span>
            </div>
            <div class="flex items-center gap-3 flex-shrink-0">
              <span class="text-xs text-gray-400">{{ op.formations.length }} formation{{ op.formations.length > 1 ? 's' : '' }}</span>
              <span v-if="op.inProgress" class="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium bg-amber-100 text-amber-700">{{ op.inProgress }} en cours</span>
              <span v-if="op.completed" class="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium bg-emerald-100 text-emerald-700">{{ op.completed }} réussie{{ op.completed > 1 ? 's' : '' }}</span>
              <span v-if="op.failed" class="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium bg-red-100 text-red-700">{{ op.failed }} échouée{{ op.failed > 1 ? 's' : '' }}</span>
              <span v-if="canContribute && op.hasCompletedFormation" @click.stop="startFormation({ id: op.operatorId })" class="text-xs text-emerald-600 hover:underline cursor-pointer ml-1">+ Nouveau poste</span>
            </div>
          </div>

          <!-- Expanded: formation history -->
          <div v-if="expandedOperators.has(op.operatorId)" class="bg-gray-50/50 border-t border-gray-100">
            <table class="w-full text-sm">
              <thead class="bg-gray-100/80"><tr>
                <th class="px-4 py-2 text-left text-xs font-medium text-gray-500">Poste</th>
                <th class="px-4 py-2 text-center text-xs font-medium text-gray-500">Jours</th>
                <th class="px-4 py-2 text-center text-xs font-medium text-gray-500">Cadence moy.</th>
                <th class="px-4 py-2 text-center text-xs font-medium text-gray-500">Défauts</th>
                <th class="px-4 py-2 text-center text-xs font-medium text-gray-500">Statut</th>
                <th class="px-4 py-2 text-right text-xs font-medium text-gray-500">Action</th>
              </tr></thead>
              <tbody>
                <tr v-for="f in op.formations" :key="f.id" class="border-t border-gray-100 hover:bg-white">
                  <td class="px-4 py-2.5 font-medium text-gray-800">{{ f.workstationName }}</td>
                  <td class="px-4 py-2.5 text-center text-gray-600">{{ f.daysWithData ?? 0 }} / 12</td>
                  <td class="px-4 py-2.5 text-center" :class="f.averageCadence != null && f.targetCadence != null && f.averageCadence >= f.targetCadence ? 'text-emerald-600 font-medium' : 'text-gray-600'">{{ f.averageCadence != null ? f.averageCadence : '-' }}</td>
                  <td class="px-4 py-2.5 text-center" :class="f.totalDefects < (f.qualityObjective ?? 7) ? 'text-emerald-600' : 'text-red-600'">{{ f.totalDefects ?? 0 }} / {{ f.qualityObjective ?? 7 }}</td>
                  <td class="px-4 py-2.5 text-center"><span class="rounded-full px-2 py-0.5 text-xs font-medium" :class="statusClass(f.status)">{{ statusLabel(f.status) }}</span></td>
                  <td class="px-4 py-2.5 text-right">
                    <router-link :to="'/training/' + f.id" class="text-emerald-600 hover:underline text-xs font-medium">{{ f.status === 'IN_PROGRESS' ? 'Saisir' : 'Détail' }}</router-link>
                  </td>
                </tr>
                <tr v-if="op.formations.length === 0"><td colspan="6" class="px-4 py-4 text-center text-gray-400 text-xs">Aucune formation</td></tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <div v-else class="py-16 text-center text-gray-400">Aucun opérateur trouvé</div>
    </div>

    <!-- ===== MODALS (unchanged) ===== -->
    <div v-if="showCreate" class="fixed inset-0 z-50 flex items-center justify-center bg-black/50" @click.self="showCreate = false">
      <form class="mx-4 w-full max-w-lg space-y-4 rounded-2xl bg-white p-6 shadow-xl" @submit.prevent="createFormation">
        <div><h2 class="text-lg font-semibold">Démarrer une formation pratique</h2><p class="mt-1 text-sm text-gray-500">Choisissez le projet, la zone et le poste pour cet opérateur.</p></div>
        <div><label class="mb-1 block text-sm font-medium">Projet</label><select v-model="form.projectId" required class="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm" @change="form.zoneId = ''; form.workstationId = ''"><option value="">Sélectionner</option><option v-for="project in availableStructure" :key="project.id" :value="project.id">{{ project.name }}</option></select></div>
        <div><label class="mb-1 block text-sm font-medium">Zone</label><select v-model="form.zoneId" required :disabled="!form.projectId" class="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm" @change="form.workstationId = ''"><option value="">Sélectionner</option><option v-for="zone in availableZones" :key="zone.id" :value="zone.id">{{ zone.name }}</option></select></div>
        <div><label class="mb-1 block text-sm font-medium">Poste de travail</label><select v-model="form.workstationId" required :disabled="!form.zoneId" class="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm"><option value="">Sélectionner</option><option v-for="workstation in availableWorkstations" :key="workstation.id" :value="workstation.id">{{ workstation.name }}</option></select></div>
        <div class="flex justify-end gap-3 pt-2"><button type="button" @click="showCreate = false" class="px-4 py-2 text-sm text-gray-600">Annuler</button><button type="submit" :disabled="creating" class="rounded-lg bg-emerald-600 px-4 py-2 text-sm text-white disabled:opacity-50">Démarrer</button></div>
      </form>
    </div>

    <div v-if="showDailyBatch" class="fixed inset-0 z-50 flex items-center justify-center bg-black/50" @click.self="showDailyBatch = false">
      <form class="mx-4 max-h-[90vh] w-full max-w-4xl overflow-auto rounded-2xl bg-white p-6 shadow-xl" @submit.prevent="saveDailyBatch">
        <div class="mb-4 flex items-start justify-between"><div><h2 class="text-lg font-semibold">Saisie quotidienne groupée</h2><p class="text-sm text-gray-500">Saisie pour les formations en cours du jour choisi.</p></div><select v-model.number="batchDay" class="rounded-lg border border-gray-300 px-3 py-2 text-sm"><option v-for="day in 12" :key="day" :value="day">Jour {{ day }}</option></select></div>
        <table class="w-full text-sm"><thead class="bg-gray-50"><tr><th class="px-3 py-2 text-left">Opérateur</th><th class="px-3 py-2 text-left">Poste</th><th v-if="canEditCadence" class="px-3 py-2 text-left">Cadence</th><th v-if="canEditDefects" class="px-3 py-2 text-left">Défauts</th></tr></thead><tbody><tr v-for="formation in inProgressFormations" :key="formation.id" class="border-b"><td class="px-3 py-2">{{ formation.operatorName }}</td><td class="px-3 py-2 text-gray-500">{{ formation.workstationName }}</td><td v-if="canEditCadence" class="px-3 py-2"><input v-model.number="batchEntries[formation.id].cadence" min="0" type="number" class="w-24 rounded border border-gray-300 px-2 py-1"></td><td v-if="canEditDefects" class="px-3 py-2"><input v-model.number="batchEntries[formation.id].defauts" min="0" type="number" class="w-24 rounded border border-gray-300 px-2 py-1"></td></tr></tbody></table>
        <div class="mt-5 flex justify-end gap-3"><button type="button" @click="showDailyBatch = false" class="px-4 py-2 text-sm text-gray-600">Annuler</button><button :disabled="savingBatch" type="submit" class="rounded-lg bg-emerald-600 px-4 py-2 text-sm text-white disabled:opacity-50">Enregistrer la saisie</button></div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { trainingApi, operatorsApi } from '@/api/endpoints'
import onboardingApi from '@/api/onboarding'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const formations = ref([])
const eligibleOperators = ref([])
const availableStructure = ref([])
const loading = ref(true)
const creating = ref(false)
const savingBatch = ref(false)
const showCreate = ref(false)
const showDailyBatch = ref(false)
const search = ref('')
const activeTab = ref('ALL')
const error = ref('')
const batchDay = ref(1)
const batchEntries = reactive({})
const form = reactive({ projectId: '', zoneId: '', workstationId: '', operatorIds: [] })
const expandedOperators = ref(new Set())

const canEditCadence = computed(() => authStore.isChefEquipe)
const canEditDefects = computed(() => authStore.isAgentQualite)
const canContribute = computed(() => canEditCadence.value || canEditDefects.value)
const inProgressFormations = computed(() => formations.value.filter(f => f.status === 'IN_PROGRESS'))
const inProgressCount = computed(() => formations.value.filter(f => f.status === 'IN_PROGRESS').length)
const completedCount = computed(() => formations.value.filter(f => f.status === 'COMPLETED').length)
const failedCount = computed(() => formations.value.filter(f => f.status === 'FAILED').length)
const selectedProject = computed(() => availableStructure.value.find(p => p.id === form.projectId))
const availableZones = computed(() => selectedProject.value?.zones || [])
const selectedZone = computed(() => availableZones.value.find(z => z.id === form.zoneId))
const availableWorkstations = computed(() => selectedZone.value?.workstations || [])

// ===== GROUP FORMATIONS BY OPERATOR =====
const operatorGroups = computed(() => {
  const map = new Map()
  for (const f of formations.value) {
    if (!map.has(f.operatorId)) {
      map.set(f.operatorId, { operatorId: f.operatorId, operatorName: f.operatorName, matricule: f.operatorEmployeeId, formations: [], inProgress: 0, completed: 0, failed: 0, hasCompletedFormation: false })
    }
    const group = map.get(f.operatorId)
    group.formations.push(f)
    if (f.status === 'IN_PROGRESS') group.inProgress++
    if (f.status === 'COMPLETED') { group.completed++; group.hasCompletedFormation = true }
    if (f.status === 'FAILED') group.failed++
  }
  return Array.from(map.values())
})

const tabs = computed(() => [
  { label: 'Tous', value: 'ALL', count: operatorGroups.value.length },
  { label: 'En cours', value: 'IN_PROGRESS', count: operatorGroups.value.filter(o => o.inProgress > 0).length },
  { label: 'Réussies', value: 'COMPLETED', count: operatorGroups.value.filter(o => o.completed > 0 && o.inProgress === 0 && o.failed === 0).length },
  { label: 'Échouées', value: 'FAILED', count: operatorGroups.value.filter(o => o.failed > 0).length },
])

const filteredGroups = computed(() => {
  let groups = operatorGroups.value
  // Tab filter
  if (activeTab.value === 'IN_PROGRESS') groups = groups.filter(o => o.inProgress > 0)
  else if (activeTab.value === 'COMPLETED') groups = groups.filter(o => o.completed > 0 && o.inProgress === 0 && o.failed === 0)
  else if (activeTab.value === 'FAILED') groups = groups.filter(o => o.failed > 0)
  // Search
  if (search.value) {
    const q = search.value.toLowerCase()
    groups = groups.filter(o => `${o.operatorName} ${o.matricule}`.toLowerCase().includes(q))
  }
  return groups
})

const toggleOperator = (id) => {
  if (expandedOperators.value.has(id)) expandedOperators.value.delete(id)
  else expandedOperators.value.add(id)
  expandedOperators.value = new Set(expandedOperators.value)
}

const statusLabel = s => ({ IN_PROGRESS: 'En cours', COMPLETED: 'Réussie', FAILED: 'Échouée' })[s] || s
const statusClass = s => ({ IN_PROGRESS: 'bg-amber-100 text-amber-700', COMPLETED: 'bg-emerald-100 text-emerald-700', FAILED: 'bg-red-100 text-red-700' })[s] || 'bg-gray-100 text-gray-600'

const load = async () => {
  error.value = ''
  const [formationsResponse, operatorsResponse] = await Promise.all([
    trainingApi.getFormations(), operatorsApi.getActive(),
  ])
  formations.value = formationsResponse.data
  if (canContribute.value) {
    availableStructure.value = (await trainingApi.getAvailableStructure()).data
  } else {
    availableStructure.value = []
  }
  const completion = (await onboardingApi.batchCheckComplete(operatorsResponse.data.map(operator => operator.id))).data
  const operatorsInProgress = new Set(
    formations.value.filter(f => f.status === 'IN_PROGRESS').map(f => f.operatorId)
  )
  eligibleOperators.value = operatorsResponse.data.filter(
    operator => completion[operator.id] && !operatorsInProgress.has(operator.id)
  )
}

const createFormation = async () => {
  creating.value = true; error.value = ''
  try {
    await trainingApi.createFormations(form.workstationId, form.operatorIds)
    showCreate.value = false
    form.projectId = ''; form.zoneId = ''; form.workstationId = ''; form.operatorIds = []
    await load()
  } catch (e) {
    error.value = e.response?.data?.message || 'Impossible de créer la formation.'
  } finally { creating.value = false }
}

const startFormation = operator => {
  form.projectId = ''; form.zoneId = ''; form.workstationId = ''
  form.operatorIds = [operator.id]
  showCreate.value = true
}

const openDailyBatch = () => {
  for (const f of inProgressFormations.value) batchEntries[f.id] = { cadence: null, defauts: null }
  showDailyBatch.value = true
}

const saveDailyBatch = async () => {
  const entries = inProgressFormations.value
    .map(f => ({
      formationId: f.id, dayNumber: batchDay.value,
      trackingDate: new Date().toISOString().slice(0, 10),
      ...(canEditCadence.value && batchEntries[f.id].cadence !== null ? { cadence: batchEntries[f.id].cadence } : {}),
      ...(canEditDefects.value && batchEntries[f.id].defauts !== null ? { defauts: batchEntries[f.id].defauts } : {}),
    }))
    .filter(e => e.cadence !== undefined || e.defauts !== undefined)
  if (!entries.length) return
  savingBatch.value = true; error.value = ''
  try {
    await trainingApi.saveDailyBatch(entries)
    showDailyBatch.value = false
    await load()
  } catch (e) {
    error.value = e.response?.data?.message || 'Impossible d\'enregistrer la saisie.'
  } finally { savingBatch.value = false }
}

onMounted(async () => {
  try { await load() } catch (e) {
    error.value = e.response?.data?.message || 'Impossible de charger les formations.'
  } finally { loading.value = false }
})
</script>