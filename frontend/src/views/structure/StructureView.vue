<template>
  <div class="space-y-6">
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
      <div><h1 class="text-2xl font-bold text-gray-900">Structure</h1><p class="text-gray-500 mt-1">Organisation des projets, zones et postes de travail</p></div>
      <button v-if="canEditStructure" @click="showCreateProject = true" class="inline-flex items-center gap-2 bg-emerald-600 hover:bg-emerald-700 text-white px-4 py-2.5 rounded-lg text-sm font-medium transition-colors">
        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"></path></svg>Nouveau Projet
      </button>
    </div>

    <div v-if="loading" class="flex items-center justify-center py-20"><div class="w-8 h-8 border-4 border-emerald-200 border-t-emerald-600 rounded-full animate-spin"></div></div>

    <div v-else>
      <div v-for="project in projects" :key="project.id" class="bg-white rounded-xl shadow-sm border border-gray-200 mb-4">
        <div class="p-4 flex items-center justify-between cursor-pointer hover:bg-gray-50 transition" @click="toggleProject(project.id)">
          <div class="flex items-center gap-3">
            <svg class="w-5 h-5 text-gray-400 transition-transform" :class="{ 'rotate-90': expandedProjects.has(project.id) }" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"></path></svg>
            <div><h2 class="font-semibold text-gray-900">{{ project.name }}</h2><p class="text-xs text-gray-500">{{ project.zones?.length || 0 }} zones, {{ project.members?.length || 0 }} membres</p></div>
          </div>
          <div v-if="canEditStructure" class="flex items-center gap-2">
            <button @click.stop="showAddZone(project.id)" class="text-sm text-emerald-600 hover:underline">+ Zone</button>
            <button @click.stop="showAddMember(project.id)" class="text-sm text-blue-600 hover:underline">+ Affecter</button>
            <button @click.stop="openEditProject(project)" class="text-gray-400 hover:text-blue-600 transition" title="Modifier"><svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"></path></svg></button><button @click.stop="deleteProject(project.id)" class="text-gray-400 hover:text-red-600 transition" title="Supprimer"><svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path></svg></button>
          </div>
        </div>
        <div v-if="expandedProjects.has(project.id)" class="border-t border-gray-100">
          <div v-if="project.members?.length" class="p-4 border-b border-gray-100">
            <h3 class="text-sm font-medium text-gray-700 mb-2">Membres du projet</h3>
            <div class="flex flex-wrap gap-2">
              <span v-for="m in project.members" :key="m.id" class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full bg-slate-100 text-sm text-slate-700">
                {{ m.employeeName || m.employeeId }}
                <span class="text-xs font-medium px-1.5 py-0.5 rounded" :class="roleBadgeClass(m.projectRole)">{{ roleLabel(m.projectRole) }}</span>
                <button @click.stop="removeMember(m.id)" class="text-slate-400 hover:text-red-500 ml-1" title="Retirer">
                  <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/></svg>
                </button>
              </span>
            </div>
          </div>
          <div v-else class="p-4 border-b border-gray-100 text-sm text-gray-400">Aucun membre dans ce projet</div>
          <div v-for="zone in project.zones" :key="zone.id" class="border-b border-gray-100 last:border-b-0">
            <div class="p-4 pl-8 flex items-center justify-between cursor-pointer hover:bg-gray-50 transition" @click="toggleZone(zone.id)">
              <div class="flex items-center gap-2">
                <svg class="w-4 h-4 text-gray-400 transition-transform" :class="{ 'rotate-90': expandedZones.has(zone.id) }" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"></path></svg>
                <span class="font-medium text-gray-800">{{ zone.name }}</span>
                <span class="text-xs text-gray-400">({{ zone.workstations?.length || 0 }} postes)</span>
              </div>
              <button @click.stop="deleteZone(zone.id)" class="text-gray-400 hover:text-red-600 transition"><svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path></svg></button>
            </div>
            <div v-if="expandedZones.has(zone.id)" class="pl-12 pr-4 pb-4">
              <div v-if="zone.workstations?.length" class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3">
                <div v-for="ws in zone.workstations" :key="ws.id" class="p-3 rounded-lg border border-gray-200 hover:border-emerald-300 transition">
                  <div class="flex items-center justify-between">
                    <div><p class="text-sm font-medium text-gray-900">{{ ws.name }}</p><p class="text-xs text-gray-500">{{ ws.type || 'Non defini' }}</p></div>
                    <button @click="openEditWorkstation(ws)" class="text-gray-400 hover:text-blue-600 transition" title="Modifier"><svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"></path></svg></button><button @click="deleteWorkstation(ws.id)" class="text-gray-400 hover:text-red-600 transition" title="Supprimer"><svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path></svg></button>
                  </div>
                  <div class="mt-2 grid grid-cols-3 gap-2 text-xs">
                    <div><span class="text-gray-400">Cadence:</span> <span class="font-medium">{{ ws.targetCadence || '-' }}</span></div>
                    <div><span class="text-gray-400">Versatilite:</span> <span class="font-medium">{{ ws.versatilityTarget || '-' }}</span></div>
                    <div><span class="text-gray-400">ILU:</span> <span class="font-medium">{{ ws.targetIluLevel || '-' }}</span></div>
                  </div>
                </div>
              </div>
              <div v-else class="text-sm text-gray-400 py-2">Aucun poste de travail</div>
              <button @click="showAddWorkstation(zone.id, project.id)" class="mt-3 text-sm text-emerald-600 hover:underline">+ Ajouter un poste</button>
            </div>
          </div>
        </div>
      </div>
      <div v-if="projects.length === 0" class="bg-white rounded-xl shadow-sm border border-gray-200 p-12 text-center text-gray-400">Aucun projet configure</div>
    </div>

    <!-- Create Project Modal -->
    <div v-if="showCreateProject" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50" @click.self="showCreateProject = false">
      <div class="bg-white rounded-2xl shadow-xl w-full max-w-md mx-4 p-6">
        <h2 class="text-lg font-semibold text-gray-900 mb-4">Nouveau Projet</h2>
        <form @submit.prevent="createProject" class="space-y-4">
          <div><label class="block text-sm font-medium text-gray-700 mb-1">Nom du projet</label><input v-model="projectForm.name" required class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 outline-none" /></div>
          <div v-if="error" class="bg-red-50 text-red-600 text-sm p-3 rounded-lg">{{ error }}</div>
          <div class="flex justify-end gap-3 pt-2"><button type="button" @click="showCreateProject = false" class="px-4 py-2 text-sm text-gray-600 hover:text-gray-800">Annuler</button><button type="submit" :disabled="creating" class="px-4 py-2 bg-emerald-600 text-white text-sm rounded-lg hover:bg-emerald-700">Créer</button></div>
        </form>
      </div>
    </div>

    <!-- Add Member Modal (just role filter + user) -->
    <div v-if="showMemberModal" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50" @click.self="showMemberModal = false">
      <div class="bg-white rounded-2xl shadow-xl w-full max-w-md mx-4 p-6">
        <h2 class="text-lg font-semibold text-gray-900 mb-4">Affecter au Projet</h2>
        <form @submit.prevent="addMember" class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Filtrer par role</label>
            <select v-model="memberForm.filterRole" @change="memberForm.employeeId = ''" required class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 outline-none">
              <option value="" disabled>Choisir un role</option>
              <option value="CHEF_EQUIPE">Chef d'Équipe</option>
              <option value="AGENT_QUALITE">Agent Qualité</option>
              <option value="SUPERVISEUR">Superviseur</option>
              <option value="RESP_QUALITE">Resp Qualité</option>
              <option value="RESP_HSE">Resp HSE</option>
            </select>
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Utilisateur</label>
            <select v-model="memberForm.employeeId" required :disabled="!memberForm.filterRole" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 outline-none disabled:bg-gray-100">
              <option value="" disabled>Selectionner un utilisateur</option>
              <option v-for="u in filteredUsers" :key="u.employeeId" :value="u.employeeId">{{ u.name }} ({{ u.employeeId }})</option>
            </select>
            <p v-if="memberForm.filterRole && filteredUsers.length === 0" class="text-xs text-gray-400 mt-1">Aucun utilisateur avec ce role</p>
          </div>
          <div v-if="error" class="bg-red-50 text-red-600 text-sm p-3 rounded-lg">{{ error }}</div>
          <div class="flex justify-end gap-3 pt-2">
            <button type="button" @click="showMemberModal = false" class="px-4 py-2 text-sm text-gray-600 hover:text-gray-800">Annuler</button>
            <button type="submit" :disabled="creating" class="px-4 py-2 bg-emerald-600 text-white text-sm rounded-lg hover:bg-emerald-700">Affecter</button>
          </div>
        </form>
      </div>
    </div>

    <!-- Add Zone Modal -->
    <div v-if="showZoneModal" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50" @click.self="showZoneModal = false">
      <div class="bg-white rounded-2xl shadow-xl w-full max-w-md mx-4 p-6">
        <h2 class="text-lg font-semibold text-gray-900 mb-4">Nouvelle Zone</h2>
        <form @submit.prevent="createZone" class="space-y-4">
          <div><label class="block text-sm font-medium text-gray-700 mb-1">Nom de la zone</label><input v-model="zoneForm.name" required class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 outline-none" /></div>
          <div class="flex justify-end gap-3 pt-2"><button type="button" @click="showZoneModal = false" class="px-4 py-2 text-sm text-gray-600 hover:text-gray-800">Annuler</button><button type="submit" :disabled="creating" class="px-4 py-2 bg-emerald-600 text-white text-sm rounded-lg">Ajouter</button></div>
        </form>
      </div>
    </div>

    <!-- Add Workstation Modal -->
    <div v-if="showWorkstationModal" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50" @click.self="showWorkstationModal = false">
      <div class="bg-white rounded-2xl shadow-xl w-full max-w-lg mx-4 p-6">
        <h2 class="text-lg font-semibold text-gray-900 mb-4">Nouveau Poste de Travail</h2>
        <form @submit.prevent="createWorkstation" class="space-y-4">
          <div><label class="block text-sm font-medium text-gray-700 mb-1">Nom</label><input v-model="wsForm.name" required class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 outline-none" /></div>
          <div class="grid grid-cols-2 gap-3">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Type</label>
              <select v-model="wsForm.type" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 outline-none">
                <option value="POSTE">Poste Standard</option>
                <option value="TEST">Test Défauthèque</option>
              </select>
            </div>
            <div><label class="block text-sm font-medium text-gray-700 mb-1">Cadence cible</label><input v-model.number="wsForm.targetCadence" type="number" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 outline-none" /></div>
            <div><label class="block text-sm font-medium text-gray-700 mb-1">Objectif Qualité (max défauts)</label><input v-model.number="wsForm.qualityObjective" type="number" placeholder="7 (< 7 défauts)" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 outline-none" /></div>
            <div><label class="block text-sm font-medium text-gray-700 mb-1">Versatilite</label><input v-model.number="wsForm.versatilityTarget" type="number" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 outline-none" /></div>
            <div><label class="block text-sm font-medium text-gray-700 mb-1">Niveau ILU cible</label><select v-model="wsForm.targetIluLevel" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 outline-none"><option value="I">I - Initiation</option><option value="L">L - Logique</option><option value="U">U - Unite</option></select></div>
          </div>
          <div class="flex justify-end gap-3 pt-2"><button type="button" @click="showWorkstationModal = false" class="px-4 py-2 text-sm text-gray-600 hover:text-gray-800">Annuler</button><button type="submit" :disabled="creating" class="px-4 py-2 bg-emerald-600 text-white text-sm rounded-lg">Ajouter</button></div>
        </form>
      </div>
    </div>
    <!-- Edit Project Modal -->
    <div v-if="showEditProjectModal" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50" @click.self="showEditProjectModal = false">
      <div class="bg-white rounded-2xl shadow-xl w-full max-w-md mx-4 p-6">
        <h2 class="text-lg font-semibold text-gray-900 mb-4">Modifier le Projet</h2>
        <form @submit.prevent="updateProject" class="space-y-4">
          <div><label class="block text-sm font-medium text-gray-700 mb-1">Nom du projet</label><input v-model="editProjectForm.name" required class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 outline-none" /></div>
          <div class="flex justify-end gap-3 pt-2"><button type="button" @click="showEditProjectModal = false" class="px-4 py-2 text-sm text-gray-600 hover:text-gray-800">Annuler</button><button type="submit" :disabled="creating" class="px-4 py-2 bg-emerald-600 text-white text-sm rounded-lg hover:bg-emerald-700">Enregistrer</button></div>
        </form>
      </div>
    </div>

    <!-- Edit Workstation Modal -->
    <div v-if="showEditWsModal" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50" @click.self="showEditWsModal = false">
      <div class="bg-white rounded-2xl shadow-xl w-full max-w-lg mx-4 p-6">
        <h2 class="text-lg font-semibold text-gray-900 mb-4">Modifier le Poste</h2>
        <form @submit.prevent="updateWorkstation" class="space-y-4">
          <div><label class="block text-sm font-medium text-gray-700 mb-1">Nom</label><input v-model="editWsForm.name" required class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 outline-none" /></div>
          <div class="grid grid-cols-2 gap-3">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Type</label>
              <select v-model="editWsForm.type" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 outline-none">
                <option value="POSTE">Poste Standard</option>
                <option value="TEST">Test Défauthèque</option>
              </select>
            </div>
            <div><label class="block text-sm font-medium text-gray-700 mb-1">Cadence cible</label><input v-model.number="editWsForm.targetCadence" type="number" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 outline-none" /></div>
            <div><label class="block text-sm font-medium text-gray-700 mb-1">Versatilité</label><input v-model.number="editWsForm.versatilityTarget" type="number" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 outline-none" /></div>
            <div><label class="block text-sm font-medium text-gray-700 mb-1">Niveau ILU cible</label><select v-model="editWsForm.targetIluLevel" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 outline-none"><option value="I">I - Initiation</option><option value="L">L - Logique</option><option value="U">U - Unité</option></select></div>
          </div>
          <div class="flex justify-end gap-3 pt-2"><button type="button" @click="showEditWsModal = false" class="px-4 py-2 text-sm text-gray-600 hover:text-gray-800">Annuler</button><button type="submit" :disabled="creating" class="px-4 py-2 bg-emerald-600 text-white text-sm rounded-lg hover:bg-emerald-700">Enregistrer</button></div>
        </form>
      </div>
    </div>

    <!-- Confirm Dialog -->
    <ConfirmDialog :visible="confirmData.visible" :title="confirmData.title" :message="confirmData.message" :type="confirmData.type" @confirm="handleConfirm" @cancel="handleCancel" />
  </div>
</template>
<script setup>
import { ref, computed, onMounted } from 'vue'
import { structureApi } from '@/api/endpoints'
import { useAuthStore } from '@/stores/auth'
import ConfirmDialog from '@/components/ConfirmDialog.vue'

const authStore = useAuthStore()
const canEditStructure = computed(() => authStore.hasAnyRole(['ADMIN', 'SUPERVISEUR', 'RESP_QUALITE']))

const projects = ref([])
const loading = ref(true)
const creating = ref(false)
const error = ref('')
const expandedProjects = ref(new Set())
const expandedZones = ref(new Set())
const showCreateProject = ref(false)
const showZoneModal = ref(false)
const showWorkstationModal = ref(false)
const showMemberModal = ref(false)
const availableUsers = ref([])
const projectForm = ref({ name: '' })
const zoneForm = ref({ name: '', projectId: null })
const wsForm = ref({ name: '', type: 'POSTE', targetCadence: null, versatilityTarget: null, targetIluLevel: 'I', zoneId: null })
const memberForm = ref({ projectId: null, filterRole: '', employeeId: '' })

const roleLabel = (r) => ({ TEAM_LEADER: "Chef d'Eq", QUALITY_MANAGER: "Agent Q", PROJECT_MANAGER: "Resp Projet", MEMBER: "Membre" }[r] || r)
const roleBadgeClass = (r) => ({ TEAM_LEADER: 'bg-blue-100 text-blue-700', QUALITY_MANAGER: 'bg-amber-100 text-amber-700', PROJECT_MANAGER: 'bg-purple-100 text-purple-700', MEMBER: 'bg-gray-100 text-gray-600' }[r] || 'bg-gray-100 text-gray-600')

const filteredUsers = computed(() => {
  if (!memberForm.value.filterRole || !availableUsers.value.length) return []
  const targetRole = memberForm.value.filterRole
  return availableUsers.value.filter(u => {
    if (!u.roles) return false
    const rolesList = u.roles.map(r => (typeof r === 'object' ? r.label || r.name : String(r)))
    return rolesList.includes(targetRole)
  })
})

const systemToProjectRole = (filterRole) => ({ CHEF_EQUIPE: 'TEAM_LEADER', AGENT_QUALITE: 'QUALITY_MANAGER', SUPERVISEUR: 'PROJECT_MANAGER', RESP_QUALITE: 'QUALITY_MANAGER', RESP_HSE: 'MEMBER' }[filterRole] || 'MEMBER')

const toggleProject = (id) => { if (expandedProjects.value.has(id)) expandedProjects.value.delete(id); else expandedProjects.value.add(id); expandedProjects.value = new Set(expandedProjects.value) }
const toggleZone = (id) => { if (expandedZones.value.has(id)) expandedZones.value.delete(id); else expandedZones.value.add(id); expandedZones.value = new Set(expandedZones.value) }

const fetchProjects = async () => { loading.value = true; try { projects.value = (await structureApi.getAll()).data } catch (e) { console.error(e) } finally { loading.value = false } }
const fetchAvailableUsers = async () => { try { availableUsers.value = (await structureApi.getAvailableUsers()).data } catch (e) { console.error(e) } }

const createProject = async () => { creating.value = true; error.value = ''; try { await structureApi.createProject({ name: projectForm.value.name }); showCreateProject.value = false; projectForm.value = { name: '' }; fetchProjects() } catch (e) { const msg = e.response?.data?.message || e.message || 'Erreur inconnue'; error.value = msg; alert('Erreur: ' + msg + ' (status: ' + (e.response?.status || 'n/a') + ')'); console.error(e) } finally { creating.value = false } }
const deleteProject = async (id) => { confirmData.value = { visible: true, title: 'Supprimer le projet', message: 'Cette action est irreversible. Voulez-vous continuer ?', type: 'danger' }; pendingDeleteAction.value = () => deleteProjectConfirmed(id); return; try { await structureApi.deleteProject(id); fetchProjects() } catch (e) { console.error(e) } }

const showAddZone = (projectId) => { zoneForm.value = { name: '', projectId }; showZoneModal.value = true }
const createZone = async () => { creating.value = true; try { await structureApi.createZone(zoneForm.value.projectId, { name: zoneForm.value.name }); showZoneModal.value = false; fetchProjects() } catch (e) { console.error(e) } finally { creating.value = false } }
const deleteZone = async (id) => { confirmData.value = { visible: true, title: 'Supprimer la zone', message: 'Voulez-vous supprimer cette zone et tous ses postes ?', type: 'danger' }; pendingDeleteAction.value = () => deleteZoneConfirmed(id); return; try { await structureApi.deleteZone(id); fetchProjects() } catch (e) { console.error(e) } }

const showAddMember = async (projectId) => { memberForm.value = { projectId, filterRole: '', employeeId: '' }; await fetchAvailableUsers(); showMemberModal.value = true }
const addMember = async () => {
  creating.value = true; error.value = ''
  try {
    const user = availableUsers.value.find(u => u.employeeId === memberForm.value.employeeId)
    await structureApi.addMember(memberForm.value.projectId, {
      employeeId: memberForm.value.employeeId,
      employeeName: user?.name || memberForm.value.employeeId,
      role: systemToProjectRole(memberForm.value.filterRole)
    })
    showMemberModal.value = false; fetchProjects()
  } catch (e) { error.value = e.response?.data?.message || e.message || 'Erreur inconnue'; alert('Erreur: ' + error.value) } finally { creating.value = false }
}
const removeMember = async (memberId) => {
  confirmData.value = { visible: true, title: 'Retirer le membre', message: 'Voulez-vous retirer ce membre du projet ?', type: 'danger' }; pendingDeleteAction.value = () => removeMemberConfirmed(memberId)
}
const showAddWorkstation = (zoneId, projectId) => { wsForm.value = { name: '', type: 'POSTE', targetCadence: null, versatilityTarget: null, targetIluLevel: 'I', zoneId }; showWorkstationModal.value = true }
const createWorkstation = async () => { creating.value = true; try { await structureApi.createWorkstation(wsForm.value); showWorkstationModal.value = false; fetchProjects() } catch (e) { console.error(e) } finally { creating.value = false } }
const deleteWorkstation = async (id) => { confirmData.value = { visible: true, title: 'Supprimer le poste', message: 'Voulez-vous supprimer ce poste de travail ?', type: 'danger' }; pendingDeleteAction.value = () => deleteWorkstationConfirmed(id) }

const confirmData = ref({ visible: false, title: '', message: '', type: 'danger' })
const pendingDeleteAction = ref(null)
const handleConfirm = () => { confirmData.value.visible = false; if (pendingDeleteAction.value) { pendingDeleteAction.value(); pendingDeleteAction.value = null } }
const handleCancel = () => { confirmData.value.visible = false; pendingDeleteAction.value = null }

const deleteProjectConfirmed = async (id) => { try { await structureApi.deleteProject(id); fetchProjects() } catch (e) { console.error(e) } }
const deleteZoneConfirmed = async (id) => { try { await structureApi.deleteZone(id); fetchProjects() } catch (e) { console.error(e) } }
const deleteWorkstationConfirmed = async (id) => { try { await structureApi.deleteWorkstation(id); fetchProjects() } catch (e) { console.error(e) } }
const removeMemberConfirmed = async (memberId) => { try { await structureApi.deleteMember(memberId); fetchProjects() } catch (e) { console.error(e) } }

// Edit Project
const showEditProjectModal = ref(false)
const editProjectForm = ref({ id: null, name: '' })
const openEditProject = (project) => { editProjectForm.value = { id: project.id, name: project.name }; showEditProjectModal.value = true }
const updateProject = async () => { creating.value = true; try { await structureApi.updateProject(editProjectForm.value.id, { name: editProjectForm.value.name }); showEditProjectModal.value = false; fetchProjects() } catch (e) { console.error(e) } finally { creating.value = false } }

// Edit Workstation
const showEditWsModal = ref(false)
const editWsForm = ref({ id: null, name: '', type: 'POSTE', targetCadence: null, versatilityTarget: null, targetIluLevel: 'I' })
const openEditWorkstation = (ws) => { editWsForm.value = { id: ws.id, name: ws.name, type: ws.type || 'POSTE', targetCadence: ws.targetCadence, versatilityTarget: ws.versatilityTarget, targetIluLevel: ws.targetIluLevel || 'I' }; showEditWsModal.value = true }
const updateWorkstation = async () => { creating.value = true; try { await structureApi.updateWorkstation(editWsForm.value.id, editWsForm.value); showEditWsModal.value = false; fetchProjects() } catch (e) { console.error(e) } finally { creating.value = false } }

onMounted(fetchProjects)
</script>