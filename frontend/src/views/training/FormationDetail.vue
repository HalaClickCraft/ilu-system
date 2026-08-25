<template>
  <div class="space-y-6">
    <div v-if="loading" class="py-20 text-center text-gray-400">Chargement...</div>
    <template v-else-if="formation">
      <BreadcrumbNav :crumbs="[{ label: 'Formation', to: '/training' }, { label: formation.workstationName || 'Détail' }]" />
    <div class="flex items-center gap-4">
        <button @click="$router.push('/training')" class="text-gray-400 hover:text-gray-600">
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M15 19l-7-7 7-7"
            ></path>
          </svg>
        </button>
        <h1 class="text-2xl font-bold text-gray-900">Évaluation formation</h1>
        <span
          class="rounded-full px-2.5 py-0.5 text-xs font-medium"
          :class="statusClass(formation.status)"
          >{{ statusLabel(formation.status) }}</span
        >
      </div>
      <div
        v-if="error"
        class="rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700"
      >
        {{ error }}
      </div>

      <div class="grid grid-cols-2 gap-4 md:grid-cols-3 lg:grid-cols-6">
        <div class="rounded-xl border border-gray-200 bg-white p-4 shadow-sm">
          <p class="text-xs text-gray-500">Opérateur</p>
          <p class="mt-1 text-sm font-semibold">{{ formation.operatorName }}</p>
        </div>
        <div class="rounded-xl border border-gray-200 bg-white p-4 shadow-sm">
          <p class="text-xs text-gray-500">Poste</p>
          <p class="mt-1 text-sm font-semibold">{{ formation.workstationName }}</p>
        </div>
        <div class="rounded-xl border border-gray-200 bg-white p-4 shadow-sm">
          <p class="text-xs text-gray-500">Cadence objectif</p>
          <p class="mt-1 text-sm font-semibold">{{ formation.targetCadence ?? '-' }} /j</p>
        </div>
        <div class="rounded-xl border border-gray-200 bg-white p-4 shadow-sm">
          <p class="text-xs text-gray-500">Moy. cadence</p>
          <p class="mt-1 text-sm font-semibold" :class="cadenceColor">{{ averageCadence }}</p>
        </div>
        <div class="rounded-xl border border-gray-200 bg-white p-4 shadow-sm">
          <p class="text-xs text-gray-500">Défauts total</p>
          <p class="mt-1 text-sm font-semibold" :class="defectsColor">
            {{ totalDefects }} / {{ formation.qualityObjective ?? 7 }}
          </p>
        </div>
        <div class="rounded-xl border border-purple-200 bg-purple-50 p-4 shadow-sm">
          <p class="text-xs font-medium text-purple-600">Objectif qualité</p>
          <div v-if="canEditQuality && editingQO" class="mt-1 flex gap-1">
            <input
              v-model.number="editQOValue"
              min="1"
              type="number"
              class="w-14 rounded border border-purple-300 px-1 text-sm"
            />
            <button @click="saveQualityObjective" class="text-xs text-purple-700">OK</button>
          </div>
          <div v-else class="mt-1 flex gap-2">
            <p class="text-sm font-semibold text-purple-700">
              {{ formation.qualityObjective ?? 7 }}
            </p>
            <button v-if="canEditQuality" @click="openEditQuality" class="text-xs text-purple-600">
              Modifier
            </button>
          </div>
        </div>
      </div>

      <!-- CHART -->
      <div class="rounded-xl border border-gray-200 bg-white p-4 shadow-sm">
        <h2 class="mb-3 text-sm font-semibold">Diagramme de suivi</h2>
        <div style="position: relative; height: 300px">
          <canvas ref="chartCanvas"></canvas>
        </div>
      </div>

      <div class="rounded-xl border border-gray-200 bg-white shadow-sm">
        <div class="flex flex-wrap items-center justify-between gap-3 border-b border-gray-100 p-4">
          <div>
            <h2 class="text-sm font-semibold">Saisie des donnees (J1 - J12)</h2>
            <p class="mt-1 text-xs text-gray-500">
              Chef d'équipe : cadence - Agent qualité : défauts
            </p>
          </div>
          <div class="flex items-center gap-2">
            <span class="text-xs text-gray-400">{{ completeDays }} / 12 jours complets</span>
            <button
              v-if="canContribute && hasDirty"
              @click="saveAll"
              :disabled="saving"
              class="rounded-lg bg-emerald-600 px-3 py-1.5 text-xs text-white disabled:opacity-50"
            >
              Enregistrer
            </button>
            <button
              v-if="canContribute"
              @click="evaluate"
              :disabled="evaluating || !readyToEvaluate"
              class="rounded-lg bg-amber-500 px-3 py-1.5 text-xs text-white disabled:opacity-50"
            >
              Auto-evaluer
            </button>
          </div>
        </div>
        <div class="overflow-x-auto">
          <table class="w-full text-sm">
            <thead class="bg-gray-50">
              <tr>
                <th class="w-48 px-3 py-2 text-left text-xs font-medium text-gray-500">
                  Indicateur
                </th>
                <th
                  v-for="day in 12"
                  :key="day"
                  class="w-16 px-1 py-2 text-center text-xs font-medium"
                >
                  J{{ day }}
                </th>
                <th class="w-20 bg-gray-100 px-3 py-2 text-center text-xs">Moy.</th>
                <th class="w-16 bg-gray-100 px-3 py-2 text-center text-xs">Total</th>
              </tr>
            </thead>
            <tbody>
              <tr class="bg-teal-50/50">
                <td class="px-3 py-2 text-xs font-medium text-teal-700">Cadence objectif</td>
                <td
                  v-for="day in 12"
                  :key="'target' + day"
                  class="px-1 py-2 text-center text-xs text-teal-700"
                >
                  {{ formation.targetCadence ?? '-' }}
                </td>
                <td class="bg-gray-100 px-3 py-2 text-center text-xs font-bold">
                  {{ formation.targetCadence ?? '-' }}
                </td>
                <td class="bg-gray-100 px-3 py-2 text-center text-xs">-</td>
              </tr>
              <tr>
                <td class="px-3 py-2 text-xs font-medium">Cadence realisee</td>
                <td v-for="day in 12" :key="'cadence' + day" class="px-1 py-2 text-center">
                  <input
                    v-if="canEditCadence && canEdit"
                    v-model.number="dayData[day].cadence"
                    min="0"
                    type="number"
                    class="w-14 rounded border border-emerald-300 py-1 text-center text-xs"
                    @input="dirtyDays.add(day)"
                  />
                  <span
                    v-else
                    :title="
                      dayData[day].cadenceSubmittedBy
                        ? 'Saisi par ' + dayData[day].cadenceSubmittedBy
                        : ''
                    "
                    class="text-xs font-semibold"
                    :class="getCadenceColor(dayData[day].cadence)"
                    >{{ dayData[day].cadence ?? '-' }}</span
                  >
                </td>
                <td
                  class="bg-gray-100 px-3 py-2 text-center text-xs font-bold"
                  :class="cadenceColor"
                >
                  {{ averageCadence }}
                </td>
                <td class="bg-gray-100 px-3 py-2 text-center text-xs">-</td>
              </tr>
              <tr>
                <td class="px-3 py-2 text-xs font-medium">Nombre de défauts</td>
                <td v-for="day in 12" :key="'defects' + day" class="px-1 py-2 text-center">
                  <input
                    v-if="canEditDefects && canEdit"
                    v-model.number="dayData[day].defects"
                    min="0"
                    type="number"
                    class="w-14 rounded border border-purple-300 py-1 text-center text-xs"
                    @input="dirtyDays.add(day)"
                  />
                  <span
                    v-else
                    :title="
                      dayData[day].defectsSubmittedBy
                        ? 'Saisi par ' + dayData[day].defectsSubmittedBy
                        : ''
                    "
                    class="text-xs font-semibold"
                    :class="(dayData[day].defects ?? 0) > 0 ? 'text-red-600' : 'text-emerald-600'"
                    >{{ dayData[day].defects ?? '-' }}</span
                  >
                </td>
                <td class="bg-gray-100 px-3 py-2 text-center text-xs">-</td>
                <td
                  class="bg-gray-100 px-3 py-2 text-center text-xs font-bold"
                  :class="defectsColor"
                >
                  {{ totalDefects }}
                </td>
              </tr>
              <tr>
                <td colspan="14" class="bg-blue-50/30 px-3 py-2 text-xs text-blue-700">
                  L'évaluation est automatique uniquement lorsque les deux mesures sont presentes
                  pour les 12 jours.
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Auto-evaluation banner: shows when formation is COMPLETED and pending eval exists -->
        <div
          v-if="formation.status === 'COMPLETED' && pendingEvalForThisOperator.length > 0"
          class="mt-4 mx-4 mb-4 bg-emerald-50 border border-emerald-300 rounded-xl p-5"
        >
          <div class="flex items-center justify-between">
            <div>
              <h3 class="font-bold text-emerald-800 flex items-center gap-2">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"
                  ></path>
                </svg>
                Suivi réussi - Évaluation disponible
              </h3>
              <p class="text-sm text-emerald-700 mt-1">
                Cet operateur a réussi le suivi 12 jours. Lancez l'évaluation pour determiner le
                niveau de polyvalence.
              </p>
            </div>
            <button
              @click="goToEvaluation"
              :disabled="goingToEvaluation"
              class="bg-emerald-600 text-white px-5 py-2.5 rounded-lg hover:bg-emerald-700 font-medium disabled:opacity-50"
            >
              {{ goingToEvaluation ? 'Chargement...' : "Passer a l'évaluation" }}
            </button>
          </div>
        </div>

        <!-- Already evaluated banner -->
        <div
          v-if="
            formation.status === 'COMPLETED' &&
            pendingEvalForThisOperator.length === 0 &&
            hasCheckedPending
          "
          class="mt-4 mx-4 mb-4 bg-blue-50 border border-blue-200 rounded-xl p-4"
        >
          <p class="text-sm text-blue-700 font-medium">
            Évaluation déjà effectuee pour cette formation.
          </p>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, onBeforeUnmount, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  LineController,
  Filler,
  Title,
  Tooltip,
  Legend,
} from 'chart.js'
import { trainingApi, evaluationApi } from '@/api/endpoints'
import { useAuthStore } from '@/stores/auth'

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  LineController,
  Filler,
  Title,
  Tooltip,
  Legend,
)

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const formation = ref(null)
const loading = ref(true)
const saving = ref(false)
const evaluating = ref(false)
const editingQO = ref(false)
const editQOValue = ref(7)
const evalResult = ref(null)
const error = ref('')
const dirtyDays = reactive(new Set())
const dayData = reactive({})
const chartCanvas = ref(null)
const pendingEvalForThisOperator = ref([])
const hasCheckedPending = ref(false)
let chartInstance = null
for (let day = 1; day <= 12; day++)
  dayData[day] = {
    cadence: null,
    defects: null,
    cadenceSubmittedBy: null,
    defectsSubmittedBy: null,
  }

const canEditCadence = computed(() => authStore.isChefEquipe)
const canEditDefects = computed(() => authStore.isAgentQualite)
const canEditQuality = computed(() => authStore.isAgentQualite)
const openEditQuality = () => {
  editingQO.value = true
  editQOValue.value = formation.value?.qualityObjective ?? 7
}
const canContribute = computed(() => canEditCadence.value || canEditDefects.value)
const canEdit = computed(() => formation.value?.status === 'IN_PROGRESS' || formation.value?.status === 'NOT_STARTED' || formation.value?.status === 'COMPLETED')
const hasDirty = computed(() => dirtyDays.size > 0)
const completeDays = computed(
  () => Object.values(dayData).filter((day) => day.cadence !== null && day.defects !== null).length,
)
const readyToEvaluate = computed(() => completeDays.value === 12)
const averageCadence = computed(() => {
  const values = Object.values(dayData)
    .map((day) => day.cadence)
    .filter((value) => value !== null)
  return values.length
    ? (values.reduce((sum, value) => sum + value, 0) / values.length).toFixed(1)
    : '-'
})
const totalDefects = computed(() =>
  Object.values(dayData).reduce((sum, day) => sum + (day.defects ?? 0), 0),
)
const cadenceColor = computed(() =>
  averageCadence.value !== '-' && Number(averageCadence.value) >= formation.value?.targetCadence
    ? 'text-emerald-600'
    : 'text-red-600',
)
const defectsColor = computed(() =>
  totalDefects.value < (formation.value?.qualityObjective ?? 7)
    ? 'text-emerald-600'
    : 'text-red-600',
)
const statusLabel = (status) =>
  ({ IN_PROGRESS: 'En cours', COMPLETED: 'Réussie', FAILED: 'Échouée' })[status] || status
const statusClass = (status) =>
  ({
    IN_PROGRESS: 'bg-amber-100 text-amber-700',
    COMPLETED: 'bg-emerald-100 text-emerald-700',
    FAILED: 'bg-red-100 text-red-700',
  })[status] || 'bg-gray-100 text-gray-600'
const getCadenceColor = (value) =>
  value !== null && formation.value?.targetCadence && value >= formation.value.targetCadence
    ? 'text-emerald-600'
    : 'text-red-600'

const renderChart = () => {
  if (!chartCanvas.value || !formation.value) return
  if (chartInstance) chartInstance.destroy()
  const target = formation.value.targetCadence ?? 0
  const labels = []
  const targetData = []
  const cadenceData = []
  const defectsData = []
  for (let i = 1; i <= 12; i++) {
    labels.push('J' + i)
    targetData.push(target)
    cadenceData.push(dayData[i]?.cadence ?? null)
    defectsData.push(dayData[i]?.defects ?? null)
  }
  chartInstance = new ChartJS(chartCanvas.value, {
    type: 'line',
    data: {
      labels,
      datasets: [
        {
          label: 'Cadence Objectif',
          data: targetData,
          type: 'line',
          borderColor: '#f59e0b',
          borderDash: [5, 5],
          pointRadius: 0,
        },
        {
          label: 'Cadence Realisee',
          data: cadenceData,
          type: 'line',
          borderColor: '#10b981',
          backgroundColor: 'rgba(16,185,129,0.1)',
          fill: true,
          tension: 0.4,
          spanGaps: true,
        },
        {
          label: 'Defauts',
          data: defectsData,
          type: 'line',
          yAxisID: 'y1',
          borderColor: '#ef4444',
          backgroundColor: 'rgba(239,68,68,0.1)',
          tension: 0.4,
        },
      ],
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      interaction: { mode: 'index', intersect: false },
      plugins: { legend: { position: 'top' } },
      scales: {
        y: { beginAtZero: true, title: { display: true, text: 'Cadence' } },
        y1: {
          type: 'linear',
          position: 'right',
          beginAtZero: true,
          title: { display: true, text: 'Defauts' },
          grid: { drawOnChartArea: false },
        },
      },
    },
  })
}

const refreshFormation = async () => {
  try {
    formation.value = (await trainingApi.getFormationDetail(route.params.id)).data
  } catch (e) {
    console.error('Formation refresh error', e)
  }
}

const checkPendingEvaluation = async () => {
  if (!formation.value?.operatorId) return
  hasCheckedPending.value = true
  try {
    const res = await evaluationApi.getPendingForOperator(formation.value.operatorId)
    pendingEvalForThisOperator.value = (res.data || []).filter(
      (pe) => pe.formationId === Number(route.params.id),
    )
  } catch (e) {
    console.error('Error checking pending eval', e)
  }
}

const goingToEvaluation = ref(false)

// FIX 1: previously this just did router.push('/evaluation/initial'), dumping the
// user on the generic "new hire" picker screen and losing all context about which
// formation/operator we're evaluating (including recyclage / second-chance retries,
// which have no marker distinguishing them from a first-time formation).
// Now it runs the same resolveTemplates -> startEvaluation sequence EvaluationInitial.vue
// uses, and jumps straight into the evaluation session.
const goToEvaluation = async () => {
  if (!formation.value) return
  goingToEvaluation.value = true
  error.value = ''
  try {
    const resolveRes = await evaluationApi.resolveTemplates(
      formation.value.operatorId,
      route.params.id,
    )
    const data = resolveRes.data

    let templateId, nextTemplateId
    if (data.startWithProduction) {
      // Already passed generic, go straight to production
      templateId = data.productionTemplateId
      nextTemplateId = null
    } else {
      // Start with generic, link to production via nextTemplateId
      templateId = data.genericTemplateId
      nextTemplateId = data.productionTemplateId
    }

    const startRes = await evaluationApi.startEvaluation({
      operatorId: formation.value.operatorId,
      templateId,
      formationId: Number(route.params.id),
      mode: 'INITIAL',
      nextTemplateId,
    })

    router.push({ name: 'evaluation-session', params: { id: startRes.data.sessionId } })
  } catch (requestError) {
    error.value =
      requestError.response?.data?.message || "Impossible de lancer l'évaluation."
  } finally {
    goingToEvaluation.value = false
  }
}

const load = async () => {
  const [detailRes, trackingRes] = await Promise.allSettled([
    trainingApi.getFormationDetail(route.params.id),
    trainingApi.getTracking(route.params.id),
  ])
  if (detailRes.status === 'fulfilled') formation.value = detailRes.value.data
  for (let day = 1; day <= 12; day++)
    dayData[day] = {
      cadence: null,
      defects: null,
      cadenceSubmittedBy: null,
      defectsSubmittedBy: null,
    }
  if (trackingRes.status === 'fulfilled') {
    for (const t of trackingRes.value.data) {
      if (t.dayNumber >= 1 && t.dayNumber <= 12) {
        dayData[t.dayNumber] = {
          cadence: t.actualCadence,
          defects: t.defects,
          cadenceSubmittedBy: t.cadenceSubmittedBy,
          defectsSubmittedBy: t.defectsSubmittedBy,
        }
      }
    }
  } else {
    console.error('Tracking load failed:', trackingRes.reason)
  }
  // Auto-trigger: check pending evaluations when formation is COMPLETED
  if (formation.value?.status === 'COMPLETED') {
    await checkPendingEvaluation()
  }
}

const saveAll = async () => {
  saving.value = true
  error.value = ''
  try {
    const toIntOrNull = (v) =>
      v === '' || v === null || v === undefined || Number.isNaN(v) ? null : v
    const days = [...dirtyDays].map((dayNumber) => ({
      dayNumber,
      trackingDate: new Date().toISOString().slice(0, 10),
      ...(canEditCadence.value ? { actualCadence: toIntOrNull(dayData[dayNumber].cadence) } : {}),
      ...(canEditDefects.value ? { defects: toIntOrNull(dayData[dayNumber].defects) } : {}),
    }))
    await trainingApi.batchSave(route.params.id, days)
    for (const dayNumber of dirtyDays) {
      if (canEditCadence.value)
        dayData[dayNumber].cadenceSubmittedBy = authStore.user?.username || null
      if (canEditDefects.value)
        dayData[dayNumber].defectsSubmittedBy = authStore.user?.username || null
    }
    dirtyDays.clear()
    await refreshFormation()
    renderChart()
  } catch (requestError) {
    error.value = requestError.response?.data?.message || "Impossible d'enregistrer le suivi."
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
    renderChart()
  } catch (requestError) {
    error.value = requestError.response?.data?.message || "Impossible d'evaluer la formation."
  } finally {
    evaluating.value = false
  }
}

const saveQualityObjective = async () => {
  try {
    await trainingApi.setQualityObjective(route.params.id, editQOValue.value)
    editingQO.value = false
    await refreshFormation()
  } catch (requestError) {
    error.value =
      requestError.response?.data?.message || "Impossible de mettre a jour l'objectif qualité."
  }
}

// FIX 1b: Watch for route changes (e.g. back/forward navigation) to reload and re-render chart
watch(() => route.params.id, async (newId) => {
  if (newId) {
    loading.value = true
    error.value = ''
    dirtyDays.clear()
    hasCheckedPending.value = false
    pendingEvalForThisOperator.value = []
    try {
      await load()
    } catch (requestError) {
      error.value = requestError.response?.data?.message || 'Impossible de charger la formation.'
    } finally {
      loading.value = false
      await nextTick()
      renderChart()
    }
  }
})

onBeforeUnmount(() => {
  if (chartInstance) chartInstance.destroy()
})

onMounted(async () => {
  try {
    await load()
  } catch (requestError) {
    error.value = requestError.response?.data?.message || 'Impossible de charger la formation.'
  } finally {
    loading.value = false
    await nextTick()
    renderChart()
  }
})
</script>