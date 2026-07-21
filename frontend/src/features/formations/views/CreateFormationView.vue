<template>
  <div class="create-formation-container">
    <div class="page-header mb-4">
      <h2>Créer une Nouvelle Formation</h2>
      <p class="text-muted">Créez une assignation de formation pour un opérateur sur un poste</p>
    </div>

    <div class="card">
      <div class="card-body">
        <form @submit.prevent="submitForm">
          <!-- Operateur Selection -->
          <div class="mb-4">
            <label for="operateurSelect" class="form-label">Opérateur *</label>
            <select
              id="operateurSelect"
              v-model.number="formData.operateurId"
              class="form-select form-select-lg"
              :disabled="loadingOperateurs"
              required
            >
              <option value="">-- Sélectionner un Opérateur --</option>
              <option v-for="op in operateurs" :key="op.idOperateur" :value="op.idOperateur">
                {{ op.matricule }} - {{ op.nom }} {{ op.prenom }}
              </option>
            </select>
          </div>

          <!-- Poste Selection -->
          <div class="mb-4">
            <label for="posteSelect" class="form-label">Poste *</label>
            <select
              id="posteSelect"
              v-model.number="formData.posteId"
              class="form-select form-select-lg"
              :disabled="loadingPostes"
              @change="updatePosteInfo"
              required
            >
              <option value="">-- Sélectionner un Poste --</option>
              <option v-for="poste in postes" :key="poste.idPoste" :value="poste.idPoste">
                {{ poste.nom }} (Cadence: {{ poste.cadenceObjectif }} pièces/jour)
              </option>
            </select>
          </div>

          <!-- Formation Type -->
          <div class="mb-4">
            <label for="typeSelect" class="form-label">Type d'Affectation *</label>
            <select
              id="typeSelect"
              v-model="formData.type"
              class="form-select form-select-lg"
              required
            >
              <option value="">-- Sélectionner le Type --</option>
              <option value="primary">Affectation Primaire</option>
              <option value="secondary">Affectation Secondaire</option>
            </select>
            <small class="form-text text-muted">
              Primaire: Poste principal où l'opérateur va travailler | Secondaire: Formation
              complémentaire
            </small>
          </div>

          <!-- Project Selection -->
          <div class="mb-4">
            <label for="projectSelect" class="form-label">Projet *</label>
            <select
              id="projectSelect"
              v-model.number="formData.projetId"
              class="form-select form-select-lg"
              :disabled="loadingProjects"
              required
            >
              <option value="">-- Sélectionner un Projet --</option>
              <option v-for="project in projects" :key="project.idProjet" :value="project.idProjet">
                {{ project.nom }}
              </option>
            </select>
          </div>

          <!-- Formation Objectives Section -->
          <div class="card bg-light mb-4">
            <div class="card-header bg-primary text-white">
              <h5 class="mb-0">📚 Objectifs de Formation</h5>
            </div>
            <div class="card-body">
              <!-- Template Status -->
              <div v-if="templateExists" class="alert alert-info mb-3">
                <strong>✅ Template Existant:</strong> Ce poste a déjà un template. Les objectifs
                ci-dessous seront utilisés.
              </div>
              <div v-else class="alert alert-warning mb-3">
                <strong>⚠️ Nouveau Template:</strong> Ce poste n'a pas encore de template. Vous
                devez définir les objectifs ci-dessous.
              </div>

              <!-- Cadence Objective -->
              <div class="mb-3">
                <label for="cadenceObjectif" class="form-label">
                  <strong>Cadence Objectif (pièces/jour) *</strong>
                </label>
                <div class="input-group">
                  <input
                    id="cadenceObjectif"
                    v-model.number="formData.cadenceObjectif"
                    type="number"
                    class="form-control form-control-lg"
                    min="1"
                    max="500"
                    required
                    placeholder="Ex: 120"
                  />
                  <span class="input-group-text">pièces/jour</span>
                </div>
                <small class="form-text text-muted">
                  Nombre de pièces que l'opérateur doit produire par jour
                </small>
              </div>

              <!-- Quality Objective -->
              <div class="mb-3">
                <label for="qualiteObjectif" class="form-label">
                  <strong>Objectif Qualité (défauts max) *</strong>
                </label>
                <input
                  id="qualiteObjectif"
                  v-model="formData.qualiteObjectif"
                  type="text"
                  class="form-control form-control-lg"
                  required
                  placeholder="Ex: < 7 defauts en 12 jours"
                />
                <small class="form-text text-muted">
                  Descriptif des objectifs de qualité (ex: "< 7 defauts", "Zero defect", etc.)
                </small>
              </div>

              <!-- Save as Template Checkbox (only for new templates) -->
              <div v-if="!templateExists" class="form-check mb-3">
                <input
                  id="saveAsTemplate"
                  v-model="formData.saveAsTemplate"
                  type="checkbox"
                  class="form-check-input"
                />
                <label for="saveAsTemplate" class="form-check-label">
                  <strong>💾 Sauvegarder comme template pour ce poste</strong>
                  <br />
                  <small class="text-muted">
                    Les formations futures sur ce poste utiliseront automatiquement ces objectifs
                  </small>
                </label>
              </div>

              <!-- Display Current Template Info -->
              <div v-if="selectedPosteInfo" class="alert alert-secondary mt-3">
                <div class="row">
                  <div class="col-md-6">
                    <small class="text-muted">Template Cadence:</small>
                    <div class="fw-bold">{{ selectedPosteInfo.cadenceObjectif }} pièces/jour</div>
                  </div>
                  <div class="col-md-6">
                    <small class="text-muted">Template Quality:</small>
                    <div class="fw-bold">
                      {{ selectedPosteInfo.qualiteObjectif || 'Non défini' }}
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Submit Buttons -->
          <div class="d-flex gap-2 mt-4">
            <button type="submit" class="btn btn-primary btn-lg" :disabled="submitting">
              <span v-if="submitting" class="spinner-border spinner-border-sm me-2"></span>
              {{ submitting ? 'Création en cours...' : 'Créer la Formation' }}
            </button>
            <router-link to="/formations" class="btn btn-secondary btn-lg">Annuler</router-link>
          </div>
        </form>
      </div>
    </div>

    <!-- Success Message -->
    <div v-if="successMessage" class="alert alert-success mt-4" role="alert">
      {{ successMessage }}
      <router-link :to="`/formations/tracking/${newFormationId}`" class="alert-link ms-2">
        Voir le suivi de formation
      </router-link>
    </div>

    <!-- Error Message -->
    <div v-if="errorMessage" class="alert alert-danger mt-4" role="alert">
      {{ errorMessage }}
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()

const loading = ref(true)
const loadingOperateurs = ref(true)
const loadingPostes = ref(true)
const loadingProjects = ref(true)
const submitting = ref(false)

const operateurs = ref([])
const postes = ref([])
const projects = ref([])
const selectedPosteInfo = ref(null)

const successMessage = ref('')
const errorMessage = ref('')
const newFormationId = ref(null)
const templateExists = ref(false)

const formData = ref({
  operateurId: '',
  posteId: '',
  type: '',
  projetId: '',
  cadenceObjectif: null,
  qualiteObjectif: '',
  saveAsTemplate: false,
})

const resetForm = () => {
  formData.value = {
    operateurId: '',
    posteId: '',
    type: '',
    projetId: '',
    cadenceObjectif: null,
    qualiteObjectif: '',
    saveAsTemplate: false,
  }
  selectedPosteInfo.value = null
  templateExists.value = false
  successMessage.value = ''
  errorMessage.value = ''
}

const loadOperateurs = async () => {
  try {
    const response = await axios.get('/api/operateurs')
    operateurs.value = response.data
  } catch (err) {
    console.error('Erreur lors du chargement des opérateurs', err)
  } finally {
    loadingOperateurs.value = false
  }
}

const loadPostes = async () => {
  try {
    const response = await axios.get('/api/structure/postes')
    postes.value = response.data
  } catch (err) {
    console.error('Erreur lors du chargement des postes', err)
  } finally {
    loadingPostes.value = false
  }
}

const loadProjects = async () => {
  try {
    const response = await axios.get('/api/structure/projets')
    projects.value = response.data
  } catch (err) {
    console.error('Erreur lors du chargement des projets', err)
  } finally {
    loadingProjects.value = false
  }
}

const updatePosteInfo = async () => {
  try {
    const response = await axios.get(`/api/formations/templates/${formData.value.posteId}`)
    templateExists.value = true
    selectedPosteInfo.value = {
      cadenceObjectif: response.data.cadenceObjectif,
      qualiteObjectif: response.data.qualiteObjectifTexte,
    }
    // Auto-fill form with template values
    formData.value.cadenceObjectif = response.data.cadenceObjectif
    formData.value.qualiteObjectif = response.data.qualiteObjectifTexte
    formData.value.saveAsTemplate = false // Already saved
  } catch (err) {
    // No template exists - user needs to define objectives
    templateExists.value = false
    const poste = postes.value.find((p) => p.idPoste === formData.value.posteId)
    if (poste) {
      selectedPosteInfo.value = {
        cadenceObjectif: poste.cadenceObjectif,
        qualiteObjectif: 'À définir',
      }
      // Pre-fill with poste default cadence, but leave quality empty for user to enter
      formData.value.cadenceObjectif = poste.cadenceObjectif
      formData.value.qualiteObjectif = ''
      formData.value.saveAsTemplate = true // Suggest saving as template
    }
  }
}

const submitForm = async () => {
  if (
    !formData.value.operateurId ||
    !formData.value.posteId ||
    !formData.value.type ||
    !formData.value.projetId
  ) {
    errorMessage.value = 'Tous les champs sont obligatoires'
    return
  }

  if (!formData.value.cadenceObjectif || !formData.value.qualiteObjectif) {
    errorMessage.value = 'Les objectifs de formation (cadence et qualité) sont obligatoires'
    return
  }

  try {
    submitting.value = true
    errorMessage.value = ''
    successMessage.value = ''

    // Step 1: Create or update template if needed
    if (formData.value.saveAsTemplate || !templateExists.value) {
      try {
        await axios.post('/api/formations/templates', {
          posteId: formData.value.posteId,
          cadenceObjectif: formData.value.cadenceObjectif,
          qualiteObjectifTexte: formData.value.qualiteObjectif,
        })
      } catch (err) {
        console.warn('Template creation/update issue (may already exist):', err.message)
      }
    }

    // Step 2: Get the operator to find their matricule
    const operator = operateurs.value.find((op) => op.idOperateur === formData.value.operateurId)
    if (!operator) {
      throw new Error('Opérateur non trouvé')
    }

    // Step 3: Create the formation
    const response = await axios.post('/api/formations/initialize', {
      operateurMatricule: operator.matricule,
      posteId: formData.value.posteId,
      projetId: formData.value.projetId,
    })

    newFormationId.value = response.data.idAffectation
    const templateMsg = formData.value.saveAsTemplate ? ' et template sauvegardé' : ''
    successMessage.value = `Formation créée avec succès${templateMsg}!`

    // Reset form after 2 seconds
    setTimeout(() => {
      resetForm()
    }, 2000)
  } catch (err) {
    errorMessage.value =
      'Erreur lors de la création de la formation: ' + (err.response?.data?.message || err.message)
    console.error(err)
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  await Promise.all([loadOperateurs(), loadPostes(), loadProjects()])
  loading.value = false
})
</script>

<style scoped>
.create-formation-container {
  background-color: #f8f9fa;
  padding: 2rem;
  min-height: 100vh;
}

.page-header h2 {
  color: #333;
  font-weight: 600;
}

.card {
  box-shadow: 0 0.125rem 0.25rem rgba(0, 0, 0, 0.075);
  border: none;
  max-width: 600px;
  margin: 0 auto;
}

.form-select-lg {
  font-size: 1rem;
  padding: 0.75rem 1rem;
}

.alert {
  margin-top: 1.5rem;
}
</style>
