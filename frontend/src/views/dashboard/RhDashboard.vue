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
      <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 mb-4">
        <div>
          <h2 class="text-lg font-semibold text-gray-900">Suivi des Départs & Absences</h2>
          <p class="text-xs text-gray-500 mt-0.5">Opérateurs inactifs ou avec date de départ déclarée</p>
        </div>
        <div class="flex items-center gap-2">
          <input
            v-model="absentSearch"
            type="text"
            placeholder="Rechercher opérateur..."
            class="px-2.5 py-1.5 border border-gray-200 rounded-lg text-xs outline-none focus:ring-2 focus:ring-emerald-500 w-36 sm:w-44"
          />
          <router-link to="/operators" class="text-xs text-emerald-600 hover:underline whitespace-nowrap">Tous les opérateurs</router-link>
        </div>
      </div>
      
      <div v-if="loading" class="flex items-center justify-center py-8"><div class="w-8 h-8 border-4 border-emerald-200 border-t-emerald-600 rounded-full animate-spin"></div></div>
      <div v-else-if="filteredAbsentOperators.length > 0" class="space-y-4">
        <div class="overflow-x-auto">
          <table class="w-full text-sm">
            <thead class="bg-gray-50">
              <tr>
                <th scope="col" class="text-left py-2.5 px-4 font-semibold text-gray-500 text-xs">Opérateur</th>
                <th scope="col" class="text-left py-2.5 px-4 font-semibold text-gray-500 text-xs">Matricule</th>
                <th scope="col" class="text-left py-2.5 px-4 font-semibold text-gray-500 text-xs">Date Sortie / Reprise</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="op in paginatedAbsentOperators" :key="op.id" class="border-b border-gray-50 hover:bg-gray-50">
                <td class="py-2.5 px-4 font-medium text-sm">{{ op.lastName }} {{ op.firstName }}</td>
                <td class="py-2.5 px-4 text-gray-500 font-mono text-xs">{{ op.employeeId }}</td>
                <td class="py-2.5 px-4 text-gray-500 text-xs">{{ formatDate(op.exitDate) }}</td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Pagination Footer -->
        <div v-if="absentTotalPages > 1" class="flex justify-between items-center text-xs text-gray-500 font-medium pt-3 border-t">
          <span>Affichage de {{ (absentCurrentPage - 1) * absentPageSize + 1 }} à {{ Math.min(absentCurrentPage * absentPageSize, filteredAbsentOperators.length) }} sur {{ filteredAbsentOperators.length }}</span>
          <div class="flex gap-1">
            <button :disabled="absentCurrentPage === 1" @click="absentCurrentPage--" class="px-2 py-1 bg-white border border-gray-200 rounded hover:bg-gray-50 disabled:opacity-50 font-semibold">Précédent</button>
            <span class="px-3 py-1 bg-gray-100 rounded flex items-center font-semibold">Page {{ absentCurrentPage }} / {{ absentTotalPages }}</span>
            <button :disabled="absentCurrentPage === absentTotalPages" @click="absentCurrentPage++" class="px-2 py-1 bg-white border border-gray-200 rounded hover:bg-gray-50 disabled:opacity-50 font-semibold">Suivant</button>
          </div>
        </div>
      </div>
      <div v-else class="text-center py-8 text-gray-400 text-sm">Aucun opérateur trouvé</div>
    </div>

    <!-- Quick Actions -->
    <div class="grid grid-cols-2 sm:grid-cols-3 gap-3">
      <router-link to="/operators" class="flex items-center gap-3 p-4 rounded-xl border border-gray-200 hover:border-emerald-300 hover:bg-emerald-50 transition group">
        <div class="w-10 h-10 bg-emerald-100 rounded-lg flex items-center justify-center group-hover:bg-emerald-200 transition"><svg class="w-5 h-5 text-emerald-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z"></path></svg></div>
        <div><p class="text-sm font-medium text-gray-700">Nouvel Embauche</p><p class="text-xs text-gray-400">Ajouter un operateur</p></div>
      </router-link>
      <router-link to="/absences" class="flex items-center gap-3 p-4 rounded-xl border border-gray-200 hover:border-blue-300 hover:bg-blue-50 transition group">
        <div class="w-10 h-10 bg-blue-100 rounded-lg flex items-center justify-center group-hover:bg-blue-200 transition"><svg class="w-5 h-5 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3M4 11h16M5 5h14a1 1 0 011 1v13a1 1 0 01-1 1H5a1 1 0 01-1-1V6a1 1 0 011-1zM8 15h8"></path></svg></div>
        <div><p class="text-sm font-medium text-gray-700">Gestion Absences</p><p class="text-xs text-gray-400">Déclarer absence / départ</p></div>
      </router-link>
      <router-link to="/teams" class="flex items-center gap-3 p-4 rounded-xl border border-gray-200 hover:border-purple-300 hover:bg-purple-50 transition group">
        <div class="w-10 h-10 bg-purple-100 rounded-lg flex items-center justify-center group-hover:bg-purple-200 transition"><svg class="w-5 h-5 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z"></path></svg></div>
        <div><p class="text-sm font-medium text-gray-700">Gestion Équipes</p><p class="text-xs text-gray-400">Organiser les équipes</p></div>
      </router-link>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { operatorsApi } from '@/api/endpoints'

const loading = ref(true)
const operators = ref([])

const absentSearch = ref('')
const absentCurrentPage = ref(1)
const absentPageSize = ref(5)

const currentDate = computed(() => new Date().toLocaleDateString('fr-FR', { day: 'numeric', month: 'long', year: 'numeric' }))
const activeCount = computed(() => operators.value.filter(o => o.active !== false).length)
const inactiveCount = computed(() => operators.value.filter(o => o.active === false).length)
const absenceCount = computed(() => operators.value.filter(o => o.absenceReason).length)

const teamDistribution = computed(() => {
  const map = {}
  operators.value.filter(o => o.active !== false).forEach(op => {
    const teamName = op.team?.name || 'Non assigné'
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

const filteredAbsentOperators = computed(() => {
  let list = absentOperators.value
  if (absentSearch.value.trim()) {
    const q = absentSearch.value.toLowerCase().trim()
    list = list.filter(op => `${op.lastName || ''} ${op.firstName || ''}`.toLowerCase().includes(q) || (op.employeeId || '').toLowerCase().includes(q))
  }
  return list
})

const paginatedAbsentOperators = computed(() => {
  const start = (absentCurrentPage.value - 1) * absentPageSize.value
  const end = start + absentPageSize.value
  return filteredAbsentOperators.value.slice(start, end)
})

const absentTotalPages = computed(() => {
  return Math.ceil(filteredAbsentOperators.value.length / absentPageSize.value) || 1
})

watch([absentSearch, absentPageSize], () => {
  absentCurrentPage.value = 1
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