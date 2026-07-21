<script setup>
import { ref, onMounted, computed } from 'vue'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()

// Operator selection
const operators = ref([])
const selectedOperator = ref('')
const selectedPoste = ref('')
const postes = ref([])
const operatorLoading = ref(false)
const operatorError = ref('')

// Add training form
const addTrainingLoading = ref(false)
const addTrainingMsg = ref('')
const templateExists = ref(false)
const cadenceObjectif = ref('')
const qualiteObjectif = ref('')
const saveAsTemplate = ref(false)

// Daily journal tracking
const trainings = ref([])
const selectedTraining = ref('')
const currentDay = ref(1)
const journalData = ref({
  cadenceRealisee: '',
  nbDefauts: '',
  remarques: '',
})
const saveJournalLoading = ref(false)
const saveJournalMsg = ref('')

// Stats
const teamStats = ref({
  totalTrainings: 0,
  enFormation: 0,
  validees: 0,
  completionRate: 0,
})

async function loadOperators() {
  operatorLoading.value = true
  operatorError.value = ''
  try {
    const response = await fetch('/api/operateurs/mon-equipe', {
      headers: { Authorization: `Bearer ${authStore.token}` },
    })
    if (!response.ok) throw new Error('Impossible de charger les opérateurs')
    operators.value = await response.json()
  } catch (err) {
    operatorError.value = err.message
  } finally {
    operatorLoading.value = false
  }
}

async function loadPostes() {
  try {
    const response = await fetch('/api/postes', {
      headers: { Authorization: `Bearer ${authStore.token}` },
    })
    if (!response.ok) throw new Error('Impossible de charger les postes')
    postes.value = await response.json()
  } catch (err) {
    operatorError.value = err.message
  }
}

async function loadTeamTrainings() {
  try {
    const response = await fetch('/api/formations/mon-equipe', {
      headers: { Authorization: `Bearer ${authStore.token}` },
    })
    if (!response.ok) throw new Error('Impossible de charger les formations')
    const data = await response.json()
    trainings.value = data

    // Calculate stats
    teamStats.value = {
      totalTrainings: data.length,
      enFormation: data.filter((t) => t.statut === 'EN_FORMATION').length,
      validees: data.filter((t) => t.statut === 'VALIDEE').length,
      completionRate:
        data.length > 0
          ? Math.round((data.filter((t) => t.statut === 'VALIDEE').length / data.length) * 100)
          : 0,
    }
  } catch (err) {
    operatorError.value = err.message
  }
}

async function handleAddTraining() {
  if (!selectedOperator.value || !selectedPoste.value) {
    addTrainingMsg.value = '⚠️ Sélectionnez un opérateur et un poste'
    return
  }

  addTrainingLoading.value = true
  addTrainingMsg.value = ''
  try {
    const response = await fetch('/api/formations/initialize', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${authStore.token}`,
      },
      body: JSON.stringify({
        operateurMatricule: selectedOperator.value,
        posteId: parseInt(selectedPoste.value),
        projetId: 1, // TODO: Get from context
      }),
    })
    if (!response.ok) {
      const data = await response.json().catch(() => ({}))
      throw new Error(data.message || "Erreur lors de l'initialisation")
    }
    addTrainingMsg.value = '✅ Formation ajoutée avec succès!'
    selectedOperator.value = ''
    selectedPoste.value = ''
    await loadTeamTrainings()
  } catch (err) {
    addTrainingMsg.value = `❌ ${err.message}`
  } finally {
    addTrainingLoading.value = false
  }
}

async function handleSaveJournal() {
  if (!selectedTraining.value || !journalData.value.cadenceRealisee) {
    saveJournalMsg.value = '⚠️ Complétez le formulaire'
    return
  }

  saveJournalLoading.value = true
  saveJournalMsg.value = ''
  try {
    const response = await fetch(`/api/formations/${selectedTraining.value}/journal`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${authStore.token}`,
      },
      body: JSON.stringify({
        jour: currentDay.value,
        cadenceRealisee: parseInt(journalData.value.cadenceRealisee),
        nbDefauts: parseInt(journalData.value.nbDefauts) || 0,
        remarques: journalData.value.remarques,
      }),
    })
    if (!response.ok) {
      const data = await response.json().catch(() => ({}))
      throw new Error(data.message || 'Erreur lors de la sauvegarde')
    }
    saveJournalMsg.value = '✅ Jour enregistré!'
    journalData.value = { cadenceRealisee: '', nbDefauts: '', remarques: '' }
    currentDay.value = Math.min(12, currentDay.value + 1)
  } catch (err) {
    saveJournalMsg.value = `❌ ${err.message}`
  } finally {
    saveJournalLoading.value = false
  }
}

onMounted(() => {
  loadOperators()
  loadPostes()
  loadTeamTrainings()
})
</script>

<template>
  <div class="chef-formation-panel">
    <!-- Stats Bar -->
    <div class="stats-bar">
      <div class="stat-card">
        <div class="stat-value">{{ teamStats.totalTrainings }}</div>
        <div class="stat-label">Formations totales</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">{{ teamStats.enFormation }}</div>
        <div class="stat-label">En cours</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">{{ teamStats.validees }}</div>
        <div class="stat-label">Validées</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">{{ teamStats.completionRate }}%</div>
        <div class="stat-label">Taux de complétude</div>
      </div>
    </div>

    <!-- Main Layout: Left (Forms) + Right (Trainings) -->
    <div class="formation-container">
      <!-- LEFT: Add Training & Daily Journal -->
      <div class="left-panel">
        <!-- Add Training Section -->
        <div class="form-section">
          <h3>📚 Ajouter une Formation</h3>

          <div v-if="operatorLoading" class="loading">Chargement des données...</div>
          <div v-else-if="operatorError" class="error">⚠️ {{ operatorError }}</div>
          <form v-else @submit.prevent="handleAddTraining" class="simple-form">
            <div class="form-group">
              <label>Opérateur</label>
              <select v-model="selectedOperator" required>
                <option value="">-- Sélectionnez --</option>
                <option v-for="op in operators" :key="op.matricule" :value="op.matricule">
                  {{ op.nom }} ({{ op.matricule }})
                </option>
              </select>
            </div>

            <div class="form-group">
              <label>Poste à apprendre</label>
              <select v-model="selectedPoste" required>
                <option value="">-- Sélectionnez --</option>
                <option v-for="p in postes" :key="p.idPoste" :value="p.idPoste">
                  {{ p.nom }}
                </option>
              </select>
            </div>

            <button type="submit" :disabled="addTrainingLoading" class="btn-primary">
              {{ addTrainingLoading ? 'Ajout...' : 'Ajouter Formation' }}
            </button>
            <p
              v-if="addTrainingMsg"
              :class="addTrainingMsg.includes('✅') ? 'msg-success' : 'msg-error'"
            >
              {{ addTrainingMsg }}
            </p>
          </form>
        </div>

        <!-- Daily Journal Section -->
        <div class="form-section">
          <h3>📝 Suivi Quotidien</h3>

          <form @submit.prevent="handleSaveJournal" class="simple-form">
            <div class="form-group">
              <label>Formation à suivre</label>
              <select v-model="selectedTraining">
                <option value="">-- Sélectionnez --</option>
                <option v-for="t in trainings" :key="t.idAffectation" :value="t.idAffectation">
                  {{ t.operateurNom }} - {{ t.posteNom }} (Jour {{ t.dernierJourSaisi || 0 }}/12)
                </option>
              </select>
            </div>

            <div class="form-group">
              <label>Jour de formation (1-12)</label>
              <input v-model.number="currentDay" type="number" min="1" max="12" />
            </div>

            <div class="form-group">
              <label>Cadence réalisée</label>
              <input
                v-model.number="journalData.cadenceRealisee"
                type="number"
                placeholder="Ex: 85"
                required
              />
            </div>

            <div class="form-group">
              <label>Nombre de défauts</label>
              <input
                v-model.number="journalData.nbDefauts"
                type="number"
                placeholder="Ex: 2"
                min="0"
              />
            </div>

            <div class="form-group">
              <label>Remarques</label>
              <textarea
                v-model="journalData.remarques"
                placeholder="Observations du jour..."
                rows="3"
              ></textarea>
            </div>

            <button type="submit" :disabled="saveJournalLoading" class="btn-primary">
              {{ saveJournalLoading ? 'Enregistrement...' : 'Enregistrer Jour' }}
            </button>
            <p
              v-if="saveJournalMsg"
              :class="saveJournalMsg.includes('✅') ? 'msg-success' : 'msg-error'"
            >
              {{ saveJournalMsg }}
            </p>
          </form>
        </div>
      </div>

      <!-- RIGHT: Trainings List -->
      <div class="right-panel">
        <h3>📋 Formations de mon équipe</h3>
        <div v-if="trainings.length === 0" class="empty-state">Aucune formation pour le moment</div>
        <div v-else class="trainings-list">
          <div v-for="training in trainings" :key="training.idAffectation" class="training-card">
            <div class="training-header">
              <strong>{{ training.operateurNom }}</strong>
              <span :class="['status-badge', training.statut.toLowerCase()]">
                {{ training.statut }}
              </span>
            </div>
            <div class="training-details">
              <p><strong>Poste:</strong> {{ training.posteNom }}</p>
              <p><strong>Projet:</strong> {{ training.projetNom }}</p>
              <p><strong>Progression:</strong> Jour {{ training.dernierJourSaisi || 0 }}/12</p>
              <div class="progress-bar">
                <div
                  class="progress-fill"
                  :style="{ width: ((training.dernierJourSaisi || 0) / 12) * 100 + '%' }"
                ></div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.chef-formation-panel {
  padding: 1.5rem;
  background: #ffffff;
  border-radius: 8px;
}

/* Stats Bar */
.stats-bar {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 1rem;
  margin-bottom: 2rem;
}

.stat-card {
  background: linear-gradient(135deg, #123f43 0%, #0f172a 100%);
  color: white;
  padding: 1.5rem;
  border-radius: 8px;
  text-align: center;
}

.stat-value {
  font-size: 2rem;
  font-weight: 700;
  margin-bottom: 0.5rem;
}

.stat-label {
  font-size: 0.85rem;
  opacity: 0.9;
}

/* Main Container */
.formation-container {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 2rem;
}

.left-panel {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.right-panel {
  background: #f8fafc;
  padding: 1.5rem;
  border-radius: 8px;
}

/* Form Sections */
.form-section {
  background: #f8fafc;
  padding: 1.5rem;
  border-radius: 8px;
  border-left: 4px solid #123f43;
}

.form-section h3 {
  margin: 0 0 1rem 0;
  color: #123f43;
  font-size: 1rem;
}

/* Simple Form */
.simple-form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}

.form-group label {
  font-weight: 600;
  color: #254b4e;
  font-size: 0.9rem;
}

.form-group input,
.form-group select,
.form-group textarea {
  padding: 0.6rem;
  border: 1px solid #d1d5db;
  border-radius: 4px;
  font-family: inherit;
  font-size: 0.95rem;
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #123f43;
  box-shadow: 0 0 0 3px rgba(18, 63, 67, 0.1);
}

/* Buttons */
.btn-primary {
  background: #123f43;
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 4px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s;
}

.btn-primary:hover:not(:disabled) {
  background: #0f2f35;
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* Messages */
.msg-success {
  color: #16a34a;
  font-size: 0.9rem;
  margin: 0;
}

.msg-error {
  color: #dc2626;
  font-size: 0.9rem;
  margin: 0;
}

.error {
  background: #fee2e2;
  color: #991b1b;
  padding: 0.75rem;
  border-radius: 4px;
}

.loading {
  color: #666;
  text-align: center;
  padding: 1rem;
}

/* Trainings List */
.trainings-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.training-card {
  background: white;
  padding: 1rem;
  border-radius: 6px;
  border: 1px solid #e5e7eb;
}

.training-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.75rem;
  padding-bottom: 0.75rem;
  border-bottom: 1px solid #e5e7eb;
}

.status-badge {
  font-size: 0.75rem;
  padding: 0.25rem 0.6rem;
  border-radius: 12px;
  font-weight: 600;
}

.status-badge.validee {
  background: #dcfce7;
  color: #166534;
}

.status-badge.en_formation {
  background: #fef3c7;
  color: #92400e;
}

.training-details {
  font-size: 0.85rem;
  color: #547174;
}

.training-details p {
  margin: 0.25rem 0;
}

/* Progress Bar */
.progress-bar {
  background: #e5e7eb;
  height: 6px;
  border-radius: 3px;
  overflow: hidden;
  margin-top: 0.5rem;
}

.progress-fill {
  background: linear-gradient(90deg, #123f43, #0f2f35);
  height: 100%;
  transition: width 0.3s ease;
}

.empty-state {
  text-align: center;
  color: #999;
  padding: 2rem 1rem;
  background: white;
  border-radius: 6px;
  border: 1px dashed #e5e7eb;
}

/* Responsive */
@media (max-width: 1024px) {
  .formation-container {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .stats-bar {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
