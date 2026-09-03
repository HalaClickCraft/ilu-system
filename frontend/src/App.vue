<template>
  <router-view />
</template>
<script>
// Global: press Escape to close any open modal (triggers click on backdrop)
document.addEventListener('keydown', (e) => {
  if (e.key === 'Escape') {
    const modal = document.querySelector('.fixed.inset-0.bg-black/50')
    if (modal) modal.click()
  }
})
</script>

<script setup>
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
if (localStorage.getItem('token')) {
  authStore.restoreFromToken()
}
</script>
