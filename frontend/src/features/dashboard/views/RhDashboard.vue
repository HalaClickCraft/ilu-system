<script setup>
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import {
  fetchAllOperators,
  updateOperatorStatus,
} from '@/features/dashboard/services/operateurService'
import StatisticsDashboard from '@/features/formations/components/StatisticsDashboard.vue'

const authStore = useAuthStore()

const operators = ref([])
const loading = ref(false)
const error = ref('')
const showFormations = ref(false)
const searchMatricule = ref('')

// Statut réel du backend -> libellé lisible + couleur du badge.
// (NOUVELLE_RECRUE existe côté backend dès la création d'un opérateur mais
// n'était jamais géré ici : il tombait dans le "else" et s'affichait donc
// comme "Suspendu", ce qui n'a aucun sens pour une nouvelle recrue.)
const STATUS_META = {
  NOUVELLE_RECRUE: { label: 'Nouvelle Recrue', class: 'pending' },
  Actif: { label: 'Actif', class: 'active' },
  'En Formation': { label: 'En Formation', class: 'training' },
  Suspendu: { label: 'Suspendu', class: 'suspended' },
  Sorti: { label: 'Sorti', class: 'exited' },
}

function statusMeta(statut) {
  return STATUS_META[statut] || { label: statut || '—', class: 'pending' }
}

const filteredOperators = computed(() => {
  const query = searchMatricule.value.trim().toLowerCase()
  if (!query) return operators.value
  return operators.value.filter((op) => op.matricule?.toLowerCase().includes(query))
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
          <input
            v-model="searchMatricule"
            type="text"
            class="search-input"
            placeholder="🔎 Rechercher par matricule..."
          />
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
                <td>{{ op.formationRework ? '✅ Oui' : '❌ Non' }}</td>
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
                    <option value="Suspendu">Suspendu</option>
                    <option value="Sorti">Sorti</option>
                  </select>
                </td>
              </tr>
              <tr v-if="filteredOperators.length === 0">
                <td colspan="8" style="text-align: center; color: #547174">
                  Aucun opérateur ne correspond à ce matricule.
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Skills Matrix (Polyvalence View) -->
      <div class="panel-card">
        <div class="panel-header">
          <h3>Matrice de Polyvalence (Postes / Compétences)</h3>
        </div>
        <div class="skills-matrix-sim">
          <p class="subtitle" style="font-size: 0.9rem; color: #547174">
            Indicateur de compétence d'après le diagramme de classe (SessionEvaluation ->
            niveauObtenu)
          </p>
          <div class="matrix-grid">
            <div class="matrix-row header-row">
              <span class="header-cell">Opérateur</span>
              <span class="header-cell">Poste Assemblage</span>
              <span class="header-cell">Poste Vissage</span>
              <span class="header-cell">Finition</span>
            </div>
            <div class="matrix-row">
              <span class="cell-name">Amine Ben Ali</span>
              <span class="cell-level lvl-4">Niveau 4 (Correct)</span>
              <span class="cell-level lvl-3">Niveau 3</span>
              <span class="cell-level lvl-1">Niveau 1</span>
            </div>
            <div class="matrix-row">
              <span class="cell-name">Salma Mansour</span>
              <span class="cell-level lvl-2">Niveau 2</span>
              <span class="cell-level lvl-4">Niveau 4 (Correct)</span>
              <span class="cell-level lvl-3">Niveau 3</span>
            </div>
            <div class="matrix-row">
              <span class="cell-name">Youssef Trabelsi</span>
              <span class="cell-level lvl-1">Niveau 1</span>
              <span class="cell-level lvl-1">Niveau 1</span>
              <span class="cell-level lvl-2">Niveau 2</span>
            </div>
          </div>
          <div class="matrix-legend">
            <span class="leg-item"><span class="color-box lvl-1"></span> Niveau 1 (Débutant)</span>
            <span class="leg-item"
              ><span class="color-box lvl-2"></span> Niveau 2 (Intermédiaire)</span
            >
            <span class="leg-item"><span class="color-box lvl-3"></span> Niveau 3 (Autonome)</span>
            <span class="leg-item"
              ><span class="color-box lvl-4"></span> Niveau 4 (Expert/Formateur)</span
            >
          </div>
        </div>
      </div>
    </div>
    <!-- End Operators View -->

    <!-- Formations Statistics View -->
    <StatisticsDashboard v-show="showFormations" />
    <!-- End Formations Statistics View -->
  </section>
</template>