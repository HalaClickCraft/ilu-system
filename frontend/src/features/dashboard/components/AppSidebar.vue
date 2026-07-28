<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const props = defineProps({
  activeSection: { type: String, required: true },
})
const emit = defineEmits(['update:activeSection'])

const router = useRouter()
const authStore = useAuthStore()
const user = computed(() => authStore.user)

const roleLabel = computed(() => {
  if (!user.value) return ''
  const labels = {
    ADMIN: 'Administrateur',
    CHEF_EQUIPE: "Chef d'équipe",
    RH: 'Ressources Humaines',
    QUALITE: 'Qualité',
    RESPONSABLE_QUALITE: 'Responsable Qualité',
    HSE: 'Responsable HSE',
    SUPERVISEUR: 'Superviseur',
  }
  return labels[user.value.role] || user.value.role
})

function setSection(section) {
  emit('update:activeSection', section)
}

function handleLogout() {
  authStore.logout()
  router.push({ name: 'login' })
}
</script>

<template>
  <aside class="app-sidebar">
    <div class="brand">
      <div class="opmobility-mark" aria-hidden="true">op</div>
      <div class="brand-text">
        <h3><span>OP</span>mobility</h3>
        <span class="brand-sub">ILU management system</span>
      </div>
    </div>

    <div class="user-profile">
      <div class="avatar">{{ user?.nom?.substring(0, 2).toUpperCase() }}</div>
      <div class="user-info">
        <span class="user-name">{{ user?.nom }}</span>
        <span class="user-role-pill">{{ roleLabel }}</span>
      </div>
    </div>

    <nav class="sidebar-nav">
      <button type="button" class="nav-item" :class="{ active: activeSection === 'dashboard' }" @click="setSection('dashboard')">
        <span class="nav-icon">📊</span><span>Dashboard</span>
      </button>

      <template v-if="user?.role === 'CHEF_EQUIPE'">
        <button type="button" class="nav-item" :class="{ active: activeSection === 'creer-operateur' }" @click="setSection('creer-operateur')">
          <span class="nav-icon">➕</span><span>Créer opérateur</span>
        </button>
        <button type="button" class="nav-item" :class="{ active: activeSection === 'saisir-suivi' }" @click="setSection('saisir-suivi')">
          <span class="nav-icon">📝</span><span>Saisir suivi</span>
        </button>
        <button type="button" class="nav-item" :class="{ active: activeSection === 'demande-maj' }" @click="setSection('demande-maj')">
          <span class="nav-icon">🔔</span><span>Demande MAJ</span>
        </button>
        <button type="button" class="nav-item" :class="{ active: activeSection === 'formations' }" @click="setSection('formations')">
          <span class="nav-icon">📚</span><span>Formations</span>
        </button>
      </template>

      <button
        v-if="['ADMIN', 'CHEF_EQUIPE', 'SUPERVISEUR'].includes(user?.role)"
        type="button"
        class="nav-item"
        :class="{ active: activeSection === 'structure' }"
        @click="setSection('structure')"
      >
        <span class="nav-icon">⚙️</span><span>Projet</span>
      </button>
    </nav>

    <div class="sidebar-footer">
      <button @click="handleLogout" class="logout-btn"><span>🚪</span> Se déconnecter</button>
    </div>
  </aside>
</template>