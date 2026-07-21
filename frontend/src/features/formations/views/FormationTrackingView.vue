<template>
  <div class="formation-tracking-container">
    <div class="breadcrumb-section mb-4">
      <nav aria-label="breadcrumb">
        <ol class="breadcrumb">
          <li class="breadcrumb-item"><router-link to="/formations">Formations</router-link></li>
          <li class="breadcrumb-item active">{{ formationDetails?.operateurNom }} - {{ formationDetails?.posteNom }}</li>
        </ol>
      </nav>
    </div>

    <div v-if="loading" class="spinner-border" role="status">
      <span class="visually-hidden">Chargement...</span>
    </div>

    <div v-else-if="error" class="alert alert-danger" role="alert">
      {{ error }}
    </div>

    <div v-else-if="formationDetails">
      <!-- Formation Header -->
      <div class="card mb-4">
        <div class="card-body">
          <div class="row">
            <div class="col-md-6">
              <h2 class="card-title">{{ formationDetails.operateurNom }} {{ formationDetails.operateurPrenom }}</h2>
              <p class="text-muted">Matricule: {{ formationDetails.operateurMatricule }}</p>
            </div>
            <div class="col-md-6 text-end">
              <span :class="['badge', getStatusBadgeClass(formationDetails.statut)]">
                {{ formationDetails.statut }}
              </span>
              <span v-if="formationDetails.estAffectationPrimaire" class="badge bg-primary ms-2">
                Affectation Primaire
              </span>
            </div>
          </div>
          <hr />
          <div class="row">
            <div class="col-md-3">
              <strong>Poste:</strong> {{ formationDetails.posteNom }}
            </div>
            <div class="col-md-3">
              <strong>Projet:</strong> {{ formationDetails.projetNom }}
            </div>
            <div class="col-md-3">
              <strong>Date Début:</strong> {{ formatDate(formationDetails.dateDebut) }}
            </div>
            <div class="col-md-3">
              <strong>Évaluation Prévue:</strong> {{ formatDate(formationDetails.dateEvaluationPrevue) }}
            </div>
          </div>
          <hr />
          <div class="row">
            <div class="col-md-6">
              <strong>Cadence Objectif:</strong> {{ formationDetails.cadenceObjectif }} pièces/jour
            </div>
            <div class="col-md-6">
              <strong>Objectif Qualité:</strong> {{ formationDetails.qualiteObjectif }}
            </div>
          </div>
        </div>
      </div>

      <!-- Chart Section -->
      <div class="card mb-4">
        <div class="card-header bg-light">
          <h5 class="card-title mb-0">Suivi de Cadence</h5>
        </div>
        <div class="card-body">
          <div v-if="chartData" style="position: relative; height: 400px">
            <Line :data="chartData" :options="chartOptions" />
          </div>
          <div v-else class="text-muted">Aucune donnée de cadence disponible</div>
        </div>
      </div>

      <!-- Statistics Section -->
      <div class="row mb-4">
        <div class="col-md-3">
          <div class="card text-center">
            <div class="card-body">
              <h6 class="card-subtitle mb-2 text-muted">Cadence Moyenne</h6>
              <h3 class="card-title">{{ statistics?.cadenceMoyenne?.toFixed(1) || 0 }}</h3>
            </div>
          </div>
        </div>
        <div class="col-md-3">
          <div class="card text-center">
            <div class="card-body">
              <h6 class="card-subtitle mb-2 text-muted">Défauts Total</h6>
              <h3 class="card-title" :class="statistics?.qualityObjectifMet ? 'text-success' : 'text-danger'">
                {{ statistics?.totalDefauts || 0 }}
              </h3>
            </div>
          </div>
        </div>
        <div class="col-md-3">
          <div class="card text-center">
            <div class="card-body">
              <h6 class="card-subtitle mb-2 text-muted">Jours Saisis</h6>
              <h3 class="card-title">{{ statistics?.daysWithData || 0 }}/12</h3>
            </div>
          </div>
        </div>
        <div class="col-md-3">
          <div class="card text-center">
            <div class="card-body">
              <h6 class="card-subtitle mb-2 text-muted">Objectif Qualité</h6>
              <h3 :class="['card-title', statistics?.qualityObjectifMet ? 'text-success' : 'text-warning']">
                {{ statistics?.qualityObjectifMet ? '✓ Atteint' : '✗ Non atteint' }}
              </h3>
            </div>
          </div>
        </div>
      </div>

      <!-- Daily Tracking Table -->
      <div class="card">
        <div class="card-header bg-light">
          <h5 class="card-title mb-0">Suivi Quotidien (12 Jours)</h5>
        </div>
        <div class="card-body">
          <div class="table-responsive">
            <table class="table table-bordered table-hover">
              <thead class="table-light">
                <tr>
                  <th style="width: 10%">Jour</th>
                  <th style="width: 15%">Cadence Objectif</th>
                  <th style="width: 15%">Cadence Réalisée</th>
                  <th style="width: 12%">Défauts</th>
                  <th style="width: 30%">Remarques</th>
                  <th style="width: 18%">Actions</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="day in 12" :key="day">
                  <td><strong>J{{ day }}</strong></td>
                  <td class="text-center">{{ formationDetails.cadenceObjectif }}</td>
                  <td>
                    <input
                      v-model.number="dailyData[day].cadenceRealisee"
                      type="number"
                      class="form-control form-control-sm"
                      min="0"
                      max="500"
                      @blur="markDayAsDirty(day)"
                    />
                  </td>
                  <td>
                    <input
                      v-model.number="dailyData[day].nbDefauts"
                      type="number"
                      class="form-control form-control-sm"
                      min="0"
                      max="100"
                      @blur="markDayAsDirty(day)"
                    />
                  </td>
                  <td>
                    <input
                      v-model="dailyData[day].remarques"
                      type="text"
                      class="form-control form-control-sm"
                      placeholder="Remarques..."
                      @blur="markDayAsDirty(day)"
                    />
                  </td>
                  <td>
                    <button
                      v-if="isDayDirty(day)"
                      @click="saveDay(day)"
                      class="btn btn-sm btn-primary"
                      :disabled="savingDay === day"
                    >
                      <span v-if="savingDay === day" class="spinner-border spinner-border-sm me-2"></span>
                      Enregistrer
                    </button>
                    <span v-else class="text-muted text-sm">Enregistré</span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <!-- Totals Row -->
          <div class="row mt-4">
            <div class="col-md-12">
              <div class="d-flex justify-content-between">
                <strong>Moyennes:</strong>
                <span>{{ statistics?.cadenceMoyenne?.toFixed(1) || '—' }} / Cadence Objectif</span>
              </div>
              <div class="d-flex justify-content-between mt-2">
                <strong>Total Défauts:</strong>
                <span :class="statistics?.qualityObjectifMet ? 'text-success' : 'text-danger'">
                  {{ statistics?.totalDefauts || 0 }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { Line } from 'vue-chartjs'
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
} from 'chart.js'
import axios from 'axios'

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, Title, Tooltip, Legend)

const route = useRoute()
const affectationId = route.params.id

const loading = ref(true)
const error = ref(null)
const formationDetails = ref(null)
const chartData = ref(null)
const statistics = ref(null)
const savingDay = ref(null)

// Daily data tracking
const dailyData = ref({})
const dirtyDays = ref(new Set())

// Initialize daily data
const initializeDailyData = () => {
  for (let day = 1; day <= 12; day++) {
    dailyData.value[day] = {
      jour: day,
      cadenceRealisee: null,
      nbDefauts: 0,
      remarques: '',
    }
  }
}

// Load formation details
const loadFormationDetails = async () => {
  try {
    loading.value = true
    error.value = null

    const [detailsResponse, chartResponse] = await Promise.all([
      axios.get(`/api/formations/${affectationId}/details`),
      axios.get(`/api/formations/${affectationId}/chart-data`),
    ])

    formationDetails.value = detailsResponse.data
    statistics.value = detailsResponse.data.statistics

    // Load daily data
    if (formationDetails.value.dailyTrackings) {
      formationDetails.value.dailyTrackings.forEach((tracking) => {
        dailyData.value[tracking.jour] = tracking
      })
    }

    // Prepare chart data
    prepareChartData(chartResponse.data)
  } catch (err) {
    error.value = 'Erreur lors du chargement des données de formation'
    console.error(err)
  } finally {
    loading.value = false
  }
}

// Prepare chart data for display
const prepareChartData = (rawChartData) => {
  chartData.value = {
    labels: rawChartData.labels.map((day) => `J${day}`),
    datasets: [rawChartData.cadenceObjectifDataset, rawChartData.cadenceRealiseeDataset],
  }
}

const chartOptions = computed(() => ({
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: {
      display: true,
      position: 'top',
    },
    title: {
      display: false,
    },
  },
  scales: {
    y: {
      beginAtZero: true,
      title: {
        display: true,
        text: 'Cadence (pièces/jour)',
      },
    },
    x: {
      title: {
        display: true,
        text: 'Jour de Formation',
      },
    },
  },
}))

// Mark day as dirty
const markDayAsDirty = (day) => {
  dirtyDays.value.add(day)
}

// Check if day is dirty
const isDayDirty = (day) => {
  return dirtyDays.value.has(day)
}

// Save day data
const saveDay = async (day) => {
  try {
    savingDay.value = day
    const data = dailyData.value[day]

    await axios.put(`/api/formations/${affectationId}/daily/${day}`, {
      cadenceRealisee: data.cadenceRealisee,
      nbDefauts: data.nbDefauts,
      remarques: data.remarques,
    })

    // Mark as clean
    dirtyDays.value.delete(day)

    // Reload formation details to update statistics
    await loadFormationDetails()
  } catch (err) {
    error.value = `Erreur lors de l'enregistrement du jour ${day}`
    console.error(err)
  } finally {
    savingDay.value = null
  }
}

// Format date
const formatDate = (dateString) => {
  if (!dateString) return '—'
  const date = new Date(dateString)
  return date.toLocaleDateString('fr-FR')
}

// Get status badge class
const getStatusBadgeClass = (status) => {
  const statusMap = {
    EN_FORMATION: 'bg-warning',
    EVALUEE: 'bg-info',
    VALIDEE: 'bg-success',
    ECHOUEE: 'bg-danger',
  }
  return statusMap[status] || 'bg-secondary'
}

onMounted(() => {
  initializeDailyData()
  loadFormationDetails()
})
</script>

<style scoped>
.formation-tracking-container {
  background-color: #f8f9fa;
  padding: 2rem;
  min-height: 100vh;
}

.card {
  box-shadow: 0 0.125rem 0.25rem rgba(0, 0, 0, 0.075);
  border: none;
}

.card-header {
  border-bottom: 1px solid #dee2e6;
}

.table-bordered {
  border-color: #dee2e6;
}

.table-hover tbody tr:hover {
  background-color: #f8f9fa;
}

.form-control-sm {
  padding: 0.25rem 0.5rem;
  font-size: 0.875rem;
}

.badge {
  font-size: 0.875rem;
  padding: 0.5rem 0.75rem;
}
</style>
