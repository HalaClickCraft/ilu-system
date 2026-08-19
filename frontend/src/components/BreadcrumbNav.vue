<template>
  <nav class="flex items-center gap-1 text-sm text-gray-500 mb-2">
    <router-link to="/" class="hover:text-emerald-600 transition">Accueil</router-link>
    <template v-for="(crumb, index) in crumbs" :key="index">
      <svg class="w-4 h-4 text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" /></svg>
      <router-link v-if="crumb.to" :to="crumb.to" class="hover:text-emerald-600 transition">{{ crumb.label }}</router-link>
      <span v-else class="text-gray-700 font-medium">{{ crumb.label }}</span>
    </template>
  </nav>
</template>
<script setup>
defineProps({ crumbs: { type: Array, default: () => [] } })
</script>
