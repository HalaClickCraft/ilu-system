<script setup>
import { ref, onMounted, computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { fetchTeamOperators, fetchAllOperators, createOperator } from '@/features/dashboard/services/operateurService'
import { fetchStructure } from '@/features/structure/services/structureService'
import { initializeFormation } from '@/features/formations/services/formationService'
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

// "Créer un opérateur" : choix Nouveau / Existant
const operatorMode = ref('nouveau') // 'nouveau' | 'existant'
const allOperatorsList = ref([])
const existingSearchQuery = ref('')
const selectedExistingOperator = ref(null)
const existingProjetId = ref('')
const existingPosteId = ref('')
const addFormationLoading = ref(false)
const addFormationMsg = ref('')

const filteredExistingOperators = computed(() => {
  const q = existingSearchQuery.value.trim().toLowerCase()
  if (!q) return []
  return allOperatorsList.value
    .filter(
      (op) =>
        op.matricule.toLowerCase().includes(q) ||
        op.nom.toLowerCase().includes(q) ||
        (op.prenom || '').toLowerCase().includes(q)
    )
    .slice(0, 8)
})

const postesForExistingProjet = computed(() => {
  if (!existingProjetId.value) return []
  const projet = (structure.value.projects || []).find(
    (p) => String(p.idProjet) === String(existingProjetId.value)
  )
  if (!projet) return []
  const postes = []
  for (const zone of projet.zones || []) {
    for (const poste of zone.postes || []) {
      postes.push({ id: poste.idPoste, nom: `${zone.nom} — ${poste.nom}` })
    }
  }
  return postes
})

function selectExistingOperator(op) {
  selectedExistingOperator.value = op
  existingSearchQuery.value = `${op.matricule} - ${op.nom} ${op.prenom}`
  existingProjetId.value = ''
  existingPosteId.value = ''
  addFormationMsg.value = ''
}

function resetExistingOperatorFlow() {
  selectedExistingOperator.value = null
  existingSearchQuery.value = ''
  existingProjetId.value = ''
  existingPosteId.value = ''
  addFormationMsg.value = ''
}

async function handleAddFormationToExisting() {
  addFormationMsg.value = ''
  addFormationLoading.value = true
  try {
    await initializeFormation(authStore.token, {
      operateurMatricule: selectedExistingOperator.value.matricule,
      posteId: Number(existingPosteId.value),
      projetId: Number(existingProjetId.value),
    })
    addFormationMsg.value = `Formation ajoutée pour ${selectedExistingOperator.value.matricule} sur le nouveau poste.`
    existingProjetId.value = ''
    existingPosteId.value = ''
    await loadData()
  } catch (err) {
    addFormationMsg.value = `Erreur: ${err.message}`
  } finally {
    addFormationLoading.value = false
  }
}

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
    const [opsData, structureData, allOpsData] = await Promise.all([
      fetchTeamOperators(authStore.token),
      fetchStructure(authStore.token),
      fetchAllOperators(authStore.token),
    ])
    operators.value = opsData
    structure.value = structureData
    allOperatorsList.value = allOpsData
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

        <!-- Toggle Nouveau / Existant -->
        <div class="mode-toggle" style="display: flex; gap: 0.5rem; margin-bottom: 1.5rem">
          <button
            type="button"
            class="submit-btn"
            :style="operatorMode !== 'nouveau' ? 'opacity: 0.5' : ''"
            @click="operatorMode = 'nouveau'; resetExistingOperatorFlow()"
          >
            ➕ Nouvel opérateur
          </button>
          <button
            type="button"
            class="submit-btn"
            :style="operatorMode !== 'existant' ? 'opacity: 0.5' : ''"
            @click="operatorMode = 'existant'"
          >
            🔍 Opérateur existe déjà
          </button>
        </div>

        <!-- FORM: Nouvel opérateur (inchangé) -->
        <form v-if="operatorMode === 'nouveau'" @submit.prevent="handleCreateOperator" class="panel-form">
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

        <!-- FORM: Opérateur existe déjà -->
        <div v-else class="panel-form">
          <div class="input-group">
            <label>Rechercher (matricule ou nom)</label>
            <input
              v-model="existingSearchQuery"
              placeholder="Ex: OP001 ou Mohamed"
              @input="selectedExistingOperator = null"
            />
          </div>

          <!-- Résultats de recherche -->
          <ul
            v-if="!selectedExistingOperator && filteredExistingOperators.length"
            style="list-style: none; padding: 0; margin: 0 0 1rem 0; border: 1px solid #e2e8f0; border-radius: 6px; overflow: hidden"
          >
            <li
              v-for="op in filteredExistingOperators"
              :key="op.matricule"
              style="padding: 0.6rem 0.9rem; cursor: pointer; border-bottom: 1px solid #f1f5f9"
              @click="selectExistingOperator(op)"
            >
              <code>{{ op.matricule }}</code> — <strong>{{ op.nom }} {{ op.prenom }}</strong>
              <span v-if="op.posteAffecte" class="empty-hint"> · actuellement sur {{ op.posteAffecte.nom }}</span>
            </li>
          </ul>
          <p v-else-if="existingSearchQuery && !selectedExistingOperator" class="empty-hint">
            Aucun opérateur trouvé.
          </p>

          <!-- Une fois l'opérateur choisi : juste poste + projet -->
          <template v-if="selectedExistingOperator">
            <p class="subtitle">
              Ajout d'une formation pour
              <strong>{{ selectedExistingOperator.matricule }} - {{ selectedExistingOperator.nom }} {{ selectedExistingOperator.prenom }}</strong>
              <button type="button" style="margin-left: 0.5rem" @click="resetExistingOperatorFlow" class="refresh-btn">
                ✕ changer
              </button>
            </p>
            <div class="input-group">
              <label>Projet</label>
              <select v-model="existingProjetId" @change="existingPosteId = ''" required>
                <option value="" disabled>-- Choisir un projet --</option>
                <option
                  v-for="projet in structure.projects"
                  :key="projet.idProjet"
                  :value="projet.idProjet"
                >
                  {{ projet.nom }}
                </option>
              </select>
            </div>
            <div class="input-group">
              <label>Poste</label>
              <select v-model="existingPosteId" :disabled="!existingProjetId" required>
                <option value="" disabled>-- Choisir un poste --</option>
                <option v-for="poste in postesForExistingProjet" :key="poste.id" :value="poste.id">
                  {{ poste.nom }}
                </option>
              </select>
            </div>
            <button
              type="button"
              :disabled="addFormationLoading || !existingProjetId || !existingPosteId"
              class="submit-btn"
              @click="handleAddFormationToExisting"
            >
              {{ addFormationLoading ? 'Ajout...' : 'Ajouter la formation' }}
            </button>
            <p v-if="addFormationMsg" class="form-msg">{{ addFormationMsg }}</p>
          </template>
        </div>
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