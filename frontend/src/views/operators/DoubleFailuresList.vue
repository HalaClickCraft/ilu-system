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
    <div v-if="loading" class="flex items-center justify-center py-20">
      <div class="w-8 h-8 border-4 border-emerald-250 border-t-emerald-600 rounded-full animate-spin"></div>
    </div>

    <!-- Empty state -->
    <div v-else-if="failures.length === 0" class="bg-white rounded-xl shadow-sm border border-gray-200 p-12 text-center text-gray-400">
      <svg class="w-12 h-12 text-gray-300 mx-auto mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
      </svg>
      <p class="text-sm">Aucun opérateur en situation de double échec pour le moment.</p>
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
            <td class="px-6 py-4 whitespace-nowrap text-center text-sm font-bold text-red-650">
              {{ item.failedCount }}
            </td>
            <td class="px-6 py-4 text-xs text-gray-600">
              <ul class="space-y-1.5 list-disc list-inside">
                <li v-for="(f, fIdx) in item.failures" :key="fIdx">
                  <span class="font-medium text-gray-800">{{ f.type }}</span> le <span class="font-semibold">{{ f.date }}</span>
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

async function fetchFailures() {
  loading.value = true
  try {
    const res = await evaluationApi.getDoubleFailures()
    failures.value = res.data || []
  } catch (err) {
    console.error('Error fetching double failures:', err)
  } finally {
    loading.value = false
  }
}

onMounted(fetchFailures)
</script>
