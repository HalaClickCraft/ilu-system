<script setup>
import { ref, computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import PasswordChangeGate from '@/features/dashboard/components/PasswordChangeGate.vue'
import AppSidebar from '@/features/dashboard/components/AppSidebar.vue'
import StructureManager from '@/features/dashboard/components/StructureManager.vue'
import AdminDashboard from '@/features/dashboard/views/AdminDashboard.vue'
import ChefEquipeDashboard from '@/features/dashboard/views/ChefEquipeDashboard.vue'
import RhDashboard from '@/features/dashboard/views/RhDashboard.vue'
import QualiteDashboard from '@/features/dashboard/views/QualiteDashboard.vue'
import HseDashboard from '@/features/dashboard/views/HseDashboard.vue'
import SuperviseurDashboard from '@/features/dashboard/views/SuperviseurDashboard.vue'
import ResponsableQualiteDashboard from '@/features/dashboard/views/ResponsableQualiteDashboard.vue'

const authStore = useAuthStore()
const activeSection = ref('dashboard')
const user = computed(() => authStore.user)

const roleComponent = computed(() => {
  if (['creer-operateur', 'saisir-suivi', 'demande-maj', 'formations'].includes(activeSection.value)) {
    return ChefEquipeDashboard
  }
  return ({
    ADMIN: AdminDashboard,
    CHEF_EQUIPE: ChefEquipeDashboard,
    RH: RhDashboard,
    QUALITE: QualiteDashboard,
    AGENT_QUALITE: QualiteDashboard,
    RESPONSABLE_QUALITE: ResponsableQualiteDashboard,
    HSE: HseDashboard,
    SUPERVISEUR: SuperviseurDashboard,
  })[user.value?.role]
})
</script>

<template>
  <div class="dashboard-container">
    <PasswordChangeGate v-if="authStore.mustChangePassword" />

    <div v-else class="app-layout">
      <AppSidebar v-model:active-section="activeSection" />

      <main class="app-main">
        <header class="main-header">
          <div class="header-title">
            <h1>Tableau de bord</h1>
            <p>Bienvenue, {{ user?.nom }} (Matricule: {{ user?.matricule }})</p>
          </div>
          <div class="header-date">
            📅
            {{ new Date().toLocaleDateString('fr-FR', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' }) }}
          </div>
        </header>

        <StructureManager v-if="activeSection === 'structure'" />
        <component v-else-if="roleComponent" :is="roleComponent" :active-section="activeSection" />
      </main>
    </div>
  </div>
</template>

<style scoped>
/* Styles come from dashboard-shared.css */
</style>