<template>
  <div class="card">
    <div class="card-header bg-success text-white">
      <h5 class="mb-0">Formations de l'Opérateur: {{ operateurMatricule }}</h5>
    </div>
    <div class="card-body">
      <div v-if="loading" class="text-center">
        <div class="spinner-border" role="status">
          <span class="visually-hidden">Chargement...</span>
        </div>
      </div>

      <div v-else-if="trainings.length === 0" class="alert alert-info">
        Aucune formation enregistrée pour cet opérateur.
      </div>

      <div v-else class="table-responsive">
        <table class="table table-hover">
          <thead class="table-light">
            <tr>
              <th>Poste</th>
              <th>Projet</th>
              <th>Type</th>
              <th>Statut</th>
              <th>Début</th>
              <th>Évaluation</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="training in trainings" :key="training.idAffectation">
              <td>{{ training.poste?.nom }}</td>
              <td>{{ training.projet?.nom }}</td>
              <td>
                <span
                  class="badge"
                  :class="training.estAffectationPrimaire ? 'bg-warning' : 'bg-info'"
                >
                  {{ training.estAffectationPrimaire ? 'Primaire' : 'Secondaire' }}
                </span>
              </td>
              <td>
                <span class="badge" :class="getStatusBadge(training.statut)">
                  {{ training.statut }}
                </span>
              </td>
              <td>{{ formatDate(training.dateDebut) }}</td>
              <td>{{ formatDate(training.dateEvaluationPrevue) }}</td>
              <td>
                <button @click="viewJournal(training.idAffectation)" class="btn btn-sm btn-primary">
                  📋 Journal
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Journal Modal -->
      <div v-if="showJournal" class="modal d-block" style="background: rgba(0, 0, 0, 0.5)">
        <div class="modal-dialog modal-lg">
          <div class="modal-content">
            <div class="modal-header">
              <h5 class="modal-title">Journal - Jour {{ currentDay }}/12</h5>
              <button type="button" class="btn-close" @click="closeJournal"></button>
            </div>
            <div class="modal-body">
              <DailyJournalForm
                :affectation-id="selectedAffectationId"
                :operateur-matricule="operateurMatricule"
                @entry-saved="refreshJournal"
              />
            </div>
            <div class="modal-footer">
              <button type="button" class="btn btn-secondary" @click="closeJournal">Fermer</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'
import DailyJournalForm from './DailyJournalForm.vue'

export default {
  name: 'OperatorTrainingView',
  components: { DailyJournalForm },
  props: {
    operateurMatricule: {
      type: String,
      required: true,
    },
  },
  data() {
    return {
      trainings: [],
      loading: false,
      showJournal: false,
      selectedAffectationId: null,
      currentDay: 1,
    }
  },
  methods: {
    async loadTrainings() {
      this.loading = true
      try {
        const response = await axios.get(`/api/formations/operateur/${this.operateurMatricule}`)
        this.trainings = response.data
      } catch (error) {
        console.error('Erreur chargement formations:', error)
      } finally {
        this.loading = false
      }
    },
    async viewJournal(affectationId) {
      this.selectedAffectationId = affectationId
      this.showJournal = true
    },
    async refreshJournal() {
      await this.loadTrainings()
    },
    closeJournal() {
      this.showJournal = false
      this.selectedAffectationId = null
    },
    formatDate(date) {
      return date ? new Date(date).toLocaleDateString('fr-FR') : '-'
    },
    getStatusBadge(statut) {
      const badges = {
        EN_FORMATION: 'bg-warning',
        EVALUEE: 'bg-info',
        VALIDEE: 'bg-success',
        ECHOUEE: 'bg-danger',
      }
      return badges[statut] || 'bg-secondary'
    },
  },
  mounted() {
    this.loadTrainings()
  },
  watch: {
    operateurMatricule() {
      this.loadTrainings()
    },
  },
}
</script>

<style scoped>
.modal.d-block {
  display: block !important;
}
</style>
