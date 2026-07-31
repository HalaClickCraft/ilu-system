<template>
  <div class="space-y-6">
    <div v-if="loading" class="flex items-center justify-center py-20"><div class="w-8 h-8 border-4 border-emerald-200 border-t-emerald-600 rounded-full animate-spin"></div></div>
    <template v-else-if="formation">
      <!-- HEADER -->
      <div class="flex items-center gap-4">
        <button @click="$router.push('/training')" class="text-gray-400 hover:text-gray-600"><svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"></path></svg></button>
        <h1 class="text-2xl font-bold text-gray-900">Evaluation Formation</h1>
        <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium" :class="statusClass(formation.status)">{{ statusLabel(formation.status) }}</span>
      </div>
      <!-- INFO CARDS -->
      <div class="grid grid-cols-2 md:grid-cols-5 gap-4">
        <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-4"><p class="text-xs text-gray-500">Operateur</p><p class="font-semibold mt-1 text-sm">{{ formation.operatorName }}</p></div>
        <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-4"><p class="text-xs text-gray-500">Poste</p><p class="font-semibold mt-1 text-sm">{{ formation.workstationName }}</p></div>
        <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-4"><p class="text-xs text-gray-500">Cadence Objectif</p><p class="font-semibold mt-1 text-sm">{{ formation.targetCadence ?? '-' }} /j</p></div>
        <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-4"><p class="text-xs text-gray-500">Moy. Cadence</p><p class="font-semibold mt-1 text-sm" :class="cadenceColor">{{ avgCadence }}</p></div>
        <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-4"><p class="text-xs text-gray-500">Defauts Total</p><p class="font-semibold mt-1 text-sm" :class="defectsColor">{{ totalDefects }} / {{ formation.qualityObjective ?? 7 }}</p></div>
      </div>

      <!-- CHART -->
      <div v-if="chartReady" class="bg-white rounded-xl shadow-sm border border-gray-200 p-4">
        <h2 class="font-semibold text-gray-900 mb-3 text-sm">Diagramme de Suivi</h2>
        <div style="position:relative; height:300px;">
          <Line :data="chartJsData" :options="chartJsOptions" />
        </div>
      </div>

      <!-- 12-DAY TABLE -->
      <div class="bg-white rounded-xl shadow-sm border border-gray-200">
        <div class="p-4 border-b border-gray-100 flex items-center justify-between">
          <h2 class="font-semibold text-gray-900 text-sm">Saisie des donnees (J1 - J12)</h2>
          <div class="flex gap-2">
            <span class="text-xs text-gray-400">{{ savedDaysCount }} / 12 jours saisis</span>
            <button v-if="canEdit && hasDirty" @click="saveAll" :disabled="saving" class="px-3 py-1.5 bg-emerald-600 hover:bg-emerald-700 text-white text-xs rounded-lg disabled:opacity-50">
              <span v-if="saving" class="inline-block w-3 h-3 border-2 border-white border-t-transparent rounded-full animate-spin mr-1"></span>
              Enregistrer Tout
            </button>
            <button v-if="canEdit" @click="evaluate" :disabled="evaluating" class="px-3 py-1.5 bg-amber-500 hover:bg-amber-600 text-white text-xs rounded-lg disabled:opacity-50">
              <span v-if="evaluating" class="inline-block w-3 h-3 border-2 border-white border-t-transparent rounded-full animate-spin mr-1"></span>
              Auto-Evaluer
            </button>
          </div>
        </div>
        <div class="overflow-x-auto">
          <table class="w-full text-sm">
            <thead class="bg-gray-50"><tr>
              <th class="text-left py-2 px-3 font-medium text-gray-500 text-xs w-48">Indicateur</th>
              <th v-for="i in 12" :key="i" class="text-center py-2 px-1 font-medium text-xs w-16" :class="dayData[i]?.saved ? 'bg-gray-50' : 'bg-teal-50'">J{{ i }}</th>
              <th class="text-center py-2 px-3 font-medium text-xs bg-gray-100 w-20">Moyenne</th>
              <th class="text-center py-2 px-3 font-medium text-xs bg-gray-100 w-16">Total</th>
            </tr></thead>
            <tbody>
              <tr class="bg-teal-50/50">
                <td class="py-2 px-3 text-xs font-medium text-teal-700">Cadence objectif du poste</td>
                <td v-for="i in 12" :key="'tc'+i" class="text-center py-2 px-1 text-xs text-teal-700">{{ formation.targetCadence ?? '-' }}</td>
                <td class="text-center py-2 px-3 text-xs font-bold bg-gray-100">{{ formation.targetCadence ?? '-' }}</td>
                <td class="text-center py-2 px-3 text-xs bg-gray-100">-</td>
              </tr>
              <tr>
                <td class="py-2 px-3 text-xs font-medium text-gray-700">Cadence realisee</td>
                <td v-for="i in 12" :key="'ac'+i" class="text-center py-2 px-1">
                  <span v-if="dayData[i]?.saved && !dirtyDays.has(i)" class="text-xs font-semibold" :class="getCadenceColor(dayData[i].cadence)">{{ dayData[i].cadence }}</span>
                  <input v-else v-model.number="dayData[i].cadence" type="number" min="0" class="w-14 text-center text-xs border border-emerald-300 rounded py-1 focus:ring-2 focus:ring-emerald-500 outline-none bg-white" @input="dirtyDays.add(i)" />
                </td>
                <td class="text-center py-2 px-3 text-xs font-bold bg-gray-100" :class="cadenceColor">{{ avgCadence }}</td>
                <td class="text-center py-2 px-3 text-xs bg-gray-100">-</td>
              </tr>
              <tr>
                <td class="py-2 px-3 text-xs font-medium text-gray-700">Nbr de defauts</td>
                <td v-for="i in 12" :key="'df'+i" class="text-center py-2 px-1">
                  <span v-if="dayData[i]?.saved && !dirtyDays.has(i)" class="text-xs font-medium" :class="(dayData[i].defects || 0) > 0 ? 'text-red-600' : 'text-emerald-600'">{{ dayData[i].defects }}</span>
                  <input v-else v-model.number="dayData[i].defects" type="number" min="0" class="w-14 text-center text-xs border border-emerald-300 rounded py-1 focus:ring-2 focus:ring-emerald-500 outline-none bg-white" @input="dirtyDays.add(i)" />
                </td>
                <td class="text-center py-2 px-3 text-xs bg-gray-100">-</td>
                <td class="text-center py-2 px-3 text-xs font-bold bg-gray-100" :class="defectsColor">{{ totalDefects }}</td>
              </tr>
              <tr>
                <td colspan="14" class="py-2 px-3 text-xs text-blue-700 bg-blue-50/30">
                  Objectif qualite: nombre de defauts &lt; {{ formation.qualityObjective ?? 7 }} sur une periode de 12 jours
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <!-- EVAL RESULT -->
        <div v-if="evalResult" class="p-4 border-t" :class="evalResult.passed ? 'bg-emerald-50 border-emerald-200' : 'bg-red-50 border-red-200'">
          <div class="text-center">
            <h3 :class="evalResult.passed ? 'text-emerald-700' : 'text-red-700'" class="text-lg font-bold">
              {{ evalResult.passed ? 'FORMATION VALIDEE' : 'FORMATION ECHOUEE' }}
            </h3>
            <div class="flex justify-center gap-8 mt-3">
              <div>
                <div class="text-xs text-gray-500">Moyenne Cadence</div>
                <div class="font-bold" :class="evalResult.passedCadence ? 'text-emerald-600' : 'text-red-600'">
                  {{ evalResult.averageCadence }} / {{ evalResult.targetCadence }}
                </div>
              </div>
              <div>
                <div class="text-xs text-gray-500">Total Defauts</div>
                <div class="font-bold" :class="evalResult.passedQuality ? 'text-emerald-600' : 'text-red-600'">
                  {{ evalResult.totalDefects }} / {{ evalResult.qualityObjective }}
                </div>
              </div>
              <div>
                <div class="text-xs text-gray-500">Jours Saisis</div>
                <div class="font-bold">{{ evalResult.daysWithData }}</div>
              </div>
            </div>
          </div>
        </div>
        <!-- RESET for FAILED -->
        <div v-if="formation.status === 'FAILED'" class="p-4 border-t border-red-100 flex justify-between items-center bg-red-50/50">
          <span class="text-sm text-red-700 font-medium">Formation echouee. Moy. cadence ou defauts insuffisants.</span>
          <button @click="resetFormation" class="px-4 py-2 bg-amber-500 hover:bg-amber-600 text-white text-sm rounded-lg">Recommencer la formation</button>
        </div>
      </div>
    </template>
  </div>
</template>
<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { trainingApi } from '@/api/endpoints'
import { Line } from 'vue-chartjs'
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  BarElement,
  Title,
  Tooltip,
  Legend,
} from 'chart.js'

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, BarElement, Title, Tooltip, Legend)

const route = useRoute()
const formation = ref(null)
const tracking = ref([])
const loading = ref(true)
const saving = ref(false)
const evaluating = ref(false)
const evalResult = ref(null)
const chartReady = ref(false)
const chartJsData = ref(null)
const dirtyDays = reactive(new Set())

// 1-indexed: dayData[1] through dayData[12]
const dayData = reactive({})
for (let i = 1; i <= 12; i++) {
  dayData[i] = { cadence: null, defects: 0, saved: false }
}

const statusLabel = (s) => ({ IN_PROGRESS: 'En Cours', COMPLETED: 'Reussie', FAILED: 'Echouee' })[s] || s
const statusClass = (s) => ({ IN_PROGRESS: 'bg-amber-100 text-amber-700', COMPLETED: 'bg-emerald-100 text-emerald-700', FAILED: 'bg-red-100 text-red-700' })[s] || 'bg-gray-100 text-gray-600'

const canEdit = computed(() => formation.value?.status === 'IN_PROGRESS')
const hasDirty = computed(() => dirtyDays.size > 0)

const savedDaysCount = computed(() => {
  let c = 0
  for (let i = 1; i <= 12; i++) { if (dayData[i]?.saved) c++ }
  return c
})

const getCadenceColor = (val) => {
  if (val == null || !formation.value?.targetCadence) return ''
  return val >= formation.value.targetCadence ? 'text-emerald-600' : 'text-red-600'
}

const avgCadence = computed(() => {
  let sum = 0, count = 0
  for (let i = 1; i <= 12; i++) {
    const v = dayData[i]?.cadence
    if (v != null && v > 0) { sum += v; count++ }
  }
  return count > 0 ? (sum / count).toFixed(1) : '-'
})

const totalDefects = computed(() => {
  let t = 0
  for (let i = 1; i <= 12; i++) { t += (dayData[i]?.defects || 0) }
  return t
})

const cadenceColor = computed(() => {
  if (!formation.value?.targetCadence || avgCadence.value === '-') return ''
  return parseFloat(avgCadence.value) >= formation.value.targetCadence ? 'text-emerald-600' : 'text-red-600'
})

const defectsColor = computed(() => totalDefects.value >= (formation.value?.qualityObjective ?? 7) ? 'text-red-600' : 'text-emerald-600')

// Chart
const chartJsOptions = {
  responsive: true,
  maintainAspectRatio: false,
  interaction: { mode: 'index', intersect: false },
  plugins: { legend: { position: 'top' } },
  scales: {
    y: { beginAtZero: true, title: { display: true, text: 'Cadence (pieces/jour)' } },
    y1: { type: 'linear', position: 'right', beginAtZero: true, title: { display: true, text: 'Defauts' }, grid: { drawOnChartArea: false } },
    x: { title: { display: true, text: 'Jour' } },
  },
}

const buildChart = async () => {
  try {
    const res = await trainingApi.getChartData(route.params.id)
    const raw = res.data
    chartJsData.value = {
      labels: raw.labels,
      datasets: [
        { ...raw.targetCadenceDataset },
        { ...raw.achievedCadenceDataset, fill: true },
        { ...raw.defectsDataset, type: 'bar', yAxisID: 'y1' },
      ],
    }
    chartReady.value = true
  } catch (e) { console.error('Chart error', e) }
}

const saveAll = async () => {
  saving.value = true
  try {
    const days = []
    for (const dayNum of dirtyDays) {
      days.push({
        dayNumber: dayNum,
        actualCadence: dayData[dayNum].cadence,
        defects: dayData[dayNum].defects || 0,
        trackingDate: new Date().toISOString().split('T')[0],
      })
    }
    if (days.length === 0) return
    await trainingApi.batchSave(route.params.id, days)
    dirtyDays.clear()
    await fetchData()
  } catch (e) { console.error(e) } finally { saving.value = false }
}

const evaluate = async () => {
  evaluating.value = true
  try {
    // Save dirty days first
    if (hasDirty.value) await saveAll()
    const res = await trainingApi.autoEvaluate(route.params.id)
    evalResult.value = res.data
    await fetchData()
  } catch (e) { console.error(e) } finally { evaluating.value = false }
}

const resetFormation = async () => {
  try {
    await trainingApi.resetFormation(route.params.id)
    evalResult.value = null
    dirtyDays.clear()
    for (let i = 1; i <= 12; i++) { dayData[i] = { cadence: null, defects: 0, saved: false } }
    await fetchData()
  } catch (e) { console.error(e) }
}

const fetchData = async () => {
  try {
    const [detail, track] = await Promise.all([
      trainingApi.getFormationDetail(route.params.id),
      trainingApi.getTracking(route.params.id),
    ])
    formation.value = detail.data
    tracking.value = track.data
    evalResult.value = null

    // Populate dayData from tracking
    for (let i = 1; i <= 12; i++) { dayData[i] = { cadence: null, defects: 0, saved: false } }
    for (const t of tracking.value) {
      if (t.dayNumber >= 1 && t.dayNumber <= 12) {
        dayData[t.dayNumber] = { cadence: t.actualCadence, defects: t.defects || 0, saved: true }
      }
    }

    // Build chart
    await buildChart()
  } catch (e) { console.error(e) }
}

onMounted(async () => { await fetchData(); loading.value = false })
</script>