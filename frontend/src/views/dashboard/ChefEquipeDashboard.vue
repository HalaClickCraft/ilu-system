<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">Tableau de bord Chef d'Équipe</h1>
        <p class="text-gray-500 mt-1">Suivi quotidien des operateurs et formations de la zone</p>
      </div>
      <div class="text-sm text-gray-400">{{ currentDate }}</div>
    </div>

    <!-- SECTION 1: Overview -->
    <div>
      <h2 class="text-sm font-bold uppercase tracking-wider text-slate-400 mb-3 flex items-center gap-1.5">
        <span class="w-1 h-3.5 bg-blue-600 rounded-full"></span>
        Vue d'ensemble (Overview)
      </h2>
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-5">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm font-medium text-gray-500">Opérateurs dans la Zone</p>
              <p class="text-3xl font-bold text-emerald-600 mt-1">{{ activeOperators.length }}</p>
            </div>
            <div class="w-12 h-12 bg-emerald-50 rounded-xl flex items-center justify-center">
              <svg class="w-6 h-6 text-emerald-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z"></path></svg>
            </div>
          </div>
        </div>
        <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-5">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm font-medium text-gray-500">En Integration (12j)</p>
              <p class="text-3xl font-bold text-amber-600 mt-1">{{ integrationOperators.length }}</p>
            </div>
            <div class="w-12 h-12 bg-amber-50 rounded-xl flex items-center justify-center">
              <svg class="w-6 h-6 text-amber-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
            </div>
          </div>
        </div>
        <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-5">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm font-medium text-gray-500">Niveaux ILU</p>
              <div class="flex items-center gap-2 mt-1">
                <span v-if="iluCounts.I > 0" class="text-lg font-bold text-amber-600">I:{{ iluCounts.I }}</span>
                <span v-if="iluCounts.L > 0" class="text-lg font-bold text-blue-600">L:{{ iluCounts.L }}</span>
                <span v-if="iluCounts.U > 0" class="text-lg font-bold text-green-600">U:{{ iluCounts.U }}</span>
                <span v-if="iluCounts.none > 0" class="text-lg font-bold text-gray-400">-:{{ iluCounts.none }}</span>
              </div>
            </div>
            <div class="w-12 h-12 bg-blue-50 rounded-xl flex items-center justify-center">
              <svg class="w-6 h-6 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6"></path></svg>
            </div>
          </div>
        </div>
        <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-5">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm font-medium text-gray-500">Suivis Aujourd'hui</p>
              <p class="text-3xl font-bold text-purple-600 mt-1">{{ todayTrackingsCount }}</p>
            </div>
            <div class="w-12 h-12 bg-purple-50 rounded-xl flex items-center justify-center">
              <svg class="w-6 h-6 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4"></path></svg>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- SECTION: Action Items / Critical Attention -->
    <div v-if="missingTodayTrackings.length > 0 || pendingEvaluations.length > 0" class="bg-red-50/50 border border-red-200 rounded-xl p-4 space-y-3">
      <h2 class="text-xs font-bold uppercase tracking-wider text-red-800 flex items-center gap-1.5">
        <span class="w-2.5 h-2.5 rounded-full bg-red-500 animate-ping"></span>
        Tâches nécessitant votre attention (Action Items)
      </h2>
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <!-- Missing Today's Tracking -->
        <div v-if="missingTodayTrackings.length > 0" class="bg-white border border-red-100 rounded-lg p-3 space-y-2">
          <p class="text-xs font-bold text-red-700 flex items-center gap-1">
            ⚠️ Saisies quotidiennes manquantes (Aujourd'hui)
          </p>
          <div class="space-y-1.5 max-h-36 overflow-y-auto">
            <div v-for="op in missingTodayTrackings" :key="op.formationId" class="flex justify-between items-center text-xs p-1.5 hover:bg-gray-50 rounded">
              <div>
                <span class="font-semibold text-gray-800">{{ op.name }}</span>
                <span class="text-gray-400 text-[10px] block">Poste: {{ op.workstationName }}</span>
              </div>
              <router-link :to="'/training?operatorId=' + op.operatorId" class="text-sky-600 font-bold hover:underline">Saisir</router-link>
            </div>
          </div>
        </div>

        <!-- Pending Evaluations -->
        <div v-if="pendingEvaluations.length > 0" class="bg-white border border-amber-100 rounded-lg p-3 space-y-2">
          <p class="text-xs font-bold text-amber-700 flex items-center gap-1">
            📋 Évaluations finales à valider
          </p>
          <div class="space-y-1.5 max-h-36 overflow-y-auto">
            <div v-for="f in pendingEvaluations" :key="f.id" class="flex justify-between items-center text-xs p-1.5 hover:bg-gray-50 rounded">
              <div>
                <span class="font-semibold text-gray-800">{{ f.operatorName }}</span>
                <span class="text-gray-400 text-[10px] block">Poste: {{ f.workstationName }}</span>
              </div>
              <router-link :to="'/evaluation/session/' + f.id" class="text-sky-600 font-bold hover:underline">Évaluer</router-link>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- SECTION 2: Statistiques de la Zone -->
    <h2 class="text-sm font-bold uppercase tracking-wider text-slate-400 mt-2 mb-3 flex items-center gap-1.5">
      <span class="w-1 h-3.5 bg-amber-500 rounded-full"></span>
      Statistiques de la Zone (Zone Stats)
    </h2>
    <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
      <!-- Card 1: Complétude du Suivi Quotidien -->
      <div class="bg-white rounded-xl border border-gray-200 p-5 shadow-sm">
        <h3 class="text-xs font-semibold text-gray-900 mb-4">Complétude du Suivi Quotidien</h3>
        <div class="flex items-center justify-between mb-2">
          <span class="text-xs text-gray-500">Saisies faites (Aujourd'hui)</span>
          <span class="text-xs font-bold text-slate-800">{{ todayTrackingsCount }} / {{ activeFormations.length }}</span>
        </div>
        <div class="w-full bg-gray-100 rounded-full h-2">
          <div
            class="bg-emerald-500 h-2 rounded-full transition-all duration-500"
            :style="{ width: activeFormations.length > 0 ? ((todayTrackingsCount / activeFormations.length) * 100) + '%' : '0%' }"
          ></div>
        </div>
        <p class="text-[10px] text-gray-400 mt-2">
          {{ Math.max(0, activeFormations.length - todayTrackingsCount) }} opérateur(s) en intégration en attente de saisie.
        </p>
      </div>

      <!-- Card 2: Niveau de Polyvalence de la Zone -->
      <div class="bg-white rounded-xl border border-gray-200 p-5 shadow-sm">
        <h3 class="text-xs font-semibold text-gray-900 mb-4">Distribution des Niveaux ILU</h3>
        <div class="space-y-2">
          <!-- Level I -->
          <div>
            <div class="flex justify-between text-[10px] mb-1">
              <span class="text-amber-700 font-medium">Niveau I (Initial)</span>
              <span class="font-bold">{{ iluCounts.I }} ({{ activeOperators.length > 0 ? Math.round((iluCounts.I / activeOperators.length) * 100) : 0 }}%)</span>
            </div>
            <div class="w-full bg-gray-100 rounded-full h-1.5">
              <div class="bg-amber-400 h-1.5 rounded-full" :style="{ width: activeOperators.length > 0 ? ((iluCounts.I / activeOperators.length) * 100) + '%' : '0%' }"></div>
            </div>
          </div>
          <!-- Level L -->
          <div>
            <div class="flex justify-between text-[10px] mb-1">
              <span class="text-blue-700 font-medium">Niveau L (Autonome)</span>
              <span class="font-bold">{{ iluCounts.L }} ({{ activeOperators.length > 0 ? Math.round((iluCounts.L / activeOperators.length) * 100) : 0 }}%)</span>
            </div>
            <div class="w-full bg-gray-100 rounded-full h-1.5">
              <div class="bg-blue-500 h-1.5 rounded-full" :style="{ width: activeOperators.length > 0 ? ((iluCounts.L / activeOperators.length) * 100) + '%' : '0%' }"></div>
            </div>
          </div>
          <!-- Level U -->
          <div>
            <div class="flex justify-between text-[10px] mb-1">
              <span class="text-green-700 font-medium">Niveau U (Formateur)</span>
              <span class="font-bold">{{ iluCounts.U }} ({{ activeOperators.length > 0 ? Math.round((iluCounts.U / activeOperators.length) * 100) : 0 }}%)</span>
            </div>
            <div class="w-full bg-gray-100 rounded-full h-1.5">
              <div class="bg-green-600 h-1.5 rounded-full" :style="{ width: activeOperators.length > 0 ? ((iluCounts.U / activeOperators.length) * 100) + '%' : '0%' }"></div>
            </div>
          </div>
        </div>
      </div>

      <!-- Card 3: Avancement de l'Intégration -->
      <div class="bg-white rounded-xl border border-gray-200 p-5 shadow-sm">
        <h3 class="text-xs font-semibold text-gray-900 mb-4">Avancement Moyen de l'Intégration</h3>
        <div class="flex items-center justify-between mb-2">
          <span class="text-xs text-gray-500">Moyenne jours complétés</span>
          <span class="text-xs font-bold text-slate-800">{{ averageIntegrationDays }} / 12 Jours</span>
        </div>
        <div class="w-full bg-gray-100 rounded-full h-2">
          <div
            class="bg-sky-500 h-2 rounded-full transition-all duration-500"
            :style="{ width: ((averageIntegrationDays / 12) * 100) + '%' }"
          ></div>
        </div>
        <p class="text-[10px] text-gray-400 mt-2">
          Moyenne calculée sur {{ integrationOperators.length }} opérateur(s) en phase active d'intégration.
        </p>
      </div>
    </div>

    <!-- SECTION 3: Actions Rapides et Activités Recéntes -->
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6 mt-4">
      <!-- Quick Actions (Left 1/3) -->
      <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-5 flex flex-col justify-between">
        <div>
          <h2 class="text-sm font-semibold text-gray-900 mb-4">Actions Rapides</h2>
          <div class="space-y-3">
            <router-link to="/training" class="flex items-center gap-3 p-3 rounded-xl border border-gray-200 hover:border-emerald-300 hover:bg-emerald-50 transition group">
              <div class="w-10 h-10 bg-emerald-100 rounded-lg flex items-center justify-center group-hover:bg-emerald-200 transition">
                <svg class="w-5 h-5 text-emerald-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"></path></svg>
              </div>
              <div>
                <p class="text-sm font-medium text-gray-700">Nouvelle Formation</p>
                <p class="text-xs text-gray-400">Démarrer une formation</p>
              </div>
            </router-link>
            <router-link to="/training" class="flex items-center gap-3 p-3 rounded-xl border border-gray-200 hover:border-amber-300 hover:bg-amber-50 transition group">
              <div class="w-10 h-10 bg-amber-100 rounded-lg flex items-center justify-center group-hover:bg-amber-200 transition">
                <svg class="w-5 h-5 text-amber-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"></path></svg>
              </div>
              <div>
                <p class="text-sm font-medium text-gray-700">Suivi Quotidien</p>
                <p class="text-xs text-gray-400">Évaluer le niveau du jour</p>
              </div>
            </router-link>
            <router-link to="/operators" class="flex items-center gap-3 p-3 rounded-xl border border-gray-200 hover:border-blue-300 hover:bg-blue-50 transition group">
              <div class="w-10 h-10 bg-blue-100 rounded-lg flex items-center justify-center group-hover:bg-blue-200 transition">
                <svg class="w-5 h-5 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"></path></svg>
              </div>
              <div>
                <p class="text-sm font-medium text-gray-700">Affecter Opérateur</p>
                <p class="text-xs text-gray-400">Assigner à un poste</p>
              </div>
            </router-link>
            <router-link to="/structure" class="flex items-center gap-3 p-3 rounded-xl border border-gray-200 hover:border-purple-300 hover:bg-purple-50 transition group">
              <div class="w-10 h-10 bg-purple-100 rounded-lg flex items-center justify-center group-hover:bg-purple-200 transition">
                <svg class="w-5 h-5 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4"></path></svg>
              </div>
              <div>
                <p class="text-sm font-medium text-gray-700">Ma Zone</p>
                <p class="text-xs text-gray-400">Voir postes et structure</p>
              </div>
            </router-link>
          </div>
        </div>
      </div>

      <!-- Recent Tracking Activity (Right 2/3) -->
      <div class="lg:col-span-2 bg-white rounded-xl shadow-sm border border-gray-200 p-5 flex flex-col justify-between">
        <div>
          <div class="flex items-center justify-between mb-4">
            <h2 class="text-sm font-semibold text-gray-900">Derniers Suivis Enregistrés</h2>
            <router-link to="/training" class="text-xs text-emerald-600 hover:underline">Voir tout</router-link>
          </div>
          <div v-if="loading" class="flex items-center justify-center py-12"><div class="w-8 h-8 border-4 border-emerald-200 border-t-emerald-600 rounded-full animate-spin"></div></div>
          <div v-else-if="recentTrackings.length > 0" class="space-y-2">
            <div v-for="t in recentTrackings" :key="t.id" class="flex items-center justify-between p-3 rounded-lg bg-gray-50 hover:bg-gray-100/50 transition">
              <div class="flex items-center gap-3">
                <div class="w-8 h-8 rounded-full flex items-center justify-center text-xs font-bold" :class="getDailyLevelClass(t.dailyLevel)">{{ getDailyLevelLabel(t.dailyLevel) }}</div>
                <div>
                  <p class="text-sm font-medium text-gray-900">{{ t.operatorName }}</p>
                  <p class="text-xs text-gray-500">{{ t.workstationName }} - {{ t.trackingDate }}</p>
                </div>
              </div>
              <span v-if="t.comment" class="text-xs text-gray-500 max-w-48 truncate">{{ t.comment }}</span>
            </div>
          </div>
          <div v-else class="text-center py-12 text-gray-400 text-xs italic">Aucune activité de suivi récente.</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { trainingApi, operatorsApi, evaluationApi } from '@/api/endpoints.js'
import { useUserScope } from '@/composables/useUserScope'

const { loadUserProjects, filterOperators, filterFormations } = useUserScope()

const loading = ref(true)
const operators = ref([])
const formations = ref([])
const allTrackings = ref([])
const matrixData = ref(null)

// Search & Pagination state
const operatorSearch = ref('')
const opCurrentPage = ref(1)
const opPageSize = ref(10)

const integrationCurrentPage = ref(1)
const integrationPageSize = ref(5)

const currentDate = computed(() => new Date().toLocaleDateString('fr-FR', { day: 'numeric', month: 'long', year: 'numeric' }))

const scopedOperators = computed(() => filterOperators(operators.value))
const activeOperators = computed(() => scopedOperators.value.filter(o => o.active !== false))

const filteredActiveOperators = computed(() => {
  let list = activeOperators.value
  if (operatorSearch.value.trim()) {
    const q = operatorSearch.value.toLowerCase().trim()
    list = list.filter(op => `${op.lastName || ''} ${op.firstName || ''}`.toLowerCase().includes(q) || (op.employeeId || '').toLowerCase().includes(q))
  }
  return list
})

const paginatedActiveOperators = computed(() => {
  const start = (opCurrentPage.value - 1) * opPageSize.value
  const end = start + opPageSize.value
  return filteredActiveOperators.value.slice(start, end)
})

const opTotalPages = computed(() => {
  return Math.ceil(filteredActiveOperators.value.length / opPageSize.value) || 1
})

watch([operatorSearch, opPageSize], () => {
  opCurrentPage.value = 1
})

const scopedFormations = computed(() => filterFormations(formations.value, operators.value))
const activeFormations = computed(() => scopedFormations.value.filter(f => f.status === 'IN_PROGRESS'))

// Build ILU level lookup from matrix data: operatorId -> workstationId -> level
const iluLookup = computed(() => {
  const lookup = {}
  if (!matrixData.value?.operators) return lookup
  for (const row of matrixData.value.operators) {
    if (!row.workstations) continue
    for (const [wsId, wsData] of Object.entries(row.workstations)) {
      if (wsData.level) {
        if (!lookup[row.operatorId]) lookup[row.operatorId] = {}
        lookup[row.operatorId][wsId] = wsData.level
      }
    }
  }
  return lookup
})

// ILU counts for KPI card
const iluCounts = computed(() => {
  const counts = { I: 0, L: 0, U: 0, none: 0 }
  for (const op of activeOperators.value) {
    const level = getIluLevel(op)
    if (level === 'I') counts.I++
    else if (level === 'L') counts.L++
    else if (level === 'U') counts.U++
    else counts.none++
  }
  return counts
})

// Get ILU level (I/L/U) for an operator at their workstation
function getIluLevel(op) {
  if (op.iluLevel) return String(op.iluLevel).toUpperCase()
  if (op.niveau) return String(op.niveau).toUpperCase()

  // Matrix lookup
  const matrixOps = matrixData.value?.operators || []
  const matOp = matrixOps.find(m => m.operatorId === op.id || m.employeeId === op.employeeId)
  if (matOp && matOp.workstations) {
    const levels = Object.values(matOp.workstations)
      .map(w => (typeof w === 'object' ? w.level : w))
      .filter(Boolean)
    if (levels.includes('U')) return 'U'
    if (levels.includes('L')) return 'L'
    if (levels.includes('I')) return 'I'
  }

  // Formations lookup
  const opFormations = formations.value.filter(fo => fo.operatorId === op.id)
  for (const f of opFormations) {
    if (f.achievedLevel === 'U' || f.targetLevel === 'U' || f.achievedLevel === 3 || f.achievedLevel === '3' || f.targetLevel === 3 || f.targetLevel === '3') return 'U'
    if (f.achievedLevel === 'L' || f.targetLevel === 'L' || f.status === 'COMPLETED' || f.achievedLevel === 2 || f.achievedLevel === '2' || f.targetLevel === 2 || f.targetLevel === '2') return 'L'
    if (f.achievedLevel === 'I' || f.status === 'IN_PROGRESS' || f.achievedLevel === 1 || f.achievedLevel === '1') return 'I'
  }

  return '-'
}

function getLevelClass(op) {
  const level = getIluLevel(op)
  if (level === 'U') return 'bg-green-100 text-green-700 font-bold'
  if (level === 'L') return 'bg-blue-100 text-blue-700 font-bold'
  if (level === 'I') return 'bg-amber-100 text-amber-700 font-bold'
  return 'bg-gray-100 text-gray-400'
}

// Operators currently in their 12-day integration period
const integrationOperators = computed(() => {
  return activeFormations.value.map(f => {
    const startDate = f.startDate ? new Date(f.startDate) : null
    const today = new Date()
    let dayCount = 0
    if (startDate) {
      const diff = Math.floor((today - startDate) / (1000 * 60 * 60 * 24)) + 1
      dayCount = Math.max(0, Math.min(diff, 12))
    }
    const op = operators.value.find(o => o.id === f.operatorId)
    return {
      formationId: f.id,
      operatorId: f.operatorId,
      name: op ? `${op.lastName} ${op.firstName}` : 'Inconnu',
      initials: op ? `${(op.lastName || '')[0]}${(op.firstName || '')[0]}` : '?',
      workstationName: f.workstationName || '-',
      achievedLevel: f.achievedLevel || 0,
      targetLevel: f.targetLevel || 0,
      dayCount,
      trackingDays: getTrackingDaysForFormation(f.id),
    }
  })
})

const paginatedIntegrationOperators = computed(() => {
  const start = (integrationCurrentPage.value - 1) * integrationPageSize.value
  const end = start + integrationPageSize.value
  return integrationOperators.value.slice(start, end)
})

const integrationTotalPages = computed(() => {
  return Math.ceil(integrationOperators.value.length / integrationPageSize.value) || 1
})

const missingTodayTrackings = computed(() => {
  const todayStr = new Date().toISOString().slice(0, 10)
  return integrationOperators.value.filter(op => {
    const hasToday = allTrackings.value.some(t => t.formationId === op.formationId && t.trackingDate === todayStr)
    return !hasToday
  })
})

const pendingEvaluations = computed(() => {
  return activeFormations.value.filter(f => (f.daysWithData || 0) >= 12)
})

const averageIntegrationDays = computed(() => {
  const ops = integrationOperators.value
  if (!ops.length) return 0
  const sum = ops.reduce((acc, op) => acc + (op.dayCount || 0), 0)
  return Math.round((sum / ops.length) * 10) / 10
})

const todayTrackingsCount = computed(() => {
  const today = new Date().toISOString().split('T')[0]
  return allTrackings.value.filter(t => t.trackingDate === today).length
})

const recentTrackings = computed(() => {
  return allTrackings.value.slice(0, 8).map(t => {
    const f = formations.value.find(fo => fo.id === t.formationId)
    const op = f ? operators.value.find(o => o.id === f.operatorId) : null
    return {
      ...t,
      operatorName: op ? `${op.lastName} ${op.firstName}` : 'Inconnu',
      workstationName: f?.workstationName || '-',
    }
  })
})

function getTrackingDaysForFormation(formationId) {
  return allTrackings.value.filter(t => t.formationId === formationId).map(t => t.trackingDate)
}

function getDayClass(op, day) {
  const startDate = op.formationId ? formations.value.find(f => f.id === op.formationId)?.startDate : null
  if (!startDate) return 'bg-gray-100 text-gray-400'
  const start = new Date(startDate)
  const dayDate = new Date(start)
  dayDate.setDate(dayDate.getDate() + day - 1)
  const dateStr = dayDate.toISOString().split('T')[0]
  const today = new Date().toISOString().split('T')[0]
  if (op.trackingDays.includes(dateStr)) return 'bg-emerald-500 text-white'
  if (dateStr <= today) return 'bg-red-200 text-red-700'
  return 'bg-gray-100 text-gray-400'
}

function getWorkstation(op) {
  const f = formations.value.find(fo => fo.operatorId === op.id && fo.status === 'IN_PROGRESS')
  return f ? f.workstationName : '-'
}

function getFormationId(op) {
  const f = formations.value.find(fo => fo.operatorId === op.id && fo.status === 'IN_PROGRESS')
  return f ? f.id : ''
}

function getStatus(op) {
  const f = activeFormations.value.find(fo => fo.operatorId === op.id)
  if (!f) return 'AUCUNE'
  const startDate = f.startDate ? new Date(f.startDate) : null
  if (startDate) {
    const diff = Math.floor((new Date() - startDate) / (1000 * 60 * 60 * 24)) + 1
    if (diff > 12) return 'TERMINEE'
    return `J${diff}/12`
  }
  return 'EN COURS'
}

function getStatusClass(op) {
  const status = getStatus(op)
  if (status === 'TERMINEE') return 'bg-emerald-100 text-emerald-700'
  if (status === 'AUCUNE') return 'bg-gray-100 text-gray-500'
  return 'bg-amber-100 text-amber-700'
}

function formatNiveauTarget(level) {
  if (level === 3 || level === '3' || level === 'U') return 'U'
  if (level === 2 || level === '2' || level === 'L') return 'L'
  if (level === 1 || level === '1' || level === 'I') return 'I'
  return level || 'L'
}

function getDailyLevelClass(level) {
  if (level >= 3) return 'bg-green-100 text-green-700'
  if (level >= 2) return 'bg-blue-100 text-blue-700'
  if (level >= 1) return 'bg-amber-100 text-amber-700'
  return 'bg-gray-100 text-gray-500'
}

function getDailyLevelLabel(level) {
  if (level >= 3) return 'U'
  if (level >= 2) return 'L'
  if (level >= 1) return 'I'
  return '-'
}

onMounted(async () => {
  loading.value = true
  try {
    await loadUserProjects()
    const [o, f] = await Promise.all([operatorsApi.getAll(), trainingApi.getFormations()])
    operators.value = o.data
    formations.value = f.data
    // Fetch tracking for each active formation
    const trackingPromises = activeFormations.value.map(fo => trainingApi.getTracking(fo.id))
    const trackingResults = await Promise.all(trackingPromises)
    allTrackings.value = trackingResults.flatMap(r => r.data).sort((a, b) => b.trackingDate?.localeCompare(a.trackingDate))
    // Fetch ILU matrix data for Niveau I/L/U display
    try {
      const matrixRes = await evaluationApi.getMatrix()
      matrixData.value = matrixRes.data
    } catch (e) {
      console.error('Error loading ILU matrix:', e)
    }
  } catch (e) { console.error(e) } finally { loading.value = false }
})
</script>
