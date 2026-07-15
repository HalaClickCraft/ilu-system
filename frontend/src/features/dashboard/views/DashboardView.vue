<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import AdminDashboard from '@/features/dashboard/views/AdminDashboard.vue'
import ChefEquipeDashboard from '@/features/dashboard/views/ChefEquipeDashboard.vue'
import RhDashboard from '@/features/dashboard/views/RhDashboard.vue'
import QualiteDashboard from '@/features/dashboard/views/QualiteDashboard.vue'
import HseDashboard from '@/features/dashboard/views/HseDashboard.vue'
import SuperviseurDashboard from '@/features/dashboard/views/SuperviseurDashboard.vue'
import {
  fetchStructure,
  createProject,
  createZone,
  createPoste,
} from '@/features/structure/services/structureService'

const router = useRouter()
const authStore = useAuthStore()

// State for Password Change Form
const oldPassword = ref('')
const newPassword = ref('')
const confirmPassword = ref('')
const changePasswordMsg = ref('')
const changePasswordSuccess = ref(false)
const changePasswordLoading = ref(false)

// State for Admin Panel (Real API data)
const users = ref([])
const usersLoading = ref(false)
const usersError = ref('')
const newUserMatricule = ref('')
const newUserNom = ref('')
const newUserCin = ref('')
const newUserRole = ref('CHEF_EQUIPE')
const newUserLoading = ref(false)
const newUserMsg = ref('')

// State for Chef d'equipe Panel (Simulated tables: Suivi Integration, Demandes MAJ)
const integrationLogs = ref([
  { id: 1, jour: 1, cadence: 80, defauts: 2, remarques: 'Bon démarrage, opérateur motivé.' },
  {
    id: 2,
    jour: 2,
    cadence: 95,
    defauts: 1,
    remarques: 'Cadence atteinte avec une bonne qualité.',
  },
])
const newDay = ref(3)
const newCadence = ref(100)
const newDefauts = ref(0)
const newRemarques = ref('')
const teamRequests = ref([
  { id: 1, type: 'Ajout Opérateur', date: '2026-07-10', statut: 'Validé' },
  { id: 2, type: 'Modification Shift', date: '2026-07-14', statut: 'En attente' },
])
const requestType = ref('Ajout Opérateur')
const requestMsg = ref('')

// State for RH Panel (Simulated Operator directory and skills matrix)
const operators = ref([
  {
    matricule: 'OP001',
    nom: 'Amine Ben Ali',
    embauche: '2025-01-10',
    statut: 'Actif',
    rework: false,
  },
  {
    matricule: 'OP002',
    nom: 'Salma Mansour',
    embauche: '2025-03-15',
    statut: 'Actif',
    rework: true,
  },
  {
    matricule: 'OP003',
    nom: 'Youssef Trabelsi',
    embauche: '2025-06-01',
    statut: 'Formation',
    rework: false,
  },
])
const rhStats = { total: 18, polyvalence: '78%', formation: 4 }

// State for Qualité Panel (Questionnaires / Templates / Questions)
const templates = ref([
  { id: 1, nom: 'Évaluation Standard Shift A', ordre: 1, date: '2026-07-01' },
  { id: 2, nom: 'Évaluation Avancée Rework', ordre: 2, date: '2026-07-05' },
])
const questions = ref([
  {
    id: 1,
    enonce: "Vérifier la conformité de l'étiquetage du bloc arrière",
    reponse: 'Conforme et lisible',
    bloc: 'Bloc A',
  },
  {
    id: 2,
    enonce: 'Calculer le temps de cycle standard sur poste de vissage',
    reponse: '42 secondes',
    bloc: 'Bloc B',
  },
])
const newQuestionEnonce = ref('')
const newQuestionReponse = ref('')
const newQuestionBloc = ref('Bloc A')

// State for HSE Panel (Safety check lists / Safety indexes)
const safetyChecks = ref([
  { id: 1, zone: 'Zone Assemblage A', portEPI: true, securiteMachine: true, statut: 'Sécurisé' },
  {
    id: 2,
    zone: 'Zone Finition B',
    portEPI: true,
    securiteMachine: false,
    statut: 'Alerte Mineure',
  },
])
const hseLogs = ref([
  {
    id: 101,
    date: '2026-07-14 09:15',
    auteur: 'Hélène HSE',
    motif: 'Ajout de consigne de sécurité Zone A',
  },
  {
    id: 102,
    date: '2026-07-14 11:30',
    auteur: 'Hélène HSE',
    motif: 'Correction gabarit sécurité incendie',
  },
])

// State for Superviseur Panel (Sessions, line assignment)
const sessions = ref([
  { id: 1, type: 'Hebdomadaire', date: '2026-07-14', statut: 'En cours', score: 85.5 },
  { id: 2, type: 'Mensuelle', date: '2026-06-30', statut: 'Clôturée', score: 91.2 },
])
const assignments = ref([
  {
    poste: 'Poste Assemblage 1',
    operateur: 'Amine Ben Ali',
    shift: 'Shift Matin',
    statut: 'Présent',
  },
  {
    poste: 'Poste Contrôle Qualité',
    operateur: 'Salma Mansour',
    shift: 'Shift Matin',
    statut: 'Présent',
  },
  { poste: 'Poste Emballage 3', operateur: 'Aucun', shift: 'Shift Matin', statut: 'Vide' },
])

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

// Computed Properties
const user = computed(() => authStore.user)
const roleComponent = computed(() => ({
  ADMIN: AdminDashboard,
  CHEF_EQUIPE: ChefEquipeDashboard,
  RH: RhDashboard,
  QUALITE: QualiteDashboard,
  HSE: HseDashboard,
  SUPERVISEUR: SuperviseurDashboard,
}[user.value?.role]))
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
      // Reload or refresh to switch view
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

// Admin API Actions
async function fetchUsers() {
  if (user.value?.role !== 'ADMIN') return
  usersLoading.value = true
  usersError.value = ''
  try {
    const response = await fetch('/api/utilisateurs', {
      headers: {
        Authorization: `Bearer ${authStore.token}`,
      },
    })
    if (!response.ok) throw new Error('Erreur lors du chargement des utilisateurs')
    users.value = await response.json()
  } catch (error) {
    usersError.value = error.message
  } finally {
    usersLoading.value = false
  }
}

async function handleCreateUser() {
  newUserMsg.value = ''
  newUserLoading.value = true
  try {
    const response = await fetch('/api/utilisateurs', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${authStore.token}`,
      },
      body: JSON.stringify({
        matricule: newUserMatricule.value,
        nom: newUserNom.value,
        cin: newUserCin.value,
        role: newUserRole.value,
      }),
    })
    if (!response.ok) {
      const data = await response.json().catch(() => ({}))
      throw new Error(data.message || "Erreur lors de la création de l'utilisateur")
    }
    newUserMatricule.value = ''
    newUserNom.value = ''
    newUserCin.value = ''
    newUserMsg.value = 'Utilisateur créé avec succès (mot de passe initial = CIN)!'
    await fetchUsers()
  } catch (error) {
    newUserMsg.value = `Erreur: ${error.message}`
  } finally {
    newUserLoading.value = false
  }
}

async function toggleUserStatus(u) {
  const endpoint = u.actif
    ? `/api/utilisateurs/${u.id}/suspendre`
    : `/api/utilisateurs/${u.id}/reactiver`
  try {
    const response = await fetch(endpoint, {
      method: 'PUT',
      headers: {
        Authorization: `Bearer ${authStore.token}`,
      },
    })
    if (!response.ok) throw new Error('Impossible de modifier le statut')
    await fetchUsers()
  } catch (error) {
    alert(error.message)
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

async function handleCreateProject() {
  if (!newProjectName.value.trim()) return
  structureMsg.value = ''
  try {
    await createProject(authStore.token, newProjectName.value.trim())
    newProjectName.value = ''
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
    await createPoste(authStore.token, Number(selectedZoneId.value), newPosteName.value.trim())
    newPosteName.value = ''
    await fetchStructureData()
    structureMsg.value = 'Poste de travail créé avec succès.'
  } catch (error) {
    structureMsg.value = error.message
  }
}

// Chef d'équipe Simulation Actions
function addIntegrationLog() {
  integrationLogs.value.push({
    id: Date.now(),
    jour: newDay.value,
    cadence: newCadence.value,
    defauts: newDefauts.value,
    remarques: newRemarques.value || 'N/A',
  })
  newDay.value++
  newRemarques.value = ''
}

function submitTeamRequest() {
  if (!requestMsg.value) return
  teamRequests.value.push({
    id: Date.now(),
    type: requestType.value,
    date: new Date().toISOString().split('T')[0],
    statut: 'En attente',
  })
  requestMsg.value = ''
}

// Qualite Simulation Actions
function addQuestion() {
  if (!newQuestionEnonce.value || !newQuestionReponse.value) return
  questions.value.push({
    id: Date.now(),
    enonce: newQuestionEnonce.value,
    reponse: newQuestionReponse.value,
    bloc: newQuestionBloc.value,
  })
  newQuestionEnonce.value = ''
  newQuestionReponse.value = ''
}

// Fetch on mount
onMounted(() => {
  if (user.value?.role === 'ADMIN') {
    fetchUsers()
    fetchStructureData()
  } else if (['CHEF_EQUIPE', 'SUPERVISEUR'].includes(user.value?.role)) {
    fetchStructureData()
  }
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
            <div v-else class="structure-grid">
              <div class="panel-card compact-card">
                <div class="panel-header"><h4>Créer un projet</h4></div>
                <form @submit.prevent="handleCreateProject" class="panel-form">
                  <div class="input-group">
                    <label>Nom du projet</label>
                    <input v-model="newProjectName" required placeholder="Ex: Smart Car" />
                  </div>
                  <button type="submit" class="submit-btn">Ajouter le projet</button>
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
                          <option v-for="zone in project.zones" :key="zone.idZone" :value="zone.idZone">
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
            <div class="panel-card">
              <div class="panel-header"><h4>Projets actuels</h4></div>
              <div v-for="project in structure.projects" :key="project.idProjet" class="structure-tree">
                <div class="tree-project">
                  <strong>📁 {{ project.nom }}</strong>
                  <span class="creation-meta">Créé le {{ formatCreation(project.dateCreation) }} par {{ project.creePar }}</span>
                </div>
                <div v-for="zone in project.zones || []" :key="zone.idZone" class="tree-zone">
                  <div class="tree-zone-title">
                    <strong>📍 {{ zone.nom }}</strong>
                    <span class="creation-meta">Créée le {{ formatCreation(zone.dateCreation) }} par {{ zone.creePar }}</span>
                  </div>
                  <ul>
                    <li v-for="poste in zone.postes || []" :key="poste.idPoste">
                      <strong>🛠️ {{ poste.nom }}</strong>
                      <span class="creation-meta">Créé le {{ formatCreation(poste.dateCreation) }} par {{ poste.creePar || 'Système' }}</span>
                    </li>
                  </ul>
                </div>
              </div>
            </div>
          </div>
        </section>

        <component v-else-if="roleComponent" :is="roleComponent" />

        <!-- Legacy role panels kept temporarily while their data is migrated to dedicated components. -->
        <!-- Role Panel: ADMIN -->
        <section v-else-if="user?.role === 'ADMIN'" class="role-section">
          <!-- Overview statistics -->
          <div class="stats-grid">
            <div class="stat-card">
              <span class="stat-icon">👥</span>
              <div class="stat-content">
                <span class="stat-val">{{ users.length }}</span>
                <span class="stat-lbl">Utilisateurs enregistrés</span>
              </div>
            </div>
            <div class="stat-card">
              <span class="stat-icon">🟢</span>
              <div class="stat-content">
                <span class="stat-val">{{ users.filter((u) => u.actif).length }}</span>
                <span class="stat-lbl">Utilisateurs actifs</span>
              </div>
            </div>
            <div class="stat-card">
              <span class="stat-icon">🔴</span>
              <div class="stat-content">
                <span class="stat-val">{{ users.filter((u) => !u.actif).length }}</span>
                <span class="stat-lbl">Utilisateurs suspendus</span>
              </div>
            </div>
          </div>

          <div class="admin-grid">
            <!-- Create User Form -->
            <div class="panel-card">
              <div class="panel-header">
                <h3>Ajouter un Utilisateur</h3>
              </div>
              <form @submit.prevent="handleCreateUser" class="panel-form">
                <div class="form-row">
                  <div class="input-group">
                    <label>Matricule</label>
                    <input v-model="newUserMatricule" required placeholder="Ex: chef2" />
                  </div>
                  <div class="input-group">
                    <label>Nom complet</label>
                    <input v-model="newUserNom" required placeholder="Ex: Paul Martin" />
                  </div>
                </div>
                <div class="form-row">
                  <div class="input-group">
                    <label>Numéro CIN</label>
                    <input v-model="newUserCin" required placeholder="Ex: 09876543" />
                  </div>
                  <div class="input-group">
                    <label>Rôle</label>
                    <select v-model="newUserRole">
                      <option value="ADMIN">ADMIN</option>
                      <option value="CHEF_EQUIPE">CHEF_EQUIPE</option>
                      <option value="RH">RH</option>
                      <option value="QUALITE">QUALITE</option>
                      <option value="HSE">HSE</option>
                      <option value="SUPERVISEUR">SUPERVISEUR</option>
                    </select>
                  </div>
                </div>
                <button type="submit" :disabled="newUserLoading" class="submit-btn">
                  {{ newUserLoading ? 'Création...' : "Créer l'utilisateur" }}
                </button>
                <p v-if="newUserMsg" class="form-msg">{{ newUserMsg }}</p>
              </form>
            </div>

            <!-- Users List -->
            <div class="panel-card list-users-card">
              <div class="panel-header">
                <h3>Liste des Utilisateurs</h3>
                <button @click="fetchUsers" class="refresh-btn">🔄 Actualiser</button>
              </div>

              <div v-if="usersLoading" class="loading-state">
                <span class="spinner-blue"></span> Chargement des utilisateurs...
              </div>

              <div v-else-if="usersError" class="error-state">⚠️ {{ usersError }}</div>

              <div v-else class="table-wrapper">
                <table class="data-table">
                  <thead>
                    <tr>
                      <th>Matricule</th>
                      <th>Nom</th>
                      <th>Rôle</th>
                      <th>Doit changer MDP</th>
                      <th>Statut</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="u in users" :key="u.id">
                      <td>
                        <code>{{ u.matricule }}</code>
                      </td>
                      <td>
                        <strong>{{ u.nom }}</strong>
                      </td>
                      <td>
                        <span class="role-badge">{{ u.role?.libelle }}</span>
                      </td>
                      <td>{{ u.doitChangerMdp ? 'Oui' : 'Non' }}</td>
                      <td>
                        <span :class="['status-badge', u.actif ? 'active' : 'suspended']">
                          {{ u.actif ? 'Actif' : 'Suspendu' }}
                        </span>
                      </td>
                      <td>
                        <button
                          @click="toggleUserStatus(u)"
                          :class="['status-btn', u.actif ? 'btn-suspend' : 'btn-activate']"
                        >
                          {{ u.actif ? 'Suspendre' : 'Réactiver' }}
                        </button>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </section>

        <!-- Role Panel: CHEF_EQUIPE -->
        <section v-else-if="user?.role === 'CHEF_EQUIPE'" class="role-section">
          <div class="stats-grid">
            <div class="stat-card">
              <span class="stat-icon">👷</span>
              <div class="stat-content">
                <span class="stat-val">12</span>
                <span class="stat-lbl">Opérateurs dans l'équipe</span>
              </div>
            </div>
            <div class="stat-card">
              <span class="stat-icon">📈</span>
              <div class="stat-content">
                <span class="stat-val">96%</span>
                <span class="stat-lbl">Cadence d'équipe moyenne</span>
              </div>
            </div>
            <div class="stat-card">
              <span class="stat-icon">🔔</span>
              <div class="stat-content">
                <span class="stat-val">{{ teamRequests.length }}</span>
                <span class="stat-lbl">Demandes de mise à jour</span>
              </div>
            </div>
          </div>

          <div class="admin-grid">
            <!-- Daily Integration Follow-up Form -->
            <div class="panel-card">
              <div class="panel-header">
                <h3>Saisir un Suivi d'Intégration Journalier</h3>
              </div>
              <form @submit.prevent="addIntegrationLog" class="panel-form">
                <div class="form-row">
                  <div class="input-group">
                    <label>Jour d'Intégration</label>
                    <input v-model="newDay" type="number" required />
                  </div>
                  <div class="input-group">
                    <label>Cadence Réalisée (%)</label>
                    <input v-model="newCadence" type="number" required />
                  </div>
                </div>
                <div class="input-group">
                  <label>Nombre de défauts détectés</label>
                  <input v-model="newDefauts" type="number" required />
                </div>
                <div class="input-group">
                  <label>Remarques</label>
                  <textarea
                    v-model="newRemarques"
                    placeholder="Observations et points de blocage..."
                  ></textarea>
                </div>
                <button type="submit" class="submit-btn">Enregistrer le suivi</button>
              </form>
            </div>

            <!-- Recent logs list -->
            <div class="panel-card">
              <div class="panel-header">
                <h3>Suivis Journaliers Récents</h3>
              </div>
              <div class="table-wrapper">
                <table class="data-table">
                  <thead>
                    <tr>
                      <th>Jour d'intégration</th>
                      <th>Cadence Réalisée</th>
                      <th>Défauts détectés</th>
                      <th>Remarques</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="log in integrationLogs" :key="log.id">
                      <td>Jour {{ log.jour }}</td>
                      <td>
                        <strong>{{ log.cadence }}%</strong>
                      </td>
                      <td>{{ log.defauts }}</td>
                      <td>{{ log.remarques }}</td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </div>

          <div class="admin-grid" style="margin-top: 2rem">
            <!-- Submit Team Update Request -->
            <div class="panel-card">
              <div class="panel-header">
                <h3>Nouvelle demande de Mise à Jour de l'Équipe</h3>
              </div>
              <form @submit.prevent="submitTeamRequest" class="panel-form">
                <div class="input-group">
                  <label>Type de demande</label>
                  <select v-model="requestType">
                    <option value="Ajout Opérateur">Ajout Opérateur</option>
                    <option value="Modification Shift">Modification Shift</option>
                    <option value="Alerte Effectif">Alerte Effectif</option>
                  </select>
                </div>
                <div class="input-group">
                  <label>Message explicatif</label>
                  <textarea
                    v-model="requestMsg"
                    placeholder="Saisir la raison de la demande..."
                    required
                  ></textarea>
                </div>
                <button type="submit" class="submit-btn btn-secondary">Envoyer la demande</button>
              </form>
            </div>

            <!-- Team Requests list -->
            <div class="panel-card">
              <div class="panel-header">
                <h3>Historique des Demandes (DemandeMajEquipe)</h3>
              </div>
              <div class="table-wrapper">
                <table class="data-table">
                  <thead>
                    <tr>
                      <th>Type</th>
                      <th>Date demande</th>
                      <th>Statut</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="req in teamRequests" :key="req.id">
                      <td>
                        <strong>{{ req.type }}</strong>
                      </td>
                      <td>{{ req.date }}</td>
                      <td>
                        <span
                          :class="[
                            'status-badge',
                            req.statut === 'Validé' ? 'active' : 'suspended',
                          ]"
                        >
                          {{ req.statut }}
                        </span>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </section>

        <!-- Role Panel: RH -->
        <section v-else-if="user?.role === 'RH'" class="role-section">
          <div class="stats-grid">
            <div class="stat-card">
              <span class="stat-icon">👔</span>
              <div class="stat-content">
                <span class="stat-val">{{ rhStats.total }}</span>
                <span class="stat-lbl">Opérateurs enregistrés</span>
              </div>
            </div>
            <div class="stat-card">
              <span class="stat-icon">🏆</span>
              <div class="stat-content">
                <span class="stat-val">{{ rhStats.polyvalence }}</span>
                <span class="stat-lbl">Taux de polyvalence cible</span>
              </div>
            </div>
            <div class="stat-card">
              <span class="stat-icon">🎓</span>
              <div class="stat-content">
                <span class="stat-val">{{ rhStats.formation }}</span>
                <span class="stat-lbl">Opérateurs en formation</span>
              </div>
            </div>
          </div>

          <div class="admin-grid">
            <!-- Operator directory -->
            <div class="panel-card list-users-card">
              <div class="panel-header">
                <h3>Annuaire des Opérateurs</h3>
              </div>
              <div class="table-wrapper">
                <table class="data-table">
                  <thead>
                    <tr>
                      <th>Matricule</th>
                      <th>Nom</th>
                      <th>Date d'embauche</th>
                      <th>Formation Rework</th>
                      <th>Statut</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="op in operators" :key="op.matricule">
                      <td>
                        <code>{{ op.matricule }}</code>
                      </td>
                      <td>
                        <strong>{{ op.nom }}</strong>
                      </td>
                      <td>{{ op.embauche }}</td>
                      <td>{{ op.rework ? '✅ Oui' : '❌ Non' }}</td>
                      <td>
                        <span
                          :class="['status-badge', op.statut === 'Actif' ? 'active' : 'suspended']"
                        >
                          {{ op.statut }}
                        </span>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>

            <!-- Skills Matrix (Polyvalence View) -->
            <div class="panel-card">
              <div class="panel-header">
                <h3>Matrice de Polyvalence (Postes / Compétences)</h3>
              </div>
              <div class="skills-matrix-sim">
                <p class="subtitle">
                  Indicateur de compétence d'après le diagramme de classe (SessionEvaluation ->
                  niveauObtenu)
                </p>
                <div class="matrix-grid">
                  <div class="matrix-row header-row">
                    <span class="header-cell">Opérateur</span>
                    <span class="header-cell">Poste Assemblage</span>
                    <span class="header-cell">Poste Vissage</span>
                    <span class="header-cell">Finition</span>
                  </div>
                  <div class="matrix-row">
                    <span class="cell-name">Amine Ben Ali</span>
                    <span class="cell-level lvl-4">Niveau 4 (Correct)</span>
                    <span class="cell-level lvl-3">Niveau 3</span>
                    <span class="cell-level lvl-1">Niveau 1</span>
                  </div>
                  <div class="matrix-row">
                    <span class="cell-name">Salma Mansour</span>
                    <span class="cell-level lvl-2">Niveau 2</span>
                    <span class="cell-level lvl-4">Niveau 4 (Correct)</span>
                    <span class="cell-level lvl-3">Niveau 3</span>
                  </div>
                  <div class="matrix-row">
                    <span class="cell-name">Youssef Trabelsi</span>
                    <span class="cell-level lvl-1">Niveau 1</span>
                    <span class="cell-level lvl-1">Niveau 1</span>
                    <span class="cell-level lvl-2">Niveau 2</span>
                  </div>
                </div>
                <div class="matrix-legend">
                  <span class="leg-item"
                    ><span class="color-box lvl-1"></span> Niveau 1 (Débutant)</span
                  >
                  <span class="leg-item"
                    ><span class="color-box lvl-2"></span> Niveau 2 (Intermédiaire)</span
                  >
                  <span class="leg-item"
                    ><span class="color-box lvl-3"></span> Niveau 3 (Autonome)</span
                  >
                  <span class="leg-item"
                    ><span class="color-box lvl-4"></span> Niveau 4 (Expert/Formateur)</span
                  >
                </div>
              </div>
            </div>
          </div>
        </section>

        <!-- Role Panel: QUALITE -->
        <section v-else-if="user?.role === 'QUALITE'" class="role-section">
          <div class="stats-grid">
            <div class="stat-card">
              <span class="stat-icon">📋</span>
              <div class="stat-content">
                <span class="stat-val">{{ templates.length }}</span>
                <span class="stat-lbl">Gabarits de Questionnaire</span>
              </div>
            </div>
            <div class="stat-card">
              <span class="stat-icon">❓</span>
              <div class="stat-content">
                <span class="stat-val">{{ questions.length }}</span>
                <span class="stat-lbl">Questions enregistrées</span>
              </div>
            </div>
            <div class="stat-card">
              <span class="stat-icon">🔍</span>
              <div class="stat-content">
                <span class="stat-val">2</span>
                <span class="stat-lbl">Évaluations à valider</span>
              </div>
            </div>
          </div>

          <div class="admin-grid">
            <!-- Add Question Form -->
            <div class="panel-card">
              <div class="panel-header">
                <h3>Ajouter une Question d'Évaluation</h3>
              </div>
              <form @submit.prevent="addQuestion" class="panel-form">
                <div class="input-group">
                  <label>Énoncé de la question</label>
                  <input
                    v-model="newQuestionEnonce"
                    required
                    placeholder="Ex: Vérifier le couple de serrage"
                  />
                </div>
                <div class="input-group">
                  <label>Réponse attendue</label>
                  <input
                    v-model="newQuestionReponse"
                    required
                    placeholder="Ex: Entre 4.5 et 5.2 Nm"
                  />
                </div>
                <div class="input-group">
                  <label>Bloc d'évaluation concerné</label>
                  <select v-model="newQuestionBloc">
                    <option value="Bloc A">Bloc A (Sécurité)</option>
                    <option value="Bloc B">Bloc B (Technique)</option>
                    <option value="Bloc C">Bloc C (Qualité)</option>
                  </select>
                </div>
                <button type="submit" class="submit-btn">Ajouter à la banque</button>
              </form>
            </div>

            <!-- Templates and Questions List -->
            <div class="panel-card">
              <div class="panel-header">
                <h3>Gabarits & Banque de Questions</h3>
              </div>
              <div class="tabs-sim">
                <p><strong>Gabarits actifs (TemplateQuestionnaire) :</strong></p>
                <ul>
                  <li v-for="t in templates" :key="t.id">
                    📁 <strong>{{ t.nom }}</strong> (Ordre affichage: {{ t.ordre }}) - Créé le
                    {{ t.date }}
                  </li>
                </ul>

                <p style="margin-top: 1.5rem"><strong>Banque de questions associées :</strong></p>
                <div class="table-wrapper">
                  <table class="data-table">
                    <thead>
                      <tr>
                        <th>Bloc</th>
                        <th>Énoncé</th>
                        <th>Réponse Attendue</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr v-for="q in questions" :key="q.id">
                        <td>
                          <span class="role-badge">{{ q.bloc }}</span>
                        </td>
                        <td>{{ q.enonce }}</td>
                        <td>
                          <code>{{ q.reponse }}</code>
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          </div>
        </section>

        <!-- Role Panel: HSE -->
        <section v-else-if="user?.role === 'HSE'" class="role-section">
          <div class="stats-grid">
            <div class="stat-card">
              <span class="stat-icon">🛡️</span>
              <div class="stat-content">
                <span class="stat-val">100%</span>
                <span class="stat-lbl">Indice de sécurité Zone A</span>
              </div>
            </div>
            <div class="stat-card">
              <span class="stat-icon">🚧</span>
              <div class="stat-content">
                <span class="stat-val">1</span>
                <span class="stat-lbl">Alerte mineure (Zone B)</span>
              </div>
            </div>
            <div class="stat-card">
              <span class="stat-icon">📝</span>
              <div class="stat-content">
                <span class="stat-val">{{ hseLogs.length }}</span>
                <span class="stat-lbl">Modifications de gabarits tracées</span>
              </div>
            </div>
          </div>

          <div class="admin-grid">
            <!-- Safety status check list -->
            <div class="panel-card">
              <div class="panel-header">
                <h3>Contrôles de Sécurité par Zone Ligne</h3>
              </div>
              <div class="table-wrapper">
                <table class="data-table">
                  <thead>
                    <tr>
                      <th>Zone</th>
                      <th>Port des EPI</th>
                      <th>Sécurités Machines</th>
                      <th>Statut Global</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="check in safetyChecks" :key="check.id">
                      <td>
                        <strong>{{ check.zone }}</strong>
                      </td>
                      <td>{{ check.portEPI ? '✅ Conforme' : '❌ Non conforme' }}</td>
                      <td>{{ check.securiteMachine ? '✅ Active' : '⚠️ Anomalie' }}</td>
                      <td>
                        <span
                          :class="[
                            'status-badge',
                            check.statut === 'Sécurisé' ? 'active' : 'suspended',
                          ]"
                        >
                          {{ check.statut }}
                        </span>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>

            <!-- Modif gabarit logs -->
            <div class="panel-card">
              <div class="panel-header">
                <h3>Journal des Modifications (JournalModifGabarit)</h3>
              </div>
              <div class="table-wrapper">
                <table class="data-table">
                  <thead>
                    <tr>
                      <th>Date</th>
                      <th>Auteur</th>
                      <th>Motif de modification</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="log in hseLogs" :key="log.id">
                      <td>
                        <code>{{ log.date }}</code>
                      </td>
                      <td>{{ log.auteur }}</td>
                      <td>
                        <em>{{ log.motif }}</em>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </section>

        <!-- Role Panel: SUPERVISEUR -->
        <section v-else-if="user?.role === 'SUPERVISEUR'" class="role-section">
          <div class="stats-grid">
            <div class="stat-card">
              <span class="stat-icon">🔄</span>
              <div class="stat-content">
                <span class="stat-val">1</span>
                <span class="stat-lbl">Session d'évaluation active</span>
              </div>
            </div>
            <div class="stat-card">
              <span class="stat-icon">⚙️</span>
              <div class="stat-content">
                <span class="stat-val">3</span>
                <span class="stat-lbl">Postes de travail supervisés</span>
              </div>
            </div>
            <div class="stat-card">
              <span class="stat-icon">🚩</span>
              <div class="stat-content">
                <span class="stat-val">0</span>
                <span class="stat-lbl">Alertes de polyvalence</span>
              </div>
            </div>
          </div>

          <div class="admin-grid">
            <!-- Active Evaluation sessions -->
            <div class="panel-card">
              <div class="panel-header">
                <h3>Sessions d'Évaluation (SessionEvaluation)</h3>
              </div>
              <div class="table-wrapper">
                <table class="data-table">
                  <thead>
                    <tr>
                      <th>ID Session</th>
                      <th>Type</th>
                      <th>Date</th>
                      <th>Score Global Moyen</th>
                      <th>Statut</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="s in sessions" :key="s.id">
                      <td>
                        <code>#00{{ s.id }}</code>
                      </td>
                      <td>{{ s.type }}</td>
                      <td>{{ s.date }}</td>
                      <td>
                        <strong>{{ s.score }}%</strong>
                      </td>
                      <td>
                        <span
                          :class="[
                            'status-badge',
                            s.statut === 'En cours' ? 'active' : 'suspended',
                          ]"
                        >
                          {{ s.statut }}
                        </span>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>

            <!-- Operator Station Assignments -->
            <div class="panel-card">
              <div class="panel-header">
                <h3>Affectation Active aux Postes de Travail</h3>
              </div>
              <div class="table-wrapper">
                <table class="data-table">
                  <thead>
                    <tr>
                      <th>Poste</th>
                      <th>Opérateur Affecté</th>
                      <th>Shift</th>
                      <th>Statut Présence</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="asg in assignments" :key="asg.poste">
                      <td>
                        <strong>{{ asg.poste }}</strong>
                      </td>
                      <td>{{ asg.operateur }}</td>
                      <td>{{ asg.shift }}</td>
                      <td>
                        <span
                          :class="[
                            'status-badge',
                            asg.statut === 'Présent' ? 'active' : 'suspended',
                          ]"
                        >
                          {{ asg.statut }}
                        </span>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </section>
      </main>
    </div>
  </div>
</template>

<style scoped>
/* Dashboard Container */
.dashboard-container {
  min-height: 100vh;
  width: 100vw;
  background-color: #f1f5f9;
  position: absolute;
  top: 0;
  left: 0;
  color: #1e293b;
  font-family: Inter, sans-serif;
  overflow-x: hidden;
}

/* Overlay for Password Change */
.password-change-overlay {
  display: grid;
  place-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #1e3a8a 0%, #0f172a 100%);
  padding: 2rem;
}

.password-card {
  width: min(100%, 500px);
  background: white;
  border-radius: 16px;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.25);
  padding: 2.5rem;
}

.card-header {
  text-align: center;
  margin-bottom: 2rem;
}

.lock-icon {
  font-size: 3rem;
  display: block;
  margin-bottom: 0.5rem;
}

.card-header h2 {
  font-size: 1.5rem;
  color: #1e293b;
  font-weight: 800;
  margin: 0;
}

.desc {
  color: #64748b;
  font-size: 0.9rem;
  margin-top: 0.5rem;
}

.pwd-form {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.input-group {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
  text-align: left;
}

.input-group label {
  font-size: 0.85rem;
  font-weight: 600;
  color: #475569;
}

.input-group input {
  padding: 0.75rem 1rem;
  border: 1.5px solid #cbd5e1;
  border-radius: 8px;
  font-size: 0.95rem;
  color: #1e293b;
}

.input-group input:focus {
  outline: none;
  border-color: #2563eb;
}

.action-btn {
  padding: 0.8rem;
  background: #2563eb;
  color: white;
  border: 0;
  border-radius: 8px;
  font-weight: 700;
  cursor: pointer;
  margin-top: 0.5rem;
  transition: background 0.2s;
}

.action-btn:hover {
  background: #1d4ed8;
}

.message-box {
  padding: 0.75rem 1rem;
  border-radius: 6px;
  font-size: 0.9rem;
}

.message-box.error {
  background: #fef2f2;
  color: #b91c1c;
  border-left: 4px solid #ef4444;
}

.message-box.success {
  background: #f0fdf4;
  color: #166534;
  border-left: 4px solid #22c55e;
}

/* App Layout */
.app-layout {
  display: flex;
  min-height: 100vh;
  width: 100vw;
}

/* Sidebar */
.app-sidebar {
  width: 280px;
  background-color: #0f172a;
  color: #94a3b8;
  display: flex;
  flex-direction: column;
  border-right: 1px solid #1e293b;
  flex-shrink: 0;
}

.brand {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1.5rem;
  border-bottom: 1px solid #1e293b;
}

.logo-emoji {
  font-size: 2rem;
}

.brand-text h3 {
  margin: 0;
  color: white;
  font-size: 1.1rem;
  font-weight: 700;
}

.brand-sub {
  font-size: 0.75rem;
  color: #64748b;
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1.25rem 1.5rem;
  background: #1e293b;
  margin: 1rem;
  border-radius: 12px;
}

.avatar {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  background: #2563eb;
  color: white;
  font-weight: 800;
  display: grid;
  place-items: center;
}

.user-info {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.user-name {
  color: white;
  font-size: 0.95rem;
  font-weight: 600;
}

.user-role-pill {
  font-size: 0.75rem;
  background: rgba(37, 99, 235, 0.2);
  color: #60a5fa;
  padding: 0.15rem 0.4rem;
  border-radius: 4px;
  margin-top: 0.15rem;
}

.sidebar-nav {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  padding: 1rem;
  flex-grow: 1;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem 1rem;
  color: #94a3b8;
  text-decoration: none;
  font-weight: 600;
  font-size: 0.95rem;
  border-radius: 8px;
  transition: all 0.2s;
}

.nav-item:hover {
  background: #1e293b;
  color: white;
}

.nav-item.active {
  background: #2563eb;
  color: white;
}

.sidebar-footer {
  padding: 1.5rem;
  border-top: 1px solid #1e293b;
}

.logout-btn {
  width: 100%;
  padding: 0.75rem;
  background: transparent;
  border: 1px solid #334155;
  color: #f1f5f9;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  transition: background 0.2s;
}

.logout-btn:hover {
  background: #b91c1c;
  border-color: #b91c1c;
}

/* Main Content Area */
.app-main {
  flex-grow: 1;
  padding: 2.5rem;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.main-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #e2e8f0;
  padding-bottom: 1.5rem;
}

.header-title {
  text-align: left;
}

.header-title h1 {
  font-size: 2rem;
  font-weight: 800;
  color: #0f172a;
  margin: 0;
}

.header-title p {
  color: #64748b;
  margin: 0.25rem 0 0;
  font-size: 0.95rem;
}

.header-date {
  font-weight: 600;
  color: #475569;
}

/* Stats Cards Grid */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 1.5rem;
}

.stat-card {
  background: white;
  padding: 1.5rem;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  display: flex;
  align-items: center;
  gap: 1.25rem;
  border: 1px solid #e2e8f0;
}

.stat-icon {
  font-size: 2rem;
  background: #f8fafc;
  padding: 0.75rem;
  border-radius: 10px;
}

.stat-content {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.stat-val {
  font-size: 1.75rem;
  font-weight: 800;
  color: #0f172a;
  line-height: 1;
}

.stat-lbl {
  font-size: 0.85rem;
  color: #64748b;
  margin-top: 0.35rem;
}

/* Admin Specific / Generic grids */
.admin-grid {
  display: grid;
  grid-template-columns: 1fr 1.5fr;
  gap: 2rem;
  align-items: start;
}

@media (max-width: 1024px) {
  .admin-grid {
    grid-template-columns: 1fr;
  }
}

.panel-card {
  background: white;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  padding: 1.75rem;
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #f1f5f9;
  padding-bottom: 0.75rem;
}

.panel-header h3 {
  font-size: 1.15rem;
  font-weight: 700;
  color: #0f172a;
  margin: 0;
}

.panel-form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.form-row {
  display: flex;
  gap: 1rem;
}

.form-row .input-group {
  flex: 1;
}

textarea {
  padding: 0.75rem;
  border: 1.5px solid #cbd5e1;
  border-radius: 8px;
  font-size: 0.95rem;
  min-height: 80px;
  resize: vertical;
}

textarea:focus,
select:focus {
  outline: none;
  border-color: #2563eb;
}

select {
  padding: 0.75rem;
  border: 1.5px solid #cbd5e1;
  border-radius: 8px;
  font-size: 0.95rem;
  background: white;
}

.submit-btn {
  padding: 0.75rem;
  background: #2563eb;
  color: white;
  border: 0;
  border-radius: 8px;
  font-weight: 700;
  cursor: pointer;
  transition: background 0.2s;
}

.submit-btn:hover {
  background: #1d4ed8;
}

.submit-btn.btn-secondary {
  background: #0f172a;
}

.submit-btn.btn-secondary:hover {
  background: #1e293b;
}

.form-msg {
  font-size: 0.85rem;
  color: #16a34a;
  margin: 0;
}

/* User list panel */
.refresh-btn {
  padding: 0.4rem 0.75rem;
  font-size: 0.85rem;
  background: #f1f5f9;
  border: 1px solid #cbd5e1;
  border-radius: 6px;
  cursor: pointer;
}

.refresh-btn:hover {
  background: #e2e8f0;
}

.loading-state,
.error-state {
  padding: 2rem;
  text-align: center;
  color: #64748b;
  font-weight: 600;
}

.spinner-blue {
  display: inline-block;
  width: 16px;
  height: 16px;
  border: 2px solid rgba(37, 99, 235, 0.2);
  border-top-color: #2563eb;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

.table-wrapper {
  overflow-x: auto;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  text-align: left;
  font-size: 0.9rem;
}

.data-table th {
  background: #f8fafc;
  padding: 0.75rem 1rem;
  font-weight: 700;
  color: #475569;
  border-bottom: 1px solid #e2e8f0;
}

.data-table td {
  padding: 0.85rem 1rem;
  border-bottom: 1px solid #f1f5f9;
}

code {
  font-family: monospace;
  background: #f1f5f9;
  padding: 0.15rem 0.35rem;
  border-radius: 4px;
  color: #0f172a;
}

.role-badge {
  font-size: 0.8rem;
  background: #e0f2fe;
  color: #0369a1;
  padding: 0.15rem 0.4rem;
  border-radius: 4px;
  font-weight: 600;
}

.status-badge {
  font-size: 0.8rem;
  padding: 0.15rem 0.4rem;
  border-radius: 4px;
  font-weight: 600;
}

.status-badge.active {
  background: #dcfce7;
  color: #15803d;
}

.status-badge.suspended {
  background: #fee2e2;
  color: #b91c1c;
}

.status-btn {
  padding: 0.35rem 0.6rem;
  border: 0;
  border-radius: 4px;
  font-size: 0.8rem;
  font-weight: 600;
  cursor: pointer;
  transition: opacity 0.2s;
}

.status-btn:hover {
  opacity: 0.85;
}

.btn-suspend {
  background: #fee2e2;
  color: #b91c1c;
}

.btn-activate {
  background: #dcfce7;
  color: #15803d;
}

/* Skills Matrix Styling */
.skills-matrix-sim {
  text-align: left;
}

.matrix-grid {
  display: flex;
  flex-direction: column;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  overflow: hidden;
  margin-top: 1rem;
}

.matrix-row {
  display: grid;
  grid-template-columns: 1.5fr 1fr 1fr 1fr;
  border-bottom: 1px solid #cbd5e1;
}

.matrix-row.header-row {
  background: #f8fafc;
  font-weight: 700;
}

.matrix-row:last-child {
  border-bottom: 0;
}

.header-cell,
.cell-name,
.cell-level {
  padding: 0.75rem;
  display: flex;
  align-items: center;
  font-size: 0.85rem;
}

.cell-level {
  font-weight: 600;
}

.lvl-1 {
  background: #fee2e2;
  color: #b91c1c;
}
.lvl-2 {
  background: #fef3c7;
  color: #d97706;
}
.lvl-3 {
  background: #e0f2fe;
  color: #0369a1;
}
.lvl-4 {
  background: #dcfce7;
  color: #15803d;
}

.matrix-legend {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
  margin-top: 1rem;
  font-size: 0.8rem;
}

.leg-item {
  display: flex;
  align-items: center;
  gap: 0.35rem;
}

.color-box {
  width: 14px;
  height: 14px;
  border-radius: 3px;
  display: inline-block;
}

/* Tab Simulation in QA */
.tabs-sim {
  text-align: left;
}

.tabs-sim ul {
  padding-left: 1.2rem;
  margin: 0.5rem 0;
}

.tabs-sim li {
  margin-bottom: 0.4rem;
}

/* OPmobility-inspired dashboard theme */
.dashboard-container {
  background: #edf3f2;
  color: #254b4e;
}

.app-sidebar {
  width: 248px;
  background: #123f43;
  color: #c8d8d8;
  border-right: 0;
}

.brand {
  padding: 1.75rem 1.5rem 1.4rem;
  border-bottom-color: rgba(255, 255, 255, 0.1);
}

.opmobility-mark {
  width: 34px;
  height: 34px;
  display: grid;
  place-items: center;
  color: #123f43;
  background: #b6e675;
  border-radius: 50% 50% 50% 12%;
  font-size: 0.77rem;
  font-weight: 900;
  letter-spacing: -0.08em;
}

.brand-text h3 {
  font-size: 1.15rem;
  letter-spacing: -0.04em;
}

.brand-text h3 span { color: #b6e675; }
.brand-sub { color: #a6c0c0; }

.user-profile {
  padding: 1rem;
  margin: 1.15rem 1rem 0.5rem;
  background: rgba(255, 255, 255, 0.07);
  border-radius: 8px;
}

.avatar { background: #79c66b; }
.user-role-pill { background: transparent; color: #a6c0c0; padding: 0; }
.sidebar-nav { gap: 0.35rem; }

.nav-item {
  width: 100%;
  padding: 0.78rem 0.9rem;
  border: 0;
  border-radius: 6px;
  background: transparent;
  color: #c8d8d8;
  font-size: 0.82rem;
  font-weight: 500;
  text-align: left;
  cursor: pointer;
}

.nav-item:hover { background: rgba(255, 255, 255, 0.08); }
.nav-item.active {
  background: #255b5d;
  color: #dff2bf;
  box-shadow: inset 3px 0 #b6e675;
}

.sidebar-footer { padding: 1rem; border-top-color: rgba(255, 255, 255, 0.1); }
.logout-btn { border: 0; border-radius: 6px; color: #c8d8d8; font-weight: 500; }
.logout-btn:hover { background: rgba(255, 255, 255, 0.1); }

.app-main { padding: 1.5rem 2rem 2.5rem; gap: 1.5rem; }
.main-header { padding: 0.25rem 0 0.75rem; border-bottom: 0; }
.header-title h1 { color: #254b4e; font-size: 1.65rem; }
.header-date {
  padding: 0.7rem 0.9rem;
  border-radius: 6px;
  background: #fff;
  color: #547174;
  font-size: 0.8rem;
  box-shadow: 0 3px 12px rgba(34, 70, 72, 0.06);
}

.stats-grid { gap: 1rem; }
.stat-card,
.panel-card {
  border: 1px solid #e3eeee;
  border-radius: 7px;
  box-shadow: 0 4px 14px rgba(34, 70, 72, 0.06);
}

.stat-card { padding: 1.15rem; }
.panel-card { padding: 1.35rem; }
.panel-header h3 { color: #254b4e; }
.stat-icon { background: #e8f6f4; border-radius: 7px; font-size: 1.5rem; }

.input-group input,
select,
textarea {
  border: 1px solid #d8e5e4;
  border-radius: 6px;
}

.input-group input:focus,
select:focus,
textarea:focus {
  border-color: #58a88c;
  box-shadow: 0 0 0 3px rgba(88, 168, 140, 0.14);
}

.submit-btn,
.action-btn { background: #2c766f; border-radius: 6px; }
.submit-btn:hover,
.action-btn:hover { background: #205d58; }
.submit-btn.btn-secondary { background: #56777a; }
.submit-btn.btn-secondary:hover { background: #3d6265; }

.data-table th { background: #f2f8f7; color: #416568; }
.data-table td { border-bottom-color: #edf3f2; }
.role-badge { background: #e8f6f4; color: #28746d; }

.structure-tree {
  padding: 0.9rem 0;
  border-bottom: 1px solid #e7efee;
}

.structure-tree:last-child { border-bottom: 0; }
.tree-project,
.tree-zone-title,
.tree-zone li {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 1rem;
}

.tree-project { color: #205d58; }
.tree-zone { margin: 0.8rem 0 0 1.2rem; }
.tree-zone ul { margin: 0.55rem 0 0; padding-left: 1.25rem; }
.tree-zone li { padding: 0.35rem 0; color: #547174; }

.creation-meta {
  color: #789193;
  font-size: 0.75rem;
  font-weight: 500;
  white-space: nowrap;
}

@media (max-width: 760px) {
  .app-sidebar { width: 76px; }
  .brand { padding: 1.25rem; }
  .brand-text, .user-info, .nav-item span:last-child, .logout-btn span:last-child { display: none; }
  .user-profile { margin: 1rem 0.75rem; justify-content: center; }
  .nav-item { justify-content: center; padding: 0.85rem; }
  .app-main { padding: 1rem; }
  .main-header { align-items: flex-start; gap: 0.75rem; flex-direction: column; }
  .tree-project, .tree-zone-title, .tree-zone li { align-items: flex-start; flex-direction: column; gap: 0.2rem; }
  .creation-meta { white-space: normal; }
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
