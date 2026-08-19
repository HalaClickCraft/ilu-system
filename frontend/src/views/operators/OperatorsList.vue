<template>
  <div class="space-y-6">
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">Opérateurs</h1>
        <p class="text-gray-500 mt-1">Gestion des opérateurs de l'usine</p>
      </div>
      <button
        @click="openCreateModal"
        class="inline-flex items-center gap-2 bg-emerald-600 hover:bg-emerald-700 text-white px-4 py-2.5 rounded-lg text-sm font-medium transition-colors"
      >
        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"></path>
        </svg>
        Nouvel Opérateur
      </button>
    </div>
    <div class="bg-white rounded-xl shadow-sm border border-gray-200">
      <!-- Filters bar -->
      <div class="p-4 border-b border-gray-100 flex flex-col sm:flex-row gap-3 items-start sm:items-center">
        <div class="relative flex-1 max-w-sm">
          <svg class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path>
          </svg>
          <input
            v-model="search"
            type="text"
            placeholder="Rechercher un opérateur..."
            class="w-full pl-10 pr-4 py-2 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 focus:border-emerald-500 outline-none"
          />
        </div>
        <!-- FIX 5a: Always show project filter for multi-project roles -->
        <div v-if="showProjectFilter" class="flex items-center gap-2">
          <label class="text-sm font-medium text-gray-600 whitespace-nowrap">Projet:</label>
          <select
            v-model="selectedProjectFilter"
            class="px-3 py-2 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 focus:border-emerald-500 outline-none min-w-[180px]"
          >
            <option value="">Tous les projets</option>
            <option v-for="p in projectList" :key="p.id" :value="p.id">{{ p.name }}</option>
          </select>
        </div>
        <!-- Team filter -->
        <div class="flex items-center gap-2">
          <label class="text-sm font-medium text-gray-600 whitespace-nowrap">Équipe:</label>
          <select
            v-model="selectedTeamFilter"
            class="px-3 py-2 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 focus:border-emerald-500 outline-none min-w-[160px]"
          >
            <option value="">Toutes</option>
            <option v-for="t in teamList" :key="t.id" :value="t.id">{{ t.name }}</option>
          </select>
        </div>
      </div>

      <!-- Project group header when filter is active -->
      <div v-if="selectedProjectFilter && !loading" class="px-4 py-2 bg-emerald-50 border-b border-emerald-100">
        <span class="text-sm font-medium text-emerald-800">
          <svg class="w-4 h-4 inline -mt-0.5 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 7v10a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-6l-2-2H5a2 2 0 00-2 2z"></path></svg>
          {{ selectedProjectName }} — {{ filteredOperators.length }} opérateur(s)
        </span>
      </div>

      <div v-if="loading" class="flex items-center justify-center py-16">
        <div class="w-8 h-8 border-4 border-emerald-200 border-t-emerald-600 rounded-full animate-spin"></div>
      </div>

      <!-- Grouped by project view -->
      <div v-else-if="!selectedProjectFilter && showProjectFilter && !search && !selectedTeamFilter && groupedByProject.length" class="divide-y divide-gray-100">
        <div v-for="group in groupedByProject" :key="group.projectId || '_none'">
          <div class="px-4 py-2.5 bg-gray-50 flex items-center justify-between">
            <div class="flex items-center gap-2">
              <svg v-if="group.projectId" class="w-4 h-4 text-emerald-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 7v10a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-6l-2-2H5a2 2 0 00-2 2z"></path></svg>
              <svg v-else class="w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 12H4"></path></svg>
              <span class="text-sm font-semibold text-gray-700">{{ group.projectName }}</span>
            </div>
            <span class="text-xs text-gray-500 bg-white px-2 py-0.5 rounded-full border">{{ group.operators.length }} opérateur(s)</span>
          </div>
          <table class="w-full text-sm">
            <thead class="bg-gray-50/50">
              <tr>
                <th class="text-left py-2 px-4 font-medium text-gray-500 text-xs">Nom</th>
                <th class="text-left py-2 px-4 font-medium text-gray-500 text-xs">Matricule</th>
                <th class="text-left py-2 px-4 font-medium text-gray-500 text-xs">Équipe</th>
                <th class="text-left py-2 px-4 font-medium text-gray-500 text-xs">Date Sortie</th>
                <th class="text-left py-2 px-4 font-medium text-gray-500 text-xs">Statut</th>
                <th class="text-right py-2 px-4 font-medium text-gray-500 text-xs">Actions</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="op in group.operators" :key="op.id" class="border-b border-gray-50 hover:bg-gray-50/50">
                <td class="py-2.5 px-4">
                  <router-link :to="'/operators/' + op.id" class="font-medium text-emerald-600 hover:underline text-sm">{{ op.lastName }} {{ op.firstName }}</router-link>
                </td>
                <td class="py-2.5 px-4 text-gray-500 text-sm">{{ op.employeeId || '-' }}</td>
                <td class="py-2.5 px-4 text-gray-500 text-sm">{{ op.team?.name || '-' }}</td>
                <td class="py-2.5 px-4 text-sm" :class="op.exitDate ? 'text-red-600' : 'text-gray-400'">{{ formatDate(op.exitDate) }}</td>
                <td class="py-2.5 px-4">
                  <span class="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium" :class="op.active !== false ? 'bg-emerald-100 text-emerald-700' : 'bg-red-100 text-red-700'">{{ op.active !== false ? 'Actif' : 'Inactif' }}</span>
                </td>
                <td class="py-2.5 px-4 text-right space-x-2">
                  <button @click="$router.push('/operators/' + op.id)" class="text-gray-400 hover:text-emerald-600 transition" title="Détails">
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"></path><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"></path></svg>
                  </button>
                  <button v-if="auth.hasAnyRole(['ADMIN', 'RH'])" @click="openEditModal(op)" class="text-gray-400 hover:text-blue-600 transition" title="Modifier">
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"></path></svg>
                  </button>
                  <button v-if="op.active !== false" @click="deactivateOperator(op.id)" class="text-gray-400 hover:text-red-600 transition" title="Désactiver">
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M18.364 18.364A9 9 0 005.636 5.636m12.728 12.728A9 9 0 015.636 5.636m12.728 12.728L5.636 5.636"></path></svg>
                  </button>
                  <button v-else @click="activateOperator(op.id)" class="text-gray-400 hover:text-emerald-600 transition" title="Activer">
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path></svg>
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Flat table view -->
      <div v-else-if="filteredOperators.length" class="overflow-x-auto">
        <table class="w-full text-sm">
          <thead class="bg-gray-50">
            <tr>
              <th class="text-left py-3 px-4 font-medium text-gray-500">Nom</th>
              <th class="text-left py-3 px-4 font-medium text-gray-500">Matricule</th>
              <!-- FIX: Column order Projet → Zone → Poste (but operators table doesn't have zone/poste columns directly) -->
              <th v-if="showProjectColumn" class="text-left py-3 px-4 font-medium text-gray-500">Projet</th>
              <th class="text-left py-3 px-4 font-medium text-gray-500">Équipe</th>
              <th class="text-left py-3 px-4 font-medium text-gray-500">Date Embauche</th>
              <th class="text-left py-3 px-4 font-medium text-gray-500">Date Sortie</th>
              <th class="text-left py-3 px-4 font-medium text-gray-500">Statut</th>
              <th class="text-right py-3 px-4 font-medium text-gray-500">Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="op in filteredOperators" :key="op.id" class="border-b border-gray-50 hover:bg-gray-50">
              <td class="py-3 px-4">
                <router-link :to="'/operators/' + op.id" class="font-medium text-emerald-600 hover:underline">{{ op.lastName }} {{ op.firstName }}</router-link>
              </td>
              <td class="py-3 px-4 text-gray-500">{{ op.employeeId || '-' }}</td>
              <td v-if="showProjectColumn" class="py-3 px-4">
                <div class="flex flex-wrap gap-1">
                  <span v-for="proj in getOperatorProjects(op)" :key="proj" class="text-xs bg-emerald-50 text-emerald-700 px-2 py-0.5 rounded-full">{{ proj }}</span>
                  <span v-if="!getOperatorProjects(op).length" class="text-xs text-gray-400">-</span>
                </div>
              </td>
              <td class="py-3 px-4 text-gray-500">{{ op.team?.name || '-' }}</td>
              <td class="py-3 px-4 text-gray-500">{{ formatDate(op.hireDate) }}</td>
              <td class="py-3 px-4 text-sm" :class="op.exitDate ? 'text-red-600 font-medium' : 'text-gray-400'">{{ formatDate(op.exitDate) }}</td>
              <td class="py-3 px-4">
                <span class="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium" :class="op.active !== false ? 'bg-emerald-100 text-emerald-700' : 'bg-red-100 text-red-700'">{{ op.active !== false ? 'Actif' : 'Inactif' }}</span>
              </td>
              <td class="py-3 px-4 text-right space-x-2">
                <button @click="$router.push('/operators/' + op.id)" class="text-gray-400 hover:text-emerald-600 transition" title="Détails">
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"></path><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"></path></svg>
                </button>
                <button v-if="auth.hasAnyRole(['ADMIN', 'RH'])" @click="openEditModal(op)" class="text-gray-400 hover:text-blue-600 transition" title="Modifier">
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"></path></svg>
                </button>
                <button v-if="op.active !== false" @click="deactivateOperator(op.id)" class="text-gray-400 hover:text-red-600 transition" title="Désactiver">
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M18.364 18.364A9 9 0 005.636 5.636m12.728 12.728A9 9 0 015.636 5.636m12.728 12.728L5.636 5.636"></path></svg>
                </button>
                <button v-else @click="activateOperator(op.id)" class="text-gray-400 hover:text-emerald-600 transition" title="Activer">
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path></svg>
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div v-else class="text-center py-16 text-gray-400">Aucun opérateur trouvé</div>
    </div>

    <!-- Create Operator Modal -->
    <div v-if="showCreateModal" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50" @click.self="showCreateModal = false">
      <div class="bg-white rounded-2xl shadow-xl w-full max-w-lg mx-4 p-6">
        <h2 class="text-lg font-semibold text-gray-900 mb-4">Nouvel Opérateur</h2>
        <form @submit.prevent="createOperator" class="space-y-4">
          <div class="grid grid-cols-2 gap-3">
            <div><label class="block text-sm font-medium text-gray-700 mb-1">Nom</label><input v-model="form.lastName" required class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 outline-none" /></div>
            <div><label class="block text-sm font-medium text-gray-700 mb-1">Prénom</label><input v-model="form.firstName" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 outline-none" /></div>
          </div>
          <div class="grid grid-cols-2 gap-3">
            <div><label class="block text-sm font-medium text-gray-700 mb-1">Matricule</label><input v-model="form.employeeId" required class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 outline-none" /></div>
            <div><label class="block text-sm font-medium text-gray-700 mb-1">Rôle</label><input v-model="form.role" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 outline-none" placeholder="Ex: Opérateur" /></div>
          </div>
          <div class="grid grid-cols-2 gap-3">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Type opérateur</label>
              <select v-model="form.operatorType" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 outline-none">
                <option value="NOUVEAU_RECRU">Nouveau recru</option>
                <option value="DEJA_EN_POSTE">Déjà en poste</option>
              </select>
            </div>
            <div><label class="block text-sm font-medium text-gray-700 mb-1">Date d'embauche</label><input v-model="form.hireDate" type="date" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 outline-none" /></div>
          </div>
          <div class="grid grid-cols-2 gap-3">
            <div><label class="block text-sm font-medium text-gray-700 mb-1">Date de sortie</label><input v-model="form.exitDate" type="date" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 outline-none" /></div>
            <div></div>
          </div>
          <hr class="border-gray-200" />
          <p class="text-sm font-medium text-gray-700">Affectation</p>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Projet</label>
            <select v-model="form.projectId" @change="form.zoneId = ''; form.workstationId = ''" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 outline-none">
              <option value="">-- Aucun --</option>
              <option v-for="p in useChefProjects ? chefProjects : projects" :key="p.id" :value="p.id">{{ p.name }}</option>
            </select>
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Zone</label>
            <select v-model="form.zoneId" @change="form.workstationId = ''" :disabled="!form.projectId" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 outline-none disabled:bg-gray-100">
              <option value="" disabled>Choisir une zone</option>
              <option v-for="z in selectedProjectZones" :key="z.id" :value="z.id">{{ z.name }}</option>
            </select>
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Poste de travail</label>
            <select v-model="form.workstationId" :disabled="!form.zoneId" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 outline-none disabled:bg-gray-100">
              <option value="">-- Aucun --</option>
              <option v-for="ws in selectedZoneWorkstations" :key="ws.id" :value="ws.id">{{ ws.name }} ({{ ws.type || '-' }})</option>
            </select>
          </div>
          <div v-if="error" class="bg-red-50 text-red-600 text-sm p-3 rounded-lg">{{ error }}</div>
          <div class="flex justify-end gap-3 pt-2">
            <button type="button" @click="showCreateModal = false" class="px-4 py-2 text-sm text-gray-600 hover:text-gray-800 transition">Annuler</button>
            <button type="submit" :disabled="creating" class="px-4 py-2 bg-emerald-600 hover:bg-emerald-700 text-white text-sm rounded-lg transition">Créer</button>
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
            <div><label class="block text-sm font-medium text-gray-700 mb-1">Nom</label><input v-model="editForm.lastName" required class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 outline-none" /></div>
            <div><label class="block text-sm font-medium text-gray-700 mb-1">Prénom</label><input v-model="editForm.firstName" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 outline-none" /></div>
          </div>
          <div class="grid grid-cols-2 gap-3">
            <div><label class="block text-sm font-medium text-gray-700 mb-1">Matricule</label><input v-model="editForm.employeeId" required class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 outline-none" /></div>
            <div><label class="block text-sm font-medium text-gray-700 mb-1">Rôle</label><input v-model="editForm.role" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 outline-none" /></div>
          </div>
          <div class="grid grid-cols-2 gap-3">
            <div><label class="block text-sm font-medium text-gray-700 mb-1">Date d'embauche</label><input v-model="editForm.hireDate" type="date" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 outline-none" /></div>
            <!-- FIX 4a: Date de sortie now saves properly -->
            <div><label class="block text-sm font-medium text-gray-700 mb-1">Date de sortie</label><input v-model="editForm.exitDate" type="date" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 outline-none" /></div>
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Type operateur</label>
            <select v-model="editForm.operatorType" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 outline-none">
              <option value="NOUVEAU_RECRU">Nouveau recru</option>
              <option value="DEJA_EN_POSTE">Deja en poste</option>
            </select>
          </div>
          <div><label class="block text-sm font-medium text-gray-700 mb-1">Motif d'absence</label><input v-model="editForm.absenceReason" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 outline-none" /></div>
          <div v-if="error" class="bg-red-50 text-red-600 text-sm p-3 rounded-lg">{{ error }}</div>
          <div class="flex justify-end gap-3 pt-2">
            <button type="button" @click="showEditModal = false" class="px-4 py-2 text-sm text-gray-600 hover:text-gray-800">Annuler</button>
            <button type="submit" :disabled="creating" class="px-4 py-2 bg-emerald-600 text-white text-sm rounded-lg hover:bg-emerald-700">Enregistrer</button>
          </div>
        </form>
      </div>
    </div>

    <ConfirmDialog :visible="confirmData.visible" :title="confirmData.title" :message="confirmData.message" :type="confirmData.type" @confirm="handleConfirm" @cancel="handleCancel" />
  </div>
</template>
<script setup>
import { ref, computed, onMounted } from 'vue'
import { operatorsApi, structureApi } from '@/api/endpoints'
import { formatDate } from '@/shared/utils/date'
import ConfirmDialog from '@/components/ConfirmDialog.vue'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const operators = ref([])
const projects = ref([])
const teams = ref([])
const chefProjects = ref([])
const useChefProjects = ref(false)
const loading = ref(true)
const search = ref('')
const selectedProjectFilter = ref('')
const selectedTeamFilter = ref('')
const showCreateModal = ref(false)
const creating = ref(false)
const error = ref('')
const form = ref({
  lastName: '', firstName: '', employeeId: '', role: '',
  operatorType: 'NOUVEAU_RECRU', hireDate: '', exitDate: '',
  projectId: '', zoneId: '', workstationId: '',
})

// Multi-project roles that need project grouping
const isMultiProjectRole = computed(() =>
  auth.hasAnyRole(['RESP_QUALITE', 'AGENT_QUALITE', 'SUPERVISEUR', 'RESP_HSE', 'ADMIN', 'RH'])
)
// FIX 5a: Show project filter whenever user has multi-project role
const showProjectFilter = computed(() => isMultiProjectRole.value && projectList.value.length >= 1)
const showProjectColumn = computed(() => isMultiProjectRole.value)

// Build project list from structure API
const projectList = computed(() => {
  if (!projects.value.length) return []
  return projects.value.map(p => ({ id: p.id, name: p.name })).sort((a, b) => a.name.localeCompare(b.name))
})

// Build team list from teams data
const teamList = computed(() => {
  return teams.value.map(t => ({ id: t.id, name: t.name })).sort((a, b) => a.name.localeCompare(b.name))
})

// Build a map: teamId -> project names
const teamProjectMap = computed(() => {
  const map = {}
  for (const team of teams.value) {
    if (team.projects && team.projects.length) {
      map[team.id] = team.projects.map(p => p.name)
    }
  }
  return map
})

// Get project names for a single operator
const getOperatorProjects = (op) => {
  if (!op.team?.id) return []
  return teamProjectMap.value[op.team.id] || []
}

// Operators filtered by search, project, and team
const filteredOperators = computed(() => {
  let result = operators.value

  // Filter by project
  if (selectedProjectFilter.value) {
    const pid = Number(selectedProjectFilter.value)
    result = result.filter(op => {
      const projNames = getOperatorProjects(op)
      if (!projNames.length) return false
      const pName = projects.value.find(p => p.id === pid)?.name
      return projNames.includes(pName)
    })
  }

  // Filter by team
  if (selectedTeamFilter.value) {
    const tid = Number(selectedTeamFilter.value)
    result = result.filter(op => op.team?.id === tid)
  }

  // Filter by search text
  const q = search.value.toLowerCase()
  if (q) {
    result = result.filter(o =>
      `${o.lastName} ${o.firstName} ${o.employeeId}`.toLowerCase().includes(q)
    )
  }

  return result
})

// Group operators by project (for the default view)
const groupedByProject = computed(() => {
  const groups = {}
  for (const op of filteredOperators.value) {
    const projNames = getOperatorProjects(op)
    if (projNames.length > 0) {
      for (const pName of projNames) {
        if (!groups[pName]) groups[pName] = { projectName: pName, projectId: projects.value.find(p => p.name === pName)?.id, operators: [] }
        groups[pName].operators.push(op)
      }
    } else {
      if (!groups['_none']) groups['_none'] = { projectName: 'Sans projet', projectId: null, operators: [] }
      groups['_none'].operators.push(op)
    }
  }
  return Object.values(groups).sort((a, b) => {
    if (!a.projectId) return 1
    if (!b.projectId) return -1
    return a.projectName.localeCompare(b.projectName)
  })
})

// Selected project name for the header bar
const selectedProjectName = computed(() => {
  if (!selectedProjectFilter.value) return ''
  return projects.value.find(p => p.id === Number(selectedProjectFilter.value))?.name || ''
})

const selectedProjectZones = computed(() => {
  const list = useChefProjects.value ? chefProjects.value : projects.value
  if (!form.value.projectId || !list.length) return []
  const p = list.find(pr => pr.id === form.value.projectId)
  return p?.zones || []
})

const selectedZoneWorkstations = computed(() => {
  if (!form.value.zoneId) return []
  const zone = selectedProjectZones.value.find(z => z.id === form.value.zoneId)
  return zone?.workstations || []
})

const fetchOperators = async () => {
  loading.value = true
  try {
    const r = await operatorsApi.getAll()
    operators.value = r.data
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const fetchProjects = async () => {
  try {
    projects.value = (await structureApi.getAll()).data
  } catch (e) {
    console.error(e)
  }
}

const fetchTeams = async () => {
  try {
    const r = await structureApi.getTeams()
    teams.value = r.data || []
  } catch (e) {
    console.error(e)
  }
}

const openCreateModal = async () => {
  form.value = { lastName: '', firstName: '', employeeId: '', role: '', operatorType: 'NOUVEAU_RECRU', hireDate: '', exitDate: '', projectId: '', zoneId: '', workstationId: '' }
  error.value = ''
  useChefProjects.value = false
  chefProjects.value = []
  if (!projects.value.length) await fetchProjects()
  if (auth.isChefEquipe && projects.value.length > 0) {
    const myProjects = projects.value.filter(p => p.members?.some(m => m.employeeId === auth.user?.employeeId))
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
      employeeId: form.value.employeeId, role: form.value.role,
      operatorType: form.value.operatorType || 'NOUVEAU_RECRU',
      hireDate: form.value.hireDate || null, exitDate: form.value.exitDate || null,
      workstationId: form.value.workstationId || null,
    }
    await operatorsApi.create(payload)
    showCreateModal.value = false
    fetchOperators()
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
const handleConfirm = () => { confirmData.value.visible = false; if (pendingAction.value) { pendingAction.value(); pendingAction.value = null } }
const handleCancel = () => { confirmData.value.visible = false; pendingAction.value = null }

// Edit Operator
const showEditModal = ref(false)
const editForm = ref({ id: null, lastName: '', firstName: '', employeeId: '', role: '', operatorType: '', hireDate: '', exitDate: '', absenceReason: '' })
const openEditModal = (op) => { editForm.value = { id: op.id, lastName: op.lastName, firstName: op.firstName, employeeId: op.employeeId, role: op.role || '', operatorType: op.operatorType || 'NOUVEAU_RECRU', hireDate: op.hireDate?.slice(0, 10) || '', exitDate: op.exitDate?.slice(0, 10) || '', absenceReason: op.absenceReason || '' }; showEditModal.value = true }
// FIX 4a: Update now sends exitDate and absenceReason to backend
const updateOperator = async () => { creating.value = true; error.value = ''; try { await operatorsApi.update(editForm.value.id, editForm.value); showEditModal.value = false; fetchOperators() } catch (e) { error.value = e.response?.data?.message || e.message || 'Erreur inconnue'; alert('Erreur: ' + error.value) } finally { creating.value = false } }

onMounted(async () => {
  await Promise.allSettled([fetchOperators(), fetchProjects(), fetchTeams()])
})
</script>