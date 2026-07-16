<script setup>
import { ref, onMounted, computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import {
  fetchAllOperators,
  updateOperatorStatus
} from '@/features/dashboard/services/operateurService'

const authStore = useAuthStore()

const operators = ref([])
const loading = ref(false)
const error = ref('')

const totalOperators = computed(() => operators.value.length)
const formationCount = computed(() => operators.value.filter(op => op.statut === 'En Formation').length)

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
    <!-- Stats -->
    <div class="stats-grid">
      <div class="stat-card">
        <span class="stat-icon">👔</span>
        <div class="stat-content">
          <span class="stat-val">{{ totalOperators }}</span>
          <span class="stat-lbl">Opérateurs enregistrés</span>
        </div>
      </div>
      <div class="stat-card">
        <span class="stat-icon">🏆</span>
        <div class="stat-content">
          <span class="stat-val">78%</span>
          <span class="stat-lbl">Taux de polyvalence cible</span>
        </div>
      </div>
      <div class="stat-card">
        <span class="stat-icon">🎓</span>
        <div class="stat-content">
          <span class="stat-val">{{ formationCount }}</span>
          <span class="stat-lbl">Opérateurs en formation</span>
        </div>
      </div>
    </div>

    <div class="admin-grid" style="margin-top: 1.5rem;">
      <!-- Operator directory -->
      <div class="panel-card list-users-card">
        <div class="panel-header">
          <h3>Annuaire des Opérateurs</h3>
          <button @click="loadOperators" class="refresh-btn">🔄 Actualiser</button>
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
                <th>Équipe</th>
                <th>Statut</th>
                <th>Gérer cycle de vie</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="op in operators" :key="op.matricule">
                <td><code>{{ op.matricule }}</code></td>
                <td><strong>{{ op.nom }}</strong></td>
                <td>{{ op.dateEmbauche }}</td>
                <td>{{ op.dateSortie || '—' }}</td>
                <td>{{ op.formationRework ? '✅ Oui' : '❌ Non' }}</td>
                <td>{{ op.equipe ? op.equipe.nom : '—' }}</td>
                <td>
                  <span :class="['status-badge', op.statut === 'Actif' ? 'active' : 'suspended']">
                    {{ op.statut }}
                  </span>
                </td>
                <td>
                  <select
                    :value="op.statut"
                    @change="handleStatusChange(op, $event.target.value)"
                    style="padding: 0.25rem 0.5rem; font-size: 0.85rem;"
                  >
                    <option value="Actif">Actif</option>
                    <option value="En Formation">En Formation</option>
                    <option value="Suspendu">Suspendu</option>
                    <option value="Sorti">Sorti</option>
                  </select>
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
          <p class="subtitle" style="font-size: 0.9rem; color: #547174;">
            Indicateur de compétence d'après le diagramme de classe (SessionEvaluation -> niveauObtenu)
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
            <span class="leg-item"><span class="color-box lvl-2"></span> Niveau 2 (Intermédiaire)</span>
            <span class="leg-item"><span class="color-box lvl-3"></span> Niveau 3 (Autonome)</span>
            <span class="leg-item"><span class="color-box lvl-4"></span> Niveau 4 (Expert/Formateur)</span>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>
