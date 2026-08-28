<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">Tableau de bord RH</h1>
        <p class="text-gray-500 mt-1">Gestion des ressources humaines et suivi des operateurs</p>
      </div>
      <div class="text-sm text-gray-400">{{ currentDate }}</div>
    </div>

    <!-- HR KPI Cards -->
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
      <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-5">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm font-medium text-gray-500">Total Opérateurs</p>
            <p class="text-3xl font-bold text-blue-600 mt-1">{{ operators.length }}</p>
          </div>
          <div class="w-12 h-12 bg-blue-50 rounded-xl flex items-center justify-center">
            <svg class="w-6 h-6 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z"></path></svg>
          </div>
        </div>
      </div>
      <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-5">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm font-medium text-gray-500">Opérateurs Actifs</p>
            <p class="text-3xl font-bold text-emerald-600 mt-1">{{ activeCount }}</p>
          </div>
          <div class="w-12 h-12 bg-emerald-50 rounded-xl flex items-center justify-center">
            <svg class="w-6 h-6 text-emerald-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
          </div>
        </div>
      </div>
      <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-5">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm font-medium text-gray-500">Inactifs / Sortis</p>
            <p class="text-3xl font-bold text-red-600 mt-1">{{ inactiveCount }}</p>
          </div>
          <div class="w-12 h-12 bg-red-50 rounded-xl flex items-center justify-center">
            <svg class="w-6 h-6 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M18.364 18.364A9 9 0 005.636 5.636m12.728 12.728A9 9 0 015.636 5.636m12.728 12.728L5.636 5.636"></path></svg>
          </div>
        </div>
      </div>
      <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-5">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm font-medium text-gray-500">En Absence</p>
            <p class="text-3xl font-bold text-amber-600 mt-1">{{ absenceCount }}</p>
          </div>
          <div class="w-12 h-12 bg-amber-50 rounded-xl flex items-center justify-center">
            <svg class="w-6 h-6 text-amber-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
          </div>
        </div>
      </div>
    </div>

    <!-- Team Distribution & Recent Hires -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
        <h2 class="text-lg font-semibold text-gray-900 mb-4">Repartition par Équipe</h2>
        <div v-if="loading" class="flex items-center justify-center py-12"><div class="w-8 h-8 border-4 border-emerald-200 border-t-emerald-600 rounded-full animate-spin"></div></div>
        <div v-else-if="teamDistribution.length > 0" class="space-y-3">
          <div v-for="team in teamDistribution" :key="team.name" class="flex items-center gap-3">
            <div class="w-32 text-sm font-medium text-gray-700 truncate">{{ team.name }}</div>
            <div class="flex-1 bg-gray-100 rounded-full h-3">
              <div class="bg-emerald-500 h-3 rounded-full transition-all duration-500" :style="{ width: team.percent + '%' }"></div>
            </div>
            <span class="text-sm text-gray-600 w-8 text-right">{{ team.count }}</span>
          </div>
        </div>
        <div v-else class="text-center py-12 text-gray-400">Aucune donnee disponible</div>
      </div>

      <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
        <h2 class="text-lg font-semibold text-gray-900 mb-4">Derniers Embauches</h2>
        <div v-if="loading" class="flex items-center justify-center py-12"><div class="w-8 h-8 border-4 border-emerald-200 border-t-emerald-600 rounded-full animate-spin"></div></div>
        <div v-else-if="recentHires.length > 0" class="space-y-2">
          <div v-for="op in recentHires" :key="op.id" class="flex items-center justify-between p-3 rounded-lg bg-gray-50 hover:bg-gray-100 transition">
            <div class="flex items-center gap-3">
              <div class="w-8 h-8 bg-blue-100 rounded-full flex items-center justify-center text-sm font-medium text-blue-600">{{ op.firstName?.[0] || '' }}{{ op.lastName?.[0] || '' }}</div>
              <div><p class="text-sm font-medium text-gray-900">{{ op.firstName }} {{ op.lastName }}</p><p class="text-xs text-gray-500">{{ op.employeeId }}</p></div>
            </div>
            <span class="text-xs text-gray-400">{{ formatDate(op.hireDate) }}</span>
          </div>
        </div>
        <div v-else class="text-center py-12 text-gray-400">Aucun recrutement recent</div>
      </div>
    </div>

    <!-- Absence Tracking -->
    <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
      <div class="flex items-center justify-between mb-4">
        <h2 class="text-lg font-semibold text-gray-900">Suivi des Absences</h2>
        <router-link to="/operators" class="text-sm text-emerald-600 hover:underline">Voir tous les operateurs</router-link>
      </div>
      <div v-if="loading" class="flex items-center justify-center py-8"><div class="w-8 h-8 border-4 border-emerald-200 border-t-emerald-600 rounded-full animate-spin"></div></div>
      <div v-else-if="absentOperators.length > 0" class="overflow-x-auto">
        <table class="w-full text-sm">
          <thead class="bg-gray-50"><tr><th class="text-left py-3 px-4 font-medium text-gray-500">Opérateur</th><th class="text-left py-3 px-4 font-medium text-gray-500">Matricule</th><th class="text-left py-3 px-4 font-medium text-gray-500">Date Sortie</th></tr></thead>
          <tbody>
            <tr v-for="op in absentOperators" :key="op.id" class="border-b border-gray-50 hover:bg-gray-50">
              <td class="py-3 px-4 font-medium">{{ op.firstName }} {{ op.lastName }}</td>
              <td class="py-3 px-4 text-gray-500">{{ op.employeeId }}</td>
              <td class="py-3 px-4 text-gray-500">{{ formatDate(op.exitDate) }}</td>
            </tr>
          </tbody>
        </table>
      </div>
      <div v-else class="text-center py-8 text-gray-400">Aucune absence enregistree</div>
    </div>

    <!-- Quick Actions -->
    <div class="grid grid-cols-2 sm:grid-cols-3 gap-3">
      <router-link to="/operators" class="flex items-center gap-3 p-4 rounded-xl border border-gray-200 hover:border-emerald-300 hover:bg-emerald-50 transition group">
        <div class="w-10 h-10 bg-emerald-100 rounded-lg flex items-center justify-center group-hover:bg-emerald-200 transition"><svg class="w-5 h-5 text-emerald-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z"></path></svg></div>
        <div><p class="text-sm font-medium text-gray-700">Nouvel Embauche</p><p class="text-xs text-gray-400">Ajouter un operateur</p></div>
      </router-link>
      <router-link to="/training" class="flex items-center gap-3 p-4 rounded-xl border border-gray-200 hover:border-blue-300 hover:bg-blue-50 transition group">
        <div class="w-10 h-10 bg-blue-100 rounded-lg flex items-center justify-center group-hover:bg-blue-200 transition"><svg class="w-5 h-5 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253"></path></svg></div>
        <div><p class="text-sm font-medium text-gray-700">Planifier Formation</p><p class="text-xs text-gray-400">Assigner une formation</p></div>
      </router-link>
      <router-link to="/teams" class="flex items-center gap-3 p-4 rounded-xl border border-gray-200 hover:border-purple-300 hover:bg-purple-50 transition group">
        <div class="w-10 h-10 bg-purple-100 rounded-lg flex items-center justify-center group-hover:bg-purple-200 transition"><svg class="w-5 h-5 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z"></path></svg></div>
        <div><p class="text-sm font-medium text-gray-700">Gestion Équipes</p><p class="text-xs text-gray-400">Organiser les équipes</p></div>
      </router-link>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { operatorsApi } from '@/api/endpoints'

const loading = ref(true)
const operators = ref([])

const currentDate = computed(() => new Date().toLocaleDateString('fr-FR', { day: 'numeric', month: 'long', year: 'numeric' }))
const activeCount = computed(() => operators.value.filter(o => o.active !== false).length)
const inactiveCount = computed(() => operators.value.filter(o => o.active === false).length)
const absenceCount = computed(() => operators.value.filter(o => o.absenceReason).length)

const teamDistribution = computed(() => {
  const map = {}
  operators.value.filter(o => o.active !== false).forEach(op => {
    const teamName = op.team?.name || 'Non assigne'
    map[teamName] = (map[teamName] || 0) + 1
  })
  const max = Math.max(...Object.values(map), 1)
  return Object.entries(map).map(([name, count]) => ({ name, count, percent: Math.round((count / max) * 100) }))
})

const recentHires = computed(() => {
  return [...operators.value].filter(o => o.hireDate).sort((a, b) => new Date(b.hireDate) - new Date(a.hireDate)).slice(0, 5)
})

const absentOperators = computed(() => {
  return operators.value.filter(o => o.exitDate || o.active === false)
})

const formatDate = (d) => {
  if (!d) return '-'
  return new Date(d).toLocaleDateString('fr-FR')
}

onMounted(async () => {
  loading.value = true
  try {
    const r = await operatorsApi.getAll()
    operators.value = r.data
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
})
</script>