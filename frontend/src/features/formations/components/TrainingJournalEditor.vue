<script setup>
import { ref, computed, watch, reactive } from 'vue'
import { useAuthStore } from '@/stores/auth'
import Chart from 'chart.js/auto'

const authStore = useAuthStore()

// Search & Selection
const operatorMatricule = ref('')
const selectedTrainingId = ref('')
const trainings = ref([])
const operator = ref(null)
const loading = ref(false)
const error = ref('')

// Training Data
const trainingData = ref(null)
const journalEntries = ref({}) // Key: day, Value: { cadenceRealisee, nbDefauts, remarques }
const postObjective = ref(null)

// Chart reference
let chartInstance = null

// Role-based permissions
const userRole = computed(() => authStore.user?.role)
const isChef = computed(() => userRole.value === 'CHEF_EQUIPE')
const isQualite = computed(() => userRole.value === 'AGENT_QUALITE')
const isRhOrAdmin = computed(() => ['RH', 'ADMIN', 'SUPERVISEUR'].includes(userRole.value))

// Initialize 12-day structure
const initializeJournal = () => {
  journalEntries.value = {}
  for (let i = 1; i <= 12; i++) {
    journalEntries.value[i] = { cadenceRealisee: 0, nbDefauts: 0, remarques: '' }
  }
}

// Computed stats
const stats = computed(() => {
  let totalDefauts = 0
  let totalCadence = 0
  let count = 0

  for (let i = 1; i <= 12; i++) {
    const entry = journalEntries.value[i]
    if (entry && entry.cadenceRealisee > 0) {
      totalCadence += entry.cadenceRealisee
      count++
    }
    totalDefauts += entry.nbDefauts || 0
  }

  return {
    averageCadence: count > 0 ? Math.round(totalCadence / count) : 0,
    totalDefauts: totalDefauts,
    defautAlert: totalDefauts >= 7,
  }
})

// Load trainings for operator
async function searchOperator() {
  if (!operatorMatricule.value) return

  loading.value = true
  error.value = ''
  trainings.value = []
  selectedTrainingId.value = ''

  try {
    const response = await fetch(`/api/formations/operateur/${operatorMatricule.value}`, {
      headers: { Authorization: `Bearer ${authStore.token}` },
    })
    if (!response.ok) throw new Error('Opérateur non trouvé')

    const data = await response.json()
    if (data.length === 0) throw new Error('Aucune formation pour cet opérateur')

    trainings.value = data

    // Auto-load if only one training
    if (data.length === 1) {
      selectedTrainingId.value = data[0].idAffectation
      await loadTrainingData(data[0])
    }
  } catch (err) {
    error.value = err.message
    trainings.value = []
  } finally {
    loading.value = false
  }
}

// Load specific training data
async function loadTrainingData(training) {
  loading.value = true
  error.value = ''

  try {
    trainingData.value = training
    operator.value = training.operateur

    // Load journal entries
    const journalResponse = await fetch(`/api/formations/${training.idAffectation}/journal`, {
      headers: { Authorization: `Bearer ${authStore.token}` },
    })
    const journalData = await journalResponse.json()

    initializeJournal()
    journalData.forEach((entry) => {
      journalEntries.value[entry.jour] = {
        cadenceRealisee: entry.cadenceRealisee || 0,
        nbDefauts: entry.nbDefauts || 0,
        remarques: entry.remarques || '',
      }
    })

    // Get post objective
    postObjective.value = training.postObjectif || 40 // Default fallback

    // Draw chart after data loads
    await new Promise((resolve) => setTimeout(resolve, 100))
    drawChart()
  } catch (err) {
    error.value = 'Erreur lors du chargement: ' + err.message
  } finally {
    loading.value = false
  }
}

// Draw chart with real-time data
function drawChart() {
  const ctx = document.getElementById('trainingChart')
  if (!ctx) return

  const cadenceData = []
  for (let i = 1; i <= 12; i++) {
    cadenceData.push(journalEntries.value[i]?.cadenceRealisee || 0)
  }

  if (chartInstance) {
    chartInstance.destroy()
  }

  chartInstance = new Chart(ctx, {
    type: 'line',
    data: {
      labels: Array.from({ length: 12 }, (_, i) => `J${i + 1}`),
      datasets: [
        {
          label: 'Cadence Objectif',
          data: Array(12).fill(postObjective.value),
          borderColor: '#22c55e',
          borderWidth: 2,
          borderDash: [5, 5],
          fill: false,
          tension: 0,
        },
        {
          label: 'Cadence Réalisée',
          data: cadenceData,
          borderColor: '#3b82f6',
          backgroundColor: 'rgba(59, 130, 246, 0.1)',
          borderWidth: 2,
          fill: true,
          tension: 0.4,
          pointRadius: 4,
          pointBackgroundColor: '#3b82f6',
        },
      ],
    },
    options: {
      responsive: true,
      plugins: {
        legend: {
          display: true,
          position: 'top',
        },
      },
      scales: {
        y: {
          beginAtZero: true,
          max: Math.max(postObjective.value * 1.2, 120),
        },
      },
    },
  })
}

// Watch for changes in journal and redraw chart
watch(
  journalEntries,
  () => {
    if (chartInstance && trainingData.value) {
      drawChart()
    }
  },
  { deep: true },
)

// Can edit column based on role
const canEditCadence = computed(() => isChef.value)
const canEditDefauts = computed(() => isQualite.value)
const canEditRemarques = computed(() => isChef.value)

// Save all changes
async function saveJournal() {
  if (!trainingData.value) return

  loading.value = true
  error.value = ''

  try {
    // Save each day
    for (let day = 1; day <= 12; day++) {
      const entry = journalEntries.value[day]
      if (entry.cadenceRealisee > 0 || entry.nbDefauts > 0 || entry.remarques) {
        const response = await fetch(
          `/api/formations/${trainingData.value.idAffectation}/journal`,
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
    error.value = '✅ Journal sauvegardé avec succès!'
  } catch (err) {
    error.value = '❌ Erreur: ' + err.message
  } finally {
    loading.value = false
  }
}

// Watch for training selection change
watch(selectedTrainingId, async (newId) => {
  if (newId) {
    const training = trainings.value.find((t) => t.idAffectation === parseInt(newId))
    if (training) {
      await loadTrainingData(training)
    }
  }
})
</script>

<template>
  <div class="training-journal-container">
    <!-- Header -->
    <div class="header-section">
      <h2>📚 Suivi Quotidien des Formations</h2>
    </div>

    <!-- Search Section -->
    <div class="search-section">
      <div class="search-box">
        <label>Matricule de l'Opérateur</label>
        <div class="search-input-group">
          <input
            v-model="operatorMatricule"
            type="text"
            placeholder="Ex: OP001"
            @keyup.enter="searchOperator"
            class="search-input"
          />
          <button @click="searchOperator" class="btn-search">🔍 Chercher</button>
        </div>
      </div>

      <!-- Training Selection Dropdown (if multiple) -->
      <div v-if="trainings.length > 1" class="training-select">
        <label>Sélectionner la Formation à Évaluer</label>
        <select v-model="selectedTrainingId" class="select-input">
          <option value="">-- Choisir une formation --</option>
          <option v-for="t in trainings" :key="t.idAffectation" :value="t.idAffectation">
            {{ t.posteNom }} (Jour {{ t.dernierJourSaisi || 0 }}/12)
          </option>
        </select>
      </div>
    </div>

    <!-- Error/Loading States -->
    <div v-if="error && !trainingData" class="alert alert-danger">⚠️ {{ error }}</div>
    <div v-if="loading" class="alert alert-info">⏳ Chargement...</div>

    <!-- Training Data Section -->
    <div v-if="trainingData && operator" class="training-section">
      <!-- Operator Info -->
      <div class="operator-info">
        <div class="info-card"><strong>Opérateur:</strong> {{ operator.nom }}</div>
        <div class="info-card"><strong>Poste:</strong> {{ trainingData.posteNom }}</div>
        <div class="info-card"><strong>Projet:</strong> {{ trainingData.projetNom }}</div>
        <div class="info-card">
          <strong>Statut:</strong>
          <span :class="['status-badge', trainingData.statut.toLowerCase()]">
            {{ trainingData.statut }}
          </span>
        </div>
      </div>

      <!-- Chart Section -->
      <div class="chart-section">
        <canvas id="trainingChart"></canvas>
      </div>

      <!-- 12-Day Editable Table -->
      <div class="table-section">
        <h3>📋 Historique des 12 Jours</h3>
        <table class="training-table">
          <thead>
            <tr>
              <th>Jour</th>
              <th>Cadence Réalisée</th>
              <th>Nombre de Défauts</th>
              <th>Remarques</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="day in 12" :key="day">
              <td class="day-cell">J{{ day }}</td>
              <td class="input-cell">
                <input
                  v-model.number="journalEntries[day].cadenceRealisee"
                  type="number"
                  min="0"
                  :disabled="!canEditCadence"
                  class="cell-input"
                  :class="{ 'disabled-input': !canEditCadence }"
                />
              </td>
              <td class="input-cell">
                <input
                  v-model.number="journalEntries[day].nbDefauts"
                  type="number"
                  min="0"
                  :disabled="!canEditDefauts"
                  class="cell-input"
                  :class="{ 'disabled-input': !canEditDefauts }"
                />
              </td>
              <td class="input-cell remarks">
                <textarea
                  v-model="journalEntries[day].remarques"
                  :disabled="!canEditRemarques"
                  class="cell-input cell-textarea"
                  :class="{ 'disabled-input': !canEditRemarques }"
                  rows="1"
                ></textarea>
              </td>
            </tr>
          </tbody>
          <tfoot>
            <tr class="footer-row">
              <td><strong>Totaux</strong></td>
              <td>
                <strong>Moy: {{ stats.averageCadence }}</strong>
              </td>
              <td>
                <strong :class="{ 'alert-defauts': stats.defautAlert }">
                  Total: {{ stats.totalDefauts }}
                </strong>
              </td>
              <td></td>
            </tr>
          </tfoot>
        </table>
      </div>

      <!-- Save Button -->
      <div class="button-section">
        <button @click="saveJournal" :disabled="loading" class="btn-save">
          {{ loading ? 'Sauvegarde...' : '💾 Sauvegarder le Journal' }}
        </button>
        <p
          v-if="error && trainingData"
          :class="error.includes('✅') ? 'text-success' : 'text-danger'"
        >
          {{ error }}
        </p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.training-journal-container {
  padding: 2rem;
  background: #ffffff;
  border-radius: 8px;
  max-width: 1400px;
  margin: 0 auto;
}

.header-section {
  margin-bottom: 2rem;
  border-bottom: 2px solid #123f43;
  padding-bottom: 1rem;
}

.header-section h2 {
  color: #123f43;
  margin: 0;
  font-size: 1.8rem;
}

/* Search Section */
.search-section {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 2rem;
  margin-bottom: 2rem;
  padding: 1.5rem;
  background: #f8fafc;
  border-radius: 8px;
  border-left: 4px solid #123f43;
}

.search-box label,
.training-select label {
  display: block;
  font-weight: 600;
  color: #254b4e;
  margin-bottom: 0.5rem;
  font-size: 0.95rem;
}

.search-input-group {
  display: flex;
  gap: 0.5rem;
}

.search-input,
.select-input {
  flex: 1;
  padding: 0.75rem;
  border: 1px solid #d1d5db;
  border-radius: 4px;
  font-family: inherit;
  font-size: 0.95rem;
}

.search-input:focus,
.select-input:focus {
  outline: none;
  border-color: #123f43;
  box-shadow: 0 0 0 3px rgba(18, 63, 67, 0.1);
}

.btn-search {
  padding: 0.75rem 1.5rem;
  background: #123f43;
  color: white;
  border: none;
  border-radius: 4px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s;
}

.btn-search:hover {
  background: #0f2f35;
}

/* Alerts */
.alert {
  padding: 1rem;
  border-radius: 4px;
  margin-bottom: 1rem;
}

.alert-danger {
  background: #fee2e2;
  color: #991b1b;
  border-left: 4px solid #dc2626;
}

.alert-info {
  background: #dbeafe;
  color: #1e40af;
  border-left: 4px solid #0284c7;
}

/* Operator Info */
.operator-info {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1rem;
  margin-bottom: 2rem;
}

.info-card {
  padding: 1rem;
  background: linear-gradient(135deg, #123f43 0%, #0f2f35 100%);
  color: white;
  border-radius: 6px;
  font-size: 0.95rem;
}

.info-card strong {
  display: block;
  font-size: 0.8rem;
  opacity: 0.9;
  margin-bottom: 0.25rem;
}

.status-badge {
  display: inline-block;
  padding: 0.25rem 0.6rem;
  border-radius: 12px;
  font-weight: 600;
  font-size: 0.8rem;
}

.status-badge.validee {
  background: #dcfce7;
  color: #166534;
}

.status-badge.en_formation {
  background: #fef3c7;
  color: #92400e;
}

/* Chart Section */
.chart-section {
  background: #f8fafc;
  padding: 1.5rem;
  border-radius: 8px;
  margin-bottom: 2rem;
  position: relative;
  height: 400px;
}

#trainingChart {
  max-height: 350px;
}

/* Table Section */
.table-section {
  margin-bottom: 2rem;
}

.table-section h3 {
  color: #254b4e;
  margin-bottom: 1rem;
  font-size: 1.1rem;
}

.training-table {
  width: 100%;
  border-collapse: collapse;
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  overflow: hidden;
}

.training-table thead {
  background: #f3f4f6;
  border-bottom: 2px solid #d1d5db;
}

.training-table th {
  padding: 1rem;
  text-align: left;
  font-weight: 600;
  color: #254b4e;
  font-size: 0.9rem;
}

.training-table td {
  padding: 0.75rem 1rem;
  border-bottom: 1px solid #e5e7eb;
  color: #547174;
}

.day-cell {
  font-weight: 600;
  color: #254b4e;
  min-width: 60px;
}

.input-cell {
  vertical-align: middle;
}

.cell-input {
  width: 100%;
  padding: 0.5rem;
  border: 1px solid #d1d5db;
  border-radius: 4px;
  font-family: inherit;
  font-size: 0.95rem;
}

.cell-input:focus {
  outline: none;
  border-color: #123f43;
  box-shadow: 0 0 0 2px rgba(18, 63, 67, 0.1);
}

.cell-input:disabled,
.disabled-input {
  background-color: #f3f4f6;
  color: #999;
  cursor: not-allowed;
}

.cell-textarea {
  resize: vertical;
  min-height: 50px;
  font-family: inherit;
}

.remarks {
  width: 300px;
}

/* Footer Row */
.footer-row {
  background: #f3f4f6;
  border-top: 2px solid #d1d5db;
  font-weight: 600;
}

.footer-row td {
  border-bottom: none;
  color: #254b4e;
}

.alert-defauts {
  color: #dc2626;
  background: #fee2e2;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
}

/* Button Section */
.button-section {
  display: flex;
  gap: 1rem;
  align-items: center;
  margin-top: 2rem;
  padding-top: 2rem;
  border-top: 2px solid #e5e7eb;
}

.btn-save {
  padding: 0.75rem 2rem;
  background: #123f43;
  color: white;
  border: none;
  border-radius: 4px;
  font-weight: 600;
  font-size: 1rem;
  cursor: pointer;
  transition: background 0.2s;
}

.btn-save:hover:not(:disabled) {
  background: #0f2f35;
}

.btn-save:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.text-success {
  color: #16a34a;
  font-weight: 600;
}

.text-danger {
  color: #dc2626;
  font-weight: 600;
}

/* Responsive */
@media (max-width: 768px) {
  .training-journal-container {
    padding: 1rem;
  }

  .search-section {
    grid-template-columns: 1fr;
    gap: 1rem;
  }

  .remarks {
    width: 100%;
  }

  .table-section {
    overflow-x: auto;
  }
}
</style>
