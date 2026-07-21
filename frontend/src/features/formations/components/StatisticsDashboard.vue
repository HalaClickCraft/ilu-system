<script setup>
import { ref, onMounted, computed } from 'vue'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()

// Overall stats
const overallStats = ref({
  totalTrainings: 0,
  enFormation: 0,
  validees: 0,
  completionRate: 0,
})

// Trainings by status
const trainingsByStatus = ref({
  EN_FORMATION: 0,
  VALIDEE: 0,
  EN_ATTENTE: 0,
})

// All trainings data
const allTrainings = ref([])
const filteredTrainings = ref([])
const statusFilter = ref('')
const operatorSearch = ref('')
const loading = ref(false)
const error = ref('')

async function loadStatistics() {
  loading.value = true
  error.value = ''
  try {
    const response = await fetch('/api/formations/stats', {
      headers: { Authorization: `Bearer ${authStore.token}` },
    })
    if (!response.ok) throw new Error('Impossible de charger les statistiques')
    const data = await response.json()

    overallStats.value = data.overall
    trainingsByStatus.value = data.byStatus
    allTrainings.value = data.trainings || []
    filteredTrainings.value = allTrainings.value
  } catch (err) {
    error.value = err.message
  } finally {
    loading.value = false
  }
}

// Filter trainings
const filteredList = computed(() => {
  return filteredTrainings.value.filter((t) => {
    const matchStatus = !statusFilter.value || t.statut === statusFilter.value
    const matchSearch =
      !operatorSearch.value ||
      t.operateurNom.toLowerCase().includes(operatorSearch.value.toLowerCase()) ||
      t.operateurMatricule.includes(operatorSearch.value)
    return matchStatus && matchSearch
  })
})

// Calculate percentages for status
const statusPercentages = computed(() => {
  const total = overallStats.value.totalTrainings || 1
  return {
    enFormation: Math.round((trainingsByStatus.value.EN_FORMATION / total) * 100),
    validees: Math.round((trainingsByStatus.value.VALIDEE / total) * 100),
    enAttente: Math.round((trainingsByStatus.value.EN_ATTENTE / total) * 100),
  }
})

onMounted(loadStatistics)
</script>

<template>
  <div class="stats-dashboard">
    <h2>📊 Tableau de Bord - Formations de l'Entreprise</h2>

    <!-- Overall Stats Cards -->
    <div class="stats-grid">
      <div class="stat-card primary">
        <div class="stat-icon">📚</div>
        <div class="stat-content">
          <div class="stat-value">{{ overallStats.totalTrainings }}</div>
          <div class="stat-label">Formations totales</div>
        </div>
      </div>

      <div class="stat-card success">
        <div class="stat-icon">✅</div>
        <div class="stat-content">
          <div class="stat-value">{{ overallStats.validees }}</div>
          <div class="stat-label">Validées</div>
        </div>
      </div>

      <div class="stat-card warning">
        <div class="stat-icon">🔄</div>
        <div class="stat-content">
          <div class="stat-value">{{ overallStats.enFormation }}</div>
          <div class="stat-label">En cours</div>
        </div>
      </div>

      <div class="stat-card info">
        <div class="stat-icon">📈</div>
        <div class="stat-content">
          <div class="stat-value">{{ overallStats.completionRate }}%</div>
          <div class="stat-label">Taux de complétude</div>
        </div>
      </div>
    </div>

    <!-- Status Breakdown -->
    <div class="status-breakdown">
      <h3>Répartition par Statut</h3>
      <div class="status-bars">
        <div class="status-item">
          <div class="status-label">En Formation</div>
          <div class="status-bar-container">
            <div
              class="status-bar warning"
              :style="{ width: statusPercentages.enFormation + '%' }"
            ></div>
          </div>
          <div class="status-numbers">
            {{ trainingsByStatus.EN_FORMATION }} ({{ statusPercentages.enFormation }}%)
          </div>
        </div>

        <div class="status-item">
          <div class="status-label">Validées</div>
          <div class="status-bar-container">
            <div
              class="status-bar success"
              :style="{ width: statusPercentages.validees + '%' }"
            ></div>
          </div>
          <div class="status-numbers">
            {{ trainingsByStatus.VALIDEE }} ({{ statusPercentages.validees }}%)
          </div>
        </div>

        <div class="status-item">
          <div class="status-label">En Attente</div>
          <div class="status-bar-container">
            <div
              class="status-bar secondary"
              :style="{ width: statusPercentages.enAttente + '%' }"
            ></div>
          </div>
          <div class="status-numbers">
            {{ trainingsByStatus.EN_ATTENTE }} ({{ statusPercentages.enAttente }}%)
          </div>
        </div>
      </div>
    </div>

    <!-- Trainings List with Filters -->
    <div class="trainings-section">
      <h3>Détail des Formations</h3>

      <div class="filters">
        <input
          v-model="operatorSearch"
          type="text"
          placeholder="Rechercher par opérateur ou matricule..."
          class="filter-input"
        />
        <select v-model="statusFilter" class="filter-select">
          <option value="">-- Tous les statuts --</option>
          <option value="EN_FORMATION">En Formation</option>
          <option value="VALIDEE">Validée</option>
          <option value="EN_ATTENTE">En Attente</option>
        </select>
        <button @click="loadStatistics" class="btn-refresh">🔄 Actualiser</button>
      </div>

      <div v-if="loading" class="loading-state"><span class="spinner"></span> Chargement...</div>

      <div v-else-if="error" class="error-state">⚠️ {{ error }}</div>

      <div v-else-if="filteredList.length === 0" class="empty-state">
        Aucune formation ne correspond à vos critères
      </div>

      <div v-else class="trainings-table">
        <table>
          <thead>
            <tr>
              <th>Opérateur</th>
              <th>Poste</th>
              <th>Projet</th>
              <th>Chef d'équipe</th>
              <th>Statut</th>
              <th>Progression</th>
              <th>Début</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="training in filteredList" :key="training.idAffectation">
              <td class="operator-cell">
                <strong>{{ training.operateurNom }}</strong>
                <br />
                <small>{{ training.operateurMatricule }}</small>
              </td>
              <td>{{ training.posteNom }}</td>
              <td>{{ training.projetNom }}</td>
              <td>{{ training.chefEquipeNom || '—' }}</td>
              <td>
                <span :class="['status-badge', training.statut.toLowerCase().replace('_', '-')]">
                  {{ training.statut }}
                </span>
              </td>
              <td>
                <div class="progress-cell">
                  <span class="progress-text">{{ training.dernierJourSaisi || 0 }}/12</span>
                  <div class="mini-progress-bar">
                    <div
                      class="mini-progress-fill"
                      :style="{ width: ((training.dernierJourSaisi || 0) / 12) * 100 + '%' }"
                    ></div>
                  </div>
                </div>
              </td>
              <td>{{ training.dateDebut }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<style scoped>
.stats-dashboard {
  padding: 1.5rem;
  background: #ffffff;
}

h2 {
  color: #123f43;
  margin-bottom: 2rem;
  font-size: 1.5rem;
}

h3 {
  color: #254b4e;
  margin-bottom: 1rem;
  font-size: 1rem;
}

/* Stats Grid */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1.5rem;
  border-radius: 8px;
  color: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.stat-card.primary {
  background: linear-gradient(135deg, #123f43 0%, #0f172a 100%);
}

.stat-card.success {
  background: linear-gradient(135deg, #16a34a 0%, #15803d 100%);
}

.stat-card.warning {
  background: linear-gradient(135deg, #ea580c 0%, #c2410c 100%);
}

.stat-card.info {
  background: linear-gradient(135deg, #0284c7 0%, #0369a1 100%);
}

.stat-icon {
  font-size: 2rem;
}

.stat-content {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 1.75rem;
  font-weight: 700;
  line-height: 1;
}

.stat-label {
  font-size: 0.85rem;
  opacity: 0.9;
  margin-top: 0.25rem;
}

/* Status Breakdown */
.status-breakdown {
  background: #f8fafc;
  padding: 1.5rem;
  border-radius: 8px;
  margin-bottom: 2rem;
}

.status-bars {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  margin-top: 1rem;
}

.status-item {
  display: flex;
  gap: 1rem;
  align-items: center;
}

.status-label {
  min-width: 120px;
  font-weight: 600;
  color: #254b4e;
}

.status-bar-container {
  flex: 1;
  height: 24px;
  background: #e5e7eb;
  border-radius: 4px;
  overflow: hidden;
}

.status-bar {
  height: 100%;
  transition: width 0.3s ease;
}

.status-bar.warning {
  background: linear-gradient(90deg, #fbbf24, #f59e0b);
}

.status-bar.success {
  background: linear-gradient(90deg, #86efac, #22c55e);
}

.status-bar.secondary {
  background: linear-gradient(90deg, #93c5fd, #3b82f6);
}

.status-numbers {
  min-width: 80px;
  text-align: right;
  font-weight: 600;
  color: #547174;
  font-size: 0.85rem;
}

/* Trainings Section */
.trainings-section {
  background: #f8fafc;
  padding: 1.5rem;
  border-radius: 8px;
}

.filters {
  display: flex;
  gap: 1rem;
  margin-bottom: 1.5rem;
  flex-wrap: wrap;
}

.filter-input,
.filter-select {
  padding: 0.6rem 0.8rem;
  border: 1px solid #d1d5db;
  border-radius: 4px;
  font-family: inherit;
  font-size: 0.9rem;
}

.filter-input {
  flex: 1;
  min-width: 200px;
}

.filter-select {
  min-width: 150px;
}

.btn-refresh {
  padding: 0.6rem 1rem;
  background: #123f43;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 600;
  transition: background 0.2s;
}

.btn-refresh:hover {
  background: #0f2f35;
}

/* States */
.loading-state,
.empty-state {
  text-align: center;
  padding: 2rem;
  color: #666;
}

.error-state {
  background: #fee2e2;
  color: #991b1b;
  padding: 1rem;
  border-radius: 4px;
  margin: 1rem 0;
}

/* Table */
.trainings-table {
  overflow-x: auto;
  background: white;
  border-radius: 6px;
  border: 1px solid #e5e7eb;
}

table {
  width: 100%;
  border-collapse: collapse;
}

thead {
  background: #f3f4f6;
  border-bottom: 2px solid #d1d5db;
}

th {
  padding: 0.75rem 1rem;
  text-align: left;
  font-weight: 600;
  color: #254b4e;
  font-size: 0.85rem;
}

td {
  padding: 0.75rem 1rem;
  border-bottom: 1px solid #e5e7eb;
  font-size: 0.9rem;
  color: #547174;
}

tbody tr:hover {
  background: #f9fafb;
}

.operator-cell {
  font-weight: 600;
  color: #254b4e;
}

.operator-cell small {
  display: block;
  color: #999;
  font-weight: 400;
  font-size: 0.8rem;
}

.status-badge {
  display: inline-block;
  padding: 0.35rem 0.75rem;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 600;
  white-space: nowrap;
}

.status-badge.validee {
  background: #dcfce7;
  color: #166534;
}

.status-badge.en-formation {
  background: #fef3c7;
  color: #92400e;
}

.status-badge.en-attente {
  background: #dbeafe;
  color: #1e40af;
}

/* Progress Cell */
.progress-cell {
  display: flex;
  gap: 0.5rem;
  align-items: center;
}

.progress-text {
  min-width: 35px;
  font-weight: 600;
  color: #254b4e;
  font-size: 0.85rem;
}

.mini-progress-bar {
  flex: 1;
  height: 4px;
  background: #e5e7eb;
  border-radius: 2px;
  overflow: hidden;
}

.mini-progress-fill {
  background: linear-gradient(90deg, #123f43, #0f2f35);
  height: 100%;
  transition: width 0.3s ease;
}

/* Responsive */
@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .filters {
    flex-direction: column;
  }

  .filter-input,
  .filter-select {
    width: 100%;
  }

  table {
    font-size: 0.8rem;
  }

  th,
  td {
    padding: 0.5rem;
  }
}
</style>
