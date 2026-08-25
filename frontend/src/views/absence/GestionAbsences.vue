<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">Gestion des Absences</h1>
        <p class="text-sm text-gray-500 mt-1">Marquer les absences, reprises et departs des operateurs</p>
      </div>
      <div class="flex items-center gap-3">
        <button @click="openModal('absent')" class="inline-flex items-center gap-2 px-4 py-2 bg-orange-500 text-white rounded-lg hover:bg-orange-600 transition text-sm font-medium">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"/></svg>
          Marquer Absent
        </button>
        <button @click="openModal('depart')" class="inline-flex items-center gap-2 px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition text-sm font-medium">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"/></svg>
          Marquer Depart
        </button>
      </div>
    </div>

    <!-- Tabs -->
    <div class="bg-white rounded-xl shadow-sm border border-gray-200">
      <div class="border-b border-gray-200">
        <nav class="flex">
          <button @click="activeTab = 'active'" :class="activeTab === 'active' ? 'border-emerald-500 text-emerald-600' : 'border-transparent text-gray-500 hover:text-gray-700'" class="px-6 py-3 text-sm font-medium border-b-2 transition">Absences Actives ({{ activeAbsences.length }})</button>
          <button @click="activeTab = 'all'" :class="activeTab === 'all' ? 'border-emerald-500 text-emerald-600' : 'border-transparent text-gray-500 hover:text-gray-700'" class="px-6 py-3 text-sm font-medium border-b-2 transition">Toutes</button>
        </nav>
      </div>

      <div class="overflow-x-auto">
        <table class="min-w-full divide-y divide-gray-200">
          <thead class="bg-gray-50">
            <tr>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Operateur</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Date Debut</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Retour Prevu</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Date Reprise</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Statut</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Actions</th>
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
            <tr v-for="item in displayedItems" :key="item.id" class="hover:bg-gray-50">
              <td class="px-4 py-3">
                <span class="text-sm font-medium text-gray-900">{{ item.operatorName }}</span>
                <span class="block text-xs text-gray-400">{{ item.operatorEmployeeId || '' }}</span>
              </td>
              <td class="px-4 py-3 text-sm text-gray-700">{{ formatDate(item.startDate) }}</td>
              <td class="px-4 py-3 text-sm text-gray-700">{{ formatDate(item.expectedReturnDate) }}</td>
              <td class="px-4 py-3 text-sm text-gray-700">{{ formatDate(item.actualReturnDate) }}</td>
              <td class="px-4 py-3">
                <span :class="item.status === 'EN_COURS' ? 'bg-orange-100 text-orange-800' : 'bg-green-100 text-green-800'" class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium">
                  {{ item.status === 'EN_COURS' ? 'En Cours' : 'Terminee' }}
                </span>
              </td>
              <td class="px-4 py-3">
                <button v-if="item.status === 'EN_COURS'" @click="markReturn(item)" class="inline-flex items-center gap-1 text-sm text-emerald-600 hover:text-emerald-800 font-medium">
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"/></svg>
                  Marquer Reprise
                </button>
              </td>
            </tr>
            <tr v-if="displayedItems.length === 0">
              <td colspan="6" class="px-4 py-12 text-center text-gray-400">Aucune absence trouvee</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Mark Absent Modal -->
    <div v-if="showModal === 'absent'" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50" @click.self="showModal = null">
      <div class="bg-white rounded-xl shadow-xl w-full max-w-md mx-4 p-6">
        <h3 class="text-lg font-bold text-gray-900 mb-4">Marquer Operateur Absent</h3>
        <div class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Operateur</label>
            <select v-model="absenceForm.operatorId" class="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm">
              <option :value="null">Selectionner...</option>
              <option v-for="op in activeOperators" :key="op.id" :value="op.id">{{ op.lastName }} {{ op.firstName }} ({{ op.employeeId }})</option>
            </select>
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Date Debut</label>
            <input v-model="absenceForm.startDate" type="date" class="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm">
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Retour Prevu (optionnel)</label>
            <input v-model="absenceForm.expectedReturnDate" type="date" class="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm">
          </div>
        </div>
        <div class="flex justify-end gap-3 mt-6">
          <button @click="showModal = null" class="px-4 py-2 text-sm text-gray-700 border border-gray-300 rounded-lg hover:bg-gray-50">Annuler</button>
          <button @click="submitAbsent" :disabled="loading" class="px-4 py-2 text-sm bg-orange-500 text-white rounded-lg hover:bg-orange-600 disabled:opacity-50">Confirmer</button>
        </div>
      </div>
    </div>

    <!-- Mark Departure Modal -->
    <div v-if="showModal === 'depart'" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50" @click.self="showModal = null">
      <div class="bg-white rounded-xl shadow-xl w-full max-w-md mx-4 p-6">
        <h3 class="text-lg font-bold text-gray-900 mb-4">Marquer Depart</h3>
        <div class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Operateur</label>
            <select v-model="departForm.operatorId" class="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm">
              <option :value="null">Selectionner...</option>
              <option v-for="op in activeOperators" :key="op.id" :value="op.id">{{ op.lastName }} {{ op.firstName }} ({{ op.employeeId }})</option>
            </select>
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Date de Depart</label>
            <input v-model="departForm.exitDate" type="date" class="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm">
          </div>
        </div>
        <div class="flex justify-end gap-3 mt-6">
          <button @click="showModal = null" class="px-4 py-2 text-sm text-gray-700 border border-gray-300 rounded-lg hover:bg-gray-50">Annuler</button>
          <button @click="submitDeparture" :disabled="loading" class="px-4 py-2 text-sm bg-red-600 text-white rounded-lg hover:bg-red-700 disabled:opacity-50">Confirmer le Depart</button>
        </div>
      </div>
    </div>

    <!-- Return Confirmation -->
    <div v-if="showModal === 'return'" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50" @click.self="showModal = null">
      <div class="bg-white rounded-xl shadow-xl w-full max-w-md mx-4 p-6">
        <h3 class="text-lg font-bold text-gray-900 mb-2">Confirmer la Reprise</h3>
        <p class="text-sm text-gray-500 mb-4">Reprise de {{ returnItem?.operatorName }}. Un recyclage sera automatiquement planifie.</p>
        <div class="mb-4">
          <label class="block text-sm font-medium text-gray-700 mb-1">Date de Reprise</label>
          <input v-model="returnDate" type="date" class="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm">
        </div>
        <div class="flex justify-end gap-3">
          <button @click="showModal = null" class="px-4 py-2 text-sm text-gray-700 border border-gray-300 rounded-lg hover:bg-gray-50">Annuler</button>
          <button @click="confirmReturn" :disabled="loading" class="px-4 py-2 text-sm bg-emerald-600 text-white rounded-lg hover:bg-emerald-700 disabled:opacity-50">Confirmer la Reprise</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { absenceApi } from '@/services/absenceApi'
import { operatorsApi } from '@/api/endpoints'

const allAbsences = ref([])
const activeOperators = ref([])
const loading = ref(false)
const activeTab = ref('active')
const showModal = ref(null)
const returnItem = ref(null)
const returnDate = ref(new Date().toISOString().split('T')[0])

const absenceForm = ref({ operatorId: null, startDate: '', expectedReturnDate: '' })
const departForm = ref({ operatorId: null, exitDate: '' })

const activeAbsences = computed(() => allAbsences.value.filter(a => a.status === 'EN_COURS'))
const displayedItems = computed(() => activeTab.value === 'active' ? activeAbsences.value : allAbsences.value)

async function loadData() {
 loading.value = true
  try {
    const [absRes, opRes] = await Promise.all([
      absenceApi.getAll(),
      operatorsApi.getActive(),
    ])
    allAbsences.value = absRes.data || []
    activeOperators.value = opRes.data || []
  } catch (e) {
    console.error('Error loading data:', e)
  } finally {
    loading.value = false
  }
}

function openModal(type) {
  showModal.value = type
  absenceForm.value = { operatorId: null, startDate: new Date().toISOString().split('T')[0], expectedReturnDate: '' }
  departForm.value = { operatorId: null, exitDate: new Date().toISOString().split('T')[0] }
}

async function submitAbsent() {
  if (!absenceForm.value.operatorId || !absenceForm.value.startDate) return
  loading.value = true
  try {
    await absenceApi.markAbsent(absenceForm.value)
    showModal.value = null
    await loadData()
  } catch (e) {
    console.error('Error:', e)
  } finally {
    loading.value = false
  }
}

async function submitDeparture() {
  if (!departForm.value.operatorId || !departForm.value.exitDate) return
  loading.value = true
  try {
    await absenceApi.markDeparture(departForm.value)
    showModal.value = null
    await loadData()
  } catch (e) {
    console.error('Error:', e)
  } finally {
    loading.value = false
  }
}

function markReturn(item) {
  returnItem.value = item
  returnDate.value = new Date().toISOString().split('T')[0]
  showModal.value = 'return'
}

async function confirmReturn() {
  loading.value = true
  try {
    await absenceApi.markReturn({ operatorId: returnItem.value.operatorId, returnDate: returnDate.value })
    showModal.value = null
    await loadData()
  } catch (e) {
    console.error('Error:', e)
  } finally {
    loading.value = false
  }
}

function formatDate(d) {
  if (!d) return '-'
  if (typeof d === 'string' && /^\d{2}\/\d{2}\/\d{4}$/.test(d)) {
    const [day, month, year] = d.split('/').map(Number)
    return new Date(year, month - 1, day).toLocaleDateString('fr-FR', { day: '2-digit', month: '2-digit', year: 'numeric' })
  }
  return new Date(d).toLocaleDateString('fr-FR', { day: '2-digit', month: '2-digit', year: 'numeric' })
}

onMounted(() => loadData())
</script>
