<template>
  <div class="formation-templates-container">
    <div class="page-header mb-4">
      <div class="d-flex justify-content-between align-items-center">
        <h2>Gestion des Modèles de Formation</h2>
        <button @click="openCreateModal" class="btn btn-primary">
          <i class="bi bi-plus-circle"></i> Créer un Modèle
        </button>
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
              <th>Poste</th>
              <th>Cadence Objectif</th>
              <th>Objectif Qualité</th>
              <th>Crée par</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="template in templates" :key="template.idTemplate">
              <td>{{ template.poste.nom }}</td>
              <td class="text-center">{{ template.cadenceObjectif }} pièces/jour</td>
              <td>{{ template.qualiteObjectifTexte }}</td>
              <td>{{ template.creePar }}</td>
              <td>
                <button @click="editTemplate(template)" class="btn btn-sm btn-warning me-2">
                  <i class="bi bi-pencil"></i>
                </button>
                <button @click="deleteTemplate(template.idTemplate)" class="btn btn-sm btn-danger">
                  <i class="bi bi-trash"></i>
                </button>
              </td>
            </tr>
            <tr v-if="templates.length === 0">
              <td colspan="5" class="text-center text-muted">Aucun modèle de formation</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Create/Edit Modal -->
    <div v-if="showModal" class="modal show d-block" tabindex="-1">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">
              {{ isEditMode ? 'Modifier le Modèle de Formation' : 'Créer un Modèle de Formation' }}
            </h5>
            <button type="button" class="btn-close" @click="closeModal"></button>
          </div>
          <div class="modal-body">
            <form @submit.prevent="submitForm">
              <div class="mb-3">
                <label for="postSelect" class="form-label">Poste *</label>
                <select
                  id="postSelect"
                  v-model.number="formData.posteId"
                  class="form-select"
                  :disabled="isEditMode || loadingPostes"
                  required
                >
                  <option value="">-- Sélectionner un Poste --</option>
                  <option v-for="poste in postes" :key="poste.idPoste" :value="poste.idPoste">
                    {{ poste.nom }}
                  </option>
                </select>
              </div>

              <div class="mb-3">
                <label for="cadenceInput" class="form-label">Cadence Objectif (pièces/jour) *</label>
                <input
                  id="cadenceInput"
                  v-model.number="formData.cadenceObjectif"
                  type="number"
                  class="form-control"
                  min="1"
                  max="1000"
                  required
                />
              </div>

              <div class="mb-3">
                <label for="qualiteInput" class="form-label">Objectif Qualité *</label>
                <input
                  id="qualiteInput"
                  v-model="formData.qualiteObjectifTexte"
                  type="text"
                  class="form-control"
                  placeholder="Ex: < 7 défauts en 12 jours"
                  required
                />
                <small class="form-text text-muted">
                  Décrivez l'objectif de qualité (ex: "< 7 défauts en 12 jours")
                </small>
              </div>
            </form>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" @click="closeModal">Annuler</button>
            <button
              type="button"
              class="btn btn-primary"
              @click="submitForm"
              :disabled="submitting"
            >
              <span v-if="submitting" class="spinner-border spinner-border-sm me-2"></span>
              {{ isEditMode ? 'Modifier' : 'Créer' }}
            </button>
          </div>
        </div>
      </div>
    </div>
    <div v-if="showModal" class="modal-backdrop fade show"></div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const loading = ref(true)
const loadingPostes = ref(true)
const error = ref(null)
const templates = ref([])
const postes = ref([])

const showModal = ref(false)
const isEditMode = ref(false)
const submitting = ref(false)

const formData = ref({
  posteId: '',
  cadenceObjectif: 40,
  qualiteObjectifTexte: '< 7 défauts en 12 jours',
})

const resetForm = () => {
  formData.value = {
    posteId: '',
    cadenceObjectif: 40,
    qualiteObjectifTexte: '< 7 défauts en 12 jours',
  }
}

const loadTemplates = async () => {
  try {
    loading.value = true
    const response = await axios.get('/api/formations/templates')
    templates.value = response.data
  } catch (err) {
    error.value = 'Erreur lors du chargement des modèles de formation'
    console.error(err)
  } finally {
    loading.value = false
  }
}

const loadPostes = async () => {
  try {
    loadingPostes.value = true
    const response = await axios.get('/api/structure/postes')
    postes.value = response.data
  } catch (err) {
    console.error('Erreur lors du chargement des postes', err)
  } finally {
    loadingPostes.value = false
  }
}

const openCreateModal = () => {
  isEditMode.value = false
  resetForm()
  showModal.value = true
}

const editTemplate = (template) => {
  isEditMode.value = true
  formData.value = {
    posteId: template.poste.idPoste,
    cadenceObjectif: template.cadenceObjectif,
    qualiteObjectifTexte: template.qualiteObjectifTexte,
  }
  showModal.value = true
}

const closeModal = () => {
  showModal.value = false
  resetForm()
}

const submitForm = async () => {
  try {
    submitting.value = true
    await axios.post('/api/formations/templates', {
      posteId: formData.value.posteId,
      cadenceObjectif: formData.value.cadenceObjectif,
      qualiteObjectifTexte: formData.value.qualiteObjectifTexte,
    })

    closeModal()
    await loadTemplates()
  } catch (err) {
    error.value = 'Erreur lors de la sauvegarde du modèle'
    console.error(err)
  } finally {
    submitting.value = false
  }
}

const deleteTemplate = async (templateId) => {
  if (!confirm('Êtes-vous sûr de vouloir supprimer ce modèle?')) return

  try {
    await axios.delete(`/api/formations/templates/${templateId}`)
    await loadTemplates()
  } catch (err) {
    error.value = 'Erreur lors de la suppression du modèle'
    console.error(err)
  }
}

onMounted(async () => {
  await Promise.all([loadTemplates(), loadPostes()])
})
</script>

<style scoped>
.formation-templates-container {
  background-color: #f8f9fa;
  padding: 2rem;
  min-height: 100vh;
}

.page-header h2 {
  color: #333;
  font-weight: 600;
}

.table {
  background-color: white;
  box-shadow: 0 0.125rem 0.25rem rgba(0, 0, 0, 0.075);
}

.modal.show {
  z-index: 1050;
}

.modal-backdrop.show {
  z-index: 1040;
}
</style>
