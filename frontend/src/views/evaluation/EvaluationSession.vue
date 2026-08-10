<template>
  <div class="space-y-6">
    <button @click="$router.back()" class="flex items-center gap-2 text-gray-500 hover:text-gray-700">
      <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"></path></svg>
      Retour
    </button>

    <!-- ====== VIEW 1: Existing Session Detail ====== -->
    <div v-if="session">
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-2xl font-bold text-gray-900">Evaluation: {{ session.operatorName }}</h1>
          <p class="text-sm text-gray-500 mt-1">
            Template: {{ session.templateName }}
            <span v-if="session.formationId"> | Formation #{{ session.formationId }}</span>
            <span v-if="session.projectName"> | Projet: {{ session.projectName }}</span>
            <span v-if="session.workstationName"> | Poste: {{ session.workstationName }}</span>
            | Evaluateur: {{ session.evaluatorName || 'N/A' }}
          </p>
        </div>
        <span :class="sessionStatusClass(session.status)" class="text-sm font-semibold px-3 py-1 rounded-full">
          {{ sessionStatusLabel(session.status) }}
        </span>
      </div>

      <!-- Seniority info -->
      <div class="bg-amber-50 border border-amber-200 rounded-lg p-4 mt-4">
        <p class="text-sm font-medium text-amber-800">Anciennete: {{ session.seniorityMonths }} mois</p>
        <p class="text-xs text-amber-600 mt-1">
          Niveau I: &lt;6 mois, score 70%+ |
          Niveau L: 6+ mois, score 81%+ |
          Niveau U: 12+ mois, score 91%+
        </p>
      </div>

      <!-- Questions by section -->
      <div v-for="section in session.sections || []" :key="section.id || section.title" class="bg-white rounded-xl border p-5 mt-4">
        <div class="mb-4 flex flex-wrap items-center justify-between gap-2 border-b pb-2">
          <h2 class="text-lg font-bold text-gray-900">{{ section.title || 'Questions sans section' }}</h2>
          <div class="flex gap-2 text-xs">
            <span class="rounded bg-indigo-50 px-1.5 py-0.5 text-indigo-700">Domaine : {{ domainLabel(section.domain) }}</span>
            <span class="rounded bg-slate-100 px-1.5 py-0.5 text-slate-700">Responsable : {{ roleLabel(section.responsibleRole) }}</span>
          </div>
        </div>
        <p v-if="section.description" class="mb-3 text-sm text-gray-500">{{ section.description }}</p>
        <div v-for="q in section.questions" :key="q.id" class="py-3 border-b last:border-0">
          <div class="flex items-start gap-4">
            <span class="text-sm text-gray-400 w-8 shrink-0">{{ q.questionNumber }}</span>
            <div class="flex-1">
              <div class="flex flex-wrap items-center gap-2">
                <p class="text-gray-900 font-medium">{{ q.questionText }}</p>
              </div>
              <p v-if="q.expectedAnswer" class="text-xs text-gray-500 mt-1">Réponse espérée : {{ q.expectedAnswer }}</p>
              <p class="text-xs text-gray-400 mt-1">Contrôle binaire : 0 / 1</p>
            </div>
            <div v-if="canEditQuestion(section)" class="flex items-center gap-2">
              <button @click="setAnswer(q.id, 1)" :class="answers[q.id] === 1 ? 'bg-green-600 ring-2 ring-green-300' : 'bg-gray-200'" class="w-10 h-10 rounded-lg text-white font-bold hover:bg-green-700 transition" title="Conforme">1</button>
              <button @click="setAnswer(q.id, 0)" :class="answers[q.id] === 0 ? 'bg-red-600 ring-2 ring-red-300' : 'bg-gray-200'" class="w-10 h-10 rounded-lg text-white font-bold hover:bg-red-700 transition" title="Non conforme">0</button>
            </div>
            <span v-else class="text-sm font-medium text-gray-400">
              {{ section.domain === 'PRODUCTION' && !session.productionEnabled ? 'Verrouillée jusqu’à validation des questions communes' : (getAnswerForQuestion(q.id) === undefined ? 'Non renseignée' : getAnswerForQuestion(q.id)) }}
            </span>
          </div>
          <div v-if="canEditQuestion(section)" class="ml-12 mt-2">
            <label class="block text-xs text-gray-500">Note / observation de l'évaluateur</label>
            <textarea :value="comments[q.id] || ''" @input="setComment(q.id, $event.target.value)" :disabled="answers[q.id] === undefined" rows="2" placeholder="Sélectionnez d'abord 0 ou 1, puis ajoutez une observation si nécessaire." class="mt-1 w-full rounded border border-gray-300 px-3 py-2 text-sm disabled:bg-gray-100 disabled:text-gray-400"></textarea>
          </div>
          <p v-else-if="getCommentForQuestion(q.id)" class="ml-12 mt-2 text-sm text-gray-600"><span class="font-medium">Observation :</span> {{ getCommentForQuestion(q.id) }}</p>
        </div>
      </div>

      <!-- Action buttons -->
      <div v-if="session.status === 'IN_PROGRESS'" class="flex gap-3 mt-4">
        <button @click="saveAnswers" :disabled="saving" class="bg-blue-600 text-white px-5 py-2.5 rounded-lg text-sm hover:bg-blue-700 disabled:opacity-50">
          {{ saving ? 'Sauvegarde...' : 'Sauvegarder' }}
        </button>
        <button @click="completeEval" :disabled="saving" class="bg-emerald-600 text-white px-5 py-2.5 rounded-lg text-sm hover:bg-emerald-700 disabled:opacity-50">
          Terminer l'evaluation
        </button>
      </div>

      <!-- Results -->
      <div v-if="session.status !== 'IN_PROGRESS' && session.totalQuestions" class="bg-white rounded-xl border p-6 mt-4">
        <h2 class="text-lg font-bold mb-4">Resultats</h2>
        <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
          <div class="bg-gray-50 rounded-lg p-4 text-center">
            <p class="text-xs text-gray-500">Partie commune</p>
            <p class="text-xl font-bold" :class="session.genericPercentage >= 100 ? 'text-green-600' : 'text-red-600'">{{ session.genericPercentage }}%</p>
            <p class="text-xs text-gray-400">{{ session.genericCorrect }}/{{ session.genericTotal }}</p>
          </div>
          <div class="bg-gray-50 rounded-lg p-4 text-center">
            <p class="text-xs text-gray-500">Production</p>
            <p class="text-xl font-bold text-blue-600">{{ session.productionPercentage }}%</p>
            <p class="text-xs text-gray-400">{{ session.productionCorrect }}/{{ session.productionTotal }}</p>
          </div>
          <div class="bg-gray-50 rounded-lg p-4 text-center">
            <p class="text-xs text-gray-500">Score Total</p>
            <p class="text-xl font-bold text-gray-900">{{ session.scorePercentage }}%</p>
            <p class="text-xs text-gray-400">{{ session.correctAnswers }}/{{ session.totalQuestions }}</p>
          </div>
          <div class="bg-gray-50 rounded-lg p-4 text-center">
            <p class="text-xs text-gray-500">Niveau</p>
            <p class="text-2xl font-bold" :class="niveauClass(session.niveau)">{{ session.niveau || '-' }}</p>
          </div>
        </div>
        <div v-if="session.decision === 'FAILED'" class="mt-4 bg-red-50 border border-red-200 rounded-lg p-4 text-red-800">
          <p class="font-bold">Echec</p>
          <p class="text-sm mt-1">Le score de production ne permet pas d'attribuer le niveau correspondant a l'anciennete.</p>
        </div>
        <div v-else-if="session.decision?.startsWith('PASSED_')" class="mt-4 bg-green-50 border border-green-200 rounded-lg p-4 text-green-800">
          <p class="font-bold">Reussi - Niveau {{ session.niveau }}</p>
          <p v-if="session.niveau === 'L'" class="text-sm mt-1">Pour passer au niveau U: evaluation Animation requise apres 1 an d'anciennete.</p>
        </div>
      </div>
    </div>

    <!-- ====== VIEW 2: No session - show pending evaluations from completed 12j suivi ====== -->
    <div v-else>
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-2xl font-bold text-gray-900">Evaluations a effectuer</h1>
          <p class="text-sm text-gray-500 mt-1">Operateurs ayant reussi le suivi 12j et necessitant une evaluation</p>
        </div>
        <button @click="loadPendingEvaluations" class="bg-gray-100 text-gray-700 px-4 py-2 rounded-lg text-sm hover:bg-gray-200">
          Rafraichir
        </button>
      </div>

      <div v-if="loading" class="text-center py-12 text-gray-400">Chargement...</div>

      <div v-else-if="pendingEvaluations.length === 0" class="bg-white rounded-xl border p-12 text-center mt-4">
        <svg class="w-16 h-16 mx-auto text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
        <p class="mt-4 text-gray-400 text-lg">Aucun operateur en attente d'evaluation</p>
        <p class="text-sm text-gray-300 mt-1">Les operateurs apparaitront ici apres reussite du suivi 12 jours</p>
      </div>

      <div v-else class="space-y-3 mt-4">
        <div v-for="pe in pendingEvaluations" :key="pe.formationId" class="bg-white rounded-xl border p-5 hover:shadow-md transition">
          <div class="flex items-center justify-between">
            <div>
              <h3 class="font-semibold text-gray-900 text-lg">{{ pe.operatorName }}</h3>
              <p class="text-sm text-gray-500">{{ pe.operatorEmployeeId }} <span v-if="pe.projectName">| Projet: {{ pe.projectName }} </span>| Poste: {{ pe.workstationName }} | Anciennete: {{ pe.seniorityMonths }} mois</p>
            </div>
            <button @click="goToStartEvaluation(pe)"
              class="bg-emerald-600 text-white px-5 py-2.5 rounded-lg hover:bg-emerald-700 font-medium">
              Commencer l'evaluation
            </button>
          </div>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { evaluationApi } from '@/api/endpoints'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const session = ref(null)
const answers = reactive({})
const comments = reactive({})
const dirtyAnswers = reactive(new Set())
const loading = ref(true)
const saving = ref(false)
const pendingEvaluations = ref([])

const sessionStatusClass = (s) => ({
  IN_PROGRESS: 'bg-blue-100 text-blue-700',
  PASSED: 'bg-green-100 text-green-700',
  FAILED: 'bg-red-100 text-red-700',
  BLOCKED: 'bg-red-100 text-red-700',
}[s] || 'bg-gray-100 text-gray-700')

const sessionStatusLabel = (s) => ({
  IN_PROGRESS: 'En cours',
  PASSED: 'Reussi',
  FAILED: 'Echoue',
  BLOCKED: 'Bloque',
}[s] || s)

const niveauClass = (n) => ({ I: 'text-amber-600', L: 'text-blue-600', U: 'text-green-600' }[n] || 'text-gray-400')

const domainLabel = (domain) => ({
  SECURITY_ENVIRONMENT: 'Sécurité / environnement',
  QUALITY: 'Qualité',
  FIVE_S: '5S',
  TRACEABILITY: 'Traçabilité',
  PRODUCTION_ALARMS: 'Alarmes production',
  PRODUCTION: 'Production',
  ANIMATION: 'Animation'
}[domain] || domain || '—')
const roleLabel = (role) => ({
  RESP_HSE: 'Responsable HSE',
  AGENT_QUALITE: 'Agent qualité',
  RESP_QUALITE: 'Responsable qualité',
  CHEF_EQUIPE: "Chef d'équipe"
}[role] || role || '—')

const canModifySection = (section) => authStore.hasAnyRole(['ADMIN', section.responsibleRole])
const canEditQuestion = (section) => session.value?.status === 'IN_PROGRESS' && canModifySection(section) && (section.domain !== 'PRODUCTION' || session.value.productionEnabled)
function setAnswer(questionId, value) { answers[questionId] = value; dirtyAnswers.add(questionId) }
function setComment(questionId, value) { comments[questionId] = value; dirtyAnswers.add(questionId) }
function getAnswerForQuestion(questionId) { return answers[questionId] }
function getCommentForQuestion(questionId) { return comments[questionId] }

async function loadPendingEvaluations() {
  loading.value = true
  try {
    const res = await evaluationApi.getAllPendingEvaluations()
    pendingEvaluations.value = res.data || []
  } catch (e) { console.error('Error loading pending', e) }
  loading.value = false
}

async function goToStartEvaluation(pe) {
  saving.value = true
  try {
    const res = await evaluationApi.startEvaluation({ operatorId: pe.operatorId, formationId: pe.formationId })
    const newSessionId = res.data.sessionId
    router.push(`/evaluation/session/${newSessionId}`)
  } catch (e) {
    alert('Erreur: ' + (e.response?.data?.message || e.message))
  }
  saving.value = false
}

async function loadSessionDetail() {
  if (!session.value) return
  try {
    const sessionRes = await evaluationApi.getSessionDetail(session.value.id || route.params.id)
    session.value = sessionRes.data
    if (sessionRes.data?.answers) {
      for (const a of sessionRes.data.answers) {
        answers[a.questionId] = a.answer
        comments[a.questionId] = a.comment || ''
      }
    }
  } catch (e) { console.error('Error loading session', e) }
}

async function saveAnswers() {
  if (!session.value) return
  saving.value = true
  try {
    const answerList = [...dirtyAnswers].map(questionId => ({
      questionId: Number(questionId),
      answer: answers[questionId],
      comment: comments[questionId] || null
    }))
    if (answerList.length) {
      const res = await evaluationApi.submitAnswers(session.value.id, answerList)
      dirtyAnswers.clear()
      session.value.productionEnabled = res.data.productionEnabled
    }
  } catch (e) { alert('Erreur: ' + (e.response?.data?.message || e.message)) }
  saving.value = false
}

async function completeEval() {
  if (!session.value) return
  if (!confirm("Terminer l'evaluation ? Cette action est definitive.")) return
  saving.value = true
  try {
    await saveAnswers()
    const res = await evaluationApi.completeEvaluation(session.value.id)
    session.value = { ...session.value, ...res.data }
  } catch (e) { alert('Erreur: ' + (e.response?.data?.message || e.message)) }
  saving.value = false
}

onMounted(async () => {
  // If route has :id, load existing session
  if (route.params.id && route.params.id !== 'new') {
    try {
      const res = await evaluationApi.getSessionDetail(route.params.id)
      session.value = res.data
      await loadSessionDetail()
      loading.value = false
      return
    } catch (e) { /* fall through to pending list */ }
  }
  // Otherwise load pending evaluations + dropdowns
  await loadPendingEvaluations()
})
</script>