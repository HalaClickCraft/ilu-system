<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">Templates de questions</h1>
        <p class="mt-1 text-sm text-gray-500">Créer et organiser les questions d'évaluation.</p>
      </div>
      <button v-if="canCreateTemplate" @click="openCreateModal" class="rounded-lg bg-emerald-600 px-4 py-2 text-sm font-medium text-white hover:bg-emerald-700">
        Nouveau template
      </button>
    </div>

    <div class="flex flex-wrap gap-2">
      <button v-for="tab in typeTabs" :key="tab.key" @click="activeType = tab.key" class="rounded-lg px-3 py-2 text-sm" :class="activeType === tab.key ? 'bg-emerald-600 text-white' : 'bg-white text-gray-600 border border-gray-200'">
        {{ tab.label }} ({{ countByType(tab.key) }})
      </button>
    </div>

    <div class="grid gap-6 lg:grid-cols-3">
      <div class="space-y-3">
        <button v-for="template in filteredTemplates" :key="template.id" @click="selectTemplate(template)" class="w-full rounded-xl border p-4 text-left transition" :class="selectedTemplate?.id === template.id ? 'border-emerald-500 bg-emerald-50' : 'border-gray-200 bg-white hover:border-gray-300'">
          <div class="flex items-center justify-between gap-2">
            <h2 class="font-semibold text-gray-900">{{ template.name }}</h2>
            <span class="rounded-full px-2 py-0.5 text-xs" :class="statusClass(template.status)">{{ statusLabel(template.status) }}</span>
          </div>
          <p class="mt-1 text-sm text-gray-500">{{ typeLabel(template.type) }}</p>
          <p v-if="template.description" class="mt-2 text-xs text-gray-400">{{ template.description }}</p>
        </button>
        <div v-if="filteredTemplates.length === 0" class="rounded-xl border border-dashed border-gray-300 p-8 text-center text-sm text-gray-400">Aucun template trouvé.</div>
      </div>

      <div class="rounded-xl border border-gray-200 bg-white p-5 lg:col-span-2">
        <div v-if="templateDetail">
          <div class="flex items-start justify-between gap-4 border-b border-gray-100 pb-4">
            <div>
              <h2 class="text-lg font-semibold">{{ templateDetail.name }}</h2>
              <p class="mt-1 text-sm text-gray-500">{{ templateDetail.description }}</p>
            </div>
            <button v-if="canManageTemplate(templateDetail) && templateDetail.status === 'DRAFT'" @click="validateThisTemplate" class="rounded-lg bg-blue-600 px-3 py-2 text-sm text-white">Publier le template</button>
          </div>

          <div v-for="section in templateDetail.sections || []" :key="section.id" class="border-b border-gray-100 py-4 last:border-0">
            <div class="flex flex-wrap items-center justify-between gap-2">
              <div>
                <h3 class="font-medium text-gray-900">{{ section.title }}</h3>
                <p v-if="section.description" class="mt-1 text-sm text-gray-500">{{ section.description }}</p>
              </div>
              <div class="flex gap-2 text-xs">
                <span class="rounded bg-indigo-50 px-2 py-1 text-indigo-700">Domaine : {{ domainLabel(section.domain) }}</span>
                <span class="rounded bg-slate-100 px-2 py-1 text-slate-700">Responsable : {{ roleLabel(section.responsibleRole) }}</span>
              </div>
            </div>

            <div v-if="section.questions?.length" class="mt-3 overflow-x-auto">
              <table class="min-w-full text-left text-sm">
                <thead class="bg-gray-50 text-xs text-gray-500">
                  <tr>
                    <th class="px-3 py-2">N°</th><th class="px-3 py-2">Question</th><th class="px-3 py-2">Réponse espérée</th><th class="px-3 py-2">Contrôle</th><th class="px-3 py-2">Observation</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="question in section.questions" :key="question.id" class="border-t border-gray-100 align-top">
                    <td class="px-3 py-2 text-gray-500">{{ question.questionNumber }}</td>
                    <td class="px-3 py-2 font-medium text-gray-900">{{ question.questionText }}</td>
                    <td class="px-3 py-2 text-gray-600">{{ question.expectedAnswer || '—' }}</td>
                    <td class="px-3 py-2"><span class="rounded bg-gray-100 px-1.5 py-0.5 text-xs">0 / 1</span></td>
                    <td class="px-3 py-2 text-xs text-gray-500">Saisie par l'évaluateur</td>
                  </tr>
                </tbody>
              </table>
            </div>
            <p v-else class="mt-3 text-sm text-gray-400">Aucune question dans cette section.</p>

            <div v-if="canManageTemplate(templateDetail)" class="mt-4 rounded-lg border border-dashed border-gray-300 bg-gray-50 p-4">
              <p class="mb-3 text-sm font-medium text-gray-700">Ajouter un contrôle</p>
              <div class="grid gap-3 md:grid-cols-2">
                <label class="text-sm text-gray-600">Responsable
                  <input :value="roleLabel(section.responsibleRole)" readonly class="mt-1 w-full rounded border border-gray-200 bg-gray-100 px-3 py-2 text-sm text-gray-600">
                </label>
                <label class="text-sm text-gray-600 md:col-span-2">Question
                  <textarea v-model.trim="newQuestion[section.id].text" required rows="2" placeholder="Question à poser ou point à vérifier" class="mt-1 w-full rounded border border-gray-300 px-3 py-2 text-sm"></textarea>
                </label>
                <label class="text-sm text-gray-600">Réponse espérée
                  <input v-model.trim="newQuestion[section.id].expected" placeholder="Critère attendu" class="mt-1 w-full rounded border border-gray-300 px-3 py-2 text-sm">
                </label>
                <div class="text-sm text-gray-600">Type de réponse
                  <div class="mt-1 rounded border border-gray-200 bg-gray-100 px-3 py-2 text-sm text-gray-600">Point de contrôle binaire : 0 / 1</div>
                </div>
              </div>
              <p class="mt-3 text-xs text-gray-500">L'évaluateur peut ajouter une note ou observation avec sa réponse pendant l'évaluation.</p>
              <div class="mt-3 flex justify-end"><button @click="addQuestionToSection(section.id)" class="rounded bg-gray-800 px-3 py-2 text-sm text-white">Ajouter la question</button></div>
            </div>
          </div>

          <div v-if="canManageTemplate(templateDetail)" class="mt-4 rounded-lg bg-gray-50 p-4">
            <p class="mb-3 text-sm font-medium text-gray-700">Ajouter une section</p>
            <div class="flex flex-col gap-2 sm:flex-row">
              <input v-model.trim="newSectionTitle" placeholder="Titre de section" class="min-w-0 flex-1 rounded border border-gray-300 px-3 py-2 text-sm">
              <input v-model.trim="newSectionDescription" placeholder="Description (facultative)" class="min-w-0 flex-1 rounded border border-gray-300 px-3 py-2 text-sm">
              <select v-if="sectionDomainsForTemplate(templateDetail).length > 1" v-model="newSectionDomain" class="min-w-0 flex-1 rounded border border-gray-300 bg-white px-3 py-2 text-sm">
                <option v-for="domain in selectableSectionDomains(templateDetail)" :key="domain" :value="domain">{{ domainLabel(domain) }}</option>
              </select>
              <button @click="addSection" class="rounded bg-gray-800 px-3 py-2 text-sm text-white">Ajouter une section</button>
            </div>
            <p class="mt-2 text-xs text-gray-500">Responsable de cette section : {{ roleLabel(domainResponsibleRole(newSectionDomain || sectionDomainsForTemplate(templateDetail)[0])) }}. Le domaine définit aussi celui de ses questions.</p>
          </div>
        </div>
        <div v-else class="flex min-h-48 items-center justify-center text-sm text-gray-400">Sélectionnez un template pour voir ses questions.</div>
      </div>
    </div>

    <div v-if="showCreateModal" class="fixed inset-0 z-50 flex items-center justify-center bg-black/50" @click.self="showCreateModal = false">
      <form class="mx-4 max-h-[90vh] w-full max-w-xl space-y-4 overflow-auto rounded-xl bg-white p-6 shadow-xl" @submit.prevent="createTemplate">
        <div><h2 class="text-lg font-semibold">Nouveau template</h2><p class="mt-1 text-sm text-gray-500">Pour un template de production, sélectionnez d'abord le projet, puis la zone et le poste.</p></div>
        <input v-model.trim="newTemplate.name" required placeholder="Nom du template" class="w-full rounded border border-gray-300 px-3 py-2 text-sm">
        <textarea v-model.trim="newTemplate.description" placeholder="Description" class="w-full rounded border border-gray-300 px-3 py-2 text-sm"></textarea>
        <select v-model="newTemplate.type" class="w-full rounded border border-gray-300 px-3 py-2 text-sm" @change="resetWorkstationSelection"><option v-for="tab in creatableTypes" :key="tab.key" :value="tab.key">{{ tab.label }}</option></select>

        <div v-if="newTemplate.type === 'POSTE_PRODUCTION'" class="space-y-3 rounded-lg border border-emerald-100 bg-emerald-50/50 p-4">
          <p class="text-sm font-medium text-gray-800">Affectation aux postes</p>
          <div><label class="mb-1 block text-sm text-gray-600">Projet</label><select v-model="newTemplate.projectId" required class="w-full rounded border border-gray-300 bg-white px-3 py-2 text-sm" @change="newTemplate.zoneId = ''; newTemplate.workstationId = ''"><option value="">Sélectionner un projet</option><option v-for="project in structure" :key="project.id" :value="project.id">{{ project.name }}</option></select></div>
          <div><label class="mb-1 block text-sm text-gray-600">Zone</label><select v-model="newTemplate.zoneId" required :disabled="!newTemplate.projectId" class="w-full rounded border border-gray-300 bg-white px-3 py-2 text-sm" @change="newTemplate.workstationId = ''"><option value="">Sélectionner une zone</option><option v-for="zone in availableZones" :key="zone.id" :value="zone.id">{{ zone.name }}</option></select></div>
          <div><label class="mb-1 block text-sm text-gray-600">Poste</label><div class="flex gap-2"><select v-model="newTemplate.workstationId" :disabled="!newTemplate.zoneId" class="min-w-0 flex-1 rounded border border-gray-300 bg-white px-3 py-2 text-sm"><option value="">Sélectionner un poste</option><option v-for="workstation in availableWorkstations" :key="workstation.id" :value="workstation.id">{{ workstation.name }}</option></select><button type="button" @click="addSelectedWorkstation" :disabled="!newTemplate.workstationId" class="rounded bg-emerald-600 px-3 py-2 text-sm text-white disabled:opacity-50">Ajouter</button></div></div>
          <div v-if="selectedWorkstations.length" class="flex flex-wrap gap-2"><span v-for="workstation in selectedWorkstations" :key="workstation.id" class="inline-flex items-center gap-1 rounded-full bg-white px-3 py-1 text-sm text-gray-700 ring-1 ring-emerald-200">{{ workstation.name }}<button type="button" @click="removeWorkstation(workstation.id)" class="ml-1 text-gray-400 hover:text-red-600" :aria-label="`Retirer ${workstation.name}`">×</button></span></div>
          <p v-else class="text-xs text-amber-700">Ajoutez au moins un poste. Vous pourrez ajouter plusieurs postes via le même parcours.</p>
        </div>
        <div class="flex justify-end gap-3"><button type="button" @click="showCreateModal = false" class="px-3 py-2 text-sm text-gray-600">Annuler</button><button class="rounded bg-emerald-600 px-3 py-2 text-sm text-white">Créer</button></div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, reactive } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { evaluationApi, structureApi } from '@/api/endpoints'

const authStore = useAuthStore()
const isAgentQualite = computed(() => authStore.hasAnyRole(['AGENT_QUALITE']))
const isRespHse = computed(() => authStore.hasAnyRole(['RESP_HSE']))
const isChefEquipe = computed(() => authStore.hasAnyRole(['CHEF_EQUIPE']))
const isAdmin = computed(() => authStore.hasAnyRole(['ADMIN']))
const canCreateTemplate = computed(() => isAgentQualite.value || isRespHse.value || isChefEquipe.value || isAdmin.value)

const templates = ref([])
const structure = ref([])
const activeType = ref('ALL')
const selectedTemplate = ref(null)
const templateDetail = ref(null)
const showCreateModal = ref(false)
const newSectionTitle = ref('')
const newSectionDescription = ref('')
const newSectionDomain = ref('')
const newQuestion = reactive({})
const newTemplate = reactive({
  name: '', description: '', type: 'POSTE_PRODUCTION', workstationIds: [], projectId: '', zoneId: '', workstationId: ''
})

const typeTabs = [
  { key: 'ALL', label: 'Tous' },
  { key: 'GENERIC', label: 'Partie generique' },
  { key: 'POSTE_PRODUCTION', label: 'Poste Production' },
  { key: 'ANIMATION', label: 'Animation' },
]

const templateTypes = [
  { key: 'GENERIC_HSE', label: 'Generique HSE' },
  { key: 'GENERIC_QUALITY', label: 'Generique Qualite' },
  { key: 'GENERIC_COMMON', label: 'Partie generique' },
  { key: 'POSTE_PRODUCTION', label: 'Poste Production' },
  { key: 'ANIMATION', label: 'Animation' },
]

const creatableTypes = computed(() => templateTypes.filter((tab) =>
  isAdmin.value ||
  (tab.key === 'GENERIC_HSE' && isRespHse.value) ||
  (tab.key === 'GENERIC_QUALITY' && isAgentQualite.value) ||
  (tab.key === 'GENERIC_COMMON' && (isChefEquipe.value || isRespHse.value || isAgentQualite.value)) ||
  (['POSTE_PRODUCTION', 'ANIMATION'].includes(tab.key) && isChefEquipe.value)
))

const templateRole = (template) => ({
  GENERIC_HSE: 'RESP_HSE',
  GENERIC_QUALITY: 'AGENT_QUALITE',
  GENERIC_COMMON: 'CHEF_EQUIPE',
  POSTE_PRODUCTION: 'CHEF_EQUIPE',
  ANIMATION: 'CHEF_EQUIPE'
}[template?.type])

const sectionDomainsForTemplate = (template) => ({
  GENERIC_HSE: ['SECURITY_ENVIRONMENT'],
  GENERIC_QUALITY: ['QUALITY'],
  GENERIC_COMMON: ['SECURITY_ENVIRONMENT', 'QUALITY', 'FIVE_S', 'TRACEABILITY', 'PRODUCTION_ALARMS'],
  POSTE_PRODUCTION: ['PRODUCTION'],
  ANIMATION: ['ANIMATION']
}[template?.type] || [])

const domainResponsibleRole = (domain) => ({
  SECURITY_ENVIRONMENT: 'RESP_HSE',
  QUALITY: 'AGENT_QUALITE',
  FIVE_S: 'CHEF_EQUIPE',
  TRACEABILITY: 'CHEF_EQUIPE',
  PRODUCTION_ALARMS: 'CHEF_EQUIPE',
  PRODUCTION: 'CHEF_EQUIPE',
  ANIMATION: 'CHEF_EQUIPE'
}[domain])

const selectableSectionDomains = (template) => sectionDomainsForTemplate(template)
  .filter(domain => isAdmin.value || authStore.hasAnyRole([domainResponsibleRole(domain)]))

const canManageTemplate = (template) => isAdmin.value ||
  (template?.type === 'GENERIC_COMMON' && authStore.hasAnyRole(['RESP_HSE', 'AGENT_QUALITE', 'CHEF_EQUIPE'])) ||
  authStore.hasAnyRole([templateRole(template)])
const genericTemplateTypes = ['GENERIC_HSE', 'GENERIC_QUALITY', 'GENERIC_COMMON']
const filteredTemplates = computed(() => {
  if (activeType.value === 'ALL') return templates.value
  if (activeType.value === 'GENERIC') return templates.value.filter(template => genericTemplateTypes.includes(template.type))
  return templates.value.filter(template => template.type === activeType.value)
})
const selectedProject = computed(() => structure.value.find(project => project.id === newTemplate.projectId))
const availableZones = computed(() => selectedProject.value?.zones || [])
const selectedZone = computed(() => availableZones.value.find(zone => zone.id === newTemplate.zoneId))
const availableWorkstations = computed(() => selectedZone.value?.workstations || [])
const selectedWorkstations = computed(() => structure.value.flatMap(project => project.zones || []).flatMap(zone => zone.workstations || []).filter(workstation => newTemplate.workstationIds.includes(workstation.id)))

const countByType = (type) => {
  if (type === 'ALL') return templates.value.length
  if (type === 'GENERIC') return templates.value.filter(template => genericTemplateTypes.includes(template.type)).length
  return templates.value.filter(template => template.type === type).length
}
const typeLabel = (type) => ({ GENERIC_HSE: 'Partie generique', GENERIC_QUALITY: 'Partie generique', GENERIC_COMMON: 'Partie generique', POSTE_PRODUCTION: 'Production', ANIMATION: 'Animation' }[type] || type)
const roleLabel = (role) => ({ RESP_HSE: 'Responsable HSE', AGENT_QUALITE: 'Agent qualité', RESP_QUALITE: 'Responsable qualité', CHEF_EQUIPE: "Chef d'équipe" }[role] || role || '—')
const domainLabel = (domain) => ({ SECURITY_ENVIRONMENT: 'Sécurité / environnement', QUALITY: 'Qualité', FIVE_S: '5S', TRACEABILITY: 'Traçabilité', PRODUCTION_ALARMS: 'Alarmes production', PRODUCTION: 'Production', ANIMATION: 'Animation' }[domain] || domain || '—')
const statusLabel = (status) => ({ DRAFT: 'Brouillon', VALIDATED: 'Valide', ARCHIVED: 'Archive' }[status] || status)
const statusClass = (status) => ({ DRAFT: 'bg-gray-100 text-gray-700', VALIDATED: 'bg-green-100 text-green-700', ARCHIVED: 'bg-red-100 text-red-700' }[status] || '')

async function load() {
  const [templatesResult, structureResult] = await Promise.allSettled([evaluationApi.getTemplates(), structureApi.getAll()])
  if (templatesResult.status === 'fulfilled') templates.value = templatesResult.value.data || []
  if (structureResult.status === 'fulfilled') structure.value = structureResult.value.data || []
}

async function selectTemplate(template) {
  selectedTemplate.value = template
  try {
    const response = await evaluationApi.getTemplateDetail(template.id)
    templateDetail.value = response.data
    newSectionDomain.value = selectableSectionDomains(templateDetail.value)[0] || ''
    for (const section of templateDetail.value.sections || []) {
      if (!newQuestion[section.id]) newQuestion[section.id] = { text: '', expected: '' }
    }
  } catch (error) {
    console.error('Error loading template detail', error)
  }
}

function resetWorkstationSelection() {
  newTemplate.workstationIds = []
  newTemplate.projectId = ''
  newTemplate.zoneId = ''
  newTemplate.workstationId = ''
}

function addSelectedWorkstation() {
  const workstationId = Number(newTemplate.workstationId)
  if (workstationId && !newTemplate.workstationIds.includes(workstationId)) newTemplate.workstationIds.push(workstationId)
  newTemplate.workstationId = ''
}

function removeWorkstation(workstationId) {
  newTemplate.workstationIds = newTemplate.workstationIds.filter(id => id !== workstationId)
}

function openCreateModal() {
  newTemplate.name = ''
  newTemplate.description = ''
  newTemplate.type = creatableTypes.value[0]?.key || 'POSTE_PRODUCTION'
  resetWorkstationSelection()
  showCreateModal.value = true
}

async function createTemplate() {
  if (!newTemplate.name || !newTemplate.type) return
  if (newTemplate.type === 'POSTE_PRODUCTION' && !newTemplate.workstationIds.length) {
    alert('Sélectionnez et ajoutez au moins un poste de travail.')
    return
  }
  try {
    await evaluationApi.createTemplate({
      name: newTemplate.name,
      description: newTemplate.description,
      type: newTemplate.type,
      workstationIds: newTemplate.workstationIds.map(Number)
    })
    showCreateModal.value = false
    await load()
  } catch (error) {
    alert('Erreur lors de la creation: ' + (error.response?.data?.message || error.message))
  }
}

async function addSection() {
  if (!newSectionTitle.value || !selectedTemplate.value) return
  try {
    const domains = sectionDomainsForTemplate(templateDetail.value)
    await evaluationApi.addSection(selectedTemplate.value.id, {
      title: newSectionTitle.value,
      description: newSectionDescription.value,
      displayOrder: (templateDetail.value?.sections?.length || 0) + 1,
      ...(domains.length > 1 ? { domain: newSectionDomain.value } : {})
    })
    newSectionTitle.value = ''
    newSectionDescription.value = ''
    await selectTemplate(selectedTemplate.value)
  } catch (error) {
    alert('Erreur: ' + (error.response?.data?.message || error.message))
  }
}

async function addQuestionToSection(sectionId) {
  const question = newQuestion[sectionId]
  if (!question?.text || !selectedTemplate.value) return
  try {
    const section = templateDetail.value.sections.find(item => item.id === sectionId)
    await evaluationApi.addQuestion(selectedTemplate.value.id, {
      questionText: question.text,
      expectedAnswer: question.expected || '',
      questionNumber: (section?.questions?.length || 0) + 1,
      responseType: 'BINARY',
      sectionId
    })
    newQuestion[sectionId] = { text: '', expected: '' }
    await selectTemplate(selectedTemplate.value)
  } catch (error) {
    alert('Erreur: ' + (error.response?.data?.message || error.message))
  }
}

async function validateThisTemplate() {
  if (!selectedTemplate.value) return
  try {
    await evaluationApi.validateTemplate(selectedTemplate.value.id)
    await load()
    await selectTemplate(selectedTemplate.value)
  } catch (error) {
    alert('Erreur: ' + (error.response?.data?.message || error.message))
  }
}

onMounted(load)
</script>
