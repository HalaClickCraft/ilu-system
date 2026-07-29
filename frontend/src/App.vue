<template>
  <router-view />
</template>

<script setup>
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
if (localStorage.getItem('token')) {
  authStore.restoreFromToken()
}
</script>
