<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">Gestion des Templates d'Evaluation</h1>
        <p class="text-sm text-gray-500 mt-1">Creer et gerer les templates de questions par poste</p>
      </div>
      <button v-if="canManageTemplates" @click="openCreateModal" class="bg-emerald-600 text-white px-4 py-2 rounded-lg hover:bg-emerald-700 flex items-center gap-2">
        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"></path></svg>
        Nouveau Template
      </button>
    </div>

    <div class="flex gap-2 border-b border-gray-200">
      <button v-for="t in typeTabs" :key="t.key" @click="activeType = t.key"
        class="px-4 py-2 text-sm font-medium border-b-2 transition"
        :class="activeType === t.key ? 'border-emerald-600 text-emerald-600' : 'border-transparent text-gray-500 hover:text-gray-700'">
        {{ t.label }} ({{ countByType(t.key) }})
      </button>
    </div>

    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
      <div v-for="tpl in filteredTemplates" :key="tpl.id" @click="selectTemplate(tpl)"
        class="bg-white rounded-xl border border-gray-200 p-4 cursor-pointer hover:shadow-md hover:border-emerald-300 transition">
        <div class="flex items-start justify-between">
          <div>
            <h3 class="font-semibold text-gray-900">{{ tpl.name }}</h3>
            <p v-if="tpl.description" class="text-sm text-gray-500 mt-1">{{ tpl.description }}</p>
          </div>
          <span :class="statusClass(tpl.status)" class="text-xs font-medium px-2 py-1 rounded-full">{{ statusLabel(tpl.status) }}</span>
        </div>
        <div class="mt-3 flex items-center gap-2 text-xs text-gray-500">
          <span class="bg-gray-100 px-2 py-0.5 rounded">{{ typeLabel(tpl.type) }}</span>
          <span v-if="tpl.workstationName" class="bg-blue-50 text-blue-700 px-2 py-0.5 rounded">{{ tpl.workstationName }}</span>
          <span v-if="tpl.targetNiveau" class="bg-amber-50 text-amber-700 px-2 py-0.5 rounded">Niveau {{ tpl.targetNiveau }}</span>
          <span>{{ tpl.validatedQuestionCount || 0 }} questions</span>
        </div>
      </div>
      <div v-if="filteredTemplates.length === 0" class="col-span-full text-center py-12 text-gray-400">Aucun template trouve</div>
    </div>

    <!-- Detail Panel -->
    <div v-if="selectedTemplate" class="fixed inset-0 bg-black/40 z-40" @click="closePanel"></div>
    <div v-if="selectedTemplate" class="fixed right-0 top-0 h-full w-full max-w-2xl bg-white shadow-2xl z-50 overflow-y-auto">
      <div class="p-6">
        <div class="flex items-center justify-between mb-6">
          <h2 class="text-xl font-bold">{{ selectedTemplate.name }}</h2>
          <button @click="closePanel" class="p-2 hover:bg-gray-100 rounded-lg">X</button>
        </div>

        <div class="grid grid-cols-2 gap-4 mb-6 p-4 bg-gray-50 rounded-lg">
          <div><span class="text-xs text-gray-500">Type</span><p class="font-medium">{{ typeLabel(selectedTemplate.type) }}</p></div>
          <div><span class="text-xs text-gray-500">Statut</span><p><span :class="statusClass(selectedTemplate.status)" class="text-xs font-medium px-2 py-0.5 rounded-full">{{ statusLabel(selectedTemplate.status) }}</span></p></div>
          <div v-if="selectedTemplate.workstationName"><span class="text-xs text-gray-500">Poste</span><p class="font-medium">{{ selectedTemplate.workstationName }}</p></div>
          <div><span class="text-xs text-gray-500">Questions</span><p class="font-medium">{{ questionCount }}</p></div>
        </div>

        <!-- Template actions: edit / delete / validate -->
        <div v-if="canManageTemplates" class="flex flex-wrap gap-2 mb-6">
          <button v-if="selectedTemplate.status === 'DRAFT'" @click="openEditModal" class="bg-blue-600 text-white px-3 py-2 rounded-lg text-sm hover:bg-blue-700">Modifier le Template</button>
          <button v-if="selectedTemplate.status === 'DRAFT'" @click="deleteTemplate" class="bg-red-600 text-white px-3 py-2 rounded-lg text-sm hover:bg-red-700">Supprimer le Template</button>
          <button v-if="isResponsable && selectedTemplate.status === 'DRAFT'" @click="validateThisTemplate" class="bg-green-600 text-white px-3 py-2 rounded-lg text-sm hover:bg-green-700">Valider le Template</button>
        </div>

        <!-- Add Section -->
        <div v-if="canManageTemplates && selectedTemplate.status === 'DRAFT'" class="mb-4 flex gap-2">
          <input v-model="newSectionTitle" placeholder="Nom de la section..." class="flex-1 border border-gray-300 rounded-lg px-3 py-2 text-sm" @keyup.enter="addSection">
          <button @click="addSection" class="bg-emerald-600 text-white px-3 py-2 rounded-lg text-sm hover:bg-emerald-700">+ Section</button>
        </div>

        <!-- Sections & Questions -->
        <div v-for="section in templateDetail?.sections" :key="section.id" class="mb-6">
          <h3 class="font-semibold text-gray-800 flex items-center gap-2 mb-3">
            <span class="w-6 h-6 bg-emerald-100 text-emerald-700 rounded-full flex items-center justify-center text-xs font-bold">{{ section.displayOrder + 1 }}</span>
            {{ section.title }}
            <span class="text-xs text-gray-400">({{ section.questions.length }} questions)</span>
          </h3>

          <div v-for="q in section.questions" :key="q.id" class="ml-8 mb-2 p-3 bg-gray-50 rounded-lg border-l-4 border-emerald-500">
            <div class="flex items-start justify-between">
              <div class="flex-1">
                <p class="text-sm font-medium text-gray-800">
                  <span class="text-gray-400 mr-1">{{ q.questionNumber }}.</span>{{ q.questionText }}
                </p>
                <p v-if="q.expectedAnswer" class="text-xs text-gray-500 mt-1">Reponse: {{ q.expectedAnswer }}</p>
              </div>
              <div v-if="canManageTemplates && selectedTemplate.status === 'DRAFT'" class="flex gap-1 ml-2">
                <button @click="editQuestion(q)" class="text-blue-600 hover:text-blue-800 p-1" title="Modifier">✏️</button>
                <button @click="deleteQuestion(q.id)" class="text-red-600 hover:text-red-800 p-1" title="Supprimer">🗑️</button>
              </div>
            </div>
          </div>

          <div v-if="canManageTemplates && selectedTemplate.status === 'DRAFT'" class="ml-8 mt-2 space-y-2 mb-2">
            <input v-model="newQuestion[section.id].text" placeholder="Question..." class="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm">
            <input v-model="newQuestion[section.id].expected" placeholder="Reponse attendue..." class="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm">
            <button @click="addQuestionToSection(section.id)" class="bg-emerald-600 text-white px-3 py-1.5 rounded-lg text-sm hover:bg-emerald-700">+ Ajouter Question</button>
          </div>
        </div>

        <div v-if="!templateDetail?.sections?.length" class="text-center py-8 text-gray-400">Aucune section. Ajoutez une section pour commencer.</div>
      </div>
    </div>

    <!-- Create Template Modal -->
    <div v-if="showCreateModal" class="fixed inset-0 bg-black/40 z-40" @click="showCreateModal = false"></div>
    <div v-if="showCreateModal" class="fixed inset-0 flex items-center justify-center z-50">
      <div class="bg-white rounded-xl shadow-2xl w-full max-w-md p-6" @click.stop>
        <h2 class="text-lg font-bold mb-4">Nouveau Template</h2>
        <div class="space-y-3">
          <div>
            <label class="text-sm font-medium text-gray-700">Nom *</label>
            <input v-model="newTemplate.name" class="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm mt-1" placeholder="Ex: RAC SC">
          </div>
          <div>
            <label class="text-sm font-medium text-gray-700">Description</label>
            <textarea v-model="newTemplate.description" rows="2" class="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm mt-1"></textarea>
          </div>
          <div>
            <label class="text-sm font-medium text-gray-700">Type *</label>
            <select v-model="newTemplate.type" class="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm mt-1">
              <option value="GENERIC_COMMON">Partie Generique</option>
              <option value="POSTE_PRODUCTION">Poste - Production</option>
              <option value="ANIMATION">Animation (L vers U)</option>
            </select>
          </div>
          <div v-if="newTemplate.type === 'POSTE_PRODUCTION' || newTemplate.type === 'ANIMATION'">
            <label class="text-sm font-medium text-gray-700">Poste de travail</label>
            <select v-model="newTemplate.workstationId" class="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm mt-1">
              <option :value="null">-- Selectionner --</option>
              <option v-for="ws in workstations" :key="ws.id" :value="ws.id">{{ ws.name }}</option>
            </select>
          </div>
          <div class="flex gap-2 pt-2">
            <button @click="createTemplate" class="flex-1 bg-emerald-600 text-white py-2 rounded-lg text-sm hover:bg-emerald-700">Creer</button>
            <button @click="showCreateModal = false" class="flex-1 bg-gray-200 text-gray-700 py-2 rounded-lg text-sm hover:bg-gray-300">Annuler</button>
          </div>
        </div>
      </div>
    </div>

    <!-- Edit Template Modal -->
    <div v-if="showEditModal" class="fixed inset-0 bg-black/40 z-40" @click="showEditModal = false"></div>
    <div v-if="showEditModal" class="fixed inset-0 flex items-center justify-center z-50">
      <div class="bg-white rounded-xl shadow-2xl w-full max-w-md p-6" @click.stop>
        <h2 class="text-lg font-bold mb-4">Modifier le Template</h2>
        <div class="space-y-3">
          <div>
            <label class="text-sm font-medium text-gray-700">Nom *</label>
            <input v-model="editTemplateData.name" class="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm mt-1">
          </div>
          <div>
            <label class="text-sm font-medium text-gray-700">Description</label>
            <textarea v-model="editTemplateData.description" rows="2" class="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm mt-1"></textarea>
          </div>
          <div>
            <label class="text-sm font-medium text-gray-700">Niveau cible</label>
            <select v-model="editTemplateData.targetNiveau" class="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm mt-1">
              <option :value="null">-- Aucun --</option>
              <option value="I">Niveau I</option>
              <option value="L">Niveau L</option>
              <option value="U">Niveau U</option>
            </select>
          </div>
          <div class="flex gap-2 pt-2">
            <button @click="saveTemplateEdit" class="flex-1 bg-blue-600 text-white py-2 rounded-lg text-sm hover:bg-blue-700">Enregistrer</button>
            <button @click="showEditModal = false" class="flex-1 bg-gray-200 text-gray-700 py-2 rounded-lg text-sm hover:bg-gray-300">Annuler</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, reactive } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { evaluationApi, structureApi } from '@/api/endpoints'

const authStore = useAuthStore()
const canManageTemplates = computed(() => authStore.hasAnyRole(['ADMIN', 'AGENT_QUALITE', 'RESP_HSE', 'RESP_QUALITE', 'CHEF_EQUIPE']))
const isResponsable = computed(() => authStore.hasAnyRole(['RESP_QUALITE', 'RESP_HSE', 'ADMIN']))

const templates = ref([])
const workstations = ref([])
const activeType = ref('ALL')
const selectedTemplate = ref(null)
const templateDetail = ref(null)
const showCreateModal = ref(false)
const showEditModal = ref(false)
const newSectionTitle = ref('')
const newQuestion = reactive({})

const newTemplate = reactive({ name: '', description: '', type: 'POSTE_PRODUCTION', workstationId: null })
const editTemplateData = reactive({ name: '', description: '', targetNiveau: null })

const typeTabs = [
  { key: 'ALL', label: 'Tous' },
  { key: 'GENERIC_COMMON', label: 'Partie Generique' },
  { key: 'POSTE_PRODUCTION', label: 'Poste Production' },
  { key: 'ANIMATION', label: 'Animation' },
]

const filteredTemplates = computed(() => activeType.value === 'ALL' ? templates.value : templates.value.filter(t => t.type === activeType.value))
const countByType = (type) => type === 'ALL' ? templates.value.length : templates.value.filter(t => t.type === type).length
const typeLabel = (type) => ({ GENERIC_COMMON: 'Partie Generique', POSTE_PRODUCTION: 'Production', ANIMATION: 'Animation' }[type] || type)
const statusLabel = (s) => ({ DRAFT: 'Brouillon', VALIDATED: 'Valide', ARCHIVED: 'Archive' }[s] || s)
const statusClass = (s) => ({ DRAFT: 'bg-gray-100 text-gray-700', VALIDATED: 'bg-green-100 text-green-700', ARCHIVED: 'bg-red-100 text-red-700' }[s] || '')
const questionCount = computed(() => templateDetail.value?.sections?.reduce((sum, s) => sum + (s.questions?.length || 0), 0) || 0)

function ensureNewQuestion(sectionId) {
  if (!newQuestion[sectionId]) newQuestion[sectionId] = { text: '', expected: '' }
}

async function load() {
  const [tplRes, wsRes] = await Promise.allSettled([evaluationApi.getTemplates(), structureApi.getWorkstations()])
  if (tplRes.status === 'fulfilled') templates.value = tplRes.value.data || []
  if (wsRes.status === 'fulfilled') workstations.value = wsRes.value.data || []
}

async function selectTemplate(tpl) {
  selectedTemplate.value = tpl
  try {
    const res = await evaluationApi.getTemplateDetail(tpl.id)
    templateDetail.value = res.data
    for (const s of (res.data?.sections || [])) ensureNewQuestion(s.id)
  } catch (e) { console.error(e) }
}

function closePanel() { selectedTemplate.value = null; templateDetail.value = null }

function openCreateModal() {
  newTemplate.name = ''; newTemplate.description = ''; newTemplate.type = 'POSTE_PRODUCTION'; newTemplate.workstationId = null
  showCreateModal.value = true
}

function openEditModal() {
  editTemplateData.name = selectedTemplate.value.name
  editTemplateData.description = selectedTemplate.value.description || ''
  editTemplateData.targetNiveau = selectedTemplate.value.targetNiveau || null
  showEditModal.value = true
}

async function createTemplate() {
  if (!newTemplate.name || !newTemplate.type) return
  try {
    const payload = { ...newTemplate }
    if (payload.workstationId) payload.workstationId = Number(payload.workstationId)
    await evaluationApi.createTemplate(payload)
    showCreateModal.value = false
    await load()
  } catch (e) { alert('Erreur: ' + (e.response?.data?.message || e.message)) }
}

async function saveTemplateEdit() {
  if (!editTemplateData.name) return
  try {
    await evaluationApi.updateTemplate(selectedTemplate.value.id, {
      name: editTemplateData.name,
      description: editTemplateData.description,
      targetNiveau: editTemplateData.targetNiveau
    })
    showEditModal.value = false
    await load()
    await selectTemplate(selectedTemplate.value)
  } catch (e) { alert('Erreur: ' + (e.response?.data?.message || e.message)) }
}

async function deleteTemplate() {
  if (!confirm('Supprimer ce template ?')) return
  try {
    await evaluationApi.deleteTemplate(selectedTemplate.value.id)
    closePanel()
    await load()
  } catch (e) { alert('Erreur: ' + (e.response?.data?.message || e.message)) }
}

async function addSection() {
  if (!newSectionTitle.value || !selectedTemplate.value) return
  try {
    await evaluationApi.addSection(selectedTemplate.value.id, { title: newSectionTitle.value, displayOrder: (templateDetail.value?.sections?.length || 0) })
    newSectionTitle.value = ''
    await selectTemplate(selectedTemplate.value)
  } catch (e) { alert('Erreur: ' + (e.response?.data?.message || e.message)) }
}

async function addQuestionToSection(sectionId) {
  ensureNewQuestion(sectionId)
  const q = newQuestion[sectionId]
  if (!q.text || !selectedTemplate.value) return
  try {
    const section = templateDetail.value.sections.find(s => s.id === sectionId)
    await evaluationApi.addQuestion(selectedTemplate.value.id, {
      questionText: q.text, expectedAnswer: q.expected || '', questionNumber: (section?.questions?.length || 0) + 1, sectionId
    })
    newQuestion[sectionId] = { text: '', expected: '' }
    await selectTemplate(selectedTemplate.value)
  } catch (e) { alert('Erreur: ' + (e.response?.data?.message || e.message)) }
}

async function editQuestion(q) {
  const newText = prompt('Modifier la question:', q.questionText)
  if (newText === null || !newText.trim()) return
  const newExpected = prompt('Modifier la reponse attendue:', q.expectedAnswer || '')
  try {
    await evaluationApi.updateQuestion(q.id, { questionText: newText.trim(), expectedAnswer: newExpected || '' })
    await selectTemplate(selectedTemplate.value)
  } catch (e) { alert('Erreur: ' + (e.response?.data?.message || e.message)) }
}

async function deleteQuestion(questionId) {
  if (!confirm('Supprimer cette question ?')) return
  try {
    await evaluationApi.deleteQuestion(questionId)
    await selectTemplate(selectedTemplate.value)
  } catch (e) { alert('Erreur: ' + (e.response?.data?.message || e.message)) }
}

async function validateThisTemplate() {
  if (!selectedTemplate.value) return
  try {
    await evaluationApi.validateTemplate(selectedTemplate.value.id)
    await load()
    await selectTemplate(selectedTemplate.value)
  } catch (e) { alert('Erreur: ' + (e.response?.data?.message || e.message)) }
}

onMounted(load)
</script>