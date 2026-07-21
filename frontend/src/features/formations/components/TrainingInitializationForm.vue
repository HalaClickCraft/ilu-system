<template>
  <div class="card">
    <div class="card-header bg-primary text-white">
      <h5 class="mb-0">Initialiser une Formation Multi-Poste</h5>
    </div>
    <div class="card-body">
      <form @submit.prevent="submitTraining">
        <div class="row">
          <div class="col-md-6 mb-3">
            <label class="form-label">Opérateur *</label>
            <select v-model="form.operateurMatricule" class="form-select" required>
              <option value="">-- Sélectionner --</option>
              <option v-for="op in operateurs" :key="op.matricule" :value="op.matricule">
                {{ op.matricule }} - {{ op.nom }} {{ op.prenom }}
              </option>
            </select>
          </div>

          <div class="col-md-6 mb-3">
            <label class="form-label">Projet *</label>
            <select v-model="form.projetId" @change="loadPostes" class="form-select" required>
              <option value="">-- Sélectionner --</option>
              <option v-for="p in projets" :key="p.idProjet" :value="p.idProjet">
                {{ p.nom }}
              </option>
            </select>
          </div>
        </div>

        <div class="row">
          <div class="col-md-6 mb-3">
            <label class="form-label">Poste *</label>
            <select v-model="form.posteId" class="form-select" required>
              <option value="">-- Sélectionner --</option>
              <option v-for="poste in postesFiltered" :key="poste.idPoste" :value="poste.idPoste">
                {{ poste.nom }}
              </option>
            </select>
          </div>

          <div class="col-md-6 mb-3">
            <label class="form-label">Type d'Affectation</label>
            <input
              type="text"
              class="form-control"
              disabled
              :value="form.operateurMatricule ? 'Auto-détecté' : '-'"
            />
            <small class="text-muted">Primaire si 1ère, Secondaire sinon</small>
          </div>
        </div>

        <div class="alert alert-info mb-3">
          <strong>ℹ️ Info:</strong> La première formation sera primaire. Les suivantes seront
          parallèles.
        </div>

        <div class="d-flex gap-2 justify-content-end">
          <button type="button" @click="resetForm" class="btn btn-secondary">Réinitialiser</button>
          <button type="submit" class="btn btn-primary" :disabled="loading">
            <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
            Initialiser
          </button>
        </div>
      </form>

      <div
        v-if="message"
        :class="['alert', messageType === 'success' ? 'alert-success' : 'alert-danger', 'mt-3']"
      >
        {{ message }}
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'TrainingInitializationForm',
  data() {
    return {
      form: {
        operateurMatricule: '',
        projetId: '',
        posteId: '',
      },
      operateurs: [],
      projets: [],
      postes: [],
      loading: false,
      message: '',
      messageType: '',
    }
  },
  computed: {
    postesFiltered() {
      return this.postes.filter((p) => p.zone?.projet?.idProjet == this.form.projetId)
    },
  },
  methods: {
    async loadOperateurs() {
      try {
        const response = await axios.get('/api/operateurs')
        this.operateurs = response.data
      } catch (error) {
        console.error('Erreur chargement opérateurs:', error)
      }
    },
    async loadProjets() {
      try {
        const response = await axios.get('/api/projets')
        this.projets = response.data
      } catch (error) {
        console.error('Erreur chargement projets:', error)
      }
    },
    async loadPostes() {
      try {
        const response = await axios.get('/api/postes')
        this.postes = response.data
      } catch (error) {
        console.error('Erreur chargement postes:', error)
      }
    },
    async submitTraining() {
      if (!this.form.operateurMatricule || !this.form.projetId || !this.form.posteId) {
        this.message = 'Veuillez remplir tous les champs'
        this.messageType = 'error'
        return
      }

      this.loading = true
      this.message = ''

      try {
        const response = await axios.post('/api/formations/initialize', {
          operateurMatricule: this.form.operateurMatricule,
          posteId: parseInt(this.form.posteId),
          projetId: parseInt(this.form.projetId),
        })

        this.message = `✅ Formation initialisée (ID: ${response.data.idAffectation})`
        this.messageType = 'success'
        this.resetForm()
        this.$emit('training-created', response.data)
      } catch (error) {
        this.message = `❌ ${error.response?.data?.message || error.message}`
        this.messageType = 'error'
      } finally {
        this.loading = false
      }
    },
    resetForm() {
      this.form = { operateurMatricule: '', projetId: '', posteId: '' }
      this.message = ''
    },
  },
  mounted() {
    this.loadOperateurs()
    this.loadProjets()
    this.loadPostes()
  },
}
</script>
