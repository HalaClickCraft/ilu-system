<template>
  <div class="space-y-6">
    <!-- Standalone Page Header (Hidden when embedded) -->
    <div v-if="!embeddedMode" class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">
          {{ activeTab === 'transfers' ? 'Transferts des Opérateurs' : 'Équipes de Production' }}
        </h1>
        <p class="text-gray-500 mt-1">
          {{ activeTab === 'transfers' ? "Gérer et valider les demandes de transfert d'opérateurs (projet ou shift)." : "Gérer les affectations et la composition des équipes de shift." }}
        </p>
      </div>
      <!-- ONE single action button per tab -->
      <div class="flex items-center gap-2">
        <button
          v-if="activeTab === 'teams' && isSupervisor"
          @click="openCreateTeamModal"
          class="inline-flex items-center gap-2 rounded-lg bg-emerald-600 px-4 py-2 text-sm font-medium text-white hover:bg-emerald-700 shadow-sm transition"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"/></svg>
          Créer une Équipe
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

    <!-- Embedded Action Header for StructureView / Transferts -->
    <div v-else-if="embeddedMode === 'teams'" class="flex justify-end pt-1">
      <button
        v-if="isSupervisor"
        @click="openCreateTeamModal"
        class="inline-flex items-center gap-2 rounded-lg bg-emerald-600 px-4 py-2 text-sm font-medium text-white hover:bg-emerald-700 shadow-sm transition"
      >
        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"/></svg>
        Créer une Équipe
      </button>
    </div>

    <div v-else-if="embeddedMode === 'transfers'" class="flex justify-between items-center pt-1">
      <div>
        <h2 class="text-xl font-bold text-gray-900">Transferts des Opérateurs</h2>
        <p class="text-xs text-gray-500">Demandes de transfert de projet ou de Support Team</p>
      </div>
      <button
        v-if="canManageOrRequest"
        @click="openTransferModal"
        class="inline-flex items-center gap-2 rounded-lg bg-emerald-600 px-4 py-2 text-sm font-medium text-white hover:bg-emerald-700 shadow-sm transition"
      >
        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7h12m0 0l-4-4m4 4l-4 4m0 6H4m0 0l4 4m-4-4l4-4"/></svg>
        Demander un Transfert
      </button>
    </div>

    <!-- Tabs (Only shown when NOT embedded) -->
    <div v-if="!embeddedMode" class="flex gap-2 border-b border-gray-200">
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

      <div v-else-if="!loading && groups.length === 0" class="bg-white rounded-xl shadow-sm border border-gray-200 p-12 text-center text-gray-400">
        {{ isSupervisor ? "Aucune équipe configurée pour le moment." : "Vous n'avez aucune équipe affectée. Seul un Superviseur ou un Admin peut vous créer une équipe." }}
      </div>
      <div v-if="!loading && groups.length > 0" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
        <div v-for="group in groups" :key="group.teamId || '_none'" class="bg-white rounded-xl shadow-xs border border-gray-200 p-5 relative flex flex-col justify-between hover:shadow-md transition-shadow">
          <div class="space-y-3">
            <!-- Card Header -->
            <div class="flex items-start justify-between gap-2 border-b border-gray-100 pb-3">
              <div>
                <h3 class="font-bold text-gray-900 flex items-center gap-2 text-base">
                  <svg class="w-4 h-4 text-emerald-600 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"/></svg>
                  {{ group.teamName }}
                </h3>
                <div class="flex flex-wrap gap-1.5 mt-1.5">
                  <span class="inline-block text-[11px] font-semibold px-2 py-0.5 rounded-full bg-blue-50 text-blue-700 border border-blue-200">
                    🎯 Projets: {{ getTeamProjectNames(group.team) }}
                  </span>
                </div>
              </div>
              <div class="flex items-center gap-1">
                <button v-if="group.team && isSupervisor" @click="openEditExistingTeamModal(group.team)"
                  class="p-1.5 text-gray-400 hover:text-emerald-600 hover:bg-emerald-50 rounded-lg transition" title="Modifier l'équipe & affectations">
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"/></svg>
                </button>
                <button v-if="group.team && isSupervisor" @click="confirmDeleteTeam(group.team)"
                  class="p-1.5 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition" title="Supprimer cette équipe">
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"/></svg>
                </button>
              </div>
            </div>

            <!-- Staff role details -->
            <div v-if="group.team" class="text-xs text-gray-600 space-y-1.5 bg-slate-50 p-3 rounded-lg border border-slate-100">
              <p v-if="group.team.teamLeader"><strong>Chef d'Équipe :</strong> {{ group.team.teamLeader }} <span class="text-gray-400 font-mono">({{ group.team.teamLeaderEmployeeId }})</span></p>
              
              <!-- Agent(s) Qualite & Projets couverts -->
              <div v-if="group.team.agentQualite" class="pt-1.5 border-t border-slate-200/60">
                <strong class="text-slate-700 block mb-1">🛡️ Agent(s) Qualité & Projets :</strong>
                <div class="space-y-1 pl-1">
                  <div v-for="(aq, idx) in parseAgentQualiteItems(group.team.agentQualite, group.team.agentQualiteEmployeeId)" :key="idx"
                    class="flex items-center justify-between bg-white px-2 py-0.5 rounded border border-slate-200 text-[11px]">
                    <span class="font-medium text-slate-800">{{ aq.name }} <span class="text-slate-400 font-mono">({{ aq.employeeId }})</span></span>
                    <span class="px-1.5 py-0.5 rounded text-[10px] font-bold bg-purple-100 text-purple-800 border border-purple-200">
                      🎯 {{ aq.project ? aq.project : 'Tous projets' }}
                    </span>
                  </div>
                </div>
              </div>

              <p v-if="group.team.qualityManager" class="pt-1 border-t border-slate-200/60"><strong>Resp. Qualité :</strong> {{ group.team.qualityManager }} <span class="text-gray-400 font-mono">({{ group.team.qualityManagerEmployeeId }})</span></p>
              <p v-if="group.team.projectManager"><strong>Superviseur :</strong> {{ group.team.projectManager }} <span class="text-gray-400 font-mono">({{ group.team.projectManagerEmployeeId }})</span></p>
              <p v-if="group.team.hseManager"><strong>Resp. HSE :</strong> {{ group.team.hseManager }} <span class="text-gray-400 font-mono">({{ group.team.hseManagerEmployeeId }})</span></p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- ======================== TAB 2: Transferts ======================== -->
    <div v-if="activeTab === 'transfers'" class="space-y-6">

      <!-- ALL TRANSFERS HISTORY (Positioned First) -->
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
                <td class="px-4 py-3 text-xs text-gray-400">{{ formatDate(req.createdAt) }}</td>
                <td class="px-4 py-3">
                  <span class="px-2 py-0.5 rounded-full text-xs font-semibold"
                    :class="{
                      'bg-amber-100 text-amber-800': req.status === 'PENDING',
                      'bg-emerald-100 text-emerald-800': req.status === 'APPROVED',
                      'bg-red-100 text-red-800': req.status === 'REJECTED'
                    }">
                    {{ req.status === 'PENDING' ? 'En Attente' : req.status === 'APPROVED' ? 'Validé' : 'Refusé' }}
                  </span>
                </td>
                <td class="px-4 py-3 text-xs text-gray-600">{{ req.approvedBy || req.requestedBy || 'Système' }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

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
    </div>

    <!-- ======================== MODAL: Créer / Modifier une Équipe ======================== -->
    <div v-if="showCreateTeamModal" class="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 backdrop-blur-xs p-4">
      <div class="bg-white rounded-xl shadow-xl border border-gray-200 max-w-md w-full p-6 space-y-4 max-h-[90vh] overflow-y-auto">
        <div class="flex items-center justify-between border-b border-gray-100 pb-3">
          <h3 class="font-bold text-gray-900 text-lg">
            {{ editingTeamId ? 'Modifier l\'Équipe' : 'Créer une Équipe' }}
          </h3>
          <button @click="showCreateTeamModal = false" class="text-gray-400 hover:text-gray-600 text-xl leading-none">✕</button>
        </div>
        <form @submit.prevent="submitCreateTeam" class="space-y-3">
          <div>
            <label class="block text-xs font-semibold text-gray-700 mb-1">Chef d'Équipe (Pivot du Shift) * :</label>
            <select v-model="createTeamForm.teamLeaderEmployeeId" @change="onChefSelect($event.target.value)" required
              class="w-full px-3 py-2 border border-emerald-300 rounded-lg text-xs font-semibold outline-none focus:ring-2 focus:ring-emerald-500 bg-emerald-50/40 text-emerald-900">
              <option value="">-- Sélectionner le Chef d'Équipe --</option>
              <option v-for="u in getUsersForRole('CHEF_EQUIPE')" :key="u.employeeId" :value="u.employeeId">
                👤 {{ u.name }} (Matricule: {{ u.employeeId }})
              </option>
            </select>
          </div>
          <div v-if="createTeamForm.teamLeader" class="p-2 rounded-lg bg-emerald-50 text-emerald-800 text-xs font-bold border border-emerald-200 flex items-center gap-2">
            <svg class="w-4 h-4 text-emerald-600 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/></svg>
            Équipe définie : Équipe {{ createTeamForm.teamLeader }}
          </div>
          <div>
            <label class="block text-xs font-semibold text-gray-700 mb-1">Projets couverts par ce shift / cette équipe :</label>
            <div class="grid grid-cols-2 gap-2 bg-slate-50 p-2.5 rounded-lg border border-slate-200 text-xs">
              <label v-for="p in projects" :key="p.id" class="flex items-center gap-1.5 cursor-pointer font-medium text-gray-700 hover:text-emerald-600">
                <input type="checkbox" :value="p.id" v-model="createTeamForm.projectIds" class="rounded text-emerald-600 focus:ring-emerald-500" />
                🎯 {{ p.name }}
              </label>
            </div>
          </div>
          <div>
            <label class="block text-xs font-semibold text-gray-700 mb-1">Agent(s) Qualité et répartition des Projets sur ce shift :</label>
            <div class="space-y-2.5 bg-slate-50 p-2.5 rounded-lg border border-slate-200 text-xs max-h-48 overflow-y-auto">
              <div v-for="u in getUsersForRole('AGENT_QUALITE')" :key="u.employeeId" class="border-b border-gray-200/60 pb-2 last:border-0 last:pb-0 space-y-1.5">
                <div class="flex items-center justify-between">
                  <label class="flex items-center gap-1.5 cursor-pointer font-medium text-gray-700 hover:text-emerald-600">
                    <input type="checkbox" :value="u.employeeId" v-model="createTeamForm.agentQualiteEmployeeIds" @change="onAgentsQualiteChange" class="rounded text-emerald-600 focus:ring-emerald-500" />
                    🛡️ {{ u.name }} <span class="text-gray-400 font-mono text-[10px]">({{ u.employeeId }})</span>
                  </label>
                  <span v-if="createTeamForm.agentQualiteEmployeeIds && createTeamForm.agentQualiteEmployeeIds.includes(u.employeeId)" class="text-[10px] text-emerald-700 font-semibold bg-emerald-50 px-1.5 py-0.5 rounded border border-emerald-200">
                    Actif sur ce shift
                  </span>
                </div>
                <!-- Multi-project assignment checkboxes for this specific AQ -->
                <div v-if="createTeamForm.agentQualiteEmployeeIds && createTeamForm.agentQualiteEmployeeIds.includes(u.employeeId)" class="pl-5 pt-1 flex flex-wrap items-center gap-1.5 bg-white p-2 rounded-lg border border-slate-200">
                  <span class="text-[10px] text-slate-500 font-semibold mr-1">Projets assignés :</span>
                  <label v-for="p in selectedProjectsForShift" :key="p.id"
                    class="inline-flex items-center gap-1 px-2 py-0.5 rounded text-[11px] cursor-pointer border transition"
                    :class="(createTeamForm.agentQualiteProjectMap[u.employeeId] || []).includes(p.name) ? 'bg-emerald-100 border-emerald-400 text-emerald-900 font-bold' : 'bg-gray-50 border-gray-200 text-gray-600 hover:bg-gray-100'">
                    <input type="checkbox" :checked="(createTeamForm.agentQualiteProjectMap[u.employeeId] || []).includes(p.name)" @change="toggleAqProject(u.employeeId, p.name)" class="hidden" />
                    🎯 {{ p.name }}
                  </label>
                  <button type="button" @click="selectAllProjectsForAq(u.employeeId)" class="text-[10px] text-emerald-600 hover:text-emerald-700 font-semibold ml-1">Tous</button>
                  <button type="button" @click="clearProjectsForAq(u.employeeId)" class="text-[10px] text-gray-400 hover:text-gray-600 font-medium ml-1">Effacer</button>
                </div>
              </div>
              <div v-if="getUsersForRole('AGENT_QUALITE').length === 0" class="text-xs text-gray-400 italic">Aucun Agent Qualité configuré.</div>
            </div>
          </div>
          <div>
            <label class="block text-xs font-semibold text-gray-700 mb-1">Responsable Qualité :</label>
            <select v-model="createTeamForm.qualityManagerEmployeeId" @change="onQualityManagerSelect($event.target.value)"
              class="w-full px-3 py-2 border border-gray-200 rounded-lg text-xs outline-none focus:ring-2 focus:ring-emerald-500">
              <option value="">-- Choisir un Responsable Qualité --</option>
              <option v-for="u in getUsersForRole('RESP_QUALITE')" :key="u.employeeId" :value="u.employeeId">
                {{ u.name }} ({{ u.employeeId }})
              </option>
            </select>
          </div>
          <div>
            <label class="block text-xs font-semibold text-gray-700 mb-1">Superviseur :</label>
            <select v-model="createTeamForm.projectManagerEmployeeId" @change="onSupervisorSelect($event.target.value)"
              class="w-full px-3 py-2 border border-gray-200 rounded-lg text-xs outline-none focus:ring-2 focus:ring-emerald-500">
              <option value="">-- Choisir un Superviseur --</option>
              <option v-for="u in getUsersForRole('SUPERVISEUR')" :key="u.employeeId" :value="u.employeeId">
                {{ u.name }} ({{ u.employeeId }})
              </option>
            </select>
          </div>
          <div>
            <label class="block text-xs font-semibold text-gray-700 mb-1">Responsable HSE :</label>
            <select v-model="createTeamForm.hseManagerEmployeeId" @change="onHseSelect($event.target.value)"
              class="w-full px-3 py-2 border border-gray-200 rounded-lg text-xs outline-none focus:ring-2 focus:ring-emerald-500">
              <option value="">-- Choisir un Responsable HSE --</option>
              <option v-for="u in getUsersForRole('RESP_HSE')" :key="u.employeeId" :value="u.employeeId">
                {{ u.name }} ({{ u.employeeId }})
              </option>
            </select>
          </div>
          <div class="flex items-center justify-end gap-2 pt-3 border-t border-gray-100">
            <button type="button" @click="showCreateTeamModal = false" class="px-4 py-2 text-xs font-medium text-gray-600 hover:bg-gray-100 rounded-lg">Annuler</button>
            <button type="submit" :disabled="!createTeamForm.teamLeaderEmployeeId"
              class="px-4 py-2 text-xs font-semibold text-white bg-emerald-600 hover:bg-emerald-700 rounded-lg shadow-sm disabled:opacity-50">
              Enregistrer l'Équipe
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- ======================== MODAL: Transfert de Projet ======================== -->
    <div v-if="showTransferModal" class="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 backdrop-blur-xs p-4">
      <div class="bg-white rounded-xl shadow-xl border border-gray-200 max-w-lg w-full p-6 space-y-4">
        <div class="flex items-center justify-between border-b border-gray-100 pb-3">
          <h3 class="font-bold text-gray-900 text-lg flex items-center gap-2">
            <svg class="w-5 h-5 text-emerald-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7h12m0 0l-4-4m4 4l-4 4m0 6H4m0 0l4 4m-4-4l4-4"/></svg>
            Demande de Transfert de Projet
          </h3>
          <button @click="showTransferModal = false" class="text-gray-400 hover:text-gray-600 text-xl leading-none">✕</button>
        </div>

        <div v-if="!isSupervisor" class="p-3 bg-blue-50 border border-blue-200 rounded-lg text-xs text-blue-800">
          ℹ️ Cette demande sera soumise au <strong>Superviseur</strong> pour validation.
        </div>
        <div v-else class="p-3 bg-emerald-50 border border-emerald-200 rounded-lg text-xs text-emerald-800">
          ✅ En tant que Superviseur / Admin, ce transfert de projet sera appliqué immédiatement.
        </div>

        <div class="space-y-4">
          <!-- Operator search -->
          <div>
            <label class="block text-xs font-semibold text-gray-700 mb-1">Opérateur à transférer * :</label>
            <input v-model="transferSearchText" type="text" placeholder="Saisir matricule ou nom de l'opérateur..."
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
            <p><strong>Chef d'Équipe :</strong> {{ selectedTransferOp.team?.name || selectedTransferOp.team?.teamLeader || 'Aucun' }}</p>
          </div>

          <!-- Target Project field -->
          <div>
            <label class="block text-xs font-semibold text-gray-700 mb-1">Projet Cible * :</label>
            <select v-model="transferTargetProjectId"
              class="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm outline-none focus:ring-2 focus:ring-emerald-500">
              <option value="" disabled>-- Choisir le projet cible --</option>
              <option v-for="p in projects" :key="p.id" :value="p.id">{{ p.name }}</option>
            </select>
          </div>
        </div>

        <div class="flex items-center justify-end gap-2 pt-3 border-t border-gray-100">
          <button @click="showTransferModal = false" class="px-4 py-2 text-xs font-medium text-gray-600 hover:bg-gray-100 rounded-lg">Annuler</button>
          <button @click="submitTransferRequest"
            :disabled="!canSubmitTransfer"
            class="px-4 py-2 text-xs font-semibold text-white bg-emerald-600 hover:bg-emerald-700 rounded-lg shadow-sm disabled:opacity-50">
            {{ isSupervisor ? '🔀 Transférer immédiatement' : '📤 Soumettre au Superviseur' }}
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

import { useRoute } from 'vue-router'

const props = defineProps({
  embeddedMode: {
    type: String,
    default: null
  }
})

const auth = useAuthStore()
const route = useRoute()
const { loadUserProjects, filterOperators } = useUserScope()
const loading = ref(true)
const activeTab = ref(props.embeddedMode || (route.query.tab === 'transfers' ? 'transfers' : 'teams'))

watch(() => props.embeddedMode, (val) => {
  if (val) activeTab.value = val
})

watch(() => route.query.tab, (newTab) => {
  if (!props.embeddedMode) {
    activeTab.value = newTab === 'transfers' ? 'transfers' : 'teams'
  }
})

const operators = ref([])
const allOperators = ref([])
const projects = ref([])
const teamsList = ref([])
const systemUsers = ref([])
const pendingRequests = ref([])    // team-update requests
const pendingTransfers = ref([])   // project-transfer requests pending approval
const allTransfersList = ref([])   // full history

// Team update modal
const showModal = ref(false)
const selectedTeamId = ref('')
const selectedOpIds = ref([])
const operatorInputText = ref('')

// Create Team modal
const showCreateTeamModal = ref(false)
const createTeamForm = ref({
  name: '',
  projectId: '',
  teamLeader: '',
  teamLeaderEmployeeId: '',
  agentQualite: '',
  agentQualiteEmployeeId: '',
  agentQualiteEmployeeIds: [],
  agentQualiteProjectMap: {},
  qualityManager: '',
  qualityManagerEmployeeId: '',
  projectManager: '',
  projectManagerEmployeeId: '',
  hseManager: '',
  hseManagerEmployeeId: ''
})

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

// Group operators by Chef d'Équipe (Team) - includes all teams and lists assigned operators
const groups = computed(() => {
  const map = {}
  let visibleTeams = teamsList.value || []

  if (!isSupervisor.value && auth.hasAnyRole(['CHEF_EQUIPE'])) {
    const userEmpId = auth.user?.employeeId
    const userName = auth.user?.name ? auth.user.name.toLowerCase() : ''
    const myTeams = visibleTeams.filter(t =>
      (t.teamLeaderEmployeeId && t.teamLeaderEmployeeId === userEmpId) ||
      (t.teamLeader && t.teamLeader.toLowerCase() === userName)
    )
    if (myTeams.length > 0) {
      visibleTeams = myTeams
    }
  }

  visibleTeams.forEach(t => {
    map[t.id] = {
      teamId: t.id,
      teamName: t.name,
      teamLeader: t.teamLeader || '',
      team: t,
      operators: []
    }
  })

  // Assign operators to their teams
  const ops = allOperators.value || []
  ops.forEach(op => {
    if (op.team && map[op.team.id]) {
      map[op.team.id].operators.push(op)
    }
  })

  return Object.values(map).sort((a, b) => {
    if (!a.teamId) return 1
    if (!b.teamId) return -1
    return a.teamName.localeCompare(b.teamName)
  })
})

const selectedOperatorsList = computed(() => {
  const ids = selectedOpIds.value || []
  return (allOperators.value || []).filter(op => ids.includes(op.id))
})

const matchingSuggestions = computed(() => {
  const q = operatorInputText.value.trim().toLowerCase()
  if (!q) return []
  return (allOperators.value || []).filter(op =>
    ((op.employeeId && op.employeeId.toLowerCase().includes(q)) ||
     (op.lastName && op.lastName.toLowerCase().includes(q)) ||
     (op.firstName && op.firstName.toLowerCase().includes(q))) &&
    !selectedOpIds.value.includes(op.id)
  ).slice(0, 8)
})

const transferSuggestions = computed(() => {
  const q = transferSearchText.value.trim().toLowerCase()
  if (!q) return []
  return (allOperators.value || []).filter(op =>
    (op.employeeId && op.employeeId.toLowerCase().includes(q)) ||
    (op.lastName && op.lastName.toLowerCase().includes(q)) ||
    (op.firstName && op.firstName.toLowerCase().includes(q)) ||
    `${op.lastName} ${op.firstName}`.toLowerCase().includes(q)
  ).slice(0, 8)
})

const teamsForCurrentProject = computed(() => {
  if (!selectedTransferOp.value?.project?.id) return teamsList.value || []
  const currentProjId = selectedTransferOp.value.project.id
  return (teamsList.value || []).filter(t => {
    if (t.projects && t.projects.some(p => p.id === currentProjId)) return true
    if (t.project && t.project.id === currentProjId) return true
    return false
  })
})

const canSubmitTransfer = computed(() => {
  return Boolean(selectedTransferOp.value && transferTargetProjectId.value)
})

const editingTeamId = ref(null)

const getTeamProjectNames = (team) => {
  if (!team) return 'Aucun projet'
  if (team.projects && team.projects.length > 0) {
    return team.projects.map(p => p.name).join(', ')
  }
  if (team.project) {
    return team.project.name
  }
  return 'Tous les projets'
}

const openEditExistingTeamModal = (team) => {
  editingTeamId.value = team.id
  const pIds = team.projects ? team.projects.map(p => p.id) : (team.project ? [team.project.id] : [])
  const aqEmpIds = team.agentQualiteEmployeeId ? team.agentQualiteEmployeeId.split(',').map(s => s.trim()).filter(Boolean) : []
  const aqMap = {}
  if (team.agentQualite && team.agentQualite.includes('[Projet:')) {
    const parts = team.agentQualite.split(/(?<=\]),\s*/)
    parts.forEach(part => {
      const m = part.match(/\[Projet:\s*([^\]]+)\]/)
      if (m) {
        const uName = part.split('[Projet:')[0].trim()
        const user = (systemUsers.value || []).find(u => u.name === uName)
        if (user) {
          aqMap[user.employeeId] = m[1].split(',').map(s => s.trim()).filter(Boolean)
        }
      }
    })
  }
  createTeamForm.value = {
    name: team.name,
    projectIds: pIds,
    teamLeader: team.teamLeader || '',
    teamLeaderEmployeeId: team.teamLeaderEmployeeId || '',
    agentQualite: team.agentQualite || '',
    agentQualiteEmployeeId: team.agentQualiteEmployeeId || '',
    agentQualiteEmployeeIds: aqEmpIds,
    agentQualiteProjectMap: aqMap,
    qualityManager: team.qualityManager || '',
    qualityManagerEmployeeId: team.qualityManagerEmployeeId || '',
    projectManager: team.projectManager || '',
    projectManagerEmployeeId: team.projectManagerEmployeeId || '',
    hseManager: team.hseManager || '',
    hseManagerEmployeeId: team.hseManagerEmployeeId || ''
  }
  showCreateTeamModal.value = true
}

const openCreateTeamModal = () => {
  editingTeamId.value = null
  createTeamForm.value = {
    name: '',
    projectIds: projects.value.map(p => p.id),
    teamLeader: auth.user?.name || '',
    teamLeaderEmployeeId: auth.user?.employeeId || '',
    agentQualite: '',
    agentQualiteEmployeeId: '',
    agentQualiteEmployeeIds: [],
    agentQualiteProjectMap: {},
    qualityManager: '',
    qualityManagerEmployeeId: '',
    projectManager: '',
    projectManagerEmployeeId: '',
    hseManager: '',
    hseManagerEmployeeId: ''
  }
  showCreateTeamModal.value = true
}

const openTeamModal = (teamId) => {
  selectedTeamId.value = teamId
  operatorInputText.value = ''
  const team = teamsList.value.find(t => t.id === Number(teamId))
  selectedOpIds.value = team?.operators?.map(o => o.id) || (allOperators.value || []).filter(o => o.team?.id === Number(teamId)).map(o => o.id)
  showModal.value = true
}

const submitCreateTeam = async () => {
  if (!createTeamForm.value.teamLeaderEmployeeId) return
  try {
    const leaderName = createTeamForm.value.teamLeader || 'Chef d\'Équipe'
    const teamName = `Équipe ${leaderName}`
    const selectedProjects = (createTeamForm.value.projectIds || []).map(id => ({ id: Number(id) }))
    const payload = {
      name: teamName,
      teamLeader: createTeamForm.value.teamLeader || null,
      teamLeaderEmployeeId: createTeamForm.value.teamLeaderEmployeeId || null,
      agentQualite: createTeamForm.value.agentQualite || null,
      agentQualiteEmployeeId: createTeamForm.value.agentQualiteEmployeeId || null,
      qualityManager: createTeamForm.value.qualityManager || null,
      qualityManagerEmployeeId: createTeamForm.value.qualityManagerEmployeeId || null,
      projectManager: createTeamForm.value.projectManager || null,
      projectManagerEmployeeId: createTeamForm.value.projectManagerEmployeeId || null,
      hseManager: createTeamForm.value.hseManager || null,
      hseManagerEmployeeId: createTeamForm.value.hseManagerEmployeeId || null,
      project: selectedProjects[0] || null,
      projects: selectedProjects
    }

    if (editingTeamId.value) {
      await teamsApi.updateTeam(editingTeamId.value, payload)
      showBanner(`L'équipe "${teamName}" a été mise à jour avec succès !`)
    } else {
      await teamsApi.createTeam(payload)
      showBanner(`L'équipe "${teamName}" a été créée avec succès !`)
    }

    showCreateTeamModal.value = false
    editingTeamId.value = null
    await fetchData()
  } catch (e) {
    showBanner(e.response?.data?.message || "Erreur lors de l'enregistrement de l'équipe", false)
  }
}

const confirmDeleteTeam = async (team) => {
  if (!confirm(`Voulez-vous vraiment supprimer l'équipe "${team.name}" ?`)) return
  try {
    await teamsApi.deleteTeam(team.id)
    showBanner(`L'équipe "${team.name}" a été supprimée avec succès.`)
    await fetchData()
  } catch (e) {
    showBanner(e.response?.data?.message || "Erreur lors de la suppression de l'équipe", false)
  }
}

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

const selectedProjectsForShift = computed(() => {
  const pIds = (createTeamForm.value.projectIds || []).map(id => Number(id))
  if (!pIds.length) return projects.value || []
  return (projects.value || []).filter(p => pIds.includes(p.id))
})

function toggleAqProject(empId, projectName) {
  if (!createTeamForm.value.agentQualiteProjectMap) {
    createTeamForm.value.agentQualiteProjectMap = {}
  }
  if (!Array.isArray(createTeamForm.value.agentQualiteProjectMap[empId])) {
    createTeamForm.value.agentQualiteProjectMap[empId] = []
  }
  const list = createTeamForm.value.agentQualiteProjectMap[empId]
  const idx = list.indexOf(projectName)
  if (idx > -1) {
    list.splice(idx, 1)
  } else {
    list.push(projectName)
  }
  onAgentsQualiteChange()
}

function selectAllProjectsForAq(empId) {
  if (!createTeamForm.value.agentQualiteProjectMap) {
    createTeamForm.value.agentQualiteProjectMap = {}
  }
  createTeamForm.value.agentQualiteProjectMap[empId] = selectedProjectsForShift.value.map(p => p.name)
  onAgentsQualiteChange()
}

function clearProjectsForAq(empId) {
  if (!createTeamForm.value.agentQualiteProjectMap) {
    createTeamForm.value.agentQualiteProjectMap = {}
  }
  createTeamForm.value.agentQualiteProjectMap[empId] = []
  onAgentsQualiteChange()
}

const parseAgentQualiteItems = (agentQualiteStr, empIdsStr) => {
  if (!agentQualiteStr) return []
  const items = agentQualiteStr.split(/(?<=\]),\s*|,\s*(?=[^\]]*(?:\[|$))/).map(s => s.trim()).filter(Boolean)
  const empIds = (empIdsStr || '').split(',').map(s => s.trim()).filter(Boolean)

  return items.map((item, idx) => {
    let name = item
    let project = null
    const match = item.match(/^(.*?)\s*\[(?:Projet:\s*)?(.*?)\]$/)
    if (match) {
      name = match[1].trim()
      project = match[2].trim()
    }
    return { name, employeeId: empIds[idx] || '', project }
  })
}

const getMultiProjectSplitLabel = (projList) => {
  if (!projList || !projList.length) return ''
  const pct = Math.floor(100 / projList.length)
  return projList.map(p => `${pct}% ${p.name}`).join(' + ')
}

const getUsersForRole = (roleLabel) => {
  if (!systemUsers.value || !systemUsers.value.length) return []
  return systemUsers.value.filter(u => {
    if (!u.roles) return false
    const rolesList = u.roles.map(r => (typeof r === 'object' ? r.label || r.name : String(r)))
    return rolesList.includes(roleLabel) || rolesList.includes('ADMIN')
  })
}

function onChefSelect(empId) {
  const u = (systemUsers.value || []).find(user => user.employeeId === empId)
  createTeamForm.value.teamLeader = u ? u.name : ''
  createTeamForm.value.teamLeaderEmployeeId = u ? u.employeeId : empId
}

function onAgentsQualiteChange() {
  const selectedEmpIds = createTeamForm.value.agentQualiteEmployeeIds || []
  const selectedUsers = (systemUsers.value || []).filter(u => selectedEmpIds.includes(u.employeeId))
  if (!createTeamForm.value.agentQualiteProjectMap) {
    createTeamForm.value.agentQualiteProjectMap = {}
  }
  const formattedNames = selectedUsers.map(u => {
    const projs = createTeamForm.value.agentQualiteProjectMap[u.employeeId]
    if (Array.isArray(projs) && projs.length > 0) {
      return `${u.name} [Projet: ${projs.join(', ')}]`
    } else if (typeof projs === 'string' && projs.trim()) {
      return `${u.name} [Projet: ${projs.trim()}]`
    }
    return `${u.name} [Tous projets]`
  }).join(', ')

  createTeamForm.value.agentQualite = formattedNames
  createTeamForm.value.agentQualiteEmployeeId = selectedEmpIds.join(', ')
}

function onQualityManagerSelect(empId) {
  const u = systemUsers.value.find(user => user.employeeId === empId)
  createTeamForm.value.qualityManager = u ? u.name : ''
  createTeamForm.value.qualityManagerEmployeeId = u ? u.employeeId : empId
}

function onSupervisorSelect(empId) {
  const u = systemUsers.value.find(user => user.employeeId === empId)
  createTeamForm.value.projectManager = u ? u.name : ''
  createTeamForm.value.projectManagerEmployeeId = u ? u.employeeId : empId
}

function onHseSelect(empId) {
  const u = systemUsers.value.find(user => user.employeeId === empId)
  createTeamForm.value.hseManager = u ? u.name : ''
  createTeamForm.value.hseManagerEmployeeId = u ? u.employeeId : empId
}

// Data fetching
const fetchData = async () => {
  loading.value = true
  try {
    await loadUserProjects()
    const [opsRes, projRes, teamsRes, reqsRes, pendingRes, allRes, unassignedRes, usersRes] = await Promise.all([
      operatorsApi.getAll(),
      structureApi.getAll(),
      teamsApi.getAll(),
      teamsApi.getPendingRequests(),
      projectTransferApi.getPendingRequests(),
      projectTransferApi.getAllRequests(),
      teamsApi.getUnassigned ? teamsApi.getUnassigned() : Promise.resolve({ data: [] }),
      structureApi.getAvailableUsers()
    ])
    operators.value = filterOperators(opsRes.data || [])
    allOperators.value = filterOperators(opsRes.data || [])
    projects.value = projRes.data || []
    teamsList.value = teamsRes.data || []
    pendingRequests.value = reqsRes.data || []
    pendingTransfers.value = pendingRes.data || []
    allTransfersList.value = allRes.data || []
    unassignedOperators.value = filterOperators(unassignedRes.data || [])
    systemUsers.value = usersRes.data || []
  } catch (e) {
    console.error('Error fetching TeamsView data:', e)
  } finally {
    loading.value = false
  }
}

const claimOperator = async (op) => {
  let userTeam = teamsList.value.find(t =>
    (t.teamLeaderEmployeeId && t.teamLeaderEmployeeId === auth.user?.employeeId) ||
    (t.teamLeader && t.teamLeader.toLowerCase() === auth.user?.name?.toLowerCase())
  )
  if (!userTeam && auth.hasAnyRole(['SUPERVISEUR', 'ADMIN', 'RH'])) {
    userTeam = teamsList.value[0]
  }
  if (!userTeam) {
    showBanner("Seul un Chef d'Équipe configuré peut ajouter cet opérateur à son équipe.", false)
    return
  }
  try {
    await teamsApi.assignChef(op.id, userTeam.id)
    showBanner(`L'opérateur ${op.lastName} ${op.firstName} a été ajouté à votre équipe (${userTeam.teamLeader || userTeam.name}).`)
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
    const res = await projectTransferApi.requestTransfer(
      selectedTransferOp.value.employeeId,
      transferTargetProjectId.value,
      null
    )
    showBanner(res.data?.message || 'Demande de transfert de projet traitée', res.data?.status === 'APPROVED')
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
