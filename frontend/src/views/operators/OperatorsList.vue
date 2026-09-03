<template>
  <div class="space-y-6">
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">Opérateurs</h1>
        <p class="text-gray-500 mt-1">Gestion des opérateurs de l'usine</p>
      </div>
      <div class="flex items-center gap-2 flex-wrap">
        <button
          @click="exportOperatorsToExcel"
          class="inline-flex items-center gap-2 bg-blue-600 hover:bg-blue-700 text-white px-4 py-2.5 rounded-lg text-sm font-medium transition-colors shadow-sm"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4"/></svg>
          Exporter List
        </button>
        <button
          @click="openImportModal"
          class="inline-flex items-center gap-2 bg-teal-600 hover:bg-teal-700 text-white px-4 py-2.5 rounded-lg text-sm font-medium transition-colors shadow-sm"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-8l-4-4m0 0L8 8m4-4v12"/></svg>
          Importer Excel
        </button>
        <button
          @click="openCreateModal"
          class="inline-flex items-center gap-2 bg-sky-600 hover:bg-sky-700 text-white px-4 py-2.5 rounded-lg text-sm font-medium transition-colors shadow-sm"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"></path>
          </svg>
          Nouvel Opérateur
        </button>
      </div>
    </div>
    <!-- Stats Cards Summary (Quick Overview) -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
      <div class="bg-white rounded-xl p-4 border border-gray-200 shadow-sm flex items-center justify-between">
        <div>
          <p class="text-xs text-gray-500 font-medium">Opérateurs Actifs</p>
          <p class="text-xl font-bold text-emerald-600 mt-0.5">{{ totalActiveCount }}</p>
        </div>
        <div class="w-10 h-10 rounded-full bg-emerald-50 flex items-center justify-center text-emerald-600">
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4M7.835 4.697a3.42 3.42 0 001.946-.806 3.42 3.42 0 014.438 0 3.42 3.42 0 001.946.806 3.42 3.42 0 013.138 3.138 3.42 3.42 0 00.806 1.946 3.42 3.42 0 010 4.438 3.42 3.42 0 00-.806 1.946 3.42 3.42 0 01-3.138 3.138 3.42 3.42 0 00-1.946.806 3.42 3.42 0 01-4.438 0 3.42 3.42 0 00-1.946-.806 3.42 3.42 0 01-3.138-3.138 3.42 3.42 0 00-.806-1.946 3.42 3.42 0 010-4.438 3.42 3.42 0 00.806-1.946 3.42 3.42 0 013.138-3.138z"/></svg>
        </div>
      </div>
      <div class="bg-white rounded-xl p-4 border border-gray-200 shadow-sm flex items-center justify-between">
        <div>
          <p class="text-xs text-gray-500 font-medium">Nouvelles Recrues</p>
          <p class="text-xl font-bold text-purple-600 mt-0.5">{{ totalNewRecruits }}</p>
        </div>
        <div class="w-10 h-10 rounded-full bg-purple-50 flex items-center justify-center text-purple-600">
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M18.364 5.636l-3.536 3.536m0 5.656l3.536 3.536M9.172 9.172L5.636 5.636m3.536 9.192l-3.536 3.536M21 12a9 9 0 11-18 0 9 9 0 0118 0zm-5 0a4 4 0 11-8 0 4 4 0 018 0z"/></svg>
        </div>
      </div>
      <div class="bg-white rounded-xl p-4 border border-gray-200 shadow-sm flex items-center justify-between">
        <div>
          <p class="text-xs text-gray-500 font-medium">Inactifs / Départs</p>
          <p class="text-xl font-bold text-red-600 mt-0.5">{{ totalInactives }}</p>
        </div>
        <div class="w-10 h-10 rounded-full bg-red-50 flex items-center justify-center text-red-600">
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"/></svg>
        </div>
      </div>
    </div>

    <!-- Main Tab Navigation -->
    <div class="border-b border-gray-200">
      <nav class="-mb-px flex gap-6">
        <button
          @click="activeMainTab = 'directory'"
          :class="[
            activeMainTab === 'directory'
              ? 'border-sky-600 text-sky-600 font-bold'
              : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300 font-medium',
            'py-3 px-1 border-b-2 text-sm transition-colors flex items-center gap-2'
          ]"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z"/></svg>
          Annuaire des Opérateurs
        </button>
        <button
          @click="activeMainTab = 'assignment'"
          :class="[
            activeMainTab === 'assignment'
              ? 'border-emerald-600 text-emerald-600 font-bold'
              : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300 font-medium',
            'py-3 px-1 border-b-2 text-sm transition-colors flex items-center gap-2'
          ]"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z"/></svg>
          Affectation à mon Équipe (par Chef d'Équipe)
        </button>
      </nav>
    </div>

    <!-- TAB 1: Master-Detail Split Workspace -->
    <div v-if="activeMainTab === 'directory'" class="grid grid-cols-1 lg:grid-cols-5 gap-6 items-start">
      <!-- Left Pane: Operator Directory (Annuaire) - 2/5 width -->
      <div class="lg:col-span-2 bg-white rounded-xl border border-gray-200 shadow-sm flex flex-col h-[70vh]">
        <!-- Search bar inside left pane -->
        <div class="p-3 border-b border-gray-100 space-y-2">
          <div class="relative">
            <svg class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path></svg>
            <input
              v-model="search"
              type="text"
              placeholder="Rechercher un opérateur..."
              class="w-full pl-9 pr-3 py-1.5 border border-gray-200 rounded-lg text-xs outline-none focus:ring-2 focus:ring-sky-500"
            />
          </div>
          <div class="flex gap-2">
            <select v-model="selectedProjectFilter" class="w-1/2 px-2 py-1 border border-gray-200 rounded text-[11px] outline-none">
              <option value="">Tous les projets</option>
              <option v-for="p in projectList" :key="p.id" :value="p.id">{{ p.name }}</option>
            </select>
            <select v-model="selectedTeamFilter" class="w-1/2 px-2 py-1 border border-gray-200 rounded text-[11px] outline-none">
              <option value="">Toutes les équipes</option>
              <option v-for="t in teamList" :key="t.id" :value="t.id">{{ t.name }}</option>
            </select>
          </div>
        </div>

        <!-- Directory list -->
        <div v-if="loading" class="flex items-center justify-center flex-1">
          <div class="w-6 h-6 border-2 border-sky-200 border-t-sky-600 rounded-full animate-spin"></div>
        </div>
        <div v-else-if="!paginatedOperators.length" class="p-8 text-center text-xs text-gray-400 flex-1">
          Aucun opérateur trouvé
        </div>
        <div v-else class="flex-1 overflow-y-auto divide-y divide-gray-50">
          <div
            v-for="op in paginatedOperators"
            :key="op.id"
            @click="selectedOperatorId = op.id"
            class="p-3 flex items-center justify-between cursor-pointer transition-colors"
            :class="selectedOperatorId === op.id ? 'bg-sky-50/70 border-l-4 border-sky-600 pl-2' : 'hover:bg-gray-50 border-l-4 border-transparent'"
          >
            <div class="flex items-center gap-2.5">
              <div class="w-9 h-9 rounded-full bg-sky-100 flex items-center justify-center font-bold text-xs text-sky-700 flex-shrink-0">
                {{ (op.lastName || '')[0] }}{{ (op.firstName || '')[0] }}
              </div>
              <div class="min-w-0">
                <p class="text-xs font-semibold text-gray-800 truncate">{{ op.lastName }} {{ op.firstName }}</p>
                <p class="text-[10px] text-gray-400 font-mono mt-0.5">{{ op.employeeId }} · {{ op.team?.name || 'Sans équipe' }}</p>
              </div>
            </div>
            <div class="flex items-center gap-1.5 flex-shrink-0">
              <span class="w-2 h-2 rounded-full" :class="op.active !== false ? 'bg-emerald-500' : 'bg-red-400'"></span>
              <span class="text-[10px] bg-gray-100 text-gray-600 px-1.5 py-0.5 rounded border">{{ op.project?.name || '-' }}</span>
            </div>
          </div>
        </div>

        <!-- Directory Footer controls -->
        <div class="p-2 border-t border-gray-100 flex justify-between items-center text-[10px] text-gray-500 bg-gray-50/50">
          <span>Page {{ currentPage }} / {{ totalPages }}</span>
          <div class="flex gap-1">
            <button :disabled="currentPage === 1" @click="currentPage--" class="px-2.5 py-0.5 bg-white border rounded disabled:opacity-50 font-semibold">Préc.</button>
            <button :disabled="currentPage === totalPages" @click="currentPage++" class="px-2.5 py-0.5 bg-white border rounded disabled:opacity-50 font-semibold">Suiv.</button>
          </div>
        </div>
      </div>

      <!-- Right Pane: Contextual Profile Details & Workspace - 3/5 width -->
      <div class="lg:col-span-3 bg-white rounded-xl border border-gray-200 shadow-sm p-6 overflow-y-auto h-[70vh] flex flex-col justify-between">
        <div v-if="!selectedOperator" class="flex flex-col items-center justify-center text-center py-20 text-gray-400 flex-1">
          <svg class="w-12 h-12 text-gray-300 mb-3" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"/></svg>
          <p class="text-sm font-semibold">Sélectionnez un opérateur</p>
          <p class="text-xs mt-1">Choisissez un opérateur dans la liste de gauche pour afficher son profil complet et ses actions.</p>
        </div>
        <div v-else class="space-y-6 flex-1">
          <!-- Profile header -->
          <div class="flex items-center justify-between gap-4 pb-4 border-b">
            <div class="flex items-center gap-3">
              <div class="w-12 h-12 rounded-full bg-sky-600 flex items-center justify-center text-white font-bold text-base shadow-sm">
                {{ (selectedOperator.lastName || '')[0] }}{{ (selectedOperator.firstName || '')[0] }}
              </div>
              <div>
                <h2 class="text-lg font-bold text-gray-900 leading-tight">{{ selectedOperator.lastName }} {{ selectedOperator.firstName }}</h2>
                <p class="text-xs text-gray-500 font-medium">Matricule: <span class="font-mono">{{ selectedOperator.employeeId }}</span> · Équipe: {{ selectedOperator.team?.name || 'Aucune' }}</p>
              </div>
            </div>
            <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold" :class="selectedOperator.active !== false ? 'bg-emerald-50 text-emerald-700 border border-emerald-200' : 'bg-red-50 text-red-700 border border-red-200'">
              {{ selectedOperator.active !== false ? 'Actif' : 'Inactif' }}
            </span>
          </div>

          <!-- Quick action panel -->
          <div class="bg-gray-50 rounded-xl p-4 border border-gray-100 flex flex-wrap items-center gap-2">
            <button
              v-if="selectedOperator.active !== false && auth.hasAnyRole(['CHEF_EQUIPE', 'SUPERVISEUR', 'ADMIN', 'RH', 'AGENT_QUALITE']) && hasValidFormations"
              @click="$router.push('/training?operatorId=' + selectedOperator.id)"
              class="inline-flex items-center gap-1.5 px-3.5 py-2 bg-emerald-600 hover:bg-emerald-700 text-white text-xs font-bold rounded-lg shadow-md transition transform hover:scale-105"
            >
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"/></svg>
              Affecter à un nouveau poste
            </button>
            <div
              v-else-if="selectedOperator.active !== false && !hasValidFormations"
              class="inline-flex items-center gap-1.5 text-xs text-amber-700 bg-amber-50 border border-amber-200 px-3 py-1.5 rounded-lg font-medium"
            >
              <svg class="w-4 h-4 text-amber-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/></svg>
              <span>Formation initiale requise avant nouvelle affectation</span>
            </div>
            <button
              v-if="auth.hasAnyRole(['ADMIN', 'RH'])"
              @click="openEditModal(selectedOperator)"
              class="inline-flex items-center gap-1.5 px-3 py-1.5 bg-white border border-gray-200 text-gray-700 hover:bg-gray-50 text-xs font-semibold rounded-lg shadow-sm transition"
            >
              <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"/></svg>
              Modifier
            </button>
            <button
              v-if="selectedOperator.active !== false && auth.hasAnyRole(['ADMIN', 'RH'])"
              @click="deactivateOperator(selectedOperator.id)"
              class="inline-flex items-center gap-1.5 px-3 py-1.5 bg-white border border-red-200 text-red-600 hover:bg-red-50 text-xs font-semibold rounded-lg shadow-sm transition"
            >
              <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M18.364 18.364A9 9 0 005.636 5.636m12.728 12.728A9 9 0 015.636 5.636m12.728 12.728L5.636 5.636"/></svg>
              Désactiver
            </button>
            <button
              v-if="selectedOperator.active === false && auth.hasAnyRole(['ADMIN', 'RH'])"
              @click="activateOperator(selectedOperator.id)"
              class="inline-flex items-center gap-1.5 px-3 py-1.5 bg-emerald-600 hover:bg-emerald-700 text-white text-xs font-semibold rounded-lg shadow-sm transition"
            >
              <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"/></svg>
              Activer
            </button>
            <button
              v-if="auth.hasAnyRole(['ADMIN', 'RH'])"
              @click="deletePermanently(selectedOperator)"
              class="inline-flex items-center gap-1.5 px-3 py-1.5 bg-red-50 border border-red-200 text-red-700 hover:bg-red-100 text-xs font-semibold rounded-lg shadow-sm transition"
              title="Supprimer définitivement l'opérateur de la base de données"
            >
              <svg class="w-3.5 h-3.5 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"/></svg>
              Supprimer définitivement
            </button>
          </div>

          <!-- Profile details -->
          <div class="grid grid-cols-2 gap-4 text-xs">
            <div class="border rounded-lg p-3">
              <span class="text-gray-400 font-medium">Type d'opérateur</span>
              <p class="font-semibold text-gray-800 mt-1">{{ selectedOperator.operatorType === 'DEJA_EN_POSTE' ? 'Déjà en poste' : 'Nouvelle recrue' }}</p>
            </div>
            <div class="border rounded-lg p-3">
              <span class="text-gray-400 font-medium">Rôle</span>
              <p class="font-semibold text-gray-800 mt-1">{{ selectedOperator.role || 'Opérateur' }}</p>
            </div>
            <div class="border rounded-lg p-3">
              <span class="text-gray-400 font-medium">Projet affecté</span>
              <p class="font-semibold text-sky-700 mt-1">{{ selectedOperator.project?.name || '-' }}</p>
            </div>
            <div class="border rounded-lg p-3">
              <span class="text-gray-400 font-medium">Zone</span>
              <p class="font-semibold text-gray-800 mt-1">{{ selectedOperator.zone?.name || '-' }}</p>
            </div>
            <div class="border rounded-lg p-3">
              <span class="text-gray-400 font-medium">Date d'embauche</span>
              <p class="font-semibold text-gray-800 mt-1">{{ formatDate(selectedOperator.hireDate) }}</p>
            </div>
            <div class="border rounded-lg p-3" v-if="selectedOperator.exitDate">
              <span class="text-red-500 font-medium">Date de sortie</span>
              <p class="font-semibold text-red-700 mt-1">{{ formatDate(selectedOperator.exitDate) }}</p>
            </div>
          </div>

          <!-- Training qualifications timeline -->
          <div class="space-y-3">
            <h3 class="font-semibold text-gray-900 text-xs border-b pb-1">Formations & Certifications</h3>
            <div v-if="formationsLoading" class="flex justify-center py-6">
              <div class="w-6 h-6 border-2 border-emerald-200 border-t-emerald-600 rounded-full animate-spin"></div>
            </div>
            <div v-else-if="!selectedOperatorFormations.length" class="text-center py-6 text-xs text-gray-400">
              Aucun suivi de formation disponible
            </div>
            <div v-else class="space-y-2 max-h-[25vh] overflow-y-auto pr-1">
              <div
                v-for="f in selectedOperatorFormations"
                :key="f.id"
                class="flex items-center justify-between border border-gray-100 rounded-lg p-2.5 hover:bg-gray-50 transition-colors"
              >
                <div>
                  <p class="text-xs font-semibold text-gray-800">{{ f.workstationName }}</p>
                  <p class="text-[10px] text-gray-400 mt-0.5">Niveau cible: <span class="font-bold">{{ formatNiveau(f.targetLevel) }}</span> · Début: {{ formatDate(f.startDate) }}</p>
                </div>
                <div class="flex items-center gap-2">
                  <span class="px-2 py-0.5 rounded text-[10px] font-semibold" :class="opStatusClass(f.status)">
                    {{ opStatusLabel(f.status) }}
                  </span>
                  <router-link :to="'/training/' + f.id" class="text-sky-600 hover:text-sky-700 text-xs font-semibold">Détails</router-link>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- TAB 2: Affectation des Opérateurs par le Chef d'Équipe -->
    <div v-if="activeMainTab === 'assignment'" class="space-y-4">
      <!-- Success/Error Banner -->
      <div v-if="assignBannerMsg" class="rounded-xl p-4 text-sm font-medium border flex items-center justify-between transition shadow-sm"
        :class="assignBannerSuccess ? 'bg-emerald-50 text-emerald-800 border-emerald-200' : 'bg-red-50 text-red-800 border-red-200'">
        <div class="flex items-center gap-2">
          <span>{{ assignBannerMsg }}</span>
        </div>
        <button @click="assignBannerMsg = ''" class="text-xs text-gray-400 hover:text-gray-600">✕</button>
      </div>

      <!-- Controls & Search Header -->
      <div class="bg-white rounded-xl border border-gray-200 p-4 shadow-sm flex flex-col md:flex-row md:items-center justify-between gap-4">
        <!-- Search input by Nom, Prénom, or Matricule -->
        <div class="relative flex-1 max-w-md">
          <svg class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"/>
          </svg>
          <input
            v-model="assignSearch"
            type="text"
            placeholder="Rechercher par Nom, Prénom ou Matricule (ex: OP001, ALAMI)..."
            class="w-full pl-9 pr-3 py-2 border border-gray-200 rounded-lg text-sm outline-none focus:ring-2 focus:ring-emerald-500"
          />
        </div>

        <!-- Count Badge for Unassigned Operators -->
        <div class="flex items-center gap-2">
          <span class="px-3 py-1.5 rounded-lg text-xs font-bold bg-amber-100 text-amber-800 border border-amber-200 flex items-center gap-1.5">
            <svg class="w-4 h-4 text-amber-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"/></svg>
            {{ unassignedCount }} opérateur(s) non-affecté(s) à attribuer
          </span>
        </div>
      </div>

      <!-- Operators Assignment Grid -->
      <div v-if="filteredAssignOperators.length === 0" class="bg-white rounded-xl border border-gray-200 p-12 text-center text-gray-400 text-sm">
        <svg class="w-12 h-12 mx-auto mb-2 text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4"/></svg>
        Aucun opérateur correspondant trouvé pour cette recherche.
      </div>

      <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
        <div
          v-for="op in filteredAssignOperators"
          :key="op.id"
          class="bg-white rounded-xl border border-gray-200 p-4 shadow-sm hover:shadow-md transition flex flex-col justify-between"
        >
          <div>
            <div class="flex items-center justify-between mb-3">
              <div class="flex items-center gap-3">
                <div class="w-10 h-10 rounded-full bg-slate-100 border border-slate-200 flex items-center justify-center font-bold text-slate-700 text-sm">
                  {{ (op.firstName?.[0] || '') + (op.lastName?.[0] || '') }}
                </div>
                <div>
                  <h3 class="font-bold text-gray-900 text-sm leading-snug">{{ op.lastName }} {{ op.firstName }}</h3>
                  <span class="text-[11px] font-mono text-gray-500 bg-gray-100 px-1.5 py-0.5 rounded">{{ op.employeeId }}</span>
                </div>
              </div>
            </div>

            <div class="space-y-1 text-xs text-gray-600 mb-4 bg-slate-50 p-2.5 rounded-lg border border-slate-100">
              <div class="flex justify-between">
                <span class="text-gray-400">Statut Équipe:</span>
                <span v-if="isOpInMyTeam(op)" class="font-bold text-emerald-600">Mon Équipe</span>
                <span v-else-if="op.team" class="font-semibold text-slate-700">{{ op.team.teamLeader ? 'Équipe ' + op.team.teamLeader : op.team.name }}</span>
                <span v-else class="font-semibold text-amber-600 italic">Non assigné</span>
              </div>
              <div class="flex justify-between" v-if="op.project">
                <span class="text-gray-400">Projet:</span>
                <span class="font-medium text-gray-800">{{ op.project.name }}</span>
              </div>
            </div>
          </div>

          <!-- Action Button -->
          <div class="pt-2 border-t border-gray-100">
            <button
              v-if="isOpInMyTeam(op)"
              @click="removeFromMyTeam(op)"
              class="w-full py-2 bg-red-50 hover:bg-red-100 text-red-600 font-semibold rounded-lg text-xs transition border border-red-200 flex items-center justify-center gap-1.5"
            >
              <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"/></svg>
              Retirer de mon équipe
            </button>
            <button
              v-else
              @click="assignToMyTeam(op)"
              class="w-full py-2 bg-emerald-600 hover:bg-emerald-700 text-white font-semibold rounded-lg text-xs transition shadow-sm flex items-center justify-center gap-1.5"
            >
              <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z"/></svg>
              + Affecter à mon équipe
            </button>
          </div>
        </div>
      </div>
    </div>
    <div v-if="showCreateModal" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50" @click.self="showCreateModal = false">
      <div class="bg-white rounded-2xl shadow-xl w-full max-w-lg mx-4 p-6">
        <h2 class="text-lg font-semibold text-gray-900 mb-4">Nouvel Opérateur</h2>
        <form @submit.prevent="createOperator" class="space-y-4">
          <div class="grid grid-cols-2 gap-3">
            <div><label class="block text-sm font-medium text-gray-700 mb-1">Nom</label><input v-model="form.lastName" required class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-sky-500 outline-none" /></div>
            <div><label class="block text-sm font-medium text-gray-700 mb-1">Prénom</label><input v-model="form.firstName" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-sky-500 outline-none" /></div>
          </div>
          <div class="grid grid-cols-2 gap-3">
            <div><label class="block text-sm font-medium text-gray-700 mb-1">Matricule</label><input v-model="form.employeeId" required class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-sky-500 outline-none" /></div>
            <div><label class="block text-sm font-medium text-gray-700 mb-1">Rôle</label><input v-model="form.role" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-sky-500 outline-none" placeholder="Ex: Opérateur" /></div>
          </div>
          <div class="grid grid-cols-2 gap-3">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Type opérateur</label>
              <select v-model="form.operatorType" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-sky-500 outline-none">
                <option value="NOUVEAU_RECRU">Nouvelle recrue</option>
                <option value="DEJA_EN_POSTE">Déjà en poste</option>
              </select>
            </div>
            <div><label class="block text-sm font-medium text-gray-700 mb-1">Date d'embauche</label><input v-model="form.hireDate" type="date" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-sky-500 outline-none" /></div>
          </div>
          <div class="grid grid-cols-2 gap-3">
            <div><label class="block text-sm font-medium text-gray-700 mb-1">Date de sortie</label><input v-model="form.exitDate" type="date" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-sky-500 outline-none" /></div>
          </div>
          <hr class="border-gray-200" />
          <p class="text-sm font-medium text-gray-700">Affectation</p>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Projet</label>
            <select v-model="form.projectId" @change="form.zoneId = ''; form.workstationId = ''; form.teamId = ''" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-sky-500 outline-none">
              <option value="">-- Aucun --</option>
              <option v-for="p in useChefProjects ? chefProjects : projects" :key="p.id" :value="p.id">{{ p.name }}</option>
            </select>
          </div>
          <div v-if="!auth.isChefEquipe">
            <label class="block text-sm font-medium text-gray-700 mb-1">Équipe (Chef d'Équipe)</label>
            <select v-model="form.teamId" :disabled="!form.projectId" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-sky-500 outline-none disabled:bg-gray-100">
              <option value="">-- Choisir une équipe --</option>
              <option v-for="t in filteredTeams" :key="t.id" :value="t.id">{{ t.name }} (Chef: {{ t.teamLeader || 'Aucun' }})</option>
            </select>
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Zone</label>
            <select v-model="form.zoneId" @change="form.workstationId = ''" :disabled="!form.projectId" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-sky-500 outline-none disabled:bg-gray-100">
              <option value="" disabled>Choisir une zone</option>
              <option v-for="z in selectedProjectZones" :key="z.id" :value="z.id">{{ z.name }}</option>
            </select>
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Poste de travail</label>
            <select v-model="form.workstationId" :disabled="!form.zoneId" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-sky-500 outline-none disabled:bg-gray-100">
              <option value="">-- Aucun --</option>
              <option v-for="ws in selectedZoneWorkstations" :key="ws.id" :value="ws.id">{{ ws.name }} ({{ ws.type || '-' }})</option>
            </select>
          </div>
          <div v-if="error" class="bg-red-50 text-red-600 text-sm p-3 rounded-lg">{{ error }}</div>
          <div class="flex justify-end gap-3 pt-2">
            <button type="button" @click="showCreateModal = false" class="px-4 py-2 text-sm text-gray-600 hover:text-gray-800 transition">Annuler</button>
            <button type="submit" :disabled="creating" class="px-4 py-2 bg-sky-600 hover:bg-sky-700 text-white text-sm rounded-lg transition">Créer</button>
          </div>
        </form>
      </div>
    </div>

    <!-- Edit Operator Modal -->
    <div v-if="showEditModal" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50" @click.self="showEditModal = false">
      <div class="bg-white rounded-2xl shadow-xl w-full max-w-lg mx-4 p-6">
        <h2 class="text-lg font-semibold text-gray-900 mb-4">Modifier l'Opérateur</h2>
        <form @submit.prevent="updateOperator" class="space-y-4">
          <div class="grid grid-cols-2 gap-3">
            <div><label class="block text-sm font-medium text-gray-700 mb-1">Nom</label><input v-model="editForm.lastName" required class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-sky-500 outline-none" /></div>
            <div><label class="block text-sm font-medium text-gray-700 mb-1">Prénom</label><input v-model="editForm.firstName" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-sky-500 outline-none" /></div>
          </div>
          <div class="grid grid-cols-2 gap-3">
            <div><label class="block text-sm font-medium text-gray-700 mb-1">Matricule</label><input v-model="editForm.employeeId" required class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-sky-500 outline-none" /></div>
            <div><label class="block text-sm font-medium text-gray-700 mb-1">Rôle</label><input v-model="editForm.role" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-sky-500 outline-none" /></div>
          </div>
          <div class="grid grid-cols-2 gap-3">
            <div><label class="block text-sm font-medium text-gray-700 mb-1">Date d'embauche</label><input v-model="editForm.hireDate" type="date" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-sky-500 outline-none" /></div>
            <!-- FIX 4a: Date de sortie now saves properly -->
            <div><label class="block text-sm font-medium text-gray-700 mb-1">Date de sortie</label><input v-model="editForm.exitDate" type="date" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-sky-500 outline-none" /></div>
          </div>
          <div class="grid grid-cols-2 gap-3">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Type operateur</label>
              <select v-model="editForm.operatorType" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-sky-500 outline-none">
                <option value="NOUVEAU_RECRU">Nouvelle recrue</option>
                <option value="DEJA_EN_POSTE">Déjà en poste</option>
              </select>
            </div>
          </div>
          <hr class="border-gray-200" />
          <p class="text-sm font-medium text-gray-700">Affectation</p>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Projet</label>
            <select v-model="editForm.projectId" @change="editForm.zoneId = ''; editForm.teamId = ''" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-sky-500 outline-none">
              <option value="">-- Aucun --</option>
              <option v-for="p in projects" :key="p.id" :value="p.id">{{ p.name }}</option>
            </select>
          </div>
          <div v-if="!auth.isChefEquipe">
            <label class="block text-sm font-medium text-gray-700 mb-1">Équipe (Chef d'Équipe)</label>
            <select v-model="editForm.teamId" :disabled="!editForm.projectId" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-sky-500 outline-none disabled:bg-gray-100">
              <option value="">-- Choisir une équipe --</option>
              <option v-for="t in filteredEditTeams" :key="t.id" :value="t.id">{{ t.name }} (Chef: {{ t.teamLeader || 'Aucun' }})</option>
            </select>
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Zone</label>
            <select v-model="editForm.zoneId" :disabled="!editForm.projectId" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-sky-500 outline-none disabled:bg-gray-100">
              <option value="">-- Aucune --</option>
              <option v-for="z in editProjectZones" :key="z.id" :value="z.id">{{ z.name }}</option>
            </select>
          </div>
          <div v-if="error" class="bg-red-50 text-red-600 text-sm p-3 rounded-lg">{{ error }}</div>
          <div class="flex justify-end gap-3 pt-2">
            <button type="button" @click="showEditModal = false" class="px-4 py-2 text-sm text-gray-600 hover:text-gray-800">Annuler</button>
            <button type="submit" :disabled="creating" class="px-4 py-2 bg-sky-600 text-white text-sm rounded-lg hover:bg-sky-700">Enregistrer</button>
          </div>
        </form>
      </div>
    </div>

    <!-- Import Excel Modal -->
    <div v-if="showImportModal" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50" @click.self="closeImportModal">
      <div class="bg-white rounded-2xl shadow-xl w-full max-w-xl mx-4 p-6">
        <h2 class="text-lg font-semibold text-gray-900 mb-2">Importer depuis Excel</h2>
        <p class="text-xs text-gray-500 mb-4">
          Téléversez un fichier Excel. Il doit contenir au minimum les colonnes : 
          <strong class="text-gray-700">Nom, Prénom, Matricule</strong>.
          Optionnel : <strong class="text-gray-700">Rôle, Type d'opérateur, Date d'embauche</strong>.
        </p>

        <div class="space-y-4">
          <div>
            <label class="block text-xs font-semibold text-gray-700 mb-1">Si le Type d'opérateur n'est pas spécifié dans l'Excel :</label>
            <select v-model="defaultImportType" @change="reparseImportFile" class="w-full px-3 py-1.5 border border-gray-200 rounded-lg text-xs outline-none focus:ring-2 focus:ring-sky-500">
              <option value="DEJA_EN_POSTE">Importer comme Déjà en poste (par défaut)</option>
              <option value="NOUVEAU_RECRU">Importer comme Nouvelle recrue</option>
            </select>
          </div>

          <div class="border-2 border-dashed border-gray-200 rounded-lg p-6 text-center">
            <input type="file" ref="fileInput" accept=".xlsx, .xls" @change="handleFileChange" class="hidden" id="excel-file-upload" />
            <label for="excel-file-upload" class="cursor-pointer inline-flex flex-col items-center gap-2">
              <svg class="w-10 h-10 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 13h6m-3-3v6m-9 1V4a2 2 0 012-2h6l2 2h6a2 2 0 012 2v8a2 2 0 01-2 2H5a2 2 0 01-2-2z"/></svg>
              <span class="text-sm font-medium text-sky-600 hover:text-sky-700">Choisir un fichier Excel</span>
              <span class="text-xs text-gray-400 block mt-1" v-if="importFile">{{ importFile.name }}</span>
            </label>
          </div>

          <div v-if="parsedOperators.length > 0" class="max-h-48 overflow-y-auto border border-gray-100 rounded-lg p-2 bg-gray-50">
            <p class="text-xs font-semibold text-gray-600 mb-2">Opérateurs détectés ({{ parsedOperators.length }}) :</p>
            <ul class="text-xs space-y-1 divide-y divide-gray-100">
              <li v-for="(op, i) in parsedOperators" :key="i" class="py-1 flex justify-between">
                <span>{{ op.lastName }} {{ op.firstName }} ({{ op.employeeId }})</span>
                <span class="text-gray-400">{{ op.role || 'Opérateur' }} · {{ op.operatorType === 'DEJA_EN_POSTE' ? 'Déjà en poste' : 'Nouvelle recrue' }}</span>
              </li>
            </ul>
          </div>

          <div v-if="importError" class="bg-red-50 text-red-600 text-sm p-3 rounded-lg max-h-32 overflow-y-auto">{{ importError }}</div>
          <div v-if="importSuccess" class="bg-emerald-50 text-emerald-700 text-sm p-3 rounded-lg">{{ importSuccess }}</div>
        </div>

        <div class="flex justify-end gap-3 pt-4 border-t border-gray-100 mt-6">
          <button type="button" @click="closeImportModal" class="px-4 py-2 text-sm text-gray-600 hover:text-gray-800">Fermer</button>
          <button type="button" @click="submitImport" :disabled="parsedOperators.length === 0 || importing" class="px-4 py-2 bg-sky-600 text-white text-sm rounded-lg hover:bg-sky-700 disabled:opacity-50">
            {{ importing ? 'Importation...' : 'Importer' }}
          </button>
        </div>
      </div>
    </div>

    <ConfirmDialog :visible="confirmData.visible" :title="confirmData.title" :message="confirmData.message" :type="confirmData.type" @confirm="handleConfirm" @cancel="handleCancel" />
  </div>
</template>
<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { operatorsApi, structureApi, teamsApi } from '@/api/endpoints'
import { formatDate } from '@/shared/utils/date'
import ConfirmDialog from '@/components/ConfirmDialog.vue'
import { useAuthStore } from '@/stores/auth'

import * as XLSX from 'xlsx'

import { useUserScope } from '@/composables/useUserScope'

const auth = useAuthStore()
const { loadUserProjects, filterOperators, isRestrictedRole } = useUserScope()

const activeMainTab = ref('directory')
const assignSearch = ref('')
const assignFilterMode = ref('unassigned')
const assignBannerMsg = ref('')
const assignBannerSuccess = ref(true)

const operators = ref([])
const projects = ref([])
const teams = ref([])
const chefProjects = ref([])
const useChefProjects = ref(false)
const loading = ref(true)

const search = ref('')
const selectedProjectFilter = ref('')
const selectedTeamFilter = ref('')
const currentPage = ref(1)
const pageSize = ref(15)
const sortBy = ref('lastName')
const sortOrder = ref('asc')

const selectedOperatorId = ref(null)
const selectedOperatorFormations = ref([])
const formationsLoading = ref(false)

function formatNiveau(level) {
  if (!level) return '-'
  const upper = String(level).toUpperCase().trim()
  if (upper === 'I' || upper === 'NIVEAU_1' || upper === '1') return 'I'
  if (upper === 'L' || upper === 'NIVEAU_2' || upper === '2') return 'L'
  if (upper === 'U' || upper === 'NIVEAU_3' || upper === '3') return 'U'
  return level
}

const hasValidFormations = computed(() => {
  if (!selectedOperator.value) return false
  if (selectedOperator.value.operatorType === 'DEJA_EN_POSTE') return true
  const list = selectedOperatorFormations.value || []
  return list.some(f => f.status === 'VALIDEE' || f.status === 'COMPLETED' || f.achievedLevel === '3' || f.achievedLevel === 'U' || f.achievedLevel === '2' || f.achievedLevel === 'L' || f.achievedLevel === '1' || f.achievedLevel === 'I')
})

watch(selectedOperatorId, async (id) => {
  if (!id) {
    selectedOperatorFormations.value = []
    return
  }
  formationsLoading.value = true
  try {
    const res = await operatorsApi.getFormations(id)
    selectedOperatorFormations.value = Array.isArray(res.data) ? res.data : []
  } catch (e) {
    console.error('Error loading operator formations:', e)
    selectedOperatorFormations.value = []
  } finally {
    formationsLoading.value = false
  }
}, { immediate: true })

// 1. Base scoped operators:
// - Responsable Qualité, Superviseur, RH, HSE, Admin see ALL operators across all shifts
// - Chef d'Équipe & Agent Qualité see ONLY operators in their assigned shift(s) / team(s)
const scopedOperators = computed(() => {
  if (auth.isRespQualite || auth.isSuperviseur || auth.isRh || auth.isAdmin || auth.isRespHse) {
    return operators.value || []
  }
  if (auth.isChefEquipe || auth.isAgentQualite) {
    return (operators.value || []).filter(op => isOpInMyTeam(op))
  }
  return operators.value || []
})

// 2. Filtered operators (search & dropdown filters)
const filteredOperators = computed(() => {
  let result = scopedOperators.value || []
  if (selectedProjectFilter.value) {
    const pid = Number(selectedProjectFilter.value)
    result = result.filter(op => op.project?.id === pid)
  }
  if (selectedTeamFilter.value) {
    const tid = Number(selectedTeamFilter.value)
    result = result.filter(op => op.team?.id === tid)
  }
  const q = (search.value || '').toLowerCase()
  if (q) {
    result = result.filter(o =>
      `${o.lastName || ''} ${o.firstName || ''} ${o.employeeId || ''}`.toLowerCase().includes(q)
    )
  }
  return result
})

// 3. Sorted operators
const sortedOperators = computed(() => {
  const result = [...filteredOperators.value]
  const field = sortBy.value
  const order = sortOrder.value === 'asc' ? 1 : -1
  result.sort((a, b) => {
    let valA = '', valB = ''
    if (field === 'lastName') {
      valA = `${a.lastName || ''} ${a.firstName || ''}`.toLowerCase()
      valB = `${b.lastName || ''} ${b.firstName || ''}`.toLowerCase()
    } else if (field === 'employeeId') {
      valA = (a.employeeId || '').toLowerCase()
      valB = (b.employeeId || '').toLowerCase()
    } else if (field === 'team') {
      valA = (a.team?.name || '').toLowerCase()
      valB = (b.team?.name || '').toLowerCase()
    } else if (field === 'hireDate') {
      valA = a.hireDate || ''
      valB = b.hireDate || ''
    } else if (field === 'exitDate') {
      valA = a.exitDate || ''
      valB = b.exitDate || ''
    } else if (field === 'active') {
      valA = a.active !== false ? '1' : '0'
      valB = b.active !== false ? '1' : '0'
    }
    if (valA < valB) return -1 * order
    if (valA > valB) return 1 * order
    return 0
  })
  return result
})

// 4. Selected operator (now safely accesses sortedOperators!)
const selectedOperator = computed(() => {
  const list = sortedOperators.value
  if (!selectedOperatorId.value && list.length > 0) {
    return list[0]
  }
  return (operators.value || []).find(op => op.id === selectedOperatorId.value) || null
})

// 5. Stats counters
const totalActiveCount = computed(() => (scopedOperators.value || []).filter(o => o && o.active !== false).length)
const totalNewRecruits = computed(() => (scopedOperators.value || []).filter(o => o && o.operatorType !== 'DEJA_EN_POSTE').length)
const totalInactives = computed(() => (scopedOperators.value || []).filter(o => o && o.active === false).length)
const showCreateModal = ref(false)
const creating = ref(false)
const error = ref('')

const unassignedCount = computed(() => {
  return (operators.value || []).filter(op => op && op.active !== false && !op.team).length
})

const myTeamCount = computed(() => {
  return (operators.value || []).filter(op => op && op.active !== false && isOpInMyTeam(op)).length
})

const isOpInMyTeam = (op) => {
  if (!op || !op.team) return false
  const userEmpId = auth.user?.employeeId
  const userName = auth.user?.name ? auth.user.name.toLowerCase() : null
  const leaderEmpId = op.team.teamLeaderEmployeeId
  const leaderName = op.team.teamLeader ? op.team.teamLeader.toLowerCase() : null

  if (leaderEmpId && userEmpId && leaderEmpId === userEmpId) return true
  if (leaderName && userName && leaderName === userName) return true
  return false
}

const filteredAssignOperators = computed(() => {
  let list = (operators.value || []).filter(op => op && op.active !== false && !op.team)

  if (assignSearch.value && assignSearch.value.trim()) {
    const q = assignSearch.value.trim().toLowerCase()
    list = list.filter(op => {
      const fn = (op.firstName || '').toLowerCase()
      const ln = (op.lastName || '').toLowerCase()
      const empId = (op.employeeId || '').toLowerCase()
      return fn.includes(q) || ln.includes(q) || empId.includes(q) || `${ln} ${fn}`.includes(q)
    })
  }

  return list
})

const assignToMyTeam = async (op) => {
  const userEmpId = auth.user?.employeeId
  const userName = auth.user?.name ? auth.user.name.toLowerCase() : null

  let userTeam = (teams.value || []).find(t => {
    const leaderEmpId = t.teamLeaderEmployeeId
    const leaderName = t.teamLeader ? t.teamLeader.toLowerCase() : null
    if (leaderEmpId && userEmpId && leaderEmpId === userEmpId) return true
    if (leaderName && userName && leaderName === userName) return true
    return false
  })

  if (!userTeam && auth.hasAnyRole(['SUPERVISEUR', 'ADMIN', 'RH'])) {
    userTeam = (teams.value || [])[0]
  }

  if (!userTeam) {
    showAssignBanner("Aucune équipe configurée pour votre compte Chef d'Équipe. Veuillez contacter l'administrateur.", false)
    return
  }

  try {
    await teamsApi.assignChef(op.id, userTeam.id)
    showAssignBanner(`L'opérateur ${op.lastName || ''} ${op.firstName || ''} (${op.employeeId || ''}) a été affecté avec succès à votre équipe.`)
    await fetchOperators()
    await fetchTeams()
  } catch (e) {
    showAssignBanner("Erreur lors de l'affectation de l'opérateur.", false)
  }
}

const removeFromMyTeam = async (op) => {
  try {
    await teamsApi.assignChef(op.id, null)
    showAssignBanner(`L'opérateur ${op.lastName || ''} ${op.firstName || ''} (${op.employeeId || ''}) a été retiré de votre équipe.`)
    await fetchOperators()
    await fetchTeams()
  } catch (e) {
    showAssignBanner("Erreur lors du retrait de l'opérateur.", false)
  }
}

const showAssignBanner = (msg, success = true) => {
  assignBannerMsg.value = msg
  assignBannerSuccess.value = success
  setTimeout(() => { assignBannerMsg.value = '' }, 5000)
}

// Pagination and Sorting state
const projectPages = ref({})
const collapsedProjects = ref(new Set())

const toggleProjectCollapse = (projectId) => {
  const key = projectId || '_none'
  if (collapsedProjects.value.has(key)) {
    collapsedProjects.value.delete(key)
  } else {
    collapsedProjects.value.add(key)
  }
}

const getProjectPage = (projectId) => {
  const key = projectId || '_none'
  return projectPages.value[key] || 1
}

const setProjectPage = (projectId, page) => {
  const key = projectId || '_none'
  projectPages.value[key] = page
}

const handleSort = (field) => {
  if (sortBy.value === field) {
    sortOrder.value = sortOrder.value === 'asc' ? 'desc' : 'asc'
  } else {
    sortBy.value = field
    sortOrder.value = 'asc'
  }
}

// Reset page when filters change
watch([search, selectedProjectFilter, selectedTeamFilter, pageSize], () => {
  currentPage.value = 1
})

const showImportModal = ref(false)
const importFile = ref(null)
const parsedOperators = ref([])
const importError = ref('')
const importSuccess = ref('')
const importing = ref(false)
const fileInput = ref(null)

const form = ref({
  lastName: '', firstName: '', employeeId: '', role: '', shift: '',
  operatorType: 'NOUVEAU_RECRU', hireDate: '', exitDate: '',
  projectId: '', zoneId: '', workstationId: '',
})

// Roles that legitimately work across several projects and therefore need
// the "grouped by project" view + project filter (RH/Superviseur/Qualite/Admin...).
const isMultiProjectRole = computed(() =>
  auth.hasAnyRole(['RESP_QUALITE', 'AGENT_QUALITE', 'SUPERVISEUR', 'RESP_HSE', 'ADMIN', 'RH'])
)
// Chef d'equipe is scoped to a single project (his own) - see myProjectIds below.
const isChefEquipeRole = computed(() => auth.hasAnyRole(['CHEF_EQUIPE']))

const showProjectFilter = computed(() => isMultiProjectRole.value && projectList.value.length >= 1)
const showProjectColumn = computed(() => isMultiProjectRole.value)

// Build project list from structure API
const projectList = computed(() => {
  if (!projects.value.length) return []
  return projects.value.map(p => ({ id: p.id, name: p.name })).sort((a, b) => a.name.localeCompare(b.name))
})

// Build team list from teams data
const teamList = computed(() => {
  if (!Array.isArray(teams.value)) return []
  return teams.value.map(t => ({ id: t.id, name: t.name })).sort((a, b) => (a.name || '').localeCompare(b.name || ''))
})

const filteredTeams = computed(() => {
  if (!form.value.projectId || !Array.isArray(teams.value)) return []
  const pid = Number(form.value.projectId)
  return teams.value.filter(t => {
    if (t.projects && t.projects.some(p => p.id === pid)) return true
    if (t.project && t.project.id === pid) return true
    return false
  })
})

const filteredEditTeams = computed(() => {
  if (!editForm.value.projectId || !Array.isArray(teams.value)) return []
  const pid = Number(editForm.value.projectId)
  return teams.value.filter(t => {
    if (t.projects && t.projects.some(p => p.id === pid)) return true
    if (t.project && t.project.id === pid) return true
    return false
  })
})

// Project(s) led by the current chef d'equipe (based on project membership)
const myChefMember = computed(() => {
  const empId = auth.user?.employeeId
  if (!Array.isArray(projects.value)) return null
  for (const p of projects.value) {
    if (Array.isArray(p.members)) {
      const m = p.members.find(mem => mem.employeeId === empId)
      if (m) return m
    }
  }
  return null
})

const myProjectIds = computed(() => {
  const empId = auth.user?.employeeId
  if (!Array.isArray(projects.value)) return new Set()
  return new Set(
    projects.value
      .filter(p => Array.isArray(p.members) && p.members.some(m => m.employeeId === empId))
      .map(p => p.id)
  )
})

// Project name(s) for a single operator - now sourced directly from the
// operator's own project assignment (op.project), not the old broken
// Team -> team_projects link.
const getOperatorProjects = (op) => {
  return op.project ? [op.project.name] : []
}

const groupedByProject = computed(() => {
  const groups = {}
  for (const op of (filteredOperators.value || [])) {
    const proj = op.project
    const key = proj ? proj.id : '_none'
    if (!groups[key]) {
      groups[key] = { projectName: proj ? proj.name : 'Sans projet', projectId: proj ? proj.id : null, operators: [] }
    }
    groups[key].operators.push(op)
  }
  return Object.values(groups).sort((a, b) => {
    if (!a.projectId) return 1
    if (!b.projectId) return -1
    return a.projectName.localeCompare(b.projectName)
  })
})



const paginatedOperators = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return sortedOperators.value.slice(start, end)
})

const totalPages = computed(() => {
  return Math.ceil(filteredOperators.value.length / pageSize.value) || 1
})

const paginatedGroupedByProject = computed(() => {
  return groupedByProject.value.map(group => {
    const key = group.projectId || '_none'
    const page = getProjectPage(group.projectId)
    const start = (page - 1) * pageSize.value
    const end = start + pageSize.value
    
    const sortedGroupOps = [...group.operators]
    const field = sortBy.value
    const order = sortOrder.value === 'asc' ? 1 : -1
    sortedGroupOps.sort((a, b) => {
      let valA = '', valB = ''
      if (field === 'lastName') {
        valA = `${a.lastName || ''} ${a.firstName || ''}`.toLowerCase()
        valB = `${b.lastName || ''} ${b.firstName || ''}`.toLowerCase()
      } else if (field === 'employeeId') {
        valA = (a.employeeId || '').toLowerCase()
        valB = (b.employeeId || '').toLowerCase()
      } else if (field === 'team') {
        valA = (a.team?.name || '').toLowerCase()
        valB = (b.team?.name || '').toLowerCase()
      } else if (field === 'hireDate') {
        valA = a.hireDate || ''
        valB = b.hireDate || ''
      } else if (field === 'exitDate') {
        valA = a.exitDate || ''
        valB = b.exitDate || ''
      } else if (field === 'active') {
        valA = a.active !== false ? '1' : '0'
        valB = b.active !== false ? '1' : '0'
      }
      if (valA < valB) return -1 * order
      if (valA > valB) return 1 * order
      return 0
    })

    return {
      ...group,
      totalCount: group.operators.length,
      totalPages: Math.ceil(group.operators.length / pageSize.value) || 1,
      operators: sortedGroupOps.slice(start, end)
    }
  })
})

// Selected project name for the header bar
const selectedProjectName = computed(() => {
  if (!selectedProjectFilter.value || !Array.isArray(projects.value)) return ''
  return projects.value.find(p => p.id === Number(selectedProjectFilter.value))?.name || ''
})

const selectedProjectZones = computed(() => {
  const list = useChefProjects.value ? chefProjects.value : projects.value
  if (!form.value.projectId || !Array.isArray(list)) return []
  const p = list.find(pr => pr.id === form.value.projectId)
  return Array.isArray(p?.zones) ? p.zones : []
})

const selectedZoneWorkstations = computed(() => {
  if (!form.value.zoneId || !Array.isArray(selectedProjectZones.value)) return []
  const zone = selectedProjectZones.value.find(z => z.id === form.value.zoneId)
  return Array.isArray(zone?.workstations) ? zone.workstations : []
})

const fetchOperators = async () => {
  loading.value = true
  try {
    const r = await operatorsApi.getAll()
    operators.value = Array.isArray(r.data) ? r.data : []
  } catch (e) {
    console.error(e)
    operators.value = []
  } finally {
    loading.value = false
  }
}

const fetchProjects = async () => {
  try {
    const r = await structureApi.getAll()
    projects.value = Array.isArray(r.data) ? r.data : []
  } catch (e) {
    console.error(e)
    projects.value = []
  }
}

const fetchTeams = async () => {
  try {
    const r = await structureApi.getTeams()
    teams.value = Array.isArray(r.data) ? r.data : []
  } catch (e) {
    console.error(e)
    teams.value = []
  }
}

const openCreateModal = async () => {
  form.value = { lastName: '', firstName: '', employeeId: '', role: '', shift: '', operatorType: 'NOUVEAU_RECRU', hireDate: '', exitDate: '', projectId: '', zoneId: '', workstationId: '', teamId: '' }
  error.value = ''
  useChefProjects.value = false
  chefProjects.value = []
  if (!Array.isArray(projects.value) || !projects.value.length) await fetchProjects()
  if (!Array.isArray(teams.value) || !teams.value.length) await fetchTeams()
  if (auth.isChefEquipe && Array.isArray(projects.value) && projects.value.length > 0) {
    const myProjects = projects.value.filter(p => Array.isArray(p.members) && p.members.some(m => m.employeeId === auth.user?.employeeId))
    if (myProjects.length > 0) {
      chefProjects.value = myProjects
      useChefProjects.value = true
    }
  }
  showCreateModal.value = true
}

const createOperator = async () => {
  creating.value = true
  error.value = ''
  try {
    const payload = {
      lastName: form.value.lastName, firstName: form.value.firstName,
      employeeId: form.value.employeeId, role: form.value.role, shift: form.value.shift || null,
      operatorType: form.value.operatorType || 'NOUVEAU_RECRU',
      hireDate: form.value.hireDate || null, exitDate: form.value.exitDate || null,
      projectId: form.value.projectId || null, zoneId: form.value.zoneId || null,
      workstationId: form.value.workstationId || null,
    }
    if (auth.isChefEquipe) {
      const userEmpId = (auth.user?.employeeId || '').trim().toLowerCase()
      const userName = (auth.user?.name || '').trim().toLowerCase()
      const myTeam = (teams.value || []).find(t => {
        const leaderEmpId = (t.teamLeaderEmployeeId || '').trim().toLowerCase()
        const leaderName = (t.teamLeader || '').trim().toLowerCase()
        if (userEmpId && leaderEmpId === userEmpId) return true
        if (userName && leaderName === userName) return true
        return false
      })
      if (myTeam) {
        payload.teamId = myTeam.id
      }
    } else {
      payload.teamId = form.value.teamId || null
    }
    const res = await operatorsApi.create(payload)
    showCreateModal.value = false
    await fetchOperators()
    if (res.data?.id) {
      selectedOperatorId.value = res.data.id
    }
  } catch (e) {
    const msg = e.response?.data?.message || e.message || 'Erreur inconnue'
    error.value = msg + ' (status: ' + (e.response?.status || 'n/a') + ')'
    alert('Erreur: ' + error.value)
  } finally {
    creating.value = false
  }
}

const confirmData = ref({ visible: false, title: '', message: '', type: 'danger' })
const pendingAction = ref(null)
const deactivateOperator = (id) => { confirmData.value = { visible: true, title: "Désactiver l'opérateur", message: 'Voulez-vous vraiment désactiver cet opérateur ?', type: 'danger' }; pendingAction.value = async () => { try { await operatorsApi.deactivate(id); fetchOperators() } catch (e) { console.error(e) } } }
const activateOperator = (id) => { confirmData.value = { visible: true, title: "Activer l'opérateur", message: 'Voulez-vous réactiver cet opérateur ?', type: 'info' }; pendingAction.value = async () => { try { await operatorsApi.activate(id); fetchOperators() } catch (e) { console.error(e) } } }
const deletePermanently = (op) => {
  confirmData.value = {
    visible: true,
    title: "Suppression définitive de l'opérateur",
    message: `ATTENTION : Êtes-vous sûr de vouloir supprimer définitivement ${op.lastName} ${op.firstName} (${op.employeeId}) ? Toutes ses formations, évaluations, sessions et plannings associés seront supprimés de la base de données de manière irréversible.`,
    type: 'danger'
  };
  pendingAction.value = async () => {
    try {
      await operatorsApi.deletePermanently(op.id);
      selectedOperatorId.value = null;
      await fetchOperators();
    } catch (e) {
      console.error(e);
      alert('Erreur lors de la suppression: ' + (e.response?.data?.message || e.message));
    }
  };
};
const handleConfirm = () => { confirmData.value.visible = false; if (pendingAction.value) { pendingAction.value(); pendingAction.value = null } }
const handleCancel = () => { confirmData.value.visible = false; pendingAction.value = null }

// Edit Operator
const showEditModal = ref(false)
const editForm = ref({ id: null, lastName: '', firstName: '', employeeId: '', role: '', shift: '', operatorType: '', hireDate: '', exitDate: '', projectId: '', zoneId: '', teamId: '' })
const editProjectZones = computed(() => {
  if (!editForm.value.projectId) return []
  return projects.value.find(p => p.id === editForm.value.projectId)?.zones || []
})
const openEditModal = (op) => { editForm.value = { id: op.id, lastName: op.lastName, firstName: op.firstName, employeeId: op.employeeId, role: op.role || '', shift: op.shift || '', operatorType: op.operatorType || 'NOUVEAU_RECRU', hireDate: op.hireDate?.slice(0, 10) || '', exitDate: op.exitDate?.slice(0, 10) || '', projectId: op.project?.id || '', zoneId: op.zone?.id || '', teamId: op.team?.id || '' }; showEditModal.value = true }
// FIX 4a: Update now sends exitDate and absenceReason to backend
const updateOperator = async () => {
  creating.value = true; error.value = ''
  try {
    await operatorsApi.update(editForm.value.id, { 
      ...editForm.value, 
      projectId: editForm.value.projectId || null, 
      zoneId: editForm.value.zoneId || null,
      teamId: editForm.value.teamId || null
    })
    showEditModal.value = false
    fetchOperators()
  } catch (e) {
    error.value = e.response?.data?.message || e.message || 'Erreur inconnue'
    alert('Erreur: ' + error.value)
  } finally {
    creating.value = false
  }
}

const defaultImportType = ref('DEJA_EN_POSTE')
const lastLoadedBuffer = ref(null)

function openImportModal() {
  importFile.value = null
  parsedOperators.value = []
  importError.value = ''
  importSuccess.value = ''
  lastLoadedBuffer.value = null
  showImportModal.value = true
}

function closeImportModal() {
  showImportModal.value = false
  importFile.value = null
  parsedOperators.value = []
  importError.value = ''
  importSuccess.value = ''
  lastLoadedBuffer.value = null
}

function handleFileChange(event) {
  const file = event.target.files ? event.target.files[0] : null
  if (!file) return
  importFile.value = file
  importError.value = ''
  importSuccess.value = ''
  
  const reader = new FileReader()
  reader.onload = (e) => {
    lastLoadedBuffer.value = e.target.result
    parseBufferData(e.target.result)
  }
  reader.readAsArrayBuffer(file)
}

function reparseImportFile() {
  if (lastLoadedBuffer.value) {
    parseBufferData(lastLoadedBuffer.value)
  }
}

function parseBufferData(buffer) {
  try {
    const data = new Uint8Array(buffer)
    const workbook = XLSX.read(data, { type: 'array' })
    const firstSheetName = workbook.SheetNames[0]
    const worksheet = workbook.Sheets[firstSheetName]
    const jsonData = XLSX.utils.sheet_to_json(worksheet, { header: 1 })
    
    if (jsonData.length < 2) {
      importError.value = "Le fichier Excel est vide ou ne contient pas assez de données."
      return
    }
    
    const headers = jsonData[0].map(h => String(h).trim().toLowerCase())
    
    const nomIdx = headers.findIndex(h => h.includes('nom'))
    const prenomIdx = headers.findIndex(h => h.includes('prénom') || h.includes('prenom'))
    const matriculeIdx = headers.findIndex(h => h.includes('matricule') || h.includes('code') || h.includes('id'))
    const roleIdx = headers.findIndex(h => h.includes('rôle') || h.includes('role') || h.includes('fonction'))
    const typeIdx = headers.findIndex(h => h.includes('type'))
    const embaucheIdx = headers.findIndex(h => h.includes('embauche') || h.includes('recrutement'))
    
    if (nomIdx === -1 || prenomIdx === -1 || matriculeIdx === -1) {
      importError.value = "Colonnes requises manquantes. Assurez-vous d'avoir des colonnes nommées 'Nom', 'Prénom', et 'Matricule'."
      return
    }
    
    const list = []
    for (let i = 1; i < jsonData.length; i++) {
      const row = jsonData[i]
      if (!row || row.length === 0 || !row[matriculeIdx]) continue
      
      let opType = defaultImportType.value
      if (typeIdx !== -1 && row[typeIdx]) {
        const tVal = String(row[typeIdx]).toLowerCase().trim()
        if (tVal.includes('poste') || tVal.includes('déjà') || tVal.includes('deja') || tVal.includes('ancien')) {
          opType = 'DEJA_EN_POSTE'
        } else if (tVal.includes('recru') || tVal.includes('nouveau') || tVal.includes('nouvelle')) {
          opType = 'NOUVEAU_RECRU'
        }
      }
      
      let hireDateVal = null
      if (embaucheIdx !== -1 && row[embaucheIdx]) {
        if (typeof row[embaucheIdx] === 'number') {
          const dateObj = XLSX.SSF.parse_date_code(row[embaucheIdx])
          const d = new Date(dateObj.y, dateObj.m - 1, dateObj.d)
          hireDateVal = d.toISOString().slice(0, 10)
        } else {
          const rawStr = String(row[embaucheIdx]).trim()
          if (/^\d{4}-\d{2}-\d{2}$/.test(rawStr)) {
            hireDateVal = rawStr
          } else if (/^\d{2}\/\d{2}\/\d{4}$/.test(rawStr)) {
            const [day, m, y] = rawStr.split('/')
            hireDateVal = `${y}-${m}-${day}`
          }
        }
      }
      
      list.push({
        lastName: String(row[nomIdx]).trim().toUpperCase(),
        firstName: String(row[prenomIdx]).trim(),
        employeeId: String(row[matriculeIdx]).trim(),
        role: roleIdx !== -1 && row[roleIdx] ? String(row[roleIdx]).trim() : 'Opérateur',
        operatorType: opType,
        hireDate: hireDateVal,
        active: true
      })
    }
    
    if (list.length === 0) {
      importError.value = "Aucun opérateur valide détecté dans le fichier."
    } else {
      parsedOperators.value = list
    }
  } catch (err) {
    console.error(err)
    importError.value = "Erreur lors de la lecture du fichier Excel."
  }
}

async function submitImport() {
  if (parsedOperators.value.length === 0) return
  importing.value = true
  importError.value = ''
  importSuccess.value = ''
  
  try {
    await operatorsApi.createBatch(parsedOperators.value)
    importSuccess.value = `${parsedOperators.value.length} opérateurs ont été importés avec succès !`
    parsedOperators.value = []
    fetchOperators()
  } catch (err) {
    console.error(err)
    importError.value = err.response?.data?.message || err.message || "Erreur lors de l'importation."
  } finally {
    importing.value = false
  }
}

function exportOperatorsToExcel() {
  const data = [
    ["Matricule", "Nom", "Prénom", "Rôle", "Type d'opérateur", "Date d'embauche", "Statut", "Projet", "Zone"]
  ]
  
  operators.value.forEach(op => {
    data.push([
      op.employeeId || "",
      op.lastName || "",
      op.firstName || "",
      op.role || "Opérateur",
      op.operatorType === 'DEJA_EN_POSTE' ? "Déjà en poste" : "Nouvelle recrue",
      op.hireDate || "-",
      op.active !== false ? "Actif" : "Inactif",
      op.project?.name || "-",
      op.zone?.name || "-"
    ])
  })
  
  const worksheet = XLSX.utils.aoa_to_sheet(data)
  const workbook = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(workbook, worksheet, "Opérateurs")
  
  XLSX.writeFile(workbook, `Liste_Operateurs_${new Date().toISOString().slice(0, 10)}.xlsx`)
}

onMounted(() => {
  Promise.allSettled([
    loadUserProjects(),
    fetchOperators(),
    fetchProjects(),
    fetchTeams()
  ])
})
</script>