<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">Calendrier de Recyclage</h1>
        <p class="text-sm text-gray-500 mt-1">Vue mensuelle des evaluations planifiees</p>
      </div>
      <div class="flex items-center gap-3">
        <select v-model="selectedProject" @change="loadCalendar(false)" class="border border-gray-300 rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-emerald-500">
          <option :value="null">Tous les projets</option>
          <option v-for="p in projectList" :key="p.id" :value="p.id">{{ p.name }}</option>
        </select>
      </div>
    </div>

    <!-- Month Navigation -->
    <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-4">
      <div class="flex items-center justify-between">
        <button @click="prevMonth" class="p-2 rounded-lg hover:bg-gray-100 transition">
          <svg class="w-5 h-5 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"/></svg>
        </button>
        <h2 class="text-lg font-semibold text-gray-900">
          {{ monthNames[currentMonth] }} {{ currentYear }}
        </h2>
        <div class="flex items-center gap-1">
          <button @click="goToday" class="px-3 py-1.5 rounded-lg text-xs font-medium text-gray-600 hover:bg-gray-100 border border-gray-200 mr-1">Aujourd'hui</button>
          <button @click="nextMonth" class="p-2 rounded-lg hover:bg-gray-100 transition">
            <svg class="w-5 h-5 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"/></svg>
          </button>
        </div>
      </div>
      <!-- Calendar error / loading -->
      <div v-if="loadError" class="bg-red-50 border border-red-200 rounded-lg p-3 mb-2 flex items-center justify-between gap-3">
        <p class="text-sm text-red-800">Erreur de chargement du calendrier.</p>
        <button @click="loadCalendar(false)" class="text-sm text-red-700 underline font-medium">Reessayer</button>
      </div>
      <div v-if="loading" class="text-center text-sm text-gray-400 py-2">Chargement...</div>
      <!-- Empty month message -->
      <div v-if="!loading && !loadError && events.length === 0" class="text-center py-3">
        <p class="text-sm text-gray-400">Aucune planification pour {{ monthNames[currentMonth] }} {{ currentYear }}</p>
      </div>
    </div>

    <!-- Calendar Grid -->
    <div class="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
      <!-- Day Headers -->
      <div class="grid grid-cols-7 bg-gray-50 border-b border-gray-200">
        <div v-for="d in dayNames" :key="d" class="px-2 py-3 text-center text-xs font-semibold text-gray-500 uppercase">{{ d }}</div>
      </div>
      <!-- Calendar Days -->
      <div class="grid grid-cols-7">
        <div
          v-for="(cell, idx) in calendarCells"
          :key="idx"
          :class="cell.isCurrentMonth ? 'bg-white' : 'bg-gray-50'"
          class="min-h-[100px] border-r border-b border-gray-100 p-1"
        >
          <span
            :class="{
              'text-gray-400': !cell.isCurrentMonth,
              'text-gray-900': cell.isCurrentMonth,
              'bg-emerald-600 text-white rounded-full w-6 h-6 flex items-center justify-center text-xs font-bold': cell.isToday
            }"
            class="text-xs font-medium px-1"
          >{{ cell.day }}</span>
          <div class="mt-1 space-y-0.5">
            <div
              v-for="event in cell.events"
              :key="event.id"
              :class="eventColor(event)"
              class="text-[10px] px-1.5 py-0.5 rounded truncate cursor-pointer hover:opacity-80"
              @click="openDetail(event)"
            >
              {{ event.operatorName }}
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Legend -->
    <div class="flex flex-wrap gap-4 text-xs">
      <div class="flex items-center gap-1.5"><span class="w-3 h-3 rounded bg-blue-500"></span> Initiale</div>
      <div class="flex items-center gap-1.5"><span class="w-3 h-3 rounded bg-orange-400"></span> Recyclage a venir</div>
      <div class="flex items-center gap-1.5"><span class="w-3 h-3 rounded bg-red-500"></span> Recyclage urgent</div>
      <div class="flex items-center gap-1.5"><span class="w-3 h-3 rounded bg-green-500"></span> Terminee</div>
      <div class="flex items-center gap-1.5"><span class="w-3 h-3 rounded bg-gray-300"></span> Annulee</div>
    </div>

    <!-- Detail Modal -->
    <div v-if="selectedEvent" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50" @click.self="selectedEvent = null">
      <div class="bg-white rounded-xl shadow-xl w-full max-w-sm mx-4 p-6">
        <h3 class="text-lg font-bold text-gray-900 mb-4">Detail Evaluation</h3>
        <div class="space-y-3 text-sm">
          <div class="flex justify-between"><span class="text-gray-500">Operateur:</span><span class="font-medium">{{ selectedEvent.operatorName }}</span></div>
          <div class="flex justify-between"><span class="text-gray-500">Poste:</span><span class="font-medium">{{ selectedEvent.workstationName }}</span></div>
          <div class="flex justify-between"><span class="text-gray-500">Type:</span><span :class="isInitialType(selectedEvent.type) ? 'text-purple-700' : selectedEvent.type === 'EVALUATION_ANNUELLE_MOIS_1' ? 'text-blue-700' : 'text-orange-700'" class="font-medium">{{ typeLabel(selectedEvent.type) }}</span></div>
          <div class="flex justify-between"><span class="text-gray-500">Date:</span><span class="font-medium">{{ formatDate(selectedEvent.scheduledDate) }}</span></div>
          <div class="flex justify-between"><span class="text-gray-500">Statut:</span><span class="font-medium">{{ statusLabel(selectedEvent.status) }}</span></div>
          <div v-if="selectedEvent.niveauObtenu" class="flex justify-between"><span class="text-gray-500">Niveau:</span><span class="font-bold">{{ selectedEvent.niveauObtenu }}</span></div>
        </div>
        <button @click="selectedEvent = null" class="mt-6 w-full px-4 py-2 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 text-sm font-medium">Fermer</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { recyclageApi } from '@/services/recyclageApi'
import { structureApi } from '@/api/endpoints'

const events = ref([])
const projects = ref([])
const selectedProject = ref(null)
const currentMonth = ref(new Date().getMonth())
const currentYear = ref(new Date().getFullYear())
const selectedEvent = ref(null)
const loading = ref(false)
const loadError = ref(false)
const isInitialLoad = ref(true)

const projectList = computed(() => projects.value.map(p => ({ id: p.id, name: p.name })).sort((a, b) => a.name.localeCompare(b.name)))

watch(selectedProject, () => {
  loadCalendar(isInitialLoad.value)
  isInitialLoad.value = false
})

const monthNames = ['Janvier','Fevrier','Mars','Avril','Mai','Juin','Juillet','Aout','Septembre','Octobre','Novembre','Decembre']
const dayNames = ['Lun','Mar','Mer','Jeu','Ven','Sam','Dim']

const calendarCells = computed(() => {
  const y = currentYear.value
  const m = currentMonth.value
  const firstDay = new Date(y, m, 1)
  let startDow = firstDay.getDay() - 1
  if (startDow < 0) startDow = 6
  const daysInMonth = new Date(y, m + 1, 0).getDate()
  const daysInPrev = new Date(y, m, 0).getDate()
  const today = new Date()
  const cells = []

  const eventMap = new Map()
  for (const e of events.value) {
    const d = parsePlanningDate(e.scheduledDate)
    if (!isFinite(d.getTime())) continue
    const key = `${d.getFullYear()}-${d.getMonth()}-${d.getDate()}`
    if (!eventMap.has(key)) eventMap.set(key, [])
    eventMap.get(key).push(e)
  }

  for (let i = 0; i < startDow; i++) {
    const day = daysInPrev - startDow + i + 1
    cells.push({ day, isCurrentMonth: false, events: [], isToday: false })
  }

  for (let d = 1; d <= daysInMonth; d++) {
    const key = `${y}-${m}-${d}`
    const isToday = today.getFullYear() === y && today.getMonth() === m && today.getDate() === d
    cells.push({ day: d, isCurrentMonth: true, events: eventMap.get(key) || [], isToday })
  }

  const remaining = 42 - cells.length
  for (let d = 1; d <= remaining; d++) {
    cells.push({ day: d, isCurrentMonth: false, events: [], isToday: false })
  }

  return cells
})

function eventColor(event) {
  if (event.status === 'TERMINEE') return 'bg-green-100 text-green-800'
  if (event.status === 'ANNULEE') return 'bg-gray-200 text-gray-500'
  if (isInitialType(event.type)) return 'bg-blue-100 text-blue-800'
  if (event.type === 'EVALUATION_ANNUELLE_MOIS_1') return 'bg-violet-100 text-violet-800'
  const today = new Date()
  const eventDate = parsePlanningDate(event.scheduledDate)
  const diff = Math.ceil((eventDate - today) / (1000 * 60 * 60 * 24))
  if (diff <= 15) return 'bg-red-100 text-red-800'
  return 'bg-orange-100 text-orange-800'
}

function isInitialType(type) {
  return type === 'INITIALE_NOUVELLE_RECRUE' || type === 'INITIALE'
}

function typeLabel(type) {
  return {
    INITIALE_NOUVELLE_RECRUE: 'Initiale nouvelle recrue',
    EVALUATION_ANNUELLE_MOIS_1: 'Evaluation annuelle',
    INITIALE: 'Initiale (ancien)',
    RECYCLAGE: 'Recyclage',
  }[type] || type
}

function prevMonth() {
  if (currentMonth.value === 0) { currentMonth.value = 11; currentYear.value-- }
  else currentMonth.value--
  loadCalendar(false)
}

function nextMonth() {
  if (currentMonth.value === 11) { currentMonth.value = 0; currentYear.value++ }
  else currentMonth.value++
  loadCalendar(false)
}

function goToday() {
  const now = new Date()
  currentMonth.value = now.getMonth()
  currentYear.value = now.getFullYear()
  loadCalendar(false)
}

function statusLabel(s) {
  return { PLANIFIEE: 'Planifiee', EN_COURS: 'En cours', TERMINEE: 'Terminee', ANNULEE: 'Annulee' }[s] || s
}

async function loadCalendar(allowAutoNavigate = false) {
  loading.value = true
  loadError.value = false
  try {
    const params = { month: currentMonth.value + 1, year: currentYear.value }
    if (selectedProject.value) params.projectId = selectedProject.value
    const res = await recyclageApi.getCalendar(params)
    events.value = res.data || []

    // Auto-navigate ONLY on initial page load (allowAutoNavigate=true)
    // When user manually clicks arrows, they want to see THAT month.
    if (allowAutoNavigate && events.value.length === 0) {
      try {
        const upcomingRes = await recyclageApi.getUpcoming({ daysAhead: 90 })
        const upcoming = upcomingRes.data || []
        if (upcoming.length > 0) {
          const firstDate = parsePlanningDate(upcoming[0].scheduledDate)
          if (isFinite(firstDate.getTime())) {
            const targetMonth = firstDate.getMonth()
            const targetYear = firstDate.getFullYear()
            if (targetMonth !== currentMonth.value || targetYear !== currentYear.value) {
              currentMonth.value = targetMonth
              currentYear.value = targetYear
              await loadCalendar(false) // don't auto-navigate again
              return
            }
          }
        }
      } catch (e) {
        console.error('Error checking upcoming:', e)
      }
    }
  } catch (e) {
    console.error('Error loading calendar:', e)
    loadError.value = true
    events.value = []
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

function openDetail(event) { selectedEvent.value = event }

function formatDate(d) {
  if (!d) return '-'
  return parsePlanningDate(d).toLocaleDateString('fr-FR', { day: '2-digit', month: '2-digit', year: 'numeric' })
}

function parsePlanningDate(value) {
  if (value instanceof Date) return value
  if (typeof value === 'string' && /^\d{2}\/\d{2}\/\d{4}$/.test(value)) {
    const [day, month, year] = value.split('/').map(Number)
    return new Date(year, month - 1, day)
  }
  return new Date(value)
}

onMounted(async () => {
  await loadProjects()
  await loadCalendar(true)
})
</script>
