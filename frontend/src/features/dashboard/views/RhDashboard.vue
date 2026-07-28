<script setup>
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import {
  fetchAllOperators,
  updateOperatorStatus,
  markOperatorAbsence,
  markOperatorReprise,
} from '@/features/dashboard/services/operateurService'
import StatisticsDashboard from '@/features/formations/components/StatisticsDashboard.vue'

const authStore = useAuthStore()
const operators = ref([])
const loading = ref(false)
const error = ref('')
const showFormations = ref(false)
const searchMatricule = ref('')
const selectedProjet = ref('')
const selectedStatut = ref('')


const STATUS_META = {
  NOUVELLE_RECRUE: { label: 'Nouvelle Recrue', class: 'pending' },
  Actif: { label: 'Actif', class: 'active' },
  'En Formation': { label: 'En Formation', class: 'training' },
  
  Sorti: { label: 'Sorti', class: 'exited' },
  ABSENT: { label: 'Absent', class: 'suspended' },
}
function statusMeta(statut) {
  return STATUS_META[statut] || { label: statut || '—', class: 'pending' }
}

// Projets présents dans la liste actuelle (pour peupler le filtre).
const availableProjets = computed(() => {
  const names = new Set()
  for (const op of operators.value) {
    const nom = op.posteAffecte?.zone?.projet?.nom
    if (nom) names.add(nom)
  }
  return Array.from(names).sort((a, b) => a.localeCompare(b))
})

const filteredOperators = computed(() => {
  const query = searchMatricule.value.trim().toLowerCase()
  return operators.value.filter((op) => {
    const matchesMatricule = !query || op.matricule?.toLowerCase().includes(query)
    const matchesProjet =
      !selectedProjet.value || op.posteAffecte?.zone?.projet?.nom === selectedProjet.value
    const matchesStatut = !selectedStatut.value || op.statut === selectedStatut.value
    return matchesMatricule && matchesProjet && matchesStatut
  })
})


function formatDate(value) {
  if (!value) return '—'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return date.toLocaleDateString('fr-FR')
}

async function loadOperators() {
  loading.value = true
  error.value = ''
  try {
    const data = await fetchAllOperators(authStore.token)
    operators.value = data
  } catch (err) {
    error.value = err.message
  } finally {
    loading.value = false
  }
}

async function handleStatusChange(op, newStatus) {
  try {
    await updateOperatorStatus(authStore.token, op.matricule, newStatus)
    await loadOperators()
  } catch (err) {
    alert(`Erreur lors du changement de statut: ${err.message}`)
  }
}

async function handleMarkAbsence(op, motif) {
  if (!motif) return
  try {
    await markOperatorAbsence(authStore.token, op.matricule, motif)
    await loadOperators()
  } catch (err) {
    alert(`Erreur lors du signalement de l'absence: ${err.message}`)
  }
}

async function handleMarkReprise(op) {
  try {
    await markOperatorReprise(authStore.token, op.matricule)
    await loadOperators()
  } catch (err) {
    alert(`Erreur lors du signalement de la reprise: ${err.message}`)
  }
}

onMounted(loadOperators)
</script>

<template>
  <section class="role-section">
    <!-- Toggle Between Operators and Formations -->
    <div class="tabs-navigation" style="margin-bottom: 1.5rem">
      <button 
        @click="showFormations = false"
        :class="['tab-btn', !showFormations ? 'active' : '']"
      >
        👥 Annuaire des Opérateurs
      </button>
      <button 
        @click="showFormations = true"
        :class="['tab-btn', showFormations ? 'active' : '']"
      >
        📊 Statistiques Formations
      </button>
    </div>

    <!-- Operators View -->
    <div v-show="!showFormations" class="admin-grid" style="margin-top: 1.5rem">
      <!-- Operator directory -->
    <div class="panel-card list-users-card">
        <div class="panel-header">
          <h3>Annuaire des Opérateurs</h3>
          <div style="display: flex; gap: 0.6rem; align-items: center; flex-wrap: wrap">
            <select v-model="selectedProjet" class="search-input" style="min-width: 160px">
              <option value="">Tous les projets</option>
              <option v-for="projet in availableProjets" :key="projet" :value="projet">
                {{ projet }}
              </option>
            </select>
            <select v-model="selectedStatut" class="search-input" style="min-width: 160px">
              <option value="">Tous les statuts</option>
              <option value="NOUVELLE_RECRUE">Nouvelle Recrue</option>
              <option value="Actif">Actif</option>
              <option value="En Formation">En Formation</option>
              
              <option value="Sorti">Sorti</option>
              <option value="ABSENT">Absent</option>
            </select>
            <input
              v-model="searchMatricule"
              type="text"
              class="search-input"
              placeholder="🔎 Rechercher par matricule..."
            />
          </div>
        </div>
        <div v-if="loading" class="loading-state">
          <span class="spinner-blue"></span> Chargement des opérateurs...
        </div>
        <div v-else-if="error" class="error-state">⚠️ {{ error }}</div>
        <div v-else class="table-wrapper">
          <table class="data-table">
            <thead>
              <tr>
                <th>Matricule</th>
                <th>Nom</th>
                <th>Date d'embauche</th>
                <th>Date de sortie</th>
                <th>Formation Rework</th>
                <th>Projet</th>
                <th>Poste</th>
                <th>Statut</th>
                <th>Arrêt maladie / Reprise</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="op in filteredOperators" :key="op.matricule">
                <td>
                  <code>{{ op.matricule }}</code>
                </td>
                <td>
                  <strong>{{ op.nom }}</strong>
                </td>
                <td>{{ formatDate(op.dateEmbauche) }}</td>
                <td>{{ formatDate(op.dateSortie) }}</td>
                <td>{{ op.posteAffecte?.zone?.projet?.nom || '—' }}</td>
                <td>{{ op.posteAffecte?.nom || '—' }}</td>
                       <td>
                  <select
                    :value="op.statut"
                    :class="['status-select', statusMeta(op.statut).class]"
                    @change="handleStatusChange(op, $event.target.value)"
                  >
                    <option value="NOUVELLE_RECRUE">Nouvelle Recrue</option>
                    <option value="Actif">Actif</option>
                    <option value="En Formation">En Formation</option>
                    
                    <option value="Sorti">Sorti</option>
                    <option value="ABSENT">Absent</option>
                  </select>
                </td>
                <td>
                  <template v-if="op.statut === 'ABSENT'">
                   
                    <button
                      class="submit-btn"
                      style="padding: 0.25rem 0.6rem; font-size: 0.8rem; height: auto"
                      @click="handleMarkReprise(op)"
                    >
                      Marquer la reprise
                    </button>
                  </template>
                  <select
                    v-else
                    :value="''"
                    class="status-select pending"
                    @change="handleMarkAbsence(op, $event.target.value); $event.target.value = ''"
                  >
                    <option value="" disabled>Signaler une absence…</option>
                    <option value="MALADIE">Arrêt maladie</option>
                    <option value="ACCOUCHEMENT">Arrêt accouchement</option>
                  </select>
                </td>
              </tr>
              <tr v-if="filteredOperators.length === 0">
                <td colspan="9" style="text-align: center; color: #547174">
                  Aucun opérateur ne correspond à ce matricule.
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
    <!-- End Operators View -->

    <!-- Formations Statistics View -->
    <StatisticsDashboard v-show="showFormations" />
    <!-- End Formations Statistics View -->
  </section>
</template>