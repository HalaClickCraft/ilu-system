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
      <div v-for="section in templateSections" :key="section.id || section.title" class="bg-white rounded-xl border p-5 mt-4">
        <h2 class="text-lg font-bold text-gray-900 mb-4 pb-2 border-b">{{ section.title || 'Questions sans section' }}</h2>
        <div v-for="q in section.questions" :key="q.id" class="flex items-start gap-4 py-3 border-b last:border-0">
          <span class="text-sm text-gray-400 w-8 shrink-0">{{ q.questionNumber }}</span>
          <div class="flex-1">
            <div class="flex items-center gap-2">
              <p class="text-gray-900 font-medium">{{ q.questionText }}</p>
            </div>
            <p v-if="q.expectedAnswer" class="text-xs text-gray-500 mt-1">Attendu: {{ q.expectedAnswer }}</p>
          </div>
          <div v-if="session.status === 'IN_PROGRESS'" class="flex items-center gap-2">
            <button @click="setAnswer(q.id, 1)" :class="answers[q.id] === 1 ? 'bg-green-600 ring-2 ring-green-300' : 'bg-gray-200'" class="w-10 h-10 rounded-lg text-white font-bold hover:bg-green-700 transition">1</button>
            <button @click="setAnswer(q.id, 0)" :class="answers[q.id] === 0 ? 'bg-red-600 ring-2 ring-red-300' : 'bg-gray-200'" class="w-10 h-10 rounded-lg text-white font-bold hover:bg-red-700 transition">0</button>
          </div>
          <span v-else class="text-xl font-bold" :class="getAnswerForQuestion(q.id) === 1 ? 'text-green-600' : 'text-red-600'">
            {{ getAnswerForQuestion(q.id) === 1 ? '1' : '0' }}
          </span>
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
            <p class="text-xs text-gray-500">Partie Generique (HSE+Q)</p>
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
        <div v-if="session.decision === 'BLOCKED_GENERIC'" class="mt-4 bg-red-50 border border-red-200 rounded-lg p-4 text-red-800">
          <p class="font-bold">BLOQUE - Partie generique insuffisante</p>
          <p class="text-sm mt-1">La partie generique (HSE + Qualite) doit etre a 100% pour poursuivre l'evaluation.</p>
        </div>
        <div v-else-if="session.decision === 'FAILED'" class="mt-4 bg-red-50 border border-red-200 rounded-lg p-4 text-red-800">
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
              <p class="text-sm text-gray-500">{{ pe.operatorEmployeeId }} | Poste: {{ pe.workstationName }} | Anciennete: {{ pe.seniorityMonths }} mois</p>
            </div>
            <button @click="goToStartEvaluation(pe)"
              class="bg-emerald-600 text-white px-5 py-2.5 rounded-lg hover:bg-emerald-700 font-medium">
              Commencer l'evaluation
            </button>
          </div>
        </div>
      </div>

      <!-- Manual start form -->
      <div class="bg-white rounded-xl border p-6 mt-8">
        <h2 class="text-lg font-bold mb-4">Demarrer une evaluation manuellement</h2>
        <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div>
            <label class="text-sm font-medium text-gray-700">Operateur</label>
            <select v-model="startForm.operatorId" class="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm mt-1">
              <option :value="null">-- Selectionner --</option>
              <option v-for="op in operators" :key="op.id" :value="op.id">{{ op.lastName }} {{ op.firstName }} ({{ op.employeeId }})</option>
            </select>
          </div>
          <div>
            <label class="text-sm font-medium text-gray-700">Template</label>
            <select v-model="startForm.templateId" class="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm mt-1">
              <option :value="null">-- Selectionner --</option>
              <option v-for="tpl in validatedTemplates" :key="tpl.id" :value="tpl.id">{{ tpl.name }} ({{ tpl.type }})</option>
            </select>
          </div>
          <div class="flex items-end">
            <button @click="manualStart" :disabled="!startForm.operatorId || !startForm.templateId" class="bg-emerald-600 text-white px-5 py-2 rounded-lg text-sm hover:bg-emerald-700 disabled:opacity-50 w-full">
              Demarrer
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
import { evaluationApi, operatorsApi } from '@/api/endpoints'

const route = useRoute()
const router = useRouter()
const session = ref(null)
const templateSections = ref([])
const answers = reactive({})
const loading = ref(true)
const saving = ref(false)
const pendingEvaluations = ref([])
const operators = ref([])
const validatedTemplates = ref([])

const startForm = reactive({ operatorId: null, templateId: null, formationId: null })

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

function setAnswer(questionId, value) { answers[questionId] = value }
function getAnswerForQuestion(questionId) { return answers[questionId] }

async function loadPendingEvaluations() {
  loading.value = true
  try {
    const res = await evaluationApi.getAllPendingEvaluations()
    pendingEvaluations.value = res.data || []
  } catch (e) { console.error('Error loading pending', e) }
  loading.value = false
}

async function loadDropdowns() {
  const [opsRes, tplRes] = await Promise.allSettled([
    operatorsApi.getActive(),
    evaluationApi.getTemplates()
  ])
  if (opsRes.status === 'fulfilled') operators.value = opsRes.value.data || []
  if (tplRes.status === 'fulfilled') validatedTemplates.value = (tplRes.value.data || []).filter(t => t.status === 'VALIDATED')
}

function goToStartEvaluation(pe) {
  startForm.operatorId = pe.operatorId
  startForm.formationId = pe.formationId
  const tpl = validatedTemplates.value.find(t => t.type === 'POSTE_PRODUCTION')
  if (tpl) startForm.templateId = tpl.id
  manualStart()
}

async function manualStart() {
  if (!startForm.operatorId || !startForm.templateId) return
  saving.value = true
  try {
    const payload = {
      operatorId: startForm.operatorId,
      templateId: startForm.templateId,
    }
    if (startForm.formationId) payload.formationId = startForm.formationId
    const res = await evaluationApi.startEvaluation(payload)
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
    const [sessionRes, templateRes] = await Promise.all([
      evaluationApi.getSessionDetail(session.value.id || route.params.id),
      evaluationApi.getTemplateDetail(session.value.templateId)
    ])
    session.value = sessionRes.data
    const sections = templateRes.data?.sections || []
    templateSections.value = sections
    if (sessionRes.data?.answers) {
      for (const a of sessionRes.data.answers) { answers[a.questionId] = a.answer }
    }
  } catch (e) { console.error('Error loading session', e) }
}

async function saveAnswers() {
  if (!session.value) return
  saving.value = true
  try {
    const answerList = Object.entries(answers).map(([questionId, answer]) => ({
      questionId: Number(questionId), answer
    }))
    await evaluationApi.submitAnswers(session.value.id, answerList)
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
  if (route.params.id && route.params.id !== 'new') {
    try {
      const res = await evaluationApi.getSessionDetail(route.params.id)
      session.value = res.data
      await loadSessionDetail()
      loading.value = false
      return
    } catch (e) { /* fall through to pending list */ }
  }
  await Promise.all([loadPendingEvaluations(), loadDropdowns()])
})
</script>
