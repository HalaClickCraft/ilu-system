<template>
  <div class="min-h-screen bg-gray-50 p-6">
    <!-- Header -->
    <div class="flex items-center justify-between mb-6">
      <div>
        <h1 class="text-2xl font-bold text-gray-800">Onboarding - Formation Théorique</h1>
        <p class="text-gray-500 mt-1">Suivi des modules de formation théorique par département</p>
      </div>
      <div v-if="userRoles.length" class="text-sm text-gray-500">
        <span class="font-medium text-gray-700">{{ userRoles.join(', ') }}</span>
      </div>
    </div>

    <!-- ===== TAB SWITCHER ===== -->
    <div class="flex gap-2 mb-6">
      <button
        @click="activeTab = 'matrix'"
        class="px-5 py-2.5 rounded-lg text-sm font-semibold transition-colors"
        :class="activeTab === 'matrix'
          ? 'bg-blue-600 text-white shadow-md'
          : 'bg-white text-gray-600 hover:bg-gray-100 border border-gray-200'"
      >
        Tableau des Opérateurs
      </button>
      <button
        @click="activeTab = 'detail'"
        class="px-5 py-2.5 rounded-lg text-sm font-semibold transition-colors"
        :class="activeTab === 'detail'
          ? 'bg-blue-600 text-white shadow-md'
          : 'bg-white text-gray-600 hover:bg-gray-100 border border-gray-200'"
      >
        Détail Opérateur
      </button>
      <button
        @click="activeTab = 'history'"
        class="px-5 py-2.5 rounded-lg text-sm font-semibold transition-colors relative"
        :class="activeTab === 'history'
          ? 'bg-blue-600 text-white shadow-md'
          : 'bg-white text-gray-600 hover:bg-gray-100 border border-gray-200'"
      >
        Historique
        <span
          v-if="scopedCompletedOperators.length > 0"
          class="ml-1.5 text-xs px-1.5 py-0.5 rounded-full bg-green-100 text-green-700"
        >
          {{ scopedCompletedOperators.length }}
        </span>
      </button>
    </div>

    <!-- ================================================== -->
    <!-- TAB 1: MATRIX TABLE                                 -->
    <!-- ================================================== -->
    <div v-if="activeTab === 'matrix'">
      <!-- Search + Refresh -->
      <div class="bg-white rounded-xl shadow-sm p-4 mb-4 flex items-center gap-4">
        <input
          v-model="searchQuery"
          type="text"
          placeholder="Rechercher par nom ou matricule..."
          class="flex-1 max-w-sm border border-gray-300 rounded-lg px-4 py-2 text-sm focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
        />
        <button
          @click="loadMatrixData"
          :disabled="loadingMatrix"
          class="px-4 py-2 text-sm font-medium bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 transition-colors"
        >
          {{ loadingMatrix ? 'Chargement...' : 'Actualiser' }}
        </button>
      </div>

      <!-- Section 1: In Progress -->
      <div class="mb-8">
        <h2 class="text-md font-bold text-gray-800 mb-3 flex items-center gap-2">
          <span class="w-2.5 h-2.5 bg-orange-500 rounded-full"></span>
          Opérateurs en cours d'onboarding ({{ pendingOperators.length }})
        </h2>
        <div class="bg-white rounded-xl shadow-sm overflow-x-auto">
          <table class="w-full text-sm">
            <thead>
              <tr class="bg-gray-50 border-b border-gray-200">
                <th class="text-left px-4 py-3 font-semibold text-gray-700 sticky left-0 bg-gray-50 z-10 min-w-[220px]">Opérateur</th>
                <th
                  v-for="dept in departmentNames"
                  :key="dept"
                  class="text-center px-3 py-3 font-semibold text-gray-700 min-w-[120px]"
                >
                  {{ dept }}
                </th>
                <th class="text-center px-3 py-3 font-semibold text-gray-700 min-w-[90px]">Total</th>
                <th class="text-center px-3 py-3 font-semibold text-gray-700 min-w-[110px]">Statut</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-gray-100">
              <tr
                v-for="op in paginatedPendingOperators"
                :key="op.operatorId"
                class="hover:bg-blue-50/50 cursor-pointer transition-colors"
                @click="goToDetail(op.operatorId)"
              >
                <td class="px-4 py-3 sticky left-0 bg-white z-10">
                  <div class="font-medium text-gray-800">{{ op.firstName }} {{ op.lastName }}</div>
                  <div class="text-xs text-gray-400">{{ op.matricule }}</div>
                </td>
                <td
                  v-for="dept in departmentNames"
                  :key="dept"
                  class="text-center px-3 py-3"
                >
                  <div v-if="op.departmentProgress && op.departmentProgress[dept]">
                    <span
                      class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-medium"
                      :class="op.departmentProgress[dept].completed > 0
                          ? 'bg-yellow-100 text-yellow-700'
                          : 'bg-gray-100 text-gray-500'"
                    >
                      {{ op.departmentProgress[dept].completed }}/{{ op.departmentProgress[dept].total }}
                    </span>
                  </div>
                  <span v-else class="text-gray-300">—</span>
                </td>
                <td class="text-center px-3 py-3">
                  <span class="text-sm font-semibold text-gray-700">
                    {{ op.completionPercentage }}%
                  </span>
                </td>
                <td class="text-center px-3 py-3">
                  <span
                    class="inline-flex items-center px-2.5 py-1 rounded-full text-xs font-medium bg-orange-100 text-orange-700"
                  >
                    En cours
                  </span>
                </td>
              </tr>
              <tr v-if="pendingOperators.length === 0">
                <td colspan="999" class="text-center py-12 text-gray-400">
                  Aucun opérateur en cours d'onboarding
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Matrix Pagination Footer -->
        <div v-if="matrixTotalPages > 1" class="px-6 py-3 bg-gray-50 border-t flex justify-between items-center text-xs text-gray-500 font-medium">
          <span>Affichage de {{ (matrixCurrentPage - 1) * matrixPageSize + 1 }} à {{ Math.min(matrixCurrentPage * matrixPageSize, pendingOperators.length) }} sur {{ pendingOperators.length }} opérateur(s)</span>
          <div class="flex gap-1">
            <button :disabled="matrixCurrentPage === 1" @click="matrixCurrentPage--" class="px-2 py-1 bg-white border border-gray-200 rounded hover:bg-gray-50 disabled:opacity-50 font-bold">Précédent</button>
            <span class="px-3.5 py-1 bg-gray-100 rounded flex items-center font-bold">Page {{ matrixCurrentPage }} / {{ matrixTotalPages }}</span>
            <button :disabled="matrixCurrentPage === matrixTotalPages" @click="matrixCurrentPage++" class="px-2 py-1 bg-white border border-gray-200 rounded hover:bg-gray-50 disabled:opacity-50 font-bold">Suivant</button>
          </div>
        </div>
      </div>

    </div>

    <!-- ================================================== -->
    <!-- TAB 2: DETAIL OPERATOR                             -->
    <!-- ================================================== -->
    <div v-if="activeTab === 'detail'">
      <!-- Operator Selection -->
      <div class="bg-white rounded-xl shadow-sm p-4 mb-6">
        <label class="block text-sm font-medium text-gray-700 mb-2">Sélectionner un Opérateur</label>
        <div class="flex flex-col sm:flex-row gap-3 max-w-2xl">
          <input
            v-model="detailSearchQuery"
            type="text"
            placeholder="Rechercher par nom ou matricule..."
            class="flex-1 border border-gray-300 rounded-lg px-4 py-2 text-sm focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none"
          />
          <select
            v-model="selectedOperatorId"
            @change="loadOperatorStatus"
            class="flex-1 border border-gray-300 rounded-lg px-4 py-2 text-sm focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
          >
            <option value="">-- Choisir un opérateur ({{ filteredDetailOperators.length }} trouvé(s)) --</option>
            <option v-for="op in filteredDetailOperators" :key="op.id" :value="op.id">
              {{ op.lastName }} {{ op.firstName }} — {{ op.employeeId }}
            </option>
          </select>
        </div>
      </div>

      <!-- Progress Card -->
      <div v-if="selectedOperatorId && progress" class="bg-white rounded-xl shadow-sm p-6 mb-6">
        <div class="flex items-center justify-between mb-4">
          <h2 class="text-lg font-semibold text-gray-800">Progression Globale</h2>
          <span
            v-if="progress.onboardingComplete"
            class="inline-flex items-center gap-1.5 px-4 py-1.5 rounded-full text-sm font-semibold bg-green-100 text-green-700"
          >
            &#10003; Onboarding Terminé — Prêt pour formation pratique
          </span>
          <span
            v-else
            class="inline-flex items-center px-4 py-1.5 rounded-full text-sm font-medium bg-orange-100 text-orange-700"
          >
            En cours
          </span>
        </div>

        <div class="grid grid-cols-2 md:grid-cols-4 gap-4 mb-5">
          <div class="bg-blue-50 rounded-lg p-4 text-center">
            <div class="text-3xl font-bold text-blue-600">{{ progress.completedModules }}</div>
            <div class="text-sm text-blue-700">Complétés</div>
          </div>
          <div class="bg-gray-50 rounded-lg p-4 text-center">
            <div class="text-3xl font-bold text-gray-600">{{ progress.totalModules }}</div>
            <div class="text-sm text-gray-700">Total</div>
          </div>
          <div class="bg-orange-50 rounded-lg p-4 text-center">
            <div class="text-3xl font-bold text-orange-600">{{ progress.remainingModules }}</div>
            <div class="text-sm text-orange-700">Restants</div>
          </div>
          <div class="rounded-lg p-4 text-center" :class="progress.completionPercentage === 100 ? 'bg-green-50' : 'bg-gray-50'">
            <div class="text-3xl font-bold" :class="progress.completionPercentage === 100 ? 'text-green-600' : 'text-gray-600'">
              {{ progress.completionPercentage }}%
            </div>
            <div class="text-sm text-gray-700">Progression</div>
          </div>
        </div>


        <!-- Ready for formation link -->
        <div v-if="progress.onboardingComplete" class="mt-5 p-3 bg-green-50 border border-green-200 rounded-lg">
          <p class="text-sm text-green-700">
            Cet opérateur a terminé tous les modules théoriques. Le chef d'équipe peut maintenant
            <router-link :to="'/training?operatorId=' + selectedOperatorId" class="underline font-semibold hover:text-green-900">
              lancer la formation pratique (suivi 12 jours)
            </router-link>.
          </p>
        </div>
      </div>

      <!-- Department Tabs -->
      <div v-if="selectedOperatorId && departments.length > 0">
        <div class="flex flex-wrap gap-2 mb-4">
          <button
            v-for="dept in departments"
            :key="dept.department"
            @click="activeDepartment = dept.department"
            class="px-4 py-2 rounded-lg text-sm font-medium transition-colors"
            :class="activeDepartment === dept.department
              ? 'bg-blue-600 text-white shadow-md'
              : 'bg-white text-gray-600 hover:bg-gray-100 border border-gray-200'"
          >
            {{ dept.department }}
            <span
              class="ml-1.5 text-xs px-1.5 py-0.5 rounded-full"
              :class="dept.completedModules === dept.totalModules ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-600'"
            >
              {{ dept.completedModules }}/{{ dept.totalModules }}
            </span>
          </button>
        </div>

        <!-- Module List -->
        <div class="bg-white rounded-xl shadow-sm overflow-hidden">
          <div
            class="px-6 py-4 border-b border-gray-200"
            :class="activeDeptData.editable ? 'bg-gray-50' : 'bg-yellow-50'"
          >
            <h3 class="text-lg font-semibold text-gray-800">
              {{ activeDepartment }}
              <span class="text-sm font-normal text-gray-500">
                — {{ activeDeptData.completedModules }}/{{ activeDeptData.totalModules }} ({{ activeDeptData.completionPercentage }}%)
              </span>
            </h3>
            <p v-if="!activeDeptData.editable" class="text-xs text-yellow-700 mt-1">
              Mode lecture seule — vous n'avez pas les droits de validation pour ce département
            </p>
          </div>
          <div class="divide-y divide-gray-100">
            <div
              v-for="mod in activeDeptData.modules"
              :key="mod.id"
              class="px-6 py-4 flex items-center justify-between hover:bg-gray-50 transition-colors"
            >
              <div class="flex items-center gap-4 flex-1">
                <div
                  class="w-8 h-8 rounded-full flex items-center justify-center text-sm font-bold flex-shrink-0"
                  :class="mod.completed ? 'bg-green-100 text-green-600' : 'bg-gray-100 text-gray-400'"
                >
                  {{ mod.completed ? '&#10003;' : mod.displayOrder }}
                </div>
                <div>
                  <div
                    class="font-medium text-gray-800"
                    :class="mod.completed ? 'line-through text-gray-400' : ''"
                  >
                    {{ mod.name }}
                  </div>
                  <div v-if="mod.completed && mod.completedDate" class="text-xs text-gray-400 mt-0.5">
                    Complété le {{ mod.completedDate }}
                    <span v-if="mod.validatedBy"> par {{ mod.validatedBy }}</span>
                  </div>
                  <div v-if="mod.comment" class="text-xs text-gray-500 mt-0.5 italic">
                    "{{ mod.comment }}"
                  </div>
                </div>
              </div>
              <div v-if="activeDeptData.editable">
                <button
                  @click="validateModule(mod)"
                  :disabled="validatingModuleId === mod.id"
                  class="px-3 py-1.5 text-sm rounded-lg font-medium transition-colors"
                  :class="mod.completed
                    ? 'bg-red-50 text-red-600 hover:bg-red-100'
                    : 'bg-green-50 text-green-600 hover:bg-green-100'"
                >
                  {{ validatingModuleId === mod.id ? 'Enregistrement...' : (mod.completed ? 'Annuler' : 'Valider') }}
                </button>
              </div>
              <div v-else class="text-xs text-gray-400 italic">—</div>
            </div>
          </div>
        </div>
      </div>

      <!-- Empty state -->
      <div v-if="!selectedOperatorId" class="text-center py-20 text-gray-400">
        <svg class="mx-auto h-16 w-16 mb-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5"
            d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
        </svg>
        <p class="text-lg">Sélectionnez un opérateur pour voir son suivi onboarding</p>
      </div>
    </div>

    <!-- TAB 3: HISTORY -->
    <div v-if="activeTab === 'history'">
      <!-- Summary Cards -->
      <div class="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
        <div class="bg-white rounded-xl shadow-sm p-5 text-center">
          <div class="text-4xl font-bold text-gray-800">{{ scopedPendingOperators.length + scopedCompletedOperators.length }}</div>
          <div class="text-sm text-gray-500 mt-1">Total Opérateurs</div>
        </div>
        <div class="bg-green-50 rounded-xl shadow-sm p-5 text-center border border-green-200">
          <div class="text-4xl font-bold text-green-600">{{ scopedCompletedOperators.length }}</div>
          <div class="text-sm text-green-700 mt-1">Onboarding Terminé</div>
        </div>
        <div class="bg-orange-50 rounded-xl shadow-sm p-5 text-center border border-orange-200">
          <div class="text-4xl font-bold text-orange-600">{{ scopedPendingOperators.length }}</div>
          <div class="text-sm text-orange-700 mt-1">En Cours</div>
        </div>
      </div>

      <!-- Sub-Tabs Selector inside History -->
      <div class="flex border-b border-gray-250 mb-4 overflow-x-auto gap-2">
        <button
          @click="historySubTab = 'nouveaux_encours'"
          class="px-4 py-2 border-b-2 font-semibold text-xs whitespace-nowrap transition-all"
          :class="historySubTab === 'nouveaux_encours' ? 'border-orange-500 text-orange-600 font-bold' : 'border-transparent text-gray-500 hover:text-gray-700'"
        >
          Nouveaux Recrus (En Cours: {{ historyNouveauxRecrus.length }})
        </button>
        <button
          @click="historySubTab = 'deja_encours'"
          class="px-4 py-2 border-b-2 font-semibold text-xs whitespace-nowrap transition-all"
          :class="historySubTab === 'deja_encours' ? 'border-orange-500 text-orange-600 font-bold' : 'border-transparent text-gray-500 hover:text-gray-700'"
        >
          Déjà en Poste (En Cours: {{ historyDejaEnPoste.length }})
        </button>
        <button
          @click="historySubTab = 'nouveaux_prêt'"
          class="px-4 py-2 border-b-2 font-semibold text-xs whitespace-nowrap transition-all"
          :class="historySubTab === 'nouveaux_prêt' ? 'border-green-500 text-green-600 font-bold' : 'border-transparent text-gray-500 hover:text-gray-700'"
        >
          Nouveaux Recrus (Prêt: {{ historyCompletedNouveaux.length }})
        </button>
        <button
          @click="historySubTab = 'deja_prêt'"
          class="px-4 py-2 border-b-2 font-semibold text-xs whitespace-nowrap transition-all"
          :class="historySubTab === 'deja_prêt' ? 'border-green-500 text-green-600 font-bold' : 'border-transparent text-gray-500 hover:text-gray-700'"
        >
          Déjà en Poste (Prêt: {{ historyCompletedDeja.length }})
        </button>
      </div>

      <!-- Single Unified Paginated Table -->
      <div v-if="paginatedHistoryList.length > 0" class="bg-white rounded-xl shadow-sm overflow-hidden">
        <div class="overflow-x-auto">
          <table class="w-full text-sm">
            <thead>
              <tr class="bg-gray-50 border-b border-gray-200">
                <th class="text-left px-4 py-3 font-semibold text-gray-700 min-w-[200px]">Opérateur</th>
                <th class="text-left px-4 py-3 font-semibold text-gray-700 min-w-[120px]">Matricule</th>
                <th v-for="dept in departmentNames" :key="'hdept-'+dept" class="text-center px-3 py-3 font-semibold text-gray-700 min-w-[110px]">{{ dept }}</th>
                <th class="text-center px-4 py-3 font-semibold text-gray-700 min-w-[100px]">
                  {{ historySubTab.includes('prêt') ? 'Statut' : 'Reste' }}
                </th>
              </tr>
            </thead>
            <tbody class="divide-y divide-gray-100">
              <tr
                v-for="op in paginatedHistoryList"
                :key="op.operatorId"
                class="hover:bg-blue-50/40 cursor-pointer transition-colors"
                @click="goToDetail(op.operatorId)"
              >
                <td class="px-4 py-3 font-semibold text-gray-900">{{ op.firstName }} {{ op.lastName }}</td>
                <td class="px-4 py-3 text-gray-500 font-mono text-xs">{{ op.matricule }}</td>
                <td v-for="dept in departmentNames" :key="'hdept-val-'+dept" class="text-center px-3 py-3">
                  <span
                    v-if="op.departmentProgress && op.departmentProgress[dept]"
                    class="text-xs font-semibold"
                    :class="op.departmentProgress[dept].departmentComplete ? 'text-green-600' : op.departmentProgress[dept].completed > 0 ? 'text-yellow-600' : 'text-gray-400'"
                  >
                    {{ op.departmentProgress[dept].completed }}/{{ op.departmentProgress[dept].total }}
                  </span>
                  <span v-else class="text-gray-300">—</span>
                </td>
                <td class="text-center px-4 py-3">
                  <span v-if="historySubTab.includes('prêt')" class="inline-flex px-2.5 py-1 rounded-full text-xs font-bold bg-green-100 text-green-700">
                    &#10003; Prêt
                  </span>
                  <span v-else class="text-xs font-semibold text-orange-600">
                    {{ (op.totalModules - op.completedModules) }} modules
                  </span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- History Pagination Footer -->
        <div v-if="historyTotalPages > 1" class="px-6 py-3 bg-gray-50 border-t flex justify-between items-center text-xs text-gray-500 font-medium">
          <span>Affichage de {{ (historyCurrentPage - 1) * historyPageSize + 1 }} à {{ Math.min(historyCurrentPage * historyPageSize, selectedHistoryList.length) }} sur {{ selectedHistoryList.length }} opérateur(s)</span>
          <div class="flex gap-1">
            <button :disabled="historyCurrentPage === 1" @click="historyCurrentPage--" class="px-2 py-1 bg-white border border-gray-200 rounded hover:bg-gray-50 disabled:opacity-50 font-bold">Précédent</button>
            <span class="px-3.5 py-1 bg-gray-100 rounded flex items-center font-bold">Page {{ historyCurrentPage }} / {{ historyTotalPages }}</span>
            <button :disabled="historyCurrentPage === historyTotalPages" @click="historyCurrentPage++" class="px-2 py-1 bg-white border border-gray-200 rounded hover:bg-gray-50 disabled:opacity-50 font-bold">Suivant</button>
          </div>
        </div>
      </div>
      
      <!-- Fallback empty table when no operators found in history for current scope -->
      <div v-else class="bg-white rounded-xl border border-gray-200 p-12 text-center text-gray-400 text-sm">
        Aucun opérateur trouvé dans cette catégorie.
      </div>
    </div>


  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import onboardingApi from '@/api/onboarding';
import { operatorsApi } from '@/api/endpoints';
import { useAuthStore } from '@/stores/auth';
import { useUserScope } from '@/composables/useUserScope';

const router = useRouter();
const authStore = useAuthStore();
const { loadUserProjects, filterOperators } = useUserScope();

// Tab state
const activeTab = ref('matrix');

const matrixCurrentPage = ref(1)
const matrixPageSize = ref(15)

const historySubTab = ref('nouveaux_encours')
const historyCurrentPage = ref(1)
const historyPageSize = ref(15)

// Data
const allOperators = ref([]);
const operatorsSummary = ref([]);
const selectedOperatorId = ref('');
const departments = ref([]);
const activeDepartment = ref('');
const progress = ref(null);
const historyData = ref({
  totalOperators: 0,
  completedCount: 0,
  pendingCount: 0,
  completedOperators: [],
  pendingOperators: []
});
const searchQuery = ref('');
const detailSearchQuery = ref('');
const filteredDetailOperators = computed(() => {
  const scoped = allOperators.value || []
  if (!detailSearchQuery.value) return scoped
  const q = detailSearchQuery.value.trim().toLowerCase()
  return scoped.filter(op =>
    `${op.lastName} ${op.firstName}`.toLowerCase().includes(q) ||
    (op.employeeId && op.employeeId.toLowerCase().includes(q))
  )
})
const loadingMatrix = ref(false);

const validatingModuleId = ref(null);

// Current user roles
const userRoles = computed(() => authStore.user?.roles || []);

const scopedPendingOperators = computed(() => {
  return historyData.value.pendingOperators || []
})

const scopedCompletedOperators = computed(() => {
  return historyData.value.completedOperators || []
})

// Split operators by type in history
const historyNouveauxRecrus = computed(() => {
  return scopedPendingOperators.value.filter(op => op.operatorType === 'NOUVEAU_RECRU')
})
const historyDejaEnPoste = computed(() => {
  return scopedPendingOperators.value.filter(op => op.operatorType === 'DEJA_EN_POSTE')
})
const historyCompletedNouveaux = computed(() => {
  return scopedCompletedOperators.value.filter(op => op.operatorType === 'NOUVEAU_RECRU')
})
const historyCompletedDeja = computed(() => {
  return scopedCompletedOperators.value.filter(op => op.operatorType === 'DEJA_EN_POSTE')
})

// Department column names from summary data
const departmentNames = computed(() => {
  if (operatorsSummary.value.length === 0) return [];
  const first = operatorsSummary.value[0];
  return first.departmentProgress ? Object.keys(first.departmentProgress) : [];
});

const scopedOperatorsSummary = computed(() => {
  return operatorsSummary.value || []
})

// Filter operators in matrix
const filteredOperators = computed(() => {
  if (!searchQuery.value) return scopedOperatorsSummary.value;
  const q = searchQuery.value.toLowerCase();
  return scopedOperatorsSummary.value.filter(op =>
    (op.firstName + ' ' + op.lastName).toLowerCase().includes(q) ||
    (op.matricule || '').toLowerCase().includes(q)
  );
});

const pendingOperators = computed(() => filteredOperators.value.filter(op => !op.onboardingComplete))

const paginatedPendingOperators = computed(() => {
  const start = (matrixCurrentPage.value - 1) * matrixPageSize.value
  const end = start + matrixPageSize.value
  return pendingOperators.value.slice(start, end)
})

const matrixTotalPages = computed(() => {
  return Math.ceil(pendingOperators.value.length / matrixPageSize.value) || 1
})

const selectedHistoryList = computed(() => {
  if (historySubTab.value === 'nouveaux_encours') return historyNouveauxRecrus.value
  if (historySubTab.value === 'deja_encours') return historyDejaEnPoste.value
  if (historySubTab.value === 'nouveaux_prêt') return historyCompletedNouveaux.value
  return historyCompletedDeja.value
})

const paginatedHistoryList = computed(() => {
  const start = (historyCurrentPage.value - 1) * historyPageSize.value
  const end = start + historyPageSize.value
  return selectedHistoryList.value.slice(start, end)
})

const historyTotalPages = computed(() => {
  return Math.ceil(selectedHistoryList.value.length / historyPageSize.value) || 1
})

watch([searchQuery, matrixPageSize], () => {
  matrixCurrentPage.value = 1
})

watch(historySubTab, () => {
  historyCurrentPage.value = 1
})

// Active department data for detail view
const activeDeptData = computed(() => {
  return departments.value.find(d => d.department === activeDepartment.value)
    || { modules: [], completedModules: 0, totalModules: 0, completionPercentage: 0, editable: false };
});

// ==================== LIFECYCLE ====================

onMounted(async () => {
  try {
    await loadUserProjects();
    const opsRes = await operatorsApi.getAll();
    allOperators.value = opsRes.data || [];
  } catch (e) {
    console.error('Error loading operators:', e);
  }
  loadMatrixData();
  loadHistory();
});

// ==================== DATA LOADERS ====================

async function loadMatrixData() {
  loadingMatrix.value = true;
  try {
    const res = await onboardingApi.getOperatorsSummary();
    operatorsSummary.value = res.data || [];
  } catch (e) {
    console.error('Error loading operators summary:', e);
  } finally {
    loadingMatrix.value = false;
  }
}

async function loadHistory() {
  try {
    const res = await onboardingApi.getHistory();
    historyData.value = res.data || {
      totalOperators: 0,
      completedCount: 0,
      pendingCount: 0,
      completedOperators: [],
      pendingOperators: []
    };
  } catch (e) {
    console.error('Error loading history:', e);
  }
}

async function loadOperatorStatus() {
  if (!selectedOperatorId.value) {
    departments.value = [];
    progress.value = null;
    return;
  }
  try {
    const [statusRes, progressRes] = await Promise.all([
      onboardingApi.getOperatorStatus(selectedOperatorId.value),
      onboardingApi.getOperatorProgress(selectedOperatorId.value),
    ]);
    departments.value = statusRes.data || [];
    progress.value = progressRes.data || {};
    if (departments.value.length > 0 && !activeDepartment.value) {
      activeDepartment.value = departments.value[0].department;
    }
  } catch (e) {
    console.error('Error loading operator status:', e);
  }
}

// ==================== NAVIGATION ====================

function goToDetail(operatorId) {
  selectedOperatorId.value = operatorId;
  activeTab.value = 'detail';
  activeDepartment.value = '';
  loadOperatorStatus();
}

async function validateModule(mod) {
  validatingModuleId.value = mod.id;
  try {
    await onboardingApi.validateModule(selectedOperatorId.value, mod.id, {
      completed: !mod.completed,
    });
    await loadOperatorStatus();
    loadMatrixData();
    loadHistory();
  } catch (e) {
    console.error('Error validating module:', e);
    alert(e?.response?.data?.message || 'Erreur lors de la validation.');
  } finally {
    validatingModuleId.value = null;
  }
}
</script>
