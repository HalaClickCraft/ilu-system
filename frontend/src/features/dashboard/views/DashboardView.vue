<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import AdminDashboard from '@/features/dashboard/views/AdminDashboard.vue'
import ChefEquipeDashboard from '@/features/dashboard/views/ChefEquipeDashboard.vue'
import RhDashboard from '@/features/dashboard/views/RhDashboard.vue'
import QualiteDashboard from '@/features/dashboard/views/QualiteDashboard.vue'
import HseDashboard from '@/features/dashboard/views/HseDashboard.vue'
import SuperviseurDashboard from '@/features/dashboard/views/SuperviseurDashboard.vue'
import ResponsableQualiteDashboard from '@/features/dashboard/views/ResponsableQualiteDashboard.vue'
import {
  fetchStructure,
  createProject,
  createZone,
  createPoste,
  addProjectMember
} from '@/features/structure/services/structureService'

const ROLE_LABELS = {
  CHEF_DE_PROJET: 'Chef de Projet',
  RESPONSABLE_QUALITE: 'Responsable Qualité',
  AGENT_QUALITE: 'Agent Qualité',
  RESPONSABLE_HSE: 'Responsable HSE',
  SUPERVISEUR: 'Superviseur',
}

const router = useRouter()
const authStore = useAuthStore()

// State for Password Change Form
const oldPassword = ref('')
const newPassword = ref('')
const confirmPassword = ref('')
const changePasswordMsg = ref('')
const changePasswordSuccess = ref(false)
const changePasswordLoading = ref(false)

// State for Configurable Structure Module
const activeSection = ref('dashboard')
const structure = ref({ projects: [] })
const structureLoading = ref(false)
const structureError = ref('')
const newProjectName = ref('')
const newZoneName = ref('')
const selectedProjectId = ref('')
const newPosteName = ref('')
const selectedZoneId = ref('')
const structureMsg = ref('')

// State for project member assignment
const projectRoles = ['CHEF_DE_PROJET', 'RESPONSABLE_QUALITE', 'AGENT_QUALITE', 'RESPONSABLE_HSE', 'SUPERVISEUR']
const membersByRole = ref({
  CHEF_DE_PROJET: null,
  RESPONSABLE_QUALITE: null,
  AGENT_QUALITE: null,
  RESPONSABLE_HSE: null,
  SUPERVISEUR: null,
})
const usersByRole = ref({})
const loadingUsersByRole = ref(false)

// Member assignment for existing projects
const selectedMemberUserId = ref({})
const selectedMemberRoleProjet = ref({})
const assignMemberLoading = ref({})

// Computed Properties
const user = computed(() => authStore.user)
const roleComponent = computed(() => {
  if (['creer-operateur', 'saisir-suivi', 'demande-maj'].includes(activeSection.value)) {
    return ChefEquipeDashboard
  }
  return ({
    ADMIN: AdminDashboard,
    CHEF_EQUIPE: ChefEquipeDashboard,
    RH: RhDashboard,
    QUALITE: QualiteDashboard,
    RESPONSABLE_QUALITE: ResponsableQualiteDashboard,
    HSE: HseDashboard,
    SUPERVISEUR: SuperviseurDashboard,
  })[user.value?.role]
})
const roleLabel = computed(() => {
  if (!user.value) return ''
  switch (user.value.role) {
    case 'ADMIN':
      return 'Administrateur'
    case 'CHEF_EQUIPE':
      return "Chef d'équipe"
    case 'RH':
      return 'Ressources Humaines'
    case 'QUALITE':
      return 'Qualité'
    case 'RESPONSABLE_QUALITE':
      return 'Responsable Qualité'
    case 'HSE':
      return 'Responsable HSE'
    case 'SUPERVISEUR':
      return 'Superviseur'
    default:
      return user.value.role
  }
})

// Authentication & Logout Actions
function handleLogout() {
  authStore.logout()
  router.push({ name: 'login' })
}

// Password Change Action
async function handlePasswordChange() {
  if (newPassword.value !== confirmPassword.value) {
    changePasswordMsg.value = 'Les mots de passe ne correspondent pas.'
    return
  }
  changePasswordLoading.value = true
  changePasswordMsg.value = ''
  try {
    await authStore.changePassword(oldPassword.value, newPassword.value)
    changePasswordSuccess.value = true
    changePasswordMsg.value = 'Mot de passe mis à jour avec succès! Redirection...'
    setTimeout(() => {
      changePasswordSuccess.value = false
      oldPassword.value = ''
      newPassword.value = ''
      confirmPassword.value = ''
    }, 1500)
  } catch (error) {
    changePasswordMsg.value = error.message || 'Impossible de changer le mot de passe.'
  } finally {
    changePasswordLoading.value = false
  }
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
      const selectedProject = data.projects.find(
        (project) => project.idProjet === Number(selectedProjectId.value),
      )
      const project = selectedProject || data.projects[0]
      selectedProjectId.value = project.idProjet

      const selectedZone = project.zones?.find(
        (zone) => zone.idZone === Number(selectedZoneId.value),
      )
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

function requireStructureSelection(value, label) {
  if (value) {
    return true
  }
  structureMsg.value = `Veuillez sélectionner ${label}.`
  return false
}

function formatCreation(dateCreation) {
  if (!dateCreation) return 'Date non disponible'
  return new Intl.DateTimeFormat('fr-FR', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(new Date(dateCreation))
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
      const response = await fetch(`/api/utilisateurs/par-role?role=${systemRole}`, {
        headers: { Authorization: `Bearer ${authStore.token}` },
      })
      if (response.ok) {
        result[projectRole] = await response.json()
      } else {
        result[projectRole] = []
      }
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
    if (userId) {
      membres.push({
        utilisateurId: Number(userId),
        roleProjet,
      })
    }
  }

  try {
    await createProject(authStore.token, newProjectName.value.trim(), membres)
    newProjectName.value = ''
    for (const key of Object.keys(membersByRole.value)) {
      membersByRole.value[key] = null
    }
    await fetchStructureData()
    structureMsg.value = 'Projet créé avec succès.'
  } catch (error) {
    structureMsg.value = error.message
  }
}

async function handleCreateZone() {
  if (!requireStructureSelection(selectedProjectId.value, 'un projet') || !newZoneName.value.trim())
    return
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
  if (!requireStructureSelection(selectedZoneId.value, 'une zone') || !newPosteName.value.trim())
    return
  structureMsg.value = ''
  try {
    await createPoste(authStore.token, Number(selectedZoneId.value), newPosteName.value.trim())
    newPosteName.value = ''
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
    structureMsg.value = "Membre affecté avec succès au projet."
  } catch (error) {
    structureError.value = error.message
  } finally {
    assignMemberLoading.value[projectId] = false
  }
}

watch(activeSection, (section) => {
  if (section === 'structure' && authStore.token) {
    loadUsersByRole()
  }
})

onMounted(() => {
  fetchStructureData()
  loadUsersByRole()
})
</script>

<template>
  <div class="dashboard-container">
    <!-- Forced Password Change View for First Connection -->
    <div v-if="authStore.mustChangePassword" class="password-change-overlay">
      <div class="password-card">
        <div class="card-header">
          <span class="lock-icon">🔒</span>
          <h2>Changement de mot de passe obligatoire</h2>
          <p class="desc">
            C'est votre première connexion. Veuillez définir un nouveau mot de passe pour des
            raisons de sécurité.
          </p>
        </div>

        <form @submit.prevent="handlePasswordChange" class="pwd-form">
          <div class="input-group">
            <label>Mot de passe actuel</label>
            <input
              v-model="oldPassword"
              type="password"
              required
              placeholder="Saisir votre mot de passe de test"
            />
          </div>

          <div class="input-group">
            <label>Nouveau mot de passe</label>
            <input
              v-model="newPassword"
              type="password"
              required
              placeholder="Minimum 6 caractères"
            />
          </div>

          <div class="input-group">
            <label>Confirmer le nouveau mot de passe</label>
            <input
              v-model="confirmPassword"
              type="password"
              required
              placeholder="Confirmer le mot de passe"
            />
          </div>

          <button type="submit" :disabled="changePasswordLoading" class="action-btn">
            {{ changePasswordLoading ? 'Modification...' : 'Modifier et continuer' }}
          </button>

          <div
            v-if="changePasswordMsg"
            :class="['message-box', changePasswordSuccess ? 'success' : 'error']"
          >
            {{ changePasswordMsg }}
          </div>
        </form>
      </div>
    </div>

    <!-- Main Dashboard Application View -->
    <div v-else class="app-layout">
      <!-- Sidebar -->
      <aside class="app-sidebar">
        <div class="brand">
          <div class="opmobility-mark" aria-hidden="true">op</div>
          <div class="brand-text">
            <h3><span>OP</span>mobility</h3>
            <span class="brand-sub">ILU management system</span>
          </div>
        </div>

        <div class="user-profile">
          <div class="avatar">{{ user?.nom?.substring(0, 2).toUpperCase() }}</div>
          <div class="user-info">
            <span class="user-name">{{ user?.nom }}</span>
            <span class="user-role-pill">{{ roleLabel }}</span>
          </div>
        </div>

        <nav class="sidebar-nav">
          <button
            type="button"
            class="nav-item"
            :class="{ active: activeSection === 'dashboard' }"
            @click="activeSection = 'dashboard'"
          >
            <span class="nav-icon">📊</span>
            <span>Dashboard</span>
          </button>

          <!-- Chef Equipe Sub-menus -->
          <template v-if="user?.role === 'CHEF_EQUIPE'">
            <button
              type="button"
              class="nav-item"
              :class="{ active: activeSection === 'creer-operateur' }"
              @click="activeSection = 'creer-operateur'"
            >
              <span class="nav-icon">➕</span>
              <span>Créer opérateur</span>
            </button>
            <button
              type="button"
              class="nav-item"
              :class="{ active: activeSection === 'saisir-suivi' }"
              @click="activeSection = 'saisir-suivi'"
            >
              <span class="nav-icon">📝</span>
              <span>Saisir suivi</span>
            </button>
            <button
              type="button"
              class="nav-item"
              :class="{ active: activeSection === 'demande-maj' }"
              @click="activeSection = 'demande-maj'"
            >
              <span class="nav-icon">🔔</span>
              <span>Demande MAJ</span>
            </button>
          </template>

          <button
            v-if="['ADMIN', 'CHEF_EQUIPE', 'SUPERVISEUR'].includes(user?.role)"
            type="button"
            class="nav-item"
            :class="{ active: activeSection === 'structure' }"
            @click="activeSection = 'structure'"
          >
            <span class="nav-icon">⚙️</span>
            <span>Projet</span>
          </button>
        </nav>

        <div class="sidebar-footer">
          <button @click="handleLogout" class="logout-btn"><span>🚪</span> Se déconnecter</button>
        </div>
      </aside>

      <!-- Main Dashboard Panel -->
      <main class="app-main">
        <header class="main-header">
          <div class="header-title">
            <h1>Tableau de bord</h1>
            <p>Bienvenue, {{ user?.nom }} (Matricule: {{ user?.matricule }})</p>
          </div>
          <div class="header-date">
            📅
            {{
              new Date().toLocaleDateString('fr-FR', {
                weekday: 'long',
                year: 'numeric',
                month: 'long',
                day: 'numeric',
              })
            }}
          </div>
        </header>

        <section v-if="activeSection === 'structure'" class="role-section">
          <div class="panel-card structure-panel">
            <div class="panel-header">
              <h3>Gestion des projets</h3>
            </div>
            <div v-if="structureMsg" class="message-box success">{{ structureMsg }}</div>
            <div v-if="structureError" class="message-box error">{{ structureError }}</div>
            <div v-if="structureLoading" class="loading-state">
              <span class="spinner-blue"></span> Chargement des projets...
            </div>
            <div v-else class="structure-grid" style="display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 1rem;">
              <div class="panel-card compact-card">
                <div class="panel-header">
                  <h4>Créer un projet</h4>
                </div>
                <form @submit.prevent="handleCreateProject" class="panel-form">
                  <div class="input-group">
                    <label>Nom du projet</label>
                    <input v-model="newProjectName" required placeholder="Ex: Smart Car" />
                  </div>
                  <div v-if="loadingUsersByRole" class="loading-state">
                    <span class="spinner-blue"></span> Chargement des utilisateurs...
                  </div>
                  <div v-else class="member-assignments">
                    <div v-for="roleKey in projectRoles" :key="roleKey" class="input-group">
                      <label style="font-size: 0.8rem; margin-top: 0.5rem;">{{ ROLE_LABELS[roleKey] }}</label>
                      <select v-model="membersByRole[roleKey]">
                        <option :value="null">— Non affecté —</option>
                        <option
                          v-for="u in usersByRole[roleKey] || []"
                          :key="u.id"
                          :value="u.id"
                        >
                          {{ u.nom }} ({{ u.matricule }})
                        </option>
                      </select>
                      <span v-if="!(usersByRole[roleKey] || []).length" class="empty-hint" style="font-size: 0.75rem; color: #a6c0c0;">
                        Aucun utilisateur disponible
                      </span>
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
                      <option
                        v-for="project in structure.projects"
                        :key="project.idProjet"
                        :value="project.idProjet"
                      >
                        {{ project.nom }}
                      </option>
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
                      <option
                        v-for="project in structure.projects"
                        :key="project.idProjet"
                        :value="null"
                        disabled
                      >
                        Choisir un projet puis une zone
                      </option>
                      <template v-for="project in structure.projects" :key="project.idProjet">
                        <optgroup v-if="project.zones?.length" :label="project.nom">
                          <option
                            v-for="zone in project.zones"
                            :key="zone.idZone"
                            :value="zone.idZone"
                          >
                            {{ zone.nom }}
                          </option>
                        </optgroup>
                      </template>
                    </select>
                  </div>
                  <div class="input-group">
                    <label>Nom du poste</label>
                    <input v-model="newPosteName" required placeholder="Ex: Poste 1" />
                  </div>
                  <button type="submit" class="submit-btn">Ajouter le poste</button>
                </form>
              </div>
            </div>
            <div class="panel-card" style="margin-top: 1.5rem;">
              <div class="panel-header"><h4>Projets actuels</h4></div>
              <div
                v-for="project in structure.projects"
                :key="project.idProjet"
                class="structure-tree"
              >
                <div class="tree-project">
                  <strong>📁 {{ project.nom }}</strong>
                  <span class="creation-meta"
                    >Créé le {{ formatCreation(project.dateCreation) }} par
                    {{ project.creePar }}</span
                  >
                </div>

                <!-- Display Members -->
                <div class="project-members" style="margin-top: 0.5rem; padding-left: 1.2rem; text-align: left;">
                  <strong style="font-size: 0.85rem;">Membres du projet :</strong>
                  <div v-if="project.membres && project.membres.length" style="display: flex; flex-wrap: wrap; gap: 0.5rem; margin-top: 0.25rem;">
                    <span v-for="member in project.membres" :key="member.id" class="role-badge">
                      👤 {{ member.nom }} ({{ ROLE_LABELS[member.roleProjet] || member.roleProjet }})
                    </span>
                  </div>
                  <div v-else style="color: #547174; font-size: 0.8rem; margin-top: 0.25rem; font-style: italic;">Aucun membre affecté.</div>
                </div>

                <!-- Affecter un membre Form -->
                <form @submit.prevent="handleAssignProjectMember(project.idProjet)" class="form-row" style="margin-top: 0.8rem; margin-left: 1.2rem; align-items: flex-end; gap: 0.5rem; background: #f2f8f7; padding: 0.75rem; border-radius: 6px; border: 1px solid #d8e5e4;">
                  <div class="input-group" style="flex: 1; margin: 0;">
                    <label style="font-size: 0.75rem;">Rôle projet</label>
                    <select v-model="selectedMemberRoleProjet[project.idProjet]" @change="selectedMemberUserId[project.idProjet] = ''" style="padding: 0.4rem; font-size: 0.85rem; height: 34px;">
                      <option value="" disabled selected>-- Rôle --</option>
                      <option v-for="roleKey in projectRoles" :key="roleKey" :value="roleKey">
                        {{ ROLE_LABELS[roleKey] }}
                      </option>
                    </select>
                  </div>
                  
                  <div class="input-group" style="flex: 1.5; margin: 0;">
                    <label style="font-size: 0.75rem;">Utilisateur</label>
                    <select v-model="selectedMemberUserId[project.idProjet]" :disabled="!selectedMemberRoleProjet[project.idProjet]" style="padding: 0.4rem; font-size: 0.85rem; height: 34px;">
                      <option value="" disabled selected>-- Utilisateur --</option>
                      <option v-for="u in usersByRole[selectedMemberRoleProjet[project.idProjet]] || []" :key="u.id" :value="u.id">
                        {{ u.nom }} ({{ u.matricule }})
                      </option>
                    </select>
                  </div>
                  
                  <button type="submit" :disabled="!selectedMemberUserId[project.idProjet] || assignMemberLoading[project.idProjet]" class="submit-btn" style="padding: 0.4rem 0.8rem; font-size: 0.85rem; height: 34px; line-height: 1;">
                    {{ assignMemberLoading[project.idProjet] ? '...' : 'Affecter' }}
                  </button>
                </form>

                <div v-for="zone in project.zones || []" :key="zone.idZone" class="tree-zone">
                  <div class="tree-zone-title">
                    <strong>📍 {{ zone.nom }}</strong>
                    <span class="creation-meta"
                      >Créée le {{ formatCreation(zone.dateCreation) }} par {{ zone.creePar }}</span
                    >
                  </div>
                  <ul>
                    <li v-for="poste in zone.postes || []" :key="poste.idPoste">
                      <strong>🛠️ {{ poste.nom }}</strong>
                      <span class="creation-meta"
                        >Créé le {{ formatCreation(poste.dateCreation) }} par
                        {{ poste.creePar || 'Système' }}</span
                      >
                    </li>
                  </ul>
                </div>
              </div>
            </div>
          </div>
        </section>

        <component v-else-if="roleComponent" :is="roleComponent" :active-section="activeSection" />
      </main>
    </div>
  </div>
</template>

<style scoped>
/* No ad-hoc styling needed as main styles are globally imported via dashboard-shared.css */
</style>
