<script setup>
import { ref, onMounted, computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { fetchTeamOperators, createOperator } from '@/features/dashboard/services/operateurService'
import { fetchStructure } from '@/features/structure/services/structureService'
import ChefFormationPanel from '@/features/formations/components/ChefFormationPanel.vue'

defineProps({
  activeSection: {
    type: String,
    default: 'dashboard',
  },
})

const authStore = useAuthStore()

// Real operator management state
const operators = ref([])
const structure = ref({ projects: [] })
const opLoading = ref(false)
const opError = ref('')

const newOpMatricule = ref('')
const newOpNom = ref('')
const newOpPrenom = ref('')
const newOpFonctionnalite = ref('')
const newOpDateEmbauche = ref(new Date().toISOString().split('T')[0])
const newOpPosteId = ref('')
const createLoading = ref(false)
const createMsg = ref('')

const allPostes = computed(() => {
  const postes = []
  for (const projet of structure.value.projects || []) {
    for (const zone of projet.zones || []) {
      for (const poste of zone.postes || []) {
        postes.push({ id: poste.idPoste, nom: `${projet.nom} — ${zone.nom} — ${poste.nom}` })
      }
    }
  }
  return postes
})

async function loadData() {
  opLoading.value = true
  opError.value = ''
  try {
    const [opsData, structureData] = await Promise.all([
      fetchTeamOperators(authStore.token),
      fetchStructure(authStore.token),
    ])
    operators.value = opsData
    structure.value = structureData
  } catch (err) {
    opError.value = err.message
  } finally {
    opLoading.value = false
  }
}

async function handleCreateOperator() {
  createMsg.value = ''
  createLoading.value = true
  try {
    await createOperator(authStore.token, {
      matricule: newOpMatricule.value,
      nom: newOpNom.value,
      prenom: newOpPrenom.value,
      fonctionnalite: newOpFonctionnalite.value,
      dateEmbauche: newOpDateEmbauche.value,
      posteId: Number(newOpPosteId.value),
    })
    newOpMatricule.value = ''
    newOpNom.value = ''
    newOpPrenom.value = ''
    newOpFonctionnalite.value = ''
    newOpPosteId.value = ''
    createMsg.value = 'Nouvelle recrue créée : formation de 12 jours démarrée sur le poste choisi.'
    await loadData()
  } catch (err) {
    createMsg.value = `Erreur: ${err.message}`
  } finally {
    createLoading.value = false
  }
}

onMounted(loadData)
</script>

<template>
  <section class="role-section">
    <!-- SUB-VIEW: main dashboard overview -->
    <div
      v-if="activeSection === 'dashboard'"
      style="display: flex; flex-direction: column; gap: 1.5rem"
    >
      <!-- Mon Equipe Table -->
      <div class="panel-card" style="width: 100%">
        <div class="panel-header">
          <h3>Mon Équipe (Opérateurs)</h3>
          <button @click="loadData" class="refresh-btn">🔄 Actualiser</button>
        </div>
        <div v-if="opLoading" class="loading-state">
          <span class="spinner-blue"></span> Chargement des opérateurs...
        </div>
        <div v-else-if="opError" class="error-state">⚠️ {{ opError }}</div>
        <div v-else class="table-wrapper">
          <table class="data-table">
            <thead>
              <tr>
                <th>Matricule</th>
                <th>Nom</th>
                <th>Embauche</th>
                <th>Formation Rework</th>
                <th>Affectation Poste</th>
                <th>Statut</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="op in operators" :key="op.matricule">
                <td>
                  <code>{{ op.matricule }}</code>
                </td>
                <td>
                  <strong>{{ op.nom }}</strong>
                </td>
                <td>{{ op.dateEmbauche }}</td>
                <td>{{ op.formationRework ? '✅ Oui' : '❌ Non' }}</td>
                <td>
                  <span v-if="op.posteAffecte" class="role-badge">
                    {{ op.posteAffecte.nom }}
                  </span>
                  <span v-else class="empty-hint">— Aucun —</span>
                </td>
                <td>
                  <span :class="['status-badge', op.statut === 'Actif' ? 'active' : 'suspended']">
                    {{ op.statut }}
                  </span>
                </td>
              </tr>
              <tr v-if="operators.length === 0">
                <td colspan="6" style="text-align: center; color: #547174">
                  Aucun opérateur trouvé dans votre équipe.
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- SUB-VIEW: Create Operator Form -->
    <div
      v-else-if="activeSection === 'creer-operateur'"
      style="max-width: 600px; margin: 0 auto; width: 100%"
    >
      <div class="panel-card">
        <div class="panel-header">
          <h3>Créer un Opérateur</h3>
        </div>
        <form @submit.prevent="handleCreateOperator" class="panel-form">
          <div class="input-group">
            <label>Matricule</label>
            <input v-model="newOpMatricule" required placeholder="Ex: OP123" />
          </div>
          <div class="input-group">
            <label>Nom</label>
            <input v-model="newOpNom" required placeholder="Ex: Dupont" />
          </div>
          <div class="input-group">
            <label>Prénom</label>
            <input v-model="newOpPrenom" required placeholder="Ex: Jean" />
          </div>
          <div class="input-group">
            <label>Fonctionnalité</label>
            <input v-model="newOpFonctionnalite" placeholder="Ex: Assemblage" />
          </div>
          <div class="input-group">
            <label>Date d'embauche</label>
            <input v-model="newOpDateEmbauche" type="date" required />
          </div>
          <div class="input-group">
            <label>Premier poste d'affectation</label>
            <select v-model="newOpPosteId" required>
              <option value="" disabled>-- Choisir un poste --</option>
              <option v-for="poste in allPostes" :key="poste.id" :value="poste.id">
                {{ poste.nom }}
              </option>
            </select>
          </div>
          <p class="subtitle">
            Statut : <strong>NOUVELLE_RECRUE</strong>. Formation de 12 jours avant l'évaluation ;
            l'équipe est déduite du projet du poste.
          </p>
          <button type="submit" :disabled="createLoading" class="submit-btn">
            {{ createLoading ? 'Création...' : "Créer l'opérateur" }}
          </button>
          <p v-if="createMsg" class="form-msg">{{ createMsg }}</p>
        </form>
      </div>
    </div>

    <!-- SUB-VIEW: Saisir Suivi -->
    <div
      v-else-if="activeSection === 'saisir-suivi'"
      style="max-width: 600px; margin: 0 auto; width: 100%"
    >
      <div class="panel-card">
        <div class="panel-header">
          <h3>Saisir un Suivi d'Intégration Journalier</h3>
        </div>
        <p class="subtitle">
          Cette section affichera uniquement les suivis venant de la base MySQL dès que l'API dédiée
          sera branchée.
        </p>
      </div>
    </div>

    <!-- SUB-VIEW: Demande MAJ -->
    <div
      v-else-if="activeSection === 'demande-maj'"
      style="max-width: 600px; margin: 0 auto; width: 100%"
    >
      <div class="panel-card">
        <div class="panel-header">
          <h3>Nouvelle demande de Mise à Jour de l'Équipe</h3>
        </div>
        <p class="subtitle">
          Les demandes visibles ici seront limitées aux enregistrements stockés en base de données.
        </p>
      </div>
    </div>

    <!-- SUB-VIEW: Formations Management -->
    <ChefFormationPanel v-else-if="activeSection === 'formations'" />
  </section>
</template>
