<template>
  <div class="max-w-4xl mx-auto p-6">
    <!-- HEADER -->
    <div v-if="!showResults" class="mb-6">
      <button @click="goBack" class="text-sm text-blue-600 hover:underline mb-2 inline-block">
        &larr; Retour aux templates
      </button>
      <h1 class="text-2xl font-bold text-gray-800">Évaluation : {{ templateName }}</h1>
      <p v-if="sessionInfo.operatorName" class="text-gray-600 mt-1">
        Opérateur : <strong>{{ sessionInfo.operatorName }}</strong>
      </p>
      <p class="text-sm text-gray-500 mt-1">
        {{ totalAnswered }} / {{ totalQuestions }} questions repondues
      </p>
      <div class="w-full bg-gray-200 rounded-full h-2.5 mt-2">
        <div class="bg-blue-600 h-2.5 rounded-full transition-all" :style="{ width: progressPercent + '%' }"></div>
      </div>
    </div>

    <!-- LOADING -->
    <div v-if="loading" class="text-center py-12 text-gray-500">Chargement...</div>

    <!-- QUESTIONS BY SECTION -->
    <div v-if="!loading && !showResults" v-for="section in sections" :key="section.id" class="mb-8">
      <div class="flex items-center gap-3 mb-4">
        <div class="w-1 h-6 bg-blue-600 rounded"></div>
        <h2 class="text-lg font-semibold text-gray-800">{{ section.title }}</h2>
        <span class="text-xs text-gray-400">({{ section.questions.length }})</span>
      </div>

      <div v-for="question in section.questions" :key="question.id"
        class="border border-gray-200 rounded-lg p-4 mb-3 bg-white hover:shadow-sm transition"
        :class="{
          'border-l-4 border-l-green-500': getAnswer(question.id) === 1,
          'border-l-4 border-l-red-500': getAnswer(question.id) === 0
        }">
        <div class="flex items-start justify-between gap-4">
          <div class="flex-1">
            <p class="font-medium text-gray-800">
              <span class="text-blue-600 mr-1">{{ question.questionNumber || '#' }}.</span>
              {{ question.questionText }}
            </p>
            <p class="text-sm text-gray-500 mt-1">
              Reponse attendue : <span class="italic">{{ question.expectedAnswer }}</span>
            </p>
            <p v-if="question.complementaryQuestions" class="text-sm text-purple-600 mt-1">
              Q. complementaires : {{ question.complementaryQuestions }}
            </p>
          </div>
          <div class="flex gap-2 shrink-0">
            <button @click="setAnswer(question.id, 1)"
              class="px-4 py-2 rounded-lg text-sm font-medium transition-all"
              :class="getAnswer(question.id) === 1
                ? 'bg-green-500 text-white shadow-md'
                : 'bg-green-100 text-green-700 hover:bg-green-200'">
              Correct
            </button>
            <button @click="setAnswer(question.id, 0)"
              class="px-4 py-2 rounded-lg text-sm font-medium transition-all"
              :class="getAnswer(question.id) === 0
                ? 'bg-red-500 text-white shadow-md'
                : 'bg-red-100 text-red-700 hover:bg-red-200'">
              Incorrect
            </button>
          </div>
        </div>
        <div class="mt-3">
          <textarea v-model="comments[question.id]"
            placeholder="Observation / commentaire (facultatif)..."
            rows="2"
            class="w-full border border-gray-300 rounded-lg p-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-400 resize-none"></textarea>
        </div>
      </div>
    </div>

    <!-- ACTION BUTTONS -->
    <div v-if="!loading && !showResults" class="flex gap-4 mt-8 sticky bottom-0 bg-white py-4 border-t">
      <button @click="saveAnswers" :disabled="totalAnswered === 0"
        class="flex-1 py-3 rounded-lg font-medium transition-all"
        :class="totalAnswered > 0 ? 'bg-blue-600 text-white hover:bg-blue-700' : 'bg-gray-200 text-gray-400 cursor-not-allowed'">
        Sauvegarder ({{ totalAnswered }} reponses)
      </button>
      <button @click="confirmFinish" :disabled="totalAnswered < totalQuestions"
        class="flex-1 py-3 rounded-lg font-medium transition-all"
        :class="totalAnswered >= totalQuestions ? 'bg-green-600 text-white hover:bg-green-700' : 'bg-gray-200 text-gray-400 cursor-not-allowed'">
        Terminer l'évaluation
      </button>
    </div>

    <!-- RESULTS PAGE -->
    <div v-if="showResults" class="bg-white rounded-xl shadow-lg p-8">
      <h2 class="text-2xl font-bold text-center mb-6">Resultats de l'évaluation</h2>

      <div class="rounded-xl p-6 mb-6 text-center text-white text-lg font-bold" :class="resultBannerClass">
        {{ resultMessage }}
      </div>

      <div class="grid grid-cols-2 gap-4 mb-6">
        <div class="bg-blue-50 rounded-lg p-4 text-center">
          <p class="text-sm text-blue-600 mb-1">Partie Générique (HSE + Qualité)</p>
          <p class="text-3xl font-bold text-blue-800">{{ results.genericPercentage ?? '-' }}%</p>
          <p class="text-sm text-gray-500">{{ results.genericCorrect ?? 0 }} / {{ results.genericTotal ?? 0 }}</p>
        </div>
        <div class="bg-orange-50 rounded-lg p-4 text-center">
          <p class="text-sm text-orange-600 mb-1">Partie Production</p>
          <p class="text-3xl font-bold text-orange-800">{{ results.productionPercentage ?? '-' }}%</p>
          <p class="text-sm text-gray-500">{{ results.productionCorrect ?? 0 }} / {{ results.productionTotal ?? 0 }}</p>
        </div>
      </div>

      <div class="bg-gray-50 rounded-lg p-6 text-center mb-6">
        <p class="text-sm text-gray-500 mb-1">Score Global</p>
        <p class="text-4xl font-bold" :class="results.scorePercentage >= 80 ? 'text-green-600' : 'text-red-600'">
          {{ results.scorePercentage ?? 0 }}%
        </p>
        <p class="text-sm text-gray-500">{{ results.correctAnswers ?? 0 }} / {{ results.totalQuestions ?? 0 }} reponses correctes</p>
      </div>

      <div class="grid grid-cols-2 gap-4 mb-6">
        <div class="border rounded-lg p-4 text-center">
          <p class="text-sm text-gray-500 mb-1">Niveau</p>
          <p class="text-2xl font-bold" :class="niveauClass">{{ results.niveau || '-' }}</p>
        </div>
        <div class="border rounded-lg p-4 text-center">
          <p class="text-sm text-gray-500 mb-1">Decision</p>
          <p class="text-lg font-bold" :class="decisionClass">{{ results.decision || '-' }}</p>
        </div>
      </div>

      <div class="text-center">
        <button @click="goBack" class="bg-blue-600 text-white px-8 py-3 rounded-lg hover:bg-blue-700">
          Retour aux templates
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { evaluationApi } from '@/api/endpoints'

const route = useRoute()
const router = useRouter()

const sessionId = ref(null)
const templateName = ref('')
const sessionInfo = ref({})
const sections = ref([])
const answers = ref({})
const comments = ref({})
const loading = ref(true)
const showResults = ref(false)
const results = ref({})

onMounted(async () => {
  const operatorId = route.query.operatorId
  const templateId = route.query.templateId

  if (!operatorId || !templateId) {
    alert('Parametres manquants')
    goBack()
    return
  }

  try {
    // 1) Start session
    const startRes = await evaluationApi.startEvaluation({
      operatorId: Number(operatorId),
      templateId: Number(templateId)
    })
    sessionId.value = startRes.data?.sessionId || startRes.data?.id
    templateName.value = startRes.data?.templateName || ''

    // 2) Get session detail (questions)
    const detailRes = await evaluationApi.getSessionDetail(sessionId.value)
    const detail = detailRes.data
    sessionInfo.value = detail
    sections.value = detail.sections || []

    // Pre-fill saved answers if any
    if (detail.answers && detail.answers.length > 0) {
      detail.answers.forEach(a => {
        answers.value[a.questionId] = a.answer
        if (a.comment) comments.value[a.questionId] = a.comment
      })
    }
  } catch (err) {
    console.error('Error starting evaluation:', err)
    alert('Erreur: ' + (err.response?.data?.error || err.message))
    goBack()
  } finally {
    loading.value = false
  }
})

const allQuestions = computed(() => {
  return sections.value.flatMap(s => s.questions || [])
})

const totalQuestions = computed(() => allQuestions.value.length)

const totalAnswered = computed(() => {
  return allQuestions.value.filter(q => answers.value[q.id] !== undefined).length
})

const progressPercent = computed(() => {
  if (totalQuestions.value === 0) return 0
  return Math.round((totalAnswered.value / totalQuestions.value) * 100)
})

function getAnswer(questionId) {
  return answers.value[questionId]
}

function setAnswer(questionId, value) {
  answers.value[questionId] = value
}

async function saveAnswers() {
  try {
    const answersList = allQuestions.value.map(q => ({
      questionId: q.id,
      answer: answers.value[q.id] !== undefined ? answers.value[q.id] : 0,
      comment: comments.value[q.id] || ''
    }))
    await evaluationApi.submitAnswers(sessionId.value, answersList)
    alert('Reponses sauvegardees')
  } catch (err) {
    alert('Erreur: ' + (err.response?.data?.error || err.message))
  }
}

function confirmFinish() {
  if (totalAnswered.value < totalQuestions.value) {
    alert('Veuillez repondre a toutes les questions')
    return
  }
  if (!confirm('Terminer l\'evaluation ? Cette action est irreversible.')) return
  finishEvaluation()
}

async function finishEvaluation() {
  try {
    const answersList = allQuestions.value.map(q => ({
      questionId: q.id,
      answer: answers.value[q.id] !== undefined ? answers.value[q.id] : 0,
      comment: comments.value[q.id] || ''
    }))
    await evaluationApi.submitAnswers(sessionId.value, answersList)

    const res = await evaluationApi.completeEvaluation(sessionId.value)
    results.value = res.data
    showResults.value = true
  } catch (err) {
    alert('Erreur: ' + (err.response?.data?.error || err.message))
  }
}

const resultMessage = computed(() => {
  const p = results.value?.scorePercentage
  if (p === undefined || p === null) return ''
  if (p >= 80) return 'EVALUATION REUSSIE'
  if (p >= 50) return 'EVALUATION PARTIELLE'
  return 'EVALUATION NON REUSSIE'
})

const resultBannerClass = computed(() => {
  const p = results.value?.scorePercentage
  if (p === undefined || p === null) return 'bg-gray-500'
  if (p >= 80) return 'bg-green-600'
  if (p >= 50) return 'bg-yellow-500'
  return 'bg-red-600'
})

const niveauClass = computed(() => {
  const n = results.value?.niveau
  if (!n) return 'text-gray-600'
  const nl = n.toLowerCase()
  if (nl.includes('l3') || nl.includes('3')) return 'text-green-600'
  if (nl.includes('l2') || nl.includes('2')) return 'text-yellow-600'
  return 'text-orange-600'
})

const decisionClass = computed(() => {
  const d = results.value?.decision
  if (!d) return 'text-gray-600'
  const dl = d.toLowerCase()
  if (dl.includes('valide') || dl.includes('qualifie')) return 'text-green-600'
  if (dl.includes('non')) return 'text-red-600'
  return 'text-yellow-600'
})

function goBack() {
  router.push({ name: 'evaluation-templates' })
}
</script>