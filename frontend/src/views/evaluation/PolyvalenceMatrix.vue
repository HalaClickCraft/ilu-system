<template>
  <div class="space-y-6">
    <div class="flex flex-col sm:flex-row sm:items-start sm:justify-between gap-3">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">MATRICE DE POLYVALENCE{{ selectedProjectName ? ' — ' + selectedProjectName : '' }}</h1>
        <p class="text-sm text-gray-500 mt-1">Indicateur de polyvalence: Minimum 6 personnes formees par poste => 6 personnes en L</p>
      </div>
      <div class="flex flex-col sm:flex-row items-start sm:items-center gap-3">
        <div v-if="showProjectFilter" class="flex items-center gap-2">
          <label class="text-sm font-medium text-gray-600 whitespace-nowrap">Projet:</label>
          <select v-model="selectedProject" class="px-3 py-2 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 outline-none min-w-[180px]">
            <option value="">Tous les projets</option>
            <option v-for="p in projectList" :key="p.id" :value="p.id">{{ p.name }}</option>
          </select>
        </div>

        <button v-if="matrixData.operators?.length" @click="exportMatrixToExcel" class="inline-flex items-center gap-2 px-4 py-2 bg-sky-600 hover:bg-sky-700 text-white rounded-lg transition text-sm font-medium shadow-sm">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4"/></svg>
          Exporter Matrix
        </button>
        <button @click="openImportModal" class="inline-flex items-center gap-2 px-4 py-2 bg-teal-600 hover:bg-teal-700 text-white rounded-lg transition text-sm font-medium shadow-sm">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-8l-4-4m0 0L8 8m4-4v12"/></svg>
          Importer Certifications
        </button>
        <div class="flex items-center gap-3 text-xs">
          <span class="flex items-center gap-1"><span class="w-4 h-4 rounded bg-gray-100 border inline-block"></span> Non forme</span>
          <span class="flex items-center gap-1"><span class="w-4 h-4 rounded bg-amber-400 inline-block text-white"></span> Niveau I</span>
          <span class="flex items-center gap-1"><span class="w-4 h-4 rounded bg-blue-500 inline-block text-white"></span> Niveau L</span>
          <span class="flex items-center gap-1"><span class="w-4 h-4 rounded bg-green-600 inline-block text-white"></span> Niveau U</span>
        </div>
      </div>
    </div>

    <!-- Campaign Tabs -->
    <div v-if="campaignTabs.length > 0" class="border-b border-gray-200">
      <nav class="-mb-px flex space-x-6 overflow-x-auto pb-1" aria-label="Tabs">
        <button
          v-for="tab in campaignTabs"
          :key="tab.key"
          @click="selectedCampaignTab = tab.key"
          :class="[
            selectedCampaignTab === tab.key
              ? 'border-sky-600 text-sky-700 font-bold border-b-2'
              : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300 border-b-2',
            'whitespace-nowrap py-3 px-1 text-sm font-medium transition focus:outline-none'
          ]"
        >
          {{ tab.label }}
        </button>
      </nav>
    </div>

    <div v-if="loading" class="text-center py-12 text-gray-400">Chargement de la matrice...</div>
    <div v-else-if="errorMsg" class="bg-red-50 border border-red-200 rounded-xl p-6 text-center">
      <p class="text-red-700 font-semibold">Erreur de chargement</p>
      <p class="text-red-500 text-sm mt-1">{{ errorMsg }}</p>
      <button @click="loadMatrix" class="mt-3 bg-red-600 text-white px-4 py-2 rounded-lg text-sm hover:bg-red-700">Reessayer</button>
    </div>

    <template v-else>
      <div class="bg-white rounded-xl border overflow-x-auto shadow-sm">
        <table class="min-w-full border-collapse text-xs text-gray-700">
          <thead>
            <tr class="bg-gray-100 border-b border-gray-200">
              <th rowspan="3" class="px-4 py-3 text-left font-bold text-gray-700 sticky left-0 bg-gray-100 z-20 border-r border-gray-200 min-w-[180px]">Operateur</th>
              <th class="px-3 py-2 text-center font-bold text-gray-700 border-r border-gray-200 min-w-[140px] bg-gray-100">Zone</th>
              <th v-for="z in zones" :key="z.name" :colspan="z.workstations.length" class="px-3 py-2 text-center font-bold text-gray-800 border-r border-gray-200 bg-gray-50">{{ z.name }}</th>
              <th rowspan="3" class="px-4 py-3 text-center font-bold text-gray-700 border-l border-gray-200 bg-gray-50 max-w-[150px] whitespace-normal">Nombre des postes sur lesquels est forme un operateur</th>
            </tr>
            <tr class="bg-gray-100 border-b border-gray-200">
              <th class="px-3 py-2 text-center font-bold text-gray-700 border-r border-gray-200 bg-gray-100">Poste</th>
              <th v-for="col in allColumns" :key="col.id" class="px-3 py-2 text-center font-semibold text-gray-700 border-r border-gray-200 min-w-[90px]">{{ col.name }}</th>
            </tr>
            <tr class="bg-gray-50 border-b border-gray-300 text-gray-500">
              <th class="px-3 py-1.5 text-center font-bold text-gray-600 border-r border-gray-200 bg-gray-100">Target per station</th>
              <th v-for="col in allColumns" :key="col.id + '-target'" class="px-3 py-1.5 text-center font-bold border-r border-gray-200 text-gray-700 bg-gray-50">{{ formatNiveau(col.targetLevel || 'L') }}</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-200">
            <template v-for="op in displayOperators" :key="op.operatorId">
              <!-- Case: Show Résultat Recyclage (Senior Operator, seniority > 6 months) -->
              <template v-if="shouldShowRecyclage(op)">
                <tr class="hover:bg-orange-50/20">
                  <td rowspan="2" class="px-4 py-3 sticky left-0 bg-white z-10 font-bold text-gray-900 border-r border-gray-200">
                    {{ op.operatorName }}
                    <div class="text-[10px] text-gray-400 font-normal">{{ op.employeeId }} · {{ op.seniorityMonths }} mois</div>
                  </td>
                  <td class="px-3 py-2 font-medium text-orange-700 bg-orange-50 border-r border-gray-200 text-left">Date de recyclage</td>
                  <td v-for="col in allColumns" :key="col.id + '-recy-date'" class="px-2 py-2 text-center border-r border-gray-200 text-[10px] text-gray-600">{{ getColumnRecyclageDate(op, col) }}</td>
                  <td rowspan="2" class="px-4 py-3 text-center border-l border-gray-200 font-bold text-lg text-slate-800 bg-slate-50">{{ getTrainedCount(op) }}</td>
                </tr>
                <tr class="hover:bg-orange-50/30 border-b border-gray-300">
                  <td class="px-3 py-2 font-medium text-orange-700 bg-orange-50/50 border-r border-gray-200 text-left">Résultat recyclage</td>
                  <td v-for="col in allColumns" :key="col.id + '-recy-result'" class="px-2 py-2 text-center border-r border-gray-200 text-[10px]" :class="recyclageClass(getRecyclageStatus(op, col))">{{ getRecyclageDisplay(op, col) }}</td>
                </tr>
              </template>

              <!-- Case: Show Date d'évaluation & Niveau compétence (New recruit, seniority <= 6 months) -->
              <template v-else>
                <tr class="hover:bg-gray-50">
                  <td rowspan="2" class="px-4 py-3 sticky left-0 bg-white z-10 font-bold text-gray-900 border-r border-gray-200">
                    {{ op.operatorName }}
                    <div class="text-[10px] text-gray-400 font-normal">{{ op.employeeId }} · {{ op.seniorityMonths }} mois</div>
                  </td>
                  <td class="px-3 py-2 font-medium text-gray-500 bg-gray-50 border-r border-gray-200 text-left">Date d'évaluation</td>
                  <td v-for="col in allColumns" :key="col.id + '-date'" class="px-2 py-2 text-center border-r border-gray-200 text-[10px] text-gray-600">{{ getColumnDate(op, col) }}</td>
                  <td rowspan="2" class="px-4 py-3 text-center border-l border-gray-200 font-bold text-lg text-slate-800 bg-slate-50">{{ getTrainedCount(op) }}</td>
                </tr>
                <tr class="hover:bg-gray-50 border-b border-gray-300">
                  <td class="px-3 py-2 font-medium text-gray-500 bg-gray-50 border-r border-gray-200 text-left">Niveau compétence</td>
                  <td v-for="col in allColumns" :key="col.id + '-level'" class="px-2 py-2 text-center border-r border-gray-200">
                    <span :class="niveauBgClass(formatNiveau(getColumnLevel(op, col)))" class="inline-block w-8 h-8 leading-8 rounded-lg text-white font-bold text-xs shadow-sm">{{ formatNiveau(getColumnLevel(op, col)) }}</span>
                  </td>
                </tr>
              </template>
            </template>
            <tr v-if="!displayOperators?.length"><td colspan="99" class="px-4 py-8 text-center text-gray-400">Aucun operateur trouve</td></tr>
          </tbody>
          <tfoot v-if="displayOperators?.length" class="bg-gray-100 border-t-2 border-gray-300 font-medium">
            <tr class="border-b"><td colspan="2" class="px-4 py-3 text-left font-semibold text-amber-700 bg-amber-50/50">Nombres de personnes au niveau I</td><td v-for="col in allColumns" :key="col.id + '-sumI'" class="px-2 py-3 text-center font-bold text-amber-700 bg-amber-50/30 border-r border-gray-200">{{ getCountPerNiveau(col, 'I') }}</td><td class="bg-gray-100"></td></tr>
            <tr class="border-b"><td colspan="2" class="px-4 py-3 text-left font-semibold text-blue-700 bg-blue-50/50">Nombres de personnes au niveau L</td><td v-for="col in allColumns" :key="col.id + '-sumL'" class="px-2 py-3 text-center font-bold text-blue-700 bg-blue-50/30 border-r border-gray-200">{{ getCountPerNiveau(col, 'L') }}</td><td class="bg-gray-100"></td></tr>
            <tr><td colspan="2" class="px-4 py-3 text-left font-semibold text-green-700 bg-green-50/50">Nombres de personnes au niveau U</td><td v-for="col in allColumns" :key="col.id + '-sumU'" class="px-2 py-3 text-center font-bold text-green-700 bg-green-50/30 border-r border-gray-200">{{ getCountPerNiveau(col, 'U') }}</td><td class="bg-gray-100"></td></tr>
            <tr class="border-t-2 border-gray-300"><td colspan="2" class="px-4 py-3 text-left font-semibold text-gray-700 bg-gray-50">Statut Conformité (Cible L/U >= 6)</td><td v-for="col in allColumns" :key="col.id + '-compliance'" class="px-2 py-3 text-center font-bold border-r border-gray-200" :class="col.isGeneric ? 'bg-gray-100' : isWorkstationCompliant(col) ? 'text-emerald-700 bg-emerald-50' : 'text-rose-700 bg-rose-50'"><span v-if="col.isGeneric" class="text-gray-400 font-normal">-</span><span v-else-if="isWorkstationCompliant(col)">✅ {{ getCompliantCount(col) }}/6</span><span v-else>⚠️ {{ getCompliantCount(col) }}/6</span></td><td class="bg-gray-100"></td></tr>
          </tfoot>
        </table>
      </div>
    </template>

    <!-- Import Certifications Modal -->
    <div v-if="showImportModal" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50" @click.self="closeImportModal">
      <div class="bg-white rounded-2xl shadow-xl w-full max-w-xl mx-4 p-6">
        <h2 class="text-lg font-semibold text-gray-900 mb-2">Importer Certifications (Matrice)</h2>
        <p class="text-xs text-gray-500 mb-4">
          Téléversez un fichier Excel. Il doit contenir les colonnes : 
          <strong class="text-gray-700">Matricule, Poste de Travail, Niveau (I, L, U)</strong>. 
          Optionnel : <strong class="text-gray-700">Date de Validation</strong>.
        </p>

        <div class="space-y-4">
          <div class="border-2 border-dashed border-gray-200 rounded-lg p-6 text-center">
            <input type="file" accept=".xlsx, .xls" @change="handleFileChange" class="hidden" id="cert-excel-upload" />
            <label for="cert-excel-upload" class="cursor-pointer inline-flex flex-col items-center gap-2">
              <svg class="w-10 h-10 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 13h6m-3-3v6m-9 1V4a2 2 0 012-2h6l2 2h6a2 2 0 012 2v8a2 2 0 01-2 2H5a2 2 0 01-2-2z"/></svg>
              <span class="text-sm font-medium text-teal-600 hover:text-teal-700">Choisir un fichier</span>
              <span class="text-xs text-gray-400 block mt-1" v-if="importFile">{{ importFile.name }}</span>
            </label>
          </div>

          <div v-if="parsedCertifications.length > 0" class="max-h-48 overflow-y-auto border border-gray-100 rounded-lg p-2 bg-gray-50">
            <p class="text-xs font-semibold text-gray-600 mb-2">Certifications détectées ({{ parsedCertifications.length }}) :</p>
            <ul class="text-xs space-y-1 divide-y divide-gray-100">
              <li v-for="(c, i) in parsedCertifications" :key="i" class="py-1 flex justify-between">
                <span>{{ c.employeeId }} ➔ {{ c.workstationName }}</span>
                <span class="font-bold px-2 py-0.5 rounded text-white" :class="c.level === 'U' ? 'bg-green-600' : c.level === 'L' ? 'bg-blue-500' : 'bg-amber-400'">Niveau {{ c.level }}</span>
              </li>
            </ul>
          </div>

          <div v-if="importError" class="bg-red-50 text-red-600 text-sm p-3 rounded-lg max-h-32 overflow-y-auto">{{ importError }}</div>
          <div v-if="importSuccess" class="bg-emerald-50 text-emerald-700 text-sm p-3 rounded-lg">{{ importSuccess }}</div>
        </div>

        <div class="flex justify-end gap-3 pt-4 border-t border-gray-100 mt-6">
          <button type="button" @click="closeImportModal" class="px-4 py-2 text-sm text-gray-600 hover:text-gray-800">Fermer</button>
          <button type="button" @click="submitImport" :disabled="parsedCertifications.length === 0 || importing" class="px-4 py-2 bg-teal-600 text-white text-sm rounded-lg hover:bg-teal-700 disabled:opacity-50">
            {{ importing ? 'Importation...' : 'Importer' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { evaluationApi, structureApi, operatorsApi } from '@/api/endpoints'
import { recyclageApi } from '@/services/recyclageApi'
import { useAuthStore } from '@/stores/auth'
import * as XLSX from 'xlsx'
import ExcelJS from 'exceljs'
import { useUserScope } from '@/composables/useUserScope'

const authStore = useAuthStore()
const { loadUserProjects, filterOperators } = useUserScope()
const allOperatorsData = ref([])

const loading = ref(true)
const errorMsg = ref('')
const matrixData = ref({ operators: [], workstations: [] })

const showImportModal = ref(false)
const importFile = ref(null)
const parsedCertifications = ref([])
const importError = ref('')
const importSuccess = ref('')
const importing = ref(false)

const projects = ref([])
const selectedProject = ref('')
const selectedProjectName = ref('')
const plannings = ref([])
const selectedCampaignTab = ref('')

const myChefMember = computed(() => {
  const empId = authStore.user?.employeeId
  for (const p of projects.value) {
    const m = p.members?.find(mem => mem.employeeId === empId)
    if (m) return m
  }
  return null
})

const displayOperators = computed(() => {
  const tab = activeTab.value
  if (!tab) return []
  
  const ops = matrixData.value.operators || []
  const scopedOps = filterOperators(allOperatorsData.value)
  const scopedOpIds = new Set(scopedOps.map(o => o.id))
  
  return ops.filter(op => {
    // 1. Project scope filter
    if (!scopedOpIds.has(op.operatorId)) return false
    
    // 2. Hide operators still in training (no evaluations completed)
    if (getTrainedCount(op) === 0) return false
    
    // 3. Campaign tab filter
    const opTabs = getOperatorCampaignTabs(op, plannings.value)
    if (!opTabs.has(tab.key)) return false
    
    return true
  })
})

const isMultiProjectRole = computed(() =>
  authStore.hasAnyRole(['RESP_QUALITE', 'AGENT_QUALITE', 'SUPERVISEUR', 'RESP_HSE', 'ADMIN', 'RH', 'CHEF_EQUIPE'])
)
const showProjectFilter = computed(() => isMultiProjectRole.value && projectList.value.length >= 1)
const projectList = computed(() => projects.value.map(p => ({ id: p.id, name: p.name })).sort((a, b) => a.name.localeCompare(b.name)))

const campaignTabs = computed(() => {
  const keys = new Set()
  
  function getCategoryKey(dateStr) {
    if (!dateStr || dateStr === '-') return null
    let year = new Date().getFullYear()
    let month = 1
    if (dateStr.includes('/')) {
      const parts = dateStr.split('/')
      if (parts.length === 3) {
        year = parts[2]
        month = parseInt(parts[1], 10)
      }
    } else {
      const d = new Date(dateStr)
      year = d.getFullYear()
      month = d.getMonth() + 1
    }
    const category = month < 7 ? 'EVAL_INITIALE' : 'RECYCLAGE'
    return { key: `${year}-${category}`, year, category }
  }
  
  plannings.value.forEach(item => {
    if (item.status === 'ANNULEE') return
    const info = getCategoryKey(item.scheduledDate)
    if (info) keys.add(info.key)
  })
  
  const ops = matrixData.value.operators || []
  ops.forEach(op => {
    if (op.genericPassed && op.genericDate) {
      const info = getCategoryKey(op.genericDate)
      if (info) keys.add(info.key)
    }
    if (op.workstations) {
      Object.values(op.workstations).forEach(ws => {
        if (ws.level && ws.date) {
          const info = getCategoryKey(ws.date)
          if (info) keys.add(info.key)
        }
      })
    }
  })
  
  const tabsList = Array.from(keys).map(key => {
    const [year, category] = key.split('-')
    const typeLabel = category === 'EVAL_INITIALE' ? 'Éval. Initiale' : 'Recyclage'
    
    const firstPlanning = plannings.value.find(p => p.projectId && p.status !== 'ANNULEE')
    const projName = firstPlanning ? (projectList.value.find(p => p.id === firstPlanning.projectId)?.name || 'Projet') : 'Projet'
    
    const label = `${projName} ${year} ${typeLabel}`
    return {
      key,
      label,
      year,
      category
    }
  })
  
  tabsList.sort((a, b) => b.year - a.year || a.label.localeCompare(b.label))
  return tabsList
})

watch(campaignTabs, (newTabs) => {
  if (newTabs.length > 0) {
    const exists = newTabs.some(t => t.key === selectedCampaignTab.value)
    if (!exists) {
      selectedCampaignTab.value = newTabs[0].key
    }
  } else {
    selectedCampaignTab.value = ''
  }
}, { immediate: true })

const workstations = computed(() => matrixData.value.workstations || [])

const zones = computed(() => {
  const map = {}
  map['Partie Generique'] = {
    name: 'Partie Generique',
    workstations: [
      { id: 'generic_security', name: 'Securite/5s', isGeneric: true, targetLevel: 'L' },
      { id: 'generic_quality', name: 'Qualite', isGeneric: true, targetLevel: 'L' }
    ]
  }
  workstations.value.forEach(ws => {
    const zoneName = ws.zoneName || 'Autres'
    if (!map[zoneName]) {
      map[zoneName] = { name: zoneName, workstations: [] }
    }
    map[zoneName].workstations.push({ ...ws, isGeneric: false })
  })
  return Object.values(map)
})

const allColumns = computed(() => zones.value.flatMap(z => z.workstations))

function getColumnDate(op, col) {
  if (col.isGeneric) {
    return op.genericPassed ? formatDate(op.genericDate) : '-'
  }
  return op.workstations?.[col.id]?.date ? formatDate(op.workstations[col.id].date) : '-'
}

function getColumnLevel(op, col) {
  if (col.isGeneric) {
    return op.genericPassed ? (op.genericLevel || 'L') : ''
  }
  return op.workstations?.[col.id]?.level || ''
}

const activeTab = computed(() => {
  if (!selectedCampaignTab.value || !campaignTabs.value.length) return null
  return campaignTabs.value.find(t => t.key === selectedCampaignTab.value) || null
})

function getOperatorCampaignTabs(op, planningsList) {
  const keys = new Set()
  
  function addDate(dateStr) {
    if (!dateStr || dateStr === '-') return
    let year = new Date().getFullYear()
    let month = 1
    if (dateStr.includes('/')) {
      const parts = dateStr.split('/')
      if (parts.length === 3) {
        year = parts[2]
        month = parseInt(parts[1], 10)
      }
    } else {
      const d = new Date(dateStr)
      year = d.getFullYear()
      month = d.getMonth() + 1
    }
    const category = month < 7 ? 'EVAL_INITIALE' : 'RECYCLAGE'
    keys.add(`${year}-${category}`)
  }
  
  if (op.genericPassed && op.genericDate) {
    addDate(op.genericDate)
  }
  
  if (op.workstations) {
    Object.values(op.workstations).forEach(ws => {
      if (ws.level && ws.date) {
        addDate(ws.date)
      }
    })
  }
  
  const opPlannings = planningsList.filter(p => p.operatorId === op.operatorId && p.status !== 'ANNULEE')
  opPlannings.forEach(p => {
    addDate(p.scheduledDate)
  })
  
  return keys
}

function shouldShowRecyclage(op) {
  return op.seniorityMonths > 6
}

function getColumnRecyclageDate(op, col) {
  if (col.isGeneric) return '-'
  return op.workstations?.[col.id]?.recyclageDate ? formatDate(op.workstations[col.id].recyclageDate) : '-'
}

function getRecyclageStatus(op, col) {
  return col.isGeneric ? '' : (op.workstations?.[col.id]?.recyclageStatus || '')
}

function getRecyclageDisplay(op, col) {
  if (col.isGeneric) return '-'
  const recyclage = op.workstations?.[col.id]
  if (!recyclage?.recyclageStatus) return '-'
  const labels = { PLANIFIEE: 'Planifie', EN_COURS: 'En cours', TERMINEE: 'Termine', ANNULEE: 'Annule' }
  const result = recyclage.recyclageLevel ? ` · ${recyclage.recyclageLevel}` : ''
  return `${labels[recyclage.recyclageStatus] || recyclage.recyclageStatus}${result}`
}

function recyclageClass(status) {
  return {
    PLANIFIEE: 'text-orange-700',
    EN_COURS: 'text-blue-700',
    TERMINEE: 'text-green-700 font-semibold',
    ANNULEE: 'text-gray-400',
  }[status] || 'text-gray-400'
}

function getTrainedCount(op) {
  if (!op.workstations) return 0
  let count = 0
  Object.values(op.workstations).forEach(ws => {
    if (['I', 'L', 'U'].includes(ws.level)) count++
  })
  return count
}

function getCountPerNiveau(col, niveau) {
  return (matrixData.value.operators || []).filter(op => getColumnLevel(op, col) === niveau).length
}

function getCompliantCount(col) {
  return getCountPerNiveau(col, 'L') + getCountPerNiveau(col, 'U')
}

function isWorkstationCompliant(col) {
  if (col.isGeneric) return true
  return getCompliantCount(col) >= 6
}

const niveauBgClass = (n) => ({
  I: 'bg-amber-400 text-white',
  L: 'bg-blue-500 text-white',
  U: 'bg-green-600 text-white'
}[n] || 'bg-gray-100 text-gray-400 border border-gray-200')

function formatDate(dateStr) {
  if (!dateStr) return '-'
  if (typeof dateStr !== 'string') return String(dateStr)
  if (dateStr.includes('/')) return dateStr
  try {
    const d = new Date(dateStr)
    if (isNaN(d.getTime())) return dateStr
    return d.toLocaleDateString('fr-FR', { day: '2-digit', month: '2-digit', year: 'numeric' })
  } catch {
    return dateStr
  }
}

function formatNiveau(level) {
  if (!level) return ''
  const upper = level.toUpperCase().trim()
  if (upper === 'I' || upper === 'NIVEAU_1' || upper === '1') return 'I'
  if (upper === 'L' || upper === 'NIVEAU_2' || upper === '2') return 'L'
  if (upper === 'U' || upper === 'NIVEAU_3' || upper === '3') return 'U'
  if (['I', 'L', 'U'].includes(upper.charAt(0))) return upper.charAt(0)
  return level
}

async function loadPlanningsForProject() {
  try {
    const projectId = selectedProject.value ? Number(selectedProject.value) : null
    if (projectId) {
      const res = await recyclageApi.getPlanning({ projectId })
      plannings.value = res.data || []
    } else {
      plannings.value = []
    }
  } catch (e) {
    console.error('Error loading plannings', e)
    plannings.value = []
  }
}

async function loadMatrix() {
  loading.value = true
  errorMsg.value = ''
  if (!authStore.user && authStore.isAuthenticated) authStore.restoreFromToken()
  try {
    const projectId = selectedProject.value ? Number(selectedProject.value) : null
    let year = null
    let type = null
    if (selectedCampaignTab.value && campaignTabs.value.length > 0) {
      const activeTab = campaignTabs.value.find(t => t.key === selectedCampaignTab.value)
      if (activeTab) {
        year = Number(activeTab.year)
        type = activeTab.type
      }
    }
    const res = await evaluationApi.getMatrix(projectId, year, type)
    matrixData.value = res.data || { operators: [], workstations: [] }
    selectedProjectName.value = res.data?.projectName || ''
  } catch (e) {
    console.error('Error loading matrix', e)
    errorMsg.value = e.response?.data?.error || e.response?.data?.message || e.message || 'Erreur inconnue'
  }
  loading.value = false
}

async function exportMatrixToExcel() {
  const workbook = new ExcelJS.Workbook()
  const worksheet = workbook.addWorksheet('SYSTEME ILU')

  const columnsList = allColumns.value
  const totalCols = columnsList.length + 3

  worksheet.getColumn(1).width = 30
  worksheet.getColumn(2).width = 24
  for (let c = 3; c <= columnsList.length + 2; c++) {
    worksheet.getColumn(c).width = 18
  }
  worksheet.getColumn(columnsList.length + 3).width = 20

  const borderStyle = {
    top: { style: 'thin', color: { argb: 'CBD5E1' } },
    left: { style: 'thin', color: { argb: 'CBD5E1' } },
    bottom: { style: 'thin', color: { argb: 'CBD5E1' } },
    right: { style: 'thin', color: { argb: 'CBD5E1' } }
  }

  // Row 1: Title
  const titleText = `MATRICE DE POLYVALENCE ${selectedProjectName.value ? selectedProjectName.value.toUpperCase() : ''}`
  const r1 = worksheet.addRow([titleText])
  worksheet.mergeCells(1, 1, 1, totalCols)
  r1.height = 30
  const titleCell = r1.getCell(1)
  titleCell.font = { name: 'Arial', size: 14, bold: true, color: { argb: '0F172A' } }
  titleCell.fill = { type: 'pattern', pattern: 'solid', fgColor: { argb: 'E0F2FE' } }
  titleCell.alignment = { vertical: 'middle', horizontal: 'left', indent: 1 }

  // Blank spacer row
  worksheet.addRow([])

  // Row 3: Zone Headers
  const row3Vals = ['SHIFT / Opérateur', 'Zone']
  zones.value.forEach(z => {
    for (let i = 0; i < z.workstations.length; i++) {
      row3Vals.push(i === 0 ? z.name : '')
    }
  })
  row3Vals.push("Nombre des postes sur lesquels est formé un opérateur")
  const r3 = worksheet.addRow(row3Vals)
  r3.height = 24

  let currentCol = 3
  zones.value.forEach(z => {
    const wsCount = z.workstations.length
    if (wsCount > 1) {
      worksheet.mergeCells(3, currentCol, 3, currentCol + wsCount - 1)
    }
    currentCol += wsCount
  })

  // Row 4: Workstation / Ref. Machine Headers
  const row4Vals = ['', 'Ref. Machine']
  columnsList.forEach(col => {
    row4Vals.push(col.name)
  })
  row4Vals.push('')
  const r4 = worksheet.addRow(row4Vals)
  r4.height = 24

  worksheet.mergeCells(3, 1, 4, 1)
  worksheet.mergeCells(3, 2, 4, 2)
  worksheet.mergeCells(3, totalCols, 4, totalCols)

  for (let r = 3; r <= 4; r++) {
    const row = worksheet.getRow(r)
    row.eachCell({ includeEmpty: true }, (cell, colNumber) => {
      if (colNumber <= totalCols) {
        cell.font = { name: 'Arial', size: 10, bold: true, color: { argb: '1E293B' } }
        cell.fill = { type: 'pattern', pattern: 'solid', fgColor: { argb: 'F1F5F9' } }
        cell.alignment = { vertical: 'middle', horizontal: 'center', wrapText: true }
        cell.border = borderStyle
      }
    })
  }

  // Row 5: Target per Station
  const row5Vals = ['', 'Target per Station']
  columnsList.forEach(col => {
    row5Vals.push(formatNiveau(col.targetIluLevel || col.targetLevel || 'L'))
  })
  row5Vals.push('')
  const r5 = worksheet.addRow(row5Vals)
  r5.height = 20
  r5.eachCell({ includeEmpty: true }, (cell, colNumber) => {
    if (colNumber <= totalCols) {
      cell.font = { name: 'Arial', size: 10, bold: true, color: { argb: '475569' } }
      cell.fill = { type: 'pattern', pattern: 'solid', fgColor: { argb: 'F8FAFC' } }
      cell.alignment = { vertical: 'middle', horizontal: 'center' }
      cell.border = borderStyle
    }
  })

  // Operator Data Rows
  const ops = displayOperators.value
  ops.forEach(op => {
    const startRowIdx = worksheet.rowCount + 1

    const dateVals = [op.operatorName, "Date d'évaluation"]
    columnsList.forEach(col => {
      dateVals.push(getColumnDate(op, col) || '-')
    })
    dateVals.push(getTrainedCount(op))
    const rDate = worksheet.addRow(dateVals)
    rDate.height = 20

    const levelVals = ['', "Niveau de compétences"]
    columnsList.forEach(col => {
      levelVals.push(formatNiveau(getColumnLevel(op, col)) || '-')
    })
    levelVals.push('')
    const rLevel = worksheet.addRow(levelVals)
    rLevel.height = 24

    const recyclageVals = ['', "Résultat de recyclage"]
    columnsList.forEach(col => {
      recyclageVals.push(getRecyclageDisplay(op, col) || '-')
    })
    recyclageVals.push('')
    const rRecyc = worksheet.addRow(recyclageVals)
    rRecyc.height = 20

    worksheet.mergeCells(startRowIdx, 1, startRowIdx + 2, 1)
    worksheet.mergeCells(startRowIdx, totalCols, startRowIdx + 2, totalCols)

    const opCell = worksheet.getCell(startRowIdx, 1)
    opCell.font = { name: 'Arial', size: 11, bold: true, color: { argb: '0F172A' } }
    opCell.alignment = { vertical: 'middle', horizontal: 'left', wrapText: true }
    opCell.border = borderStyle

    const trainedCell = worksheet.getCell(startRowIdx, totalCols)
    trainedCell.font = { name: 'Arial', size: 14, bold: true, color: { argb: '0F172A' } }
    trainedCell.fill = { type: 'pattern', pattern: 'solid', fgColor: { argb: 'F8FAFC' } }
    trainedCell.alignment = { vertical: 'middle', horizontal: 'center' }
    trainedCell.border = borderStyle

    rDate.eachCell({ includeEmpty: true }, (cell, colNum) => {
      if (colNum > 1 && colNum < totalCols) {
        cell.font = { name: 'Arial', size: 9, color: { argb: '64748B' } }
        cell.alignment = { vertical: 'middle', horizontal: 'center' }
        cell.border = borderStyle
        if (colNum === 2) {
          cell.font = { name: 'Arial', size: 9, italic: true, color: { argb: '475569' } }
          cell.alignment = { vertical: 'middle', horizontal: 'left' }
        }
      }
    })

    rLevel.eachCell({ includeEmpty: true }, (cell, colNum) => {
      if (colNum > 1 && colNum < totalCols) {
        cell.border = borderStyle
        if (colNum === 2) {
          cell.font = { name: 'Arial', size: 9, bold: true, color: { argb: '1E293B' } }
          cell.alignment = { vertical: 'middle', horizontal: 'left' }
        } else {
          const val = String(cell.value || '').trim().toUpperCase()
          cell.alignment = { vertical: 'middle', horizontal: 'center' }
          if (val === 'U') {
            cell.font = { name: 'Arial', size: 11, bold: true, color: { argb: 'FFFFFF' } }
            cell.fill = { type: 'pattern', pattern: 'solid', fgColor: { argb: '16A34A' } }
          } else if (val === 'L') {
            cell.font = { name: 'Arial', size: 11, bold: true, color: { argb: 'FFFFFF' } }
            cell.fill = { type: 'pattern', pattern: 'solid', fgColor: { argb: '3B82F6' } }
          } else if (val === 'I') {
            cell.font = { name: 'Arial', size: 11, bold: true, color: { argb: 'FFFFFF' } }
            cell.fill = { type: 'pattern', pattern: 'solid', fgColor: { argb: 'F59E0B' } }
          } else {
            cell.font = { name: 'Arial', size: 9, color: { argb: '94A3B8' } }
          }
        }
      }
    })

    rRecyc.eachCell({ includeEmpty: true }, (cell, colNum) => {
      if (colNum > 1 && colNum < totalCols) {
        cell.font = { name: 'Arial', size: 9, color: { argb: 'C2410C' } }
        cell.alignment = { vertical: 'middle', horizontal: 'center' }
        cell.border = borderStyle
        if (colNum === 2) {
          cell.font = { name: 'Arial', size: 9, bold: true, color: { argb: 'C2410C' } }
          cell.alignment = { vertical: 'middle', horizontal: 'left' }
        }
      }
    })
  })

  // Footer Rows (Counts & Compliance)
  const sumIRowVals = ["Nombres de personnes au niveau I", ""]
  const sumLRowVals = ["Nombres de personnes au niveau L", ""]
  const sumURowVals = ["Nombres de personnes au niveau U", ""]
  const complianceRowVals = ["Statut Conformité (Cible L/U >= 6)", ""]

  columnsList.forEach(col => {
    sumIRowVals.push(getCountPerNiveau(col, 'I'))
    sumLRowVals.push(getCountPerNiveau(col, 'L'))
    sumURowVals.push(getCountPerNiveau(col, 'U'))
    if (col.isGeneric) {
      complianceRowVals.push("-")
    } else {
      complianceRowVals.push(isWorkstationCompliant(col) ? `✅ Conforme (${getCompliantCount(col)}/6)` : `⚠️ Risque (${getCompliantCount(col)}/6)`)
    }
  })
  sumIRowVals.push("")
  sumLRowVals.push("")
  sumURowVals.push("")
  complianceRowVals.push("")

  const rI = worksheet.addRow(sumIRowVals)
  const rL = worksheet.addRow(sumLRowVals)
  const rU = worksheet.addRow(sumURowVals)
  const rComp = worksheet.addRow(complianceRowVals)

  const footStart = worksheet.rowCount - 3
  worksheet.mergeCells(footStart, 1, footStart, 2)
  worksheet.mergeCells(footStart + 1, 1, footStart + 1, 2)
  worksheet.mergeCells(footStart + 2, 1, footStart + 2, 2)
  worksheet.mergeCells(footStart + 3, 1, footStart + 3, 2)

  rI.height = 22
  rI.eachCell({ includeEmpty: true }, (cell, c) => {
    if (c <= totalCols) {
      cell.font = { name: 'Arial', size: 10, bold: true, color: { argb: 'B45309' } }
      cell.fill = { type: 'pattern', pattern: 'solid', fgColor: { argb: 'FEF3C7' } }
      cell.alignment = { vertical: 'middle', horizontal: c === 1 ? 'left' : 'center' }
      cell.border = borderStyle
    }
  })

  rL.height = 22
  rL.eachCell({ includeEmpty: true }, (cell, c) => {
    if (c <= totalCols) {
      cell.font = { name: 'Arial', size: 10, bold: true, color: { argb: '1D4ED8' } }
      cell.fill = { type: 'pattern', pattern: 'solid', fgColor: { argb: 'DBEAFE' } }
      cell.alignment = { vertical: 'middle', horizontal: c === 1 ? 'left' : 'center' }
      cell.border = borderStyle
    }
  })

  rU.height = 22
  rU.eachCell({ includeEmpty: true }, (cell, c) => {
    if (c <= totalCols) {
      cell.font = { name: 'Arial', size: 10, bold: true, color: { argb: '15803D' } }
      cell.fill = { type: 'pattern', pattern: 'solid', fgColor: { argb: 'DCFCE7' } }
      cell.alignment = { vertical: 'middle', horizontal: c === 1 ? 'left' : 'center' }
      cell.border = borderStyle
    }
  })

  rComp.height = 24
  rComp.eachCell({ includeEmpty: true }, (cell, c) => {
    if (c <= totalCols) {
      cell.border = borderStyle
      if (c === 1) {
        cell.font = { name: 'Arial', size: 10, bold: true, color: { argb: '334155' } }
        cell.fill = { type: 'pattern', pattern: 'solid', fgColor: { argb: 'F1F5F9' } }
        cell.alignment = { vertical: 'middle', horizontal: 'left' }
      } else {
        const val = String(cell.value || '')
        cell.alignment = { vertical: 'middle', horizontal: 'center' }
        if (val.includes('Conforme')) {
          cell.font = { name: 'Arial', size: 10, bold: true, color: { argb: '065F46' } }
          cell.fill = { type: 'pattern', pattern: 'solid', fgColor: { argb: 'D1FAE5' } }
        } else if (val.includes('Risque')) {
          cell.font = { name: 'Arial', size: 10, bold: true, color: { argb: '991B1B' } }
          cell.fill = { type: 'pattern', pattern: 'solid', fgColor: { argb: 'FFE4E6' } }
        } else {
          cell.fill = { type: 'pattern', pattern: 'solid', fgColor: { argb: 'F8FAFC' } }
        }
      }
    }
  })

  const buffer = await workbook.xlsx.writeBuffer()
  const blob = new Blob([buffer], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
  const url = window.URL.createObjectURL(blob)
  const anchor = document.createElement('a')
  anchor.href = url
  anchor.download = `SYSTEME_ILU_${selectedProjectName.value || 'MATRICE'}_${new Date().toISOString().slice(0, 10)}.xlsx`
  anchor.click()
  window.URL.revokeObjectURL(url)
}

function openImportModal() {
  importFile.value = null
  parsedCertifications.value = []
  importError.value = ''
  importSuccess.value = ''
  showImportModal.value = true
}

function closeImportModal() {
  showImportModal.value = false
  importFile.value = null
  parsedCertifications.value = []
  importError.value = ''
  importSuccess.value = ''
}

function handleFileChange(event) {
  const file = event.target.files[0]
  if (!file) return
  importFile.value = file
  importError.value = ''
  importSuccess.value = ''
  
  const reader = new FileReader()
  reader.onload = (e) => {
    try {
      const data = new Uint8Array(e.target.result)
      const workbook = XLSX.read(data, { type: 'array' })
      const firstSheetName = workbook.SheetNames[0]
      const worksheet = workbook.Sheets[firstSheetName]
      const jsonData = XLSX.utils.sheet_to_json(worksheet, { header: 1 })
      
      if (!jsonData || jsonData.length < 2) {
        importError.value = "Le fichier Excel est vide ou ne contient pas assez de données."
        return
      }

      // Check if this is a Native OPmobility Matrix spreadsheet (e.g. SYSTEME ILU CMP 2026.xlsx)
      let isNativeMatrix = false
      let wsHeaderRowIdx = -1
      
      for (let r = 0; r < Math.min(20, jsonData.length); r++) {
        const row = jsonData[r] || []
        const rowStr = row.map(cell => String(cell || '').toLowerCase()).join(' ')
        if (rowStr.includes('target per station') || rowStr.includes('ref. machine') || rowStr.includes('partie générique') || rowStr.includes('partie generique') || rowStr.includes('résultat de recyclage') || rowStr.includes('niveau de compétences')) {
          isNativeMatrix = true
          wsHeaderRowIdx = r
          break
        }
      }

      const list = []

      if (isNativeMatrix) {
        // Native OPmobility Matrix Parser
        let wsRowIdx = -1
        for (let r = 0; r < Math.min(25, jsonData.length); r++) {
          const rowStr = (jsonData[r] || []).map(cell => String(cell || '').toLowerCase()).join(' ')
          if (rowStr.includes('ref. machine') || rowStr.includes('sécurité') || rowStr.includes('securite')) {
            wsRowIdx = r
            break
          }
        }

        if (wsRowIdx === -1) {
          wsRowIdx = wsHeaderRowIdx
        }

        const wsRow = jsonData[wsRowIdx] || []
        const zoneRow = wsRowIdx > 0 ? (jsonData[wsRowIdx - 1] || []) : []
        wsHeaderRowIdx = wsRowIdx

        // Detect project name from sheet or file name (e.g. SYSTEME ILU CMP 2026.xlsx -> CMP 2026)
        let detectedProject = file.name.replace(/\.xlsx$/i, '').replace(/SYSTEME ILU/i, '').trim()
        if (!detectedProject) detectedProject = 'CMP 2026'

        const workstations = []
        let lastZoneName = 'Zone Generale'
        for (let c = 0; c < wsRow.length; c++) {
          if (zoneRow[c] && String(zoneRow[c]).trim()) {
            const zCandidate = String(zoneRow[c]).trim()
            if (!zCandidate.toLowerCase().includes('shift') && !zCandidate.toLowerCase().includes('zone')) {
              lastZoneName = zCandidate
            }
          }

          const val = String(wsRow[c] || '').trim()
          if (val && !val.toLowerCase().includes('shift') && !val.toLowerCase().includes('zone') && !val.toLowerCase().includes('ref. machine') && !val.toLowerCase().includes('nombre') && !val.toLowerCase().includes('opérateur')) {
            workstations.push({ colIdx: c, name: val, zoneName: lastZoneName })
          }
        }

        if (workstations.length === 0) {
          importError.value = "Impossible d'identifier les postes de travail dans le fichier matrice OPmobility."
          return
        }

        // Iterate through rows looking for operator names and levels
        for (let r = wsHeaderRowIdx + 1; r < jsonData.length; r++) {
          const row = jsonData[r] || []
          let opName = null
          for (let col = 0; col < 3; col++) {
            const cellVal = String(row[col] || '').trim()
            if (cellVal && cellVal.length > 2 && !cellVal.toLowerCase().includes('nombres') && !cellVal.toLowerCase().includes('target') && !cellVal.toLowerCase().includes('statut') && !cellVal.toLowerCase().includes('shift')) {
              opName = cellVal
              break
            }
          }

          if (opName) {
            const nextRow = jsonData[r + 1] || []
            workstations.forEach(ws => {
              const lvlVal = String(nextRow[ws.colIdx] || row[ws.colIdx] || '').trim().toUpperCase()
              const dateCell = row[ws.colIdx] || nextRow[ws.colIdx]
              
              if (['I', 'L', 'U'].includes(lvlVal)) {
                let valDate = null
                if (dateCell && typeof dateCell === 'number') {
                  const dateObj = XLSX.SSF.parse_date_code(dateCell)
                  if (dateObj) {
                    const d = new Date(dateObj.y, dateObj.m - 1, dateObj.d)
                    valDate = d.toISOString().slice(0, 10)
                  }
                } else if (dateCell && /^\d{4}-\d{2}-\d{2}/.test(String(dateCell))) {
                  valDate = String(dateCell).slice(0, 10)
                }

                list.push({
                  operatorName: opName,
                  workstationName: ws.name,
                  zoneName: ws.zoneName,
                  projectName: detectedProject,
                  level: lvlVal,
                  validationDate: valDate
                })
              }
            })
            r++
          }
        }
      } else {
        // Flat Table Parser (Matricule / Nom, Poste de Travail, Niveau)
        const headers = jsonData[0].map(h => String(h).trim().toLowerCase())
        const matriculeIdx = headers.findIndex(h => h.includes('matricule') || h.includes('code') || h.includes('id'))
        const nameIdx = headers.findIndex(h => h.includes('nom') || h.includes('opérateur') || h.includes('operateur'))
        const wsIdx = headers.findIndex(h => h.includes('poste') || h.includes('workstation') || h.includes('machine'))
        const levelIdx = headers.findIndex(h => h.includes('niveau') || h.includes('level') || h.includes('grade'))
        const dateIdx = headers.findIndex(h => h.includes('date') || h.includes('validation'))

        if ((matriculeIdx === -1 && nameIdx === -1) || wsIdx === -1 || levelIdx === -1) {
          importError.value = "Colonnes requises manquantes. Assurez-vous d'avoir des colonnes nommées 'Matricule' (ou 'Nom'), 'Poste de Travail', et 'Niveau'."
          return
        }

        for (let i = 1; i < jsonData.length; i++) {
          const row = jsonData[i]
          if (!row || row.length === 0 || (!row[matriculeIdx] && !row[nameIdx]) || !row[wsIdx] || !row[levelIdx]) continue

          let valDate = null
          if (dateIdx !== -1 && row[dateIdx]) {
            if (typeof row[dateIdx] === 'number') {
              const dateObj = XLSX.SSF.parse_date_code(row[dateIdx])
              const d = new Date(dateObj.y, dateObj.m - 1, dateObj.d)
              valDate = d.toISOString().slice(0, 10)
            } else {
              const rawStr = String(row[dateIdx]).trim()
              if (/^\d{4}-\d{2}-\d{2}$/.test(rawStr)) {
                valDate = rawStr
              } else if (/^\d{2}\/\d{2}\/\d{4}$/.test(rawStr)) {
                const [day, m, y] = rawStr.split('/')
                valDate = `${y}-${m}-${day}`
              }
            }
          }

          list.push({
            employeeId: matriculeIdx !== -1 && row[matriculeIdx] ? String(row[matriculeIdx]).trim() : null,
            operatorName: nameIdx !== -1 && row[nameIdx] ? String(row[nameIdx]).trim() : null,
            workstationName: String(row[wsIdx]).trim(),
            level: String(row[levelIdx]).trim().toUpperCase(),
            validationDate: valDate
          })
        }
      }

      if (list.length === 0) {
        importError.value = "Aucune certification valide (Niveau I, L, U) détectée dans le fichier."
      } else {
        parsedCertifications.value = list
      }
    } catch (err) {
      console.error(err)
      importError.value = "Erreur lors de la lecture du fichier Excel."
    }
  }
  reader.readAsArrayBuffer(file)
}

async function submitImport() {
  if (parsedCertifications.value.length === 0) return
  importing.value = true
  importError.value = ''
  importSuccess.value = ''
  
  try {
    const res = await evaluationApi.importCertifications(parsedCertifications.value)
    const successCount = (res.data || []).filter(r => r.status === 'SUCCESS').length
    importSuccess.value = `${successCount} sur ${parsedCertifications.value.length} certifications ont été importées avec succès !`
    parsedCertifications.value = []
    await loadMatrix()
  } catch (err) {
    console.error(err)
    importError.value = err.response?.data?.message || err.message || "Erreur lors de l'importation."
  } finally {
    importing.value = false
  }
}

async function loadProjects() {
  if (!isMultiProjectRole.value) return
  try {
    const res = await structureApi.getAll()
    projects.value = res.data || []
    selectedProject.value = ''
  } catch (e) {
    console.error('Error loading projects', e)
  }
}

watch(selectedProject, async () => {
  await loadPlanningsForProject()
  await loadMatrix()
})

watch(selectedCampaignTab, () => {
  loadMatrix()
})

onMounted(async () => {
  await loadUserProjects()
  try {
    const opsRes = await operatorsApi.getAll()
    allOperatorsData.value = opsRes.data || []
  } catch (e) {
    console.error('Error loading operators:', e)
  }
  await loadProjects()
  await loadPlanningsForProject()
  await loadMatrix()
})
</script>