<script setup>
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import {
  fetchTeamOperators,
  fetchMyTeams,
  createOperator
} from '@/features/dashboard/services/operateurService'

defineProps({
  activeSection: {
    type: String,
    default: 'dashboard'
  }
})

const authStore = useAuthStore()

// Real operator management state
const operators = ref([])
const teams = ref([])
const opLoading = ref(false)
const opError = ref('')

const newOpMatricule = ref('')
const newOpNom = ref('')
const newOpDateEmbauche = ref(new Date().toISOString().split('T')[0])
const newOpStatut = ref('Actif')
const newOpRework = ref(false)
const newOpEquipeId = ref('')
const createLoading = ref(false)
const createMsg = ref('')

// Simulated integration follow-up & update requests
const integrationLogs = ref([
  { id: 1, jour: 1, cadence: 80, defauts: 2, remarques: 'Bon démarrage, opérateur motivé.' },
  { id: 2, jour: 2, cadence: 95, defauts: 1, remarques: 'Cadence atteinte avec une bonne qualité.' }
])
const newDay = ref(3)
const newCadence = ref(100)
const newDefauts = ref(0)
const newRemarques = ref('')

const teamRequests = ref([
  { id: 1, type: 'Ajout Opérateur', date: '2026-07-10', statut: 'Validé' },
  { id: 2, type: 'Modification Shift', date: '2026-07-14', statut: 'En attente' }
])
const requestType = ref('Ajout Opérateur')
const requestMsg = ref('')

async function loadData() {
  opLoading.value = true
  opError.value = ''
  try {
    const [opsData, teamsData] = await Promise.all([
      fetchTeamOperators(authStore.token),
      fetchMyTeams(authStore.token)
    ])
    operators.value = opsData
    teams.value = teamsData
    if (teamsData.length > 0) {
      newOpEquipeId.value = teamsData[0].idEquipe
    }
  } catch (err) {
    opError.value = err.message
  } finally {
    opLoading.value = false
  }
}

async function handleCreateOperator() {
  createMsg.value = ''
  createLoading.value = true
  try {
    await createOperator(authStore.token, {
      matricule: newOpMatricule.value,
      nom: newOpNom.value,
      dateEmbauche: newOpDateEmbauche.value,
      statut: newOpStatut.value,
      formationRework: newOpRework.value,
      equipeId: newOpEquipeId.value ? Number(newOpEquipeId.value) : null
    })
    newOpMatricule.value = ''
    newOpNom.value = ''
    newOpRework.value = false
    createMsg.value = 'Opérateur créé avec succès!'
    await loadData()
  } catch (err) {
    createMsg.value = `Erreur: ${err.message}`
  } finally {
    createLoading.value = false
  }
}

function addIntegrationLog() {
  integrationLogs.value.push({
    id: Date.now(),
    jour: newDay.value,
    cadence: newCadence.value,
    defauts: newDefauts.value,
    remarques: newRemarques.value || 'N/A'
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
    statut: 'En attente'
  })
  requestMsg.value = ''
}

onMounted(loadData)
</script>

<template>
  <section class="role-section">
    <!-- SUB-VIEW: main dashboard overview -->
    <div v-if="activeSection === 'dashboard'" style="display: flex; flex-direction: column; gap: 1.5rem;">
      <!-- Overview stats -->
      <div class="stats-grid">
        <div class="stat-card">
          <span class="stat-icon">👷</span>
          <div class="stat-content">
            <span class="stat-val">{{ operators.length }}</span>
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

      <!-- Mon Equipe Table -->
      <div class="panel-card" style="width: 100%;">
        <div class="panel-header">
          <h3>Mon Équipe (Opérateurs)</h3>
          <button @click="loadData" class="refresh-btn">🔄 Actualiser</button>
        </div>
        <div v-if="opLoading" class="loading-state">
          <span class="spinner-blue"></span> Chargement des opérateurs...
        </div>
        <div v-else-if="opError" class="error-state">⚠️ {{ opError }}</div>
        <div v-else class="table-wrapper">
          <table class="data-table">
            <thead>
              <tr>
                <th>Matricule</th>
                <th>Nom</th>
                <th>Embauche</th>
                <th>Formation Rework</th>
                <th>Affectation Poste</th>
                <th>Statut</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="op in operators" :key="op.matricule">
                <td><code>{{ op.matricule }}</code></td>
                <td><strong>{{ op.nom }}</strong></td>
                <td>{{ op.dateEmbauche }}</td>
                <td>{{ op.formationRework ? '✅ Oui' : '❌ Non' }}</td>
                <td>
                  <span v-if="op.posteAffecte" class="role-badge">
                    {{ op.posteAffecte.nom }}
                  </span>
                  <span v-else class="empty-hint">— Aucun —</span>
                </td>
                <td>
                  <span :class="['status-badge', op.statut === 'Actif' ? 'active' : 'suspended']">
                    {{ op.statut }}
                  </span>
                </td>
              </tr>
              <tr v-if="operators.length === 0">
                <td colspan="6" style="text-align: center; color: #547174;">
                  Aucun opérateur trouvé dans votre équipe.
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Integrations & Requests History List (Two columns side-by-side) -->
      <div class="admin-grid">
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
                  <td><strong>{{ log.cadence }}%</strong></td>
                  <td>{{ log.defauts }}</td>
                  <td>{{ log.remarques }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <div class="panel-card">
          <div class="panel-header">
            <h3>Historique des Demandes</h3>
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
                  <td><strong>{{ req.type }}</strong></td>
                  <td>{{ req.date }}</td>
                  <td>
                    <span :class="['status-badge', req.statut === 'Validé' ? 'active' : 'suspended']">
                      {{ req.statut }}
                    </span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>

    <!-- SUB-VIEW: Create Operator Form -->
    <div v-else-if="activeSection === 'creer-operateur'" style="max-width: 600px; margin: 0 auto; width: 100%;">
      <div class="panel-card">
        <div class="panel-header">
          <h3>Créer un Opérateur</h3>
        </div>
        <form @submit.prevent="handleCreateOperator" class="panel-form">
          <div class="input-group">
            <label>Matricule</label>
            <input v-model="newOpMatricule" required placeholder="Ex: OP123" />
          </div>
          <div class="input-group">
            <label>Nom complet</label>
            <input v-model="newOpNom" required placeholder="Ex: Jean Dupont" />
          </div>
          <div class="input-group">
            <label>Date d'embauche</label>
            <input v-model="newOpDateEmbauche" type="date" required />
          </div>
          <div class="input-group">
            <label>Équipe</label>
            <select v-model="newOpEquipeId" required>
              <option v-for="team in teams" :key="team.idEquipe" :value="team.idEquipe">
                {{ team.nom }}
              </option>
            </select>
          </div>
          <div class="input-group">
            <label>Statut initial</label>
            <select v-model="newOpStatut">
              <option value="Actif">Actif</option>
              <option value="En Formation">En Formation</option>
            </select>
          </div>
          <div class="input-group" style="flex-direction: row; gap: 0.5rem; align-items: center;">
            <input v-model="newOpRework" type="checkbox" id="reworkCheck" />
            <label for="reworkCheck">Qualifié Formation Rework</label>
          </div>
          <button type="submit" :disabled="createLoading" class="submit-btn">
            {{ createLoading ? 'Création...' : "Créer l'opérateur" }}
          </button>
          <p v-if="createMsg" class="form-msg">{{ createMsg }}</p>
        </form>
      </div>
    </div>

    <!-- SUB-VIEW: Saisir Suivi -->
    <div v-else-if="activeSection === 'saisir-suivi'" style="max-width: 600px; margin: 0 auto; width: 100%;">
      <div class="panel-card">
        <div class="panel-header">
          <h3>Saisir un Suivi d'Intégration Journalier</h3>
        </div>
        <form @submit.prevent="addIntegrationLog" class="panel-form">
          <div class="form-row">
            <div class="input-group" style="flex: 1;">
              <label>Jour d'Intégration</label>
              <input v-model="newDay" type="number" required />
            </div>
            <div class="input-group" style="flex: 1;">
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
            <textarea v-model="newRemarques" placeholder="Observations et points de blocage..."></textarea>
          </div>
          <button type="submit" class="submit-btn">Enregistrer le suivi</button>
        </form>
      </div>
    </div>

    <!-- SUB-VIEW: Demande MAJ -->
    <div v-else-if="activeSection === 'demande-maj'" style="max-width: 600px; margin: 0 auto; width: 100%;">
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
            <textarea v-model="requestMsg" placeholder="Saisir la raison de la demande..." required></textarea>
          </div>
          <button type="submit" class="submit-btn btn-secondary">Envoyer la demande</button>
        </form>
      </div>
    </div>
  </section>
</template>
