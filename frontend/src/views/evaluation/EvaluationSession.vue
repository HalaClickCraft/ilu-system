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

      <!-- Role completion status bar -->
      <div v-if="session.status === 'IN_PROGRESS' && roleCompletionStatus.length > 0" class="bg-white rounded-xl border p-4 mt-4">
        <p class="text-sm font-semibold text-gray-700 mb-3">Avancement par role:</p>
        <div class="flex flex-wrap gap-3">
          <div v-for="rs in roleCompletionStatus" :key="rs.role"
            class="flex items-center gap-2 px-3 py-2 rounded-lg border text-sm"
            :class="rs.completed ? 'bg-green-50 border-green-300 text-green-800' : 'bg-yellow-50 border-yellow-300 text-yellow-800'">
            <svg v-if="rs.completed" class="w-5 h-5 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path>
            </svg>
            <svg v-else class="w-5 h-5 text-yellow-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path>
            </svg>
            <span class="font-medium">{{ roleLabel(rs.role) }}</span>
            <span class="text-xs opacity-75">({{ rs.answered }}/{{ rs.total }})</span>
          </div>
        </div>
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
          <!-- Current role can answer -->
          <div v-if="session.status === 'IN_PROGRESS' && canAnswerQuestion(q.validatorRole)" class="flex items-center gap-2">
            <button @click="setAnswer(q.id, 1)" :class="answers[q.id] === 1 ? 'bg-green-600 ring-2 ring-green-300' : 'bg-gray-200'" class="w-10 h-10 rounded-lg text-white font-bold hover:bg-green-700 transition">1</button>
            <button @click="setAnswer(q.id, 0)" :class="answers[q.id] === 0 ? 'bg-red-600 ring-2 ring-red-300' : 'bg-gray-200'" class="w-10 h-10 rounded-lg text-white font-bold hover:bg-red-700 transition">0</button>
          </div>
          <!-- Other role: show completed or pending -->
          <div v-else-if="session.status === 'IN_PROGRESS' && !canAnswerQuestion(q.validatorRole)" class="flex items-center gap-1">
            <span v-if="answers[q.id] !== undefined"
              class="flex items-center gap-1 text-xs bg-green-100 text-green-700 px-2 py-1 rounded">
              <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path></svg>
              {{ roleLabel(q.validatorRole) }}
            </span>
            <span v-else
              class="flex items-center gap-1 text-xs bg-yellow-50 text-yellow-600 px-2 py-1 rounded">
              <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
              {{ roleLabel(q.validatorRole) }} en attente
            </span>
          </div>
          <!-- Completed session: show final answer -->
          <span v-else class="text-xl font-bold" :class="getAnswerForQuestion(q.id) === 1 ? 'text-green-600' : 'text-red-600'">
            {{ getAnswerForQuestion(q.id) === 1 ? '1' : '0' }}
          </span>
        </div>
      </div>

       <!-- Action buttons: only Sauvegarder, no Terminer (auto-complete) -->
      <div v-if="session.status === 'IN_PROGRESS'" class="flex items-center gap-3 mt-4 relative">
        <button @click="saveAnswers" :disabled="saving" class="bg-blue-600 text-white px-5 py-2.5 rounded-lg text-sm hover:bg-blue-700 disabled:opacity-50">
          {{ saving ? 'Sauvegarde...' : 'Sauvegarder' }}
        </button>
        <!-- Inline save confirmation (like a copy success popup) -->
        <Transition name="save-pop">
          <span v-if="saveSuccess" class="inline-flex items-center gap-1.5 px-3 py-1.5 bg-green-100 text-green-700 text-sm font-medium rounded-lg border border-green-200">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" /></svg>
            Sauvegardé !
          </span>
        </Transition>
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
  <style scoped>
.save-pop-enter-active {
  transition: all 0.3s ease-out;
}
.save-pop-leave-active {
  transition: all 0.4s ease-in;
}
.save-pop-enter-from {
  opacity: 0;
  transform: translateY(6px) scale(0.9);
}
.save-pop-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}
</style>
</template>

<script setup>
import { ref, onMounted, reactive, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { evaluationApi, operatorsApi } from '@/api/endpoints'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const session = ref(null)
const templateSections = ref([])
const answers = reactive({})
const loading = ref(true)
const saving = ref(false)
const saveSuccess = ref(false)
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

const roleLabel = (role) => ({
  CHEF_EQUIPE: "Chef d'Equipe",
  AGENT_QUALITE: 'Agent Qualite',
  RESP_HSE: 'Resp. HSE',
  RESP_QUALITE: 'Resp. Qualite',
}[role] || role)

const canAnswerQuestion = (validatorRole) => {
  return authStore.hasAnyRole([validatorRole])
}

// Collect all questions flat from templateSections
const allQuestions = computed(() => {
  const qs = []
  for (const section of templateSections.value) {
    if (section.questions) {
      for (const q of section.questions) {
        qs.push(q)
      }
    }
  }
  return qs
})

// Role completion status: which roles have all their questions answered
const roleCompletionStatus = computed(() => {
  const roleMap = {}
  for (const q of allQuestions.value) {
    const role = q.validatorRole
    if (!role) continue
    if (!roleMap[role]) {
      roleMap[role] = { role, total: 0, answered: 0 }
    }
    roleMap[role].total++
    if (answers[q.id] !== undefined) {
      roleMap[role].answered++
    }
  }
  return Object.values(roleMap).map(r => ({
    ...r,
    completed: r.answered === r.total && r.total > 0,
  }))
})

// Check if ALL questions are answered (auto-complete trigger)
const allQuestionsAnswered = computed(() => {
  if (allQuestions.value.length === 0) return false
  return allQuestions.value.every(q => answers[q.id] !== undefined)
})

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
  saveSuccess.value = false
  try {
    const answerList = Object.entries(answers).map(([questionId, answer]) => ({
      questionId: Number(questionId), answer
    }))
    await evaluationApi.submitAnswers(session.value.id, answerList)

    // Show inline confirmation near the button (no page scroll)
    saveSuccess.value = true
    setTimeout(() => { saveSuccess.value = false }, 2500)

    // Auto-complete: if all questions answered, automatically complete the evaluation
    if (allQuestionsAnswered.value) {
      try {
        const res = await evaluationApi.completeEvaluation(session.value.id)
        session.value = { ...session.value, ...res.data }
        toast.info('Evaluation completee automatiquement')
      } catch (e) {
        toast.error('Erreur completion: ' + (e.response?.data?.message || e.message))
      }
    }
  } catch (e) {
    toast.error('Erreur: ' + (e.response?.data?.message || e.message))
  }
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
