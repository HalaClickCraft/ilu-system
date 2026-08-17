<template>
  <div class="space-y-6">
    <button
      @click="$router.back()"
      class="flex items-center gap-2 text-gray-500 hover:text-gray-700"
    >
      <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path
          stroke-linecap="round"
          stroke-linejoin="round"
          stroke-width="2"
          d="M15 19l-7-7 7-7"
        ></path>
      </svg>
      Retour
    </button>

    <div v-if="session">
      <!-- INITIAL MODE banner -->
      <div
        v-if="session.mode === 'INITIAL'"
        class="rounded-lg p-4 mb-2"
        :class="
          session.templateType === 'GENERIC_COMMON'
            ? 'bg-indigo-50 border border-indigo-200'
            : 'bg-blue-50 border border-blue-200'
        "
      >
        <div class="flex items-center gap-3">
          <span
            class="text-2xl font-bold"
            :class="session.templateType === 'GENERIC_COMMON' ? 'text-indigo-600' : 'text-blue-600'"
          >
            {{ session.templateType === 'GENERIC_COMMON' ? '1' : '2' }} / 2
          </span>
          <div>
            <p
              class="font-semibold"
              :class="
                session.templateType === 'GENERIC_COMMON' ? 'text-indigo-800' : 'text-blue-800'
              "
            >
              {{
                session.templateType === 'GENERIC_COMMON'
                  ? 'Etape 1 - Partie Generique'
                  : 'Etape 2 - Partie Production'
              }}
            </p>
            <p
              class="text-xs"
              :class="
                session.templateType === 'GENERIC_COMMON' ? 'text-indigo-600' : 'text-blue-600'
              "
            >
              {{
                session.templateType === 'GENERIC_COMMON'
                  ? 'Securite + Qualite + Non-conforme - 100% requis'
                  : 'Questions specifiques au poste - Niveau attribue selon anciennete'
              }}
            </p>
          </div>
        </div>
      </div>

      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-2xl font-bold text-gray-900">Evaluation: {{ session.operatorName }}</h1>
          <p class="text-sm text-gray-500 mt-1">
            Template: {{ session.templateName }}
            <span v-if="session.formationId"> | Formation #{{ session.formationId }}</span>
            | Evaluateur: {{ session.evaluatorName || 'N/A' }}
          </p>
        </div>
        <span
          :class="sessionStatusClass(session.status)"
          class="text-sm font-semibold px-3 py-1 rounded-full"
        >
          {{ sessionStatusLabel(session.status) }}
        </span>
      </div>

      <!-- Seniority info -->
      <div class="bg-amber-50 border border-amber-200 rounded-lg p-4 mt-4">
        <p class="text-sm font-medium text-amber-800">
          Anciennete: {{ session.seniorityMonths }} mois
        </p>
        <p class="text-xs text-amber-600 mt-1">
          Niveau I: &lt;6 mois, score 70%+ | Niveau L: 6+ mois, score 81%+ | Niveau U: 12+ mois,
          score 91%+
        </p>
      </div>

      <!-- Questions by section -->
      <div
        v-for="section in templateSections"
        :key="section.id || section.title"
        class="bg-white rounded-xl border p-5 mt-4 overflow-hidden"
      >
        <h2 class="text-lg font-bold text-gray-900 mb-4 pb-2 border-b">
          {{ section.title || 'Questions sans section' }}
        </h2>
        
        <div class="overflow-x-auto">
          <table class="min-w-full divide-y divide-gray-200">
            <thead class="bg-gray-50">
              <tr>
                <th scope="col" class="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider w-12">N°</th>
                <th scope="col" class="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider w-1/3">Question</th>
                <th scope="col" class="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider w-1/4">Réponse attendue</th>
                <th scope="col" class="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider w-32">Validé par</th>
                <th scope="col" class="px-4 py-3 text-center text-xs font-semibold text-gray-500 uppercase tracking-wider w-36">Évaluation</th>
                <th scope="col" class="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider w-1/4">Question Complémentaire</th>
              </tr>
            </thead>
            <tbody class="bg-white divide-y divide-gray-200">
              <tr v-for="q in section.questions" :key="q.id" class="hover:bg-gray-50">
                <!-- N° -->
                <td class="px-4 py-4 whitespace-nowrap text-sm text-gray-500 font-medium">
                  {{ q.questionNumber }}
                </td>
                
                <!-- Question -->
                <td class="px-6 py-4 text-sm font-semibold text-gray-900 break-words">
                  {{ q.questionText }}
                </td>
                
                <!-- Réponse attendue -->
                <td class="px-6 py-4 text-xs text-gray-700 break-words">
                  {{ q.expectedAnswer || '—' }}
                </td>
                
                <!-- Validé par -->
                <td class="px-4 py-4 whitespace-nowrap">
                  <span class="inline-flex px-2 py-1 rounded text-xs font-medium bg-blue-50 text-blue-700 border border-blue-200">
                    {{ q.validatorRole || '—' }}
                  </span>
                </td>
                
                <!-- Évaluation -->
                <td class="px-4 py-4 whitespace-nowrap text-center">
                  <div v-if="session.status === 'IN_PROGRESS'" class="flex items-center justify-center gap-2">
                    <button
                      @click="setAnswer(q.id, 1)"
                      :class="
                        answers[q.id] === 1
                          ? 'bg-green-600 ring-2 ring-green-300 text-white shadow'
                          : 'bg-gray-100 text-gray-700 hover:bg-gray-200 border'
                      "
                      class="w-10 h-10 rounded-lg font-bold transition-all"
                    >
                      1
                    </button>
                    <button
                      @click="setAnswer(q.id, 0)"
                      :class="
                        answers[q.id] === 0
                          ? 'bg-red-600 ring-2 ring-red-300 text-white shadow'
                          : 'bg-gray-100 text-gray-700 hover:bg-gray-200 border'
                      "
                      class="w-10 h-10 rounded-lg font-bold transition-all"
                    >
                      0
                    </button>
                  </div>
                  <span
                    v-else
                    class="inline-flex px-3 py-1 rounded-full text-sm font-bold"
                    :class="getAnswerForQuestion(q.id) === 1 ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'"
                  >
                    {{ getAnswerForQuestion(q.id) === 1 ? '1' : '0' }}
                  </span>
                </td>

                <!-- Question Complémentaire -->
                <td class="px-6 py-4 text-xs text-gray-600 break-words italic">
                  {{ q.complementaryQuestions || '—' }}
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Action buttons -->
      <div v-if="session.status === 'IN_PROGRESS'" class="flex gap-3 mt-4">
        <button
          @click="saveAnswers"
          :disabled="saving"
          class="bg-blue-600 text-white px-5 py-2.5 rounded-lg text-sm hover:bg-blue-700 disabled:opacity-50"
        >
          {{ saving ? 'Sauvegarde...' : 'Sauvegarder' }}
        </button>
        <button
          @click="completeEval"
          :disabled="saving"
          class="bg-emerald-600 text-white px-5 py-2.5 rounded-lg text-sm hover:bg-emerald-700 disabled:opacity-50"
        >
          Terminer l'evaluation
        </button>
      </div>

      <!-- PASSED_GENERIC in INITIAL mode -->
      <div
        v-if="
          session.mode === 'INITIAL' &&
          session.decision === 'PASSED_GENERIC' &&
          session.nextTemplateId
        "
        class="mt-4 bg-green-50 border border-green-200 rounded-lg p-6"
      >
        <div class="flex items-center justify-between">
          <div>
            <p class="font-bold text-green-800 text-lg">Partie Generique reussie a 100%</p>
            <p class="text-sm text-green-600 mt-1">
              L'operateur peut maintenant passer la Partie Production.
            </p>
          </div>
          <button
            @click="startProductionSession"
            :disabled="startingProduction"
            class="bg-blue-600 text-white px-6 py-3 rounded-lg hover:bg-blue-700 font-medium flex items-center gap-2 disabled:opacity-50"
          >
            <svg
              v-if="startingProduction"
              class="w-4 h-4 animate-spin"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"
              ></path>
            </svg>
            Commencer la Partie Production
          </button>
        </div>
      </div>

      <!-- Results -->
      <div
        v-if="session.status !== 'IN_PROGRESS' && session.totalQuestions"
        class="bg-white rounded-xl border p-6 mt-4"
      >
        <h2 class="text-lg font-bold mb-4">Resultats</h2>
        <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
          <div class="bg-gray-50 rounded-lg p-4 text-center">
            <p class="text-xs text-gray-500">Partie Generique</p>
            <p
              class="text-xl font-bold"
              :class="session.genericPercentage >= 100 ? 'text-green-600' : 'text-red-600'"
            >
              {{ session.genericPercentage }}%
            </p>
            <p class="text-xs text-gray-400">
              {{ session.genericCorrect }}/{{ session.genericTotal }}
            </p>
          </div>
          <div class="bg-gray-50 rounded-lg p-4 text-center">
            <p class="text-xs text-gray-500">Production</p>
            <p class="text-xl font-bold text-blue-600">{{ session.productionPercentage }}%</p>
            <p class="text-xs text-gray-400">
              {{ session.productionCorrect }}/{{ session.productionTotal }}
            </p>
          </div>
          <div class="bg-gray-50 rounded-lg p-4 text-center">
            <p class="text-xs text-gray-500">Score Total</p>
            <p class="text-xl font-bold text-gray-900">{{ session.scorePercentage }}%</p>
            <p class="text-xs text-gray-400">
              {{ session.correctAnswers }}/{{ session.totalQuestions }}
            </p>
          </div>
          <div class="bg-gray-50 rounded-lg p-4 text-center">
            <p class="text-xs text-gray-500">Niveau</p>
            <p class="text-2xl font-bold" :class="niveauClass(session.niveau)">
              {{ session.niveau || '-' }}
            </p>
          </div>
        </div>

        <div
          v-if="session.decision === 'BLOCKED_GENERIC'"
          class="mt-4 bg-red-50 border border-red-200 rounded-lg p-4 text-red-800"
        >
          <p class="font-bold">BLOQUE - Partie generique insuffisante</p>
          <p class="text-sm mt-1">
            La partie generique (HSE + Qualite) doit etre a 100% pour poursuivre l'evaluation.
          </p>
        </div>
        <div
          v-else-if="session.decision === 'FAILED'"
          class="mt-4 bg-red-50 border border-red-200 rounded-lg p-4 text-red-800"
        >
          <p class="font-bold">Echec</p>
          <p class="text-sm mt-1">
            Le score de production ne permet pas d'attribuer le niveau correspondant a l'anciennete.
          </p>
        </div>
        <div
          v-else-if="session.decision === 'PASSED_GENERIC'"
          class="mt-4 bg-green-50 border border-green-200 rounded-lg p-4 text-green-800"
        >
          <p class="font-bold">Partie Generique reussie</p>
          <p class="text-sm mt-1">
            Score: {{ session.genericPercentage }}%. L'operateur peut maintenant passer la Partie
            Production.
          </p>
        </div>
        <div
          v-else-if="session.decision?.startsWith('PASSED_')"
          class="mt-4 bg-green-50 border border-green-200 rounded-lg p-4 text-green-800"
        >
          <p class="font-bold">Reussi - Niveau {{ session.niveau }}</p>
          <p v-if="session.niveau === 'L'" class="text-sm mt-1">
            Pour passer au niveau U: evaluation Animation requise apres 1 an d'anciennete.
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { evaluationApi } from '@/api/endpoints'

const route = useRoute()
const router = useRouter()
const session = ref(null)
const templateSections = ref([])
const answers = reactive({})
const saving = ref(false)
const startingProduction = ref(false)

const sessionStatusClass = (s) =>
  ({
    IN_PROGRESS: 'bg-blue-100 text-blue-700',
    PASSED: 'bg-green-100 text-green-700',
    FAILED: 'bg-red-100 text-red-700',
    BLOCKED: 'bg-red-100 text-red-700',
  })[s] || 'bg-gray-100 text-gray-700'

const sessionStatusLabel = (s) =>
  ({
    IN_PROGRESS: 'En cours',
    PASSED: 'Reussi',
    FAILED: 'Echoue',
    BLOCKED: 'Bloque',
  })[s] || s

const niveauClass = (n) =>
  ({ I: 'text-amber-600', L: 'text-blue-600', U: 'text-green-600' })[n] || 'text-gray-400'

function setAnswer(questionId, value) {
  answers[questionId] = value
}
function getAnswerForQuestion(questionId) {
  return answers[questionId]
}

async function loadSessionDetail() {
  if (!session.value) return
  try {
    const [sessionRes, templateRes] = await Promise.all([
      evaluationApi.getSessionDetail(session.value.id || route.params.id),
      evaluationApi.getTemplateDetail(session.value.templateId),
    ])
    session.value = sessionRes.data
    const sections = templateRes.data?.sections || []
    templateSections.value = sections
    if (sessionRes.data?.answers) {
      for (const a of sessionRes.data.answers) {
        answers[a.questionId] = a.answer
      }
    }
  } catch (e) {
    console.error('Error loading session', e)
  }
}

async function saveAnswers() {
  if (!session.value) return
  saving.value = true
  try {
    const answerList = Object.entries(answers).map(([questionId, answer]) => ({
      questionId: Number(questionId),
      answer,
    }))
    await evaluationApi.submitAnswers(session.value.id, answerList)
  } catch (e) {
    alert('Erreur: ' + (e.response?.data?.message || e.message))
  }
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
  } catch (e) {
    alert('Erreur: ' + (e.response?.data?.message || e.message))
  }
  saving.value = false
}

async function startProductionSession() {
  if (!session.value?.nextTemplateId) return
  startingProduction.value = true
  try {
    const res = await evaluationApi.startEvaluation({
      operatorId: session.value.operatorId,
      templateId: session.value.nextTemplateId,
      formationId: session.value.formationId,
      mode: 'INITIAL',
      nextTemplateId: null,
    })
    router.push('/evaluation/session/' + res.data.sessionId)
  } catch (e) {
    alert('Erreur: ' + (e.response?.data?.error || e.response?.data?.message || e.message))
    startingProduction.value = false
  }
}

onMounted(async () => {
  if (route.params.id && route.params.id !== 'new') {
    try {
      const res = await evaluationApi.getSessionDetail(route.params.id)
      session.value = res.data
      await loadSessionDetail()
    } catch (e) {
      console.error('Error loading session', e)
      router.push('/evaluation/initial')
    }
  } else {
    router.push('/evaluation/initial')
  }
})
</script>
