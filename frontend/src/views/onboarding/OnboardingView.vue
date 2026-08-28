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
                v-for="op in pendingOperators"
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

        <!-- Department progress bars -->
        <div class="space-y-3">
          <div v-for="dept in progress.departments" :key="dept.department" class="flex items-center gap-3">
            <span class="w-44 text-sm font-medium text-gray-700 truncate">{{ dept.department }}</span>
            <div class="flex-1 bg-gray-200 rounded-full h-5 relative">
              <div
                class="h-5 rounded-full transition-all duration-500"
                :class="dept.percentage === 100 ? 'bg-green-500' : 'bg-blue-500'"
                :style="{ width: dept.percentage + '%' }"
              ></div>
              <span class="absolute inset-0 flex items-center justify-center text-xs font-medium text-gray-700">
                {{ dept.completed }}/{{ dept.total }} ({{ dept.percentage }}%)
              </span>
            </div>
            <svg v-if="dept.departmentComplete" class="w-5 h-5 text-green-500 flex-shrink-0" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"/>
            </svg>
            <span v-if="!dept.editable" class="text-xs text-gray-400 italic w-24 text-right flex-shrink-0">Lecture seule</span>
          </div>
        </div>

        <!-- Ready for formation link -->
        <div v-if="progress.onboardingComplete" class="mt-5 p-3 bg-green-50 border border-green-200 rounded-lg">
          <p class="text-sm text-green-700">
            Cet opérateur a terminé tous les modules théoriques. Le chef d'équipe peut maintenant
            <router-link :to="'/formations?operatorId=' + selectedOperatorId" class="underline font-semibold hover:text-green-900">
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

      <!-- COMPLETED: DEJA EN POSTE -->
      <div v-if="historyCompletedDeja.length > 0" class="mb-6">
        <h2 class="text-lg font-semibold text-green-700 mb-3 flex items-center gap-2">
          <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"/></svg>
          Onboarding Terminé - Déjà en Poste ({{ historyCompletedDeja.length }})
        </h2>
        <div class="bg-white rounded-xl shadow-sm overflow-x-auto">
          <table class="w-full text-sm"><thead><tr class="bg-green-50 border-b border-green-200"><th class="text-left px-4 py-3 font-semibold text-green-800 min-w-[200px]">Opérateur</th><th class="text-left px-4 py-3 font-semibold text-green-800 min-w-[120px]">Matricule</th><th v-for="dept in departmentNames" :key="'hcd-'+dept" class="text-center px-3 py-3 font-semibold text-green-800 min-w-[110px]">{{ dept }}</th><th class="text-center px-3 py-3 font-semibold text-green-800 min-w-[70px]">Statut</th></tr></thead>
          <tbody class="divide-y divide-gray-100"><tr v-for="op in historyCompletedDeja" :key="op.operatorId" class="hover:bg-green-50/50 cursor-pointer" @click="goToDetail(op.operatorId)"><td class="px-4 py-3 font-medium text-gray-800">{{ op.firstName }} {{ op.lastName }}</td><td class="px-4 py-3 text-gray-500">{{ op.matricule }}</td><td v-for="dept in departmentNames" :key="'hcd2-'+dept" class="text-center px-3 py-3"><span v-if="op.departmentProgress && op.departmentProgress[dept]" class="text-green-600 font-medium">{{ op.departmentProgress[dept].completed }}/{{ op.departmentProgress[dept].total }}</span></td><td class="text-center px-3 py-3"><span class="inline-flex items-center px-2 py-1 rounded-full text-xs font-semibold bg-green-100 text-green-700">&#10003; Prêt</span></td></tr></tbody></table>
        </div>
      </div>

      <!-- COMPLETED: NOUVEAUX RECRUS -->
      <div v-if="historyCompletedNouveaux.length > 0" class="mb-6">
        <h2 class="text-lg font-semibold text-blue-700 mb-3 flex items-center gap-2">
          <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"/></svg>
          Onboarding Terminé - Nouveaux Recrus ({{ historyCompletedNouveaux.length }})
        </h2>
        <div class="bg-white rounded-xl shadow-sm overflow-x-auto">
          <table class="w-full text-sm"><thead><tr class="bg-blue-50 border-b border-blue-200"><th class="text-left px-4 py-3 font-semibold text-blue-800 min-w-[200px]">Opérateur</th><th class="text-left px-4 py-3 font-semibold text-blue-800 min-w-[120px]">Matricule</th><th v-for="dept in departmentNames" :key="'hcn-'+dept" class="text-center px-3 py-3 font-semibold text-blue-800 min-w-[110px]">{{ dept }}</th><th class="text-center px-3 py-3 font-semibold text-blue-800 min-w-[70px]">Statut</th></tr></thead>
          <tbody class="divide-y divide-gray-100"><tr v-for="op in historyCompletedNouveaux" :key="op.operatorId" class="hover:bg-blue-50/50 cursor-pointer" @click="goToDetail(op.operatorId)"><td class="px-4 py-3 font-medium text-gray-800">{{ op.firstName }} {{ op.lastName }}</td><td class="px-4 py-3 text-gray-500">{{ op.matricule }}</td><td v-for="dept in departmentNames" :key="'hcn2-'+dept" class="text-center px-3 py-3"><span v-if="op.departmentProgress && op.departmentProgress[dept]" class="text-green-600 font-medium">{{ op.departmentProgress[dept].completed }}/{{ op.departmentProgress[dept].total }}</span></td><td class="text-center px-3 py-3"><span class="inline-flex items-center px-2 py-1 rounded-full text-xs font-semibold bg-blue-100 text-blue-700">&#10003; Prêt formation</span></td></tr></tbody></table>
        </div>
      </div>

      <!-- PENDING: DEJA EN POSTE -->
      <div v-if="historyDejaEnPoste.length > 0" class="mb-6">
        <h2 class="text-lg font-semibold text-orange-700 mb-3 flex items-center gap-2">
          <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm1-12a1 1 0 10-2 0v4a1 1 0 00.293.707l2.828 2.829a1 1 0 101.415-1.415L11 9.586V6z" clip-rule="evenodd"/></svg>
          En Cours - Déjà en Poste ({{ historyDejaEnPoste.length }})
        </h2>
        <div class="bg-white rounded-xl shadow-sm overflow-x-auto">
          <table class="w-full text-sm"><thead><tr class="bg-orange-50 border-b border-orange-200"><th class="text-left px-4 py-3 font-semibold text-orange-800 min-w-[200px]">Opérateur</th><th class="text-left px-4 py-3 font-semibold text-orange-800 min-w-[120px]">Matricule</th><th v-for="dept in departmentNames" :key="'hpd-'+dept" class="text-center px-3 py-3 font-semibold text-orange-800 min-w-[110px]">{{ dept }}</th><th class="text-center px-3 py-3 font-semibold text-orange-800 min-w-[100px]">Reste</th></tr></thead>
          <tbody class="divide-y divide-gray-100"><tr v-for="op in historyDejaEnPoste" :key="op.operatorId" class="hover:bg-orange-50/50 cursor-pointer" @click="goToDetail(op.operatorId)"><td class="px-4 py-3 font-medium text-gray-800">{{ op.firstName }} {{ op.lastName }}</td><td class="px-4 py-3 text-gray-500">{{ op.matricule }}</td><td v-for="dept in departmentNames" :key="'hpd2-'+dept" class="text-center px-3 py-3"><span v-if="op.departmentProgress && op.departmentProgress[dept]" class="text-xs font-medium" :class="op.departmentProgress[dept].departmentComplete ? 'text-green-600' : op.departmentProgress[dept].completed > 0 ? 'text-yellow-600' : 'text-gray-400'">{{ op.departmentProgress[dept].completed }}/{{ op.departmentProgress[dept].total }}</span></td><td class="text-center px-3 py-3"><span class="text-orange-600 font-bold">{{ (op.totalModules - op.completedModules) }}</span><span class="text-xs text-gray-400"> restants</span></td></tr></tbody></table>
        </div>
      </div>

      <!-- PENDING: NOUVEAUX RECRUS -->
      <div v-if="historyNouveauxRecrus.length > 0">
        <h2 class="text-lg font-semibold text-blue-700 mb-3 flex items-center gap-2">
          <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm1-12a1 1 0 10-2 0v4a1 1 0 00.293.707l2.828 2.829a1 1 0 101.415-1.415L11 9.586V6z" clip-rule="evenodd"/></svg>
          En Cours - Nouveaux Recrus ({{ historyNouveauxRecrus.length }})
        </h2>
        <div class="bg-white rounded-xl shadow-sm overflow-x-auto">
          <table class="w-full text-sm"><thead><tr class="bg-blue-50 border-b border-blue-200"><th class="text-left px-4 py-3 font-semibold text-blue-800 min-w-[200px]">Opérateur</th><th class="text-left px-4 py-3 font-semibold text-blue-800 min-w-[120px]">Matricule</th><th v-for="dept in departmentNames" :key="'hpn-'+dept" class="text-center px-3 py-3 font-semibold text-blue-800 min-w-[110px]">{{ dept }}</th><th class="text-center px-3 py-3 font-semibold text-blue-800 min-w-[100px]">Reste</th></tr></thead>
          <tbody class="divide-y divide-gray-100"><tr v-for="op in historyNouveauxRecrus" :key="op.operatorId" class="hover:bg-blue-50/50 cursor-pointer" @click="goToDetail(op.operatorId)"><td class="px-4 py-3 font-medium text-gray-800">{{ op.firstName }} {{ op.lastName }}</td><td class="px-4 py-3 text-gray-500">{{ op.matricule }}</td><td v-for="dept in departmentNames" :key="'hpn2-'+dept" class="text-center px-3 py-3"><span v-if="op.departmentProgress && op.departmentProgress[dept]" class="text-xs font-medium" :class="op.departmentProgress[dept].departmentComplete ? 'text-green-600' : op.departmentProgress[dept].completed > 0 ? 'text-yellow-600' : 'text-gray-400'">{{ op.departmentProgress[dept].completed }}/{{ op.departmentProgress[dept].total }}</span></td><td class="text-center px-3 py-3"><span class="text-blue-600 font-bold">{{ (op.totalModules - op.completedModules) }}</span><span class="text-xs text-gray-400"> restants</span></td></tr></tbody></table>
        </div>
      </div>

      <!-- Fallback empty table when no operators found in history for current scope -->
      <div v-if="scopedPendingOperators.length + scopedCompletedOperators.length === 0" class="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
        <div class="overflow-x-auto">
          <table class="w-full text-sm">
            <thead class="bg-gray-50">
              <tr class="border-b border-gray-200">
                <th class="text-left px-4 py-3 font-semibold text-gray-700">Opérateur</th>
                <th class="text-left px-4 py-3 font-semibold text-gray-700">Matricule</th>
                <th class="text-center px-4 py-3 font-semibold text-gray-700">Statut</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td colspan="3" class="text-center py-12 text-gray-400">
                  Aucun opérateur trouvé dans l'historique d'onboarding
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>


  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
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
  const scoped = filterOperators(allOperators.value)
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
  const scoped = filterOperators(allOperators.value)
  const scopedIds = new Set(scoped.map(o => o.id))
  return (historyData.value.pendingOperators || []).filter(op => scopedIds.has(op.operatorId || op.id))
})

const scopedCompletedOperators = computed(() => {
  const scoped = filterOperators(allOperators.value)
  const scopedIds = new Set(scoped.map(o => o.id))
  return (historyData.value.completedOperators || []).filter(op => scopedIds.has(op.operatorId || op.id))
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
  const scoped = filterOperators(allOperators.value)
  const scopedIds = new Set(scoped.map(o => o.id))
  return operatorsSummary.value.filter(op => scopedIds.has(op.operatorId || op.id))
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
