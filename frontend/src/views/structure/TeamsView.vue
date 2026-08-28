<template>
  <div class="space-y-6">
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">Équipes & Transferts</h1>
        <p class="text-gray-500 mt-1">
          Gérer les affectations d'opérateurs aux équipes et valider les demandes de transferts entre projets.
        </p>
      </div>
      <div class="flex gap-2">
        <button
          v-if="activeTab === 'teams' && canManageOrRequest"
          @click="openUpdateModal"
          class="inline-flex items-center gap-2 rounded-lg bg-emerald-600 px-4 py-2 text-sm font-medium text-white hover:bg-emerald-700 shadow-sm transition"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"/></svg>
          {{ isSupervisor ? 'Modifier l\'équipe (Superviseur)' : 'Proposer Mise à jour de l\'Équipe' }}
        </button>
        <button
          v-if="activeTab === 'transfers' && canManageOrRequest"
          @click="openTransferModal"
          class="inline-flex items-center gap-2 rounded-lg bg-emerald-600 px-4 py-2 text-sm font-medium text-white hover:bg-emerald-700 shadow-sm transition"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"/></svg>
          Demander un Transfert d'Opérateur
        </button>
      </div>
    </div>

    <!-- Navigation Tabs -->
    <div class="flex gap-2 border-b border-gray-200">
      <button
        @click="activeTab = 'teams'"
        class="px-4 py-2 text-sm font-medium border-b-2 transition"
        :class="activeTab === 'teams' ? 'border-emerald-600 text-emerald-600' : 'border-transparent text-gray-500 hover:text-gray-700'"
      >
        Équipes de Production
      </button>
      <button
        @click="activeTab = 'transfers'"
        class="px-4 py-2 text-sm font-medium border-b-2 transition"
        :class="activeTab === 'transfers' ? 'border-emerald-600 text-emerald-600' : 'border-transparent text-gray-500 hover:text-gray-700'"
      >
        Demandes de Transferts de Projets
      </button>
    </div>

    <!-- Alert / Banner message -->
    <div v-if="bannerMsg" class="rounded-lg p-4 text-sm font-medium border transition" :class="bannerSuccess ? 'bg-emerald-50 text-emerald-800 border-emerald-200' : 'bg-amber-50 text-amber-800 border-amber-200'">
      {{ bannerMsg }}
    </div>

    <!-- Tab 1: Équipes de Production -->
    <div v-if="activeTab === 'teams'" class="space-y-6">
      <!-- PENDING APPROVAL REQUESTS SECTION -->
      <div v-if="isSupervisor && pendingRequests.length > 0" class="rounded-xl border border-amber-200 bg-amber-50/60 p-5 shadow-sm space-y-3">
        <div class="flex items-center justify-between border-b border-amber-200/80 pb-3">
          <h2 class="font-bold text-amber-900 flex items-center gap-2">
            <svg class="w-5 h-5 text-amber-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"/></svg>
            Demandes de mise à jour d'équipe en attente ({{ pendingRequests.length }})
          </h2>
          <span class="text-xs bg-amber-200 text-amber-900 px-2.5 py-0.5 rounded-full font-semibold">Validation Superviseur requise</span>
        </div>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div v-for="req in pendingRequests" :key="req.id" class="bg-white rounded-lg p-4 border border-amber-200 shadow-xs space-y-2">
            <div class="flex items-center justify-between">
              <span class="font-bold text-gray-900">{{ req.teamName || 'Équipe #' + req.teamId }}</span>
              <span class="text-xs text-gray-400">Demandé par: {{ req.requestedBy }}</span>
            </div>
            <p class="text-xs text-gray-500">
              Nouveaux membres proposées (Opérateur IDs): <span class="font-mono text-gray-700">{{ req.operatorIds || 'Aucun' }}</span>
            </p>
            <div class="flex items-center justify-end gap-2 pt-2 border-t border-gray-100">
              <button @click="rejectRequest(req.id)" class="px-3 py-1 text-xs font-medium text-red-600 hover:bg-red-50 rounded border border-red-200">
                Refuser
              </button>
              <button @click="approveRequest(req.id)" class="px-3 py-1 text-xs font-semibold text-white bg-emerald-600 hover:bg-emerald-700 rounded shadow-xs">
                ✅ Valider (Superviseur)
              </button>
            </div>
          </div>
        </div>
      </div>

      <div v-if="loading" class="flex items-center justify-center py-20">
        <div class="w-8 h-8 border-4 border-emerald-200 border-t-emerald-600 rounded-full animate-spin"></div>
      </div>

      <div v-else-if="groups.length === 0" class="bg-white rounded-xl shadow-sm border border-gray-200 p-12 text-center text-gray-400">
        Aucun opérateur affecté pour le moment.
      </div>

      <!-- Teams Grid -->
      <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        <div v-for="group in groups" :key="group.projectId || '_none'" class="bg-white rounded-xl shadow-sm border border-gray-200 p-5 flex flex-col justify-between">
          <div>
            <div class="flex items-center justify-between mb-3">
              <h3 class="font-semibold text-gray-900 flex items-center gap-2">
                <svg v-if="group.projectId" class="w-4 h-4 text-emerald-600 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 7v10a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-6l-2-2H5a2 2 0 00-2 2z"></path></svg>
                {{ group.projectName }}
              </h3>
              <span class="text-xs text-gray-500 bg-gray-50 px-2 py-0.5 rounded-full border">{{ group.operators.length }} opérateur(s)</span>
            </div>
            <ul class="space-y-1.5">
              <li v-for="op in group.operators" :key="op.id" class="flex items-center justify-between py-1.5 px-2 rounded bg-gray-50 text-sm">
                <router-link :to="'/operators/' + op.id" class="text-emerald-600 hover:underline font-medium">
                  — {{ op.lastName }} {{ op.firstName }}
                </router-link>
                <span class="inline-flex items-center px-1.5 py-0.5 rounded text-xs" :class="op.active !== false ? 'bg-emerald-100 text-emerald-700' : 'bg-red-100 text-red-700'">
                  {{ op.active !== false ? 'Actif' : 'Inactif' }}
                </span>
              </li>
            </ul>
          </div>
        </div>
      </div>
    </div>

    <!-- Tab 2: Project Transfer Requests -->
    <div v-if="activeTab === 'transfers'" class="space-y-6">
      <!-- PENDING TRANSFER REQUESTS FOR SUPERVISOR -->
      <div v-if="isSupervisor" class="rounded-xl border border-gray-200 bg-white p-5 shadow-sm space-y-4">
        <h2 class="font-bold text-gray-900 text-lg flex items-center gap-2 border-b border-gray-100 pb-3">
          <svg class="w-5 h-5 text-emerald-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"/></svg>
          Demandes de transfert d'opérateurs en attente ({{ pendingTransfers.length }})
        </h2>

        <div v-if="pendingTransfers.length === 0" class="text-center py-10 text-gray-400 text-sm italic">
          Aucune demande de transfert de projet en attente.
        </div>

        <div v-else class="overflow-x-auto">
          <table class="w-full text-left text-sm text-gray-500 border-collapse">
            <thead class="bg-gray-50 text-xs text-gray-700 uppercase font-semibold border-b border-gray-200">
              <tr>
                <th class="px-4 py-3">Opérateur</th>
                <th class="px-4 py-3">Projet Source</th>
                <th class="px-4 py-3">Projet Cible</th>
                <th class="px-4 py-3">Demandé Par</th>
                <th class="px-4 py-3">Date</th>
                <th class="px-4 py-3 text-right">Actions</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-gray-100 bg-white">
              <tr v-for="req in pendingTransfers" :key="req.id" class="hover:bg-gray-50">
                <td class="px-4 py-3 font-semibold text-gray-900">
                  {{ req.operatorName }}
                  <span class="text-xs text-gray-400 block font-mono">{{ req.employeeId }}</span>
                </td>
                <td class="px-4 py-3">{{ req.sourceProjectName || 'Sans Projet' }}</td>
                <td class="px-4 py-3 font-medium text-emerald-700">{{ req.targetProjectName }}</td>
                <td class="px-4 py-3 text-xs text-gray-600">{{ req.requestedBy }}</td>
                <td class="px-4 py-3 text-xs text-gray-400">{{ formatDate(req.createdAt) }}</td>
                <td class="px-4 py-3 text-right space-x-2">
                  <button @click="rejectTransfer(req.id)" class="px-2.5 py-1 text-xs font-medium text-red-600 bg-red-50 hover:bg-red-100 rounded border border-red-200 transition">
                    Refuser
                  </button>
                  <button @click="approveTransfer(req.id)" class="px-2.5 py-1 text-xs font-semibold text-white bg-emerald-600 hover:bg-emerald-700 rounded shadow-xs transition">
                    ✅ Valider
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- CHEF D'EQUIPE / USER VIEW OF TRANSFERS -->
      <div class="rounded-xl border border-gray-200 bg-white p-5 shadow-sm space-y-4">
        <div class="flex items-center justify-between border-b border-gray-100 pb-3">
          <h2 class="font-bold text-gray-900 text-lg">Suivi des demandes de transferts</h2>
          <button
            v-if="canManageOrRequest"
            @click="openTransferModal"
            class="bg-emerald-600 text-white px-3 py-1.5 rounded-lg hover:bg-emerald-700 text-xs font-medium shadow-xs"
          >
            + Demander transfert
          </button>
        </div>

        <div v-if="allTransfersList.length === 0" class="text-center py-12 text-gray-400 text-sm italic">
          Aucun transfert demandé ou enregistré.
        </div>

        <div v-else class="overflow-x-auto">
          <table class="w-full text-left text-sm text-gray-500 border-collapse">
            <thead class="bg-gray-50 text-xs text-gray-700 uppercase font-semibold border-b border-gray-200">
              <tr>
                <th class="px-4 py-3">Opérateur</th>
                <th class="px-4 py-3">Source -> Cible</th>
                <th class="px-4 py-3">Date de Demande</th>
                <th class="px-4 py-3">Statut</th>
                <th class="px-4 py-3">Traité Par</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-gray-100 bg-white">
              <tr v-for="req in allTransfersList" :key="req.id" class="hover:bg-gray-50">
                <td class="px-4 py-3">
                  <span class="font-semibold text-gray-900 block">{{ req.operatorName }}</span>
                  <span class="text-xs text-gray-400 block font-mono">{{ req.employeeId }}</span>
                </td>
                <td class="px-4 py-3 text-xs">
                  <span class="text-gray-400">{{ req.sourceProjectName || 'Sans Projet' }}</span>
                  <span class="text-gray-500 mx-1.5">➔</span>
                  <span class="font-semibold text-emerald-700">{{ req.targetProjectName }}</span>
                </td>
                <td class="px-4 py-3 text-xs text-gray-400">{{ formatDate(req.createdAt) }}</td>
                <td class="px-4 py-3">
                  <span
                    class="px-2 py-0.5 rounded-full text-xs font-semibold"
                    :class="{
                      'bg-amber-100 text-amber-800': req.status === 'PENDING',
                      'bg-emerald-100 text-emerald-800': req.status === 'APPROVED',
                      'bg-red-100 text-red-800': req.status === 'REJECTED'
                    }"
                  >
                    {{ req.status === 'PENDING' ? 'En attente' : req.status === 'APPROVED' ? 'Accepté' : 'Refusé' }}
                  </span>
                </td>
                <td class="px-4 py-3 text-xs text-gray-500">
                  <span v-if="req.approvedBy">{{ req.approvedBy }} <span class="text-gray-400">({{ formatDate(req.approvedAt) }})</span></span>
                  <span v-else class="text-gray-400">—</span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- UPDATE TEAM MEMBERS MODAL (EXISTING) -->
    <div v-if="showModal" class="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 backdrop-blur-xs p-4">
      <div class="bg-white rounded-xl shadow-xl border border-gray-200 max-w-lg w-full p-6 space-y-4">
        <div class="flex items-center justify-between border-b border-gray-100 pb-3">
          <h3 class="font-bold text-gray-900 text-lg">
            {{ isSupervisor ? 'Mise à jour directe (Superviseur)' : 'Proposer une mise à jour d\'équipe' }}
          </h3>
          <button @click="showModal = false" class="text-gray-400 hover:text-gray-600">✕</button>
        </div>

        <div v-if="!isSupervisor" class="p-3 bg-blue-50 border border-blue-200 rounded-lg text-xs text-blue-800">
          ℹ️ Conformément aux règles OPmobility, toute modification d'équipe par un Chef d'équipe est transmise au <strong>Superviseur de la Production</strong> pour validation avant application.
        </div>

        <div class="space-y-3">
          <div>
            <label class="block text-xs font-semibold text-gray-700 mb-1">Sélectionner l'équipe:</label>
            <select v-model="selectedTeamId" class="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm outline-none focus:ring-2 focus:ring-emerald-500">
              <option value="">-- Choisir une équipe --</option>
              <option v-for="t in teamsList" :key="t.id" :value="t.id">{{ t.name }} (Actuel: {{ t.operators?.length || 0 }} membres)</option>
            </select>
          </div>

          <div>
            <label class="block text-xs font-semibold text-gray-700 mb-1">Écrire la matricule ou le nom de l'opérateur :</label>
            <div class="flex gap-2">
              <input
                v-model="operatorInputText"
                @keyup.enter="addOperatorByText"
                type="text"
                placeholder="Saisir matricule ou nom (ex: OP001)..."
                class="flex-1 px-3 py-2 border border-gray-200 rounded-lg text-sm outline-none focus:ring-2 focus:ring-emerald-500"
              />
              <button
                type="button"
                @click="addOperatorByText"
                class="px-3 py-2 bg-emerald-600 hover:bg-emerald-700 text-white rounded-lg text-xs font-medium"
              >
                + Ajouter
              </button>
            </div>

            <!-- Auto-suggestions when typing -->
            <div v-if="matchingSuggestions.length > 0" class="mt-1 max-h-32 overflow-y-auto border border-gray-200 rounded-lg bg-white shadow-md divide-y divide-gray-100 z-10 relative">
              <div
                v-for="op in matchingSuggestions"
                :key="op.id"
                @click="selectOperatorFromSuggestion(op)"
                class="px-3 py-1.5 hover:bg-emerald-50 cursor-pointer flex items-center justify-between text-xs"
              >
                <span class="font-medium text-gray-900">{{ op.lastName }} {{ op.firstName }}</span>
                <span class="text-gray-400 font-mono text-[10px]">{{ op.employeeId }}</span>
              </div>
            </div>

            <!-- List of selected operators -->
            <div class="mt-3">
              <label class="block text-[11px] font-semibold text-gray-500 mb-1">Membres actuels dans la liste ({{ selectedOperatorsList.length }}) :</label>
              <div class="flex flex-wrap gap-1.5 max-h-36 overflow-y-auto p-2 border border-gray-200 rounded-lg bg-gray-50">
                <span
                  v-for="op in selectedOperatorsList"
                  :key="op.id"
                  class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-md bg-white border border-gray-200 text-xs font-medium text-gray-800 shadow-xs"
                >
                  <span>{{ op.lastName }} {{ op.firstName }}</span>
                  <span class="text-gray-400 font-mono text-[10px]">({{ op.employeeId }})</span>
                  <button @click="removeOperatorFromSelected(op.id)" class="text-gray-400 hover:text-red-600 font-bold ml-1 text-xs">×</button>
                </span>
                <span v-if="selectedOperatorsList.length === 0" class="text-xs text-gray-400 italic py-1">
                  Aucun opérateur dans l'équipe. Saisissez une matricule ou un nom ci-dessus.
                </span>
              </div>
            </div>
          </div>
        </div>

        <div class="flex items-center justify-end gap-2 pt-3 border-t border-gray-100">
          <button @click="showModal = false" class="px-4 py-2 text-xs font-medium text-gray-600 hover:bg-gray-100 rounded-lg">Annuler</button>
          <button @click="submitTeamUpdate" :disabled="!selectedTeamId" class="px-4 py-2 text-xs font-semibold text-white bg-emerald-600 hover:bg-emerald-700 rounded-lg shadow-sm disabled:opacity-50">
            {{ isSupervisor ? 'Appliquer immédiatement' : 'Soumettre au Superviseur' }}
          </button>
        </div>
      </div>
    </div>

    <!-- NEW OPERATOR TRANSFER REQUEST MODAL -->
    <div v-if="showTransferModal" class="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 backdrop-blur-xs p-4">
      <div class="bg-white rounded-xl shadow-xl border border-gray-200 max-w-lg w-full p-6 space-y-4">
        <div class="flex items-center justify-between border-b border-gray-100 pb-3">
          <h3 class="font-bold text-gray-900 text-lg">Demande de Transfert de Projet</h3>
          <button @click="showTransferModal = false" class="text-gray-400 hover:text-gray-600">✕</button>
        </div>

        <div class="space-y-4">
          <!-- Operator Lookup -->
          <div>
            <label class="block text-xs font-semibold text-gray-700 mb-1">Rechercher l'opérateur par matricule ou nom :</label>
            <div class="flex gap-2">
              <input
                v-model="transferSearchText"
                type="text"
                placeholder="Saisir matricule ou nom..."
                class="flex-1 px-3 py-2 border border-gray-200 rounded-lg text-sm outline-none focus:ring-2 focus:ring-emerald-500"
              />
            </div>
            <!-- Auto-suggestions for transfer -->
            <div v-if="transferSuggestions.length > 0" class="mt-1 max-h-32 overflow-y-auto border border-gray-200 rounded-lg bg-white shadow-md divide-y divide-gray-100 z-10 relative">
              <div
                v-for="op in transferSuggestions"
                :key="op.id"
                @click="selectOperatorForTransfer(op)"
                class="px-3 py-1.5 hover:bg-emerald-50 cursor-pointer flex items-center justify-between text-xs"
              >
                <span class="font-medium text-gray-900">{{ op.lastName }} {{ op.firstName }}</span>
                <span class="text-gray-400 font-mono text-[10px] ml-2">
                  {{ op.employeeId }} {{ op.project ? '(' + op.project.name + ')' : '(Sans projet)' }}
                </span>
              </div>
            </div>
          </div>

          <!-- Target Project selection -->
          <div>
            <label class="block text-xs font-semibold text-gray-700 mb-1">Projet Cible :</label>
            <select v-model="transferTargetProjectId" class="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm outline-none focus:ring-2 focus:ring-emerald-500">
              <option value="" disabled>-- Choisir le projet cible --</option>
              <option v-for="p in projects" :key="p.id" :value="p.id">{{ p.name }}</option>
            </select>
          </div>

          <!-- Display Selected Operator info -->
          <div v-if="selectedTransferOp" class="p-3 bg-slate-50 border border-gray-200 rounded-lg text-xs space-y-1">
            <p class="font-bold text-gray-700">Opérateur Sélectionné :</p>
            <p><strong>Nom:</strong> {{ selectedTransferOp.lastName }} {{ selectedTransferOp.firstName }} ({{ selectedTransferOp.employeeId }})</p>
            <p><strong>Projet Actuel:</strong> {{ selectedTransferOp.project ? selectedTransferOp.project.name : 'Aucun' }}</p>
          </div>
        </div>

        <div class="flex items-center justify-end gap-2 pt-3 border-t border-gray-100">
          <button @click="showTransferModal = false" class="px-4 py-2 text-xs font-medium text-gray-600 hover:bg-gray-100 rounded-lg">Annuler</button>
          <button
            @click="submitTransferRequest"
            :disabled="!selectedTransferOp || !transferTargetProjectId"
            class="px-4 py-2 text-xs font-semibold text-white bg-emerald-600 hover:bg-emerald-700 rounded-lg shadow-sm disabled:opacity-50"
          >
            {{ isSupervisor ? 'Transférer immédiatement' : 'Soumettre la Demande' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { operatorsApi, structureApi, teamsApi, projectTransferApi } from '@/api/endpoints'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const loading = ref(true)
const activeTab = ref('teams')

const operators = ref([])
const allOperators = ref([])
const projects = ref([])
const teamsList = ref([])
const pendingRequests = ref([])
const pendingTransfers = ref([])
const allTransfersList = ref([])

// Form states for team update modal
const showModal = ref(false)
const selectedTeamId = ref('')
const selectedOpIds = ref([])
const operatorInputText = ref('')

// Form states for project transfer request modal
const showTransferModal = ref(false)
const transferSearchText = ref('')
const selectedTransferOp = ref(null)
const transferTargetProjectId = ref('')

const bannerMsg = ref('')
const bannerSuccess = ref(true)

const isSupervisor = computed(() => auth.hasAnyRole(['SUPERVISEUR', 'ADMIN', 'RH']))
const canManageOrRequest = computed(() => auth.hasAnyRole(['CHEF_EQUIPE', 'SUPERVISEUR', 'ADMIN']))

// Group operators by Project
const groups = computed(() => {
  const map = {}
  operators.value.forEach(op => {
    const proj = op.project
    const projId = proj ? proj.id : '_none'
    const projName = proj ? proj.name : 'Sans Projet'
    if (!map[projId]) {
      map[projId] = { projectId: projId === '_none' ? null : projId, projectName: projName, operators: [] }
    }
    map[projId].operators.push(op)
  })
  return Object.values(map)
})

// Team updates selections
const selectedOperatorsList = computed(() => {
  const set = new Set(selectedOpIds.value)
  return allOperators.value.filter(op => set.has(op.id))
})

const matchingSuggestions = computed(() => {
  const q = operatorInputText.value.trim().toLowerCase()
  if (!q || q.length < 1) return []
  return allOperators.value.filter(op =>
    !selectedOpIds.value.includes(op.id) &&
    ((op.employeeId && op.employeeId.toLowerCase().includes(q)) ||
     (op.lastName && op.lastName.toLowerCase().includes(q)) ||
     (op.firstName && op.firstName.toLowerCase().includes(q)) ||
     `${op.lastName} ${op.firstName}`.toLowerCase().includes(q))
  )
})

// Transfer suggestions lookup
const transferSuggestions = computed(() => {
  const q = transferSearchText.value.trim().toLowerCase()
  if (!q || q.length < 1) return []
  return allOperators.value.filter(op =>
    (op.employeeId && op.employeeId.toLowerCase().includes(q)) ||
    (op.lastName && op.lastName.toLowerCase().includes(q)) ||
    (op.firstName && op.firstName.toLowerCase().includes(q)) ||
    `${op.lastName} ${op.firstName}`.toLowerCase().includes(q)
  )
})

// Add operator from suggestion inside update team modal
const selectOperatorFromSuggestion = (op) => {
  if (!selectedOpIds.value.includes(op.id)) {
    selectedOpIds.value.push(op.id)
  }
  operatorInputText.value = ''
}

const addOperatorByText = () => {
  const q = operatorInputText.value.trim().toLowerCase()
  if (!q) return
  const found = allOperators.value.find(op =>
    (op.employeeId && op.employeeId.toLowerCase() === q) ||
    (op.lastName && op.lastName.toLowerCase() === q) ||
    `${op.lastName} ${op.firstName}`.toLowerCase() === q ||
    (op.employeeId && op.employeeId.toLowerCase().includes(q))
  )
  if (found && !selectedOpIds.value.includes(found.id)) {
    selectedOpIds.value.push(found.id)
    operatorInputText.value = ''
  }
}

const removeOperatorFromSelected = (id) => {
  selectedOpIds.value = selectedOpIds.value.filter(opId => opId !== id)
}

// Select operator for transfer
const selectOperatorForTransfer = (op) => {
  selectedTransferOp.value = op
  transferSearchText.value = `${op.lastName} ${op.firstName} (${op.employeeId})`
}

watch(selectedTeamId, (newId) => {
  if (!newId) {
    selectedOpIds.value = []
    return
  }
  const team = teamsList.value.find(t => t.id === Number(newId))
  if (team && team.operators) {
    selectedOpIds.value = team.operators.map(o => o.id)
  } else {
    selectedOpIds.value = []
  }
})

const openUpdateModal = () => {
  selectedTeamId.value = teamsList.value[0]?.id || ''
  operatorInputText.value = ''
  if (teamsList.value[0] && teamsList.value[0].operators) {
    selectedOpIds.value = teamsList.value[0].operators.map(o => o.id)
  } else {
    selectedOpIds.value = []
  }
  showModal.value = true
}

const openTransferModal = () => {
  transferSearchText.value = ''
  selectedTransferOp.value = null
  // Try to default target project to the first project of chef
  const empId = auth.user?.employeeId
  const myProj = projects.value.find(p => p.members?.some(m => m.employeeId === empId))
  transferTargetProjectId.value = myProj ? myProj.id : ''
  showTransferModal.value = true
}

const fetchData = async () => {
  loading.value = true
  try {
    const [opsRes, projRes, teamsRes, reqsRes, transRes] = await Promise.all([
      operatorsApi.getAll(),
      structureApi.getAll(),
      teamsApi.getAll(),
      teamsApi.getPendingRequests(),
      projectTransferApi.getPendingRequests()
    ])
    operators.value = opsRes.data || []
    allOperators.value = opsRes.data || []
    projects.value = projRes.data || []
    teamsList.value = teamsRes.data || []
    pendingRequests.value = reqsRes.data || []
    pendingTransfers.value = transRes.data || []
    
    // For full tracking, let's load transfers list
    allTransfersList.value = transRes.data || []
  } catch (e) {
    console.error("Error fetching data in TeamsView:", e)
  } finally {
    loading.value = false
  }
}

const submitTeamUpdate = async () => {
  if (!selectedTeamId.value) return
  try {
    const res = await teamsApi.requestUpdate(selectedTeamId.value, selectedOpIds.value)
    bannerMsg.value = res.data?.message || 'Mise à jour traitée'
    bannerSuccess.value = res.data?.status === 'APPROVED'
    showModal.value = false
    await fetchData()
  } catch (e) {
    bannerMsg.value = e.response?.data?.message || 'Erreur lors de la mise à jour'
    bannerSuccess.value = false
  }
}

const approveRequest = async (requestId) => {
  try {
    const res = await teamsApi.approveRequest(requestId)
    bannerMsg.value = res.data?.message || 'Demande validée'
    bannerSuccess.value = true
    await fetchData()
  } catch (e) {
    console.error(e)
  }
}

const rejectRequest = async (requestId) => {
  try {
    const res = await teamsApi.rejectRequest(requestId)
    bannerMsg.value = res.data?.message || 'Demande refusée'
    bannerSuccess.value = false
    await fetchData()
  } catch (e) {
    console.error(e)
  }
}

// Transfer Actions
const submitTransferRequest = async () => {
  if (!selectedTransferOp.value || !transferTargetProjectId.value) return
  try {
    const res = await projectTransferApi.requestTransfer(
      selectedTransferOp.value.employeeId,
      transferTargetProjectId.value
    )
    bannerMsg.value = res.data?.message || 'Demande de transfert traitée'
    bannerSuccess.value = res.data?.status === 'APPROVED'
    showTransferModal.value = false
    await fetchData()
  } catch (e) {
    bannerMsg.value = e.response?.data?.message || 'Erreur lors de la demande de transfert'
    bannerSuccess.value = false
  }
}

const approveTransfer = async (requestId) => {
  try {
    const res = await projectTransferApi.approveRequest(requestId)
    bannerMsg.value = res.data?.message || 'Transfert validé avec succès'
    bannerSuccess.value = true
    await fetchData()
  } catch (e) {
    console.error(e)
  }
}

const rejectTransfer = async (requestId) => {
  try {
    const res = await projectTransferApi.rejectRequest(requestId)
    bannerMsg.value = res.data?.message || 'Transfert refusé'
    bannerSuccess.value = false
    await fetchData()
  } catch (e) {
    console.error(e)
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('fr-FR', {
    day: '2-digit', month: '2-digit', year: 'numeric',
    hour: '2-digit', minute: '2-digit'
  })
}

onMounted(fetchData)
</script>
