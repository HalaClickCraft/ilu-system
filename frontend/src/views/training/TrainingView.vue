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

    <div class="grid grid-cols-1 gap-4 md:grid-cols-3">
      <div class="rounded-xl border border-gray-200 bg-white p-5 shadow-sm"><p class="text-sm text-gray-500">Total formations</p><p class="mt-1 text-2xl font-bold">{{ formations.length }}</p></div>
      <div class="rounded-xl border border-gray-200 bg-white p-5 shadow-sm"><p class="text-sm text-gray-500">En cours</p><p class="mt-1 text-2xl font-bold text-amber-600">{{ inProgressCount }}</p></div>
      <div class="rounded-xl border border-gray-200 bg-white p-5 shadow-sm"><p class="text-sm text-gray-500">Terminées</p><p class="mt-1 text-2xl font-bold text-emerald-600">{{ completedCount }}</p></div>
    </div>

    <div v-if="canContribute" class="rounded-xl border border-gray-200 bg-white shadow-sm">
      <div class="border-b border-gray-100 p-4">
        <h2 class="font-semibold text-gray-900">Opérateurs éligibles à la formation pratique</h2>
        <p class="mt-1 text-sm text-gray-500">Les modules théoriques sont terminés. Sélectionnez un poste pour démarrer une formation de 12 jours.</p>
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

    <div class="rounded-xl border border-gray-200 bg-white shadow-sm">
      <div class="flex flex-col gap-3 border-b border-gray-100 p-4 sm:flex-row">
        <input v-model="search" type="text" placeholder="Rechercher..." class="flex-1 rounded-lg border border-gray-200 px-4 py-2 text-sm outline-none focus:ring-2 focus:ring-emerald-500">
        <select v-model="statusFilter" class="rounded-lg border border-gray-200 px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-emerald-500">
          <option value="">Tous les statuts</option><option value="IN_PROGRESS">En cours</option><option value="COMPLETED">Terminée</option><option value="FAILED">Échouée</option>
        </select>
      </div>
      <div v-if="loading" class="py-16 text-center text-gray-400">Chargement…</div>
      <div v-else-if="filteredFormations.length" class="overflow-x-auto">
        <table class="w-full text-sm">
          <thead class="bg-gray-50"><tr><th class="px-4 py-3 text-left font-medium text-gray-500">Opérateur</th><th class="px-4 py-3 text-left font-medium text-gray-500">Poste</th><th class="px-4 py-3 text-left font-medium text-gray-500">Jours complets</th><th class="px-4 py-3 text-left font-medium text-gray-500">Statut</th><th class="px-4 py-3 text-right font-medium text-gray-500">Actions</th></tr></thead>
          <tbody><tr v-for="formation in filteredFormations" :key="formation.id" class="border-b border-gray-50 hover:bg-gray-50"><td class="px-4 py-3 font-medium">{{ formation.operatorName }}</td><td class="px-4 py-3 text-gray-500">{{ formation.workstationName }}</td><td class="px-4 py-3">{{ formation.daysWithData }} / 12</td><td class="px-4 py-3"><span class="rounded-full px-2 py-0.5 text-xs font-medium" :class="statusClass(formation.status)">{{ statusLabel(formation.status) }}</span></td><td class="px-4 py-3 text-right"><router-link :to="'/training/' + formation.id" class="text-emerald-600 hover:underline">Détail</router-link><button v-if="canContribute && formation.status === 'COMPLETED'" @click="startFormation({ id: formation.operatorId })" class="ml-3 text-emerald-600 hover:underline">Nouveau poste</button></td></tr></tbody>
        </table>
      </div>
      <div v-else class="py-16 text-center text-gray-400">Aucune formation trouvée</div>
    </div>

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
        <div class="mb-4 flex items-start justify-between"><div><h2 class="text-lg font-semibold">Saisie quotidienne groupée</h2><p class="text-sm text-gray-500">Première version : saisie pour les formations en cours du jour choisi.</p></div><select v-model.number="batchDay" class="rounded-lg border border-gray-300 px-3 py-2 text-sm"><option v-for="day in 12" :key="day" :value="day">Jour {{ day }}</option></select></div>
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
const statusFilter = ref('')
const error = ref('')
const batchDay = ref(1)
const batchEntries = reactive({})
const form = reactive({ projectId: '', zoneId: '', workstationId: '', operatorIds: [] })

const canEditCadence = computed(() => authStore.isChefEquipe)
const canEditDefects = computed(() => authStore.isAgentQualite)
const canContribute = computed(() => canEditCadence.value || canEditDefects.value)
const inProgressFormations = computed(() => formations.value.filter(formation => formation.status === 'IN_PROGRESS'))
const inProgressCount = computed(() => inProgressFormations.value.length)
const completedCount = computed(() => formations.value.filter(formation => formation.status === 'COMPLETED').length)
const selectedProject = computed(() => availableStructure.value.find(project => project.id === form.projectId))
const availableZones = computed(() => selectedProject.value?.zones || [])
const selectedZone = computed(() => availableZones.value.find(zone => zone.id === form.zoneId))
const availableWorkstations = computed(() => selectedZone.value?.workstations || [])
const filteredFormations = computed(() => formations.value.filter(formation => {
  const matchesSearch = !search.value || `${formation.operatorName} ${formation.workstationName}`.toLowerCase().includes(search.value.toLowerCase())
  return matchesSearch && (!statusFilter.value || formation.status === statusFilter.value)
}))

const statusLabel = status => ({ IN_PROGRESS: 'En cours', COMPLETED: 'Terminée', FAILED: 'Échouée' })[status] || status
const statusClass = status => ({ IN_PROGRESS: 'bg-amber-100 text-amber-700', COMPLETED: 'bg-emerald-100 text-emerald-700', FAILED: 'bg-red-100 text-red-700' })[status] || 'bg-gray-100 text-gray-600'

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
    formations.value
      .filter(formation => formation.status === 'IN_PROGRESS')
      .map(formation => formation.operatorId)
  )
  eligibleOperators.value = operatorsResponse.data.filter(
    operator => completion[operator.id] && !operatorsInProgress.has(operator.id)
  )
}

const createFormation = async () => {
  creating.value = true
  error.value = ''
  try {
    await trainingApi.createFormations(form.workstationId, form.operatorIds)
    showCreate.value = false
    form.projectId = ''; form.zoneId = ''; form.workstationId = ''; form.operatorIds = []
    await load()
  } catch (requestError) {
    error.value = requestError.response?.data?.message || 'Impossible de créer la formation.'
  } finally {
    creating.value = false
  }
}

const startFormation = operator => {
  form.projectId = ''
  form.zoneId = ''
  form.workstationId = ''
  form.operatorIds = [operator.id]
  showCreate.value = true
}

const openDailyBatch = () => {
  for (const formation of inProgressFormations.value) {
    batchEntries[formation.id] = { cadence: null, defauts: null }
  }
  showDailyBatch.value = true
}

const saveDailyBatch = async () => {
  const entries = inProgressFormations.value
    .map(formation => ({
      formationId: formation.id,
      dayNumber: batchDay.value,
      trackingDate: new Date().toISOString().slice(0, 10),
      ...(canEditCadence.value && batchEntries[formation.id].cadence !== null ? { cadence: batchEntries[formation.id].cadence } : {}),
      ...(canEditDefects.value && batchEntries[formation.id].defauts !== null ? { defauts: batchEntries[formation.id].defauts } : {}),
    }))
    .filter(entry => entry.cadence !== undefined || entry.defauts !== undefined)
  if (!entries.length) return
  savingBatch.value = true
  error.value = ''
  try {
    await trainingApi.saveDailyBatch(entries)
    showDailyBatch.value = false
    await load()
  } catch (requestError) {
    error.value = requestError.response?.data?.message || 'Impossible d’enregistrer la saisie.'
  } finally {
    savingBatch.value = false
  }
}

onMounted(async () => {
  try {
    await load()
  } catch (requestError) {
    error.value = requestError.response?.data?.message || 'Impossible de charger les formations.'
  } finally {
    loading.value = false
  }
})
</script>
