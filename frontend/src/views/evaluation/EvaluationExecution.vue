<template>
  <div class="max-w-4xl mx-auto p-6">
    <!-- HEADER -->
    <div v-if="!showResults" class="mb-6">
      <button @click="goBack" class="text-sm text-blue-600 hover:underline mb-2 inline-block">
        &larr; Retour aux templates
      </button>
      <h1 class="text-2xl font-bold text-gray-800">
        Evaluation : {{ templateName }}
      </h1>
      <p v-if="sessionInfo.operatorName" class="text-gray-600 mt-1">
        Operateur : <strong>{{ sessionInfo.operatorName }}</strong>
        &nbsp;|&nbsp; Anciennete : <strong>{{ sessionInfo.seniorityMonths }} mois</strong>
      </p>
      <p class="text-sm text-gray-500 mt-1">
        {{ totalAnswered }} / {{ totalQuestions }} questions repondues
      </p>
      <div class="w-full bg-gray-200 rounded-full h-2.5 mt-2">
        <div
          class="bg-blue-600 h-2.5 rounded-full transition-all"
          :style="{ width: progressPercent + '%' }"
        ></div>
      </div>
    </div>

    <!-- LOADING -->
    <div v-if="loading" class="text-center py-12 text-gray-500">
      Chargement des questions...
    </div>

    <!-- QUESTIONS BY SECTION -->
    <div v-if="!loading && !showResults" v-for="section in sections" :key="section.id" class="mb-8">
      <div class="flex items-center gap-3 mb-4">
        <div class="w-1 h-6 bg-blue-600 rounded"></div>
        <h2 class="text-lg font-semibold text-gray-800">{{ section.title }}</h2>
        <span class="text-xs text-gray-400">({{ section.questions.length }} questions)</span>
      </div>

      <div
        v-for="question in section.questions"
        :key="question.id"
        class="border border-gray-200 rounded-lg p-4 mb-3 bg-white hover:shadow-sm transition"
        :class="{ 'border-l-4 border-l-green-500': getAnswer(question.id) === 1, 'border-l-4 border-l-red-500': getAnswer(question.id) === 0 }"
      >
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
            <button
              @click="setAnswer(question.id, 1)"
              class="px-4 py-2 rounded-lg text-sm font-medium transition-all"
              :class="getAnswer(question.id) === 1
                ? 'bg-green-500 text-white shadow-md'
                : 'bg-green-100 text-green-700 hover:bg-green-200'"
            >
              Correct
            </button>
            <button
              @click="setAnswer(question.id, 0)"
              class="px-4 py-2 rounded-lg text-sm font-medium transition-all"
              :class="getAnswer(question.id) === 0
                ? 'bg-red-500 text-white shadow-md'
                : 'bg-red-100 text-red-700 hover:bg-red-200'"
            >
              Incorrect
            </button>
          </div>
        </div>
        <div class="mt-3">
          <textarea
            v-model="comments[question.id]"
            placeholder="Observation / commentaire (facultatif)..."
            rows="2"
            class="w-full border border-gray-300 rounded-lg p-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-400 resize-none"
          ></textarea>
        </div>
      </div>
    </div>

    <!-- ACTION BUTTONS -->
    <div v-if="!loading && !showResults" class="flex gap-4 mt-8 sticky bottom-0 bg-white py-4 border-t">
      <button
        @click="saveAnswers"
        :disabled="totalAnswered === 0"
        class="flex-1 py-3 rounded-lg font-medium transition-all"
        :class="totalAnswered > 0
          ? 'bg-blue-600 text-white hover:bg-blue-700'
          : 'bg-gray-200 text-gray-400 cursor-not-allowed'"
      >
        Sauvegarder ({{ totalAnswered }} reponses)
      </button>
      <button
        @click="confirmFinish"
        :disabled="totalAnswered < totalQuestions"
        class="flex-1 py-3 rounded-lg font-medium transition-all"
        :class="totalAnswered >= totalQuestions
          ? 'bg-green-600 text-white hover:bg-green-700'
          : 'bg-gray-200 text-gray-400 cursor-not-allowed'"
      >
        Terminer l'evaluation
      </button>
    </div>

    <!-- RESULTS PAGE -->
    <div v-if="showResults" class="bg-white rounded-xl shadow-lg p-8">
      <h2 class="text-2xl font-bold text-center mb-6">Resultats de l'evaluation</h2>

      <div
        class="rounded-xl p-6 mb-6 text-center text-white text-lg font-bold"
        :class="resultBannerClass"
      >
        {{ resultMessage }}
      </div>

      <div class="grid grid-cols-2 gap-4 mb-6">
        <div class="bg-blue-50 rounded-lg p-4 text-center">
          <p class="text-sm text-blue-600 mb-1">Partie Generique (HSE + Qualite)</p>
          <p class="text-3xl font-bold text-blue-800">{{ results.genericPercentage }}%</p>
          <p class="text-sm text-gray-500">{{ results.genericCorrect }} / {{ results.genericTotal }}</p>
        </div>
        <div class="bg-orange-50 rounded-lg p-4 text-center">
          <p class="text-sm text-orange-600 mb-1">Partie Production</p>
          <p class="text-3xl font-bold text-orange-800">{{ results.productionPercentage }}%</p>
          <p class="text-sm text-gray-500">{{ results.productionCorrect }} / {{ results.productionTotal }}</p>
        </div>
      </div>

      <div class="bg-gray-50 rounded-lg p-6 text-center mb-6">
        <p class="text-sm text-gray-500 mb-1">Score Global</p>
        <p class="text-4xl font-bold" :class="results.scorePercentage >= 80 ? 'text-green-600' : 'text-red-600'">
          {{ results.scorePercentage }}%
        </p>
        <p class="text-sm text-gray-500">{{ results.correctAnswers }} / {{ results.totalQuestions }} reponses correctes</p>
      </div>

      <div class="grid grid-cols-2 gap-4 mb-6">
        <div class="border rounded-lg p-4 text-center">
          <p class="text-sm text-gray-500 mb-1">Niveau</p>
          <p class="text-2xl font-bold" :class="niveauClass">{{ results.niveau || '-' }}</p>
        </div>
        <div class="border rounded-lg p-4 text-center">
          <p class="text-sm text-gray-500 mb-1">Decision</p>
          <p class="text-lg font-bold" :class="decisionClass">{{ decisionLabel }}</p>
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
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { evaluationApi, operatorsApi, structureApi } from '@/api/endpoints'

const router = useRouter()
const authStore = useAuthStore()

const currentEmployeeId = computed(() => authStore?.user?.employeeId || '')

function isOwnQuestion(q) {
  return q.createdByEmployeeId && q.createdByEmployeeId === currentEmployeeId.value
}

const templates = ref([])
const loading = ref(true)
const showCreateForm = ref(false)
const workstations = ref([])

const selectedTemplate = ref(null)
const templateSections = ref([])
const loadingDetail = ref(false)

const newTemplate = ref({ name: '', description: '', type: 'GENERIC_COMMON', workstationId: null, targetNiveau: null })
const newSectionTitle = ref('')
const newQuestion = ref({ sectionId: null, questionText: '', expectedAnswer: '', complementaryQuestions: '', questionNumber: 1 })

const editingQuestionId = ref(null)
const editForm = ref({ questionText: '', expectedAnswer: '', complementaryQuestions: '', questionNumber: null, sectionId: null })

const showOperatorDialog = ref(false)
const evalTemplate = ref(null)
const operators = ref([])
const selectedOperatorId = ref(null)

onMounted(async () => {
  await loadTemplates()
  await loadWorkstations()
})

async function loadTemplates() {
  loading.value = true
  try {
    const res = await evaluationApi.getTemplates()
    templates.value = res.data
  } catch (err) {
    console.error(err)
  } finally {
    loading.value = false
  }
}

async function loadWorkstations() {
  try {
    const res = await structureApi.getWorkstations()
    workstations.value = res.data
  } catch (e) {}
}

async function createTemplate() {
  if (!newTemplate.value.name) return alert('Le nom est obligatoire')
  try {
    await evaluationApi.createTemplate(newTemplate.value)
    newTemplate.value = { name: '', description: '', type: 'GENERIC_COMMON', workstationId: null, targetNiveau: null }
    showCreateForm.value = false
    await loadTemplates()
  } catch (err) {
    alert('Erreur: ' + (err.response?.data?.error || err.message))
  }
}

async function openTemplate(t) {
  selectedTemplate.value = t
  loadingDetail.value = true
  try {
    const res = await evaluationApi.getTemplateDetail(t.id)
    const data = res.data
    selectedTemplate.value = { ...t, ...data }
    templateSections.value = data.sections || []
  } catch (err) {
    alert('Erreur: ' + err.message)
  } finally {
    loadingDetail.value = false
  }
}

async function addSection() {
  if (!newSectionTitle.value || !selectedTemplate.value) return
  try {
    await evaluationApi.addSection(selectedTemplate.value.id, {
      title: newSectionTitle.value,
      complementaryQuestions: ''
    })
    newSectionTitle.value = ''
    await openTemplate(selectedTemplate.value)
  } catch (err) {
    alert('Erreur: ' + (err.response?.data?.error || err.message))
  }
}

async function addQuestion() {
  if (!newQuestion.value.questionText || !newQuestion.value.sectionId) {
    return alert('La question et la section sont obligatoires')
  }
  try {
    await evaluationApi.addQuestion(selectedTemplate.value.id, {
      sectionId: newQuestion.value.sectionId,
      questionText: newQuestion.value.questionText,
      expectedAnswer: newQuestion.value.expectedAnswer,
      complementaryQuestions: newQuestion.value.complementaryQuestions,
      questionNumber: newQuestion.value.questionNumber
    })
    newQuestion.value = { sectionId: templateSections.value[0]?.id || null, questionText: '', expectedAnswer: '', complementaryQuestions: '', questionNumber: 1 }
    await openTemplate(selectedTemplate.value)
  } catch (err) {
    alert('Erreur: ' + (err.response?.data?.error || err.message))
  }
}

function startEditQuestion(q) {
  editingQuestionId.value = q.id
  editForm.value = {
    questionText: q.questionText,
    expectedAnswer: q.expectedAnswer,
    complementaryQuestions: q.complementaryQuestions || '',
    questionNumber: q.questionNumber,
    sectionId: q.sectionId
  }
}

async function saveEditQuestion() {
  try {
    await evaluationApi.updateQuestion(editingQuestionId.value, editForm.value)
    editingQuestionId.value = null
    await openTemplate(selectedTemplate.value)
  } catch (err) {
    alert('Erreur: ' + (err.response?.data?.error || err.message))
  }
}

async function deleteQuestion(questionId) {
  if (!confirm('Supprimer cette question?')) return
  try {
    await evaluationApi.deleteQuestion(questionId)
    await openTemplate(selectedTemplate.value)
  } catch (err) {
    alert('Erreur: ' + (err.response?.data?.error || err.message))
  }
}

async function validateTemplate() {
  if (!confirm('Publier ce template?')) return
  try {
    await evaluationApi.validateTemplate(selectedTemplate.value.id)
    await loadTemplates()
    selectedTemplate.value = null
  } catch (err) {
    alert('Erreur: ' + (err.response?.data?.error || err.message))
  }
}

async function deleteCurrentTemplate() {
  if (!confirm('Supprimer ce template brouillon?')) return
  try {
    await evaluationApi.deleteTemplate(selectedTemplate.value.id)
    await loadTemplates()
    selectedTemplate.value = null
  } catch (err) {
    alert('Erreur: ' + (err.response?.data?.error || err.message))
  }
}

async function startEvaluation(template) {
  evalTemplate.value = template
  try {
    const res = await operatorsApi.getAll()
    operators.value = res.data
    showOperatorDialog.value = true
  } catch (err) {
    alert("Erreur de chargement des operateurs")
  }
}

function confirmStartEvaluation() {
  if (!selectedOperatorId.value) {
    alert('Veuillez selectionner un operateur')
    return
  }
  showOperatorDialog.value = false
  router.push({
    path: '/evaluation/execute',
    query: {
      operatorId: selectedOperatorId.value,
      templateId: evalTemplate.value.id
    }
  })
}

const validatedCount = computed(() => {
  return templateSections.value.reduce((sum, s) => {
    return sum + s.questions.filter(q => q.status === 'VALIDATED').length
  }, 0)
})

function statusClass(status) {
  if (status === 'DRAFT') return 'bg-gray-100 text-gray-600'
  if (status === 'VALIDATED') return 'bg-green-100 text-green-700'
  if (status === 'ARCHIVED') return 'bg-yellow-100 text-yellow-700'
  return 'bg-gray-100 text-gray-500'
}
function statusLabel(status) {
  if (status === 'DRAFT') return 'BROUILLON'
  if (status === 'VALIDATED') return 'VALIDE'
  if (status === 'ARCHIVED') return 'ARCHIVE'
  return status
}
function typeLabel(type) {
  if (type === 'GENERIC_COMMON') return 'Generique'
  if (type === 'POSTE_PRODUCTION') return 'Poste de production'
  return type
}
function questionBorderClass(status) {
  if (status === 'PENDING') return 'border-l-4 border-l-yellow-400'
  if (status === 'REJECTED') return 'border-l-4 border-l-red-400'
  if (status === 'VALIDATED') return 'border-l-4 border-l-green-400'
  return 'border-l-4 border-l-gray-200'
}
function questionStatusClass(status) {
  if (status === 'PENDING') return 'bg-yellow-100 text-yellow-700'
  if (status === 'REJECTED') return 'bg-red-100 text-red-700'
  if (status === 'VALIDATED') return 'bg-green-100 text-green-700'
  return 'bg-gray-100 text-gray-500'
}
function questionStatusLabel(status) {
  if (status === 'PENDING') return 'EN ATTENTE'
  if (status === 'REJECTED') return 'REJETEE'
  if (status === 'VALIDATED') return 'VALIDEE'
  return status
}
</script>