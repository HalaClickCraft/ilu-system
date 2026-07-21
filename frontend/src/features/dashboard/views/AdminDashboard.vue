<script setup>
import { onMounted, ref } from 'vue'
import { useAuthStore } from '@/stores/auth'
import StatisticsDashboard from '@/features/formations/components/StatisticsDashboard.vue'

const authStore = useAuthStore()
const users = ref([])
const loading = ref(false)
const error = ref('')

const newUserMatricule = ref('')
const newUserNom = ref('')
const newUserCin = ref('')
const newUserRole = ref('CHEF_EQUIPE')
const newUserLoading = ref(false)
const newUserMsg = ref('')

// View state
const showFormations = ref(false)

async function loadUsers() {
  loading.value = true
  error.value = ''
  try {
    const response = await fetch('/api/utilisateurs', {
      headers: { Authorization: `Bearer ${authStore.token}` },
    })
    if (!response.ok) throw new Error('Impossible de charger les utilisateurs.')
    users.value = await response.json()
  } catch (err) {
    error.value = err.message
  } finally {
    loading.value = false
  }
}

async function handleCreateUser() {
  newUserMsg.value = ''
  newUserLoading.value = true
  try {
    const response = await fetch('/api/utilisateurs', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${authStore.token}`,
      },
      body: JSON.stringify({
        matricule: newUserMatricule.value,
        nom: newUserNom.value,
        cin: newUserCin.value,
        role: newUserRole.value,
      }),
    })
    if (!response.ok) {
      const data = await response.json().catch(() => ({}))
      throw new Error(data.message || "Erreur lors de la création de l'utilisateur")
    }
    newUserMatricule.value = ''
    newUserNom.value = ''
    newUserCin.value = ''
    newUserMsg.value = 'Utilisateur créé avec succès (mot de passe initial = CIN)!'
    await loadUsers()
  } catch (err) {
    newUserMsg.value = `Erreur: ${err.message}`
  } finally {
    newUserLoading.value = false
  }
}

async function toggleUserStatus(u) {
  const endpoint = u.actif
    ? `/api/utilisateurs/${u.id}/suspendre`
    : `/api/utilisateurs/${u.id}/reactiver`
  try {
    const response = await fetch(endpoint, {
      method: 'PUT',
      headers: {
        Authorization: `Bearer ${authStore.token}`,
      },
    })
    if (!response.ok) throw new Error('Impossible de modifier le statut')
    await loadUsers()
  } catch (err) {
    alert(err.message)
  }
}

onMounted(loadUsers)
</script>

<template>
  <section class="role-section">
    <!-- Navigation Buttons -->
    <div class="tabs-navigation" style="margin-bottom: 1.5rem">
      <button 
        @click="showFormations = false"
        :class="['tab-btn', !showFormations ? 'active' : '']"
      >
        👥 Gestion des Utilisateurs
      </button>
      <button 
        @click="showFormations = true"
        :class="['tab-btn', showFormations ? 'active' : '']"
      >
        📊 Statistiques Formations
      </button>
    </div>

    <!-- Users Tab -->
    <div v-show="!showFormations" class="admin-grid" style="margin-top: 1.5rem">
      <!-- Create User Form -->
      <div class="panel-card">
        <div class="panel-header">
          <h3>Ajouter un Utilisateur</h3>
        </div>
        <form @submit.prevent="handleCreateUser" class="panel-form">
          <div class="form-row">
            <div class="input-group">
              <label>Matricule</label>
              <input v-model="newUserMatricule" required placeholder="Ex: chef2" />
            </div>
            <div class="input-group">
              <label>Nom complet</label>
              <input v-model="newUserNom" required placeholder="Ex: Paul Martin" />
            </div>
          </div>
          <div class="form-row">
            <div class="input-group">
              <label>Numéro CIN</label>
              <input v-model="newUserCin" required placeholder="Ex: 09876543" />
            </div>
            <div class="input-group">
              <label>Rôle</label>
              <select v-model="newUserRole">
                <option value="ADMIN">ADMIN</option>
                <option value="CHEF_EQUIPE">CHEF_EQUIPE</option>
                <option value="RH">RH</option>
                <option value="QUALITE">QUALITE</option>
                <option value="RESPONSABLE_QUALITE">RESPONSABLE_QUALITE</option>
                <option value="HSE">HSE</option>
                <option value="SUPERVISEUR">SUPERVISEUR</option>
              </select>
            </div>
          </div>
          <button type="submit" :disabled="newUserLoading" class="submit-btn">
            {{ newUserLoading ? 'Création...' : "Créer l'utilisateur" }}
          </button>
          <p v-if="newUserMsg" class="form-msg">{{ newUserMsg }}</p>
        </form>
      </div>

      <!-- Users List -->
      <div class="panel-card list-users-card">
        <div class="panel-header">
          <h3>Liste des Utilisateurs</h3>
          <button @click="loadUsers" class="refresh-btn">🔄 Actualiser</button>
        </div>

        <div v-if="loading" class="loading-state">
          <span class="spinner-blue"></span> Chargement des utilisateurs...
        </div>

        <div v-else-if="error" class="error-state">⚠️ {{ error }}</div>

        <div v-else class="table-wrapper">
          <table class="data-table">
            <thead>
              <tr>
                <th>Matricule</th>
                <th>Nom</th>
                <th>Rôle</th>
                <th>Doit changer MDP</th>
                <th>Statut</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="u in users" :key="u.id">
                <td>
                  <code>{{ u.matricule }}</code>
                </td>
                <td>
                  <strong>{{ u.nom }}</strong>
                </td>
                <td>
                  <span class="role-badge">{{ u.role?.libelle }}</span>
                </td>
                <td>{{ u.doitChangerMdp ? 'Oui' : 'Non' }}</td>
                <td>
                  <span :class="['status-badge', u.actif ? 'active' : 'suspended']">
                    {{ u.actif ? 'Actif' : 'Suspendu' }}
                  </span>
                </td>
                <td>
                  <button
                    @click="toggleUserStatus(u)"
                    :class="['status-btn', u.actif ? 'btn-suspend' : 'btn-activate']"
                  >
                    {{ u.actif ? 'Suspendre' : 'Réactiver' }}
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
    <!-- End Users Tab -->

    <!-- Formations Statistics Tab -->
    <StatisticsDashboard v-show="showFormations" />
    <!-- End Formations Statistics Tab -->
  </section>
</template>
