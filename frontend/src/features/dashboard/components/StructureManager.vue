<script setup>
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import {
  fetchStructure,
  createProject,
  createZone,
  createPoste,
  addProjectMember,
  fetchUsersByRole,
} from '@/features/structure/services/structureService'

const authStore = useAuthStore()

const ROLE_LABELS = {
  CHEF_DE_PROJET: 'Chef de Projet',
  RESPONSABLE_QUALITE: 'Responsable Qualité',
  AGENT_QUALITE: 'Agent Qualité',
  RESPONSABLE_HSE: 'Responsable HSE',
  SUPERVISEUR: 'Superviseur',
}
const projectRoles = Object.keys(ROLE_LABELS)

const structure = ref({ projects: [] })
const structureLoading = ref(false)
const structureError = ref('')
const structureMsg = ref('')

const newProjectName = ref('')
const newZoneName = ref('')
const selectedProjectId = ref('')
const newPosteName = ref('')
const selectedZoneId = ref('')
const newPosteNiveauCibleIlu = ref('I')

const membersByRole = ref({
  CHEF_DE_PROJET: null,
  RESPONSABLE_QUALITE: null,
  AGENT_QUALITE: null,
  RESPONSABLE_HSE: null,
  SUPERVISEUR: null,
})
const usersByRole = ref({})
const loadingUsersByRole = ref(false)

const selectedMemberUserId = ref({})
const selectedMemberRoleProjet = ref({})
const assignMemberLoading = ref({})

function formatCreation(dateCreation) {
  if (!dateCreation) return 'Date non disponible'
  return new Intl.DateTimeFormat('fr-FR', { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(dateCreation))
}

function requireStructureSelection(value, label) {
  if (value) return true
  structureMsg.value = `Veuillez sélectionner ${label}.`
  return false
}

async function fetchStructureData() {
  if (!authStore.token) return
  structureLoading.value = true
  structureError.value = ''
  structureMsg.value = ''
  try {
    const data = await fetchStructure(authStore.token)
    structure.value = data
    if (data.projects?.length) {
      const selectedProject = data.projects.find((p) => p.idProjet === Number(selectedProjectId.value))
      const project = selectedProject || data.projects[0]
      selectedProjectId.value = project.idProjet
      const selectedZone = project.zones?.find((z) => z.idZone === Number(selectedZoneId.value))
      selectedZoneId.value = selectedZone?.idZone || project.zones?.[0]?.idZone || ''
    } else {
      selectedProjectId.value = ''
      selectedZoneId.value = ''
    }
  } catch (error) {
    structureError.value = error.message
  } finally {
    structureLoading.value = false
  }
}

async function loadUsersByRole() {
  loadingUsersByRole.value = true
  try {
    const roleMap = {
      CHEF_DE_PROJET: 'CHEF_EQUIPE',
      RESPONSABLE_QUALITE: 'RESPONSABLE_QUALITE',
      AGENT_QUALITE: 'QUALITE',
      RESPONSABLE_HSE: 'HSE',
      SUPERVISEUR: 'SUPERVISEUR',
    }
    const result = {}
    for (const [projectRole, systemRole] of Object.entries(roleMap)) {
      result[projectRole] = await fetchUsersByRole(authStore.token, systemRole)
    }
    usersByRole.value = result
  } catch (error) {
    console.error('Erreur chargement utilisateurs par rôle:', error)
  } finally {
    loadingUsersByRole.value = false
  }
}

async function handleCreateProject() {
  if (!newProjectName.value.trim()) return
  structureMsg.value = ''
  const membres = []
  for (const [roleProjet, userId] of Object.entries(membersByRole.value)) {
    if (userId) membres.push({ utilisateurId: Number(userId), roleProjet })
  }
  try {
    await createProject(authStore.token, newProjectName.value.trim(), membres)
    newProjectName.value = ''
    for (const key of Object.keys(membersByRole.value)) membersByRole.value[key] = null
    await fetchStructureData()
    structureMsg.value = 'Projet créé avec succès.'
  } catch (error) {
    structureMsg.value = error.message
  }
}

async function handleCreateZone() {
  if (!requireStructureSelection(selectedProjectId.value, 'un projet') || !newZoneName.value.trim()) return
  structureMsg.value = ''
  try {
    await createZone(authStore.token, Number(selectedProjectId.value), newZoneName.value.trim())
    newZoneName.value = ''
    await fetchStructureData()
    structureMsg.value = 'Zone créée avec succès.'
  } catch (error) {
    structureMsg.value = error.message
  }
}

async function handleCreatePoste() {
  if (!requireStructureSelection(selectedZoneId.value, 'une zone') || !newPosteName.value.trim()) return
  structureMsg.value = ''
  try {
    await createPoste(authStore.token, Number(selectedZoneId.value), newPosteName.value.trim(), newPosteNiveauCibleIlu.value)
    newPosteName.value = ''
    newPosteNiveauCibleIlu.value = 'I'
    await fetchStructureData()
    structureMsg.value = 'Poste de travail créé avec succès.'
  } catch (error) {
    structureMsg.value = error.message
  }
}

async function handleAssignProjectMember(projectId) {
  const userId = selectedMemberUserId.value[projectId]
  const roleProjet = selectedMemberRoleProjet.value[projectId]
  if (!userId || !roleProjet) return
  assignMemberLoading.value[projectId] = true
  structureMsg.value = ''
  structureError.value = ''
  try {
    await addProjectMember(authStore.token, projectId, Number(userId), roleProjet)
    selectedMemberUserId.value[projectId] = ''
    selectedMemberRoleProjet.value[projectId] = ''
    await fetchStructureData()
    structureMsg.value = 'Membre affecté avec succès au projet.'
  } catch (error) {
    structureError.value = error.message
  } finally {
    assignMemberLoading.value[projectId] = false
  }
}

onMounted(() => {
  fetchStructureData()
  loadUsersByRole()
})
</script>

<template>
  <section class="role-section">
    <div class="panel-card structure-panel">
      <div class="panel-header"><h3>Gestion des projets</h3></div>
      <div v-if="structureMsg" class="message-box success">{{ structureMsg }}</div>
      <div v-if="structureError" class="message-box error">{{ structureError }}</div>
      <div v-if="structureLoading" class="loading-state"><span class="spinner-blue"></span> Chargement des projets...</div>

      <div v-else class="structure-grid" style="display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 1rem;">
        <div class="panel-card compact-card">
          <div class="panel-header"><h4>Créer un projet</h4></div>
          <form @submit.prevent="handleCreateProject" class="panel-form">
            <div class="input-group">
              <label>Nom du projet</label>
              <input v-model="newProjectName" required placeholder="Ex: Smart Car" />
            </div>
            <div v-if="loadingUsersByRole" class="loading-state"><span class="spinner-blue"></span> Chargement des utilisateurs...</div>
            <div v-else class="member-assignments">
              <div v-for="roleKey in projectRoles" :key="roleKey" class="input-group">
                <label style="font-size: 0.8rem; margin-top: 0.5rem;">{{ ROLE_LABELS[roleKey] }}</label>
                <select v-model="membersByRole[roleKey]">
                  <option :value="null">— Non affecté —</option>
                  <option v-for="u in usersByRole[roleKey] || []" :key="u.id" :value="u.id">{{ u.nom }} ({{ u.matricule }})</option>
                </select>
                <span v-if="!(usersByRole[roleKey] || []).length" class="empty-hint" style="font-size: 0.75rem; color: #a6c0c0;">Aucun utilisateur disponible</span>
              </div>
            </div>
            <button type="submit" class="submit-btn" style="margin-top: 1rem;">Ajouter le projet</button>
          </form>
        </div>

        <div class="panel-card compact-card">
          <div class="panel-header"><h4>Créer une zone</h4></div>
          <form @submit.prevent="handleCreateZone" class="panel-form">
            <div class="input-group">
              <label>Projet</label>
              <select v-model="selectedProjectId">
                <option v-for="project in structure.projects" :key="project.idProjet" :value="project.idProjet">{{ project.nom }}</option>
              </select>
            </div>
            <div class="input-group">
              <label>Nom de la zone</label>
              <input v-model="newZoneName" required placeholder="Ex: Zone Montage" />
            </div>
            <button type="submit" class="submit-btn">Ajouter la zone</button>
          </form>
        </div>

        <div class="panel-card compact-card">
          <div class="panel-header"><h4>Créer un poste de travail</h4></div>
          <form @submit.prevent="handleCreatePoste" class="panel-form">
            <div class="input-group">
              <label>Zone</label>
              <select v-model="selectedZoneId">
                <template v-for="project in structure.projects" :key="project.idProjet">
                  <optgroup v-if="project.zones?.length" :label="project.nom">
                    <option v-for="zone in project.zones" :key="zone.idZone" :value="zone.idZone">{{ zone.nom }}</option>
                  </optgroup>
                </template>
              </select>
            </div>
            <div class="input-group">
              <label>Nom du poste</label>
              <input v-model="newPosteName" required placeholder="Ex: Poste 1" />
            </div>
            <div class="input-group">
              <label>Niveau cible (I / L / U)</label>
              <select v-model="newPosteNiveauCibleIlu">
                <option value="I">I</option>
                <option value="L">L</option>
                <option value="U">U</option>
              </select>
            </div>
            <button type="submit" class="submit-btn">Ajouter le poste</button>
          </form>
        </div>
      </div>

      <div class="panel-card" style="margin-top: 1.5rem;">
        <div class="panel-header"><h4>Projets actuels</h4></div>
        <div v-for="project in structure.projects" :key="project.idProjet" class="structure-tree">
          <div class="tree-project">
            <strong>📁 {{ project.nom }}</strong>
            <span class="creation-meta">Créé le {{ formatCreation(project.dateCreation) }} par {{ project.creePar }}</span>
          </div>

          <div class="project-members" style="margin-top: 0.5rem; padding-left: 1.2rem; text-align: left;">
            <strong style="font-size: 0.85rem;">Membres du projet :</strong>
            <div v-if="project.membres && project.membres.length" style="display: flex; flex-wrap: wrap; gap: 0.5rem; margin-top: 0.25rem;">
              <span v-for="member in project.membres" :key="member.id" class="role-badge">👤 {{ member.nom }} ({{ ROLE_LABELS[member.roleProjet] || member.roleProjet }})</span>
            </div>
            <div v-else style="color: #547174; font-size: 0.8rem; margin-top: 0.25rem; font-style: italic;">Aucun membre affecté.</div>
          </div>

          <form @submit.prevent="handleAssignProjectMember(project.idProjet)" class="form-row" style="margin-top: 0.8rem; margin-left: 1.2rem; align-items: flex-end; gap: 0.5rem; background: #f2f8f7; padding: 0.75rem; border-radius: 6px; border: 1px solid #d8e5e4;">
            <div class="input-group" style="flex: 1; margin: 0;">
              <label style="font-size: 0.75rem;">Rôle projet</label>
              <select v-model="selectedMemberRoleProjet[project.idProjet]" @change="selectedMemberUserId[project.idProjet] = ''" style="padding: 0.4rem; font-size: 0.85rem; height: 34px;">
                <option value="" disabled selected>-- Rôle --</option>
                <option v-for="roleKey in projectRoles" :key="roleKey" :value="roleKey">{{ ROLE_LABELS[roleKey] }}</option>
              </select>
            </div>
            <div class="input-group" style="flex: 1.5; margin: 0;">
              <label style="font-size: 0.75rem;">Utilisateur</label>
              <select v-model="selectedMemberUserId[project.idProjet]" :disabled="!selectedMemberRoleProjet[project.idProjet]" style="padding: 0.4rem; font-size: 0.85rem; height: 34px;">
                <option value="" disabled selected>-- Utilisateur --</option>
                <option v-for="u in usersByRole[selectedMemberRoleProjet[project.idProjet]] || []" :key="u.id" :value="u.id">{{ u.nom }} ({{ u.matricule }})</option>
              </select>
            </div>
            <button type="submit" :disabled="!selectedMemberUserId[project.idProjet] || assignMemberLoading[project.idProjet]" class="submit-btn" style="padding: 0.4rem 0.8rem; font-size: 0.85rem; height: 34px; line-height: 1;">
              {{ assignMemberLoading[project.idProjet] ? '...' : 'Affecter' }}
            </button>
          </form>

          <div v-for="zone in project.zones || []" :key="zone.idZone" class="tree-zone">
            <div class="tree-zone-title">
              <strong>📍 {{ zone.nom }}</strong>
              <span class="creation-meta">Créée le {{ formatCreation(zone.dateCreation) }} par {{ zone.creePar }}</span>
            </div>
            <ul>
              <li v-for="poste in zone.postes || []" :key="poste.idPoste">
                <strong>🛠️ {{ poste.nom }}</strong>
                <span class="ilu-badge">Cible : {{ poste.niveauCibleIlu || 'I' }}</span>
                <span class="creation-meta">Créé le {{ formatCreation(poste.dateCreation) }} par {{ poste.creePar || 'Système' }}</span>
              </li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>