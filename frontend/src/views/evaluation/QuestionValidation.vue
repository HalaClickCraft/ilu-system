<template>
  <div class="space-y-6">
    <div>
      <h1 class="text-2xl font-bold text-gray-900">Validation des Questions</h1>
      <p class="text-sm text-gray-500 mt-1">Les questions Qualite soumises par les agents requierent votre approbation.</p>
    </div>

    <!-- Stats -->
    <div class="grid grid-cols-2 gap-4">
      <div class="bg-white rounded-xl border p-4">
        <p class="text-sm text-gray-500">En attente</p>
        <p class="text-2xl font-bold text-amber-600">{{ pendingQuestions.length }}</p>
      </div>
      <div class="bg-white rounded-xl border p-4">
        <p class="text-sm text-gray-500">Traitees cette session</p>
        <p class="text-2xl font-bold text-green-600">{{ processedCount }}</p>
      </div>
    </div>

    <!-- Pending questions list -->
    <div class="space-y-3">
      <div v-for="q in pendingQuestions" :key="q.id" class="bg-white rounded-xl border border-gray-200 p-5">
        <div class="flex items-start justify-between">
          <div class="flex-1">
            <div class="flex items-center gap-2 mb-2">
              <span class="text-xs bg-indigo-50 text-indigo-700 px-2 py-0.5 rounded">Domaine : {{ domainLabel(q.sectionDomain) }}</span>
              <span class="text-xs bg-gray-100 text-gray-600 px-2 py-0.5 rounded">Responsable : {{ roleLabel(q.responsibleRole) }}</span>
              <span class="text-xs text-gray-400">Template: {{ q.templateName }}</span>
            </div>
            <p class="text-gray-900 font-medium">{{ q.questionText }}</p>
            <p v-if="q.expectedAnswer" class="text-sm text-gray-500 mt-1">Reponse attendue: {{ q.expectedAnswer }}</p>
            <p class="text-xs text-gray-400 mt-2">Soumise par: {{ q.createdByName || 'Inconnu' }} le {{ formatDate(q.createdAt) }}</p>
          </div>
          <div class="flex gap-2 ml-4">
            <button @click="validateQuestion(q.id)" class="bg-green-600 text-white px-4 py-2 rounded-lg text-sm hover:bg-green-700 flex items-center gap-1">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path></svg>
              Valider
            </button>
            <button @click="openRejectModal(q)" class="bg-red-600 text-white px-4 py-2 rounded-lg text-sm hover:bg-red-700 flex items-center gap-1">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path></svg>
              Rejeter
            </button>
          </div>
        </div>
      </div>
      <div v-if="filteredQuestions.length === 0" class="text-center py-12 text-gray-400">
        Aucune question en attente de validation
      </div>
    </div>

    <!-- Reject modal -->
    <div v-if="showRejectModal" class="fixed inset-0 bg-black/40 z-40" @click="showRejectModal = false"></div>
    <div v-if="showRejectModal" class="fixed inset-0 flex items-center justify-center z-50">
      <div class="bg-white rounded-xl shadow-2xl w-full max-w-md p-6" @click.stop>
        <h2 class="text-lg font-bold mb-2">Rejeter la question</h2>
        <p class="text-sm text-gray-500 mb-4">{{ rejectTarget?.questionText }}</p>
        <textarea v-model="rejectReason" rows="3" class="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm" placeholder="Raison du rejet..."></textarea>
        <div class="flex gap-2 mt-4">
          <button @click="confirmReject" class="flex-1 bg-red-600 text-white py-2 rounded-lg text-sm hover:bg-red-700">Confirmer le rejet</button>
          <button @click="showRejectModal = false" class="flex-1 bg-gray-200 text-gray-700 py-2 rounded-lg text-sm hover:bg-gray-300">Annuler</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { evaluationApi } from '@/api/endpoints'

const pendingQuestions = ref([])
const showRejectModal = ref(false)
const rejectTarget = ref(null)
const rejectReason = ref('')
const processedCount = ref(0)

const domainLabel = (domain) => ({ QUALITY: 'Qualité' }[domain] || domain || 'Qualité')
const roleLabel = (role) => ({ AGENT_QUALITE: 'Agent qualité', RESP_QUALITE: 'Responsable qualité' }[role] || role || '—')

const formatDate = (d) => {
  if (!d) return ''
  return new Date(d).toLocaleDateString('fr-FR', { day: '2-digit', month: 'short', year: 'numeric' })
}

async function load() {
  try {
    const res = await evaluationApi.getPendingQuestions()
    pendingQuestions.value = res.data || []
  } catch (e) {
    console.error('Error loading pending questions', e)
  }
}

function openRejectModal(q) {
  rejectTarget.value = q
  rejectReason.value = ''
  showRejectModal.value = true
}

async function validateQuestion(id) {
  try {
    await evaluationApi.validateQuestion(id)
    processedCount.value++
    await load()
  } catch (e) {
    alert('Erreur: ' + (e.response?.data?.error || e.response?.data?.message || e.message))
  }
}

async function confirmReject() {
  if (!rejectTarget.value) return
  try {
    await evaluationApi.rejectQuestion(rejectTarget.value.id, rejectReason.value)
    processedCount.value++
    showRejectModal.value = false
    await load()
  } catch (e) {
    alert('Erreur: ' + (e.response?.data?.error || e.response?.data?.message || e.message))
  }
}

onMounted(load)
</script>