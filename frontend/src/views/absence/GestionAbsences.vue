<template>
  <div class="space-y-6">
    <!-- Header -->
    <div>
      <h1 class="text-2xl font-bold text-gray-900">Statuts & Absences des Opérateurs</h1>
      <p class="text-sm text-gray-500 mt-1">
        Déclarer les absences (RH uniquement), les reprises d'activités (RH/Chef d'équipe) et les départs (RH/Chef d'équipe).
      </p>
    </div>

    <!-- Filter and Search Bar -->
    <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-4 flex flex-col md:flex-row md:items-center justify-between gap-4">
      <div class="relative flex-1 max-w-md">
        <svg class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path>
        </svg>
        <input
          v-model="searchQuery"
          type="text"
          placeholder="Rechercher un opérateur par nom ou matricule..."
          class="w-full pl-10 pr-4 py-2 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 outline-none"
        />
      </div>

      <!-- Tabs/Filters -->
      <div class="flex items-center gap-1.5 overflow-x-auto pb-1 md:pb-0">
        <button
          v-for="status in ['TOUS', 'EN_POSTE', 'ABSENT', 'SORTI', 'HISTORIQUE']"
          :key="status"
          @click="selectedStatusFilter = status"
          :class="[
            selectedStatusFilter === status
              ? 'bg-slate-800 text-white font-semibold'
              : 'bg-gray-100 text-gray-600 hover:bg-gray-200',
            'px-4 py-2 rounded-lg text-xs transition whitespace-nowrap'
          ]"
        >
          {{ statusLabel(status) }}
        </button>
      </div>
    </div>

    <!-- Operators Status Table -->
    <div v-if="selectedStatusFilter !== 'HISTORIQUE'" class="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
      <div class="overflow-x-auto">
        <table class="min-w-full divide-y divide-gray-200 text-sm">
          <thead class="bg-gray-50">
            <tr>
              <th scope="col" class="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">Opérateur</th>
              <th scope="col" class="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">Affectation</th>
              <th scope="col" class="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">Statut Actuel</th>
              <th scope="col" class="px-6 py-3 text-right text-xs font-semibold text-gray-500 uppercase tracking-wider">Actions</th>
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
            <tr v-for="op in filteredOperators" :key="op.id" class="hover:bg-gray-50/50">
              <td class="px-6 py-4 whitespace-nowrap">
                <div class="font-semibold text-gray-900">{{ op.lastName }} {{ op.firstName }}</div>
                <div class="text-xs text-gray-400">Matricule: {{ op.employeeId }}</div>
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-gray-600 text-xs">
                <div>Projet: <span class="font-medium text-gray-800">{{ op.project?.name || '-' }}</span></div>
                <div>Zone: <span class="font-medium text-gray-800">{{ op.zone?.name || '-' }}</span></div>
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <span :class="badgeClass(getOperatorState(op))" class="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-semibold border">
                  <span class="w-1.5 h-1.5 rounded-full" :class="dotClass(getOperatorState(op))"></span>
                  {{ getOperatorStateLabel(op) }}
                </span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-right text-xs space-x-2">
                <!-- Actions for EN_POSTE -->
                <template v-if="getOperatorState(op) === 'EN_POSTE'">
                  <button
                    @click="promptAbsent(op)"
                    :disabled="!canDeclareAbsent"
                    :class="canDeclareAbsent ? 'bg-orange-50 text-orange-700 border-orange-200 hover:bg-orange-100' : 'bg-gray-50 text-gray-400 border-gray-100 cursor-not-allowed'"
                    class="px-3 py-1.5 border rounded-lg font-medium transition"
                    title="Déclarer absent (Réservé RH)"
                  >
                    Déclarer Absent
                  </button>
                  <button
                    @click="promptDeparture(op)"
                    :disabled="!canManageRepriseOrSortie"
                    :class="canManageRepriseOrSortie ? 'bg-red-50 text-red-700 border-red-200 hover:bg-red-100' : 'bg-gray-50 text-gray-400 border-gray-100 cursor-not-allowed'"
                    class="px-3 py-1.5 border rounded-lg font-medium transition"
                  >
                    Déclarer Départ
                  </button>
                </template>

                <!-- Actions for ABSENT -->
                <template v-else-if="getOperatorState(op) === 'ABSENT'">
                  <button
                    @click="promptReturn(op)"
                    :disabled="!canManageRepriseOrSortie"
                    :class="canManageRepriseOrSortie ? 'bg-emerald-50 text-emerald-700 border-emerald-200 hover:bg-emerald-100' : 'bg-gray-50 text-gray-400 border-gray-100 cursor-not-allowed'"
                    class="px-3 py-1.5 border rounded-lg font-medium transition"
                  >
                    Marquer Reprise
                  </button>
                </template>

                <!-- Actions for SORTI -->
                <template v-else-if="getOperatorState(op) === 'SORTI'">
                  <button
                    @click="promptReactivate(op)"
                    :disabled="!canManageRepriseOrSortie"
                    :class="canManageRepriseOrSortie ? 'bg-blue-50 text-blue-700 border-blue-200 hover:bg-blue-100' : 'bg-gray-50 text-gray-400 border-gray-100 cursor-not-allowed'"
                    class="px-3 py-1.5 border rounded-lg font-medium transition"
                  >
                    Réactiver
                  </button>
                </template>
              </td>
            </tr>
            <tr v-if="filteredOperators.length === 0">
              <td colspan="4" class="px-6 py-12 text-center text-gray-400">Aucun opérateur trouvé</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Absence History Table -->
    <div v-else class="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
      <div class="overflow-x-auto">
        <table class="min-w-full divide-y divide-gray-200 text-sm">
          <thead class="bg-gray-50">
            <tr>
              <th scope="col" class="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">Opérateur</th>
              <th scope="col" class="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">Date Début</th>
              <th scope="col" class="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">Reprise Prévue</th>
              <th scope="col" class="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">Reprise Réelle</th>
              <th scope="col" class="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">Statut</th>
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
            <tr v-for="item in filteredHistory" :key="item.id" class="hover:bg-gray-50/50">
              <td class="px-6 py-4 whitespace-nowrap">
                <div class="font-semibold text-gray-900">{{ item.operatorName }}</div>
                <div class="text-xs text-gray-400">Matricule: {{ item.employeeId }}</div>
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-gray-600 text-xs">
                {{ item.startDate }}
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-gray-600 text-xs">
                {{ item.expectedReturnDate || '-' }}
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-gray-600 text-xs">
                {{ item.actualReturnDate || '-' }}
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <span :class="item.status === 'EN_COURS' ? 'bg-orange-50 text-orange-700 border-orange-200' : 'bg-emerald-50 text-emerald-700 border-emerald-200'" class="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-semibold border">
                  <span class="w-1.5 h-1.5 rounded-full" :class="item.status === 'EN_COURS' ? 'bg-orange-500' : 'bg-emerald-500'"></span>
                  {{ item.status === 'EN_COURS' ? 'Absent' : 'Reprise effectuée' }}
                </span>
              </td>
            </tr>
            <tr v-if="filteredHistory.length === 0">
              <td colspan="5" class="px-6 py-12 text-center text-gray-400">Aucun historique d'absence trouvé</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Modals -->

    <!-- Modal: Mark Absent -->
    <div v-if="activeModal === 'absent'" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50" @click.self="closeModal">
      <div class="bg-white rounded-xl shadow-xl w-full max-w-md mx-4 p-6">
        <h3 class="text-lg font-bold text-gray-900 mb-2">Déclarer l'Opérateur Absent</h3>
        <p class="text-xs text-gray-500 mb-4">
          Marquer <strong class="text-gray-700">{{ selectedOperator?.lastName }} {{ selectedOperator?.firstName }}</strong> absent pour maladie ou congé de longue durée (+3 mois).
        </p>
        <div class="space-y-4">
          <div>
            <label class="block text-xs font-semibold text-gray-600 mb-1">Date de début d'absence</label>
            <input v-model="absentForm.startDate" type="date" class="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm outline-none focus:border-emerald-500">
          </div>
          <div>
            <label class="block text-xs font-semibold text-gray-600 mb-1">Date de retour prévue (optionnel)</label>
            <input v-model="absentForm.expectedReturnDate" type="date" class="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm outline-none focus:border-emerald-500">
          </div>
        </div>
        <div class="flex justify-end gap-3 mt-6">
          <button @click="closeModal" class="px-4 py-2 text-xs text-gray-600 border rounded-lg hover:bg-gray-50">Annuler</button>
          <button @click="submitAbsent" :disabled="loading" class="px-4 py-2 text-xs bg-orange-500 text-white rounded-lg hover:bg-orange-600 disabled:opacity-50 font-semibold">
            Confirmer l'Absence
          </button>
        </div>
      </div>
    </div>

    <!-- Modal: Mark Exit (Départ) -->
    <div v-if="activeModal === 'depart'" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50" @click.self="closeModal">
      <div class="bg-white rounded-xl shadow-xl w-full max-w-md mx-4 p-6">
        <h3 class="text-lg font-bold text-gray-900 mb-2">Déclarer le Départ de l'Opérateur</h3>
        <p class="text-xs text-gray-500 mb-4">
          Enregistrer le départ définitif (fin de contrat, démission, etc.) de <strong class="text-gray-700">{{ selectedOperator?.lastName }} {{ selectedOperator?.firstName }}</strong>.
        </p>
        <div>
          <label class="block text-xs font-semibold text-gray-600 mb-1">Date de départ (sortie)</label>
          <input v-model="exitForm.exitDate" type="date" class="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm outline-none focus:border-emerald-500">
        </div>
        <div class="flex justify-end gap-3 mt-6">
          <button @click="closeModal" class="px-4 py-2 text-xs text-gray-600 border rounded-lg hover:bg-gray-50">Annuler</button>
          <button @click="submitDeparture" :disabled="loading" class="px-4 py-2 text-xs bg-red-600 text-white rounded-lg hover:bg-red-700 disabled:opacity-50 font-semibold">
            Confirmer le Départ
          </button>
        </div>
      </div>
    </div>

    <!-- Modal: Mark Reprise -->
    <div v-if="activeModal === 'reprise'" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50" @click.self="closeModal">
      <div class="bg-white rounded-xl shadow-xl w-full max-w-md mx-4 p-6">
        <h3 class="text-lg font-bold text-gray-900 mb-2">Déclarer la Reprise de l'Opérateur</h3>
        <p class="text-xs text-gray-500 mb-4">
          Valider le retour au travail de <strong class="text-gray-700">{{ selectedOperator?.lastName }} {{ selectedOperator?.firstName }}</strong>. Un recyclage sera automatiquement planifié.
        </p>
        <div>
          <label class="block text-xs font-semibold text-gray-600 mb-1">Date de reprise (retour effectif)</label>
          <input v-model="repriseForm.returnDate" type="date" class="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm outline-none focus:border-emerald-500">
        </div>
        <div class="flex justify-end gap-3 mt-6">
          <button @click="closeModal" class="px-4 py-2 text-xs text-gray-600 border rounded-lg hover:bg-gray-50">Annuler</button>
          <button @click="submitReturn" :disabled="loading" class="px-4 py-2 text-xs bg-emerald-600 text-white rounded-lg hover:bg-emerald-700 disabled:opacity-50 font-semibold">
            Confirmer la Reprise
          </button>
        </div>
      </div>
    </div>

    <!-- Modal: Reactivate -->
    <div v-if="activeModal === 'reactivate'" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50" @click.self="closeModal">
      <div class="bg-white rounded-xl shadow-xl w-full max-w-md mx-4 p-6">
        <h3 class="text-lg font-bold text-gray-900 mb-2">Réactiver l'Opérateur ?</h3>
        <p class="text-xs text-gray-500 mb-6">
          Voulez-vous réactiver l'opérateur <strong class="text-gray-700">{{ selectedOperator?.lastName }} {{ selectedOperator?.firstName }}</strong> ? Ses dates de départ et motifs d'absence seront réinitialisés.
        </p>
        <div class="flex justify-end gap-3">
          <button @click="closeModal" class="px-4 py-2 text-xs text-gray-600 border rounded-lg hover:bg-gray-50">Annuler</button>
          <button @click="submitReactivate" :disabled="loading" class="px-4 py-2 text-xs bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 font-semibold">
            Confirmer la Réactivation
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { operatorsApi } from '@/api/endpoints'
import { absenceApi } from '@/services/absenceApi'
import { useAuthStore } from '@/stores/auth'
import { useUserScope } from '@/composables/useUserScope'

const authStore = useAuthStore()
const { loadUserProjects, filterOperators } = useUserScope()

// State
const operatorsList = ref([])
const absencesHistory = ref([])
const loading = ref(false)
const searchQuery = ref('')
const selectedStatusFilter = ref('TOUS')
const activeModal = ref(null)
const selectedOperator = ref(null)

// Forms State
const absentForm = ref({ startDate: '', expectedReturnDate: '' })
const exitForm = ref({ exitDate: '' })
const repriseForm = ref({ returnDate: '' })

// Permissions
const canDeclareAbsent = computed(() => authStore.hasAnyRole(['RH', 'ADMIN']))
const canManageRepriseOrSortie = computed(() => authStore.hasAnyRole(['CHEF_EQUIPE', 'RH', 'ADMIN']))

// Get operator state helper
function getOperatorState(op) {
  if (op.active === false) {
    if (op.exitDate) return 'SORTI'
    return 'ABSENT'
  }
  return 'EN_POSTE'
}

function getOperatorStateLabel(op) {
  const state = getOperatorState(op)
  if (state === 'EN_POSTE') return 'En poste'
  if (state === 'ABSENT') return 'Absent'
  return 'Sorti (Départ)'
}

// Styling classes
function badgeClass(state) {
  if (state === 'EN_POSTE') return 'bg-emerald-50 text-emerald-700 border-emerald-200'
  if (state === 'ABSENT') return 'bg-orange-50 text-orange-700 border-orange-200'
  return 'bg-rose-50 text-rose-700 border-rose-200'
}

function dotClass(state) {
  if (state === 'EN_POSTE') return 'bg-emerald-500'
  if (state === 'ABSENT') return 'bg-orange-500'
  return 'bg-rose-500'
}

function statusLabel(status) {
  const map = { TOUS: 'Tous', EN_POSTE: 'En poste', ABSENT: 'Absents', SORTI: 'Sortis', HISTORIQUE: 'Historique des absences' }
  return map[status] || status
}

// Fetch Operators list
async function loadOperators() {
  loading.value = true
  try {
    const res = await operatorsApi.getAll()
    operatorsList.value = res.data || []
  } catch (e) {
    console.error('Error loading operators:', e)
  } finally {
    loading.value = false
  }
}

// Fetch Absences History
async function loadHistory() {
  try {
    const res = await absenceApi.getAll()
    absencesHistory.value = res.data || []
  } catch (e) {
    console.error('Error loading absences history:', e)
  }
}

const scopedOperatorsList = computed(() => filterOperators(operatorsList.value))

const filteredHistory = computed(() => {
  let list = absencesHistory.value
  const scopedOps = filterOperators(operatorsList.value)
  const scopedOpIds = new Set(scopedOps.map(o => o.id))
  list = list.filter(a => scopedOpIds.has(a.operatorId))

  if (searchQuery.value.trim()) {
    const q = searchQuery.value.toLowerCase().trim()
    list = list.filter(a => {
      const name = (a.operatorName || '').toLowerCase()
      const mat = (a.employeeId || '').toLowerCase()
      return name.includes(q) || mat.includes(q)
    })
  }
  return list
})

// Filtering
const filteredOperators = computed(() => {
  let list = scopedOperatorsList.value

  // Status Filter
  if (selectedStatusFilter.value !== 'TOUS') {
    list = list.filter(op => getOperatorState(op) === selectedStatusFilter.value)
  }

  // Search Filter
  if (searchQuery.value.trim()) {
    const q = searchQuery.value.toLowerCase().trim()
    list = list.filter(op => {
      const name = `${op.lastName || ''} ${op.firstName || ''}`.toLowerCase()
      const mat = (op.employeeId || '').toLowerCase()
      return name.includes(q) || mat.includes(q)
    })
  }

  return list
})

// Modals Triggers
function promptAbsent(op) {
  selectedOperator.value = op
  absentForm.value = { startDate: new Date().toISOString().split('T')[0], expectedReturnDate: '' }
  activeModal.value = 'absent'
}

function promptDeparture(op) {
  selectedOperator.value = op
  exitForm.value = { exitDate: new Date().toISOString().split('T')[0] }
  activeModal.value = 'depart'
}

function promptReturn(op) {
  selectedOperator.value = op
  repriseForm.value = { returnDate: new Date().toISOString().split('T')[0] }
  activeModal.value = 'reprise'
}

function promptReactivate(op) {
  selectedOperator.value = op
  activeModal.value = 'reactivate'
}

function closeModal() {
  activeModal.value = null
  selectedOperator.value = null
}

// Submit actions
async function submitAbsent() {
  if (!selectedOperator.value || !absentForm.value.startDate) return
  loading.value = true
  try {
    await absenceApi.markAbsent({
      operatorId: selectedOperator.value.id,
      startDate: absentForm.value.startDate,
      expectedReturnDate: absentForm.value.expectedReturnDate || null
    })
    closeModal()
    await loadOperators()
    await loadHistory()
  } catch (e) {
    console.error(e)
    alert("Erreur lors de la déclaration d'absence : " + (e.response?.data?.message || e.message))
  } finally {
    loading.value = false
  }
}

async function submitDeparture() {
  if (!selectedOperator.value || !exitForm.value.exitDate) return
  loading.value = true
  try {
    await absenceApi.markDeparture({
      operatorId: selectedOperator.value.id,
      exitDate: exitForm.value.exitDate
    })
    closeModal()
    await loadOperators()
    await loadHistory()
  } catch (e) {
    console.error(e)
    alert("Erreur lors de la déclaration de départ : " + (e.response?.data?.message || e.message))
  } finally {
    loading.value = false
  }
}

async function submitReturn() {
  if (!selectedOperator.value || !repriseForm.value.returnDate) return
  loading.value = true
  try {
    await absenceApi.markReturn({
      operatorId: selectedOperator.value.id,
      returnDate: repriseForm.value.returnDate
    })
    closeModal()
    await loadOperators()
    await loadHistory()
  } catch (e) {
    console.error(e)
    alert("Erreur lors de la déclaration de reprise : " + (e.response?.data?.message || e.message))
  } finally {
    loading.value = false
  }
}

async function submitReactivate() {
  if (!selectedOperator.value) return
  loading.value = true
  try {
    await operatorsApi.activate(selectedOperator.value.id)
    closeModal()
    await loadOperators()
    await loadHistory()
  } catch (e) {
    console.error(e)
    alert("Erreur lors de la réactivation : " + (e.response?.data?.message || e.message))
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await loadUserProjects()
  loadOperators()
  loadHistory()
})
</script>
