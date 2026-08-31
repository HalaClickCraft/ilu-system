<template>
  <div class="space-y-6">
    <div class="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">Formation</h1>
        <p class="mt-1 text-gray-500">Suivi et planification des formations ILU</p>
      </div>
      <div v-if="canContribute" class="flex gap-2">
        <button @click="openDailyBatch" class="rounded-lg border border-sky-600 px-4 py-2.5 text-sm font-medium text-sky-700 hover:bg-sky-50">Saisie quotidienne</button>
      </div>
    </div>

    <div v-if="error" class="rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">{{ error }}</div>

    <!-- Stats cards -->
    <div class="grid grid-cols-2 gap-4 md:grid-cols-4">
      <div class="rounded-xl border border-gray-200 bg-white p-5 shadow-sm"><p class="text-sm text-gray-500">Opérateurs</p><p class="mt-1 text-2xl font-bold">{{ operatorGroups.length }}</p></div>
      <div class="rounded-xl border border-gray-200 bg-white p-5 shadow-sm"><p class="text-sm text-gray-500">En cours</p><p class="mt-1 text-2xl font-bold text-amber-600">{{ inProgressCount }}</p></div>
      <div class="rounded-xl border border-gray-200 bg-white p-5 shadow-sm"><p class="text-sm text-gray-500">Réussies</p><p class="mt-1 text-2xl font-bold text-emerald-600">{{ completedCount }}</p></div>
      <div class="rounded-xl border border-gray-200 bg-white p-5 shadow-sm"><p class="text-sm text-gray-500">Échouées</p><p class="mt-1 text-2xl font-bold text-red-600">{{ failedCount }}</p></div>
    </div>

    <!-- Master-Detail Split Workspace -->
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6 items-start">
      <!-- Left Pane: Worklist Navigation & List (Width 1/3) -->
      <div class="lg:col-span-1 bg-white rounded-xl border border-gray-200 shadow-sm flex flex-col h-[70vh]">
        <!-- Vertical/Tabs select bar inside Left Pane -->
        <div class="p-3 border-b border-gray-100 space-y-2">
          <!-- Small Dropdown workflow tabs -->
          <div class="grid grid-cols-1 gap-1 bg-gray-50 p-1 rounded-lg">
            <div class="flex flex-wrap gap-1">
              <button
                v-for="t in pageTabs"
                :key="t.value"
                @click="currentWorkflowTab = t.value"
                class="flex-1 min-w-[100px] px-2 py-1.5 rounded-md text-[10px] font-semibold transition text-center"
                :class="currentWorkflowTab === t.value ? 'bg-white text-sky-700 shadow-sm border border-gray-100' : 'text-gray-500 hover:text-gray-700'"
              >
                {{ t.label.split(' ')[1] || t.label }} ({{ t.badge }})
              </button>
            </div>
          </div>

          <!-- Dynamic search input -->
          <div class="relative">
            <svg class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path></svg>
            <input
              v-model="search"
              type="text"
              placeholder="Rechercher..."
              class="w-full pl-9 pr-3 py-1.5 border border-gray-200 rounded-lg text-xs outline-none focus:ring-2 focus:ring-sky-500"
            />
          </div>
        </div>

        <!-- Worklist List body -->
        <div v-if="loading" class="flex items-center justify-center flex-1">
          <div class="w-6 h-6 border-2 border-sky-200 border-t-sky-600 rounded-full animate-spin"></div>
        </div>

        <!-- If on eligible tab -->
        <template v-else-if="currentWorkflowTab === 'eligible'">
          <div v-if="!paginatedEligibleOperators.length" class="p-8 text-center text-xs text-gray-400 flex-1">
            Aucun opérateur éligible
          </div>
          <div v-else class="flex-1 overflow-y-auto divide-y divide-gray-50">
            <div
              v-for="op in paginatedEligibleOperators"
              :key="op.id"
              @click="selectedOperatorId = op.id"
              class="p-3 flex items-center justify-between cursor-pointer transition-colors"
              :class="selectedOperatorId === op.id ? 'bg-sky-50/70 border-l-4 border-sky-600 pl-2' : 'hover:bg-gray-50 border-l-4 border-transparent'"
            >
              <div class="flex items-center gap-2.5">
                <div class="w-9 h-9 rounded-full bg-indigo-50 flex items-center justify-center font-bold text-xs text-indigo-700 flex-shrink-0">
                  {{ (op.lastName || '')[0] }}{{ (op.firstName || '')[0] }}
                </div>
                <div class="min-w-0">
                  <p class="text-xs font-semibold text-gray-800 truncate">{{ op.lastName }} {{ op.firstName }}</p>
                  <p class="text-[10px] text-gray-400 mt-0.5">{{ op.employeeId }} · {{ op.team?.name || 'Sans équipe' }}</p>
                </div>
              </div>
              <span class="text-[10px] bg-slate-100 text-slate-600 px-1.5 py-0.5 rounded border flex-shrink-0">Éligible</span>
            </div>
          </div>
          <!-- Pagination for eligible -->
          <div class="p-2 border-t border-gray-100 flex justify-between items-center text-[10px] text-gray-500 bg-gray-50/50">
            <span>Page {{ eligibleCurrentPage }}</span>
            <div class="flex gap-1">
              <button :disabled="eligibleCurrentPage === 1" @click="eligibleCurrentPage--" class="px-2 py-0.5 bg-white border rounded disabled:opacity-50 font-semibold">Préc.</button>
              <button :disabled="paginatedEligibleOperators.length < eligiblePageSize" @click="eligibleCurrentPage++" class="px-2 py-0.5 bg-white border rounded disabled:opacity-50 font-semibold">Suiv.</button>
            </div>
          </div>
        </template>

        <!-- If on active / history tab -->
        <template v-else-if="currentWorkflowTab === 'active' || currentWorkflowTab === 'history'">
          <div v-if="!paginatedGroups.length" class="p-8 text-center text-xs text-gray-400 flex-1">
            Aucun opérateur trouvé
          </div>
          <div v-else class="flex-1 overflow-y-auto divide-y divide-gray-50">
            <div
              v-for="op in paginatedGroups"
              :key="op.operatorId"
              @click="selectedOperatorId = op.operatorId"
              class="p-3 flex items-center justify-between cursor-pointer transition-colors"
              :class="selectedOperatorId === op.operatorId ? 'bg-sky-50/70 border-l-4 border-sky-600 pl-2' : 'hover:bg-gray-50 border-l-4 border-transparent'"
            >
              <div class="flex items-center gap-2.5">
                <div class="w-9 h-9 rounded-full bg-sky-50 flex items-center justify-center font-bold text-xs text-sky-700 flex-shrink-0">
                  {{ (op.operatorName || '')[0] }}
                </div>
                <div class="min-w-0">
                  <p class="text-xs font-semibold text-gray-800 truncate">{{ op.operatorName }}</p>
                  <p class="text-[10px] text-gray-400 mt-0.5">{{ op.matricule }}</p>
                </div>
              </div>
              <div class="flex flex-col items-end gap-1 flex-shrink-0">
                <span v-if="currentWorkflowTab === 'active'" class="text-[10px] bg-sky-50 text-sky-700 border border-sky-200 px-1.5 py-0.5 rounded font-bold">
                  J{{ op.currentFormation?.daysWithData || 0 }}/12
                </span>
                <span v-else class="text-[10px] text-gray-500 font-medium">
                  {{ op.formations.length }} F.
                </span>
              </div>
            </div>
          </div>
          <!-- Pagination for groups -->
          <div class="p-2 border-t border-gray-100 flex justify-between items-center text-[10px] text-gray-500 bg-gray-50/50">
            <span>Page {{ mainCurrentPage }}</span>
            <div class="flex gap-1">
              <button :disabled="mainCurrentPage === 1" @click="mainCurrentPage--" class="px-2 py-0.5 bg-white border rounded disabled:opacity-50 font-semibold">Préc.</button>
              <button :disabled="paginatedGroups.length < mainPageSize" @click="mainCurrentPage++" class="px-2 py-0.5 bg-white border rounded disabled:opacity-50 font-semibold">Suiv.</button>
            </div>
          </div>
        </template>

        <!-- If on retard / alerts tab -->
        <template v-else-if="currentWorkflowTab === 'retard'">
          <!-- For alerts we can list either overdue formations or pending evaluations -->
          <div class="flex-1 overflow-y-auto divide-y divide-gray-50">
            <!-- Overdue list -->
            <div v-for="f in overdueFormations" :key="'overdue_' + f.id" @click="selectedOperatorId = f.operatorId" class="p-3 cursor-pointer hover:bg-gray-50 border-l-4" :class="selectedOperatorId === f.operatorId ? 'bg-red-50/70 border-red-500 pl-2' : 'border-transparent'">
              <p class="text-xs font-semibold text-red-700">{{ f.operatorName }}</p>
              <p class="text-[10px] text-gray-500 mt-0.5">{{ f.workstationName }} · Sans activité depuis 14+ jours</p>
            </div>
            <!-- Pending evaluation validation list -->
            <div v-for="ev in evalEnRetard" :key="'eval_' + ev.id" @click="selectedOperatorId = ev.operatorId" class="p-3 cursor-pointer hover:bg-gray-50 border-l-4" :class="selectedOperatorId === ev.operatorId ? 'bg-amber-50/70 border-amber-500 pl-2' : 'border-transparent'">
              <p class="text-xs font-semibold text-amber-700">{{ ev.operatorName }}</p>
              <p class="text-[10px] text-gray-500 mt-0.5">{{ ev.workstationName }} · J12 complété, évaluation finale requise</p>
            </div>
            <div v-if="!overdueFormations.length && !evalEnRetard.length" class="p-8 text-center text-xs text-gray-400 flex-1">
              Aucune alerte active
            </div>
          </div>
        </template>
      </div>

      <!-- Right Pane: Actions & Form Workspace (Width 2/3) -->
      <div class="lg:col-span-2 bg-white rounded-xl border border-gray-200 shadow-sm p-6 overflow-y-auto h-[70vh] flex flex-col justify-between">
        
        <!-- === IF TAB IS ACTIVE === -->
        <template v-if="currentWorkflowTab === 'active'">
          <div v-if="!selectedGroup" class="flex flex-col items-center justify-center text-center py-20 text-gray-400 flex-1">
            <svg class="w-12 h-12 text-gray-300 mb-3" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M13 10V3L4 14h7v7l9-11h-7z"/></svg>
            <p class="text-sm font-semibold">Sélectionnez une formation active</p>
            <p class="text-xs mt-1">Choisissez un opérateur dans la liste de gauche pour enregistrer son suivi quotidien ou visualiser sa progression J1-J12.</p>
          </div>
          <div v-else class="space-y-6 flex-1">
            <!-- Active formation header details -->
            <div class="flex items-center justify-between pb-4 border-b">
              <div>
                <h2 class="text-lg font-bold text-gray-900 leading-tight">{{ selectedGroup.operatorName }}</h2>
                <p class="text-xs text-gray-500 mt-0.5">Poste: <span class="font-bold text-sky-700">{{ selectedGroup.currentFormation?.workstationName || '-' }}</span> · Matricule: {{ selectedGroup.matricule }}</p>
              </div>
              <router-link :to="'/training/' + selectedGroup.formations.find(f => f.status === 'IN_PROGRESS')?.id" class="px-3 py-1.5 bg-gray-100 hover:bg-gray-200 text-gray-700 text-xs font-semibold rounded-lg shadow-sm transition border">
                Historique Complet
              </router-link>
            </div>

            <!-- J1 - J12 Progress Blocks -->
            <div>
              <h3 class="text-xs font-semibold text-gray-700 mb-2.5">Progression des 12 Jours</h3>
              <div class="grid grid-cols-4 sm:grid-cols-6 gap-2">
                <div
                  v-for="day in 12"
                  :key="day"
                  class="rounded-lg border p-2 flex flex-col items-center justify-center text-center transition-colors"
                  :class="day <= (selectedGroup.currentFormation?.daysWithData || 0) ? 'bg-sky-50 border-sky-200 text-sky-700 font-semibold' : 'bg-gray-50 border-gray-100 text-gray-400'"
                >
                  <span class="text-[10px] font-bold">J{{ day }}</span>
                  <span class="text-[9px] mt-0.5" v-if="day <= (selectedGroup.currentFormation?.daysWithData || 0)">Saisi</span>
                  <span class="text-[9px] mt-0.5" v-else>Vide</span>
                </div>
              </div>
            </div>

            <!-- Direct Daily Saisie Input Form -->
            <div class="bg-slate-50 border rounded-xl p-4 space-y-4" v-if="canContribute">
              <div class="flex items-center justify-between">
                <h3 class="text-xs font-bold text-slate-800">Saisie du Suivi Quotidien</h3>
                <span class="text-[10px] font-bold text-slate-600 bg-slate-200 px-2 py-0.5 rounded-full">
                  Jour cible: J{{ singleEntry.dayNumber }}
                </span>
              </div>
              
              <div class="grid grid-cols-3 gap-3">
                <div>
                  <label class="block text-[10px] font-semibold text-gray-500 mb-1">Numéro du jour</label>
                  <input v-model.number="singleEntry.dayNumber" type="number" min="1" max="12" class="w-full px-2.5 py-1.5 border rounded-lg text-xs" />
                </div>
                <div v-if="canEditCadence">
                  <label class="block text-[10px] font-semibold text-gray-500 mb-1">Cadence réalisée</label>
                  <input v-model.number="singleEntry.cadence" type="number" min="0" placeholder="Pcs/h" class="w-full px-2.5 py-1.5 border rounded-lg text-xs" />
                </div>
                <div v-if="canEditDefects">
                  <label class="block text-[10px] font-semibold text-gray-500 mb-1">Nombre de défauts</label>
                  <input v-model.number="singleEntry.defauts" type="number" min="0" placeholder="Défauts" class="w-full px-2.5 py-1.5 border rounded-lg text-xs" />
                </div>
              </div>

              <div class="flex justify-end pt-2">
                <button
                  type="button"
                  @click="saveSingleDay(selectedGroup.formations.find(f => f.status === 'IN_PROGRESS'))"
                  :disabled="savingSingle || (singleEntry.cadence === null && singleEntry.defauts === null)"
                  class="px-4 py-2 bg-sky-600 hover:bg-sky-700 text-white text-xs font-semibold rounded-lg shadow-sm transition disabled:opacity-50"
                >
                  {{ savingSingle ? 'Enregistrement...' : 'Enregistrer la Saisie' }}
                </button>
              </div>
            </div>
          </div>
        </template>

        <!-- === IF TAB IS ELIGIBLE === -->
        <template v-else-if="currentWorkflowTab === 'eligible'">
          <div v-if="!selectedEligibleOperator" class="flex flex-col items-center justify-center text-center py-20 text-gray-400 flex-1">
            <svg class="w-12 h-12 text-gray-300 mb-3" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M12 4v16m8-8H4"/></svg>
            <p class="text-sm font-semibold">Sélectionnez un opérateur éligible</p>
            <p class="text-xs mt-1">Choisissez un opérateur dans la liste de gauche pour configurer et lancer son affectation à un poste.</p>
          </div>
          <div v-else class="space-y-6 flex-1">
            <div class="pb-4 border-b">
              <h2 class="text-lg font-bold text-gray-900">{{ selectedEligibleOperator.lastName }} {{ selectedEligibleOperator.firstName }}</h2>
              <p class="text-xs text-gray-500 mt-0.5">Matricule: {{ selectedEligibleOperator.employeeId }} · Équipe actuelle: {{ selectedEligibleOperator.team?.name || 'Aucune' }}</p>
            </div>

            <!-- Direct Workstation Assignment Form -->
            <div class="bg-gray-50 border rounded-xl p-4 space-y-4">
              <h3 class="text-xs font-bold text-gray-800">Affectation de Poste</h3>
              
              <div class="space-y-3">
                <div>
                  <label class="block text-[10px] font-semibold text-gray-500 mb-1">Projet</label>
                  <select v-model="form.projectId" class="w-full px-2.5 py-1.5 border rounded-lg text-xs outline-none focus:ring-2 focus:ring-sky-500" @change="form.zoneId = ''; form.workstationId = ''">
                    <option value="">-- Sélectionner le projet --</option>
                    <option v-for="p in availableStructure" :key="p.id" :value="p.id">{{ p.name }}</option>
                  </select>
                </div>

                <div>
                  <label class="block text-[10px] font-semibold text-gray-500 mb-1">Zone</label>
                  <select v-model="form.zoneId" :disabled="!form.projectId" class="w-full px-2.5 py-1.5 border rounded-lg text-xs outline-none focus:ring-2 focus:ring-sky-500 disabled:bg-gray-100" @change="form.workstationId = ''">
                    <option value="">-- Sélectionner la zone --</option>
                    <option v-for="z in availableZones" :key="z.id" :value="z.id">{{ z.name }}</option>
                  </select>
                </div>

                <div>
                  <label class="block text-[10px] font-semibold text-gray-500 mb-1">Poste de travail</label>
                  <select v-model="form.workstationId" :disabled="!form.zoneId" class="w-full px-2.5 py-1.5 border rounded-lg text-xs outline-none focus:ring-2 focus:ring-sky-500 disabled:bg-gray-100">
                    <option value="">-- Sélectionner le poste --</option>
                    <option v-for="w in availableWorkstations" :key="w.id" :value="w.id">{{ w.name }} ({{ w.type || '-' }})</option>
                  </select>
                </div>
              </div>

              <div class="flex justify-end pt-2">
                <button
                  type="button"
                  @click="assignOperatorWorkstation(selectedEligibleOperator)"
                  :disabled="creating || !form.workstationId"
                  class="px-4 py-2 bg-emerald-600 hover:bg-emerald-700 text-white text-xs font-semibold rounded-lg shadow-sm transition disabled:opacity-50"
                >
                  {{ creating ? 'Lancement...' : 'Démarrer la Formation' }}
                </button>
              </div>
            </div>
          </div>
        </template>

        <!-- === IF TAB IS HISTORY === -->
        <template v-else-if="currentWorkflowTab === 'history'">
          <div v-if="!selectedGroup" class="flex flex-col items-center justify-center text-center py-20 text-gray-400 flex-1">
            <svg class="w-12 h-12 text-gray-300 mb-3" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253"/></svg>
            <p class="text-sm font-semibold">Sélectionnez un historique opérateur</p>
            <p class="text-xs mt-1">Choisissez un opérateur dans la liste de gauche pour consulter la liste de ses formations complétées et qualifications.</p>
          </div>
          <div v-else class="space-y-6 flex-1">
            <div class="pb-4 border-b">
              <h2 class="text-lg font-bold text-gray-900">{{ selectedGroup.operatorName }}</h2>
              <p class="text-xs text-gray-500 mt-0.5">Matricule: {{ selectedGroup.matricule }}</p>
            </div>

            <!-- Qualification and Certificates list -->
            <div class="space-y-3">
              <h3 class="text-xs font-semibold text-gray-700">Historique des Formations de Poste</h3>
              <div class="space-y-2 max-h-[40vh] overflow-y-auto pr-1">
                <div v-for="f in selectedGroup.formations" :key="f.id" class="border rounded-lg p-3 flex justify-between items-center bg-gray-50/50 hover:bg-gray-50 transition-colors">
                  <div>
                    <h4 class="text-xs font-bold text-gray-800">{{ f.workstationName }}</h4>
                    <p class="text-[10px] text-gray-400 mt-0.5">Début: {{ formatDate(f.startDate) }} · Niveau ciblé: {{ formatNiveau(f.targetLevel) }}</p>
                  </div>
                  <div class="flex items-center gap-2">
                    <span class="px-2 py-0.5 rounded text-[9px] font-semibold" :class="opStatusClass(f.status)">
                      {{ opStatusLabel(f.status) }}
                    </span>
                    <router-link :to="'/training/' + f.id" class="text-sky-600 hover:text-sky-700 text-xs font-semibold">Détails</router-link>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </template>
        
      </div>
    </div>

    <!-- Alerts & Retards tab panel -->
    <div v-if="currentWorkflowTab === 'retard' && canSeeRetard" class="space-y-4">
      <div v-if="overdueFormations.length" class="rounded-xl border border-red-200 bg-red-50/50 shadow-sm p-4">
        <h3 class="text-sm font-semibold text-red-800 mb-3 flex items-center gap-2">
          <svg class="w-4.5 h-4.5 text-red-600 animate-bounce" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" /></svg>
          Formations Actives en Retard (> 14 jours d'inactivité)
        </h3>
        <div class="overflow-x-auto border border-red-100 rounded-lg bg-white">
          <table class="w-full text-xs text-gray-700">
            <thead class="bg-red-50/70 text-red-800">
              <tr>
                <th class="px-4 py-2 text-left font-medium text-red-800 text-xs">Opérateur</th>
                <th class="px-4 py-2 text-left font-medium text-red-800 text-xs">Matricule</th>
                <th class="px-4 py-2 text-left font-medium text-red-800 text-xs">Poste</th>
                <th class="px-4 py-2 text-center font-medium text-red-800 text-xs">Jours Remplis</th>
                <th class="px-4 py-2 text-left font-medium text-red-800 text-xs">Démarrée le</th>
                <th class="px-4 py-2 text-right font-medium text-red-800 text-xs">Action</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-red-100">
              <tr v-for="f in overdueFormations" :key="f.id" class="hover:bg-red-50/30 text-xs">
                <td class="px-4 py-2.5 font-semibold text-gray-900">{{ f.operatorName }}</td>
                <td class="px-4 py-2.5 font-mono text-gray-500">{{ f.operatorEmployeeId }}</td>
                <td class="px-4 py-2.5"><span class="bg-gray-100 text-gray-700 px-2 py-0.5 rounded font-medium">{{ f.workstationName }}</span></td>
                <td class="px-4 py-2.5 text-center font-semibold text-red-600">{{ f.daysWithData ?? 0 }} / 12</td>
                <td class="px-4 py-2.5 text-gray-500">{{ f.startDate }}</td>
                <td class="px-4 py-2.5 text-right"><router-link :to="'/training/' + f.id" class="text-sky-600 hover:underline font-semibold">Reprendre le suivi</router-link></td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div v-if="evalEnRetard.length" class="rounded-xl border border-amber-200 bg-amber-50/50 shadow-sm p-4">
        <h3 class="text-sm font-semibold text-amber-800 mb-3 flex items-center gap-2">
          <svg class="w-4.5 h-4.5 text-amber-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
          Évaluations Finales en Retard (12 jours complétés, en attente de décision)
        </h3>
        <div class="overflow-x-auto border border-amber-100 rounded-lg bg-white">
          <table class="w-full text-xs text-gray-700">
            <thead class="bg-amber-50/70 text-amber-800">
              <tr>
                <th class="px-4 py-2 text-left font-medium text-amber-800 text-xs">Opérateur</th>
                <th class="px-4 py-2 text-left font-medium text-amber-800 text-xs">Poste</th>
                <th class="px-4 py-2 text-center font-medium text-amber-800 text-xs">Score / Décision</th>
                <th class="px-4 py-2 text-right font-medium text-amber-800 text-xs">Action</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-amber-100">
              <tr v-for="e in evalEnRetard" :key="e.id" class="hover:bg-amber-50/30 text-xs">
                <td class="px-4 py-2.5 font-semibold text-gray-900">{{ e.operator?.name || e.operatorName }}</td>
                <td class="px-4 py-2.5"><span class="bg-gray-100 text-gray-700 px-2 py-0.5 rounded font-medium">{{ e.workstationName || '-' }}</span></td>
                <td class="px-4 py-2.5 text-center text-amber-600 font-semibold">En attente de validation</td>
                <td class="px-4 py-2.5 text-right"><router-link :to="'/evaluation/session/' + e.id" class="text-sky-600 hover:underline font-semibold">Évaluer maintenant</router-link></td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
      <div v-if="!overdueFormations.length && !evalEnRetard.length" class="py-16 text-center text-gray-400">
        Aucun opérateur trouvé
      </div>
    </div>

    <!-- ===== MODALS (unchanged) ===== -->
    <div v-if="showCreate" class="fixed inset-0 z-50 flex items-center justify-center bg-black/50" @click.self="showCreate = false">
      <form class="mx-4 w-full max-w-lg space-y-4 rounded-2xl bg-white p-6 shadow-xl" @submit.prevent="createFormation">
        <div><h2 class="text-lg font-semibold">Démarrer une formation pratique</h2><p class="mt-1 text-sm text-gray-500">Choisissez le projet, la zone et le poste pour cet opérateur.</p></div>
        <div><label class="mb-1 block text-sm font-medium">Projet</label><select v-model="form.projectId" required class="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm" @change="form.zoneId = ''; form.workstationId = ''"><option value="">Sélectionner</option><option v-for="project in availableStructure" :key="project.id" :value="project.id">{{ project.name }}</option></select></div>
        <div><label class="mb-1 block text-sm font-medium">Zone</label><select v-model="form.zoneId" required :disabled="!form.projectId" class="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm" @change="form.workstationId = ''"><option value="">Sélectionner</option><option v-for="zone in availableZones" :key="zone.id" :value="zone.id">{{ zone.name }}</option></select></div>
        <div><label class="mb-1 block text-sm font-medium">Poste de travail</label><select v-model="form.workstationId" required :disabled="!form.zoneId" class="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm"><option value="">Sélectionner</option><option v-for="workstation in availableWorkstations" :key="workstation.id" :value="workstation.id">{{ workstation.name }}</option></select></div>
        <div class="flex justify-end gap-3 pt-2"><button type="button" @click="showCreate = false" class="px-4 py-2 text-sm text-gray-600">Annuler</button><button type="submit" :disabled="creating" class="rounded-lg bg-sky-600 hover:bg-sky-700 px-4 py-2 text-sm text-white disabled:opacity-50">Démarrer</button></div>
      </form>
    </div>

    <div v-if="showDailyBatch" class="fixed inset-0 z-50 flex items-center justify-center bg-black/50" @click.self="showDailyBatch = false">
      <form class="mx-4 max-h-[90vh] w-full max-w-4xl overflow-auto rounded-2xl bg-white p-6 shadow-xl" @submit.prevent="saveDailyBatch">
        <div class="mb-4"><h2 class="text-lg font-semibold">Saisie quotidienne groupée</h2><p class="text-sm text-gray-500">Le jour de suivi de chaque opérateur est calculé automatiquement (J = jours déjà saisis + 1). Vous pouvez le corriger si besoin.</p></div>
        <div v-if="isMultiProjectRole" class="mb-4 flex flex-wrap gap-2">
          <select v-model="batchFilterProject" class="px-2 py-1.5 border border-gray-200 rounded-lg text-xs" @change="batchFilterZone=''; batchFilterWorkstation=''">
            <option value="">Tous les projets</option>
            <option v-for="p in projectList" :key="p.id" :value="p.id">{{ p.name }}</option>
          </select>
          <select v-if="batchFilterProject" v-model="batchFilterZone" class="px-2 py-1.5 border border-gray-200 rounded-lg text-xs" @change="batchFilterWorkstation=''">
            <option value="">Toutes les zones</option>
            <option v-for="z in allZones" :key="z.id" :value="z.id">{{ z.name }}</option>
          </select>
          <select v-if="batchFilterZone" v-model="batchFilterWorkstation" class="px-2 py-1.5 border border-gray-200 rounded-lg text-xs">
            <option value="">Tous les postes</option>
            <option v-for="w in allWorkstationsList" :key="w.id" :value="w.id">{{ w.name }}</option>
          </select>
        </div>
        <table class="w-full text-sm">
          <thead class="bg-gray-50">
            <tr>
              <th class="px-3 py-2 text-left">Opérateur</th>
              <th class="px-3 py-2 text-left">Poste</th>
              <th class="px-3 py-2 text-left">Jour</th>
              <th v-if="canEditCadence" class="px-3 py-2 text-left">Cadence</th>
              <th v-if="canEditDefects" class="px-3 py-2 text-left">Défauts</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="formation in batchFilteredFormations" :key="formation.id" class="border-b">
              <td class="px-3 py-2">{{ formation.operatorName }}</td>
              <td class="px-3 py-2 text-gray-500">{{ formation.workstationName }}</td>
              <td class="px-3 py-2">
                <div class="flex items-center gap-1.5">
                  <span class="inline-flex h-7 w-10 items-center justify-center rounded-md bg-slate-50 text-xs font-bold text-slate-700 border border-slate-200">
                    J{{ batchEntries[formation.id]?.dayNumber }}
                  </span>
                  <input v-model.number="batchEntries[formation.id].dayNumber" type="number" min="1" max="12"
                    class="w-14 rounded border border-gray-300 px-1.5 py-1 text-xs" title="Corriger le jour si besoin">
                </div>
              </td>
              <td v-if="canEditCadence" class="px-3 py-2"><input v-model.number="batchEntries[formation.id].cadence" min="0" type="number" class="w-24 rounded border border-gray-300 px-2 py-1"></td>
              <td v-if="canEditDefects" class="px-3 py-2"><input v-model.number="batchEntries[formation.id].defauts" min="0" type="number" class="w-24 rounded border border-gray-300 px-2 py-1"></td>
            </tr>
            <tr v-if="batchFilteredFormations.length === 0">
              <td colspan="5" class="px-4 py-10 text-center">
                <div class="flex flex-col items-center gap-2">
                  <svg class="w-10 h-10 text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z"/></svg>
                  <p class="text-sm font-medium text-gray-500">Aucun opérateur en formation active</p>
                  <p class="text-xs text-gray-400">La saisie quotidienne est disponible dès qu'un opérateur est en cours de formation (J1–J12).</p>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
        <div class="mt-5 flex justify-end gap-3"><button type="button" @click="showDailyBatch = false" class="px-4 py-2 text-sm text-gray-600">Annuler</button><button :disabled="savingBatch" type="submit" class="rounded-lg bg-sky-600 hover:bg-sky-700 px-4 py-2 text-sm text-white disabled:opacity-50">Enregistrer la saisie</button></div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { trainingApi, operatorsApi, structureApi, evaluationApi } from '@/api/endpoints'
import onboardingApi from '@/api/onboarding'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()

function formatNiveau(level) {
  if (!level) return '-'
  const upper = String(level).toUpperCase().trim()
  if (upper === 'I' || upper === 'NIVEAU_1' || upper === '1') return 'I'
  if (upper === 'L' || upper === 'NIVEAU_2' || upper === '2') return 'L'
  if (upper === 'U' || upper === 'NIVEAU_3' || upper === '3') return 'U'
  return level
}

const route = useRoute()
const formations = ref([])
const eligibleOperators = ref([])
const availableStructure = ref([])
const allOperatorsData = ref([])
const teams = ref([])
const projects = ref([])
const selectedProject = ref('')
const loading = ref(true)
const creating = ref(false)
const savingBatch = ref(false)
const showCreate = ref(false)
const showDailyBatch = ref(false)
const search = ref('')
const eligibleSearch = ref('')
const eligibleTypeFilter = ref('ALL')
const activeTab = ref('ALL')
const error = ref('')

// Workflow Tabs and Pagination/Sorting state
const currentWorkflowTab = ref('active')

const mainCurrentPage = ref(1)
const mainPageSize = ref(15)
const mainSortBy = ref('operatorName')
const mainSortOrder = ref('asc')

const eligibleCurrentPage = ref(1)
const eligiblePageSize = ref(10)

const myChefMember = computed(() => {
  const empId = authStore.user?.employeeId
  for (const p of projects.value) {
    const m = p.members?.find(mem => mem.employeeId === empId)
    if (m) return m
  }
  return null
})

const myShift = computed(() => myChefMember.value?.shift || null)

const filteredEligibleOperators = computed(() => {
  let list = filterOperators(eligibleOperators.value)
  if (eligibleTypeFilter.value !== 'ALL') {
    list = list.filter(o => o.operatorType === eligibleTypeFilter.value)
  }
  if (eligibleSearch.value && eligibleSearch.value.trim()) {
    const q = eligibleSearch.value.trim().toLowerCase()
    list = list.filter(o => 
      `${o.lastName} ${o.firstName}`.toLowerCase().includes(q) ||
      (o.employeeId && o.employeeId.toLowerCase().includes(q))
    )
  }
  return list
})
const batchEntries = reactive({})
const form = reactive({ projectId: '', zoneId: '', workstationId: '', operatorIds: [] })
const expandedOperators = ref(new Set())
const batchFilterProject = ref('')
const batchFilterZone = ref('')
const batchFilterWorkstation = ref('')
const evalEnRetard = ref([])

// Multi-project role detection
const isMultiProjectRole = computed(() =>
  authStore.hasAnyRole(['RESP_QUALITE', 'AGENT_QUALITE', 'SUPERVISEUR', 'RESP_HSE', 'ADMIN', 'RH'])
)
const showProjectFilter = computed(() => isMultiProjectRole.value && projectList.value.length > 1)
const projectList = computed(() => projects.value.map(p => ({ id: p.id, name: p.name })).sort((a, b) => a.name.localeCompare(b.name)))
const selectedProjectName = computed(() => {
  if (!selectedProject.value) return ''
  return projects.value.find(p => p.id === Number(selectedProject.value))?.name || ''
})

// Get project names for a given operator ID - sourced directly from the
// operator's own project assignment (op.project).
const getProjectNamesForOperator = (operatorId) => {
  const op = allOperatorsData.value.find(o => o.id === operatorId)
  return op?.project ? [op.project.name] : []
}

const canEditCadence = computed(() => authStore.isChefEquipe)
const canEditDefects = computed(() => authStore.isAgentQualite)
const canContribute = computed(() => canEditCadence.value || canEditDefects.value)
const canAssignPost = computed(() => authStore.isChefEquipe || authStore.isAdmin || authStore.isSuperviseur)
import { useUserScope } from '@/composables/useUserScope'

const { loadUserProjects, filterOperators, filterFormations } = useUserScope()

const scopedFormations = computed(() => filterFormations(formations.value, allOperatorsData.value))

const inProgressFormations = computed(() => scopedFormations.value.filter(f => f.status === 'IN_PROGRESS'))
const inProgressCount = computed(() => scopedFormations.value.filter(f => f.status === 'IN_PROGRESS').length)
const completedCount = computed(() => scopedFormations.value.filter(f => f.status === 'COMPLETED').length)
const failedCount = computed(() => scopedFormations.value.filter(f => f.status === 'FAILED').length)
const selectedFormProject = computed(() => availableStructure.value.find(p => p.id === form.projectId))
const availableZones = computed(() => selectedFormProject.value?.zones || [])
const selectedZone = computed(() => availableZones.value.find(z => z.id === form.zoneId))
const availableWorkstations = computed(() => selectedZone.value?.workstations || [])

// Can see retard section
const canSeeRetard = computed(() =>
  authStore.hasAnyRole(['RH', 'SUPERVISEUR', 'CHEF_EQUIPE', 'AGENT_QUALITE', 'RESP_QUALITE', 'ADMIN'])
)

// Overdue formations (IN_PROGRESS for more than 14 days)
const overdueFormations = computed(() => {
  const twoWeeksAgo = new Date()
  twoWeeksAgo.setDate(twoWeeksAgo.getDate() - 14)
  return scopedFormations.value.filter(f => {
    if (f.status !== 'IN_PROGRESS') return false
    if (!f.startDate) return true
    return new Date(f.startDate) < twoWeeksAgo
  })
})

// Batch modal filter computeds
const allZones = computed(() => {
  if (!batchFilterProject.value) return []
  const p = projects.value.find(pr => pr.id === Number(batchFilterProject.value))
  return p?.zones || []
})
const allWorkstationsList = computed(() => {
  if (!batchFilterZone.value) return []
  const zone = allZones.value.find(z => z.id === Number(batchFilterZone.value))
  return zone?.workstations || []
})
const batchFilteredFormations = computed(() => {
  // Only formations that still have a day left to fill (< 12 days of data).
  // A formation with 12/12 days needs to be evaluated, not entered further.
  let result = inProgressFormations.value.filter(f => (f.daysWithData || 0) < 12)
  if (batchFilterWorkstation.value) {
    result = result.filter(f => f.workstationId === Number(batchFilterWorkstation.value))
  } else if (batchFilterZone.value) {
    const wsIds = new Set(allWorkstationsList.value.map(w => w.id))
    result = result.filter(f => wsIds.has(f.workstationId))
  } else if (batchFilterProject.value) {
    const zoneIds = new Set(allZones.value.map(z => z.id))
    // Get workstations in those zones
    const wsIds = new Set()
    for (const z of allZones.value) {
      for (const w of (z.workstations || [])) wsIds.add(w.id)
    }
    result = result.filter(f => wsIds.has(f.workstationId))
  }
  return result
})

// ===== GROUP FORMATIONS BY OPERATOR =====
const operatorGroups = computed(() => {
  const map = new Map()
  for (const f of scopedFormations.value) {
    if (!map.has(f.operatorId)) {
      map.set(f.operatorId, { operatorId: f.operatorId, operatorName: f.operatorName, matricule: f.operatorEmployeeId, formations: [], inProgress: 0, completed: 0, failed: 0, hasCompletedFormation: false, currentFormation: null })
    }
    const group = map.get(f.operatorId)
    group.formations.push(f)
    if (f.status === 'IN_PROGRESS') {
      group.inProgress++
      group.currentFormation = { workstationName: f.workstationName, daysWithData: f.daysWithData }
    }
    if (f.status === 'COMPLETED') { group.completed++; group.hasCompletedFormation = true }
    if (f.status === 'FAILED') group.failed++
  }
  return Array.from(map.values())
})

const pageTabs = computed(() => {
  const tabsList = [
    { label: '🚀 Suivis Actifs (J1-J12)', value: 'active', badge: inProgressCount.value },
    { label: '📋 Opérateurs Éligibles', value: 'eligible', badge: filteredEligibleOperators.value.length },
    { label: '📜 Certifications & Historique', value: 'history', badge: completedCount.value + failedCount.value }
  ]
  if (canSeeRetard.value && (overdueFormations.value.length > 0 || evalEnRetard.value.length > 0)) {
    tabsList.push({ label: '⚠️ Alertes & Retards', value: 'retard', badge: overdueFormations.value.length + evalEnRetard.value.length })
  }
  return tabsList
})

const filteredGroups = computed(() => {
  let groups = operatorGroups.value
  
  // Scoped by workflow tab
  if (currentWorkflowTab.value === 'active') {
    groups = groups.filter(o => o.inProgress > 0)
  } else if (currentWorkflowTab.value === 'history') {
    groups = groups.filter(o => o.completed > 0 || o.failed > 0)
  }
  
  // Project filter
  if (selectedProject.value) {
    const pid = Number(selectedProject.value)
    const pName = projects.value.find(p => p.id === pid)?.name
    if (pName) {
      groups = groups.filter(o => getProjectNamesForOperator(o.operatorId).includes(pName))
    }
  }

  // Search
  if (search.value) {
    const q = search.value.toLowerCase()
    groups = groups.filter(o => `${o.operatorName} ${o.matricule}`.toLowerCase().includes(q))
  }
  return groups
})

const sortedGroups = computed(() => {
  const result = [...filteredGroups.value]
  const field = mainSortBy.value
  const order = mainSortOrder.value === 'asc' ? 1 : -1
  
  result.sort((a, b) => {
    let valA = '', valB = ''
    if (field === 'operatorName') {
      valA = (a.operatorName || '').toLowerCase()
      valB = (b.operatorName || '').toLowerCase()
    } else if (field === 'matricule') {
      valA = (a.matricule || '').toLowerCase()
      valB = (b.matricule || '').toLowerCase()
    } else if (field === 'formationsCount') {
      const cntA = a.formations?.length || 0
      const cntB = b.formations?.length || 0
      return (cntA - cntB) * order
    } else if (field === 'currentWorkstation') {
      valA = (a.currentFormation?.workstationName || '').toLowerCase()
      valB = (b.currentFormation?.workstationName || '').toLowerCase()
    }
    
    if (valA < valB) return -1 * order
    if (valA > valB) return 1 * order
    return 0
  })
  return result
})

const paginatedGroups = computed(() => {
  const start = (mainCurrentPage.value - 1) * mainPageSize.value
  const end = start + mainPageSize.value
  return sortedGroups.value.slice(start, end)
})

const mainTotalPages = computed(() => {
  return Math.ceil(filteredGroups.value.length / mainPageSize.value) || 1
})

const handleSort = (field) => {
  if (mainSortBy.value === field) {
    mainSortOrder.value = mainSortOrder.value === 'asc' ? 'desc' : 'asc'
  } else {
    mainSortBy.value = field
    mainSortOrder.value = 'asc'
  }
}

// Reset page on filter changes
watch([search, selectedProject, currentWorkflowTab, mainPageSize], () => {
  mainCurrentPage.value = 1
})

watch(() => route.query.operatorId, (newId) => {
  if (newId) {
    const opId = Number(newId)
    startFormation({ id: opId })
  }
})

// Paginated eligible operators
const paginatedEligibleOperators = computed(() => {
  const start = (eligibleCurrentPage.value - 1) * eligiblePageSize.value
  const end = start + eligiblePageSize.value
  return filteredEligibleOperators.value.slice(start, end)
})

const eligibleTotalPages = computed(() => {
  return Math.ceil(filteredEligibleOperators.value.length / eligiblePageSize.value) || 1
})

watch([eligibleSearch, eligibleTypeFilter, eligiblePageSize], () => {
  eligibleCurrentPage.value = 1
})

const toggleOperator = (id) => {
  if (expandedOperators.value.has(id)) expandedOperators.value.delete(id)
  else expandedOperators.value.add(id)
  expandedOperators.value = new Set(expandedOperators.value)
}

const statusLabel = s => ({ IN_PROGRESS: 'En cours', COMPLETED: 'Réussie', FAILED: 'Échouée' })[s] || s
const statusClass = s => ({ IN_PROGRESS: 'bg-amber-100 text-amber-700', COMPLETED: 'bg-emerald-100 text-emerald-700', FAILED: 'bg-red-100 text-red-700' })[s] || 'bg-gray-100 text-gray-600'

const load = async () => {
  error.value = ''
  const [formationsResponse, operatorsResponse] = await Promise.all([
    trainingApi.getFormations(), operatorsApi.getActive(),
  ])
  formations.value = formationsResponse.data
  if (canContribute.value || canAssignPost.value) {
    availableStructure.value = (await trainingApi.getAvailableStructure()).data
  } else {
    availableStructure.value = []
  }
  const completion = (await onboardingApi.batchCheckComplete(operatorsResponse.data.map(operator => operator.id))).data
  const operatorsInProgress = new Set(
    formations.value.filter(f => f.status === 'IN_PROGRESS').map(f => f.operatorId)
  )
  eligibleOperators.value = operatorsResponse.data.filter(
    operator => completion[operator.id] && !operatorsInProgress.has(operator.id)
  )
  // Fetch project/teams/operator data for project & shift filtering
  const [projRes, teamsRes, opsRes] = await Promise.allSettled([
    structureApi.getAll(),
    structureApi.getTeams(),
    operatorsApi.getAll(),
  ])
  if (projRes.status === 'fulfilled') projects.value = projRes.value.data || []
  if (teamsRes.status === 'fulfilled') teams.value = teamsRes.value.data || []
  if (opsRes.status === 'fulfilled') allOperatorsData.value = opsRes.value.data || []
  // Load evaluation sessions to check for overdue evaluations
  if (canSeeRetard.value) {
    try {
      const evalRes = await evaluationApi.getHistory()
      const completedFormations = new Set(formations.value.filter(f => f.status === 'COMPLETED').map(f => f.operatorId + '-' + f.workstationId))
     const evalData = Array.isArray(evalRes.data) ? evalRes.data : (evalRes.data?.content || evalRes.data?.items || [])
evalEnRetard.value = evalData.filter(s =>
  s.status === 'COMPLETED' && !completedFormations.has((s.operatorId || s.operator?.id) + '-' + (s.formationId || ''))
)
    } catch (e) {
      console.error('Error loading eval retard', e)
    }
  }
}

const createFormation = async () => {
  creating.value = true; error.value = ''
  try {
    await trainingApi.createFormations(form.workstationId, form.operatorIds)
    showCreate.value = false
    form.projectId = ''; form.zoneId = ''; form.workstationId = ''; form.operatorIds = []
    await load()
  } catch (e) {
    error.value = e.response?.data?.message || 'Impossible de créer la formation.'
  } finally { creating.value = false }
}

const startFormation = operator => {
  form.projectId = ''; form.zoneId = ''; form.workstationId = ''
  form.operatorIds = [operator.id]
  showCreate.value = true
}

const openDailyBatch = () => {
  // FIX: each formation now gets its own next day-to-fill (daysWithData + 1),
  // instead of one global "batchDay" being forced onto every operator - which
  // used to overwrite/repeat the wrong day for anyone not on day 1.
  for (const f of batchFilteredFormations.value) {
    batchEntries[f.id] = { cadence: null, defauts: null, dayNumber: Math.min((f.daysWithData || 0) + 1, 12) }
  }
  showDailyBatch.value = true
}

const saveDailyBatch = async () => {
  const entries = batchFilteredFormations.value
    .map(f => ({
      formationId: f.id, dayNumber: batchEntries[f.id].dayNumber,
      trackingDate: new Date().toISOString().slice(0, 10),
      ...(canEditCadence.value && batchEntries[f.id].cadence !== null ? { cadence: batchEntries[f.id].cadence } : {}),
      ...(canEditDefects.value && batchEntries[f.id].defauts !== null ? { defauts: batchEntries[f.id].defauts } : {}),
    }))
    .filter(e => e.cadence !== undefined || e.defauts !== undefined)
  if (!entries.length) return
  savingBatch.value = true; error.value = ''
  try {
    await trainingApi.saveDailyBatch(entries)
    showDailyBatch.value = false
    await load()
  } catch (e) {
    error.value = e.response?.data?.message || 'Impossible d\'enregistrer la saisie.'
  } finally { savingBatch.value = false }
}

const selectedOperatorId = ref(null)

const selectedGroup = computed(() => {
  const list = filteredGroups.value
  if (!selectedOperatorId.value && list.length > 0) {
    return list[0]
  }
  return list.find(o => o.operatorId === selectedOperatorId.value) || null
})

const selectedEligibleOperator = computed(() => {
  const list = paginatedEligibleOperators.value
  if (!selectedOperatorId.value && list.length > 0) {
    return list[0]
  }
  return list.find(o => o.id === selectedOperatorId.value) || null
})

watch(currentWorkflowTab, () => {
  selectedOperatorId.value = null
})

const singleEntry = reactive({ cadence: null, defauts: null, dayNumber: 1 })
const savingSingle = ref(false)

watch(() => selectedGroup.value, (newGroup) => {
  if (newGroup && newGroup.currentFormation) {
    singleEntry.dayNumber = Math.min((newGroup.currentFormation.daysWithData || 0) + 1, 12)
    singleEntry.cadence = null
    singleEntry.defauts = null
  } else {
    singleEntry.dayNumber = 1
    singleEntry.cadence = null
    singleEntry.defauts = null
  }
})

const saveSingleDay = async (formation) => {
  if (singleEntry.cadence === null && singleEntry.defauts === null) return
  savingSingle.value = true
  error.value = ''
  try {
    const payload = [{
      formationId: formation.id,
      dayNumber: singleEntry.dayNumber,
      trackingDate: new Date().toISOString().slice(0, 10),
      ...(canEditCadence.value && singleEntry.cadence !== null ? { cadence: singleEntry.cadence } : {}),
      ...(canEditDefects.value && singleEntry.defauts !== null ? { defauts: singleEntry.defauts } : {}),
    }]
    await trainingApi.saveDailyBatch(payload)
    singleEntry.cadence = null
    singleEntry.defauts = null
    await load()
  } catch (e) {
    error.value = e.response?.data?.message || "Impossible d'enregistrer la saisie."
  } finally {
    savingSingle.value = false
  }
}

const assignOperatorWorkstation = async (op) => {
  form.operatorIds = [op.id]
  creating.value = true; error.value = ''
  try {
    await trainingApi.createFormations(form.workstationId, form.operatorIds)
    form.projectId = ''; form.zoneId = ''; form.workstationId = ''; form.operatorIds = []
    await load()
  } catch (e) {
    error.value = e.response?.data?.message || "Impossible d'affecter l'opérateur."
  } finally {
    creating.value = false
  }
}

onMounted(async () => {
  try {
    await loadUserProjects()
    await load()
    if (route.query.operatorId) {
      const opId = Number(route.query.operatorId)
      startFormation({ id: opId })
    }
  } catch (e) {
    error.value = e.response?.data?.message || 'Impossible de charger les formations.'
  } finally { loading.value = false }
})
</script>
