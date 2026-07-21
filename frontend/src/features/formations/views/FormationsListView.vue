<template>
  <div class="formations-list-container">
    <div class="page-header mb-4">
      <h2>Suivi des Formations</h2>
      <p class="text-muted">Consultez et mettez à jour le suivi quotidien des formations</p>
    </div>

    <div class="filters-section mb-4">
      <div class="row">
        <div class="col-md-4">
          <label for="statusFilter" class="form-label">Statut</label>
          <select id="statusFilter" v-model="filters.status" class="form-select" @change="loadFormations">
            <option value="">-- Tous les statuts --</option>
            <option value="EN_FORMATION">En Formation</option>
            <option value="EVALUEE">Évaluée</option>
            <option value="VALIDEE">Validée</option>
            <option value="ECHOUEE">Échouée</option>
          </select>
        </div>
        <div class="col-md-4">
          <label for="projectFilter" class="form-label">Projet</label>
          <select id="projectFilter" v-model.number="filters.projectId" class="form-select" @change="loadFormations">
            <option value="">-- Tous les projets --</option>
            <option v-for="project in projects" :key="project.idProjet" :value="project.idProjet">
              {{ project.nom }}
            </option>
          </select>
        </div>
        <div class="col-md-4">
          <label for="typeFilter" class="form-label">Type</label>
          <select id="typeFilter" v-model="filters.type" class="form-select" @change="loadFormations">
            <option value="">-- Tous les types --</option>
            <option value="primary">Affectation Primaire</option>
            <option value="secondary">Affectation Secondaire</option>
          </select>
        </div>
      </div>
    </div>

    <div v-if="loading" class="spinner-border" role="status">
      <span class="visually-hidden">Chargement...</span>
    </div>

    <div v-else-if="error" class="alert alert-danger" role="alert">
      {{ error }}
    </div>

    <div v-else>
      <div class="table-responsive">
        <table class="table table-striped table-hover">
          <thead class="table-light">
            <tr>
              <th>Opérateur</th>
              <th>Poste</th>
              <th>Projet</th>
              <th>Cadence Moyenne</th>
              <th>Défauts</th>
              <th>Statut</th>
              <th>Type</th>
              <th>Progression</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="formation in formations" :key="formation.idAffectation">
              <td>
                <strong>{{ formation.operateur.nom }} {{ formation.operateur.prenom }}</strong>
                <br />
                <small class="text-muted">{{ formation.operateur.matricule }}</small>
              </td>
              <td>{{ formation.poste.nom }}</td>
              <td>{{ formation.projet.nom }}</td>
              <td>
                <span v-if="getStatistics(formation.idAffectation)?.cadenceMoyenne" class="badge bg-info">
                  {{ getStatistics(formation.idAffectation).cadenceMoyenne.toFixed(0) }}/{{ formation.poste.cadenceObjectif }}
                </span>
                <span v-else class="text-muted">—</span>
              </td>
              <td>
                <span
                  :class="['badge', getDefectsBadgeClass(formation.idAffectation)]"
                >
                  {{ getStatistics(formation.idAffectation)?.totalDefauts || 0 }}
                </span>
              </td>
              <td>
                <span :class="['badge', getStatusBadgeClass(formation.statut)]">
                  {{ formation.statut }}
                </span>
              </td>
              <td>
                <span v-if="formation.estAffectationPrimaire" class="badge bg-primary">Primaire</span>
                <span v-else class="badge bg-secondary">Secondaire</span>
              </td>
              <td>
                <div class="progress" style="height: 20px">
                  <div
                    class="progress-bar"
                    :style="{ width: getProgressPercentage(formation.idAffectation) + '%' }"
                    role="progressbar"
                    :aria-valuenow="getProgressPercentage(formation.idAffectation)"
                    aria-valuemin="0"
                    aria-valuemax="100"
                  >
                    {{ getProgressPercentage(formation.idAffectation) }}%
                  </div>
                </div>
              </td>
              <td>
                <router-link
                  :to="`/formations/tracking/${formation.idAffectation}`"
                  class="btn btn-sm btn-primary"
                >
                  <i class="bi bi-eye"></i> Voir
                </router-link>
              </td>
            </tr>
            <tr v-if="formations.length === 0">
              <td colspan="9" class="text-center text-muted">Aucune formation trouvée</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()
const loading = ref(true)
const error = ref(null)
const formations = ref([])
const projects = ref([])
const statisticsCache = ref({})

const filters = ref({
  status: '',
  projectId: '',
  type: '',
})

const loadFormations = async () => {
  try {
    loading.value = true
    error.value = null

    // Build query string
    const params = new URLSearchParams()
    if (filters.value.projectId) {
      params.append('projectId', filters.value.projectId)
    }
    if (filters.value.status) {
      params.append('status', filters.value.status)
    }

    const response = await axios.get(`/api/formations/mon-equipe?${params}`)
    formations.value = response.data

    // Load statistics for each formation
    for (const formation of formations.value) {
      await loadFormationStats(formation.idAffectation)
    }
  } catch (err) {
    error.value = 'Erreur lors du chargement des formations'
    console.error(err)
  } finally {
    loading.value = false
  }
}

const loadFormationStats = async (affectationId) => {
  try {
    const response = await axios.get(`/api/formations/${affectationId}/details`)
    statisticsCache.value[affectationId] = response.data.statistics
  } catch (err) {
    console.error(`Erreur lors du chargement des stats pour ${affectationId}`, err)
  }
}

const loadProjects = async () => {
  try {
    const response = await axios.get('/api/structure/projets')
    projects.value = response.data
  } catch (err) {
    console.error('Erreur lors du chargement des projets', err)
  }
}

const getStatistics = (affectationId) => {
  return statisticsCache.value[affectationId] || {}
}

const getProgressPercentage = (affectationId) => {
  const stats = getStatistics(affectationId)
  return stats.daysWithData ? Math.round((stats.daysWithData / 12) * 100) : 0
}

const getStatusBadgeClass = (status) => {
  const statusMap = {
    EN_FORMATION: 'bg-warning text-dark',
    EVALUEE: 'bg-info',
    VALIDEE: 'bg-success',
    ECHOUEE: 'bg-danger',
  }
  return statusMap[status] || 'bg-secondary'
}

const getDefectsBadgeClass = (affectationId) => {
  const stats = getStatistics(affectationId)
  if (!stats.totalDefauts) return 'bg-success'
  return stats.qualityObjectifMet ? 'bg-info' : 'bg-warning'
}

onMounted(async () => {
  await Promise.all([loadFormations(), loadProjects()])
})
</script>

<style scoped>
.formations-list-container {
  background-color: #f8f9fa;
  padding: 2rem;
  min-height: 100vh;
}

.page-header h2 {
  color: #333;
  font-weight: 600;
}

.filters-section {
  background-color: white;
  padding: 1.5rem;
  border-radius: 0.375rem;
  box-shadow: 0 0.125rem 0.25rem rgba(0, 0, 0, 0.075);
}

.table {
  background-color: white;
  box-shadow: 0 0.125rem 0.25rem rgba(0, 0, 0, 0.075);
  margin-bottom: 0;
}

.table thead th {
  border-bottom: 2px solid #dee2e6;
  font-weight: 600;
}

.progress {
  background-color: #e9ecef;
}

.badge {
  font-size: 0.75rem;
  padding: 0.4rem 0.6rem;
}
</style>
