<script setup>
import { ref, onMounted, computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { fetchStructure } from '@/features/structure/services/structureService'
import { fetchAllOperators, assignPoste } from '@/features/dashboard/services/operateurService'
import StatisticsDashboard from '@/features/formations/components/StatisticsDashboard.vue'

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

// Show formations stats view
const showFormations = ref(false)

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
          nom: `${project.nom} - ${zone.nom} - ${poste.nom}`,
        })
      }
    }
  }
  return list
})

// Operators who currently have a workstation assigned
const assignedOperators = computed(() => {
  return operators.value.filter((op) => op.posteAffecte)
})

async function loadData() {
  loading.value = true
  error.value = ''
  try {
    const [opsData, structData] = await Promise.all([
      fetchAllOperators(authStore.token),
      fetchStructure(authStore.token),
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
    <!-- Tabs Navigation -->
    <div class="tabs-navigation" style="margin-bottom: 1.5rem">
      <button 
        @click="showFormations = false"
        :class="['tab-btn', !showFormations ? 'active' : '']"
      >
        🏭 Affectations aux Postes
      </button>
      <button 
        @click="showFormations = true"
        :class="['tab-btn', showFormations ? 'active' : '']"
      >
        📊 Statistiques Formations
      </button>
    </div>

    <!-- Workstations Assignment -->
    <div v-show="!showFormations" class="admin-grid" style="margin-top: 1.5rem">
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
            {{ assignLoading ? 'Affectation...' : "Valider l'affectation" }}
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
                <td>
                  <strong>{{ op.nom }}</strong>
                </td>
                <td>
                  <code>{{ op.matricule }}</code>
                </td>
                <td>
                  <span class="role-badge">{{ op.posteAffecte.nom }}</span>
                </td>
                <td>{{ op.equipe ? op.equipe.nom : '—' }}</td>
                <td>
                  <span :class="['status-badge', op.statut === 'Actif' ? 'active' : 'suspended']">
                    {{ op.statut }}
                  </span>
                </td>
              </tr>
              <tr v-if="assignedOperators.length === 0">
                <td colspan="5" style="text-align: center; color: #547174">
                  Aucun opérateur n'est affecté actuellement.
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
    <!-- End Workstations Tab -->

    <!-- Formations Statistics Tab -->
    <StatisticsDashboard v-show="showFormations" />
    <!-- End Formations Statistics Tab -->
  </section>
</template>
