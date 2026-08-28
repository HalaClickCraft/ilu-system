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

    <!-- Empty state -->
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

    <!-- Table content -->
    <div v-else class="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
      <table class="min-w-full divide-y divide-gray-200">
        <thead class="bg-gray-50">
          <tr>
            <th scope="col" class="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">Opérateur</th>
            <th scope="col" class="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">Matricule</th>
            <th scope="col" class="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">Poste de Travail</th>
            <th scope="col" class="px-6 py-3 text-center text-xs font-semibold text-gray-500 uppercase tracking-wider">Total Échecs</th>
            <th scope="col" class="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">Historique des Échecs</th>
          </tr>
        </thead>
        <tbody class="bg-white divide-y divide-gray-200">
          <tr v-for="(item, index) in failures" :key="index" class="hover:bg-gray-50">
            <td class="px-6 py-4 whitespace-nowrap">
              <div class="text-sm font-semibold text-gray-900">{{ item.operatorName }}</div>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-600">
              {{ item.employeeId }}
            </td>
            <td class="px-6 py-4 whitespace-nowrap">
              <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-50 text-blue-700 border border-blue-200">
                {{ item.workstationName }}
              </span>
            </td>
            <!-- FIX: Changed text-red-650 (invalid) to text-red-600 (valid Tailwind class) -->
            <td class="px-6 py-4 whitespace-nowrap text-center text-sm font-bold text-red-600">
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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { evaluationApi } from '@/api/endpoints'

const failures = ref([])
const loading = ref(true)
const loadError = ref('')

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
