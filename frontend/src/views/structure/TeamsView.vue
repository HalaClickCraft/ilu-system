<template>
  <div class="space-y-6">
    <!-- Page Header -->
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">Équipes & Transferts</h1>
        <p class="text-gray-500 mt-1">
          Gérer les affectations d'opérateurs et les demandes de transfert (projet ou Support Team).
        </p>
      </div>
      <!-- ONE single action button per tab -->
      <div>
        <button
          v-if="activeTab === 'teams' && canManageOrRequest"
          @click="openUpdateModal"
          class="inline-flex items-center gap-2 rounded-lg bg-emerald-600 px-4 py-2 text-sm font-medium text-white hover:bg-emerald-700 shadow-sm transition"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"/></svg>
          {{ isSupervisor ? 'Modifier l\'équipe (Superviseur)' : 'Proposer Mise à jour d\'Équipe' }}
        </button>
        <button
          v-if="activeTab === 'transfers' && canManageOrRequest"
          @click="openTransferModal"
          class="inline-flex items-center gap-2 rounded-lg bg-emerald-600 px-4 py-2 text-sm font-medium text-white hover:bg-emerald-700 shadow-sm transition"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7h12m0 0l-4-4m4 4l-4 4m0 6H4m0 0l4 4m-4-4l4-4"/></svg>
          Demander un Transfert
        </button>
      </div>
    </div>

    <!-- Tabs -->
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
        Transferts
        <span v-if="pendingTransfers.length > 0" class="ml-1.5 bg-amber-500 text-white text-[10px] font-bold px-1.5 py-0.5 rounded-full">
          {{ pendingTransfers.length }}
        </span>
      </button>
    </div>

    <!-- Banner -->
    <div v-if="bannerMsg" class="rounded-lg p-4 text-sm font-medium border transition"
      :class="bannerSuccess ? 'bg-emerald-50 text-emerald-800 border-emerald-200' : 'bg-red-50 text-red-800 border-red-200'">
      {{ bannerMsg }}
    </div>

    <!-- ======================== TAB 1: Équipes ======================== -->
    <div v-if="activeTab === 'teams'" class="space-y-6">
      <!-- Supervisor: pending team-update requests -->
      <div v-if="isSupervisor && pendingRequests.length > 0" class="rounded-xl border border-amber-200 bg-amber-50/60 p-5 shadow-sm space-y-3">
        <div class="flex items-center justify-between border-b border-amber-200/80 pb-3">
          <h2 class="font-bold text-amber-900 flex items-center gap-2">
            <svg class="w-5 h-5 text-amber-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"/></svg>
            Mises à jour d'équipe en attente ({{ pendingRequests.length }})
          </h2>
          <span class="text-xs bg-amber-200 text-amber-900 px-2.5 py-0.5 rounded-full font-semibold">Validation Superviseur requise</span>
        </div>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div v-for="req in pendingRequests" :key="req.id" class="bg-white rounded-lg p-4 border border-amber-200 shadow-xs space-y-2">
            <div class="flex items-center justify-between">
              <span class="font-bold text-gray-900">{{ req.teamName || 'Équipe #' + req.teamId }}</span>
              <span class="text-xs text-gray-400">{{ req.requestedBy }}</span>
            </div>
            <p class="text-xs text-gray-500">Opérateurs proposés : <span class="font-mono text-gray-700">{{ req.operatorIds || 'Aucun' }}</span></p>
            <div class="flex items-center justify-end gap-2 pt-2 border-t border-gray-100">
              <button @click="rejectRequest(req.id)" class="px-3 py-1 text-xs font-medium text-red-600 hover:bg-red-50 rounded border border-red-200">Refuser</button>
              <button @click="approveRequest(req.id)" class="px-3 py-1 text-xs font-semibold text-white bg-emerald-600 hover:bg-emerald-700 rounded shadow-xs">✅ Valider</button>
            </div>
          </div>
        </div>
      </div>

      <div v-if="loading" class="flex items-center justify-center py-20">
        <div class="w-8 h-8 border-4 border-emerald-200 border-t-emerald-600 rounded-full animate-spin"></div>
      </div>

      <!-- Unassigned operators panel (visible to Chef d'Équipe) -->
      <div v-if="!loading && unassignedOperators.length > 0 && canManageOrRequest" class="bg-amber-50 border border-amber-200 rounded-xl p-5 space-y-3">
        <div class="flex items-center justify-between">
          <h2 class="font-bold text-amber-900 flex items-center gap-2">
            <svg class="w-5 h-5 text-amber-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"/></svg>
            Opérateurs non assignés ({{ unassignedOperators.length }})
          </h2>
          <span class="text-xs text-amber-700 bg-amber-200 px-2 py-0.5 rounded-full font-semibold">À affecter à un Chef d'Équipe</span>
        </div>
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-2">
          <div v-for="op in unassignedOperators" :key="op.id" class="flex items-center justify-between bg-white rounded-lg px-3 py-2 border border-amber-200 text-sm">
            <span class="font-medium text-gray-900">{{ op.lastName }} {{ op.firstName }} <span class="text-xs text-gray-400 font-mono">({{ op.employeeId }})</span></span>
            <button @click="claimOperator(op)" class="ml-2 px-2 py-1 text-xs font-semibold bg-emerald-600 text-white rounded hover:bg-emerald-700">+ Mon équipe</button>
          </div>
        </div>
      </div>

      <div v-else-if="!loading && groups.length === 0" class="bg-white rounded-xl shadow-sm border border-gray-200 p-12 text-center text-gray-400">
        Aucun opérateur affecté pour le moment.
      </div>
      <div v-if="!loading && groups.length > 0" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        <div v-for="group in groups" :key="group.teamId || '_none'" class="bg-white rounded-xl shadow-sm border border-gray-200 p-5">
          <div class="flex items-center justify-between mb-3">
            <h3 class="font-semibold text-gray-900 flex items-center gap-2">
              <svg class="w-4 h-4 text-emerald-600 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"/></svg>
              {{ group.teamName }}
            </h3>
            <span class="text-xs text-gray-500 bg-gray-50 px-2 py-0.5 rounded-full border">{{ group.operators.length }} opérateur(s)</span>
          </div>
          <ul class="space-y-1.5">
            <li v-for="op in group.operators" :key="op.id" class="flex items-center justify-between py-1.5 px-2 rounded bg-gray-50 text-sm">
              <router-link :to="'/operators/' + op.id" class="text-emerald-600 hover:underline font-medium">
                — {{ op.lastName }} {{ op.firstName }}
              </router-link>
              <span class="inline-flex items-center px-1.5 py-0.5 rounded text-xs"
                :class="op.active !== false ? 'bg-emerald-100 text-emerald-700' : 'bg-red-100 text-red-700'">
                {{ op.active !== false ? 'Actif' : 'Inactif' }}
              </span>
            </li>
          </ul>
        </div>
      </div>

    </div>

    <!-- ======================== TAB 2: Transferts ======================== -->
    <div v-if="activeTab === 'transfers'" class="space-y-6">

      <!-- PENDING — Supervisor validation section -->
      <div v-if="isSupervisor" class="rounded-xl border border-gray-200 bg-white p-5 shadow-sm space-y-4">
        <h2 class="font-bold text-gray-900 text-lg flex items-center gap-2 border-b border-gray-100 pb-3">
          <svg class="w-5 h-5 text-amber-500" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"/></svg>
          Demandes en attente de validation ({{ pendingTransfers.length }})
        </h2>
        <div v-if="pendingTransfers.length === 0" class="text-center py-8 text-gray-400 text-sm italic">
          Aucune demande en attente.
        </div>
        <div v-else class="overflow-x-auto">
          <table class="w-full text-left text-sm text-gray-500 border-collapse">
            <thead class="bg-gray-50 text-xs text-gray-700 uppercase font-semibold border-b border-gray-200">
              <tr>
                <th class="px-4 py-3">Opérateur</th>
                <th class="px-4 py-3">Changement</th>
                <th class="px-4 py-3">Demandé par</th>
                <th class="px-4 py-3">Date</th>
                <th class="px-4 py-3 text-right">Actions</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-gray-100 bg-white">
              <tr v-for="req in pendingTransfers" :key="req.id" class="hover:bg-gray-50">
                <td class="px-4 py-3">
                  <span class="font-semibold text-gray-900 block">{{ req.operatorName }}</span>
                  <span class="text-xs text-gray-400 font-mono">{{ req.employeeId }}</span>
                </td>
                <td class="px-4 py-3 text-xs">
                  <div class="flex flex-col gap-0.5">
                    <span class="text-gray-500">
                      <span class="font-semibold text-gray-700">Projet:</span>
                      {{ req.sourceProjectName || 'Aucun' }}
                      <span class="mx-1 text-gray-400">→</span>
                      <span class="font-semibold text-emerald-700">{{ req.targetProjectName }}</span>
                    </span>
                    <span v-if="req.targetTeamName" class="text-gray-500">
                      <span class="font-semibold text-gray-700">Équipe:</span>
                      <span class="text-emerald-600 font-semibold">↳ {{ req.targetTeamName }}</span>
                    </span>
                  </div>
                </td>
                <td class="px-4 py-3 text-xs text-gray-600">{{ req.requestedBy }}</td>
                <td class="px-4 py-3 text-xs text-gray-400">{{ formatDate(req.createdAt) }}</td>
                <td class="px-4 py-3 text-right space-x-2">
                  <button @click="rejectTransfer(req.id)" class="px-2.5 py-1 text-xs font-medium text-red-600 bg-red-50 hover:bg-red-100 rounded border border-red-200 transition">Refuser</button>
                  <button @click="approveTransfer(req.id)" class="px-2.5 py-1 text-xs font-semibold text-white bg-emerald-600 hover:bg-emerald-700 rounded shadow-xs transition">✅ Valider</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- ALL TRANSFERS HISTORY -->
      <div class="rounded-xl border border-gray-200 bg-white p-5 shadow-sm space-y-4">
        <h2 class="font-bold text-gray-900 text-lg border-b border-gray-100 pb-3">Historique de tous les transferts</h2>
        <div v-if="allTransfersList.length === 0" class="text-center py-12 text-gray-400 text-sm italic">
          Aucun transfert enregistré.
        </div>
        <div v-else class="overflow-x-auto">
          <table class="w-full text-left text-sm text-gray-500 border-collapse">
            <thead class="bg-gray-50 text-xs text-gray-700 uppercase font-semibold border-b border-gray-200">
              <tr>
                <th class="px-4 py-3">Opérateur</th>
                <th class="px-4 py-3">Source → Cible</th>
                <th class="px-4 py-3">Support Team Cible</th>
                <th class="px-4 py-3">Date</th>
                <th class="px-4 py-3">Statut</th>
                <th class="px-4 py-3">Traité par</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-gray-100 bg-white">
              <tr v-for="req in allTransfersList" :key="req.id" class="hover:bg-gray-50">
                <td class="px-4 py-3">
                  <span class="font-semibold text-gray-900 block">{{ req.operatorName }}</span>
                  <span class="text-xs text-gray-400 font-mono">{{ req.employeeId }}</span>
                </td>
                <td class="px-4 py-3 text-xs">
                  <span class="text-gray-400">{{ req.sourceProjectName || 'Sans Projet' }}</span>
                  <span class="text-gray-500 mx-1.5">→</span>
                  <span class="font-semibold text-emerald-700">{{ req.targetProjectName }}</span>
                </td>
                <td class="px-4 py-3 text-xs">
                  <span v-if="req.targetTeamName" class="text-emerald-600 font-semibold">{{ req.targetTeamName }}</span>
                  <span v-else class="text-gray-400">—</span>
                </td>
                <td class="px-4 py-3 text-xs text-gray-400">{{ formatDate(req.createdAt) }}</td>
                <td class="px-4 py-3">
                  <span class="px-2 py-0.5 rounded-full text-xs font-semibold"
                    :class="{
                      'bg-amber-100 text-amber-800': req.status === 'PENDING',
                      'bg-emerald-100 text-emerald-800': req.status === 'APPROVED',
                      'bg-red-100 text-red-800': req.status === 'REJECTED'
                    }">
                    {{ req.status === 'PENDING' ? '⏳ En attente' : req.status === 'APPROVED' ? '✅ Approuvé' : '❌ Refusé' }}
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

    <!-- ======================== MODAL: Mise à jour d'équipe ======================== -->
    <div v-if="showModal" class="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 backdrop-blur-xs p-4">
      <div class="bg-white rounded-xl shadow-xl border border-gray-200 max-w-lg w-full p-6 space-y-4">
        <div class="flex items-center justify-between border-b border-gray-100 pb-3">
          <h3 class="font-bold text-gray-900 text-lg">
            {{ isSupervisor ? 'Mise à jour directe (Superviseur)' : 'Proposer une mise à jour d\'équipe' }}
          </h3>
          <button @click="showModal = false" class="text-gray-400 hover:text-gray-600 text-xl leading-none">✕</button>
        </div>
        <div v-if="!isSupervisor" class="p-3 bg-blue-50 border border-blue-200 rounded-lg text-xs text-blue-800">
          ℹ️ Toute modification est transmise au <strong>Superviseur</strong> pour validation avant application.
        </div>
        <div class="space-y-3">
          <div>
            <label class="block text-xs font-semibold text-gray-700 mb-1">Équipe :</label>
            <select v-model="selectedTeamId" class="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm outline-none focus:ring-2 focus:ring-emerald-500">
              <option value="">-- Choisir une équipe --</option>
              <option v-for="t in teamsList" :key="t.id" :value="t.id">{{ t.name }} ({{ t.operators?.length || 0 }} membres)</option>
            </select>
          </div>
          <div>
            <label class="block text-xs font-semibold text-gray-700 mb-1">Ajouter un opérateur :</label>
            <div class="flex gap-2">
              <input v-model="operatorInputText" @keyup.enter="addOperatorByText" type="text" placeholder="Matricule ou nom..."
                class="flex-1 px-3 py-2 border border-gray-200 rounded-lg text-sm outline-none focus:ring-2 focus:ring-emerald-500" />
              <button type="button" @click="addOperatorByText" class="px-3 py-2 bg-emerald-600 hover:bg-emerald-700 text-white rounded-lg text-xs font-medium">+ Ajouter</button>
            </div>
            <div v-if="matchingSuggestions.length > 0" class="mt-1 max-h-32 overflow-y-auto border border-gray-200 rounded-lg bg-white shadow-md divide-y divide-gray-100 z-10 relative">
              <div v-for="op in matchingSuggestions" :key="op.id" @click="selectOperatorFromSuggestion(op)"
                class="px-3 py-1.5 hover:bg-emerald-50 cursor-pointer flex items-center justify-between text-xs">
                <span class="font-medium text-gray-900">{{ op.lastName }} {{ op.firstName }}</span>
                <span class="text-gray-400 font-mono text-[10px]">{{ op.employeeId }}</span>
              </div>
            </div>
            <div class="mt-3">
              <label class="block text-[11px] font-semibold text-gray-500 mb-1">Membres sélectionnés ({{ selectedOperatorsList.length }}) :</label>
              <div class="flex flex-wrap gap-1.5 max-h-32 overflow-y-auto p-2 border border-gray-200 rounded-lg bg-gray-50">
                <span v-for="op in selectedOperatorsList" :key="op.id"
                  class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-md bg-white border border-gray-200 text-xs font-medium text-gray-800 shadow-xs">
                  {{ op.lastName }} {{ op.firstName }}
                  <span class="text-gray-400 font-mono text-[10px]">({{ op.employeeId }})</span>
                  <button @click="removeOperatorFromSelected(op.id)" class="text-gray-400 hover:text-red-600 font-bold ml-1">×</button>
                </span>
                <span v-if="selectedOperatorsList.length === 0" class="text-xs text-gray-400 italic py-1">Aucun opérateur sélectionné.</span>
              </div>
            </div>
          </div>
        </div>
        <div class="flex items-center justify-end gap-2 pt-3 border-t border-gray-100">
          <button @click="showModal = false" class="px-4 py-2 text-xs font-medium text-gray-600 hover:bg-gray-100 rounded-lg">Annuler</button>
          <button @click="submitTeamUpdate" :disabled="!selectedTeamId"
            class="px-4 py-2 text-xs font-semibold text-white bg-emerald-600 hover:bg-emerald-700 rounded-lg shadow-sm disabled:opacity-50">
            {{ isSupervisor ? 'Appliquer immédiatement' : 'Soumettre au Superviseur' }}
          </button>
        </div>
      </div>
    </div>

    <!-- ======================== MODAL: Transfert (Projet + Support Team) ======================== -->
    <div v-if="showTransferModal" class="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 backdrop-blur-xs p-4">
      <div class="bg-white rounded-xl shadow-xl border border-gray-200 max-w-lg w-full p-6 space-y-4">
        <div class="flex items-center justify-between border-b border-gray-100 pb-3">
          <h3 class="font-bold text-gray-900 text-lg">Demande de Transfert</h3>
          <button @click="showTransferModal = false" class="text-gray-400 hover:text-gray-600 text-xl leading-none">✕</button>
        </div>

        <!-- Transfer type toggle -->
        <div class="flex gap-2 bg-gray-100 p-1 rounded-lg">
          <button @click="transferType = 'project'"
            class="flex-1 px-3 py-1.5 rounded-md text-xs font-semibold transition"
            :class="transferType === 'project' ? 'bg-white shadow text-emerald-700' : 'text-gray-500 hover:text-gray-700'">
            🔀 Changer de Projet
          </button>
          <button @click="transferType = 'shift'"
            class="flex-1 px-3 py-1.5 rounded-md text-xs font-semibold transition"
            :class="transferType === 'shift' ? 'bg-white shadow text-emerald-700' : 'text-gray-500 hover:text-gray-700'">
            👥 Changer de Support Team
          </button>
        </div>

        <div v-if="!isSupervisor && transferType === 'project'" class="p-3 bg-blue-50 border border-blue-200 rounded-lg text-xs text-blue-800">
          ℹ️ Cette demande sera soumise au <strong>Superviseur</strong> pour validation.
        </div>
        <div v-if="transferType === 'shift'" class="p-3 bg-emerald-50 border border-emerald-200 rounded-lg text-xs text-emerald-800">
          ✅ Changement de Support Team dans le même projet. Appliqué immédiatement par le Superviseur.
        </div>

        <div class="space-y-4">
          <!-- Operator search -->
          <div>
            <label class="block text-xs font-semibold text-gray-700 mb-1">Opérateur :</label>
            <input v-model="transferSearchText" type="text" placeholder="Saisir matricule ou nom..."
              class="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm outline-none focus:ring-2 focus:ring-emerald-500" />
            <div v-if="transferSuggestions.length > 0" class="mt-1 max-h-32 overflow-y-auto border border-gray-200 rounded-lg bg-white shadow-md divide-y divide-gray-100 z-10 relative">
              <div v-for="op in transferSuggestions" :key="op.id" @click="selectOperatorForTransfer(op)"
                class="px-3 py-1.5 hover:bg-emerald-50 cursor-pointer flex items-center justify-between text-xs">
                <span class="font-medium text-gray-900">{{ op.lastName }} {{ op.firstName }}</span>
                <span class="text-gray-400 font-mono text-[10px] ml-2">
                  {{ op.employeeId }} {{ op.project ? '(' + op.project.name + ')' : '(Sans projet)' }}
                </span>
              </div>
            </div>
          </div>

          <!-- Selected operator info -->
          <div v-if="selectedTransferOp" class="p-3 bg-slate-50 border border-gray-200 rounded-lg text-xs space-y-1">
            <p class="font-bold text-gray-700">Opérateur sélectionné :</p>
            <p><strong>Nom :</strong> {{ selectedTransferOp.lastName }} {{ selectedTransferOp.firstName }} ({{ selectedTransferOp.employeeId }})</p>
            <p><strong>Projet actuel :</strong> {{ selectedTransferOp.project?.name || 'Aucun' }}</p>
            <p><strong>Équipe actuelle :</strong> {{ selectedTransferOp.team?.name || 'Aucune' }}</p>
          </div>

          <!-- Project transfer fields -->
          <template v-if="transferType === 'project'">
            <div>
              <label class="block text-xs font-semibold text-gray-700 mb-1">Projet Cible :</label>
              <select v-model="transferTargetProjectId" @change="transferTargetTeamId = ''"
                class="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm outline-none focus:ring-2 focus:ring-emerald-500">
                <option value="" disabled>-- Choisir le projet cible --</option>
                <option v-for="p in projects" :key="p.id" :value="p.id">{{ p.name }}</option>
              </select>
            </div>
            <div>
              <label class="block text-xs font-semibold text-gray-700 mb-1">
                Support Team Cible : <span class="font-normal text-gray-400">(optionnel)</span>
              </label>
              <select v-model="transferTargetTeamId" :disabled="!transferTargetProjectId"
                class="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm outline-none focus:ring-2 focus:ring-emerald-500 disabled:bg-gray-100">
                <option value="">-- Pas de changement d'équipe --</option>
                <option v-for="t in filteredTransferTeams" :key="t.id" :value="t.id">
                  {{ t.name }} {{ t.teamLeader ? '(Chef: ' + t.teamLeader + ')' : '' }}
                </option>
              </select>
            </div>
          </template>

          <!-- Shift-only transfer fields -->
          <template v-if="transferType === 'shift'">
            <div>
              <label class="block text-xs font-semibold text-gray-700 mb-1">Nouvelle Support Team :</label>
              <select v-model="transferTargetTeamId"
                class="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm outline-none focus:ring-2 focus:ring-emerald-500">
                <option value="" disabled>-- Choisir la Support Team --</option>
                <option v-for="t in teamsForCurrentProject" :key="t.id" :value="t.id">
                  {{ t.name }} {{ t.teamLeader ? '(Chef: ' + t.teamLeader + ')' : '' }}
                </option>
              </select>
            </div>
          </template>
        </div>

        <div class="flex items-center justify-end gap-2 pt-3 border-t border-gray-100">
          <button @click="showTransferModal = false" class="px-4 py-2 text-xs font-medium text-gray-600 hover:bg-gray-100 rounded-lg">Annuler</button>
          <button @click="submitTransferRequest"
            :disabled="!canSubmitTransfer"
            class="px-4 py-2 text-xs font-semibold text-white bg-emerald-600 hover:bg-emerald-700 rounded-lg shadow-sm disabled:opacity-50">
            <template v-if="transferType === 'project'">
              {{ isSupervisor ? '🔀 Transférer immédiatement' : '📤 Soumettre au Superviseur' }}
            </template>
            <template v-else>
              👥 Changer la Support Team
            </template>
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
import { useUserScope } from '@/composables/useUserScope'

const auth = useAuthStore()
const { loadUserProjects, filterOperators } = useUserScope()
const loading = ref(true)
const activeTab = ref('teams')

const operators = ref([])
const allOperators = ref([])
const projects = ref([])
const teamsList = ref([])
const pendingRequests = ref([])    // team-update requests
const pendingTransfers = ref([])   // project-transfer requests pending approval
const allTransfersList = ref([])   // full history

// Team update modal
const showModal = ref(false)
const selectedTeamId = ref('')
const selectedOpIds = ref([])
const operatorInputText = ref('')

// Transfer modal
const showTransferModal = ref(false)
const transferType = ref('project')       // 'project' | 'shift'
const transferSearchText = ref('')
const selectedTransferOp = ref(null)
const transferTargetProjectId = ref('')
const transferTargetTeamId = ref('')

const bannerMsg = ref('')
const bannerSuccess = ref(true)

const isSupervisor = computed(() => auth.hasAnyRole(['SUPERVISEUR', 'ADMIN', 'RH']))
const canManageOrRequest = computed(() => auth.hasAnyRole(['CHEF_EQUIPE', 'SUPERVISEUR', 'ADMIN']))

const unassignedOperators = ref([])

// Group operators by Chef d'Équipe (Team)
const groups = computed(() => {
  const map = {}
  operators.value.forEach(op => {
    const team = op.team
    const teamId = team ? team.id : '_none'
    const teamName = team ? (team.teamLeader || team.name) : 'Non assigné'
    if (!map[teamId]) {
      map[teamId] = { teamId: teamId === '_none' ? null : teamId, teamName, teamLeader: team?.teamLeader || '', operators: [] }
    }
    map[teamId].operators.push(op)
  })
  return Object.values(map).sort((a, b) => {
    if (!a.teamId) return 1
    if (!b.teamId) return -1
    return a.teamName.localeCompare(b.teamName)
  })
})

// Team update modal helpers
const selectedOperatorsList = computed(() => {
  const set = new Set(selectedOpIds.value)
  return allOperators.value.filter(op => set.has(op.id))
})

const matchingSuggestions = computed(() => {
  const q = operatorInputText.value.trim().toLowerCase()
  if (!q) return []
  return allOperators.value.filter(op =>
    !selectedOpIds.value.includes(op.id) &&
    ((op.employeeId && op.employeeId.toLowerCase().includes(q)) ||
     (op.lastName && op.lastName.toLowerCase().includes(q)) ||
     (op.firstName && op.firstName.toLowerCase().includes(q)) ||
     `${op.lastName} ${op.firstName}`.toLowerCase().includes(q))
  )
})

// Transfer modal helpers
const transferSuggestions = computed(() => {
  const q = transferSearchText.value.trim().toLowerCase()
  if (!q || q.length < 1) return []
  // Hide already-selected operator from suggestions
  return allOperators.value.filter(op =>
    (!selectedTransferOp.value || op.id !== selectedTransferOp.value.id) &&
    ((op.employeeId && op.employeeId.toLowerCase().includes(q)) ||
     (op.lastName && op.lastName.toLowerCase().includes(q)) ||
     (op.firstName && op.firstName.toLowerCase().includes(q)) ||
     `${op.lastName} ${op.firstName}`.toLowerCase().includes(q))
  )
})

// Teams filtered by the chosen target project (for project transfers)
const filteredTransferTeams = computed(() => {
  if (!transferTargetProjectId.value) return []
  return teamsList.value.filter(t => t.projects?.some(p => p.id === Number(transferTargetProjectId.value)))
})

// Teams filtered by the operator's current project (for shift-only transfer)
const teamsForCurrentProject = computed(() => {
  if (!selectedTransferOp.value?.project?.id) return teamsList.value
  return teamsList.value.filter(t => t.projects?.some(p => p.id === selectedTransferOp.value.project.id))
})

// Can the submit button be clicked?
const canSubmitTransfer = computed(() => {
  if (!selectedTransferOp.value) return false
  if (transferType.value === 'project') return !!transferTargetProjectId.value
  if (transferType.value === 'shift') return !!transferTargetTeamId.value
  return false
})

// Team update modal logic
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

// Transfer modal logic
const selectOperatorForTransfer = (op) => {
  selectedTransferOp.value = op
  transferSearchText.value = `${op.lastName} ${op.firstName} (${op.employeeId})`
  // reset team selection when operator changes
  transferTargetTeamId.value = ''
}

watch(selectedTeamId, (newId) => {
  if (!newId) { selectedOpIds.value = []; return }
  const team = teamsList.value.find(t => t.id === Number(newId))
  selectedOpIds.value = team?.operators?.map(o => o.id) || []
})

const openUpdateModal = () => {
  selectedTeamId.value = teamsList.value[0]?.id || ''
  operatorInputText.value = ''
  selectedOpIds.value = teamsList.value[0]?.operators?.map(o => o.id) || []
  showModal.value = true
}

const openTransferModal = () => {
  transferType.value = 'project'
  transferSearchText.value = ''
  selectedTransferOp.value = null
  transferTargetProjectId.value = ''
  transferTargetTeamId.value = ''
  showTransferModal.value = true
}

// Data fetching
const fetchData = async () => {
  loading.value = true
  try {
    await loadUserProjects()
    const [opsRes, projRes, teamsRes, reqsRes, pendingRes, allRes, unassignedRes] = await Promise.all([
      operatorsApi.getAll(),
      structureApi.getAll(),
      teamsApi.getAll(),
      teamsApi.getPendingRequests(),
      projectTransferApi.getPendingRequests(),
      projectTransferApi.getAllRequests(),
      teamsApi.getUnassigned ? teamsApi.getUnassigned() : Promise.resolve({ data: [] })
    ])
    operators.value = filterOperators(opsRes.data || [])
    allOperators.value = filterOperators(opsRes.data || [])
    projects.value = projRes.data || []
    teamsList.value = teamsRes.data || []
    pendingRequests.value = reqsRes.data || []
    pendingTransfers.value = pendingRes.data || []
    allTransfersList.value = allRes.data || []
    unassignedOperators.value = filterOperators(unassignedRes.data || [])
  } catch (e) {
    console.error('Error fetching TeamsView data:', e)
  } finally {
    loading.value = false
  }
}

const claimOperator = async (op) => {
  // Find current user's team or the first available team
  const userTeam = teamsList.value.find(t => t.teamLeader === auth.user?.name) || teamsList.value[0]
  if (!userTeam) {
    showBanner("Aucune équipe disponible. Veuillez contacter l'administrateur.", false)
    return
  }
  try {
    await teamsApi.assignChef(op.id, userTeam.id)
    showBanner(`L'opérateur ${op.lastName} ${op.firstName} a été ajouté à l'équipe ${userTeam.teamLeader || userTeam.name}.`)
    await fetchData()
  } catch (e) {
    showBanner("Erreur lors de l'affectation de l'opérateur.", false)
  }
}

const showBanner = (msg, success = true) => {
  bannerMsg.value = msg
  bannerSuccess.value = success
  setTimeout(() => { bannerMsg.value = '' }, 5000)
}

// Team update actions
const submitTeamUpdate = async () => {
  if (!selectedTeamId.value) return
  try {
    const res = await teamsApi.requestUpdate(selectedTeamId.value, selectedOpIds.value)
    showBanner(res.data?.message || 'Mise à jour traitée', res.data?.status === 'APPROVED')
    showModal.value = false
    await fetchData()
  } catch (e) {
    showBanner(e.response?.data?.message || 'Erreur lors de la mise à jour', false)
  }
}

const approveRequest = async (requestId) => {
  try {
    const res = await teamsApi.approveRequest(requestId)
    showBanner(res.data?.message || 'Demande validée')
    await fetchData()
  } catch (e) { console.error(e) }
}

const rejectRequest = async (requestId) => {
  try {
    const res = await teamsApi.rejectRequest(requestId)
    showBanner(res.data?.message || 'Demande refusée', false)
    await fetchData()
  } catch (e) { console.error(e) }
}

// Transfer actions
const submitTransferRequest = async () => {
  if (!canSubmitTransfer.value) return
  try {
    if (transferType.value === 'shift') {
      // Shift-only: immediate change (Supervisor only)
      const res = await projectTransferApi.changeShift(
        selectedTransferOp.value.employeeId,
        transferTargetTeamId.value
      )
      showBanner(res.data?.message || 'Support Team mis à jour')
    } else {
      // Project transfer (with optional team)
      const res = await projectTransferApi.requestTransfer(
        selectedTransferOp.value.employeeId,
        transferTargetProjectId.value,
        transferTargetTeamId.value || null
      )
      showBanner(res.data?.message || 'Demande de transfert traitée', res.data?.status === 'APPROVED')
    }
    showTransferModal.value = false
    await fetchData()
  } catch (e) {
    showBanner(e.response?.data?.message || 'Erreur lors du transfert', false)
  }
}

const approveTransfer = async (requestId) => {
  try {
    const res = await projectTransferApi.approveRequest(requestId)
    showBanner(res.data?.message || 'Transfert validé avec succès')
    await fetchData()
  } catch (e) { console.error(e) }
}

const rejectTransfer = async (requestId) => {
  try {
    const res = await projectTransferApi.rejectRequest(requestId)
    showBanner(res.data?.message || 'Transfert refusé', false)
    await fetchData()
  } catch (e) { console.error(e) }
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
