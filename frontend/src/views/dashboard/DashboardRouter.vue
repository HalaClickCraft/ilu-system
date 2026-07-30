<template>
  <component :is="currentDashboard" />
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth.js'
import AdminDashboard from './AdminDashboard.vue'
import RhDashboard from './RhDashboard.vue'
import AgentQualiteDashboard from './AgentQualiteDashboard.vue'
import ResponsableHseDashboard from './ResponsableHseDashboard.vue'
import ResponsableQualiteDashboard from './ResponsableQualiteDashboard.vue'
import SupervisorDashboard from './SupervisorDashboard.vue'
import ChefEquipeDashboard from './ChefEquipeDashboard.vue'

const authStore = useAuthStore()

const dashboardMap = {
  ADMIN: AdminDashboard,
  RH: RhDashboard,
  AGENT_QUALITE: AgentQualiteDashboard,
  RESP_HSE: ResponsableHseDashboard,
  RESP_QUALITE: ResponsableQualiteDashboard,
  CHEF_EQUIPE: ChefEquipeDashboard,
  SUPERVISEUR: SupervisorDashboard,
}

const currentDashboard = computed(() => {
  const role = authStore.primaryRole
  return dashboardMap[role] || AdminDashboard
})

onMounted(() => {
  if (!authStore.user && authStore.isAuthenticated) {
    authStore.restoreFromToken()
  }
})
</script>