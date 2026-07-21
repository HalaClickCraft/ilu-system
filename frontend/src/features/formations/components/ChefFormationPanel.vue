<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { fetchStructure } from '@/features/structure/services/structureService'
import Chart from 'chart.js/auto'

const authStore = useAuthStore()

// Operator selection
const operators = ref([])
const selectedOperator = ref('')
const selectedPosteId = ref('')
const structure = ref({ projects: [] })
const operatorLoading = ref(false)
const operatorError = ref('')

// Add training form
const addTrainingLoading = ref(false)
const addTrainingMsg = ref('')
const qualiteObjectifInput = ref('')

// Flatten postes out of the project/zone structure so we know each
// poste's projetId (needed to create the training) and cadenceObjectif
// (needed to display the default target).
const postesFlat = computed(() => {
  const list = []
  for (const projet of structure.value.projects || []) {
    for (const zone of projet.zones || []) {
      for (const poste of zone.postes || []) {
        list.push({
          id: poste.idPoste,
          nom: `${projet.nom} — ${zone.nom} — ${poste.nom}`,
          projetId: projet.idProjet,
          cadenceObjectif: poste.cadenceObjectif,
        })
      }
    }
  }
  return list
})

const selectedPoste = computed(() =>
  postesFlat.value.find((p) => p.id === Number(selectedPosteId.value)),
)

// Daily journal tracking (selected training)
const trainings = ref([])
const selectedTrainingId = ref('')
const selectedTraining = ref(null)
const journalEntries = ref({})
const journalLoading = ref(false)
const journalError = ref('')
const saveJournalLoading = ref(false)

let chartInstance = null

// Stats
const teamStats = ref({
  totalTrainings: 0,
  enFormation: 0,
  validees: 0,
  completionRate: 0,
})

const journalStats = computed(() => {
  let totalDefauts = 0
  let totalCadence = 0
  let count = 0
  for (let i = 1; i <= 12; i++) {
    const entry = journalEntries.value[i]
    if (entry && entry.cadenceRealisee > 0) {
      totalCadence += entry.cadenceRealisee
      count++
    }
    totalDefauts += entry?.nbDefauts || 0
  }
  return {
    averageCadence: count > 0 ? Math.round((totalCadence / count) * 100) / 100 : 0,
    avgDefauts: Math.round((totalDefauts / 12) * 10000) / 10000,
    totalDefauts,
    defautAlert: totalDefauts >= 7,
  }
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

async function loadStructure() {
  try {
    structure.value = await fetchStructure(authStore.token)
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
        posteId: selectedPoste.value.id,
        projetId: selectedPoste.value.projetId,
        qualiteObjectif:
          qualiteObjectifInput.value ||
          'nbre de défaut < 7 sur une période de 12 jours (équivalent de 10000 ppm sur les 12 jours)',
      }),
    })
    if (!response.ok) {
      const data = await response.json().catch(() => ({}))
      throw new Error(data.message || "Erreur lors de l'initialisation")
    }
    addTrainingMsg.value = '✅ Formation ajoutée avec succès!'
    selectedOperator.value = ''
    selectedPosteId.value = ''
    qualiteObjectifInput.value = ''
    await loadTeamTrainings()
  } catch (err) {
    addTrainingMsg.value = `❌ ${err.message}`
  } finally {
    addTrainingLoading.value = false
  }
}

function initializeJournal() {
  journalEntries.value = {}
  for (let i = 1; i <= 12; i++) {
    journalEntries.value[i] = { cadenceRealisee: 0, nbDefauts: 0, remarques: '' }
  }
}

async function loadJournal(training) {
  journalLoading.value = true
  journalError.value = ''
  selectedTraining.value = training
  try {
    const response = await fetch(`/api/formations/${training.idAffectation}/journal`, {
      headers: { Authorization: `Bearer ${authStore.token}` },
    })
    if (!response.ok) throw new Error('Impossible de charger le suivi journalier')
    const data = await response.json()

    initializeJournal()
    data.forEach((entry) => {
      journalEntries.value[entry.jour] = {
        cadenceRealisee: entry.cadenceRealisee || 0,
        nbDefauts: entry.nbDefauts || 0,
        remarques: entry.remarques || '',
      }
    })

    await new Promise((resolve) => setTimeout(resolve, 50))
    drawChart()
  } catch (err) {
    journalError.value = err.message
  } finally {
    journalLoading.value = false
  }
}

function drawChart() {
  const ctx = document.getElementById('chefTrainingChart')
  if (!ctx || !selectedTraining.value) return

  const objectif = selectedTraining.value.cadenceObjectif || 40
  const cadenceData = []
  for (let i = 1; i <= 12; i++) {
    cadenceData.push(journalEntries.value[i]?.cadenceRealisee || 0)
  }

  if (chartInstance) chartInstance.destroy()

  chartInstance = new Chart(ctx, {
    type: 'line',
    data: {
      labels: Array.from({ length: 12 }, (_, i) => `J${i + 1}`),
      datasets: [
        {
          label: 'Cadence objectif du poste',
          data: Array(12).fill(objectif),
          borderColor: '#16a34a',
          borderWidth: 2,
          pointRadius: 0,
          fill: false,
          tension: 0,
        },
        {
          label: 'Cadence réalisée',
          data: cadenceData,
          borderColor: '#2563eb',
          backgroundColor: 'rgba(37, 99, 235, 0.08)',
          borderWidth: 2,
          pointStyle: 'crossRot',
          pointRadius: 6,
          pointBorderWidth: 2,
          pointBackgroundColor: '#2563eb',
          fill: false,
          tension: 0,
        },
      ],
    },
    options: {
      responsive: true,
      plugins: { legend: { display: true, position: 'top' } },
      scales: { y: { beginAtZero: true, max: Math.max(objectif * 1.5, 60) } },
    },
  })
}

watch(
  journalEntries,
  () => {
    if (chartInstance && selectedTraining.value) drawChart()
  },
  { deep: true },
)

watch(selectedTrainingId, (newId) => {
  if (!newId) {
    selectedTraining.value = null
    return
  }
  const training = trainings.value.find((t) => t.idAffectation === Number(newId))
  if (training) loadJournal(training)
})

async function saveJournal() {
  if (!selectedTraining.value) return
  saveJournalLoading.value = true
  journalError.value = ''
  try {
    for (let day = 1; day <= 12; day++) {
      const entry = journalEntries.value[day]
      if (entry.cadenceRealisee > 0 || entry.nbDefauts > 0 || entry.remarques) {
        const response = await fetch(
          `/api/formations/${selectedTraining.value.idAffectation}/journal`,
          {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json',
              Authorization: `Bearer ${authStore.token}`,
            },
            body: JSON.stringify({
              jour: day,
              cadenceRealisee: entry.cadenceRealisee,
              nbDefauts: entry.nbDefauts,
              remarques: entry.remarques,
            }),
          },
        )
        if (!response.ok) throw new Error(`Erreur jour ${day}`)
      }
    }
    journalError.value = '✅ Journal sauvegardé avec succès!'
    await loadTeamTrainings()
  } catch (err) {
    journalError.value = `❌ ${err.message}`
  } finally {
    saveJournalLoading.value = false
  }
}

onMounted(() => {
  loadOperators()
  loadStructure()
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

    <!-- Add Training Section -->
    <div class="form-section">
      <h3>📚 Ajouter une Formation</h3>

      <div v-if="operatorLoading" class="loading">Chargement des données...</div>
      <div v-else-if="operatorError" class="error">⚠️ {{ operatorError }}</div>
      <form v-else @submit.prevent="handleAddTraining" class="simple-form add-training-form">
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
          <select v-model="selectedPosteId" required>
            <option value="">-- Sélectionnez --</option>
            <option v-for="p in postesFlat" :key="p.id" :value="p.id">
              {{ p.nom }}
            </option>
          </select>
        </div>

        <div class="form-group">
          <label>Cadence objectif du poste</label>
          <input
            type="text"
            :value="selectedPoste ? `${selectedPoste.cadenceObjectif} pièces/jour` : '—'"
            disabled
          />
          <small class="field-hint">Valeur par défaut du poste, non modifiable ici.</small>
        </div>

        <div class="form-group">
          <label>Objectif qualité</label>
          <input
            v-model="qualiteObjectifInput"
            type="text"
            placeholder="Ex: nbre de défaut < 7 sur une période de 12 jours"
          />
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

    <!-- Formation à suivre -->
    <div class="form-section">
      <h3>📝 Suivi Quotidien</h3>

      <div class="form-group">
        <label>Formation à suivre</label>
        <select v-model="selectedTrainingId">
          <option value="">-- Sélectionnez --</option>
          <option v-for="t in trainings" :key="t.idAffectation" :value="t.idAffectation">
            {{ t.operateurNom }} — {{ t.posteNom }} (Jour {{ t.dernierJourSaisi || 0 }}/12)
          </option>
        </select>
        <small v-if="trainings.length === 0" class="field-hint">
          Aucune formation trouvée. Si vous venez d'être assigné comme Chef d'Équipe, vérifiez
          qu'un Admin/RH vous a bien ajouté comme membre du projet dans l'onglet Structure — sans
          cela cette liste reste vide.
        </small>
      </div>

      <div v-if="journalLoading" class="loading">Chargement du suivi...</div>

      <template v-else-if="selectedTraining">
        <!-- Chart -->
        <div class="chart-section">
          <canvas id="chefTrainingChart"></canvas>
        </div>

        <!-- Pivoted 12-day table -->
        <div class="table-section">
          <table class="training-table pivoted">
            <thead>
              <tr>
                <th class="row-label"></th>
                <th v-for="day in 12" :key="'h' + day">J{{ day }}</th>
                <th class="summary-col">Moyenne</th>
                <th class="summary-col">Total</th>
              </tr>
            </thead>
            <tbody>
              <tr class="objectif-row">
                <td class="row-label">Cadence objectif du poste</td>
                <td v-for="day in 12" :key="'co' + day">
                  {{ selectedTraining.cadenceObjectif }}
                </td>
                <td class="summary-col">{{ selectedTraining.cadenceObjectif }}</td>
                <td class="summary-col">--</td>
              </tr>

              <tr>
                <td class="row-label">Cadence réalisée</td>
                <td v-for="day in 12" :key="'cr' + day" class="input-cell">
                  <input
                    v-model.number="journalEntries[day].cadenceRealisee"
                    type="number"
                    min="0"
                    class="cell-input"
                  />
                </td>
                <td class="summary-col">{{ journalStats.averageCadence }}</td>
                <td class="summary-col">--</td>
              </tr>

              <tr>
                <td class="row-label">Nbr de défauts</td>
                <td v-for="day in 12" :key="'nd' + day" class="input-cell">
                  <input
                    v-model.number="journalEntries[day].nbDefauts"
                    type="number"
                    min="0"
                    class="cell-input"
                  />
                </td>
                <td class="summary-col">{{ journalStats.avgDefauts }}</td>
                <td class="summary-col" :class="{ 'alert-defauts': journalStats.defautAlert }">
                  {{ journalStats.totalDefauts }}
                </td>
              </tr>

              <tr class="qualite-row">
                <td class="row-label">Objectif qualité</td>
                <td colspan="14">
                  {{
                    selectedTraining.qualiteObjectif ||
                    'nbre de défaut < 7 sur une période de 12 jours (équivalent de 10000 ppm sur les 12 jours)'
                  }}
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Remarks -->
        <div class="remarks-section">
          <h4>Remarques et observations</h4>
          <div class="remarks-grid">
            <div v-for="day in 12" :key="'rem' + day" class="remark-item">
              <label>J{{ day }}</label>
              <textarea v-model="journalEntries[day].remarques" rows="2"></textarea>
            </div>
          </div>
        </div>

        <button @click="saveJournal" :disabled="saveJournalLoading" class="btn-primary">
          {{ saveJournalLoading ? 'Enregistrement...' : '💾 Enregistrer le Journal' }}
        </button>
        <p v-if="journalError" :class="journalError.includes('✅') ? 'msg-success' : 'msg-error'">
          {{ journalError }}
        </p>
      </template>
    </div>

    <!-- Trainings List -->
    <div class="form-section">
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
</template>

<style scoped>
.chef-formation-panel {
  padding: 1.5rem;
  background: #ffffff;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

/* Stats Bar */
.stats-bar {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 1rem;
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

.form-section h4 {
  margin: 0 0 0.75rem 0;
  color: #254b4e;
  font-size: 0.95rem;
}

.add-training-form {
  max-width: 500px;
}

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

.form-group input:disabled {
  background: #f3f4f6;
  color: #6b7280;
}

.field-hint {
  color: #6b7280;
  font-size: 0.8rem;
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
  align-self: flex-start;
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
  margin: 0.5rem 0 0;
}

.msg-error {
  color: #dc2626;
  font-size: 0.9rem;
  margin: 0.5rem 0 0;
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

/* Chart */
.chart-section {
  background: white;
  padding: 1.5rem;
  border-radius: 8px;
  margin: 1rem 0;
  position: relative;
  height: 350px;
}

/* Pivoted Table */
.table-section {
  overflow-x: auto;
  margin-bottom: 1rem;
}

.training-table {
  width: 100%;
  border-collapse: collapse;
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
}

.training-table th,
.training-table td {
  padding: 0.6rem 0.75rem;
  border-bottom: 1px solid #e5e7eb;
  text-align: center;
  min-width: 55px;
}

.training-table thead {
  background: #f3f4f6;
  border-bottom: 2px solid #d1d5db;
}

.row-label {
  text-align: left !important;
  font-weight: 600;
  background: #f0fdf4;
  min-width: 200px;
  position: sticky;
  left: 0;
}

.summary-col {
  background: #f3f4f6;
  font-weight: 700;
}

.qualite-row td:last-child {
  text-align: left;
  font-style: italic;
  background: #fffbeb;
}

.cell-input {
  width: 100%;
  padding: 0.4rem;
  border: 1px solid #d1d5db;
  border-radius: 4px;
  text-align: center;
}

.alert-defauts {
  color: #dc2626;
  background: #fee2e2 !important;
}

/* Remarks */
.remarks-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 0.75rem;
  margin-bottom: 1rem;
}

.remark-item label {
  font-weight: 600;
  display: block;
  margin-bottom: 0.25rem;
  color: #254b4e;
  font-size: 0.85rem;
}

.remark-item textarea {
  width: 100%;
  padding: 0.5rem;
  border: 1px solid #d1d5db;
  border-radius: 4px;
  resize: vertical;
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

@media (max-width: 768px) {
  .stats-bar {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>