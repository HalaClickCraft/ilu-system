<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">Tableau de bord Agent Qualite</h1>
        <p class="text-gray-500 mt-1">Controle qualite et suivi des niveaux ILU</p>
      </div>
      <div class="text-sm text-gray-400">{{ currentDate }}</div>
    </div>

    <!-- Quality KPIs -->
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
      <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-5">
        <div class="flex items-center justify-between">
          <div><p class="text-sm font-medium text-gray-500">Formations en Cours</p><p class="text-3xl font-bold text-amber-600 mt-1">{{ inProgressCount }}</p></div>
          <div class="w-12 h-12 bg-amber-50 rounded-xl flex items-center justify-center"><svg class="w-6 h-6 text-amber-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253"></path></svg></div>
        </div>
      </div>
      <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-5">
        <div class="flex items-center justify-between">
          <div><p class="text-sm font-medium text-gray-500">Niveau Moyen Atteint</p><p class="text-3xl font-bold text-blue-600 mt-1">{{ averageLevel }}</p></div>
          <div class="w-12 h-12 bg-blue-50 rounded-xl flex items-center justify-center"><svg class="w-6 h-6 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"></path></svg></div>
        </div>
      </div>
      <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-5">
        <div class="flex items-center justify-between">
          <div><p class="text-sm font-medium text-gray-500">Non Conformes</p><p class="text-3xl font-bold text-red-600 mt-1">{{ nonConformingCount }}</p></div>
          <div class="w-12 h-12 bg-red-50 rounded-xl flex items-center justify-center"><svg class="w-6 h-6 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4.5c-.77-.833-2.694-.833-3.464 0L3.34 16.5c-.77.833.192 2.5 1.732 2.5z"></path></svg></div>
        </div>
      </div>
      <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-5">
        <div class="flex items-center justify-between">
          <div><p class="text-sm font-medium text-gray-500">Certifies</p><p class="text-3xl font-bold text-emerald-600 mt-1">{{ completedCount }}</p></div>
          <div class="w-12 h-12 bg-emerald-50 rounded-xl flex items-center justify-center"><svg class="w-6 h-6 text-emerald-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg></div>
        </div>
      </div>
    </div>

    <!-- Level Distribution & Workstation Quality -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
        <h2 class="text-lg font-semibold text-gray-900 mb-4">Repartition par Niveau ILU</h2>
        <div v-if="loading" class="flex items-center justify-center py-12"><div class="w-8 h-8 border-4 border-emerald-200 border-t-emerald-600 rounded-full animate-spin"></div></div>
        <div v-else class="space-y-3">
          <div v-for="level in levelDistribution" :key="level.level" class="flex items-center gap-3">
            <div class="w-16 text-sm font-medium text-gray-700">Niveau {{ level.level }}</div>
            <div class="flex-1 bg-gray-100 rounded-full h-3"><div class="h-3 rounded-full transition-all duration-500" :class="level.color" :style="{ width: level.percent + '%' }"></div></div>
            <span class="text-sm text-gray-600 w-8 text-right">{{ level.count }}</span>
          </div>
        </div>
      </div>

      <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
        <h2 class="text-lg font-semibold text-gray-900 mb-4">Formations Necessitant une Attention</h2>
        <div v-if="loading" class="flex items-center justify-center py-12"><div class="w-8 h-8 border-4 border-emerald-200 border-t-emerald-600 rounded-full animate-spin"></div></div>
        <div v-else-if="attentionFormations.length > 0" class="space-y-2 max-h-80 overflow-y-auto">
          <div v-for="f in attentionFormations" :key="f.id" class="flex items-center justify-between p-3 rounded-lg bg-red-50 border border-red-100">
            <div><p class="text-sm font-medium text-gray-900">{{ f.operatorName }}</p><p class="text-xs text-gray-500">{{ f.workstationName }} - Cible: {{ f.targetLevel }}</p></div>
            <router-link :to="'/training/' + f.id" class="text-sm text-red-600 hover:underline font-medium">Controle</router-link>
          </div>
        </div>
        <div v-else class="text-center py-12 text-gray-400">Aucune formation critique</div>
      </div>
    </div>

    <!-- Active Formations Table -->
    <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
      <div class="flex items-center justify-between mb-4">
        <h2 class="text-lg font-semibold text-gray-900">Formations Actives - Controle Qualite</h2>
        <router-link to="/training" class="text-sm text-emerald-600 hover:underline">Voir toutes les formations</router-link>
      </div>
      <div v-if="loading" class="flex items-center justify-center py-8"><div class="w-8 h-8 border-4 border-emerald-200 border-t-emerald-600 rounded-full animate-spin"></div></div>
      <div v-else-if="activeFormations.length > 0" class="overflow-x-auto">
        <table class="w-full text-sm">
          <thead class="bg-gray-50"><tr><th class="text-left py-3 px-4 font-medium text-gray-500">Operateur</th><th class="text-left py-3 px-4 font-medium text-gray-500">Poste</th><th class="text-left py-3 px-4 font-medium text-gray-500">Atteint</th><th class="text-left py-3 px-4 font-medium text-gray-500">Cible</th><th class="text-left py-3 px-4 font-medium text-gray-500">Ecart</th><th class="text-right py-3 px-4 font-medium text-gray-500">Action</th></tr></thead>
          <tbody>
            <tr v-for="f in activeFormations" :key="f.id" class="border-b border-gray-50 hover:bg-gray-50">
              <td class="py-3 px-4 font-medium">{{ f.operatorName }}</td>
              <td class="py-3 px-4 text-gray-500">{{ f.workstationName }}</td>
              <td class="py-3 px-4 font-medium">{{ f.achievedLevel ?? 0 }}</td>
              <td class="py-3 px-4">{{ f.targetLevel }}</td>
              <td class="py-3 px-4"><span class="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium" :class="gapClass(f)">{{ gapLabel(f) }}</span></td>
              <td class="py-3 px-4 text-right"><router-link :to="'/training/' + f.id" class="text-emerald-600 hover:underline text-sm">Details</router-link></td>
            </tr>
          </tbody>
        </table>
      </div>
      <div v-else class="text-center py-8 text-gray-400">Aucune formation active</div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { trainingApi } from '@/api/endpoints'

const loading = ref(true)
const formations = ref([])
const stats = ref({})

const currentDate = computed(() => new Date().toLocaleDateString('fr-FR', { day: 'numeric', month: 'long', year: 'numeric' }))
const activeFormations = computed(() => formations.value.filter(f => f.status === 'IN_PROGRESS'))
const completedFormations = computed(() => formations.value.filter(f => f.status === 'COMPLETED'))
const inProgressCount = computed(() => activeFormations.value.length)
const completedCount = computed(() => completedFormations.value.length)

const averageLevel = computed(() => {
  const active = activeFormations.value
  if (active.length === 0) return '0'
  const sum = active.reduce((s, f) => s + (f.achievedLevel || 0), 0)
  return (sum / active.length).toFixed(1)
})

const nonConformingCount = computed(() => {
  return activeFormations.value.filter(f => (f.achievedLevel || 0) < (f.targetLevel || 5) * 0.5).length
})

const attentionFormations = computed(() => {
  return activeFormations.value.filter(f => (f.achievedLevel || 0) === 0).slice(0, 5)
})

const levelDistribution = computed(() => {
  const levels = [1, 2, 3, 4, 5]
  const colors = ['bg-red-400', 'bg-orange-400', 'bg-amber-400', 'bg-emerald-400', 'bg-emerald-600']
  return levels.map((level, i) => {
    const count = formations.value.filter(f => (f.achievedLevel || 0) === level).length
    return { level, count, percent: formations.value.length > 0 ? Math.round((count / formations.value.length) * 100) : 0, color: colors[i] }
  })
})

const gapLabel = (f) => { const gap = (f.targetLevel || 5) - (f.achievedLevel || 0); return gap <= 1 ? 'OK' : gap <= 2 ? 'Moyen' : 'Critique' }
const gapClass = (f) => { const gap = (f.targetLevel || 5) - (f.achievedLevel || 0); return gap <= 1 ? 'bg-emerald-100 text-emerald-700' : gap <= 2 ? 'bg-amber-100 text-amber-700' : 'bg-red-100 text-red-700' }

onMounted(async () => {
  loading.value = true
  try {
    const [f, s] = await Promise.all([trainingApi.getFormations(), trainingApi.getStatistics()])
    formations.value = f.data
    stats.value = s.data
  } catch (e) { console.error(e) } finally { loading.value = false }
})
</script>