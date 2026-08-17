<template>
  <div class="p-6">
    <h1 class="text-2xl font-bold text-gray-800 mb-6">Evaluation Initiale</h1>

    <!-- Search -->
    <div class="mb-6">
      <input
        v-model="search"
        type="text"
        placeholder="Rechercher par nom ou matricule..."
        class="w-full max-w-md px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none"
      />
    </div>

    <!-- Loading -->
    <div v-if="loading" class="flex items-center justify-center py-12">
      <svg class="animate-spin h-8 w-8 text-blue-500" viewBox="0 0 24 24">
        <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" fill="none"/>
        <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
      </svg>
      <span class="ml-3 text-gray-500">Chargement...</span>
    </div>

    <!-- Empty -->
    <div v-else-if="filteredOperators.length === 0" class="text-center py-12 text-gray-400">
      <svg class="mx-auto h-16 w-16 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M9 5H7a2 2 0 00-2 2v10a2 2 0 002 2h8a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"/>
      </svg>
      <p v-if="search">Aucun resultat pour "{{ search }}"</p>
      <p v-else>Aucun operateur en attente d'evaluation</p>
    </div>

    <!-- Table -->
    <div v-else class="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
      <table class="min-w-full divide-y divide-gray-200">
        <thead class="bg-gray-50">
          <tr>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Operateur</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Matricule</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Poste</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Fin Formation</th>
            <th class="px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider">Action</th>
          </tr>
        </thead>
        <tbody class="bg-white divide-y divide-gray-200">
          <tr v-for="op in filteredOperators" :key="op.operatorId + '-' + op.formationId" class="hover:bg-gray-50">
            <td class="px-6 py-4 whitespace-nowrap">
              <div class="text-sm font-medium text-gray-900">{{ op.operatorName }}</div>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-600">{{ op.operatorEmployeeId }}</td>
            <td class="px-6 py-4 whitespace-nowrap">
              <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
                {{ op.workstationName }}
              </span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-600">
              {{ op.formationEndDate || '-' }}
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-center">
              <button
                @click="startInitialEvaluation(op)"
                :disabled="op.loading"
                class="inline-flex items-center px-4 py-2 bg-blue-600 text-white text-sm font-medium rounded-lg hover:bg-blue-700 focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
              >
                <svg v-if="op.loading" class="animate-spin -ml-1 mr-2 h-4 w-4" viewBox="0 0 24 24">
                  <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" fill="none"/>
                  <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
                </svg>
                <span v-else class="mr-1">&#9654;</span>
                Commencer
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Error Toast -->
    <div v-if="errorMsg" class="fixed bottom-6 right-6 bg-red-500 text-white px-6 py-3 rounded-lg shadow-lg max-w-md">
      <div class="flex items-start">
        <svg class="h-5 w-5 mt-0.5 mr-3 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
        </svg>
        <div>
          <p class="text-sm font-medium">Erreur</p>
          <p class="text-sm mt-1 opacity-90">{{ errorMsg }}</p>
        </div>
        <button @click="errorMsg = ''" class="ml-3 flex-shrink-0 text-white hover:text-red-100">&times;</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { evaluationApi } from '@/api/endpoints'

const router = useRouter()
const operators = ref([])
const search = ref('')
const loading = ref(true)
const errorMsg = ref('')

const filteredOperators = computed(() => {
  if (!search.value) return operators.value
  const q = search.value.toLowerCase()
  return operators.value.filter(op =>
    op.operatorName?.toLowerCase().includes(q) ||
    op.operatorEmployeeId?.toLowerCase().includes(q) ||
    op.workstationName?.toLowerCase().includes(q)
  )
})

async function fetchPending() {
  loading.value = true
  try {
    const res = await evaluationApi.getAllPendingEvaluations()
    operators.value = res.data.map(op => ({ ...op, loading: false }))
  } catch (err) {
    errorMsg.value = err.response?.data?.message || 'Erreur de chargement'
  } finally {
    loading.value = false
  }
}

async function startInitialEvaluation(op) {
  op.loading = true
  errorMsg.value = ''
  try {
    // Step 1: Auto-resolve templates (generic + production)
    const resolveRes = await evaluationApi.resolveTemplates(op.operatorId, op.formationId)
    const data = resolveRes.data

    let templateId, mode, nextTemplateId

    if (data.startWithProduction) {
      // Already passed generic, go straight to production
      templateId = data.productionTemplateId
      mode = 'INITIAL'
      nextTemplateId = null
    } else {
      // Start with generic, link to production via nextTemplateId
      templateId = data.genericTemplateId
      mode = 'INITIAL'
      nextTemplateId = data.productionTemplateId
    }

    // Step 2: Start the evaluation session
    const startRes = await evaluationApi.startEvaluation({
      operatorId: op.operatorId,
      templateId: templateId,
      formationId: op.formationId,
      mode: mode,
      nextTemplateId: nextTemplateId
    })

    const sessionId = startRes.data.sessionId
    router.push({ name: 'evaluation-session', params: { id: sessionId } })
  } catch (err) {
    const msg = err.response?.data?.message || err.response?.data || 'Erreur inconnue'
    errorMsg.value = typeof msg === 'string' ? msg : JSON.stringify(msg)
  } finally {
    op.loading = false
  }
}

onMounted(fetchPending)
</script>