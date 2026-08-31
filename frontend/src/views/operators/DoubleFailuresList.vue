<template>
  <div class="space-y-6">
    <div>
      <h1 class="text-2xl font-bold text-gray-900">Signalements RH — Opérateurs en Double Échec</h1>
      <p class="text-sm text-gray-500 mt-1">
        Liste des opérateurs ayant échoué 2 fois (suivi de formation ou évaluation finale) sur un même poste de travail.
        Ces dossiers nécessitent un traitement administratif RH (fin de contrat, réaffectation, etc.) en dehors de l'application.
      </p>
    </div>

    <!-- Loading state -->
    <!-- FIX: Changed border-emerald-250 (invalid) to border-emerald-200 (valid Tailwind class) -->
    <div v-if="loading" class="flex items-center justify-center py-20">
      <div class="w-8 h-8 border-4 border-emerald-200 border-t-emerald-600 rounded-full animate-spin"></div>
    </div>

    <!-- Error state (previously silently swallowed and shown as "empty") -->
    <div v-else-if="loadError" class="bg-red-50 border border-red-200 rounded-xl p-6 text-center">
      <p class="text-red-700 font-semibold">Impossible de charger la liste</p>
      <p class="text-red-500 text-sm mt-1">{{ loadError }}</p>
      <button @click="fetchFailures" class="mt-3 bg-red-600 text-white px-4 py-2 rounded-lg text-sm hover:bg-red-700">Réessayer</button>
    </div>

    <!-- Empty state when failures list is completely empty -->
    <div v-else-if="failures.length === 0" class="bg-white rounded-xl shadow-sm border border-gray-200 p-12 text-center text-gray-400">
      <svg class="w-12 h-12 text-gray-300 mx-auto mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
      </svg>
      <p class="text-sm">Aucun opérateur en situation de double échec pour le moment.</p>
      <p class="text-xs text-gray-400 mt-2">
        Cette liste n'affiche un opérateur que s'il a échoué <strong>deux fois sur le même poste</strong>
        (suivi 12 jours ou évaluation). Un seul échec, ou deux échecs sur deux postes différents, n'y apparaît pas.
      </p>
    </div>

    <!-- Active content -->
    <div v-else class="space-y-4">
      <!-- Search and Page Size Filter -->
      <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-4 flex flex-col sm:flex-row gap-3 items-center justify-between">
        <div class="relative flex-1 max-w-sm w-full">
          <svg class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path>
          </svg>
          <input
            v-model="searchQuery"
            type="text"
            placeholder="Rechercher par opérateur, matricule ou poste..."
            class="w-full pl-10 pr-4 py-2 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 outline-none"
          />
        </div>
        
        <div class="flex items-center gap-2">
          <label class="text-sm font-medium text-gray-600 whitespace-nowrap">Taille de page:</label>
          <select
            v-model="pageSize"
            class="px-2.5 py-1.5 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 outline-none"
          >
            <option :value="10">10</option>
            <option :value="15">15</option>
            <option :value="25">25</option>
            <option :value="50">50</option>
          </select>
        </div>
      </div>

      <div class="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
        <div v-if="filteredFailures.length === 0" class="p-12 text-center text-gray-400 text-sm">
          Aucun signalement ne correspond à votre recherche.
        </div>
        <div v-else class="overflow-x-auto">
          <table class="min-w-full divide-y divide-gray-200 text-sm">
            <thead class="bg-gray-50">
              <tr>
                <th scope="col" @click="handleSort('operatorName')" class="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider cursor-pointer hover:bg-gray-100 select-none">
                  Opérateur <span v-if="sortBy === 'operatorName'">{{ sortOrder === 'asc' ? '▲' : '▼' }}</span>
                </th>
                <th scope="col" @click="handleSort('employeeId')" class="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider cursor-pointer hover:bg-gray-100 select-none">
                  Matricule <span v-if="sortBy === 'employeeId'">{{ sortOrder === 'asc' ? '▲' : '▼' }}</span>
                </th>
                <th scope="col" @click="handleSort('workstationName')" class="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider cursor-pointer hover:bg-gray-100 select-none">
                  Poste de Travail <span v-if="sortBy === 'workstationName'">{{ sortOrder === 'asc' ? '▲' : '▼' }}</span>
                </th>
                <th scope="col" @click="handleSort('failedCount')" class="px-6 py-3 text-center text-xs font-semibold text-gray-500 uppercase tracking-wider cursor-pointer hover:bg-gray-100 select-none">
                  Total Échecs <span v-if="sortBy === 'failedCount'">{{ sortOrder === 'asc' ? '▲' : '▼' }}</span>
                </th>
                <th scope="col" class="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">Historique des Échecs</th>
              </tr>
            </thead>
            <tbody class="bg-white divide-y divide-gray-200">
              <tr v-for="(item, index) in paginatedFailures" :key="index" class="hover:bg-gray-50">
                <td class="px-6 py-4 whitespace-nowrap font-semibold text-gray-900">
                  {{ item.operatorName }}
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-gray-600">
                  {{ item.employeeId }}
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                  <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-50 text-blue-700 border border-blue-200">
                    {{ item.workstationName }}
                  </span>
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-center font-bold text-red-600">
                  {{ item.failedCount }}
                </td>
                <td class="px-6 py-4 text-xs text-gray-600">
                  <ul class="space-y-1.5 list-disc list-inside">
                    <li v-for="(f, fIdx) in item.failures" :key="fIdx">
                      <span class="font-medium text-gray-800">{{ formatFailureType(f.type) }}</span> le <span class="font-semibold">{{ f.date }}</span>
                      <span class="text-gray-400"> ({{ f.details }})</span>
                    </li>
                  </ul>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Pagination Footer -->
        <div v-if="totalPages > 1" class="px-6 py-3.5 bg-gray-50 border-t flex flex-col sm:flex-row justify-between items-center gap-3 text-xs text-gray-500 font-medium">
          <span>Affichage de {{ (currentPage - 1) * pageSize + 1 }} à {{ Math.min(currentPage * pageSize, filteredFailures.length) }} sur {{ filteredFailures.length }} opérateur(s)</span>
          <div class="flex gap-1">
            <button :disabled="currentPage === 1" @click="currentPage--" class="px-2.5 py-1.5 bg-white border border-gray-200 rounded-lg hover:bg-gray-50 disabled:opacity-50 font-semibold text-gray-700">Précédent</button>
            <span class="px-3 py-1.5 bg-gray-100 rounded-lg flex items-center font-semibold text-gray-800">Page {{ currentPage }} sur {{ totalPages }}</span>
            <button :disabled="currentPage === totalPages" @click="currentPage++" class="px-2.5 py-1.5 bg-white border border-gray-200 rounded-lg hover:bg-gray-50 disabled:opacity-50 font-semibold text-gray-700">Suivant</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { evaluationApi } from '@/api/endpoints'

const failures = ref([])
const loading = ref(true)
const loadError = ref('')

const searchQuery = ref('')
const currentPage = ref(1)
const pageSize = ref(15)
const sortBy = ref('operatorName')
const sortOrder = ref('asc')

async function fetchFailures() {
  loading.value = true
  loadError.value = ''
  try {
    const res = await evaluationApi.getDoubleFailures()
    failures.value = res.data || []
  } catch (err) {
    console.error('Error fetching double failures:', err)

    if (err.response?.status === 403) {
      loadError.value = "Vous n'avez pas les droits pour consulter cette liste."
    } else {
      loadError.value = err.response?.data?.message || err.message || 'Erreur inconnue lors du chargement.'
    }
  } finally {
    loading.value = false
  }
}

const filteredFailures = computed(() => {
  let list = failures.value
  if (searchQuery.value.trim()) {
    const q = searchQuery.value.toLowerCase().trim()
    list = list.filter(item => {
      const name = (item.operatorName || '').toLowerCase()
      const mat = (item.employeeId || '').toLowerCase()
      const ws = (item.workstationName || '').toLowerCase()
      return name.includes(q) || mat.includes(q) || ws.includes(q)
    })
  }
  return list
})

const sortedFailures = computed(() => {
  const result = [...filteredFailures.value]
  const field = sortBy.value
  const order = sortOrder.value === 'asc' ? 1 : -1
  
  result.sort((a, b) => {
    let valA = '', valB = ''
    if (field === 'operatorName') {
      valA = (a.operatorName || '').toLowerCase()
      valB = (b.operatorName || '').toLowerCase()
    } else if (field === 'employeeId') {
      valA = (a.employeeId || '').toLowerCase()
      valB = (b.employeeId || '').toLowerCase()
    } else if (field === 'workstationName') {
      valA = (a.workstationName || '').toLowerCase()
      valB = (b.workstationName || '').toLowerCase()
    } else if (field === 'failedCount') {
      return ((a.failedCount || 0) - (b.failedCount || 0)) * order
    }
    
    if (valA < valB) return -1 * order
    if (valA > valB) return 1 * order
    return 0
  })
  
  return result
})

const paginatedFailures = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return sortedFailures.value.slice(start, end)
})

const totalPages = computed(() => {
  return Math.ceil(filteredFailures.value.length / pageSize.value) || 1
})

const handleSort = (field) => {
  if (sortBy.value === field) {
    sortOrder.value = sortOrder.value === 'asc' ? 'desc' : 'asc'
  } else {
    sortBy.value = field
    sortOrder.value = 'asc'
  }
}

watch([searchQuery, pageSize], () => {
  currentPage.value = 1
})

function formatFailureType(type) {
  if (!type) return '-'
  if (type.includes('NOUVELLE_RECRUE')) return 'Évaluation nouvelle recrue'
  if (type.includes('RECYCLAGE')) return 'Évaluation de recyclage'
  if (type.includes('ANNUELLE')) return 'Évaluation annuelle'
  if (type.includes('INITIAL')) return 'Évaluation initiale'
  return type
}

onMounted(fetchFailures)
</script>
