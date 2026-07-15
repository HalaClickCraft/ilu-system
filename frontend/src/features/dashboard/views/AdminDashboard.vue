<script setup>
import { onMounted, ref } from 'vue'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const users = ref([])
const loading = ref(false)
const error = ref('')

async function loadUsers() {
  loading.value = true
  error.value = ''
  try {
    const response = await fetch('/api/utilisateurs', { headers: { Authorization: `Bearer ${authStore.token}` } })
    if (!response.ok) throw new Error('Impossible de charger les utilisateurs.')
    users.value = await response.json()
  } catch (err) { error.value = err.message } finally { loading.value = false }
}
onMounted(loadUsers)
</script>

<template>
  <section class="role-section">
    <div class="stats-grid"><div class="stat-card"><div><span class="stat-val">{{ users.length }}</span><span class="stat-lbl">Utilisateurs enregistrés</span></div></div></div>
    <div class="panel-card"><div class="panel-header"><h3>Utilisateurs</h3><button class="refresh-btn" @click="loadUsers">Actualiser</button></div><p v-if="loading">Chargement…</p><p v-else-if="error" class="error-state">{{ error }}</p><div v-else class="table-wrapper"><table class="data-table"><thead><tr><th>Matricule</th><th>Nom</th><th>Rôle</th><th>Statut</th></tr></thead><tbody><tr v-for="u in users" :key="u.id"><td>{{ u.matricule }}</td><td>{{ u.nom }}</td><td>{{ u.role?.libelle }}</td><td>{{ u.actif ? 'Actif' : 'Suspendu' }}</td></tr></tbody></table></div></div>
  </section>
</template>
