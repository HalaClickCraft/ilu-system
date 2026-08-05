<template>
  <div class="space-y-6">
    <div v-if="loading" class="py-20 text-center text-gray-400">Chargement…</div>
    <template v-else-if="formation">
      <div class="flex items-center gap-4">
        <button @click="$router.push('/training')" class="text-gray-400 hover:text-gray-600">←</button>
        <h1 class="text-2xl font-bold text-gray-900">Évaluation formation</h1>
        <span class="rounded-full px-2.5 py-0.5 text-xs font-medium" :class="statusClass(formation.status)">{{ statusLabel(formation.status) }}</span>
      </div>
      <div v-if="error" class="rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">{{ error }}</div>

      <div class="grid grid-cols-2 gap-4 md:grid-cols-3 lg:grid-cols-6">
        <div class="rounded-xl border border-gray-200 bg-white p-4 shadow-sm"><p class="text-xs text-gray-500">Opérateur</p><p class="mt-1 text-sm font-semibold">{{ formation.operatorName }}</p></div>
        <div class="rounded-xl border border-gray-200 bg-white p-4 shadow-sm"><p class="text-xs text-gray-500">Poste</p><p class="mt-1 text-sm font-semibold">{{ formation.workstationName }}</p></div>
        <div class="rounded-xl border border-gray-200 bg-white p-4 shadow-sm"><p class="text-xs text-gray-500">Cadence objectif</p><p class="mt-1 text-sm font-semibold">{{ formation.targetCadence ?? '-' }} /j</p></div>
        <div class="rounded-xl border border-gray-200 bg-white p-4 shadow-sm"><p class="text-xs text-gray-500">Moy. cadence</p><p class="mt-1 text-sm font-semibold" :class="cadenceColor">{{ averageCadence }}</p></div>
        <div class="rounded-xl border border-gray-200 bg-white p-4 shadow-sm"><p class="text-xs text-gray-500">Défauts total</p><p class="mt-1 text-sm font-semibold" :class="defectsColor">{{ totalDefects }} / {{ formation.qualityObjective ?? 7 }}</p></div>
        <div class="rounded-xl border border-purple-200 bg-purple-50 p-4 shadow-sm"><p class="text-xs font-medium text-purple-600">Objectif qualité</p><div v-if="canEditQuality && editingQO" class="mt-1 flex gap-1"><input v-model.number="editQOValue" min="1" type="number" class="w-14 rounded border border-purple-300 px-1 text-sm"><button @click="saveQualityObjective" class="text-xs text-purple-700">OK</button></div><div v-else class="mt-1 flex gap-2"><p class="text-sm font-semibold text-purple-700">{{ formation.qualityObjective ?? 7 }}</p><button v-if="canEditQuality" @click="editingQO = true; editQOValue = formation.qualityObjective ?? 7" class="text-xs text-purple-600">Modifier</button></div></div>
      </div>

      <div v-if="chartReady" class="rounded-xl border border-gray-200 bg-white p-4 shadow-sm"><h2 class="mb-3 text-sm font-semibold">Diagramme de suivi</h2><div class="h-[300px]"><Line :data="chartData" :options="chartOptions" /></div></div>

      <div class="rounded-xl border border-gray-200 bg-white shadow-sm">
        <div class="flex flex-wrap items-center justify-between gap-3 border-b border-gray-100 p-4">
          <div><h2 class="text-sm font-semibold">Saisie des données (J1–J12)</h2><p class="mt-1 text-xs text-gray-500">Chef d’équipe : cadence · Agent qualité : défauts</p></div>
          <div class="flex items-center gap-2"><span class="text-xs text-gray-400">{{ completeDays }} / 12 jours complets</span><button v-if="canContribute && hasDirty" @click="saveAll" :disabled="saving" class="rounded-lg bg-emerald-600 px-3 py-1.5 text-xs text-white disabled:opacity-50">Enregistrer</button><button v-if="canContribute" @click="evaluate" :disabled="evaluating || !readyToEvaluate" class="rounded-lg bg-amber-500 px-3 py-1.5 text-xs text-white disabled:opacity-50">Auto-évaluer</button></div>
        </div>
        <div class="overflow-x-auto">
          <table class="w-full text-sm">
            <thead class="bg-gray-50"><tr><th class="w-48 px-3 py-2 text-left text-xs font-medium text-gray-500">Indicateur</th><th v-for="day in 12" :key="day" class="w-16 px-1 py-2 text-center text-xs font-medium">J{{ day }}</th><th class="w-20 bg-gray-100 px-3 py-2 text-center text-xs">Moy.</th><th class="w-16 bg-gray-100 px-3 py-2 text-center text-xs">Total</th></tr></thead>
            <tbody>
              <tr class="bg-teal-50/50"><td class="px-3 py-2 text-xs font-medium text-teal-700">Cadence objectif</td><td v-for="day in 12" :key="'target' + day" class="px-1 py-2 text-center text-xs text-teal-700">{{ formation.targetCadence ?? '-' }}</td><td class="bg-gray-100 px-3 py-2 text-center text-xs font-bold">{{ formation.targetCadence ?? '-' }}</td><td class="bg-gray-100 px-3 py-2 text-center text-xs">-</td></tr>
              <tr><td class="px-3 py-2 text-xs font-medium">Cadence réalisée</td><td v-for="day in 12" :key="'cadence' + day" class="px-1 py-2 text-center"><input v-if="canEditCadence && canEdit" v-model.number="dayData[day].cadence" min="0" type="number" class="w-14 rounded border border-emerald-300 py-1 text-center text-xs" @input="dirtyDays.add(day)"><span v-else :title="dayData[day].cadenceSubmittedBy ? `Saisi par ${dayData[day].cadenceSubmittedBy}` : ''" class="text-xs font-semibold" :class="getCadenceColor(dayData[day].cadence)">{{ dayData[day].cadence ?? '-' }}</span></td><td class="bg-gray-100 px-3 py-2 text-center text-xs font-bold" :class="cadenceColor">{{ averageCadence }}</td><td class="bg-gray-100 px-3 py-2 text-center text-xs">-</td></tr>
              <tr><td class="px-3 py-2 text-xs font-medium">Nombre de défauts</td><td v-for="day in 12" :key="'defects' + day" class="px-1 py-2 text-center"><input v-if="canEditDefects && canEdit" v-model.number="dayData[day].defects" min="0" type="number" class="w-14 rounded border border-purple-300 py-1 text-center text-xs" @input="dirtyDays.add(day)"><span v-else :title="dayData[day].defectsSubmittedBy ? `Saisi par ${dayData[day].defectsSubmittedBy}` : ''" class="text-xs font-semibold" :class="(dayData[day].defects ?? 0) > 0 ? 'text-red-600' : 'text-emerald-600'">{{ dayData[day].defects ?? '-' }}</span></td><td class="bg-gray-100 px-3 py-2 text-center text-xs">-</td><td class="bg-gray-100 px-3 py-2 text-center text-xs font-bold" :class="defectsColor">{{ totalDefects }}</td></tr>
              <tr><td colspan="14" class="bg-blue-50/30 px-3 py-2 text-xs text-blue-700">L’évaluation est automatique uniquement lorsque les deux mesures sont présentes pour les 12 jours.</td></tr>
            </tbody>
          </table>
        </div>
        <div v-if="evalResult" class="border-t p-4 text-center" :class="evalResult.passed ? 'bg-emerald-50' : 'bg-red-50'"><h3 class="font-bold" :class="evalResult.passed ? 'text-emerald-700' : 'text-red-700'">{{ evalResult.passed ? 'FORMATION VALIDÉE' : 'FORMATION ÉCHOUÉE' }}</h3><p class="mt-2 text-sm">Cadence moyenne : {{ evalResult.averageCadence }} / {{ evalResult.targetCadence }} · Défauts : {{ evalResult.totalDefects }} / {{ evalResult.qualityObjective }}</p></div>
        <div v-if="formation.status === 'FAILED'" class="flex items-center justify-between border-t border-red-100 bg-red-50/50 p-4"><span class="text-sm font-medium text-red-700">Formation échouée. La période de 12 jours peut être recommencée.</span><button @click="resetFormation" class="rounded-lg bg-amber-500 px-4 py-2 text-sm text-white hover:bg-amber-600">Recommencer</button></div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { Line } from 'vue-chartjs'
import { BarElement } from 'chart.js'
import { Chart as ChartJS, CategoryScale, LinearScale, PointElement, LineElement, Title, Tooltip, Legend } from 'chart.js'
import { trainingApi } from '@/api/endpoints'
import { useAuthStore } from '@/stores/auth'

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, BarElement, Title, Tooltip, Legend)

const route = useRoute()
const authStore = useAuthStore()
const formation = ref(null)
const loading = ref(true)
const saving = ref(false)
const evaluating = ref(false)
const editingQO = ref(false)
const editQOValue = ref(7)
const chartReady = ref(false)
const chartData = ref(null)
const evalResult = ref(null)
const error = ref('')
const dirtyDays = reactive(new Set())
const dayData = reactive({})
for (let day = 1; day <= 12; day++) dayData[day] = { cadence: null, defects: null, cadenceSubmittedBy: null, defectsSubmittedBy: null }

const canEditCadence = computed(() => authStore.isChefEquipe)
const canEditDefects = computed(() => authStore.isAgentQualite)
const canEditQuality = computed(() => authStore.isAgentQualite)
const canContribute = computed(() => canEditCadence.value || canEditDefects.value)
const canEdit = computed(() => formation.value?.status === 'IN_PROGRESS')
const hasDirty = computed(() => dirtyDays.size > 0)
const completeDays = computed(() => Object.values(dayData).filter(day => day.cadence !== null && day.defects !== null).length)
const readyToEvaluate = computed(() => completeDays.value === 12)
const averageCadence = computed(() => {
  const values = Object.values(dayData).map(day => day.cadence).filter(value => value !== null)
  return values.length ? (values.reduce((sum, value) => sum + value, 0) / values.length).toFixed(1) : '-'
})
const totalDefects = computed(() => Object.values(dayData).reduce((sum, day) => sum + (day.defects ?? 0), 0))
const cadenceColor = computed(() => averageCadence.value !== '-' && Number(averageCadence.value) >= formation.value?.targetCadence ? 'text-emerald-600' : 'text-red-600')
const defectsColor = computed(() => totalDefects.value < (formation.value?.qualityObjective ?? 7) ? 'text-emerald-600' : 'text-red-600')
const statusLabel = status => ({ IN_PROGRESS: 'En cours', COMPLETED: 'Réussie', FAILED: 'Échouée' })[status] || status
const statusClass = status => ({ IN_PROGRESS: 'bg-amber-100 text-amber-700', COMPLETED: 'bg-emerald-100 text-emerald-700', FAILED: 'bg-red-100 text-red-700' })[status] || 'bg-gray-100 text-gray-600'
const getCadenceColor = value => value !== null && formation.value?.targetCadence && value >= formation.value.targetCadence ? 'text-emerald-600' : 'text-red-600'
const chartOptions = { responsive: true, maintainAspectRatio: false, interaction: { mode: 'index', intersect: false }, plugins: { legend: { position: 'top' } }, scales: { y: { beginAtZero: true, title: { display: true, text: 'Cadence' } }, y1: { type: 'linear', position: 'right', beginAtZero: true, title: { display: true, text: 'Défauts' }, grid: { drawOnChartArea: false } } } }

const load = async () => {
  const [detailResponse, trackingResponse, chartResponse] = await Promise.all([trainingApi.getFormationDetail(route.params.id), trainingApi.getTracking(route.params.id), trainingApi.getChartData(route.params.id)])
  formation.value = detailResponse.data
  for (let day = 1; day <= 12; day++) dayData[day] = { cadence: null, defects: null, cadenceSubmittedBy: null, defectsSubmittedBy: null }
  for (const tracking of trackingResponse.data) {
    if (tracking.dayNumber >= 1 && tracking.dayNumber <= 12) {
      dayData[tracking.dayNumber] = { cadence: tracking.actualCadence, defects: tracking.defects, cadenceSubmittedBy: tracking.cadenceSubmittedBy, defectsSubmittedBy: tracking.defectsSubmittedBy }
    }
  }
  const raw = chartResponse.data
  chartData.value = { labels: raw.labels, datasets: [raw.targetCadenceDataset, raw.achievedCadenceDataset, raw.defectsDataset] }
  chartReady.value = true
}

const saveAll = async () => {
  saving.value = true
  error.value = ''
  try {
    const days = [...dirtyDays].map(dayNumber => ({
      dayNumber,
      trackingDate: new Date().toISOString().slice(0, 10),
      ...(canEditCadence.value ? { actualCadence: dayData[dayNumber].cadence } : {}),
      ...(canEditDefects.value ? { defects: dayData[dayNumber].defects } : {}),
    }))
    await trainingApi.batchSave(route.params.id, days)
    dirtyDays.clear()
    await load()
  } catch (requestError) {
    error.value = requestError.response?.data?.message || 'Impossible d’enregistrer le suivi.'
  } finally {
    saving.value = false
  }
}

const evaluate = async () => {
  evaluating.value = true
  error.value = ''
  try {
    if (hasDirty.value) await saveAll()
    evalResult.value = (await trainingApi.autoEvaluate(route.params.id)).data
    await load()
  } catch (requestError) {
    error.value = requestError.response?.data?.message || 'Impossible d’évaluer la formation.'
  } finally {
    evaluating.value = false
  }
}

const saveQualityObjective = async () => {
  try {
    await trainingApi.setQualityObjective(route.params.id, editQOValue.value)
    editingQO.value = false
    await load()
  } catch (requestError) {
    error.value = requestError.response?.data?.message || 'Impossible de mettre à jour l’objectif qualité.'
  }
}

const resetFormation = async () => {
  try {
    await trainingApi.resetFormation(route.params.id)
    evalResult.value = null
    dirtyDays.clear()
    await load()
  } catch (requestError) {
    error.value = requestError.response?.data?.message || 'Impossible de réinitialiser la formation.'
  }
}

onMounted(async () => {
  try {
    await load()
  } catch (requestError) {
    error.value = requestError.response?.data?.message || 'Impossible de charger la formation.'
  } finally {
    loading.value = false
  }
})
</script>
