<script setup>
import { ref, onMounted, computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { fetchStructure } from '@/features/structure/services/structureService'
import {
  fetchAllOperators,
  assignPoste
} from '@/features/dashboard/services/operateurService'

const authStore = useAuthStore()

// Real states
const operators = ref([])
const structure = ref({ projects: [] })
const loading = ref(false)
const error = ref('')

const selectedOperator = ref('')
const selectedPoste = ref('')
const assignLoading = ref(false)
const assignMsg = ref('')

// Simulated sessions
const sessions = ref([
  { id: 1, type: 'Hebdomadaire', date: '2026-07-14', statut: 'En cours', score: 85.5 },
  { id: 2, type: 'Mensuelle', date: '2026-06-30', statut: 'Clôturée', score: 91.2 },
])

// Flat list of all workstations in the system
const allPostes = computed(() => {
  const list = []
  if (!structure.value.projects) return list
  for (const project of structure.value.projects) {
    if (!project.zones) continue
    for (const zone of project.zones) {
      if (!zone.postes) continue
      for (const poste of zone.postes) {
        list.push({
          id: poste.idPoste,
          nom: `${project.nom} - ${zone.nom} - ${poste.nom}`
        })
      }
    }
  }
  return list
})

// Operators who currently have a workstation assigned
const assignedOperators = computed(() => {
  return operators.value.filter(op => op.posteAffecte)
})

async function loadData() {
  loading.value = true
  error.value = ''
  try {
    const [opsData, structData] = await Promise.all([
      fetchAllOperators(authStore.token),
      fetchStructure(authStore.token)
    ])
    operators.value = opsData
    structure.value = structData
  } catch (err) {
    error.value = err.message
  } finally {
    loading.value = false
  }
}

async function handleAssign() {
  if (!selectedOperator.value) return
  assignLoading.value = true
  assignMsg.value = ''
  try {
    const pId = selectedPoste.value ? Number(selectedPoste.value) : null
    await assignPoste(authStore.token, selectedOperator.value, pId)
    assignMsg.value = 'Affectation mise à jour avec succès!'
    selectedOperator.value = ''
    selectedPoste.value = ''
    await loadData()
  } catch (err) {
    assignMsg.value = `Erreur: ${err.message}`
  } finally {
    assignLoading.value = false
  }
}

onMounted(loadData)
</script>

<template>
  <section class="role-section">
    <div class="stats-grid">
      <div class="stat-card">
        <span class="stat-icon">🔄</span>
        <div class="stat-content">
          <span class="stat-val">1</span>
          <span class="stat-lbl">Session d'évaluation active</span>
        </div>
      </div>
      <div class="stat-card">
        <span class="stat-icon">🛠️</span>
        <div class="stat-content">
          <span class="stat-val">{{ allPostes.length }}</span>
          <span class="stat-lbl">Postes de travail configurés</span>
        </div>
      </div>
      <div class="stat-card">
        <span class="stat-icon">👷</span>
        <div class="stat-content">
          <span class="stat-val">{{ assignedOperators.length }} / {{ operators.length }}</span>
          <span class="stat-lbl">Opérateurs affectés</span>
        </div>
      </div>
    </div>

    <!-- Workstation assignment form and active assignments -->
    <div class="admin-grid" style="margin-top: 1.5rem;">
      <div class="panel-card">
        <div class="panel-header">
          <h3>Affecter un Opérateur à un Poste</h3>
        </div>
        <form @submit.prevent="handleAssign" class="panel-form">
          <div class="input-group">
            <label>Opérateur</label>
            <select v-model="selectedOperator" required>
              <option value="" disabled>-- Choisir un opérateur --</option>
              <option v-for="op in operators" :key="op.matricule" :value="op.matricule">
                {{ op.nom }} ({{ op.matricule }}) - {{ op.statut }}
              </option>
            </select>
          </div>
          <div class="input-group">
            <label>Poste de travail</label>
            <select v-model="selectedPoste">
              <option value="">-- Libérer le poste (Désaffecter) --</option>
              <option v-for="p in allPostes" :key="p.id" :value="p.id">
                {{ p.nom }}
              </option>
            </select>
          </div>
          <button type="submit" :disabled="assignLoading" class="submit-btn">
            {{ assignLoading ? 'Affectation...' : 'Valider l\'affectation' }}
          </button>
          <p v-if="assignMsg" class="form-msg">{{ assignMsg }}</p>
        </form>
      </div>

      <div class="panel-card">
        <div class="panel-header">
          <h3>Affectations Actives aux Postes</h3>
          <button @click="loadData" class="refresh-btn">🔄 Actualiser</button>
        </div>
        <div v-if="loading" class="loading-state">
          <span class="spinner-blue"></span> Chargement...
        </div>
        <div v-else-if="error" class="error-state">⚠️ {{ error }}</div>
        <div v-else class="table-wrapper">
          <table class="data-table">
            <thead>
              <tr>
                <th>Opérateur</th>
                <th>Matricule</th>
                <th>Poste Affecté</th>
                <th>Équipe</th>
                <th>Statut</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="op in assignedOperators" :key="op.matricule">
                <td><strong>{{ op.nom }}</strong></td>
                <td><code>{{ op.matricule }}</code></td>
                <td><span class="role-badge">{{ op.posteAffecte.nom }}</span></td>
                <td>{{ op.equipe ? op.equipe.nom : '—' }}</td>
                <td>
                  <span :class="['status-badge', op.statut === 'Actif' ? 'active' : 'suspended']">
                    {{ op.statut }}
                  </span>
                </td>
              </tr>
              <tr v-if="assignedOperators.length === 0">
                <td colspan="5" style="text-align: center; color: #547174;">
                  Aucun opérateur n'est affecté actuellement.
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- Active sessions (simulated) -->
    <div class="panel-card" style="margin-top: 1.5rem;">
      <div class="panel-header">
        <h3>Sessions d'Évaluation (SessionEvaluation)</h3>
      </div>
      <div class="table-wrapper">
        <table class="data-table">
          <thead>
            <tr>
              <th>ID Session</th>
              <th>Type</th>
              <th>Date</th>
              <th>Score Global Moyen</th>
              <th>Statut</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="s in sessions" :key="s.id">
              <td><code>#00{{ s.id }}</code></td>
              <td>{{ s.type }}</td>
              <td>{{ s.date }}</td>
              <td><strong>{{ s.score }}%</strong></td>
              <td>
                <span :class="['status-badge', s.statut === 'En cours' ? 'active' : 'suspended']">
                  {{ s.statut }}
                </span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </section>
</template>
